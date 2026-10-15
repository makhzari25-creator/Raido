package ir.tripanalyzer.app;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PromptActionReceiver extends BroadcastReceiver {

    public static final String ACTION_REDUCE_YES = "ir.tripanalyzer.app.REDUCE_YES";
    public static final String ACTION_REDUCE_NO = "ir.tripanalyzer.app.REDUCE_NO";
    public static final String ACTION_CONFIRM_YES = "ir.tripanalyzer.app.CONFIRM_YES";
    public static final String ACTION_CONFIRM_NO = "ir.tripanalyzer.app.CONFIRM_NO";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || intent.getAction() == null) return;
        Context app = context.getApplicationContext();
        String action = intent.getAction();
        try {
            if (ACTION_REDUCE_YES.equals(action)) {
                Store.INSTANCE.clearTempTarget(app);
                Store.INSTANCE.resetConsecutiveRejects(app);
                // کاهش ۱۰٪ حذف شد
                cancel(app, PromptActivity.NOTIF_REDUCE);
            } else if (ACTION_REDUCE_NO.equals(action)) {
                Store.INSTANCE.resetConsecutiveRejects(app);
                cancel(app, PromptActivity.NOTIF_REDUCE);
            } else if (ACTION_CONFIRM_YES.equals(action)) {
                long ts = intent.getLongExtra(PromptActivity.EXTRA_TS, 0L);
                if (ts == 0L || Store.INSTANCE.getPendingConfirmTs(app) == ts) {
                    Store.INSTANCE.saveToRecentFromLast(app);
                    Store.INSTANCE.clearTempTarget(app);
                    Store.INSTANCE.resetConsecutiveRejects(app);
                    Store.INSTANCE.clearPendingConfirm(app);
                    Toast.makeText(app, "در گزارش ثبت شد", Toast.LENGTH_SHORT).show();
                }
                cancel(app, PromptActivity.NOTIF_CONFIRM);
            } else if (ACTION_CONFIRM_NO.equals(action)) {
                Store.INSTANCE.clearPendingConfirm(app);
                cancel(app, PromptActivity.NOTIF_CONFIRM);
            }
        } catch (Exception ignored) {}
    }

    private void cancel(Context app, int id) {
        try {
            NotificationManager nm = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.cancel(id);
        } catch (Exception ignored) {}
    }
}
