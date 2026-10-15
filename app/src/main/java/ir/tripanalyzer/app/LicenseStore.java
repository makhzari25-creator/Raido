package ir.tripanalyzer.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

/**
 * لایسنس RAIDO v2.2.17
 * - ۵ نوع کد: تست۷ / ماهانه / سالانه / پرمیوم‌ماهانه / پرمیوم‌سالانه
 * - پنجرهٔ فعال‌سازی ۷۲ ساعت از تولید
 * - قفل به اثرانگشت دستگاه
 * - یک‌بارمصرف روی دستگاه (لیست هش محلی)
 * - مستر نامحدود
 * - فلگ Premium برای لایهٔ نشان
 */
public final class LicenseStore {
    public static final LicenseStore INSTANCE = new LicenseStore();

    private static final String PREFS = "raido_license";

    private static final String KEY_TRIAL_START = "trial_start_ms";
    private static final String KEY_TRIAL_USED = "trial_used";
    private static final String KEY_SUB_EXPIRY = "sub_expiry_ms";
    private static final String KEY_SUB_TYPE = "sub_type";
    private static final String KEY_DEVICE_FP = "device_fp";
    private static final String KEY_LAST_PAYMENT_REF = "last_payment_ref";

    private static final String KEY_ACT_CODE = "act_code_norm";
    private static final String KEY_ACT_CODE_HASH = "act_code_hash";
    private static final String KEY_ACT_DEVICE = "act_device_fp";
    private static final String KEY_ACT_AT = "act_at_ms";
    private static final String KEY_ACT_EXPIRY = "act_expiry_ms";
    private static final String KEY_ACT_MASTER = "act_is_master";
    private static final String KEY_ACT_EVER = "act_ever_activated";
    private static final String KEY_USED_CODES = "used_code_hashes";
    private static final String KEY_PLAN_TYPE = "act_plan_type";
    private static final String KEY_IS_PREMIUM = "act_is_premium";
    private static final String KEY_CLOCK_LOCKED = "clock_tamper_lock";

    public static final boolean TRIAL_ENABLED = false;
    public static final long TRIAL_DAYS = 7L;
    public static final long ACTIVATION_DAYS = 30L;
    public static final long MONTHLY_DAYS = 30L;
    public static final long YEARLY_DAYS = 365L;

    public static final int PRICE_MONTHLY = 49000;
    public static final int PRICE_YEARLY = 499000;
    public static final int PRICE_PREMIUM_MONTHLY = 73500;
    public static final int PRICE_PREMIUM_YEARLY = 748500;
    public static final String TELEGRAM_SUPPORT = "https://t.me/TripAnalyzerSupport";
    public static final String TELEGRAM_ID = "@TripAnalyzerSupport";

    /** @deprecated زرین‌پال حذف شد */
    @Deprecated
    public static final String ZARINPAL_MERCHANT_ID = "";

    private LicenseStore() {}

