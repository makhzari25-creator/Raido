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

  if (action === "set_setting") {
    const key = String(body.key ?? "");
    let value = String(body.value ?? "").trim();
    if (key !== "tutorial_url") return json({ error: "bad_request" }, 400);
    if (value && !/^https:\/\/[^\s]{4,500}$/.test(value)) return json({ error: "bad_url" }, 400);
    value = value.slice(0, 500);
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
