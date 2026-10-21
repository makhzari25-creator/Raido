package ir.tripanalyzer.app;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** راهنمای استفاده — محتوا از سرور (پنل ← راهنمای استفاده)، با نسخهٔ پیش‌فرض داخل اپ */
public final class HelpActivity extends BaseActivity {

    private LinearLayout list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(PageKit.BG);
        shell.addView(PageKit.header(this, "راهنمای رایدو", "از نصب تا تحلیل هر درخواست"), new LinearLayout.LayoutParams(-1, -2));

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
        List<RemoteConfig.Section> secs = RemoteConfig.guide(this);
        if (secs == null) secs = defaults();
        int i = 0;
        for (RemoteConfig.Section s : secs) {
            LinearLayout card = PageKit.card(this);
            card.addView(PageKit.sectionTitle(this, s.title));
            if (s.body != null && !s.body.isEmpty()) card.addView(PageKit.body(this, s.body));
            list.addView(card, PageKit.gap(this, i++ == 0 ? 0 : 12));
        }
        TextView foot = new TextView(this);
        foot.setText("رایدو نسخه " + BuildConfig.VERSION_NAME);
        foot.setTextSize(12f);
        foot.setTextColor(PageKit.BLUE);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(0, PageKit.dp(this, 22), 0, 0);
        list.addView(foot);
        try { Fonts.INSTANCE.applyRecursive(list); } catch (Throwable ignored) {}
    }

    private List<RemoteConfig.Section> defaults() {
        List<RemoteConfig.Section> out = new ArrayList<>();
        try {
            InputStream in = getAssets().open("guide_default.json");
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            in.close();
            JSONArray a = new JSONArray(bo.toString("UTF-8"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                RemoteConfig.Section s = new RemoteConfig.Section();
                s.title = o.optString("title");
                s.body = o.optString("body");
                out.add(s);
            }
        } catch (Throwable ignored) {}
        return out;
    }
}
