package ir.tripanalyzer.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: UiKit.kt */
@Metadata(d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0004\bÆ\u0002\u0018\u00002\u00020\u0001:\u000278B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0016\u0010\n\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u0005J\u0016\u0010\u000e\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\u0005J\u0016\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0012\u001a\u00020\u0013J\u000e\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u000b\u001a\u00020\fJ\u000e\u0010\u0016\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\fJ\u000e\u0010\u0017\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\fJ7\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u00132\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u00052\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u0005¢\u0006\u0002\u0010\u001eJ\u0016\u0010\u001f\u001a\u00020 2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010!\u001a\u00020\u0013J\u001e\u0010\"\u001a\u00020\u00192\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010#\u001a\u00020\u00132\u0006\u0010$\u001a\u00020\u0005J\u001e\u0010%\u001a\u00020&2\u0006\u0010'\u001a\u00020 2\u0006\u0010(\u001a\u00020)2\u0006\u0010\u000b\u001a\u00020\fJ\u001e\u0010*\u001a\u00020+2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010!\u001a\u00020\u00132\u0006\u0010,\u001a\u00020)J\u0010\u0010-\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\fH\u0002JG\u0010'\u001a\u00020\u00152\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010!\u001a\u00020\u00132\u0006\u0010.\u001a\u00020\u00132\n\b\u0002\u0010/\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u00100\u001a\u0004\u0018\u00010\u00132\n\b\u0002\u00101\u001a\u0004\u0018\u00010\u0005¢\u0006\u0002\u00102J\u001e\u00103\u001a\u0002042\u0006\u0010\u000b\u001a\u00020\f2\u0006\u00105\u001a\u00020\u00132\u0006\u00106\u001a\u00020\u0013R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0005X\u0086T¢\u0006\u0002\n\u0000"}, d2 = {"Lir/tripanalyzer/app/UiKit;", "", "<init>", "()V", "BRAND", "", "VERDICT_GREEN", "VERDICT_YELLOW", "VERDICT_RED", "VERDICT_GRAY", "dp", "c", "Landroid/content/Context;", "v", "color", "resId", "page", "Lir/tripanalyzer/app/UiKit$Page;", "title", "", "card", "Landroid/widget/LinearLayout;", "surfaceColor", "textColor", "text", "Landroid/widget/TextView;", "s", "sizeSp", "", "weight", "(Landroid/content/Context;Ljava/lang/String;FILjava/lang/Integer;)Landroid/widget/TextView;", "statusRow", "Lir/tripanalyzer/app/UiKit$StatusRow;", "label", "pill", "textValue", "bg", "updateStatusRow", "", "row", "enabled", "", "button", "Landroid/widget/Button;", "primary", "brandStroke", "value", "valueColor", "pillText", "pillColor", "(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Integer;)Landroid/widget/LinearLayout;", "input", "Landroid/widget/EditText;", "hint", "initial", "Page", "StatusRow"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class UiKit {
    public static final int BRAND = 0xFF2563EB;
    public static final UiKit INSTANCE = new UiKit();
    public static final int VERDICT_GRAY = 0xFF64748B;
    public static final int VERDICT_GREEN = 0xFF16A34A;
    public static final int VERDICT_RED = 0xFFDC2626;
    public static final int VERDICT_YELLOW = 0xFFCA8A04;

    private UiKit() {
    }

    public final int dp(Context c, int v) {
        Intrinsics.checkNotNullParameter(c, "c");
        return (int) TypedValue.applyDimension(1, v, c.getResources().getDisplayMetrics());
    }

    public final int color(Context c, int resId) {
        Intrinsics.checkNotNullParameter(c, "c");
        return c.getResources().getColor(resId, c.getTheme());
    }

    /* compiled from: UiKit.kt */
    @Metadata(d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007¢\u0006\u0004\b\b\u0010\tR\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0007¢\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f"}, d2 = {"Lir/tripanalyzer/app/UiKit$Page;", "", "scroll", "Landroid/widget/ScrollView;", "content", "Landroid/widget/LinearLayout;", "title", "Landroid/widget/TextView;", "<init>", "(Landroid/widget/ScrollView;Landroid/widget/LinearLayout;Landroid/widget/TextView;)V", "getScroll", "()Landroid/widget/ScrollView;", "getContent", "()Landroid/widget/LinearLayout;", "getTitle", "()Landroid/widget/TextView;"}, k = 1, mv = {2, 0, 0}, xi = 48)
    public static final class Page {
        private final LinearLayout content;
        private final ScrollView scroll;
        private final TextView title;

        public Page(ScrollView scroll, LinearLayout content, TextView title) {
            Intrinsics.checkNotNullParameter(scroll, "scroll");
            Intrinsics.checkNotNullParameter(content, "content");
            Intrinsics.checkNotNullParameter(title, "title");
            this.scroll = scroll;
            this.content = content;
            this.title = title;
        }

        public final LinearLayout getContent() {
            return this.content;
        }

        public final ScrollView getScroll() {
            return this.scroll;
        }

        public final TextView getTitle() {
            return this.title;
        }
    }

    public final Page page(Context c, String title) {
        Intrinsics.checkNotNullParameter(c, "c");
        Intrinsics.checkNotNullParameter(title, "title");
        ScrollView scrollView = new ScrollView(c);
        scrollView.setFitsSystemWindows(true);
        LinearLayout linearLayout = new LinearLayout(c);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(c, 16), dp(c, 12), dp(c, 16), dp(c, 24));
        TextView textView = new TextView(c);
        textView.setText(title);
        textView.setTextSize(2, 20.0f);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setPadding(0, dp(c, 8), 0, dp(c, 12));
        linearLayout.addView(textView);
        scrollView.addView(linearLayout);
        return new Page(scrollView, linearLayout, textView);
    }

    public final LinearLayout card(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        LinearLayout linearLayout = new LinearLayout(c);
        linearLayout.setOrientation(1);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(dp(c, 16));
        gradientDrawable.setColor(surfaceColor(c));
        linearLayout.setBackground(gradientDrawable);
        linearLayout.setPadding(dp(c, 16), dp(c, 14), dp(c, 16), dp(c, 14));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, 0, 0, dp(c, 12));
        linearLayout.setLayoutParams(layoutParams);
        return linearLayout;
    }

    public final int surfaceColor(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        TypedArray obtainStyledAttributes = c.getTheme().obtainStyledAttributes(new int[]{android.R.attr.colorBackgroundFloating});
        Intrinsics.checkNotNullExpressionValue(obtainStyledAttributes, "obtainStyledAttributes(...)");
        int color = obtainStyledAttributes.getColor(0, -1);
        obtainStyledAttributes.recycle();
        return color;
    }

    public final int textColor(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        return new TextView(c).getCurrentTextColor();
    }

    public static /* synthetic */ TextView text$default(UiKit uiKit, Context context, String str, float f, int i, Integer num, int i2, Object obj) {
        if ((i2 & 16) != 0) {
            num = null;
        }
        return uiKit.text(context, str, f, i, num);
    }

    public final TextView text(Context c, String s, float sizeSp, int weight, Integer color) {
        Intrinsics.checkNotNullParameter(c, "c");
        Intrinsics.checkNotNullParameter(s, "s");
        TextView textView = new TextView(c);
        textView.setText(s);
        textView.setTextSize(2, sizeSp);
        textView.setTypeface(Typeface.DEFAULT, weight >= 700 ? 1 : 0);
        if (color != null) {
            textView.setTextColor(color.intValue());
        }
        textView.setLineSpacing(dp(c, 2), 1.0f);
        return textView;
    }

    /* compiled from: UiKit.kt */
    @Metadata(d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b"}, d2 = {"Lir/tripanalyzer/app/UiKit$StatusRow;", "", "container", "Landroid/widget/LinearLayout;", "pill", "Landroid/widget/TextView;", "<init>", "(Landroid/widget/LinearLayout;Landroid/widget/TextView;)V", "getContainer", "()Landroid/widget/LinearLayout;", "getPill", "()Landroid/widget/TextView;"}, k = 1, mv = {2, 0, 0}, xi = 48)
    public static final class StatusRow {
        private final LinearLayout container;
        private final TextView pill;

        public StatusRow(LinearLayout container, TextView pill) {
            Intrinsics.checkNotNullParameter(container, "container");
            Intrinsics.checkNotNullParameter(pill, "pill");
            this.container = container;
            this.pill = pill;
        }

        public final LinearLayout getContainer() {
            return this.container;
        }

        public final TextView getPill() {
            return this.pill;
        }
    }

    public final StatusRow statusRow(Context c, String label) {
        Intrinsics.checkNotNullParameter(c, "c");
        Intrinsics.checkNotNullParameter(label, "label");
        LinearLayout linearLayout = new LinearLayout(c);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        linearLayout.setPadding(0, dp(c, 8), 0, dp(c, 4));
        linearLayout.addView(text$default(this, c, label, 14.0f, 400, null, 16, null), new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView pill = pill(c, "", VERDICT_GRAY);
        linearLayout.addView(pill);
        return new StatusRow(linearLayout, pill);
    }

    public final TextView pill(Context c, String textValue, int bg) {
        Intrinsics.checkNotNullParameter(c, "c");
        Intrinsics.checkNotNullParameter(textValue, "textValue");
        TextView textView = new TextView(c);
        textView.setText(textValue);
        textView.setTextSize(2, 12.0f);
        textView.setTextColor(-1);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(dp(c, 12));
        gradientDrawable.setColor(bg);
        textView.setBackground(gradientDrawable);
        textView.setPadding(dp(c, 10), dp(c, 3), dp(c, 10), dp(c, 3));
        return textView;
    }

    public final void updateStatusRow(StatusRow row, boolean enabled, Context c) {
        Intrinsics.checkNotNullParameter(row, "row");
        Intrinsics.checkNotNullParameter(c, "c");
        row.getPill().setText(c.getString(enabled ? ir.tripanalyzer.app.R.string.state_enabled : ir.tripanalyzer.app.R.string.state_disabled));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(dp(c, 12));
        gradientDrawable.setColor(enabled ? VERDICT_GREEN : VERDICT_GRAY);
        row.getPill().setBackground(gradientDrawable);
    }

    public final Button button(Context c, String label, boolean primary) {
        Intrinsics.checkNotNullParameter(c, "c");
        Intrinsics.checkNotNullParameter(label, "label");
        Button button = new Button(c);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(2, 14.0f);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(dp(c, 14));
        if (primary) {
            gradientDrawable.setColor(-16028061);
            button.setTextColor(-1);
        } else {
            gradientDrawable.setStroke(dp(c, 1), brandStroke(c));
            gradientDrawable.setColor(0);
            button.setTextColor(brandStroke(c));
        }
        button.setBackground(gradientDrawable);
        button.setPadding(dp(c, 16), dp(c, 10), dp(c, 16), dp(c, 10));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, dp(c, 8), 0, 0);
        button.setLayoutParams(layoutParams);
        return button;
    }

    private final int brandStroke(Context c) {
        return color(c, ir.tripanalyzer.app.R.color.brand);
    }

    public final LinearLayout row(Context c, String label, String value, Integer valueColor, String pillText, Integer pillColor) {
        Intrinsics.checkNotNullParameter(c, "c");
        Intrinsics.checkNotNullParameter(label, "label");
        Intrinsics.checkNotNullParameter(value, "value");
        LinearLayout linearLayout = new LinearLayout(c);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        linearLayout.setPadding(0, dp(c, 6), 0, dp(c, 6));
        linearLayout.addView(text$default(this, c, label, 13.0f, 400, null, 16, null), new LinearLayout.LayoutParams(0, -2, 1.0f));
        if (pillText != null) {
            linearLayout.addView(pill(c, pillText, pillColor != null ? pillColor.intValue() : VERDICT_GRAY));
            TextView textView = new TextView(c);
            textView.setMinWidth(dp(c, 8));
            linearLayout.addView(textView);
        }
        linearLayout.addView(text(c, value, 14.0f, 700, valueColor));
        return linearLayout;
    }

    public final EditText input(Context c, String hint, String initial) {
        Intrinsics.checkNotNullParameter(c, "c");
        Intrinsics.checkNotNullParameter(hint, "hint");
        Intrinsics.checkNotNullParameter(initial, "initial");
        EditText editText = new EditText(c);
        editText.setHint(hint);
        editText.setText(initial);
        editText.setInputType(8194);
        editText.setTextSize(2, 14.0f);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(dp(c, 12));
        gradientDrawable.setStroke(dp(c, 1), 855638016);
        gradientDrawable.setColor(0);
        editText.setBackground(gradientDrawable);
        editText.setPadding(dp(c, 12), dp(c, 10), dp(c, 12), dp(c, 10));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, dp(c, 6), 0, 0);
        editText.setLayoutParams(layoutParams);
        return editText;
    }
}
