package ir.tripanalyzer.app;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * فارسی: Vazirmatn | هر کلمه/تکهٔ لاتین: Voga Medium | +1sp یک‌بار روی هر TextView
 */
public final class Fonts {
    public static final Fonts INSTANCE = new Fonts();
    private static final Pattern LATIN = Pattern.compile("[A-Za-z][A-Za-z0-9._\\-]*");

    private Typeface persian;
    private Typeface english;
    private boolean ready;
    private Context appCtx;

    private Fonts() {}

    public void init(Context context) {
        if (context != null) appCtx = context.getApplicationContext();
        if (ready && persian != null) return;
        try {
            persian = Typeface.createFromAsset(context.getAssets(), "fonts/Vazirmatn-Regular.ttf");
        } catch (Exception e) {
            try {
                persian = Typeface.createFromAsset(context.getAssets(), "fonts/IRANSans-Regular.ttf");
            } catch (Exception e2) {
                try {
                    persian = ResourcesCompat.getFont(context, R.font.iranian_sans);
                } catch (Exception e3) {
                    persian = Typeface.DEFAULT;
                }
            }
        }
        try {
            english = ResourcesCompat.getFont(context, R.font.voga_medium);
        } catch (Exception e) {
            try {
                english = Typeface.createFromAsset(context.getAssets(), "fonts/voga_medium.otf");
            } catch (Exception e2) {
                english = Typeface.SERIF;
            }
        }
        ready = true;
    }

    public Typeface regular() { return persian != null ? persian : Typeface.DEFAULT; }
    public Typeface bold() {
        try {
            if (appCtx != null) {
                Typeface b = Typeface.createFromAsset(appCtx.getAssets(), "fonts/Vazirmatn-Bold.ttf");
                if (b != null) return b;
            }
        } catch (Exception ignored) {}
        return persian != null ? persian : Typeface.DEFAULT_BOLD;
    }
    public Typeface medium() { return regular(); }
    public Typeface english() {
        if (english != null) return english;
        if (appCtx != null) {
            try { english = Typeface.createFromAsset(appCtx.getAssets(), "fonts/voga_medium.otf"); }
            catch (Exception ignored) { english = Typeface.SERIF; }
        }
        return english != null ? english : Typeface.SERIF;
    }

    /** اعمال فونت پایه فارسی + Voga روی هر تکهٔ لاتین */
    public void apply(TextView tv, boolean isBold) {
        if (tv == null) return;
        CharSequence cs = tv.getText();
        if (cs == null) return;
        String s = cs.toString();
        if (s.isEmpty()) {
            tv.setTypeface(regular());
            return;
        }
        // اگر کل متن لاتین است
        if (isAllLatin(s)) {
            tv.setTypeface(english());
            return;
        }
        tv.setTypeface(regular());
        if (!containsLatin(s)) return;
        // Spannable برای کلمات انگلیسی داخل جمله فارسی
        try {
            SpannableString span = new SpannableString(s);
            Matcher m = LATIN.matcher(s);
            Typeface eng = english();
            while (m.find()) {
                span.setSpan(new CustomTypefaceSpan(eng), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            tv.setText(span);
        } catch (Exception ignored) {}
    }

    public void applyEnglish(TextView tv) {
        if (tv != null) tv.setTypeface(english());
    }

    public void apply(Button btn, boolean isBold) {
        if (btn == null) return;
        apply((TextView) btn, isBold);
    }

    public void apply(EditText et) {
        if (et != null) et.setTypeface(regular());
    }

    private static final int TAG_SIZE_BUMP = 0x70A11D01;

    public void applyRecursive(View root) {
        if (root == null) return;
        if (root instanceof TextView) {
            TextView tv = (TextView) root;
            apply(tv, false);
            try {
                if (tv.getTag(TAG_SIZE_BUMP) == null) {
                    float sp = tv.getTextSize() / tv.getResources().getDisplayMetrics().scaledDensity;
                    tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, sp + 1f);
                    tv.setTag(TAG_SIZE_BUMP, Boolean.TRUE);
                }
            } catch (Throwable ignored) {}
        }
        if (root instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i < vg.getChildCount(); i++) applyRecursive(vg.getChildAt(i));
        }
    }

    private static boolean containsLatin(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) return true;
        }
        return false;
    }

    private static boolean isAllLatin(String s) {
        boolean any = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) {
                if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) return false;
                any = true;
            }
        }
        return any;
    }

    /** TypefaceSpan سازگار با API قدیمی */
    private static final class CustomTypefaceSpan extends TypefaceSpan {
        private final Typeface tf;
        CustomTypefaceSpan(Typeface tf) {
            super("");
            this.tf = tf;
        }
        @Override
        public void updateDrawState(android.text.TextPaint ds) {
            apply(ds);
        }
        @Override
        public void updateMeasureState(android.text.TextPaint paint) {
            apply(paint);
        }
        private void apply(android.text.TextPaint paint) {
            Typeface old = paint.getTypeface();
            int style = old != null ? old.getStyle() : 0;
            Typeface nt = Typeface.create(tf, style);
            paint.setTypeface(nt);
        }
    }
}
