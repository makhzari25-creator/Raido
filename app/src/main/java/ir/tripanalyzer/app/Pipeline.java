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
            if (ScreenHints.isActiveTripScreen(rawText)) return;
            if (ParseLayer.INSTANCE.looksLikeTripRequest(rawText)) {
                new Thread(new Bg(applicationContext, rawText, source)).start();
            }
        }
    }

    private Pipeline() {
    }

    // هر تغییر متن صفحه (مثلاً شمارندهٔ ثانیه) یک ترد تحلیل جدید می‌سازد؛ برای یک درخواست فقط یکی باید کار کند.
    private static final java.util.Set<String> IN_FLIGHT = new java.util.HashSet<>();

    private static final java.util.Map<String, Object> FARE_LOCKS = new java.util.HashMap<>();

    private static Object fareLock(Double fare) {
        String k = String.valueOf(fare);
        synchronized (FARE_LOCKS) {
            Object o = FARE_LOCKS.get(k);
            if (o == null) { o = new Object(); if (FARE_LOCKS.size() > 64) FARE_LOCKS.clear(); FARE_LOCKS.put(k, o); }
            return o;
        }
    }

    private static boolean acquire(String k) {
        synchronized (IN_FLIGHT) { return IN_FLIGHT.add(k); }
    }

    private static void release(String k) {
        synchronized (IN_FLIGHT) { IN_FLIGHT.remove(k); }
    }

    public final void processSync(Context context, String rawText, String source) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(rawText, "rawText");
        Intrinsics.checkNotNullParameter(source, "source");
        Context applicationContext = context.getApplicationContext();
        Store store = Store.INSTANCE;
        Intrinsics.checkNotNull(applicationContext);
        if (ScreenHints.isActiveTripScreen(rawText)) return;
        if (store.isAnalysisEnabled(applicationContext) && LicenseStore.INSTANCE.canUseAnalysis(applicationContext) && ParseLayer.INSTANCE.looksLikeTripRequest(rawText)) {
            SnappLegParser.TripShape shape = ParseLayer.INSTANCE.analyzeShape(rawText);
            SnappLegParser.Legs legs = shape.legs;
            NotificationParser.Parsed parse = ParseLayer.INSTANCE.parseMerged(rawText);
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

            // کلید درخواست: امضای عددی + آدرس‌ها (برای تشخیص دقیق تکراری‌ها)
            String addrKey = "";
            try {
                String[] ad = ParseLayer.INSTANCE.extractAddresses(rawText);
                addrKey = RecentRequests.norm(ad[0]) + "|" + RecentRequests.norm(ad[1])
                        + "|" + (ad.length > 2 ? RecentRequests.norm(ad[2]) : "");
                if ("||".equals(addrKey)) addrKey = "";
            } catch (Throwable ignored) {}
            final String reqKey = signatureOf + "#" + addrKey;
            final String baseKey = String.valueOf(parse.getFare()) + "#" + addrKey;
            final String[] reqKeyHolder = new String[]{reqKey};

            // در حال تحلیل همین درخواست (قیمت/کیلومتر/دقیقه یکسان) → نادیده بگیر
            final String sigKey = "sig:" + signatureOf;
            if (!acquire(sigKey)) return;
            try {
            // تحلیل‌های هم‌قیمت پشت‌سرهم اجرا می‌شوند تا دومی نتیجهٔ اولی را ببیند (صفحهٔ قبل/بعد از «مشاهده جزئیات»)
            synchronized (fareLock(parse.getFare())) {
            // ۱) همین درخواست قبلاً تحلیل و ثبت شده → نتیجهٔ ذخیره‌شده، بدون ثبت و تحلیل دوباره
            try {
                org.json.JSONObject hit = RecentRequests.find(applicationContext, reqKey);
                if (hit != null && hit.optJSONObject("rec") != null) {
                    org.json.JSONObject stored = hit.optJSONObject("rec");
                    final String hk = reqKeyHolder[0];
                    try { RaidoDataStore.INSTANCE.touchSeen(applicationContext, stored.optLong("ts", 0L)); } catch (Throwable ignored) {}
                    RecentRequests.touch(applicationContext, hk);
                    if (RecentRequests.isCurrent(applicationContext, hk, 60000L)) {
                        RecentRequests.touchCurrent(applicationContext);
                        return; // همان صفحه هنوز باز است؛ فقط ادامه
                    }
                    kotlin.Pair<TripResult, Long> pr = Store.INSTANCE.parseResult(stored);
                    Store.INSTANCE.saveLast(applicationContext, stored);
                    if (!stored.optBoolean("duringTrip", false) && pr.getFirst().getVerdict() != Verdict.GRAY) {
                        Store.INSTANCE.saveActiveTrip(applicationContext, stored);
                    }
                    RecentRequests.markCurrent(applicationContext, hk);
                    Notifier.INSTANCE.show(applicationContext, pr.getFirst(), pr.getSecond().longValue(), false, parse.getFare());
                    return;
                }
            } catch (Throwable ignored) {}

            // ۲) همان درخواست با کیلومتر تخمینی ثبت شده و حالا کیلومتر واقعی رسید → به‌روزرسانی همان ثبت
            boolean isFresh = true;
            long upgradeOldTs = 0L;
            if (legs.hasAnyKm() && addrKey.length() > 0) {
                try {
                    org.json.JSONObject est = RecentRequests.findEstimatedByBase(applicationContext, baseKey);
                    if (est != null && est.optJSONObject("rec") != null) {
                        upgradeOldTs = est.optJSONObject("rec").optLong("ts", 0L);
                    }
                } catch (Throwable ignored) {}
            }
            // ادغام روی خودِ لیست تحلیل‌ها: همان کرایه در پنجرهٔ لغزان ۱۰ دقیقه → همان ردیف (کامل‌تر جایگزین می‌شود)
            if (parse.getFare() != null) {
                try {
                    String nd = "";
                    try { String[] ad3 = ParseLayer.INSTANCE.extractAddresses(rawText); nd = RecentRequests.norm(ad3[1]); } catch (Throwable ignored) {}
                    RaidoDataStore.MergeHit mh = RaidoDataStore.INSTANCE.findMergeTarget(applicationContext,
                            parse.getFare(), nd, parse.getPickupKm(), parse.getTripKm(), 10L * 60L * 1000L);
                    if (mh != null) {
                        if (mh.accepted) return; // سفر رفته؛ نه ردیف جدید نه تغییر
                        if (!legs.hasAnyKm() && mh.hasKm) {
                            RaidoDataStore.INSTANCE.touchSeen(applicationContext, mh.ts);
                            return; // ناقص‌تر از ردیف قبلی
                        }
                        upgradeOldTs = mh.ts;
                    }
                } catch (Throwable ignored) {}
            }
            final boolean isUpgrade = upgradeOldTs > 0L;
            EngineSettings engineSettings = Store.INSTANCE.settings(applicationContext);

            TripResult analyze = TripEngine.INSTANCE.analyze(
                    new RawTrip(parse.getFare(), parse.getPickupKm(), parse.getTripKm(),
                            parse.getPickupMin(), parse.getTripMin()),
                    engineSettings,
                    applicationContext);

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
            boolean duringTrip = ParseLayer.INSTANCE.isDuringTripRequest(rawText);
            String dest2Addr = null;
            try {
                String[] addrs = ParseLayer.INSTANCE.extractAddresses(rawText);
                originAddr = addrs[0];
                destAddr = addrs[1];
                if (addrs.length > 2) dest2Addr = addrs[2];
            } catch (Throwable ignored) {}
            originAddr = ParseLayer.INSTANCE.resolveOrigin(applicationContext, originAddr, duringTrip);
            // فقط سفر دو‌مقصده: dest2 برای محدوده معنا دارد (فیکس OUT جعلی ۷۸۰+ کم)
            if (shape.kind != SnappLegParser.TripKind.TWO_DEST) {
                dest2Addr = null;
            }

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
                    if (isUpgrade) { try { rec.put("ts", upgradeOldTs); } catch (Throwable ignored) {} }
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
                    TripLifecycle.INSTANCE.bindActiveIfNormal(
                            applicationContext, rec, duringTrip, isFresh, analyze.getVerdict());
                    try {
                        String distLabel = analyze.getDistanceSource() == DataSource.REAL ? "واقعی"
                                : (analyze.getDistanceSource() == DataSource.MIXED ? "ترکیبی" : "تخمینی");
                        Store.INSTANCE.setLastDistSource(applicationContext, distLabel);
                    } catch (Throwable ignored) {}
                    try {
                        if (isFresh && analyze.getVerdict() != Verdict.GRAY) {
                            if (isUpgrade) RaidoDataStore.INSTANCE.upsertAnalysis(applicationContext, rec, upgradeOldTs);
                            else RaidoDataStore.INSTANCE.appendAnalysis(applicationContext, rec);
                        }
                    } catch (Throwable ignored) {}
                    try {
                        boolean estimated = analyze.getDistanceSource() != DataSource.REAL;
                        RecentRequests.put(applicationContext, reqKey, baseKey, rec, estimated, signatureOf);
                        RecentRequests.markCurrent(applicationContext, reqKey);
                    } catch (Throwable ignored) {}
                    ReportAutoSave.INSTANCE.markPending(applicationContext);
                    // همگام‌سازی کم‌هزینه و محدودشده (فقط وقتی موعدش رسیده باشد؛ روی ترد جدا)
                    try { CloudSync.syncIfDue(applicationContext); } catch (Throwable ignored) {}

                    try {
                        if (isFresh && !isUpgrade) {
                            Store.INSTANCE.recordVerdict(applicationContext, analyze.getVerdict());
                        }
                    } catch (Throwable ignored) {
                    }

                    if (isFresh) {
                        Notifier.INSTANCE.show(applicationContext, analyze, currentTimeMillis, false, parse.getFare());
                    }
                }
            }
            }
            } finally {
                release(sigKey);
            }
        }
    }
}
