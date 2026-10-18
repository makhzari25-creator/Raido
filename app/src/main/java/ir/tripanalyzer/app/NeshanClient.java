package ir.tripanalyzer.app;

import android.content.Context;
import org.json.JSONObject;

/**
 * کلاینت ژئوکد نشان — فقط برای Premium.
 * نتیجهٔ غنی برای اثبات فراخوانی واقعی API در جزئیات.
 */
public final class NeshanClient {
    public static final NeshanClient INSTANCE = new NeshanClient();

    public static final class GeoPoint {
        public final double lat;
        public final double lng;
        public GeoPoint(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    /** نتیجهٔ کامل یک درخواست ژئوکد — برای شفافیت در جزئیات */
    public static final class GeocodeResult {
        public final boolean attempted;   // درخواست واقعاً ساخته شد؟
        public final boolean ok;          // مختصات معتبر گرفته شد؟
        public final int httpCode;        // 0 = شبکه نرفت
        public final String apiStatus;    // فیلد status پاسخ نشان (OK/...)
        public final Double lat;
        public final Double lng;
        public final String error;        // علت فارسی/کوتاه
        public final String endpoint;     // برای اثبات مسیر API

        public GeocodeResult(boolean attempted, boolean ok, int httpCode,
                             String apiStatus, Double lat, Double lng,
                             String error, String endpoint) {
            this.attempted = attempted;
            this.ok = ok;
            this.httpCode = httpCode;
            this.apiStatus = apiStatus;
            this.lat = lat;
            this.lng = lng;
            this.error = error;
            this.endpoint = endpoint;
        }

        public static GeocodeResult skip(String reason) {
            return new GeocodeResult(false, false, 0, null, null, null, reason, null);
        }

        public GeoPoint toPoint() {
            if (!ok || lat == null || lng == null) return null;
            return new GeoPoint(lat, lng);
        }
    }

    private NeshanClient() {}

    private static final java.util.concurrent.ExecutorService POOL =
            java.util.concurrent.Executors.newCachedThreadPool(new java.util.concurrent.ThreadFactory() {
                @Override public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "geocode");
                    t.setDaemon(true);
                    return t;
                }
            });

    /** ژئوکد هم‌زمان؛ با get(...) نتیجه گرفته می‌شود. */
    public java.util.concurrent.Future<GeocodeResult> geocodeAsync(final Context ctx, final String address) {
        return POOL.submit(new java.util.concurrent.Callable<GeocodeResult>() {
            @Override public GeocodeResult call() { return geocodeDetailed(ctx, address); }
        });
    }

    public static GeocodeResult await(java.util.concurrent.Future<GeocodeResult> f) {
        try {
            return f.get(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Throwable t) {
            return GeocodeResult.skip("ژئوکد به‌موقع تمام نشد");
        }
    }

    /** سازگاری با کد قبلی — فقط نقطه یا null */
    public GeoPoint geocode(Context ctx, String address) {
        return geocodeDetailed(ctx, address).toPoint();
    }

    /**
     * ژئوکد با جزئیات کامل برای اثبات فراخوانی API.
     * GET https://api.neshan.org/v4/geocoding?address=...
     * Header: Api-Key: ...
     */
    public GeocodeResult geocodeDetailed(Context ctx, String address) {
        final String endpoint = "raido-cloud/geocode";
        if (address == null || address.trim().length() < 4) {
            return GeocodeResult.skip("آدرس کوتاه/خالی");
        }
        if (ctx != null && !LicenseStore.INSTANCE.isPremium(ctx)) {
            return GeocodeResult.skip("غیرپرمیوم");
        }
        // کلید نشان فقط روی سرور است؛ اپ از طریق Edge Function «geocode» درخواست می‌دهد.
        if (ctx == null) return GeocodeResult.skip("بدون context");
        // ۱) کش گوشی: بدون شبکه و فوری
        double[] cached = GeoCache.INSTANCE.get(ctx, address);
        if (cached != null) {
            return new GeocodeResult(true, true, 200, "OK", cached[0], cached[1], null, "raido-cloud/geocode (کش گوشی)");
        }
        String token = LicenseStore.INSTANCE.getToken(ctx);
        if (token == null) return GeocodeResult.skip("ورود لازم است");
        try {
            JSONObject req = new JSONObject().put("address", address.trim());
            JSONObject r;
            try {
                // timeout کوتاه: اگر سرور دیر جواب داد، تحلیل عادی بدون لایهٔ نشان نمایش داده می‌شود
                r = RaidoCloud.post("geocode", req, token, 2500, 3500);
            } catch (Throwable net) {
                return new GeocodeResult(true, false, 0, null, null, null,
                        "شبکه/اتصال به سرور برقرار نشد", endpoint);
            }
            int code = r.optInt("_http");
            if (code == 401) {
                LicenseStore.INSTANCE.refreshStatus(ctx); // خروج اجباری + ثبت دلیل
                return new GeocodeResult(true, false, code, null, null, null, "نشست نامعتبر", endpoint);
            }
            if (code != 200) {
                String why = r.optString("error", "");
                return new GeocodeResult(true, false, code, null, null, null,
                        "HTTP " + code + (why.isEmpty() ? "" : " · " + why), endpoint);
            }
            String st = r.optString("status", "");
            if (!"OK".equalsIgnoreCase(st)) {
                return new GeocodeResult(true, false, code, st, null, null,
                        "status نشان: " + (st.isEmpty() ? "خالی" : st), endpoint);
            }
            JSONObject loc = r.optJSONObject("location");
            if (loc == null) {
                return new GeocodeResult(true, false, code, st, null, null,
                        "بدون location در پاسخ نشان", endpoint);
            }
            // Neshan: x = lng, y = lat
            double lng = loc.getDouble("x");
            double lat = loc.getDouble("y");
            if (lat < 25 || lat > 40 || lng < 44 || lng > 64) {
                return new GeocodeResult(true, false, code, st, lat, lng,
                        "مختصات خارج از محدوده ایران", endpoint);
            }
            GeoCache.INSTANCE.put(ctx, address, lat, lng);
            return new GeocodeResult(true, true, code, st, lat, lng, null,
                    r.optBoolean("cached") ? endpoint + " (کش سرور)" : endpoint);
        } catch (Throwable t) {
            String msg = t.getMessage();
            if (msg == null || msg.isEmpty()) msg = t.getClass().getSimpleName();
            if (msg.length() > 80) msg = msg.substring(0, 80);
            return new GeocodeResult(true, false, 0, null, null, null,
                    "خطا: " + msg, endpoint);
        }
    }
}
