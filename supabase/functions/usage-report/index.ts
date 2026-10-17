import { authenticate, db, json, preflight, readJson } from "../_shared/util.ts";

Deno.serve(async (req) => {
  const pre = preflight(req); if (pre) return pre;
  const auth = await authenticate(req);
  if (!auth) return json({ error: "unauthorized" }, 401);
  const body = await readJson(req);
  const version = typeof body.app_version === "string" ? body.app_version.slice(0, 20) : null;
  const days = Array.isArray(body.days) ? body.days.slice(0, 40) : [];
  let n = 0;
  for (const d of days) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(String(d?.day))) continue;
    const a = Math.min(Math.max(parseInt(d.analyses) || 0, 0), 100000);
    const c = Math.min(Math.max(parseInt(d.accepted) || 0, 0), 100000);
    const { error } = await db.rpc("put_usage", { p_user: auth.userId, p_day: d.day, p_analyses: a, p_accepted: c, p_version: version });
    if (!error) n++;
  }
  await db.from("users").update({ last_seen: new Date().toISOString(), ...(version ? { app_version: version } : {}) }).eq("id", auth.userId);
  return json({ ok: true, saved: n });
});
