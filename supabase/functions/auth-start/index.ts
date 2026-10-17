import { db, json, preflight, readJson, randomCode } from "../_shared/util.ts";

const BOT = Deno.env.get("TELEGRAM_BOT_USERNAME") ?? "TripAnalyzerbot";

Deno.serve(async (req) => {
  const pre = preflight(req); if (pre) return pre;
  const body = await readJson(req);
  const fp = String(body.device_fp ?? "");
  if (!/^[A-Za-z0-9]{8,64}$/.test(fp)) return json({ error: "bad_request" }, 400);

  // ضد سوءاستفاده: حداکثر ۱۰ درخواست ورود در ساعت برای هر دستگاه
  const since = new Date(Date.now() - 3600_000).toISOString();
  const { count } = await db.from("login_sessions").select("code", { count: "exact", head: true })
    .eq("device_fp", fp).gte("created_at", since);
  if ((count ?? 0) >= 10) return json({ error: "rate_limited" }, 429);

  // پاکسازی ردیف‌های قدیمی
  await db.from("login_sessions").delete().lt("expires_at", new Date(Date.now() - 86400_000).toISOString());

  const code = randomCode(16);
  const { error } = await db.from("login_sessions").insert({ code, device_fp: fp });
  if (error) return json({ error: "server" }, 500);
  return json({ code, bot_url: `https://t.me/${BOT}?start=${code}`, expires_in: 600 });
});
