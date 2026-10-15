package ir.tripanalyzer.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** درباره و پشتیبانی — راهنما + تلگرام + حریم خصوصی */
public final class SupportActivity extends BaseActivity {

    private static final int PRIMARY = 0xFF2563EB;
    private static final int TEAL = 0xFF0F3D3A;
    private static final int TEXT_DARK = 0xFF0F172A;
    private static final int TEXT_MUTED = 0xFF64748B;
    private static final String TELEGRAM = "https://t.me/TripAnalyzerSupport";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(36));
        root.setBackgroundColor(0xFFF1F5F9);

        TextView title = new TextView(this);
        title.setText("درباره و پشتیبانی");
        title.setTextSize(22f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(TEXT_DARK);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("رایدو — مشاور جیب راننده اسنپ");
        sub.setTextSize(14f);
        sub.setTextColor(TEXT_MUTED);
        sub.setPadding(0, dp(6), 0, dp(20));
        root.addView(sub);

        root.addView(sectionCard("درباره رایدو",
                "رایدو درخواست‌های اسنپ را می‌خواند و با در نظر گرفتن کارمزد، سوخت و استهلاک واقعی ماشین، تحلیل سبز/زرد/قرمز می‌دهد تا جیب شما پرتر بماند.\n\n"
                + "محاسبات روی خود گوشی انجام می‌شود و متن اسنپ برای تحلیل محلی استفاده می‌شود."));
        space(root, 12);

        LinearLayout helpCard = whiteCard();
        helpCard.setPadding(dp(18), dp(16), dp(18), dp(16));
        TextView helpTitle = new TextView(this);
        helpTitle.setText("راهنمای استفاده");
        helpTitle.setTextSize(16f);
        helpTitle.setTypeface(Typeface.DEFAULT_BOLD);
        helpTitle.setTextColor(TEXT_DARK);
        helpCard.addView(helpTitle);
        TextView helpBody = new TextView(this);
        helpBody.setText("مراحل راه‌اندازی، دسترسی‌ها، تحلیل‌ها، ثبت گزارش و اشتراک");
        helpBody.setTextSize(13f);
        helpBody.setTextColor(TEXT_MUTED);
        helpBody.setPadding(0, dp(6), 0, dp(12));
        helpCard.addView(helpBody);
        Button helpBtn = actionButton("مشاهده راهنمای کامل", TEAL);
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(SupportActivity.this, HelpActivity.class));
            }
        });
        helpCard.addView(helpBtn);
        root.addView(helpCard);
        space(root, 12);

        LinearLayout tgCard = whiteCard();
        tgCard.setPadding(dp(18), dp(16), dp(18), dp(16));
        TextView tgTitle = new TextView(this);
        tgTitle.setText("تلگرام پشتیبانی");
        tgTitle.setTextSize(16f);
        tgTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tgTitle.setTextColor(TEXT_DARK);
        tgCard.addView(tgTitle);
        TextView tgId = new TextView(this);
        tgId.setText("@TripAnalyzerSupport");
        tgId.setTextSize(15f);
        tgId.setTextColor(PRIMARY);
        tgId.setPadding(0, dp(6), 0, dp(4));
        tgCard.addView(tgId);
        TextView tgHint = new TextView(this);
        tgHint.setText("برای سوالات، گزارش باگ یا پیشنهاد با ما در ارتباط باشید.");
        tgHint.setTextSize(13f);
        tgHint.setTextColor(TEXT_MUTED);
        tgHint.setPadding(0, 0, 0, dp(12));
        tgCard.addView(tgHint);
        Button tgBtn = actionButton("باز کردن تلگرام", PRIMARY);
        tgBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM)));
                } catch (Exception e) {
                    Toast.makeText(SupportActivity.this, "تلگرام باز نشد. آیدی: @TripAnalyzerSupport", Toast.LENGTH_LONG).show();
                }
            }
        });
        tgCard.addView(tgBtn);
        root.addView(tgCard);
        space(root, 12);

        LinearLayout privCard = whiteCard();
        privCard.setPadding(dp(18), dp(16), dp(18), dp(16));
        TextView privTitle = new TextView(this);
        privTitle.setText("حریم خصوصی");
        privTitle.setTextSize(16f);
        privTitle.setTypeface(Typeface.DEFAULT_BOLD);
        privTitle.setTextColor(TEXT_DARK);
        privCard.addView(privTitle);
        TextView privBody = new TextView(this);
        privBody.setText("وضعیت دسترسی‌ها و پاک کردن داده‌های ذخیره‌شده");
        privBody.setTextSize(13f);
        privBody.setTextColor(TEXT_MUTED);
        privBody.setPadding(0, dp(6), 0, dp(12));
        privCard.addView(privBody);
        Button privBtn = actionButton("مشاهده حریم خصوصی", 0xFF64748B);
        privBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(SupportActivity.this, PrivacyActivity.class));
            }
        });
        privCard.addView(privBtn);
        root.addView(privCard);

        TextView foot = new TextView(this);
        foot.setText("رایدو نسخه ۲٫۲٫۰");
        foot.setTextSize(12f);
        foot.setTextColor(PRIMARY);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(0, dp(28), 0, 0);
        root.addView(foot);

        scroll.addView(root);
        setContentView(scroll);
        try { Fonts.INSTANCE.applyRecursive(root); } catch (Exception ignored) {}
    }

    private LinearLayout sectionCard(String heading, String body) {
        LinearLayout card = whiteCard();
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        TextView h = new TextView(this);
        h.setText(heading);
        h.setTextSize(16f);
        h.setTypeface(Typeface.DEFAULT_BOLD);
        h.setTextColor(TEXT_DARK);
        card.addView(h);
        TextView b = new TextView(this);
        b.setText(body);
        b.setTextSize(13f);
        b.setTextColor(TEXT_MUTED);
        b.setLineSpacing(dp(4), 1f);
        b.setPadding(0, dp(8), 0, 0);
        card.addView(b);
        return card;
    }

    private LinearLayout whiteCard() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(16));
        bg.setColor(Color.WHITE);
        bg.setStroke(dp(1), 0xFFE2E8F0);
        c.setBackground(bg);
        return c;
    }

    private Button actionButton(String text, int color) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setAllCaps(false);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(14f);
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setCornerRadius(dp(12));
        btnBg.setColor(color);
        btn.setBackground(btnBg);
        btn.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        btn.setPadding(dp(12), dp(12), dp(12), dp(12));
        return btn;
    }

    private void space(LinearLayout p, int d) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(d)));
        p.addView(v);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
