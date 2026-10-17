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
    private TextView avgHourlyBody;
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
            mainActivity.distanceLabel.setText("0.0 km");
        }
        if (mainActivity.speedLabel != null) {
            mainActivity.speedLabel.setText("—");
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
        try { if (tipCard != null) scheduleTipCollapse(); } catch (Throwable ignored) {}
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

    private final void buildUi() {
        MainActivity mainActivity = this;

        // کارت وضعیت اشتراک (سبز / قرمز)
        LinearLayout subCard = UiKit.INSTANCE.card(mainActivity);
        boolean accessOk = LicenseStore.INSTANCE.isAccessActive(mainActivity);
        String statusText = LicenseStore.INSTANCE.getStatusLabel(mainActivity);
        TextView subTitle = new TextView(mainActivity);
        subTitle.setText(accessOk ? "✓  " + statusText : "✗  " + statusText);
        subTitle.setTextSize(15.0f);
        subTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        subTitle.setTextColor(accessOk ? 0xFF16A34A : 0xFFDC2626);
        subCard.addView(subTitle);
        if (!accessOk) {
            TextView expHint = new TextView(mainActivity);
            if (LicenseStore.INSTANCE.isActivationExpired(mainActivity)) {
                expHint.setText("کد منقضی شده — کارت تحلیل غیرفعال است. گزارش‌ها و آمار همچنان قابل مشاهده‌اند.");
            } else {
                expHint.setText("برای استفاده از کارت تحلیل باید کد فعال‌سازی وارد کنید.");
            }
            expHint.setTextSize(13.0f);
            expHint.setTextColor(0xFFDC2626);
            expHint.setPadding(0, 8, 0, 8);
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

        // هشدار مهم: مشاهده جزئیات اسنپ
        this.tipCard = UiKit.INSTANCE.card(mainActivity);
        this.tipCard.setBackgroundColor(0xFFFFF7ED);
        LinearLayout tipHeader = new LinearLayout(mainActivity);
        tipHeader.setOrientation(LinearLayout.HORIZONTAL);
        tipHeader.setGravity(android.view.Gravity.CENTER_VERTICAL);
        TextView tipTitle = new TextView(mainActivity);
        tipTitle.setText("برای تحلیل دقیق");
        tipTitle.setTextSize(17.0f);
        tipTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tipTitle.setTextColor(0xFF9A3412);
        LinearLayout.LayoutParams tipTitleLp = new LinearLayout.LayoutParams(0, -2, 1f);
        tipHeader.addView(tipTitle, tipTitleLp);
        this.tipWarnBtn = new TextView(mainActivity);
        this.tipWarnBtn.setText("\u26A0");
        this.tipWarnBtn.setTextSize(22.0f);
        this.tipWarnBtn.setTextColor(0xFFEA580C);
        this.tipWarnBtn.setPadding(16, 4, 8, 4);
        this.tipWarnBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                expandTipBanner();
            }
        });
        tipHeader.addView(this.tipWarnBtn);
        this.tipCard.addView(tipHeader);
        this.tipBody = new TextView(mainActivity);
        this.tipBody.setText("بعد از آمدن درخواست در اسنپ، حتماً «مشاهده جزئیات» را بزنید تا کیلومتر واقعی خوانده شود.\nرایدو تا قبل از دیدن کیلومتر تحلیل نهایی نمی‌دهد.");
        this.tipBody.setTextSize(14.0f);
        this.tipBody.setTextColor(0xFF9A3412);
        this.tipBody.setPadding(0, 8, 0, 0);
        this.tipCard.addView(this.tipBody);
        this.content.addView(this.tipCard);
        scheduleTipCollapse();


        LinearLayout card = UiKit.INSTANCE.card(mainActivity);
        UiKit uiKit = UiKit.INSTANCE;
        String string = getString(R.string.services_status);
        Intrinsics.checkNotNullExpressionValue(string, "getString(...)");
        card.addView(UiKit.text$default(uiKit, mainActivity, string, 16.0f, 700, null, 16, null));
        // دو مربع: مسافت | میانگین سرعت
        LinearLayout gpsRow = new LinearLayout(mainActivity);
        gpsRow.setOrientation(LinearLayout.HORIZONTAL);
        gpsRow.setPadding(0, 8, 0, 8);

        LinearLayout boxDist = new LinearLayout(mainActivity);
        boxDist.setOrientation(LinearLayout.VERTICAL);
        boxDist.setPadding(20, 16, 20, 16);
        android.graphics.drawable.GradientDrawable bgD = new android.graphics.drawable.GradientDrawable();
        bgD.setCornerRadius(20f);
        bgD.setColor(0xFFF8FAFC);
        bgD.setStroke(2, 0xFFE2E8F0);
        boxDist.setBackground(bgD);
        TextView distCap = new TextView(mainActivity);
        distCap.setText("مسافت واقعی");
        distCap.setTextSize(11f);
        distCap.setTextColor(0xFF64748B);
        boxDist.addView(distCap);
        TextView textView = new TextView(mainActivity);
        textView.setText("-- km");
        textView.setTextSize(18f);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);
        textView.setTextColor(0xFF0F172A);
        this.distanceLabel = textView;
        boxDist.addView(textView);
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, -2, 1f);
        half.setMargins(0, 0, 8, 0);
        gpsRow.addView(boxDist, half);

        LinearLayout boxSpd = new LinearLayout(mainActivity);
        boxSpd.setOrientation(LinearLayout.VERTICAL);
        boxSpd.setPadding(20, 16, 20, 16);
        android.graphics.drawable.GradientDrawable bgS = new android.graphics.drawable.GradientDrawable();
        bgS.setCornerRadius(20f);
        bgS.setColor(0xFFF8FAFC);
        bgS.setStroke(2, 0xFFE2E8F0);
        boxSpd.setBackground(bgS);
        TextView spdCap = new TextView(mainActivity);
        spdCap.setText("میانگین سرعت");
        spdCap.setTextSize(11f);
        spdCap.setTextColor(0xFF64748B);
        boxSpd.addView(spdCap);
        TextView spdVal = new TextView(mainActivity);
        spdVal.setText("—");
        spdVal.setTextSize(18f);
        spdVal.setTypeface(null, android.graphics.Typeface.BOLD);
        spdVal.setTextColor(0xFF0F172A);
        this.speedLabel = spdVal;
        boxSpd.addView(spdVal);
        LinearLayout.LayoutParams half2 = new LinearLayout.LayoutParams(0, -2, 1f);
        half2.setMargins(8, 0, 0, 0);
        gpsRow.addView(boxSpd, half2);
        card.addView(gpsRow);

        Button button = new Button(mainActivity);
        button.setText("صفر کردن مسافت GPS");
        button.setOnClickListener(new View.OnClickListener() { // from class: ir.tripanalyzer.app.MainActivity$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.resetGpsDistance(MainActivity.this);
            }
        });
        card.addView(button);
        UiKit uiKit2 = UiKit.INSTANCE;
        String string2 = getString(R.string.status_accessibility);
        Intrinsics.checkNotNullExpressionValue(string2, "getString(...)");
        UiKit.StatusRow statusRow = uiKit2.statusRow(mainActivity, string2);
        UiKit uiKit3 = UiKit.INSTANCE;
        String string3 = getString(R.string.status_notification);
        Intrinsics.checkNotNullExpressionValue(string3, "getString(...)");
        UiKit.StatusRow statusRow2 = uiKit3.statusRow(mainActivity, string3);
        card.addView(statusRow.getContainer());
        card.addView(statusRow2.getContainer());
        statusRow.getContainer().setOnClickListener(new View.OnClickListener() { // from class: ir.tripanalyzer.app.MainActivity$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.openAccessibilitySettings(MainActivity.this);
            }
        });
        statusRow2.getContainer().setOnClickListener(new View.OnClickListener() { // from class: ir.tripanalyzer.app.MainActivity$$ExternalSyntheticLambda5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.openNotificationSettings(MainActivity.this);
            }
        });
        this.accPill = statusRow.getPill();
        this.notifPill = statusRow2.getPill();
        LinearLayout linearLayout = new LinearLayout(mainActivity);
        linearLayout.setOrientation(0);
        linearLayout.setPadding(0, UiKit.INSTANCE.dp(mainActivity, 10), 0, UiKit.INSTANCE.dp(mainActivity, 2));
        Switch r13 = new Switch(mainActivity);
        r13.setChecked(Store.INSTANCE.isAnalysisEnabled(mainActivity));
        r13.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: ir.tripanalyzer.app.MainActivity$$ExternalSyntheticLambda0
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                MainActivity.buildUi$lambda$1(MainActivity.this, compoundButton, z);
            }
        });
        UiKit uiKit4 = UiKit.INSTANCE;
        String string4 = getString(R.string.status_auto_analysis);
        Intrinsics.checkNotNullExpressionValue(string4, "getString(...)");
        linearLayout.addView(UiKit.text$default(uiKit4, mainActivity, string4, 14.0f, 400, null, 16, null), new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayout.addView(r13);
        card.addView(linearLayout);
        LinearLayout linearLayout4 = this.content;
        if (linearLayout4 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("content");
            linearLayout4 = null;
        }
        linearLayout4.addView(card);
        LinearLayout card2 = UiKit.INSTANCE.card(mainActivity);
        UiKit uiKit5 = UiKit.INSTANCE;
        String string5 = getString(R.string.perm_title);
        Intrinsics.checkNotNullExpressionValue(string5, "getString(...)");
        card2.addView(uiKit5.text(mainActivity, string5, 16.0f, 700, Integer.valueOf(UiKit.VERDICT_RED)));
        UiKit uiKit6 = UiKit.INSTANCE;
        String string6 = getString(R.string.perm_body);
        Intrinsics.checkNotNullExpressionValue(string6, "getString(...)");
        card2.addView(UiKit.text$default(uiKit6, mainActivity, string6, 13.0f, 400, null, 16, null));
        UiKit uiKit7 = UiKit.INSTANCE;
        String string7 = getString(R.string.enable_in_settings);
        Intrinsics.checkNotNullExpressionValue(string7, "getString(...)");
        Button button2 = uiKit7.button(mainActivity, string7, true);
        button2.setOnClickListener(new View.OnClickListener() { // from class: ir.tripanalyzer.app.MainActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.buildUi$lambda$3(MainActivity.this, view);
            }
        });
        card2.addView(button2);
        LinearLayout linearLayout6 = this.content;
        if (linearLayout6 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("content");
            linearLayout6 = null;
        }
        LinearLayout linearLayout7 = card2;
        linearLayout6.addView(linearLayout7);
        this.permCard = linearLayout7;
        LinearLayout card3 = UiKit.INSTANCE.card(mainActivity);
        UiKit uiKit8 = UiKit.INSTANCE;
        String string8 = getString(R.string.last_result);
        Intrinsics.checkNotNullExpressionValue(string8, "getString(...)");
        card3.addView(UiKit.text$default(uiKit8, mainActivity, string8, 16.0f, 700, null, 16, null));
        LinearLayout linearLayout8 = new LinearLayout(mainActivity);
        linearLayout8.setOrientation(1);
        card3.addView(linearLayout8);
        UiKit uiKit9 = UiKit.INSTANCE;
        String string9 = getString(R.string.view_details);
        Intrinsics.checkNotNullExpressionValue(string9, "getString(...)");
        Button button3 = uiKit9.button(mainActivity, string9, false);
        button3.setOnClickListener(new View.OnClickListener() { // from class: ir.tripanalyzer.app.MainActivity$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.buildUi$lambda$4(MainActivity.this, view);
            }
        });
        card3.addView(button3);
        LinearLayout linearLayout9 = this.content;
        if (linearLayout9 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("content");
            linearLayout9 = null;
        }
        linearLayout9.addView(card3);
        this.lastBody = linearLayout8;

        try {
            final MainActivity act = mainActivity;
            RaidoDataStore.DashboardHourly snap = RaidoDataStore.INSTANCE.snapshotDashboard(act);
            android.widget.LinearLayout avgCard = new android.widget.LinearLayout(act);
            avgCard.setOrientation(android.widget.LinearLayout.VERTICAL);
            avgCard.setPadding(28, 28, 28, 28);
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setCornerRadius(28f);
            bg.setColor(0xFFFFFFFF);
            bg.setStroke(2, 0xFFE2E8F0);
            avgCard.setBackground(bg);
            android.widget.TextView avgTitle = new android.widget.TextView(act);
            avgTitle.setText("میانگین ساعتی درخواست‌ها");
            avgTitle.setTextSize(16f);
            avgTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            avgTitle.setTextColor(0xFF0F172A);
            avgCard.addView(avgTitle);
            android.widget.TextView avgSub = new android.widget.TextView(act);
            avgSub.setTextSize(13f);
            avgSub.setTextColor(0xFF64748B);
            avgSub.setPadding(0, 16, 0, 0);
            avgCard.addView(avgSub);
            this.avgHourlyBody = avgSub;
            fillAvgHourlyBody(act, avgSub);
            avgCard.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(android.view.View v) {
                    android.content.Intent i = new android.content.Intent(act, ReportsActivity.class);
                    i.putExtra(ReportsActivity.EXTRA_TAB, 2);
                    act.startActivity(i);
                }
            });
            android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(-1, -2);
            lp.topMargin = 24;
            linearLayout9.addView(avgCard, lp);
        } catch (Throwable ignored) {}

        // —— درآمد قبول‌شده + بنزین/استهلاک GPS (فاز بعداً — الان فعال)
        try {
            final MainActivity act2 = mainActivity;
            LinearLayout accCard = new LinearLayout(act2);
            accCard.setOrientation(LinearLayout.VERTICAL);
            accCard.setPadding(28, 28, 28, 28);
            android.graphics.drawable.GradientDrawable bgA = new android.graphics.drawable.GradientDrawable();
            bgA.setCornerRadius(28f);
            bgA.setColor(0xFFFFFFFF);
            bgA.setStroke(2, 0xFFA7F3D0);
            accCard.setBackground(bgA);
            TextView accTitle = new TextView(act2);
            accTitle.setText("درآمد ساعتی قبول‌شده‌ها");
            accTitle.setTextSize(14f);
            accTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            accTitle.setTextColor(0xFF64748B);
            accCard.addView(accTitle);
            TextView accVal = new TextView(act2);
            accVal.setTextSize(20f);
            accVal.setTypeface(null, android.graphics.Typeface.BOLD);
            accVal.setTextColor(0xFF047857);
            accVal.setPadding(0, 8, 0, 0);
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
            LinearLayout.LayoutParams lpA = new LinearLayout.LayoutParams(-1, -2);
            lpA.topMargin = 24;
            linearLayout9.addView(accCard, lpA);

            LinearLayout costRow = new LinearLayout(act2);
            costRow.setOrientation(LinearLayout.HORIZONTAL);
            costRow.setPadding(0, 8, 0, 0);
            // fuel box
            LinearLayout fuelBox = new LinearLayout(act2);
            fuelBox.setOrientation(LinearLayout.VERTICAL);
            fuelBox.setPadding(20, 16, 20, 16);
            android.graphics.drawable.GradientDrawable bgF = new android.graphics.drawable.GradientDrawable();
            bgF.setCornerRadius(20f); bgF.setColor(0xFFF8FAFC); bgF.setStroke(2, 0xFFE2E8F0);
            fuelBox.setBackground(bgF);
            TextView fCap = new TextView(act2); fCap.setText("بنزین واقعی GPS"); fCap.setTextSize(11f); fCap.setTextColor(0xFF64748B);
            fuelBox.addView(fCap);
            TextView fVal = new TextView(act2); fVal.setText("—"); fVal.setTextSize(16f); fVal.setTypeface(null, android.graphics.Typeface.BOLD); fVal.setTextColor(0xFF0F172A);
            this.gpsFuelLabel = fVal; fuelBox.addView(fVal);
            LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(0, -2, 1f); hp.setMargins(0, 0, 8, 0);
            costRow.addView(fuelBox, hp);
            // dep box
            LinearLayout depBox = new LinearLayout(act2);
            depBox.setOrientation(LinearLayout.VERTICAL);
            depBox.setPadding(20, 16, 20, 16);
            android.graphics.drawable.GradientDrawable bgDep = new android.graphics.drawable.GradientDrawable();
            bgDep.setCornerRadius(20f); bgDep.setColor(0xFFF8FAFC); bgDep.setStroke(2, 0xFFE2E8F0);
            depBox.setBackground(bgDep);
            TextView dCap = new TextView(act2); dCap.setText("استهلاک واقعی GPS"); dCap.setTextSize(11f); dCap.setTextColor(0xFF64748B);
            depBox.addView(dCap);
            TextView dVal = new TextView(act2); dVal.setText("—"); dVal.setTextSize(16f); dVal.setTypeface(null, android.graphics.Typeface.BOLD); dVal.setTextColor(0xFF0F172A);
            this.gpsDepLabel = dVal; depBox.addView(dVal);
            LinearLayout.LayoutParams hp2 = new LinearLayout.LayoutParams(0, -2, 1f); hp2.setMargins(8, 0, 0, 0);
            costRow.addView(depBox, hp2);
            LinearLayout.LayoutParams lpC = new LinearLayout.LayoutParams(-1, -2);
            lpC.topMargin = 12;
            linearLayout9.addView(costRow, lpC);

            // روند ۷ روز
            LinearLayout trendCard = new LinearLayout(act2);
            trendCard.setOrientation(LinearLayout.VERTICAL);
            trendCard.setPadding(28, 24, 28, 24);
            android.graphics.drawable.GradientDrawable bgT = new android.graphics.drawable.GradientDrawable();
            bgT.setCornerRadius(28f); bgT.setColor(0xFFFFFFFF); bgT.setStroke(2, 0xFFE2E8F0);
            trendCard.setBackground(bgT);
            TextView tTitle = new TextView(act2);
            tTitle.setText("روند میانگین ساعتی · ۷ روز");
            tTitle.setTextSize(13f); tTitle.setTypeface(null, android.graphics.Typeface.BOLD); tTitle.setTextColor(0xFF64748B);
            trendCard.addView(tTitle);
            LinearLayout trendHost = new LinearLayout(act2);
            trendHost.setOrientation(LinearLayout.VERTICAL);
            trendHost.setPadding(0, 12, 0, 0);
            this.chartTrendHost = trendHost;
            trendCard.addView(trendHost);
            LinearLayout.LayoutParams lpT = new LinearLayout.LayoutParams(-1, -2);
            lpT.topMargin = 24;
            linearLayout9.addView(trendCard, lpT);

            // بنچمارک
            LinearLayout benchCard = new LinearLayout(act2);
            benchCard.setOrientation(LinearLayout.VERTICAL);
            benchCard.setPadding(28, 24, 28, 24);
            android.graphics.drawable.GradientDrawable bgB = new android.graphics.drawable.GradientDrawable();
            bgB.setCornerRadius(28f); bgB.setColor(0xFFFFFFFF); bgB.setStroke(2, 0xFFE2E8F0);
            benchCard.setBackground(bgB);
            TextView bTitle = new TextView(act2);
            bTitle.setText("این ساعت در یک نگاه");
            bTitle.setTextSize(13f); bTitle.setTypeface(null, android.graphics.Typeface.BOLD); bTitle.setTextColor(0xFF64748B);
            benchCard.addView(bTitle);
            LinearLayout benchHost = new LinearLayout(act2);
            benchHost.setOrientation(LinearLayout.VERTICAL);
            benchHost.setPadding(0, 8, 0, 0);
            this.benchmarkHost = benchHost;
            benchCard.addView(benchHost);
            TextView bHint = new TextView(act2);
            bHint.setText("اعداد به هزار تومان · فقط نمایش");
            bHint.setTextSize(11f); bHint.setTextColor(0xFF94A3B8); bHint.setPadding(0, 8, 0, 0);
            benchCard.addView(bHint);
            LinearLayout.LayoutParams lpB = new LinearLayout.LayoutParams(-1, -2);
            lpB.topMargin = 16;
            linearLayout9.addView(benchCard, lpB);
        } catch (Throwable ignored) {}
        // منوی صفحات از طریق آیکون همبرگری بالای داشبورد در دسترس است
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
                linearLayout2.addView(UiKit.text$default(uiKit, mainActivity, string, 14.0f, 700, null, 16, null));
            }
            LinearLayout linearLayout3 = this.lastBody;
            if (linearLayout3 != null) {
                UiKit uiKit2 = UiKit.INSTANCE;
                String string2 = getString(R.string.empty_last_result_body);
                Intrinsics.checkNotNullExpressionValue(string2, "getString(...)");
                linearLayout3.addView(UiKit.text$default(uiKit2, mainActivity, string2, 12.0f, 400, null, 16, null));
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
            TextView text = uiKit3.text(mainActivity, res.str(R.string.hourly_income_format, objArr), 24.0f, 900, Integer.valueOf(Ui.INSTANCE.verdictColor(component1.getVerdict())));
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
                linearLayout6.addView(UiKit.text$default(uiKit4, mainActivity, string3, 13.0f, 400, null, 16, null));
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
            if (d != null) d.setText("GPS خاموش");
            if (s != null) s.setText("GPS خاموش");
            if (gpsFuelLabel != null) gpsFuelLabel.setText("GPS خاموش");
            if (gpsDepLabel != null) gpsDepLabel.setText("GPS خاموش");
        } else {
            if (d != null) d.setText((((long) (km * 10.0d)) / 10.0d) + " km");
            if (s != null) {
                double spd = GpsTracker.INSTANCE.getAvgSpeedKmh(this);
                if (spd < 0) s.setText("—");
                else s.setText(String.format(java.util.Locale.US, "%.1f km/h", spd));
            }
            // بنزین و استهلاک واقعی از مسافت GPS (نمایش؛ نه تحلیل)
            try {
                EngineSettings es = Store.INSTANCE.settings(this);
                double fuel = km * (es.getFuelPer100() / 100.0) * es.getFuelPrice();
                double dep = km * es.getDepPerKm();
                if (gpsFuelLabel != null) gpsFuelLabel.setText(Ui.INSTANCE.money(fuel) + " ت");
                if (gpsDepLabel != null) gpsDepLabel.setText(Ui.INSTANCE.money(dep) + " ت");
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
                    acceptedIncomeLabel.setText(Ui.INSTANCE.money(a.avg) + " تومان\n" + a.count + " سفر قبول‌شده");
                } else {
                    acceptedIncomeLabel.setText("هنوز سفری با «رفتم» ثبت نشده");
                }
            }
        } catch (Throwable ignored) {}
        // نمودار روند ۷روز
        try {
            if (chartTrendHost != null) {
                chartTrendHost.removeAllViews();
                float[] av = RaidoDataStore.INSTANCE.last7DayHourlyAvgs(this);
                chartTrendHost.addView(ChartKit.areaSpark(this, av, 88));
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

    private static void fillAvgHourlyBody(MainActivity act, TextView avgSub) {
        if (act == null || avgSub == null) return;
        try {
            RaidoDataStore.DashboardHourly snap = RaidoDataStore.INSTANCE.snapshotDashboard(act);
            int h2 = snap.currentHour == 23 ? 24 : snap.currentHour + 1;
            String hRange = String.format(java.util.Locale.US, "%02d–%02d", snap.currentHour, h2);
            String hourTxt = snap.hourCount > 0
                    ? ("الان (" + hRange + "): " + Ui.INSTANCE.money(snap.hourAvg) + " تومان · " + snap.hourCount + " تحلیل")
                    : ("الان (" + hRange + "): هنوز تحلیلی نیست");
            String dayTxt = snap.dayCount > 0
                    ? ("امروز: " + Ui.INSTANCE.money(snap.dayAvg) + " تومان · " + snap.dayCount + " تحلیل")
                    : "امروز: هنوز تحلیلی ثبت نشده";
            avgSub.setText(hourTxt + "\n" + dayTxt + "\nبرای جزئیات روز و ساعت‌ها اینجا بزنید ←");
        } catch (Throwable ignored) {
            avgSub.setText("برای جزئیات روز و ساعت‌ها اینجا بزنید ←");
        }
    }

    private final GradientDrawable pillBg(int color) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(UiKit.INSTANCE.dp(this, 12));
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
            if (tipCard != null) tipCard.setVisibility(View.VISIBLE);
            tipCollapseRunnable = new Runnable() {
                @Override public void run() {
                    collapseTipBanner();
                }
            };
            tipHandler.postDelayed(tipCollapseRunnable, 2L * 60L * 1000L);
        } catch (Throwable ignored) {}
    }

    private void collapseTipBanner() {
        try {
            tipExpanded = false;
            if (tipBody != null) tipBody.setVisibility(View.GONE);
        } catch (Throwable ignored) {}
    }

    private void expandTipBanner() {
        try {
            tipExpanded = true;
            if (tipBody != null) tipBody.setVisibility(View.VISIBLE);
            if (tipCard != null) tipCard.setVisibility(View.VISIBLE);
            scheduleTipCollapse();
        } catch (Throwable ignored) {}
    }

}
