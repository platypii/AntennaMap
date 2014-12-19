package com.platypii.basemap;

import java.io.IOException;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;

import com.google.android.gms.maps.model.LatLngBounds;

public class ASR {

	private static final String fileUrl = "https://s3-us-west-1.amazonaws.com/platypii.asrdata/asr.csv";
	private static final int LIMIT = 4;
    private static ASRFile asrFile = null;

    private static ProgressDialog mProgressDialog;

	public static void init(final Context context) {
        asrFile = new ASRFile(context);
	}

    public static List<ASRRecord> query(LatLngBounds bounds) throws IOException {
        if(asrFile != null) {
            return asrFile.query(bounds.southwest.latitude, bounds.northeast.latitude, bounds.southwest.longitude, bounds.northeast.longitude, LIMIT);
        } else {
            return null;
        }
    }

}
