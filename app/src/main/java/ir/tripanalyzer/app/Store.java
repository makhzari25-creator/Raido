package ir.tripanalyzer.app;

import android.content.Context;
import android.content.SharedPreferences;
import ir.tripanalyzer.app.NotificationParser;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.collections.IntIterator;
import kotlin.comparisons.ComparisonsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.IntRange;
import kotlin.ranges.RangesKt;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Store.kt */
@Metadata(d1 = {"\u0000x\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u000f\u001a\u00020\u0010J\u0016\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0015\u001a\u00020\u0012J\u000e\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u000f\u001a\u00020\u0010J\u0016\u0010\u0018\u001a\u00020\u00142\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0019\u001a\u00020\u0017J\u000e\u0010\u001a\u001a\u00020\u00172\u0006\u0010\u000f\u001a\u00020\u0010J\u0016\u0010\u001b\u001a\u00020\u00142\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0019\u001a\u00020\u0017J\u001e\u0010\u001c\u001a\u00020\u00172\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u001d\u001a\u00020\u00052\u0006\u0010\u001e\u001a\u00020\u001fJ\u000e\u0010 \u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\u0010J\u0016\u0010!\u001a\u00020\u00142\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\"\u001a\u00020#J\u0010\u0010$\u001a\u0004\u0018\u00010#2\u0006\u0010\u000f\u001a\u00020\u0010J\u0014\u0010%\u001a\b\u0012\u0004\u0012\u00020#0&2\u0006\u0010\u000f\u001a\u00020\u0010J&\u0010'\u001a\u00020#2\u0006\u0010(\u001a\u00020)2\u0006\u0010\u0015\u001a\u00020\u00122\u0006\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020\u001fJ\u001a\u0010-\u001a\u000e\u0012\u0004\u0012\u00020)\u0012\u0004\u0012\u00020\u001f0.2\u0006\u0010\"\u001a\u00020#J\u001b\u0010/\u001a\u0004\u0018\u000100*\u00020#2\u0006\u00101\u001a\u00020\u0005H\u0002¢\u0006\u0002\u00102J\u001b\u00103\u001a\n 5*\u0004\u0018\u000104042\u0006\u0010\u000f\u001a\u00020\u0010¢\u0006\u0002\u00106J\u001e\u00107\u001a\u00020\u00052\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u00108\u001a\u00020\u00052\u0006\u00109\u001a\u00020\u0005J\u0014\u0010:\u001a\b\u0012\u0004\u0012\u0002040&2\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010;\u001a\u00020\u00142\u0006\u0010\u000f\u001a\u00020\u0010R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082T¢\u0006\u0002\n\u0000"}, d2 = {"Lir/tripanalyzer/app/Store;", "", "<init>", "()V", "PREFS", "", "KEY_LAST", "KEY_RECENT", "KEY_DEDUP_SIG", "KEY_DEDUP_TS", "KEY_NOTIF_SEQ", "MAX_RECENT", "", "prefs", "Landroid/content/SharedPreferences;", "c", "Landroid/content/Context;", "settings", "Lir/tripanalyzer/app/EngineSettings;", "saveSettings", "", "s", "isOnboardingDone", "", "setOnboardingDone", "v", "isAnalysisEnabled", "setAnalysisEnabled", "isFresh", "sig", "now", "", "nextNotifId", "saveLast", "rec", "Lorg/json/JSONObject;", "last", Store.KEY_RECENT, "", "recordFrom", "r", "Lir/tripanalyzer/app/TripResult;", "parsed", "Lir/tripanalyzer/app/NotificationParser$Parsed;", "ts", "parseResult", "Lkotlin/Pair;", "optDoubleOrNull", "", "k", "(Lorg/json/JSONObject;Ljava/lang/String;)Ljava/lang/Double;", "errorReportsDir", "Ljava/io/File;", "kotlin.jvm.PlatformType", "(Landroid/content/Context;)Ljava/io/File;", "saveErrorReport", "rawText", "parsedSummary", "listErrorReports", "eraseEverything"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class Store {
    public static final Store INSTANCE = new Store();
    private static final String KEY_DEDUP_SIG = "dedup_sig";
    private static final String KEY_DEDUP_TS = "dedup_ts";
    private static final String KEY_LAST = "last_result";
    private static final String KEY_NOTIF_SEQ = "notif_seq";
    static final String KEY_RECENT = "recent";
    private static final int MAX_RECENT = 50;
    private static final String PREFS = "trip_analyzer";

    public final void clearRecent(Context context) {
        SharedPreferences.Editor edit = prefs(context).edit();
        edit.putString(KEY_RECENT, "[]");
        edit.apply();
    }

    public final double computeNetFromGross(Context context, double d) {
        SharedPreferences prefs = prefs(context);
        double d2 = d - ((((prefs.getFloat("fuel_per_100", 7.5f) / 100.0d) * prefs.getFloat("fuel_price", 5000.0f)) + prefs.getFloat("dep_per_km", 2100.0f)) * prefs.getFloat("speed", 60.0f));
        if (d2 < 0.0d) {
            return 0.0d;
        }
        return d2;
    }

    public final double getEffectiveSpeed(Context context, double d) {
        return prefs(context).getBoolean("day_heavy_traffic", false) ? getTrafficSpeed(context) : d;
    }

    public final String getLastDistSource(Context context) {
        if (context == null) return "تخمینی";
        String string = prefs(context).getString("last_dist_source", "تخمینی");
        return string != null ? string : "تخمینی";
    }

    public final double getTrafficSpeed(Context context) {
        return prefs(context).getFloat("traffic_speed", 35.0f);
    }

    public final double getCitySpeed(Context context) {
        return prefs(context).getFloat("city_speed", 35.0f);
    }

    public final double getHighwaySpeed(Context context) {
        float hw = prefs(context).getFloat("highway_speed", 0f);
        if (hw <= 0f) {
            float city = prefs(context).getFloat("city_speed", 35.0f);
            float avg = prefs(context).getFloat("speed", 50.0f);
            hw = Math.max(avg, city);
        }
        return hw;
    }

    public final boolean isDayHeavyTraffic(Context context) {
        return prefs(context).getBoolean("day_heavy_traffic", false);
    }

    public final boolean isDayRainy(Context context) {
        return prefs(context).getBoolean("day_rainy", false);
    }

    public final void removeAcceptedByTs(Context context, long j) {
        SharedPreferences prefs = prefs(context);
        try {
            JSONArray jSONArray = new JSONArray(prefs.getString(KEY_RECENT, "[]"));
            JSONArray jSONArray2 = new JSONArray();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                if (jSONObject.optLong("ts", -1L) != j) {
                    jSONArray2.put(jSONObject);
                }
            }
            prefs.edit().putString(KEY_RECENT, jSONArray2.toString()).apply();
        } catch (Exception unused) {
        }
    }

    public final void saveAccepted(Context c, JSONObject rec) {
        JSONArray jSONArray;
        Intrinsics.checkNotNullParameter(c, "c");
        Intrinsics.checkNotNullParameter(rec, "rec");
        SharedPreferences prefs = prefs(c);
        try {
            rec.put("accepted", true);
            rec.put("acceptedAt", System.currentTimeMillis());
            try {
                RaidoDataStore.INSTANCE.markAccepted(c, rec.optLong("ts", 0L));
            } catch (Exception ignored) {}
        } catch (Exception unused) {
        }
        try {
            jSONArray = new JSONArray(prefs.getString(KEY_RECENT, "[]"));
        } catch (Exception unused2) {
            jSONArray = new JSONArray();
        }
        long optLong = rec.optLong("ts", 0L);
        int i = 0;
        try {
        while (true) {
            if (i >= jSONArray.length()) {
                JSONArray jSONArray2 = new JSONArray();
                jSONArray2.put(rec);
                for (int i2 = 0; i2 < jSONArray.length() && i2 < 49; i2++) {
                    try {
                        jSONArray2.put(jSONArray.get(i2));
                    } catch (Exception unused3) {
                    }
                }
                jSONArray = jSONArray2;
                break;
            } else if (jSONArray.getJSONObject(i).optLong("ts", -1L) == optLong) {
                jSONArray.put(i, rec);
                break;
            } else {
                i++;
            }
        }
        } catch (Exception unusedLoop) {
        }
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(KEY_RECENT, jSONArray.toString());
        edit.apply();
    }

    public final void saveLast(Context c, JSONObject rec) {
        Intrinsics.checkNotNullParameter(c, "c");
        Intrinsics.checkNotNullParameter(rec, "rec");
        SharedPreferences.Editor edit = prefs(c).edit();
        edit.putString(KEY_LAST, rec.toString());
        edit.apply();
    }

    public final void setDayHeavyTraffic(Context context, boolean z) {
        prefs(context).edit().putBoolean("day_heavy_traffic", z).apply();
    }

    public final void setDayRainy(Context context, boolean z) {
        prefs(context).edit().putBoolean("day_rainy", z).apply();
    }

    public final void setLastDistSource(Context context, String str) {
        if (context == null) return;
        SharedPreferences.Editor edit = prefs(context).edit();
        if (str == null) str = "تخمینی";
        edit.putString("last_dist_source", str);
        edit.apply();
    }

    public final void setTrafficSpeed(Context context, float f) {
        SharedPreferences.Editor edit = prefs(context).edit();
        edit.putFloat("traffic_speed", f);
        edit.apply();
    }

    public final void setVehicleInfo(Context context, String str, int i) {
        SharedPreferences.Editor edit = prefs(context).edit();
        edit.putString("vehicle_family", str);
        edit.putInt("vehicle_mileage", i);
        edit.apply();
    }

    private Store() {
    }

    public final SharedPreferences prefs(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        SharedPreferences sharedPreferences = c.getSharedPreferences(PREFS, 0);
        Intrinsics.checkNotNullExpressionValue(sharedPreferences, "getSharedPreferences(...)");
        return sharedPreferences;
    }

    /**
     * کف پیش‌فرض پویا:
     * ۱۱۲٬۰۰۰ (حقوق پایه) + بنزین ساعتی + استهلاک ساعتی
     * بنزین/استهلاک ساعتی با سرعت کاری ۳۰ km/h
     */
    public final float computeDefaultAnalysisFloor(Context c) {
        float salary = 112000f;
        float fuelPer100 = 7.5f;
        float fuelPrice = 10000f;
        float depPerKm = 2100f;
        try {
            if (c != null) {
                SharedPreferences p = prefs(c);
                fuelPer100 = p.getFloat("fuel_per_100", fuelPer100);
                fuelPrice = p.getFloat("fuel_price", fuelPrice);
                depPerKm = p.getFloat("dep_per_km", depPerKm);
            }
        } catch (Throwable ignored) {}
        float fuelPerKm = (fuelPer100 / 100f) * fuelPrice;
        float hourlyFuel = 30f * fuelPerKm;
        float hourlyDep = 30f * depPerKm;
        float floor = salary + hourlyFuel + hourlyDep;
        if (floor < 50000f) floor = 50000f;
        if (floor > 2000000f) floor = 2000000f;
        return floor;
    }

    /** اگر کاربر دستی عوض کرده باشد true */
    public final boolean isAnalysisFloorOverridden(Context c) {
        if (c == null) return false;
        return prefs(c).getBoolean("analysis_floor_overridden", false);
    }

    /**
     * کف فعال برای تحلیل:
     * - اگر کاربر override کرده → مقدار ذخیره‌شده
     * - وگرنه → فرمول پویا
     */
    public final float getAnalysisFloor(Context c) {
        if (c == null) return computeDefaultAnalysisFloor(null);
        if (isAnalysisFloorOverridden(c)) {
            float f = prefs(c).getFloat("analysis_floor", computeDefaultAnalysisFloor(c));
            if (f < 50000f) f = 50000f;
            if (f > 2000000f) f = 2000000f;
            return f;
        }
        return computeDefaultAnalysisFloor(c);
    }

    /** ذخیره کف دستی (override) */
    public final void setAnalysisFloor(Context c, float floor) {
        if (c == null) return;
        if (floor < 50000f) floor = 50000f;
        if (floor > 2000000f) floor = 2000000f;
        prefs(c).edit()
                .putFloat("analysis_floor", floor)
                .putBoolean("analysis_floor_overridden", true)
                .apply();
    }

    /** بازگشت به کف پویا از فرمول */
    public final void clearAnalysisFloorOverride(Context c) {
        if (c == null) return;
        float def = computeDefaultAnalysisFloor(c);
        prefs(c).edit()
                .putBoolean("analysis_floor_overridden", false)
                .putFloat("analysis_floor", def)
                .apply();
    }

    public final EngineSettings settings(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        SharedPreferences prefs = prefs(c);
        double effectiveSpeed = getEffectiveSpeed(c, prefs.getFloat("speed", 60.0f));
        double d = prefs.getFloat("fuel_per_100", 7.5f);
        double d2 = prefs.getFloat("fuel_price", 5000.0f);
        double d3 = prefs.getFloat("dep_per_km", 2100.0f);
        double d4 = prefs.getFloat("commission", 22.0f);
        double d5 = prefs.getFloat("buffer", 10.0f);
        float f = prefs.getFloat("target", 220000.0f);
        float f2 = prefs.getFloat("driver_gross_hourly", 0.0f);
        if (f2 > 0.0f && f < 0.5f * f2) {
            f = f2;
        }
        // هدف کاربر ثابت — کاهش موقت ۱۰٪ بعد از ۳ رد حذف شد (میانگین ساعتی جایگزین است)
        try {
            if (prefs.getBoolean("temp_target_active", false)) {
                prefs.edit().putBoolean("temp_target_active", false).remove("temp_target").apply();
            }
        } catch (Throwable ignored) {}
        return new EngineSettings(effectiveSpeed, d, d2, d3, d4, d5, f);
    }

    public final void saveSettings(Context c, EngineSettings s) {
        Intrinsics.checkNotNullParameter(c, "c");
        Intrinsics.checkNotNullParameter(s, "s");
        prefs(c).edit().putFloat("speed", (float) s.getSpeedKmh()).putFloat("fuel_per_100", (float) s.getFuelPer100()).putFloat("fuel_price", (float) s.getFuelPrice()).putFloat("dep_per_km", (float) s.getDepPerKm()).putFloat("commission", (float) s.getCommissionPct()).putFloat("buffer", (float) s.getBufferPct()).putFloat("target", (float) s.getTargetHourly()).apply();
    }

    public final boolean isOnboardingDone(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        return prefs(c).getBoolean("onboarding_done", false);
    }

    public final void setOnboardingDone(Context c, boolean v) {
        Intrinsics.checkNotNullParameter(c, "c");
        prefs(c).edit().putBoolean("onboarding_done", v).apply();
    }

    public final boolean isPermissionsWizardDone(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        return prefs(c).getBoolean("permissions_wizard_done", false);
    }

    public final void setPermissionsWizardDone(Context c, boolean v) {
        Intrinsics.checkNotNullParameter(c, "c");
        prefs(c).edit().putBoolean("permissions_wizard_done", v).apply();
    }

    public final boolean isAnalysisEnabled(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        return prefs(c).getBoolean("analysis_enabled", true);
    }

    public final void setAnalysisEnabled(Context c, boolean v) {
        Intrinsics.checkNotNullParameter(c, "c");
        prefs(c).edit().putBoolean("analysis_enabled", v).apply();
    }

    /** ثبت امضای جدید بدون چک (برای اجبار تحلیل بعد از رسیدن کیلومتر واقعی) */
    public final void forceFresh(Context c, String sig, long now) {
        Intrinsics.checkNotNullParameter(c, "c");
        Intrinsics.checkNotNullParameter(sig, "sig");
        prefs(c).edit().putString(KEY_DEDUP_SIG, sig).putLong(KEY_DEDUP_TS, now).apply();
    }

    public final boolean isFresh(Context c, String sig, long now) {
        Intrinsics.checkNotNullParameter(c, "c");
        Intrinsics.checkNotNullParameter(sig, "sig");
        SharedPreferences prefs = prefs(c);
        boolean z = !Intrinsics.areEqual(prefs.getString(KEY_DEDUP_SIG, null), sig) || now - prefs.getLong(KEY_DEDUP_TS, Long.MIN_VALUE) > 60000;
        if (z) {
            prefs.edit().putString(KEY_DEDUP_SIG, sig).putLong(KEY_DEDUP_TS, now).apply();
        }
        return z;
    }

    public final int nextNotifId(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        SharedPreferences prefs = prefs(c);
        int i = (prefs.getInt(KEY_NOTIF_SEQ, 0) + 1) & 1048575;
        prefs.edit().putInt(KEY_NOTIF_SEQ, i).apply();
        return i;
    }

    public final JSONObject last(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        String string = prefs(c).getString(KEY_LAST, null);
        if (string == null) return null;
        try {
            return new JSONObject(string);
        } catch (Throwable th) {
            return null;
        }
    }

    public final List<JSONObject> recent(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        org.json.JSONArray arr;
        try {
            arr = new JSONArray(prefs(c).getString(KEY_RECENT, "[]"));
        } catch (Throwable th) {
            arr = new JSONArray();
        }
        java.util.ArrayList<JSONObject> list = new java.util.ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            try {
                list.add(arr.getJSONObject(i));
            } catch (Throwable ignored) {}
        }
        return list;
    }

    public final JSONObject recordFrom(TripResult r, EngineSettings s, NotificationParser.Parsed parsed, long ts) {
        Intrinsics.checkNotNullParameter(r, "r");
        Intrinsics.checkNotNullParameter(s, "s");
        Intrinsics.checkNotNullParameter(parsed, "parsed");
        JSONObject jSONObject = new JSONObject();
        try {
        jSONObject.put("ts", ts);
        jSONObject.put("verdict", r.getVerdict().name());
        jSONObject.put("confidence", r.getConfidence().name());
        Object fare = parsed.getFare();
        if (fare == null) {
            fare = JSONObject.NULL;
        }
        jSONObject.put("fare", fare);
        Object pickupKm = parsed.getPickupKm();
        if (pickupKm == null) {
            pickupKm = JSONObject.NULL;
        }
        jSONObject.put("pickupKm", pickupKm);
        Object tripKm = parsed.getTripKm();
        if (tripKm == null) {
            tripKm = JSONObject.NULL;
        }
        jSONObject.put("tripKm", tripKm);
        Object pickupMin = parsed.getPickupMin();
        if (pickupMin == null) {
            pickupMin = JSONObject.NULL;
        }
        jSONObject.put("pickupMin", pickupMin);
        Object tripMin = parsed.getTripMin();
        if (tripMin == null) {
            tripMin = JSONObject.NULL;
        }
        jSONObject.put("tripMin", tripMin);
        jSONObject.put("distSrc", r.getDistanceSource().name());
        jSONObject.put("timeSrc", r.getTimeSource().name());
        if (r.getCommission() != null) {
            jSONObject.put("commission", r.getCommission().doubleValue());
        }
        if (r.getNetAfterCommission() != null) {
            jSONObject.put("netAfter", r.getNetAfterCommission().doubleValue());
        }
        if (r.getFuelCost() != null) {
            jSONObject.put("fuel", r.getFuelCost().doubleValue());
        }
        if (r.getDepCost() != null) {
            jSONObject.put("dep", r.getDepCost().doubleValue());
        }
        if (r.getTotalCost() != null) {
            jSONObject.put("totalCost", r.getTotalCost().doubleValue());
        }
        if (r.getNetIncome() != null) {
            jSONObject.put("net", r.getNetIncome().doubleValue());
        }
        if (r.getTotalDistanceKm() != null) {
            jSONObject.put("dist", r.getTotalDistanceKm().doubleValue());
        }
        if (r.getRawTimeMin() != null) {
            jSONObject.put("rawTime", r.getRawTimeMin().doubleValue());
        }
        if (r.getAdjustedTimeMin() != null) {
            jSONObject.put("adjusted", r.getAdjustedTimeMin().doubleValue());
        }
        if (r.getHourlyIncome() != null) {
            jSONObject.put("hourly", r.getHourlyIncome().doubleValue());
        }
        if (r.getPerKmIncome() != null) {
            jSONObject.put("perKm", r.getPerKmIncome().doubleValue());
        }
        List<YellowReason> yellowReasons = r.getYellowReasons();
        ArrayList arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(yellowReasons, 10));
        Iterator<YellowReason> it = yellowReasons.iterator();
        while (it.hasNext()) {
            arrayList.add(((YellowReason) it.next()).name());
        }
        jSONObject.put("reasons", new JSONArray((Collection) arrayList));
        if (r.getRejectReason() != null) {
            jSONObject.put("reject", r.getRejectReason().name());
        }
        jSONObject.put("snapCommission", s.getCommissionPct());
        jSONObject.put("snapFuelPrice", s.getFuelPrice());
        jSONObject.put("snapFuelPer100", s.getFuelPer100());
        jSONObject.put("snapDepPerKm", s.getDepPerKm());
        jSONObject.put("snapBuffer", s.getBufferPct());
        jSONObject.put("snapTarget", s.getTargetHourly());
        } catch (Exception e) { /* JSONObject.put checked */ }
        return jSONObject;
    }

    public final Pair<TripResult, Long> parseResult(JSONObject rec) {
        Intrinsics.checkNotNullParameter(rec, "rec");
        try {
        rec.optDouble("fare", Double.NaN);
        String string = rec.getString("verdict");
        Intrinsics.checkNotNullExpressionValue(string, "getString(...)");
        Verdict valueOf = Verdict.valueOf(string);
        JSONArray optJSONArray = rec.optJSONArray("reasons");
        IntRange until = RangesKt.until(0, optJSONArray != null ? optJSONArray.length() : 0);
        ArrayList arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(until, 10));
        Iterator<Integer> it = until.iterator();
        while (it.hasNext()) {
            int nextInt = ((IntIterator) it).nextInt();
            Intrinsics.checkNotNull(optJSONArray);
            String string2 = optJSONArray.getString(nextInt);
            Intrinsics.checkNotNullExpressionValue(string2, "getString(...)");
            arrayList.add(YellowReason.valueOf(string2));
        }
        ArrayList arrayList2 = arrayList;
        String string3 = rec.getString("confidence");
        Intrinsics.checkNotNullExpressionValue(string3, "getString(...)");
        Confidence valueOf2 = Confidence.valueOf(string3);
        String optString = rec.optString("reject", "");
        Intrinsics.checkNotNull(optString);
        if (!(optString.length() > 0)) {
            optString = null;
        }
        RejectReason valueOf3 = optString != null ? RejectReason.valueOf(optString) : null;
        Double optDoubleOrNull = optDoubleOrNull(rec, "commission");
        Double optDoubleOrNull2 = optDoubleOrNull(rec, "netAfter");
        Double optDoubleOrNull3 = optDoubleOrNull(rec, "fuel");
        Double optDoubleOrNull4 = optDoubleOrNull(rec, "dep");
        Double optDoubleOrNull5 = optDoubleOrNull(rec, "totalCost");
        Double optDoubleOrNull6 = optDoubleOrNull(rec, "net");
        Double optDoubleOrNull7 = optDoubleOrNull(rec, "dist");
        Double optDoubleOrNull8 = optDoubleOrNull(rec, "rawTime");
        Double optDoubleOrNull9 = optDoubleOrNull(rec, "adjusted");
        Double optDoubleOrNull10 = optDoubleOrNull(rec, "hourly");
        Double optDoubleOrNull11 = optDoubleOrNull(rec, "perKm");
        String string4 = rec.getString("distSrc");
        Intrinsics.checkNotNullExpressionValue(string4, "getString(...)");
        DataSource valueOf4 = DataSource.valueOf(string4);
        String string5 = rec.getString("timeSrc");
        Intrinsics.checkNotNullExpressionValue(string5, "getString(...)");
        return TuplesKt.to(new TripResult(valueOf, valueOf2, arrayList2, valueOf3, optDoubleOrNull, optDoubleOrNull2, optDoubleOrNull3, optDoubleOrNull4, optDoubleOrNull5, optDoubleOrNull6, optDoubleOrNull7, optDoubleOrNull8, optDoubleOrNull9, optDoubleOrNull10, optDoubleOrNull11, valueOf4, DataSource.valueOf(string5)), Long.valueOf(rec.getLong("ts")));
        } catch (org.json.JSONException e) { throw new RuntimeException(e); }
    }

    private final Double optDoubleOrNull(JSONObject jSONObject, String str) {
        if (!jSONObject.has(str) || jSONObject.isNull(str)) {
            return null;
        }
        try { return Double.valueOf(jSONObject.getDouble(str)); } catch (Exception e) { return null; }
    }

    public final File errorReportsDir(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        return c.getDir("error_reports", 0);
    }

    public final String saveErrorReport(Context c, String rawText, String parsedSummary) {
        Intrinsics.checkNotNullParameter(c, "c");
        Intrinsics.checkNotNullParameter(rawText, "rawText");
        Intrinsics.checkNotNullParameter(parsedSummary, "parsedSummary");
        File errorReportsDir = errorReportsDir(c);
        errorReportsDir.mkdirs();
        File file = new File(errorReportsDir, "report-" + System.currentTimeMillis() + ".txt");
        try {
            java.io.FileWriter fw = new java.io.FileWriter(file);
            fw.write("== متن خام (با اقدام صریح کاربر ذخیره شد) ==\n" + rawText + "\n\n== خلاصه پارس ==\n" + parsedSummary + "\n");
            fw.close();
        } catch (Exception ignored) {}
        String name = file.getName();
        Intrinsics.checkNotNullExpressionValue(name, "getName(...)");
        return name;
    }

    public final List<File> listErrorReports(Context c) {
        List<File> sortedWith;
        Intrinsics.checkNotNullParameter(c, "c");
        File[] listFiles = errorReportsDir(c).listFiles();
        return (listFiles == null || (sortedWith = ArraysKt.sortedWith(listFiles, new Comparator() { // from class: ir.tripanalyzer.app.Store$listErrorReports$$inlined$sortedByDescending$1
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.util.Comparator
            public final int compare(Object t, Object t2) {
                return ComparisonsKt.compareValues(((File) t2).getName(), ((File) t).getName());
            }
        })) == null) ? CollectionsKt.emptyList() : sortedWith;
    }

    public final void eraseEverything(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        prefs(c).edit().clear().apply();
        File[] listFiles = errorReportsDir(c).listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                file.delete();
            }
        }
    }

    public final double getBaseTargetHourly(Context c) {
        return prefs(c).getFloat("target", 220000f);
    }

    public final void setBaseTargetHourly(Context c, double v) {
        prefs(c).edit().putFloat("target", (float) v).apply();
    }

    public final int getConsecutiveRejects(Context c) {
        return prefs(c).getInt("consec_rejects", 0);
    }

    public final void recordVerdict(Context c, Verdict v) {
        // شمارنده ۳ رد برای کاهش هدف دیگر استفاده نمی‌شود
        if (v == null) return;
        try { prefs(c).edit().putInt("consec_rejects", 0).apply(); } catch (Throwable ignored) {}
    }

    public final void resetConsecutiveRejects(Context c) {
        prefs(c).edit().putInt("consec_rejects", 0).apply();
    }

    public final boolean isTempTargetActive(Context c) {
        return prefs(c).getBoolean("temp_target_active", false);
    }

    public final void activateTempTarget10pct(Context c) {
        // غیرفعال: دیگر هدف ۱۰٪ کم نمی‌شود
        try { clearTempTarget(c); resetConsecutiveRejects(c); } catch (Throwable ignored) {}
    }

    public final void clearTempTarget(Context c) {
        SharedPreferences.Editor e = prefs(c).edit();
        e.putBoolean("temp_target_active", false);
        e.remove("temp_target");
        // هدف اصلی را برنگردانیم مگر target_base_saved داشته باشیم — target همان فیلد کاربر است
        e.apply();
    }

    public final void setPendingConfirm(Context c, long ts, double fare) {
        prefs(c).edit()
                .putLong("pending_confirm_ts", ts)
                .putFloat("pending_confirm_fare", (float) fare)
                .putLong("pending_confirm_at", System.currentTimeMillis())
                .apply();
    }

    public final long getPendingConfirmTs(Context c) {
        return prefs(c).getLong("pending_confirm_ts", 0L);
    }

    public final double getPendingConfirmFare(Context c) {
        return prefs(c).getFloat("pending_confirm_fare", 0f);
    }

    public final long getPendingConfirmAt(Context c) {
        return prefs(c).getLong("pending_confirm_at", 0L);
    }

    public final void clearPendingConfirm(Context c) {
        prefs(c).edit()
                .remove("pending_confirm_ts")
                .remove("pending_confirm_fare")
                .remove("pending_confirm_at")
                .apply();
    }


    /** سفر جاری که باید با «پایان سفر» ثبت شود (نه آخرین تحلیل حین سفر). */
    public final void saveActiveTrip(Context c, org.json.JSONObject rec) {
        if (c == null || rec == null) return;
        try {
            prefs(c).edit().putString("active_trip_json", rec.toString()).apply();
        } catch (Exception ignored) {}
    }

    public final org.json.JSONObject getActiveTrip(Context c) {
        try {
            String s = prefs(c).getString("active_trip_json", null);
            if (s == null || s.length() == 0) return null;
            return new org.json.JSONObject(s);
        } catch (Exception e) {
            return null;
        }
    }

    public final void clearActiveTrip(Context c) {
        try {
            prefs(c).edit().remove("active_trip_json").apply();
        } catch (Exception ignored) {}
    }

    public final void saveToRecentFromLast(Context c) {
        try {
            org.json.JSONObject last = last(c);
            if (last != null) saveAccepted(c, last);
        } catch (Exception ignored) {}
    }

}
