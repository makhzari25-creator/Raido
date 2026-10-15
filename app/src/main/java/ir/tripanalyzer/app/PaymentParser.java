package ir.tripanalyzer.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * سه شکل UI پرداخت پایان سفر اسنپ (قفل ۲۰۲۶-۱۰-۱۵).
 * مبالغ اسنپ معمولاً ریال‌اند → به تومان (/۱۰) نرمال می‌شوند مگر صریحاً تومان باشد.
 */
public final class PaymentParser {
    public static final PaymentParser INSTANCE = new PaymentParser();

    public enum Form {
        CREDIT_ONLY,   // وجه نقد دریافت نکنید + X به اعتبار (خالص)
        MIXED,         // نقدی Y + بقیه Z به اعتبار
        FULL_CASH,     // نقدی کامل + هزینه را نقدی دریافت کنید
        UNKNOWN
    }

    public static final class Result {
        public final Form form;
        /** مبلغ نقد از مسافر (تومان) — بدون کسر کمسیون */
        public final Double cashToman;
        /** مبلغ واریز به اعتبار (تومان) — خالص بعد کمسیون */
        public final Double creditToman;
        /** درآمد مؤثر برای گزارش (تومان) */
        public final Double effectiveToman;
        public final String labelFa;

        public Result(Form form, Double cashToman, Double creditToman, Double effectiveToman, String labelFa) {
            this.form = form;
            this.cashToman = cashToman;
            this.creditToman = creditToman;
            this.effectiveToman = effectiveToman;
            this.labelFa = labelFa;
        }
    }

    private PaymentParser() {}

    private static String norm(String s) {
        return SnappLegParser.normalizeDigits(s != null ? s.replace('\u200c', ' ') : "");
    }

    private static Double parseMoneyToToman(String num, boolean explicitToman, boolean explicitRial) {
        if (num == null) return null;
        try {
            double v = Double.parseDouble(num.replace(",", "").replace(" ", "").replace("٬", ""));
            if (explicitToman) return v;
            if (explicitRial) return v / 10.0;
            // اعداد بزرگ اسنپ معمولاً ریال
            if (v >= 10000) return v / 10.0;
            return v;
        } catch (Exception e) {
            return null;
        }
    }

    public Result parse(String raw) {
        if (raw == null) return new Result(Form.UNKNOWN, null, null, null, null);
        String t = norm(raw);

        boolean noCash = t.contains("وجه نقد دریافت نکنید") || t.contains("وجه نقد دريافت نکنيد");
        boolean getCashInstr = t.contains("هزینه سفر را نقدی از مسافر دریافت کنید")
                || t.contains("هزینه سفر را نقدی دریافت کنید")
                || t.contains("نقدی از مسافر دریافت");
        boolean creditLine = t.contains("به اعتبارتان افزوده") || t.contains("به اعتبار تان افزوده")
                || t.contains("به اعتبارتان اضافه");

        // شکل ۱: فقط اعتبار
        if (noCash) {
            Matcher m = Pattern.compile(
                    "(\\d{1,3}(?:[\\d,٬]{0,12}\\d)?)\\s*(ریال|تومان)?[^\\n]{0,20}(?:به\\s*اعتبار)"
            ).matcher(t);
            Double credit = null;
            if (m.find()) {
                boolean tom = m.group(2) != null && m.group(2).contains("تومان");
                boolean rial = m.group(2) != null && m.group(2).contains("ریال");
                credit = parseMoneyToToman(m.group(1), tom, rial || !tom);
            } else {
                // fallback: مبلغ X ریال به اعتبار
                Matcher m2 = Pattern.compile("مبلغ\\s*(\\d{1,3}(?:[\\d,٬]{0,12}\\d)?)\\s*(ریال|تومان)").matcher(t);
                if (m2.find()) {
                    boolean tom = "تومان".equals(m2.group(2));
                    credit = parseMoneyToToman(m2.group(1), tom, !tom);
                }
            }
            return new Result(Form.CREDIT_ONLY, 0.0, credit, credit,
                    "فقط اعتبار (خالص بعد کمسیون)");
        }

        // نقدی Y ریال
        Matcher cashM = Pattern.compile("نقدی\\s*(\\d{1,3}(?:[\\d,٬]{0,12}\\d)?)\\s*(ریال|تومان)").matcher(t);
        Double cash = null;
        if (cashM.find()) {
            boolean tom = "تومان".equals(cashM.group(2));
            cash = parseMoneyToToman(cashM.group(1), tom, !tom);
        }

        // بقیه Z به اعتبار
        Matcher restM = Pattern.compile(
                "بقیه\\s*(\\d{1,3}(?:[\\d,٬]{0,12}\\d)?)\\s*(ریال|تومان)\\s*به\\s*اعتبار"
        ).matcher(t);
        Double credit = null;
        if (restM.find()) {
            boolean tom = "تومان".equals(restM.group(2));
            credit = parseMoneyToToman(restM.group(1), tom, !tom);
        } else if (creditLine && cash != null) {
            Matcher anyCred = Pattern.compile(
                    "(\\d{1,3}(?:[\\d,٬]{0,12}\\d)?)\\s*(ریال|تومان)[^\\n]{0,15}به\\s*اعتبار"
            ).matcher(t);
            if (anyCred.find()) {
                boolean tom = "تومان".equals(anyCred.group(2));
                credit = parseMoneyToToman(anyCred.group(1), tom, !tom);
            }
        }

        if (cash != null && credit != null) {
            double eff = cash + credit;
            return new Result(Form.MIXED, cash, credit, eff, "ترکیبی: نقد + اعتبار");
        }
        if (cash != null && (getCashInstr || (!creditLine && !noCash))) {
            return new Result(Form.FULL_CASH, cash, 0.0, cash, "تمام‌نقد (بدون کسر روی مبلغ نقد)");
        }
        if (cash != null) {
            return new Result(Form.FULL_CASH, cash, 0.0, cash, "نقدی");
        }
        return new Result(Form.UNKNOWN, null, null, null, null);
    }

    /** اعمال روی JSON سفر برای گزارش */
    public void applyToTrip(org.json.JSONObject trip, Result r) {
        if (trip == null || r == null || r.form == Form.UNKNOWN) return;
        try {
            trip.put("payForm", r.form.name());
            if (r.labelFa != null) trip.put("payLabel", r.labelFa);
            if (r.cashToman != null) trip.put("payCashToman", r.cashToman);
            if (r.creditToman != null) trip.put("payCreditToman", r.creditToman);
            if (r.effectiveToman != null) {
                trip.put("payEffectiveToman", r.effectiveToman);
                // fare گزارش را با درآمد مؤثر هم‌تراز کن (تومان)
                trip.put("fare", r.effectiveToman);
                trip.put("fareFinal", r.effectiveToman);
            }
        } catch (Throwable ignored) {}
    }
}
