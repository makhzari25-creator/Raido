package ir.tripanalyzer.app;

import ir.tripanalyzer.app.core.TripLifecycle;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.List;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.text.StringsKt;

/* compiled from: SnappScreenWatcherService.kt */
@Metadata(d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 \u00122\u00020\u0001:\u0001\u0012B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\n\u001a\u00020\u000bH\u0014J\u0012\u0010\f\u001a\u00020\u000b2\b\u0010\r\u001a\u0004\u0018\u00010\u000eH\u0016J\b\u0010\u000f\u001a\u00020\u000bH\u0016J\b\u0010\u0010\u001a\u00020\u000bH\u0002J\b\u0010\u0011\u001a\u00020\u000bH\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e¢\u0006\u0002\n\u0000"}, d2 = {"Lir/tripanalyzer/app/SnappScreenWatcherService;", "Landroid/accessibilityservice/AccessibilityService;", "<init>", "()V", "handler", "Landroid/os/Handler;", "captureTask", "Ljava/lang/Runnable;", "inCapture", "", "onServiceConnected", "", "onAccessibilityEvent", "event", "Landroid/view/accessibility/AccessibilityEvent;", "onDestroy", "captureAndProcess", "onInterrupt", "Companion"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class SnappScreenWatcherService extends AccessibilityService {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static final long DEBOUNCE_MS = 450;
    private static final int MAX_CHARS = 16000;
    private static final int MAX_DEPTH = 22;
    private static final int MAX_NODES = 800;
    public static final String SNAPP_PACKAGE = "cab.snapp.driver";
    private static volatile String lastDump;
    private static volatile long lastDumpAt;
    private static volatile String lastDumpSource;
    private static volatile int lastNodeCount;
    private boolean inCapture;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable captureTask = new Runnable() { // from class: ir.tripanalyzer.app.SnappScreenWatcherService$$ExternalSyntheticLambda0
        @Override // java.lang.Runnable
        public final void run() {
            SnappScreenWatcherService.captureTask$lambda$0(SnappScreenWatcherService.this);
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public static final void captureTask$lambda$0(SnappScreenWatcherService snappScreenWatcherService) {
        try {
            snappScreenWatcherService.captureAndProcess();
        } catch (Throwable unused) {
        }
    }

    @Override // android.accessibilityservice.AccessibilityService
    public void onInterrupt() {
    }

    /* compiled from: SnappScreenWatcherService.kt */
    @Metadata(d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0006\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0016\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00052\u0006\u0010\u0017\u001a\u00020\u0005R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T¢\u0006\u0002\n\u0000R\"\u0010\u0007\u001a\u0004\u0018\u00010\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u0005@BX\u0086\u000e¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\"\u0010\n\u001a\u0004\u0018\u00010\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u0005@BX\u0086\u000e¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\tR\u001e\u0010\r\u001a\u00020\f2\u0006\u0010\u0006\u001a\u00020\f@BX\u0086\u000e¢\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u001e\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u0006\u001a\u00020\u0010@BX\u0086\u000e¢\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u000e\u0010\u0018\u001a\u00020\fX\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u0010X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u0010X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u0010X\u0082T¢\u0006\u0002\n\u0000"}, d2 = {"Lir/tripanalyzer/app/SnappScreenWatcherService$Companion;", "", "<init>", "()V", "SNAPP_PACKAGE", "", "value", "lastDump", "getLastDump", "()Ljava/lang/String;", "lastDumpSource", "getLastDumpSource", "", "lastDumpAt", "getLastDumpAt", "()J", "", "lastNodeCount", "getLastNodeCount", "()I", "recordDump", "", "text", "source", "DEBOUNCE_MS", "MAX_NODES", "MAX_DEPTH", "MAX_CHARS"}, k = 1, mv = {2, 0, 0}, xi = 48)
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public final String getLastDump() {
            return SnappScreenWatcherService.lastDump;
        }

        public final String getLastDumpSource() {
            return SnappScreenWatcherService.lastDumpSource;
        }

        public final long getLastDumpAt() {
            return SnappScreenWatcherService.lastDumpAt;
        }

        public final int getLastNodeCount() {
            return SnappScreenWatcherService.lastNodeCount;
        }

        public final void recordDump(String text, String source) {
            Intrinsics.checkNotNullParameter(text, "text");
            Intrinsics.checkNotNullParameter(source, "source");
            if (StringsKt.isBlank(text)) {
                return;
            }
            SnappScreenWatcherService.lastDump = StringsKt.take(text, SnappScreenWatcherService.MAX_CHARS);
            SnappScreenWatcherService.lastDumpSource = source;
            SnappScreenWatcherService.lastDumpAt = System.currentTimeMillis();
        }
    }

    @Override // android.accessibilityservice.AccessibilityService
    protected void onServiceConnected() {
        super.onServiceConnected();
        live = this;
        // فقط در راه‌اندازی اول: بعد از روشن شدن کلید، کاربر خودکار به ویزارد برمی‌گردد
        try {
            if (Store.INSTANCE.prefs(this).getBoolean("wiz_tried_acc", false)
                    && !Store.INSTANCE.isPermissionsWizardDone(this)) {
                Intent back = new Intent(this, PermissionsOnboardingActivity.class);
                back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(back);
            }
        } catch (Throwable ignored) {}
    }

    // ───────── کارت تحلیل به‌صورت Accessibility overlay ─────────
    // TYPE_ACCESSIBILITY_OVERLAY برای سرویس دسترس‌پذیری بدون مجوز
    // «نمایش روی سایر برنامه‌ها» (SYSTEM_ALERT_WINDOW) مجاز است و برای شروع Activity
    // از پس‌زمینه هم نیازی به استثنای BAL نیست.
    private static volatile SnappScreenWatcherService live;
    private View cardView;
    /** آخرین کرایهٔ روی کارت — با تغییر قیمت، کارت قبلی فوری برداشته می‌شود */
    public static volatile double lastShownFare = -1;
    private static volatile long lastAutoAcceptTs = 0L;
    private static volatile long lastAutoAcceptAnalysisTs = 0L;
    private final Runnable cardHide = new Runnable() {
        @Override public void run() { removeCard(); }
    };

    /** کارت فعلی را فوری ببند (مثلاً وقتی قیمت درخواست عوض شد) */
    public static void dismissCardNow() {
        final SnappScreenWatcherService s = live;
        if (s == null) return;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            s.removeCard();
            return;
        }
        try {
            s.handler.post(new Runnable() {
                @Override public void run() { s.removeCard(); }
            });
        } catch (Throwable ignored) {}
    }

    /** امن برای هر thread: addView روی main انجام می‌شود. true یعنی کارت اضافه شد. */
    public static boolean showCard(Intent extras) {
        final SnappScreenWatcherService s = live;
        if (s == null || extras == null) return false;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return s.addCard(extras);
        }
        final boolean[] ok = new boolean[]{false};
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        try {
            s.handler.post(new Runnable() {
                @Override public void run() {
                    try { ok[0] = s.addCard(extras); }
                    catch (Throwable ignored) { ok[0] = false; }
                    finally { latch.countDown(); }
                }
            });
            latch.await(2, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Throwable ignored) {
            return false;
        }
        return ok[0];
    }

    private boolean addCard(Intent x) {
        try {
            removeCard();
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            if (wm == null) return false;
            View v = VerdictCard.build(this,
                    x.getStringExtra("verdict"),
                    x.getStringExtra("fare"),
                    x.getStringExtra("hourly"),
                    x.getStringExtra("distSrc"),
                    x.getFloatExtra("greenRatio", 1.0f),
                    x.getStringExtra("zoneLabel"),
                    new View.OnClickListener() {
                        @Override public void onClick(View view) { removeCard(); }
                    });
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
            // بالاتر از وسط — نزدیک ناحیهٔ مشخص‌شده روی نقشه (حدود ۱۲٪ از بالا)
            lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            try {
                android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
                lp.y = (int) (dm.heightPixels * 0.12f);
            } catch (Throwable ignored) {
                lp.y = 180;
            }
            // لمس هرجای بیرون کارت (مثلاً روی اسنپ) → کارت بسته شود؛ لمس به اسنپ هم می‌رسد
            v.setOnTouchListener(new View.OnTouchListener() {
                @Override public boolean onTouch(View view, MotionEvent e) {
                    if (e.getActionMasked() == MotionEvent.ACTION_OUTSIDE) {
                        removeCard();
                        return true;
                    }
                    return false;
                }
            });
            wm.addView(v, lp);
            cardView = v;
            long showMs = 6000L;
            try {
                int sec = Store.INSTANCE.prefs(this).getInt("overlay_card_seconds", 6);
                if (sec < 3) sec = 3;
                if (sec > 15) sec = 15;
                showMs = sec * 1000L;
            } catch (Throwable ignored) {}
            handler.removeCallbacks(cardHide);
            handler.postDelayed(cardHide, showMs);
            return true;
        } catch (Throwable t) {
            cardView = null;
            return false;
        }
    }


            private void tryAutoAcceptOnTripEnd(String raw) {
        // [TripLifecycle] ثبت پایان سفر — منطق قبلی، لایه جدا
        TripLifecycle.AcceptGate gate = new TripLifecycle.AcceptGate(lastAutoAcceptTs, lastAutoAcceptAnalysisTs);
        if (TripLifecycle.INSTANCE.tryAcceptOnTripEnd(this, raw, gate)) {
            lastAutoAcceptTs = gate.lastAutoAcceptTs;
            lastAutoAcceptAnalysisTs = gate.lastAutoAcceptAnalysisTs;
        }
    }

    private void removeCard() {
        handler.removeCallbacks(cardHide);
        View v = cardView;
        cardView = null;
        lastShownFare = -1;
        if (v == null) return;
        try {
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) wm.removeView(v);
        } catch (Throwable ignored) {}
    }

    @Override // android.accessibilityservice.AccessibilityService
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }
        CharSequence packageName = event.getPackageName();
        if (Intrinsics.areEqual(packageName != null ? packageName.toString() : null, SNAPP_PACKAGE)) {
            int eventType = event.getEventType();
            // فقط تغییر پنجره/محتوا — نه typeViewTextChanged (۱۶) که روی نقشه مدام شلیک می‌شود
            if (eventType == 32 || eventType == 2048 || eventType == 4194304) {
                this.handler.removeCallbacks(this.captureTask);
                this.handler.postDelayed(this.captureTask, DEBOUNCE_MS);
            }
        }
    }

    @Override // android.app.Service
    public void onDestroy() {
        removeCard();
        if (live == this) live = null;
        this.handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private final void captureAndProcess() {
        List<AccessibilityWindowInfo> list;
        AccessibilityNodeInfo accessibilityNodeInfo;
        if (this.inCapture) {
            return;
        }
        // تحلیل خاموش → درخت UI را پیمایش نکن (باتری)
        try {
            if (!Store.INSTANCE.isAnalysisEnabled(getApplicationContext())) {
                return;
            }
        } catch (Throwable ignored) {}
        boolean z = true;
        this.inCapture = true;
        try {
            StringBuilder sb = new StringBuilder();
            Ref.IntRef intRef = new Ref.IntRef();
            try {
                list = getWindows();
            } catch (Throwable unused) {
                list = null;
            }
            if (list != null) {
                for (AccessibilityWindowInfo accessibilityWindowInfo : list) {
                    try {
                        accessibilityNodeInfo = accessibilityWindowInfo.getRoot();
                    } catch (Throwable unused2) {
                        accessibilityNodeInfo = null;
                    }
                    if (accessibilityNodeInfo != null) {
                        captureAndProcess$collect(intRef, sb, accessibilityNodeInfo, 0);
                        try {
                            accessibilityNodeInfo.recycle();
                        } catch (Throwable unused3) {
                        }
                    }
                    try {
                        accessibilityWindowInfo.recycle();
                    } catch (Throwable unused4) {
                    }
                }
            }
            try {
                AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
                if (rootInActiveWindow != null) {
                    captureAndProcess$collect(intRef, sb, rootInActiveWindow, 0);
                }
            } catch (Throwable unused5) {
            }
            Companion companion = INSTANCE;
            lastNodeCount = intRef.element;
            String sb2 = sb.toString();
            Intrinsics.checkNotNullExpressionValue(sb2, "toString(...)");
            String obj = StringsKt.trim((CharSequence) sb2).toString();
            if (obj.length() <= 0) {
                z = false;
            }
            if (z) {
                // متن یکسان با آخرین capture → پردازش نکن
                String prev = lastDump;
                if (prev != null && prev.equals(obj)) {
                    // ignore duplicate screen text
                } else {
                    companion.recordDump(obj, "accessibility");
                    Pipeline.INSTANCE.process(this, obj, "accessibility");
                    tryAutoAcceptOnTripEnd(obj);
                }
            }
        } catch (Throwable unused6) {
        }
        this.inCapture = false;
    }

    private static final void captureAndProcess$collect(Ref.IntRef intRef, StringBuilder sb, AccessibilityNodeInfo accessibilityNodeInfo, int i) {
        AccessibilityNodeInfo accessibilityNodeInfo2;
        CharSequence tooltipText;
        String obj;
        String obj2;
        String obj3;
        if (accessibilityNodeInfo != null && i <= MAX_DEPTH && intRef.element <= MAX_NODES && sb.length() <= MAX_CHARS) {
            CharSequence packageName = accessibilityNodeInfo.getPackageName();
            String obj4 = packageName != null ? packageName.toString() : null;
            if (obj4 == null || Intrinsics.areEqual(obj4, SNAPP_PACKAGE)) {
                if (!accessibilityNodeInfo.isPassword()) {
                    CharSequence text = accessibilityNodeInfo.getText();
                    if (text != null && (obj3 = text.toString()) != null) {
                        if (!(!StringsKt.isBlank(obj3))) {
                            obj3 = null;
                        }
                        if (obj3 != null) {
                            sb.append(obj3);
                            sb.append('\n');
                        }
                    }
                    CharSequence contentDescription = accessibilityNodeInfo.getContentDescription();
                    if (contentDescription != null && (obj2 = contentDescription.toString()) != null) {
                        if (!(!StringsKt.isBlank(obj2))) {
                            obj2 = null;
                        }
                        if (obj2 != null) {
                            sb.append(obj2);
                            sb.append('\n');
                        }
                    }
                    if (Build.VERSION.SDK_INT >= 28 && (tooltipText = accessibilityNodeInfo.getTooltipText()) != null && (obj = tooltipText.toString()) != null) {
                        if (!(!StringsKt.isBlank(obj))) {
                            obj = null;
                        }
                        if (obj != null) {
                            sb.append(obj);
                            sb.append('\n');
                        }
                    }
                    intRef.element++;
                }
                int childCount = accessibilityNodeInfo.getChildCount();
                for (int i2 = 0; i2 < childCount; i2++) {
                    try {
                        accessibilityNodeInfo2 = accessibilityNodeInfo.getChild(i2);
                    } catch (Throwable unused) {
                        accessibilityNodeInfo2 = null;
                    }
                    if (accessibilityNodeInfo2 != null) {
                        captureAndProcess$collect(intRef, sb, accessibilityNodeInfo2, i + 1);
                        try {
                            accessibilityNodeInfo2.recycle();
                        } catch (Throwable unused2) {
                        }
                    }
                }
            }
        }
    }
}
