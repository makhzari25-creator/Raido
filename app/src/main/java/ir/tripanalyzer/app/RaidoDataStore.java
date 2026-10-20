package ir.tripanalyzer.app;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ذخیرهٔ سبک JSON فقط برای تاریخچهٔ تحلیل و آمار هفته.
 * تنظیمات اپ همچنان در Store/SharedPreferences است.
 * هر خطا بلعیده می‌شود تا مسیر اصلی تحلیل قطع نشود.
 */
public final class RaidoDataStore {
    public static final RaidoDataStore INSTANCE = new RaidoDataStore();
    private static final String FILE_NAME = "raido_data.json";
    private static final int MAX_ANALYSES_SAFETY = 100_000;
    private static final long RETENTION_MS = 90L * 24 * 60 * 60 * 1000;
    private static final int MARKET_MIN_SAMPLES = 3;
    private static final long WEEK_MS = 7L * 24 * 60 * 60 * 1000;
    private static final long MONTH_MS = 30L * 24 * 60 * 60 * 1000;

    private RaidoDataStore() {}

    private File file(Context c) {
        return new File(c.getApplicationContext().getFilesDir(), FILE_NAME);
    }

    private synchronized JSONObject readRoot(Context c) {
        try {
            File f = file(c);
            if (!f.exists() || f.length() == 0) {
                JSONObject o = new JSONObject();
                o.put("analyses", new JSONArray());
                o.put("meta", new JSONObject());
                return o;
            }
            byte[] buf = new byte[(int) f.length()];
            FileInputStream in = new FileInputStream(f);
            int n = in.read(buf);
            in.close();
            if (n <= 0) {
                JSONObject o = new JSONObject();
                o.put("analyses", new JSONArray());
                o.put("meta", new JSONObject());
                return o;
            }
            return new JSONObject(new String(buf, 0, n, StandardCharsets.UTF_8));
        } catch (Exception e) {
            try {
                JSONObject o = new JSONObject();
                o.put("analyses", new JSONArray());
                o.put("meta", new JSONObject());
                return o;
            } catch (Exception e2) {
                return new JSONObject();
            }
        }
    }

