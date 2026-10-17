package ir.tripanalyzer.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

/** همگام‌سازی کم‌مصرف: وضعیت اشتراک + آمار روزانه. بدون WorkManager و بدون بیدارکردن دستگاه. */
final class CloudSync {
    private static final AtomicBoolean running = new AtomicBoolean(false);
    private static volatile long lastCheckElapsed = 0L;
    private CloudSync() {}

    static void syncIfDue(Context ctx) {
        try {
            long el = SystemClock.elapsedRealtime();
            if (lastCheckElapsed != 0 && el - lastCheckElapsed < 10 * 60 * 1000L) return;
            lastCheckElapsed = el;
            final Context app = ctx.getApplicationContext();
            if (!LicenseStore.INSTANCE.isLoggedIn(app)) return;
            SharedPreferences sp = app.getSharedPreferences("raido_cloud_sync", Context.MODE_PRIVATE);
            long last = sp.getLong("last_ok", 0L);
            long wall = System.currentTimeMillis();
            long interval = LicenseStore.INSTANCE.needsFrequentSync(app) ? 30 * 60 * 1000L : 6 * 3600 * 1000L;
            if (last <= wall && wall - last < interval) return;
            if (!running.compareAndSet(false, true)) return;
            new Thread(() -> {
                try {
                    boolean ok = LicenseStore.INSTANCE.refreshStatus(app) == 0;
                    try {
                        if (LicenseStore.INSTANCE.isLoggedIn(app)) {
                            JSONObject b = new JSONObject();
                            b.put("days", UsageCounter.recentDays(app));
                            b.put("app_version", BuildConfig.VERSION_NAME);
                            RaidoCloud.post("usage-report", b, LicenseStore.INSTANCE.getToken(app));
                        }
                    } catch (Throwable ignored) {}
                    if (ok) sp.edit().putLong("last_ok", System.currentTimeMillis()).apply();
                } catch (Throwable ignored) {
                } finally {
                    running.set(false);
                }
            }, "raido-sync").start();
        } catch (Throwable ignored) {}
    }
}
