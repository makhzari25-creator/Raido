package ir.tripanalyzer.app;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

/**
 * ویرایش دستی سفر فعال / آخرین تحلیل — قیمت، توقف، نوع، کیلومتر/زمان → محاسبه مجدد.
 */
public final class TripEditActivity extends BaseActivity {

    private EditText fareInput, stopInput, pKmInput, tKmInput, pMinInput, tMinInput;
    private Spinner kindSpinner;
    private JSONObject target;
    private boolean editingActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        target = Store.INSTANCE.getActiveTrip(this);
        editingActive = target != null;
        if (target == null) target = Store.INSTANCE.last(this);
        if (target == null) {
            Toast.makeText(this, "سفری برای ویرایش نیست", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(32));
        root.setBackgroundColor(0xE60B1220);

        TextView title = new TextView(this);
        title.setText(editingActive ? "ویرایش سفر فعال" : "ویرایش آخرین تحلیل");
        title.setTextColor(0xFFF8FAFC);
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("توقف، مقصد دوم یا قیمت جدید را دستی وارد کنید؛ سپس محاسبه مجدد.");
        sub.setTextColor(0xFF94A3B8);
        sub.setTextSize(12.5f);
        sub.setPadding(0, dp(6), 0, dp(16));
        root.addView(sub);

        LinearLayout card = card();
        card.addView(label("قیمت نهایی (تومان)"));
        fareInput = field(String.valueOf((long) target.optDouble("fare", 0)));
        card.addView(fareInput);

        card.addView(label("توقف (دقیقه) — هر دقیقه " + MidTripEventParser.STOP_FEE_PER_MIN + " تومان"));
        stopInput = field(target.has("stopMin") ? String.valueOf(target.optInt("stopMin")) : "0");
        card.addView(stopInput);

        card.addView(label("نوع سفر"));
        kindSpinner = new Spinner(this);
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"ONE_DEST", "TWO_DEST", "ROUND_TRIP"});
        kindSpinner.setAdapter(ad);
        String tk = target.optString("tripKind", "ONE_DEST");
        if ("TWO_DEST".equals(tk)) kindSpinner.setSelection(1);
        else if ("ROUND_TRIP".equals(tk)) kindSpinner.setSelection(2);
        else kindSpinner.setSelection(0);
        card.addView(kindSpinner);

        card.addView(label("کیلومتر تا مبدأ"));
        pKmInput = field(fmt(target.optDouble("pickupKm", target.optDouble("pKm", 0))));
        card.addView(pKmInput);
        card.addView(label("کیلومتر سفر/مقصد"));
        tKmInput = field(fmt(target.optDouble("tripKm", target.optDouble("tKm", 0))));
        card.addView(tKmInput);
        card.addView(label("دقیقه تا مبدأ"));
        pMinInput = field(fmt(target.optDouble("pickupMin", target.optDouble("pMin", 0))));
        card.addView(pMinInput);
        card.addView(label("دقیقه سفر"));
        tMinInput = field(fmt(target.optDouble("tripMin", target.optDouble("tMin", 0))));
        card.addView(tMinInput);
        root.addView(card);

        Button save = new Button(this);
        save.setText("محاسبه مجدد و ذخیره");
        save.setAllCaps(false);
        save.setTextColor(Color.WHITE);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF0F766E);
        bg.setCornerRadius(dp(14));
        save.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        lp.topMargin = dp(12);
        save.setLayoutParams(lp);
        save.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { saveRecalc(); }
        });
        root.addView(save);

        scroll.addView(root);
        setContentView(scroll);
    }

    private void saveRecalc() {
        try {
            double fare = parseD(fareInput.getText().toString());
            int stopMin = (int) parseD(stopInput.getText().toString());
            if (stopMin < 0) stopMin = 0;
            int stopFee = stopMin * MidTripEventParser.STOP_FEE_PER_MIN;
            // اگر کاربر قیمت را دستی کامل وارد کرده، stop را جداگانه به fare اضافه نکن مگر fare بدون stop باشد
            double fareTotal = fare;
            Double pKm = nz(parseD(pKmInput.getText().toString()));
            Double tKm = nz(parseD(tKmInput.getText().toString()));
            Double pMin = nz(parseD(pMinInput.getText().toString()));
            Double tMin = nz(parseD(tMinInput.getText().toString()));
            String kind = (String) kindSpinner.getSelectedItem();

            if ("ROUND_TRIP".equals(kind) && tKm != null && tKm > 0) {
                tKm = 2.0 * tKm;
            }
            if ((tKm == null || tKm <= 0) && tMin != null && tMin > 0) {
                tKm = SnappLegParser.minToKm(tMin);
            }
            if ((pKm == null || pKm <= 0) && pMin != null && pMin > 0) {
                pKm = SnappLegParser.minToKm(pMin);
            }

            EngineSettings es = Store.INSTANCE.settings(this);
            TripResult tr = TripEngine.INSTANCE.analyze(
                    new RawTrip(fareTotal, pKm, tKm, pMin, tMin), es, this);

            target.put("fare", fareTotal);
            target.put("stopMin", stopMin);
            target.put("stopFeeToman", stopFee);
            target.put("tripKind", kind);
            if (pKm != null) target.put("pickupKm", pKm);
            if (tKm != null) target.put("tripKm", tKm);
            if (pMin != null) target.put("pickupMin", pMin);
            if (tMin != null) target.put("tripMin", tMin);
            target.put("verdict", tr.getVerdict() != null ? tr.getVerdict().name() : "GRAY");
            if (tr.getHourlyIncome() != null) target.put("hourly", tr.getHourlyIncome());
            target.put("manualEdit", true);
            target.put("manualEditAt", System.currentTimeMillis());

            if (editingActive) {
                Store.INSTANCE.saveActiveTrip(this, target);
            }
            Store.INSTANCE.saveLast(this, target);
            // اگر قبلاً accepted بوده، accepted را هم با اعداد جدید به‌روز کن
            if (target.optBoolean("accepted", false)) {
                try { Store.INSTANCE.saveAccepted(this, target); } catch (Throwable ignored) {}
            }
            Toast.makeText(this, "ذخیره و محاسبه مجدد انجام شد", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "خطا در ذخیره", Toast.LENGTH_SHORT).show();
        }
    }

    private static double parseD(String s) {
        try {
            return Double.parseDouble(s.trim().replace(",", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private static Double nz(double v) {
        return v > 0 ? v : null;
    }

    private static String fmt(double v) {
        if (v <= 0) return "0";
        if (Math.abs(v - Math.round(v)) < 0.05) return String.valueOf(Math.round(v));
        return String.valueOf(v);
    }

    private EditText field(String v) {
        EditText e = new EditText(this);
        e.setText(v);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        e.setTextColor(0xFF0F172A);
        e.setHintTextColor(0xFF94A3B8);
        return e;
    }

    private TextView label(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(0xFF64748B);
        t.setTextSize(12f);
        t.setPadding(0, dp(10), 0, dp(4));
        return t;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14), dp(14), dp(14), dp(14));
        GradientDrawable g = new GradientDrawable();
        g.setColor(0xFFFFFFFF);
        g.setCornerRadius(dp(16));
        g.setStroke(dp(1), 0xFFE2E8F0);
        c.setBackground(g);
        return c;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
