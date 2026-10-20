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
        root.setBackgroundColor(0xFFF1F5F9);

        // ── هدر: تصویر جاده + گرادیان آبی رایدو ──
        FrameLayout head = new FrameLayout(this);
        android.widget.ImageView road = new android.widget.ImageView(this);
        road.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        try { road.setImageResource(R.drawable.bg_road); } catch (Throwable ignored) {}
        head.addView(road, new FrameLayout.LayoutParams(-1, -1));
        View shade = new View(this);
        shade.setBackground(new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x990F172A, 0xE61E3A8A}));
        head.addView(shade, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout headIn = new LinearLayout(this);
        headIn.setOrientation(LinearLayout.VERTICAL);
        headIn.setPadding(dp(18), dp(16), dp(18), dp(18));
        TextView back = new TextView(this);
        back.setText("→");
        back.setTextColor(0xFFFFFFFF);
        back.setTextSize(20);
        back.setGravity(android.view.Gravity.CENTER);
        back.setBackground(shape(0x33FFFFFF, 99, 0x55FFFFFF));
        back.setOnClickListener(v -> finish());
        headIn.addView(back, new LinearLayout.LayoutParams(dp(40), dp(40)));
        TextView title = new TextView(this);
        title.setText("محدوده فعالیت");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(14), 0, 0);
        headIn.addView(title);
        TextView sub = new TextView(this);
        sub.setText("تعیین کن رایدو فقط در چه منطقه‌ای برایت تحلیل کند");
        sub.setTextColor(0xE6FFFFFF);
        sub.setTextSize(13.5f);
        sub.setPadding(0, dp(6), 0, 0);
        headIn.addView(sub);
        head.addView(headIn, new FrameLayout.LayoutParams(-1, -2));
        root.addView(head, new LinearLayout.LayoutParams(-1, dp(168)));

        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(14), dp(14), dp(14));
        scroll.addView(body);

        // ── کارت نقشه ──
        LinearLayout mapCard = card();
        mapBox = new FrameLayout(this);
        mapBox.setBackground(shape(0xFFE2E8F0, 16, 0));
        mapBox.setClipToOutline(true);
        mapBox.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override public void getOutline(View v, android.graphics.Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), dp(16));
            }
        });
        noMapPanel = new LinearLayout(this);
        noMapPanel.setOrientation(LinearLayout.VERTICAL);
        noMapPanel.setGravity(android.view.Gravity.CENTER);
        noMapPanel.setPadding(dp(20), dp(24), dp(20), dp(24));
        noMapPanel.setBackgroundColor(0xFFE8EEF7);
        TextView noTitle = new TextView(this);
        noTitle.setText("نقشه نصب نشده");
        noTitle.setTextColor(0xFF0F172A);
        noTitle.setTextSize(16);
        noTitle.setTypeface(Typeface.DEFAULT_BOLD);
        noTitle.setGravity(android.view.Gravity.CENTER);
        TextView noBody = new TextView(this);
        noBody.setText("فایل نقشه استان (.mbtiles) را از پشتیبانی بگیر و آپلود کن.\nتا قبل از آپلود، محدوده روی تحلیل اثر ندارد.");
        noBody.setTextColor(0xFF475569);
        noBody.setTextSize(13);
        noBody.setGravity(android.view.Gravity.CENTER);
        noBody.setLineSpacing(dp(3), 1f);
        noBody.setPadding(0, dp(8), 0, 0);
        noMapPanel.addView(noTitle);
        noMapPanel.addView(noBody);
        mapBox.addView(noMapPanel, new FrameLayout.LayoutParams(-1, -1));
        mapCard.addView(mapBox, new LinearLayout.LayoutParams(-1, dp(250)));

        mapStatusTv = new TextView(this);
        mapStatusTv.setTextColor(0xFF1D4ED8);
        mapStatusTv.setTextSize(12.5f);
        mapStatusTv.setPadding(0, dp(10), 0, dp(8));
        mapCard.addView(mapStatusTv);

        Button uploadBtn = new Button(this);
        uploadBtn.setText("آپلود نقشه (.mbtiles)");
        uploadBtn.setAllCaps(false);
        stylePrimary(uploadBtn);
        uploadBtn.setOnClickListener(v -> pickMapFile());
        mapCard.addView(uploadBtn, new LinearLayout.LayoutParams(-1, dp(48)));
        body.addView(mapCard);

        // ── نکتهٔ مهم ──
        LinearLayout tip = new LinearLayout(this);
        tip.setOrientation(LinearLayout.VERTICAL);
        tip.setPadding(dp(16), dp(14), dp(16), dp(14));
        tip.setBackground(shape(0xFFEFF6FF, 18, 0xFFBFDBFE));
        TextView tipT = new TextView(this);
        tipT.setText("نکتهٔ مهم");
        tipT.setTextColor(0xFF1D4ED8);
        tipT.setTextSize(15);
        tipT.setTypeface(Typeface.DEFAULT_BOLD);
        TextView tipB = new TextView(this);
        tipB.setText("در صورت فعال بودن این گزینه، رایدو فقط در محدودهٔ مشخص‌شده فعال است و بیرون از آن به حالت غیرفعال درمی‌آید. بدون نقشه، تحلیل مثل قبل کار می‌کند.");
        tipB.setTextColor(0xFF334155);
        tipB.setTextSize(13);
        tipB.setLineSpacing(dp(3), 1f);
        tipB.setPadding(0, dp(4), 0, 0);
        tip.addView(tipT);
        tip.addView(tipB);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-1, -2);
        tlp.bottomMargin = dp(12);
        body.addView(tip, tlp);

        // ── تنظیمات محدودیت ──
        LinearLayout setCard = card();
        TextView setT = new TextView(this);
        setT.setText("تنظیمات محدودیت");
        setT.setTextColor(0xFF0F172A);
        setT.setTextSize(16);
        setT.setTypeface(Typeface.DEFAULT_BOLD);
        setT.setPadding(0, 0, 0, dp(8));
        setCard.addView(setT);

        enableSwitch = new Switch(this);
        enableSwitch.setChecked(ActivityZoneStore.INSTANCE.isEnabled(this));
        setCard.addView(switchRow("فعال‌سازی محدودهٔ فعالیت", "فقط با پرمیوم + نقشه + مرکز؛ وگرنه تحلیل عادی قبلی", 0xFFDBEAFE, enableSwitch));
        strictSwitch = new Switch(this);
        strictSwitch.setChecked(ActivityZoneStore.INSTANCE.isStrictRed(this));
        setCard.addView(switchRow("خارج محدوده = همیشه قرمز", "در صورت خروج از محدوده، وضعیت همیشه قرمز باشد", 0xFFFEE2E2, strictSwitch));
        body.addView(setCard);

        // ── شعاع و موقعیت ──
        LinearLayout radCard = card();
        radiusLabel = new TextView(this);
        radiusLabel.setText("شعاع: " + radiusKm + " کیلومتر");
        radiusLabel.setTextColor(0xFF1D4ED8);
        radiusLabel.setTextSize(16);
        radiusLabel.setTypeface(Typeface.DEFAULT_BOLD);
        radCard.addView(radiusLabel);

        LinearLayout sr = new LinearLayout(this);
        sr.setOrientation(LinearLayout.HORIZONTAL);
        sr.setGravity(android.view.Gravity.CENTER_VERTICAL);
        sr.setPadding(0, dp(8), 0, dp(4));
        radiusSeek = new SeekBar(this);
        radiusSeek.setMax(5);
        radiusSeek.setProgress(radiusKm - 3);
        try {
            radiusSeek.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF2563EB));
            radiusSeek.setThumbTintList(android.content.res.ColorStateList.valueOf(0xFF2563EB));
        } catch (Throwable ignored) {}
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
        TextView minus = roundBtn("−");
        minus.setOnClickListener(v -> radiusSeek.setProgress(Math.max(0, radiusSeek.getProgress() - 1)));
        TextView plus = roundBtn("+");
        plus.setOnClickListener(v -> radiusSeek.setProgress(Math.min(radiusSeek.getMax(), radiusSeek.getProgress() + 1)));
        sr.addView(plus, new LinearLayout.LayoutParams(dp(40), dp(40)));
        sr.addView(radiusSeek, new LinearLayout.LayoutParams(0, -2, 1f));
        sr.addView(minus, new LinearLayout.LayoutParams(dp(40), dp(40)));
        radCard.addView(sr);

        TextView radHint = new TextView(this);
        radHint.setText("محدودهٔ مجاز فعالیت از موقعیت مرکز (۳ تا ۸ کیلومتر)");
        radHint.setTextColor(0xFF64748B);
        radHint.setTextSize(12.5f);
        radCard.addView(radHint);

        centerLabel = new TextView(this);
        centerLabel.setText(String.format(java.util.Locale.US, "مرکز: %.4f ، %.4f", centerLat, centerLng));
        centerLabel.setTextColor(0xFF334155);
        centerLabel.setTextSize(13);
        centerLabel.setTextDirection(View.TEXT_DIRECTION_LTR);
        centerLabel.setGravity(android.view.Gravity.CENTER);
        centerLabel.setBackground(shape(0xFFF8FAFC, 14, 0xFFE2E8F0));
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-1, dp(46));
        clp.topMargin = dp(12);
        radCard.addView(centerLabel, clp);

        Button gpsBtn = new Button(this);
        gpsBtn.setText("موقعیت من");
        gpsBtn.setAllCaps(false);
        styleSecondary(gpsBtn);
        gpsBtn.setOnClickListener(v -> requestGps());
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(-1, dp(46));
        gp.topMargin = dp(8);
        radCard.addView(gpsBtn, gp);
        body.addView(radCard);

        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        // ── نوار پایین: لغو / تأیید ──
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setPadding(dp(14), dp(10), dp(14), dp(12));
        bar.setBackgroundColor(0xFFFFFFFF);
        Button saveBtn = new Button(this);
        saveBtn.setText("تأیید محدوده");
        saveBtn.setAllCaps(false);
        stylePrimary(saveBtn);
        saveBtn.setOnClickListener(v -> saveZone());
        LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(0, dp(50), 1.6f);
        sbp.setMarginEnd(dp(10));
        bar.addView(saveBtn, sbp);
        Button cancelBtn = new Button(this);
        cancelBtn.setText("لغو");
        cancelBtn.setAllCaps(false);
        styleSecondary(cancelBtn);
        cancelBtn.setOnClickListener(v -> finish());
        bar.addView(cancelBtn, new LinearLayout.LayoutParams(0, dp(50), 1f));
        root.addView(bar);
        setContentView(root);
        try { Fonts.INSTANCE.applyRecursive(root); } catch (Throwable ignored) {}

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
        w.setOnTouchListener((v, ev) -> { if (v.getParent() != null) v.getParent().requestDisallowInterceptTouchEvent(true); return false; });
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

    private android.graphics.drawable.GradientDrawable shape(int fill, int radiusDp, int stroke) {
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        if (stroke != 0) g.setStroke(Math.max(1, dp(1)), stroke);
        return g;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14), dp(14), dp(14), dp(14));
        c.setBackground(shape(0xFFFFFFFF, 20, 0xFFE2E8F0));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(12);
        c.setLayoutParams(lp);
        return c;
    }

    private TextView roundBtn(String t) {
        TextView v = new TextView(this);
        v.setText(t);
        v.setTextSize(20);
        v.setTextColor(0xFF1D4ED8);
        v.setGravity(android.view.Gravity.CENTER);
        v.setBackground(shape(0xFFEFF6FF, 99, 0xFFBFDBFE));
        return v;
    }

    private LinearLayout switchRow(String title, String desc, int tint, Switch sw) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(12), dp(12), dp(12));
        row.setBackground(shape(0xFFF8FAFC, 16, 0xFFE2E8F0));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-1, -2);
        rlp.topMargin = dp(8);
        row.setLayoutParams(rlp);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(0xFF0F172A);
        t.setTextSize(14.5f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        TextView d = new TextView(this);
        d.setText(desc);
        d.setTextColor(0xFF64748B);
        d.setTextSize(12);
        d.setPadding(0, dp(2), 0, 0);
        col.addView(t);
        col.addView(d);
        row.addView(col, new LinearLayout.LayoutParams(0, -2, 1f));
        try {
            sw.setThumbTintList(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
            sw.setTrackTintList(new android.content.res.ColorStateList(
                    new int[][]{{android.R.attr.state_checked}, {}},
                    new int[]{0xFF2563EB, 0xFFCBD5E1}));
        } catch (Throwable ignored) {}
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-2, -2);
        slp.setMarginStart(dp(10));
        row.addView(sw, slp);
        return row;
    }

    private void stylePrimary(Button b) {
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF3B82F6, 0xFF2563EB});
        g.setCornerRadius(dp(14));
        b.setBackground(g);
        b.setTextColor(0xFFFFFFFF);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setGravity(android.view.Gravity.CENTER);
    }

    private void styleSecondary(Button b) {
        b.setBackground(shape(0xFFFFFFFF, 14, 0xFFBFDBFE));
        b.setTextColor(0xFF1D4ED8);
        b.setTextSize(14);
        b.setGravity(android.view.Gravity.CENTER);
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
