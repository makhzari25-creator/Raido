package ir.tripanalyzer.app.core;

import android.content.Context;

import org.json.JSONObject;

import ir.tripanalyzer.app.PaymentParser;
import ir.tripanalyzer.app.RaidoDataStore;
import ir.tripanalyzer.app.Store;
import ir.tripanalyzer.app.Verdict;

/**
 * چرخه عمر سفر برای ثبت گزارش:
 * - سفر عادی تازه → activeTrip
 * - پایان سفر → پرداخت ۳شکلی + فقط activeTrip
 */
public final class TripLifecycle {
    public static final TripLifecycle INSTANCE = new TripLifecycle();

    private TripLifecycle() {}

    public void bindActiveIfNormal(Context context, JSONObject rec, boolean duringTrip, boolean isFresh, Verdict verdict) {
        try {
            if (!duringTrip && isFresh && verdict != Verdict.GRAY) {
                Store.INSTANCE.saveActiveTrip(context, rec);
            }
        } catch (Throwable ignored) {}
    }

    public static final class AcceptGate {
        public long lastAutoAcceptTs;
        public long lastAutoAcceptAnalysisTs;

        public AcceptGate(long lastTs, long lastAnalysisTs) {
            this.lastAutoAcceptTs = lastTs;
            this.lastAutoAcceptAnalysisTs = lastAnalysisTs;
        }
    }

    public boolean tryAcceptOnTripEnd(Context context, String raw, AcceptGate gate) {
        if (raw == null || gate == null) return false;
        try {
            String n = raw.replace('\u200c', ' ');
            if (!n.contains("پایان سفر")) return false;
            long now = System.currentTimeMillis();
            if (now - gate.lastAutoAcceptTs < 8000L) return false;

            JSONObject trip = Store.INSTANCE.getActiveTrip(context);
            if (trip == null) {
                trip = Store.INSTANCE.last(context);
                if (trip != null && trip.optBoolean("duringTrip", false)) {
                    return false;
                }
            }
            if (trip == null) return false;
            if (trip.optBoolean("duringTrip", false)) return false;

            long ats = trip.optLong("ts", 0);
            if (ats <= 0) return false;
            if (ats == gate.lastAutoAcceptAnalysisTs) return false;
            if (trip.optBoolean("accepted", false)) {
                gate.lastAutoAcceptAnalysisTs = ats;
                Store.INSTANCE.clearActiveTrip(context);
                return true;
            }

            // فاز ۲: پارس ۳ شکل پرداخت و اعمال روی سفر
            try {
                PaymentParser.Result pay = PaymentParser.INSTANCE.parse(raw);
                PaymentParser.INSTANCE.applyToTrip(trip, pay);
                // اگر effective داریم و hourly قبلی بر اساس fare قدیمی بود، نسبت ساده
                if (pay.effectiveToman != null && pay.effectiveToman > 0) {
                    double oldFare = trip.optDouble("fareOriginal", trip.optDouble("fare", 0));
                    if (!trip.has("fareOriginal") && oldFare > 0) {
                        // fare ممکن است همین الان عوض شده؛ از قبل از apply نگه نداریم — skip ratio
                    }
                }
            } catch (Throwable ignored) {}

            gate.lastAutoAcceptTs = now;
            gate.lastAutoAcceptAnalysisTs = ats;
            try {
                trip.put("accepted", true);
                trip.put("acceptedAt", now);
            } catch (Throwable ignored) {}
            try { Store.INSTANCE.saveAccepted(context, trip); } catch (Throwable ignored) {}
            try { RaidoDataStore.INSTANCE.markAccepted(context, ats); } catch (Throwable ignored) {}
            try {
                JSONObject last = Store.INSTANCE.last(context);
                if (last != null && last.optLong("ts", -1) == ats) {
                    last.put("accepted", true);
                    last.put("acceptedAt", now);
                    if (trip.has("payForm")) last.put("payForm", trip.optString("payForm"));
                    if (trip.has("payEffectiveToman")) last.put("payEffectiveToman", trip.optDouble("payEffectiveToman"));
                    if (trip.has("fare")) last.put("fare", trip.optDouble("fare"));
                    Store.INSTANCE.saveLast(context, last);
                }
            } catch (Throwable ignored) {}
            Store.INSTANCE.clearActiveTrip(context);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
