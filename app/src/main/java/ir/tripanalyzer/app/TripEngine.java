package ir.tripanalyzer.app;

import android.content.Context;
import java.util.List;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;

/**
 * موتور تحلیل RAIDO v2.2.6:
 * - درآمد بعد کمیسیون = کرایه × 0.78
 * - معیار تحلیل = درآمد ساعتی بعد کمیسیون (بدون کم کردن بنزین/استهلاک از معیار رنگ)
 * - بنزین/استهلاک فقط برای نمایش سود جیب و گزارش
 * - کف پویا = 112k + بنزین‌ساعتی + استهلاک‌ساعتی (یا مقدار دستی کاربر)
 * - زرد تا 1.2×کف | سبز از 1.2×کف (پررنگ‌تر نزدیک هدف)
 * - بدون تلرانس 1.1 | km واقعی در صورت پارس؛ وگرنه تخمین با سرعت 30
 */
public final class TripEngine {
    public static final TripEngine INSTANCE = new TripEngine();
    public static final double COMMISSION_TAX_RATE = 0.22;
    public static final double SALARY_HOURLY = 112_000.0;
    public static final double GREEN_MULT = 1.2;
    public static final double MARKET_WEAK = 0.90;
    public static final double MARKET_GREEN = 0.95;
    public static final int MARKET_MIN_SAMPLES = 3;
    private static final ThreadLocal<double[]> MARKET_OVERRIDE = new ThreadLocal<>(); // [avg, count]

    /** سازگاری با کدهای قدیمی که به این ثابت‌ها ارجاع می‌دادند */
    public static final double BASE_WAGE = 110_000.0;
    public static final double MIN_FLOOR = 220_000.0;
    /** سرعت ثابت برای تخمین مسافت وقتی کیلومتر نیست */
    public static final double ESTIMATE_SPEED_KMH = 30.0;
    private static final ThreadLocal<Double> FLOOR_OVERRIDE = new ThreadLocal<>();

    private TripEngine() {}

    private enum Src { REAL, EST, MISS }

    private static final class Leg {
        final Double km, min;
        final Src dSrc, tSrc;
        Leg(Double km, Double min, Src dSrc, Src tSrc) {
            this.km = km; this.min = min; this.dSrc = dSrc; this.tSrc = tSrc;
        }
        Double getKm() { return km; }
        Double getMin() { return min; }
        Src getDSrc() { return dSrc; }
        Src getTSrc() { return tSrc; }
    }

    public double pickSpeedKmh(Context ctx, double totalTimeMin, EngineSettings s) {
        // تخمین مسافت فقط با سرعت ثابت ۳۰ (میانگین واقعی کار اسنپ ۲۵–۳۵)
        return ESTIMATE_SPEED_KMH;
    }

    private Leg resolveLeg(Double km, Double min, double speed) {
        if (km != null && min != null) return new Leg(km, min, Src.REAL, Src.REAL);
        if (km != null) return new Leg(km, (km / speed) * 60.0, Src.REAL, Src.EST);
        if (min != null) return new Leg((min / 60.0) * speed, min, Src.EST, Src.REAL);
        return new Leg(null, null, Src.MISS, Src.MISS);
    }

    private DataSource combine(Src a, Src b) {
        if (a == Src.REAL && b == Src.REAL) return DataSource.REAL;
        if ((a == Src.REAL && b == Src.EST) || (a == Src.EST && b == Src.REAL)) return DataSource.MIXED;
        if ((a == Src.REAL && b == Src.MISS) || (a == Src.MISS && b == Src.REAL)) return DataSource.MIXED;
        return DataSource.ESTIMATED;
    }

    /** سازگاری با فراخوانی‌های قدیمی */
    public static double floorHourly(double totalKm) {
        if (totalKm < 0) totalKm = 0;
        int coeff = (int) Math.floor(totalKm / 45.0) + 1;
        if (coeff < 2) coeff = 2;
        return BASE_WAGE * coeff;
    }

    /** fallback اگر Context نباشد؛ کف واقعی از Store.getAnalysisFloor */
    public static final double FIXED_FLOOR = 225_000.0;

    public static double computeFloor(EngineSettings s, double citySpeedKmh) {
        return FIXED_FLOOR;
    }

    public static double computeFloor(Context ctx) {
        try {
            if (ctx != null) return Store.INSTANCE.getAnalysisFloor(ctx);
        } catch (Throwable ignored) {}
        return FIXED_FLOOR;
    }

