package ir.tripanalyzer.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * ویجت‌های نمودار سبک — بدون کتابخانه خارجی.
 * فقط نمایش؛ هیچ اثری روی منطق تحلیل ندارند.
 */
public final class ChartKit {
    private ChartKit() {}

    public static int dp(Context c, int v) {
        float d = c.getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }

    public static GradientDrawable roundRect(int color, float radiusDp, Context c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, (int) radiusDp));
        return g;
    }

    public static GradientDrawable roundStroke(int fill, int stroke, float radiusDp, Context c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(c, (int) radiusDp));
        g.setStroke(dp(c, 1), stroke);
        return g;
    }

    /** میله‌های عمودی ساده (اسپارک / ۲۴ساعته) با عدد میانگین روی هر ستون */
    public static LinearLayout verticalBars(Context c, float[] values, int heightDp, boolean highlightMax) {
        LinearLayout wrap = new LinearLayout(c);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

        float max = 0.001f;
        int peakIdx = -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) { max = values[i]; peakIdx = i; }
        }

        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.BOTTOM);
        row.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(c, heightDp + 18)));

        for (int i = 0; i < values.length; i++) {
            LinearLayout col = new LinearLayout(c);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, -1, 1f);
            clp.setMargins(dp(c, 1), 0, dp(c, 1), 0);
            col.setLayoutParams(clp);

            TextView lab = new TextView(c);
            if (values[i] > 0) {
                // کوتاه: هزارتومان
                float v = values[i];
                String s;
                if (v >= 1000f) s = String.format(java.util.Locale.US, "%.0f", v / 1000f);
                else s = String.format(java.util.Locale.US, "%.0f", v);
                lab.setText(s);
            } else {
                lab.setText("");
            }
            lab.setTextSize(8f);
            lab.setTextColor(0xFF334155);
            lab.setGravity(Gravity.CENTER_HORIZONTAL);
            lab.setSingleLine(true);
            col.addView(lab);

            View bar = new View(c);
            float ratio = values[i] <= 0 ? 0.04f : Math.max(0.06f, values[i] / max);
            int h = Math.max(dp(c, 3), (int) (dp(c, heightDp) * ratio));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, h);
            lp.gravity = Gravity.BOTTOM;
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(c, 3));
            if (values[i] <= 0) {
                bg.setColor(0xFFE2E8F0);
            } else if (highlightMax && i == peakIdx) {
                bg.setColors(new int[]{0xFF6EE7B7, 0xFF047857});
                bg.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
            } else {
                bg.setColors(new int[]{0xFF99F6E4, 0xFF0F766E});
                bg.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
            }
            bar.setBackground(bg);
            col.addView(bar, lp);
            row.addView(col);
        }
        wrap.addView(row);
        return wrap;
    }

    /** نوار افقی پیشرفت */
    public static LinearLayout hBarRow(Context c, String label, float value, float maxRef, int colorStart, int colorEnd) {
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(c, 4), 0, dp(c, 4));

        TextView lab = new TextView(c);
        lab.setText(label);
        lab.setTextSize(11f);
        lab.setTextColor(0xFF64748B);
        LinearLayout.LayoutParams lpLab = new LinearLayout.LayoutParams(dp(c, 56), -2);
        row.addView(lab, lpLab);

        LinearLayout track = new LinearLayout(c);
        track.setBackground(roundRect(0xFFEEF2F6, 99, c));
        LinearLayout.LayoutParams lpTrack = new LinearLayout.LayoutParams(0, dp(c, 10), 1f);
        lpTrack.setMargins(dp(c, 6), 0, dp(c, 6), 0);
        track.setLayoutParams(lpTrack);

        View fill = new View(c);
        float ratio = maxRef > 0 ? Math.min(1f, value / maxRef) : 0f;
        GradientDrawable fg = new GradientDrawable();
        fg.setCornerRadius(dp(c, 99));
        fg.setColors(new int[]{colorStart, colorEnd});
        fg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        fill.setBackground(fg);
        track.addView(fill, new LinearLayout.LayoutParams(0, -1, Math.max(0.02f, ratio)));
        // spacer
        if (ratio < 0.99f) {
            View spacer = new View(c);
            track.addView(spacer, new LinearLayout.LayoutParams(0, -1, 1f - Math.max(0.02f, ratio)));
        }
        row.addView(track);

        TextView val = new TextView(c);
        val.setText(value > 0 ? String.valueOf(Math.round(value / 1000f)) : "—");
        val.setTextSize(12f);
        val.setTypeface(Typeface.DEFAULT_BOLD);
        val.setTextColor(0xFF0F172A);
        val.setMinWidth(dp(c, 32));
        val.setGravity(Gravity.LEFT);
        row.addView(val);
        return row;
    }

    /** هیت‌مپ ۷ خانه */
    public static LinearLayout heatWeek(Context c, int[] counts, String[] labels) {
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        int max = 1;
        for (int n : counts) if (n > max) max = n;
        for (int i = 0; i < 7; i++) {
            int n = i < counts.length ? counts[i] : 0;
            float r = n <= 0 ? 0 : (float) n / max;
            int color;
            if (n <= 0) color = 0xFFE8EEF4;
            else if (r < 0.25f) color = 0xFF99F6E4;
            else if (r < 0.5f) color = 0xFF2DD4BF;
            else if (r < 0.75f) color = 0xFF0F766E;
            else color = 0xFF115E59;
            TextView cell = new TextView(c);
            cell.setText(labels != null && i < labels.length ? labels[i] : "");
            cell.setTextSize(10f);
            cell.setTypeface(Typeface.DEFAULT_BOLD);
            cell.setGravity(Gravity.CENTER);
            cell.setTextColor(n <= 0 || r < 0.5f ? 0xFF115E59 : 0xFFFFFFFF);
            GradientDrawable bg = roundRect(color, 8, c);
            cell.setBackground(bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(c, 36), 1f);
            lp.setMargins(dp(c, 2), 0, dp(c, 2), 0);
            row.addView(cell, lp);
        }
        return row;
    }

    /** دونات ساده تحلیل با Canvas */
    public static View donut(Context c, int green, int yellow, int red, int sizeDp) {
        return new DonutView(c, green, yellow, red, sizeDp);
    }

    public static final class DonutView extends View {
        private final int g, y, r, sizePx;
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint t = new Paint(Paint.ANTI_ALIAS_FLAG);

        public DonutView(Context c, int green, int yellow, int red, int sizeDp) {
            super(c);
            this.g = Math.max(0, green);
            this.y = Math.max(0, yellow);
            this.r = Math.max(0, red);
            this.sizePx = ChartKit.dp(c, sizeDp);
            t.setColor(0xFF0F172A);
            t.setTextAlign(Paint.Align.CENTER);
            t.setTypeface(Typeface.DEFAULT_BOLD);
        }

        @Override protected void onMeasure(int w, int h) {
            setMeasuredDimension(sizePx, sizePx);
        }

        @Override protected void onDraw(Canvas canvas) {
            int total = g + y + r;
            float cx = sizePx / 2f, cy = sizePx / 2f, rad = sizePx * 0.38f;
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(sizePx * 0.1f);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(0xFFE8EEF4);
            canvas.drawCircle(cx, cy, rad, p);
            if (total <= 0) {
                t.setTextSize(sizePx * 0.18f);
                canvas.drawText("0", cx, cy + sizePx * 0.06f, t);
                return;
            }
            float start = -90f;
            float[] sweeps = {
                    360f * g / total,
                    360f * y / total,
                    360f * r / total
            };
            int[] cols = {0xFF10B981, 0xFFF59E0B, 0xFFEF4444};
            for (int i = 0; i < 3; i++) {
                if (sweeps[i] <= 0.5f) continue;
                p.setColor(cols[i]);
                canvas.drawArc(cx - rad, cy - rad, cx + rad, cy + rad, start, sweeps[i] - 1.5f, false, p);
                start += sweeps[i];
            }
            t.setTextSize(sizePx * 0.22f);
            canvas.drawText(String.valueOf(total), cx, cy + sizePx * 0.02f, t);
            t.setTextSize(sizePx * 0.1f);
            t.setColor(0xFF64748B);
            t.setTypeface(Typeface.DEFAULT);
            canvas.drawText("تحلیل", cx, cy + sizePx * 0.16f, t);
            t.setColor(0xFF0F172A);
            t.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }

    /** نمودار خط+ناحیه ساده برای ۷ نقطه */
    public static View areaSpark(Context c, float[] values, int heightDp) {
        return new AreaSparkView(c, values, heightDp);
    }

    public static final class AreaSparkView extends View {
        private final float[] vals;
        private final int hPx;
        private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint grid = new Paint(Paint.ANTI_ALIAS_FLAG);

        public AreaSparkView(Context c, float[] values, int heightDp) {
            super(c);
            this.vals = values != null ? values : new float[7];
            this.hPx = ChartKit.dp(c, heightDp);
            line.setStyle(Paint.Style.STROKE);
            line.setStrokeWidth(ChartKit.dp(c, 2));
            line.setColor(0xFF12B5A0);
            line.setStrokeCap(Paint.Cap.ROUND);
            line.setStrokeJoin(Paint.Join.ROUND);
            grid.setColor(0xFFE8EEF4);
            grid.setStrokeWidth(1f);
        }

        @Override protected void onMeasure(int wSpec, int hSpec) {
            int w = MeasureSpec.getSize(wSpec);
            setMeasuredDimension(w, hPx);
        }

        @Override protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;
            for (int i = 1; i <= 3; i++) {
                float y = h * i / 4f;
                canvas.drawLine(0, y, w, y, grid);
            }
            float max = 0.001f;
            for (float v : vals) if (v > max) max = v;
            int n = vals.length;
            if (n < 2) return;
            Path path = new Path();
            Path area = new Path();
            for (int i = 0; i < n; i++) {
                float x = n == 1 ? w / 2f : (w * i / (float) (n - 1));
                float y = h - (vals[i] / max) * (h * 0.85f) - h * 0.08f;
                if (i == 0) {
                    path.moveTo(x, y);
                    area.moveTo(x, h);
                    area.lineTo(x, y);
                } else {
                    path.lineTo(x, y);
                    area.lineTo(x, y);
                }
            }
            area.lineTo(w, h);
            area.close();
            fill.setShader(new LinearGradient(0, 0, 0, h, 0x5912B5A0, 0x0012B5A0, Shader.TileMode.CLAMP));
            canvas.drawPath(area, fill);
            canvas.drawPath(path, line);
            // end dot
            float ex = w;
            float ey = h - (vals[n - 1] / max) * (h * 0.85f) - h * 0.08f;
            Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
            dot.setColor(0xFF12B5A0);
            canvas.drawCircle(ex, ey, ChartKit.dp(getContext(), 4), dot);
            dot.setColor(0xFFFFFFFF);
            canvas.drawCircle(ex, ey, ChartKit.dp(getContext(), 2), dot);
        }
    }
}
