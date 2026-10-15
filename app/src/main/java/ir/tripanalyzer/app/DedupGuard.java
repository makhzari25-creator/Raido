package ir.tripanalyzer.app;

import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: DedupGuard.kt */
@Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0011\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005J\u0016\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u00072\u0006\u0010\f\u001a\u00020\u0003J=\u0010\r\u001a\u00020\u00072\b\u0010\u000e\u001a\u0004\u0018\u00010\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u000f2\b\u0010\u0011\u001a\u0004\u0018\u00010\u000f2\b\u0010\u0012\u001a\u0004\u0018\u00010\u000f2\b\u0010\u0013\u001a\u0004\u0018\u00010\u000f¢\u0006\u0002\u0010\u0014R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0003X\u0082\u000e¢\u0006\u0002\n\u0000"}, d2 = {"Lir/tripanalyzer/app/DedupGuard;", "", "windowMillis", "", "<init>", "(J)V", "lastSignature", "", "lastTimestamp", "isFresh", "", "signature", "nowMillis", "signatureOf", "fare", "", "pickupKm", "tripKm", "pickupMin", "tripMin", "(Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;)Ljava/lang/String;"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class DedupGuard {
    private String lastSignature;
    private long lastTimestamp;
    private final long windowMillis;

    public DedupGuard() {
        this(0L, 1, null);
    }

    public DedupGuard(long j) {
        this.windowMillis = j;
        this.lastTimestamp = Long.MIN_VALUE;
    }

    public /* synthetic */ DedupGuard(long j, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? 60000L : j);
    }

    public final boolean isFresh(String signature, long nowMillis) {
        Intrinsics.checkNotNullParameter(signature, "signature");
        boolean z = !Intrinsics.areEqual(this.lastSignature, signature) || nowMillis - this.lastTimestamp > this.windowMillis;
        if (z) {
            this.lastSignature = signature;
            this.lastTimestamp = nowMillis;
        }
        return z;
    }

    public final String signatureOf(Double fare, Double pickupKm, Double tripKm, Double pickupMin, Double tripMin) {
        StringBuilder sb = new StringBuilder();
        Object obj = fare;
        if (fare == null) {
            obj = "-";
        }
        sb.append(obj);
        sb.append('|');
        Object obj2 = pickupKm;
        if (pickupKm == null) {
            obj2 = "-";
        }
        sb.append(obj2);
        sb.append('|');
        Object obj3 = tripKm;
        if (tripKm == null) {
            obj3 = "-";
        }
        sb.append(obj3);
        sb.append('|');
        Object obj4 = pickupMin;
        if (pickupMin == null) {
            obj4 = "-";
        }
        sb.append(obj4);
        sb.append('|');
        Object obj5 = tripMin;
        if (tripMin == null) {
            obj5 = "-";
        }
        sb.append(obj5);
        return sb.toString();
    }
}
