import { authenticate, db, deny, json, preflight } from "../_shared/util.ts";

// خروج از حساب: نشست همین دستگاه سمت سرور باطل می‌شود (توکن دیگر کار نمی‌کند).
Deno.serve(async (req) => {
  const pre = preflight(req); if (pre) return pre;
  const auth = await authenticate(req);
  if (!auth) return await deny(req);
  await db.from("ended_sessions").upsert({ token_hash: auth.tokenHash, reason: "logout" }, { onConflict: "token_hash" });
  await db.from("sessions").delete().eq("token_hash", auth.tokenHash);
  return json({ ok: true });
});
