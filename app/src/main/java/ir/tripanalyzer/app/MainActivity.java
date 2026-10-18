package ir.tripanalyzer.app;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.widget.Toast;
import kotlin.jvm.internal.Intrinsics;

/** Temporary restore shell — full UI will be restored in next commit */
public final class MainActivity extends BaseActivity {
    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!LicenseStore.INSTANCE.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF4F7F4);
        root.setPadding(32, 48, 32, 32);

        TextView t = new TextView(this);
        t.setText("RAIDO");
        t.setTextSize(22f);
        t.setTextColor(0xFF0C4745);
        root.addView(t);

        TextView s = new TextView(this);
        s.setText("نسخه UI در حال به‌روزرسانی است. لطفاً APK بعدی را نصب کنید.");
        s.setTextSize(14f);
        s.setTextColor(0xFF738181);
        s.setPadding(0, 16, 0, 24);
        root.addView(s);

        Button b = new Button(this);
        b.setText("تنظیمات");
        b.setAllCaps(false);
        b.setTextColor(0xFFFFFFFF);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF0C4745);
        bg.setCornerRadius(28f);
        b.setBackground(bg);
        b.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        root.addView(b);

        Button b2 = new Button(this);
        b2.setText("گزارش‌ها");
        b2.setAllCaps(false);
        b2.setTextColor(0xFF0C4745);
        b2.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        root.addView(b2);

        Button b3 = new Button(this);
        b3.setText("تحلیل / جزئیات");
        b3.setAllCaps(false);
        b3.setTextColor(0xFF0C4745);
        b3.setOnClickListener(v -> startActivity(new Intent(this, DetailsActivity.class)));
        root.addView(b3);

        setContentView(root);
        try { Fonts.INSTANCE.applyRecursive(root); } catch (Throwable ignored) {}
    }
}
