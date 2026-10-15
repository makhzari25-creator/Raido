package ir.tripanalyzer.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ویزارد تنظیمات اولیه — ظاهر تأییدشده (هدر تیل + کارت).
 * منطق ذخیره و مراحل بدون تغییر.
 */
public final class SetupWizardActivity extends BaseActivity {

    private static final int TEAL2 = 0xFF0F766E;
    private static final int PRIMARY = 0xFF2563EB;
    private static final int TEXT_DARK = 0xFF0F172A;
    private static final int TEXT_MUTED = 0xFF64748B;
    private static final int CARD_BORDER = 0xFFE2E8F0;

    private int step = 0;
    private LinearLayout contentHost;
    private LinearLayout progressHost;
    private TextView headerTitle;
    private TextView headerSub;
    private Button btnPrimary;
    private Button btnSecondary;

    private Spinner brandSpinner;
    private EditText mileageInput;
    private EditText consumptionInput;
    private EditText fuelPriceInput;
    private EditText targetInput;
    private EditText citySpeedInput;
    private EditText highwaySpeedInput;
    private EditText trafficSpeedInput;
    private EditText nameInput;
    private ImageView avatarPreview;
    private static final int REQ_PICK_PHOTO = 4401;

    private static final String[] BRANDS = {
            "انتخاب برند",
            "پژو ۴۰۵", "پژو ۲۰۶", "پژو پارس", "سمند", "دنا", "رانا",
            "تیبا", "ساینا", "کوییک", "پراید", "آریو", "جک", "چری",
            "هیوندای", "کیا", "تویوتا", "سایر"
    };

