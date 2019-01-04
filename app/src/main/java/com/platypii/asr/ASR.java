package com.platypii.asr;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.gms.maps.model.LatLngBounds;
import java.util.List;

class ASR {
    private static final String TAG = "ASR";

    private static final int LIMIT = 24;

    // Flag to indicate that cache file was loaded from resources, and needs to be reloaded
    static boolean reloadRequired = false;

    static void init(@NonNull Context appContext) {
        // Start the database
        PlaceDatabase.start(appContext);
        if (!PlaceDatabase.isReady()) {
            reloadRequired = true;
        }
        PlaceFile.start(appContext);
        PlaceDownload.updateAsync();
        if (PlaceDatabase.isReady()) {
            ASR.ready();
        }
    }

    // Callback for when file loading complete
    static void fileLoaded() {
        // Load file into database
        PlaceDatabase.loadDataAsync(PlaceFile.iterator());
        reloadRequired = false;
    }

    // Callback for when database loading complete
    static void ready() {
        MapsActivity.updateMap();
    }

    @Nullable
    static List<Place> query(@NonNull LatLngBounds bounds) {
        Log.w(TAG, "Querying for " + bounds);
        final long startTime = System.nanoTime();
        final List<Place> results = PlaceDatabase.query(bounds.southwest.latitude, bounds.northeast.latitude, bounds.southwest.longitude, bounds.northeast.longitude, LIMIT);
        final double queryTime = (System.nanoTime() - startTime) * 10E-9;
        if (results != null) {
            Log.w(TAG, String.format("Query returned %d results (%.3fs)", results.size(), queryTime));
        } else {
            Log.e(TAG, "Query returned null!");
        }
        return results;
    }

}
