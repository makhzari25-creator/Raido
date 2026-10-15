package ir.tripanalyzer.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** حریم خصوصی و داده — ظاهر تجاری RAIDO */
public final class PrivacyActivity extends BaseActivity {

    private static final int PRIMARY = 0xFF2563EB;
    private static final int TEXT_DARK = 0xFF0F172A;
    private static final int TEXT_MUTED = 0xFF64748B;
    private static final int CARD_BORDER = 0xFFE2E8F0;

    private TextView accStatus;
    private TextView notifStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(32));
        root.setBackgroundColor(0xE60B1220);

        TextView title = new TextView(this);
        title.setText("حریم خصوصی");
        title.setTextSize(22f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(TEXT_DARK);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("RAIDO همه محاسبات را فقط روی دستگاه شما انجام می‌دهد و داده‌ای به سرور ارسال نمی‌کند (تا زمان فعال‌سازی سرور اشتراک).");
        sub.setTextSize(13f);
        sub.setTextColor(TEXT_MUTED);
        sub.setPadding(0, dp(6), 0, dp(16));
        root.addView(sub);

        LinearLayout status = card();
        status.addView(section("وضعیت دسترسی‌ها"));
        accStatus = new TextView(this);
        accStatus.setTextSize(14f);
        accStatus.setPadding(0, dp(6), 0, dp(4));
        status.addView(accStatus);
        notifStatus = new TextView(this);
        notifStatus.setTextSize(14f);
        notifStatus.setPadding(0, dp(4), 0, 0);
        status.addView(notifStatus);
        root.addView(status);
        addSpace(root, 14);

        LinearLayout policy = card();
        policy.addView(section("سیاست حریم خصوصی"));
        TextView body = new TextView(this);
        body.setText("• متن صفحه اسنپ فقط برای تحلیل محلی خوانده می‌شود.\n"
                + "• هیچ اقدامی به‌جای شما در اسنپ انجام نمی‌شود.\n"
                + "• گزارش‌ها و تنظیمات فقط روی گوشی ذخیره می‌شوند.\n"
                + "• می‌توانید هر زمان همه داده‌ها را پاک کنید.");
        body.setTextSize(13f);
        body.setTextColor(TEXT_DARK);
        body.setLineSpacing(0, 1.3f);
        policy.addView(body);
        root.addView(policy);
        addSpace(root, 20);

        Button erase = new Button(this);
        erase.setText("پاک کردن همه داده‌ها");
        erase.setAllCaps(false);
        erase.setTextColor(Color.WHITE);
        erase.setTextSize(15f);
        erase.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(12));
        g.setColor(0xFFDC2626);
        erase.setBackground(g);
        erase.setPadding(dp(12), dp(14), dp(12), dp(14));
        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmErase();
            }
        });
        root.addView(erase);

        scroll.addView(root);
        setContentView(scroll);
        refreshStatus();
    }

    private void refreshStatus() {
        boolean acc = isAccessibilityEnabled();
        boolean notif = isNotificationListenerEnabled();
        if (accStatus != null) {
            accStatus.setText(acc ? "✓ سرویس دسترسی‌پذیری فعال" : "✗ سرویس دسترسی‌پذیری غیرفعال");
            accStatus.setTextColor(acc ? 0xFF16A34A : 0xFFDC2626);
        }
        if (notifStatus != null) {
            notifStatus.setText(notif ? "✓ گوش دادن به اعلان‌ها فعال" : "✗ گوش دادن به اعلان‌ها غیرفعال");
            notifStatus.setTextColor(notif ? 0xFF16A34A : 0xFFDC2626);
        }
    }

    private boolean isAccessibilityEnabled() {
        try {
            String enabled = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (enabled == null) return false;
            return enabled.toLowerCase().contains(getPackageName().toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNotificationListenerEnabled() {
        try {
            String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
            if (TextUtils.isEmpty(flat)) return false;
            return flat.toLowerCase().contains(getPackageName().toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    private void confirmErase() {
        new AlertDialog.Builder(this)
                .setTitle("پاک کردن داده‌ها")
                .setMessage("همه گزارش‌ها، آخرین نتیجه و تنظیمات پاک می‌شود. این عمل قابل بازگشت نیست.")
                .setPositiveButton("پاک کن", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Store.INSTANCE.eraseEverything(PrivacyActivity.this);
                            Toast.makeText(PrivacyActivity.this, "همه داده‌ها پاک شد", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(PrivacyActivity.this, "خطا در پاک‌سازی", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("انصراف", null)
                .show();
    }

    private TextView section(String t) {
        TextView v = new TextView(this);
        v.setText(t);
        v.setTextSize(15f);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setTextColor(TEXT_DARK);
        v.setPadding(0, 0, 0, dp(8));
        return v;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(14));
        g.setColor(Color.WHITE);
        g.setStroke(dp(1), CARD_BORDER);
        c.setBackground(g);
        c.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        return c;
    }

    private void addSpace(LinearLayout p, int d) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(d)));
        p.addView(v);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
