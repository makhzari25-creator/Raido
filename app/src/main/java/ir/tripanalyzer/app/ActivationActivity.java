package ir.tripanalyzer.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * صفحه کد فعال‌سازی — ظاهر ویزارد تأییدشده.
 * منطق activateWithCode بدون تغییر.
 */
public final class ActivationActivity extends BaseActivity {

    /** اگر true باشد حتی با اشتراک فعال هم فرم کد نشان داده می‌شود (تمدید/ارتقا). */
    public static final String EXTRA_RENEW = "renew_mode";

    private static final int TEAL2 = 0xFF0F766E;
    private static final int MUTED = 0xFF64748B;
    private static final int INK = 0xFF0F172A;
    private static final int BLUE = 0xFF2563EB;

    private EditText codeInput;
    private boolean renewMode;

    @Override
    protected boolean usePagesBackground() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (!LicenseStore.INSTANCE.isLoggedIn(this)) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
            renewMode = getIntent() != null && getIntent().getBooleanExtra(EXTRA_RENEW, false);
            // فقط وقتی تمدید نیست و اشتراک فعال است → داشبورد
            if (!renewMode && LicenseStore.INSTANCE.canUseAnalysis(this)) {
                goMain();
                return;
            }
            buildUi();
        } catch (Throwable t) {
            try { buildUiMinimal(); } catch (Throwable ignored) {}
        }
    }

    private void buildUiMinimal() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(40), dp(20), dp(20));
        root.setBackgroundColor(Color.WHITE);
        TextView t = new TextView(this);
        t.setText("فعال‌سازی رایدو");
        t.setTextSize(18f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(t);
        codeInput = new EditText(this);
        codeInput.setHint("کد فعال‌سازی");
        root.addView(codeInput);
        Button b = new Button(this);
        b.setText("تأیید");
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { doActivate(); }
        });
        root.addView(b);
        setContentView(root);
    }

    private void buildUi() {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(0xFFF0FDFA);
        shell.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.VERTICAL);
        top.setPadding(dp(18), dp(22), dp(18), dp(24));
        GradientDrawable tg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF0B3D3A, 0xFF134E4A, 0xFF0F766E});
        top.setBackground(tg);

        TextView brand = new TextView(this);
        brand.setText("RAIDO · فعال‌سازی");
        brand.setTextSize(12f);
        brand.setTextColor(0xCCFFFFFF);
        top.addView(brand);

        TextView h1 = new TextView(this);
        h1.setText(renewMode ? "تمدید یا ارتقا اشتراک" : "کد ورود را وارد کن");
        h1.setTextSize(20f);
        h1.setTypeface(Typeface.DEFAULT_BOLD);
        h1.setTextColor(Color.WHITE);
        h1.setPadding(0, dp(8), 0, 0);
        top.addView(h1);

        TextView sub = new TextView(this);
        sub.setText(renewMode
                ? "تاریخچه و تنظیمات حفظ می‌شود · کد جدید از پشتیبانی"
                : "یک دستگاه · بر اساس نوع کد (۷ / ۳۰ / ۳۶۵ روز)");
        sub.setTextSize(13f);
        sub.setTextColor(0xE6FFFFFF);
        sub.setPadding(0, dp(8), 0, 0);
        top.addView(sub);

        top.addView(progressBars(2));
        shell.addView(top);

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(16), dp(14), dp(28));

        body.addView(pill("② کد فعال‌سازی"));

        LinearLayout card = whiteCard(true);
        TextView badge = badge("ضروری", 0xFFEFF6FF, BLUE);
        card.addView(badge);

        TextView title = new TextView(this);
        title.setText(renewMode ? "کد تمدید / پرمیوم را وارد کن" : "کدت را از پشتیبانی بگیر");
        title.setTextSize(16.5f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(INK);
        title.setPadding(0, dp(8), 0, 0);
        card.addView(title);

        TextView desc = new TextView(this);
        desc.setText("بدون فاصله و دقیق وارد کن. بعد از تأیید، تحلیل درخواست‌ها باز می‌شود.");
        desc.setTextSize(13f);
        desc.setTextColor(MUTED);
        desc.setPadding(0, dp(6), 0, 0);
        card.addView(desc);

        TextView lab = new TextView(this);
        lab.setText("کد فعال‌سازی");
        lab.setTextSize(12f);
        lab.setTypeface(Typeface.DEFAULT_BOLD);
        lab.setTextColor(MUTED);
        lab.setPadding(0, dp(12), 0, 0);
        card.addView(lab);

        codeInput = new EditText(this);
        codeInput.setHint("XXXX-XXXX-XXXX");
        codeInput.setTextSize(14f);
        codeInput.setSingleLine(true);
        codeInput.setTextColor(INK);
        codeInput.setHintTextColor(0xFF94A3B8);
        codeInput.setPadding(dp(14), dp(13), dp(14), dp(13));
        codeInput.setTextDirection(View.TEXT_DIRECTION_LTR);
        codeInput.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        GradientDrawable ig = new GradientDrawable();
        ig.setColor(Color.WHITE);
        ig.setCornerRadius(dp(13));
        ig.setStroke(dp(2), 0xFFE2E8F0);
        codeInput.setBackground(ig);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(-1, -2);
        ilp.topMargin = dp(8);
        codeInput.setLayoutParams(ilp);
        card.addView(codeInput);

        Button btn = new Button(this);
        btn.setText("تأیید و ادامه");
        btn.setAllCaps(false);
        btn.setTextSize(14.5f);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setTextColor(Color.WHITE);
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF3B82F6, 0xFF1D4ED8});
        bg.setCornerRadius(dp(14));
        btn.setBackground(bg);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, -2);
        blp.topMargin = dp(12);
        btn.setLayoutParams(blp);
        btn.setPadding(dp(12), dp(14), dp(12), dp(14));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { doActivate(); }
        });
        card.addView(btn);

        LinearLayout status = new LinearLayout(this);
        status.setOrientation(LinearLayout.HORIZONTAL);
        status.setPadding(0, dp(12), 0, 0);
        TextView left = new TextView(this);
        left.setText("کد نداری؟");
        left.setTextSize(12.5f);
        left.setTextColor(MUTED);
        status.addView(left, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView right = new TextView(this);
        right.setText("تلگرام پشتیبانی");
        right.setTextSize(12.5f);
        right.setTypeface(Typeface.DEFAULT_BOLD);
        right.setTextColor(BLUE);
        right.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/TripAnalyzerSupport"));
                    startActivity(i);
                } catch (Throwable e) {
                    Toast.makeText(ActivationActivity.this, "تلگرام باز نشد", Toast.LENGTH_SHORT).show();
                }
            }
        });
        status.addView(right);
        card.addView(status);
        body.addView(card);

        if (LicenseStore.INSTANCE.trialAvailable(this)) {
            LinearLayout tcard = whiteCard(false);
            TextView tt = new TextView(this);
            tt.setText("🎁 تست رایگان ۷ روزه");
            tt.setTextSize(16f);
            tt.setTypeface(Typeface.DEFAULT_BOLD);
            tt.setTextColor(INK);
            tcard.addView(tt);
            TextView td = new TextView(this);
            td.setText("کد نداری؟ همین حالا ۷ روز تحلیل رایگان بگیر. فقط یک‌بار برای هر شماره و دستگاه.");
            td.setTextSize(13f);
            td.setTextColor(MUTED);
            td.setPadding(0, dp(4), 0, 0);
            tcard.addView(td);
            final Button tb = new Button(this);
            tb.setText("شروع تست رایگان");
            tb.setAllCaps(false);
            tb.setTextColor(Color.WHITE);
            tb.setTypeface(Typeface.DEFAULT_BOLD);
            GradientDrawable tbg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{0xFF14B8A6, 0xFF0F766E});
            tbg.setCornerRadius(dp(14));
            tb.setBackground(tbg);
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-1, -2);
            tlp.topMargin = dp(10);
            tb.setLayoutParams(tlp);
            tb.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { doTrial(tb); }
            });
            tcard.addView(tb);
            body.addView(tcard);
        }

        LinearLayout why = whiteCard(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView ico = new TextView(this);
        ico.setText("🔐");
        ico.setTextSize(20f);
        ico.setGravity(Gravity.CENTER);
        GradientDrawable ig2 = new GradientDrawable();
        ig2.setColor(0xFFEDE9FE);
        ig2.setCornerRadius(dp(14));
        ico.setBackground(ig2);
        LinearLayout.LayoutParams i2 = new LinearLayout.LayoutParams(dp(44), dp(44));
        i2.setMargins(0, 0, dp(12), 0);
        row.addView(ico, i2);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        TextView wt = new TextView(this);
        wt.setText("چرا کد؟");
        wt.setTextSize(16f);
        wt.setTypeface(Typeface.DEFAULT_BOLD);
        wt.setTextColor(INK);
        col.addView(wt);
        TextView wd = new TextView(this);
        wd.setText("برای استفاده مجاز و به‌روزرسانی منظم قابلیت‌های تحلیلی روی دستگاه خودت.");
        wd.setTextSize(13f);
        wd.setTextColor(MUTED);
        wd.setPadding(0, dp(4), 0, 0);
        col.addView(wd);
        row.addView(col, new LinearLayout.LayoutParams(0, -2, 1f));
        why.addView(row);
        body.addView(why);

        if (LicenseStore.INSTANCE.canViewHistory(this)) {
            TextView link = new TextView(this);
            link.setText("مشاهده گزارش‌ها و آمار (بدون کارت تحلیل)");
            link.setTextSize(14f);
            link.setTextColor(BLUE);
            link.setGravity(Gravity.CENTER);
            link.setPadding(0, dp(16), 0, 0);
            link.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { goMain(); }
            });
            body.addView(link);
        }

        scroll.addView(body);
        shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(shell);
        try { Fonts.INSTANCE.applyRecursive(shell); } catch (Exception ignored) {}
    }

    private void doTrial(final Button b) {
        b.setEnabled(false);
        new Thread(new Runnable() {
            @Override public void run() {
                final int r = LicenseStore.INSTANCE.startTrial(ActivationActivity.this);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (r == 0) {
                            Toast.makeText(ActivationActivity.this, "تست ۷ روزه فعال شد", Toast.LENGTH_SHORT).show();
                            goMain();
                            return;
                        }
                        b.setEnabled(true);
                        Toast.makeText(ActivationActivity.this, messageFor(r), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    private String messageFor(int r) {
        switch (r) {
            case 1: return "کد نامعتبر است";
            case 4: return "این کد قبلاً مصرف شده";
            case 5: return "این کد منقضی شده — از پشتیبانی کد جدید بگیر";
            case 8: return "تلاش اشتباه زیاد؛ ۱۰ دقیقه بعد دوباره امتحان کن";
            case 9: return "تست رایگان قبلاً برای این شماره یا دستگاه استفاده شده";
            case 7:
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return "دوباره وارد شو";
            default: return "خطای اتصال — اینترنت را بررسی کن";
        }
    }

    private void doActivate() {
        final String code = codeInput != null && codeInput.getText() != null
                ? codeInput.getText().toString().trim() : "";
        if (code.isEmpty()) {
            Toast.makeText(this, "کد را وارد کن", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "در حال بررسی…", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override public void run() {
                final int r = LicenseStore.INSTANCE.activateWithCode(ActivationActivity.this, code);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (r == 0) {
                            Toast.makeText(ActivationActivity.this,
                                    renewMode ? "تمدید/ارتقا موفق — داده‌ها حفظ شد" : "فعال‌سازی موفق",
                                    Toast.LENGTH_SHORT).show();
                            goMain();
                        } else {
                            Toast.makeText(ActivationActivity.this, messageFor(r), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void goMain() {
        try {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        } catch (Throwable ignored) {}
    }

    private LinearLayout progressBars(int filled) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(14), 0, 0);
        for (int i = 0; i < 4; i++) {
            View bar = new View(this);
            GradientDrawable g = new GradientDrawable();
            g.setCornerRadius(dp(99));
            g.setColor(i < filled ? 0xFF5EEAD4 : 0x33FFFFFF);
            bar.setBackground(g);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(5), 1f);
            if (i < 3) lp.setMargins(0, 0, dp(7), 0);
            row.addView(bar, lp);
        }
        return row;
    }

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
        lp.bottomMargin = dp(12);
        v.setLayoutParams(lp);
        return v;
    }

    private LinearLayout whiteCard(boolean hero) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15), dp(16), dp(15), dp(16));
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(20));
        g.setStroke(dp(1), hero ? 0xFF99F6E4 : 0xFFE2E8F0);
        c.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(12);
        c.setLayoutParams(lp);
        return c;
    }

    private TextView badge(String t, int bg, int fg) {
        TextView v = new TextView(this);
        v.setText(t);
        v.setTextSize(11f);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setTextColor(fg);
        v.setPadding(dp(10), dp(4), dp(10), dp(4));
        GradientDrawable g = new GradientDrawable();
        g.setColor(bg);
        g.setCornerRadius(dp(99));
        v.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        v.setLayoutParams(lp);
        return v;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
