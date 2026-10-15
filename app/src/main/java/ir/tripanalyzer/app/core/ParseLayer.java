package ir.tripanalyzer.app.core;

import android.content.Context;

import org.json.JSONObject;

import ir.tripanalyzer.app.AddressExtractor;
import ir.tripanalyzer.app.NotificationParser;
import ir.tripanalyzer.app.SnappLegParser;
import ir.tripanalyzer.app.Store;

/**
 * لایه پارس: متن خام اسنپ → ساختار عددی/آدرس + مدل سفر.
 */
public final class ParseLayer {
    public static final ParseLayer INSTANCE = new ParseLayer();

    private ParseLayer() {}

    public boolean looksLikeTripRequest(String rawText) {
        return NotificationParser.INSTANCE.looksLikeTripRequest(rawText);
    }

    public boolean isOfferScreen(String rawText) {
        return NotificationParser.INSTANCE.isOfferScreen(rawText);
    }

    public SnappLegParser.TripShape analyzeShape(String rawText) {
        return SnappLegParser.INSTANCE.analyze(rawText);
    }

    public NotificationParser.Parsed parseMerged(String rawText) {
        NotificationParser.Parsed baseParse = NotificationParser.INSTANCE.parse(rawText);
        SnappLegParser.TripShape shape = SnappLegParser.INSTANCE.analyze(rawText);
        return SnappLegParser.mergeShape(baseParse, shape);
    }

    public SnappLegParser.Legs extractLegs(String rawText) {
        return SnappLegParser.INSTANCE.extract(rawText);
    }

    public boolean isDuringTripRequest(String rawText) {
        if (rawText == null) return false;
        try {
            String n = rawText.replace('\u200c', ' ');
            return n.contains("از مقصد سفر فعلی")
                    || n.contains("از مقصد سفر")
                    || n.contains("مقصد سفر فعلی");
        } catch (Throwable t) {
            return false;
        }
    }

    /** [0]=origin [1]=dest1 [2]=dest2 (optional) */
    public String[] extractAddresses(String rawText) {
        try {
            return AddressExtractor.INSTANCE.extractMulti(rawText);
        } catch (Throwable t) {
            try {
                String[] a = AddressExtractor.INSTANCE.extract(rawText);
                return new String[]{a[0], a[1], null};
            } catch (Throwable t2) {
                return new String[]{null, null, null};
            }
        }
    }

    public String resolveOrigin(Context context, String originFromParse, boolean duringTrip) {
        if (!duringTrip) return originFromParse;
        try {
            JSONObject active = Store.INSTANCE.getActiveTrip(context);
            if (active != null) {
                String ad = active.optString("dest", "");
                if (ad != null && ad.length() > 0) return ad;
            }
        } catch (Throwable ignored) {}
        return originFromParse;
    }
}
