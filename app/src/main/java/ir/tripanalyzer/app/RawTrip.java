package ir.tripanalyzer.app;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: TripEngine.kt */
@Metadata(d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0015\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\b\u0086\b\u0018\u00002\u00020\u0001B9\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\u0003¢\u0006\u0004\b\b\u0010\tJ\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u000bJ\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u000bJ\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u000bJ\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u000bJ\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u000bJJ\u0010\u0016\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0003HÆ\u0001¢\u0006\u0002\u0010\u0017J\u0013\u0010\u0018\u001a\u00020\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u001b\u001a\u00020\u001cHÖ\u0001J\t\u0010\u001d\u001a\u00020\u001eHÖ\u0001R\u0015\u0010\u0002\u001a\u0004\u0018\u00010\u0003¢\u0006\n\n\u0002\u0010\f\u001a\u0004\b\n\u0010\u000bR\u0015\u0010\u0004\u001a\u0004\u0018\u00010\u0003¢\u0006\n\n\u0002\u0010\f\u001a\u0004\b\r\u0010\u000bR\u0015\u0010\u0005\u001a\u0004\u0018\u00010\u0003¢\u0006\n\n\u0002\u0010\f\u001a\u0004\b\u000e\u0010\u000bR\u0015\u0010\u0006\u001a\u0004\u0018\u00010\u0003¢\u0006\n\n\u0002\u0010\f\u001a\u0004\b\u000f\u0010\u000bR\u0015\u0010\u0007\u001a\u0004\u0018\u00010\u0003¢\u0006\n\n\u0002\u0010\f\u001a\u0004\b\u0010\u0010\u000b"}, d2 = {"Lir/tripanalyzer/app/RawTrip;", "", "fare", "", "pickupKm", "tripKm", "pickupMin", "tripMin", "<init>", "(Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;)V", "getFare", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getPickupKm", "getTripKm", "getPickupMin", "getTripMin", "component1", "component2", "component3", "component4", "component5", "copy", "(Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;)Lir/tripanalyzer/app/RawTrip;", "equals", "", "other", "hashCode", "", "toString", ""}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final /* data */ class RawTrip {
    private final Double fare;
    private final Double pickupKm;
    private final Double pickupMin;
    private final Double tripKm;
    private final Double tripMin;

    public static /* synthetic */ RawTrip copy$default(RawTrip rawTrip, Double d, Double d2, Double d3, Double d4, Double d5, int i, Object obj) {
        if ((i & 1) != 0) {
            d = rawTrip.fare;
        }
        if ((i & 2) != 0) {
            d2 = rawTrip.pickupKm;
        }
        Double d6 = d2;
        if ((i & 4) != 0) {
            d3 = rawTrip.tripKm;
        }
        Double d7 = d3;
        if ((i & 8) != 0) {
            d4 = rawTrip.pickupMin;
        }
        Double d8 = d4;
        if ((i & 16) != 0) {
            d5 = rawTrip.tripMin;
        }
        return rawTrip.copy(d, d6, d7, d8, d5);
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

    public final RawTrip copy(Double fare, Double pickupKm, Double tripKm, Double pickupMin, Double tripMin) {
        return new RawTrip(fare, pickupKm, tripKm, pickupMin, tripMin);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RawTrip)) {
            return false;
        }
        RawTrip rawTrip = (RawTrip) other;
        return Intrinsics.areEqual((Object) this.fare, (Object) rawTrip.fare) && Intrinsics.areEqual((Object) this.pickupKm, (Object) rawTrip.pickupKm) && Intrinsics.areEqual((Object) this.tripKm, (Object) rawTrip.tripKm) && Intrinsics.areEqual((Object) this.pickupMin, (Object) rawTrip.pickupMin) && Intrinsics.areEqual((Object) this.tripMin, (Object) rawTrip.tripMin);
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
        return hashCode4 + (d5 != null ? d5.hashCode() : 0);
    }

    public String toString() {
        return "RawTrip(fare=" + this.fare + ", pickupKm=" + this.pickupKm + ", tripKm=" + this.tripKm + ", pickupMin=" + this.pickupMin + ", tripMin=" + this.tripMin + ')';
    }

    public RawTrip(Double d, Double d2, Double d3, Double d4, Double d5) {
        this.fare = d;
        this.pickupKm = d2;
        this.tripKm = d3;
        this.pickupMin = d4;
        this.tripMin = d5;
    }
}
