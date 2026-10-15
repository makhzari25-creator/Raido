package ir.tripanalyzer.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * همگام‌سازی ساعت با مرجع داخلی (هدر Date سایت‌های ایرانی).
 * یک‌بار در هر روزی که اپ استفاده می‌شود.
 * اختلاف بیش از ۳۰ دقیقه → قفل لایسنس و هدایت به صفحهٔ رمز.
 */
public final class TimeGuard {
    public static final TimeGuard INSTANCE = new TimeGuard();

    private static final String PREFS = "raido_time_guard";
    private static final String KEY_LAST_CHECK_DAY = "last_check_day"; // yyyyMMdd
    private static final long MAX_SKEW_MS = 30L * 60L * 1000L; // ۳۰ دقیقه

    private static final String[] SOURCES = {
            "https://www.digikala.com",
            "https://www.irancell.ir",
            "https://www.time.ir",
            "https://www.snapp.ir"
    };

    private final AtomicBoolean running = new AtomicBoolean(false);

    public interface Callback {
        void onClockOk();
        void onClockTampered(long networkMs, long deviceMs);
    }

    private TimeGuard() {}

    private SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private String todayKey() {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd", Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("Asia/Tehran"));
        return f.format(System.currentTimeMillis());
    }

    /** اگر امروز هنوز چک نشده و اینترنت در دسترس است، در پس‌زمینه چک کن. */
    public void checkOncePerDay(Context context, Callback cb) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        String today = todayKey();
        if (today.equals(prefs(app).getString(KEY_LAST_CHECK_DAY, ""))) {
            if (cb != null) cb.onClockOk();
            return;
        }
        if (!running.compareAndSet(false, true)) return;

        new Thread(() -> {
            try {
                Long networkMs = fetchNetworkTime();
                prefs(app).edit().putString(KEY_LAST_CHECK_DAY, today).apply();
                if (networkMs == null) {
                    // بدون شبکه → رد شو
                    post(() -> { if (cb != null) cb.onClockOk(); });
                    return;
                }
                long deviceMs = System.currentTimeMillis();
                long skew = Math.abs(deviceMs - networkMs);
                if (skew > MAX_SKEW_MS) {
                    LicenseStore.INSTANCE.setClockLocked(app, true);
                    post(() -> { if (cb != null) cb.onClockTampered(networkMs, deviceMs); });
                } else {
                    post(() -> { if (cb != null) cb.onClockOk(); });
                }
            } catch (Throwable t) {
                post(() -> { if (cb != null) cb.onClockOk(); });
            } finally {
                running.set(false);
            }
        }, "raido-time-guard").start();
    }

    private void post(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    private Long fetchNetworkTime() {
        for (String src : SOURCES) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(src).openConnection();
                conn.setRequestMethod("HEAD");
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                conn.setInstanceFollowRedirects(true);
                conn.connect();
                long date = conn.getDate();
                conn.disconnect();
                if (date > 1_600_000_000_000L) { // بعد از ۲۰۲۰
                    return date;
                }
            } catch (Throwable ignored) {
            }
        }
        // fallback: GET
        for (String src : SOURCES) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(src).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                conn.connect();
                long date = conn.getDate();
                conn.disconnect();
                if (date > 1_600_000_000_000L) return date;
            } catch (Throwable ignored) {
            }
        }
        return null;
    }
}
