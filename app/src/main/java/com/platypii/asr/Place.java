package com.platypii.asr;

import com.google.android.gms.maps.model.LatLng;

public class Place {

    public final long id;
    public final double latitude;
    public final double longitude;
    public final double altitude;

	Place(long id, double latitude, double longitude, double altitude) {
        this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}

    LatLng latLng() {
        return new LatLng(latitude, longitude);
    }

    String url() {
        return "http://wireless2.fcc.gov/UlsApp/AsrSearch/asrRegistration.jsp?regKey=" + id;
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof Place && id == ((Place)obj).id;
    }

    @Override public int hashCode() {
        return Long.valueOf(id).hashCode();
    }
	
}
