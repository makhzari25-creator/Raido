package ir.tripanalyzer.app;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * پارس پای‌های اسنپ + تشخیص مدل سفر (یک‌مقصده / دو‌مقصده / رفت‌وبرگشت).
 *
 * v2.3.12:
 * - یکتاسازی پای‌های تکراری (فیکس دو برابر شدن به‌خاطر Accessibility تکراری)
 * - تشخیص ROUND_TRIP از لوپ / بازگشت / رفت‌وبرگشت
 * - بازه «X تا Y دقیقه» هرگز پای سفر یا ROUND_TRIP حساب نمی‌شود (فقط توقف)
 */
public final class SnappLegParser {
    public static final SnappLegParser INSTANCE = new SnappLegParser();

    public static final double TIME_TO_KM_SPEED = 30.0;

    public enum TripKind {
        ONE_DEST,
        TWO_DEST,
        ROUND_TRIP
    }

    public static final class Legs {
        public final Double pickupMin;
        public final Double pickupKm;
        public final Double tripMin;
        public final Double tripKm;

        public Legs(Double pickupMin, Double pickupKm, Double tripMin, Double tripKm) {
            this.pickupMin = pickupMin;
            this.pickupKm = pickupKm;
            this.tripMin = tripMin;
            this.tripKm = tripKm;
        }

        public boolean hasAnyKm() {
            return (pickupKm != null && pickupKm > 0) || (tripKm != null && tripKm > 0);
        }

        public boolean hasAnyMin() {
            return (pickupMin != null && pickupMin > 0) || (tripMin != null && tripMin > 0);
        }
    }

    public static final class TripShape {
        public final TripKind kind;
        public final Legs legs;
        public final int destCount;
        public final boolean insufficient;
        public final String messageFa;
        public final boolean usedTimeFallback;

        public TripShape(TripKind kind, Legs legs, int destCount,
                         boolean insufficient, String messageFa, boolean usedTimeFallback) {
            this.kind = kind;
            this.legs = legs;
            this.destCount = destCount;
            this.insufficient = insufficient;
            this.messageFa = messageFa;
            this.usedTimeFallback = usedTimeFallback;
        }
    }

    private static final Pattern COMBINED_PICKUP = Pattern.compile(
            "(\\d+(?:[.,]\\d+)?)\\s*دقیقه\\s*تا\\s*(?:رسیدن\\s*)?(?:به\\s*)?(مبد[اأ]?|مسافر|محل|سوار)"
                    + "\\s*[-–—ـ:|٫،]?\\s*"
                    + "(\\d+(?:[.,]\\d+)?)\\s*(کیلومتر|km|متر)"
    );

    private static final Pattern COMBINED_TRIP = Pattern.compile(
            "(\\d+(?:[.,]\\d+)?)\\s*دقیقه"
                    + "(?!\\s*تا\\s*(?:رسیدن\\s*)?(?:به\\s*)?(?:مبد|مسافر|محل|سوار))"
                    + "\\s*[-–—ـ:|٫،]?\\s*"
                    + "(\\d+(?:[.,]\\d+)?)\\s*(کیلومتر|km|متر)"
    );

    private static final Pattern TIME_PICKUP = Pattern.compile(
            "(\\d+(?:[.,]\\d+)?)\\s*دقیقه\\s*تا\\s*(?:رسیدن\\s*)?(?:به\\s*)?(مبد[اأ]?|مسافر|محل|سوار)"
    );

    private static final Pattern TIME_TRIP = Pattern.compile(
            "(\\d+(?:[.,]\\d+)?)\\s*دقیقه"
                    + "(?!\\s*تا\\s*(?:رسیدن\\s*)?(?:به\\s*)?(?:مبد|مسافر|محل|سوار))"
    );

    private static final Pattern ANY_KM = Pattern.compile(
            "(\\d+(?:[.,]\\d+)?)\\s*(کیلومتر|km)"
    );

    private static final Pattern DURING_PICKUP_TIME = Pattern.compile(
            "از\\s*مقصد\\s*سفر\\s*(?:فعلی)?[^\\d]{0,12}(\\d+(?:[.,]\\d+)?)\\s*دقیقه"
    );

    private static final Pattern STOP_RANGE = Pattern.compile(
            "(\\d{1,3})\\s*تا\\s*(\\d{1,3})\\s*دقیقه"
    );

    private SnappLegParser() {}