    private SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String getDeviceFingerprint(Context context) {
        SharedPreferences p = prefs(context);
        String cached = p.getString(KEY_DEVICE_FP, null);
        if (cached != null && !cached.isEmpty()) return cached;
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId == null || androidId.isEmpty()) androidId = "unknown";
        String fp = sha1Short(androidId + "|RAIDO|2026");
        p.edit().putString(KEY_DEVICE_FP, fp).apply();
        return fp;
    }

    private String sha1Short(String input) {
        try {
            byte[] dig = MessageDigest.getInstance("SHA-1").digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                String h = Integer.toHexString(dig[i] & 0xFF);
                if (h.length() == 1) sb.append('0');
                sb.append(h);
            }
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            return "ERR00000";
        }
    }

    private String sha256Hex(String input) {
        try {
            byte[] dig = MessageDigest.getInstance("SHA-256").digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) {
                String h = Integer.toHexString(b & 0xFF);
                if (h.length() == 1) sb.append('0');
                sb.append(h);
            }
            return sb.toString();
        } catch (Exception e) {
            return input;
        }
    }

    // ─── Trial (disabled) ───
    public void ensureTrialStarted(Context context) {
        if (!TRIAL_ENABLED) return;
        SharedPreferences p = prefs(context);
        if (p.getBoolean(KEY_TRIAL_USED, false)) return;
        if (p.getLong(KEY_TRIAL_START, 0L) == 0L) {
            p.edit()
                    .putLong(KEY_TRIAL_START, System.currentTimeMillis())
                    .putBoolean(KEY_TRIAL_USED, true)
                    .apply();
        }
    }

    public boolean isInTrial(Context context) {
        if (!TRIAL_ENABLED) return false;
        long start = prefs(context).getLong(KEY_TRIAL_START, 0L);
        if (start == 0L) return false;
        return (System.currentTimeMillis() - start) < TimeUnit.DAYS.toMillis(TRIAL_DAYS);
    }

    public long getTrialDaysLeft(Context context) {
        if (!TRIAL_ENABLED) return 0;
        long start = prefs(context).getLong(KEY_TRIAL_START, 0L);
        if (start == 0L) return 0;
        long remaining = TimeUnit.DAYS.toMillis(TRIAL_DAYS) - (System.currentTimeMillis() - start);
        if (remaining <= 0) return 0;
        return TimeUnit.MILLISECONDS.toDays(remaining) + 1;
    }

    // ─── Legacy subscription (kept read-only; no Zarinpal) ───
    public boolean isSubscribed(Context context) {
        long expiry = prefs(context).getLong(KEY_SUB_EXPIRY, 0L);
        return expiry > System.currentTimeMillis();
    }

    public long getSubscriptionDaysLeft(Context context) {
        long expiry = prefs(context).getLong(KEY_SUB_EXPIRY, 0L);
        if (expiry <= System.currentTimeMillis()) return 0;
        return TimeUnit.MILLISECONDS.toDays(expiry - System.currentTimeMillis()) + 1;
    }

    public String getSubscriptionType(Context context) {
        return prefs(context).getString(KEY_SUB_TYPE, "");
    }

    /** @deprecated پرداخت داخل‌اپ حذف شد */
    @Deprecated
    public void activateSubscription(Context context, String type, String paymentRef) {
        long days = "yearly".equals(type) ? YEARLY_DAYS : MONTHLY_DAYS;
        long now = System.currentTimeMillis();
        long currentExpiry = prefs(context).getLong(KEY_SUB_EXPIRY, 0L);
        long base = Math.max(now, currentExpiry);
        long newExpiry = base + TimeUnit.DAYS.toMillis(days);
        prefs(context).edit()
                .putLong(KEY_SUB_EXPIRY, newExpiry)
                .putString(KEY_SUB_TYPE, type)
                .putString(KEY_LAST_PAYMENT_REF, paymentRef != null ? paymentRef : "")
                .apply();
    }

    // ─── Clock lock ───
    public boolean isClockLocked(Context context) {
        return prefs(context).getBoolean(KEY_CLOCK_LOCKED, false);
    }

    public void setClockLocked(Context context, boolean locked) {
        SharedPreferences.Editor ed = prefs(context).edit().putBoolean(KEY_CLOCK_LOCKED, locked);
        if (locked) {
            // باطل کردن دسترسی تا کد جدید
            ed.putLong(KEY_ACT_EXPIRY, 0L)
              .putBoolean(KEY_ACT_MASTER, false)
              .putBoolean(KEY_IS_PREMIUM, false);
        }
        ed.apply();
    }

    // ─── Activation codes ───

    public boolean hasEverActivated(Context context) {
        return prefs(context).getBoolean(KEY_ACT_EVER, false)
                || prefs(context).getLong(KEY_ACT_AT, 0L) > 0;
    }

    public boolean isMasterActivated(Context context) {
        return prefs(context).getBoolean(KEY_ACT_MASTER, false);
    }

    public boolean isPremium(Context context) {
        if (isMasterActivated(context)) return true;
        if (!isActivationActive(context)) return false;
        return prefs(context).getBoolean(KEY_IS_PREMIUM, false);
    }

    public String getPlanTypeCode(Context context) {
        return prefs(context).getString(KEY_PLAN_TYPE, "");
    }

    public boolean isActivationActive(Context context) {
        if (isClockLocked(context)) return false;
        SharedPreferences p = prefs(context);
        if (p.getBoolean(KEY_ACT_MASTER, false)) return true;
        long expiry = p.getLong(KEY_ACT_EXPIRY, 0L);
        if (expiry <= 0) return false;
        String bound = p.getString(KEY_ACT_DEVICE, "");
        String fp = getDeviceFingerprint(context);
        if (bound.length() > 0 && !bound.equals(fp)) return false;
        return System.currentTimeMillis() < expiry;
    }

    public boolean isActivationExpired(Context context) {
        SharedPreferences p = prefs(context);
        if (p.getBoolean(KEY_ACT_MASTER, false)) return false;
        if (!hasEverActivated(context)) return false;
        long expiry = p.getLong(KEY_ACT_EXPIRY, 0L);
        return expiry > 0 && System.currentTimeMillis() >= expiry;
    }

    public long getActivationDaysLeft(Context context) {
        if (isMasterActivated(context)) return 9999;
        long expiry = prefs(context).getLong(KEY_ACT_EXPIRY, 0L);
        if (expiry <= System.currentTimeMillis()) return 0;
        return TimeUnit.MILLISECONDS.toDays(expiry - System.currentTimeMillis()) + 1;
    }

    /**
     * 0 = موفق
     * 1 = کد نامعتبر
     * 3 = خطا
     * 4 = قبلاً مصرف شده
     * 5 = خارج از پنجرهٔ ۷۲ ساعته
     * 6 = قفل ساعت (باید کد جدید)
     */
    private boolean isCodeConsumed(Context context, String hash) {
        if (hash == null || hash.isEmpty()) return false;
        String blob = prefs(context).getString(KEY_USED_CODES, "");
        if (blob == null || blob.isEmpty()) return false;
        for (String h : blob.split("\\|")) {
            if (hash.equals(h)) return true;
        }
        return false;
    }

    private void markCodeConsumed(Context context, String hash) {
        if (hash == null || hash.isEmpty()) return;
        if (isCodeConsumed(context, hash)) return;
        String blob = prefs(context).getString(KEY_USED_CODES, "");
        if (blob == null) blob = "";
        if (!blob.isEmpty()) blob = blob + "|";
        blob = blob + hash;
        String[] parts = blob.split("\\|");
        if (parts.length > 200) {
            StringBuilder sb = new StringBuilder();
            for (int i = parts.length - 200; i < parts.length; i++) {
                if (sb.length() > 0) sb.append('|');
                sb.append(parts[i]);
            }
            blob = sb.toString();
        }
        prefs(context).edit().putString(KEY_USED_CODES, blob).apply();
    }

    public int activateWithCode(Context context, String rawCode) {
        try {
            if (isClockLocked(context)) {
                // اجازهٔ فعال‌سازی با کد جدید بعد از قفل ساعت
            }

            String norm = KeyHelper.normalize(rawCode);
            if (norm.isEmpty()) return 1;

            // مستر
            if (KeyHelper.isMasterCode(norm)) {
                prefs(context).edit()
                        .putString(KEY_ACT_CODE, norm)
                        .putString(KEY_ACT_CODE_HASH, sha256Hex(norm))
                        .putString(KEY_ACT_DEVICE, getDeviceFingerprint(context))
                        .putLong(KEY_ACT_AT, System.currentTimeMillis())
                        .putLong(KEY_ACT_EXPIRY, Long.MAX_VALUE / 4)
                        .putBoolean(KEY_ACT_MASTER, true)
                        .putBoolean(KEY_ACT_EVER, true)
                        .putBoolean(KEY_IS_PREMIUM, true)
                        .putString(KEY_PLAN_TYPE, "MASTER")
                        .putBoolean(KEY_CLOCK_LOCKED, false)
                        .apply();
                return 0;
            }

            KeyHelper.Parsed parsed = KeyHelper.parse(norm);
            if (parsed == null || !parsed.valid || parsed.type == null) return 1;

            long now = System.currentTimeMillis();
            if (!KeyHelper.isWithinActivateWindow(parsed, now)) return 5;

            String hash = sha256Hex(norm);
            String fp = getDeviceFingerprint(context);
            SharedPreferences p = prefs(context);

            String existingHash = p.getString(KEY_ACT_CODE_HASH, "");
            if (hash.equals(existingHash) && isActivationActive(context)) {
                return 0;
            }

            if (isCodeConsumed(context, hash)) {
                return 4;
            }

            KeyHelper.PlanType plan = parsed.type;
            // تمدید / ارتقا: دادهٔ سفر و تنظیمات در Store جداست و اینجا پاک نمی‌شود.
            // انقضا = max(انقضای فعلی، الان) + مدت پلن جدید → روزهای باقی‌مانده از بین نمی‌رود.
            long currentExpiry = p.getLong(KEY_ACT_EXPIRY, 0L);
            long base = Math.max(currentExpiry, now);
            long expiry = base + TimeUnit.DAYS.toMillis(plan.days);
            // اگر پلن جدید پرمیوم است، پرمیوم روشن می‌شود؛ اگر عادی است و قبلاً پرمیوم بود
            // فقط وقتی پلن جدید صراحتاً عادی است، پرمیوم خاموش می‌شود (تعویض پلن).
            // پرمیوم بودن فقط از روی نوع کد جدید (ارتقا با کد P/R، عادی با B/M/Y)
            boolean newPremium = plan.premium;
            p.edit()
                    .putString(KEY_ACT_CODE, norm)
                    .putString(KEY_ACT_CODE_HASH, hash)
                    .putString(KEY_ACT_DEVICE, fp)
                    .putLong(KEY_ACT_AT, now)
                    .putLong(KEY_ACT_EXPIRY, expiry)
                    .putBoolean(KEY_ACT_MASTER, false)
                    .putBoolean(KEY_ACT_EVER, true)
                    .putBoolean(KEY_IS_PREMIUM, newPremium)
                    .putString(KEY_PLAN_TYPE, plan.name())
                    .putBoolean(KEY_CLOCK_LOCKED, false)
                    .apply();
            markCodeConsumed(context, hash);
            // عمداً هیچ‌چیز از Store / تاریخچه / تنظیمات پاک نمی‌شود
            return 0;
        } catch (Exception e) {
            return 3;
        }
    }

    public boolean canUseAnalysis(Context context) {
        if (isClockLocked(context)) return false;
        return isActivationActive(context) || isSubscribed(context) || isInTrial(context);
    }

    public boolean canViewHistory(Context context) {
        return canUseAnalysis(context) || hasEverActivated(context) || isSubscribed(context);
    }

    public boolean hasAccess(Context context) {
        return canUseAnalysis(context);
    }

    public boolean isAccessActive(Context context) {
        return canUseAnalysis(context);
    }

    public String getStatusLabel(Context context) {
        if (isClockLocked(context)) {
            return "قفل امنیتی ساعت — کد جدید لازم است";
        }
        if (isMasterActivated(context)) {
            return "دسترسی نامحدود (مستر) · پرمیوم";
        }
        if (isActivationActive(context)) {
            String plan = prefs(context).getString(KEY_PLAN_TYPE, "");
            String planFa = plan;
            try {
                KeyHelper.PlanType t = KeyHelper.PlanType.valueOf(plan);
                planFa = t.labelFa;
            } catch (Exception ignored) {}
            String prem = isPremium(context) ? " · پرمیوم" : "";
            return planFa + prem + " · " + getActivationDaysLeft(context) + " روز باقی‌مانده";
        }
        if (isSubscribed(context)) {
            String type = getSubscriptionType(context);
            long days = getSubscriptionDaysLeft(context);
            String typeFa = "yearly".equals(type) ? "سالانه" : "ماهانه";
            return "اشتراک " + typeFa + " · " + days + " روز باقی‌مانده";
        }
        if (isInTrial(context)) {
            return "دوره آزمایشی · " + getTrialDaysLeft(context) + " روز باقی‌مانده";
        }
        if (isActivationExpired(context)) {
            return "کد منقضی شده — نیاز به تمدید";
        }
        return "نیاز به فعال‌سازی";
    }

    public void syncWithServer(Context context) {}
    public void verifyPaymentOnServer(Context context, String authority, String type) {}

    @Deprecated
    public boolean isActivated(Context context) {
        return hasAccess(context);
    }

    @Deprecated
    public void setActivated(Context context, boolean z) {}

    @Deprecated
    public boolean validateCode(Context context, String str) {
        return activateWithCode(context, str) == 0;
    }

    @Deprecated
    public String getDeviceCode(Context context) {
        return getDeviceFingerprint(context);
    }
}
