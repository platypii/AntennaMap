package com.platypii.basemap;

import com.google.android.gms.maps.model.LatLng;

public class ASRRecord {

	public double latitude;
	public double longitude;
	public double height;

	public ASRRecord(double latitude, double longitude, double height) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.height = height;
	}

    public LatLng latLng() {
        return new LatLng(latitude, longitude);
    }
	
}
