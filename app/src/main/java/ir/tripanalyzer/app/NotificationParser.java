package ir.tripanalyzer.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.IntRange;
import kotlin.sequences.SequencesKt;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.StringsKt;

/* compiled from: NotificationParser.kt */
@Metadata(d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bÆ\u0002\u0018\u00002\u00020\u0001:\u0002)*B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0005J\u0010\u0010\f\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u000eH\u0002J\u0017\u0010\u000f\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0010\u001a\u00020\u0005H\u0002¢\u0006\u0002\u0010\u0011J\u0017\u0010\u0012\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0010\u001a\u00020\u0005H\u0002¢\u0006\u0002\u0010\u0011J=\u0010\u0013\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0014\u001a\u00020\u00052\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00050\u00162\u0006\u0010\u0017\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0018\u001a\u00020\u000eH\u0002¢\u0006\u0002\u0010\u0019J\u000e\u0010 \u001a\u00020\u000e2\u0006\u0010\u0014\u001a\u00020\u0005J\u000e\u0010!\u001a\u00020\u000e2\u0006\u0010\u0014\u001a\u00020\u0005J\u0016\u0010\"\u001a\b\u0012\u0004\u0012\u00020#0\u00162\u0006\u0010$\u001a\u00020\u0005H\u0002J\u001e\u0010%\u001a\u0010\u0012\u0006\u0012\u0004\u0018\u00010\u000b\u0012\u0004\u0012\u00020\u000e0&2\u0006\u0010$\u001a\u00020\u0005H\u0002J\u000e\u0010'\u001a\u00020(2\u0006\u0010\u0014\u001a\u00020\u0005R\u000e\u0010\u0007\u001a\u00020\u0005X\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0005X\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u001bX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u001bX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u001bX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u001f\u001a\u00020\u001bX\u0082\u0004¢\u0006\u0002\n\u0000"}, d2 = {"Lir/tripanalyzer/app/NotificationParser;", "", "<init>", "()V", "normalizeDigits", "", "s", "NUM", "OPT_SPACE", "SEP", "MIN_FARE", "", "num", "allowComma", "", "moneyToDouble", "raw", "(Ljava/lang/String;)Ljava/lang/Double;", "qtyToDouble", "findAfter", "text", "labels", "", "pattern", "distanceUnit", "(Ljava/lang/String;Ljava/util/List;Ljava/lang/String;ZZ)Ljava/lang/Double;", "PICKUP_MIN_SCREEN", "Lkotlin/text/Regex;", "ANY_MIN", "PICKUP_KM_SCREEN", "ANY_KM", "ANCHOR", "looksLikeTripRequest", "isOfferScreen", "fareCandidates", "Lir/tripanalyzer/app/NotificationParser$FareCand;", "norm", "pickFare", "Lkotlin/Pair;", "parse", "Lir/tripanalyzer/app/NotificationParser$Parsed;", "Parsed", "FareCand"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class NotificationParser {
    private static final double MIN_FARE = 1000.0d;
    public static final NotificationParser INSTANCE = new NotificationParser();
    private static final String NUM = "((?:\\d{1,3}(?:,\\d{3})+|\\d+)(?:\\.\\d+)?)";
    private static final String OPT_SPACE = "\\s*";
    private static final String SEP = "[:：]?\\s*";
    private static final Regex PICKUP_MIN_SCREEN = new Regex("(\\d+)\\s*دقیقه\\s*تا\\s*(?:رسیدن\\s*)?(مبد|مسافر|محل|سوار)");
    private static final Regex ANY_MIN = new Regex("(?:(\\d+)\\s*ساعت\\s*(?:و\\s*)?)?(\\d+)\\s*دقیقه");
    private static final Regex PICKUP_KM_SCREEN = new Regex("(\\d+(?:[.,]\\d+)?)\\s*(کیلومتر|متر)\\s*تا\\s*(?:رسیدن\\s*)?(مبد|مسافر|محل|سوار)");
    private static final Regex ANY_KM = new Regex("(\\d+(?:[.,]\\d+)?)\\s*(کیلومتر|متر)");
    private static final Regex ANCHOR = new Regex("تا\\s*(?:رسیدن\\s*)?(مبد|مسافر)");

    private NotificationParser() {
    }

    /* compiled from: NotificationParser.kt */
    @Metadata(d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0016\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\b\u0086\b\u0018\u00002\u00020\u0001BA\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\b\u001a\u00020\t¢\u0006\u0004\b\n\u0010\u000bJ\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\rJ\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\rJ\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\rJ\u0010\u0010\u0018\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\rJ\u0010\u0010\u0019\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\rJ\t\u0010\u001a\u001a\u00020\tHÆ\u0003JT\u0010\u001b\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\b\u001a\u00020\tHÆ\u0001¢\u0006\u0002\u0010\u001cJ\u0013\u0010\u001d\u001a\u00020\t2\b\u0010\u001e\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u001f\u001a\u00020 HÖ\u0001J\t\u0010!\u001a\u00020\"HÖ\u0001R\u0015\u0010\u0002\u001a\u0004\u0018\u00010\u0003¢\u0006\n\n\u0002\u0010\u000e\u001a\u0004\b\f\u0010\rR\u0015\u0010\u0004\u001a\u0004\u0018\u00010\u0003¢\u0006\n\n\u0002\u0010\u000e\u001a\u0004\b\u000f\u0010\rR\u0015\u0010\u0005\u001a\u0004\u0018\u00010\u0003¢\u0006\n\n\u0002\u0010\u000e\u001a\u0004\b\u0010\u0010\rR\u0015\u0010\u0006\u001a\u0004\u0018\u00010\u0003¢\u0006\n\n\u0002\u0010\u000e\u001a\u0004\b\u0011\u0010\rR\u0015\u0010\u0007\u001a\u0004\u0018\u00010\u0003¢\u0006\n\n\u0002\u0010\u000e\u001a\u0004\b\u0012\u0010\rR\u0011\u0010\b\u001a\u00020\t¢\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014"}, d2 = {"Lir/tripanalyzer/app/NotificationParser$Parsed;", "", "fare", "", "pickupKm", "tripKm", "pickupMin", "tripMin", "fareWasRial", "", "<init>", "(Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Z)V", "getFare", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getPickupKm", "getTripKm", "getPickupMin", "getTripMin", "getFareWasRial", "()Z", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "(Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Z)Lir/tripanalyzer/app/NotificationParser$Parsed;", "equals", "other", "hashCode", "", "toString", ""}, k = 1, mv = {2, 0, 0}, xi = 48)
    public static final /* data */ class Parsed {
        private final Double fare;
        private final boolean fareWasRial;
        private final Double pickupKm;
        private final Double pickupMin;
        private final Double tripKm;
        private final Double tripMin;

        public static /* synthetic */ Parsed copy$default(Parsed parsed, Double d, Double d2, Double d3, Double d4, Double d5, boolean z, int i, Object obj) {
            if ((i & 1) != 0) {
                d = parsed.fare;
            }
            if ((i & 2) != 0) {
                d2 = parsed.pickupKm;
            }
            Double d6 = d2;
            if ((i & 4) != 0) {
                d3 = parsed.tripKm;
            }
            Double d7 = d3;
            if ((i & 8) != 0) {
                d4 = parsed.pickupMin;
            }
            Double d8 = d4;
            if ((i & 16) != 0) {
                d5 = parsed.tripMin;
            }
            Double d9 = d5;
            if ((i & 32) != 0) {
                z = parsed.fareWasRial;
            }
            return parsed.copy(d, d6, d7, d8, d9, z);
        }

        /* renamed from: component1, reason: from getter */
        public final Double getFare() {
            return this.fare;
        }

        /* renamed from: component2, reason: from getter */
        public final Double getPickupKm() {
            return this.pickupKm;
        }

        /* renamed from: component3, reason: from getter */
        public final Double getTripKm() {
            return this.tripKm;
        }

        /* renamed from: component4, reason: from getter */
        public final Double getPickupMin() {
            return this.pickupMin;
        }

        /* renamed from: component5, reason: from getter */
        public final Double getTripMin() {
            return this.tripMin;
        }

        /* renamed from: component6, reason: from getter */
        public final boolean getFareWasRial() {
            return this.fareWasRial;
        }

        public final Parsed copy(Double fare, Double pickupKm, Double tripKm, Double pickupMin, Double tripMin, boolean fareWasRial) {
            return new Parsed(fare, pickupKm, tripKm, pickupMin, tripMin, fareWasRial);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Parsed)) {
                return false;
            }
            Parsed parsed = (Parsed) other;
            return Intrinsics.areEqual((Object) this.fare, (Object) parsed.fare) && Intrinsics.areEqual((Object) this.pickupKm, (Object) parsed.pickupKm) && Intrinsics.areEqual((Object) this.tripKm, (Object) parsed.tripKm) && Intrinsics.areEqual((Object) this.pickupMin, (Object) parsed.pickupMin) && Intrinsics.areEqual((Object) this.tripMin, (Object) parsed.tripMin) && this.fareWasRial == parsed.fareWasRial;
        }

        public int hashCode() {
            Double d = this.fare;
            int hashCode = (d == null ? 0 : d.hashCode()) * 31;
            Double d2 = this.pickupKm;
            int hashCode2 = (hashCode + (d2 == null ? 0 : d2.hashCode())) * 31;
            Double d3 = this.tripKm;
            int hashCode3 = (hashCode2 + (d3 == null ? 0 : d3.hashCode())) * 31;
            Double d4 = this.pickupMin;
            int hashCode4 = (hashCode3 + (d4 == null ? 0 : d4.hashCode())) * 31;
            Double d5 = this.tripMin;
            return ((hashCode4 + (d5 != null ? d5.hashCode() : 0)) * 31) + Boolean.hashCode(this.fareWasRial);
        }

        public String toString() {
            return "Parsed(fare=" + this.fare + ", pickupKm=" + this.pickupKm + ", tripKm=" + this.tripKm + ", pickupMin=" + this.pickupMin + ", tripMin=" + this.tripMin + ", fareWasRial=" + this.fareWasRial + ')';
        }

        public Parsed(Double d, Double d2, Double d3, Double d4, Double d5, boolean z) {
            this.fare = d;
            this.pickupKm = d2;
            this.tripKm = d3;
            this.pickupMin = d4;
            this.tripMin = d5;
            this.fareWasRial = z;
        }
    }

    public final String normalizeDigits(String s) {
        Intrinsics.checkNotNullParameter(s, "s");
        StringBuilder sb = new StringBuilder(s.length());
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char charAt = s.charAt(i);
            if (1776 <= charAt && charAt < 1786) {
                sb.append(charAt - 1776);
            } else if (1632 <= charAt && charAt < 1642) {
                sb.append(charAt - 1632);
            } else if (charAt == 1643) {
                sb.append('.');
            } else if (charAt == 1644) {
                sb.append(',');
            } else if (charAt == 1548) {
                sb.append(',');
            } else {
                sb.append(charAt);
            }
        }
        String sb2 = sb.toString();
        Intrinsics.checkNotNullExpressionValue(sb2, "toString(...)");
        return sb2;
    }

    private final String num(boolean allowComma) {
        return NUM;
    }

    private final Double moneyToDouble(String raw) {
        String replace$default = raw.replace(",","");
        if (replace$default != null) {
            return StringsKt.toDoubleOrNull(replace$default);
        }
        return null;
    }

    private final Double qtyToDouble(String raw) {
        String str = raw;
        boolean z = false;
        if ((str!=null && str.toString().indexOf(',')>=0)) {
            List split$default = java.util.Arrays.asList(str.toString().split(","));
            if (((String) CollectionsKt.last(split$default)).length() == 3 && split$default.size() > 1) {
                List dropLast = CollectionsKt.dropLast(split$default, 1);
                if (!(dropLast instanceof Collection) || !dropLast.isEmpty()) {
                    Iterator it = dropLast.iterator();
                    while (it.hasNext()) {
                        if (!new Regex("\\d{1,3}").matches((String) it.next())) {
                            break;
                        }
                    }
                }
                z = true;
                if (z) {
                    raw = raw.replace(",","");
                }
            }
            raw = raw.replace(",",".");
        }
        return StringsKt.toDoubleOrNull(raw);
    }

    private final Double findAfter(String text, List<String> labels, String pattern, boolean allowComma, boolean distanceUnit) {
        String normalizeDigits = normalizeDigits(text);
        String str = distanceUnit ? "(کیلومتر|km|متر|m)?" : "(دقیقه|min)?";
        for (String str2 : labels) {
            StringBuilder sb = new StringBuilder();
            sb.append(Regex.Companion.escape(normalizeDigits(str2)));
            String str3 = OPT_SPACE;
            sb.append(str3);
            sb.append(SEP);
            sb.append(pattern);
            sb.append(str3);
            sb.append(str);
            MatchResult find$default = (new Regex(sb.toString())).find(normalizeDigits, 0);
            if (find$default != null) {
                String str4 = find$default.getGroupValues().get(1);
                Double moneyToDouble = allowComma ? moneyToDouble(str4) : qtyToDouble(str4);
                if (moneyToDouble != null) {
                    String str5 = find$default.getGroupValues().get(2);
                    return (Intrinsics.areEqual(str5, "متر") || Intrinsics.areEqual(str5, "m")) ? Double.valueOf(moneyToDouble.doubleValue() / MIN_FARE) : moneyToDouble;
                }
            }
        }
        return null;
    }

    public final boolean looksLikeTripRequest(String text) {
        Intrinsics.checkNotNullParameter(text, "text");
        String normalizeDigits = normalizeDigits(text);
        if (normalizeDigits == null) return false;
        // کلیدواژه‌های درخواست عادی + سفر-در-سفر (از مقصد سفر فعلی)
        String[] keys = new String[]{
                "قبول سفر", "درخواست سفر", "سفر جدید", "مسافر", "دقیقه تا مبد"
        };
        for (String k : keys) {
            if (normalizeDigits.contains(k)) return true;
        }
        String str = normalizeDigits;
        boolean money = str.contains("تومان") || str.contains("ریال");
        boolean timeOrKm = str.contains("دقیقه") || str.contains("کیلومتر");
        return money && timeOrKm;
    }

    public final boolean isOfferScreen(String text) {
        Intrinsics.checkNotNullParameter(text, "text");
        String normalizeDigits = normalizeDigits(text);
        String str = normalizeDigits;
        if ((str!=null && str.toString().contains(String.valueOf("قبول سفر"))) || (str!=null && str.toString().contains(String.valueOf("دقیقه تا مبد")))) {
            return true;
        }
        Parsed parse = parse(normalizeDigits);
        if (parse.getFare() != null) {
            return (parse.getPickupMin() == null && parse.getTripMin() == null) ? false : true;
        }
        return false;
    }

    /* compiled from: NotificationParser.kt */
    @Metadata(d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\b\b\u0002\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007¢\u0006\u0004\b\b\u0010\tR\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0007¢\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f"}, d2 = {"Lir/tripanalyzer/app/NotificationParser$FareCand;", "", "value", "", "rial", "", "index", "", "<init>", "(DZI)V", "getValue", "()D", "getRial", "()Z", "getIndex", "()I"}, k = 1, mv = {2, 0, 0}, xi = 48)
    private static final class FareCand {
        private final int index;
        private final boolean rial;
        private final double value;

        public FareCand(double d, boolean z, int i) {
            this.value = d;
            this.rial = z;
            this.index = i;
        }

        public final int getIndex() {
            return this.index;
        }

        public final boolean getRial() {
            return this.rial;
        }

        public final double getValue() {
            return this.value;
        }
    }

    private final List<FareCand> fareCandidates(String norm) {
        try {
            norm = new Regex("[0-9][0-9,]*(?:\\.[0-9]+)?\\s*تومان\\s*بیشتر از[^\\n\\r]*").replace(norm, "");
        } catch (Exception unused) {
        }
        ArrayList arrayList = new ArrayList();
        String str = norm;
        for (MatchResult matchResult : SequencesKt.toList((new Regex(NUM + OPT_SPACE + "(تومان|ریال)")).findAll(str, 0))) {
            Double moneyToDouble = moneyToDouble(matchResult.getGroupValues().get(1));
            if (moneyToDouble != null) {
                double doubleValue = moneyToDouble.doubleValue();
                if (doubleValue >= MIN_FARE) {
                    arrayList.add(new FareCand(doubleValue, Intrinsics.areEqual(matchResult.getGroupValues().get(2), "ریال"), matchResult.getRange().getFirst()));
                }
            }
        }
        for (MatchResult matchResult2 : SequencesKt.toList((new Regex("(تومان|ریال)" + OPT_SPACE + NUM)).findAll(str, 0))) {
            Double moneyToDouble2 = moneyToDouble(matchResult2.getGroupValues().get(2));
            if (moneyToDouble2 != null) {
                double doubleValue2 = moneyToDouble2.doubleValue();
                if (doubleValue2 >= MIN_FARE) {
                    arrayList.add(new FareCand(doubleValue2, Intrinsics.areEqual(matchResult2.getGroupValues().get(1), "ریال"), matchResult2.getRange().getFirst()));
                }
            }
        }
        return arrayList;
    }

    private final Pair<Double, Boolean> pickFare(String norm) {
        FareCand fareCand;
        IntRange range;
        Double moneyToDouble;
        Iterator it = CollectionsKt.listOf(new String[]{"کرایه", "مبلغ سفر", "مبلغ", "قیمت سفر", "قیمت", "درآمد این سفر", "درآمد سفر"}).iterator();
        while (true) {
            Object obj = null;
            if (it.hasNext()) {
                String str = (String) it.next();
                StringBuilder sb = new StringBuilder();
                sb.append(Regex.Companion.escape(str));
                String str2 = OPT_SPACE;
                sb.append(str2);
                sb.append(SEP);
                sb.append(NUM);
                sb.append(str2);
                sb.append("(تومان|ریال)");
                MatchResult find$default = (new Regex(sb.toString())).find(norm, 0);
                if (find$default != null && (moneyToDouble = moneyToDouble(find$default.getGroupValues().get(1))) != null) {
                    return TuplesKt.to(moneyToDouble, Boolean.valueOf(Intrinsics.areEqual(find$default.getGroupValues().get(2), "ریال")));
                }
            } else {
                List<FareCand> fareCandidates = fareCandidates(norm);
                if (fareCandidates.isEmpty()) {
                    return TuplesKt.to(null, false);
                }
                MatchResult find$default2 = (ANCHOR).find(norm, 0);
                Integer valueOf = (find$default2 == null || (range = find$default2.getRange()) == null) ? null : Integer.valueOf(range.getFirst());
                if (valueOf != null && fareCandidates.size() > 1) {
                    List<FareCand> list = fareCandidates;
                    ArrayList arrayList = new ArrayList();
                    for (Object obj2 : list) {
                        if (((FareCand) obj2).getIndex() < valueOf.intValue()) {
                            arrayList.add(obj2);
                        }
                    }
                    ArrayList arrayList2 = arrayList;
                    ArrayList arrayList3 = new ArrayList();
                    for (Object obj3 : list) {
                        if (((FareCand) obj3).getIndex() > valueOf.intValue()) {
                            arrayList3.add(obj3);
                        }
                    }
                    ArrayList arrayList4 = arrayList3;
                    ArrayList arrayList5 = arrayList2;
                    if ((!arrayList5.isEmpty()) && (!arrayList4.isEmpty())) {
                        fareCand = (FareCand) CollectionsKt.last((List) arrayList2);
                        FareCand fareCand2 = (FareCand) CollectionsKt.first((List) arrayList4);
                        if (valueOf.intValue() - fareCand.getIndex() > fareCand2.getIndex() - valueOf.intValue()) {
                            fareCand = fareCand2;
                        }
                    } else {
                        fareCand = arrayList5.isEmpty() ^ true ? (FareCand) CollectionsKt.last((List) arrayList2) : (FareCand) CollectionsKt.first((List) arrayList4);
                    }
                } else {
                    Iterator it2 = fareCandidates.iterator();
                    if (it2.hasNext()) {
                        obj = it2.next();
                        if (it2.hasNext()) {
                            double value = ((FareCand) obj).getValue();
                            do {
                                Object next = it2.next();
                                double value2 = ((FareCand) next).getValue();
                                if (Double.compare(value, value2) < 0) {
                                    obj = next;
                                    value = value2;
                                }
                            } while (it2.hasNext());
                        }
                    }
                    Intrinsics.checkNotNull(obj);
                    fareCand = (FareCand) obj;
                }
                return TuplesKt.to(Double.valueOf(fareCand.getValue()), Boolean.valueOf(fareCand.getRial()));
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:17:0x00c3  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x017a  */
    /* JADX WARN: Removed duplicated region for block: B:58:0x0114  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Parsed parse(String text) {
        Double d = null;
        Double findAfter = null;
        Double d2 = null;
        MatchResult find$default = null;
        MatchResult matchResult = null;
        Double findAfter2 = null;
        Double findAfter3 = null;
        Double doubleOrNull = null;
        List groupValues = null;
        String str;
        Double doubleOrNull2;
        Intrinsics.checkNotNullParameter(text, "text");
        String normalizeDigits = normalizeDigits(text);
        Pair<Double, Boolean> pickFare = pickFare(normalizeDigits);
        Double component1 = pickFare.component1();
        boolean booleanValue = pickFare.component2().booleanValue();
        if (component1 != null && booleanValue) {
            component1 = Double.valueOf(component1.doubleValue() / 10.0d);
        }
        Double d3 = component1;
        Double findAfter4 = findAfter(normalizeDigits, CollectionsKt.listOf(new String[]{"فاصله تا مسافر", "فاصله تا مبدا", "مسافت تا مسافر", "مسافت تا مبدا", "مسیر تا مبدا", "مسیر تا مسافر"}), num(false), false, true);
        String str2 = normalizeDigits;
        Double d4 = null;
        final MatchResult find$default2 = (PICKUP_KM_SCREEN).find(str2, 0);
        if (findAfter4 == null) {
            if (find$default2 != null) {
                findAfter4 = INSTANCE.qtyToDouble(find$default2.getGroupValues().get(1));
                if (findAfter4 == null) {
                    findAfter4 = null;
                } else if (Intrinsics.areEqual(find$default2.getGroupValues().get(2), "متر")) {
                    findAfter4 = Double.valueOf(findAfter4.doubleValue() / MIN_FARE);
                }
            } else {
                d = null;
                findAfter = findAfter(normalizeDigits, CollectionsKt.listOf(new String[]{"مسافت سفر", "فاصله سفر", "مسافت مسیر", "طول مسیر"}), num(false), false, true);
                if (findAfter != null) {
                    MatchResult matchResult2 = (MatchResult) CollectionsKt.lastOrNull(SequencesKt.toList(SequencesKt.filter((ANY_KM).findAll(str2, 0), new Function1() { // from class: ir.tripanalyzer.app.NotificationParser$$ExternalSyntheticLambda0
                        @Override // kotlin.jvm.functions.Function1
                        public final Object invoke(Object obj) {
                            boolean parse$lambda$6;
                            parse$lambda$6 = NotificationParser.parse$lambda$6(null, (MatchResult) obj);
                            return Boolean.valueOf(parse$lambda$6);
                        }
                    })));
                    if (matchResult2 != null) {
                        Double qtyToDouble = INSTANCE.qtyToDouble(matchResult2.getGroupValues().get(1));
                        if (qtyToDouble == null) {
                            qtyToDouble = null;
                        } else if (Intrinsics.areEqual(matchResult2.getGroupValues().get(2), "متر")) {
                            qtyToDouble = Double.valueOf(qtyToDouble.doubleValue() / MIN_FARE);
                        }
                        d2 = qtyToDouble;
                    } else {
                        d2 = null;
                    }
                } else {
                    d2 = findAfter;
                }
                find$default = (PICKUP_MIN_SCREEN).find(str2, 0);
                if (find$default == null || (groupValues = find$default.getGroupValues()) == null || (str = (String) groupValues.get(1)) == null || (doubleOrNull2 = StringsKt.toDoubleOrNull(str)) == null) {
                    matchResult = find$default;
                    findAfter2 = findAfter(normalizeDigits, CollectionsKt.listOf(new String[]{"زمان تا مسافر", "زمان تا مبدا", "مدت تا مسافر", "مدت تا مبدا", "زمان رسیدن به مسافر"}), num(false), false, false);
                } else {
                    findAfter2 = doubleOrNull2;
                    matchResult = find$default;
                }
                findAfter3 = findAfter(normalizeDigits, CollectionsKt.listOf(new String[]{"زمان سفر", "مدت سفر", "زمان مسیر", "مدت زمان سفر"}), num(false), false, false);
                if (findAfter3 == null) {
                    MatchResult matchResult3 = (MatchResult) CollectionsKt.lastOrNull(SequencesKt.toList(SequencesKt.filter((ANY_MIN).findAll(str2, 0), new Function1() { // from class: ir.tripanalyzer.app.NotificationParser$$ExternalSyntheticLambda1
                        @Override // kotlin.jvm.functions.Function1
                        public final Object invoke(Object obj) {
                            boolean parse$lambda$8;
                            parse$lambda$8 = NotificationParser.parse$lambda$8(null, (MatchResult) obj);
                            return Boolean.valueOf(parse$lambda$8);
                        }
                    })));
                    if (matchResult3 != null) {
                        String str3 = matchResult3.getGroupValues().get(1);
                        double doubleValue = (str3 == null || (doubleOrNull = StringsKt.toDoubleOrNull(str3)) == null) ? 0.0d : doubleOrNull.doubleValue();
                        Double doubleOrNull3 = StringsKt.toDoubleOrNull(matchResult3.getGroupValues().get(2));
                        if (doubleOrNull3 != null && (doubleValue > 0.0d || doubleOrNull3.doubleValue() > 0.0d)) {
                            d4 = Double.valueOf((doubleValue * 60.0d) + doubleOrNull3.doubleValue());
                        }
                    }
                    findAfter3 = d4;
                }
                        // چندمقصدی: جمع همهٔ دقیقه‌های مقصد (بدون مبدأ)
        Double multiDest = sumDestinationMinutes(normalizeDigits);
        if (multiDest != null && multiDest.doubleValue() > 0) {
            // اگر از قبل tripMin برچسب‌دار کوچک‌تر بود، با جمع مقصدها جایگزین کن
            if (findAfter3 == null || multiDest.doubleValue() >= findAfter3.doubleValue()) {
                findAfter3 = multiDest;
            }
        }
        return new Parsed(d3, d, d2, findAfter2, findAfter3, booleanValue);
            }
        }
        // Labeled pickup-km was found → still fill trip-km / times with labels + regex fallbacks
        d = findAfter4;
        findAfter = findAfter(normalizeDigits, CollectionsKt.listOf(new String[]{"مسافت سفر", "فاصله سفر", "مسافت مسیر", "طول مسیر"}), num(false), false, true);
        if (findAfter != null) {
            d2 = findAfter;
        } else {
            // regex fallback for trip distance
            MatchResult tripKmMatch = (MatchResult) CollectionsKt.lastOrNull(SequencesKt.toList((ANY_KM).findAll(str2, 0)));
            if (tripKmMatch != null) {
                Double qty = INSTANCE.qtyToDouble(tripKmMatch.getGroupValues().get(1));
                if (qty != null && Intrinsics.areEqual(tripKmMatch.getGroupValues().get(2), "متر")) {
                    qty = Double.valueOf(qty.doubleValue() / MIN_FARE);
                }
                d2 = qty;
            }
        }
        find$default = (PICKUP_MIN_SCREEN).find(str2, 0);
        if (find$default != null) {
            List gv = find$default.getGroupValues();
            String minsStr = gv != null ? (String) gv.get(1) : null;
            Double mins = minsStr != null ? StringsKt.toDoubleOrNull(minsStr) : null;
            if (mins != null) {
                findAfter2 = mins;
            }
        }
        if (findAfter2 == null) {
            findAfter2 = findAfter(normalizeDigits, CollectionsKt.listOf(new String[]{"زمان تا مسافر", "زمان تا مبدا", "مدت تا مسافر", "مدت تا مبدا", "زمان رسیدن به مسافر"}), num(false), false, false);
        }
        findAfter3 = findAfter(normalizeDigits, CollectionsKt.listOf(new String[]{"زمان سفر", "مدت سفر", "زمان مسیر", "مدت زمان سفر"}), num(false), false, false);
        if (findAfter3 == null) {
            // regex fallback for trip duration
            MatchResult tripMinMatch = (MatchResult) CollectionsKt.lastOrNull(SequencesKt.toList((ANY_MIN).findAll(str2, 0)));
            if (tripMinMatch != null) {
                String s1 = tripMinMatch.getGroupValues().get(1);
                Double m1 = s1 != null ? StringsKt.toDoubleOrNull(s1) : null;
                Double m2 = StringsKt.toDoubleOrNull(tripMinMatch.getGroupValues().get(2));
                if (m1 != null && m2 != null && (m1.doubleValue() > 0 || m2.doubleValue() > 0)) {
                    findAfter3 = Double.valueOf(m1.doubleValue() * 60.0d + m2.doubleValue());
                } else if (m1 != null) {
                    findAfter3 = m1;
                }
            }
        }
                // چندمقصدی: جمع همهٔ دقیقه‌های مقصد (بدون مبدأ)
        Double multiDest = sumDestinationMinutes(normalizeDigits);
        if (multiDest != null && multiDest.doubleValue() > 0) {
            // اگر از قبل tripMin برچسب‌دار کوچک‌تر بود، با جمع مقصدها جایگزین کن
            if (findAfter3 == null || multiDest.doubleValue() >= findAfter3.doubleValue()) {
                findAfter3 = multiDest;
            }
        }
        return new Parsed(d3, d, d2, findAfter2, findAfter3, booleanValue);
    }


    
    /**
     * جمع دقیقه‌های مقصد (بدون «تا مبدأ»). اگر کل لیست دوبار در dump تکرار شده باشد، فقط یک‌بار جمع می‌شود.
     */
    private final Double sumDestinationMinutes(String text) {
        if (text == null) return null;
        // بدون escape مشکل‌ساز جاوا: [0-9] و فاصلهٔ معمولی
        java.util.regex.Pattern pat = java.util.regex.Pattern.compile(
                "([0-9]+)[ \t]*دقیقه(?![ \t]*تا[ \t]*(?:رسیدن[ \t]*)?(?:مبد[اأ]?|مسافر|محل|سوار))");
        java.util.regex.Matcher m = pat.matcher(text);
        java.util.ArrayList<Double> all = new java.util.ArrayList<>();
        while (m.find()) {
            try { all.add(Double.parseDouble(m.group(1))); } catch (Exception ignored) {}
        }
        if (all.isEmpty()) return null;
        if (all.size() >= 2 && all.size() % 2 == 0) {
            int half = all.size() / 2;
            boolean dup = true;
            for (int i = 0; i < half; i++) {
                if (Double.compare(all.get(i), all.get(i + half)) != 0) { dup = false; break; }
            }
            if (dup) {
                double s = 0;
                for (int i = 0; i < half; i++) s += all.get(i);
                return s > 0 ? Double.valueOf(s) : null;
            }
        }
        double s = 0;
        for (Double d : all) s += d;
        return s > 0 ? Double.valueOf(s) : null;
    }





    /* JADX INFO: Access modifiers changed from: private */
    public static final boolean parse$lambda$6(MatchResult matchResult, MatchResult m) {
        Intrinsics.checkNotNullParameter(m, "m");
        return matchResult == null || m.getRange().getFirst() != matchResult.getRange().getFirst();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final boolean parse$lambda$8(MatchResult matchResult, MatchResult m) {
        Intrinsics.checkNotNullParameter(m, "m");
        return matchResult == null || m.getRange().getFirst() != matchResult.getRange().getFirst();
    }
}
