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
    private static final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.CompletableFuture<GeocodeResult>> INFLIGHT =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static boolean usable(String a) {
        return a != null && a.trim().length() >= 4;
    }

    /**
     * پیش‌دریافت: ژئوکد مبدأ و مقصد در «یک» درخواست و در پس‌زمینه شروع می‌شود (قبل از بقیهٔ مراحل تحلیل).
     * geocodeDetailed بعداً از همین نتیجه استفاده می‌کند و درخواست تازه نمی‌زند.
     */
    public void prefetch(final Context ctx, String origin, String dest) {
        if (ctx == null || !LicenseStore.INSTANCE.isPremium(ctx)) return;
        final java.util.List<String> todo = new java.util.ArrayList<>();
        final java.util.List<java.util.concurrent.CompletableFuture<GeocodeResult>> futs = new java.util.ArrayList<>();
        for (String a : new String[]{dest, origin}) {
            if (!usable(a)) continue;
            String k = GeoCache.norm(a);
            if (GeoCache.INSTANCE.get(ctx, a) != null) continue;
            java.util.concurrent.CompletableFuture<GeocodeResult> f = new java.util.concurrent.CompletableFuture<>();
            if (INFLIGHT.putIfAbsent(k, f) != null) continue;
            todo.add(a.trim());
            futs.add(f);
        }
        if (todo.isEmpty()) return;
        POOL.submit(new Runnable() {
            @Override public void run() {
                java.util.List<GeocodeResult> res = null;
                try { res = networkBatch(ctx, todo); } catch (Throwable ignored) {}
                for (int i = 0; i < todo.size(); i++) {
                    GeocodeResult r = (res != null && i < res.size() && res.get(i) != null) ? res.get(i)
                            : new GeocodeResult(true, false, 0, null, null, null, "شبکه/اتصال به سرور برقرار نشد", "raido-cloud/geocode");
                    futs.get(i).complete(r);
                    INFLIGHT.remove(GeoCache.norm(todo.get(i)), futs.get(i));
                }
            }
        });
    }

    public GeocodeResult geocodeDetailed(Context ctx, String address) {
        final String endpoint = "raido-cloud/geocode";
        if (!usable(address)) {
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
        // ۲) پیش‌دریافتِ در حال انجام برای همین آدرس
        java.util.concurrent.CompletableFuture<GeocodeResult> pf = INFLIGHT.get(GeoCache.norm(address));
        if (pf != null) {
            try {
                return pf.get(5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Throwable t) {
                return new GeocodeResult(true, false, 0, null, null, null, "ژئوکد به‌موقع تمام نشد", endpoint);
            }
        }
        java.util.List<String> one = new java.util.ArrayList<>();
        one.add(address.trim());
        java.util.List<GeocodeResult> r = networkBatch(ctx, one);
        return (r != null && !r.isEmpty() && r.get(0) != null) ? r.get(0)
                : new GeocodeResult(true, false, 0, null, null, null, "شبکه/اتصال به سرور برقرار نشد", endpoint);
    }

    /** یک درخواست شبکه برای یک یا چند آدرس؛ اگر سرور قدیمی بود تک‌تک. */
    private java.util.List<GeocodeResult> networkBatch(Context ctx, java.util.List<String> addrs) {
        final String endpoint = "raido-cloud/geocode";
        java.util.List<GeocodeResult> out = new java.util.ArrayList<>();
        String token = LicenseStore.INSTANCE.getToken(ctx);
        if (token == null) {
            for (int i = 0; i < addrs.size(); i++) out.add(GeocodeResult.skip("ورود لازم است"));
            return out;
        }
        try {
            org.json.JSONArray arr = new org.json.JSONArray();
            for (String a : addrs) arr.put(a);
            JSONObject r;
            try {
                // timeout کوتاه: اگر سرور دیر جواب داد، تحلیل عادی بدون لایهٔ نشان نمایش داده می‌شود
                r = RaidoCloud.post("geocode", new JSONObject().put("addresses", arr), token, 2500, 3500);
            } catch (Throwable net) {
                for (int i = 0; i < addrs.size(); i++)
                    out.add(new GeocodeResult(true, false, 0, null, null, null, "شبکه/اتصال به سرور برقرار نشد", endpoint));
                return out;
            }
            int code = r.optInt("_http");
            if (code == 401) {
                LicenseStore.INSTANCE.refreshStatus(ctx); // خروج اجباری + ثبت دلیل
                for (int i = 0; i < addrs.size(); i++)
                    out.add(new GeocodeResult(true, false, code, null, null, null, "نشست نامعتبر", endpoint));
                return out;
            }
            org.json.JSONArray results = r.optJSONArray("results");
            if (code != 200 || results == null) {
                String why = r.optString("error", "");
                for (int i = 0; i < addrs.size(); i++)
                    out.add(new GeocodeResult(true, false, code, null, null, null,
                            "HTTP " + code + (why.isEmpty() ? "" : " · " + why), endpoint));
                return out;
            }
            for (int i = 0; i < addrs.size(); i++) {
                JSONObject it = i < results.length() ? results.optJSONObject(i) : null;
                out.add(parseItem(ctx, addrs.get(i), it, endpoint));
            }
        } catch (Throwable t) {
            String msg = t.getMessage();
            if (msg == null || msg.isEmpty()) msg = t.getClass().getSimpleName();
            if (msg.length() > 80) msg = msg.substring(0, 80);
            out.clear();
            for (int i = 0; i < addrs.size(); i++)
                out.add(new GeocodeResult(true, false, 0, null, null, null, "خطا: " + msg, endpoint));
        }
        return out;
    }

    private GeocodeResult parseItem(Context ctx, String address, JSONObject it, String endpoint) {
        if (it == null) return new GeocodeResult(true, false, 200, null, null, null, "پاسخ ناقص", endpoint);
        String err = it.optString("error", "");
        if (!err.isEmpty()) {
            return new GeocodeResult(true, false, it.optInt("http", 0), null, null, null, "سرور: " + err, endpoint);
        }
        String st = it.optString("status", "");
        if (!"OK".equalsIgnoreCase(st)) {
            return new GeocodeResult(true, false, 200, st, null, null,
                    "status نشان: " + (st.isEmpty() ? "خالی" : st), endpoint);
        }
        JSONObject loc = it.optJSONObject("location");
        if (loc == null) {
            return new GeocodeResult(true, false, 200, st, null, null, "بدون location در پاسخ نشان", endpoint);
        }
        double lng = loc.optDouble("x", Double.NaN);
        double lat = loc.optDouble("y", Double.NaN);
        if (Double.isNaN(lat) || Double.isNaN(lng)) {
            return new GeocodeResult(true, false, 200, st, null, null, "مختصات نامعتبر", endpoint);
        }
        if (lat < 25 || lat > 40 || lng < 44 || lng > 64) {
            return new GeocodeResult(true, false, 200, st, lat, lng, "مختصات خارج از محدوده ایران", endpoint);
        }
        GeoCache.INSTANCE.put(ctx, address, lat, lng);
        return new GeocodeResult(true, true, 200, st, lat, lng, null,
                it.optBoolean("cached") ? endpoint + " (کش سرور)" : endpoint);
    }
}
