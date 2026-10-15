package ir.tripanalyzer.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * صفحه خوش‌آمد — ظاهر ویزارد تأییدشده (بدون تغییر منطق).
 */
public final class OnboardingActivity extends BaseActivity {

    private static final int TEAL = 0xFF0B3D3A;
    private static final int TEAL2 = 0xFF0F766E;
    private static final int MUTED = 0xFF64748B;
    private static final int INK = 0xFF0F172A;

    @Override
    protected boolean usePagesBackground() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            buildUi();
        } catch (Throwable t) {
            // fallback ایمن: مستقیم ویزارد بعدی
            try {
                startActivity(new Intent(this, SetupWizardActivity.class));
                finish();
            } catch (Throwable ignored) {}
        }
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        root.setBackgroundColor(0xFFF0FDFA);

        // هدر تیره
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.VERTICAL);
        top.setPadding(dp(18), dp(22), dp(18), dp(24));
        GradientDrawable tg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF0B3D3A, 0xFF134E4A, 0xFF0F766E});
        top.setBackground(tg);

        TextView brand = new TextView(this);
        brand.setText("RAIDO");
        brand.setTextSize(12f);
        brand.setTextColor(0xCCFFFFFF);
        brand.setLetterSpacing(0.08f);
        top.addView(brand);

        TextView h1 = new TextView(this);
        h1.setText("سلام راننده 👋\nآماده‌ای هوشمندتر کار کنی؟");
        h1.setTextSize(20f);
        h1.setTypeface(Typeface.DEFAULT_BOLD);
        h1.setTextColor(Color.WHITE);
        h1.setPadding(0, dp(8), 0, 0);
        h1.setLineSpacing(0, 1.25f);
        top.addView(h1);

        TextView sub = new TextView(this);
        sub.setText("قبل از قبول هر سفر، ببین به هدفت می‌ارزه یا نه");
        sub.setTextSize(13f);
        sub.setTextColor(0xE6FFFFFF);
        sub.setPadding(0, dp(8), 0, 0);
        top.addView(sub);

        top.addView(progressRow(1));
        root.addView(top);

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(16), dp(14), dp(28));

        body.addView(pill("① خوش‌آمدگویی"));

        // کارت تحلیل
        LinearLayout c1 = card(true);
        c1.addView(iconRow("💡", 0xFFCCFBF1,
                "تحلیل فوری هر درخواست",
                "رایدو عددها را حساب می‌کند و با سه رنگ راهنمایی‌ات می‌کند."));
        LinearLayout lights = new LinearLayout(this);
        lights.setOrientation(LinearLayout.HORIZONTAL);
        lights.setPadding(0, dp(10), 0, 0);
        lights.addView(tl("سبز · برو", 0xFFECFDF5, 0xFF059669));
        lights.addView(tl("زرد · فکر کن", 0xFFFFFBEB, 0xFFD97706));
        lights.addView(tl("قرمز · رد کن", 0xFFFEF2F2, 0xFFDC2626));
        c1.addView(lights);
        body.addView(c1);

        LinearLayout c2 = card(false);
        c2.addView(iconRow("🛡️", 0xFFDBEAFE,
                "دست به اسنپ نمی‌زنیم",
                "سفر را قبول یا رد نمی‌کنیم. فقط صفحه را می‌خوانیم تا خودت تصمیم بگیری."));
        body.addView(c2);

        LinearLayout c3 = card(false);
        c3.addView(iconRow("⏱️", 0xFFFEF3C7,
                "کمتر از ۲ دقیقه",
                "کد → خودرو و هدف → دسترسی‌ها. بعد کارت تحلیل روی اسنپ می‌آید."));
        Button start = primaryBtn("شروع راه‌اندازی", 0xFF0F766E, 0xFF14B8A6);
        start.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    startActivity(new Intent(OnboardingActivity.this, SetupWizardActivity.class));
                    finish();
                } catch (Throwable ignored) {}
            }
        });
        c3.addView(start);
        body.addView(c3);

        TextView foot = new TextView(this);
        foot.setText("با ادامه، شرایط استفاده را می‌پذیری. داده‌هایت روی خود گوشی می‌ماند.");
        foot.setTextSize(11.5f);
        foot.setTextColor(MUTED);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(dp(8), dp(12), dp(8), 0);
        body.addView(foot);

        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
        try { Fonts.INSTANCE.applyRecursive(root); } catch (Exception ignored) {}
    }

    private LinearLayout progressRow(int filled) {
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

    private LinearLayout card(boolean hero) {
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

    private LinearLayout iconRow(String emoji, int icoBg, String title, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);

        TextView ico = new TextView(this);
        ico.setText(emoji);
        ico.setTextSize(20f);
        ico.setGravity(Gravity.CENTER);
        GradientDrawable ig = new GradientDrawable();
        ig.setColor(icoBg);
        ig.setCornerRadius(dp(14));
        ico.setBackground(ig);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(44), dp(44));
        ilp.setMargins(0, 0, dp(12), 0);
        row.addView(ico, ilp);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(16f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(INK);
        col.addView(t);
        TextView d = new TextView(this);
        d.setText(desc);
        d.setTextSize(13f);
        d.setTextColor(MUTED);
        d.setPadding(0, dp(4), 0, 0);
        d.setLineSpacing(0, 1.25f);
        col.addView(d);
        row.addView(col, new LinearLayout.LayoutParams(0, -2, 1f));
        return row;
    }

    private TextView tl(String text, int bg, int fg) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(11f);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setTextColor(fg);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(4), dp(10), dp(4), dp(10));
        GradientDrawable g = new GradientDrawable();
        g.setColor(bg);
        g.setCornerRadius(dp(12));
        v.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
        lp.setMargins(dp(3), 0, dp(3), 0);
        v.setLayoutParams(lp);
        return v;
    }

    private Button primaryBtn(String label, int c1, int c2) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(14.5f);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(Color.WHITE);
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{c1, c2});
        g.setCornerRadius(dp(14));
        b.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(12);
        b.setLayoutParams(lp);
        b.setPadding(dp(12), dp(14), dp(12), dp(14));
        return b;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
