package ir.tripanalyzer.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * حافظهٔ چند درخواست اخیر (۱۰ مورد / ۱۵ دقیقه).
 * هدف: وقتی کاربر بین درخواست‌ها ورق می‌زند و به درخواست قبلی برمی‌گردد،
 * همان نتیجهٔ ذخیره‌شده نشان داده شود و تحلیل/ثبت تکراری انجام نشود.
 */
public final class RecentRequests {
    public static final long TTL_MS = 15L * 60L * 1000L;
    private static final int MAX = 10;
    private static final String PREFS = "raido_recent_requests";
    private static final String K_LIST = "list";
    private static final String K_CUR = "cur_key";
    private static final String K_CUR_TS = "cur_ts";

    private RecentRequests() {}

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static JSONArray load(Context c) {
        try {
            JSONArray in = new JSONArray(sp(c).getString(K_LIST, "[]"));
            JSONArray out = new JSONArray();
            long cutoff = System.currentTimeMillis() - TTL_MS;
            for (int i = 0; i < in.length(); i++) {
                JSONObject o = in.optJSONObject(i);
                if (o != null && o.optLong("at", 0) >= cutoff) out.put(o);
            }
            return out;
        } catch (Throwable t) {
            return new JSONArray();
        }
    }

    private static void save(Context c, JSONArray a) {
        try { sp(c).edit().putString(K_LIST, a.toString()).apply(); } catch (Throwable ignored) {}
    }

    /** نرمال‌سازی آدرس برای کلید */
    public static String norm(String s) {
        if (s == null) return "";
        return s.replace('‌', ' ').replaceAll("\\s+", "").toLowerCase(java.util.Locale.ROOT);
    }

    /** درخواست با کلید کامل (قیمت + کیلومتر + دقیقه + آدرس‌ها) */
    public static JSONObject find(Context c, String key) {
        JSONArray a = load(c);
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o != null && key.equals(o.optString("key"))) return o;
        }
        return null;
    }

    /** درخواست تخمینی با همان قیمت و آدرس (برای به‌روزرسانی وقتی کیلومتر واقعی رسید) */
    public static JSONObject findEstimatedByBase(Context c, String base) {
        JSONArray a = load(c);
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o != null && base.equals(o.optString("base")) && o.optBoolean("est", false)) return o;
        }
        return null;
    }

    /** همان اعداد (قیمت + کیلومترها) که تا windowMs پیش ذخیره شده؛ آدرس در نظر گرفته نمی‌شود */
    public static JSONObject findBySig(Context c, String sig, long windowMs) {
        JSONArray a = load(c);
        long now = System.currentTimeMillis();
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o != null && sig.equals(o.optString("sig")) && now - o.optLong("saved", 0L) < windowMs) return o;
        }
        return null;
    }

    /** آخرین ثبت با همین قیمت که تا windowMs پیش ذخیره شده (برای ادغام صفحهٔ قبل/بعد از جزئیات) */
    public static JSONObject findByFare(Context c, Double fare, long windowMs) {
        if (fare == null) return null;
        JSONArray a = load(c);
        long now = System.currentTimeMillis();
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o == null || now - o.optLong("saved", 0L) >= windowMs) continue;
            JSONObject r = o.optJSONObject("rec");
            if (r != null && r.has("fare") && !r.isNull("fare") && Math.abs(r.optDouble("fare") - fare) < 1.0) return o;
        }
        return null;
    }

    public static void put(Context c, String key, String base, JSONObject rec, boolean estimated) {
        put(c, key, base, rec, estimated, "");
    }

    public static void put(Context c, String key, String base, JSONObject rec, boolean estimated, String sig) {
        try {
            JSONArray a = load(c);
            JSONArray next = new JSONArray();
            JSONObject e = new JSONObject();
            e.put("key", key);
            e.put("base", base);
            e.put("est", estimated);
            e.put("at", System.currentTimeMillis());
            e.put("saved", System.currentTimeMillis());
            e.put("sig", sig == null ? "" : sig);
            e.put("rec", rec);
            next.put(e);
            long ts = rec.optLong("ts", -1);
            for (int i = 0; i < a.length() && next.length() < MAX; i++) {
                JSONObject o = a.optJSONObject(i);
                if (o == null) continue;
                if (key.equals(o.optString("key"))) continue;
                JSONObject r = o.optJSONObject("rec");
                if (ts > 0 && r != null && r.optLong("ts", -2) == ts) continue; // همان سفر (به‌روزرسانی)
                next.put(o);
            }
            save(c, next);
        } catch (Throwable ignored) {}
    }

    /** لمس‌کردن زمان ورودی (تازه نگه‌داشتن ۱۵ دقیقه) */
    public static void touch(Context c, String key) {
        try {
            JSONArray a = load(c);
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.optJSONObject(i);
                if (o != null && key.equals(o.optString("key"))) { o.put("at", System.currentTimeMillis()); break; }
            }
            save(c, a);
        } catch (Throwable ignored) {}
    }

    /** آیا همین درخواست همین الان روی صفحه است؟ (برای جلوگیری از نمایش مکرر کارت) */
    public static boolean isCurrent(Context c, String key, long windowMs) {
        SharedPreferences s = sp(c);
        return key.equals(s.getString(K_CUR, null))
                && System.currentTimeMillis() - s.getLong(K_CUR_TS, 0L) < windowMs;
    }

    public static void markCurrent(Context c, String key) {
        try { sp(c).edit().putString(K_CUR, key).putLong(K_CUR_TS, System.currentTimeMillis()).apply(); }
        catch (Throwable ignored) {}
    }

    public static void touchCurrent(Context c) {
        try { sp(c).edit().putLong(K_CUR_TS, System.currentTimeMillis()).apply(); } catch (Throwable ignored) {}
    }
}
