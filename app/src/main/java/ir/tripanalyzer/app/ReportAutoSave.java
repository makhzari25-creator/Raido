package ir.tripanalyzer.app;

import android.content.Context;

/**
 * مسیر قدیمی auto-save — برای جلوگیری از کرش decompile، امن شده.
 * ثبت اصلی گزارش با کادر «رفتی؟» (TripConfirmHelper) است.
 */
public final class ReportAutoSave {
    public static final ReportAutoSave INSTANCE = new ReportAutoSave();

    private ReportAutoSave() {}

    public final void cancel(Context context) {
        if (context == null) return;
        try {
            Store.INSTANCE.prefs(context).edit().putLong("report_pending_until", 0L).apply();
        } catch (Exception ignored) {}
    }

    public final void markPending(Context context) {
        if (context == null) return;
        try {
            Store.INSTANCE.prefs(context.getApplicationContext())
                    .edit()
                    .putLong("report_pending_until", System.currentTimeMillis() + 180000)
                    .apply();
        } catch (Exception ignored) {}
    }

    public final void trySaveOnAccepted(Context context, String str) {
        // عمداً خالی: جلوگیری از ثبت اشتباه؛ کاربر با «رفتم» ثبت می‌کند
    }
}
