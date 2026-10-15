package ir.tripanalyzer.app.core;

import android.content.Context;

import ir.tripanalyzer.app.ActivityZoneEvaluator;
import ir.tripanalyzer.app.ActivityZoneStore;
import ir.tripanalyzer.app.EngineSettings;
import ir.tripanalyzer.app.LicenseStore;
import ir.tripanalyzer.app.NeshanClient;
import ir.tripanalyzer.app.ShiftAnalyzer;
import ir.tripanalyzer.app.TripEngine;
import ir.tripanalyzer.app.TripResult;
import ir.tripanalyzer.app.Verdict;

/**
 * لایه ژئو:
 * - اگر محدوده فعالیت روشن باشد: فقط ژئوکد مقصد + ارزیابی زون کاربر (ماژول شهری قدیمی خاموش)
 * - اگر خاموش باشد: همان ShiftAnalyzer شهری (A/B/C + H_شیفت)
 */
public final class GeoLayer {
    public static final GeoLayer INSTANCE = new GeoLayer();

    private GeoLayer() {}

    public static final class Enrichment {
        public String zoneLabel;
        public String geoStatus;
        public boolean geoAttempted;
        public boolean geoOk;
        public int geoHttp;
        public String geoApiStatus;
        public Double destLat;
        public Double destLng;
        public String geoError;
        public String geoEndpoint;
        public TripResult analyze;
        /** in | out | unknown | disabled */
        public String activityZoneStatus;
        public Double activityZoneDistanceKm;
        public boolean activityZoneMode;
    }

    public Enrichment enrich(
            Context applicationContext,
            TripResult analyze,
            Double fare,
            Double pickupMin,
            Double tripMin,
            String originAddr,
            String destAddr,
            EngineSettings engineSettings
    ) {
        return enrich(applicationContext, analyze, fare, pickupMin, tripMin, originAddr, destAddr, null, engineSettings);
    }

