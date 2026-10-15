package ir.tripanalyzer.app;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * راهنمای داخل‌برنامه — نسخه به‌روز با فونت وزیر و سایز بزرگ‌تر
 */
public final class HelpActivity extends BaseActivity {

    private static final int TEXT_DARK = 0xFF0F172A;
    private static final int TEXT_MUTED = 0xFF475569;
    private static final int PRIMARY = 0xFF0D5C4D;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(16), dp(18), dp(28));
        root.setBackgroundColor(0xFFF8FAFC);

        addTitle(root, "راهنمای رایدو");
        addPara(root, "رایدو به راننده اسنپ کمک می‌کند بفهمد هر درخواست به‌صرفه است یا نه — قبل از قبول سفر. کارت تحلیل رنگی روی صفحه اسنپ می‌آید.");

        addSection(root, "۱) نصب و ورود",
                "• اپ را نصب و باز کنید.\n"
                + "• کد فعال‌سازی را از پشتیبانی تلگرام بگیرید و وارد کنید.\n"
                + "• برند خودرو، کارکرد، مصرف سوخت و قیمت بنزین را در تنظیمات وارد کنید.\n"
                + "• کف تحلیلی و هدف درآمد ساعتی را تنظیم کنید.\n"
                + "• مدت نمایش کارت تحلیل را می‌توانید بین ۳ تا ۱۵ ثانیه بگذارید (پیش‌فرض ۶).");

        addSection(root, "۲) دسترسی‌ها (خیلی مهم)",
                "بدون این‌ها رایدو درخواست اسنپ را نمی‌بیند:\n\n"
                + "• دسترسی‌پذیری (Accessibility)\n"
                + "  Settings > Accessibility > Installed apps > RAIDO → روشن\n\n"
                + "• دسترسی اعلان‌ها (Notification access)\n\n"
                + "• نمایش روی سایر برنامه‌ها (Appear on top)\n"
                + "  اگر در لیست نبود: App info > Other permissions > Appear on top\n\n"
                + "• موقعیت (GPS) برای مسافت واقعی\n\n"
                + "روی سامسونگ و اندروید ۱۳+: گزینه Manage app if unused را خاموش کنید تا دسترسی‌ها قطع نشوند.\n"
                + "شیائومی/هایپراواس: محدودیت باتری پس‌زمینه را بردارید.");

        addSection(root, "۳) صفحه اصلی (داشبورد)",
                "• وضعیت دسترسی‌ها (فعال / غیرفعال)\n"
                + "• راهنمای «مشاهده جزئیات» — حدود ۲ دقیقه باز است؛ بعد جمع می‌شود. با زدن ⚠ دوباره باز می‌شود.\n"
                + "• مسافت واقعی GPS و صفر کردن\n"
                + "• کلید تحلیل خودکار سفرها\n"
                + "• آخرین نتیجه تحلیل و دکمه مشاهده جزئیات\n"
                + "• منو: تنظیمات، گزارش‌ها، راهنما، پشتیبانی");

        addSection(root, "۴) هنگام درخواست اسنپ",
                "۱. درخواست در اسنپ می‌آید.\n"
                + "۲. روی «مشاهده جزئیات» در اسنپ بزنید تا کیلومتر واقعی خوانده شود.\n"
                + "۳. کارت تحلیل رایدو روی صفحه می‌آید (سبز / زرد / قرمز).\n"
                + "۴. مدت نمایش کارت را از تنظیمات عوض کنید؛ با لمس صفحه زودتر بسته می‌شود.\n"
                + "۵. اگر سفر را می‌روید، ثبت در گزارش را تأیید کنید.");

        addSection(root, "منطق تحلیل",
                "• معیار رنگ = درآمد ساعتی بعد از کمیسیون اسنپ\n"
                + "• زیر کف تحلیلی → قرمز\n"
                + "• تا حدود ۱٫۲ برابر کف → زرد / نزدیک هدف با احتیاط\n"
                + "• بالاتر و نزدیک هدف ایده‌آل → سبز (پررنگ‌تر نزدیک هدف)\n"
                + "• بنزین و استهلاک در جزئیات و سود خالص دیده می‌شوند\n"
                + "• کاربران پرمیوم: لایه مسیر خالی (H_shift) با ژئوکد نشان و محدوده شهری");

        addSection(root, "۵) تنظیمات",
                "• برند و کارکرد خودرو و استهلاک\n"
                + "• مصرف سوخت، قیمت بنزین، کارمزد\n"
                + "• کف تحلیلی و هدف درآمد ساعتی\n"
                + "• مدت نمایش کارت تحلیل (ثانیه)\n"
                + "بعد از تغییر، ذخیره تنظیمات را بزنید.");

        addSection(root, "۶) گزارش‌ها و آمار",
                "سفرهای ثبت‌شده، سود تقریبی، ترکیب تحلیل (سبز/زرد/قرمز) و میانگین‌های ساعتی روز و ماه.");

        addSection(root, "۷) اشتراک",
                "کد ورود را از پشتیبانی بگیرید. انواع: تست ۷روزه، ماهانه، سالانه، پرمیوم ماهانه/سالانه.\n"
                + "می‌توانید قبل از اتمام، با کد جدید تمدید یا به پرمیوم ارتقا دهید؛ گزارش‌ها پاک نمی‌شوند.\n"
                + "پس از انقضا برای کارت تحلیل به کد تازه نیاز است.");

        addSection(root, "۸) نکات",
                "• رایدو جایگزین اسنپ نیست؛ فقط مشاور قبول/رد است.\n"
                + "• همیشه بعد از درخواست، «مشاهده جزئیات» اسنپ را بزنید.\n"
                + "• اگر کارت نیامد: دسترسی‌ها، Appear on top و روشن بودن تحلیل را چک کنید.\n"
                + "• پشتیبانی تلگرام: @TripAnalyzerSupport");

        TextView foot = new TextView(this);
        foot.setText("رایدو — راهنمای نسخه ۲.۲ — فونت وزیر");
        foot.setTextSize(14f);
        foot.setTextColor(PRIMARY);
        foot.setPadding(0, dp(28), 0, 0);
        Fonts.INSTANCE.apply(foot, false);
        root.addView(foot);

        scroll.addView(root);
        setContentView(scroll);
        try { Fonts.INSTANCE.applyRecursive(root); } catch (Exception ignored) {}
    }

    private void addTitle(LinearLayout root, String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(24f);
        t.setTextColor(TEXT_DARK);
        t.setPadding(0, 0, 0, dp(8));
        Fonts.INSTANCE.apply(t, true);
        root.addView(t);
    }

    private void addPara(LinearLayout root, String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(16f);
        t.setTextColor(TEXT_MUTED);
        t.setLineSpacing(dp(4), 1f);
        t.setPadding(0, 0, 0, dp(16));
        Fonts.INSTANCE.apply(t, false);
        root.addView(t);
    }

    private void addSection(LinearLayout root, String heading, String body) {
        TextView h = new TextView(this);
        h.setText(heading);
        h.setTextSize(18f);
        h.setTextColor(TEXT_DARK);
        h.setPadding(0, dp(14), 0, dp(8));
        Fonts.INSTANCE.apply(h, true);
        root.addView(h);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(14));
        bg.setColor(Color.WHITE);
        bg.setStroke(dp(1), 0xFFE2E8F0);
        card.setBackground(bg);

        TextView b = new TextView(this);
        b.setText(body);
        b.setTextSize(16f);
        b.setTextColor(TEXT_MUTED);
        b.setLineSpacing(dp(6), 1f);
        Fonts.INSTANCE.apply(b, false);
        card.addView(b);
        root.addView(card);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
