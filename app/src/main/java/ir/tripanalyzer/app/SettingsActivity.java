package ir.tripanalyzer.app;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Bitmap;
import android.widget.ImageView;
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

/** تنظیمات RAIDO — ظاهر تجاری */
public final class SettingsActivity extends BaseActivity {

    private static final int REQ_PICK_PHOTO = 4402;
    private ImageView avatarPreview;
    private EditText nameInput;

    private static final int PRIMARY = 0xFF2563EB;
    private static final int TEXT_DARK = 0xFF0F172A;
    private static final int TEXT_MUTED = 0xFF64748B;
    private static final int CARD_BORDER = 0xFFE2E8F0;

    private Spinner brandSpinner;
    private EditText mileageInput;
    private EditText consumptionInput;
    private EditText fuelPriceInput;
    private EditText depInput;
    private EditText commissionInput;
    private EditText targetInput;
    private EditText overlaySecondsInput;
    private EditText floorInput;
    private EditText speedInput;
    private EditText bufferInput;
    private EditText trafficSpeedInput;

    private static final String[] BRANDS = {
            "انتخاب برند",
            "پراید", "تیبا", "ساینا", "کوییک", "سهند", "اطلس",
            "پژو 405", "پژو 206", "پژو 207", "پژو پارس", "سمند", "سورن", "رانا",
            "دنا", "تارا", "شاهین", "ال 90", "ساندرو",
            "جک", "جک S3", "جک S5", "چری", "MVM X22", "چانگان CS35",
            "تویوتا", "هیوندای", "کیا", "آریو", "هایما S5/S7", "سایر"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(0xFFF1F5F9);
        shell.addView(buildHeader(), new LinearLayout.LayoutParams(-1, dp(176)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(14), dp(14), dp(28));

        // محدوده فعالیت
        LinearLayout zoneCard = card();
        zoneCard.addView(sectionTitle("محدوده فعالیت"));
        TextView zoneSum = new TextView(this);
        zoneSum.setText(ActivityZoneStore.INSTANCE.summary(this));
        zoneSum.setTextColor(TEXT_MUTED);
        zoneSum.setTextSize(12.5f);
        zoneCard.addView(zoneSum);
        Button zoneBtn = new Button(this);
        zoneBtn.setText("تنظیم محدوده فعالیت");
        zoneBtn.setAllCaps(false);
        zoneBtn.setTextColor(Color.WHITE);
        zoneBtn.setBackground(rounded(PRIMARY, PRIMARY));
        zoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ActivityZoneActivity.class));
            }
        });
        zoneCard.addView(zoneBtn);
        TextView zoneHint = new TextView(this);
        zoneHint.setText("روشن = تحلیل با زون شما؛ خاموش = محدوده شهری قبلی.");
        zoneHint.setTextColor(TEXT_MUTED);
        zoneHint.setTextSize(11.5f);
        zoneHint.setPadding(0, dp(6), 0, 0);
        zoneCard.addView(zoneHint);
        root.addView(group(R.drawable.ic_mn_zone, 0xFFDBEAFE, "محدوده فعالیت", "نقشه، شعاع و محدودیت منطقه", zoneCard, false));

        // پروفایل
        LinearLayout profileCard = card();
        profileCard.addView(sectionTitle("پروفایل"));
        profileCard.addView(fieldLabel("نام نمایشی"));
        nameInput = input(ProfileStore.INSTANCE.hasCustomName(this)
                ? ProfileStore.INSTANCE.getDisplayName(this) : "", InputType.TYPE_CLASS_TEXT);
        nameInput.setHint("خالی = کاربر رایدو");
        profileCard.addView(nameInput);
        addSpace(profileCard, 12);
        avatarPreview = new ImageView(this);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(dp(72), dp(72));
        avatarPreview.setLayoutParams(alp);
        avatarPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Bitmap ph = ProfileStore.INSTANCE.loadPhotoBitmap(this);
        if (ph != null) avatarPreview.setImageBitmap(ph);
        else { try { avatarPreview.setImageResource(R.mipmap.ic_launcher); } catch (Exception ignored) {} }
        profileCard.addView(avatarPreview);
        addSpace(profileCard, 8);
        Button photoBtn = primaryBtn("تغییر عکس پروفایل");
        photoBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.setType("image/*");
                    startActivityForResult(Intent.createChooser(i, "عکس"), REQ_PICK_PHOTO);
                } catch (Exception e) {
                    Toast.makeText(SettingsActivity.this, "انتخاب عکس ممکن نشد", Toast.LENGTH_SHORT).show();
                }
            }
        });
        profileCard.addView(photoBtn);
        root.addView(group(R.drawable.ic_st_user, 0xFFE0E7FF, "پروفایل", "نام نمایشی و عکس", profileCard, false));


        SharedPreferences prefs = Store.INSTANCE.prefs(this);

        // Vehicle card
        LinearLayout vehicle = card();
        vehicle.addView(sectionTitle("خودرو"));
        vehicle.addView(fieldLabel("برند"));
        brandSpinner = new Spinner(this);
        brandSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, BRANDS));
        styleField(brandSpinner);
        String family = prefs.getString("vehicle_family", "");
        for (int i = 0; i < BRANDS.length; i++) {
            if (BRANDS[i].equals(family)) {
                brandSpinner.setSelection(i);
                break;
            }
        }
        vehicle.addView(brandSpinner);
        addSpace(vehicle, 12);
        vehicle.addView(fieldLabel("کارکرد (کیلومتر)"));
        mileageInput = input(String.valueOf(prefs.getInt("vehicle_mileage", 0)), InputType.TYPE_CLASS_NUMBER);
        vehicle.addView(mileageInput);
        Button depBtn = primaryBtn("محاسبه استهلاک از جدول");
        depBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calcDep();
            }
        });
        LinearLayout.LayoutParams depLp = new LinearLayout.LayoutParams(-1, -2);
        depLp.topMargin = dp(12);
        depBtn.setLayoutParams(depLp);
        vehicle.addView(depBtn);
        root.addView(group(R.drawable.ic_st_car, 0xFFDBEAFE, "خودرو", "برند، کارکرد و جدول استهلاک", vehicle, false));

        // Fuel card
        LinearLayout fuel = card();
        fuel.addView(sectionTitle("سوخت و استهلاک"));
        fuel.addView(fieldLabel("مصرف (لیتر در ۱۰۰ کیلومتر)"));
        consumptionInput = input(str(prefs.getFloat("fuel_per_100", 7.5f)), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        fuel.addView(consumptionInput);
        addSpace(fuel, 10);
        fuel.addView(fieldLabel("قیمت سوخت (تومان)"));
        fuelPriceInput = input(str(prefs.getFloat("fuel_price", 15000f)), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        fuel.addView(fuelPriceInput);
        addSpace(fuel, 10);
        fuel.addView(fieldLabel("استهلاک هر کیلومتر (تومان)"));
        depInput = input(str(prefs.getFloat("dep_per_km", 2100f)), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        fuel.addView(depInput);
        addSpace(fuel, 10);
        fuel.addView(fieldLabel("کارمزد اسنپ (درصد)"));
        commissionInput = input(str(prefs.getFloat("commission", 22f)), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        fuel.addView(commissionInput);
        root.addView(group(R.drawable.ic_st_fuel, 0xFFFEF3C7, "سوخت و استهلاک", "مصرف، قیمت سوخت، استهلاک و کارمزد", fuel, false));

        // کف تحلیلی
        LinearLayout floorCard = card();
        floorCard.addView(sectionTitle("کف درآمد تحلیلی"));
        floorCard.addView(fieldLabel("کف تحلیلی (تومان در ساعت)"));
        floorInput = input(str((long) Store.INSTANCE.getAnalysisFloor(this)), InputType.TYPE_CLASS_NUMBER);
        floorCard.addView(floorInput);
        TextView floorHint = new TextView(this);
        floorHint.setText("کف تحلیلی آپ = درآمد ساعتی یک خانواده ۳ نفره + هزینه بنزین + هزینه استهلاک.\nزیر این مبلغ → قرمز | تا ۱٫۲ برابر → زرد | بالاتر → سبز (پررنگ‌تر نزدیک هدف ایده‌آل).");
        floorHint.setTextSize(12f);
        floorHint.setTextColor(0xFF64748B);
        floorHint.setPadding(0, (int)(8*getResources().getDisplayMetrics().density), 0, 0);
        floorCard.addView(floorHint);
        root.addView(group(R.drawable.ic_st_flag, 0xFFDCFCE7, "کف درآمد تحلیلی", "زیر این مبلغ قرمز می‌شود", floorCard, false));

        // هدف ایده‌آل
        LinearLayout target = card();
        target.addView(sectionTitle("هدف درآمد"));
        target.addView(fieldLabel("درآمد ساعتی ایده‌آل (تومان)"));
        targetInput = input(str(prefs.getFloat("driver_gross_hourly", 320000f)), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        target.addView(targetInput);
        TextView targetHint = new TextView(this);
        targetHint.setText("هرچه درآمد ساعتی بعد کمیسیون به این عدد نزدیک‌تر باشد، کارت سبز پررنگ‌تر می‌شود.");
        targetHint.setTextSize(12f);
        targetHint.setTextColor(0xFF64748B);
        targetHint.setPadding(0, (int)(8*getResources().getDisplayMetrics().density), 0, 0);
        target.addView(targetHint);
        root.addView(group(R.drawable.ic_st_flag, 0xFFDCFCE7, "هدف درآمد", "درآمد ساعتی ایده‌آل", target, false));

        LinearLayout speedNote = card();
        speedNote.addView(sectionTitle("تخمین مسافت"));
        TextView note = new TextView(this);
        note.setText("اگر کیلومتر در درخواست اسنپ باشد از همان استفاده می‌شود. اگر نباشد، طبق بررسی‌ها راننده در هر ساعت حدود ۲۵ تا ۳۵ کیلومتر راه می‌رود؛ میانگین ۳۰ برای تخمین به‌کار می‌رود.");
        note.setTextSize(13f);
        note.setTextColor(0xFF64748B);
        speedNote.addView(note);
        root.addView(group(R.drawable.ic_st_gear, 0xFFE2E8F0, "تخمین مسافت", "وقتی کیلومتر در درخواست نیست", speedNote, false));

        // مدت نمایش کارت تحلیل روی اسنپ
        LinearLayout overlayCard = card();
        overlayCard.addView(sectionTitle("نمایش کارت تحلیل"));
        overlayCard.addView(fieldLabel("مدت نمایش روی صفحه (ثانیه)"));
        int ovSec = 6;
        try { ovSec = prefs.getInt("overlay_card_seconds", 6); } catch (Throwable ignored) {}
        if (ovSec < 3) ovSec = 3;
        if (ovSec > 15) ovSec = 15;
        overlaySecondsInput = input(String.valueOf(ovSec), InputType.TYPE_CLASS_NUMBER);
        overlaySecondsInput.setHint("۳ تا ۱۵ — پیش‌فرض ۶");
        overlayCard.addView(overlaySecondsInput);
        TextView ovHint = new TextView(this);
        ovHint.setText("کارت تحلیل بعد از این مدت خودکار بسته می‌شود. لمس صفحه زودتر می‌بندد.");
        ovHint.setTextSize(12f);
        ovHint.setTextColor(0xFF64748B);
        ovHint.setPadding(0, (int)(8*getResources().getDisplayMetrics().density), 0, 0);
        overlayCard.addView(ovHint);
        root.addView(group(R.drawable.ic_st_eye, 0xFFE0F2FE, "نمایش کارت تحلیل", "مدت نمایش روی صفحهٔ اسنپ", overlayCard, false));
        addSpace(root, 6);

        Button save = primaryBtn("ذخیره تنظیمات");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSave();
            }
        });
        root.addView(save);
        addSpace(root, 10);

        Button reset = outlineBtn("بازگردانی به پیش‌فرض");
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doReset();
            }
        });
        root.addView(reset);

        scroll.addView(root);
        shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(shell);
    }

    /** هدر: تصویر جاده + گرادیان آبی رایدو + دکمهٔ بازگشت */
    private View buildHeader() {
        android.widget.FrameLayout head = new android.widget.FrameLayout(this);
        ImageView road = new ImageView(this);
        road.setScaleType(ImageView.ScaleType.CENTER_CROP);
        try { road.setImageResource(R.drawable.bg_road); } catch (Throwable ignored) {}
        head.addView(road, new android.widget.FrameLayout.LayoutParams(-1, -1));
        View shade = new View(this);
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x990F172A, 0xE61E3A8A}));
        head.addView(shade, new android.widget.FrameLayout.LayoutParams(-1, -1));
        LinearLayout in = new LinearLayout(this);
        in.setOrientation(LinearLayout.VERTICAL);
        in.setPadding(dp(18), dp(16), dp(18), dp(16));
        TextView back = new TextView(this);
        back.setText("→");
        back.setTextColor(Color.WHITE);
        back.setTextSize(20f);
        back.setGravity(android.view.Gravity.CENTER);
        GradientDrawable bb = new GradientDrawable();
        bb.setShape(GradientDrawable.OVAL);
        bb.setColor(0x33FFFFFF);
        bb.setStroke(dp(1), 0x55FFFFFF);
        back.setBackground(bb);
        back.setOnClickListener(v -> finish());
        in.addView(back, new LinearLayout.LayoutParams(dp(40), dp(40)));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(14), 0, 0);
        ImageView g = new ImageView(this);
        g.setImageResource(R.drawable.ic_st_gear);
        g.setColorFilter(0xFF2563EB);
        g.setPadding(dp(9), dp(9), dp(9), dp(9));
        GradientDrawable gc = new GradientDrawable();
        gc.setShape(GradientDrawable.OVAL);
        gc.setColor(Color.WHITE);
        g.setBackground(gc);
        row.addView(g, new LinearLayout.LayoutParams(dp(42), dp(42)));
        TextView t = new TextView(this);
        t.setText("تنظیمات");
        t.setTextColor(Color.WHITE);
        t.setTextSize(24f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPaddingRelative(dp(12), 0, 0, 0);
        row.addView(t);
        in.addView(row);
        TextView sub = new TextView(this);
        sub.setText("خودرو، سوخت، کف تحلیلی، هدف ایده‌آل و مدت کارت");
        sub.setTextColor(0xE6FFFFFF);
        sub.setTextSize(13f);
        sub.setPadding(0, dp(8), 0, 0);
        in.addView(sub);
        head.addView(in, new android.widget.FrameLayout.LayoutParams(-1, -2));
        return head;
    }

    /** گروه تاشونده: آیکون + عنوان + توضیح + فلش؛ محتوای اصلی فیلدها بدون تغییر داخلش است. */
    private View group(int icon, int tint, String title, String subtitle, final LinearLayout body, boolean open) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(20));
        bg.setStroke(dp(1), CARD_BORDER);
        box.setBackground(bg);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, -2);
        blp.bottomMargin = dp(12);
        box.setLayoutParams(blp);
        try { box.setElevation(dp(1)); } catch (Throwable ignored) {}

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(android.view.Gravity.CENTER_VERTICAL);
        head.setPadding(dp(14), dp(14), dp(14), dp(14));
        ImageView ic = new ImageView(this);
        ic.setImageResource(icon);
        ic.setColorFilter(PRIMARY);
        ic.setPadding(dp(11), dp(11), dp(11), dp(11));
        GradientDrawable ig = new GradientDrawable();
        ig.setCornerRadius(dp(14));
        ig.setColor(tint);
        ic.setBackground(ig);
        head.addView(ic, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPaddingRelative(dp(12), 0, dp(8), 0);
        TextView tt = new TextView(this);
        tt.setText(title);
        tt.setTextSize(15.5f);
        tt.setTypeface(Typeface.DEFAULT_BOLD);
        tt.setTextColor(TEXT_DARK);
        TextView st = new TextView(this);
        st.setText(subtitle);
        st.setTextSize(12f);
        st.setTextColor(TEXT_MUTED);
        st.setPadding(0, dp(2), 0, 0);
        col.addView(tt);
        col.addView(st);
        head.addView(col, new LinearLayout.LayoutParams(0, -2, 1f));
        final ImageView chev = new ImageView(this);
        chev.setImageResource(R.drawable.ic_mn_chev);
        chev.setColorFilter(0xFF94A3B8);
        head.addView(chev, new LinearLayout.LayoutParams(dp(24), dp(24)));
        box.addView(head);

        body.setBackground(null);
        body.setPadding(dp(14), 0, dp(14), dp(14));
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(-1, -2);
        body.setLayoutParams(bodyLp);
        body.setVisibility(open ? View.VISIBLE : View.GONE);
        chev.setRotation(open ? -90f : 0f);
        box.addView(body);
        head.setOnClickListener(v -> {
            boolean show = body.getVisibility() != View.VISIBLE;
            body.setVisibility(show ? View.VISIBLE : View.GONE);
            chev.setRotation(show ? -90f : 0f);
        });
        return box;
    }

    private void calcDep() {
        String brand = "سایر";
        if (brandSpinner != null && brandSpinner.getSelectedItemPosition() > 0) {
            brand = BRANDS[brandSpinner.getSelectedItemPosition()];
        }
        int mileage = parseInt(mileageInput, 0);
        try {
            double dep = DepLookup.INSTANCE.lookup(this, brand, mileage);
            if (depInput != null) depInput.setText(String.valueOf((long) dep));
            Toast.makeText(this, "استهلاک محاسبه شد", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "خطا در محاسبه استهلاک", Toast.LENGTH_SHORT).show();
        }
    }

    private void doSave() {
        String brand = "سایر";
        if (brandSpinner != null && brandSpinner.getSelectedItemPosition() > 0) {
            brand = BRANDS[brandSpinner.getSelectedItemPosition()];
        }
        int mileage = parseInt(mileageInput, 0);
        Store.INSTANCE.setVehicleInfo(this, brand, mileage);

        SharedPreferences.Editor edit = Store.INSTANCE.prefs(this).edit();
        edit.putFloat("fuel_per_100", parseFloat(consumptionInput, 7.5f));
        edit.putFloat("fuel_price", parseFloat(fuelPriceInput, 15000f));
        edit.putFloat("dep_per_km", parseFloat(depInput, 2100f));
        edit.putFloat("commission", parseFloat(commissionInput, 22f));
        edit.apply(); // تا کف پویا با مقادیر جدید سوخت/استهلاک حساب شود

        float defFloor = Store.INSTANCE.computeDefaultAnalysisFloor(this);
        float floor = parseFloat(floorInput, defFloor);
        if (floor < 50000f) floor = 50000f;
        if (Math.abs(floor - defFloor) < 1000f) {
            Store.INSTANCE.clearAnalysisFloorOverride(this);
        } else {
            Store.INSTANCE.setAnalysisFloor(this, floor);
        }

        edit = Store.INSTANCE.prefs(this).edit();
        float target = parseFloat(targetInput, 320000f);
        if (target < floor) {
            Toast.makeText(this,
                    "درآمد ایده‌آل از کف تحلیلی کمتر است؛ بهتر است هدف بالاتر از کف باشد.",
                    Toast.LENGTH_LONG).show();
        }
        edit.putFloat("driver_gross_hourly", target);
        edit.putFloat("target", target);
        edit.putFloat("speed", 30f);
        edit.putFloat("traffic_speed", 30f);
        edit.putFloat("buffer", 0f);
        int ov = parseInt(overlaySecondsInput, 6);
        if (ov < 3) ov = 3;
        if (ov > 15) ov = 15;
        edit.putInt("overlay_card_seconds", ov);
        edit.apply();
        if (nameInput != null) {
            ProfileStore.INSTANCE.setDisplayName(this, nameInput.getText() != null ? nameInput.getText().toString() : "");
        }
        Toast.makeText(this, "تنظیمات ذخیره شد ✓", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void doReset() {
        if (consumptionInput != null) consumptionInput.setText("7.5");
        if (fuelPriceInput != null) fuelPriceInput.setText("15000");
        if (depInput != null) depInput.setText("2100");
        if (commissionInput != null) commissionInput.setText("22");
        if (consumptionInput != null) { /* already set above */ }
        Store.INSTANCE.clearAnalysisFloorOverride(this);
        if (floorInput != null) floorInput.setText(String.valueOf((long) Store.INSTANCE.getAnalysisFloor(this)));
        if (targetInput != null) targetInput.setText("320000");
        if (overlaySecondsInput != null) overlaySecondsInput.setText("6");
        Toast.makeText(this, "مقادیر پیش‌فرض اعمال شد", Toast.LENGTH_SHORT).show();
    }

    // UI helpers
    private TextView pageTitle(String t) {
        TextView v = new TextView(this);
        v.setText(t);
        v.setTextSize(22f);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setTextColor(TEXT_DARK);
        return v;
    }

    private TextView pageSub(String t) {
        TextView v = new TextView(this);
        v.setText(t);
        v.setTextSize(13f);
        v.setTextColor(TEXT_MUTED);
        v.setPadding(0, dp(4), 0, dp(16));
        return v;
    }

    private TextView sectionTitle(String t) {
        TextView v = new TextView(this);
        v.setText(t);
        v.setTextSize(15f);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setTextColor(TEXT_DARK);
        v.setPadding(0, 0, 0, dp(10));
        v.setVisibility(View.GONE);
        return v;
    }

    private TextView fieldLabel(String t) {
        TextView v = new TextView(this);
        v.setText(t);
        v.setTextSize(12f);
        v.setTextColor(TEXT_MUTED);
        v.setPadding(0, 0, 0, dp(4));
        return v;
    }

    private EditText input(String value, int type) {
        EditText e = new EditText(this);
        e.setText(value);
        e.setInputType(type);
        e.setTextSize(15f);
        e.setTextColor(TEXT_DARK);
        e.setBackground(rounded(0xFFF8FAFC, CARD_BORDER));
        e.setPadding(dp(14), dp(12), dp(14), dp(12));
        e.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        return e;
    }

    private void styleField(View v) {
        v.setBackground(rounded(Color.WHITE, CARD_BORDER));
        v.setPadding(dp(10), dp(8), dp(10), dp(8));
        v.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        c.setBackground(rounded(Color.WHITE, CARD_BORDER));
        c.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        return c;
    }

    private GradientDrawable rounded(int fill, int stroke) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(14));
        g.setColor(fill);
        g.setStroke(dp(1), stroke);
        return g;
    }

    private Button primaryBtn(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15f);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{0xFF3B82F6, 0xFF2563EB});
        g.setCornerRadius(dp(14));
        b.setBackground(g);
        b.setGravity(android.view.Gravity.CENTER);
        b.setPadding(dp(12), dp(12), dp(12), dp(12));
        return b;
    }

    private Button outlineBtn(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(TEXT_MUTED);
        b.setTextSize(14f);
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(12));
        g.setColor(Color.WHITE);
        g.setStroke(dp(1), CARD_BORDER);
        b.setBackground(g);
        b.setPadding(dp(12), dp(12), dp(12), dp(12));
        return b;
    }

    private void addSpace(LinearLayout parent, int d) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(d)));
        parent.addView(v);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (ProfileStore.INSTANCE.savePhotoFromUri(this, data.getData())) {
                Bitmap b = ProfileStore.INSTANCE.loadPhotoBitmap(this);
                if (b != null && avatarPreview != null) avatarPreview.setImageBitmap(b);
                Toast.makeText(this, "عکس ذخیره شد", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private String str(float f) {
        if (f == (long) f) return String.valueOf((long) f);
        return String.valueOf(f);
    }

    private int parseInt(EditText e, int def) {
        if (e == null) return def;
        try { return Integer.parseInt(e.getText().toString().trim()); } catch (Exception ex) { return def; }
    }

    private float parseFloat(EditText e, float def) {
        if (e == null) return def;
        try { return Float.parseFloat(e.getText().toString().trim()); } catch (Exception ex) { return def; }
    }
}
