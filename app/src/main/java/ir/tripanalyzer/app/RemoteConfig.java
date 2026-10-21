package ir.tripanalyzer.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * تنظیمات قابل‌تغییر از پنل: لینک‌های پشتیبانی، راهنما، قیمت‌ها، ویدیوی آموزشی و جدول استهلاک.
 * یک درخواست کوچک (app-config)، با کش محلی؛ اگر آفلاین باشیم همان مقدار قبلی یا پیش‌فرض داخل اپ استفاده می‌شود.
 */
final class RemoteConfig {
    private static final String PREFS = "raido_remote_cfg";
    private static volatile long lastFetchElapsed = 0L;
    private static volatile boolean running = false;

    private RemoteConfig() {}

    private static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static final class Link { String title; String url; String desc; }
    static final class Section { String title; String body; }
    static final class Plan { String code; String title; int price; String desc; }

    // ───── خواندن (از کش) ─────
    static String tutorialUrl(Context c) {
        return sp(c).getString("tutorial_url", "");
    }

    static List<Link> supportLinks(Context c) {
        List<Link> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(sp(c).getString("support_links", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.optJSONObject(i);
                if (o == null) continue;
                String u = o.optString("url", "");
                if (!u.startsWith("https://")) continue;
                Link l = new Link();
                l.title = o.optString("title", "پشتیبانی");
                l.url = u;
                l.desc = o.optString("desc", "");
                out.add(l);
            }
        } catch (Throwable ignored) {}
        if (out.isEmpty()) {
            Link l = new Link();
            l.title = "تلگرام پشتیبانی";
            l.url = LicenseStore.TELEGRAM_SUPPORT;
            l.desc = "برای سؤال، گزارش باگ، خرید اشتراک و پیشنهاد";
            out.add(l);
        }
        return out;
    }

    /** لینک اصلی پشتیبانی (برای دکمهٔ درخواست اشتراک) */
    static String primarySupportUrl(Context c) {
        return supportLinks(c).get(0).url;
    }

    /** null = راهنمای پیش‌فرض داخل اپ */
    static List<Section> guide(Context c) {
        List<Section> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(sp(c).getString("guide", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.optJSONObject(i);
                if (o == null) continue;
                Section s = new Section();
                s.title = o.optString("title", "");
                s.body = o.optString("body", "");
                if (!s.title.isEmpty()) out.add(s);
            }
        } catch (Throwable ignored) {}
        return out.isEmpty() ? null : out;
    }

    /** قیمت و متن طرح؛ اگر روی سرور نبود null */
    static Plan plan(Context c, String code) {
        try {
            JSONArray a = new JSONArray(sp(c).getString("plans", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.optJSONObject(i);
                if (o != null && code.equals(o.optString("code"))) {
                    Plan p = new Plan();
                    p.code = code;
                    p.title = o.optString("title", "");
                    p.price = o.optInt("price", -1);
                    p.desc = o.optString("desc", "");
                    return p.price >= 0 ? p : null;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    // ───── دریافت ─────
    interface Done { void onDone(); }

    /** دریافت در پس‌زمینه؛ اگر کمتر از minGapMs از آخرین دریافت گذشته باشد انجام نمی‌شود. بعد از موفقیت روی UI صدا می‌شود. */
    static void refresh(final Context ctx, long minGapMs, final Done done) {
        final Context app = ctx.getApplicationContext();
        long el = SystemClock.elapsedRealtime();
        if (running || (lastFetchElapsed != 0 && el - lastFetchElapsed < minGapMs)) return;
        running = true;
        new Thread(() -> {
            boolean ok = false;
            try {
                SharedPreferences prefs = sp(app);
                JSONObject r = RaidoCloud.post("app-config",
                        new JSONObject().put("dep_version", DepLookup.INSTANCE.currentVersion(app)), null, 6000, 10000);
                if (r.optInt("_http") == 200) {
                    SharedPreferences.Editor e = prefs.edit();
                    e.putString("tutorial_url", r.optString("tutorial_url", ""));
                    if (r.has("support_links") && !r.isNull("support_links")) e.putString("support_links", r.optJSONArray("support_links") != null ? r.optJSONArray("support_links").toString() : "[]");
                    if (r.has("guide") && !r.isNull("guide")) e.putString("guide", r.optJSONArray("guide") != null ? r.optJSONArray("guide").toString() : "[]");
                    if (r.has("plans") && !r.isNull("plans")) e.putString("plans", r.optJSONArray("plans") != null ? r.optJSONArray("plans").toString() : "[]");
                    e.apply();
                    try {
                        DepLookup.INSTANCE.onRemote(app, r.optString("dep_version", ""), r.optString("dep_table", ""));
                    } catch (Throwable ignored) {}
                    ok = true;
                    lastFetchElapsed = SystemClock.elapsedRealtime();
                }
            } catch (Throwable ignored) {
            } finally {
                running = false;
            }
            if (ok && done != null) new Handler(Looper.getMainLooper()).post(done::onDone);
        }, "raido-cfg").start();
    }

    /** همگام‌سازی کم‌مصرف پس‌زمینه (حداکثر هر ۶ ساعت) */
    static void syncIfDue(Context ctx) {
        try {
            SharedPreferences p = sp(ctx);
            long wall = System.currentTimeMillis();
            long last = p.getLong("checked_at", 0L);
            if (last <= wall && wall - last < 6 * 3600 * 1000L) return;
            p.edit().putLong("checked_at", wall).apply();
            refresh(ctx, 0L, null);
        } catch (Throwable ignored) {}
    }
}
