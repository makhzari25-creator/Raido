package ir.tripanalyzer.app;

import android.content.Context;

/**
 * لایهٔ H_شیفت برای برگشت خالی (فقط Premium).
 *
 * d_out = max(0, فاصله تا مرکز نزدیک‌ترین شهر − شعاع)
 * T_بازگشت = d_out / 30
 * C_بازگشت = d_out × (بنزین/km + استهلاک/km)
 * H_شیفت = (کرایه×0.78 − C_بازگشت) / (T_سفر + T_بازگشت)
 *
 * حالت‌ها:
 * A — مبدأ و مقصد داخل یک محدوده
 * B — داخل محدوده‌های شهری ولی شهر متفاوت
 * C — مقصد خارج همهٔ شعاع‌ها → H_شیفت
 *
 * منطق تحلیل عمداً تغییر نکرده؛ فقط شواهد فراخوانی API نشان
 * (http/status/lat/lng) برای نمایش در جزئیات اضافه شده.
 */
public final class ShiftAnalyzer {
    public static final ShiftAnalyzer INSTANCE = new ShiftAnalyzer();

    public enum ZoneMode { A, B, C, UNKNOWN }

    public static final class Result {
        public final ZoneMode mode;
        public final double dOutKm;
        public final double hShift;
        public final String labelFa;
        public final String originCity;
        public final String destCity;
        public final boolean applied; // آیا رنگ از H_شیفت آمده

        // شواهد API نشان (مقصد) — برای اثبات فراخوانی واقعی
        public final boolean geoAttempted;
        public final boolean geoOk;
        public final int geoHttp;
        public final String geoApiStatus;
        public final Double destLat;
        public final Double destLng;
        public final String geoError;
        public final String geoEndpoint;

        public Result(ZoneMode mode, double dOutKm, double hShift, String labelFa,
                      String originCity, String destCity, boolean applied,
                      boolean geoAttempted, boolean geoOk, int geoHttp,
                      String geoApiStatus, Double destLat, Double destLng,
                      String geoError, String geoEndpoint) {
            this.mode = mode;
            this.dOutKm = dOutKm;
            this.hShift = hShift;
            this.labelFa = labelFa;
            this.originCity = originCity;
            this.destCity = destCity;
            this.applied = applied;
            this.geoAttempted = geoAttempted;
            this.geoOk = geoOk;
            this.geoHttp = geoHttp;
            this.geoApiStatus = geoApiStatus;
            this.destLat = destLat;
            this.destLng = destLng;
            this.geoError = geoError;
            this.geoEndpoint = geoEndpoint;
        }
    }

    private ShiftAnalyzer() {}

    public Result analyze(Context ctx, String originText, String destText,
                          double fare, double totalTimeMin, EngineSettings settings) {
        if (ctx == null || !LicenseStore.INSTANCE.isPremium(ctx)) {
            return new Result(ZoneMode.UNKNOWN, 0, 0, "", null, null, false,
                    false, false, 0, null, null, null, "غیرپرمیوم", null);
        }
        if (destText == null || destText.trim().isEmpty()) {
            return new Result(ZoneMode.UNKNOWN, 0, 0, "", null, null, false,
                    false, false, 0, null, null, null, "آدرس مقصد پارس نشد", null);
        }

        // ژئوکد مقصد — منبع مختصات فقط API نشان
        NeshanClient.GeocodeResult destRes = NeshanClient.INSTANCE.geocodeDetailed(ctx, destText);
        if (!destRes.ok) {
            String msg = destRes.error != null ? destRes.error : "ژئوکد مقصد ناموفق";
            return new Result(ZoneMode.UNKNOWN, 0, 0, msg, null, null, false,
                    destRes.attempted, false, destRes.httpCode, destRes.apiStatus,
                    destRes.lat, destRes.lng, destRes.error, destRes.endpoint);
        }

        NeshanClient.GeoPoint dest = destRes.toPoint();

        // ژئوکد مبدأ (اختیاری برای تشخیص A/B)
        NeshanClient.GeoPoint origin = null;
        if (originText != null && originText.trim().length() >= 4) {
            NeshanClient.GeocodeResult oRes = NeshanClient.INSTANCE.geocodeDetailed(ctx, originText);
            origin = oRes.toPoint();
        }

        UrbanZones.City originCity = origin != null
                ? UrbanZones.INSTANCE.findContaining(ctx, origin.lat, origin.lng) : null;
        UrbanZones.City destCity = UrbanZones.INSTANCE.findContaining(ctx, dest.lat, dest.lng);
        UrbanZones.Nearest nearestDest = UrbanZones.INSTANCE.nearest(ctx, dest.lat, dest.lng);

        if (destCity != null && originCity != null && originCity.id.equals(destCity.id)) {
            return new Result(ZoneMode.A, 0, 0,
                    "محدوده شهری (محل فعالیت) · " + destCity.nameFa,
                    originCity.nameFa, destCity.nameFa, false,
                    true, true, destRes.httpCode, destRes.apiStatus,
                    destRes.lat, destRes.lng, null, destRes.endpoint);
        }
        if (destCity != null) {
            String oc = originCity != null ? originCity.nameFa : "—";
            return new Result(ZoneMode.B, 0, 0,
                    "محدوده شهری (خارج از محل فعالیت) · " + destCity.nameFa,
                    oc, destCity.nameFa, false,
                    true, true, destRes.httpCode, destRes.apiStatus,
                    destRes.lat, destRes.lng, null, destRes.endpoint);
        }

        // حالت C
        double dOut = nearestDest != null ? nearestDest.dOutKm : 0;
        String nearName = nearestDest != null ? nearestDest.city.nameFa : "شهر";
        double fuelPerKm = (settings.getFuelPer100() / 100.0) * settings.getFuelPrice();
        double depPerKm = settings.getDepPerKm();
        double cReturn = dOut * (fuelPerKm + depPerKm);
        double tReturnH = dOut / TripEngine.ESTIMATE_SPEED_KMH;
        double tTripH = totalTimeMin > 0 ? totalTimeMin / 60.0 : 0.01;
        double net = fare * (1.0 - TripEngine.COMMISSION_TAX_RATE) - cReturn;
        double hShift = (tTripH + tReturnH) > 0 ? net / (tTripH + tReturnH) : 0;

        String label = String.format(
                java.util.Locale.US,
                "خارج محدوده شهری · حدود %.1f km تا بافت %s",
                dOut, nearName);

        return new Result(ZoneMode.C, dOut, hShift, label,
                originCity != null ? originCity.nameFa : null,
                nearName, true,
                true, true, destRes.httpCode, destRes.apiStatus,
                destRes.lat, destRes.lng, null, destRes.endpoint);
    }

    /** رنگ‌بندی مجدد بر اساس H_شیفت با همان آستانه‌های F و 1.2F */
    public TripResult recolorWithShift(TripResult base, double hShift, double floor, double targetHourly) {
        if (base == null) return null;
        double greenLine = floor * TripEngine.GREEN_MULT;
        Verdict v;
        if (hShift < floor) {
            v = Verdict.RED;
        } else if (hShift >= greenLine || (targetHourly > 0 && hShift >= targetHourly)) {
            v = Verdict.GREEN;
        } else {
            v = Verdict.YELLOW;
        }
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
