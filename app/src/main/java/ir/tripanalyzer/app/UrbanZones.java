package ir.tripanalyzer.app;

import android.content.Context;
import android.location.Location;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * محدوده‌های شهری از assets/iran_urban_zones.json (مازندران، گیلان، گلستان، تهران، …)
 */
public final class UrbanZones {
    public static final UrbanZones INSTANCE = new UrbanZones();

    public static final class City {
        public final String id;
        public final String nameFa;
        public final double lat;
        public final double lng;
        public final double radiusKm;

        public City(String id, String nameFa, double lat, double lng, double radiusKm) {
            this.id = id;
            this.nameFa = nameFa;
            this.lat = lat;
            this.lng = lng;
            this.radiusKm = radiusKm;
        }

        public boolean contains(double lat, double lng) {
            float[] r = new float[1];
            Location.distanceBetween(this.lat, this.lng, lat, lng, r);
            return (r[0] / 1000.0) <= radiusKm;
        }

        public double distanceKm(double lat, double lng) {
            float[] r = new float[1];
            Location.distanceBetween(this.lat, this.lng, lat, lng, r);
            return r[0] / 1000.0;
        }
    }

    public static final class Nearest {
        public final City city;
        public final double distanceKm;
        public final double dOutKm; // max(0, dist - radius)

        public Nearest(City city, double distanceKm, double dOutKm) {
            this.city = city;
            this.distanceKm = distanceKm;
            this.dOutKm = dOutKm;
        }
    }

    private List<City> cities;
    private boolean loaded;

    private UrbanZones() {}

    public synchronized void ensureLoaded(Context ctx) {
        if (loaded) return;
        cities = new ArrayList<>();
        try {
            InputStream is = ctx.getAssets().open("iran_urban_zones.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            JSONObject root = new JSONObject(sb.toString());
            JSONArray arr = root.getJSONArray("cities");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                cities.add(new City(
                        o.optString("id", ""),
                        o.optString("name_fa", ""),
                        o.getDouble("lat"),
                        o.getDouble("lng"),
                        o.getDouble("radius_km")
                ));
            }
            loaded = true;
        } catch (Throwable t) {
            loaded = false;
            cities = new ArrayList<>();
        }
    }

    public List<City> all(Context ctx) {
        ensureLoaded(ctx);
        return cities;
    }

    public City findContaining(Context ctx, double lat, double lng) {
        ensureLoaded(ctx);
        City best = null;
        double bestDist = Double.MAX_VALUE;
        for (City c : cities) {
            if (c.contains(lat, lng)) {
                double d = c.distanceKm(lat, lng);
                if (d < bestDist) {
                    bestDist = d;
                    best = c;
                }
            }
        }
        return best;
    }

    public Nearest nearest(Context ctx, double lat, double lng) {
        ensureLoaded(ctx);
        // اولویت با کمترین فاصلهٔ خارج از شعاع (dOut)، بعد فاصله تا مرکز
        // تا حومهٔ یک شهر کوچک به شهر بزرگ‌تر دور نسبت داده نشود
        City best = null;
        double bestDout = Double.MAX_VALUE;
        double bestDist = Double.MAX_VALUE;
        for (City c : cities) {
            double d = c.distanceKm(lat, lng);
            double dout = Math.max(0, d - c.radiusKm);
            if (dout < bestDout - 1e-6 || (Math.abs(dout - bestDout) < 1e-6 && d < bestDist)) {
                bestDout = dout;
                bestDist = d;
                best = c;
            }
        }
        if (best == null) return null;
        return new Nearest(best, bestDist, bestDout);
    }
}
