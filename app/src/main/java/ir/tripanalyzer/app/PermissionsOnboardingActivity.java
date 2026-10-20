package ir.tripanalyzer.app;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * ویزارد دسترسی‌ها — ظاهر رایدو + مسیر فارسی و انگلیسی درشت.
 * مخصوص اندروید ۱۳+ و سامسونگ (Installed apps / Manage if unused).
 */
public final class PermissionsOnboardingActivity extends BaseActivity {

    private static final int TEAL = 0xFF0B3D3A;
    private static final int TEAL2 = 0xFF0F766E;
    private static final int TEXT_DARK = 0xFF0F172A;
    private static final int TEXT_MUTED = 0xFF64748B;
    private static final int GREEN = 0xFF059669;
    private static final int RED = 0xFFDC2626;
    private static final int AMBER = 0xFFD97706;
    private static final int BLUE = 0xFF2563EB;
    private static final int CARD_BORDER = 0xFFE2E8F0;
    private static final int REQ_POST = 9201;

    private LinearLayout contentHost;
    private LinearLayout progressHost;

    @Override
    protected boolean usePagesBackground() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(0xFFF0FDFA);
        shell.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        // Header
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.VERTICAL);
        top.setPadding(dp(18), dp(22), dp(18), dp(20));
        GradientDrawable tg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF0B3D3A, 0xFF134E4A, 0xFF0F766E});
        top.setBackground(tg);

        TextView brand = new TextView(this);
        brand.setText("RAIDO · راه‌اندازی");
        brand.setTextSize(11f);
        brand.setTextColor(0xCCFFFFFF);
        top.addView(brand);

        TextView h1 = new TextView(this);
        h1.setText("دسترسی‌ها را کامل کن");
        h1.setTextSize(20f);
        h1.setTypeface(Typeface.DEFAULT_BOLD);
        h1.setTextColor(Color.WHITE);
        h1.setPadding(0, dp(6), 0, 0);
        top.addView(h1);

        TextView sub = new TextView(this);
        sub.setText("فقط یک مورد اجباری است؛ بقیه چند ثانیه‌ای هستند");
        sub.setTextSize(13f);
        sub.setTextColor(0xE6FFFFFF);
        sub.setPadding(0, dp(6), 0, 0);
        top.addView(sub);

        progressHost = new LinearLayout(this);
        top.addView(progressHost);
        shell.addView(top);

        ScrollView scroll = new ScrollView(this);
        contentHost = new LinearLayout(this);
        contentHost.setOrientation(LinearLayout.VERTICAL);
        contentHost.setPadding(dp(14), dp(14), dp(14), dp(36));
        scroll.addView(contentHost);
        shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(shell);
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    @Override
    public void onBackPressed() {
        if (Status.INSTANCE.accessibilityEnabled(this)) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "برای استفاده، دسترس‌پذیری (Accessibility) باید روشن باشد.", Toast.LENGTH_LONG).show();
        }
    }

    private void render() {
        contentHost.removeAllViews();
        boolean is13 = Build.VERSION.SDK_INT >= 33;
        boolean acc = Status.INSTANCE.accessibilityEnabled(this);
        boolean notif = Status.INSTANCE.listenerEnabled(this);
        boolean postOk = okPostNotifications();
        boolean batt = batteryOk();
        String brand = brandFamily();
        boolean triedAcc = false;
        try { triedAcc = Store.INSTANCE.prefs(this).getBoolean("wiz_tried_acc", false); } catch (Throwable ignored) {}
        // کلید دسترس‌پذیری در اندروید ۱۳+ برای APK نصب‌شده بیرون از استور خاکستری می‌شود
        boolean needRestricted = is13 && !acc && (!fromKnownStore() || triedAcc);

        fillProgress(acc, batt, needRestricted);

        contentHost.addView(pill("وضعیت · " + brandLabel(brand)));
        contentHost.addView(statusCard(acc, batt, notif, postOk));

        contentHost.addView(pill("مراحل اصلی"));

        int step = 1;
        if (needRestricted) {
            contentHost.addView(stepCard(
                    String.valueOf(step++),
                    "اگر کلید دسترس‌پذیری خاکستری بود",
                    "اندروید برای برنامه‌هایی که از استور نصب نشده‌اند، این کلید را قفل می‌کند. "
                            + "اگر ⋮ را نمی‌بینی، یک‌بار کلید رایدو را در دسترس‌پذیری بزن تا پیام «Restricted setting» بیاید، بعد برگرد.",
                    faPath("اطلاعات اپ رایدو ← سه‌نقطه ⋮ بالا سمت راست ← Allow restricted settings ← تأیید"),
                    enPath("App info > ⋮ > Allow restricted settings"),
                    null,
                    false,
                    true,
                    "باز کردن اطلاعات اپ",
                    v -> openAppInfo()
            ));
        }

        contentHost.addView(stepCard(
                String.valueOf(step++),
                "دسترس‌پذیری (اجباری)",
                brand.equals("samsung")
                        ? "در سامسونگ رایدو معمولاً در صفحه اول Accessibility نیست — از Installed apps برو."
                        : "سرویس خواندن صفحه اسنپ را روشن کن. کارت تحلیل هم از همین مجوز نمایش داده می‌شود.",
                faPath(brand.equals("samsung")
                        ? "تنظیمات ← دسترس‌پذیری ← برنامه‌های نصب‌شده ← رایدو ← روشن"
                        : "تنظیمات ← دسترس‌پذیری ← رایدو ← روشن"),
                enPath(brand.equals("samsung")
                        ? "Settings > Accessibility > Installed apps > RAIDO > On"
                        : "Settings > Accessibility > RAIDO > On"),
                null,
                acc,
                !acc,
                "باز کردن Accessibility",
                v -> openAccessibility()
        ));

        contentHost.addView(stepCard(
                String.valueOf(step++),
                "بدون محدودیت باتری",
                "تا سیستم وسط شیفت رایدو را نبندد. فقط یک پنجره‌ی تأیید است.",
                faPath("پنجره‌ی باز‌شده ← مجاز / Allow"),
                enPath("Allow"),
                null,
                batt,
                !batt,
                "اجازه‌ی باتری",
                v -> requestBattery()
        ));

        contentHost.addView(pill("اختیاری — می‌توانی بعداً انجام دهی"));

        contentHost.addView(stepCard(
                String.valueOf(step++),
                "خواندن اعلان اسنپ",
                "برای تشخیص سریع‌تر درخواست سفر. بدون آن هم برنامه کار می‌کند.",
                faPath("تنظیمات ← دسترسی اعلان / Notification access ← رایدو ← روشن"),
                enPath("Settings > Notification access > RAIDO > On"),
                null,
                notif,
                !notif,
                "باز کردن Notification access",
                v -> openNotificationListener()
        ));

        if (is13) {
            contentHost.addView(stepCard(
                    String.valueOf(step++),
                    "اعلان‌های خود رایدو",
                    "اگر Blocked باشد، اعلان خلاصه‌ی تحلیل نمی‌آید.",
                    faPath("پنجره‌ی باز‌شده ← اجازه"),
                    enPath("Allow"),
                    null,
                    postOk,
                    !postOk,
                    "اجازه Notifications",
                    v -> requestPostNotifications()
            ));

            contentHost.addView(stepCard(
                    String.valueOf(step++),
                    "Manage app if unused را خاموش کن",
                    "اگر روشن بماند، سیستم بعد از چند روز مجوزها را برمی‌دارد (بیشتر در سامسونگ).",
                    faPath("اطلاعات اپ رایدو ← مدیریت برنامه در صورت عدم استفاده ← خاموش"),
                    enPath("App info > Manage app if unused > OFF"),
                    enPath("Remove permissions if app is unused → OFF"),
                    false,
                    true,
                    "باز کردن اطلاعات اپ",
                    v -> openAppInfo()
            ));
        }

        // Finish
        if (acc) {
            Button done = primaryBtn("ورود به داشبورد");
            done.setOnClickListener(v -> {
                try {
                    Store.INSTANCE.setPermissionsWizardDone(this, true);
                } catch (Throwable ignored) {}
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.topMargin = dp(8);
            done.setLayoutParams(lp);
            contentHost.addView(done);
        } else {
            TextView need = new TextView(this);
            need.setText("تا Accessibility روشن نشود نمی‌توانی وارد داشبورد شوی.");
            need.setTextSize(13f);
            need.setTextColor(RED);
            need.setPadding(0, dp(12), 0, 0);
            contentHost.addView(need);
        }

        Button help = primaryBtn("گیر کردی؟ پشتیبانی تلگرام");
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(-1, -2);
        hlp.topMargin = dp(14);
        help.setLayoutParams(hlp);
        help.setOnClickListener(v -> {
            try { startActivity(new Intent(this, SupportActivity.class)); } catch (Throwable ignored) {}
        });
        contentHost.addView(help);

        TextView foot = new TextView(this);
        foot.setText("مسیر انگلیسی را با حروف بزرگ دنبال کن. با برگشتن به این صفحه، مرحله‌های انجام‌شده خودکار تیک می‌خورند.");
        foot.setTextSize(12f);
        foot.setTextColor(TEXT_MUTED);
        foot.setPadding(0, dp(16), 0, 0);
        contentHost.addView(foot);
    }

    private void fillProgress(boolean acc, boolean batt, boolean needRestricted) {
        progressHost.removeAllViews();
        progressHost.setOrientation(LinearLayout.HORIZONTAL);
        progressHost.setPadding(0, dp(14), 0, 0);
        boolean[] done = needRestricted
                ? new boolean[]{acc, acc, batt}
                : new boolean[]{acc, batt};
        for (int i = 0; i < done.length; i++) {
            View bar = new View(this);
            GradientDrawable g = new GradientDrawable();
            g.setCornerRadius(dp(99));
            g.setColor(done[i] ? 0xFF5EEAD4 : 0x33FFFFFF);
            bar.setBackground(g);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(5), 1f);
            if (i < done.length - 1) lp.setMargins(0, 0, dp(6), 0);
            progressHost.addView(bar, lp);
        }
    }

    // ——— UI pieces ———

    private View statusCard(boolean acc, boolean batt, boolean notif, boolean postOk) {
        LinearLayout card = whiteCard();
        card.addView(statusRow("Accessibility (اجباری)", acc));
        card.addView(statusRow("Battery unrestricted", batt));
        card.addView(statusRow("Notification access (اختیاری)", notif));
        if (Build.VERSION.SDK_INT >= 33) {
            card.addView(statusRow("Notifications (app)", postOk));
        }
        Button tut = new Button(this);
        tut.setText("ویدیو آموزشی فعال کردن دسترسی‌ها");
        tut.setAllCaps(false);
        tut.setTextSize(14f);
        tut.setTypeface(Typeface.DEFAULT_BOLD);
        tut.setTextColor(0xFF1D4ED8);
        tut.setGravity(Gravity.CENTER);
        GradientDrawable tg = new GradientDrawable();
        tg.setColor(0xFFEFF6FF);
        tg.setCornerRadius(dp(14));
        tg.setStroke(dp(1), 0xFFBFDBFE);
        tut.setBackground(tg);
        tut.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-1, dp(48));
        tlp.topMargin = dp(10);
        tut.setLayoutParams(tlp);
        tut.setOnClickListener(v -> openTutorial(tut));
        card.addView(tut);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(12);
        card.setLayoutParams(lp);
        return card;
    }

    /** لینک ویدیو از سرور (قابل تغییر از پنل) گرفته می‌شود؛ بدون نیاز به آپدیت اپ. */
    private void openTutorial(final Button b) {
        b.setEnabled(false);
        new Thread(() -> {
            String url = "";
            try {
                org.json.JSONObject r = RaidoCloud.post("app-config", new org.json.JSONObject(), null, 5000, 6000);
                url = r.optString("tutorial_url", "");
            } catch (Throwable ignored) {}
            final String u = url;
            runOnUiThread(() -> {
                b.setEnabled(true);
                if (u.startsWith("https://")) {
                    try {
                        startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(u)));
                    } catch (Throwable e) {
                        android.widget.Toast.makeText(this, "لینک باز نشد", android.widget.Toast.LENGTH_LONG).show();
                    }
                } else {
                    android.widget.Toast.makeText(this, "ویدیوی آموزشی هنوز آماده نیست یا اینترنت قطع است", android.widget.Toast.LENGTH_LONG).show();
                }
            });
        }, "raido-tut").start();
    }

    private View statusRow(String name, boolean on) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));
        TextView n = new TextView(this);
        n.setText(name);
        n.setTextSize(13.5f);
        n.setTypeface(Typeface.DEFAULT_BOLD);
        n.setTextColor(TEXT_DARK);
        n.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        row.addView(n);
        TextView b = new TextView(this);
        b.setText(on ? "On ✓" : "Off");
        b.setTextSize(12f);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(on ? GREEN : RED);
        b.setPadding(dp(10), dp(4), dp(10), dp(4));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(99));
        bg.setColor(on ? 0xFFECFDF5 : 0xFFFEF2F2);
        b.setBackground(bg);
        row.addView(b);
        return row;
    }

    private View stepCard(String num, String title, String desc,
                          String fa, String en1, String en2,
                          boolean done, boolean showAction,
                          String actionLabel, View.OnClickListener action) {
        return stepCard(num, title, desc, fa, en1, en2, null, done, showAction, actionLabel, action);
    }

    private View stepCard(String num, String title, String desc,
                          String fa, String en1, String en2, String en3,
                          boolean done, boolean showAction,
                          String actionLabel, View.OnClickListener action) {
        LinearLayout card = whiteCard();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(12);
        card.setLayoutParams(lp);

        TextView badge = new TextView(this);
        badge.setText(done ? "✓ انجام شد" : "مرحله " + num);
        badge.setTextSize(11f);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setTextColor(done ? GREEN : BLUE);
        badge.setPadding(dp(10), dp(4), dp(10), dp(4));
        GradientDrawable bb = new GradientDrawable();
        bb.setCornerRadius(dp(99));
        bb.setColor(done ? 0xFFECFDF5 : 0xFFEFF6FF);
        badge.setBackground(bb);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-2, -2);
        card.addView(badge, blp);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(16f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(TEXT_DARK);
        t.setPadding(0, dp(8), 0, 0);
        card.addView(t);

        if (desc != null && desc.length() > 0) {
            TextView d = new TextView(this);
            d.setText(desc);
            d.setTextSize(13f);
            d.setTextColor(TEXT_MUTED);
            d.setPadding(0, dp(4), 0, 0);
            card.addView(d);
        }

        // Path box — dark, LTR English large
        LinearLayout path = new LinearLayout(this);
        path.setOrientation(LinearLayout.VERTICAL);
        path.setPadding(dp(12), dp(12), dp(12), dp(12));
        GradientDrawable pg = new GradientDrawable();
        pg.setColor(0xFF0F172A);
        pg.setCornerRadius(dp(12));
        path.setBackground(pg);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(-1, -2);
        plp.topMargin = dp(10);
        path.setLayoutParams(plp);

        if (fa != null) {
            TextView faTv = new TextView(this);
            faTv.setText(fa);
            faTv.setTextSize(12.5f);
            faTv.setTextColor(0xFFCBD5E1);
            faTv.setLineSpacing(0, 1.35f);
            path.addView(faTv);
        }
        if (en1 != null) {
            TextView enTv = new TextView(this);
            enTv.setText(en1);
            enTv.setTextSize(15f);
            enTv.setTypeface(Typeface.DEFAULT_BOLD);
            enTv.setTextColor(0xFF5EEAD4);
            enTv.setTextDirection(View.TEXT_DIRECTION_LTR);
            enTv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            enTv.setGravity(Gravity.START);
            enTv.setPadding(0, fa != null ? dp(8) : 0, 0, 0);
            enTv.setLineSpacing(0, 1.35f);
            path.addView(enTv);
        }
        if (en2 != null) {
            TextView en2Tv = new TextView(this);
            en2Tv.setText(en2);
            en2Tv.setTextSize(13.5f);
            en2Tv.setTypeface(Typeface.DEFAULT_BOLD);
            en2Tv.setTextColor(0xFFFCD34D);
            en2Tv.setTextDirection(View.TEXT_DIRECTION_LTR);
            en2Tv.setGravity(Gravity.START);
            en2Tv.setPadding(0, dp(6), 0, 0);
            path.addView(en2Tv);
        }
        if (en3 != null) {
            TextView en3Tv = new TextView(this);
            en3Tv.setText(en3);
            en3Tv.setTextSize(13f);
            en3Tv.setTypeface(Typeface.DEFAULT_BOLD);
            en3Tv.setTextColor(0xFF93C5FD);
            en3Tv.setTextDirection(View.TEXT_DIRECTION_LTR);
            en3Tv.setGravity(Gravity.START);
            en3Tv.setPadding(0, dp(6), 0, 0);
            path.addView(en3Tv);
        }
        card.addView(path);

        if (done && !showAction) {
            TextView ok = new TextView(this);
            ok.setText("این مورد فعال است.");
            ok.setTextSize(13f);
            ok.setTextColor(GREEN);
            ok.setPadding(0, dp(10), 0, 0);
            card.addView(ok);
        } else if (showAction) {
            Button btn = primaryBtn(actionLabel);
            LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(-1, -2);
            alp.topMargin = dp(12);
            btn.setLayoutParams(alp);
            btn.setOnClickListener(action);
            card.addView(btn);
            if (done) {
                TextView ok = new TextView(this);
                ok.setText("وضعیت فعلی: On — اگر مطمئن نیستی دوباره چک کن.");
                ok.setTextSize(12f);
                ok.setTextColor(GREEN);
                ok.setPadding(0, dp(8), 0, 0);
                card.addView(ok);
            }
        }
        return card;
    }

    private String faPath(String s) { return s; }
    private String enPath(String s) { return s; }

    private TextView pill(String t) {
        TextView v = new TextView(this);
        v.setText(t);
        v.setTextSize(11f);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setTextColor(TEAL2);
        v.setPadding(dp(12), dp(5), dp(12), dp(5));
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(99));
        g.setStroke(dp(1), 0xFF99F6E4);
        v.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.bottomMargin = dp(10);
        v.setLayoutParams(lp);
        return v;
    }

    private LinearLayout whiteCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(16));
        g.setStroke(dp(1), CARD_BORDER);
        card.setBackground(g);
        return card;
    }

    private Button primaryBtn(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setAllCaps(false);
        btn.setTextSize(14.5f);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setTextColor(Color.WHITE);
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF0F766E, 0xFF134E4A});
        bg.setCornerRadius(dp(14));
        btn.setBackground(bg);
        btn.setPadding(dp(12), dp(14), dp(12), dp(14));
        return btn;
    }

    // ——— open settings ———

    private void openAppInfo() {
        try {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            i.setData(Uri.parse("package:" + getPackageName()));
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "اطلاعات اپ باز نشد.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAccessibility() {
        try { Store.INSTANCE.prefs(this).edit().putBoolean("wiz_tried_acc", true).apply(); } catch (Throwable ignored) {}
        try {
            Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            try {
                // در AOSP/پیکسل سرویس را هایلایت می‌کند؛ روی سامسونگ نادیده گرفته می‌شود
                String comp = new ComponentName(this, SnappScreenWatcherService.class).flattenToString();
                Bundle args = new Bundle();
                args.putString(":settings:fragment_args_key", comp);
                i.putExtra(":settings:fragment_args_key", comp);
                i.putExtra(":settings:show_fragment_args", args);
            } catch (Throwable ignored) {}
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Accessibility باز نشد.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openNotificationListener() {
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                // مستقیم صفحه‌ی خود رایدو
                Intent i = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS);
                i.putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                        new ComponentName(this, SnappNotificationListenerService.class).flattenToString());
                startActivity(i);
                return;
            }
        } catch (Exception ignored) {}
        try {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        } catch (Exception e2) {
            openAppInfo();
        }
    }

    private boolean batteryOk() {
        try {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            return pm != null && pm.isIgnoringBatteryOptimizations(getPackageName());
        } catch (Throwable t) {
            return false;
        }
    }

    private void requestBattery() {
        try {
            startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:" + getPackageName())));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            } catch (Exception e2) {
                openAppInfo();
            }
        }
    }

    /** نصب از بازار/مایکت/پلی‌استور → معمولاً Restricted settings ندارد. */
    private boolean fromKnownStore() {
        try {
            String installer;
            if (Build.VERSION.SDK_INT >= 30) {
                installer = getPackageManager().getInstallSourceInfo(getPackageName()).getInstallingPackageName();
            } else {
                installer = getPackageManager().getInstallerPackageName(getPackageName());
            }
            if (installer == null) return false;
            return installer.equals("com.farsitel.bazaar")
                    || installer.equals("ir.mservices.market")
                    || installer.equals("com.android.vending")
                    || installer.equals("com.sec.android.app.samsungapps");
        } catch (Throwable t) {
            return false;
        }
    }

    private void requestPostNotifications() {
        if (Build.VERSION.SDK_INT < 33) return;
        if (okPostNotifications()) {
            Toast.makeText(this, "اعلان از قبل مجاز است.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST);
        } catch (Exception e) {
            openAppInfo();
        }
    }

    private boolean okPostNotifications() {
        return Build.VERSION.SDK_INT < 33
                || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private String brandFamily() {
        String m = (Build.MANUFACTURER == null ? "" : Build.MANUFACTURER).toLowerCase();
        String b = (Build.BRAND == null ? "" : Build.BRAND).toLowerCase();
        if (m.contains("xiaomi") || b.contains("xiaomi") || m.contains("redmi") || b.contains("redmi")
                || m.contains("poco") || b.contains("poco")) return "xiaomi";
        if (m.contains("samsung") || b.contains("samsung")) return "samsung";
        if (m.contains("huawei") || b.contains("huawei") || m.contains("honor") || b.contains("honor")) return "huawei";
        return "generic";
    }

    private String brandLabel(String f) {
        switch (f) {
            case "xiaomi": return "شیائومی / ردمی";
            case "samsung": return "سامسونگ / One UI";
            case "huawei": return "هواوی / آنر";
            default: return "اندروید";
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        render();
    }
}