    public Enrichment enrich(
            Context applicationContext,
            TripResult analyze,
            Double fare,
            Double pickupMin,
            Double tripMin,
            String originAddr,
            String destAddr,
            String dest2Addr,
            EngineSettings engineSettings
    ) {
        Enrichment e = new Enrichment();
        e.analyze = analyze;

        // محدوده فقط وقتی کامل است روی تحلیل اثر دارد؛ وگرنه مسیر شهری/عادی
        boolean azReady = ActivityZoneStore.INSTANCE.isZoneActiveForAnalysis(applicationContext);
        e.activityZoneMode = azReady;

        if (azReady) {
            // مسیر محدوده فعالیت — ماژول شهری قدیمی غیرفعال
            try {
                if (destAddr == null || destAddr.trim().isEmpty()) {
                    e.geoStatus = "fail_no_dest";
                    e.activityZoneStatus = "unknown";
                    e.zoneLabel = "محدوده نامشخص";
                    return e;
                }
                if (analyze.getVerdict() == Verdict.GRAY) {
                    e.geoStatus = "skip_gray";
                    e.activityZoneStatus = "unknown";
                    return e;
                }
                NeshanClient.GeocodeResult destRes =
                        NeshanClient.INSTANCE.geocodeDetailed(applicationContext, destAddr);
                e.geoAttempted = destRes.attempted;
                e.geoOk = destRes.ok;
                e.geoHttp = destRes.httpCode;
                e.geoApiStatus = destRes.apiStatus;
                e.destLat = destRes.lat;
                e.destLng = destRes.lng;
                e.geoError = destRes.error;
                e.geoEndpoint = destRes.endpoint;
                if (!destRes.ok) {
                    e.geoStatus = "fail";
                    e.activityZoneStatus = "unknown";
                    e.zoneLabel = "محدوده نامشخص";
                    return e;
                }
                e.geoStatus = "ok";
                ActivityZoneEvaluator.Result zr = ActivityZoneEvaluator.INSTANCE.evaluate(
                        applicationContext, destRes.lat, destRes.lng);
                e.zoneLabel = zr.labelFa;
                e.activityZoneDistanceKm = zr.distanceKm;
                if (zr.status == ActivityZoneEvaluator.Status.IN) {
                    e.activityZoneStatus = "in";
                } else if (zr.status == ActivityZoneEvaluator.Status.OUT) {
                    e.activityZoneStatus = "out";
                } else {
                    e.activityZoneStatus = "unknown";
                }
                // دو‌مقصده: هر دو مقصد باید داخل محدوده باشند
                if (dest2Addr != null && dest2Addr.trim().length() > 0
                        && zr.status == ActivityZoneEvaluator.Status.IN) {
                    try {
                        NeshanClient.GeocodeResult d2 =
                                NeshanClient.INSTANCE.geocodeDetailed(applicationContext, dest2Addr);
                        if (d2.ok && d2.lat != null && d2.lng != null) {
                            ActivityZoneEvaluator.Result zr2 = ActivityZoneEvaluator.INSTANCE.evaluate(
                                    applicationContext, d2.lat, d2.lng);
                            if (zr2.status == ActivityZoneEvaluator.Status.OUT) {
                                zr = zr2;
                                e.zoneLabel = "خارج محدوده (مقصد ۲) · " + (zr2.labelFa != null ? zr2.labelFa : "");
                                e.activityZoneStatus = "out";
                                e.activityZoneDistanceKm = zr2.distanceKm;
                            } else if (zr2.status == ActivityZoneEvaluator.Status.UNKNOWN) {
                                e.activityZoneStatus = "unknown";
                                e.zoneLabel = "محدوده نامشخص (مقصد ۲)";
                            }
                        } else {
                            e.activityZoneStatus = "unknown";
                            e.zoneLabel = "محدوده نامشخص (مقصد ۲)";
                        }
                    } catch (Throwable ignored2) {}
                }
                e.analyze = ActivityZoneEvaluator.INSTANCE.applyToVerdict(
                        applicationContext, analyze, zr);
            } catch (Throwable ignored) {
                e.geoStatus = "fail";
                e.geoError = "exception";
                e.activityZoneStatus = "unknown";
                e.zoneLabel = "محدوده نامشخص";
            }
            return e;
        }

        // مسیر قدیمی: ShiftAnalyzer شهری
        try {
            if (LicenseStore.INSTANCE.isPremium(applicationContext)
                    && analyze.getVerdict() != Verdict.GRAY
                    && fare != null) {
                double totalMin = 0;
                if (pickupMin != null) totalMin += pickupMin;
                if (tripMin != null) totalMin += tripMin;
                if (totalMin <= 0 && analyze.getRawTimeMin() != null) {
                    totalMin = analyze.getRawTimeMin();
                }
                ShiftAnalyzer.Result shift = ShiftAnalyzer.INSTANCE.analyze(
                        applicationContext, originAddr, destAddr,
                        fare, totalMin, engineSettings);
                if (shift != null) {
                    e.geoAttempted = shift.geoAttempted;
                    e.geoOk = shift.geoOk;
                    e.geoHttp = shift.geoHttp;
                    e.geoApiStatus = shift.geoApiStatus;
                    e.destLat = shift.destLat;
                    e.destLng = shift.destLng;
                    e.geoError = shift.geoError;
                    e.geoEndpoint = shift.geoEndpoint;
                    if (shift.labelFa != null && !shift.labelFa.isEmpty()) {
                        e.zoneLabel = shift.labelFa;
                    }
                    if (shift.mode == ShiftAnalyzer.ZoneMode.UNKNOWN) {
                        if (!shift.geoAttempted && (destAddr == null || destAddr.isEmpty())) {
                            e.geoStatus = "fail_no_dest";
                        } else if (shift.geoOk) {
                            e.geoStatus = "ok";
                        } else {
                            e.geoStatus = "fail";
                        }
                    } else {
                        e.geoStatus = "ok";
                    }
                    if (shift.applied) {
                        double floor = TripEngine.computeFloor(applicationContext);
                        e.analyze = ShiftAnalyzer.INSTANCE.recolorWithShift(
                                analyze, shift.hShift, floor, engineSettings.getTargetHourly());
                    }
                } else {
                    e.geoStatus = "fail";
                }
            } else if (!LicenseStore.INSTANCE.isPremium(applicationContext)) {
                e.geoStatus = "skip_not_premium";
            }
        } catch (Throwable ignored) {
            e.geoStatus = "fail";
            e.geoError = "exception";
        }
        e.activityZoneStatus = "disabled";
        return e;
    }
}