    public static String normalizeDigits(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0x06F0 && c <= 0x06F9) {
                sb.append((char) ('0' + (c - 0x06F0)));
            } else if (c >= 0x0660 && c <= 0x0669) {
                sb.append((char) ('0' + (c - 0x0660)));
            } else if (c == 0x066B) {
                sb.append('.');
            } else if (c == 0x066C || c == 0x060C) {
                sb.append(',');
            } else if (c == '\u200c' || c == '\u200f' || c == '\u200e') {
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static Double parseNum(String raw) {
        if (raw == null) return null;
        try {
            String t = raw.replace(",", "").replace(" ", "");
            if (t.isEmpty()) return null;
            return Double.parseDouble(t);
        } catch (Exception e) {
            return null;
        }
    }

    private static double toKm(Double val, String unit) {
        if (val == null) return 0;
        if (unit != null && unit.contains("متر") && !unit.contains("کیلومتر")) {
            return val / 1000.0;
        }
        return val;
    }

    public static double minToKm(double minutes) {
        return minutes * (TIME_TO_KM_SPEED / 60.0);
    }

    private static boolean isInsideStopRange(String text, int matchStart, int matchEnd) {
        Matcher sr = STOP_RANGE.matcher(text);
        while (sr.find()) {
            if (matchStart >= sr.start() && matchEnd <= sr.end()) return true;
            if (matchStart < sr.end() && matchEnd > sr.start()) return true;
        }
        return false;
    }

    private static void addUnique(List<Double> list, Double v, double eps) {
        if (v == null || v <= 0) return;
        for (Double x : list) {
            if (x != null && Math.abs(x - v) <= eps) return;
        }
        list.add(v);
    }

    public Legs extract(String raw) {
        return analyze(raw).legs;
    }

    public TripShape analyze(String raw) {
        String text = normalizeDigits(raw != null ? raw : "");

        Double pMin = null, pKm = null;
        List<Double> tripMins = new ArrayList<>();
        List<Double> tripKms = new ArrayList<>();

        Matcher mPick = COMBINED_PICKUP.matcher(text);
        if (mPick.find()) {
            pMin = parseNum(mPick.group(1));
            pKm = toKm(parseNum(mPick.group(3)), mPick.group(4));
        }

        Matcher mTrip = COMBINED_TRIP.matcher(text);
        while (mTrip.find()) {
            if (isInsideStopRange(text, mTrip.start(), mTrip.end())) continue;
            Double tm = parseNum(mTrip.group(1));
            Double tk = toKm(parseNum(mTrip.group(2)), mTrip.group(3));
            if (pMin != null && tm != null && Math.abs(tm - pMin) < 0.01) continue;
            addUnique(tripMins, tm, 0.05);
            addUnique(tripKms, tk, 0.05);
        }

        if (pMin == null) {
            Matcher tp = TIME_PICKUP.matcher(text);
            if (tp.find()) pMin = parseNum(tp.group(1));
        }
        if (pMin == null) {
            Matcher dp = DURING_PICKUP_TIME.matcher(text);
            if (dp.find()) pMin = parseNum(dp.group(1));
        }

        if (tripMins.isEmpty()) {
            Matcher tt = TIME_TRIP.matcher(text);
            while (tt.find()) {
                if (isInsideStopRange(text, tt.start(), tt.end())) continue;
                Double tm = parseNum(tt.group(1));
                if (tm == null) continue;
                if (pMin != null && Math.abs(tm - pMin) < 0.01) continue;
                int start = tt.start();
                String around = text.substring(Math.max(0, start - 4), Math.min(text.length(), tt.end() + 8));
                if (around.contains("تا") && around.matches("(?s).*\\d+\\s*تا\\s*\\d+.*")) {
                    continue;
                }
                addUnique(tripMins, tm, 0.05);
            }
        }

        if (pKm == null && tripKms.isEmpty()) {
            Matcher mk = ANY_KM.matcher(text);
            List<Double> kms = new ArrayList<>();
            while (mk.find()) {
                Double k = toKm(parseNum(mk.group(1)), mk.group(2));
                addUnique(kms, k, 0.05);
            }
            if (kms.size() >= 2) {
                pKm = kms.get(0);
                for (int i = 1; i < kms.size(); i++) tripKms.add(kms.get(i));
            } else if (kms.size() == 1) {
                tripKms.add(kms.get(0));
            }
        }

        TripKind kind = detectKind(text, tripMins.size(), tripKms.size());
        int destCount = Math.max(1, Math.max(tripMins.size(), tripKms.size()));
        if (kind == TripKind.ONE_DEST) destCount = 1;
        if (kind == TripKind.TWO_DEST) destCount = Math.max(2, destCount);
        if (kind == TripKind.ROUND_TRIP) destCount = 1;

        Double tMin = null;
        Double tKm = null;
        if (!tripMins.isEmpty()) {
            double s = 0;
            for (Double d : tripMins) s += d;
            tMin = s;
        }
        if (!tripKms.isEmpty()) {
            double s = 0;
            for (Double d : tripKms) s += d;
            tKm = s;
        }

        boolean usedTimeFallback = false;
        boolean insufficient = false;
        String msg = null;

        if (kind == TripKind.TWO_DEST) {
            boolean hasKmData = tKm != null && tKm > 0;
            boolean hasMinData = tMin != null && tMin > 0;
            if (!hasKmData && !hasMinData) {
                insufficient = true;
                msg = "سفر دو مقصده اما اطلاعات کافی وجود ندارد";
            } else if (!hasKmData && hasMinData) {
                tKm = minToKm(tMin);
                if (pKm == null && pMin != null) {
                    pKm = minToKm(pMin);
                }
                usedTimeFallback = true;
            }
        } else if (kind == TripKind.ROUND_TRIP) {
            if (tKm != null && tKm > 0) {
                if (pKm == null && pMin != null) {
                    pKm = minToKm(pMin);
                    usedTimeFallback = true;
                }
                tKm = 2.0 * tKm;
                if (tMin != null && tMin > 0) {
                    tMin = 2.0 * tMin;
                }
            } else if (tMin != null && tMin > 0) {
                double pickMin = pMin != null ? pMin : 0;
                pMin = pickMin > 0 ? pickMin : pMin;
                tMin = 2.0 * tMin;
                pKm = pMin != null && pMin > 0 ? minToKm(pMin) : pKm;
                tKm = minToKm(tMin);
                usedTimeFallback = true;
            }
        } else {
            if ((tKm == null || tKm <= 0) && tMin != null && tMin > 0) {
                tKm = minToKm(tMin);
                usedTimeFallback = true;
            }
            if ((pKm == null || pKm <= 0) && pMin != null && pMin > 0) {
                pKm = minToKm(pMin);
                usedTimeFallback = true;
            }
        }

        Legs legs = new Legs(pMin, pKm, tMin, tKm);
        return new TripShape(kind, legs, destCount, insufficient, msg, usedTimeFallback);
    }

    private TripKind detectKind(String text, int tripMinCount, int tripKmCount) {
        String t = text.replace('\u200c', ' ');
        if (looksLikeRoundTrip(t)) {
            return TripKind.ROUND_TRIP;
        }
        String tCount = t
                .replace("از مقصد سفر فعلی", " ")
                .replace("از مقصد سفر", " ")
                .replace("به مقصد رسیدید", " ")
                .replace("نزدیک مقصد", " ");
        int destLabels = 0;
        Matcher dm = Pattern.compile("مقصد").matcher(tCount);
        while (dm.find()) destLabels++;
        if (destLabels >= 2 || tripMinCount >= 2 || tripKmCount >= 2) {
            return TripKind.TWO_DEST;
        }
        if (t.contains("مقصد دوم") || t.contains("مقصد ۲") || t.contains("مقصد 2")
                || t.contains("مقصد اضافه") || t.contains("توقفگاه بعدی")) {
            return TripKind.TWO_DEST;
        }
        return TripKind.ONE_DEST;
    }

    static boolean looksLikeRoundTrip(String t) {
        if (t == null || t.isEmpty()) return false;
        String s = t.replace('\u200c', ' ');
        if (s.contains("برگشت به مبدا") || s.contains("برگشت به مبدأ")
                || s.contains("بازگشت به مبدا") || s.contains("بازگشت به مبدأ")
                || s.contains("رفت و برگشت") || s.contains("رفت‌وبرگشت")
                || s.contains("رفت برگشت")
                || s.contains("رفت و برگشت به")
                || (s.contains("برگشت") && (s.contains("مبدا") || s.contains("مبدأ")))) {
            return true;
        }
        if (s.contains("round trip") || s.contains("roundtrip") || s.contains("Round trip")) {
            return true;
        }
        if (s.contains("لوپ") || s.contains("loop") || s.contains("Loop")
                || s.contains("سفر رفت و برگشت")
                || s.contains("آیکون برگشت") || s.contains("آیکون بازگشت")
                || s.contains("icon_return") || s.contains("ic_return")
                || s.contains("return_trip") || s.contains("return trip")
                || s.contains("بازگشت به مقصد") || s.contains("برگشت به مقصد اول")
                || s.contains("برگشت به مقصد")) {
            return true;
        }
        Matcher nearReturn = Pattern.compile(
                "(?:برگشت|بازگشت|loop|لوپ).{0,24}دقیقه|دقیقه.{0,24}(?:برگشت|بازگشت|loop|لوپ)"
        ).matcher(s);
        if (nearReturn.find()) {
            int a = nearReturn.start();
            int b = nearReturn.end();
            if (!isInsideStopRange(s, a, b)) {
                return true;
            }
        }
        return false;
    }

    public static NotificationParser.Parsed merge(NotificationParser.Parsed base, Legs legs) {
        if (base == null) return null;
        Double fare = base.getFare();
        boolean rial = base.getFareWasRial();
        Double pKm = legs.pickupKm != null ? legs.pickupKm : base.getPickupKm();
        Double tKm = legs.tripKm != null ? legs.tripKm : base.getTripKm();
        Double pMin = legs.pickupMin != null ? legs.pickupMin : base.getPickupMin();
        Double tMin = legs.tripMin != null ? legs.tripMin : base.getTripMin();
        return new NotificationParser.Parsed(fare, pKm, tKm, pMin, tMin, rial);
    }

    public static NotificationParser.Parsed mergeShape(NotificationParser.Parsed base, TripShape shape) {
        if (shape == null) return base;
        return merge(base, shape.legs);
    }
}
