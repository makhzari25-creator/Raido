import { db, json, preflight, readJson, randomCode } from "../_shared/util.ts";

const CLIENT_ID = Deno.env.get("GOOGLE_CLIENT_ID") ?? "";
const REDIRECT = "https://atzyjiljtzxhctblziti.supabase.co/functions/v1/auth-google-callback";

function toEnDigits(s: string): string {
  return s.replace(/[۰-۹]/g, (d) => String("۰۱۲۳۴۵۶۷۸۹".indexOf(d)))
          .replace(/[٠-٩]/g, (d) => String("٠١٢٣٤٥٦٧٨٩".indexOf(d)));
}

Deno.serve(async (req) => {
  const pre = preflight(req); if (pre) return pre;
  if (!CLIENT_ID || !Deno.env.get("GOOGLE_CLIENT_SECRET")) return json({ error: "not_configured" }, 503);
  const body = await readJson(req);
  const fp = String(body.device_fp ?? "");
  if (!/^[A-Za-z0-9]{8,64}$/.test(fp)) return json({ error: "bad_request" }, 400);
  const phone = toEnDigits(String(body.phone ?? "")).replace(/[\s-]/g, "").replace(/^\+98|^0098|^98/, "0");
  if (!/^09[0-9]{9}$/.test(phone)) return json({ error: "bad_phone" }, 400);
  const email = String(body.email ?? "").trim().toLowerCase().slice(0, 120);
  if (email && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) return json({ error: "bad_email" }, 400);

  const ip = (req.headers.get("x-forwarded-for") ?? "").split(",")[0].trim().slice(0, 64) || null;
  const h = new Date(Date.now() - 3600_000).toISOString();
  const d = new Date(Date.now() - 86400_000).toISOString();
  const { count: perFp } = await db.from("login_sessions").select("code", { count: "exact", head: true })
    .eq("device_fp", fp).gte("created_at", h);
  if ((perFp ?? 0) >= 10) return json({ error: "rate_limited" }, 429);
  if (ip) {
    // سقف سخاوتمندانه (شبکهٔ موبایل اشتراکی است)
    const { count: perIp } = await db.from("login_sessions").select("code", { count: "exact", head: true })
      .eq("ip", ip).gte("created_at", d);
    if ((perIp ?? 0) >= 60) return json({ error: "rate_limited" }, 429);
  }

  const code = randomCode(16);
  const { error } = await db.from("login_sessions").insert({
    code, device_fp: fp, via: "google", contact_phone: phone, contact_email: email || null, ip,
  });
  if (error) return json({ error: "server" }, 500);
  const q = new URLSearchParams({
    client_id: CLIENT_ID, redirect_uri: REDIRECT, response_type: "code",
    scope: "openid email", state: code, prompt: "select_account",
  });
  return json({ code, url: "https://accounts.google.com/o/oauth2/v2/auth?" + q.toString(), expires_in: 600 });
});
