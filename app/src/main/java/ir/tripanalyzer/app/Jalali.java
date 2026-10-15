package ir.tripanalyzer.app;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/** تبدیل میلادی ↔ شمسی (بدون وابستگی خارجی) */
public final class Jalali {
    private static final String[] MONTH_NAMES = {
            "", "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    };
    private static final String[] WEEKDAYS = {
            "", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"
    };

    private Jalali() {}

    /** @return int[]{year, month, day} شمسی */
    public static int[] toJalali(long epochMs) {
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        c.setTimeInMillis(epochMs);
        return toJalali(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    public static int[] toJalali(int gy, int gm, int gd) {
        int[] g_d_m = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
        int gy2 = (gm > 2) ? (gy + 1) : gy;
        int days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100)
                + ((gy2 + 399) / 400) + gd + g_d_m[gm - 1];
        int jy = -1595 + (33 * (days / 12053));
        days %= 12053;
        jy += 4 * (days / 1461);
        days %= 1461;
        if (days > 365) {
            jy += (days - 1) / 365;
            days = (days - 1) % 365;
        }
        int jm, jd;
        if (days < 186) {
            jm = 1 + days / 31;
            jd = 1 + (days % 31);
        } else {
            jm = 7 + (days - 186) / 30;
            jd = 1 + ((days - 186) % 30);
        }
        return new int[]{jy, jm, jd};
    }

    public static int[] nowJalali() {
        return toJalali(System.currentTimeMillis());
    }

    public static String monthName(int jm) {
        if (jm < 1 || jm > 12) return "";
        return MONTH_NAMES[jm];
    }

    /** Calendar.SUNDAY=1 ... SATURDAY=7 → نام فارسی */
    public static String weekdayName(long epochMs) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(epochMs);
        int d = c.get(Calendar.DAY_OF_WEEK); // 1 Sun .. 7 Sat
        return WEEKDAYS[d];
    }

    public static String formatDate(long epochMs) {
        int[] j = toJalali(epochMs);
        return weekdayName(epochMs) + " " + j[0] + "/" + j[1] + "/" + j[2];
    }

    public static String formatDateShort(int jy, int jm, int jd) {
        return jy + "/" + jm + "/" + jd;
    }

    public static String dayKey(long epochMs) {
        int[] j = toJalali(epochMs);
        return j[0] + "-" + j[1] + "-" + j[2];
    }

    public static String monthKey(long epochMs) {
        int[] j = toJalali(epochMs);
        return j[0] + "-" + j[1];
    }

    public static boolean sameJalaliMonth(long epochMs, int jy, int jm) {
        int[] j = toJalali(epochMs);
        return j[0] == jy && j[1] == jm;
    }

    public static boolean sameJalaliDay(long epochMs, int jy, int jm, int jd) {
        int[] j = toJalali(epochMs);
        return j[0] == jy && j[1] == jm && j[2] == jd;
    }

    public static int hourOfDay(long epochMs) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(epochMs);
        return c.get(Calendar.HOUR_OF_DAY);
    }

    public static String faDigits(String s) {
        if (s == null) return "";
        char[] out = s.toCharArray();
        for (int i = 0; i < out.length; i++) {
            if (out[i] >= '0' && out[i] <= '9') out[i] = (char) ('\u06F0' + (out[i] - '0'));
        }
        return new String(out);
    }
}
