package ir.tripanalyzer.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

/**
 * محدوده فعالیت — مرکز GPS/نقشه + شعاع + فایل نقشه آپلودی (جدا از APK).
 * بدون فایل نقشه، محدوده «آماده» نیست و تحلیل به مسیر عادی برمی‌گردد.
 */
public final class ActivityZoneStore {
    public static final ActivityZoneStore INSTANCE = new ActivityZoneStore();
    private static final String PREF = "raido_activity_zone";
    public static final String MAP_FILE_NAME = "map.mbtiles";

    private ActivityZoneStore() {}

    private SharedPreferences p(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public boolean isEnabled(Context c) {
        return p(c).getBoolean("enabled", false);
    }

    public void setEnabled(Context c, boolean v) {
        p(c).edit().putBoolean("enabled", v).apply();
    }

    public String getCity(Context c) {
        return p(c).getString("city", "");
    }

    public String getLandmark(Context c) {
        return p(c).getString("landmark", "");
    }

    public double getLat(Context c) {
        return Double.longBitsToDouble(p(c).getLong("lat", Double.doubleToRawLongBits(0)));
    }

    public double getLng(Context c) {
        return Double.longBitsToDouble(p(c).getLong("lng", Double.doubleToRawLongBits(0)));
    }

    public double getRadiusKm(Context c) {
        return Double.longBitsToDouble(p(c).getLong("radius_km", Double.doubleToRawLongBits(5.0)));
    }

    public boolean isStrictRed(Context c) {
        return p(c).getBoolean("strict_red", false);
    }

    public void setStrictRed(Context c, boolean v) {
        p(c).edit().putBoolean("strict_red", v).apply();
    }

    public boolean hasPoint(Context c) {
        double lat = getLat(c);
        double lng = getLng(c);
        return Math.abs(lat) > 0.1 && Math.abs(lng) > 0.1;
    }

    public void savePoint(Context c, String city, String landmark, double lat, double lng, double radiusKm) {
        p(c).edit()
                .putString("city", city != null ? city : "")
                .putString("landmark", landmark != null ? landmark : "")
                .putLong("lat", Double.doubleToRawLongBits(lat))
                .putLong("lng", Double.doubleToRawLongBits(lng))
                .putLong("radius_km", Double.doubleToRawLongBits(radiusKm <= 0 ? 5.0 : radiusKm))
                .apply();
    }

    public void saveMapCenter(Context c, double lat, double lng, double radiusKm, String source) {
        p(c).edit()
                .putString("city", "نقشه")
                .putString("landmark", source != null ? source : "map")
                .putLong("lat", Double.doubleToRawLongBits(lat))
                .putLong("lng", Double.doubleToRawLongBits(lng))
                .putLong("radius_km", Double.doubleToRawLongBits(radiusKm <= 0 ? 5.0 : radiusKm))
                .apply();
    }

    public void setMapDisplayName(Context c, String name) {
        p(c).edit().putString("map_display_name", name != null ? name : "").apply();
    }

    public String getMapDisplayName(Context c) {
        return p(c).getString("map_display_name", "");
    }

    public File mapFile(Context c) {
        File dir = new File(c.getApplicationContext().getFilesDir(), "zone_map");
        return new File(dir, MAP_FILE_NAME);
    }

    public boolean hasMapFile(Context c) {
        File f = mapFile(c);
        return f.isFile() && f.length() > 100_000L;
    }

    /**
     * محدوده واقعاً روی تحلیل اثر می‌گذارد؟
     * نیاز: سوییچ روشن + پرمیوم + فایل نقشه + نقطه مرکز.
     */
    public boolean isZoneActiveForAnalysis(Context c) {
        return isEnabled(c)
                && LicenseStore.INSTANCE.isPremium(c)
                && hasMapFile(c)
                && hasPoint(c);
    }

    public String summary(Context c) {
        if (!isEnabled(c)) return "خاموش";
        if (!LicenseStore.INSTANCE.isPremium(c)) return "روشن · نیاز به پرمیوم";
        if (!hasMapFile(c)) return "روشن · نقشه آپلود نشده";
        if (!hasPoint(c)) return "روشن · مرکز تنظیم نشده";
        double r = getRadiusKm(c);
        String rs = (r == Math.floor(r)) ? String.valueOf((int) r) : String.valueOf(r);
        String mapName = getMapDisplayName(c);
        String prefix = mapName.isEmpty() ? "نقشه" : mapName;
        return "فعال · " + prefix + " · شعاع " + rs + " کم";
    }
}
