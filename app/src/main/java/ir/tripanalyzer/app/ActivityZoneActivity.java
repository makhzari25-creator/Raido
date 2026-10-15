package ir.tripanalyzer.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * محدوده فعالیت — نقشه .mbtiles جدا از APK، فقط با آپلود کاربر پرمیوم.
 * بدون نقشه: WebView لود نمی‌شود؛ اپ و تحلیل عادی مختل نمی‌شود.
 */
public final class ActivityZoneActivity extends BaseActivity {

    private static final int REQ_LOC = 4411;
    private static final int REQ_PICK_MAP = 4412;
    // fetch() روی file:// در WebView کروم کار نمی‌کند (Failed to fetch)؛ فایل‌ها از یک origin مجازی https سرو می‌شوند.
    private static final String VHOST = "raido-zone.local";

    private WebView web;
    private FrameLayout mapBox;
    private LinearLayout noMapPanel;
    private TextView mapStatusTv;
    private Switch enableSwitch;
    private Switch strictSwitch;
    private TextView radiusLabel;
    private TextView centerLabel;
    private SeekBar radiusSeek;

    private double centerLat = 36.551;
    private double centerLng = 52.678;
    private int radiusKm = 5;
    private boolean mapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ActivityZoneStore.INSTANCE.hasPoint(this)) {
            centerLat = ActivityZoneStore.INSTANCE.getLat(this);
            centerLng = ActivityZoneStore.INSTANCE.getLng(this);
            double r = ActivityZoneStore.INSTANCE.getRadiusKm(this);
            radiusKm = (int) Math.round(r <= 0 ? 5 : r);
            if (radiusKm < 3) radiusKm = 3;
            if (radiusKm > 8) radiusKm = 8;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0B1220);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.VERTICAL);
        head.setPadding(dp(16), dp(18), dp(16), dp(12));
        head.setBackgroundColor(0xFF0B3D3A);
        TextView title = new TextView(this);
        title.setText("محدوده فعالیت");
        title.setTextColor(0xFFF8FAFC);
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        TextView sub = new TextView(this);
        sub.setText("نقشه جدا از برنامه است. پرمیوم: آپلود .mbtiles ← مرکز و شعاع ← فعال‌سازی.\nبدون نقشه، تحلیل قیمتی/ساعتی مثل قبل کار می‌کند.");
        sub.setTextColor(0xFF99F6E4);
        sub.setTextSize(12.5f);
        sub.setPadding(0, dp(4), 0, 0);
        head.addView(title);
        head.addView(sub);
        root.addView(head);

        mapBox = new FrameLayout(this);
        mapBox.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        noMapPanel = new LinearLayout(this);
        noMapPanel.setOrientation(LinearLayout.VERTICAL);
        noMapPanel.setPadding(dp(20), dp(24), dp(20), dp(24));
        noMapPanel.setBackgroundColor(0xFF0F172A);
        TextView noTitle = new TextView(this);
        noTitle.setText("نقشه نصب نشده");
        noTitle.setTextColor(0xFFF8FAFC);
        noTitle.setTextSize(17);
        noTitle.setTypeface(Typeface.DEFAULT_BOLD);
        TextView noBody = new TextView(this);
        noBody.setText("فایل نقشه استان (.mbtiles) را از پشتیبانی بگیرید و آپلود کنید.\nتا قبل از آپلود، محدوده روی تحلیل اثر ندارد.");
        noBody.setTextColor(0xFF94A3B8);
        noBody.setTextSize(13);
        noBody.setPadding(0, dp(8), 0, 0);
        noMapPanel.addView(noTitle);
        noMapPanel.addView(noBody);
        mapBox.addView(noMapPanel, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        root.addView(mapBox);

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.VERTICAL);
        bottom.setPadding(dp(16), dp(12), dp(16), dp(16));
        bottom.setBackgroundColor(0xFFF8FAFC);

        mapStatusTv = new TextView(this);
        mapStatusTv.setTextColor(0xFF0F766E);
        mapStatusTv.setTextSize(12.5f);
        mapStatusTv.setPadding(0, 0, 0, dp(8));
        bottom.addView(mapStatusTv);

        Button uploadBtn = new Button(this);
        uploadBtn.setText("آپلود نقشه (.mbtiles)");
        uploadBtn.setAllCaps(false);
        stylePrimary(uploadBtn);
        uploadBtn.setOnClickListener(v -> pickMapFile());
        bottom.addView(uploadBtn);

        enableSwitch = new Switch(this);
        enableSwitch.setText("فعال‌سازی محدوده فعالیت");
        enableSwitch.setTextColor(0xFF0F172A);
        enableSwitch.setChecked(ActivityZoneStore.INSTANCE.isEnabled(this));
        enableSwitch.setPadding(0, dp(10), 0, 0);
        bottom.addView(enableSwitch);

        TextView enHint = new TextView(this);
        enHint.setText("فقط با پرمیوم + نقشه + مرکز؛ وگرنه تحلیل عادی/شهری قبلی");
        enHint.setTextColor(0xFF64748B);
        enHint.setTextSize(11.5f);
        bottom.addView(enHint);

        strictSwitch = new Switch(this);
        strictSwitch.setText("خارج محدوده = همیشه قرمز");
        strictSwitch.setTextColor(0xFF0F172A);
        strictSwitch.setChecked(ActivityZoneStore.INSTANCE.isStrictRed(this));
        strictSwitch.setPadding(0, dp(8), 0, 0);
        bottom.addView(strictSwitch);

        radiusLabel = new TextView(this);
        radiusLabel.setText("شعاع: " + radiusKm + " کیلومتر");
        radiusLabel.setTextColor(0xFF0F766E);
        radiusLabel.setTextSize(14);
        radiusLabel.setTypeface(Typeface.DEFAULT_BOLD);
        radiusLabel.setPadding(0, dp(10), 0, 0);
        bottom.addView(radiusLabel);

        radiusSeek = new SeekBar(this);
        radiusSeek.setMax(5);
        radiusSeek.setProgress(radiusKm - 3);
        radiusSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusKm = 3 + progress;
                radiusLabel.setText("شعاع: " + radiusKm + " کیلومتر");
                if (mapReady && web != null) {
                    web.evaluateJavascript(
                            "window.RaidoMapApi && RaidoMapApi.setRadius(" + radiusKm + ")", null);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        bottom.addView(radiusSeek);

        centerLabel = new TextView(this);
        centerLabel.setText(String.format(java.util.Locale.US, "مرکز: %.4f ، %.4f", centerLat, centerLng));
        centerLabel.setTextColor(0xFF475569);
        centerLabel.setTextSize(12);
        centerLabel.setPadding(0, dp(6), 0, dp(10));
        bottom.addView(centerLabel);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        Button gpsBtn = new Button(this);
        gpsBtn.setText("موقعیت من");
        gpsBtn.setAllCaps(false);
        styleSecondary(gpsBtn);
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        gp.setMargins(0, 0, dp(6), 0);
        gpsBtn.setLayoutParams(gp);
        gpsBtn.setOnClickListener(v -> requestGps());
        row.addView(gpsBtn);

        Button saveBtn = new Button(this);
        saveBtn.setText("تأیید محدوده");
        saveBtn.setAllCaps(false);
        stylePrimary(saveBtn);
        saveBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f));
        saveBtn.setOnClickListener(v -> saveZone());
        row.addView(saveBtn);

        bottom.addView(row);
        root.addView(bottom);
        setContentView(root);

        refreshMapUi();
    }

    private void refreshMapUi() {
        boolean hasMap = ActivityZoneStore.INSTANCE.hasMapFile(this);
        String name = ActivityZoneStore.INSTANCE.getMapDisplayName(this);
        if (hasMap) {
            long mb = Math.max(1, ActivityZoneStore.INSTANCE.mapFile(this).length() / (1024 * 1024));
            mapStatusTv.setText("نقشه آماده" + (name.isEmpty() ? "" : (" · " + name)) + " · حدود " + mb + " مگ");
            noMapPanel.setVisibility(View.GONE);
            ensureWebViewAndLoad();
        } else {
            mapStatusTv.setText("نقشه آپلود نشده — تحلیل عادی بدون محدوده");
            noMapPanel.setVisibility(View.VISIBLE);
            if (web != null) {
                mapBox.removeView(web);
                try { web.destroy(); } catch (Throwable ignored) {}
                web = null;
                mapReady = false;
            }
        }
    }

    private void pickMapFile() {
        if (!LicenseStore.INSTANCE.isPremium(this)) {
            Toast.makeText(this, "آپلود نقشه فقط برای اشتراک پرمیوم است", Toast.LENGTH_LONG).show();
            return;
        }
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        try {
            startActivityForResult(i, REQ_PICK_MAP);
        } catch (Exception e) {
            Toast.makeText(this, "انتخاب فایل در دسترس نیست", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_PICK_MAP || resultCode != Activity.RESULT_OK || data == null) return;
        final Uri uri = data.getData();
        if (uri == null) return;
        Toast.makeText(this, "در حال ذخیره نقشه…", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                String display = queryDisplayName(uri);
                if (display == null || display.isEmpty()) display = "map.mbtiles";
                File dir = new File(getFilesDir(), "zone_map");
                if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("mkdir zone_map");
                copyWebAssets(dir);
                File dest = ActivityZoneStore.INSTANCE.mapFile(this);
                try (InputStream in = getContentResolver().openInputStream(uri);
                     OutputStream out = new FileOutputStream(dest)) {
                    if (in == null) throw new IllegalStateException("باز نشدن فایل");
                    byte[] buf = new byte[8192];
                    int n;
                    long total = 0;
                    while ((n = in.read(buf)) >= 0) {
                        out.write(buf, 0, n);
                        total += n;
                    }
                    out.flush();
                    if (total < 100_000L) {
                        //noinspection ResultOfMethodCallIgnored
                        dest.delete();
                        throw new IllegalStateException("فایل خیلی کوچک است");
                    }
                }
                final String finalName = display;
                ActivityZoneStore.INSTANCE.setMapDisplayName(this, finalName);
                runOnUiThread(() -> {
                    Toast.makeText(this, "نقشه ذخیره شد", Toast.LENGTH_SHORT).show();
                    refreshMapUi();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "خطای آپلود: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private String queryDisplayName(Uri uri) {
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return c.getString(idx);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void copyWebAssets(File dir) throws Exception {
        String[] files = {"index.html", "maplibre-gl.js", "maplibre-gl.css", "sql-wasm.js", "sql-wasm.wasm"};
        for (String name : files) {
            File dest = new File(dir, name);
            try (InputStream in = getAssets().open("zone_map/" + name);
                 OutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
                out.flush();
            }
        }
    }

    private void ensureWebViewAndLoad() {
        if (!ActivityZoneStore.INSTANCE.hasMapFile(this)) return;
        try {
            File dir = new File(getFilesDir(), "zone_map");
            copyWebAssets(dir);
            if (web == null) {
                web = new WebView(this);
                setupWebView(web);
                mapBox.addView(web, 0, new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
            }
            mapReady = false;
            web.loadUrl("https://" + VHOST + "/index.html");
        } catch (Exception e) {
            Toast.makeText(this, "نمایش نقشه: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void setupWebView(WebView w) {
        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        try {
            s.setAllowFileAccessFromFileURLs(true);
            s.setAllowUniversalAccessFromFileURLs(true);
        } catch (Throwable ignored) {}
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        w.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        w.addJavascriptInterface(new Bridge(), "RaidoZone");
        w.setWebViewClient(new WebViewClient() {
            @Override
            public android.webkit.WebResourceResponse shouldInterceptRequest(
                    WebView view, android.webkit.WebResourceRequest req) {
                Uri u = req.getUrl();
                if (u == null || !VHOST.equals(u.getHost())) {
                    // آفلاین: هر درخواست بیرونی مسدود
                    return new android.webkit.WebResourceResponse("text/plain", "utf-8", 403, "Blocked",
                            new java.util.HashMap<String, String>(), new java.io.ByteArrayInputStream(new byte[0]));
                }
                try {
                    String name = u.getLastPathSegment();
                    if (name == null || name.isEmpty()) name = "index.html";
                    File dir = new File(getFilesDir(), "zone_map");
                    File f = new File(dir, name);
                    if (!f.getCanonicalPath().startsWith(dir.getCanonicalPath()) || !f.isFile()) {
                        return new android.webkit.WebResourceResponse("text/plain", "utf-8", 404, "Not Found",
                                new java.util.HashMap<String, String>(), new java.io.ByteArrayInputStream(new byte[0]));
                    }
                    String mime = "application/octet-stream";
                    if (name.endsWith(".html")) mime = "text/html";
                    else if (name.endsWith(".js")) mime = "application/javascript";
                    else if (name.endsWith(".css")) mime = "text/css";
                    else if (name.endsWith(".wasm")) mime = "application/wasm";
                    java.util.HashMap<String, String> h = new java.util.HashMap<>();
                    h.put("Access-Control-Allow-Origin", "*");
                    android.webkit.WebResourceResponse r = new android.webkit.WebResourceResponse(
                            mime, mime.startsWith("text/") || mime.endsWith("javascript") ? "utf-8" : null,
                            new java.io.FileInputStream(f));
                    r.setStatusCodeAndReasonPhrase(200, "OK");
                    r.setResponseHeaders(h);
                    return r;
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mapReady = true;
                view.evaluateJavascript(
                        "window.RaidoMapApi && RaidoMapApi.setCenter(" +
                                centerLat + "," + centerLng + "," + radiusKm + ")", null);
            }
        });
    }

    private final class Bridge {
        @JavascriptInterface
        public void onCenterChanged(final double lat, final double lng, final double r) {
            runOnUiThread(() -> {
                centerLat = lat;
                centerLng = lng;
                if (r >= 3 && r <= 8) {
                    radiusKm = (int) Math.round(r);
                    radiusSeek.setProgress(radiusKm - 3);
                    radiusLabel.setText("شعاع: " + radiusKm + " کیلومتر");
                }
                centerLabel.setText(String.format(java.util.Locale.US, "مرکز: %.4f ، %.4f", centerLat, centerLng));
            });
        }
    }

    private void requestGps() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOC);
            return;
        }
        applyGps();
    }

    private void applyGps() {
        try {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (lm == null) return;
            Location best = null;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location gps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Location net = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                best = gps;
                if (net != null && (best == null || net.getTime() > best.getTime())) best = net;
            }
            if (best == null) {
                Toast.makeText(this, "موقعیت پیدا نشد", Toast.LENGTH_LONG).show();
                return;
            }
            centerLat = best.getLatitude();
            centerLng = best.getLongitude();
            centerLabel.setText(String.format(java.util.Locale.US, "مرکز GPS: %.4f ، %.4f", centerLat, centerLng));
            if (mapReady && web != null) {
                web.evaluateJavascript(
                        "window.RaidoMapApi && RaidoMapApi.setCenter(" + centerLat + "," + centerLng + "," + radiusKm + ")",
                        null);
            }
            Toast.makeText(this, "موقعیت ثبت شد", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "خطای GPS", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            applyGps();
        }
    }

    private void saveZone() {
        boolean wantOn = enableSwitch.isChecked();
        if (wantOn) {
            if (!LicenseStore.INSTANCE.isPremium(this)) {
                Toast.makeText(this, "فعال‌سازی محدوده فقط برای پرمیوم است", Toast.LENGTH_LONG).show();
                return;
            }
            if (!ActivityZoneStore.INSTANCE.hasMapFile(this)) {
                Toast.makeText(this, "اول نقشه را آپلود کنید", Toast.LENGTH_LONG).show();
                return;
            }
            if (!ActivityZoneStore.INSTANCE.hasPoint(this)
                    && (Math.abs(centerLat) < 0.1 || Math.abs(centerLng) < 0.1)) {
                Toast.makeText(this, "مرکز را با GPS یا لمس نقشه مشخص کنید", Toast.LENGTH_LONG).show();
                return;
            }
        }
        ActivityZoneStore.INSTANCE.setEnabled(this, wantOn);
        ActivityZoneStore.INSTANCE.setStrictRed(this, strictSwitch.isChecked());
        if (Math.abs(centerLat) > 0.1 && Math.abs(centerLng) > 0.1) {
            ActivityZoneStore.INSTANCE.saveMapCenter(this, centerLat, centerLng, radiusKm, "map_or_gps");
        }
        Toast.makeText(this,
                wantOn ? ("ذخیره شد · شعاع " + radiusKm + " کم") : "ذخیره شد · محدوده خاموش — تحلیل عادی",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private void stylePrimary(Button b) {
        b.setBackgroundColor(0xFF0F766E);
        b.setTextColor(0xFFFFFFFF);
        b.setTextSize(15);
    }

    private void styleSecondary(Button b) {
        b.setBackgroundColor(0xFFE2E8F0);
        b.setTextColor(0xFF0F172A);
        b.setTextSize(14);
    }

    private int dp(int v) {
        return Math.round(getResources().getDisplayMetrics().density * v);
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            try { web.destroy(); } catch (Throwable ignored) {}
            web = null;
        }
        super.onDestroy();
    }
}
