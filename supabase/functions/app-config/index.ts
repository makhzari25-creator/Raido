import { db, json, preflight } from "../_shared/util.ts";

// تنظیمات عمومی و بی‌خطر اپ (فعلاً فقط لینک ویدیوی آموزشی)
Deno.serve(async (req) => {
  const pre = preflight(req); if (pre) return pre;
  const { data } = await db.from("app_settings").select("value").eq("key", "tutorial_url").maybeSingle();
  return json({ tutorial_url: data?.value ?? "" });
});
