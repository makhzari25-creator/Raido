package ir.tripanalyzer.app;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** آمار و گزارش‌ها — تب هفته | ماه | گزارش‌ها */
public final class ReportsActivity extends BaseActivity {

    private static final int TEXT_DARK = 0xFF0F172A;
    private static final int TEXT_MUTED = 0xFF64748B;
    private static final int CARD_BORDER = 0xFFE2E8F0;
    private static final int TEAL = 0xFF0F3D3A;
    private static final int RED = 0xFFDC2626;
    private static final int GREEN = 0xFF16A34A;
    private static final int AMBER = 0xFFD97706;
    private static final int TAB_ON = 0xFF0F3D3A;
    private static final int TAB_OFF = 0xFFE2E8F0;

    private LinearLayout contentHost;
    public static final String EXTRA_TAB = "open_tab";
    private TextView tabWeek, tabMonth, tabHourly, tabReports;
    private int currentTab = 0;
    private Integer drillJy, drillJm, drillJd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(0xFFF1F5F9);
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
        title.setText("آمار و گزارش‌ها");
        title.setTextSize(18f);
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        title.setGravity(Gravity.CENTER);
        try { Fonts.INSTANCE.apply(title, true); } catch (Exception ignored) {}
        bar.addView(title);
        View pad = new View(this);
        pad.setLayoutParams(new LinearLayout.LayoutParams(dp(48), 1));
        bar.addView(pad);
        shell.addView(bar);

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(dp(12), dp(12), dp(12), dp(4));
        tabWeek = tabButton("هفته");
        tabMonth = tabButton("ماه");
        tabHourly = tabButton("میانگین ساعتی");
        tabReports = tabButton("گزارش‌ها");
        tabWeek.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { currentTab = 0; drillJy = null; refreshTabs(); render(); }
        });
        tabMonth.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { currentTab = 1; drillJy = null; refreshTabs(); render(); }
        });
        tabHourly.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { currentTab = 2; drillJy = null; refreshTabs(); render(); }
        });
        tabReports.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { currentTab = 3; drillJy = null; refreshTabs(); render(); }
        });
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, -2, 1f);
        tlp.setMargins(dp(2), 0, dp(2), 0);
        tabs.addView(tabWeek, tlp);
        tabs.addView(tabMonth, tlp);
        tabs.addView(tabHourly, tlp);
        tabs.addView(tabReports, tlp);
        shell.addView(tabs);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        contentHost = new LinearLayout(this);
        contentHost.setOrientation(LinearLayout.VERTICAL);
        contentHost.setPadding(dp(16), dp(12), dp(16), dp(32));
        scroll.addView(contentHost);
        shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        setContentView(shell);
        int open = getIntent() != null ? getIntent().getIntExtra(EXTRA_TAB, 0) : 0;
        if (open < 0 || open > 3) open = 0;
        currentTab = open;
        refreshTabs();
        render();
    }

    private TextView tabButton(String label) {
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(14f);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(8), dp(12), dp(8), dp(12));
        t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private void refreshTabs() {
        styleTab(tabWeek, currentTab == 0);
        styleTab(tabMonth, currentTab == 1);
        styleTab(tabHourly, currentTab == 2);
        styleTab(tabReports, currentTab == 3);
    }

    private void styleTab(TextView t, boolean on) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(12));
        g.setColor(on ? TAB_ON : TAB_OFF);
        t.setBackground(g);
        t.setTextColor(on ? Color.WHITE : TEXT_DARK);
    }

    private void render() {
        contentHost.removeAllViews();
        if (currentTab == 2) renderHourly();
        else if (currentTab == 3) renderReports();
        else renderStats(currentTab == 0);
        try { Fonts.INSTANCE.applyRecursive(contentHost); } catch (Exception ignored) {}
    }

    private void renderHourly() {
        int[] now = Jalali.nowJalali();
        TextView sub = new TextView(this);
        sub.setText("ماه جاری شمسی (" + Jalali.monthName(now[1]) + " " + now[0] + ") · فقط روی این گوشی\nراهنما: درباره و پشتیبانی ← میانگین ساعتی");
        sub.setTextSize(12f);
        sub.setTextColor(TEXT_MUTED);
        sub.setPadding(0, 0, 0, dp(12));
        contentHost.addView(sub);

        if (drillJy != null && drillJm != null && drillJd != null) {
            renderHourlyDay(drillJy, drillJm, drillJd);
            return;
        }

        java.util.List<RaidoDataStore.DaySummary> days = RaidoDataStore.INSTANCE.listDaysInCurrentMonth(this);
        if (days.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("هنوز در این ماه تحلیلی با نرخ ساعتی ثبت نشده.\nبعد از چند درخواست، روزها اینجا ظاهر می‌شوند.");
            empty.setTextSize(14f);
            empty.setTextColor(TEXT_MUTED);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(32), 0, dp(32));
            contentHost.addView(empty);
            return;
        }
        for (final RaidoDataStore.DaySummary ds : days) {
            LinearLayout row = whiteCard();
            row.setPadding(dp(14), dp(14), dp(14), dp(14));
            row.setOrientation(LinearLayout.VERTICAL);
            TextView title = new TextView(this);
            title.setText(ds.label);
            title.setTextSize(15f);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setTextColor(TEXT_DARK);
            row.addView(title);
            TextView meta = new TextView(this);
            meta.setText(ds.count + " تحلیل · میانگین روز " + formatMoney(ds.avg) + " تومان");
            meta.setTextSize(12f);
            meta.setTextColor(TEXT_MUTED);
            meta.setPadding(0, dp(4), 0, 0);
            row.addView(meta);
            row.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    drillJy = ds.jy; drillJm = ds.jm; drillJd = ds.jd;
                    render();
                }
            });
            contentHost.addView(row);
            space(contentHost, 8);
        }
    }

    private void renderHourlyDay(int jy, int jm, int jd) {
        TextView back = new TextView(this);
        back.setText("← بازگشت به لیست روزها");
        back.setTextSize(14f);
        back.setTextColor(0xFF2563EB);
        back.setPadding(0, 0, 0, dp(12));
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                drillJy = null; drillJm = null; drillJd = null;
                render();
            }
        });
        contentHost.addView(back);

        TextView head = new TextView(this);
        head.setText("جزئیات ساعتی · " + Jalali.formatDateShort(jy, jm, jd));
        head.setTextSize(16f);
        head.setTypeface(Typeface.DEFAULT_BOLD);
        head.setTextColor(TEXT_DARK);
        head.setPadding(0, 0, 0, dp(12));
        contentHost.addView(head);

        // نمودار ۲۴ ساعته
        try {
            float[] avgs = RaidoDataStore.INSTANCE.hourAvgsArray(this, jy, jm, jd);
            LinearLayout barCard = whiteCard();
            barCard.setOrientation(LinearLayout.VERTICAL);
            barCard.setPadding(dp(12), dp(12), dp(12), dp(12));
            TextView bt = new TextView(this);
            bt.setText("پروفایل ساعتی روز");
            bt.setTextSize(12f); bt.setTypeface(Typeface.DEFAULT_BOLD); bt.setTextColor(TEXT_MUTED);
            barCard.addView(bt);
            barCard.addView(ChartKit.verticalBars(this, avgs, 120, true));
            TextView ax = new TextView(this);
            ax.setText("۰۰        ۰۶        ۱۲        ۱۸        ۲۴");
            ax.setTextSize(10f); ax.setTextColor(TEXT_MUTED); ax.setPadding(0, dp(4), 0, 0);
            barCard.addView(ax);
            contentHost.addView(barCard);
            space(contentHost, 10);

            // مقایسه بازار vs — میانگین روز
            double daySum = 0; int dayN = 0;
            for (float v : avgs) { if (v > 0) { daySum += v; dayN++; } }
            RaidoDataStore.MarketRef mr = RaidoDataStore.INSTANCE.marketRefNow(this);
            LinearLayout cmp = whiteCard();
            cmp.setOrientation(LinearLayout.VERTICAL);
            cmp.setPadding(dp(14), dp(14), dp(14), dp(14));
            TextView ct = new TextView(this);
            ct.setText("بازار این ساعت vs میانگین این روز");
            ct.setTextSize(12f); ct.setTypeface(Typeface.DEFAULT_BOLD); ct.setTextColor(TEXT_MUTED);
            cmp.addView(ct);
            float mVal = mr.isValid() ? (float) mr.avg : 0f;
            float dVal = dayN > 0 ? (float) (daySum / dayN) : 0f;
            float maxR = Math.max(mVal, dVal);
            if (maxR < 1) maxR = 1;
            cmp.addView(ChartKit.hBarRow(this, "بازار", mVal, maxR, 0xFFFCD34D, 0xFFD97706));
            cmp.addView(ChartKit.hBarRow(this, "این روز", dVal, maxR, 0xFF34D399, 0xFF059669));
            contentHost.addView(cmp);
            space(contentHost, 10);
        } catch (Throwable ignored) {}

        RaidoDataStore.HourBucket[] buckets = RaidoDataStore.INSTANCE.hoursForDay(this, jy, jm, jd);
        LinearLayout card = whiteCard();
        card.setPadding(dp(12), dp(8), dp(12), dp(8));
        card.setOrientation(LinearLayout.VERTICAL);

        // هدر ستون‌ها
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(0, dp(4), 0, dp(10));
        header.addView(colHeader("زمان", 0.30f));
        header.addView(colHeader("میانگین", 0.45f));
        header.addView(colHeader("تعداد درخواست", 0.25f));
        card.addView(header);

        for (RaidoDataStore.HourBucket b : buckets) {
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setPadding(0, dp(8), 0, dp(8));
            TextView h = new TextView(this);
            // LTR تا ۰۱–۰۰ برعکس نشود
            h.setText("\u200E" + b.rangeLabel());
            h.setTextDirection(View.TEXT_DIRECTION_LTR);
            h.setTextSize(13f);
            h.setTextColor(TEXT_MUTED);
            h.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 0.30f));
            TextView avg = new TextView(this);
            if (b.count > 0) {
                avg.setText(formatMoney(b.avg) + " تومان");
                avg.setTextColor(TEXT_DARK);
                avg.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                avg.setText("بدون تحلیل");
                avg.setTextColor(TEXT_MUTED);
            }
            avg.setTextSize(13f);
            avg.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 0.45f));
            TextView cnt = new TextView(this);
            cnt.setText(b.count > 0 ? String.valueOf(b.count) : "—");
            cnt.setTextSize(12f);
            cnt.setTextColor(TEXT_MUTED);
            cnt.setGravity(Gravity.CENTER);
            cnt.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 0.25f));
            line.addView(h);
            line.addView(avg);
            line.addView(cnt);
            card.addView(line);
        }
        contentHost.addView(card);
        space(contentHost, 12);
        TextView tip = new TextView(this);
        tip.setText("این میانگین از همه درخواست‌های تحلیل‌شده (سبز/زرد/قرمز) در همان ساعت است و با پایان ماه شمسی برای ماه جدید از نو جمع می‌شود.");
        tip.setTextSize(11f);
        tip.setTextColor(TEXT_MUTED);
        contentHost.addView(tip);
    }

    private void renderStats(boolean week) {
        RaidoDataStore.WeeklyStats st = week
                ? RaidoDataStore.INSTANCE.computeWeekly(this)
                : RaidoDataStore.INSTANCE.computeMonthly(this);

        TextView sub = new TextView(this);
        sub.setText(week ? "۷ روز اخیر · فقط روی این گوشی" : "۳۰ روز اخیر · فقط روی این گوشی");
        sub.setTextSize(12f);
        sub.setTextColor(TEXT_MUTED);
        sub.setPadding(0, 0, 0, dp(12));
        contentHost.addView(sub);

        if (st.total == 0) {
            TextView empty = new TextView(this);
            empty.setText(week
                    ? "هنوز برای این هفته تحلیلی ثبت نشده.\nبعد از چند درخواست اسنپ، اینجا پر می‌شود."
                    : "هنوز برای این ماه تحلیلی ثبت نشده.\nبعد از چند درخواست اسنپ، اینجا پر می‌شود.");
            empty.setTextSize(14f);
            empty.setTextColor(TEXT_MUTED);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(24), 0, dp(24));
            contentHost.addView(empty);
        }

        LinearLayout card1 = whiteCard();
        card1.setPadding(dp(16), dp(16), dp(16), dp(16));
        card1.addView(sectionTitle(week ? "خلاصه هفته" : "خلاصه ماه"));
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

        // دونات تحلیل
        try {
            LinearLayout donutCard = whiteCard();
            donutCard.setPadding(dp(16), dp(16), dp(16), dp(16));
            donutCard.setOrientation(LinearLayout.VERTICAL);
            donutCard.addView(sectionTitle("ترکیب تحلیل"));
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(ChartKit.donut(this, st.green, st.yellow, st.red, 110));
            LinearLayout leg = new LinearLayout(this);
            leg.setOrientation(LinearLayout.VERTICAL);
            leg.setPadding(dp(16), 0, 0, 0);
            int total = Math.max(1, st.total);
            TextView lg = new TextView(this);
            lg.setText("سبز  " + st.green + "  (" + (st.green * 100 / total) + "٪)");
            lg.setTextSize(13f); lg.setTextColor(0xFF059669); lg.setTypeface(Typeface.DEFAULT_BOLD);
            leg.addView(lg);
            TextView ly = new TextView(this);
            ly.setText("زرد  " + st.yellow);
            ly.setTextSize(13f); ly.setTextColor(0xFFD97706); ly.setPadding(0, dp(6), 0, 0);
            leg.addView(ly);
            TextView lr = new TextView(this);
            lr.setText("قرمز  " + st.red);
            lr.setTextSize(13f); lr.setTextColor(0xFFDC2626); lr.setPadding(0, dp(6), 0, 0);
            leg.addView(lr);
            row.addView(leg);
            donutCard.addView(row);
            contentHost.addView(donutCard);
            space(contentHost, 12);
        } catch (Throwable ignored) {}

        // هیت‌مپ فقط برای هفته
        if (week) {
            try {
                LinearLayout heatCard = whiteCard();
                heatCard.setPadding(dp(16), dp(16), dp(16), dp(16));
                heatCard.setOrientation(LinearLayout.VERTICAL);
                heatCard.addView(sectionTitle("شدت کار · ۷ روز"));
                int[] counts = RaidoDataStore.INSTANCE.last7DayCounts(this);
                String[] labs = new String[]{"ش","ی","د","س","چ","پ","ج"};
                // map array idx 0=oldest to weekday labels approximate
                heatCard.addView(ChartKit.heatWeek(this, counts, labs));
                TextView hm = new TextView(this);
                hm.setText("پررنگ‌تر = تحلیل بیشتر");
                hm.setTextSize(11f); hm.setTextColor(TEXT_MUTED); hm.setPadding(0, dp(8), 0, 0);
                heatCard.addView(hm);
                contentHost.addView(heatCard);
                space(contentHost, 12);
            } catch (Throwable ignored) {}
        }

        LinearLayout card2 = whiteCard();
        card2.setPadding(dp(16), dp(16), dp(16), dp(16));
        card2.addView(sectionTitle("سفرهای انجام‌شده"));
        TextView gone = new TextView(this);
        gone.setText(st.accepted + " رفته");
        gone.setTextSize(14f);
        gone.setTextColor(TEXT_MUTED);
        card2.addView(gone);
        TextView avgLabel = new TextView(this);
        avgLabel.setText("میانگین ساعتی");
        avgLabel.setTextSize(12f);
        avgLabel.setTextColor(TEXT_MUTED);
        avgLabel.setPadding(0, dp(8), 0, 0);
        card2.addView(avgLabel);
        TextView avg = new TextView(this);
        avg.setText(formatMoney(st.avgHourlyAccepted) + " تومان");
        avg.setTextSize(22f);
        avg.setTypeface(Typeface.DEFAULT_BOLD);
        avg.setTextColor(TEXT_DARK);
        card2.addView(avg);
        contentHost.addView(card2);
        space(contentHost, 12);

        LinearLayout card3 = whiteCard();
        card3.setPadding(dp(16), dp(16), dp(16), dp(16));
        card3.addView(sectionTitle("گزارش اشتباه تحلیل"));
        TextView wn = new TextView(this);
        wn.setText(String.valueOf(st.wrongReports));
        wn.setTextSize(28f);
        wn.setTypeface(Typeface.DEFAULT_BOLD);
        wn.setTextColor(TEXT_DARK);
        card3.addView(wn);
        TextView wh = new TextView(this);
        wh.setText(st.wrongReports + " مورد گزارش شده.\nاز «جزئیات آخرین نتیجه» می‌توانید گزارش بفرستید.");
        wh.setTextSize(12f);
        wh.setTextColor(TEXT_MUTED);
        card3.addView(wh);
        contentHost.addView(card3);
        space(contentHost, 16);

        TextView foot = new TextView(this);
        foot.setText("داده‌ها روی گوشی شماست · سقف نگهداری ۵۰۰۰ تحلیل");
        foot.setTextSize(11f);
        foot.setTextColor(TEXT_MUTED);
        foot.setGravity(Gravity.CENTER);
        contentHost.addView(foot);
    }

    private void renderReports() {
        TextView sub = new TextView(this);
        sub.setText("تاریخچه سفرهای پذیرفته‌شده · برای جزئیات روی هر سفر بزنید");
        sub.setTextSize(13f);
        sub.setTextColor(TEXT_MUTED);
        sub.setPadding(0, 0, 0, dp(12));
        contentHost.addView(sub);

        LinearLayout cards = new LinearLayout(this);
        cards.setOrientation(LinearLayout.HORIZONTAL);

        double fuelSum = 0, depSum = 0, netSum = 0;
        JSONArray arr;
        try {
            arr = new JSONArray(Store.INSTANCE.prefs(this).getString("recent", "[]"));
        } catch (Exception e) {
            arr = new JSONArray();
        }
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject o = arr.getJSONObject(i);
                netSum += o.optDouble("net", 0);
                depSum += o.optDouble("dep", 0);
                fuelSum += o.optDouble("fuel", 0);
            } catch (Exception ignored) {}
        }

        cards.addView(metricCard("سوخت", formatMoney(fuelSum), 0xFFEFF6FF, 0xFF2563EB,
                "هزینه بنزین", "هزینه بنزینی که برای سفرها هزینه شده"), cardLp());
        cards.addView(metricCard("استهلاک", formatMoney(depSum), 0xFFFFFBEB, 0xFFD97706,
                "استهلاک", "هزینه تعمیرات و تعویض‌های آینده ماشین (همین الان کنار بگذار)"), cardLp());
        cards.addView(metricCard("درآمد خالص",
                (netSum < 0 ? "-" : "") + formatMoney(Math.abs(netSum)),
                0xFFECFDF5, netSum < 0 ? RED : GREEN,
                "درآمد خالص", "پولی که تو جیبت حسش می‌کنی"), cardLp());
        contentHost.addView(cards);
        space(contentHost, 12);

        try {
            LinearLayout share = whiteCard();
            share.setOrientation(LinearLayout.VERTICAL);
            share.setPadding(dp(14), dp(14), dp(14), dp(14));
            TextView st = new TextView(this);
            st.setText("سهم هزینه از درآمد (هزار تومان)");
            st.setTextSize(12f); st.setTypeface(Typeface.DEFAULT_BOLD); st.setTextColor(TEXT_MUTED);
            share.addView(st);
            float maxR = (float) Math.max(Math.abs(netSum), Math.max(fuelSum, depSum));
            if (maxR < 1) maxR = 1;
            share.addView(ChartKit.hBarRow(this, "بنزین", (float) fuelSum, maxR, 0xFFFCD34D, 0xFFD97706));
            share.addView(ChartKit.hBarRow(this, "استهلاک", (float) depSum, maxR, 0xFFFCA5A5, 0xFFDC2626));
            share.addView(ChartKit.hBarRow(this, "خالص", (float) Math.abs(netSum), maxR, 0xFF34D399, 0xFF059669));
            contentHost.addView(share);
            space(contentHost, 12);
        } catch (Throwable ignored) {}

        if (arr.length() == 0) {
            TextView empty = new TextView(this);
            empty.setText("هنوز گزارشی ذخیره نشده");
            empty.setTextSize(14f);
            empty.setTextColor(TEXT_MUTED);
            empty.setPadding(0, dp(24), 0, 0);
            empty.setGravity(Gravity.CENTER);
            contentHost.addView(empty);
            return;
        }

        for (int i = arr.length() - 1; i >= 0; i--) {
            try {
                final JSONObject o = arr.getJSONObject(i);
                double net = o.optDouble("net", 0);
                String verdict = o.optString("verdict", "");
                String origin = o.optString("origin", "");
                String dest = o.optString("dest", "");
                final long ts = o.optLong("ts", 0);

                LinearLayout row = whiteCard();
                row.setPadding(dp(14), dp(12), dp(14), dp(12));
                row.setOrientation(LinearLayout.VERTICAL);

                LinearLayout top = new LinearLayout(this);
                top.setOrientation(LinearLayout.HORIZONTAL);
                top.setGravity(Gravity.CENTER_VERTICAL);

                TextView badge = new TextView(this);
                String verdShow = verdict;
                if ("GREEN".equalsIgnoreCase(verdict) || "سبز".equals(verdict)) verdShow = "تحلیل سبز";
                else if ("RED".equalsIgnoreCase(verdict) || "قرمز".equals(verdict)) verdShow = "تحلیل قرمز";
                else if ("YELLOW".equalsIgnoreCase(verdict) || "زرد".equals(verdict)) verdShow = "تحلیل زرد";
                String az = o.optString("activityZoneStatus", "");
                if ("in".equals(az)) verdShow += " · داخل محدوده";
                else if ("out".equals(az)) verdShow += " · خارج محدوده";
                else if ("unknown".equals(az)) verdShow += " · محدوده نامشخص";
                badge.setText(verdShow.isEmpty() ? "-" : verdShow);
                badge.setTextSize(11f);
                badge.setTypeface(Typeface.DEFAULT_BOLD);
                badge.setPadding(dp(10), dp(4), dp(10), dp(4));
                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(dp(20));
                if ("GREEN".equalsIgnoreCase(verdict)) {
                    bg.setColor(0xFFECFDF5); badge.setTextColor(GREEN);
                } else if ("RED".equalsIgnoreCase(verdict)) {
                    bg.setColor(0xFFFEF2F2); badge.setTextColor(RED);
                } else {
                    bg.setColor(0xFFFFFBEB); badge.setTextColor(AMBER);
                }
                badge.setBackground(bg);
                top.addView(badge);

                View spacer = new View(this);
                spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
                top.addView(spacer);

                TextView del = new TextView(this);
                del.setText("حذف");
                del.setTextSize(13f);
                del.setTextColor(RED);
                del.setTypeface(Typeface.DEFAULT_BOLD);
                del.setPadding(dp(8), dp(4), dp(4), dp(4));
                del.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Store.INSTANCE.removeAcceptedByTs(ReportsActivity.this, ts);
                        Toast.makeText(ReportsActivity.this, "حذف شد", Toast.LENGTH_SHORT).show();
                        render();
                    }
                });
                top.addView(del);
                row.addView(top);

                TextView netTv = new TextView(this);
                netTv.setText((net < 0 ? "-" : "") + formatMoney(Math.abs(net)) + " تومان");
                netTv.setTextSize(17f);
                netTv.setTypeface(Typeface.DEFAULT_BOLD);
                netTv.setTextColor(net < 0 ? RED : GREEN);
                netTv.setPadding(0, dp(8), 0, dp(4));
                row.addView(netTv);

                if (origin.length() > 0 || dest.length() > 0) {
                    TextView meta = new TextView(this);
                    StringBuilder msb = new StringBuilder();
                    if (origin.length() > 0) msb.append("مبدأ: ").append(origin);
                    if (dest.length() > 0) {
                        if (msb.length() > 0) msb.append("\n");
                        msb.append("مقصد: ").append(dest);
                    }
                    meta.setText(msb.toString());
                    meta.setTextSize(12f);
                    meta.setTextColor(TEXT_MUTED);
                    row.addView(meta);
                }

                TextView hint = new TextView(this);
                hint.setText("برای جزئیات کامل ضربه بزنید");
                hint.setTextSize(11f);
                hint.setTextColor(0xFF94A3B8);
                hint.setPadding(0, dp(6), 0, 0);
                row.addView(hint);

                row.setClickable(true);
                row.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { showTripDetail(o); }
                });

                contentHost.addView(row);
                space(contentHost, 10);
            } catch (Exception ignored) {}
        }
    }

    private void showTripDetail(JSONObject o) {
        if (o == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("تحلیل: ").append(o.optString("verdict", "-")).append("\n\n");
        if (o.has("fare") && !o.isNull("fare"))
            sb.append("کرایه: ").append(formatMoney(o.optDouble("fare"))).append(" تومان\n");
        if (o.has("net") && !o.isNull("net"))
            sb.append("سود خالص: ").append(formatMoney(o.optDouble("net"))).append(" تومان\n");
        if (o.has("hourly") && !o.isNull("hourly"))
            sb.append("سود ساعتی: ").append(formatMoney(o.optDouble("hourly"))).append(" تومان\n");
        if (o.has("commission") && !o.isNull("commission"))
            sb.append("کارمزد: ").append(formatMoney(o.optDouble("commission"))).append(" تومان\n");
        if (o.has("fuel") && !o.isNull("fuel"))
            sb.append("سوخت: ").append(formatMoney(o.optDouble("fuel"))).append(" تومان\n");
        if (o.has("dep") && !o.isNull("dep"))
            sb.append("استهلاک: ").append(formatMoney(o.optDouble("dep"))).append(" تومان\n");
        double dist = o.optDouble("dist", o.optDouble("distance", 0));
        if (dist > 0) sb.append("مسافت: ").append(String.format(Locale.US, "%.1f", dist)).append(" کیلومتر\n");
        double rawTime = o.optDouble("rawTime", o.optDouble("time", 0));
        double adjusted = o.optDouble("adjusted", 0);
        if (rawTime > 0) sb.append("زمان خام درخواست: ").append(String.format(Locale.US, "%.0f", rawTime)).append(" دقیقه\n");
        if (adjusted > 0) sb.append("تخمین مجموع زمان: ").append(String.format(Locale.US, "%.0f", adjusted)).append(" دقیقه\n");
        String origin = o.optString("origin", "");
        String dest = o.optString("dest", "");
        if (origin.length() > 0) sb.append("مبدأ: ").append(origin).append("\n");
        if (dest.length() > 0) sb.append("مقصد: ").append(dest).append("\n");
        long ts = o.optLong("ts", 0);
        if (ts > 0) {
            try {
                sb.append("\nزمان ثبت: ").append(new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(new Date(ts)));
            } catch (Exception ignored) {}
        }
        new AlertDialog.Builder(this)
                .setTitle("جزئیات سفر")
                .setMessage(sb.toString().trim())
                .setPositiveButton("باشه", null)
                .show();
    }

    private LinearLayout metricCard(String label, String value, int bgColor, int accent,
                                   final String tipTitle, final String tipMsg) {
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable g = new GradientDrawable();
        g.setColor(bgColor);
        g.setCornerRadius(dp(14));
        g.setStroke(dp(1), CARD_BORDER);
        outer.setBackground(g);
        outer.setPadding(dp(10), dp(12), dp(10), dp(12));
        outer.setGravity(Gravity.CENTER);
        TextView num = new TextView(this);
        num.setText(value);
        num.setTextSize(14f);
        num.setTypeface(Typeface.DEFAULT_BOLD);
        num.setTextColor(accent);
        num.setGravity(Gravity.CENTER);
        outer.addView(num);
        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextSize(11f);
        lbl.setTextColor(TEXT_MUTED);
        lbl.setGravity(Gravity.CENTER);
        lbl.setPadding(0, dp(4), 0, 0);
        outer.addView(lbl);
        outer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                new AlertDialog.Builder(ReportsActivity.this)
                        .setTitle(tipTitle).setMessage(tipMsg)
                        .setPositiveButton("باشه", null).show();
            }
        });
        return outer;
    }

    private LinearLayout.LayoutParams cardLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
        lp.setMargins(dp(4), 0, dp(4), 0);
        return lp;
    }
    private LinearLayout.LayoutParams lpHalf() {
        return new LinearLayout.LayoutParams(0, -2, 1f);
    }
    private TextView sectionTitle(String s) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(15f); t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(TEXT_DARK); t.setPadding(0, 0, 0, dp(12));
        return t;
    }
    private LinearLayout statCell(String num, String label, int color) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL); c.setGravity(Gravity.CENTER);
        c.setPadding(dp(8), dp(8), dp(8), dp(8));
        TextView n = new TextView(this); n.setText(num); n.setTextSize(28f);
        n.setTypeface(Typeface.DEFAULT_BOLD); n.setTextColor(color); n.setGravity(Gravity.CENTER);
        TextView l = new TextView(this); l.setText(label); l.setTextSize(13f);
        l.setTextColor(TEXT_MUTED); l.setGravity(Gravity.CENTER);
        c.addView(n); c.addView(l); return c;
    }
    private LinearLayout whiteCard() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE); g.setCornerRadius(dp(14)); g.setStroke(dp(1), CARD_BORDER);
        c.setBackground(g); c.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        return c;
    }
    private void space(LinearLayout p, int d) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(d)));
        p.addView(v);
    }
    private String formatMoney(double v) {
        try {
            NumberFormat nf = NumberFormat.getInstance(new Locale("fa", "IR"));
            nf.setMaximumFractionDigits(0);
            return nf.format(Math.round(v));
        } catch (Exception e) { return String.valueOf(Math.round(v)); }
    }
    private TextView colHeader(String title, float weight) {
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(11f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(TEXT_MUTED);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, -2, weight));
        return t;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
