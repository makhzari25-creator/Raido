package ir.tripanalyzer.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/* compiled from: VerdictOverlayActivity.kt */
/* loaded from: classes.dex */
public final class VerdictOverlayActivity extends Activity {
    public static /* synthetic */ void $r8$lambda$miram(VerdictOverlayActivity verdictOverlayActivity, View view) {
        try {
            Store store = Store.INSTANCE;
            // همان مسیر «رفتم» در PromptActivity: ثبت + بازگشت هدف + صفر کردن شمارنده + لغو کادر «رفتی؟»
            store.saveToRecentFromLast(verdictOverlayActivity);
            store.clearTempTarget(verdictOverlayActivity);
            store.resetConsecutiveRejects(verdictOverlayActivity);
            store.clearPendingConfirm(verdictOverlayActivity);
        } catch (Exception unused) {
        }
        Toast.makeText(verdictOverlayActivity, "سفر در گزارش\u200cها ذخیره شد", 0).show();
        verdictOverlayActivity.finish();
    }

    /**
     * بین یک سبز کم‌رنگ (تازه از خط سبز رد شده) و یک سبز پررنگ (نزدیک/بالای هدف کاربر)
     * درون‌یابی می‌کند. ratio=0 → سبز کم‌رنگ، ratio=1 → سبز پررنگ.
     */
    private static int interpolateGreen(float ratio) {
        if (ratio < 0f) ratio = 0f;
        if (ratio > 1f) ratio = 1f;
        int lightR = 0xA7, lightG = 0xF3, lightB = 0xD0; // سبز نعنایی کم‌رنگ
        int fullR = 0x00, fullG = 0xC8, fullB = 0x53;    // سبز پررنگ
        int r = Math.round(lightR + (fullR - lightR) * ratio);
        int g = Math.round(lightG + (fullG - lightG) * ratio);
        int b = Math.round(lightB + (fullB - lightB) * ratio);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Window window = getWindow();
        window.addFlags(524288);
        window.addFlags(2097152);
        window.addFlags(128);
        window.addFlags(67108864);

        Intent intent = getIntent();
        View.OnClickListener dismiss = new View.OnClickListener() {
            @Override public void onClick(View v) {
                try { VerdictOverlayActivity.this.finish(); } catch (Exception ignored) {}
            }
        };
        View card = VerdictCard.build(this,
                intent.getStringExtra("verdict"),
                intent.getStringExtra("fare"),
                intent.getStringExtra("hourly"),
                intent.getStringExtra("distSrc"),
                intent.getFloatExtra("greenRatio", 1.0f),
                intent.getStringExtra("zoneLabel"),
                dismiss);
        card.setOnClickListener(dismiss); // لمس هرجای صفحه → بستن (رفتار قبلی fallback)
        setContentView(card);

        long showMs = 6000L;
        try {
            int sec = Store.INSTANCE.prefs(this).getInt("overlay_card_seconds", 6);
            if (sec < 3) sec = 3;
            if (sec > 15) sec = 15;
            showMs = sec * 1000L;
        } catch (Throwable ignored) {}
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public final void run() {
                try { VerdictOverlayActivity.this.finish(); } catch (Throwable ignored) {}
            }
        }, showMs);
    }
}
