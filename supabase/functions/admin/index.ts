import { db, json, preflight, readJson, randomCode, sha256Hex, safeEqual, PLANS } from "../_shared/util.ts";

const ADMIN_KEY = Deno.env.get("ADMIN_KEY") ?? "";

const MAX_FAILS = 8;           // تلاش ناموفق مجاز
const WINDOW_MS = 15 * 60_000; // در این بازه (۱۵ دقیقه)

function clientIp(req: Request): string {
  const f = req.headers.get("x-forwarded-for") ?? req.headers.get("cf-connecting-ip") ?? "";
  return (f.split(",")[0] || "unknown").trim().slice(0, 64) || "unknown";
}

Deno.serve(async (req) => {
  const pre = preflight(req); if (pre) return pre;
  const ip = clientIp(req);
  const since = new Date(Date.now() - WINDOW_MS).toISOString();
  const { count: fails } = await db.from("admin_fail").select("id", { count: "exact", head: true }).eq("ip", ip).gt("at", since);
  if ((fails ?? 0) >= MAX_FAILS) return json({ error: "locked" }, 429);

  const key = req.headers.get("x-admin-key") ?? "";
  if (!ADMIN_KEY || ADMIN_KEY.length < 12 || !safeEqual(key, ADMIN_KEY)) {
    await db.from("admin_fail").insert({ ip });
    return json({ error: "forbidden" }, 403);
  }
  const body = await readJson(req);
  const action = String(body.action ?? "");

  if (action === "stats") {
    const { data, error } = await db.rpc("admin_stats");
    if (error) return json({ error: "server" }, 500);
    return json(data);
  }

  if (action === "create_codes") {
    const plan = String(body.plan ?? "");
    const p = PLANS[plan];
    if (!p) return json({ error: "bad_plan" }, 400);
    const count = Math.min(Math.max(parseInt(body.count) || 1, 1), 50);
    const validDays = Math.min(Math.max(parseInt(body.valid_days) || 30, 1), 365);
    const note = typeof body.note === "string" ? body.note.slice(0, 100) : null;
    const codes: string[] = [];
    const rows: Record<string, unknown>[] = [];
    for (let i = 0; i < count; i++) {
      const c = randomCode(12);
      codes.push(`${c.slice(0, 4)}-${c.slice(4, 8)}-${c.slice(8)}`);
      rows.push({
        code_hash: await sha256Hex(c), plan, days: p.days, is_premium: p.premium, note,
        valid_until: new Date(Date.now() + validDays * 86400_000).toISOString(),
      });
    }
    const { error } = await db.from("activation_codes").insert(rows);
    if (error) return json({ error: "server" }, 500);
    return json({ plan, codes });
  }

  if (action === "users") {
    const qraw = String(body.q ?? "").trim().toLowerCase();
    const q = qraw.includes("@") ? "" : qraw.replace(/\D/g, "");
    let query = db.from("users").select("id, phone, email, phone_verified, signup_via, last_via, created_at, last_seen, app_version, device_model, banned, ban_reason, subscriptions(plan,is_premium,expires_at,trial_used)")
      .order("created_at", { ascending: false }).limit(50);
    if (q) query = query.or(`phone.like.%${q}%,contact_phone.like.%${q}%`);
    else if (qraw.includes("@") && /^[a-z0-9@._+-]+$/.test(qraw)) query = query.like("email", `%${qraw}%`);
    const { data, error } = await query;
    if (error) return json({ error: "server" }, 500);
    return json({ users: data });
  }

  if (action === "get_settings") {
    const { data } = await db.from("app_settings").select("key, value");
    return json({ settings: Object.fromEntries((data ?? []).map((r: { key: string; value: string }) => [r.key, r.value])) });
  }

  if (action === "get_dep") {
    const { data } = await db.from("app_settings").select("key, value").in("key", ["dep_table", "dep_version"]);
    const m: Record<string, string> = Object.fromEntries((data ?? []).map((r: { key: string; value: string }) => [r.key, r.value]));
    return json({ dep_table: m.dep_table ?? "", dep_version: m.dep_version ?? "" });
  }

  if (action === "set_dep") {
    const raw = String(body.dep_table ?? "");
    if (raw.length < 50 || raw.length > 300_000) return json({ error: "bad_table" }, 400);
    let t: { families?: unknown; data?: Record<string, unknown> };
    try { t = JSON.parse(raw); } catch { return json({ error: "bad_json" }, 400); }
    const fams = t.families, dat = t.data;
    if (!Array.isArray(fams) || fams.length < 1 || fams.length > 300 || fams.some((f) => typeof f !== "string")) return json({ error: "bad_families" }, 400);
    if (!dat || typeof dat !== "object" || !Array.isArray((dat as Record<string, unknown>)["سایر"])) return json({ error: "need_other" }, 400);
    for (const [k, rows] of Object.entries(dat)) {
      if (!Array.isArray(rows) || rows.length < 1) return json({ error: "bad_rows", family: k }, 400);
      for (const r of rows as unknown[]) {
        if (!Array.isArray(r) || r.length !== 3 || r.some((x) => typeof x !== "number" || !isFinite(x) || x < 0 || x > 5_000_000)) return json({ error: "bad_row", family: k }, 400);
      }
    }
    for (const f of fams as string[]) if (!Array.isArray(dat[f])) return json({ error: "family_without_data", family: f }, 400);
    const version = new Date().toISOString();
    const { error } = await db.from("app_settings").upsert([
      { key: "dep_table", value: JSON.stringify(t), updated_at: version },
      { key: "dep_version", value: version, updated_at: version },
    ]);
    if (error) return json({ error: "server" }, 500);
    return json({ ok: true, dep_version: version });
  }

  if (action === "set_setting") {
    const key = String(body.key ?? "");
    let value = String(body.value ?? "").trim();
    const https = /^https:\/\/[^\s]{4,500}$/;
    const str = (x: unknown, max: number) => typeof x === "string" && x.length <= max;
    if (key === "tutorial_url") {
      if (value && !https.test(value)) return json({ error: "bad_url" }, 400);
    } else if (key === "support_links") {
      let a: unknown; try { a = JSON.parse(value); } catch { return json({ error: "bad_json" }, 400); }
      if (!Array.isArray(a) || a.length > 10) return json({ error: "bad_links" }, 400);
      for (const l of a as Record<string, unknown>[]) {
        if (!l || !str(l.title, 60) || !(l.title as string).trim() || !str(l.url, 500) || !https.test(l.url as string) || (l.desc != null && !str(l.desc, 200))) return json({ error: "bad_link_item" }, 400);
      }
    } else if (key === "guide") {
      let a: unknown; try { a = JSON.parse(value); } catch { return json({ error: "bad_json" }, 400); }
      if (!Array.isArray(a) || a.length > 40) return json({ error: "bad_guide" }, 400);
      for (const g of a as Record<string, unknown>[]) {
        if (!g || !str(g.title, 120) || !(g.title as string).trim() || !str(g.body, 6000)) return json({ error: "bad_guide_item" }, 400);
      }
    } else if (key === "plans") {
      let a: unknown; try { a = JSON.parse(value); } catch { return json({ error: "bad_json" }, 400); }
      if (!Array.isArray(a) || a.length > 10) return json({ error: "bad_plans" }, 400);
      for (const p of a as Record<string, unknown>[]) {
        if (!p || !PLANS[String(p.code)] || typeof p.price !== "number" || !isFinite(p.price) || p.price < 0 || p.price > 1_000_000_000 || !str(p.title, 60) || !str(p.desc ?? "", 200)) return json({ error: "bad_plan_item" }, 400);
      }
    } else return json({ error: "bad_request" }, 400);
    value = value.slice(0, 60000);
    const { error } = await db.from("app_settings").upsert({ key, value, updated_at: new Date().toISOString() });
    if (error) return json({ error: "server" }, 500);
    return json({ ok: true });
  }

  if (action === "grant") {
    const plan = String(body.plan ?? "");
    const p = PLANS[plan];
    const phone = String(body.phone ?? "");
    if (!p || !/^09\d{9}$/.test(phone)) return json({ error: "bad_request" }, 400);
    const { data: u } = await db.from("users").select("id").eq("phone", phone).maybeSingle();
    if (!u) return json({ error: "user_not_found" }, 404);
    const { error } = await db.rpc("apply_plan", { p_user: u.id, p_plan: plan, p_days: p.days, p_premium: p.premium });
    if (error) return json({ error: "server" }, 500);
    return json({ ok: true });
  }

  if (action === "ban" || action === "unban" || action === "revoke") {
    const phone = String(body.phone ?? "");
    if (!/^09\d{9}$/.test(phone)) return json({ error: "bad_request" }, 400);
    const { data: u } = await db.from("users").select("id").eq("phone", phone).maybeSingle();
    if (!u) return json({ error: "user_not_found" }, 404);
    if (action === "revoke") {
      const { error } = await db.rpc("revoke_plan", { p_user: u.id });
      if (error) return json({ error: "server" }, 500);
    } else {
      const reason = typeof body.reason === "string" ? body.reason.slice(0, 200) : "";
      const { error } = await db.rpc("set_ban", { p_user: u.id, p_banned: action === "ban", p_reason: reason });
      if (error) return json({ error: "server" }, 500);
    }
    return json({ ok: true });
  }

  return json({ error: "unknown_action" }, 400);
});