    /**
     * نسبت سبزی کارت: هرچه درآمد ساعتی بعد کمیسیون به هدف کاربر نزدیک‌تر، سبز پررنگ‌تر.
     * مرجع پایین = خط سبز (floor × 1.2)، مرجع بالا = هدف کاربر (یا 1.3×خط سبز).
     */
    public static double greenRatio(double hourlyNetReal, double floor, double targetHourly) {
        double greenLine = floor * GREEN_MULT;
        // به هدف شخصی رسیده یا از آن گذشته → کاملاً سبز
        if (targetHourly > 0 && hourlyNetReal >= targetHourly) return 1.0;
        // هدف بالاتر از خط سبز → بین خط سبز و هدف؛ بدون هدف معنادار → مرجع پیش‌فرض
        double top = targetHourly > greenLine ? targetHourly : greenLine * 1.3;
        double ratio = (hourlyNetReal - greenLine) / (top - greenLine);
        if (ratio < 0) ratio = 0;
        if (ratio > 1) ratio = 1;
        return ratio;
    }

    private TripResult gray() {
        return new TripResult(Verdict.GRAY, Confidence.LOW, CollectionsKt.emptyList(), null,
                null, null, null, null, null, null, null, null, null, null, null,
                DataSource.ESTIMATED, DataSource.ESTIMATED);
    }

    private Confidence confidence(DataSource d, DataSource t) {
        if (d == DataSource.REAL && t == DataSource.REAL) return Confidence.HIGH;
        if (d == DataSource.ESTIMATED || t == DataSource.ESTIMATED) return Confidence.LOW;
        return Confidence.MEDIUM;
    }

    public TripResult analyzeTotals(double fare, double totalDistanceKm, DataSource distanceSource,
                                    double totalTimeMin, DataSource timeSource, EngineSettings s) {
        return analyzeTotals(fare, totalDistanceKm, distanceSource, totalTimeMin, timeSource, s, 55.0);
    }

    public TripResult analyzeTotals(double fare, double totalDistanceKm, DataSource distanceSource,
                                    double totalTimeMin, DataSource timeSource, EngineSettings s,
                                    double citySpeedKmh) {
        Intrinsics.checkNotNullParameter(distanceSource, "distanceSource");
        Intrinsics.checkNotNullParameter(timeSource, "timeSource");
        Intrinsics.checkNotNullParameter(s, "s");

        double netAfter = fare * (1.0 - COMMISSION_TAX_RATE);
        double commission = fare - netAfter;

        double fuelCost = (s.getFuelPer100() / 100.0) * totalDistanceKm * s.getFuelPrice();
        double depCost = totalDistanceKm * s.getDepPerKm();
        double totalCost = fuelCost + depCost;
        double netIncome = netAfter - totalCost;

        double hours = totalTimeMin > 0 ? totalTimeMin / 60.0 : 0;
        // معیار تحلیل: درآمد ساعتی بعد کمیسیون (بدون کسر بنزین/استهلاک)
        double hourlyAfterCommission = hours > 0 ? netAfter / hours : 0;
        // سود جیب ساعتی — فقط نمایش/گزارش
        double hourlyPocket = hours > 0 ? netIncome / hours : 0;

        double floor;
        Double ov = FLOOR_OVERRIDE.get();
        if (ov != null && ov > 0) floor = ov;
        else floor = computeFloor(s, citySpeedKmh);
        double greenLine = floor * GREEN_MULT;
        double target = s.getTargetHourly();

        // مرجع بازار (اختیاری)
        double marketAvg = 0;
        int marketCount = 0;
        double[] mOv = MARKET_OVERRIDE.get();
        if (mOv != null && mOv.length >= 2) {
            marketAvg = mOv[0];
            marketCount = (int) mOv[1];
        }
        boolean marketOk = marketCount >= MARKET_MIN_SAMPLES && marketAvg > 0;

        Verdict verdict;
        List yellows;
        if (hourlyAfterCommission < floor) {
            // همیشه زیر کف معیشت = قرمز
            verdict = Verdict.RED;
            yellows = CollectionsKt.emptyList();
        } else if (marketOk) {
            // دوآستانه: F + بازار
            double weakLine = marketAvg * MARKET_WEAK;
            double marketGreenLine = Math.max(greenLine, marketAvg * MARKET_GREEN);
            if (hourlyAfterCommission < weakLine) {
                verdict = Verdict.YELLOW;
                yellows = CollectionsKt.listOf(YellowReason.NEAR_TARGET);
            } else if (hourlyAfterCommission >= marketGreenLine) {
                verdict = Verdict.GREEN;
                yellows = CollectionsKt.emptyList();
            } else {
                verdict = Verdict.YELLOW;
                yellows = CollectionsKt.listOf(YellowReason.NEAR_TARGET);
            }
        } else if (hourlyAfterCommission >= greenLine) {
            verdict = Verdict.GREEN;
            yellows = CollectionsKt.emptyList();
        } else {
            verdict = Verdict.YELLOW;
            yellows = CollectionsKt.listOf(YellowReason.NEAR_TARGET);
        }

        // رسیدن به هدف شخصی → سبز (اگر قرمز نباشد)
        if (target > 0 && hourlyAfterCommission >= target && verdict != Verdict.RED) {
            verdict = Verdict.GREEN;
            yellows = CollectionsKt.emptyList();
        }

        RejectReason reject = verdict == Verdict.RED
                ? (hourlyAfterCommission < floor ? RejectReason.LOW_FARE_PER_KM : RejectReason.LOW_NET)
                : null;

        // زمان بدون تلرانس ۱.۱
        double adjustedTime = totalTimeMin;
        return new TripResult(verdict, confidence(distanceSource, timeSource), yellows, reject,
                Double.valueOf(commission),
                Double.valueOf(netAfter),
                Double.valueOf(fuelCost),
                Double.valueOf(depCost),
                Double.valueOf(totalCost),
                Double.valueOf(netIncome),
                Double.valueOf(totalDistanceKm),
                Double.valueOf(totalTimeMin),
                Double.valueOf(adjustedTime),
                Double.valueOf(hourlyAfterCommission),
                Double.valueOf(totalDistanceKm > 0 ? netIncome / totalDistanceKm : 0),
                distanceSource, timeSource);
    }

