package com.platypii.asr;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.maps.model.LatLngBounds;
import java.util.List;

class ASR {
    private static final String TAG = "ASR";

    private static final int LIMIT = 22;

    // Flag to indicate that cache file was loaded from resources, and needs to be reloaded
    static boolean reloadRequired = false;

    static void init(Context appContext) {
        // Start the database
        ASRDatabase.start(appContext);
        if(!ASRDatabase.isReady()) {
            reloadRequired = true;
        }
        ASRFile.start(appContext);
        ASRDownload.updateAsync();
        if(ASRDatabase.isReady()) {
            ASR.ready();
        }
    }

    // Callback for when file loading complete
    static void fileLoaded() {
        // Load file into database
        ASRDatabase.loadDataAsync(ASRFile.iterator());
        reloadRequired = false;
    }

    // Callback for when database loading complete
    static void ready() {
        MapsActivity.updateMap();
    }

    static List<ASRRecord> query(LatLngBounds bounds) {
        Log.w(TAG, "Querying for " + bounds);
        final long startTime = System.nanoTime();
        final List<ASRRecord> results = ASRDatabase.query(bounds.southwest.latitude, bounds.northeast.latitude, bounds.southwest.longitude, bounds.northeast.longitude, LIMIT);
        final double queryTime = (System.nanoTime() - startTime) * 10E-9;
        if(results != null) {
            Log.w(TAG, String.format("Query returned %d results (%.3fs)", results.size(), queryTime));
        } else {
            Log.e(TAG, "Query returned null!");
        }
        return results;
    }

}
