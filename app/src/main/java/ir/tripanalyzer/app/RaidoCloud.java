package ir.tripanalyzer.app;

import android.util.Base64;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

/**
 * ارتباط با بک‌اند Supabase (Edge Functions). بدون SDK تا حجم و باتری کم بماند.
 * همهٔ متدها blocking هستند؛ فقط از ترد پس‌زمینه صدا بزنید.
 */
final class RaidoCloud {
    static final String BASE = "https://atzyjiljtzxhctblziti.supabase.co/functions/v1/";
    /** کلید publishable (عمومی). */
    static final String APIKEY = "sb_publishable_uls5J9duWsSEDIUY5WmmRA_Ik5v1oNa";
    /** کلید عمومی RSA برای تأیید امضای وضعیت اشتراک (base64 از X.509 DER). */
    static final String PUBLIC_KEY_B64 =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvkAgJmKRqmgjCAcpAvRoNs7iCBLqTdU7Yo83kWw+X2K+LWGwF7ivZWF8ltIZKhWcpFnsXHaDB8YNToJmiRNMvzhjDf4g26orw1Wm6kNrfkafyTCHRuzDwv4nX0gp7mCswTmwag7f02qxX+Amu+ysD6Q8ZnNVEYZ8ZXKDYma12Ns8PSA9yeN1cQQdGENlOqg5afbX98TR6YskzHwqiRzYmX+ludy6rbwm00sr89vOxkgOTNlw9XN2Fa54ZTxXRY4Rm6HGaI8ejPIVQNprr885SXWyQ1veYTzkGQhngQ+F5E4cOLpKRxJVXCB3biKAbc2FVjLcECB+qpTBWZUk4CKxlwIDAQAB";

    private RaidoCloud() {}

    /** نتیجه: JSON پاسخ + کلید "_http" با کد HTTP. در خطای شبکه Exception پرتاب می‌شود. */
    static JSONObject post(String fn, JSONObject body, String token) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(BASE + fn).openConnection();
        try {
            c.setConnectTimeout(8000);
            c.setReadTimeout(12000);
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            c.setRequestProperty("apikey", APIKEY);
            if (token != null) c.setRequestProperty("x-raido-token", token);
            byte[] data = (body == null ? "{}" : body.toString()).getBytes("UTF-8");
            try (OutputStream os = c.getOutputStream()) { os.write(data); }
            int code = c.getResponseCode();
            InputStream is = code >= 400 ? c.getErrorStream() : c.getInputStream();
            String txt = "";
            if (is != null) {
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n;
                while ((n = is.read(buf)) > 0) bo.write(buf, 0, n);
                txt = bo.toString("UTF-8");
                is.close();
            }
            JSONObject j;
            try { j = new JSONObject(txt); } catch (Exception e) { j = new JSONObject(); }
            j.put("_http", code);
            return j;
        } finally {
            c.disconnect();
        }
    }

    private static volatile PublicKey cachedKey;

    private static PublicKey key() throws Exception {
        PublicKey k = cachedKey;
        if (k == null) {
            byte[] der = Base64.decode(PUBLIC_KEY_B64, Base64.DEFAULT);
            k = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
            cachedKey = k;
        }
        return k;
    }

    /** تأیید امضا؛ اگر کلید عمومی تنظیم نشده باشد false (fail-closed). */
    static boolean verify(String payload, String sigB64) {
        try {
            if (PUBLIC_KEY_B64.isEmpty() || payload == null || sigB64 == null) return false;
            Signature s = Signature.getInstance("SHA256withRSA");
            s.initVerify(key());
            s.update(payload.getBytes("UTF-8"));
            return s.verify(Base64.decode(sigB64, Base64.DEFAULT));
        } catch (Throwable t) {
            return false;
        }
    }
}
