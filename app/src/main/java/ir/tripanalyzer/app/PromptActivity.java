package ir.tripanalyzer.app;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * کارت شناور شبیه تحلیل (سبز/زرد/قرمز) — نه تمام‌صفحه.
 * روی اسنپ به‌صورت کادر وسط صفحه می‌آید.
 */
public class PromptActivity extends Activity {

    public static final String EXTRA_MODE = "mode";
    public static final String MODE_REDUCE = "reduce";
    public static final String MODE_CONFIRM = "confirm";
    public static final String EXTRA_TS = "ts";
    public static final String EXTRA_FARE = "fare";
    public static final int NOTIF_REDUCE = 71001;
    public static final int NOTIF_CONFIRM = 71002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        // مثل VerdictOverlay — نشان روی قفل / روشن کردن صفحه بدون بلعیدن کل UI
        window.addFlags(524288);   // SHOW_WHEN_LOCKED
        window.addFlags(2097152);  // TURN_SCREEN_ON
        window.addFlags(128);      // KEEP_SCREEN_ON
        window.addFlags(67108864); // LAYOUT_IN_SCREEN

        String mode = getIntent() != null ? getIntent().getStringExtra(EXTRA_MODE) : null;
        if (MODE_REDUCE.equals(mode)) {
            showReduceCard();
        } else if (MODE_CONFIRM.equals(mode)) {
            long ts = getIntent().getLongExtra(EXTRA_TS, 0L);
            double fare = getIntent().getDoubleExtra(EXTRA_FARE, 0);
            showConfirmCard(ts, fare);
        } else {
            finish();
        }
    }

    private void showReduceCard() {
        double base = Store.INSTANCE.getBaseTargetHourly(this);
        long reduced = Math.round(base * 0.9);
        String body = "۳ رد پشت‌سرهم\nهدف ۱۰٪ کم شود؟\n≈ " + fa(reduced) + " تومان";

        setContentView(overlayCard(
                0xFF2563EB, // آبی
                0xFFFFFFFF,
                "کاهش موقت هدف",
                body,
                "بله، کم کن",
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Store.INSTANCE.clearTempTarget(PromptActivity.this); /* 10% reduce removed */
                        cancelNotif(NOTIF_REDUCE);
                        toast("هدف موقتاً ۱۰٪ کم شد");
                        finish();
                    }
                },
                "خیر",
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Store.INSTANCE.resetConsecutiveRejects(PromptActivity.this);
                        cancelNotif(NOTIF_REDUCE);
                        finish();
                    }
                },
                0 // بدون auto-dismiss
        ));
    }

    private void showConfirmCard(final long ts, final double fare) {
        String body = "قیمت: " + fa((long) fare) + " تومان\nاین سفر را رفتی؟";

        setContentView(overlayCard(
                0xFF0F172A, // تیره شبیه کارت جزئیات
                0xFFFFFFFF,
                "ثبت در گزارش",
                body,
                "رفتم",
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (ts == 0L || Store.INSTANCE.getPendingConfirmTs(PromptActivity.this) == ts) {
                            Store.INSTANCE.saveToRecentFromLast(PromptActivity.this);
                            Store.INSTANCE.clearTempTarget(PromptActivity.this);
                            Store.INSTANCE.resetConsecutiveRejects(PromptActivity.this);
                            Store.INSTANCE.clearPendingConfirm(PromptActivity.this);
                            toast("در گزارش ثبت شد");
                        }
                        cancelNotif(NOTIF_CONFIRM);
                        finish();
                    }
                },
                "نرفتم",
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Store.INSTANCE.clearPendingConfirm(PromptActivity.this);
                        cancelNotif(NOTIF_CONFIRM);
                        finish();
                    }
                },
                5000 // ۵ ثانیه
        ));
    }

    /**
     * قاب شفاف + کارت وسط — پس‌زمینه تیرهٔ کامل ندارد تا صفحه اسنپ دیده شود.
     */
    private View overlayCard(int bgColor, int textColor, String title, String body,
                             String yesLabel, View.OnClickListener yesClick,
                             String noLabel, View.OnClickListener noClick,
                             long autoDismissMs) {
        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundColor(0x00000000); // شفاف

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        int pad = dp(20);
        card.setPadding(pad, dp(18), pad, dp(18));

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(dp(16));
        card.setBackground(gd);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(300), FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        card.setLayoutParams(lp);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(20f);
        t.setTextColor(textColor);
        t.setGravity(Gravity.CENTER);
        try { t.setTypeface(Fonts.INSTANCE.regular()); } catch (Exception ignored) {}
        card.addView(t);

        TextView b = new TextView(this);
        b.setText(body);
        b.setTextSize(15f);
        b.setTextColor(textColor);
        b.setGravity(Gravity.CENTER);
        b.setPadding(0, dp(10), 0, dp(16));
        try { b.setTypeface(Fonts.INSTANCE.regular()); } catch (Exception ignored) {}
        card.addView(b);

        // دکمه رفتم / بله
        TextView yes = new TextView(this);
        yes.setText(yesLabel);
        yes.setTextSize(16f);
        yes.setTextColor(0xFF0F172A);
        yes.setGravity(Gravity.CENTER);
        yes.setPadding(dp(12), dp(12), dp(12), dp(12));
        GradientDrawable yesBg = new GradientDrawable();
        yesBg.setColor(0xFFFFFFFF);
        yesBg.setCornerRadius(dp(12));
        yes.setBackground(yesBg);
        yes.setOnClickListener(yesClick);
        LinearLayout.LayoutParams yesLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        yesLp.bottomMargin = dp(8);
        card.addView(yes, yesLp);

        // دکمه نرفتم / خیر
        TextView no = new TextView(this);
        no.setText(noLabel);
        no.setTextSize(15f);
        no.setTextColor(textColor);
        no.setGravity(Gravity.CENTER);
        no.setPadding(dp(10), dp(10), dp(10), dp(10));
        GradientDrawable noBg = new GradientDrawable();
        noBg.setColor(0x33FFFFFF);
        noBg.setCornerRadius(dp(12));
        no.setBackground(noBg);
        no.setOnClickListener(noClick);
        card.addView(no);

        // ضربه بیرون کارت = بستن بدون ثبت
        frame.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { /* بلع کلیک */ }
        });

        frame.addView(card);

        if (autoDismissMs > 0) {
            frame.postDelayed(new Runnable() {
                @Override public void run() {
                    if (!isFinishing()) {
                        try { Store.INSTANCE.clearPendingConfirm(PromptActivity.this); } catch (Exception ignored) {}
                        cancelNotif(NOTIF_CONFIRM);
                        finish();
                    }
                }
            }, autoDismissMs);
        }
        return frame;
    }

    private void cancelNotif(int id) {
        try {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.cancel(id);
        } catch (Exception ignored) {}
    }

    private void toast(String s) {
        try { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); } catch (Exception ignored) {}
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private static String fa(long n) {
        try {
            return NumberFormat.getInstance(new Locale("fa")).format(n);
        } catch (Exception e) {
            return String.valueOf(n);
        }
    }
}
