package ir.tripanalyzer.app;

/**
 * تشخیص وضعیت صفحه اسنپ از متن Accessibility.
 * قانون جزئیات (قفل ۲۰۲۶-۱۰-۱۵):
 * - اگر جزئیات کنار قبول باشد و زمان یا کیلومتر یکی نباشد → تحلیل نکن
 * - اگر هم زمان و هم کیلومتر باشد → تحلیل حتی با جزئیات
 * - اگر جزئیات نباشد → با کیلومتر یا زمان تحلیل
 */
public final class ScreenHints {
    private ScreenHints() {}

    public static boolean hasViewDetailsButton(String raw) {
        if (raw == null) return false;
        String t = raw.replace('\u200c', ' ');
        // «جزئیات» کنار قبول — نه لزوماً فقط «مشاهده جزئیات»
        boolean hasDetailsWord = t.contains("مشاهده جزئیات")
                || t.contains("مشاهده جزییات")
                || t.contains("جزئیات")
                || t.contains("جزییات");
        boolean hasAccept = t.contains("قبول سفر") || t.contains("قبول");
        // اگر فقط کلمه جزئیات در راهنما باشد اشتباه نشود؛ با قبول یا مشاهده قوی‌تر است
        if (t.contains("مشاهده جزئیات") || t.contains("مشاهده جزییات")) return true;
        return hasDetailsWord && hasAccept;
    }

    public static boolean hasRealKm(NotificationParser.Parsed p) {
        if (p == null) return false;
        Double a = p.getPickupKm();
        Double b = p.getTripKm();
        return (a != null && a > 0) || (b != null && b > 0);
    }

    public static boolean hasAnyTime(NotificationParser.Parsed p) {
        if (p == null) return false;
        Double a = p.getPickupMin();
        Double b = p.getTripMin();
        return (a != null && a > 0) || (b != null && b > 0);
    }

    /**
     * آیا الان وقت تحلیل است؟
     */
    public static boolean shouldAnalyzeNow(String raw, NotificationParser.Parsed p) {
        if (p == null || p.getFare() == null) return false;
        boolean time = hasAnyTime(p);
        boolean km = hasRealKm(p);
        if (time && km) return true; // هر دو کامل — حتی با جزئیات
        if (hasViewDetailsButton(raw)) {
            // جزئیات هست و یکی از زمان/کیلومتر نیست → صبر
            return false;
        }
        // بدون جزئیات: کیلومتر یا زمان کافی است
        return time || km;
    }
}
