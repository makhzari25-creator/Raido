package ir.tripanalyzer.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/** درباره و پشتیبانی — لینک‌های پشتیبانی از سرور (پنل ← پشتیبانی) */
public final class SupportActivity extends BaseActivity {

    private LinearLayout list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(PageKit.BG);
        shell.addView(PageKit.header(this, "درباره و پشتیبانی", "رایدو — مشاور جیب راننده اسنپ"), new LinearLayout.LayoutParams(-1, -2));
        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(PageKit.dp(this, 16), PageKit.dp(this, 16), PageKit.dp(this, 16), PageKit.dp(this, 28));
        scroll.addView(list);
        shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(shell);
        render();
        RemoteConfig.refresh(this, 30000L, this::render);
    }

    private void render() {
        list.removeAllViews();

        LinearLayout about = PageKit.card(this);
        about.addView(PageKit.sectionTitle(this, "درباره رایدو"));
        about.addView(PageKit.body(this,
                "رایدو درخواست‌های اسنپ را می‌خواند و با در نظر گرفتن کارمزد، سوخت و استهلاک واقعی ماشین، تحلیل سبز/زرد/قرمز می‌دهد تا جیب شما پرتر بماند.\n\n"
                + "محاسبات روی خود گوشی انجام می‌شود و متن اسنپ برای تحلیل محلی استفاده می‌شود."));
        list.addView(about, PageKit.gap(this, 0));

        LinearLayout help = PageKit.card(this);
        help.addView(PageKit.sectionTitle(this, "راهنمای استفاده"));
        help.addView(PageKit.body(this, "مراحل راه‌اندازی، دسترسی‌ها، تحلیل‌ها، ثبت گزارش و اشتراک"));
        TextView hb = PageKit.button(this, "مشاهده راهنمای کامل", PageKit.TEAL);
        hb.setOnClickListener(v -> startActivity(new Intent(this, HelpActivity.class)));
        help.addView(hb, PageKit.gap(this, 12));
        list.addView(help, PageKit.gap(this, 12));

        List<RemoteConfig.Link> links = RemoteConfig.supportLinks(this);
        LinearLayout sup = PageKit.card(this);
        sup.addView(PageKit.sectionTitle(this, "پشتیبانی"));
        boolean first = true;
        for (final RemoteConfig.Link l : links) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            TextView t = new TextView(this);
            t.setText(l.title);
            t.setTextSize(14.5f);
            t.setTextColor(PageKit.DARK);
            t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            item.addView(t);
            if (l.desc != null && !l.desc.isEmpty()) {
                TextView d = PageKit.body(this, l.desc);
                d.setTextColor(PageKit.MUTED);
                d.setTextSize(13f);
                d.setPadding(0, PageKit.dp(this, 2), 0, 0);
                item.addView(d);
            }
            TextView b = PageKit.button(this, "باز کردن", PageKit.BLUE);
            b.setOnClickListener(v -> open(l.url));
            item.addView(b, PageKit.gap(this, 10));
            sup.addView(item, PageKit.gap(this, first ? 0 : 16));
            first = false;
        }
        list.addView(sup, PageKit.gap(this, 12));

        LinearLayout priv = PageKit.card(this);
        priv.addView(PageKit.sectionTitle(this, "حریم خصوصی"));
        priv.addView(PageKit.body(this, "وضعیت دسترسی‌ها و پاک کردن داده‌های ذخیره‌شده"));
        TextView pb = PageKit.button(this, "مشاهده حریم خصوصی", 0xFF64748B);
        pb.setOnClickListener(v -> startActivity(new Intent(this, PrivacyActivity.class)));
        priv.addView(pb, PageKit.gap(this, 12));
        list.addView(priv, PageKit.gap(this, 12));

        TextView foot = new TextView(this);
        foot.setText("رایدو نسخه " + BuildConfig.VERSION_NAME);
        foot.setTextSize(12f);
        foot.setTextColor(PageKit.BLUE);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(0, PageKit.dp(this, 24), 0, 0);
        list.addView(foot);
        try { Fonts.INSTANCE.applyRecursive(list); } catch (Throwable ignored) {}
    }

    private void open(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Throwable e) {
            Toast.makeText(this, "لینک باز نشد", Toast.LENGTH_LONG).show();
        }
    }
}
