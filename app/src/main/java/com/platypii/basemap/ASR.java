package com.platypii.basemap;

import java.io.IOException;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;

import com.google.android.gms.maps.model.LatLngBounds;

public class ASR {

	private static final int LIMIT = 20;

    public static List<ASRRecord> query(LatLngBounds bounds) throws IOException {
        return ASRDatabase.query(bounds.southwest.latitude, bounds.northeast.latitude, bounds.southwest.longitude, bounds.northeast.longitude, LIMIT);
    }

}
