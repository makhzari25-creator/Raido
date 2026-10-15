package ir.tripanalyzer.app;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * آمار هفته (۷ روز) و ماه (۳۰ روز) از raido_data.json
 */
public final class WeeklyStatsActivity extends BaseActivity {

    private static final int BG = 0xFFF8FAFC;
    private static final int TEAL = 0xFF0F3D3A;
    private static final int TEXT_DARK = 0xFF0F172A;
    private static final int TEXT_MUTED = 0xFF64748B;
    private static final int GREEN = 0xFF16A34A;
    private static final int AMBER = 0xFFD97706;
    private static final int RED = 0xFFDC2626;
    private static final int CARD_BORDER = 0xFFE2E8F0;
    private static final int TAB_ON = 0xFF0F3D3A;
    private static final int TAB_OFF = 0xFFE2E8F0;

    private LinearLayout contentHost;
    private TextView tabWeek;
    private TextView tabMonth;
    private boolean showingWeek = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(BG);
        shell.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(8), dp(14), dp(16), dp(14));
        bar.setBackgroundColor(TEAL);
        TextView back = new TextView(this);
        back.setText("←");
        back.setTextSize(22f);
        back.setTextColor(Color.WHITE);
        back.setPadding(dp(12), dp(4), dp(12), dp(4));
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        bar.addView(back);
        TextView title = new TextView(this);
        title.setText("آمار");
        title.setTextSize(18f);
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        title.setGravity(Gravity.CENTER);
        try { Fonts.INSTANCE.apply(title, true); } catch (Exception ignored) {}
        bar.addView(title);
        View pad = new View(this);
        pad.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(1)));
        bar.addView(pad);
        shell.addView(bar);

        // Tabs
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(dp(16), dp(12), dp(16), dp(4));
        tabWeek = tabButton("هفته");
        tabMonth = tabButton("ماه");
        tabWeek.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showingWeek = true;
                refreshTabs();
                renderContent();
            }
        });
        tabMonth.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showingWeek = false;
                refreshTabs();
                renderContent();
            }
        });
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, -2, 1f);
        tlp.setMargins(dp(4), 0, dp(4), 0);
        tabs.addView(tabWeek, tlp);
        tabs.addView(tabMonth, tlp);
        shell.addView(tabs);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        contentHost = new LinearLayout(this);
        contentHost.setOrientation(LinearLayout.VERTICAL);
        contentHost.setPadding(dp(16), dp(12), dp(16), dp(28));
        scroll.addView(contentHost);
        shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        setContentView(shell);
        refreshTabs();
        renderContent();
    }

    private void refreshTabs() {
        styleTab(tabWeek, showingWeek);
        styleTab(tabMonth, !showingWeek);
    }

    private TextView tabButton(String label) {
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(15f);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(12), dp(12), dp(12));
        t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private void styleTab(TextView t, boolean on) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(12));
        g.setColor(on ? TAB_ON : TAB_OFF);
        t.setBackground(g);
        t.setTextColor(on ? Color.WHITE : TEXT_DARK);
    }

    private void renderContent() {
        contentHost.removeAllViews();

        RaidoDataStore.WeeklyStats st = showingWeek
                ? RaidoDataStore.INSTANCE.computeWeekly(this)
                : RaidoDataStore.INSTANCE.computeMonthly(this);

        TextView sub = new TextView(this);
        sub.setText(showingWeek
                ? "۷ روز اخیر · فقط روی این گوشی"
                : "۳۰ روز اخیر · فقط روی این گوشی");
        sub.setTextSize(12f);
        sub.setTextColor(TEXT_MUTED);
        sub.setGravity(Gravity.RIGHT | Gravity.START);
        sub.setPadding(0, 0, 0, dp(12));
        contentHost.addView(sub);

        if (st.total == 0) {
            TextView empty = new TextView(this);
            empty.setText(showingWeek
                    ? "هنوز برای این هفته تحلیلی ثبت نشده.\nبعد از چند درخواست اسنپ، اینجا پر می‌شود."
                    : "هنوز برای این ماه تحلیلی ثبت نشده.\nبعد از چند درخواست اسنپ، اینجا پر می‌شود.");
            empty.setTextSize(14f);
            empty.setTextColor(TEXT_MUTED);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(24), 0, dp(24));
            contentHost.addView(empty);
        }

        // Card 1
        LinearLayout card1 = card();
        card1.setPadding(dp(16), dp(16), dp(16), dp(16));
        card1.addView(sectionTitle(showingWeek ? "خلاصه هفته" : "خلاصه ماه"));
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(statCell(String.valueOf(st.total), "تحلیل", TEXT_DARK), lpHalf());
        row1.addView(statCell(String.valueOf(st.green), "سبز", GREEN), lpHalf());
        card1.addView(row1);
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(0, dp(12), 0, 0);
        row2.addView(statCell(String.valueOf(st.yellow), "زرد", AMBER), lpHalf());
        row2.addView(statCell(String.valueOf(st.red), "قرمز", RED), lpHalf());
        card1.addView(row2);
        contentHost.addView(card1);
        space(contentHost, 12);

        // Card 2
        LinearLayout card2 = card();
        card2.setPadding(dp(16), dp(16), dp(16), dp(16));
        card2.addView(sectionTitle("سفرهای انجام‌شده"));
        LinearLayout rowA = new LinearLayout(this);
        rowA.setOrientation(LinearLayout.HORIZONTAL);
        rowA.setGravity(Gravity.CENTER_VERTICAL);
        TextView car = new TextView(this);
        car.setText("🚗");
        car.setTextSize(28f);
        car.setPadding(0, 0, dp(12), 0);
        rowA.addView(car);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        TextView gone = new TextView(this);
        gone.setText(st.accepted + " رفته");
        gone.setTextSize(14f);
        gone.setTextColor(TEXT_MUTED);
        gone.setGravity(Gravity.RIGHT | Gravity.START);
        col.addView(gone);
        TextView avgLabel = new TextView(this);
        avgLabel.setText("میانگین ساعتی");
        avgLabel.setTextSize(12f);
        avgLabel.setTextColor(TEXT_MUTED);
        avgLabel.setGravity(Gravity.RIGHT | Gravity.START);
        col.addView(avgLabel);
        TextView avg = new TextView(this);
        avg.setText(formatMoney(st.avgHourlyAccepted) + " تومان");
        avg.setTextSize(22f);
        avg.setTypeface(Typeface.DEFAULT_BOLD);
        avg.setTextColor(TEXT_DARK);
        avg.setGravity(Gravity.RIGHT | Gravity.START);
        col.addView(avg);
        rowA.addView(col);
        card2.addView(rowA);
        contentHost.addView(card2);
        space(contentHost, 12);

        // Card 3
        LinearLayout card3 = card();
        card3.setPadding(dp(16), dp(16), dp(16), dp(16));
        card3.addView(sectionTitle("گزارش اشتباه تحلیل"));
        LinearLayout rowW = new LinearLayout(this);
        rowW.setOrientation(LinearLayout.HORIZONTAL);
        rowW.setGravity(Gravity.CENTER_VERTICAL);
        TextView bang = new TextView(this);
        bang.setText("!");
        bang.setTextSize(22f);
        bang.setTextColor(AMBER);
        bang.setTypeface(Typeface.DEFAULT_BOLD);
        bang.setPadding(0, 0, dp(12), 0);
        rowW.addView(bang);
        LinearLayout colW = new LinearLayout(this);
        colW.setOrientation(LinearLayout.VERTICAL);
        colW.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        TextView wn = new TextView(this);
        wn.setText(String.valueOf(st.wrongReports));
        wn.setTextSize(28f);
        wn.setTypeface(Typeface.DEFAULT_BOLD);
        wn.setTextColor(TEXT_DARK);
        wn.setGravity(Gravity.RIGHT | Gravity.START);
        colW.addView(wn);
        TextView wh = new TextView(this);
        wh.setText(st.wrongReports + " مورد گزارش شده.\nاز منو «گزارش اشتباه تحلیل» جزئیات بفرستید.");
        wh.setTextSize(12f);
        wh.setTextColor(TEXT_MUTED);
        wh.setGravity(Gravity.RIGHT | Gravity.START);
        colW.addView(wh);
        rowW.addView(colW);
        card3.addView(rowW);
        contentHost.addView(card3);
        space(contentHost, 16);

        TextView foot = new TextView(this);
        foot.setText("🔒 داده‌ها روی گوشی شماست · سقف نگهداری ۵٬۰۰۰ تحلیل");
        foot.setTextSize(11f);
        foot.setTextColor(TEXT_MUTED);
        foot.setGravity(Gravity.CENTER);
        contentHost.addView(foot);

        try { Fonts.INSTANCE.applyRecursive(contentHost); } catch (Exception ignored) {}
    }

    private TextView sectionTitle(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(15f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(TEXT_DARK);
        t.setGravity(Gravity.RIGHT | Gravity.START);
        t.setPadding(0, 0, 0, dp(12));
        return t;
    }

    private LinearLayout statCell(String num, String label, int color) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(8), dp(8), dp(8), dp(8));
        TextView n = new TextView(this);
        n.setText(num);
        n.setTextSize(28f);
        n.setTypeface(Typeface.DEFAULT_BOLD);
        n.setTextColor(color);
        n.setGravity(Gravity.CENTER);
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextSize(13f);
        l.setTextColor(TEXT_MUTED);
        l.setGravity(Gravity.CENTER);
        c.addView(n);
        c.addView(l);
        return c;
    }

    private LinearLayout.LayoutParams lpHalf() {
        return new LinearLayout.LayoutParams(0, -2, 1f);
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(16));
        g.setStroke(dp(1), CARD_BORDER);
        c.setBackground(g);
        c.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        return c;
    }

    private void space(LinearLayout p, int d) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(d)));
        p.addView(v);
    }

    private String formatMoney(double v) {
        if (v <= 0) return "۰";
        try {
            NumberFormat nf = NumberFormat.getInstance(new Locale("fa", "IR"));
            nf.setMaximumFractionDigits(0);
            return nf.format(Math.round(v));
        } catch (Exception e) {
            return String.valueOf(Math.round(v));
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