    public TripResult analyze(RawTrip raw, EngineSettings s) {
        return analyze(raw, s, null);
    }

    public TripResult analyze(RawTrip raw, EngineSettings s, Context ctx) {
        Intrinsics.checkNotNullParameter(raw, "raw");
        Intrinsics.checkNotNullParameter(s, "s");
        if (raw.getFare() == null) return gray();

        double knownMin = 0;
        boolean anyMin = false;
        if (raw.getPickupMin() != null) { knownMin += raw.getPickupMin(); anyMin = true; }
        if (raw.getTripMin() != null) { knownMin += raw.getTripMin(); anyMin = true; }

        double speed = pickSpeedKmh(ctx, anyMin ? knownMin : 20.0, s);

        Leg l1 = resolveLeg(raw.getPickupKm(), raw.getPickupMin(), speed);
        Leg l2 = resolveLeg(raw.getTripKm(), raw.getTripMin(), speed);

        double sumMinProbe = 0;
        boolean hasMin = false;
        if (l1.getMin() != null) { sumMinProbe += l1.getMin(); hasMin = true; }
        if (l2.getMin() != null) { sumMinProbe += l2.getMin(); hasMin = true; }
        if (hasMin) {
            double speed2 = pickSpeedKmh(ctx, sumMinProbe, s);
            if (Math.abs(speed2 - speed) > 0.5) {
                l1 = resolveLeg(raw.getPickupKm(), raw.getPickupMin(), speed2);
                l2 = resolveLeg(raw.getTripKm(), raw.getTripMin(), speed2);
            }
        }

        List kms = CollectionsKt.listOfNotNull(new Double[]{l1.getKm(), l2.getKm()});
        List mins = CollectionsKt.listOfNotNull(new Double[]{l1.getMin(), l2.getMin()});
        if (kms.isEmpty() || mins.isEmpty()) return gray();

        double sumKm = 0, sumMin = 0;
        for (Object o : kms) sumKm += ((Double) o);
        for (Object o : mins) sumMin += ((Double) o);

        boolean timeWasReal = (raw.getPickupMin() != null || raw.getTripMin() != null);
        double adjustedMin = sumMin; // بدون تلرانس ۱۰٪

        double citySpd = ESTIMATE_SPEED_KMH;
        try {
            if (ctx != null) FLOOR_OVERRIDE.set((double) Store.INSTANCE.getAnalysisFloor(ctx));
            else FLOOR_OVERRIDE.set(FIXED_FLOOR);
        } catch (Throwable ignored) {
            FLOOR_OVERRIDE.set(FIXED_FLOOR);
        }
        try {
            if (ctx != null) {
                RaidoDataStore.MarketRef mr = RaidoDataStore.INSTANCE.marketRefNow(ctx);
                MARKET_OVERRIDE.set(new double[]{mr.avg, mr.count});
            } else {
                MARKET_OVERRIDE.set(new double[]{0, 0});
            }
        } catch (Throwable ignored) {
            MARKET_OVERRIDE.set(new double[]{0, 0});
        }
        try {
            return analyzeTotals(raw.getFare().doubleValue(), sumKm,
                    combine(l1.getDSrc(), l2.getDSrc()),
                    adjustedMin, combine(l1.getTSrc(), l2.getTSrc()), s, citySpd);
        } finally {
            FLOOR_OVERRIDE.remove();
            MARKET_OVERRIDE.remove();
        }
    }
}
