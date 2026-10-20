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
    private Button googleBtn;
    private android.widget.EditText phoneEt;
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
            if (statusTv != null) statusTv.setText("منتظر تأیید ورود…");
            startPolling(code);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Uri d = intent == null ? null : intent.getData();
        if (d != null && "0".equals(d.getQueryParameter("ok")) && statusTv != null) {
            String e = String.valueOf(d.getQueryParameter("e"));
            String m = "denied".equals(e) ? "ورود با گوگل لغو شد"
                    : "phone_taken".equals(e) ? "این شماره قبلاً ثبت شده؛ با همان روش قبلی (مثلاً تلگرام) وارد شو"
                    : "unverified".equals(e) ? "ایمیل گوگل تأیید‌شده نیست"
                    : "expired".equals(e) ? "درخواست منقضی شد؛ دوباره امتحان کن"
                    : "خطا در ورود با گوگل؛ دوباره امتحان کن";
            getSharedPreferences(P, Context.MODE_PRIVATE).edit().clear().apply();
            statusTv.setText(m);
            busy(false);
        }
    }

    @Override
    protected void onDestroy() {
        alive = false;
        super.onDestroy();
    }

    private GradientDrawable shp(int fill, int radiusDp, int stroke) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        if (stroke != 0) g.setStroke(Math.max(1, dp(1)), stroke);
        return g;
    }

    /** ردیف مرحله: آیکون گرد + شمارهٔ کوچک + عنوان و توضیح */
    private LinearLayout stepRow(int icon, int tint, int accent, String num, String title, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        android.widget.FrameLayout fl = new android.widget.FrameLayout(this);
        android.widget.ImageView iv = new android.widget.ImageView(this);
        iv.setImageResource(icon);
        iv.setColorFilter(accent);
        iv.setPadding(dp(14), dp(14), dp(14), dp(14));
        GradientDrawable circ = new GradientDrawable();
        circ.setShape(GradientDrawable.OVAL);
        circ.setColor(tint);
        iv.setBackground(circ);
        fl.addView(iv, new android.widget.FrameLayout.LayoutParams(dp(54), dp(54)));
        TextView badge = tv(num, 11f, Color.WHITE, true);
        badge.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(accent);
        badge.setBackground(bg);
        android.widget.FrameLayout.LayoutParams blp = new android.widget.FrameLayout.LayoutParams(dp(22), dp(22));
        blp.gravity = Gravity.TOP | Gravity.END;
        fl.addView(badge, blp);
        row.addView(fl, new LinearLayout.LayoutParams(dp(58), dp(58)));
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPaddingRelative(dp(14), 0, 0, 0);
        col.addView(tv(title, 16f, 0xFF0F172A, true));
        TextView d = tv(desc, 13.5f, 0xFF475569, false);
        d.setLineSpacing(dp(3), 1f);
        d.setPadding(0, dp(4), 0, 0);
        col.addView(d);
        row.addView(col, new LinearLayout.LayoutParams(0, -2, 1f));
        return row;
    }

    private LinearLayout stepCard(int tint, int edge) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(16), dp(16), dp(16));
        c.setBackground(shp(Color.WHITE, 22, edge));
        try { c.setElevation(dp(2)); } catch (Throwable ignored) {}
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(14);
        c.setLayoutParams(lp);
        return c;
    }

    private void buildUi() {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(0xFFF1F5F9);

        // ── هدر با گرادیان آبی برند رایدو ──
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.VERTICAL);
        top.setPadding(dp(22), dp(34), dp(22), dp(30));
        float rr = dp(28);
        GradientDrawable hg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF0F172A, 0xFF1E3A8A, 0xFF2563EB});
        hg.setCornerRadii(new float[]{0, 0, 0, 0, rr, rr, rr, rr});
        top.setBackground(hg);
        top.addView(tv("RAIDO · ورود", 12f, 0xCCFFFFFF, false));
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        titleRow.setPadding(0, dp(12), 0, 0);
        android.widget.ImageView tIc = new android.widget.ImageView(this);
        tIc.setImageResource(R.drawable.ic_lg_send);
        tIc.setColorFilter(0xFF2563EB);
        tIc.setPadding(dp(10), dp(10), dp(10), dp(10));
        GradientDrawable tc = new GradientDrawable();
        tc.setShape(GradientDrawable.OVAL);
        tc.setColor(0xFFFFFFFF);
        tIc.setBackground(tc);
        titleRow.addView(tIc, new LinearLayout.LayoutParams(dp(42), dp(42)));
        TextView h1 = tv("ورود به رایدو", 24f, Color.WHITE, true);
        h1.setPaddingRelative(dp(12), 0, 0, 0);
        titleRow.addView(h1);
        top.addView(titleRow);
        TextView sub = tv("با گوگل یا تلگرام · فقط یک‌بار وارد می‌شوی", 14f, 0xE6FFFFFF, false);
        sub.setPadding(0, dp(10), 0, 0);
        top.addView(sub);
        shell.addView(top);

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(16), dp(20), dp(16), dp(28));

        // ── کارت ۱: اطلاعات تماس + دو راه ورود ──
        LinearLayout card = stepCard(0xFFEFF6FF, 0xFFBFDBFE);
        card.addView(tv("اطلاعات تماس", 16f, 0xFF0F172A, true));
        TextView hint = tv("ایمیل و شمارهٔ موبایل برای پشتیبانی و جلوگیری از ثبت‌نام تکراری است.", 13f, 0xFF64748B, false);
        hint.setPadding(0, dp(4), 0, dp(10));
        card.addView(hint);
        phoneEt = field("شمارهٔ موبایل", android.text.InputType.TYPE_CLASS_PHONE);
        phoneEt.setTextDirection(View.TEXT_DIRECTION_LTR);
        phoneEt.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        phoneEt.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(14)});
        phoneEt.setTextSize(17f);
        phoneEt.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence a, int b, int c, int d) {}
            public void onTextChanged(CharSequence a, int b, int c, int d) {}
            public void afterTextChanged(android.text.Editable e) { refreshEnabled(); }
        });
        card.addView(phoneEt);
        TextView ex = tv("مثال: 09123456789 — شمارهٔ تو یک‌بار ثبت می‌شود و با آن از هر دستگاه می‌توانی دوباره وارد شوی.", 12f, 0xFF64748B, false);
        ex.setPadding(0, dp(6), 0, 0);
        card.addView(ex);

        googleBtn = new Button(this);
        googleBtn.setText("ورود با گوگل");
        googleBtn.setAllCaps(false);
        googleBtn.setTextSize(16f);
        googleBtn.setTypeface(Typeface.DEFAULT_BOLD);
        googleBtn.setTextColor(0xFF0F172A);
        googleBtn.setGravity(Gravity.CENTER);
        googleBtn.setPadding(dp(24), 0, dp(24), 0);
        googleBtn.setBackground(shp(Color.WHITE, 16, 0xFFCBD5E1));
        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(-1, dp(54));
        glp.topMargin = dp(14);
        googleBtn.setLayoutParams(glp);
        googleBtn.setOnClickListener(v -> startGoogle());
        card.addView(googleBtn);

        TextView or = tv("یا", 13f, 0xFF94A3B8, false);
        or.setGravity(Gravity.CENTER);
        or.setPadding(0, dp(10), 0, dp(2));
        card.addView(or);

        loginBtn = new Button(this);
        loginBtn.setText("ورود با تلگرام");
        loginBtn.setAllCaps(false);
        loginBtn.setTextSize(16f);
        loginBtn.setTypeface(Typeface.DEFAULT_BOLD);
        loginBtn.setTextColor(Color.WHITE);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF3B82F6, 0xFF2563EB});
        bg.setCornerRadius(dp(16));
        loginBtn.setBackground(bg);
        try {
            android.graphics.drawable.Drawable ic = getResources().getDrawable(R.drawable.ic_lg_send, getTheme()).mutate();
            ic.setTint(Color.WHITE);
            ic.setBounds(0, 0, dp(20), dp(20));
            loginBtn.setCompoundDrawablesRelative(ic, null, null, null);
            loginBtn.setCompoundDrawablePadding(dp(10));
        } catch (Throwable ignored) {}
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, dp(54));
        blp.topMargin = dp(6);
        loginBtn.setLayoutParams(blp);
        loginBtn.setPadding(dp(24), 0, dp(24), 0);
        loginBtn.setGravity(Gravity.CENTER);
        loginBtn.setOnClickListener(v -> startLogin());
        card.addView(loginBtn);

        statusTv = tv("", 14f, 0xFF1D4ED8, false);
        statusTv.setGravity(Gravity.CENTER);
        statusTv.setPadding(0, dp(12), 0, 0);
        card.addView(statusTv);

        diagTv = tv("", 11f, 0xFF94A3B8, false);
        diagTv.setGravity(Gravity.CENTER);
        diagTv.setTextDirection(View.TEXT_DIRECTION_LTR);
        diagTv.setPadding(0, dp(6), 0, 0);
        card.addView(diagTv);
        body.addView(card);

        // ── کارت ۲: امنیت و دسترسی ──
        LinearLayout c2 = stepCard(0xFFF0FDF4, 0xFFE2E8F0);
        c2.addView(stepRow(R.drawable.ic_lg_shield, 0xFFDCFCE7, 0xFF16A34A, "۲",
                "امنیت و دسترسی",
                "اطلاعاتت فقط برای شناسایی اشتراک ذخیره می‌شود. ورود با تلگرام به تلگرام روی گوشی نیاز دارد (در صورت فیلتر، با VPN)."));
        body.addView(c2);

        // ── کارت ۳: شروع سریع ──
        LinearLayout c3 = stepCard(0xFFFFFBEB, 0xFFE2E8F0);
        c3.addView(stepRow(R.drawable.ic_lg_bulb, 0xFFFEF3C7, 0xFFD97706, "۳",
                "شروع سریع",
                "پس از تأیید (گوگل یا تلگرام) خودکار وارد می‌شوی و می‌توانی تحلیل سفرهای خود را آغاز کنی."));
        body.addView(c3);

        scroll.addView(body);
        shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(shell);
        try { Fonts.INSTANCE.applyRecursive(shell); } catch (Throwable ignored) {}
        refreshEnabled();
    }

    private android.widget.EditText field(String hint, int type) {
        android.widget.EditText e = new android.widget.EditText(this);
        e.setHint(hint);
        e.setInputType(type);
        e.setSingleLine(true);
        e.setTextSize(15f);
        e.setTextColor(0xFF0F172A);
        e.setHintTextColor(0xFF94A3B8);
        e.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        e.setPaddingRelative(dp(14), 0, dp(14), 0);
        e.setBackground(shp(0xFFF8FAFC, 14, 0xFFE2E8F0));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(50));
        lp.topMargin = dp(8);
        e.setLayoutParams(lp);
        return e;
    }

    private static String enDigits(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= '\u06F0' && c <= '\u06F9') b.append((char) ('0' + (c - '\u06F0')));
            else if (c >= '\u0660' && c <= '\u0669') b.append((char) ('0' + (c - '\u0660')));
            else b.append(c);
        }
        return b.toString();
    }

    private boolean busy;

    private String cleanPhone() {
        if (phoneEt == null) return "";
        return enDigits(phoneEt.getText().toString()).replaceAll("[\\s-]", "").replaceFirst("^(\\+98|0098|98)", "0");
    }

    private void refreshEnabled() {
        boolean ok = !busy && cleanPhone().matches("^09[0-9]{9}$");
        if (loginBtn != null) { loginBtn.setEnabled(ok); loginBtn.setAlpha(ok ? 1f : 0.45f); }
        if (googleBtn != null) { googleBtn.setEnabled(ok); googleBtn.setAlpha(ok ? 1f : 0.45f); }
    }

    private void busy(boolean b) {
        busy = b;
        refreshEnabled();
    }

    private void startGoogle() {
        String phone = cleanPhone();
        if (!phone.matches("^09[0-9]{9}$")) { statusTv.setText("شمارهٔ موبایل را درست وارد کن (مثلاً 09123456789)"); return; }
        busy(true);
        statusTv.setText("در حال آماده‌سازی…");
        final String ph = phone;
        new Thread(() -> {
            try {
                JSONObject b = new JSONObject().put("device_fp", LicenseStore.INSTANCE.getDeviceFingerprint(this))
                        .put("phone", ph);
                JSONObject r = RaidoCloud.post("auth-google-start", b, null);
                String code = r.optString("code");
                String url = r.optString("url");
                int h = r.optInt("_http");
                if (h == 200 && code.length() == 16 && url.startsWith("https://accounts.google.com/")) {
                    getSharedPreferences(P, Context.MODE_PRIVATE).edit()
                            .putString("code", code).putLong("at", System.currentTimeMillis()).apply();
                    runOnUiThread(() -> {
                        statusTv.setText("منتظر تأیید گوگل…");
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        } catch (Throwable e) {
                            Toast.makeText(this, "مرورگر باز نشد", Toast.LENGTH_LONG).show();
                        }
                    });
                    startPolling(code);
                } else if (h == 503) {
                    fail("ورود با گوگل هنوز فعال نشده؛ فعلاً با تلگرام وارد شو");
                } else if (h == 429) {
                    fail("تلاش زیاد؛ کمی بعد دوباره امتحان کن");
                } else if (h == 400) {
                    fail("اطلاعات واردشده معتبر نیست");
                } else {
                    fail("خطای سرور؛ دوباره امتحان کن");
                }
            } catch (Throwable t) {
                fail("اتصال اینترنت را بررسی کن");
            }
        }, "raido-google").start();
    }

    private void startLogin() {
        if (!cleanPhone().matches("^09[0-9]{9}$")) return;
        busy(true);
        statusTv.setText("در حال آماده‌سازی…");
        new Thread(() -> {
            try {
                JSONObject b = new JSONObject().put("device_fp", LicenseStore.INSTANCE.getDeviceFingerprint(this))
                        .put("phone", cleanPhone());
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
                } else if (r.optInt("_http") == 400) {
                    fail("شمارهٔ واردشده معتبر نیست");
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
            busy(false);
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