    @Override
    protected boolean usePagesBackground() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            buildShell();
            renderStep();
        } catch (Throwable t) {
            Toast.makeText(this, "خطا در نمایش ویزارد", Toast.LENGTH_SHORT).show();
            try { finishWizard(); } catch (Throwable ignored) {}
        }
    }

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF0FDFA);
        root.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(18), dp(20), dp(18), dp(18));
        GradientDrawable hg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF0B3D3A, 0xFF134E4A, 0xFF0F766E});
        header.setBackground(hg);

        TextView brand = new TextView(this);
        brand.setText("RAIDO · تنظیمات اولیه");
        brand.setTextSize(12f);
        brand.setTextColor(0xCCFFFFFF);
        header.addView(brand);

        headerTitle = new TextView(this);
        headerTitle.setTextSize(20f);
        headerTitle.setTypeface(Typeface.DEFAULT_BOLD);
        headerTitle.setTextColor(Color.WHITE);
        headerTitle.setPadding(0, dp(6), 0, 0);
        header.addView(headerTitle);

        headerSub = new TextView(this);
        headerSub.setTextSize(13f);
        headerSub.setTextColor(0xE6FFFFFF);
        headerSub.setPadding(0, dp(6), 0, 0);
        header.addView(headerSub);

        progressHost = new LinearLayout(this);
        progressHost.setOrientation(LinearLayout.HORIZONTAL);
        progressHost.setPadding(0, dp(14), 0, 0);
        header.addView(progressHost);
        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        contentHost = new LinearLayout(this);
        contentHost.setOrientation(LinearLayout.VERTICAL);
        contentHost.setPadding(dp(14), dp(16), dp(14), dp(20));
        scroll.addView(contentHost);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(dp(14), dp(8), dp(14), dp(16));
        btnRow.setBackgroundColor(0xFFF0FDFA);

        btnSecondary = outlineButton("قبلی");
        btnSecondary.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    if (step <= 0 || step == 5) finishWizard();
                    else { step--; renderStep(); }
                } catch (Throwable ignored) {}
            }
        });
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, -2, 1f);
        slp.setMargins(0, 0, dp(6), 0);
        btnRow.addView(btnSecondary, slp);

        btnPrimary = primaryButton("بعدی");
        btnPrimary.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try { onNext(); } catch (Throwable t) {
                    Toast.makeText(SetupWizardActivity.this, "خطا", Toast.LENGTH_SHORT).show();
                }
            }
        });
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(0, -2, 1f);
        plp.setMargins(dp(6), 0, 0, 0);
        btnRow.addView(btnPrimary, plp);
        root.addView(btnRow);
        setContentView(root);
        try { Fonts.INSTANCE.applyRecursive(root); } catch (Exception ignored) {}
    }

    private void renderProgress() {
        progressHost.removeAllViews();
        for (int i = 0; i < 6; i++) {
            View bar = new View(this);
            GradientDrawable g = new GradientDrawable();
            g.setCornerRadius(dp(99));
            g.setColor(i <= step ? 0xFF5EEAD4 : 0x33FFFFFF);
            bar.setBackground(g);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(5), 1f);
            if (i < 5) lp.setMargins(0, 0, dp(6), 0);
            progressHost.addView(bar, lp);
        }
    }

    private void renderStep() {
        contentHost.removeAllViews();
        renderProgress();
        brandSpinner = null; mileageInput = null; consumptionInput = null;
        fuelPriceInput = null; targetInput = null; nameInput = null; avatarPreview = null;
        switch (step) {
            case 0: renderWelcome(); break;
            case 1: renderBrand(); break;
            case 2: renderFuel(); break;
            case 3: renderTarget(); break;
            case 4: renderSpeeds(); break;
            case 5: renderProfile(); break;
            default: renderWelcome(); break;
        }
        updateButtons();
        try { Fonts.INSTANCE.applyRecursive(contentHost); } catch (Exception ignored) {}
    }

    private void updateButtons() {
        if (step == 0) {
            btnSecondary.setText("رد کردن");
            btnPrimary.setText("بعدی");
        } else if (step == 5) {
            btnSecondary.setText("رد کردن");
            btnPrimary.setText("پایان و ادامه");
        } else {
            btnSecondary.setText("قبلی");
            btnPrimary.setText("بعدی");
        }
    }

    private void setHeader(String title, String sub) {
        if (headerTitle != null) headerTitle.setText(title);
        if (headerSub != null) headerSub.setText(sub);
    }

    private void renderWelcome() {
        setHeader("خوش آمدی", "چند قدم تا تحلیل هوشمند درخواست‌ها");
        contentHost.addView(stepPill("1 خوش‌آمد"));
        LinearLayout c = whiteCard(true);
        c.addView(iconTitle("تحليل", 0xFFCCFBF1, "تحلیل فوری هر درخواست",
                "رایدو عددها را حساب می‌کند و با سه رنگ راهنمایی می‌کند."));
        LinearLayout lights = new LinearLayout(this);
        lights.setOrientation(LinearLayout.HORIZONTAL);
        lights.setPadding(0, dp(10), 0, 0);
        lights.addView(tlChip("سبز · برو", 0xFFECFDF5, 0xFF059669));
        lights.addView(tlChip("زرد · فکر کن", 0xFFFFFBEB, 0xFFD97706));
        lights.addView(tlChip("قرمز · رد کن", 0xFFFEF2F2, 0xFFDC2626));
        c.addView(lights);
        contentHost.addView(c);
        LinearLayout c2 = whiteCard(false);
        c2.addView(bodyText("رایدو کنارت است تا وسط خیابان وقتت صرف سفرهای ضعیف نشود."));
        c2.addView(bodyText("هر درخواست اسنپ را از روی کرایه، زمان و مسافت می‌خواند و می‌گوید این سفر به‌صرفه است یا نه."));
        c2.addView(bodyText("هدف را واقعی و نزدیک عرف بگذار."));
        contentHost.addView(c2);
    }

    private void renderBrand() {
        setHeader("برند خودرو و کارکرد", "برای محاسبه دقیق استهلاک");
        contentHost.addView(stepPill("2 خودرو"));
        LinearLayout c = whiteCard(true);
        c.addView(fieldLabel("برند خودرو"));
        brandSpinner = new Spinner(this);
        brandSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, BRANDS));
        styleSpinner(brandSpinner);
        c.addView(brandSpinner);
        c.addView(fieldLabel("کارکرد فعلی (کیلومتر)"));
        mileageInput = inputField("مثال: 125000", InputType.TYPE_CLASS_NUMBER);
        c.addView(mileageInput);
        c.addView(infoBox("کیلومتر دقیق = دقت بالاتر در محاسبات استهلاک"));
        contentHost.addView(c);
    }

    private void renderFuel() {
        setHeader("مصرف سوخت و قیمت بنزین", "این مقادیر هزینه بنزین را می‌سازند");
        contentHost.addView(stepPill("3 سوخت"));
        LinearLayout c = whiteCard(true);
        c.addView(fieldLabel("مصرف سوخت (لیتر در 100 کیلومتر)"));
        consumptionInput = inputField("مثال: 7.5", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        c.addView(consumptionInput);
        c.addView(fieldLabel("قیمت بنزین (تومان در لیتر)"));
        fuelPriceInput = inputField("مثال: 10000", InputType.TYPE_CLASS_NUMBER);
        c.addView(fuelPriceInput);
        c.addView(infoBox("این مقادیر در محاسبه هزینه سوخت تأثیر مستقیم دارند."));
        contentHost.addView(c);
    }

    private void renderTarget() {
        setHeader("درآمد ساعتی ایده‌آلت", "کارت سبزتر نزدیک این عدد");
        contentHost.addView(stepPill("4 هدف"));
        LinearLayout c = whiteCard(true);
        c.addView(fieldLabel("هدف ساعتی (تومان)"));
        targetInput = inputField("مثال: 320000", InputType.TYPE_CLASS_NUMBER);
        c.addView(targetInput);
        contentHost.addView(c);
        LinearLayout tip = whiteCard(false);
        GradientDrawable tg = new GradientDrawable();
        tg.setColor(0xFFECFDF5);
        tg.setCornerRadius(dp(16));
        tg.setStroke(dp(1), 0xFF86EFAC);
        tip.setBackground(tg);
        TextView tt = new TextView(this);
        tt.setText("نکته EcoPlus");
        tt.setTextSize(14f);
        tt.setTypeface(Typeface.DEFAULT_BOLD);
        tt.setTextColor(0xFF166534);
        tip.addView(tt);
        TextView tb = new TextView(this);
        tb.setText("اگر جزو ناوگان EcoPlus هستید، درآمد ساعتی‌تان معمولاً بالاتر است. فیلد را با دقت پر کنید.");
        tb.setTextSize(13f);
        tb.setTextColor(0xFF15803D);
        tb.setPadding(0, dp(6), 0, 0);
        tip.addView(tb);
        contentHost.addView(tip);
    }

    private void renderSpeeds() {
        setHeader("تخمین مسافت", "وقتی کیلومتر روی صفحه نباشد");
        contentHost.addView(stepPill("5 تخمین"));
        LinearLayout c = whiteCard(true);
        TextView d = new TextView(this);
        d.setText("اگر در درخواست اسنپ کیلومتر دیده نشود، برای تخمین مسافت از میانگین سرعت کاری راننده استفاده می‌کنیم. طبق بررسی‌ها معمولاً بین 25 تا 35 کیلومتر در ساعت راه می‌رود؛ در رایدو میانگین 30 کیلومتر بر ساعت برای تخمین به‌کار می‌رود.\n\nاگر کیلومتر روی صفحه باشد، همان مقدار واقعی در تحلیل استفاده می‌شود.");
        d.setTextSize(13.5f);
        d.setTextColor(TEXT_MUTED);
        d.setLineSpacing(0, 1.35f);
        c.addView(d);
        citySpeedInput = inputField("30", InputType.TYPE_CLASS_NUMBER);
        highwaySpeedInput = inputField("30", InputType.TYPE_CLASS_NUMBER);
        trafficSpeedInput = inputField("30", InputType.TYPE_CLASS_NUMBER);
        citySpeedInput.setVisibility(View.GONE);
        highwaySpeedInput.setVisibility(View.GONE);
        trafficSpeedInput.setVisibility(View.GONE);
        c.addView(citySpeedInput);
        c.addView(highwaySpeedInput);
        c.addView(trafficSpeedInput);
        contentHost.addView(c);
    }

    private void renderProfile() {
        setHeader("پروفایل راننده", "اختیاری — فقط روی گوشی خودت");
        contentHost.addView(stepPill("6 پروفایل"));
        LinearLayout c = whiteCard(true);
        c.addView(fieldLabel("نام نمایشی"));
        nameInput = inputField("خالی = کاربر رایدو", InputType.TYPE_CLASS_TEXT);
        try {
            if (ProfileStore.INSTANCE.hasCustomName(this)) {
                nameInput.setText(ProfileStore.INSTANCE.getDisplayName(this));
            }
        } catch (Throwable ignored) {}
        c.addView(nameInput);
        c.addView(fieldLabel("عکس پروفایل"));
        avatarPreview = new ImageView(this);
        avatarPreview.setLayoutParams(new LinearLayout.LayoutParams(dp(72), dp(72)));
        avatarPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        try {
            Bitmap ph = ProfileStore.INSTANCE.loadPhotoBitmap(this);
            if (ph != null) avatarPreview.setImageBitmap(ph);
            else avatarPreview.setImageResource(R.mipmap.ic_launcher);
        } catch (Throwable e) {
            try { avatarPreview.setImageResource(R.mipmap.ic_launcher); } catch (Throwable ignored) {}
        }
        c.addView(avatarPreview);
        Button photoBtn = primaryButton("تغییر عکس پروفایل");
        photoBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.setType("image/*");
                    startActivityForResult(Intent.createChooser(i, "انتخاب عکس"), REQ_PICK_PHOTO);
                } catch (Throwable e) {
                    Toast.makeText(SetupWizardActivity.this, "انتخاب عکس ممکن نشد", Toast.LENGTH_SHORT).show();
                }
            }
        });
        LinearLayout.LayoutParams pbl = new LinearLayout.LayoutParams(-1, -2);
        pbl.topMargin = dp(12);
        photoBtn.setLayoutParams(pbl);
        c.addView(photoBtn);
        c.addView(infoBox("این اطلاعات فقط روی گوشی خودت ذخیره می‌شود."));
        contentHost.addView(c);
    }

    private void onNext() {
        if (step == 0) { step = 1; renderStep(); return; }
        if (step == 1) { saveBrandMileage(); step = 2; renderStep(); return; }
        if (step == 2) { saveFuel(); step = 3; renderStep(); return; }
        if (step == 3) { saveTarget(); step = 4; renderStep(); return; }
        if (step == 4) { saveSpeeds(); step = 5; renderStep(); return; }
        if (step == 5) { saveProfile(); finishWizard(); }
    }

    private void saveBrandMileage() {
        String brand = "سایر";
        if (brandSpinner != null && brandSpinner.getSelectedItemPosition() > 0) {
            brand = BRANDS[brandSpinner.getSelectedItemPosition()];
        }
        int mileage = parseInt(mileageInput, 0);
        try { Store.INSTANCE.setVehicleInfo(this, brand, mileage); } catch (Throwable ignored) {}
        try {
            float dep = (float) DepLookup.INSTANCE.lookup(this, brand, mileage);
            Store.INSTANCE.prefs(this).edit().putFloat("dep_per_km", dep).apply();
        } catch (Exception ignored) {}
    }

    private void saveFuel() {
        float cons = parseFloat(consumptionInput, 7.5f);
        float price = parseFloat(fuelPriceInput, 10000f);
        try {
            Store.INSTANCE.prefs(this).edit()
                    .putFloat("fuel_per_100", cons)
                    .putFloat("fuel_price", price)
                    .apply();
        } catch (Throwable ignored) {}
    }

    private void saveTarget() {
        float target = parseFloat(targetInput, 320000f);
        if (target < 220000f) {
            Toast.makeText(this, "مبلغ واردشده از حداقل منطقی سیستم کمتر است.", Toast.LENGTH_LONG).show();
        }
        try {
            Store.INSTANCE.prefs(this).edit()
                    .putFloat("driver_gross_hourly", target)
                    .putFloat("target", target)
                    .apply();
        } catch (Throwable ignored) {}
    }

    private void saveSpeeds() {
        try {
            Store.INSTANCE.prefs(this).edit()
                    .putFloat("speed", 30f)
                    .putFloat("traffic_speed", 30f)
                    .putFloat("city_speed", 30f)
                    .putFloat("highway_speed", 30f)
                    .apply();
        } catch (Throwable ignored) {}
    }

    private void saveProfile() {
        try {
            if (nameInput != null) {
                ProfileStore.INSTANCE.setDisplayName(this,
                        nameInput.getText() != null ? nameInput.getText().toString().trim() : "");
            }
        } catch (Throwable ignored) {}
    }

    private void finishWizard() {
        try { Store.INSTANCE.setOnboardingDone(this, true); } catch (Throwable ignored) {}
        try {
            startActivity(new Intent(this, PermissionsOnboardingActivity.class));
            finish();
        } catch (Throwable t) {
            try { startActivity(new Intent(this, MainActivity.class)); finish(); } catch (Throwable ignored) {}
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                if (ProfileStore.INSTANCE.savePhotoFromUri(this, data.getData())) {
                    Bitmap b = ProfileStore.INSTANCE.loadPhotoBitmap(this);
                    if (avatarPreview != null && b != null) avatarPreview.setImageBitmap(b);
                }
            } catch (Throwable e) {
                Toast.makeText(this, "ذخیره عکس ممکن نشد", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private TextView stepPill(String t) {
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

    private LinearLayout whiteCard(boolean hero) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15), dp(16), dp(15), dp(16));
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(20));
        g.setStroke(dp(1), hero ? 0xFF99F6E4 : CARD_BORDER);
        c.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(12);
        c.setLayoutParams(lp);
        return c;
    }

    private LinearLayout iconTitle(String emoji, int bg, String title, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView ico = new TextView(this);
        ico.setText(emoji);
        ico.setTextSize(14f);
        ico.setGravity(Gravity.CENTER);
        ico.setTypeface(Typeface.DEFAULT_BOLD);
        ico.setTextColor(TEAL2);
        GradientDrawable ig = new GradientDrawable();
        ig.setColor(bg);
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
        t.setTextColor(TEXT_DARK);
        col.addView(t);
        TextView d = new TextView(this);
        d.setText(desc);
        d.setTextSize(13f);
        d.setTextColor(TEXT_MUTED);
        d.setPadding(0, dp(4), 0, 0);
        col.addView(d);
        row.addView(col, new LinearLayout.LayoutParams(0, -2, 1f));
        return row;
    }

    private TextView tlChip(String text, int bg, int fg) {
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

    private TextView bodyText(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(13.5f);
        t.setTextColor(TEXT_DARK);
        t.setPadding(0, 0, 0, dp(10));
        t.setLineSpacing(0, 1.3f);
        return t;
    }

    private TextView fieldLabel(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(12f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(TEXT_MUTED);
        t.setPadding(0, dp(10), 0, dp(6));
        return t;
    }

    private EditText inputField(String hint, int inputType) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setInputType(inputType);
        e.setTextSize(14f);
        e.setTextColor(TEXT_DARK);
        e.setHintTextColor(0xFF94A3B8);
        e.setSingleLine(true);
        e.setPadding(dp(14), dp(13), dp(14), dp(13));
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(13));
        g.setStroke(dp(2), CARD_BORDER);
        e.setBackground(g);
        return e;
    }

    private void styleSpinner(Spinner sp) {
        sp.setPadding(dp(8), dp(8), dp(8), dp(8));
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(13));
        g.setStroke(dp(2), CARD_BORDER);
        sp.setBackground(g);
    }

    private TextView infoBox(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(12.5f);
        t.setTextColor(TEXT_MUTED);
        t.setPadding(dp(12), dp(12), dp(12), dp(12));
        GradientDrawable g = new GradientDrawable();
        g.setColor(0xFFF8FAFC);
        g.setCornerRadius(dp(12));
        t.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(12);
        t.setLayoutParams(lp);
        return t;
    }

    private Button primaryButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14.5f);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF3B82F6, 0xFF1D4ED8});
        g.setCornerRadius(dp(14));
        b.setBackground(g);
        b.setPadding(dp(12), dp(14), dp(12), dp(14));
        return b;
    }

    private Button outlineButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(TEXT_MUTED);
        b.setTextSize(14.5f);
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(14));
        g.setColor(Color.WHITE);
        g.setStroke(dp(1), CARD_BORDER);
        b.setBackground(g);
        b.setPadding(dp(12), dp(14), dp(12), dp(14));
        return b;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private int parseInt(EditText e, int def) {
        if (e == null) return def;
        try { return Integer.parseInt(e.getText().toString().trim()); }
        catch (Exception ex) { return def; }
    }

    private float parseFloat(EditText e, float def) {
        if (e == null) return def;
        try { return Float.parseFloat(e.getText().toString().trim()); }
        catch (Exception ex) { return def; }
    }
}
// 1791193683
