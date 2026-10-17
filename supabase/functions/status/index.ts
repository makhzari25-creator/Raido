import { authenticate, db, json, preflight, readJson, signedStatus, deny } from "../_shared/util.ts";

Deno.serve(async (req) => {
  const pre = preflight(req); if (pre) return pre;
  const auth = await authenticate(req);
  if (!auth) return await deny(req);
  const body = await readJson(req);
  const upd: Record<string, unknown> = { last_seen: new Date().toISOString() };
  if (typeof body.app_version === "string") upd.app_version = body.app_version.slice(0, 20);
  if (typeof body.device_model === "string") upd.device_model = body.device_model.slice(0, 60);
  await db.from("users").update(upd).eq("id", auth.userId);
  return json(await signedStatus(auth.userId, auth.deviceFp));
});
