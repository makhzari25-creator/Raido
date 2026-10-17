package ir.tripanalyzer.app;

import android.content.Context;

/**
 * قبلاً ساعت را با سایت‌های بیرونی چک می‌کرد. حالا زمان مؤثر از ساعت سرور می‌آید
 * (LicenseStore.effectiveNow)، پس فقط همگام‌سازی کم‌مصرف را راه می‌اندازد.
 */
public final class TimeGuard {
    public static final TimeGuard INSTANCE = new TimeGuard();

    public interface Callback {
        void onClockOk();
        void onClockTampered(long networkMs, long deviceMs);
    }

    private TimeGuard() {}

    public void checkOncePerDay(Context context, Callback cb) {
        if (context == null) return;
        CloudSync.syncIfDue(context);
        if (cb != null) cb.onClockOk();
    }
}
