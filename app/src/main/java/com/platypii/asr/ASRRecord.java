package com.platypii.asr;

import com.google.android.gms.maps.model.LatLng;

class ASRRecord {

    final long id;
	final double latitude;
	final double longitude;
	final double height;

	ASRRecord(long id, double latitude, double longitude, double height) {
        this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.height = height;
	}

    LatLng latLng() {
        return new LatLng(latitude, longitude);
    }

    String url() {
        return "http://wireless2.fcc.gov/UlsApp/AsrSearch/asrRegistration.jsp?regKey=" + id;
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof ASRRecord && id == ((ASRRecord)obj).id;
    }

    @Override public int hashCode() {
        return Long.valueOf(id).hashCode();
    }
	
}
