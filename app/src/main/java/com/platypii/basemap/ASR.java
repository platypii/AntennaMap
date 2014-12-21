package com.platypii.basemap;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;

class ASR {

	private static final int LIMIT = 20;

    public static void init(Context appContext, MapsActivity activity) {
        // Start the database
        ASRDatabase.start(appContext);
        if(ASRDatabase.isEmpty()) {
            // Load file
            ASRFile.init(appContext);
            if (!ASRFile.isCached()) {
                ASRFile.downloadAsync(appContext, activity);
            } else {
                ASR.fileLoaded(appContext, activity);
            }
        } else {
            ASR.databaseLoaded(appContext, activity);
        }
    }

    // Callback for when file loading complete
    public static void fileLoaded(Context appContext, MapsActivity activity) {
        // Load file into database
        ASRDatabase.loadDataAsync(ASRFile.iterator(), appContext, activity);
    }

    // Callback for when database loading complete
    public static void databaseLoaded(Context appContext, MapsActivity activity) {
        activity.updateMap();
    }

    public static List<ASRRecord> query(LatLngBounds bounds) {
        return ASRDatabase.query(bounds.southwest.latitude, bounds.northeast.latitude, bounds.southwest.longitude, bounds.northeast.longitude, LIMIT);
    }

}
