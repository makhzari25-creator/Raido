import { db, json, preflight, readJson, randomCode, sha256Hex, safeEqual, PLANS } from "../_shared/util.ts";

const ADMIN_KEY = Deno.env.get("ADMIN_KEY") ?? "";

Deno.serve(async (req) => {
  const pre = preflight(req); if (pre) return pre;
  const key = req.headers.get("x-admin-key") ?? "";
  if (!ADMIN_KEY || ADMIN_KEY.length < 12 || !safeEqual(key, ADMIN_KEY)) return json({ error: "forbidden" }, 403);
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
    const q = String(body.q ?? "").replace(/\D/g, "");
    let query = db.from("users").select("id, phone, created_at, last_seen, app_version, device_model, subscriptions(plan,is_premium,expires_at,trial_used)")
      .order("created_at", { ascending: false }).limit(50);
    if (q) query = query.like("phone", `%${q}%`);
    const { data, error } = await query;
    if (error) return json({ error: "server" }, 500);
    return json({ users: data });
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

  return json({ error: "unknown_action" }, 400);
});
