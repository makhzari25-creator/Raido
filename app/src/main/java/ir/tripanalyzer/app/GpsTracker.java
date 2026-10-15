package ir.tripanalyzer.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * ردیاب GPS — فقط جابه‌جایی واقعی با دقت قابل‌قبول شمرده می‌شود
 * تا وقتی گوشی ثابت است عدد بالا نرود.
 */
public final class GpsTracker implements LocationListener {
    private static Context appCtx;
    public static final GpsTracker INSTANCE = new GpsTracker();
    private static boolean started = false;
    /** شروع جلسه فعلی GPS (برای میانگین سرعت) */
    private static long sessionStartMs = 0L;

    /** حداقل جابه‌جایی معتبر (متر) — نویز شبکه/GPS را حذف می‌کند */
    private static final float MIN_MOVE_METERS = 25.0f;
    /** حداکثر accuracy قابل قبول (متر) */
    private static final float MAX_ACCURACY_METERS = 40.0f;
    /** حداکثر سرعت معقول (متر/ثانیه) ~ 180 km/h — برای رد پرش‌های اشتباه */
    private static final float MAX_SPEED_MPS = 50.0f;

    private GpsTracker() {}

    private SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("gps_store", 0);
    }

    public double getKm(Context context) {
        return prefs(context).getFloat("meters", 0.0f) / 1000.0d;
    }

    @Override
    public void onLocationChanged(Location location) {
        Context context = appCtx;
        if (location == null || context == null) return;

        // رد نقاط با دقت بد
        if (location.hasAccuracy() && location.getAccuracy() > MAX_ACCURACY_METERS) {
            return;
        }

        double lat = location.getLatitude();
        double lng = location.getLongitude();
        SharedPreferences prefs = prefs(context);

        if (prefs.getBoolean("has_last", false)) {
            float lastLat = prefs.getFloat("last_lat", 0f);
            float lastLng = prefs.getFloat("last_lng", 0f);
            long lastTs = prefs.getLong("last_ts", 0L);

            float[] results = new float[1];
            Location.distanceBetween(lastLat, lastLng, lat, lng, results);
            float delta = results[0];

            long now = location.getTime() > 0 ? location.getTime() : System.currentTimeMillis();
            long dtMs = lastTs > 0 ? Math.max(1L, now - lastTs) : 60000L;
            float speed = delta / (dtMs / 1000.0f);

            // فقط حرکت واقعی و بدون پرش غیرمنطقی
            if (delta >= MIN_MOVE_METERS && speed <= MAX_SPEED_MPS) {
                float meters = prefs.getFloat("meters", 0f) + delta;
                SharedPreferences.Editor edit = prefs.edit();
                edit.putFloat("meters", meters);
                edit.putFloat("last_lat", (float) lat);
                edit.putFloat("last_lng", (float) lng);
                edit.putLong("last_ts", now);
                edit.putBoolean("has_last", true);
                edit.apply();
                return;
            }
            // جابه‌جایی کوچک: فقط last را به‌روز کن (بدون جمع مسافت)
            if (delta < MIN_MOVE_METERS) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.putFloat("last_lat", (float) lat);
                edit.putFloat("last_lng", (float) lng);
                edit.putLong("last_ts", now);
                edit.apply();
                return;
            }
        }

        SharedPreferences.Editor edit = prefs.edit();
        edit.putFloat("last_lat", (float) lat);
        edit.putFloat("last_lng", (float) lng);
        edit.putLong("last_ts", location.getTime() > 0 ? location.getTime() : System.currentTimeMillis());
        edit.putBoolean("has_last", true);
        edit.apply();
    }

    @Override public void onProviderDisabled(String s) {}
    @Override public void onProviderEnabled(String s) {}
    @Override public void onStatusChanged(String s, int i, Bundle b) {}

    public void reset(Context context) {
        SharedPreferences.Editor edit = prefs(context).edit();
        edit.putFloat("meters", 0.0f);
        edit.putBoolean("has_last", false);
        edit.putLong("last_ts", 0L);
        edit.apply();
        // با صفر کردن مسافت، مبدأ زمان سرعت هم از نو
        if (started) sessionStartMs = System.currentTimeMillis();
    }

    public boolean isRunning() {
        return started;
    }

    /**
     * میانگین سرعت جلسه فعلی (km/h) =
     * مسافت ÷ مدت از start/reset تا الان.
     * اگر GPS خاموش یا مدت خیلی کم یا مسافت ۰ → -1
     */
    public double getAvgSpeedKmh(Context context) {
        if (!started || context == null) return -1;
        if (sessionStartMs <= 0) return -1;
        double km = getKm(context);
        if (km <= 0.01) return -1;
        long elapsed = System.currentTimeMillis() - sessionStartMs;
        if (elapsed < 120_000L) return -1; // کمتر از ۲ دقیقه
        double hours = elapsed / 3_600_000.0;
        if (hours <= 0) return -1;
        return km / hours;
    }

    public void start(Context context) {
        if (context == null) return;
        appCtx = context.getApplicationContext();
        if (started) return;
        try {
            // GPS فقط وقتی تحلیل روشن است
            if (!Store.INSTANCE.isAnalysisEnabled(appCtx)) {
                return;
            }
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm == null) return;
            // فاصله/زمان بیشتر = مصرف باتری کمتر (۱۵ثانیه / ۴۰متر)
            try {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000L, 40.0f, this);
            } catch (Exception ignored) {}
            started = true;
            if (sessionStartMs <= 0) sessionStartMs = System.currentTimeMillis();
        } catch (Exception ignored) {}
    }

    public void stop(Context context) {
        try {
            if (context == null) context = appCtx;
            if (context != null) {
                LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (lm != null) lm.removeUpdates(this);
            }
        } catch (Exception ignored) {}
        started = false;
        sessionStartMs = 0L;
    }
}
