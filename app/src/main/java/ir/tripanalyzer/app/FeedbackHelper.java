package ir.tripanalyzer.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import org.json.JSONObject;

/**
 * گزارش اشتباه تحلیل: متن خام Accessibility/اعلان + خلاصهٔ آخرین پارس را
 * روی دستگاه ذخیره می‌کند و در صورت تمایل کاربر برای پشتیبانی اشتراک می‌گذارد.
 */
public final class FeedbackHelper {
    public static final FeedbackHelper INSTANCE = new FeedbackHelper();

    private FeedbackHelper() {}

    public static void reportWrongAnalysis(Context context) {
        if (context == null) return;
        try {
            String raw = SnappScreenWatcherService.INSTANCE.getLastDump();
            if (raw == null || raw.trim().isEmpty()) {
                raw = "(متن خام در حافظه نبود — لطفاً یک‌بار صفحهٔ درخواست اسنپ را باز کنید و دوباره تلاش کنید)";
            }
            String summary = buildSummary(context);
            String fileName = Store.INSTANCE.saveErrorReport(context, raw, summary);

            // شمارندهٔ محلی برای متریک اعتماد (بدون سرور)
            try {
                Store.INSTANCE.prefs(context).edit()
                        .putInt("wrong_report_count",
                                Store.INSTANCE.prefs(context).getInt("wrong_report_count", 0) + 1)
                        .putLong("wrong_report_last_at", System.currentTimeMillis())
                        .apply();
                RaidoDataStore.INSTANCE.incrementWrongReports(context);
            } catch (Exception ignored) {}

            final String shareBody = "گزارش اشتباه رایدو\nفایل: " + fileName + "\n\n"
                    + "== خلاصه ==\n" + summary + "\n\n"
                    + "== متن خام (بخش اول) ==\n"
                    + (raw.length() > 2500 ? raw.substring(0, 2500) + "\n…" : raw);

            new AlertDialog.Builder(context)
                    .setTitle("گزارش ذخیره شد")
                    .setMessage("متن خام و خلاصهٔ تحلیل روی گوشی شما ذخیره شد.\n"
                            + "اگر می‌خواهید برای پشتیبانی بفرستید، «اشتراک‌گذاری» را بزنید.")
                    .setPositiveButton("اشتراک‌گذاری", (d, w) -> {
                        try {
                            Intent send = new Intent(Intent.ACTION_SEND);
                            send.setType("text/plain");
                            send.putExtra(Intent.EXTRA_SUBJECT, "گزارش اشتباه رایدو");
                            send.putExtra(Intent.EXTRA_TEXT, shareBody);
                            context.startActivity(Intent.createChooser(send, "ارسال گزارش"));
                        } catch (Exception e) {
                            Toast.makeText(context, "اشتراک‌گذاری ممکن نشد", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("باشه", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(context, "خطا در ذخیرهٔ گزارش", Toast.LENGTH_SHORT).show();
        }
    }

    private static String buildSummary(Context context) {
        StringBuilder sb = new StringBuilder();
        try {
            JSONObject last = Store.INSTANCE.last(context);
            if (last != null) {
                sb.append("verdict=").append(last.optString("verdict", "?")).append('\n');
                sb.append("fare=").append(last.opt("fare")).append('\n');
                sb.append("pickupMin=").append(last.opt("pickupMin")).append('\n');
                sb.append("tripMin=").append(last.opt("tripMin")).append('\n');
                sb.append("dist=").append(last.opt("dist")).append('\n');
                sb.append("rawTime=").append(last.opt("rawTime")).append('\n');
                sb.append("adjusted=").append(last.opt("adjusted")).append('\n');
                sb.append("hourly=").append(last.opt("hourly")).append('\n');
                sb.append("netAfter=").append(last.opt("netAfter")).append('\n');
            } else {
                sb.append("(تحلیل ذخیره‌شده نبود)\n");
            }
            String src = SnappScreenWatcherService.INSTANCE.getLastDumpSource();
            long at = SnappScreenWatcherService.INSTANCE.getLastDumpAt();
            sb.append("dumpSource=").append(src != null ? src : "?").append('\n');
            sb.append("dumpAt=").append(at).append('\n');
        } catch (Exception e) {
            sb.append("summary_error=").append(e.getMessage());
        }
        return sb.toString();
    }
}
