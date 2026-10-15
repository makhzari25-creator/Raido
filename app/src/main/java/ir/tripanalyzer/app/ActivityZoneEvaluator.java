package ir.tripanalyzer.app;

import android.content.Context;
import android.location.Location;

/**
 * ارزیابی داخل/خارج محدوده (مرکز + شعاع).
 * بدون فایل نقشه / بدون نقطه / خاموش → UNKNOWN و verd دست‌نخورده.
 *
 * خارج: dOut = max(0, dist − radius) → H_شیفت
 * سختگیری روشن → RED ؛ وگرنه hShift < کف → RED وگرنه YELLOW (هرگز GREEN)
 */
public final class ActivityZoneEvaluator {
    public static final ActivityZoneEvaluator INSTANCE = new ActivityZoneEvaluator();

    public enum Status { IN, OUT, UNKNOWN }

    public static final class Result {
        public final Status status;
        public final String labelFa;
        public final Double distanceKm;
        public final boolean appliedToVerdict;
        public final Double dOutKm;

        public Result(Status status, String labelFa, Double distanceKm, boolean appliedToVerdict) {
            this(status, labelFa, distanceKm, appliedToVerdict, null);
        }

        public Result(Status status, String labelFa, Double distanceKm, boolean appliedToVerdict, Double dOutKm) {
            this.status = status;
            this.labelFa = labelFa;
            this.distanceKm = distanceKm;
            this.appliedToVerdict = appliedToVerdict;
            this.dOutKm = dOutKm;
        }
    }

    private ActivityZoneEvaluator() {}

    public static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        float[] r = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, r);
        return r[0] / 1000.0;
    }

    public Result evaluate(Context ctx, Double destLat, Double destLng) {
        if (!ActivityZoneStore.INSTANCE.isEnabled(ctx)) {
            return new Result(Status.UNKNOWN, "محدوده غیرفعال", null, false);
        }
        if (!LicenseStore.INSTANCE.isPremium(ctx)) {
            return new Result(Status.UNKNOWN, "نیاز به پرمیوم", null, false);
        }
        if (!ActivityZoneStore.INSTANCE.hasMapFile(ctx)) {
            return new Result(Status.UNKNOWN, "نقشه آپلود نشده", null, false);
        }
        if (!ActivityZoneStore.INSTANCE.hasPoint(ctx)) {
            return new Result(Status.UNKNOWN, "نقطه مرجع تنظیم نشده", null, false);
        }
        if (destLat == null || destLng == null) {
            return new Result(Status.UNKNOWN, "محدوده نامشخص", null, false);
        }
        double rLat = ActivityZoneStore.INSTANCE.getLat(ctx);
        double rLng = ActivityZoneStore.INSTANCE.getLng(ctx);
        double radius = ActivityZoneStore.INSTANCE.getRadiusKm(ctx);
        if (radius <= 0) radius = 5;
        double d = haversineKm(rLat, rLng, destLat, destLng);
        if (d <= radius + 1e-6) {
            return new Result(Status.IN,
                    String.format("داخل محدوده · %.1f کم (شعاع %.0f کم)", d, radius),
                    d, true, 0.0);
        }
        double dOut = Math.max(0, d - radius);
        return new Result(Status.OUT,
                String.format("خارج محدوده · %.1f کم از مرکز (%.1f کم بیرون شعاع)", d, dOut),
                d, true, dOut);
    }

    public TripResult applyToVerdict(Context ctx, TripResult base, Result zone) {
        if (base == null || zone == null || !zone.appliedToVerdict) return base;
        if (zone.status != Status.OUT) return base;
        if (base.getVerdict() == Verdict.GRAY) return base;

        double dOut = zone.dOutKm != null ? zone.dOutKm : 0;
        if (dOut < 0) dOut = 0;

        EngineSettings s;
        try {
            s = Store.INSTANCE.settings(ctx);
        } catch (Throwable t) {
            s = null;
        }
        double fuelPer100 = s != null ? s.getFuelPer100() : 8.0;
        double fuelPrice = s != null ? s.getFuelPrice() : 10000.0;
        double depPerKm = s != null ? s.getDepPerKm() : 2850.0;

        double fuelPerKm = (fuelPer100 / 100.0) * fuelPrice;
        double cReturn = dOut * (fuelPerKm + depPerKm);
        double tReturnH = dOut / Math.max(1.0, TripEngine.ESTIMATE_SPEED_KMH);

        double rawMin = 0;
        if (base.getRawTimeMin() != null) rawMin = base.getRawTimeMin();
        else if (base.getAdjustedTimeMin() != null) rawMin = base.getAdjustedTimeMin();
        double tTripH = rawMin > 0 ? rawMin / 60.0 : 0.01;

        double netAfter = base.getNetAfterCommission() != null ? base.getNetAfterCommission() : 0;
        double netShift = netAfter - cReturn;
        double hShift = (tTripH + tReturnH) > 0 ? netShift / (tTripH + tReturnH) : 0;

        double floor;
        try {
            floor = TripEngine.computeFloor(ctx);
        } catch (Throwable t) {
            floor = TripEngine.FIXED_FLOOR;
        }

        Verdict v;
        if (ActivityZoneStore.INSTANCE.isStrictRed(ctx)) {
            v = Verdict.RED;
        } else if (hShift < floor) {
            v = Verdict.RED;
        } else {
            v = Verdict.YELLOW;
        }
        if (base.getVerdict() == Verdict.RED) v = Verdict.RED;

        return new TripResult(
                v,
                base.getConfidence(),
                base.getYellowReasons(),
                v == Verdict.RED ? RejectReason.LOW_NET : null,
                base.getCommission(),
                base.getNetAfterCommission(),
                base.getFuelCost(),
                base.getDepCost(),
                base.getTotalCost(),
                base.getNetIncome(),
                base.getTotalDistanceKm(),
                base.getRawTimeMin(),
                base.getAdjustedTimeMin(),
                Double.valueOf(hShift),
                base.getPerKmIncome(),
                base.getDistanceSource(),
                base.getTimeSource()
        );
    }
}
