import { createClient } from "npm:@supabase/supabase-js@2";

export const db = createClient(
  Deno.env.get("SUPABASE_URL")!,
  Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
  { auth: { persistSession: false, autoRefreshToken: false } },
);

export const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "content-type, x-raido-token, x-admin-key, apikey, authorization",
  "Access-Control-Allow-Methods": "POST, GET, OPTIONS",
};

export function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...cors, "Content-Type": "application/json; charset=utf-8", "Cache-Control": "no-store" },
  });
}

export function preflight(req: Request): Response | null {
  return req.method === "OPTIONS" ? new Response(null, { status: 204, headers: cors }) : null;
}

export async function readJson(req: Request): Promise<Record<string, any>> {
  try { return await req.json(); } catch { return {}; }
}

const enc = new TextEncoder();

export async function sha256Hex(s: string): Promise<string> {
  const d = await crypto.subtle.digest("SHA-256", enc.encode(s));
  return [...new Uint8Array(d)].map((b) => b.toString(16).padStart(2, "0")).join("");
}

export function randomHex(bytes: number): string {
  const a = crypto.getRandomValues(new Uint8Array(bytes));
  return [...a].map((b) => b.toString(16).padStart(2, "0")).join("");
}

const ALPHA = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
export function randomCode(len: number): string {
  const a = crypto.getRandomValues(new Uint8Array(len));
  return [...a].map((b) => ALPHA[b % ALPHA.length]).join("");
}

export function safeEqual(a: string, b: string): boolean {
  if (a.length !== b.length) return false;
  let r = 0;
  for (let i = 0; i < a.length; i++) r |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return r === 0;
}

export const PLANS: Record<string, { days: number; premium: boolean }> = {
  MONTHLY: { days: 30, premium: false },
  YEARLY: { days: 365, premium: false },
  PREMIUM_MONTHLY: { days: 30, premium: true },
  PREMIUM_YEARLY: { days: 365, premium: true },
};

export const OFFLINE_GRACE_MS = 10 * 24 * 3600 * 1000;

export type Auth = { userId: string; deviceFp: string };

/** احراز هویت با توکن دستگاه (هدر x-raido-token). */
export async function authenticate(req: Request): Promise<Auth | null> {
  const token = req.headers.get("x-raido-token") ?? "";
  if (token.length < 32 || token.length > 128) return null;
  const hash = await sha256Hex(token);
  const { data } = await db.from("sessions").select("user_id, device_fp").eq("token_hash", hash).maybeSingle();
  if (!data) return null;
  db.from("sessions").update({ last_used: new Date().toISOString() }).eq("token_hash", hash).then(() => {});
  return { userId: data.user_id, deviceFp: data.device_fp };
}

function b64(buf: ArrayBuffer): string {
  let s = "";
  for (const b of new Uint8Array(buf)) s += String.fromCharCode(b);
  return btoa(s);
}

let keyPromise: Promise<CryptoKey> | null = null;
function signingKey(): Promise<CryptoKey> {
  if (!keyPromise) {
    const raw = Uint8Array.from(atob(Deno.env.get("SIGNING_PRIVATE_KEY")!), (c) => c.charCodeAt(0));
    keyPromise = crypto.subtle.importKey(
      "pkcs8", raw, { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" }, false, ["sign"],
    );
  }
  return keyPromise;
}

/** وضعیت اشتراک امضاشده؛ اپ با کلید عمومی اعتبار آن را بدون اینترنت چک می‌کند. */
export async function signedStatus(userId: string, deviceFp: string) {
  const { data: s } = await db.from("subscriptions").select("*").eq("user_id", userId).maybeSingle();
  const now = Date.now();
  const exp = s?.expires_at ? new Date(s.expires_at).getTime() : 0;
  const payload = JSON.stringify({
    uid: userId,
    fp: deviceFp,
    plan: s?.plan ?? "NONE",
    premium: !!s?.is_premium,
    expires_at: exp,
    trial_used: !!s?.trial_used,
    server_time: now,
    offline_until: now + OFFLINE_GRACE_MS,
  });
  const sig = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", await signingKey(), enc.encode(payload));
  return { payload, sig: b64(sig) };
}
