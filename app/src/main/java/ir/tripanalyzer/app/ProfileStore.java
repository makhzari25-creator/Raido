package ir.tripanalyzer.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/** نام نمایشی و عکس پروفایل — فقط روی گوشی */
public final class ProfileStore {
    public static final ProfileStore INSTANCE = new ProfileStore();
    private static final String PREFS = "raido_profile";
    private static final String KEY_NAME = "display_name";
    private static final String KEY_PHOTO = "photo_path";
    public static final String DEFAULT_NAME = "کاربر رایدو";

    private ProfileStore() {}

    private SharedPreferences p(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String getDisplayName(Context c) {
        if (c == null) return DEFAULT_NAME;
        String n = p(c).getString(KEY_NAME, "");
        if (n == null || n.trim().isEmpty()) return DEFAULT_NAME;
        return n.trim();
    }

    public void setDisplayName(Context c, String name) {
        if (c == null) return;
        String v = name == null ? "" : name.trim();
        p(c).edit().putString(KEY_NAME, v).apply();
    }

    public boolean hasCustomName(Context c) {
        if (c == null) return false;
        String n = p(c).getString(KEY_NAME, "");
        return n != null && !n.trim().isEmpty();
    }

    public String getPhotoPath(Context c) {
        if (c == null) return null;
        String path = p(c).getString(KEY_PHOTO, null);
        if (path == null || path.isEmpty()) return null;
        File f = new File(path);
        return f.exists() ? path : null;
    }

    public boolean hasPhoto(Context c) {
        return getPhotoPath(c) != null;
    }

    /** کپی عکس از Uri به فایل داخلی اپ */
    public boolean savePhotoFromUri(Context c, Uri uri) {
        if (c == null || uri == null) return false;
        try {
            InputStream in = c.getContentResolver().openInputStream(uri);
            if (in == null) return false;
            Bitmap bmp = BitmapFactory.decodeStream(in);
            in.close();
            if (bmp == null) return false;
            // کوچک‌سازی سبک
            int max = 512;
            int w = bmp.getWidth();
            int h = bmp.getHeight();
            if (w > max || h > max) {
                float scale = Math.min(max / (float) w, max / (float) h);
                bmp = Bitmap.createScaledBitmap(bmp, Math.round(w * scale), Math.round(h * scale), true);
            }
            File dir = new File(c.getApplicationContext().getFilesDir(), "profile");
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, "avatar.jpg");
            FileOutputStream fos = new FileOutputStream(out);
            bmp.compress(Bitmap.CompressFormat.JPEG, 88, fos);
            fos.close();
            p(c).edit().putString(KEY_PHOTO, out.getAbsolutePath()).apply();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void clearPhoto(Context c) {
        if (c == null) return;
        try {
            String path = p(c).getString(KEY_PHOTO, null);
            if (path != null) new File(path).delete();
        } catch (Exception ignored) {}
        p(c).edit().remove(KEY_PHOTO).apply();
    }

    public Bitmap loadPhotoBitmap(Context c) {
        String path = getPhotoPath(c);
        if (path == null) return null;
        try {
            return BitmapFactory.decodeFile(path);
        } catch (Exception e) {
            return null;
        }
    }
}
