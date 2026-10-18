package ir.tripanalyzer.app;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.widget.Toast;
import ir.tripanalyzer.app.UiKit;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import org.json.JSONObject;

/* compiled from: MainActivity.kt */
@Metadata(d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u000fH\u0014J\b\u0010\u0010\u001a\u00020\rH\u0014J\b\u0010\u0011\u001a\u00020\rH\u0002J\b\u0010\u0012\u001a\u00020\rH\u0002J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0002J\u0010\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u000bH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082.¢\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\u0007X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\u0005X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e¢\u0006\u0002\n\u0000"}, d2 = {"Lir/tripanalyzer/app/MainActivity;", "Lir/tripanalyzer/app/BaseActivity;", "<init>", "()V", "content", "Landroid/widget/LinearLayout;", "accPill", "Landroid/widget/TextView;", "notifPill", "lastBody", "permCard", "Landroid/view/View;", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "onResume", "buildUi", "refresh", "pillBg", "Landroid/graphics/drawable/GradientDrawable;", "color", "", "wrap", "Landroid/view/ViewGroup;", "v"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class MainActivity extends BaseActivity {
    private TextView accPill;
    private LinearLayout content;
    private LinearLayout tipCard;
    private TextView tipBody;
    private TextView tipWarnBtn;
    private boolean tipExpanded = true;
    private final android.os.Handler tipHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable tipCollapseRunnable;
    private TextView distanceLabel;
    private TextView speedLabel;
    private TextView distUnit;
    private TextView speedUnit;
    private LinearLayout avgHourlyBody;
    private TextView gpsFuelUnit;
    private TextView gpsDepUnit;
    private TextView acceptedIncomeLabel;
    private TextView gpsFuelLabel;
    private TextView gpsDepLabel;
    private LinearLayout chartTrendHost;
    private LinearLayout benchmarkHost;
    private LinearLayout lastBody;
    private TextView notifPill;
    private View permCard;

    public static void openAccessibilitySettings(MainActivity mainActivity) {
        try {
            mainActivity.startActivity(new Intent(mainActivity, PermissionsOnboardingActivity.class));
        } catch (Exception unused) {
            try {
                mainActivity.startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));
            } catch (Exception ignored) {}
        }
    }

    public static void openNotificationSettings(MainActivity mainActivity) {
        try {
            mainActivity.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        } catch (Exception unused) {
        }
    }

    private final void requestBatteryUnrestricted() {
        try {
            PowerManager powerManager = (PowerManager) getSystemService("power");
            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } catch (Exception unused) {
        }
    }

    public static void resetGpsDistance(MainActivity mainActivity) {
        GpsTracker.INSTANCE.reset(mainActivity);
        if (mainActivity.distanceLabel != null) {
            mainActivity.distanceLabel.setText("0.0");
            if (mainActivity.distUnit != null) mainActivity.distUnit.setText("km");
        }
        if (mainActivity.speedLabel != null) {
            mainActivity.speedLabel.setText("—");
            if (mainActivity.speedUnit != null) mainActivity.speedUnit.setText("km/h");
        }
        Toast.makeText(mainActivity, "مسافت صفر شد", 0).show();
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i != 1001 || iArr == null || iArr.length <= 0 || iArr[0] != 0) {
            return;
        }
        GpsTracker.INSTANCE.start(this);
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        // اگر نشست سمت سرور بسته شده (دستگاه دیگر / مسدود / خروج) → صفحهٔ ورود
        try {
            if (!LicenseStore.INSTANCE.isLoggedIn(this)) {
                startActivity(new Intent(this, (Class<?>) LoginActivity.class));
                finish();
                return;
            }
            CloudSync.syncIfDue(this);
        } catch (Throwable ignored) {}
        try {
            if (Store.INSTANCE.isOnboardingDone(this)
                    && !Status.INSTANCE.accessibilityEnabled(this)) {
                startActivity(new Intent(this, PermissionsOnboardingActivity.class));
                finish();
                return;
            }
        } catch (Throwable ignored) {}

        // درخواست باتری به ویزارد دسترسی‌ها منتقل شد (PermissionsOnboardingActivity)
        if (this.content == null) {
            return;
        }
        try {
            if (checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") != 0) {
                requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 1001);
            } else {
                GpsTracker.INSTANCE.start(this);
            }
        } catch (Exception unused) {
        }
        refresh();
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // دوره آزمایشی فعلاً غیرفعال است؛ فقط در صورت فعال بودن TRIAL_ENABLED شروع می‌شود
        LicenseStore.INSTANCE.ensureTrialStarted(this);
        // بدون ورود → صفحه ورود با تلگرام
        if (!LicenseStore.INSTANCE.isLoggedIn(this)) {
            startActivity(new Intent(this, (Class<?>) LoginActivity.class));
            finish();
            return;
        }
        // بدون فعال‌سازی → صفحه کد / تست رایگان
        if (!LicenseStore.INSTANCE.hasEverActivated(this)
                && !LicenseStore.INSTANCE.canUseAnalysis(this)) {
            startActivity(new Intent(this, (Class<?>) ActivationActivity.class));
            finish();
            return;
        }
        // چک ساعت یک‌بار در روز استفاده
        try {
            TimeGuard.INSTANCE.checkOncePerDay(this, new TimeGuard.Callback() {
                @Override public void onClockOk() {}
                @Override public void onClockTampered(long networkMs, long deviceMs) {
                    try {
                        startActivity(new Intent(MainActivity.this, ActivationActivity.class));
                        finish();
                    } catch (Throwable ignored) {}
                }
            });
        } catch (Throwable ignored) {}
        MainActivity mainActivity = this;
        if (!Store.INSTANCE.isOnboardingDone(mainActivity)) {
            startActivity(new Intent(mainActivity, (Class<?>) OnboardingActivity.class));
            finish();
            return;
        }
        // ویزارد دسترسی اجباری تا Accessibility روشن شود
        try {
            boolean accOn = Status.INSTANCE.accessibilityEnabled(mainActivity);
            boolean wizDone = Store.INSTANCE.isPermissionsWizardDone(mainActivity);
            if (!accOn || !wizDone) {
                startActivity(new Intent(mainActivity, (Class<?>) PermissionsOnboardingActivity.class));
                finish();
                return;
            }
        } catch (Throwable ignored) {}
        UiKit uiKit = UiKit.INSTANCE;
        String string = getString(R.string.dashboard_title);
        Intrinsics.checkNotNullExpressionValue(string, "getString(...)");
        UiKit.Page page = uiKit.page(mainActivity, string);
        this.content = page.getContent();
        buildUi();
        // هدر بدون همبرگر — ناوبری پایین صفحه
        attachHamburgerHeader(page);

        LinearLayout rootShell = new LinearLayout(this);
        rootShell.setOrientation(LinearLayout.VERTICAL);
        rootShell.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        android.view.View scroll = page.getScroll();
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, 0, 1f);
        rootShell.addView(scroll, sp);
        rootShell.addView(buildBottomNav());
        setContentView(rootShell);
    }

    private LinearLayout buildBottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(android.view.Gravity.CENTER_VERTICAL);
        nav.setPadding(UiKit.INSTANCE.dp(this, 8), UiKit.INSTANCE.dp(this, 10),
                UiKit.INSTANCE.dp(this, 8), UiKit.INSTANCE.dp(this, 10));
        nav.setLayoutDirection(android.view.View.LAYOUT_DIRECTION_LTR);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFFFFFFFF);
        bg.setCornerRadius(UiKit.INSTANCE.dp(this, 28));
        try {
            nav.setElevation(UiKit.INSTANCE.dp(this, 8));
        } catch (Throwable ignored) {}
        nav.setBackground(bg);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(-1, -2);
        int m = UiKit.INSTANCE.dp(this, 12);
        nlp.setMargins(m, m / 2, m, m);
        nav.setLayoutParams(nlp);

        nav.addView(navItem("⌂", "خانه", true, null));
        nav.addView(navItem("➤", "جزئیات", false, DetailsActivity.class));
        nav.addView(navItem("▦", "آمار", false, ReportsActivity.class));
        nav.addView(navItem("☰", "منو", false, null));
        return nav;
    }

    private LinearLayout navItem(String icon, String label, boolean selected, final Class<?> target) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
        item.setLayoutParams(lp);
        item.setPadding(0, UiKit.INSTANCE.dp(this, 4), 0, UiKit.INSTANCE.dp(this, 4));
        if (selected) {
            GradientDrawable pill = new GradientDrawable();
            pill.setCornerRadius(UiKit.INSTANCE.dp(this, 16));
            pill.setColor(0xFFF1F5F9);
            item.setBackground(pill);
            item.setPadding(UiKit.INSTANCE.dp(this, 8), UiKit.INSTANCE.dp(this, 6),
                    UiKit.INSTANCE.dp(this, 8), UiKit.INSTANCE.dp(this, 6));
        }
        TextView ic = new TextView(this);
        ic.setText(icon);
        ic.setTextSize(20f);
        ic.setGravity(android.view.Gravity.CENTER);
        ic.setTextColor(selected ? 0xFF0F172A : 0xFF64748B);
        item.addView(ic);
        TextView lb = new TextView(this);
        lb.setText(label);
        lb.setTextSize(11f);
        lb.setGravity(android.view.Gravity.CENTER);
        lb.setTextColor(selected ? 0xFF0F172A : 0xFF64748B);
        item.addView(lb);
        item.setClickable(true);
        final MainActivity self = this;
        item.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (target != null) {
                    startActivity(new Intent(self, target));
                } else if ("منو".equals(label)) {
                    showAppMenu(v);
                }
                // خانه: همین صفحه
            }
        });
        return item;
    }

    private final void attachHamburgerHeader(UiKit.Page page) {
        TextView titleView = page.getTitle();
        LinearLayout content = page.getContent();
        content.removeView(titleView);
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        // LTR تا: چپ = پروفایل، راست = منو
        header.setLayoutDirection(android.view.View.LAYOUT_DIRECTION_LTR);
        header.setPadding(0, UiKit.INSTANCE.dp(this, 4), 0, UiKit.INSTANCE.dp(this, 12));

        // پروفایل سمت چپ
        LinearLayout profile = new LinearLayout(this);
        profile.setOrientation(LinearLayout.HORIZONTAL);
        profile.setGravity(android.view.Gravity.CENTER_VERTICAL);
        profile.setPadding(UiKit.INSTANCE.dp(this, 4), 0, UiKit.INSTANCE.dp(this, 8), 0);

        ImageView avatar = new ImageView(this);
        int s = UiKit.INSTANCE.dp(this, 40);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(s, s);
        avatar.setLayoutParams(alp);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        try {
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(0x33FFFFFF);
            avatar.setBackground(circle);
            avatar.setClipToOutline(true);
            avatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
                @Override public void getOutline(android.view.View view, android.graphics.Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
        } catch (Exception ignored) {}
        Bitmap photo = ProfileStore.INSTANCE.loadPhotoBitmap(this);
        if (photo != null) {
            avatar.setImageBitmap(photo);
        } else {
            try { avatar.setImageResource(R.mipmap.ic_launcher); } catch (Exception e) {}
        }
        profile.addView(avatar);

        TextView nameTv = new TextView(this);
        nameTv.setText(ProfileStore.INSTANCE.getDisplayName(this));
        nameTv.setTextSize(14.0f);
        nameTv.setTextColor(0xFFFFFFFF);
        nameTv.setPadding(UiKit.INSTANCE.dp(this, 8), 0, 0, 0);
        nameTv.setMaxLines(1);
        profile.addView(nameTv);
        header.addView(profile);

        titleView.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
        titleView.setTextColor(0xFFFFFFFF);
        titleView.setGravity(android.view.Gravity.CENTER);
        if (titleView.getText() != null && titleView.getText().toString().contains("RAIDO")) {
            titleView.setTypeface(Fonts.INSTANCE.english());
        }
        header.addView(titleView);

        // منو سمت راست
        TextView menuBtn = new TextView(this);
        menuBtn.setText("☰");
        menuBtn.setTextSize(28.0f);
        menuBtn.setTextColor(0xFFFFFFFF);
        menuBtn.setPadding(UiKit.INSTANCE.dp(this, 8), UiKit.INSTANCE.dp(this, 4), UiKit.INSTANCE.dp(this, 4), UiKit.INSTANCE.dp(this, 4));
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAppMenu(v);
            }
        });
        /* menuBtn removed — bottom nav */
        content.addView(header, 0);
    }

    private final void showAppMenu(View anchor) {
        final String[] items = new String[]{
                "تنظیمات",
                "محدوده فعالیت",
                "آمار و گزارش‌ها",
                "جزئیات آخرین نتیجه",
                "اشتراک و مالی",
                "درباره و پشتیبانی"
        };
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(this);
        b.setTitle("منوی RAIDO");
        b.setItems(items, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                Class<?> target = null;
                switch (which) {
                    case 0: target = SettingsActivity.class; break;
                    case 1: target = ActivityZoneActivity.class; break;
                    case 2: target = ReportsActivity.class; break;
                    case 3: target = DetailsActivity.class; break;
                    case 4: target = SubscriptionActivity.class; break;
                    case 5: target = SupportActivity.class; break;
                    default: break;
                }
                if (target != null) {
                    startActivity(new Intent(MainActivity.this, target));
                }
            }
        });
        b.setNegativeButton("بستن", null);
        b.show();
    }

    // ───────── helpers ظاهری داشبورد ─────────
    private int dpi(int v) { return UiKit.INSTANCE.dp(this, v); }

    private GradientDrawable shape(int fill, int radiusDp, int strokeColor) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dpi(radiusDp));
        g.setColor(fill);
        if (strokeColor != 0) g.setStroke(Math.max(1, dpi(1)), strokeColor);
        return g;
    }

    private TextView tv(String s, float sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(null, android.graphics.Typeface.BOLD);
        return t;
    }

    /** کارت یکدست داشبورد: شعاع ۲۲dp، لبهٔ نازک، سایهٔ کم */
    private LinearLayout dashCard(int fill, int stroke) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(shape(fill, 22, stroke));
        c.setPadding(dpi(18), dpi(16), dpi(18), dpi(16));
        try { c.setElevation(dpi(2)); } catch (Throwable ignored) {}
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dpi(14));
        c.setLayoutParams(lp);
        return c;
    }

    private TextView chip(String glyph, int bg, int sizeDp) {
        TextView t = tv(glyph, 14f, 0xFFFFFFFF, true);
        t.setGravity(android.view.Gravity.CENTER);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(bg);
        t.setBackground(g);
        t.setLayoutParams(new LinearLayout.LayoutParams(dpi(sizeDp), dpi(sizeDp)));
        return t;
    }

    /** مربع GPS: out[0]=عدد، out[1]=واحد */
    private LinearLayout gpsTile(String caption, String glyph, int tint, int edge, int accent, String num, String unit, TextView[] out) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dpi(14), dpi(14), dpi(14), dpi(14));
        box.setBackground(shape(tint, 20, edge));
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(android.view.Gravity.CENTER_VERTICAL);
        head.addView(chip(glyph, accent, 28));
        TextView cap = tv(caption, 13f, 0xFF475569, false);
        cap.setPaddingRelative(dpi(8), 0, 0, 0);
        head.addView(cap);
        box.addView(head);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        row.setGravity(android.view.Gravity.BOTTOM);
        TextView n = tv(num, 30f, 0xFF0F172A, true);
        n.setSingleLine(true);
        TextView u = tv(unit, 13f, 0xFF64748B, false);
        u.setPadding(dpi(5), 0, 0, dpi(4));
        row.addView(n);
        row.addView(u);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-2, -2);
        rlp.topMargin = dpi(10);
        rlp.gravity = android.view.Gravity.START;
        box.addView(row, rlp);
        out[0] = n;
        out[1] = u;
        return box;
    }

    /** عنوان بخش با نوار رنگی کوتاه */
    private LinearLayout sectionHeader(String title, int accent, String sub) {
        LinearLayout h = new LinearLayout(this);
        h.setOrientation(LinearLayout.HORIZONTAL);
        h.setGravity(android.view.Gravity.CENTER_VERTICAL);
        View bar = new View(this);
        bar.setBackground(shape(accent, 3, 0));
        h.addView(bar, new LinearLayout.LayoutParams(dpi(4), dpi(sub == null ? 20 : 34)));
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPaddingRelative(dpi(10), 0, 0, 0);
        col.addView(tv(title, 17f, 0xFF0F172A, true));
        if (sub != null) col.addView(tv(sub, 13f, 0xFF64748B, false));
        h.addView(col, new LinearLayout.LayoutParams(0, -2, 1f));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dpi(12);
        h.setLayoutParams(lp);
        return h;
    }

    private LinearLayout softBox(int fill, int stroke) {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setBackground(shape(fill, 16, stroke));
        b.setPadding(dpi(14), dpi(12), dpi(14), dpi(12));
        b.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        return b;
    }

    /** ردیف برچسب | مقدار داخل جعبهٔ ملایم */
    private View statRow(String label, String value, boolean muted, boolean last) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(android.view.Gravity.CENTER_VERTICAL);
        r.setBackground(shape(0xFFF8FAFC, 14, 0xFFE2E8F0));
        r.setPadding(dpi(14), dpi(12), dpi(14), dpi(12));
        r.addView(tv(label, 14f, 0xFF475569, false), new LinearLayout.LayoutParams(0, -2, 1f));
        r.addView(tv(value, muted ? 14f : 15f, muted ? 0xFF94A3B8 : 0xFF0F172A, !muted));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        if (!last) lp.bottomMargin = dpi(8);
        r.setLayoutParams(lp);
        return r;
    }

    /** مربع بنزین/استهلاک: out[0]=عدد، out[1]=واحد */
    private LinearLayout costTile(String caption, int tint, int edge, int accent, TextView[] out) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dpi(14), dpi(14), dpi(14), dpi(14));
        box.setBackground(shape(tint, 20, edge));
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(android.view.Gravity.CENTER_VERTICAL);
        View dot = new View(this);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(accent);
        dot.setBackground(g);
        head.addView(dot, new LinearLayout.LayoutParams(dpi(10), dpi(10)));
        TextView cap = tv(caption, 13f, 0xFF475569, false);
        cap.setPaddingRelative(dpi(8), 0, 0, 0);
        head.addView(cap);
        box.addView(head);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.BOTTOM);
        TextView n = tv("—", 22f, 0xFF0F172A, true);
        n.setSingleLine(true);
        TextView u = tv("تومان", 12f, 0xFF64748B, false);
        u.setPaddingRelative(dpi(5), 0, 0, dpi(3));
        row.addView(n);
        row.addView(u);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-2, -2);
        rlp.topMargin = dpi(12);
        box.addView(row, rlp);
        out[0] = n;
        out[1] = u;
        return box;
    }

    private UiKit.StatusRow serviceRow(String label, View.OnClickListener click) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(android.view.Gravity.CENTER_VERTICAL);
        r.setMinimumHeight(dpi(56));
        TextView l = tv(label, 15f, 0xFF0F172A, false);
        r.addView(l, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView p = tv("", 13f, 0xFFFFFFFF, true);
        p.setBackground(pillBg(UiKit.VERDICT_GRAY));
        p.setPadding(dpi(14), dpi(6), dpi(14), dpi(6));
        r.addView(p);
        r.setOnClickListener(click);
        return new UiKit.StatusRow(r, p);
    }

    private TextView fuelUnitOf(LinearLayout box) {
        try {
            LinearLayout row = (LinearLayout) box.getChildAt(1);
            return (TextView) row.getChildAt(1);
        } catch (Throwable t) { return null; }
    }

    private View divider() {
        View d = new View(this);
        d.setBackgroundColor(0xFFE2E8F0);
        d.setLayoutParams(new LinearLayout.LayoutParams(-1, Math.max(1, dpi(1))));
        return d;
    }

    private void addHeaderTitle(LinearLayout card, String title) {
        TextView t = tv(title, 18f, 0xFF0F172A, true);
        t.setPadding(0, 0, 0, dpi(10));
        card.addView(t);
    }

    private final void buildUi() {
        final MainActivity mainActivity = this;

        // ── کارت اشتراک ──
        boolean accessOk = LicenseStore.INSTANCE.isAccessActive(mainActivity);
        String statusText = LicenseStore.INSTANCE.getStatusLabel(mainActivity);
        LinearLayout subCard = dashCard(accessOk ? 0xFFF0FDF4 : 0xFFFEF2F2, accessOk ? 0xFFBBF7D0 : 0xFFFECACA);
        LinearLayout subRow = new LinearLayout(mainActivity);
        subRow.setOrientation(LinearLayout.HORIZONTAL);
        subRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        subRow.addView(chip(accessOk ? "✓" : "✗", accessOk ? 0xFF16A34A : 0xFFDC2626, 32));
        TextView subTitle = tv(statusText, 17f, accessOk ? 0xFF166534 : 0xFF991B1B, true);
        subTitle.setPaddingRelative(dpi(12), 0, 0, 0);
        subRow.addView(subTitle, new LinearLayout.LayoutParams(0, -2, 1f));
        subCard.addView(subRow);
        if (!accessOk) {
            TextView expHint = tv(LicenseStore.INSTANCE.isActivationExpired(mainActivity)
                    ? "کد منقضی شده — کارت تحلیل غیرفعال است. گزارش‌ها و آمار همچنان قابل مشاهده‌اند."
                    : "برای استفاده از کارت تحلیل باید کد فعال‌سازی وارد کنید.", 14f, 0xFF991B1B, false);
            expHint.setPadding(0, dpi(10), 0, dpi(4));
            subCard.addView(expHint);
            Button subBtn = UiKit.INSTANCE.button(mainActivity, "ورود / تمدید کد فعال‌سازی", true);
            subBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent ai = new Intent(MainActivity.this, ActivationActivity.class);
                    ai.putExtra(ActivationActivity.EXTRA_RENEW, true);
                    startActivity(ai);
                }
            });
            subCard.addView(subBtn);
        }
        this.content.addView(subCard);

        // ── نکتهٔ مهم: فقط بار اول باز؛ بعد از آن جمع‌شونده ──
        this.tipCard = dashCard(0xFFFFF7ED, 0xFFFED7AA);
        LinearLayout tipHeader = new LinearLayout(mainActivity);
        tipHeader.setOrientation(LinearLayout.HORIZONTAL);
        tipHeader.setGravity(android.view.Gravity.CENTER_VERTICAL);
        tipHeader.addView(chip("!", 0xFFEA580C, 30));
        TextView tipTitle = tv("برای تحلیل دقیق", 16f, 0xFF9A3412, true);
        tipTitle.setPaddingRelative(dpi(10), 0, 0, 0);
        tipHeader.addView(tipTitle, new LinearLayout.LayoutParams(0, -2, 1f));
        this.tipWarnBtn = tv("▾", 18f, 0xFFEA580C, true);
        this.tipWarnBtn.setPadding(dpi(10), dpi(4), dpi(4), dpi(4));
        tipHeader.addView(this.tipWarnBtn);
        tipHeader.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (tipExpanded) collapseTipBanner(); else expandTipBanner();
            }
        });
        this.tipCard.addView(tipHeader);
        this.tipBody = tv("بعد از آمدن درخواست در اسنپ، حتماً «مشاهده جزئیات» را بزنید تا کیلومتر واقعی خوانده شود.\nرایدو تا قبل از دیدن کیلومتر تحلیل نهایی نمی‌دهد.", 14.5f, 0xFF7C2D12, false);
        this.tipBody.setLineSpacing(dpi(3), 1f);
        this.tipBody.setPadding(0, dpi(10), 0, 0);
        this.tipCard.addView(this.tipBody);
        this.content.addView(this.tipCard);
        boolean tipSeen = false;
        try { tipSeen = getSharedPreferences("raido_ui", 0).getBoolean("tip_seen_v1", false); } catch (Throwable ignored) {}
        if (tipSeen) {
            collapseTipBanner();
        } else {
            try { getSharedPreferences("raido_ui", 0).edit().putBoolean("tip_seen_v1", true).apply(); } catch (Throwable ignored) {}
            scheduleTipCollapse();
        }

        // ── وضعیت سرویس‌ها + GPS ──
        LinearLayout card = dashCard(0xFFFFFFFF, 0xFFE2E8F0);
        addHeaderTitle(card, getString(R.string.services_status));

        LinearLayout gpsRow = new LinearLayout(mainActivity);
        gpsRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView[] dOut = new TextView[2];
        TextView[] sOut = new TextView[2];
        LinearLayout boxDist = gpsTile("مسافت واقعی", "↔", 0xFFEFF6FF, 0xFFBFDBFE, 0xFF2563EB, "--", "km", dOut);
        LinearLayout boxSpd = gpsTile("میانگین سرعت", "➤", 0xFFF0FDF4, 0xFFBBF7D0, 0xFF16A34A, "—", "km/h", sOut);
        this.distanceLabel = dOut[0]; this.distUnit = dOut[1];
        this.speedLabel = sOut[0]; this.speedUnit = sOut[1];
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, -2, 1f);
        half.setMargins(0, 0, dpi(5), 0);
        gpsRow.addView(boxDist, half);
        LinearLayout.LayoutParams half2 = new LinearLayout.LayoutParams(0, -2, 1f);
        half2.setMargins(dpi(5), 0, 0, 0);
        gpsRow.addView(boxSpd, half2);
        card.addView(gpsRow);

        Button button = UiKit.INSTANCE.button(mainActivity, "صفر کردن مسافت GPS", false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { MainActivity.resetGpsDistance(MainActivity.this); }
        });
        card.addView(button);

        card.addView(divider());
        UiKit.StatusRow statusRow = serviceRow(getString(R.string.status_accessibility), new View.OnClickListener() {
            @Override public void onClick(View view) { MainActivity.openAccessibilitySettings(MainActivity.this); }
        });
        card.addView(statusRow.getContainer());
        card.addView(divider());
        UiKit.StatusRow statusRow2 = serviceRow(getString(R.string.status_notification), new View.OnClickListener() {
            @Override public void onClick(View view) { MainActivity.openNotificationSettings(MainActivity.this); }
        });
        card.addView(statusRow2.getContainer());
        card.addView(divider());
        this.accPill = statusRow.getPill();
        this.notifPill = statusRow2.getPill();

        LinearLayout swRow = new LinearLayout(mainActivity);
        swRow.setOrientation(LinearLayout.HORIZONTAL);
        swRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        swRow.setMinimumHeight(dpi(56));
        Switch r13 = new Switch(mainActivity);
        r13.setChecked(Store.INSTANCE.isAnalysisEnabled(mainActivity));
        r13.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                MainActivity.buildUi$lambda$1(MainActivity.this, compoundButton, z);
            }
        });
        swRow.addView(tv(getString(R.string.status_auto_analysis), 15f, 0xFF0F172A, false), new LinearLayout.LayoutParams(0, -2, 1f));
        swRow.addView(r13);
        card.addView(swRow);
        this.content.addView(card);

        // ── کارت دسترسی‌ها (فقط وقتی ناقص است) ──
        LinearLayout card2 = dashCard(0xFFFEF2F2, 0xFFFECACA);
        card2.addView(tv(getString(R.string.perm_title), 17f, 0xFF991B1B, true));
        TextView permBody = tv(getString(R.string.perm_body), 14f, 0xFF7F1D1D, false);
        permBody.setPadding(0, dpi(6), 0, 0);
        card2.addView(permBody);
        Button button2 = UiKit.INSTANCE.button(mainActivity, getString(R.string.enable_in_settings), true);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { MainActivity.buildUi$lambda$3(MainActivity.this, view); }
        });
        card2.addView(button2);
        this.content.addView(card2);
        this.permCard = card2;

        // ── آخرین نتیجه ──
        LinearLayout card3 = dashCard(0xFFFFFFFF, 0xFFE2E8F0);
        card3.addView(sectionHeader(getString(R.string.last_result), 0xFF2563EB, null));
        LinearLayout lastHost = new LinearLayout(mainActivity);
        lastHost.setOrientation(LinearLayout.VERTICAL);
        card3.addView(lastHost);
        Button button3 = UiKit.INSTANCE.button(mainActivity, getString(R.string.view_details), false);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { MainActivity.buildUi$lambda$4(MainActivity.this, view); }
        });
        card3.addView(button3);
        this.content.addView(card3);
        this.lastBody = lastHost;

        // ── میانگین ساعتی ──
        try {
            final MainActivity act = mainActivity;
            LinearLayout avgCard = dashCard(0xFFFFFFFF, 0xFFE2E8F0);
            avgCard.addView(sectionHeader("میانگین ساعتی درخواست‌ها", 0xFF7C3AED, "درآمد ساعتی تحلیل‌شده‌ها"));
            LinearLayout avgHost = new LinearLayout(act);
            avgHost.setOrientation(LinearLayout.VERTICAL);
            avgCard.addView(avgHost);
            this.avgHourlyBody = avgHost;
            fillAvgHourlyBody(act, avgHost);
            avgCard.setClickable(true);
            avgCard.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Intent i = new Intent(act, ReportsActivity.class);
                    i.putExtra(ReportsActivity.EXTRA_TAB, 2);
                    act.startActivity(i);
                }
            });
            this.content.addView(avgCard);
        } catch (Throwable ignored) {}

        // ── درآمد قبول‌شده، بنزین/استهلاک، روند، بنچمارک ──
        try {
            final MainActivity act2 = mainActivity;
            LinearLayout accCard = dashCard(0xFFECFDF5, 0xFFA7F3D0);
            accCard.addView(sectionHeader("درآمد ساعتی قبول‌شده‌ها", 0xFF059669, "فقط سفرهایی که «رفتم» زده‌اید"));
            TextView accVal = tv("", 24f, 0xFF047857, true);
            accVal.setLineSpacing(dpi(3), 1f);
            accVal.setPadding(0, dpi(2), 0, 0);
            this.acceptedIncomeLabel = accVal;
            accCard.addView(accVal);
            accCard.setClickable(true);
            accCard.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Intent i = new Intent(act2, ReportsActivity.class);
                    i.putExtra(ReportsActivity.EXTRA_TAB, 3);
                    act2.startActivity(i);
                }
            });
            this.content.addView(accCard);

            LinearLayout costRow = new LinearLayout(act2);
            costRow.setOrientation(LinearLayout.HORIZONTAL);
            TextView[] fo = new TextView[1];
            TextView[] dpo = new TextView[1];
            LinearLayout fuelBox = costTile("بنزین واقعی GPS", 0xFFFFFBEB, 0xFFFDE68A, 0xFFF59E0B, fo);
            LinearLayout depBox = costTile("استهلاک واقعی GPS", 0xFFF5F3FF, 0xFFDDD6FE, 0xFF7C3AED, dpo);
            this.gpsFuelLabel = fo[0]; this.gpsFuelUnit = fuelUnitOf(fuelBox);
            this.gpsDepLabel = dpo[0]; this.gpsDepUnit = fuelUnitOf(depBox);
            LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(0, -2, 1f);
            hp.setMargins(0, 0, dpi(5), 0);
            costRow.addView(fuelBox, hp);
            LinearLayout.LayoutParams hp2 = new LinearLayout.LayoutParams(0, -2, 1f);
            hp2.setMargins(dpi(5), 0, 0, 0);
            costRow.addView(depBox, hp2);
            LinearLayout.LayoutParams lpC = new LinearLayout.LayoutParams(-1, -2);
            lpC.bottomMargin = dpi(14);
            this.content.addView(costRow, lpC);

            LinearLayout trendCard = dashCard(0xFFFFFFFF, 0xFFE2E8F0);
            trendCard.addView(sectionHeader("روند میانگین ساعتی", 0xFF0D9488, "۷ روز اخیر"));
            LinearLayout trendHost = new LinearLayout(act2);
            trendHost.setOrientation(LinearLayout.VERTICAL);
            trendHost.setPadding(0, 0, 0, 0);
            this.chartTrendHost = trendHost;
            trendCard.addView(trendHost);
            this.content.addView(trendCard);

            LinearLayout benchCard = dashCard(0xFFFFFFFF, 0xFFE2E8F0);
            benchCard.addView(sectionHeader("این ساعت در یک نگاه", 0xFFEA580C, "کف، بازار و آخرین تحلیل"));
            LinearLayout benchHost = new LinearLayout(act2);
            benchHost.setOrientation(LinearLayout.VERTICAL);
            benchHost.setPadding(0, 0, 0, 0);
            this.benchmarkHost = benchHost;
            benchCard.addView(benchHost);
            TextView bHint = tv("اعداد به هزار تومان · فقط نمایش", 12f, 0xFF94A3B8, false);
            bHint.setPadding(0, dpi(8), 0, 0);
            benchCard.addView(bHint);
            this.content.addView(benchCard);
        } catch (Throwable ignored) {}
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void buildUi$lambda$1(MainActivity mainActivity, CompoundButton compoundButton, boolean z) {
        MainActivity mainActivity2 = mainActivity;
        Store.INSTANCE.setAnalysisEnabled(mainActivity2, z);
        if (z) {
            try { GpsTracker.INSTANCE.start(mainActivity2); } catch (Throwable ignored) {}
            return;
        }
        try { GpsTracker.INSTANCE.stop(mainActivity2); } catch (Throwable ignored) {}
        Toast.makeText(mainActivity2, mainActivity.getString(R.string.auto_off_toast), 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void buildUi$lambda$3(MainActivity mainActivity, View view) {
        try {
            mainActivity.startActivity(new Intent(mainActivity, PermissionsOnboardingActivity.class));
        } catch (Throwable th) {
            try { mainActivity.startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS")); } catch (Throwable th2) {}
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void buildUi$lambda$4(MainActivity mainActivity, View view) {
        mainActivity.startActivity(new Intent(mainActivity, (Class<?>) DetailsActivity.class));
    }

    private static final void buildUi$nav(final MainActivity mainActivity, LinearLayout linearLayout, String str, final Class<?> cls) {
        Button button = UiKit.INSTANCE.button(mainActivity, str, false);
        button.setOnClickListener(new View.OnClickListener() { // from class: ir.tripanalyzer.app.MainActivity$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.buildUi$nav$lambda$5(mainActivity, cls, view);
            }
        });
        linearLayout.addView(button);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void buildUi$nav$lambda$5(MainActivity mainActivity, Class cls, View view) {
        mainActivity.startActivity(new Intent(mainActivity, (Class<?>) cls));
    }

    private final void refresh() {
        refreshGpsBoxes();
        if (this.avgHourlyBody != null) {
            try { fillAvgHourlyBody(this, this.avgHourlyBody); } catch (Throwable ignored) {}
        }
        MainActivity mainActivity = this;
        boolean accessibilityEnabled = Status.INSTANCE.accessibilityEnabled(mainActivity);
        boolean listenerEnabled = Status.INSTANCE.listenerEnabled(mainActivity);
        TextView textView2 = this.accPill;
        int i = R.string.state_enabled;
        if (textView2 != null) {
            textView2.setText(getString(accessibilityEnabled ? R.string.state_enabled : R.string.state_disabled));
        }
        TextView textView3 = this.accPill;
        int i2 = UiKit.VERDICT_GREEN;
        if (textView3 != null) {
            textView3.setBackground(pillBg(accessibilityEnabled ? -14774722 : -10523787));
        }
        TextView textView4 = this.notifPill;
        if (textView4 != null) {
            if (!listenerEnabled) {
                i = R.string.state_disabled;
            }
            textView4.setText(getString(i));
        }
        TextView textView5 = this.notifPill;
        if (textView5 != null) {
            if (!listenerEnabled) {
                i2 = -10523787;
            }
            textView5.setBackground(pillBg(i2));
        }
        View view = this.permCard;
        if (view != null) {
            boolean allOk = accessibilityEnabled && listenerEnabled;
            view.setVisibility(allOk ? 8 : 0);
        }
        LinearLayout linearLayout = this.lastBody;
        if (linearLayout != null) {
            linearLayout.removeAllViews();
        }
        JSONObject last = Store.INSTANCE.last(mainActivity);
        if (last == null) {
            LinearLayout linearLayout2 = this.lastBody;
            if (linearLayout2 != null) {
                UiKit uiKit = UiKit.INSTANCE;
                String string = getString(R.string.empty_last_result_title);
                Intrinsics.checkNotNullExpressionValue(string, "getString(...)");
                LinearLayout eb = softBox(0xFFF8FAFC, 0xFFE2E8F0);
                eb.addView(tv(string, 15f, 0xFF0F172A, true));
                this.lastBody.addView(eb);
            }
            LinearLayout linearLayout3 = this.lastBody;
            if (linearLayout3 != null) {
                UiKit uiKit2 = UiKit.INSTANCE;
                String string2 = getString(R.string.empty_last_result_body);
                Intrinsics.checkNotNullExpressionValue(string2, "getString(...)");
                TextView eb2 = tv(string2, 13.5f, 0xFF64748B, false);
                eb2.setLineSpacing(dpi(3), 1f);
                ((LinearLayout) linearLayout3.getChildAt(0)).addView(eb2);
                return;
            }
            return;
        }
        Pair<TripResult, Long> parseResult = Store.INSTANCE.parseResult(last);
        TripResult component1 = parseResult.component1();
        long longValue = parseResult.component2().longValue();
        TextView pill = UiKit.INSTANCE.pill(mainActivity, Ui.INSTANCE.verdictText(component1.getVerdict()), Ui.INSTANCE.verdictColor(component1.getVerdict()));
        LinearLayout linearLayout4 = this.lastBody;
        if (linearLayout4 != null) {
            linearLayout4.addView(wrap(pill));
        }
        if (component1.getVerdict() != Verdict.GRAY) {
            UiKit uiKit3 = UiKit.INSTANCE;
            Res res = Res.INSTANCE;
            Object[] objArr = new Object[1];
            Ui ui = Ui.INSTANCE;
            Double hourlyIncome = component1.getHourlyIncome();
            objArr[0] = ui.money(hourlyIncome != null ? hourlyIncome.doubleValue() : 0.0d);
            TextView text = uiKit3.text(mainActivity, res.str(R.string.hourly_income_format, objArr), 28.0f, 900, Integer.valueOf(Ui.INSTANCE.verdictColor(component1.getVerdict())));
            LinearLayout linearLayout5 = this.lastBody;
            if (linearLayout5 != null) {
                linearLayout5.addView(text);
            }
            LinearLayout linearLayout6 = this.lastBody;
            if (linearLayout6 != null) {
                UiKit uiKit4 = UiKit.INSTANCE;
                Object[] objArr2 = new Object[2];
                Ui ui2 = Ui.INSTANCE;
                Double netIncome = component1.getNetIncome();
                objArr2[0] = ui2.money(netIncome != null ? netIncome.doubleValue() : 0.0d);
                objArr2[1] = Ui.INSTANCE.ago(longValue, System.currentTimeMillis());
                String string3 = getString(R.string.net_income_ago_format, objArr2);
                Intrinsics.checkNotNullExpressionValue(string3, "getString(...)");
                linearLayout6.addView(UiKit.text$default(uiKit4, mainActivity, string3, 14.0f, 400, null, 16, null));
                return;
            }
            return;
        }
        LinearLayout linearLayout7 = this.lastBody;
        if (linearLayout7 != null) {
            UiKit uiKit5 = UiKit.INSTANCE;
            String string4 = getString(R.string.verdict_gray);
            Intrinsics.checkNotNullExpressionValue(string4, "getString(...)");
            linearLayout7.addView(uiKit5.text(mainActivity, string4, 14.0f, 700, Integer.valueOf(Ui.INSTANCE.verdictColor(component1.getVerdict()))));
        }
    }


    private final void refreshGpsBoxes() {
        TextView d = this.distanceLabel;
        TextView s = this.speedLabel;
        boolean on = false;
        try { on = GpsTracker.INSTANCE.isRunning() && Store.INSTANCE.isAnalysisEnabled(this); } catch (Throwable ignored) {}
        double km = 0;
        try { km = GpsTracker.INSTANCE.getKm(this); } catch (Throwable ignored) {}
        if (!on) {
            if (d != null) d.setText("—");
            if (s != null) s.setText("—");
            if (distUnit != null) distUnit.setText("GPS خاموش");
            if (speedUnit != null) speedUnit.setText("GPS خاموش");
            if (gpsFuelLabel != null) gpsFuelLabel.setText("خاموش");
            if (gpsDepLabel != null) gpsDepLabel.setText("خاموش");
            if (gpsFuelUnit != null) gpsFuelUnit.setVisibility(View.GONE);
            if (gpsDepUnit != null) gpsDepUnit.setVisibility(View.GONE);
        } else {
            if (d != null) d.setText(String.format(java.util.Locale.US, "%.1f", km));
            if (distUnit != null) distUnit.setText("km");
            if (s != null) {
                double spd = GpsTracker.INSTANCE.getAvgSpeedKmh(this);
                if (spd < 0) s.setText("—");
                else s.setText(String.format(java.util.Locale.US, "%.1f", spd));
            }
            if (speedUnit != null) speedUnit.setText("km/h");
            // بنزین و استهلاک واقعی از مسافت GPS (نمایش؛ نه تحلیل)
            try {
                EngineSettings es = Store.INSTANCE.settings(this);
                double fuel = km * (es.getFuelPer100() / 100.0) * es.getFuelPrice();
                double dep = km * es.getDepPerKm();
                if (gpsFuelLabel != null) gpsFuelLabel.setText(Ui.INSTANCE.money(fuel));
                if (gpsDepLabel != null) gpsDepLabel.setText(Ui.INSTANCE.money(dep));
                if (gpsFuelUnit != null) gpsFuelUnit.setVisibility(View.VISIBLE);
                if (gpsDepUnit != null) gpsDepUnit.setVisibility(View.VISIBLE);
            } catch (Throwable ignored) {
                if (gpsFuelLabel != null) gpsFuelLabel.setText("—");
                if (gpsDepLabel != null) gpsDepLabel.setText("—");
            }
        }
        // درآمد ساعتی قبول‌شده‌ها
        try {
            if (acceptedIncomeLabel != null) {
                RaidoDataStore.AcceptedAvg a = RaidoDataStore.INSTANCE.acceptedHourlyAvg(this);
                if (a.count > 0) {
                    acceptedIncomeLabel.setTextSize(24f);
                    acceptedIncomeLabel.setTextColor(0xFF047857);
                    acceptedIncomeLabel.setText(Ui.INSTANCE.money(a.avg) + " تومان\n" + a.count + " سفر قبول‌شده");
                } else {
                    acceptedIncomeLabel.setTextSize(14.5f);
                    acceptedIncomeLabel.setTextColor(0xFF64748B);
                    acceptedIncomeLabel.setText("هنوز سفری با «رفتم» ثبت نشده");
                }
            }
        } catch (Throwable ignored) {}
        // نمودار روند ۷روز
        try {
            if (chartTrendHost != null) {
                chartTrendHost.removeAllViews();
                float[] av = RaidoDataStore.INSTANCE.last7DayHourlyAvgs(this);
                boolean anyV = false;
                if (av != null) for (float f : av) if (f > 0) { anyV = true; break; }
                if (anyV) {
                    chartTrendHost.addView(ChartKit.areaSpark(this, av, 88));
                } else {
                    LinearLayout eb = softBox(0xFFF0FDFA, 0xFFCCFBF1);
                    TextView et = tv("هنوز دادهٔ کافی برای نمودار نیست. با ثبت تحلیل‌ها، روند ۷ روزه اینجا می‌آید.", 14f, 0xFF0F766E, false);
                    et.setLineSpacing(dpi(3), 1f);
                    eb.addView(et);
                    chartTrendHost.addView(eb);
                }
            }
        } catch (Throwable ignored) {}
        // بنچمارک کف / بازار / آخرین
        try {
            if (benchmarkHost != null) {
                benchmarkHost.removeAllViews();
                double floor = Store.INSTANCE.getAnalysisFloor(this);
                RaidoDataStore.MarketRef mr = RaidoDataStore.INSTANCE.marketRefNow(this);
                double market = mr.isValid() ? mr.avg : 0;
                double lastH = 0;
                try {
                    org.json.JSONObject last = Store.INSTANCE.last(this);
                    if (last != null && last.has("hourly")) lastH = last.optDouble("hourly", 0);
                } catch (Throwable ignored2) {}
                float maxRef = (float) Math.max(floor, Math.max(market, lastH));
                if (maxRef < 1) maxRef = 1;
                benchmarkHost.addView(ChartKit.hBarRow(this, "کف", (float) floor, maxRef, 0xFFFCA5A5, 0xFFDC2626));
                benchmarkHost.addView(ChartKit.hBarRow(this, "بازار", (float) market, maxRef, 0xFFFCD34D, 0xFFD97706));
                benchmarkHost.addView(ChartKit.hBarRow(this, "آخرین", (float) lastH, maxRef, 0xFF34D399, 0xFF059669));
            }
        } catch (Throwable ignored) {}
    }

    private static void fillAvgHourlyBody(MainActivity act, LinearLayout host) {
        if (act == null || host == null) return;
        host.removeAllViews();
        try {
            RaidoDataStore.DashboardHourly snap = RaidoDataStore.INSTANCE.snapshotDashboard(act);
            int h2 = snap.currentHour == 23 ? 24 : snap.currentHour + 1;
            String hRange = String.format(java.util.Locale.US, "%02d–%02d", snap.currentHour, h2);
            boolean hOk = snap.hourCount > 0;
            host.addView(act.statRow("الان (" + hRange + ")",
                    hOk ? (Ui.INSTANCE.money(snap.hourAvg) + " تومان · " + snap.hourCount + " تحلیل") : "هنوز تحلیلی نیست", !hOk, false));
            boolean dOk = snap.dayCount > 0;
            host.addView(act.statRow("امروز",
                    dOk ? (Ui.INSTANCE.money(snap.dayAvg) + " تومان · " + snap.dayCount + " تحلیل") : "هنوز تحلیلی ثبت نشده", !dOk, true));
        } catch (Throwable ignored) {}
        TextView more = act.tv("جزئیات روز و ساعت‌ها ←", 14f, 0xFF2563EB, true);
        more.setPadding(0, act.dpi(12), 0, 0);
        host.addView(more);
    }

    private final GradientDrawable pillBg(int color) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(UiKit.INSTANCE.dp(this, 16));
        gradientDrawable.setColor(color);
        return gradientDrawable;
    }

    private final ViewGroup wrap(View v) {
        MainActivity mainActivity = this;
        LinearLayout linearLayout = new LinearLayout(mainActivity);
        linearLayout.setPadding(0, UiKit.INSTANCE.dp(mainActivity, 6), 0, UiKit.INSTANCE.dp(mainActivity, 4));
        linearLayout.addView(v);
        return linearLayout;
    }

    private void scheduleTipCollapse() {
        try {
            if (tipCollapseRunnable != null) tipHandler.removeCallbacks(tipCollapseRunnable);
            tipExpanded = true;
            if (tipBody != null) tipBody.setVisibility(View.VISIBLE);
            if (tipWarnBtn != null) tipWarnBtn.setText("▴");
            tipCollapseRunnable = new Runnable() {
                @Override public void run() { collapseTipBanner(); }
            };
            tipHandler.postDelayed(tipCollapseRunnable, 12L * 1000L);
        } catch (Throwable ignored) {}
    }

    private void collapseTipBanner() {
        try {
            if (tipCollapseRunnable != null) tipHandler.removeCallbacks(tipCollapseRunnable);
            tipExpanded = false;
            if (tipBody != null) tipBody.setVisibility(View.GONE);
            if (tipWarnBtn != null) tipWarnBtn.setText("▾");
        } catch (Throwable ignored) {}
    }

    private void expandTipBanner() {
        scheduleTipCollapse();
    }

    @Override
    protected void onDestroy() {
        try { if (tipCollapseRunnable != null) tipHandler.removeCallbacks(tipCollapseRunnable); } catch (Throwable ignored) {}
        super.onDestroy();
    }

}
