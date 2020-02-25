package com.platypii.asr;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;
import java.util.Locale;

class Place {

    final double latitude;
    final double longitude;
    final double height;
    final String url;

    Place(double latitude, double longitude, double height, String url) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
        this.url = url;
    }

    @NonNull
    LatLng latLng() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Place) {
            final Place other = (Place) obj;
            return latitude == other.latitude && longitude == other.longitude && height == other.height;
        } else {
            return false;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "Place(%.6f, %.6f, %.1f)", latitude, longitude, height);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}
