package com.platypii.asr;

import android.support.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;

class Place {

    final long id;
    final double latitude;
    final double longitude;
    final double altitude;

    Place(long id, double latitude, double longitude, double altitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    @NonNull
    LatLng latLng() {
        return new LatLng(latitude, longitude);
    }

    @NonNull
    String url() {
        return "http://wireless2.fcc.gov/UlsApp/AsrSearch/asrRegistration.jsp?regKey=" + id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Place && id == ((Place) obj).id;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(id).hashCode();
    }

}
