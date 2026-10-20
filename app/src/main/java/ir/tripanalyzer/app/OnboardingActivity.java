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
        root.setBackgroundColor(0xFFF1F5F9);

        // ── هدر: تصویر جاده + پوشش گرادیان آبی برند ──
        // ارتفاع هدر فقط از متن (آخرین فرزند) تعیین می‌شود؛ تصویر و پوشش هم‌اندازهٔ آن می‌شوند
        android.widget.FrameLayout hero = new android.widget.FrameLayout(this) {
            @Override protected void onMeasure(int wSpec, int hSpec) {
                int width = View.MeasureSpec.getSize(wSpec);
                int n = getChildCount();
                View t = getChildAt(n - 1);
                t.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                int height = t.getMeasuredHeight();
                for (int i = 0; i < n - 1; i++) {
                    getChildAt(i).measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
                }
                setMeasuredDimension(width, height);
            }
        };
        try {
            android.widget.ImageView road = new android.widget.ImageView(this);
            road.setImageResource(R.drawable.bg_road);
            road.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            hero.addView(road, new android.widget.FrameLayout.LayoutParams(-1, -1));
        } catch (Throwable ignored) {}
        View shade = new View(this);
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xE60F172A, 0xCC1E3A8A, 0xB32563EB}));
        hero.addView(shade, new android.widget.FrameLayout.LayoutParams(-1, -1));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.VERTICAL);
        top.setPadding(dp(22), dp(30), dp(22), dp(30));

        LinearLayout brandCol = new LinearLayout(this);
        brandCol.setOrientation(LinearLayout.VERTICAL);
        TextView brand = new TextView(this);
        brand.setText("RAIDO");
        brand.setTextSize(30f);
        brand.setTypeface(Fonts.INSTANCE.english());
        brand.setTextColor(Color.WHITE);
        brand.setLetterSpacing(0.06f);
        brandCol.addView(brand);
        TextView tag = new TextView(this);
        tag.setText("تحلیل سفر، تصمیم بهتر");
        tag.setTextSize(12f);
        tag.setTextColor(0xCCFFFFFF);
        brandCol.addView(tag);
        top.addView(brandCol);

        TextView h1 = new TextView(this);
        h1.setText("سلام راننده 👋");
        h1.setTextSize(22f);
        h1.setTypeface(Typeface.DEFAULT_BOLD);
        h1.setTextColor(Color.WHITE);
        h1.setPadding(0, dp(22), 0, 0);
        top.addView(h1);
        TextView h2 = new TextView(this);
        h2.setText("آماده‌ای هوشمندتر کار کنی؟");
        h2.setTextSize(22f);
        h2.setTypeface(Typeface.DEFAULT_BOLD);
        h2.setTextColor(Color.WHITE);
        top.addView(h2);
        TextView sub = new TextView(this);
        sub.setText("قبل از قبول هر سفر، ببین به هدفت می‌ارزه یا نه");
        sub.setTextSize(14f);
        sub.setTextColor(0xE6FFFFFF);
        sub.setPadding(0, dp(8), 0, 0);
        top.addView(sub);
        hero.addView(top, new android.widget.FrameLayout.LayoutParams(-1, -2));

        float rr = dp(28);
        final float[] radii = new float[]{0, 0, 0, 0, rr, rr, rr, rr};
        hero.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override public void getOutline(View v, android.graphics.Outline o) {
                android.graphics.Path path = new android.graphics.Path();
                path.addRoundRect(0, 0, v.getWidth(), v.getHeight(), radii, android.graphics.Path.Direction.CW);
                try { o.setPath(path); } catch (Throwable t) { o.setRect(0, 0, v.getWidth(), v.getHeight()); }
            }
        });
        hero.setClipToOutline(true);
        root.addView(hero, new LinearLayout.LayoutParams(-1, -2));

        root.addView(dotsRow(1));

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(16), dp(8), dp(16), dp(24));

        body.addView(pill("خوش‌آمدگویی"));

        LinearLayout c1 = card();
        c1.addView(iconRow(R.drawable.ic_ob_chart, 0xFFDBEAFE, 0xFF2563EB,
                "تحلیل فوری هر درخواست",
                "رایدو عددها را حساب می‌کند و با سه رنگ راهنمایی‌ات می‌کند."));
        LinearLayout lights = new LinearLayout(this);
        lights.setOrientation(LinearLayout.HORIZONTAL);
        lights.setPadding(0, dp(14), 0, 0);
        lights.addView(tl("سبز · برو", 0xFFECFDF5, 0xFF16A34A));
        lights.addView(tl("زرد · فکر کن", 0xFFFEF9C3, 0xFFA16207));
        lights.addView(tl("قرمز · رد کن", 0xFFFEF2F2, 0xFFDC2626));
        c1.addView(lights);
        body.addView(c1);

        LinearLayout c2 = card();
        c2.addView(iconRow(R.drawable.ic_lg_shield, 0xFFDCFCE7, 0xFF16A34A,
                "دست به اسنپ نمی‌زنیم",
                "سفر را قبول یا رد نمی‌کنیم. فقط صفحه را می‌خوانیم تا خودت تصمیم بگیری."));
        body.addView(c2);

        LinearLayout c3 = card();
        c3.addView(iconRow(R.drawable.ic_ob_timer, 0xFFFEF3C7, 0xFFD97706,
                "کمتر از ۲ دقیقه",
                "کد → خودرو و هدف → دسترسی‌ها. بعد کارت تحلیل روی اسنپ می‌آید."));
        body.addView(c3);

        Button start = primaryBtn("شروع راه‌اندازی");
        start.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    startActivity(new Intent(OnboardingActivity.this, SetupWizardActivity.class));
                    finish();
                } catch (Throwable ignored) {}
            }
        });
        body.addView(start);

        TextView foot = new TextView(this);
        foot.setText("با ادامه، شرایط استفاده را می‌پذیری. داده‌هایت روی خود گوشی می‌ماند.");
        foot.setTextSize(12f);
        foot.setTextColor(MUTED);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(dp(8), dp(14), dp(8), 0);
        body.addView(foot);

        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
        try { Fonts.INSTANCE.applyRecursive(root); } catch (Exception ignored) {}
    }

    /** نقطه‌های مرحله با خط اتصال (فقط نمایشی) */
    private LinearLayout dotsRow(int active) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        row.setPadding(0, dp(16), 0, dp(6));
        for (int i = 1; i <= 4; i++) {
            View dot = new View(this);
            GradientDrawable g = new GradientDrawable();
            g.setShape(GradientDrawable.OVAL);
            g.setColor(i == active ? 0xFF2563EB : 0xFFCBD5E1);
            dot.setBackground(g);
            int sz = i == active ? dp(14) : dp(10);
            row.addView(dot, new LinearLayout.LayoutParams(sz, sz));
            if (i < 4) {
                View line = new View(this);
                line.setBackgroundColor(0xFFCBD5E1);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(34), Math.max(1, dp(2)));
                lp.setMargins(dp(4), 0, dp(4), 0);
                row.addView(line, lp);
            }
        }
        return row;
    }

    private TextView pill(String t) {
        TextView v = new TextView(this);
        v.setText("✓  " + t);
        v.setTextSize(12f);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setTextColor(0xFF2563EB);
        v.setPadding(dp(14), dp(6), dp(14), dp(6));
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(99));
        g.setStroke(dp(1), 0xFFBFDBFE);
        v.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.topMargin = dp(8);
        lp.bottomMargin = dp(14);
        v.setLayoutParams(lp);
        return v;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(16), dp(16), dp(16));
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(22));
        g.setStroke(dp(1), 0xFFE2E8F0);
        c.setBackground(g);
        try { c.setElevation(dp(2)); } catch (Throwable ignored) {}
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(14);
        c.setLayoutParams(lp);
        return c;
    }

    private LinearLayout iconRow(int icon, int tint, int accent, String title, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);

        android.widget.ImageView ico = new android.widget.ImageView(this);
        ico.setImageResource(icon);
        ico.setColorFilter(accent);
        ico.setPadding(dp(13), dp(13), dp(13), dp(13));
        GradientDrawable ig = new GradientDrawable();
        ig.setColor(tint);
        ig.setCornerRadius(dp(16));
        ico.setBackground(ig);
        row.addView(ico, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPaddingRelative(dp(14), 0, 0, 0);
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(16.5f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(INK);
        col.addView(t);
        TextView d = new TextView(this);
        d.setText(desc);
        d.setTextSize(13.5f);
        d.setTextColor(MUTED);
        d.setPadding(0, dp(4), 0, 0);
        d.setLineSpacing(dp(3), 1f);
        col.addView(d);
        row.addView(col, new LinearLayout.LayoutParams(0, -2, 1f));
        return row;
    }

    /** چیپ چراغ راهنما: نقطهٔ رنگی + متن */
    private TextView tl(String text, int bg, int fg) {
        TextView v = new TextView(this);
        v.setText("●  " + text);
        v.setTextSize(12f);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setTextColor(fg);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(4), dp(11), dp(4), dp(11));
        GradientDrawable g = new GradientDrawable();
        g.setColor(bg);
        g.setCornerRadius(dp(14));
        v.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
        lp.setMargins(dp(3), 0, dp(3), 0);
        v.setLayoutParams(lp);
        return v;
    }

    private Button primaryBtn(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(16f);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(Color.WHITE);
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{0xFF3B82F6, 0xFF2563EB});
        g.setCornerRadius(dp(16));
        b.setBackground(g);
        try {
            android.graphics.drawable.Drawable ic = getResources().getDrawable(R.drawable.ic_ob_arrow, getTheme()).mutate();
            ic.setTint(Color.WHITE);
            ic.setBounds(0, 0, dp(20), dp(20));
            b.setCompoundDrawablesRelative(null, null, ic, null);
            b.setCompoundDrawablePadding(dp(10));
        } catch (Throwable ignored) {}
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(56));
        lp.topMargin = dp(4);
        b.setLayoutParams(lp);
        b.setPadding(dp(24), 0, dp(24), 0);
        return b;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
