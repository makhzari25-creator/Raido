import { db, json, preflight, readJson, randomHex, sha256Hex, signedStatus } from "../_shared/util.ts";

Deno.serve(async (req) => {
  const pre = preflight(req); if (pre) return pre;
  const body = await readJson(req);
  const code = String(body.code ?? "");
  const fp = String(body.device_fp ?? "");
  if (!/^[A-Z0-9]{16}$/.test(code) || !/^[A-Za-z0-9]{8,64}$/.test(fp)) return json({ error: "bad_request" }, 400);

  const { data: ls } = await db.from("login_sessions").select("*").eq("code", code).maybeSingle();
  if (!ls || ls.device_fp !== fp) return json({ status: "expired" });
  if (ls.expires_at < new Date().toISOString() && ls.status !== "verified") return json({ status: "expired" });
  if (ls.status === "pending" || ls.status === "awaiting_contact") return json({ status: ls.status });
  if (ls.status !== "verified" || !ls.user_id) return json({ status: "expired" });

  // یک‌بارمصرف و ایمن در برابر race
  const { data: claimed } = await db.from("login_sessions")
    .update({ status: "consumed" }).eq("code", code).eq("status", "verified").select("user_id").maybeSingle();
  if (!claimed) return json({ status: "expired" });

  const token = randomHex(32);
  // هر کاربر فقط یک دستگاه فعال: نشست‌های قبلی باطل می‌شوند
  await db.from("sessions").delete().eq("user_id", ls.user_id);
  await db.from("sessions").insert({ token_hash: await sha256Hex(token), user_id: ls.user_id, device_fp: fp });
  await db.from("subscriptions").upsert({ user_id: ls.user_id }, { onConflict: "user_id", ignoreDuplicates: true });
  await db.from("users").update({ last_seen: new Date().toISOString() }).eq("id", ls.user_id);

  return json({ status: "ok", token, ...(await signedStatus(ls.user_id, fp)) });
});
