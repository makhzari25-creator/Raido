package ir.tripanalyzer.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

/** شمارندهٔ سبک و محلی مصرف روزانه (فقط عدد؛ هیچ آدرس/مسیری ثبت نمی‌شود). */
final class UsageCounter {
    private static final String PREFS = "raido_usage";
    private UsageCounter() {}

    private static SharedPreferences p(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String today() {
        return LocalDate.now(ZoneId.of("Asia/Tehran")).toString();
    }

    private static void inc(Context c, String prefix) {
        try {
            SharedPreferences sp = p(c);
            String k = prefix + today();
            sp.edit().putInt(k, sp.getInt(k, 0) + 1).apply();
        } catch (Throwable ignored) {}
    }

    static void onAnalysis(Context c) { inc(c, "a_"); }
    static void onAccepted(Context c) { inc(c, "c_"); }

    /** آمار ۴ روز اخیر برای ارسال (سرور بیشینه را نگه می‌دارد، پس تکراری بی‌ضرر است). */
    static JSONArray recentDays(Context c) {
        JSONArray out = new JSONArray();
        try {
            SharedPreferences sp = p(c);
            LocalDate now = LocalDate.now(ZoneId.of("Asia/Tehran"));
            for (int i = 0; i < 4; i++) {
                String d = now.minusDays(i).toString();
                int a = sp.getInt("a_" + d, 0), ac = sp.getInt("c_" + d, 0);
                if (a == 0 && ac == 0) continue;
                out.put(new JSONObject().put("day", d).put("analyses", a).put("accepted", ac));
            }
            // پاکسازی ورودی‌های قدیمی‌تر از ۱۰ روز
            String cutoff = now.minusDays(10).toString();
            SharedPreferences.Editor ed = sp.edit();
            for (Map.Entry<String, ?> e : sp.getAll().entrySet()) {
                String k = e.getKey();
                if (k.length() > 2 && k.substring(2).compareTo(cutoff) < 0) ed.remove(k);
            }
            ed.apply();
        } catch (Throwable ignored) {}
        return out;
    }
}
