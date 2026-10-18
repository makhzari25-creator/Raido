import { authenticate, db, deny, json, preflight, readJson, sha256Hex } from "../_shared/util.ts";

const NESHAN_KEY = Deno.env.get("NESHAN_KEY") ?? "";
const CACHE_TTL_MS = 30 * 24 * 3600 * 1000;
const DAILY_LIMIT = 1500; // فقط درخواست‌های کش‌نخورده (مصرف واقعی کلید) شمرده می‌شود

/** نرمال‌سازی آدرس: ی/ک عربی، ارقام فارسی/عربی، نیم‌فاصله، فاصله‌های اضافه. باید با اپ یکی باشد. */
function norm(s: string): string {
  return s
    .replace(/[يى]/g, "ی").replace(/ك/g, "ک")
    .replace(/[۰-۹]/g, (d) => String(d.charCodeAt(0) - 0x06F0))
    .replace(/[٠-٩]/g, (d) => String(d.charCodeAt(0) - 0x0660))
    .replace(/[‌‍‏‎ـ]/g, " ")
    .replace(/\s+/g, " ").trim().toLowerCase();
}

// پروکسی ژئوکد نشان: کلید فقط سمت سرور است و فقط کاربر پرمیوم فعال و غیرمسدود مجاز است.
Deno.serve(async (req) => {
  const t0 = performance.now();
  const pre = preflight(req); if (pre) return pre;
  if (!NESHAN_KEY) return json({ error: "not_configured" }, 503);

  const body = await readJson(req);
  const address = String(body.address ?? "").trim();
  if (address.length < 4 || address.length > 200) return json({ error: "bad_address" }, 400);
  const key = await sha256Hex(norm(address));

  // احراز هویت و جست‌وجوی کش هم‌زمان
  const [auth, cacheRes] = await Promise.all([
    authenticate(req),
    db.from("geocode_cache").select("x, y, created_at").eq("k", key).maybeSingle(),
  ]);
  if (!auth) return await deny(req);
  const t1 = performance.now();

  const { data: s } = await db.from("subscriptions").select("is_premium, expires_at").eq("user_id", auth.userId).maybeSingle();
  const active = !!s?.is_premium && !!s?.expires_at && new Date(s.expires_at).getTime() > Date.now();
  if (!active) return json({ error: "premium_required" }, 403);
  const t2 = performance.now();

  const hit: any = cacheRes?.data;
  if (hit && Date.now() - new Date(hit.created_at).getTime() < CACHE_TTL_MS) {
    const ms = { auth: Math.round(t1 - t0), sub: Math.round(t2 - t1), neshan: 0, total: Math.round(performance.now() - t0) };
    console.log(JSON.stringify({ fn: "geocode", cached: true, ms }));
    return json({ status: "OK", location: { x: hit.x, y: hit.y }, cached: true, ms });
  }

  // کش‌نخورده: شمارنده و تماس با نشان هم‌زمان؛ اگر سقف پر بود نتیجه دور ریخته می‌شود
  const day = new Date(Date.now() + 3.5 * 3600_000).toISOString().slice(0, 10);
  const ac = new AbortController();
  const timer = setTimeout(() => ac.abort(), 4500);
  const hitP = db.rpc("geocode_hit", { p_user: auth.userId, p_day: day });
  const fetchP = fetch("https://api.neshan.org/v4/geocoding?address=" + encodeURIComponent(address), {
    headers: { "Api-Key": NESHAN_KEY },
    signal: ac.signal,
  }).then(async (r) => ({ r, j: r.ok ? await r.json() : null })).catch(() => null);

  const { data: n } = await hitP;
  if (typeof n === "number" && n > DAILY_LIMIT) { ac.abort(); clearTimeout(timer); return json({ error: "limit" }, 429); }

  const res = await fetchP;
  clearTimeout(timer);
  const t3 = performance.now();
  const ms = { auth: Math.round(t1 - t0), sub: Math.round(t2 - t1), neshan: Math.round(t3 - t2), total: Math.round(t3 - t0) };
  console.log(JSON.stringify({ fn: "geocode", cached: false, http: res?.r.status ?? 0, ms }));
  if (!res || !res.r.ok) return json({ error: "upstream", http: res?.r.status ?? 0, ms }, 502);

  const j = res.j;
  const loc = j?.location;
  if (String(j?.status ?? "").toUpperCase() === "OK" && loc && typeof loc.x === "number" && typeof loc.y === "number") {
    const p = db.from("geocode_cache").upsert({ k: key, x: loc.x, y: loc.y, created_at: new Date().toISOString() }).then(() => {}, () => {});
    try { (globalThis as any).EdgeRuntime?.waitUntil?.(p); } catch { /* ignore */ }
  }
  return json({ status: j?.status ?? "", location: loc ?? null, cached: false, ms });
});
