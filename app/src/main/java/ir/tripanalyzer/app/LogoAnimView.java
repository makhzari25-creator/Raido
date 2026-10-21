package ir.tripanalyzer.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** لوگوی رایدو: R سفید ثابت + جادهٔ آبی که از پایین به بالا ظاهر می‌شود (حدود ۱ ثانیه) */
final class LogoAnimView extends View {
    private final Bitmap white;
    private final Bitmap cyan;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint maskPaint = new Paint();
    private final RectF dst = new RectF();
    private float reveal = 0f;   // 0..1
    private float fade = 0f;     // 0..1 (R سفید)
    private float glow = 0f;     // 0..1 درخشش نرم پایانی

    LogoAnimView(Context c) {
        super(c);
        white = BitmapFactory.decodeResource(getResources(), R.drawable.logo_white);
        cyan = BitmapFactory.decodeResource(getResources(), R.drawable.logo_cyan);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    void play(final Runnable done) {
        ValueAnimator whiteIn = ValueAnimator.ofFloat(0f, 1f);
        whiteIn.setDuration(260);
        whiteIn.addUpdateListener(a -> { fade = (float) a.getAnimatedValue(); invalidate(); });

        ValueAnimator road = ValueAnimator.ofFloat(0f, 1f);
        road.setStartDelay(120);
        road.setDuration(720);
        road.setInterpolator(new DecelerateInterpolator(1.4f));
        road.addUpdateListener(a -> { reveal = (float) a.getAnimatedValue(); invalidate(); });

        ValueAnimator shine = ValueAnimator.ofFloat(0f, 1f, 0f);
        shine.setStartDelay(780);
        shine.setDuration(300);
        shine.addUpdateListener(a -> { glow = (float) a.getAnimatedValue(); invalidate(); });
        shine.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (done != null) done.run();
            }
        });
        whiteIn.start();
        road.start();
        shine.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;
        float ratio = (float) white.getHeight() / white.getWidth();
        float lw = w, lh = lw * ratio;
        if (lh > h) { lh = h; lw = lh / ratio; }
        float l = (w - lw) / 2f, t = (h - lh) / 2f;
        dst.set(l, t, l + lw, t + lh);

        paint.setAlpha(Math.round(255 * fade));
        canvas.drawBitmap(white, null, dst, paint);

        if (reveal <= 0f) return;
        int sc = canvas.saveLayer(dst, null);
        paint.setAlpha(255);
        canvas.drawBitmap(cyan, null, dst, paint);
        // لبهٔ نرم: زیر خط ظاهر‌شدن کامل، بالای آن محو
        float front = dst.bottom - (lh * 1.18f) * reveal;     // خط جلو (از پایین به بالا)
        float soft = lh * 0.18f;
        maskPaint.setShader(new LinearGradient(0, front, 0, front + soft,
                0x00000000, 0xFF000000, Shader.TileMode.CLAMP));
        canvas.drawRect(dst, maskPaint);
        canvas.restoreToCount(sc);

        if (glow > 0f) {
            // درخششی ملایم روی جاده
            int sc2 = canvas.saveLayerAlpha(dst, Math.round(110 * glow));
            paint.setAlpha(255);
            paint.setColorFilter(new android.graphics.PorterDuffColorFilter(0xFFBFFCFF, PorterDuff.Mode.SRC_ATOP));
            canvas.drawBitmap(cyan, null, dst, paint);
            paint.setColorFilter(null);
            canvas.restoreToCount(sc2);
        }
    }
}
