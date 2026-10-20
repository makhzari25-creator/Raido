import { db, json, preflight, readJson } from "../_shared/util.ts";

// تنظیمات عمومی و بی‌خطر اپ: لینک ویدیوی آموزشی + جدول استهلاک (فقط اگر نسخهٔ اپ قدیمی‌تر است)
Deno.serve(async (req) => {
  const pre = preflight(req); if (pre) return pre;
  const body = await readJson(req);
  const { data } = await db.from("app_settings").select("key, value").in("key", ["tutorial_url", "dep_version"]);
  const m: Record<string, string> = Object.fromEntries((data ?? []).map((r: { key: string; value: string }) => [r.key, r.value]));
  const out: Record<string, unknown> = { tutorial_url: m.tutorial_url ?? "", dep_version: m.dep_version ?? "" };
  if (m.dep_version && String(body.dep_version ?? "") !== m.dep_version) {
    const { data: t } = await db.from("app_settings").select("value").eq("key", "dep_table").maybeSingle();
    if (t?.value) out.dep_table = t.value;
  }
  return json(out);
});
