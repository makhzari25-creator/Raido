import { db } from "../_shared/util.ts";

const CLIENT_ID = Deno.env.get("GOOGLE_CLIENT_ID") ?? "";
const CLIENT_SECRET = Deno.env.get("GOOGLE_CLIENT_SECRET") ?? "";
const REDIRECT = "https://atzyjiljtzxhctblziti.supabase.co/functions/v1/auth-google-callback";

function back(code: string, ok: boolean, why = ""): Response {
  // گیت‌وی Supabase هر text/html را متن ساده می‌کند؛ پس مستقیم به لینک عمیق اپ برمی‌گردیم.
  const loc = `raido://login-done?c=${encodeURIComponent(code)}&ok=${ok ? 1 : 0}${why ? "&e=" + why : ""}`;
  return new Response(null, { status: 302, headers: { Location: loc } });
}

function normEmail(e: string): string {
  const [l, d] = e.toLowerCase().split("@");
  if (d === "gmail.com" || d === "googlemail.com") return l.split("+")[0].replace(/\./g, "") + "@gmail.com";
  return l + "@" + d;
}

Deno.serve(async (req) => {
  const u = new URL(req.url);
  const state = u.searchParams.get("state") ?? "";
  if (!/^[A-Z0-9]{16}$/.test(state)) return new Response("bad request", { status: 400 });
  try {
    const gcode = u.searchParams.get("code");
    if (u.searchParams.get("error") || !gcode) return back(state, false, "denied");

    const { data: ls } = await db.from("login_sessions").select("*").eq("code", state).maybeSingle();
    if (!ls || ls.via !== "google" || ls.status !== "pending" || new Date(ls.expires_at).getTime() < Date.now()) {
      return back(state, false, "expired");
    }

    const tr = await fetch("https://oauth2.googleapis.com/token", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        code: gcode, client_id: CLIENT_ID, client_secret: CLIENT_SECRET,
        redirect_uri: REDIRECT, grant_type: "authorization_code",
      }).toString(),
    });
    if (!tr.ok) return back(state, false, "google");
    const tok = await tr.json();
    // id_token مستقیم از گوگل و روی TLS گرفته شده؛ فقط ادعاها را بررسی می‌کنیم.
    const part = String(tok.id_token ?? "").split(".")[1];
    if (!part) return back(state, false, "google");
    const claims = JSON.parse(new TextDecoder().decode(
      Uint8Array.from(atob(part.replace(/-/g, "+").replace(/_/g, "/")), (c) => c.charCodeAt(0))));
    if (claims.aud !== CLIENT_ID || !["accounts.google.com", "https://accounts.google.com"].includes(claims.iss)
        || !claims.sub || !claims.email || claims.email_verified !== true) {
      return back(state, false, "unverified");
    }
    const sub = String(claims.sub);
    const email = String(claims.email).toLowerCase();

    let userId: string | null = null;
    const { data: ex } = await db.from("users").select("id").eq("google_sub", sub).maybeSingle();
    if (ex) {
      userId = ex.id;
    } else {
      const { data: created, error } = await db.from("users").insert({
        google_sub: sub, email, email_norm: normEmail(email), contact_phone: ls.contact_phone,
      }).select("id").single();
      if (error || !created) return back(state, false, "server");
      userId = created.id;
    }
    const { data: ok } = await db.from("login_sessions")
      .update({ status: "verified", user_id: userId }).eq("code", state).eq("status", "pending")
      .select("code").maybeSingle();
    return back(state, !!ok, ok ? "" : "expired");
  } catch (e) {
    console.error("auth-google-callback:", e instanceof Error ? e.message : String(e));
    return back(state, false, "server");
  }
});
