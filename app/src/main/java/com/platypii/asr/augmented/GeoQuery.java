package com.platypii.asr.augmented;

import android.location.Location;
import com.platypii.asr.PlaceDatabase;
import com.platypii.asr.Place;
import java.util.List;

/**
 * Searches for towers in the vicinity
 */
class GeoQuery {

    private static final int limit = 20;

    static List<Place> getNeighbors(Location loc) {
        final double lat1 = loc.getLatitude() - 0.1;
        final double lat2 = loc.getLatitude() + 0.1;
        final double lon1 = loc.getLongitude() - 0.1;
        final double lon2 = loc.getLongitude() + 0.1;
        return PlaceDatabase.query(lat1, lat2, lon1, lon2, limit);
    }

}