    private synchronized void writeRoot(Context c, JSONObject root) {
        try {
            File f = file(c);
            FileOutputStream out = new FileOutputStream(f);
            out.write(root.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        } catch (Exception ignored) {}
    }

    private JSONObject buildRow(JSONObject recordFromStore) throws Exception {
            JSONObject row = new JSONObject();
            row.put("ts", recordFromStore.optLong("ts", System.currentTimeMillis()));
            row.put("verdict", recordFromStore.optString("verdict", "GRAY"));
            if (recordFromStore.has("fare") && !recordFromStore.isNull("fare")) {
                row.put("fare", recordFromStore.optDouble("fare"));
            }
            if (recordFromStore.has("hourly") && !recordFromStore.isNull("hourly")) {
                row.put("hourly", recordFromStore.optDouble("hourly"));
            }
            if (recordFromStore.has("netAfter") && !recordFromStore.isNull("netAfter")) {
                row.put("netAfter", recordFromStore.optDouble("netAfter"));
            }
            if (recordFromStore.has("net") && !recordFromStore.isNull("net")) {
                row.put("net", recordFromStore.optDouble("net"));
            } else if (recordFromStore.has("netAfter") && !recordFromStore.isNull("netAfter")) {
                row.put("net", recordFromStore.optDouble("netAfter"));
            }
            if (recordFromStore.has("fuel") && !recordFromStore.isNull("fuel")) {
                row.put("fuel", recordFromStore.optDouble("fuel"));
            }
            if (recordFromStore.has("dep") && !recordFromStore.isNull("dep")) {
                row.put("dep", recordFromStore.optDouble("dep"));
            }
            if (recordFromStore.has("dist") && !recordFromStore.isNull("dist")) {
                row.put("dist", recordFromStore.optDouble("dist"));
            }
            if (recordFromStore.has("rawTime") && !recordFromStore.isNull("rawTime")) {
                row.put("rawTime", recordFromStore.optDouble("rawTime"));
            }
            if (recordFromStore.has("origin")) row.put("origin", recordFromStore.optString("origin"));
            if (recordFromStore.has("dest")) row.put("dest", recordFromStore.optString("dest"));
            if (recordFromStore.has("activityZoneStatus")) {
                row.put("activityZoneStatus", recordFromStore.optString("activityZoneStatus"));
            }
            row.put("accepted", false);
        return row;
    }

    /** اگر ردیف قبلی با همین ts هست جایگزین می‌شود (کیلومتر واقعی رسید)، وگرنه اضافه. شمارنده دوباره زیاد نمی‌شود. */
    public void upsertAnalysis(Context c, JSONObject rec, long oldTs) {
        if (c == null || rec == null) return;
        try {
            JSONObject root = readRoot(c);
            JSONArray arr = root.optJSONArray("analyses");
            if (arr != null && oldTs > 0) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject old = arr.optJSONObject(i);
                    if (old != null && old.optLong("ts", -1) == oldTs) {
                        JSONObject row = buildRow(rec);
                        row.put("ts", oldTs);
                        row.put("accepted", old.optBoolean("accepted", false));
                        arr.put(i, row);
                        root.put("analyses", arr);
                        writeRoot(c, root);
                        return;
                    }
                }
            }
        } catch (Exception ignored) {}
        appendAnalysis(c, rec);
    }

    public void appendAnalysis(Context c, JSONObject recordFromStore) {
        if (c == null || recordFromStore == null) return;
        UsageCounter.onAnalysis(c);
        try {
            JSONObject root = readRoot(c);
            JSONArray arr = root.optJSONArray("analyses");
            if (arr == null) arr = new JSONArray();

            JSONObject row = buildRow(recordFromStore);

            long cutoff = System.currentTimeMillis() - RETENTION_MS;
            JSONArray next = new JSONArray();
            next.put(row);
            for (int i = 0; i < arr.length() && next.length() < MAX_ANALYSES_SAFETY; i++) {
                JSONObject old = arr.optJSONObject(i);
                if (old == null) continue;
                long ts = old.optLong("ts", 0);
                if (ts <= 0 || ts < cutoff) continue;
                next.put(old);
            }
            root.put("analyses", next);
            writeRoot(c, root);
        } catch (Exception ignored) {}
    }

    public void markAccepted(Context c, long ts) {
        if (c == null || ts <= 0) return;
        UsageCounter.onAccepted(c, ts);
        try {
            JSONObject root = readRoot(c);
            JSONArray arr = root.optJSONArray("analyses");
            if (arr == null) return;
            boolean found = false;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null && o.optLong("ts", -1) == ts) {
                    o.put("accepted", true);
                    o.put("acceptedAt", System.currentTimeMillis());
                    found = true;
                    break;
                }
            }
            if (found) writeRoot(c, root);
        } catch (Exception ignored) {}
    }

    public JSONObject findAnalysisByTs(Context c, long ts) {
        if (c == null || ts <= 0) return null;
        try {
            JSONArray arr = analysesArray(c);
            if (arr == null) return null;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null && o.optLong("ts", -1) == ts) {
                    return new JSONObject(o.toString());
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public org.json.JSONArray analysesLast7Days(Context c) {
        org.json.JSONArray out = new org.json.JSONArray();
        if (c == null) return out;
        try {
            long cutoff = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000;
            JSONArray arr = analysesArray(c);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                long ts = o.optLong("ts", 0);
                if (ts <= 0 || ts < cutoff) continue;
                out.put(o);
            }
        } catch (Exception ignored) {}
        return out;
    }

    public boolean isAccepted(Context c, long ts) {
        if (c == null || ts <= 0) return false;
        try {
            JSONArray arr = analysesArray(c);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null && o.optLong("ts", -1) == ts) {
                    return o.optBoolean("accepted", false);
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public void incrementWrongReports(Context c) {
        if (c == null) return;
        try {
            JSONObject root = readRoot(c);
            JSONObject meta = root.optJSONObject("meta");
            if (meta == null) meta = new JSONObject();
            meta.put("wrongReports", meta.optInt("wrongReports", 0) + 1);
            meta.put("wrongReportLastAt", System.currentTimeMillis());
            root.put("meta", meta);
            writeRoot(c, root);
        } catch (Exception ignored) {}
    }

    public int getWrongReports(Context c) {
        try {
            JSONObject meta = readRoot(c).optJSONObject("meta");
            if (meta == null) return 0;
            return meta.optInt("wrongReports", 0);
        } catch (Exception e) {
            return 0;
        }
    }

    public WeeklyStats computeWeekly(Context c) {
        return computeSince(c, WEEK_MS);
    }

    public WeeklyStats computeMonthly(Context c) {
        return computeSince(c, MONTH_MS);
    }

    private WeeklyStats computeSince(Context c, long windowMs) {
        WeeklyStats s = new WeeklyStats();
        if (c == null) return s;
        try {
            long now = System.currentTimeMillis();
            long from = now - windowMs;
            JSONArray arr = readRoot(c).optJSONArray("analyses");
            if (arr == null) arr = new JSONArray();
            double hourlySum = 0;
            int hourlyN = 0;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                long ts = o.optLong("ts", 0);
                if (ts < from) continue;
                s.total++;
                String v = o.optString("verdict", "");
                if ("GREEN".equals(v)) s.green++;
                else if ("YELLOW".equals(v)) s.yellow++;
                else if ("RED".equals(v)) s.red++;
                if (o.optBoolean("accepted", false)) {
                    s.accepted++;
                    if (o.has("hourly") && !o.isNull("hourly")) {
                        hourlySum += o.optDouble("hourly", 0);
                        hourlyN++;
                    }
                }
            }
            s.avgHourlyAccepted = hourlyN > 0 ? hourlySum / hourlyN : 0;
            s.wrongReports = getWrongReports(c);
            if (s.wrongReports == 0) {
                try {
                    s.wrongReports = Store.INSTANCE.prefs(c).getInt("wrong_report_count", 0);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return s;
    }

    public static final class WeeklyStats {
        public int total;
        public int green;
        public int yellow;
        public int red;
        public int accepted;
        public double avgHourlyAccepted;
        public int wrongReports;
    }

    private JSONArray analysesArray(Context c) {
        try {
            JSONArray arr = readRoot(c).optJSONArray("analyses");
            return arr != null ? arr : new JSONArray();
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private JSONArray analysesRetained(Context c) {
        JSONArray arr = analysesArray(c);
        JSONArray out = new JSONArray();
        long cutoff = System.currentTimeMillis() - RETENTION_MS;
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                long ts = o.optLong("ts", 0);
                if (ts <= 0 || ts < cutoff) continue;
                out.put(o);
            }
        } catch (Exception ignored) {}
        return out;
    }

    public DashboardHourly snapshotDashboard(Context c) {
        DashboardHourly d = new DashboardHourly();
        if (c == null) return d;
        try {
            int[] now = Jalali.nowJalali();
            long nowMs = System.currentTimeMillis();
            int hourNow = Jalali.hourOfDay(nowMs);
            JSONArray arr = analysesRetained(c);
            double sumHour = 0, sumDay = 0;
            int nHour = 0, nDay = 0;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null || !o.has("hourly") || o.isNull("hourly")) continue;
                long ts = o.optLong("ts", 0);
                if (ts <= 0) continue;
                if (!Jalali.sameJalaliMonth(ts, now[0], now[1])) continue;
                if (!Jalali.sameJalaliDay(ts, now[0], now[1], now[2])) continue;
                double h = o.optDouble("hourly", 0);
                if (h <= 0) continue;
                sumDay += h;
                nDay++;
                if (Jalali.hourOfDay(ts) == hourNow) {
                    sumHour += h;
                    nHour++;
                }
            }
            d.currentHour = hourNow;
            d.hourAvg = nHour > 0 ? sumHour / nHour : 0;
            d.hourCount = nHour;
            d.dayAvg = nDay > 0 ? sumDay / nDay : 0;
            d.dayCount = nDay;
            d.jy = now[0];
            d.jm = now[1];
            d.jd = now[2];
        } catch (Exception ignored) {}
        return d;
    }

    public List<DaySummary> listDaysInCurrentMonth(Context c) {
        List<DaySummary> out = new ArrayList<>();
        if (c == null) return out;
        try {
            int[] now = Jalali.nowJalali();
            JSONArray arr = analysesRetained(c);
            Map<String, DaySummary> map = new LinkedHashMap<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null || !o.has("hourly") || o.isNull("hourly")) continue;
                long ts = o.optLong("ts", 0);
                if (ts <= 0) continue;
                if (!Jalali.sameJalaliMonth(ts, now[0], now[1])) continue;
                double h = o.optDouble("hourly", 0);
                if (h <= 0) continue;
                int[] j = Jalali.toJalali(ts);
                String key = j[0] + "-" + j[1] + "-" + j[2];
                DaySummary ds = map.get(key);
                if (ds == null) {
                    ds = new DaySummary();
                    ds.jy = j[0];
                    ds.jm = j[1];
                    ds.jd = j[2];
                    ds.label = Jalali.weekdayName(ts) + " " + Jalali.formatDateShort(j[0], j[1], j[2]);
                    ds.anyTs = ts;
                    map.put(key, ds);
                }
                ds.sum += h;
                ds.count++;
                if (ts > ds.anyTs) ds.anyTs = ts;
            }
            out.addAll(map.values());
            Collections.sort(out, new Comparator<DaySummary>() {
                @Override public int compare(DaySummary a, DaySummary b) {
                    if (a.jy != b.jy) return b.jy - a.jy;
                    if (a.jm != b.jm) return b.jm - a.jm;
                    return b.jd - a.jd;
                }
            });
            for (DaySummary ds : out) {
                ds.avg = ds.count > 0 ? ds.sum / ds.count : 0;
            }
        } catch (Exception ignored) {}
        return out;
    }

    public HourBucket[] hoursForDay(Context c, int jy, int jm, int jd) {
        HourBucket[] buckets = new HourBucket[24];
        for (int i = 0; i < 24; i++) buckets[i] = new HourBucket(i);
        if (c == null) return buckets;
        try {
            JSONArray arr = analysesRetained(c);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null || !o.has("hourly") || o.isNull("hourly")) continue;
                long ts = o.optLong("ts", 0);
                if (ts <= 0) continue;
                if (!Jalali.sameJalaliDay(ts, jy, jm, jd)) continue;
                double h = o.optDouble("hourly", 0);
                if (h <= 0) continue;
                int hour = Jalali.hourOfDay(ts);
                if (hour < 0 || hour > 23) continue;
                buckets[hour].sum += h;
                buckets[hour].count++;
            }
            for (HourBucket b : buckets) {
                b.avg = b.count > 0 ? b.sum / b.count : 0;
            }
        } catch (Exception ignored) {}
        return buckets;
    }

    public MarketRef marketRefForHour(Context c, int hourOfDay) {
        MarketRef ref = new MarketRef();
        if (c == null || hourOfDay < 0 || hourOfDay > 23) return ref;
        try {
            int[] now = Jalali.nowJalali();
            JSONArray arr = analysesRetained(c);
            double sumToday = 0;
            int nToday = 0;
            double sumAll = 0;
            int nAll = 0;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null || !o.has("hourly") || o.isNull("hourly")) continue;
                long ts = o.optLong("ts", 0);
                if (ts <= 0) continue;
                double h = o.optDouble("hourly", 0);
                if (h <= 0) continue;
                if (Jalali.hourOfDay(ts) != hourOfDay) continue;
                sumAll += h;
                nAll++;
                if (Jalali.sameJalaliDay(ts, now[0], now[1], now[2])) {
                    sumToday += h;
                    nToday++;
                }
            }
            if (nToday >= MARKET_MIN_SAMPLES) {
                ref.avg = sumToday / nToday;
                ref.count = nToday;
                ref.scope = "today";
            } else if (nAll >= MARKET_MIN_SAMPLES) {
                ref.avg = sumAll / nAll;
                ref.count = nAll;
                ref.scope = "hour_history";
            } else {
                double sumN = 0; int nN = 0;
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o2 = arr.optJSONObject(i);
                    if (o2 == null || !o2.has("hourly") || o2.isNull("hourly")) continue;
                    long ts2 = o2.optLong("ts", 0);
                    if (ts2 <= 0) continue;
                    double h2 = o2.optDouble("hourly", 0);
                    if (h2 <= 0) continue;
                    int hr = Jalali.hourOfDay(ts2);
                    int diff = Math.abs(hr - hourOfDay);
                    if (diff > 12) diff = 24 - diff;
                    if (diff >= 1 && diff <= 2) { sumN += h2; nN++; }
                }
                if (nN >= MARKET_MIN_SAMPLES) {
                    ref.avg = sumN / nN;
                    ref.count = nN;
                    ref.scope = "neighbor_hours";
                } else {
                    ref.avg = nAll > 0 ? sumAll / nAll : 0;
                    ref.count = nAll;
                    ref.scope = "insufficient";
                }
            }
        } catch (Exception ignored) {}
        return ref;
    }

    public MarketRef marketRefNow(Context c) {
        return marketRefForHour(c, Jalali.hourOfDay(System.currentTimeMillis()));
    }

    public float[] hourAvgsArray(Context c, int jy, int jm, int jd) {
        HourBucket[] b = hoursForDay(c, jy, jm, jd);
        float[] out = new float[24];
        for (int i = 0; i < 24; i++) out[i] = (float) b[i].avg;
        return out;
    }

    public int[] last7DayCounts(Context c) {
        int[] counts = new int[7];
        if (c == null) return counts;
        try {
            long now = System.currentTimeMillis();
            JSONArray arr = analysesRetained(c);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                long ts = o.optLong("ts", 0);
                if (ts <= 0) continue;
                int dayIndex = (int) ((now - ts) / (24L * 60 * 60 * 1000));
                if (dayIndex >= 0 && dayIndex < 7) counts[6 - dayIndex]++;
            }
        } catch (Exception ignored) {}
        return counts;
    }

    public float[] last7DayHourlyAvgs(Context c) {
        float[] avgs = new float[7];
        if (c == null) return avgs;
        try {
            long now = System.currentTimeMillis();
            JSONArray arr = analysesRetained(c);
            double[] sums = new double[7];
            int[] ns = new int[7];
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null || !o.has("hourly") || o.isNull("hourly")) continue;
                long ts = o.optLong("ts", 0);
                if (ts <= 0) continue;
                double h = o.optDouble("hourly", 0);
                if (h <= 0) continue;
                int dayIndex = (int) ((now - ts) / (24L * 60 * 60 * 1000));
                if (dayIndex >= 0 && dayIndex < 7) {
                    sums[6 - dayIndex] += h;
                    ns[6 - dayIndex]++;
                }
            }
            for (int i = 0; i < 7; i++) {
                avgs[i] = ns[i] > 0 ? (float) (sums[i] / ns[i]) : 0f;
            }
        } catch (Exception ignored) {}
        return avgs;
    }

    public AcceptedAvg acceptedHourlyAvg(Context c) {
        AcceptedAvg a = new AcceptedAvg();
        if (c == null) return a;
        try {
            JSONArray arr = analysesRetained(c);
            double sum = 0;
            int n = 0;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                if (!o.optBoolean("accepted", false)) continue;
                if (!o.has("hourly") || o.isNull("hourly")) continue;
                double h = o.optDouble("hourly", 0);
                if (h <= 0) continue;
                sum += h;
                n++;
            }
            a.count = n;
            a.avg = n > 0 ? sum / n : 0;
        } catch (Exception ignored) {}
        return a;
    }

    public static final class AcceptedAvg {
        public double avg;
        public int count;
    }

    public static final class DashboardHourly {
        public int currentHour;
        public double hourAvg;
        public int hourCount;
        public double dayAvg;
        public int dayCount;
        public int jy, jm, jd;
    }

    public static final class DaySummary {
        public int jy, jm, jd;
        public String label = "";
        public double sum, avg;
        public int count;
        public long anyTs;
    }

    public static final class HourBucket {
        public final int hour;
        public double sum, avg;
        public int count;
        public HourBucket(int hour) { this.hour = hour; }
        public String rangeLabel() {
            int h2 = hour == 23 ? 24 : hour + 1;
            return String.format(Locale.US, "%02d تا %02d", hour, h2);
        }
    }

    public static final class MarketRef {
        public double avg;
        public int count;
        public String scope = "insufficient";
        public boolean isValid() { return count >= MARKET_MIN_SAMPLES && avg > 0; }
    }
}
