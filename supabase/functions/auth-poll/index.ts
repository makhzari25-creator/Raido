import { db, json, preflight, readJson, randomHex, sha256Hex, signedStatus } from "../_shared/util.ts";

Deno.serve(async (req) => {
  const pre = preflight(req); if (pre) return pre;
  try {
    const body = await readJson(req);
    const code = String(body.code ?? "");
    const fp = String(body.device_fp ?? "");
    if (!/^[A-Z0-9]{16}$/.test(code) || !/^[A-Za-z0-9]{8,64}$/.test(fp)) return json({ error: "bad_request" }, 400);

    const { data: ls } = await db.from("login_sessions").select("*").eq("code", code).maybeSingle();
    if (!ls || ls.device_fp !== fp) return json({ status: "expired" });
    if (ls.status === "pending" || ls.status === "awaiting_contact") {
      if (new Date(ls.expires_at).getTime() < Date.now()) return json({ status: "expired" });
      return json({ status: ls.status });
    }
    if (ls.status !== "verified" || !ls.user_id) return json({ status: "expired" });

    // کاربر مسدود وارد نمی‌شود
    const { data: usr } = await db.from("users").select("banned").eq("id", ls.user_id).maybeSingle();
    if (usr?.banned) {
      await db.from("login_sessions").update({ status: "consumed" }).eq("code", code);
      return json({ status: "banned" });
    }

    // اول همهٔ کارهای بدون اثرجانبی (تا خطا باعث مصرف شدن نشست نشود)
    await db.from("subscriptions").upsert({ user_id: ls.user_id }, { onConflict: "user_id", ignoreDuplicates: true });
    const signed = await signedStatus(ls.user_id, fp);

    // یک‌بارمصرف و ایمن در برابر race
    const { data: claimed } = await db.from("login_sessions")
      .update({ status: "consumed" }).eq("code", code).eq("status", "verified").select("user_id").maybeSingle();
    if (!claimed) return json({ status: "expired" });

    const token = randomHex(32);
    // هر کاربر فقط یک دستگاه فعال: نشست‌های قبلی باطل می‌شوند
    await db.rpc("end_other_sessions", { p_user: ls.user_id });
    const ins = await db.from("sessions").insert({ token_hash: await sha256Hex(token), user_id: ls.user_id, device_fp: fp });
    if (ins.error) throw new Error("session insert: " + ins.error.message);
    await db.from("users").update({ last_seen: new Date().toISOString(), last_via: ls.via ?? "telegram" }).eq("id", ls.user_id);

    return json({ status: "ok", token, ...signed });
  } catch (e) {
    console.error("auth-poll failed:", e instanceof Error ? e.message : String(e));
    return json({ status: "error", error: "server" }, 500);
  }
});
