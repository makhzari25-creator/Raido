package ir.tripanalyzer.app;

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
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** جزئیات آخرین نتیجه + تب تحلیل‌های ۷ روز */
public final class DetailsActivity extends BaseActivity {

    private static final int TEXT_DARK = 0xFF0F172A;
    private static final int TEXT_MUTED = 0xFF64748B;
    private static final int CARD_BORDER = 0xFFE2E8F0;
    private static final int TEAL = 0xFF0B3D3A;

    private LinearLayout contentHost;
    private TextView tabLast, tabDays;
    private int currentTab = 0;
    private String expandedDayKeyStr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(0xFFF1F5F9);
        shell.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        shell.addView(buildHeader(), new LinearLayout.LayoutParams(-1, dp(150)));

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(dp(4), dp(4), dp(4), dp(4));
        GradientDrawable tabsBg = new GradientDrawable();
        tabsBg.setColor(Color.WHITE);
        tabsBg.setCornerRadius(dp(18));
        tabsBg.setStroke(dp(1), CARD_BORDER);
        tabs.setBackground(tabsBg);
        tabLast = tabBtn("آخرین نتیجه");
        tabDays = tabBtn("تحلیل‌های ۷ روز");
        tabLast.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { currentTab = 0; refreshTabs(); render(); }
        });
        tabDays.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { currentTab = 1; refreshTabs(); render(); }
        });
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, -2, 1f);
        tlp.setMargins(dp(4), 0, dp(4), 0);
        tabs.addView(tabLast, tlp);
        tabs.addView(tabDays, tlp);
        LinearLayout.LayoutParams tabsLp = new LinearLayout.LayoutParams(-1, -2);
        tabsLp.setMargins(dp(14), dp(12), dp(14), dp(4));
        shell.addView(tabs, tabsLp);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        contentHost = new LinearLayout(this);
        contentHost.setOrientation(LinearLayout.VERTICAL);
        contentHost.setPadding(dp(14), dp(10), dp(14), dp(32));
        scroll.addView(contentHost);
        shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        setContentView(shell);
        refreshTabs();
        render();
    }

    private TextView tabBtn(String label) {
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(14f);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(8), dp(11), dp(8), dp(11));
        t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private void refreshTabs() {
        styleTab(tabLast, currentTab == 0);
        styleTab(tabDays, currentTab == 1);
    }

    private void styleTab(TextView t, boolean on) {
        if (on) {
            GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{0xFF3B82F6, 0xFF2563EB});
            g.setCornerRadius(dp(14));
            t.setBackground(g);
            t.setTextColor(Color.WHITE);
        } else {
            t.setBackground(null);
            t.setTextColor(0xFF475569);
        }
    }

    private void render() {
        contentHost.removeAllViews();
        if (currentTab == 0) renderLast();
        else renderDays();
        try { Fonts.INSTANCE.applyRecursive(contentHost); } catch (Exception ignored) {}
    }

    private void renderLast() {
        TextView sub = new TextView(this);
        sub.setText("تحلیل آخرین سفر خوانده‌شده از اسنپ");
        sub.setTextSize(13f);
        sub.setTextColor(TEXT_MUTED);
        sub.setPadding(0, 0, 0, dp(12));
        contentHost.addView(sub);

        JSONObject last = null;
        try { last = Store.INSTANCE.last(this); } catch (Exception ignored) {}

        if (last == null) {
            LinearLayout empty = card();
            TextView t = new TextView(this);
            t.setText("هنوز نتیجه‌ای ثبت نشده.\nیک درخواست سفر در اسنپ راننده باز کنید تا تحلیل انجام شود.");
            t.setTextSize(14f);
            t.setTextColor(TEXT_MUTED);
            empty.addView(t);
            contentHost.addView(empty);
            return;
        }

        String verdict = last.optString("verdict", "—");
        int verdictColor = 0xFF64748B;
        String vLow = verdict.toLowerCase(Locale.US);
        if (verdict.contains("سبز") || vLow.contains("green")) verdictColor = 0xFF16A34A;
        else if (verdict.contains("قرمز") || vLow.contains("red")) verdictColor = 0xFFDC2626;
        else if (verdict.contains("زرد") || vLow.contains("yellow")) verdictColor = 0xFFCA8A04;

        LinearLayout head = card();
        try {
            GradientDrawable hb = new GradientDrawable();
            hb.setCornerRadius(dp(20));
            hb.setColor((verdictColor & 0x00FFFFFF) | 0x1A000000);
            hb.setStroke(dp(1), (verdictColor & 0x00FFFFFF) | 0x55000000);
            head.setBackground(hb);
        } catch (Throwable ignored) {}
        TextView vLabel = new TextView(this);
        vLabel.setText(verdictFa(verdict));
        vLabel.setTextSize(20f);
        vLabel.setTypeface(Typeface.DEFAULT_BOLD);
        vLabel.setTextColor(verdictColor);
        head.addView(vLabel);
        contentHost.addView(head);
        addSpace(contentHost, 10);

        LinearLayout money = card();
        money.addView(row("کرایه", money(last.optDouble("fare", 0))));
        double netVal = last.has("net") ? last.optDouble("net", 0) : last.optDouble("netAfter", 0);
        money.addView(row("سود خالص (بعد از سوخت و استهلاک)", money(netVal)));
        money.addView(row("سود ساعتی خالص", money(last.optDouble("hourly", 0))));
        money.addView(row("کارمزد + مالیات", money(last.optDouble("commission", 0))));
        money.addView(row("هزینه سوخت", money(last.optDouble("fuel", 0))));
        money.addView(row("استهلاک", money(last.optDouble("dep", 0))));
        contentHost.addView(money);
        addSpace(contentHost, 10);

        LinearLayout trip = card();
        double dist = last.optDouble("dist", 0);
        double rawTime = last.optDouble("rawTime", 0);
        String distLabel = "مسافت";
        try {
            String ds = last.optString("distSrc", "");
            if (ds.length() > 0) distLabel = "مسافت (" + ds + ")";
        } catch (Exception ignored) {}
        trip.addView(row(distLabel, String.format(Locale.US, "%.1f کیلومتر", dist)));
        trip.addView(row("زمان درخواست", String.format(Locale.US, "%.0f دقیقه", rawTime)));
        String origin = last.optString("origin", "");
        String dest = last.optString("dest", "");
        if (origin.length() > 0) trip.addView(row("مبدأ", origin));
        if (dest.length() > 0) trip.addView(row("مقصد", dest));

        try {
            if (LicenseStore.INSTANCE.isPremium(this)) {
                String geoStatus = last.optString("geoStatus", "");
                boolean geoOk = last.optBoolean("geoOk", false);
                boolean geoAttempted = last.optBoolean("geoAttempted", false);
                int geoHttp = last.optInt("geoHttp", 0);
                String geoApiStatus = last.optString("geoApiStatus", "");
                String geoError = last.optString("geoError", "");
                String geoEndpoint = last.optString("geoEndpoint", "");
                String zoneLabel = last.optString("zoneLabel", "");
                double destLat = last.optDouble("destLat", Double.NaN);
                double destLng = last.optDouble("destLng", Double.NaN);
                boolean duringTrip = last.optBoolean("duringTrip", false);

                String geoText;
                if ("skip_not_premium".equals(geoStatus)) {
                    geoText = "فقط برای پرمیوم";
                } else if (geoOk || "ok".equals(geoStatus)) {
                    geoText = "موفق ✓ — پاسخ از API نشان";
                    if (geoHttp > 0) geoText += " · HTTP " + geoHttp;
                    if (geoApiStatus.length() > 0) geoText += " · status=" + geoApiStatus;
                } else if ("fail_no_dest".equals(geoStatus)) {
                    geoText = "ناموفق — مقصد از اسنپ پارس نشد";
                } else if (!geoAttempted && geoStatus.length() == 0) {
                    geoText = "هنوز اجرا نشده";
                } else if (geoHttp == 0 && geoAttempted) {
                    geoText = "ناموفق — اتصال به نشان برقرار نشد";
                    if (geoError.length() > 0) geoText += " · " + geoError;
                } else if (geoHttp > 0) {
                    geoText = "ناموفق — HTTP " + geoHttp;
                    if (geoApiStatus.length() > 0) geoText += " · status=" + geoApiStatus;
                    if (geoError.length() > 0) geoText += " · " + geoError;
                } else if (geoError.length() > 0) {
                    geoText = "ناموفق — " + geoError;
                } else {
                    geoText = "ناموفق";
                }
                trip.addView(row("ژئوکد نشان", geoText));
                if (!Double.isNaN(destLat) && !Double.isNaN(destLng) && destLat != 0 && destLng != 0) {
                    String coord = String.format(java.util.Locale.US, "%.5f , %.5f", destLat, destLng);
                    trip.addView(row("مختصات مقصد (نشان)", coord));
                }
                if (zoneLabel.length() > 0) {
                    trip.addView(row("تطبیق محدوده", zoneLabel));
                }
                try {
                    String az = last.optString("activityZoneStatus", "");
                    if ("in".equals(az)) {
                        trip.addView(row("محدوده فعالیت", "داخل محدوده"));
                    } else if ("out".equals(az)) {
                        trip.addView(row("محدوده فعالیت", "خارج محدوده"));
                    } else if ("unknown".equals(az)) {
                        trip.addView(row("محدوده فعالیت", "نامشخص"));
                    }
                    if (last.has("activityZoneDistanceKm") && !last.isNull("activityZoneDistanceKm")) {
                        double d = last.optDouble("activityZoneDistanceKm", -1);
                        if (d >= 0) trip.addView(row("فاصله تا مرجع", String.format("%.1f کم", d)));
                    }
                } catch (Throwable ignored) {}
                if (duringTrip) {
                    trip.addView(row("نوع درخواست", "حین سفر · ثبت خودکار با پایان سفر نمی‌شود"));
                }
                if (geoEndpoint != null && geoEndpoint.length() > 0) {
                    trip.addView(row("endpoint نشان", geoEndpoint));
                }
            }
        } catch (Throwable ignored) {}

        boolean accepted = last.optBoolean("accepted", false);
        trip.addView(row("وضعیت ثبت", accepted ? "✓ به‌عنوان رفته ثبت شده" : "ثبت نشده"));
        try {
            if (last.has("payForm") && last.optString("payForm").length() > 0) {
                trip.addView(row("پرداخت", last.optString("payLabel", last.optString("payForm"))));
            }
            if (last.has("payCashToman") && !last.isNull("payCashToman")) {
                trip.addView(row("نقد دریافتی", String.format(Locale.US, "%,.0f تومان", last.optDouble("payCashToman"))));
            }
            if (last.has("payCreditToman") && !last.isNull("payCreditToman")) {
                trip.addView(row("اعتبار", String.format(Locale.US, "%,.0f تومان", last.optDouble("payCreditToman"))));
            }
            if (last.has("stopMin") && last.optInt("stopMin") > 0) {
                trip.addView(row("توقف", last.optInt("stopMin") + " دقیقه · " + last.optInt("stopFeeToman") + " تومان"));
            }
            if (last.has("tripKind") && last.optString("tripKind").length() > 0) {
                trip.addView(row("مدل سفر", last.optString("tripKind")));
            }
        } catch (Throwable ignored) {}
        contentHost.addView(trip);
        addSpace(contentHost, 12);

        if (!accepted) {
            Button went = new Button(this);
            went.setText("من این سفر رفتم");
            went.setTextSize(15f);
            went.setTextColor(Color.WHITE);
            styleSolid(went, 0xFF16A34A, 15);
            went.setPadding(dp(16), dp(14), dp(16), dp(14));
            final long ts = last.optLong("ts", 0);
            went.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    markWent(ts);
                    render();
                }
            });
            contentHost.addView(went);
            addSpace(contentHost, 10);
        }

        Button reportBtn = new Button(this);
        reportBtn.setText("گزارش اشتباه تحلیل");
        reportBtn.setTextSize(15f);
        reportBtn.setTextColor(Color.WHITE);
        styleSolid(reportBtn, 0xFFDC2626, 15);
        reportBtn.setPadding(dp(16), dp(14), dp(16), dp(14));
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                FeedbackHelper.reportWrongAnalysis(DetailsActivity.this);
            }
        });

        Button editTrip = new Button(this);
        editTrip.setText("ویرایش سفر / توقف / قیمت");
        editTrip.setTextSize(15f);
        editTrip.setTextColor(Color.WHITE);
        styleSolid(editTrip, 0xFF2563EB, 15);
        editTrip.setPadding(dp(16), dp(14), dp(16), dp(14));
        editTrip.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new android.content.Intent(DetailsActivity.this, TripEditActivity.class));
            }
        });
        contentHost.addView(editTrip);
        addSpace(contentHost, 10);
        contentHost.addView(reportBtn);
        TextView hint = new TextView(this);
        hint.setText("اگر زمان، مسافت یا تحلیل اشتباه بود، با این دکمه متن خام همان لحظه ذخیره می‌شود.");
        hint.setTextSize(12f);
        hint.setTextColor(TEXT_MUTED);
        hint.setPadding(0, dp(8), 0, 0);
        contentHost.addView(hint);
    }

    private void renderDays() {
        TextView sub = new TextView(this);
        sub.setText("هر روز تا ۷ روز نگه داشته می‌شود. با دیدن «پایان سفر» در اسنپ، همان سفر قبول‌شده ثبت می‌شود (نه درخواست حین سفر).");
        sub.setTextSize(13f);
        sub.setTextColor(TEXT_MUTED);
        sub.setPadding(0, 0, 0, dp(12));
        contentHost.addView(sub);

        JSONArray arr;
        try {
            arr = RaidoDataStore.INSTANCE.analysesLast7Days(this);
        } catch (Exception e) {
            arr = new JSONArray();
        }

        Map<String, List<JSONObject>> byDay = new LinkedHashMap<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            long ts = o.optLong("ts", 0);
            if (ts <= 0) continue;
            try {
                int[] j = Jalali.toJalali(ts);
                String key = j[0] + "-" + j[1] + "-" + j[2];
                if (!byDay.containsKey(key)) byDay.put(key, new ArrayList<JSONObject>());
                byDay.get(key).add(o);
            } catch (Exception ignored) {}
        }

        if (byDay.isEmpty()) {
            LinearLayout empty = card();
            TextView t = new TextView(this);
            t.setText("در ۷ روز اخیر تحلیلی ثبت نشده.");
            t.setTextSize(14f);
            t.setTextColor(TEXT_MUTED);
            empty.addView(t);
            contentHost.addView(empty);
            return;
        }

        String firstKey = null;
        for (String k : byDay.keySet()) {
            firstKey = k;
            break;
        }
        final String expandKey = (expandedDayKeyStr != null && expandedDayKeyStr.length() > 0)
                ? expandedDayKeyStr : firstKey;

        for (Map.Entry<String, List<JSONObject>> e : byDay.entrySet()) {
            final String dayKey = e.getKey();
            List<JSONObject> list = e.getValue();
            int[] parts;
            try {
                String[] sp = dayKey.split("-");
                parts = new int[]{Integer.parseInt(sp[0]), Integer.parseInt(sp[1]), Integer.parseInt(sp[2])};
            } catch (Exception ex) {
                parts = new int[]{0, 0, 0};
            }
            String dayLabel;
            try {
                dayLabel = Jalali.formatDateShort(parts[0], parts[1], parts[2]);
            } catch (Exception ex) {
                dayLabel = dayKey;
            }
            int wentCount = 0;
            for (JSONObject o : list) {
                if (o.optBoolean("accepted", false)) wentCount++;
            }

            LinearLayout dayHead = card();
            dayHead.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    expandedDayKeyStr = dayKey.equals(expandKey) ? "" : dayKey;
                    render();
                }
            });
            TextView dayTitle = new TextView(this);
            dayTitle.setText(dayLabel + " · " + list.size() + " تحلیل · " + wentCount + " رفته");
            dayTitle.setTextSize(15f);
            dayTitle.setTypeface(Typeface.DEFAULT_BOLD);
            dayTitle.setTextColor(TEXT_DARK);
            dayHead.addView(dayTitle);
            contentHost.addView(dayHead);
            addSpace(contentHost, 6);

            if (!dayKey.equals(expandKey)) continue;

            for (JSONObject o : list) {
                long ts = o.optLong("ts", 0);
                boolean acc = o.optBoolean("accepted", false);
                LinearLayout rowCard = card();
                String v = o.optString("verdict", "—");
                try {
                    GradientDrawable rb = new GradientDrawable();
                    rb.setCornerRadius(dp(18));
                    rb.setColor(Color.WHITE);
                    rb.setStroke(dp(2), (verdictColor(v) & 0x00FFFFFF) | 0x66000000);
                    rowCard.setBackground(rb);
                } catch (Throwable ignored) {}
                TextView line = new TextView(this);
                String fareS = o.has("fare") ? money(o.optDouble("fare", 0)) : "—";
                String hourlyS = o.has("hourly") ? money(o.optDouble("hourly", 0)) : "—";
                line.setText(verdictFa(v) + "\nکرایه " + fareS + " · ساعتی " + hourlyS);
                line.setTextSize(13f);
                line.setTextColor(TEXT_DARK);
                rowCard.addView(line);
                if (!acc) {
                    Button went = new Button(this);
                    went.setText("من این سفر رفتم");
                    went.setTextSize(13f);
                    went.setTextColor(Color.WHITE);
                    styleSolid(went, 0xFF16A34A, 13);
                    went.setPadding(dp(12), dp(10), dp(12), dp(10));
                    final long fts = ts;
                    went.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            markWent(fts);
                            render();
                        }
                    });
                    rowCard.addView(went);
                } else {
                    TextView ok = new TextView(this);
                    ok.setText("✓ ثبت‌شده به‌عنوان رفته");
                    ok.setTextSize(12f);
                    ok.setTextColor(0xFF16A34A);
                    ok.setPadding(0, dp(6), 0, 0);
                    rowCard.addView(ok);
                }
                contentHost.addView(rowCard);
                addSpace(contentHost, 6);
            }
        }
    }

    private void markWent(long ts) {
        if (ts <= 0) {
            Toast.makeText(this, "شناسه تحلیل نامعتبر", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            RaidoDataStore.INSTANCE.markAccepted(this, ts);
            JSONObject last = Store.INSTANCE.last(this);
            if (last != null && last.optLong("ts", -1) == ts) {
                Store.INSTANCE.saveAccepted(this, last);
                last.put("accepted", true);
                last.put("acceptedAt", System.currentTimeMillis());
                Store.INSTANCE.saveLast(this, last);
            } else {
                JSONObject row = RaidoDataStore.INSTANCE.findAnalysisByTs(this, ts);
                if (row != null) {
                    row.put("accepted", true);
                    row.put("acceptedAt", System.currentTimeMillis());
                    if (!row.has("net") && row.has("netAfter")) {
                        try { row.put("net", row.optDouble("netAfter")); } catch (Exception ignored) {}
                    }
                    Store.INSTANCE.saveAccepted(this, row);
                }
            }
            Toast.makeText(this, "به‌عنوان سفر رفته ثبت شد", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "خطا در ثبت", Toast.LENGTH_SHORT).show();
        }
    }

    private String verdictFa(String v) {
        if (v == null) return "—";
        String l = v.toLowerCase(Locale.US);
        if (l.contains("green") || v.contains("سبز")) return "تحلیل سبز · به‌صرفه";
        if (l.contains("red") || v.contains("قرمز")) return "تحلیل قرمز · به‌صرفه نیست";
        if (l.contains("yellow") || v.contains("زرد")) return "تحلیل زرد · مرزی";
        if (l.contains("gray") || v.contains("خاکستری")) return "خاکستری";
        return v;
    }

    private int verdictColor(String v) {
        if (v == null) return TEXT_MUTED;
        String l = v.toLowerCase(Locale.US);
        if (l.contains("green") || v.contains("سبز")) return 0xFF16A34A;
        if (l.contains("red") || v.contains("قرمز")) return 0xFFDC2626;
        if (l.contains("yellow") || v.contains("زرد")) return 0xFFCA8A04;
        return TEXT_MUTED;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14), dp(14), dp(14), dp(14));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(20));
        bg.setStroke(dp(1), CARD_BORDER);
        c.setBackground(bg);
        try { c.setElevation(dp(1)); } catch (Throwable ignored) {}
        return c;
    }

    private LinearLayout row(String k, String v) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(0, dp(9), 0, dp(9));
        TextView kt = new TextView(this);
        kt.setText(k);
        kt.setTextSize(12.5f);
        kt.setTextColor(TEXT_MUTED);
        LinearLayout.LayoutParams klp = new LinearLayout.LayoutParams(0, -2, 1f);
        klp.setMarginEnd(dp(10));
        r.addView(kt, klp);
        TextView vt = new TextView(this);
        vt.setText(v);
        vt.setTextSize(13.5f);
        vt.setTypeface(Typeface.DEFAULT_BOLD);
        vt.setTextColor(TEXT_DARK);
        vt.setGravity(Gravity.END);
        r.addView(vt, new LinearLayout.LayoutParams(0, -2, 1.25f));
        r.setBackground(new RowDivider());
        return r;
    }

    /** خط جداکنندهٔ نازک پایین هر ردیف */
    private final class RowDivider extends android.graphics.drawable.Drawable {
        private final android.graphics.Paint p = new android.graphics.Paint();
        RowDivider() { p.setColor(0xFFEEF2F7); }
        @Override public void draw(android.graphics.Canvas c) {
            android.graphics.Rect b = getBounds();
            c.drawRect(b.left, b.bottom - Math.max(1, dp(1)), b.right, b.bottom, p);
        }
        @Override public void setAlpha(int a) {}
        @Override public void setColorFilter(android.graphics.ColorFilter f) {}
        @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
    }

    private void styleSolid(Button b, int color, int textSizeSp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(14));
        b.setBackground(g);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(textSizeSp);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setGravity(Gravity.CENTER);
    }

    /** هدر: تصویر جاده + گرادیان آبی رایدو + بازگشت + عنوان */
    private View buildHeader() {
        android.widget.FrameLayout head = new android.widget.FrameLayout(this);
        android.widget.ImageView road = new android.widget.ImageView(this);
        road.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        try { road.setImageResource(R.drawable.bg_road); } catch (Throwable ignored) {}
        head.addView(road, new android.widget.FrameLayout.LayoutParams(-1, -1));
        View shade = new View(this);
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x990F172A, 0xE61E3A8A}));
        head.addView(shade, new android.widget.FrameLayout.LayoutParams(-1, -1));
        LinearLayout in = new LinearLayout(this);
        in.setOrientation(LinearLayout.VERTICAL);
        in.setPadding(dp(16), dp(14), dp(16), dp(14));
        TextView back = new TextView(this);
        back.setText("→");
        back.setTextSize(20f);
        back.setTextColor(Color.WHITE);
        back.setGravity(Gravity.CENTER);
        GradientDrawable bb = new GradientDrawable();
        bb.setShape(GradientDrawable.OVAL);
        bb.setColor(0x33FFFFFF);
        bb.setStroke(dp(1), 0x55FFFFFF);
        back.setBackground(bb);
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        in.addView(back, new LinearLayout.LayoutParams(dp(40), dp(40)));
        TextView title = new TextView(this);
        title.setText("جزئیات و تحلیل‌ها");
        title.setTextSize(22f);
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(12), 0, 0);
        in.addView(title);
        TextView sub = new TextView(this);
        sub.setText("آخرین نتیجه و تاریخچهٔ تحلیل‌های ۷ روز اخیر");
        sub.setTextSize(13f);
        sub.setTextColor(0xE6FFFFFF);
        sub.setPadding(0, dp(4), 0, 0);
        in.addView(sub);
        head.addView(in, new android.widget.FrameLayout.LayoutParams(-1, -2));
        return head;
    }

    private String money(double v) {
        return String.format(Locale.US, "%,.0f تومان", v);
    }

    private void addSpace(LinearLayout parent, int d) {
        View s = new View(this);
        s.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(d)));
        parent.addView(s);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
