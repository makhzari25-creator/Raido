package ir.tripanalyzer.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import kotlin.Metadata;
import kotlin.NoWhenBranchMatchedException;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Notifier.kt */
@Metadata(d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0003\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fJ\u0010\u0010\u0010\u001a\u00020\u00052\u0006\u0010\u0011\u001a\u00020\u0012H\u0002J\u0010\u0010\u0013\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\u0012H\u0002J\u0010\u0010\u0014\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\u0012H\u0002J\u0010\u0010\u0015\u001a\u00020\u000b2\u0006\u0010\u0016\u001a\u00020\u0017H\u0002J\u0010\u0010\u0018\u001a\u00020\u00052\u0006\u0010\u0019\u001a\u00020\u001aH\u0002J5\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\b\u0010#\u001a\u0004\u0018\u00010$¢\u0006\u0002\u0010%J5\u0010&\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\b\u0010#\u001a\u0004\u0018\u00010$¢\u0006\u0002\u0010'R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0005X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082T¢\u0006\u0002\n\u0000"}, d2 = {"Lir/tripanalyzer/app/Notifier;", "", "<init>", "()V", "CH_GREEN", "", "CH_YELLOW", "CH_RED", "CH_GRAY", "CH_SAMPLE", "BASE_ID", "", "ensureChannels", "", "c", "Landroid/content/Context;", "channelId", "v", "Lir/tripanalyzer/app/Verdict;", "accent", "titleRes", "confRes", "conf", "Lir/tripanalyzer/app/Confidence;", "srcText", "ds", "Lir/tripanalyzer/app/DataSource;", "build", "Landroid/app/Notification;", "r", "Lir/tripanalyzer/app/TripResult;", "recordTs", "", "updated", "", "fare", "", "(Landroid/content/Context;Lir/tripanalyzer/app/TripResult;JZLjava/lang/Double;)Landroid/app/Notification;", "show", "(Landroid/content/Context;Lir/tripanalyzer/app/TripResult;JZLjava/lang/Double;)V"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class Notifier {
    private static final int BASE_ID = 4100;
    public static final String CH_GRAY = "verdict_gray";
    public static final String CH_GREEN = "verdict_green";
    public static final String CH_RED = "verdict_red";
    public static final String CH_SAMPLE = "sample";
    public static final String CH_YELLOW = "verdict_yellow";
    public static final Notifier INSTANCE = new Notifier();

    /* compiled from: Notifier.kt */
    final class ShowTask implements Runnable {
        private final Context c;
        private final Double fare;
        private final boolean force;
        private final TripResult r;
        private final long ts;

        ShowTask(Context context, TripResult tripResult, long j, boolean z, Double d) {
            this.c = context;
            this.r = tripResult;
            this.ts = j;
            this.force = z;
            this.fare = d;
        }

        @Override // java.lang.Runnable
        public final void run() {
            Notifier.INSTANCE.showOnMain(this.c, this.r, this.ts, this.force, this.fare);
        }
    }

    /* compiled from: Notifier.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = 48)
    public /* synthetic */ class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;
        public static final /* synthetic */ int[] $EnumSwitchMapping$1;
        public static final /* synthetic */ int[] $EnumSwitchMapping$2;

        static {
            int[] iArr = new int[Verdict.values().length];
            try {
                iArr[Verdict.GREEN.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                iArr[Verdict.YELLOW.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                iArr[Verdict.RED.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                iArr[Verdict.GRAY.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            $EnumSwitchMapping$0 = iArr;
            int[] iArr2 = new int[Confidence.values().length];
            try {
                iArr2[Confidence.HIGH.ordinal()] = 1;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                iArr2[Confidence.MEDIUM.ordinal()] = 2;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                iArr2[Confidence.LOW.ordinal()] = 3;
            } catch (NoSuchFieldError unused7) {
            }
            $EnumSwitchMapping$1 = iArr2;
            int[] iArr3 = new int[DataSource.values().length];
            try {
                iArr3[DataSource.REAL.ordinal()] = 1;
            } catch (NoSuchFieldError unused8) {
            }
            $EnumSwitchMapping$2 = iArr3;
        }
    }

    public final void show(Context c, TripResult r, long recordTs, boolean updated, Double fare) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            showOnMain(c, r, recordTs, updated, fare);
        } else {
            new Handler(Looper.getMainLooper()).post(new ShowTask(c, r, recordTs, updated, fare));
        }
    }

    public final void showOnMain(Context c, TripResult r, long j, boolean z, Double d) {
        Intrinsics.checkNotNullParameter(c, "c");
        Intrinsics.checkNotNullParameter(r, "r");
        ensureChannels(c);
        NotificationManager notificationManager = (NotificationManager) c.getSystemService("notification");
        int nextNotifId = Store.INSTANCE.nextNotifId(c) + BASE_ID;
        try {
            Intent intent = new Intent(c, (Class<?>) VerdictOverlayActivity.class);
            intent.addFlags(268435456);
            intent.putExtra("verdict", r.getVerdict().name());
            intent.putExtra("fare", d != null ? formatFare(d.doubleValue()) : "");
            intent.putExtra("hourly", r.getHourlyIncome() != null ? formatFare(r.getHourlyIncome().doubleValue()) : "");
            intent.putExtra("distSrc", srcText(r.getDistanceSource()));
            intent.putExtra("greenRatio", (float) computeGreenRatio(c, r));
            try {
                org.json.JSONObject last = Store.INSTANCE.last(c);
                if (last != null) {
                    String zl = last.optString("zoneLabel", "");
                    if (zl != null && zl.length() > 0) intent.putExtra("zoneLabel", zl);
                }
            } catch (Throwable ignored) {}
            // اول: کارت داخل پنجرهٔ Accessibility (بدون نیاز به Appear on top)
            try {
                if (d != null) SnappScreenWatcherService.lastShownFare = d.doubleValue();
                if (SnappScreenWatcherService.showCard(intent)) return;
            } catch (Throwable ignored) {}
            Context applicationContext = c.getApplicationContext();
            intent.addFlags(268435456);
            applicationContext.startActivity(intent);
        } catch (Throwable unused) {
            // ساخت اعلان فقط در مسیر fallback (سرعت/باتری در مسیر عادی)
            notificationManager.notify(nextNotifId, build(c, r, j, z, d));
        }
    }

    private Notifier() {
    }

    /** نسبت سبزی (۰..۱) برای رنگ گرادیانی کارت — فقط وقتی verdict سبز است معنا دارد. */
    private double computeGreenRatio(Context c, TripResult r) {
        try {
            if (r.getVerdict() != Verdict.GREEN) return 1.0;
            EngineSettings s = Store.INSTANCE.settings(c);
            double floor = TripEngine.computeFloor(c);
            Double hourly = r.getHourlyIncome();
            double hourlyNetReal = hourly != null ? hourly.doubleValue() : 0.0;
            return TripEngine.greenRatio(hourlyNetReal, floor, s.getTargetHourly());
        } catch (Exception e) {
            return 1.0;
        }
    }

    private static String formatFare(double v) {
        long r = Math.round(v);
        String s = Long.toString(Math.abs(r));
        StringBuilder sb = new StringBuilder();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            if (i > 0 && (n - i) % 3 == 0) sb.append(',');
            sb.append(s.charAt(i));
        }
        return (r < 0 ? "-" : "") + sb.toString();
    }

    public final void ensureChannels(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        Object systemService = c.getSystemService("notification");
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.app.NotificationManager");
        NotificationManager notificationManager = (NotificationManager) systemService;
        ensureChannels$ch(c, notificationManager, CH_GREEN, R.string.channel_green, Integer.valueOf(Color.parseColor("#1E8E3E")), 4);
        ensureChannels$ch(c, notificationManager, CH_YELLOW, R.string.channel_yellow, Integer.valueOf(Color.parseColor("#C98A00")), 3);
        ensureChannels$ch(c, notificationManager, CH_RED, R.string.channel_red, Integer.valueOf(Color.parseColor("#C5221F")), 4);
        ensureChannels$ch(c, notificationManager, CH_GRAY, R.string.channel_gray, null, 3);
        ensureChannels$ch(c, notificationManager, CH_SAMPLE, R.string.channel_sample, null, 2);
    }

    private static final void ensureChannels$ch(Context context, NotificationManager notificationManager, String str, int i, Integer num, int i2) {
        NotificationChannel notificationChannel = new NotificationChannel(str, context.getString(i), i2);
        if (num != null) {
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(num.intValue());
        }
        notificationChannel.enableVibration(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private final String channelId(Verdict v) {
        int i = WhenMappings.$EnumSwitchMapping$0[v.ordinal()];
        if (i == 1) {
            return CH_GREEN;
        }
        if (i == 2) {
            return CH_YELLOW;
        }
        if (i == 3) {
            return CH_RED;
        }
        if (i == 4) {
            return CH_GRAY;
        }
        throw new NoWhenBranchMatchedException();
    }

    private final int accent(Verdict v) {
        int i = WhenMappings.$EnumSwitchMapping$0[v.ordinal()];
        if (i == 1) {
            return Color.parseColor("#1E8E3E");
        }
        if (i == 2) {
            return Color.parseColor("#C98A00");
        }
        if (i == 3) {
            return Color.parseColor("#C5221F");
        }
        if (i != 4) {
            throw new NoWhenBranchMatchedException();
        }
        return Color.parseColor("#5F6B75");
    }

    private final int titleRes(Verdict v) {
        int i = WhenMappings.$EnumSwitchMapping$0[v.ordinal()];
        if (i == 1) {
            return R.string.notif_green_title;
        }
        if (i == 2) {
            return R.string.notif_yellow_title;
        }
        if (i == 3) {
            return R.string.notif_red_title;
        }
        if (i == 4) {
            return R.string.notif_gray_title;
        }
        throw new NoWhenBranchMatchedException();
    }

    private final int confRes(Confidence conf) {
        int i = WhenMappings.$EnumSwitchMapping$1[conf.ordinal()];
        if (i == 1) {
            return R.string.confidence_short_high;
        }
        if (i == 2) {
            return R.string.confidence_short_medium;
        }
        if (i == 3) {
            return R.string.confidence_short_low;
        }
        throw new NoWhenBranchMatchedException();
    }

    private final String srcText(DataSource ds) {
        if (WhenMappings.$EnumSwitchMapping$2[ds.ordinal()] == 1) {
            return Res.INSTANCE.str(R.string.source_real, new Object[0]);
        }
        return Res.INSTANCE.str(R.string.source_estimated, new Object[0]);
    }

    public final Notification build(Context c, TripResult r, long recordTs, boolean updated, Double fare) {
        String str;
        Res res;
        double d;
        String str2;
        Res res2;
        double d2;
        String str3;
        double d3;
        String joinToString$default;
        Intrinsics.checkNotNullParameter(c, "c");
        Intrinsics.checkNotNullParameter(r, "r");
        StringBuilder sb = new StringBuilder();
        sb.append(Res.INSTANCE.str(titleRes(r.getVerdict()), new Object[0]));
        if (updated) {
            str = " · " + Res.INSTANCE.str(R.string.notif_updated_tag, new Object[0]);
        } else {
            str = "";
        }
        sb.append(str);
        String sb2 = sb.toString();
        String money = fare != null ? Ui.INSTANCE.money(fare.doubleValue()) : null;
        if (r.getVerdict() == Verdict.GRAY && money != null) {
            str2 = Res.INSTANCE.str(R.string.notif_gray_collapsed_with_fare, money);
        } else if (r.getVerdict() == Verdict.GRAY) {
            str2 = Res.INSTANCE.str(R.string.notif_gray_collapsed_line, new Object[0]);
        } else {
            Res res3 = Res.INSTANCE;
            Object[] objArr = new Object[3];
            objArr[0] = money == null ? "۰" : money;
            Res res4 = Res.INSTANCE;
            Object[] objArr2 = new Object[1];
            Ui ui = Ui.INSTANCE;
            Double hourlyIncome = r.getHourlyIncome();
            if (hourlyIncome != null) {
                res = res4;
                d = hourlyIncome.doubleValue();
            } else {
                res = res4;
                d = 0.0d;
            }
            objArr2[0] = ui.money(d);
            objArr[1] = res.str(R.string.hourly_income_format, objArr2);
            objArr[2] = Res.INSTANCE.str(confRes(r.getConfidence()), new Object[0]);
            str2 = res3.str(R.string.notif_collapsed_line_fmt, objArr);
        }
        if (r.getVerdict() == Verdict.GRAY) {
            if (money != null) {
                joinToString$default = Res.INSTANCE.str(R.string.notif_expanded_fare, money) + '\n' + Res.INSTANCE.str(R.string.notif_gray_expanded_line1, new Object[0]);
            } else {
                joinToString$default = Res.INSTANCE.str(R.string.notif_gray_expanded_line1, new Object[0]);
            }
            str3 = sb2;
        } else {
            ArrayList arrayList = new ArrayList();
            if (money != null) {
                arrayList.add(Res.INSTANCE.str(R.string.notif_expanded_fare, money));
            }
            Res res5 = Res.INSTANCE;
            Object[] objArr3 = new Object[1];
            Ui ui2 = Ui.INSTANCE;
            Double netIncome = r.getNetIncome();
            if (netIncome != null) {
                res2 = res5;
                d2 = netIncome.doubleValue();
            } else {
                res2 = res5;
                d2 = 0.0d;
            }
            objArr3[0] = ui2.money(d2);
            arrayList.add(res2.str(R.string.notif_expanded_net, objArr3));
            Res res6 = Res.INSTANCE;
            Object[] objArr4 = new Object[1];
            Ui ui3 = Ui.INSTANCE;
            Double hourlyIncome2 = r.getHourlyIncome();
            objArr4[0] = ui3.money(hourlyIncome2 != null ? hourlyIncome2.doubleValue() : 0.0d);
            arrayList.add(res6.str(R.string.notif_expanded_hourly, objArr4));
            Res res7 = Res.INSTANCE;
            Object[] objArr5 = new Object[2];
            Ui ui4 = Ui.INSTANCE;
            Double totalDistanceKm = r.getTotalDistanceKm();
            objArr5[0] = ui4.km(totalDistanceKm != null ? totalDistanceKm.doubleValue() : 0.0d);
            objArr5[1] = srcText(r.getDistanceSource());
            arrayList.add(res7.str(R.string.notif_expanded_distance, objArr5));
            Res res8 = Res.INSTANCE;
            Object[] objArr6 = new Object[2];
            Ui ui5 = Ui.INSTANCE;
            Double rawTimeMin = r.getRawTimeMin();
            if (rawTimeMin != null) {
                double doubleValue = rawTimeMin.doubleValue();
                str3 = sb2;
                d3 = doubleValue;
            } else {
                str3 = sb2;
                d3 = 0.0d;
            }
            objArr6[0] = ui5.min(d3);
            objArr6[1] = srcText(r.getTimeSource());
            arrayList.add(res8.str(R.string.notif_expanded_time, objArr6));
            Res res9 = Res.INSTANCE;
            Object[] objArr7 = new Object[1];
            Ui ui6 = Ui.INSTANCE;
            Double fuelCost = r.getFuelCost();
            objArr7[0] = ui6.money(fuelCost != null ? fuelCost.doubleValue() : 0.0d);
            arrayList.add(res9.str(R.string.notif_expanded_fuel, objArr7));
            Res res10 = Res.INSTANCE;
            Object[] objArr8 = new Object[1];
            Ui ui7 = Ui.INSTANCE;
            Double depCost = r.getDepCost();
            objArr8[0] = ui7.money(depCost != null ? depCost.doubleValue() : 0.0d);
            arrayList.add(res10.str(R.string.notif_expanded_dep, objArr8));
            arrayList.add(Res.INSTANCE.str(R.string.notif_expanded_source, srcText(r.getDistanceSource()), srcText(r.getTimeSource())));
            if (r.getVerdict() == Verdict.YELLOW && (!r.getYellowReasons().isEmpty())) {
                arrayList.add(money != null ? 2 : 1, Res.INSTANCE.str(R.string.notif_yellow_why, CollectionsKt.joinToString(r.getYellowReasons(), " · ", "", "", -1, "...", new Function1() { public Object invoke(Object o) { return Notifier.build$lambda$1((YellowReason)o); } })));
            }
            if (r.getVerdict() == Verdict.RED && r.getRejectReason() != null) {
                arrayList.add(Res.INSTANCE.str(R.string.notif_red_main_reason, Ui.INSTANCE.rejectReasonText(r.getRejectReason(), r.getTotalDistanceKm())));
            }
            joinToString$default = CollectionsKt.joinToString(arrayList, "\n", "", "", -1, "...", null);
        }
        Intent intent = new Intent(c, (Class<?>) DetailsActivity.class);
        intent.putExtra("ts", recordTs);
        intent.setFlags(335544320);
        PendingIntent activity = PendingIntent.getActivity(c, (int) (recordTs & 65535), intent, 201326592);
        Notification.Builder addAction = new Notification.Builder(c, channelId(r.getVerdict())).setSmallIcon(R.drawable.ic_stat).setContentTitle(str3).setContentText(str2).setColor(accent(r.getVerdict())).setContentIntent(activity).setAutoCancel(true).setStyle(new Notification.BigTextStyle().bigText(joinToString$default)).addAction(new Notification.Action.Builder(Icon.createWithResource(c, R.drawable.ic_stat), Res.INSTANCE.str(R.string.notif_action_details, new Object[0]), activity).build());
        Intent intent2 = new Intent(c, (Class<?>) VerdictOverlayActivity.class);
        intent2.addFlags(268435456);
        intent2.putExtra("verdict", r.getVerdict().name());
        intent2.putExtra("fare", fare != null ? formatFare(fare.doubleValue()) : "");
        intent2.putExtra("hourly", r.getHourlyIncome() != null ? formatFare(r.getHourlyIncome().doubleValue()) : "");
        intent2.putExtra("distSrc", srcText(r.getDistanceSource()));
        intent2.putExtra("greenRatio", (float) computeGreenRatio(c, r));
        try {
            org.json.JSONObject last2 = Store.INSTANCE.last(c);
            if (last2 != null) {
                String zl2 = last2.optString("zoneLabel", "");
                if (zl2 != null && zl2.length() > 0) intent2.putExtra("zoneLabel", zl2);
            }
        } catch (Throwable ignored) {}
        Notification build = addAction.setFullScreenIntent(PendingIntent.getActivity(c, 9999, intent2, 201326592), true).build();
        Intrinsics.checkNotNullExpressionValue(build, "build(...)");
        return build;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final CharSequence build$lambda$1(YellowReason it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return Ui.INSTANCE.yellowReasonText(it);
    }
}
