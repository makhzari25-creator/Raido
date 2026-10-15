package ir.tripanalyzer.app;

import android.content.Context;

import ir.tripanalyzer.app.core.GeoLayer;
import ir.tripanalyzer.app.core.ParseLayer;
import ir.tripanalyzer.app.core.TripLifecycle;
import kotlin.jvm.internal.Intrinsics;

public final class Pipeline {
    public static final Pipeline INSTANCE = new Pipeline();

    final class Bg implements Runnable {
        private final Context ctx;
        private final String source;
        private final String text;

        Bg(Context context, String str, String str2) {
            this.ctx = context;
            this.text = str;
            this.source = str2;
        }

        @Override
        public final void run() {
            try {
                Pipeline.INSTANCE.processSync(this.ctx, this.text, this.source);
            } catch (Throwable unused) {
            }
        }
    }

    public final void process(Context context, String rawText, String source) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(rawText, "rawText");
        Intrinsics.checkNotNullParameter(source, "source");
        Context applicationContext = context.getApplicationContext();
        if (Store.INSTANCE.isAnalysisEnabled(applicationContext)) {
            try {
                ReportAutoSave.INSTANCE.trySaveOnAccepted(applicationContext, rawText);
            } catch (Throwable unused) {
            }
            try {
                MidTripEventParser.INSTANCE.tryUpdateActiveTrip(applicationContext, rawText);
            } catch (Throwable ignored) {
            }
            try {
                TripConfirmHelper.INSTANCE.cancelPending(applicationContext);
            } catch (Throwable ignored) {
            }
            if (ParseLayer.INSTANCE.looksLikeTripRequest(rawText)) {
                new Thread(new Bg(applicationContext, rawText, source)).start();
            }
        }
    }

    private Pipeline() {
    }

    public final void processSync(Context context, String rawText, String source) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(rawText, "rawText");
        Intrinsics.checkNotNullParameter(source, "source");
        Context applicationContext = context.getApplicationContext();
        Store store = Store.INSTANCE;
        Intrinsics.checkNotNull(applicationContext);
        if (store.isAnalysisEnabled(applicationContext) && LicenseStore.INSTANCE.canUseAnalysis(applicationContext) && ParseLayer.INSTANCE.looksLikeTripRequest(rawText)) {
            // [ParseLayer] مدل سفر + پارس ادغام‌شده (فاز ۱)
            SnappLegParser.TripShape shape = ParseLayer.INSTANCE.analyzeShape(rawText);
            SnappLegParser.Legs legs = shape.legs;
            NotificationParser.Parsed parse = ParseLayer.INSTANCE.parseMerged(rawText);
            // دو‌مقصده بدون داده کافی → GRAY فوری (بدون نیاز به جزئیات کامل)
            if (shape.insufficient) {
                try {
                    EngineSettings esGray = Store.INSTANCE.settings(applicationContext);
                    TripResult gray = TripEngine.INSTANCE.analyze(
                            new RawTrip(parse.getFare(), null, null, null, null),
                            esGray,
                            applicationContext);
                    long nowGray = System.currentTimeMillis();
                    org.json.JSONObject rec = Store.INSTANCE.recordFrom(gray, esGray, parse, nowGray);
                    try {
                        rec.put("tripKind", "TWO_DEST");
                        rec.put("grayReason", shape.messageFa != null ? shape.messageFa
                                : "سفر دو مقصده اما اطلاعات کافی وجود ندارد");
                        rec.put("note", shape.messageFa != null ? shape.messageFa
                                : "سفر دو مقصده اما اطلاعات کافی وجود ندارد");
                        rec.put("source", source);
                    } catch (Throwable ignored) {}
                    Store.INSTANCE.saveLast(applicationContext, rec);
                    if (parse.getFare() != null) {
                        Notifier.INSTANCE.show(applicationContext, gray, nowGray, false, parse.getFare());
                    }
                } catch (Throwable ignored) {}
                return;
            }
            if (!ScreenHints.shouldAnalyzeNow(rawText, parse)) {
                return;
            }
            // با آمدن قیمت/امضای جدید، کارت قبلی فوری کنار برود تا درخواست بعدی سریع دیده شود
            try {
                Double curF = parse.getFare();
                if (curF != null) {
                    double cf = curF.doubleValue();
                    double prev = SnappScreenWatcherService.lastShownFare;
                    if (prev > 0 && Math.abs(prev - cf) >= 1.0) {
                        SnappScreenWatcherService.dismissCardNow();
                    }
                } else {
                    SnappScreenWatcherService.dismissCardNow();
                }
            } catch (Throwable ignored) {}
            String signatureOf = new DedupGuard(0L, 1, null).signatureOf(
                    parse.getFare(), parse.getPickupKm(), parse.getTripKm(),
                    parse.getPickupMin(), parse.getTripMin());
            long currentTimeMillis = System.currentTimeMillis();
            boolean isFresh = Store.INSTANCE.isFresh(applicationContext, signatureOf, currentTimeMillis);
            if (!isFresh && legs.hasAnyKm()) {
                try {
                    org.json.JSONObject last = Store.INSTANCE.last(applicationContext);
                    if (last != null) {
                        String ds = last.optString("distSrc", last.optString("distanceSource", ""));
                        boolean lastEst = ds.contains("EST") || "ESTIMATED".equalsIgnoreCase(ds) || "MIXED".equalsIgnoreCase(ds);
                        double lastFare = last.optDouble("fare", -1);
                        double curFare = parse.getFare() != null ? parse.getFare() : -2;
                        if (lastEst && Math.abs(lastFare - curFare) < 1.0) {
                            isFresh = true;
                            Store.INSTANCE.forceFresh(applicationContext, signatureOf, currentTimeMillis);
                        }
                    }
                } catch (Throwable ignored) {}
            }
            EngineSettings engineSettings = Store.INSTANCE.settings(applicationContext);

            TripResult analyze = TripEngine.INSTANCE.analyze(
                    new RawTrip(parse.getFare(), parse.getPickupKm(), parse.getTripKm(),
                            parse.getPickupMin(), parse.getTripMin()),
                    engineSettings,
                    applicationContext);

            // لایهٔ Premium: H_شیفت + برچسب محدوده + شواهد API نشان
            String zoneLabel = null;
            String geoStatus = null;
            String originAddr = null;
            String destAddr = null;
            boolean geoAttempted = false;
            boolean geoOk = false;
            int geoHttp = 0;
            String geoApiStatus = null;
            Double destLat = null;
            Double destLng = null;
            String geoError = null;
            String geoEndpoint = null;
            String activityZoneStatus = "disabled";
            Double activityZoneDistanceKm = null;
            boolean activityZoneMode = false;
            // [ParseLayer] آدرس + تشخیص حین سفر + مبدأ از activeTrip
            boolean duringTrip = ParseLayer.INSTANCE.isDuringTripRequest(rawText);
            String dest2Addr = null;
            try {
                String[] addrs = ParseLayer.INSTANCE.extractAddresses(rawText);
                originAddr = addrs[0];
                destAddr = addrs[1];
                if (addrs.length > 2) dest2Addr = addrs[2];
            } catch (Throwable ignored) {}
            originAddr = ParseLayer.INSTANCE.resolveOrigin(applicationContext, originAddr, duringTrip);

            // [GeoLayer] پرمیوم: نشان + محدوده + recolor — همان شرایط قبلی
            {
                GeoLayer.Enrichment en = GeoLayer.INSTANCE.enrich(
                        applicationContext, analyze, parse.getFare(),
                        parse.getPickupMin(), parse.getTripMin(),
                        originAddr, destAddr, dest2Addr, engineSettings);
                zoneLabel = en.zoneLabel;
                geoStatus = en.geoStatus;
                geoAttempted = en.geoAttempted;
                geoOk = en.geoOk;
                geoHttp = en.geoHttp;
                geoApiStatus = en.geoApiStatus;
                destLat = en.destLat;
                destLng = en.destLng;
                geoError = en.geoError;
                geoEndpoint = en.geoEndpoint;
                if (en.analyze != null) analyze = en.analyze;
                activityZoneStatus = en.activityZoneStatus != null ? en.activityZoneStatus : "disabled";
                activityZoneDistanceKm = en.activityZoneDistanceKm;
                activityZoneMode = en.activityZoneMode;
            }

            if (analyze.getVerdict() != Verdict.GRAY || ParseLayer.INSTANCE.isOfferScreen(rawText)
                    || Intrinsics.areEqual(source, "manual")) {
                if (analyze.getVerdict() != Verdict.GRAY || isFresh || Intrinsics.areEqual(source, "manual")) {
                    org.json.JSONObject rec = Store.INSTANCE.recordFrom(analyze, engineSettings, parse, currentTimeMillis);
                    try {
                        if (originAddr != null) rec.put("origin", originAddr);
                        if (destAddr != null) rec.put("dest", destAddr);
                        if (zoneLabel != null) rec.put("zoneLabel", zoneLabel);
                        if (geoStatus != null) rec.put("geoStatus", geoStatus);
                        rec.put("geoOk", geoOk || "ok".equals(geoStatus));
                        rec.put("geoAttempted", geoAttempted);
                        rec.put("geoHttp", geoHttp);
                        if (geoApiStatus != null) rec.put("geoApiStatus", geoApiStatus);
                        if (destLat != null) rec.put("destLat", destLat);
                        if (destLng != null) rec.put("destLng", destLng);
                        if (geoError != null) rec.put("geoError", geoError);
                        if (geoEndpoint != null) rec.put("geoEndpoint", geoEndpoint);
                        rec.put("activityZoneMode", activityZoneMode);
                        if (activityZoneStatus != null) rec.put("activityZoneStatus", activityZoneStatus);
                        if (activityZoneDistanceKm != null) rec.put("activityZoneDistanceKm", activityZoneDistanceKm);
                        rec.put("duringTrip", duringTrip);
                        if (originAddr != null) rec.put("origin", originAddr);
                        if (destAddr != null) rec.put("dest", destAddr);
                        try {
                            rec.put("tripKind", shape.kind.name());
                            rec.put("destCount", shape.destCount);
                            rec.put("usedTimeFallback", shape.usedTimeFallback);
                            if (dest2Addr != null) rec.put("dest2", dest2Addr);
                        } catch (Throwable ignored) {}
                    } catch (Throwable ignored) {}
                    Store.INSTANCE.saveLast(applicationContext, rec);
                    // [TripLifecycle] سفر عادی → activeTrip
                    TripLifecycle.INSTANCE.bindActiveIfNormal(
                            applicationContext, rec, duringTrip, isFresh, analyze.getVerdict());
                    try {
                        String distLabel = analyze.getDistanceSource() == DataSource.REAL ? "واقعی"
                                : (analyze.getDistanceSource() == DataSource.MIXED ? "ترکیبی" : "تخمینی");
                        Store.INSTANCE.setLastDistSource(applicationContext, distLabel);
                    } catch (Throwable ignored) {}
                    try {
                        if (isFresh && analyze.getVerdict() != Verdict.GRAY) {
                            RaidoDataStore.INSTANCE.appendAnalysis(applicationContext, rec);
                        }
                    } catch (Throwable ignored) {}
                    ReportAutoSave.INSTANCE.markPending(applicationContext);

                    try {
                        // پاپ‌آپ «آیا سفر رفتی؟» حذف شد — ثبت با دیدن «پایان سفر» یا دستی در جزئیات
                        if (isFresh) {
                            Store.INSTANCE.recordVerdict(applicationContext, analyze.getVerdict());
                        }
                    } catch (Throwable ignored) {
                    }

                    if (isFresh) {
                        Notifier.INSTANCE.show(applicationContext, analyze, currentTimeMillis, false, parse.getFare());
                        // برچسب محدوده را هم به overlay بفرست (از طریق آخرین rec)
                        try {
                            if (zoneLabel != null) {
                                // Notifier از last می‌خواند در صورت نیاز
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            }
        }
    }
}
