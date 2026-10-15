package ir.tripanalyzer.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * حتی وقتی کاربر در اسنپ است (RAIDO در پس‌زمینه):
 * با اعلان اولویت‌بالا + fullScreenIntent صفحهٔ سوال را نشان می‌دهد.
 */
public final class TripConfirmHelper {
    public static final TripConfirmHelper INSTANCE = new TripConfirmHelper();

    private static final String CH_PROMPT = "raido_prompt";
    private static final long CONFIRM_DELAY_MS = 5 * 60 * 1000L;

    private final Handler main = new Handler(Looper.getMainLooper());
    private Runnable pendingConfirmRunnable;

    private TripConfirmHelper() {}

    public void cancelPending(Context ctx) {
        if (pendingConfirmRunnable != null) {
            main.removeCallbacks(pendingConfirmRunnable);
            pendingConfirmRunnable = null;
        }
        try {
            Store.INSTANCE.clearPendingConfirm(ctx.getApplicationContext());
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.cancel(PromptActivity.NOTIF_CONFIRM);
        } catch (Exception ignored) {}
    }

    public void scheduleAfterAnalysis(final Context ctx, final long ts, final double fare) {
        final Context app = ctx.getApplicationContext();
        if (pendingConfirmRunnable != null) main.removeCallbacks(pendingConfirmRunnable);
        pendingConfirmRunnable = new Runnable() {
            @Override public void run() {
                try {
                    if (Store.INSTANCE.getPendingConfirmTs(app) != ts) return;
                    fireConfirmPrompt(app, ts, fare);
                } catch (Exception ignored) {}
            }
        };
        main.postDelayed(pendingConfirmRunnable, CONFIRM_DELAY_MS);
    }

    /** حذف‌شده: کاهش ۱۰٪ هدف بعد از ۳ رد دیگر فعال نیست */
    public void maybeOfferTargetReduction(final Context ctx) {
        // غیرفعال — میانگین ساعتی جایگزین کاهش ۱۰٪ سقف شده است
    }

    private void ensureChannel(Context app) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        NotificationChannel ch = new NotificationChannel(
                CH_PROMPT, "سوال‌های فوری رایدو", NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("پیشنهاد کاهش هدف و تأیید ثبت سفر");
        ch.enableVibration(true);
        ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        nm.createNotificationChannel(ch);
    }

    private void fireReducePrompt(Context app) {
        ensureChannel(app);
        double base = Store.INSTANCE.getBaseTargetHourly(app);
        long reduced = Math.round(base * 0.9);
        String fareStr = NumberFormat.getInstance(new Locale("fa")).format(reduced);

        Intent open = new Intent(app, PromptActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        open.putExtra(PromptActivity.EXTRA_MODE, PromptActivity.MODE_REDUCE);
        PendingIntent full = PendingIntent.getActivity(
                app, 7201, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent yes = new Intent(app, PromptActionReceiver.class);
        yes.setAction(PromptActionReceiver.ACTION_REDUCE_YES);
        PendingIntent yesPi = PendingIntent.getBroadcast(
                app, 7202, yes, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent no = new Intent(app, PromptActionReceiver.class);
        no.setAction(PromptActionReceiver.ACTION_REDUCE_NO);
        PendingIntent noPi = PendingIntent.getBroadcast(
                app, 7203, no, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder b = new Notification.Builder(app, CH_PROMPT)
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle("کاهش موقت هدف؟")
                .setContentText("۳ رد پشت‌سرهم — هدف ۱۰٪ کم شود (≈" + fareStr + ")؟")
                .setStyle(new Notification.BigTextStyle().bigText(
                        "سه درخواست پشت‌سرهم مناسب نبود. ممکن است از اسنپ درخواست کم شود.\n"
                                + "حداقل درآمد ساعتی موقتاً ۱۰٪ کم شود (حدود " + fareStr + " تومان)؟"))
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_STATUS)
                .setAutoCancel(true)
                .setContentIntent(full)
                .setFullScreenIntent(full, true)
                .addAction(new Notification.Action.Builder(
                        Icon.createWithResource(app, R.drawable.ic_stat), "بله، کم کن", yesPi).build())
                .addAction(new Notification.Action.Builder(
                        Icon.createWithResource(app, R.drawable.ic_stat), "خیر", noPi).build());

        NotificationManager nm = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(PromptActivity.NOTIF_REDUCE, b.build());

        // تلاش مستقیم برای باز کردن صفحه روی بعضی گوشی‌ها
        try {
            app.startActivity(open);
        } catch (Exception ignored) {}
    }

    private void fireConfirmPrompt(Context app, long ts, double fare) {
        ensureChannel(app);
        String fareStr = NumberFormat.getInstance(new Locale("fa")).format((long) fare);

        Intent open = new Intent(app, PromptActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        open.putExtra(PromptActivity.EXTRA_MODE, PromptActivity.MODE_CONFIRM);
        open.putExtra(PromptActivity.EXTRA_TS, ts);
        open.putExtra(PromptActivity.EXTRA_FARE, fare);
        PendingIntent full = PendingIntent.getActivity(
                app, 7211, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent yes = new Intent(app, PromptActionReceiver.class);
        yes.setAction(PromptActionReceiver.ACTION_CONFIRM_YES);
        yes.putExtra(PromptActivity.EXTRA_TS, ts);
        PendingIntent yesPi = PendingIntent.getBroadcast(
                app, 7212, yes, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent no = new Intent(app, PromptActionReceiver.class);
        no.setAction(PromptActionReceiver.ACTION_CONFIRM_NO);
        PendingIntent noPi = PendingIntent.getBroadcast(
                app, 7213, no, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder b = new Notification.Builder(app, CH_PROMPT)
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle("سفر را رفتی؟")
                .setContentText("قیمت " + fareStr + " تومان — ثبت در گزارش؟")
                .setStyle(new Notification.BigTextStyle().bigText(
                        "آیا سفری که با قیمت " + fareStr + " تومان بود را رفتی؟"))
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setTimeoutAfter(5000)
                .setContentIntent(full)
                .setFullScreenIntent(full, true)
                .addAction(new Notification.Action.Builder(
                        Icon.createWithResource(app, R.drawable.ic_stat), "رفتم", yesPi).build())
                .addAction(new Notification.Action.Builder(
                        Icon.createWithResource(app, R.drawable.ic_stat), "نرفتم", noPi).build());

        NotificationManager nm = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(PromptActivity.NOTIF_CONFIRM, b.build());

        try {
            app.startActivity(open);
        } catch (Exception ignored) {}
    }
}
