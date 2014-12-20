package com.platypii.basemap;

import java.io.IOException;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;

import com.google.android.gms.maps.model.LatLngBounds;

public class ASR {

	private static final int LIMIT = 8;
    private static ASRDatabase asrDatabase;

    private static ProgressDialog mProgressDialog;

	public static void init(final Context context) {
        asrDatabase = new ASRDatabase(context);
	}

    public static List<ASRRecord> query(LatLngBounds bounds) throws IOException {
        if(asrDatabase != null) {
            return asrDatabase.query(bounds.southwest.latitude, bounds.northeast.latitude, bounds.southwest.longitude, bounds.northeast.longitude, LIMIT);
        } else {
            // TODO: Enqueue query
            return null;
        }
    }

}
