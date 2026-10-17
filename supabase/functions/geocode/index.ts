import { authenticate, db, deny, json, preflight, readJson } from "../_shared/util.ts";

const NESHAN_KEY = Deno.env.get("NESHAN_KEY") ?? "";

// پروکسی ژئوکد نشان: کلید فقط سمت سرور است و فقط کاربر پرمیوم فعال و غیرمسدود مجاز است.
Deno.serve(async (req) => {
  const pre = preflight(req); if (pre) return pre;
  const auth = await authenticate(req);
  if (!auth) return await deny(req);
  if (!NESHAN_KEY) return json({ error: "not_configured" }, 503);

  const body = await readJson(req);
  const address = String(body.address ?? "").trim();
  if (address.length < 4 || address.length > 200) return json({ error: "bad_address" }, 400);

  const { data: s } = await db.from("subscriptions").select("is_premium, expires_at").eq("user_id", auth.userId).maybeSingle();
  const active = !!s?.is_premium && !!s?.expires_at && new Date(s.expires_at).getTime() > Date.now();
  if (!active) return json({ error: "premium_required" }, 403);

  // سقف مصرف روزانه برای هر کاربر (جلوگیری از سوءاستفاده از کلید)
  const day = new Date(Date.now() + 3.5 * 3600_000).toISOString().slice(0, 10);
  const { data: n } = await db.rpc("geocode_hit", { p_user: auth.userId, p_day: day });
  if (typeof n === "number" && n > 1500) return json({ error: "limit" }, 429);

  try {
    const r = await fetch("https://api.neshan.org/v4/geocoding?address=" + encodeURIComponent(address), {
      headers: { "Api-Key": NESHAN_KEY },
      signal: AbortSignal.timeout(6000),
    });
    if (!r.ok) return json({ error: "upstream", http: r.status }, 502);
    const j = await r.json();
    return json({ status: j?.status ?? "", location: j?.location ?? null });
  } catch {
    return json({ error: "upstream", http: 0 }, 502);
  }
});
