import { authenticate, db, deny, json, preflight, readJson, sha256Hex } from "../_shared/util.ts";

const NESHAN_KEY = Deno.env.get("NESHAN_KEY") ?? "";
const CACHE_TTL_MS = 30 * 24 * 3600 * 1000;
const DAILY_LIMIT = 1500; // فقط درخواست‌های کش‌نخورده (مصرف واقعی کلید) شمرده می‌شود
const MAX_BATCH = 4;

// حافظهٔ داخلی همین isolate (تا وقتی تابع «داغ» است): هر دسترسی به دیتابیس از ایران یک رفت‌وبرگشت طولانی تا سئول است.
const AUTH_TTL_MS = 20_000;   // نشست/مسدودی با تأخیر حداکثر ۲۰ ثانیه اثر می‌کند
const PREM_TTL_MS = 30_000;
const MEM_TTL_MS = 6 * 3600 * 1000;
const authMem = new Map<string, { a: any; t: number }>();
const premMem = new Map<string, { ok: boolean; t: number }>();
const geoMem = new Map<string, { x: number; y: number; t: number }>();

function gc() {
  if (authMem.size > 500) authMem.clear();
  if (premMem.size > 500) premMem.clear();
  if (geoMem.size > 3000) geoMem.clear();
}

/** نرمال‌سازی آدرس: ی/ک عربی، ارقام فارسی/عربی، نیم‌فاصله، فاصله‌های اضافه. باید با اپ یکی باشد. */
function norm(s: string): string {
  return s
    .replace(/[يى]/g, "ی").replace(/ك/g, "ک")
    .replace(/[۰-۹]/g, (d) => String(d.charCodeAt(0) - 0x06F0))
    .replace(/[٠-٩]/g, (d) => String(d.charCodeAt(0) - 0x0660))
    .replace(/[‌‍‏‎ـ]/g, " ")
    .replace(/\s+/g, " ").trim().toLowerCase();
}

type Item = { status?: string; location?: { x: number; y: number } | null; cached?: boolean; error?: string; http?: number };

async function neshan(address: string): Promise<{ http: number; j: any } | null> {
  const ac = new AbortController();
  const timer = setTimeout(() => ac.abort(), 4500);
  try {
    const r = await fetch("https://api.neshan.org/v4/geocoding?address=" + encodeURIComponent(address), {
      headers: { "Api-Key": NESHAN_KEY },
      signal: ac.signal,
    });
    return { http: r.status, j: r.ok ? await r.json() : null };
  } catch {
    return null;
  } finally {
    clearTimeout(timer);
  }
}

