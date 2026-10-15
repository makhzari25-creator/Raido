package ir.tripanalyzer.app;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * کارت تحلیل (سبز/زرد/قرمز). هم در پنجرهٔ Accessibility overlay استفاده می‌شود
 * (بدون نیاز به «Appear on top») و هم در VerdictOverlayActivity به‌عنوان fallback.
 */
public final class VerdictCard {

    private VerdictCard() {}

    /** بین سبز کم‌رنگ (ratio=0) و سبز پررنگ (ratio=1) درون‌یابی می‌کند. */
    static int interpolateGreen(float ratio) {
        if (ratio < 0f) ratio = 0f;
        if (ratio > 1f) ratio = 1f;
        int lightR = 0xA7, lightG = 0xF3, lightB = 0xD0;
        int fullR = 0x00, fullG = 0xC8, fullB = 0x53;
        int r = Math.round(lightR + (fullR - lightR) * ratio);
        int g = Math.round(lightG + (fullG - lightG) * ratio);
        int b = Math.round(lightB + (fullB - lightB) * ratio);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static TextView tv(Context c, String text, float size, int color, int padTop, int padBottom,
                               View.OnClickListener dismiss) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(17);
        t.setPadding(0, padTop, 0, padBottom);
        if (dismiss != null) t.setOnClickListener(dismiss);
        return t;
    }

    /**
     * @return یک FrameLayout شفاف که کارت را در مرکز دارد.
     *         با WRAP_CONTENT در WindowManager هم درست اندازه می‌گیرد.
     */
    public static View build(Context c, String verdict, String fare, String hourly, String distSrc,
                             float greenRatio, String zoneLabel, View.OnClickListener dismiss) {
        if (verdict == null) verdict = "GRAY";
        if (fare == null) fare = "";

        int bg, fg;
        String title;
        if ("GREEN".equals(verdict)) {
            bg = interpolateGreen(greenRatio);
            title = "سفر به‌صرفه است ✓";
            fg = 0xFF000000;
        } else if ("YELLOW".equals(verdict)) {
            bg = 0xFFFFFF00;
            title = "نزدیک هدف — با احتیاط";
            fg = 0xFF000000;
        } else if ("RED".equals(verdict)) {
            bg = 0xFFFF0000;
            title = "این سفر به‌صرفه نیست";
            fg = 0xFFFFFFFF;
        } else {
            bg = 0xFF888888;
            title = "اطلاعات کافی نیست";
            try {
                org.json.JSONObject last = Store.INSTANCE.last(c);
                if (last != null) {
                    String gr = last.optString("grayReason", "");
                    if (gr != null && gr.length() > 0) title = gr;
                }
            } catch (Throwable ignored) {}
            fg = 0xFFFFFFFF;
        }

        LinearLayout card = new LinearLayout(c);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(17);
        card.setPadding(32, 24, 32, 24);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bg);
        gd.setCornerRadius(24f);
        card.setBackground(gd);
        card.setOnClickListener(dismiss);

        card.addView(tv(c, title, 22f, fg, 0, 0, dismiss));

        String fareText;
        if (fare.length() > 0) {
            String clean = fare.replace(".0", "").trim();
            try {
                double fv = Double.parseDouble(clean.replace(",", ""));
                clean = String.format(java.util.Locale.US, "%,.0f", fv);
            } catch (Exception ignored) {}
            fareText = "قیمت درخواست: " + clean + " تومان";
        } else {
            fareText = "قیمت: —";
        }
        card.addView(tv(c, fareText, 18f, fg, 12, 4, dismiss));

        String hourlyText = (hourly != null && hourly.length() > 0)
                ? "درآمد ساعتی این سفر: " + hourly + " تومان"
                : "درآمد ساعتی این سفر: —";
        card.addView(tv(c, hourlyText, 16f, fg, 0, 12, dismiss));

        card.addView(tv(c, "منبع مسافت: " + (distSrc == null ? "تخمینی" : distSrc), 14f, fg, 0, 0, dismiss));

        if (zoneLabel != null && zoneLabel.length() > 0) {
            boolean outZone = zoneLabel.contains("خارج محدوده");
            if (outZone) {
                // برچسب درشت برای خارج محدوده
                card.addView(tv(c, "خارج محدوده", 17f, 0xFFFFF1F2, 10, 2, dismiss));
                String rest = zoneLabel.replace("خارج محدوده", "").replace("·", "").trim();
                if (rest.length() > 0) {
                    card.addView(tv(c, rest, 12f, fg, 0, 4, dismiss));
                }
            } else {
                card.addView(tv(c, zoneLabel, 13f, fg, 8, 4, dismiss));
            }
        }

        card.addView(tv(c, "ثبت سفر از کادر «رفتی؟» بعد از چند دقیقه انجام می‌شود · لمس هرجای صفحه = بستن",
                12f, fg, 0, 0, dismiss));

        int screenW = c.getResources().getDisplayMetrics().widthPixels;
        int w = Math.min(800, Math.max(300, screenW - 48));
        FrameLayout root = new FrameLayout(c);
        root.setBackgroundColor(0);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, -2);
        lp.gravity = 17;
        root.addView(card, lp);
        return root;
    }
}
