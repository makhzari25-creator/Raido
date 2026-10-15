package ir.tripanalyzer.app;

import java.util.Arrays;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;

/* compiled from: App.kt */
@Metadata(d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0011\n\u0000\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J'\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0012\u0010\b\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00010\t\"\u00020\u0001¢\u0006\u0002\u0010\n"}, d2 = {"Lir/tripanalyzer/app/Res;", "", "<init>", "()V", "str", "", "resId", "", "args", "", "(I[Ljava/lang/Object;)Ljava/lang/String;"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class Res {
    public static final Res INSTANCE = new Res();

    private Res() {
    }

    public final String str(int resId, Object... args) {
        Intrinsics.checkNotNullParameter(args, "args");
        if (args.length == 0) {
            String string = App.INSTANCE.getCtx().getString(resId);
            Intrinsics.checkNotNullExpressionValue(string, "getString(...)");
            return string;
        }
        StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
        String string2 = App.INSTANCE.getCtx().getString(resId);
        Intrinsics.checkNotNullExpressionValue(string2, "getString(...)");
        Object[] copyOf = Arrays.copyOf(args, args.length);
        String format = String.format(string2, Arrays.copyOf(copyOf, copyOf.length));
        Intrinsics.checkNotNullExpressionValue(format, "format(...)");
        return format;
    }
}
