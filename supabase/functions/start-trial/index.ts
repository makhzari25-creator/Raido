import { authenticate, db, json, preflight, signedStatus, deny } from "../_shared/util.ts";

Deno.serve(async (req) => {
  const pre = preflight(req); if (pre) return pre;
  const auth = await authenticate(req);
  if (!auth) return await deny(req);
  const { data, error } = await db.rpc("start_trial", { p_user: auth.userId, p_fp: auth.deviceFp });
  if (error) return json({ error: "server" }, 500);
  if (!data?.ok) return json({ ok: false, error: data?.error ?? "failed" });
  return json({ ok: true, ...(await signedStatus(auth.userId, auth.deviceFp)) });
});
