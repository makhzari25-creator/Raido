package ir.tripanalyzer.app;

import java.util.List;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: TripEngine.kt */
@Metadata(d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b/\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\b\u0086\b\u0018\u00002\u00020\u0001B\u00ad\u0001\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u0012\b\u0010\t\u001a\u0004\u0018\u00010\n\u0012\b\u0010\u000b\u001a\u0004\u0018\u00010\f\u0012\b\u0010\r\u001a\u0004\u0018\u00010\f\u0012\b\u0010\u000e\u001a\u0004\u0018\u00010\f\u0012\b\u0010\u000f\u001a\u0004\u0018\u00010\f\u0012\b\u0010\u0010\u001a\u0004\u0018\u00010\f\u0012\b\u0010\u0011\u001a\u0004\u0018\u00010\f\u0012\b\u0010\u0012\u001a\u0004\u0018\u00010\f\u0012\b\u0010\u0013\u001a\u0004\u0018\u00010\f\u0012\b\u0010\u0014\u001a\u0004\u0018\u00010\f\u0012\b\u0010\u0015\u001a\u0004\u0018\u00010\f\u0012\b\u0010\u0016\u001a\u0004\u0018\u00010\f\u0012\u0006\u0010\u0017\u001a\u00020\u0018\u0012\u0006\u0010\u0019\u001a\u00020\u0018¢\u0006\u0004\b\u001a\u0010\u001bJ\t\u00104\u001a\u00020\u0003HÆ\u0003J\t\u00105\u001a\u00020\u0005HÆ\u0003J\u000f\u00106\u001a\b\u0012\u0004\u0012\u00020\b0\u0007HÆ\u0003J\u000b\u00107\u001a\u0004\u0018\u00010\nHÆ\u0003J\u0010\u00108\u001a\u0004\u0018\u00010\fHÆ\u0003¢\u0006\u0002\u0010%J\u0010\u00109\u001a\u0004\u0018\u00010\fHÆ\u0003¢\u0006\u0002\u0010%J\u0010\u0010:\u001a\u0004\u0018\u00010\fHÆ\u0003¢\u0006\u0002\u0010%J\u0010\u0010;\u001a\u0004\u0018\u00010\fHÆ\u0003¢\u0006\u0002\u0010%J\u0010\u0010<\u001a\u0004\u0018\u00010\fHÆ\u0003¢\u0006\u0002\u0010%J\u0010\u0010=\u001a\u0004\u0018\u00010\fHÆ\u0003¢\u0006\u0002\u0010%J\u0010\u0010>\u001a\u0004\u0018\u00010\fHÆ\u0003¢\u0006\u0002\u0010%J\u0010\u0010?\u001a\u0004\u0018\u00010\fHÆ\u0003¢\u0006\u0002\u0010%J\u0010\u0010@\u001a\u0004\u0018\u00010\fHÆ\u0003¢\u0006\u0002\u0010%J\u0010\u0010A\u001a\u0004\u0018\u00010\fHÆ\u0003¢\u0006\u0002\u0010%J\u0010\u0010B\u001a\u0004\u0018\u00010\fHÆ\u0003¢\u0006\u0002\u0010%J\t\u0010C\u001a\u00020\u0018HÆ\u0003J\t\u0010D\u001a\u00020\u0018HÆ\u0003JÖ\u0001\u0010E\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u00072\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\f2\b\b\u0002\u0010\u0017\u001a\u00020\u00182\b\b\u0002\u0010\u0019\u001a\u00020\u0018HÆ\u0001¢\u0006\u0002\u0010FJ\u0013\u0010G\u001a\u00020H2\b\u0010I\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010J\u001a\u00020KHÖ\u0001J\t\u0010L\u001a\u00020MHÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007¢\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0013\u0010\t\u001a\u0004\u0018\u00010\n¢\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0015\u0010\u000b\u001a\u0004\u0018\u00010\f¢\u0006\n\n\u0002\u0010&\u001a\u0004\b$\u0010%R\u0015\u0010\r\u001a\u0004\u0018\u00010\f¢\u0006\n\n\u0002\u0010&\u001a\u0004\b'\u0010%R\u0015\u0010\u000e\u001a\u0004\u0018\u00010\f¢\u0006\n\n\u0002\u0010&\u001a\u0004\b(\u0010%R\u0015\u0010\u000f\u001a\u0004\u0018\u00010\f¢\u0006\n\n\u0002\u0010&\u001a\u0004\b)\u0010%R\u0015\u0010\u0010\u001a\u0004\u0018\u00010\f¢\u0006\n\n\u0002\u0010&\u001a\u0004\b*\u0010%R\u0015\u0010\u0011\u001a\u0004\u0018\u00010\f¢\u0006\n\n\u0002\u0010&\u001a\u0004\b+\u0010%R\u0015\u0010\u0012\u001a\u0004\u0018\u00010\f¢\u0006\n\n\u0002\u0010&\u001a\u0004\b,\u0010%R\u0015\u0010\u0013\u001a\u0004\u0018\u00010\f¢\u0006\n\n\u0002\u0010&\u001a\u0004\b-\u0010%R\u0015\u0010\u0014\u001a\u0004\u0018\u00010\f¢\u0006\n\n\u0002\u0010&\u001a\u0004\b.\u0010%R\u0015\u0010\u0015\u001a\u0004\u0018\u00010\f¢\u0006\n\n\u0002\u0010&\u001a\u0004\b/\u0010%R\u0015\u0010\u0016\u001a\u0004\u0018\u00010\f¢\u0006\n\n\u0002\u0010&\u001a\u0004\b0\u0010%R\u0011\u0010\u0017\u001a\u00020\u0018¢\u0006\b\n\u0000\u001a\u0004\b1\u00102R\u0011\u0010\u0019\u001a\u00020\u0018¢\u0006\b\n\u0000\u001a\u0004\b3\u00102"}, d2 = {"Lir/tripanalyzer/app/TripResult;", "", "verdict", "Lir/tripanalyzer/app/Verdict;", "confidence", "Lir/tripanalyzer/app/Confidence;", "yellowReasons", "", "Lir/tripanalyzer/app/YellowReason;", "rejectReason", "Lir/tripanalyzer/app/RejectReason;", "commission", "", "netAfterCommission", "fuelCost", "depCost", "totalCost", "netIncome", "totalDistanceKm", "rawTimeMin", "adjustedTimeMin", "hourlyIncome", "perKmIncome", "distanceSource", "Lir/tripanalyzer/app/DataSource;", "timeSource", "<init>", "(Lir/tripanalyzer/app/Verdict;Lir/tripanalyzer/app/Confidence;Ljava/util/List;Lir/tripanalyzer/app/RejectReason;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Lir/tripanalyzer/app/DataSource;Lir/tripanalyzer/app/DataSource;)V", "getVerdict", "()Lir/tripanalyzer/app/Verdict;", "getConfidence", "()Lir/tripanalyzer/app/Confidence;", "getYellowReasons", "()Ljava/util/List;", "getRejectReason", "()Lir/tripanalyzer/app/RejectReason;", "getCommission", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getNetAfterCommission", "getFuelCost", "getDepCost", "getTotalCost", "getNetIncome", "getTotalDistanceKm", "getRawTimeMin", "getAdjustedTimeMin", "getHourlyIncome", "getPerKmIncome", "getDistanceSource", "()Lir/tripanalyzer/app/DataSource;", "getTimeSource", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "copy", "(Lir/tripanalyzer/app/Verdict;Lir/tripanalyzer/app/Confidence;Ljava/util/List;Lir/tripanalyzer/app/RejectReason;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Lir/tripanalyzer/app/DataSource;Lir/tripanalyzer/app/DataSource;)Lir/tripanalyzer/app/TripResult;", "equals", "", "other", "hashCode", "", "toString", ""}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final /* data */ class TripResult {
    private final Double adjustedTimeMin;
    private final Double commission;
    private final Confidence confidence;
    private final Double depCost;
    private final DataSource distanceSource;
    private final Double fuelCost;
    private final Double hourlyIncome;
    private final Double netAfterCommission;
    private final Double netIncome;
    private final Double perKmIncome;
    private final Double rawTimeMin;
    private final RejectReason rejectReason;
    private final DataSource timeSource;
    private final Double totalCost;
    private final Double totalDistanceKm;
    private final Verdict verdict;
    private final List<YellowReason> yellowReasons;

    /* renamed from: component1, reason: from getter */
    public final Verdict getVerdict() {
        return this.verdict;
    }

    /* renamed from: component10, reason: from getter */
    public final Double getNetIncome() {
        return this.netIncome;
    }

    /* renamed from: component11, reason: from getter */
    public final Double getTotalDistanceKm() {
        return this.totalDistanceKm;
    }

    /* renamed from: component12, reason: from getter */
    public final Double getRawTimeMin() {
        return this.rawTimeMin;
    }

    /* renamed from: component13, reason: from getter */
    public final Double getAdjustedTimeMin() {
        return this.adjustedTimeMin;
    }

    /* renamed from: component14, reason: from getter */
    public final Double getHourlyIncome() {
        return this.hourlyIncome;
    }

    /* renamed from: component15, reason: from getter */
    public final Double getPerKmIncome() {
        return this.perKmIncome;
    }

    /* renamed from: component16, reason: from getter */
    public final DataSource getDistanceSource() {
        return this.distanceSource;
    }

    /* renamed from: component17, reason: from getter */
    public final DataSource getTimeSource() {
        return this.timeSource;
    }

    /* renamed from: component2, reason: from getter */
    public final Confidence getConfidence() {
        return this.confidence;
    }

    public final List<YellowReason> getYellowReasons() {
        return this.yellowReasons;
    }

    /* renamed from: component4, reason: from getter */
    public final RejectReason getRejectReason() {
        return this.rejectReason;
    }

    /* renamed from: component5, reason: from getter */
    public final Double getCommission() {
        return this.commission;
    }

    /* renamed from: component6, reason: from getter */
    public final Double getNetAfterCommission() {
        return this.netAfterCommission;
    }

    /* renamed from: component7, reason: from getter */
    public final Double getFuelCost() {
        return this.fuelCost;
    }

    /* renamed from: component8, reason: from getter */
    public final Double getDepCost() {
        return this.depCost;
    }

    /* renamed from: component9, reason: from getter */
    public final Double getTotalCost() {
        return this.totalCost;
    }

    public final TripResult copy(Verdict verdict, Confidence confidence, List<? extends YellowReason> yellowReasons, RejectReason rejectReason, Double commission, Double netAfterCommission, Double fuelCost, Double depCost, Double totalCost, Double netIncome, Double totalDistanceKm, Double rawTimeMin, Double adjustedTimeMin, Double hourlyIncome, Double perKmIncome, DataSource distanceSource, DataSource timeSource) {
        Intrinsics.checkNotNullParameter(verdict, "verdict");
        Intrinsics.checkNotNullParameter(confidence, "confidence");
        Intrinsics.checkNotNullParameter(yellowReasons, "yellowReasons");
        Intrinsics.checkNotNullParameter(distanceSource, "distanceSource");
        Intrinsics.checkNotNullParameter(timeSource, "timeSource");
        return new TripResult(verdict, confidence, yellowReasons, rejectReason, commission, netAfterCommission, fuelCost, depCost, totalCost, netIncome, totalDistanceKm, rawTimeMin, adjustedTimeMin, hourlyIncome, perKmIncome, distanceSource, timeSource);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TripResult)) {
            return false;
        }
        TripResult tripResult = (TripResult) other;
        return this.verdict == tripResult.verdict && this.confidence == tripResult.confidence && Intrinsics.areEqual(this.yellowReasons, tripResult.yellowReasons) && this.rejectReason == tripResult.rejectReason && Intrinsics.areEqual((Object) this.commission, (Object) tripResult.commission) && Intrinsics.areEqual((Object) this.netAfterCommission, (Object) tripResult.netAfterCommission) && Intrinsics.areEqual((Object) this.fuelCost, (Object) tripResult.fuelCost) && Intrinsics.areEqual((Object) this.depCost, (Object) tripResult.depCost) && Intrinsics.areEqual((Object) this.totalCost, (Object) tripResult.totalCost) && Intrinsics.areEqual((Object) this.netIncome, (Object) tripResult.netIncome) && Intrinsics.areEqual((Object) this.totalDistanceKm, (Object) tripResult.totalDistanceKm) && Intrinsics.areEqual((Object) this.rawTimeMin, (Object) tripResult.rawTimeMin) && Intrinsics.areEqual((Object) this.adjustedTimeMin, (Object) tripResult.adjustedTimeMin) && Intrinsics.areEqual((Object) this.hourlyIncome, (Object) tripResult.hourlyIncome) && Intrinsics.areEqual((Object) this.perKmIncome, (Object) tripResult.perKmIncome) && this.distanceSource == tripResult.distanceSource && this.timeSource == tripResult.timeSource;
    }

    public int hashCode() {
        int hashCode = ((((this.verdict.hashCode() * 31) + this.confidence.hashCode()) * 31) + this.yellowReasons.hashCode()) * 31;
        RejectReason rejectReason = this.rejectReason;
        int hashCode2 = (hashCode + (rejectReason == null ? 0 : rejectReason.hashCode())) * 31;
        Double d = this.commission;
        int hashCode3 = (hashCode2 + (d == null ? 0 : d.hashCode())) * 31;
        Double d2 = this.netAfterCommission;
        int hashCode4 = (hashCode3 + (d2 == null ? 0 : d2.hashCode())) * 31;
        Double d3 = this.fuelCost;
        int hashCode5 = (hashCode4 + (d3 == null ? 0 : d3.hashCode())) * 31;
        Double d4 = this.depCost;
        int hashCode6 = (hashCode5 + (d4 == null ? 0 : d4.hashCode())) * 31;
        Double d5 = this.totalCost;
        int hashCode7 = (hashCode6 + (d5 == null ? 0 : d5.hashCode())) * 31;
        Double d6 = this.netIncome;
        int hashCode8 = (hashCode7 + (d6 == null ? 0 : d6.hashCode())) * 31;
        Double d7 = this.totalDistanceKm;
        int hashCode9 = (hashCode8 + (d7 == null ? 0 : d7.hashCode())) * 31;
        Double d8 = this.rawTimeMin;
        int hashCode10 = (hashCode9 + (d8 == null ? 0 : d8.hashCode())) * 31;
        Double d9 = this.adjustedTimeMin;
        int hashCode11 = (hashCode10 + (d9 == null ? 0 : d9.hashCode())) * 31;
        Double d10 = this.hourlyIncome;
        int hashCode12 = (hashCode11 + (d10 == null ? 0 : d10.hashCode())) * 31;
        Double d11 = this.perKmIncome;
        return ((((hashCode12 + (d11 != null ? d11.hashCode() : 0)) * 31) + this.distanceSource.hashCode()) * 31) + this.timeSource.hashCode();
    }

    public String toString() {
        return "TripResult(verdict=" + this.verdict + ", confidence=" + this.confidence + ", yellowReasons=" + this.yellowReasons + ", rejectReason=" + this.rejectReason + ", commission=" + this.commission + ", netAfterCommission=" + this.netAfterCommission + ", fuelCost=" + this.fuelCost + ", depCost=" + this.depCost + ", totalCost=" + this.totalCost + ", netIncome=" + this.netIncome + ", totalDistanceKm=" + this.totalDistanceKm + ", rawTimeMin=" + this.rawTimeMin + ", adjustedTimeMin=" + this.adjustedTimeMin + ", hourlyIncome=" + this.hourlyIncome + ", perKmIncome=" + this.perKmIncome + ", distanceSource=" + this.distanceSource + ", timeSource=" + this.timeSource + ')';
    }

    /* JADX WARN: Multi-variable type inference failed */
    public TripResult(Verdict verdict, Confidence confidence, List<? extends YellowReason> yellowReasons, RejectReason rejectReason, Double d, Double d2, Double d3, Double d4, Double d5, Double d6, Double d7, Double d8, Double d9, Double d10, Double d11, DataSource distanceSource, DataSource timeSource) {
        Intrinsics.checkNotNullParameter(verdict, "verdict");
        Intrinsics.checkNotNullParameter(confidence, "confidence");
        Intrinsics.checkNotNullParameter(yellowReasons, "yellowReasons");
        Intrinsics.checkNotNullParameter(distanceSource, "distanceSource");
        Intrinsics.checkNotNullParameter(timeSource, "timeSource");
        this.verdict = verdict;
        this.confidence = confidence;
        this.yellowReasons = (List) yellowReasons;
        this.rejectReason = rejectReason;
        this.commission = d;
        this.netAfterCommission = d2;
        this.fuelCost = d3;
        this.depCost = d4;
        this.totalCost = d5;
        this.netIncome = d6;
        this.totalDistanceKm = d7;
        this.rawTimeMin = d8;
        this.adjustedTimeMin = d9;
        this.hourlyIncome = d10;
        this.perKmIncome = d11;
        this.distanceSource = distanceSource;
        this.timeSource = timeSource;
    }
}
