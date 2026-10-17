package ir.tripanalyzer.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

/**
 * لایسنس RAIDO v3 — مبتنی بر سرور (Supabase).
 * وضعیت اشتراک را سرور با امضای RSA صادر می‌کند؛ اپ فقط امضا را با کلید عمومی تأیید می‌کند.
 * هیچ راز/الگوریتم تولید کد داخل APK نیست. تحلیل بدون اینترنت هم کار می‌کند (تا پایان مهلت آفلاین).
 */
public final class LicenseStore {
    public static final LicenseStore INSTANCE = new LicenseStore();

    private static final String PREFS = "raido_cloud_license";
    private static final String K_TOKEN = "token";
    private static final String K_PAYLOAD = "payload";
    private static final String K_SIG = "sig";
    private static final String K_DEVICE_FP = "device_fp";
    private static final String K_ANC_EL = "anc_el";
    private static final String K_ANC_SRV = "anc_srv";
    private static final String K_ANC_BOOT = "anc_boot";

    public static final long TRIAL_DAYS = 7L;
    public static final int PRICE_MONTHLY = 49000;
    public static final int PRICE_YEARLY = 499000;
    public static final int PRICE_PREMIUM_MONTHLY = 73500;
    public static final int PRICE_PREMIUM_YEARLY = 748500;
    public static final String TELEGRAM_SUPPORT = "https://t.me/TripAnalyzerSupport";
    public static final String TELEGRAM_ID = "@TripAnalyzerSupport";

    private static final class Status {
        String plan = "NONE";
        boolean premium, trialUsed;
        long expiresAt, serverTime, offlineUntil;
    }

    private volatile String memPayload, memSig;
    private volatile Status memStatus;

    private LicenseStore() {}

    private SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ─── دستگاه ───
    public String getDeviceFingerprint(Context c) {
        SharedPreferences p = prefs(c);
        String cached = p.getString(K_DEVICE_FP, null);
        if (cached != null && !cached.isEmpty()) return cached;
        String id = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (id == null || id.isEmpty()) id = "unknown";
        String fp = sha1Short(id + "|RAIDO|2026");
        p.edit().putString(K_DEVICE_FP, fp).apply();
        return fp;
    }

