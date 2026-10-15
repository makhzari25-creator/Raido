package ir.tripanalyzer.app;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * استخراج مبدأ و مقصد از متن صفحه اسنپ.
 *
 * الگوی رایج:
 *   ۵ دقیقه تا مبدأ - ۳ کیلومتر
 *   روستای ایسی کلا، جاده بابل - بابلسر، بهارستان
 *   ۲۳ دقیقه - ۱۱ کیلومتر
 *   شهر بابل، فرهنگ، بلوار مدرس
 */
public final class AddressExtractor {
    public static final AddressExtractor INSTANCE = new AddressExtractor();

    private static final Pattern PICKUP_LEG = Pattern.compile(
            ".*دقیقه\\s*تا\\s*(?:رسیدن\\s*)?(?:به\\s*)?(?:مبد[اأ]?|مسافر|محل|سوار).*");
    private static final Pattern TIME_OR_DIST = Pattern.compile(
            ".*(\\d+[\\d٬,]*)\\s*(دقیقه|کیلومتر|km|متر).*");
    private static final Pattern JUNK = Pattern.compile(
            ".*(تومان|قبول|مشاهده|جزئیات|انگشت|زوم|نقشه\\s*باز|بیشتر از|گزارش|تحلیل|GREEN|YELLOW|RED).*");

    private AddressExtractor() {}

    private boolean isNoise(String t) {
        return isJunk(t);
    }

    private boolean isJunk(String t) {
        if (t == null) return true;
        String s = t.trim();
        if (s.length() < 10) return true;
        if (JUNK.matcher(s).matches()) return true;
        if (s.contains("تومان") || s.contains("قبول") || s.contains("مشاهده")) return true;
        if (s.contains("انگشت") || s.contains("زوم") || s.contains("نقشه باز")) return true;
        return false;
    }

    private boolean isLegLine(String t) {
        if (t == null) return false;
        return TIME_OR_DIST.matcher(t.trim()).matches();
    }

    private boolean isAddressCandidate(String t) {
        if (t == null) return false;
        String s = t.trim();
        if (isJunk(s)) return false;
        if (isLegLine(s)) return false;
        // آدرس معمولاً ویرگول/محله/خیابان/شهر دارد یا طول مناسب
        if (s.length() < 12) return false;
        if (s.contains("مبدأ") || s.contains("مبدا")) return false;
        return true;
    }

    /**
     * @return [origin, dest] — ممکن است هر کدام null باشد
     */
    public String[] extract(String str) {
        String[] out = {null, null};
        if (str == null || str.trim().isEmpty()) return out;

        String[] lines = str.replace("\r", "\n").split("\n");
        List<String> cleaned = new ArrayList<>();
        for (String line : lines) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            cleaned.add(t);
        }

        String origin = null;
        String dest = null;

        // روش ۱: بعد از خط «تا مبدأ» → آدرس مبدأ؛ بعد از خط سفر (دقیقه بدون تا مبدأ) → مقصد
        for (int i = 0; i < cleaned.size(); i++) {
            String line = cleaned.get(i);
            if (PICKUP_LEG.matcher(line).matches()) {
                // آدرس بعدی معتبر
                for (int j = i + 1; j < cleaned.size() && j <= i + 3; j++) {
                    if (isAddressCandidate(cleaned.get(j))) {
                        origin = cleaned.get(j);
                        break;
                    }
                    if (isLegLine(cleaned.get(j))) break;
                }
            } else if (isLegLine(line) && !PICKUP_LEG.matcher(line).matches()) {
                // خط زمان/مسافت سفر (نه تا مبدأ)
                for (int j = i + 1; j < cleaned.size() && j <= i + 3; j++) {
                    if (isAddressCandidate(cleaned.get(j))) {
                        dest = cleaned.get(j);
                        break;
                    }
                    if (isLegLine(cleaned.get(j))) break;
                }
            }
        }

        // روش ۲: اگر یکی خالی ماند — همهٔ کاندیداهای آدرس به ترتیب
        if (origin == null || dest == null) {
            List<String> addrs = new ArrayList<>();
            for (String line : cleaned) {
                if (isAddressCandidate(line)) addrs.add(line);
            }
            if (origin == null && addrs.size() >= 1) origin = addrs.get(0);
            if (dest == null && addrs.size() >= 2) {
                // مقصد معمولاً آخرین آدرس غیرتکراری است
                for (int k = addrs.size() - 1; k >= 0; k--) {
                    if (origin == null || !addrs.get(k).equals(origin)) {
                        dest = addrs.get(k);
                        break;
                    }
                }
            }
        }

        // نرمال‌سازی فاصله
        if (origin != null) origin = origin.replaceAll("\\s+", " ").trim();
        if (dest != null) dest = dest.replaceAll("\\s+", " ").trim();

        out[0] = origin;
        out[1] = dest;
        return out;
    }

    /**
     * [0]=مبدأ [1]=مقصد۱ [2]=مقصد۲ (یا null)
     * برای دو‌مقصده هر دو مقصد را برمی‌گرداند.
     */
    public String[] extractMulti(String raw) {
        String[] two = extract(raw);
        String origin = two[0];
        String dest1 = two[1];
        String dest2 = null;
        if (raw == null) return new String[]{origin, dest1, null};
        String norm = raw.replace('\u200c', ' ');
        String[] lines = norm.split("\r?\n");
        java.util.List<String> addrs = new java.util.ArrayList<>();
        for (String line : lines) {
            String s = line != null ? line.trim() : "";
            if (s.isEmpty()) continue;
            if (isNoise(s)) continue;
            if (isLegLine(s)) continue;
            if (isAddressCandidate(s)) {
                // رد کردن تکرار مبدأ
                if (origin != null && s.equals(origin)) continue;
                addrs.add(s.replaceAll("\\s+", " ").trim());
            }
        }
        // یکتا
        java.util.LinkedHashSet<String> uniq = new java.util.LinkedHashSet<>(addrs);
        addrs = new java.util.ArrayList<>(uniq);
        if (dest1 == null && !addrs.isEmpty()) dest1 = addrs.get(0);
        if (addrs.size() >= 2) {
            // اگر dest1 همان اولی است، دومی dest2
            if (dest1 != null) {
                for (String a : addrs) {
                    if (!a.equals(dest1) && (origin == null || !a.equals(origin))) {
                        dest2 = a;
                        break;
                    }
                }
            } else {
                dest1 = addrs.get(0);
                dest2 = addrs.get(1);
            }
        } else if (addrs.size() == 1 && dest1 == null) {
            dest1 = addrs.get(0);
        }
        // شمارش برچسب مقصد در متن برای اجبار دو مقصد
        String normCount = norm.replace("از مقصد سفر فعلی", " ").replace("از مقصد سفر", " ");
        int destTags = 0;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("مقصد").matcher(normCount);
        while (m.find()) destTags++;
        if (destTags >= 2 && dest2 == null && addrs.size() >= 2) {
            dest1 = addrs.get(addrs.size() - 2);
            dest2 = addrs.get(addrs.size() - 1);
        }
        return new String[]{origin, dest1, dest2};
    }

}
