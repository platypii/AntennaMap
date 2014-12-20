package com.platypii.basemap;

import com.google.android.gms.maps.model.LatLng;

public class ASRRecord {

    public final long id;
	public final double latitude;
	public final double longitude;
	public final double height;

	public ASRRecord(long id, double latitude, double longitude, double height) {
        this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.height = height;
	}

    public LatLng latLng() {
        return new LatLng(latitude, longitude);
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof ASRRecord && id == ((ASRRecord)obj).id;
    }

    @Override public int hashCode() {
        return Long.valueOf(id).hashCode();
    }
	
}
