package ir.tripanalyzer.app;

import kotlin.Metadata;

/* compiled from: TripEngine.kt */
@Metadata(d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0019\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\b\u0086\b\u0018\u00002\u00020\u0001B?\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\u0006\u0010\t\u001a\u00020\u0003¢\u0006\u0004\b\n\u0010\u000bJ\t\u0010\u0014\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0015\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0016\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0017\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0018\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0019\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001a\u001a\u00020\u0003HÆ\u0003JO\u0010\u001b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u0003HÆ\u0001J\u0013\u0010\u001c\u001a\u00020\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u001f\u001a\u00020 HÖ\u0001J\t\u0010!\u001a\u00020\"HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\rR\u0011\u0010\u0005\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\rR\u0011\u0010\u0007\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\rR\u0011\u0010\b\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\rR\u0011\u0010\t\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\r"}, d2 = {"Lir/tripanalyzer/app/EngineSettings;", "", "speedKmh", "", "fuelPer100", "fuelPrice", "depPerKm", "commissionPct", "bufferPct", "targetHourly", "<init>", "(DDDDDDD)V", "getSpeedKmh", "()D", "getFuelPer100", "getFuelPrice", "getDepPerKm", "getCommissionPct", "getBufferPct", "getTargetHourly", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "hashCode", "", "toString", ""}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final /* data */ class EngineSettings {
    private final double bufferPct;
    private final double commissionPct;
    private final double depPerKm;
    private final double fuelPer100;
    private final double fuelPrice;
    private final double speedKmh;
    private final double targetHourly;

    /* renamed from: component1, reason: from getter */
    public final double getSpeedKmh() {
        return this.speedKmh;
    }

    /* renamed from: component2, reason: from getter */
    public final double getFuelPer100() {
        return this.fuelPer100;
    }

    /* renamed from: component3, reason: from getter */
    public final double getFuelPrice() {
        return this.fuelPrice;
    }

    /* renamed from: component4, reason: from getter */
    public final double getDepPerKm() {
        return this.depPerKm;
    }

    /* renamed from: component5, reason: from getter */
    public final double getCommissionPct() {
        return this.commissionPct;
    }

    /* renamed from: component6, reason: from getter */
    public final double getBufferPct() {
        return this.bufferPct;
    }

    /* renamed from: component7, reason: from getter */
    public final double getTargetHourly() {
        return this.targetHourly;
    }

    public final EngineSettings copy(double speedKmh, double fuelPer100, double fuelPrice, double depPerKm, double commissionPct, double bufferPct, double targetHourly) {
        return new EngineSettings(speedKmh, fuelPer100, fuelPrice, depPerKm, commissionPct, bufferPct, targetHourly);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EngineSettings)) {
            return false;
        }
        EngineSettings engineSettings = (EngineSettings) other;
        return Double.compare(this.speedKmh, engineSettings.speedKmh) == 0 && Double.compare(this.fuelPer100, engineSettings.fuelPer100) == 0 && Double.compare(this.fuelPrice, engineSettings.fuelPrice) == 0 && Double.compare(this.depPerKm, engineSettings.depPerKm) == 0 && Double.compare(this.commissionPct, engineSettings.commissionPct) == 0 && Double.compare(this.bufferPct, engineSettings.bufferPct) == 0 && Double.compare(this.targetHourly, engineSettings.targetHourly) == 0;
    }

    public int hashCode() {
        return (((((((((((Double.hashCode(this.speedKmh) * 31) + Double.hashCode(this.fuelPer100)) * 31) + Double.hashCode(this.fuelPrice)) * 31) + Double.hashCode(this.depPerKm)) * 31) + Double.hashCode(this.commissionPct)) * 31) + Double.hashCode(this.bufferPct)) * 31) + Double.hashCode(this.targetHourly);
    }

    public String toString() {
        return "EngineSettings(speedKmh=" + this.speedKmh + ", fuelPer100=" + this.fuelPer100 + ", fuelPrice=" + this.fuelPrice + ", depPerKm=" + this.depPerKm + ", commissionPct=" + this.commissionPct + ", bufferPct=" + this.bufferPct + ", targetHourly=" + this.targetHourly + ')';
    }

    public EngineSettings(double d, double d2, double d3, double d4, double d5, double d6, double d7) {
        this.speedKmh = d;
        this.fuelPer100 = d2;
        this.fuelPrice = d3;
        this.depPerKm = d4;
        this.commissionPct = d5;
        this.bufferPct = d6;
        this.targetHourly = d7;
    }
}