// پروکسی ژئوکد نشان: کلید فقط سمت سرور است و فقط کاربر پرمیوم فعال و غیرمسدود مجاز است.
// ورودی: {address} (قدیمی) یا {addresses:[...]} (تا ۴ آدرس در یک درخواست). خروجی همیشه شامل results[] است.
Deno.serve(async (req) => {
  const t0 = performance.now();
  const pre = preflight(req); if (pre) return pre;
  if (!NESHAN_KEY) return json({ error: "not_configured" }, 503);

  const body = await readJson(req);
  const batch = Array.isArray(body.addresses);
  const raw: string[] = batch ? body.addresses.slice(0, MAX_BATCH).map((x: unknown) => String(x ?? "").trim())
    : [String(body.address ?? "").trim()];
  if (!batch && (raw[0].length < 4 || raw[0].length > 200)) return json({ error: "bad_address" }, 400);
  if (raw.length === 0) return json({ error: "bad_address" }, 400);

  const valid = raw.map((a) => a.length >= 4 && a.length <= 200);
  const keys = await Promise.all(raw.map((a, i) => valid[i] ? sha256Hex(norm(a)) : Promise.resolve("")));
  const now = Date.now();

  // نتیجه‌ها از حافظهٔ داخلی؛ فقط آدرس‌های بدون حافظه به دیتابیس می‌روند
  const items: Item[] = raw.map(() => ({}));
  const missIdx: number[] = [];
  for (let i = 0; i < raw.length; i++) {
    if (!valid[i]) { items[i] = { error: "bad_address" }; continue; }
    const m = geoMem.get(keys[i]);
    if (m && now - m.t < MEM_TTL_MS) items[i] = { status: "OK", location: { x: m.x, y: m.y }, cached: true };
    else missIdx.push(i);
  }

  // احراز هویت (حافظه یا دیتابیس) و جست‌وجوی کش دیتابیس هم‌زمان
  const token = req.headers.get("x-raido-token") ?? "";
  const am = authMem.get(token);
  const authCached = am && now - am.t < AUTH_TTL_MS ? am.a : null;
  const [auth, cacheRes] = await Promise.all([
    authCached ? Promise.resolve(authCached) : authenticate(req),
    missIdx.length
      ? db.from("geocode_cache").select("k, x, y, created_at").in("k", missIdx.map((i) => keys[i]))
      : Promise.resolve({ data: [] as any[] }),
  ]);
  if (!auth) { authMem.delete(token); return await deny(req); }
  if (!authCached) { authMem.set(token, { a: auth, t: now }); gc(); }
  const t1 = performance.now();

  let premOk: boolean;
  const pm = premMem.get(auth.userId);
  if (pm && now - pm.t < PREM_TTL_MS) premOk = pm.ok;
  else {
    const { data: s } = await db.from("subscriptions").select("is_premium, expires_at").eq("user_id", auth.userId).maybeSingle();
    premOk = !!s?.is_premium && !!s?.expires_at && new Date(s.expires_at).getTime() > Date.now();
    premMem.set(auth.userId, { ok: premOk, t: Date.now() });
  }
  if (!premOk) return json({ error: "premium_required" }, 403);
  const t2 = performance.now();

  const rows: any[] = (cacheRes as any)?.data ?? [];
  const byKey = new Map<string, any>(rows.map((r) => [r.k, r]));
  const needNet: number[] = [];
  for (const i of missIdx) {
    const hit = byKey.get(keys[i]);
    if (hit && Date.now() - new Date(hit.created_at).getTime() < CACHE_TTL_MS) {
      items[i] = { status: "OK", location: { x: hit.x, y: hit.y }, cached: true };
      geoMem.set(keys[i], { x: hit.x, y: hit.y, t: Date.now() });
    } else needNet.push(i);
  }

  // کش‌نخورده‌ها: شمارنده و تماس با نشان هم‌زمان (برای همهٔ آدرس‌ها با هم)
  if (needNet.length) {
    const day = new Date(Date.now() + 3.5 * 3600_000).toISOString().slice(0, 10);
    await Promise.all(needNet.map(async (i) => {
      const hitP = db.rpc("geocode_hit", { p_user: auth.userId, p_day: day });
      const fetchP = neshan(raw[i]);
      const { data: n } = await hitP;
      if (typeof n === "number" && n > DAILY_LIMIT) { items[i] = { error: "limit", http: 429 }; return; }
      const res = await fetchP;
      if (!res || res.http !== 200 || !res.j) { items[i] = { error: "upstream", http: res?.http ?? 0 }; return; }
      const j = res.j;
      const loc = j?.location;
      items[i] = { status: j?.status ?? "", location: loc ?? null, cached: false };
      if (String(j?.status ?? "").toUpperCase() === "OK" && loc && typeof loc.x === "number" && typeof loc.y === "number") {
        geoMem.set(keys[i], { x: loc.x, y: loc.y, t: Date.now() });
        const p = db.from("geocode_cache").upsert({ k: keys[i], x: loc.x, y: loc.y, created_at: new Date().toISOString() }).then(() => {}, () => {});
        try { (globalThis as any).EdgeRuntime?.waitUntil?.(p); } catch { /* ignore */ }
      }
    }));
  }

  const t3 = performance.now();
  const ms = { auth: Math.round(t1 - t0), sub: Math.round(t2 - t1), neshan: Math.round(t3 - t2), total: Math.round(t3 - t0) };
  console.log(JSON.stringify({ fn: "geocode", n: raw.length, net: needNet.length, authMem: !!authCached, ms }));
  const first = items[0] ?? {};
  // سازگاری با اپ‌های قدیمی (تک‌آدرس): فیلدهای سطح بالا = نتیجهٔ اول
  if (!batch) {
    if (first.error === "limit") return json({ error: "limit", ms }, 429);
    if (first.error === "upstream") return json({ error: "upstream", http: first.http ?? 0, ms }, 502);
  }
  return json({ status: first.status ?? "", location: first.location ?? null, cached: !!first.cached, results: items, ms });
});
