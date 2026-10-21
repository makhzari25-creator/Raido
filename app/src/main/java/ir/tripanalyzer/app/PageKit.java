package ir.tripanalyzer.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** اجزای مشترک ظاهر رایدو برای صفحات ساده (هدر جاده‌ای، کارت، عنوان بخش، دکمه) */
final class PageKit {
    static final int DARK = 0xFF0F172A;
    static final int MUTED = 0xFF64748B;
    static final int BORDER = 0xFFE2E8F0;
    static final int BLUE = 0xFF2563EB;
    static final int TEAL = 0xFF0F3D3A;
    static final int BG = 0xFFF1F5F9;

    private PageKit() {}

    static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    static View header(final Activity a, String title, String subtitle) {
        FrameLayout head = new FrameLayout(a);
        ImageView road = new ImageView(a);
        road.setScaleType(ImageView.ScaleType.CENTER_CROP);
        try { road.setImageResource(R.drawable.bg_road); } catch (Throwable ignored) {}
        head.addView(road, new FrameLayout.LayoutParams(-1, -1));
        View shade = new View(a);
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x990F172A, 0xE61E3A8A}));
        head.addView(shade, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout in = new LinearLayout(a);
        in.setOrientation(LinearLayout.VERTICAL);
        in.setPadding(dp(a, 16), dp(a, 14), dp(a, 16), dp(a, 16));
        TextView back = new TextView(a);
        back.setText("→");
        back.setTextSize(20f);
        back.setTextColor(Color.WHITE);
        back.setGravity(Gravity.CENTER);
        GradientDrawable bb = new GradientDrawable();
        bb.setShape(GradientDrawable.OVAL);
        bb.setColor(0x33FFFFFF);
        bb.setStroke(dp(a, 1), 0x55FFFFFF);
        back.setBackground(bb);
        back.setOnClickListener(v -> a.finish());
        in.addView(back, new LinearLayout.LayoutParams(dp(a, 40), dp(a, 40)));
        TextView t = new TextView(a);
        t.setText(title);
        t.setTextSize(22f);
        t.setTextColor(Color.WHITE);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(a, 12), 0, 0);
        in.addView(t);
        if (subtitle != null) {
            TextView s = new TextView(a);
            s.setText(subtitle);
            s.setTextSize(13f);
            s.setTextColor(0xE6FFFFFF);
            s.setPadding(0, dp(a, 4), 0, 0);
            in.addView(s);
        }
        head.addView(in, new FrameLayout.LayoutParams(-1, -2));
        return head;
    }

    static LinearLayout card(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(c, 16), dp(c, 14), dp(c, 16), dp(c, 14));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(c, 20));
        bg.setColor(Color.WHITE);
        bg.setStroke(dp(c, 1), BORDER);
        l.setBackground(bg);
        return l;
    }

    static LinearLayout.LayoutParams gap(Context c, int top) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(c, top);
        return lp;
    }

    static TextView sectionTitle(Context c, String s) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(15.5f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(DARK);
        t.setPadding(0, 0, 0, dp(c, 8));
        try {
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(BLUE);
            dot.setSize(dp(c, 9), dp(c, 9));
            dot.setBounds(0, 0, dp(c, 9), dp(c, 9));
            t.setCompoundDrawablesRelative(dot, null, null, null);
            t.setCompoundDrawablePadding(dp(c, 8));
        } catch (Throwable ignored) {}
        return t;
    }

    static TextView body(Context c, String s) {
        TextView b = new TextView(c);
        b.setText(s);
        b.setTextSize(13.5f);
        b.setTextColor(0xFF475569);
        b.setLineSpacing(dp(c, 4), 1f);
        return b;
    }

    static TextView button(Context c, String text, int color) {
        TextView b = new TextView(c);
        b.setText(text);
        b.setTextSize(14f);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(Color.WHITE);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(c, 12), dp(c, 12), dp(c, 12), dp(c, 12));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(c, 14));
        bg.setColor(color);
        b.setBackground(bg);
        b.setClickable(true);
        return b;
    }
}
