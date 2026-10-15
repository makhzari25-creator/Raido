package ir.tripanalyzer.app.core;

/**
 * مرزهای اجرای رایدو (جداسازی منطقی — بدون تغییر رفتار UI/UX).
 *
 * <pre>
 *  [Input]  Accessibility / Notification  →  متن خام + نوع رویداد
 *     ↓
 *  [Parse]  ParseLayer  →  ParsedTrip / آدرس / duringTrip
 *     ↓
 *  [Engine] TripEngine + GeoLayer  →  TripResult + شواهد نشان
 *     ↓
 *  [Lifecycle] TripLifecycle  →  activeTrip / ثبت پایان سفر
 *     ↓
 *  [Persist] Store / RaidoDataStore
 *     ↓
 *  [UI] Main / Details / Overlay  (بدون منطق پارس)
 * </pre>
 *
 * قوانین:
 * - Activity و Service در پکیج ریشه می‌مانند (مانیفست پایدار).
 * - منطق کسب‌وکار جابه‌جا می‌شود، نه عوض.
 * - UI فقط نتیجه ذخیره‌شده را می‌خواند.
 */
public final class Architecture {
    private Architecture() {}

    public static final String LAYER_INPUT = "input";
    public static final String LAYER_PARSE = "parse";
    public static final String LAYER_ENGINE = "engine";
    public static final String LAYER_GEO = "geo";
    public static final String LAYER_LIFECYCLE = "lifecycle";
    public static final String LAYER_PERSIST = "persist";
    public static final String LAYER_UI = "ui";
}
