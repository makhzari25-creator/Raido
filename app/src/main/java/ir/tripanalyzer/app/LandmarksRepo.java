package ir.tripanalyzer.app;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** بارگذاری mazandaran_landmarks.json از assets. */
public final class LandmarksRepo {
    public static final LandmarksRepo INSTANCE = new LandmarksRepo();

    public static final class Landmark {
        public final String name;
        public final double lat;
        public final double lng;

        public Landmark(String name, double lat, double lng) {
            this.name = name;
            this.lat = lat;
            this.lng = lng;
        }
    }

    public static final class City {
        public final String name;
        public final double centerLat;
        public final double centerLng;
        public final List<Landmark> landmarks;

        public City(String name, double centerLat, double centerLng, List<Landmark> landmarks) {
            this.name = name;
            this.centerLat = centerLat;
            this.centerLng = centerLng;
            this.landmarks = landmarks;
        }
    }

    private List<City> cache;

    private LandmarksRepo() {}

    public synchronized List<City> cities(Context ctx) {
        if (cache != null) return cache;
        ArrayList<City> out = new ArrayList<>();
        try {
            InputStream is = ctx.getAssets().open("mazandaran_landmarks.json");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
            is.close();
            JSONObject root = new JSONObject(bos.toString("UTF-8"));
            JSONArray arr = root.getJSONArray("cities");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String name = o.optString("name", "");
                JSONObject center = o.optJSONObject("center");
                double clat = center != null ? center.optDouble("lat", 0) : 0;
                double clng = center != null ? center.optDouble("lng", 0) : 0;
                ArrayList<Landmark> lms = new ArrayList<>();
                JSONArray la = o.optJSONArray("landmarks");
                if (la != null) {
                    for (int j = 0; j < la.length(); j++) {
                        JSONObject lm = la.getJSONObject(j);
                        lms.add(new Landmark(
                                lm.optString("name", ""),
                                lm.optDouble("lat", 0),
                                lm.optDouble("lng", 0)));
                    }
                }
                out.add(new City(name, clat, clng, lms));
            }
        } catch (Throwable ignored) {}
        cache = out;
        return out;
    }

    public City findCity(Context ctx, String name) {
        if (name == null) return null;
        for (City c : cities(ctx)) {
            if (name.equals(c.name)) return c;
        }
        return null;
    }
}
