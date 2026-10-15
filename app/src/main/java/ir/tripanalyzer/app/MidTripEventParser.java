package ir.tripanalyzer.app;

import android.content.Context;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * رویدادهای حین سفر: توقف / مقصد دوم / آپدیت قیمت با کادر «متوجه شدم».
 */
public final class MidTripEventParser {
    public static final MidTripEventParser INSTANCE = new MidTripEventParser();

    public static final int STOP_FEE_PER_MIN = 1000; // تومان

    private MidTripEventParser() {}

    private static String norm(String s) {
        return SnappLegParser.normalizeDigits(s != null ? s.replace('\u200c', ' ') : "");
    }

    public boolean hasUnderstoodButton(String raw) {
        if (raw == null) return false;
        String t = norm(raw);
        return t.contains("متوجه شدم") || t.contains("متوجه شدم.");
    }

    /** بیشینه دقیقه در بازه «۵ تا ۱۰ دقیقه» */
    public Integer parseStopMaxMinutes(String raw) {
        String t = norm(raw);
        boolean stopHint = t.contains("توقف") || t.contains("صبر") || t.contains("انتظار");
        Matcher range = Pattern.compile("(\\d{1,3})\\s*تا\\s*(\\d{1,3})\\s*دقیقه").matcher(t);
        if (range.find()) {
            int a = Integer.parseInt(range.group(1));
            int b = Integer.parseInt(range.group(2));
            int max = Math.max(a, b);
            // اگر نشانه توقف نباشد ولی بازه نزدیک آیکون برگشت/توقف باشد باز هم قبول در حضور متوجه شدم
            if (stopHint || hasUnderstoodButton(raw)) return max;
        }
        Matcher one = Pattern.compile("توقف[^\\d]{0,12}(\\d{1,3})\\s*دقیقه").matcher(t);
        if (one.find()) return Integer.parseInt(one.group(1));
        return null;
    }

    public boolean looksLikeSecondDest(String raw) {
        String t = norm(raw);
        return t.contains("مقصد دوم") || t.contains("مقصد ۲")
                || t.contains("مقصد اضافه") || t.contains("توقفگاه بعدی")
                || (t.contains("مسافر") && t.contains("مقصد") && t.contains("اضافه"));
    }

    /** قیمت بزرگ روی کارت (تومان) */
    public Double parseUpdatedFareToman(String raw) {
        String t = norm(raw);
        Matcher m = Pattern.compile("(\\d{1,3}(?:[\\d,٬]{3,12}))\\s*(تومان|ریال)").matcher(t);
        Double best = null;
        while (m.find()) {
            String num = m.group(1).replace(",", "").replace("٬", "");
            try {
                double v = Double.parseDouble(num);
                boolean tom = "تومان".equals(m.group(2));
                if (!tom) v = v / 10.0;
                if (best == null || v > best) best = v;
            } catch (Exception ignored) {}
        }
        return best;
    }

    /**
     * اگر صفحه رویداد حین سفر باشد، activeTrip را آپدیت می‌کند.
     * @return true اگر تغییری اعمال شد
     */
    public boolean tryUpdateActiveTrip(Context ctx, String raw) {
        if (ctx == null || raw == null) return false;
        // بدون «متوجه شدم» معمولاً دیالوگ تأیید اسنپ نیست — برای توقف بازه+توقف هم کافی است
        Integer stopMin = parseStopMaxMinutes(raw);
        boolean second = looksLikeSecondDest(raw);
        boolean understood = hasUnderstoodButton(raw);
        Double fare = parseUpdatedFareToman(raw);

        if (!understood && stopMin == null && !second) return false;
        // اگر فقط قیمت بدون کانتکست — نادیده
        if (!understood && stopMin == null && !second) return false;

        JSONObject active = Store.INSTANCE.getActiveTrip(ctx);
        if (active == null) return false;

        boolean changed = false;
        try {
            if (stopMin != null && stopMin > 0) {
                int fee = stopMin * STOP_FEE_PER_MIN;
                active.put("stopMin", stopMin);
                active.put("stopFeeToman", fee);
                // به fare اضافه کن اگر هنوز اعمال نشده
                double base = active.optDouble("fare", 0);
                double prevStop = active.optDouble("stopFeeApplied", 0);
                if (Math.abs(prevStop - fee) > 0.5) {
                    base = base - prevStop + fee;
                    active.put("fare", base);
                    active.put("stopFeeApplied", fee);
                    changed = true;
                }
            }
            if (second) {
                active.put("tripKind", "TWO_DEST");
                active.put("midTripSecondDest", true);
                changed = true;
            }
            if (fare != null && fare > 0 && understood) {
                // قیمت جدید اسنپ جایگزین (شامل توقف/مقصد دوم)
                active.put("fare", fare);
                active.put("fareUpdatedMidTrip", true);
                changed = true;
            }
            if (changed) {
                active.put("midTripUpdatedAt", System.currentTimeMillis());
                Store.INSTANCE.saveActiveTrip(ctx, active);
                // اگر last همان ts است همگام کن
                JSONObject last = Store.INSTANCE.last(ctx);
                if (last != null && last.optLong("ts", -1) == active.optLong("ts", -2)) {
                    last.put("fare", active.optDouble("fare"));
                    if (active.has("stopMin")) last.put("stopMin", active.optInt("stopMin"));
                    if (active.has("stopFeeToman")) last.put("stopFeeToman", active.optInt("stopFeeToman"));
                    if (active.has("tripKind")) last.put("tripKind", active.optString("tripKind"));
                    Store.INSTANCE.saveLast(ctx, last);
                }
            }
        } catch (Throwable t) {
            return false;
        }
        return changed;
    }
}
