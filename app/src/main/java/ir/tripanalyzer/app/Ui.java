package ir.tripanalyzer.app;

import kotlin.Metadata;
import kotlin.NoWhenBranchMatchedException;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Ui.kt */
@Metadata(d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0019\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0007J\u000e\u0010\t\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\t\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\fJ\u000e\u0010\r\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\u000e\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\u000f\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\u0010\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\u000bJ\u0010\u0010\u0011\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\u000bH\u0002J\u000e\u0010\u0012\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\u0013J\u000e\u0010\u0014\u001a\u00020\u00152\u0006\u0010\n\u001a\u00020\u0013J\u000e\u0010\u0016\u001a\u00020\u00072\u0006\u0010\u0017\u001a\u00020\u0018J\u000e\u0010\u0019\u001a\u00020\u00072\u0006\u0010\u001a\u001a\u00020\u001bJ\u001d\u0010\u001c\u001a\u00020\u00072\u0006\u0010\u001a\u001a\u00020\u001d2\b\u0010\u000e\u001a\u0004\u0018\u00010\u000b¢\u0006\u0002\u0010\u001eJ\u000e\u0010\u001f\u001a\u00020\u00072\u0006\u0010 \u001a\u00020!J\u0016\u0010\"\u001a\u00020\u00072\u0006\u0010#\u001a\u00020\f2\u0006\u0010$\u001a\u00020\fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000"}, d2 = {"Lir/tripanalyzer/app/Ui;", "", "<init>", "()V", "FA_DIGITS", "", "fa", "", "s", "money", "v", "", "", "dec1", "km", "min", "percent", "dec1Raw", "verdictText", "Lir/tripanalyzer/app/Verdict;", "verdictColor", "", "confText", "c", "Lir/tripanalyzer/app/Confidence;", "yellowReasonText", "r", "Lir/tripanalyzer/app/YellowReason;", "rejectReasonText", "Lir/tripanalyzer/app/RejectReason;", "(Lir/tripanalyzer/app/RejectReason;Ljava/lang/Double;)Ljava/lang/String;", "srcText", "ds", "Lir/tripanalyzer/app/DataSource;", "ago", "ts", "now"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class Ui {
    public static final Ui INSTANCE = new Ui();
    private static final char[] FA_DIGITS = {1776, 1777, 1778, 1779, 1780, 1781, 1782, 1783, 1784, 1785};

    /* compiled from: Ui.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = 48)
    public /* synthetic */ class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;
        public static final /* synthetic */ int[] $EnumSwitchMapping$1;
        public static final /* synthetic */ int[] $EnumSwitchMapping$2;
        public static final /* synthetic */ int[] $EnumSwitchMapping$3;
        public static final /* synthetic */ int[] $EnumSwitchMapping$4;

        static {
            int[] iArr = new int[Verdict.values().length];
            try {
                iArr[Verdict.GREEN.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                iArr[Verdict.YELLOW.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                iArr[Verdict.RED.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                iArr[Verdict.GRAY.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            $EnumSwitchMapping$0 = iArr;
            int[] iArr2 = new int[Confidence.values().length];
            try {
                iArr2[Confidence.HIGH.ordinal()] = 1;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                iArr2[Confidence.MEDIUM.ordinal()] = 2;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                iArr2[Confidence.LOW.ordinal()] = 3;
            } catch (NoSuchFieldError unused7) {
            }
            $EnumSwitchMapping$1 = iArr2;
            int[] iArr3 = new int[YellowReason.values().length];
            try {
                iArr3[YellowReason.NEAR_TARGET.ordinal()] = 1;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                iArr3[YellowReason.DIST_ESTIMATED.ordinal()] = 2;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                iArr3[YellowReason.TIME_ESTIMATED.ordinal()] = 3;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                iArr3[YellowReason.TRAFFIC_BUFFER.ordinal()] = 4;
            } catch (NoSuchFieldError unused11) {
            }
            $EnumSwitchMapping$2 = iArr3;
            int[] iArr4 = new int[RejectReason.values().length];
            try {
                iArr4[RejectReason.LONG_PICKUP.ordinal()] = 1;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                iArr4[RejectReason.LONG_TRIP.ordinal()] = 2;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                iArr4[RejectReason.LOW_NET.ordinal()] = 3;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                iArr4[RejectReason.LOW_FARE_PER_KM.ordinal()] = 4;
            } catch (NoSuchFieldError unused15) {
            }
            $EnumSwitchMapping$3 = iArr4;
            int[] iArr5 = new int[DataSource.values().length];
            try {
                iArr5[DataSource.REAL.ordinal()] = 1;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                iArr5[DataSource.MIXED.ordinal()] = 2;
            } catch (NoSuchFieldError unused17) {
            }
            try {
                iArr5[DataSource.ESTIMATED.ordinal()] = 3;
            } catch (NoSuchFieldError unused18) {
            }
            $EnumSwitchMapping$4 = iArr5;
        }
    }

    private Ui() {
    }

    public final String fa(String s) {
        Intrinsics.checkNotNullParameter(s, "s");
        StringBuilder sb = new StringBuilder(s.length());
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char charAt = s.charAt(i);
            if ('0' <= charAt && charAt < ':') {
                charAt = FA_DIGITS[charAt - '0'];
            }
            sb.append(charAt);
        }
        String sb2 = sb.toString();
        Intrinsics.checkNotNullExpressionValue(sb2, "toString(...)");
        return sb2;
    }

    public final String money(double v) {
        return money(Math.round(v));
    }

    public final String money(long v) {
        boolean z = v < 0;
        String l = Long.toString(Math.abs(v));
        StringBuilder sb = new StringBuilder();
        Intrinsics.checkNotNull(l);
        int length = l.length();
        for (int i = 0; i < length; i++) {
            sb.append(l.charAt(i));
            int length2 = (l.length() - i) - 1;
            if (length2 > 0 && length2 % 3 == 0) {
                sb.append((char) 1644);
            }
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append(z ? "−" : "");
        String sb3 = sb.toString();
        Intrinsics.checkNotNullExpressionValue(sb3, "toString(...)");
        sb2.append(fa(sb3));
        return sb2.toString();
    }

    public final String dec1(double v) {
        double round = Math.round(v * 10.0d) / 10.0d;
        long j = (long) round;
        double abs = Math.abs(round - j);
        if (abs < 0.05d) {
            return money(j);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(j);
        sb.append((char) 1643);
        sb.append((int) (abs * 10));
        return fa(sb.toString());
    }

    public final String km(double v) {
        return dec1(v);
    }

    public final String min(double v) {
        return dec1(v);
    }

    public final String percent(double v) {
        return fa(dec1Raw(v)) + (char) 1642;
    }

    private final String dec1Raw(double v) {
        double round = Math.round(v * 10.0d) / 10.0d;
        long j = (long) round;
        double abs = Math.abs(round - j);
        if (abs < 0.05d) {
            return String.valueOf(j);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(j);
        sb.append((char) 1643);
        sb.append((int) (abs * 10));
        return sb.toString();
    }

    public final String verdictText(Verdict v) {
        int i;
        Intrinsics.checkNotNullParameter(v, "v");
        Res res = Res.INSTANCE;
        int i2 = WhenMappings.$EnumSwitchMapping$0[v.ordinal()];
        if (i2 == 1) {
            i = R.string.verdict_green;
        } else if (i2 == 2) {
            i = R.string.verdict_yellow;
        } else if (i2 == 3) {
            i = R.string.verdict_red;
        } else {
            if (i2 != 4) {
                throw new NoWhenBranchMatchedException();
            }
            i = R.string.verdict_gray;
        }
        return res.str(i, new Object[0]);
    }

    public final int verdictColor(Verdict v) {
        Intrinsics.checkNotNullParameter(v, "v");
        int i = WhenMappings.$EnumSwitchMapping$0[v.ordinal()];
        if (i == 1) {
            return UiKit.VERDICT_GREEN;
        }
        if (i == 2) {
            return UiKit.VERDICT_YELLOW;
        }
        if (i == 3) {
            return UiKit.VERDICT_RED;
        }
        if (i == 4) {
            return UiKit.VERDICT_GRAY;
        }
        throw new NoWhenBranchMatchedException();
    }

    public final String confText(Confidence c) {
        int i;
        Intrinsics.checkNotNullParameter(c, "c");
        Res res = Res.INSTANCE;
        int i2 = WhenMappings.$EnumSwitchMapping$1[c.ordinal()];
        if (i2 == 1) {
            i = R.string.confidence_short_high;
        } else if (i2 == 2) {
            i = R.string.confidence_short_medium;
        } else {
            if (i2 != 3) {
                throw new NoWhenBranchMatchedException();
            }
            i = R.string.confidence_short_low;
        }
        return res.str(i, new Object[0]);
    }

    public final String yellowReasonText(YellowReason r) {
        int i;
        Intrinsics.checkNotNullParameter(r, "r");
        Res res = Res.INSTANCE;
        int i2 = WhenMappings.$EnumSwitchMapping$2[r.ordinal()];
        if (i2 == 1) {
            i = R.string.yellow_reason_near_target;
        } else if (i2 == 2) {
            i = R.string.yellow_reason_dist_estimated;
        } else if (i2 == 3) {
            i = R.string.yellow_reason_time_estimated;
        } else {
            if (i2 != 4) {
                throw new NoWhenBranchMatchedException();
            }
            i = R.string.yellow_reason_buffer;
        }
        return res.str(i, new Object[0]);
    }

    /* JADX WARN: Code restructure failed: missing block: B:21:0x0056, code lost:
    
        if (r6 == null) goto L19;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final String rejectReasonText(RejectReason r, Double km) {
        String str;
        Intrinsics.checkNotNullParameter(r, "r");
        int i = WhenMappings.$EnumSwitchMapping$3[r.ordinal()];
        if (i != 1) {
            if (i == 2) {
                return Res.INSTANCE.str(R.string.reason_long_trip, new Object[0]);
            }
            if (i == 3) {
                return Res.INSTANCE.str(R.string.reason_low_net, new Object[0]);
            }
            if (i != 4) {
                throw new NoWhenBranchMatchedException();
            }
            return Res.INSTANCE.str(R.string.reason_low_fare, new Object[0]);
        }
        Res res = Res.INSTANCE;
        Object[] objArr = new Object[1];
        if (km != null) {
            str = INSTANCE.dec1(km.doubleValue());
        } else {
            str = "0";
        }
        objArr[0] = str;
        return res.str(R.string.reason_long_pickup, objArr);
    }

    public final String srcText(DataSource ds) {
        Intrinsics.checkNotNullParameter(ds, "ds");
        int i = WhenMappings.$EnumSwitchMapping$4[ds.ordinal()];
        if (i == 1) {
            return Res.INSTANCE.str(R.string.source_real, new Object[0]);
        }
        if (i == 2) {
            return Res.INSTANCE.str(R.string.source_mixed, new Object[0]);
        }
        if (i != 3) {
            throw new NoWhenBranchMatchedException();
        }
        return Res.INSTANCE.str(R.string.source_estimated, new Object[0]);
    }

    public final String ago(long ts, long now) {
        long j = (now - ts) / 60000;
        if (j < 1) {
            return Res.INSTANCE.str(R.string.just_now, new Object[0]);
        }
        return Res.INSTANCE.str(R.string.minutes_ago_fmt, Integer.valueOf((int) j));
    }
}
