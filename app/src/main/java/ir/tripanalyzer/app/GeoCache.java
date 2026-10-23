package ir.tripanalyzer.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * کش ماندگار ژئوکد روی گوشی (فقط نتیجهٔ موفق). کلید = آدرس نرمال‌شده.
 * سقف ۳۰۰۰ آدرس (LRU) و عمر ۹۰ روز؛ ذخیره در SharedPreferences روی ترد جدا.
 */
final class GeoCache {
    static final GeoCache INSTANCE = new GeoCache();
    private static final int MAX = 3000;
    private static final long TTL_MS = 90L * 24 * 3600 * 1000;
    private static final String PREFS = "geo_cache_v1";

    private final Map<String, double[]> map = new LinkedHashMap<String, double[]>(64, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, double[]> e) { return size() > MAX; }
    };
    private boolean loaded;

    private GeoCache() {}

    /** مثل نرمال‌سازی سرور: ی/ک عربی، ارقام فارسی/عربی، نیم‌فاصله، فاصلهٔ اضافه. */
    static String norm(String s) {
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == 'ي' || c == 'ى') c = 'ی';
            else if (c == 'ك') c = 'ک';
            else if (c >= '۰' && c <= '۹') c = (char) ('0' + (c - '۰'));
            else if (c >= '٠' && c <= '٩') c = (char) ('0' + (c - '٠'));
            else if (c == '‌' || c == '‍' || c == '‏' || c == '‎' || c == 'ـ') c = ' ';
            b.append(c);
        }
        return b.toString().replaceAll("\\s+", " ").trim().toLowerCase(java.util.Locale.ROOT);
    }

    private void load(Context ctx) {
        if (loaded) return;
        loaded = true;
        try {
            String raw = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("d", null);
            if (raw == null) return;
            JSONArray a = new JSONArray(raw);
            long now = System.currentTimeMillis();
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                if (now - o.optLong("t") > TTL_MS) continue;
                map.put(o.getString("k"), new double[]{o.getDouble("y"), o.getDouble("x"), o.optLong("t")});
            }
        } catch (Throwable ignored) {}
    }

    /** {lat, lng} یا null */
    synchronized double[] get(Context ctx, String address) {
        load(ctx);
        double[] v = map.get(norm(address));
        if (v == null) return null;
        if (System.currentTimeMillis() - (long) v[2] > TTL_MS) { map.remove(norm(address)); return null; }
        return new double[]{v[0], v[1]};
    }

    synchronized void put(final Context ctx, String address, double lat, double lng) {
        load(ctx);
        map.put(norm(address), new double[]{lat, lng, System.currentTimeMillis()});
        final String snap = snapshot();
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                            .edit().putString("d", snap).apply();
                } catch (Throwable ignored) {}
            }
        }).start();
    }

    private String snapshot() {
        JSONArray a = new JSONArray();
        try {
            Iterator<Map.Entry<String, double[]>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, double[]> e = it.next();
                double[] v = e.getValue();
                a.put(new JSONObject().put("k", e.getKey()).put("y", v[0]).put("x", v[1]).put("t", (long) v[2]));
            }
        } catch (Throwable ignored) {}
        return a.toString();
    }
}
