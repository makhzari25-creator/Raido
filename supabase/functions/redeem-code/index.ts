import { authenticate, db, json, preflight, readJson, sha256Hex, signedStatus, deny } from "../_shared/util.ts";

Deno.serve(async (req) => {
  const pre = preflight(req); if (pre) return pre;
  const auth = await authenticate(req);
  if (!auth) return await deny(req);
  const body = await readJson(req);
  const code = String(body.code ?? "").toUpperCase().replace(/[^A-Z0-9]/g, "");
  if (code.length !== 12) return json({ ok: false, error: "invalid" });

  // محدودیت تلاش اشتباه: ۸ بار در ۱۰ دقیقه
  const since = new Date(Date.now() - 600_000).toISOString();
  const { count } = await db.from("redeem_log").select("id", { count: "exact", head: true })
    .eq("user_id", auth.userId).eq("ok", false).gte("at", since);
  if ((count ?? 0) >= 8) return json({ ok: false, error: "rate_limited" }, 429);

  const { data, error } = await db.rpc("redeem_code", { p_user: auth.userId, p_hash: await sha256Hex(code) });
  if (error) return json({ error: "server" }, 500);
  if (!data?.ok) return json({ ok: false, error: data?.error ?? "invalid" });
  return json({ ok: true, ...(await signedStatus(auth.userId, auth.deviceFp)) });
});