    private static String sha1Short(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-1").digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) sb.append(String.format("%02X", d[i] & 0xFF));
            return sb.toString();
        } catch (Exception e) {
            return "ERR00000";
        }
    }

    // ─── ورود / توکن ───
    public boolean isLoggedIn(Context c) {
        String t = prefs(c).getString(K_TOKEN, null);
        return t != null && t.length() >= 32;
    }

    public String getToken(Context c) {
        return prefs(c).getString(K_TOKEN, null);
    }

    public void logout(Context c) {
        prefs(c).edit().clear().apply();
        memPayload = null; memSig = null; memStatus = null;
    }

    /** ذخیرهٔ ورود موفق (توکن + وضعیت امضاشده). */
    public boolean saveLogin(Context c, String token, String payload, String sig) {
        if (token == null || token.length() < 32) return false;
        if (!saveStatus(c, payload, sig)) return false;
        prefs(c).edit().putString(K_TOKEN, token).apply();
        return true;
    }

    private static int bootCount(Context c) {
        try {
            return Settings.Global.getInt(c.getContentResolver(), Settings.Global.BOOT_COUNT, -1);
        } catch (Throwable t) {
            return -1;
        }
    }

    /** ذخیرهٔ وضعیت فقط اگر امضا معتبر و متعلق به همین دستگاه باشد. */
    public boolean saveStatus(Context c, String payload, String sig) {
        Status s = parse(c, payload, sig);
        if (s == null) return false;
        prefs(c).edit()
                .putString(K_PAYLOAD, payload).putString(K_SIG, sig)
                .putLong(K_ANC_SRV, s.serverTime)
                .putLong(K_ANC_EL, SystemClock.elapsedRealtime())
                .putInt(K_ANC_BOOT, bootCount(c))
                .apply();
        memPayload = payload; memSig = sig; memStatus = s;
        return true;
    }

    private Status parse(Context c, String payload, String sig) {
        try {
            if (!RaidoCloud.verify(payload, sig)) return null;
            JSONObject j = new JSONObject(payload);
            if (!getDeviceFingerprint(c).equals(j.optString("fp"))) return null;
            Status s = new Status();
            s.plan = j.optString("plan", "NONE");
            s.premium = j.optBoolean("premium", false);
            s.trialUsed = j.optBoolean("trial_used", false);
            s.expiresAt = j.optLong("expires_at", 0L);
            s.serverTime = j.optLong("server_time", 0L);
            s.offlineUntil = j.optLong("offline_until", 0L);
            return s;
        } catch (Throwable t) {
            return null;
        }
    }

    /** وضعیت کش‌شده؛ امضا فقط یک‌بار به‌ازای هر مقدار تأیید می‌شود (سریع برای مسیر تحلیل). */
    private Status status(Context c) {
        SharedPreferences p = prefs(c);
        String payload = p.getString(K_PAYLOAD, null);
        String sig = p.getString(K_SIG, null);
        if (payload == null || sig == null) return null;
        if (payload.equals(memPayload) && sig.equals(memSig)) return memStatus;
        Status s = parse(c, payload, sig);
        memPayload = payload; memSig = sig; memStatus = s;
        return s;
    }

    /** زمان مؤثر: بر پایهٔ ساعت سرور + elapsedRealtime؛ تغییر ساعت گوشی اثری ندارد. */
    private long effectiveNow(Context c) {
        SharedPreferences p = prefs(c);
        long srv = p.getLong(K_ANC_SRV, 0L);
        long anc = p.getLong(K_ANC_EL, 0L);
        long el = SystemClock.elapsedRealtime();
        int boot = p.getInt(K_ANC_BOOT, -1);
        boolean sameBoot = boot == bootCount(c) && el >= anc;
        if (srv > 0 && sameBoot) return srv + (el - anc);
        return Math.max(System.currentTimeMillis(), srv);
    }

    // ─── وضعیت ───
    public boolean isActivationActive(Context c) {
        Status s = status(c);
        if (s == null) return false;
        long n = effectiveNow(c);
        return n < s.expiresAt && n < s.offlineUntil;
    }

    public boolean isPremium(Context c) {
        Status s = status(c);
        return s != null && s.premium && isActivationActive(c);
    }

    public boolean hasEverActivated(Context c) {
        Status s = status(c);
        return s != null && !"NONE".equals(s.plan);
    }

    public boolean isActivationExpired(Context c) {
        return hasEverActivated(c) && !isActivationActive(c);
    }

    public boolean trialAvailable(Context c) {
        Status s = status(c);
        return s != null && !s.trialUsed && !isActivationActive(c);
    }

    public long getActivationDaysLeft(Context c) {
        Status s = status(c);
        if (s == null) return 0;
        long left = Math.min(s.expiresAt, Long.MAX_VALUE) - effectiveNow(c);
        return left <= 0 ? 0 : TimeUnit.MILLISECONDS.toDays(left) + 1;
    }

    public String getPlanTypeCode(Context c) {
        Status s = status(c);
        return s == null ? "" : s.plan;
    }

    /** نزدیک پایان مهلت آفلاین یا انقضا → همگام‌سازی پرتکرارتر. */
    boolean needsFrequentSync(Context c) {
        Status s = status(c);
        if (s == null) return true;
        long n = effectiveNow(c);
        return s.offlineUntil - n < TimeUnit.DAYS.toMillis(3) || s.expiresAt - n < TimeUnit.DAYS.toMillis(1);
    }

    public boolean canUseAnalysis(Context c) { return isActivationActive(c); }
    public boolean canViewHistory(Context c) { return isActivationActive(c) || hasEverActivated(c) || isLoggedIn(c); }
    public boolean hasAccess(Context c) { return isActivationActive(c); }
    public boolean isAccessActive(Context c) { return isActivationActive(c); }

    // سازگاری با کد قبلی (قفل ساعت و مستر حذف شد)
    public boolean isClockLocked(Context c) { return false; }
    public void setClockLocked(Context c, boolean locked) {}
    public boolean isMasterActivated(Context c) { return false; }
    public void ensureTrialStarted(Context c) {}

    private static String planFa(String plan) {
        switch (plan) {
            case "TRIAL_7": return "تست ۷ روزه";
            case "MONTHLY": return "ماهانه عادی";
            case "YEARLY": return "سالانه عادی";
            case "PREMIUM_MONTHLY": return "پرمیوم ماهانه";
            case "PREMIUM_YEARLY": return "پرمیوم سالانه";
            default: return plan;
        }
    }

    public String getStatusLabel(Context c) {
        if (!isLoggedIn(c)) return "ورود لازم است";
        Status s = status(c);
        if (s == null) return "وضعیت نامعتبر — اتصال اینترنت لازم است";
        if (isActivationActive(c)) {
            return planFa(s.plan) + (s.premium ? " · پرمیوم" : "") + " · " + getActivationDaysLeft(c) + " روز باقی‌مانده";
        }
        if (isActivationExpired(c)) {
            return effectiveNow(c) >= s.expiresAt
                    ? "اشتراک منقضی شده — نیاز به تمدید"
                    : "برای ادامه یک‌بار به اینترنت وصل شوید";
        }
        return "نیاز به فعال‌سازی";
    }

    // ─── عملیات شبکه (blocking) ───
    /** 0 موفق · 3 خطای شبکه/سرور · 7 نیاز به ورود */
    public int refreshStatus(Context c) {
        String token = getToken(c);
        if (token == null) return 7;
        try {
            JSONObject b = new JSONObject();
            b.put("app_version", BuildConfig.VERSION_NAME);
            b.put("device_model", (Build.MANUFACTURER + " " + Build.MODEL).trim());
            JSONObject r = RaidoCloud.post("status", b, token);
            int http = r.optInt("_http");
            if (http == 401) { logout(c); return 7; }
            if (http == 200 && saveStatus(c, r.optString("payload"), r.optString("sig"))) return 0;
            return 3;
        } catch (Throwable t) {
            return 3;
        }
    }

    /** 0 موفق · 4 قبلاً مصرف شده · 9 قبلاً تست گرفته شده · 3 خطا · 7 نیاز به ورود */
    public int startTrial(Context c) {
        String token = getToken(c);
        if (token == null) return 7;
        try {
            JSONObject r = RaidoCloud.post("start-trial", new JSONObject(), token);
            int http = r.optInt("_http");
            if (http == 401) { logout(c); return 7; }
            if (r.optBoolean("ok") && saveStatus(c, r.optString("payload"), r.optString("sig"))) return 0;
            String e = r.optString("error");
            if ("trial_used".equals(e)) return 9;
            if ("already_active".equals(e)) return 0;
            return 3;
        } catch (Throwable t) {
            return 3;
        }
    }

    /**
     * 0 موفق · 1 کد نامعتبر · 4 قبلاً مصرف شده · 5 منقضی · 8 تلاش زیاد · 3 خطا · 7 نیاز به ورود
     */
    public int activateWithCode(Context c, String rawCode) {
        String token = getToken(c);
        if (token == null) return 7;
        try {
            JSONObject b = new JSONObject();
            b.put("code", rawCode == null ? "" : rawCode);
            JSONObject r = RaidoCloud.post("redeem-code", b, token);
            int http = r.optInt("_http");
            if (http == 401) { logout(c); return 7; }
            if (http == 429) return 8;
            if (r.optBoolean("ok")) {
                return saveStatus(c, r.optString("payload"), r.optString("sig")) ? 0 : 3;
            }
            String e = r.optString("error");
            if ("used".equals(e)) return 4;
            if ("expired".equals(e)) return 5;
            if ("invalid".equals(e)) return 1;
            return 3;
        } catch (Throwable t) {
            return 3;
        }
    }
}
