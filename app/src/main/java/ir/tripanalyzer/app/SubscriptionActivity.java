package ir.tripanalyzer.app;

import android.app.Dialog;
import android.content.Intent;
import android.view.Window;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * صفحه اشتراک — بدون زرین‌پال.
 * زیر هر مبلغ لینک درخواست از پشتیبانی تلگرام.
 */
public final class SubscriptionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
    }

    private LinearLayout list;

    @Override
    protected void onResume() {
        super.onResume();
        if (list != null) render();
    }

    private void buildUi() {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(PageKit.BG);
        shell.addView(PageKit.header(this, "اشتراک RAIDO", "حسابداری هوشمند رانندگان"), new LinearLayout.LayoutParams(-1, PageKit.dp(this, 150)));
        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(16), dp(16), dp(16), dp(28));
        scroll.addView(list);
        shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(shell);
        render();
        RemoteConfig.refresh(this, 30000L, this::render);
    }

    private void render() {
        LinearLayout root = list;
        root.removeAllViews();

        LinearLayout statusCard = PageKit.card(this);
        boolean active = LicenseStore.INSTANCE.isAccessActive(this);
        int stColor = active ? 0xFF16A34A : 0xFFDC2626;
        TextView statusTitle = new TextView(this);
        statusTitle.setText("وضعیت فعلی");
        statusTitle.setTextSize(13f);
        statusTitle.setTextColor(PageKit.MUTED);
        statusCard.addView(statusTitle);
        TextView statusLabel = new TextView(this);
        statusLabel.setText(LicenseStore.INSTANCE.getStatusLabel(this));
        statusLabel.setTextSize(17f);
        statusLabel.setTypeface(null, Typeface.BOLD);
        statusLabel.setTextColor(stColor);
        statusLabel.setPadding(0, dp(6), 0, 0);
        statusCard.addView(statusLabel);
        TextView check = new TextView(this);
        check.setText(active ? "✓ دسترسی فعال" : "✗ نیاز به کد فعال‌سازی");
        check.setTextSize(14f);
        check.setTextColor(stColor);
        check.setPadding(0, dp(4), 0, 0);
        statusCard.addView(check);
        root.addView(statusCard, PageKit.gap(this, 0));

        TextView plansTitle = PageKit.sectionTitle(this, "طرح‌ها و قیمت");
        plansTitle.setPadding(0, dp(20), 0, dp(10));
        root.addView(plansTitle);

        root.addView(planCard("تست ۷ روزه", "رایگان با کد پشتیبانی",
                "آشنایی با تحلیل عادی · بدون لایهٔ نشان",
                "سلام، کد تست ۷ روزه رایدو می‌خوام"), PageKit.gap(this, 0));
        addPlan(root, "MONTHLY", "ماهانه عادی", LicenseStore.PRICE_MONTHLY,
                "تحلیل کامل درخواست‌ها · ۳۰ روز", false);
        addPlan(root, "YEARLY", "سالانه عادی", LicenseStore.PRICE_YEARLY,
                "تحلیل کامل · ۳۶۵ روز", false);
        addPlan(root, "PREMIUM_MONTHLY", "پرمیوم ماهانه", LicenseStore.PRICE_PREMIUM_MONTHLY,
                "نشان + محدوده شهری + برگشت خالی (Hشیفت) · ۳۰ روز", true);
        addPlan(root, "PREMIUM_YEARLY", "پرمیوم سالانه", LicenseStore.PRICE_PREMIUM_YEARLY,
                "نشان + محدوده شهری + برگشت خالی · ۳۶۵ روز", true);

        TextView note = new TextView(this);
        note.setText("پرداخت و صدور کد فقط از طریق پشتیبانی انجام می‌شود.");
        note.setTextSize(13f);
        note.setTextColor(PageKit.MUTED);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, dp(22), 0, 0);
        root.addView(note);

        TextView sup = PageKit.button(this, "ارتباط با پشتیبانی", PageKit.BLUE);
        sup.setOnClickListener(v -> openTelegram(null));
        root.addView(sup, PageKit.gap(this, 10));

        TextView actLink = PageKit.button(this, "ورود کد فعال‌سازی", PageKit.TEAL);
        actLink.setOnClickListener(v -> {
            Intent ai = new Intent(this, ActivationActivity.class);
            ai.putExtra(ActivationActivity.EXTRA_RENEW, true);
            startActivity(ai);
        });
        root.addView(actLink, PageKit.gap(this, 10));

        if (LicenseStore.INSTANCE.isLoggedIn(this)) {
            TextView outLink = new TextView(this);
            outLink.setText("خروج از حساب");
            outLink.setTextSize(14f);
            outLink.setTextColor(Color.parseColor("#DC2626"));
            outLink.setGravity(Gravity.CENTER);
            outLink.setPadding(0, dp(18), 0, dp(10));
            outLink.setOnClickListener(v -> confirmLogout());
            root.addView(outLink);
        }
        try { Fonts.INSTANCE.applyRecursive(root); } catch (Exception ignored) {}
    }

    /** قیمت و توضیح از سرور (پنل ← اشتراک و قیمت)؛ اگر نبود مقدار پیش‌فرض داخل اپ */
    private void addPlan(LinearLayout root, String code, String title, int price, String desc, boolean premium) {
        RemoteConfig.Plan p = RemoteConfig.plan(this, code);
        if (p != null) {
            if (!p.title.isEmpty()) title = p.title;
            if (!p.desc.isEmpty()) desc = p.desc;
            price = p.price;
        }
        String tgName = title;
        root.addView(planCard(title, formatPrice(price) + " تومان", desc,
                "سلام، اشتراک " + tgName + " رایدو می‌خوام"), PageKit.gap(this, 12));
        if (premium) {
            LinearLayout.LayoutParams lp = PageKit.gap(this, 8);
            root.addView(premiumDiffChip(), lp);
        }
    }

    private void confirmLogout() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("خروج از حساب")
                .setMessage("از این دستگاه خارج می‌شوی. بعداً می‌توانی دوباره با تلگرام وارد شوی.")
                .setNegativeButton("انصراف", null)
                .setPositiveButton("خروج", (d, w) -> new Thread(() -> {
                    LicenseStore.INSTANCE.logoutRemote(SubscriptionActivity.this);
                    runOnUiThread(() -> {
                        Intent i = new Intent(SubscriptionActivity.this, LoginActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    });
                }, "raido-logout").start())
                .show();
    }

    private LinearLayout planCard(String title, String price, String desc, final String tgMessage) {
        LinearLayout card = PageKit.card(this);
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(16f);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextColor(Color.parseColor("#0F172A"));
        card.addView(t);

        TextView p = new TextView(this);
        p.setText(price);
        p.setTextSize(20f);
        p.setTypeface(null, Typeface.BOLD);
        p.setTextColor(Color.parseColor("#0F766E"));
        p.setPadding(0, dp(6), 0, 0);
        card.addView(p);

        TextView d = new TextView(this);
        d.setText(desc);
        d.setTextSize(13f);
        d.setTextColor(Color.parseColor("#64748B"));
        d.setPadding(0, dp(4), 0, 0);
        card.addView(d);

        TextView link = new TextView(this);
        link.setText("درخواست از پشتیبانی تلگرام ←");
        link.setTextSize(14f);
        link.setTypeface(null, Typeface.BOLD);
        link.setTextColor(Color.parseColor("#2563EB"));
        link.setPadding(0, dp(12), 0, 0);
        link.setOnClickListener(v -> openTelegram(tgMessage));
        card.addView(link);
        return card;
    }


    /** کادر کوچک زیر پلن‌های پرمیوم */
    private TextView premiumDiffChip() {
        TextView chip = new TextView(this);
        chip.setText("تفاوت نسخه عادی و پرمیوم ←");
        chip.setTextSize(13f);
        chip.setTypeface(null, Typeface.BOLD);
        chip.setTextColor(Color.parseColor("#0F766E"));
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#ECFDF5"));
        bg.setCornerRadius(dp(12));
        bg.setStroke(dp(1), Color.parseColor("#99F6E4"));
        chip.setBackground(bg);
        chip.setClickable(true);
        chip.setFocusable(true);
        chip.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try { showPremiumDiffDialog(); } catch (Throwable ignored) {}
            }
        });
        return chip;
    }

    /** پاپ‌آپ توضیح تفاوت عادی / پرمیوم با دکمه ضربدر */
    private void showPremiumDiffDialog() {
        final Dialog dialog = new Dialog(this);
        try {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Throwable ignored) {}
        dialog.setCancelable(true);

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setPadding(dp(18), dp(16), dp(18), dp(18));
        GradientDrawable shellBg = new GradientDrawable();
        shellBg.setColor(Color.WHITE);
        shellBg.setCornerRadius(dp(18));
        shellBg.setStroke(dp(1), Color.parseColor("#E2E8F0"));
        shell.setBackground(shellBg);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("عادی و پرمیوم");
        title.setTextSize(17f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#0F172A"));
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, -2, 1f);
        header.addView(title, tLp);

        TextView close = new TextView(this);
        close.setText("✕");
        close.setTextSize(22f);
        close.setTypeface(null, Typeface.BOLD);
        close.setTextColor(Color.parseColor("#64748B"));
        close.setPadding(dp(12), dp(4), dp(4), dp(4));
        close.setClickable(true);
        close.setFocusable(true);
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try { dialog.dismiss(); } catch (Throwable ignored) {}
            }
        });
        header.addView(close);
        shell.addView(header);

        TextView body = new TextView(this);
        body.setText(
                "نسخه عادی\n"
                + "• خواندن درخواست اسنپ و کارت تحلیل سبز / زرد / قرمز\n"
                + "• محاسبه درآمد ساعتی، کارمزد، سوخت و استهلاک\n"
                + "• گزارش‌ها و آمار سفرها\n"
                + "• تنظیم کف تحلیلی، هدف و مدت نمایش کارت\n\n"
                + "نسخه پرمیوم (علاوه بر همهٔ امکانات عادی)\n"
                + "• ژئوکد نشان برای مبدأ و مقصد\n"
                + "• تشخیص محدوده شهری (چند استان)\n"
                + "• لایه مسیر خالی / برگشت (H_shift) در تحلیل\n"
                + "• نمایش وضعیت ژئوکد در جزئیات\n\n"
                + "اگر بیشتر در شهر و مسیرهای بین‌شهری کار می‌کنید، پرمیوم دقیق‌تر کمک می‌کند."
        );
        body.setTextSize(14f);
        body.setTextColor(Color.parseColor("#334155"));
        body.setLineSpacing(dp(4), 1f);
        body.setPadding(0, dp(12), 0, dp(8));
        shell.addView(body);

        dialog.setContentView(shell);
        try {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                int width = Math.round(getResources().getDisplayMetrics().widthPixels * 0.92f);
                dialog.getWindow().setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
            }
        } catch (Throwable ignored) {}
        try {
            Fonts.INSTANCE.applyRecursive(shell);
        } catch (Throwable ignored) {}
        try {
            dialog.show();
        } catch (Throwable e) {
            Toast.makeText(this, "نمایش توضیحات ممکن نشد", Toast.LENGTH_SHORT).show();
        }
    }

    private void openTelegram(String message) {
        try {
            String url = RemoteConfig.primarySupportUrl(this);
            if (message != null && message.length() > 0 && url.startsWith("https://t.me/")) {
                url = url + (url.contains("?") ? "&" : "?") + "text=" + Uri.encode(message);
            }
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Throwable e) {
            Toast.makeText(this, "تلگرام باز نشد. آیدی: " + LicenseStore.TELEGRAM_ID, Toast.LENGTH_LONG).show();
        }
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(16), dp(16), dp(16));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), Color.parseColor("#E2E8F0"));
        c.setBackground(bg);
        return c;
    }

    private void addSpace(LinearLayout root, int dpv) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(dpv)));
        root.addView(v);
    }

    private String formatPrice(int amount) {
        return NumberFormat.getNumberInstance(new Locale("fa", "IR")).format(amount);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
