package ir.tripanalyzer.app;

import android.R;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

/* compiled from: VehicleSettingsActivity.kt */
/* loaded from: classes.dex */
public final class VehicleSettingsActivity extends BaseActivity {
    private EditText depInput;
    private String[] families;
    private EditText mileageInput;
    private Spinner spinner;
    private EditText trafficInput;

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCalc(VehicleSettingsActivity vehicleSettingsActivity, View view) {
        int selectedItemPosition = vehicleSettingsActivity.spinner.getSelectedItemPosition();
        String[] strArr = vehicleSettingsActivity.families;
        if (strArr != null && selectedItemPosition >= 0 && selectedItemPosition < strArr.length) {
            try {
                vehicleSettingsActivity.depInput.setText(String.valueOf((long) DepLookup.INSTANCE.lookup(vehicleSettingsActivity, strArr[selectedItemPosition], Integer.parseInt(vehicleSettingsActivity.mileageInput.getText().toString().trim()))));
                Toast.makeText(vehicleSettingsActivity, "استهلاک از جدول پر شد. اگر مقدار دیگری می\u200cدانید تغییر دهید.", 1).show();
                return;
            } catch (Exception unused) {
            }
        }
        Toast.makeText(vehicleSettingsActivity, "نام ماشین و کارکرد را درست وارد کنید", 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onSave(VehicleSettingsActivity vehicleSettingsActivity, View view) {
        int selectedItemPosition = vehicleSettingsActivity.spinner.getSelectedItemPosition();
        String[] strArr = vehicleSettingsActivity.families;
        String str = "";
        if (strArr != null && selectedItemPosition >= 0 && selectedItemPosition < strArr.length) {
            str = strArr[selectedItemPosition];
        }
        int i = 0;
        try {
            i = Integer.parseInt(vehicleSettingsActivity.mileageInput.getText().toString().trim());
        } catch (Exception unused) {
        }
        Store store = Store.INSTANCE;
        store.setVehicleInfo(vehicleSettingsActivity, str, i);
        float f = 35.0f;
        try {
            f = Float.parseFloat(vehicleSettingsActivity.trafficInput.getText().toString());
        } catch (Exception unused2) {
        }
        store.setTrafficSpeed(vehicleSettingsActivity, f);
        try {
            float parseFloat = Float.parseFloat(vehicleSettingsActivity.depInput.getText().toString());
            SharedPreferences.Editor edit = store.prefs(vehicleSettingsActivity).edit();
            edit.putFloat("dep_per_km", parseFloat);
            edit.apply();
        } catch (Exception unused3) {
        }
        Toast.makeText(vehicleSettingsActivity, "ذخیره شد", 0).show();
        vehicleSettingsActivity.finish();
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(24, 24, 24, 24);
        TextView textView = new TextView(this);
        textView.setText("ماشین و استهلاک");
        textView.setTextSize(24.0f);
        linearLayout.addView(textView);
        TextView textView2 = new TextView(this);
        textView2.setText("اگر ماشین شما در لیست نیست، نزدیک\u200cترین ماشین از نظر قیمت و هزینه تعمیرات را انتخاب کنید.");
        textView2.setTextSize(14.0f);
        linearLayout.addView(textView2);
        String[] families = DepLookup.INSTANCE.families(this);
        this.families = families;
        TextView textView3 = new TextView(this);
        textView3.setText("نام ماشین");
        linearLayout.addView(textView3);
        Spinner spinner = new Spinner(this);
        this.spinner = spinner;
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.simple_spinner_item, families);
        arrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter((SpinnerAdapter) arrayAdapter);
        linearLayout.addView(spinner);
        TextView textView4 = new TextView(this);
        textView4.setText("کارکرد فعلی (کیلومتر)");
        linearLayout.addView(textView4);
        EditText editText = new EditText(this);
        this.mileageInput = editText;
        editText.setInputType(2);
        editText.setHint("مثلاً 85000");
        linearLayout.addView(editText);
        Button button = new Button(this);
        button.setText("محاسبه استهلاک از جدول");
        button.setOnClickListener(new View.OnClickListener() { // from class: ir.tripanalyzer.app.VehicleSettingsActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                VehicleSettingsActivity.onCalc(VehicleSettingsActivity.this, view);
            }
        });
        linearLayout.addView(button);
        TextView textView5 = new TextView(this);
        textView5.setText("استهلاک (تومان به ازای هر کیلومتر)");
        linearLayout.addView(textView5);
        EditText editText2 = new EditText(this);
        this.depInput = editText2;
        editText2.setInputType(8194);
        editText2.setHint("بعد از محاسبه پر می\u200cشود");
        linearLayout.addView(editText2);
        TextView textView6 = new TextView(this);
        textView6.setText("سرعت ترافیک (کیلومتر بر ساعت) - پیش\u200cفرض ۳۵");
        linearLayout.addView(textView6);
        TextView textView7 = new TextView(this);
        textView7.setText("در ساعات ۱۱–۱۴ و ۱۸–۲۰ از این سرعت استفاده می\u200cشود");
        textView7.setTextSize(12.0f);
        linearLayout.addView(textView7);
        EditText editText3 = new EditText(this);
        this.trafficInput = editText3;
        editText3.setInputType(8194);
        editText3.setText("35");
        linearLayout.addView(editText3);
        SharedPreferences prefs = Store.INSTANCE.prefs(this);
        String string = prefs.getString("vehicle_family", "");
        if (string != null && families != null) {
            int i = 0;
            while (true) {
                if (i >= families.length) {
                    break;
                }
                if (string.equals(families[i])) {
                    this.spinner.setSelection(i);
                    break;
                }
                i++;
            }
        }
        int i2 = prefs.getInt("vehicle_mileage", 0);
        if (i2 > 0) {
            this.mileageInput.setText(String.valueOf(i2));
        }
        this.depInput.setText(String.valueOf((long) prefs.getFloat("dep_per_km", 1000.0f)));
        this.trafficInput.setText(String.valueOf((int) prefs.getFloat("traffic_speed", 35.0f)));
        Button button2 = new Button(this);
        button2.setText("ذخیره");
        button2.setOnClickListener(new View.OnClickListener() { // from class: ir.tripanalyzer.app.VehicleSettingsActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                VehicleSettingsActivity.onSave(VehicleSettingsActivity.this, view);
            }
        });
        linearLayout.addView(button2);
        scrollView.addView(linearLayout);
        setContentView(scrollView);
    }
}
