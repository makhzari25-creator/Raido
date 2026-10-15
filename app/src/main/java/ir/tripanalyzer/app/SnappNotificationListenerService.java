package ir.tripanalyzer.app;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.LinkedHashSet;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: SnappNotificationListenerService.kt */
@Metadata(d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0004\u001a\u00020\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007H\u0016"}, d2 = {"Lir/tripanalyzer/app/SnappNotificationListenerService;", "Landroid/service/notification/NotificationListenerService;", "<init>", "()V", "onNotificationPosted", "", "sbn", "Landroid/service/notification/StatusBarNotification;"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class SnappNotificationListenerService extends NotificationListenerService {
    @Override // android.service.notification.NotificationListenerService
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification notification;
        Bundle bundle;
        CharSequence charSequence;
        if (sbn == null || !Intrinsics.areEqual(sbn.getPackageName(), SnappScreenWatcherService.SNAPP_PACKAGE) || (notification = sbn.getNotification()) == null || (bundle = notification.extras) == null) {
            return;
        }
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        CharSequence charSequence2 = bundle.getCharSequence("android.title");
        if (charSequence2 != null) {
            linkedHashSet.add(charSequence2.toString());
        }
        CharSequence charSequence3 = bundle.getCharSequence("android.text");
        if (charSequence3 != null) {
            linkedHashSet.add(charSequence3.toString());
        }
        CharSequence charSequence4 = bundle.getCharSequence("android.bigText");
        if (charSequence4 != null) {
            linkedHashSet.add(charSequence4.toString());
        }
        Notification notification2 = sbn.getNotification();
        if (notification2 != null && (charSequence = notification2.tickerText) != null) {
            linkedHashSet.add(charSequence.toString());
        }
        if (!linkedHashSet.isEmpty()) {
            String joinToString$default = CollectionsKt.joinToString(linkedHashSet, "\n", "", "", -1, "...", null);
            SnappScreenWatcherService.INSTANCE.recordDump(joinToString$default, "notification");
            Pipeline.INSTANCE.process(this, joinToString$default, "notification");
        }
    }
}
