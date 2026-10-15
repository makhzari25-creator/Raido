package ir.tripanalyzer.app;

import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * الگوریتم کد فعال‌سازی RAIDO v2.2.17
 * فرمت: XXXX-XXXX (۸ کاراکتر)
 *
 * ساختار بدنه (۶ کاراکتر):
 *   [0]   = نوع اشتراک (B/M/Y/P/R)
 *   [1-4] = ساعت از مبدأ ۲۰۲۶-۰۱-۰۱ (base32) — پنجرهٔ ۷۲ ساعتهٔ فعال‌سازی
 *   [5]   = تصادفی
 *   [6-7] = امضا
 */
public final class KeyHelper {
    public static final KeyHelper INSTANCE = new KeyHelper();

    private static final String SECRET = "RAIDO-ACT-2026-v2";
    private static final String MASTER_PHRASE = "Milad@Raido";
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    /** 2026-01-01 00:00:00 UTC */
    public static final long EPOCH_MS = 1735689600000L;
    public static final long ACTIVATE_WINDOW_MS = 72L * 60L * 60L * 1000L;

    public enum PlanType {
        TRIAL_7('B', 7, false, "تست ۷ روزه"),
        MONTHLY('M', 30, false, "ماهانه عادی"),
        YEARLY('Y', 365, false, "سالانه عادی"),
        PREMIUM_MONTHLY('P', 30, true, "پرمیوم ماهانه"),
        PREMIUM_YEARLY('R', 365, true, "پرمیوم سالانه");

        public final char code;
        public final int days;
        public final boolean premium;
        public final String labelFa;

        PlanType(char code, int days, boolean premium, String labelFa) {
            this.code = code;
            this.days = days;
            this.premium = premium;
            this.labelFa = labelFa;
        }

        public static PlanType fromChar(char c) {
            char u = Character.toUpperCase(c);
            for (PlanType t : values()) {
                if (t.code == u) return t;
            }
            return null;
        }
    }

    private KeyHelper() {}

    public final String k() {
        return SECRET;
    }

    public static String getMasterCode() {
        return buildCodeFromSeed(MASTER_PHRASE + "|" + SECRET + "|MASTER");
    }

    public static boolean isMasterCode(String raw) {
        if (raw == null) return false;
        return normalize(raw).equals(normalize(getMasterCode()));
    }

    public static String normalize(String raw) {
        if (raw == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = Character.toUpperCase(raw.charAt(i));
            if (c == '-' || c == ' ' || c == '_') continue;
            sb.append(c);
        }
        return sb.toString();
    }

    public static String formatDisplay(String normalized8) {
        String n = normalize(normalized8);
        if (n.length() != 8) return n;
        return n.substring(0, 4) + "-" + n.substring(4, 8);
    }

    public static String buildCodeFromSeed(String seed) {
        String body = hashToBody6(seed);
        return body + signature2(body);
    }

    public static String generateForPlan(PlanType plan) {
        if (plan == null) plan = PlanType.MONTHLY;
        long hours = Math.max(0, (System.currentTimeMillis() - EPOCH_MS) / 3_600_000L);
        String time4 = encodeBase32(hours, 4);
        char rnd = ALPHABET.charAt(new SecureRandom().nextInt(ALPHABET.length()));
        String body = "" + plan.code + time4 + rnd;
        return body + signature2(body);
    }

    public static boolean isValidSignedCode(String raw) {
        Parsed p = parse(raw);
        return p != null && p.valid;
    }

    public static Parsed parse(String raw) {
        if (raw == null) return null;
        String n = normalize(raw);
        if (n.length() != 8) return null;
        String body = n.substring(0, 6);
        String sig = n.substring(6, 8);
        boolean okSig = sig.equals(signature2(body));
        PlanType type = PlanType.fromChar(body.charAt(0));
        long hours = decodeBase32(body.substring(1, 5));
        if (hours < 0) return new Parsed(n, null, 0, false, okSig);
        long genMs = EPOCH_MS + hours * 3_600_000L;
        return new Parsed(n, type, genMs, okSig && type != null, okSig);
    }

    public static boolean isWithinActivateWindow(Parsed p, long nowMs) {
        if (p == null || p.type == null) return false;
        long age = nowMs - p.generatedAtMs;
        return age >= -3_600_000L && age <= ACTIVATE_WINDOW_MS;
    }

    public static final class Parsed {
        public final String normalized;
        public final PlanType type;
        public final long generatedAtMs;
        public final boolean valid;
        public final boolean validSignature;

        Parsed(String normalized, PlanType type, long generatedAtMs, boolean valid, boolean validSignature) {
            this.normalized = normalized;
            this.type = type;
            this.generatedAtMs = generatedAtMs;
            this.valid = valid;
            this.validSignature = validSignature;
        }
    }

    private static String hashToBody6(String seed) {
        try {
            byte[] dig = MessageDigest.getInstance("SHA-256").digest(seed.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            int idx = 0;
            while (sb.length() < 6) {
                int v = dig[idx % dig.length] & 0xFF;
                sb.append(ALPHABET.charAt(v % ALPHABET.length()));
                idx++;
            }
            return sb.toString();
        } catch (Exception e) {
            return "AAAAAA";
        }
    }

    private static String signature2(String body) {
        try {
            byte[] dig = MessageDigest.getInstance("SHA-256")
                    .digest((body + "|" + SECRET).getBytes("UTF-8"));
            char c1 = ALPHABET.charAt((dig[0] & 0xFF) % ALPHABET.length());
            char c2 = ALPHABET.charAt((dig[1] & 0xFF) % ALPHABET.length());
            return "" + c1 + c2;
        } catch (Exception e) {
            return "AA";
        }
    }

    private static String encodeBase32(long value, int width) {
        StringBuilder sb = new StringBuilder();
        long v = Math.max(0, value);
        for (int i = 0; i < width; i++) {
            int idx = (int) (v % ALPHABET.length());
            sb.append(ALPHABET.charAt(idx));
            v /= ALPHABET.length();
        }
        return sb.reverse().toString();
    }

    private static long decodeBase32(String s) {
        long v = 0;
        for (int i = 0; i < s.length(); i++) {
            int idx = ALPHABET.indexOf(s.charAt(i));
            if (idx < 0) return -1;
            v = v * ALPHABET.length() + idx;
        }
        return v;
    }
}
