package ir.tripanalyzer.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ورود با ربات تلگرام: کاربر شمارهٔ خودش را در ربات تأیید می‌کند (شمارهٔ یونیک).
 * اپ فقط هر ~۲.۵ ثانیه تا ۱۰ دقیقه وضعیت را می‌پرسد و بعد متوقف می‌شود.
 */
public final class LoginActivity extends BaseActivity {
    private static final String P = "raido_login_pending";
    private static final long TTL_MS = 10 * 60 * 1000L;

    private TextView statusTv;
    private TextView diagTv;
    private Button loginBtn;
    private final AtomicBoolean polling = new AtomicBoolean(false);
    private volatile boolean alive = true;

    @Override
    protected boolean usePagesBackground() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LicenseStore.INSTANCE.isLoggedIn(this)) { goMain(); return; }
        buildUi();
        String why = LicenseStore.INSTANCE.consumeLogoutReason(this);
        if (!why.isEmpty() && statusTv != null) {
            switch (why) {
                case "other_device": statusTv.setText("با این شماره در دستگاه دیگری وارد شده‌اند؛ برای ادامه دوباره وارد شو."); break;
                case "banned": statusTv.setText("این حساب مسدود شده است. با پشتیبانی تماس بگیر: " + LicenseStore.TELEGRAM_ID); break;
                case "logout": break;
                default: statusTv.setText("نشست منقضی شد؛ دوباره وارد شو."); break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // بازگشت از تلگرام (یا ادامهٔ درخواست نیمه‌کاره)
        SharedPreferences sp = getSharedPreferences(P, Context.MODE_PRIVATE);
        String code = sp.getString("code", null);
        long at = sp.getLong("at", 0L);
        if (code != null && System.currentTimeMillis() - at < TTL_MS && !LicenseStore.INSTANCE.isLoggedIn(this)) {
            if (statusTv != null) statusTv.setText("منتظر تأیید در تلگرام…");
            startPolling(code);
        }
    }

    @Override
    protected void onDestroy() {
        alive = false;
        super.onDestroy();
    }

    private void buildUi() {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(0xFFF0FDFA);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.VERTICAL);
        top.setPadding(dp(18), dp(26), dp(18), dp(26));
        top.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF0B3D3A, 0xFF134E4A, 0xFF0F766E}));
        top.addView(tv("RAIDO · ورود", 12f, 0xCCFFFFFF, false));
        TextView h1 = tv("ورود با تلگرام", 22f, Color.WHITE, true);
        h1.setPadding(0, dp(8), 0, 0);
        top.addView(h1);
        TextView sub = tv("فقط یک‌بار · شمارهٔ موبایلت در ربات تلگرام تأیید می‌شود", 13f, 0xE6FFFFFF, false);
        sub.setPadding(0, dp(8), 0, 0);
        top.addView(sub);
        shell.addView(top);

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(16), dp(14), dp(28));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(16), dp(15), dp(16));
        GradientDrawable cg = new GradientDrawable();
        cg.setColor(Color.WHITE);
        cg.setCornerRadius(dp(20));
        cg.setStroke(dp(1), 0xFF99F6E4);
        card.setBackground(cg);

        TextView steps = tv("۱) دکمهٔ زیر را بزن تا تلگرام باز شود\n"
                + "۲) در ربات «Start» و بعد «تأیید شمارهٔ من» را بزن\n"
                + "۳) به رایدو برگرد؛ خودکار وارد می‌شوی", 14f, 0xFF0F172A, false);
        steps.setLineSpacing(0, 1.3f);
        card.addView(steps);

        loginBtn = new Button(this);
        loginBtn.setText("ورود با تلگرام");
        loginBtn.setAllCaps(false);
        loginBtn.setTextSize(15f);
        loginBtn.setTypeface(Typeface.DEFAULT_BOLD);
        loginBtn.setTextColor(Color.WHITE);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF14B8A6, 0xFF0F766E});
        bg.setCornerRadius(dp(14));
        loginBtn.setBackground(bg);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, -2);
        blp.topMargin = dp(16);
        loginBtn.setLayoutParams(blp);
        loginBtn.setPadding(dp(12), dp(14), dp(12), dp(14));
        loginBtn.setOnClickListener(v -> startLogin());
        card.addView(loginBtn);

        statusTv = tv("", 13f, 0xFF0F766E, false);
        statusTv.setGravity(Gravity.CENTER);
        statusTv.setPadding(0, dp(12), 0, 0);
        card.addView(statusTv);

        diagTv = tv("", 11f, 0xFF94A3B8, false);
        diagTv.setGravity(Gravity.CENTER);
        diagTv.setTextDirection(View.TEXT_DIRECTION_LTR);
        diagTv.setPadding(0, dp(6), 0, 0);
        card.addView(diagTv);

        body.addView(card);

        TextView note = tv("شمارهٔ تو فقط برای شناسایی اشتراک ذخیره می‌شود.\nتلگرام روی گوشی لازم است (در صورت فیلتر، با VPN).",
                12f, 0xFF64748B, false);
        note.setPadding(dp(4), dp(14), dp(4), 0);
        body.addView(note);

        scroll.addView(body);
        shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(shell);
        try { Fonts.INSTANCE.applyRecursive(shell); } catch (Throwable ignored) {}
    }

    private void startLogin() {
        loginBtn.setEnabled(false);
        statusTv.setText("در حال آماده‌سازی…");
        new Thread(() -> {
            try {
                JSONObject b = new JSONObject().put("device_fp", LicenseStore.INSTANCE.getDeviceFingerprint(this));
                JSONObject r = RaidoCloud.post("auth-start", b, null);
                String code = r.optString("code");
                String url = r.optString("bot_url");
                if (r.optInt("_http") == 200 && code.length() == 16 && url.startsWith("https://t.me/")) {
                    getSharedPreferences(P, Context.MODE_PRIVATE).edit()
                            .putString("code", code).putLong("at", System.currentTimeMillis()).apply();
                    runOnUiThread(() -> {
                        statusTv.setText("منتظر تأیید در تلگرام…");
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        } catch (Throwable e) {
                            Toast.makeText(this, "تلگرام باز نشد؛ لینک را در مرورگر باز کن", Toast.LENGTH_LONG).show();
                        }
                    });
                    startPolling(code);
                } else if (r.optInt("_http") == 429) {
                    fail("تلاش زیاد؛ کمی بعد دوباره امتحان کن");
                } else {
                    fail("خطای سرور؛ دوباره امتحان کن");
                }
            } catch (Throwable t) {
                fail("اتصال اینترنت را بررسی کن");
            }
        }, "raido-login").start();
    }

    private void diag(final String t) {
        runOnUiThread(() -> { if (diagTv != null) diagTv.setText(t); });
    }

    private void fail(String msg) {
        runOnUiThread(() -> {
            if (statusTv != null) statusTv.setText(msg);
            if (loginBtn != null) loginBtn.setEnabled(true);
        });
    }

    private void startPolling(final String code) {
        if (!polling.compareAndSet(false, true)) return;
        new Thread(() -> {
            try {
                long deadline = System.currentTimeMillis() + TTL_MS;
                String fp = LicenseStore.INSTANCE.getDeviceFingerprint(this);
                while (alive && System.currentTimeMillis() < deadline) {
                    Thread.sleep(2500);
                    try {
                        JSONObject r = RaidoCloud.post("auth-poll",
                                new JSONObject().put("code", code).put("device_fp", fp), null);
                        String st = r.optString("status");
                        diag("HTTP " + r.optInt("_http") + " | " + (st.isEmpty() ? "-" : st)
                                + (r.has("error") ? " | " + r.optString("error") : ""));
                        if ("ok".equals(st)) {
                            boolean saved = LicenseStore.INSTANCE.saveLogin(this,
                                    r.optString("token"), r.optString("payload"), r.optString("sig"));
                            getSharedPreferences(P, Context.MODE_PRIVATE).edit().clear().apply();
                            if (saved) {
                                runOnUiThread(this::goMain);
                            } else {
                                diag("signature/fp check failed");
                                fail("خطای تأیید امنیتی؛ برنامه را به‌روز کن");
                            }
                            return;
                        }
                        if ("banned".equals(st)) {
                            getSharedPreferences(P, Context.MODE_PRIVATE).edit().clear().apply();
                            fail("این حساب مسدود شده است. با پشتیبانی تماس بگیر: " + LicenseStore.TELEGRAM_ID);
                            return;
                        }
                        if ("expired".equals(st)) {
                            getSharedPreferences(P, Context.MODE_PRIVATE).edit().clear().apply();
                            fail("درخواست منقضی شد؛ دوباره دکمهٔ ورود را بزن");
                            return;
                        }
                    } catch (Throwable ex) {
                        // خطای موقت شبکه: ادامه
                        diag("net: " + ex.getClass().getSimpleName() + " " + String.valueOf(ex.getMessage()));
                    }
                }
                if (alive) {
                    getSharedPreferences(P, Context.MODE_PRIVATE).edit().clear().apply();
                    fail("زمان تمام شد؛ دوباره دکمهٔ ورود را بزن");
                }
            } catch (InterruptedException ignored) {
            } finally {
                polling.set(false);
            }
        }, "raido-poll").start();
    }

    private void goMain() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private TextView tv(String t, float size, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(t);
        v.setTextSize(size);
        v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT_BOLD);
        return v;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
