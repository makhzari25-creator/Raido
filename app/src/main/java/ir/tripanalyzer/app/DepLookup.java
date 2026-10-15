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

    private JSONObject load(Context context) {
        if (cache != null) return cache;
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
