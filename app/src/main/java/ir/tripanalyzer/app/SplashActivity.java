package ir.tripanalyzer.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

/** اسپلش: لوگوی متحرک رایدو (~۱ ثانیه) و بعد: کاربر واردشده → خانه، وگرنه → صفحهٔ ورود */
public final class SplashActivity extends Activity {
    private boolean went = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF070B10);
        LogoAnimView logo = new LogoAnimView(this);
        int w = Math.round(getResources().getDisplayMetrics().density * 190);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, w);
        lp.gravity = android.view.Gravity.CENTER;
        root.addView(logo, lp);
        setContentView(root);
        logo.play(this::go);
        // اطمینان: اگر انیمیشن به هر دلیل تمام نشد
        root.postDelayed(this::go, 2000);
    }

    private void go() {
        if (went || isFinishing()) return;
        went = true;
        boolean in = false;
        try { in = LicenseStore.INSTANCE.isLoggedIn(this); } catch (Throwable ignored) {}
        Intent i = new Intent(this, in ? MainActivity.class : LoginActivity.class);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
