package ir.tripanalyzer.app;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import java.util.Locale;

/**
 * پایه Activityها — فونت ایران‌سنس + راست‌چین + پس‌زمینه صفحات (به‌جز خوش‌آمد).
 */
public abstract class BaseActivity extends Activity {

    /** صفحه خوش‌آمد پس‌زمینه خودش را می‌گذارد؛ بقیه از bg_pages استفاده می‌کنند. */
    protected boolean usePagesBackground() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyPersianLocale();
        try {
            Fonts.INSTANCE.init(this);
        } catch (Exception ignored) {
        }
        if (usePagesBackground()) {
            try {
                if (getWindow() != null) {
                    getWindow().setBackgroundDrawableResource(R.drawable.bg_pages);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void applyPersianLocale() {
        try {
            Locale locale = new Locale("fa", "IR");
            Locale.setDefault(locale);
            Configuration config = new Configuration(getResources().getConfiguration());
            config.setLocale(locale);
            config.setLayoutDirection(locale);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
            if (getWindow() != null && getWindow().getDecorView() != null) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setContentView(View v) {
        if (v != null) {
            v.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            v.setTextDirection(View.TEXT_DIRECTION_RTL);
        }
        super.setContentView(v);
        try {
            Fonts.INSTANCE.applyRecursive(v);
            forceRtlRecursive(v);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        try {
            View root = findViewById(android.R.id.content);
            if (root != null) {
                root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                Fonts.INSTANCE.applyRecursive(root);
                forceRtlRecursive(root);
            }
        } catch (Exception ignored) {
        }
    }

    private void forceRtlRecursive(View root) {
        if (root == null) return;
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        if (root instanceof android.widget.TextView) {
            android.widget.TextView tv = (android.widget.TextView) root;
            tv.setTextDirection(View.TEXT_DIRECTION_RTL);
            int hg = tv.getGravity() & android.view.Gravity.HORIZONTAL_GRAVITY_MASK;
            tv.setTextAlignment(hg == android.view.Gravity.CENTER_HORIZONTAL
                    ? View.TEXT_ALIGNMENT_CENTER : View.TEXT_ALIGNMENT_VIEW_START);
        }
        if (root instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) root;
            for (int i = 0; i < g.getChildCount(); i++) {
                forceRtlRecursive(g.getChildAt(i));
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (App.INSTANCE != null) {
                // current tracked via lifecycle
            }
            View root = findViewById(android.R.id.content);
            if (root != null) Fonts.INSTANCE.applyRecursive(root);
        } catch (Exception ignored) {}
    }

}
