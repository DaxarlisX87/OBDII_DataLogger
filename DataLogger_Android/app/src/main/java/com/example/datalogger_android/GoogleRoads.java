package com.example.datalogger_android;

import android.util.Pair;

import com.google.maps.model.LatLng;
import com.google.maps.GeoApiContext;
import com.google.maps.RoadsApi;
import com.google.maps.model.SnappedPoint;
import com.google.maps.model.SpeedLimit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleRoads {
    private static final int PAGINATION_OVERLAP = 5;
    private static final int PAGE_SIZE_LIMIT = 50;

    public List<SnappedPoint> snapToRoads(GeoApiContext context, ArrayList<Pair<Double,Double>> gpspoints) throws Exception {
        List<SnappedPoint> snappedPoints = new ArrayList<>();
        ArrayList<LatLng> mCapturedLocations = new ArrayList<>();
        for (Pair<Double,Double> p: gpspoints) {
            mCapturedLocations.add(new LatLng(p.first, p.second));
        }

        int offset = 0;
        while (offset < mCapturedLocations.size()) {
            // Calculate which points to include in this request. We can't exceed the API's
            // maximum and we want to ensure some overlap so the API can infer a good location for
            // the first few points in each request.
            if (offset > 0) {
                offset -= PAGINATION_OVERLAP;   // Rewind to include some previous points.
            }
            int lowerBound = offset;
            int upperBound = Math.min(offset + PAGE_SIZE_LIMIT, mCapturedLocations.size());

            // Get the data we need for this page.
            LatLng[] page = mCapturedLocations
                    .subList(lowerBound, upperBound)
                    .toArray(new LatLng[upperBound - lowerBound]);

            // Perform the request. Because we have interpolate=true, we will get extra data points
            // between our originally requested path. To ensure we can concatenate these points, we
            // only start adding once we've hit the first new point (that is, skip the overlap).
            SnappedPoint[] points = RoadsApi.snapToRoads(context, true, page).await();
            boolean passedOverlap = false;
            for (SnappedPoint point : points) {
                if (offset == 0 || point.originalIndex >= PAGINATION_OVERLAP - 1) {
                    passedOverlap = true;
                }
                if (passedOverlap) {
                    snappedPoints.add(point);
                }
            }

            offset = upperBound;
        }

        return snappedPoints;
    }
    /**
     * Retrieves speed limits for the previously-snapped points. This method is efficient in terms
     * of quota usage as it will only query for unique places.
     *
     * Note: Speed limit data is only available for requests using an API key enabled for a
     * Google Maps APIs Premium Plan license.
     */
    public Map<String, SpeedLimit> getSpeedLimits(GeoApiContext context, List<SnappedPoint> points)
            throws Exception {
        Map<String, SpeedLimit> placeSpeeds = new HashMap<>();

        // Pro tip: Save on quota by filtering to unique place IDs.
        for (SnappedPoint point : points) {
            placeSpeeds.put(point.placeId, null);
        }

        String[] uniquePlaceIds =
                placeSpeeds.keySet().toArray(new String[placeSpeeds.keySet().size()]);

        // Loop through the places, one page (API request) at a time.
        for (int i = 0; i < uniquePlaceIds.length; i += PAGE_SIZE_LIMIT) {
            String[] page = Arrays.copyOfRange(uniquePlaceIds, i,
                    Math.min(i + PAGE_SIZE_LIMIT, uniquePlaceIds.length));

            // Execute!
            SpeedLimit[] placeLimits = RoadsApi.speedLimits(context, page).await();
            for (SpeedLimit sl : placeLimits) {
                placeSpeeds.put(sl.placeId, sl);
            }
        }

        return placeSpeeds;
    }
    public static SpeedLimit getOneSpeedLimit(GeoApiContext context, Pair<Double,Double> gpspoint) throws Exception{
        LatLng point = new LatLng(gpspoint.first,gpspoint.second);
        return RoadsApi.speedLimits(context, point).await()[0];
    }
}
