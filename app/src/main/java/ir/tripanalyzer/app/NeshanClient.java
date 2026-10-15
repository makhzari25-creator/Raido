package ir.tripanalyzer.app;

import android.content.Context;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    static String apiKey() {
        String real = "service.071a7f18b6224ab7a45e64076c4483f6";
        StringBuilder obf = new StringBuilder();
        for (int i = 0; i < real.length(); i++) {
            obf.append((char) (real.charAt(i) ^ 0x5A));
        }
        String stored = obf.toString();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < stored.length(); i++) {
            out.append((char) (stored.charAt(i) ^ 0x5A));
        }
        return out.toString();
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
        final String endpoint = "https://api.neshan.org/v4/geocoding";
        if (address == null || address.trim().length() < 4) {
            return GeocodeResult.skip("آدرس کوتاه/خالی");
        }
        if (ctx != null && !LicenseStore.INSTANCE.isPremium(ctx)) {
            return GeocodeResult.skip("غیرپرمیوم");
        }
        String urlStr = endpoint + "?address=" + Uri.encode(address.trim());
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Api-Key", apiKey());
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(8000);
            int code;
            try {
                code = conn.getResponseCode();
            } catch (Throwable net) {
                return new GeocodeResult(true, false, 0, null, null, null,
                        "شبکه/اتصال به نشان برقرار نشد", endpoint);
            }
            if (code != 200) {
                String errBody = readQuiet(conn);
                return new GeocodeResult(true, false, code, null, null, null,
                        "HTTP " + code + (errBody != null && errBody.length() > 0
                                ? (" · " + errBody.substring(0, Math.min(80, errBody.length())))
                                : ""),
                        endpoint);
            }
            String body = readQuiet(conn);
            if (body == null || body.isEmpty()) {
                return new GeocodeResult(true, false, code, null, null, null,
                        "پاسخ خالی از نشان", endpoint);
            }
            JSONObject json = new JSONObject(body);
            String st = json.optString("status", "");
            if (!"OK".equalsIgnoreCase(st)) {
                return new GeocodeResult(true, false, code, st, null, null,
                        "status نشان: " + (st.isEmpty() ? "خالی" : st), endpoint);
            }
            JSONObject loc = json.optJSONObject("location");
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
            return new GeocodeResult(true, true, code, st, lat, lng, null, endpoint);
        } catch (Throwable t) {
            String msg = t.getMessage();
            if (msg == null || msg.isEmpty()) msg = t.getClass().getSimpleName();
            if (msg.length() > 80) msg = msg.substring(0, 80);
            return new GeocodeResult(true, false, 0, null, null, null,
                    "خطا: " + msg, endpoint);
        } finally {
            if (conn != null) {
                try { conn.disconnect(); } catch (Throwable ignored) {}
            }
        }
    }

    private static String readQuiet(HttpURLConnection conn) {
        try {
            java.io.InputStream is;
            try {
                is = conn.getInputStream();
            } catch (Throwable t) {
                is = conn.getErrorStream();
            }
            if (is == null) return null;
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            return sb.toString();
        } catch (Throwable t) {
            return null;
        }
    }
}
