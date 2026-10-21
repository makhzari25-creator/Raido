import { db, json, preflight, readJson } from "../_shared/util.ts";

// تنظیمات عمومی و بی‌خطر اپ: ویدیوی آموزشی، لینک‌های پشتیبانی، راهنما، قیمت‌ها و جدول استهلاک
// (جدول استهلاک فقط اگر نسخهٔ اپ قدیمی‌تر است ارسال می‌شود)
function parse(v: string | undefined): unknown {
  if (!v) return null;
  try { return JSON.parse(v); } catch { return null; }
}

Deno.serve(async (req) => {
  const pre = preflight(req); if (pre) return pre;
  const body = await readJson(req);
  const keys = ["tutorial_url", "dep_version", "support_links", "guide", "plans"];
  const { data } = await db.from("app_settings").select("key, value").in("key", keys);
  const m: Record<string, string> = Object.fromEntries((data ?? []).map((r: { key: string; value: string }) => [r.key, r.value]));
  const out: Record<string, unknown> = {
    tutorial_url: m.tutorial_url ?? "",
    dep_version: m.dep_version ?? "",
    support_links: parse(m.support_links),
    guide: parse(m.guide),
    plans: parse(m.plans),
  };
  if (m.dep_version && String(body.dep_version ?? "") !== m.dep_version) {
    const { data: t } = await db.from("app_settings").select("value").eq("key", "dep_table").maybeSingle();
    if (t?.value) out.dep_table = t.value;
  }
  return json(out);
});
