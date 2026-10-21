package ir.tripanalyzer.app;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/** جدول استهلاک نهایی (marginal) — فقط هزینه ناشی از کارکرد */
public final class DepLookup {
    public static final DepLookup INSTANCE = new DepLookup();
    private static JSONObject cache;

    private static final Map<String, String> ALIASES = new HashMap<>();
    static {
        ALIASES.put("جک", "جک J4");
        ALIASES.put("چری", "چری آریزو 5");
        ALIASES.put("تویوتا", "تویوتا/کرولا قدیمی");
        ALIASES.put("پژو ۴۰۵", "پژو 405");
        ALIASES.put("پژو ۲۰۶", "پژو 206");
        ALIASES.put("پژو ۲۰۷", "پژو 207");
    }

    private DepLookup() {}

    private static final String PREFS = "raido_dep_table";
    private static volatile long lastSyncElapsed = 0L;

    private static java.io.File remoteFile(Context c) {
        return new java.io.File(c.getFilesDir(), "dep_remote.json");
    }

    private static boolean valid(JSONObject o) {
        try {
            JSONArray f = o.getJSONArray("families");
            JSONObject d = o.getJSONObject("data");
            return f.length() > 0 && d.optJSONArray("سایر") != null;
        } catch (Exception e) { return false; }
    }

    /** جدول سرور (اگر دانلود و معتبر باشد) روی جدول داخل اپ اولویت دارد. */
    private JSONObject load(Context context) {
        if (cache != null) return cache;
        try {
            java.io.File rf = remoteFile(context);
            if (rf.isFile()) {
                java.io.FileInputStream in = new java.io.FileInputStream(rf);
                byte[] b = new byte[(int) rf.length()];
                int n = 0;
                while (n < b.length) { int r = in.read(b, n, b.length - n); if (r < 0) break; n += r; }
                in.close();
                JSONObject o = new JSONObject(new String(b, 0, n, "UTF-8"));
                if (valid(o)) { cache = o; return cache; }
            }
        } catch (Throwable ignored) {}
        return loadAsset(context);
    }

    static String currentVersion(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("version", "");
    }

    /** پاسخ app-config: اگر جدول جدید بود نصب می‌شود (در ترد پس‌زمینهٔ RemoteConfig صدا می‌شود). */
    void onRemote(Context app, String ver, String tbl) {
        try {
            if (tbl == null || tbl.isEmpty() || ver == null || ver.isEmpty()) return;
            JSONObject nu = new JSONObject(tbl);
            if (!valid(nu)) return;
            applyNew(app, nu, tbl, ver, app.getSharedPreferences(PREFS, Context.MODE_PRIVATE));
        } catch (Throwable ignored) {}
    }

    /** سازگار با فراخوانی قبلی: همگام‌سازی از طریق RemoteConfig */
    public void syncIfDue(final Context ctx) {
        RemoteConfig.syncIfDue(ctx);
    }

    private synchronized void applyNew(Context app, JSONObject nu, String raw, String ver,
                                       android.content.SharedPreferences sp) throws Exception {
        // مقدار فعلی اگر برابر خروجی جدول قدیمی بود (دستی تغییر نکرده) با جدول جدید جایگزین می‌شود
        android.content.SharedPreferences st = Store.INSTANCE.prefs(app);
        String fam = st.getString("vehicle_family", "");
        int mil = st.getInt("vehicle_mileage", 0);
        float cur = st.getFloat("dep_per_km", -1f);
        double oldVal = fam.isEmpty() ? -1 : lookup(app, fam, mil);
        java.io.FileOutputStream out = new java.io.FileOutputStream(remoteFile(app));
        out.write(raw.getBytes("UTF-8"));
        out.flush();
        out.close();
        cache = nu;
        sp.edit().putString("version", ver).apply();
        if (!fam.isEmpty() && cur > 0 && Math.abs(cur - oldVal) < 1.0) {
            double nv = lookup(app, fam, mil);
            if (nv > 0) st.edit().putFloat("dep_per_km", (float) nv).apply();
        }
    }

    private JSONObject loadAsset(Context context) {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("dep.json"), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            cache = new JSONObject(sb.toString());
            return cache;
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveKey(String brand) {
        if (brand == null || brand.isEmpty()) return "سایر";
        if (ALIASES.containsKey(brand)) return ALIASES.get(brand);
        return brand;
    }

    public String[] families(Context context) {
        JSONObject load = load(context);
        if (load == null) return new String[0];
        try {
            JSONArray arr = load.getJSONArray("families");
            String[] out = new String[arr.length()];
            for (int i = 0; i < arr.length(); i++) out[i] = arr.getString(i);
            return out;
        } catch (Exception e) {
            return new String[0];
        }
    }

    public double lookup(Context context, String brand, int mileage) {
        JSONObject load = load(context);
        if (load == null) return 2300.0;
        String key = resolveKey(brand);
        try {
            JSONArray ranges = load.getJSONObject("data").optJSONArray(key);
            if (ranges == null) ranges = load.getJSONObject("data").optJSONArray("سایر");
            if (ranges == null) return 2300.0;
            for (int i = 0; i < ranges.length(); i++) {
                JSONArray row = ranges.getJSONArray(i);
                int from = row.getInt(0);
                int to = row.getInt(1);
                int val = row.getInt(2);
                if (from == 0 && to == 0) {
                    if (mileage == 0) return val;
                } else if (mileage >= from && mileage <= to) {
                    return val;
                }
            }
            JSONArray last = ranges.getJSONArray(ranges.length() - 1);
            return last.getInt(2);
        } catch (Exception e) {
            return 2300.0;
        }
    }
}
