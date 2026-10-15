package ir.tripanalyzer.app;

import android.content.Context;

/**
 * لایه آماده‌سازی اتصال به سرور در آینده.
 * فعلاً هیچ درخواست شبکه‌ای انجام نمی‌دهد.
 * وقتی سرور آماده شد، پیاده‌سازی این متدها کافی است.
 */
public final class ServerApi {
    public static final ServerApi INSTANCE = new ServerApi();

    /** آدرس پایه API — بعداً پر کنید */
    public static final String BASE_URL = "https://api.raido.app/v1/";

    private ServerApi() {
    }

    /**
     * دریافت وضعیت اشتراک از سرور بر اساس اثرانگشت دستگاه.
     */
    public void fetchSubscriptionStatus(Context context, String deviceFp, Callback callback) {
        // TODO: HTTP GET BASE_URL + "subscription?fp=" + deviceFp
        if (callback != null) callback.onResult(false, null);
    }

    /**
     * گزارش پرداخت موفق به سرور (بعد از Verify زرین‌پال).
     */
    public void reportSubscription(Context context, String type, String authority, long expiryMs) {
        // TODO: HTTP POST BASE_URL + "subscription/activate"
    }

    /**
     * تأیید تراکنش زرین‌پال سمت سرور (امن).
     */
    public void verifyZarinpal(String authority, String type, String deviceFp, Callback callback) {
        // TODO: HTTP POST BASE_URL + "payment/verify"
        if (callback != null) callback.onResult(false, null);
    }

    public interface Callback {
        void onResult(boolean success, String message);
    }
}
