package com.platypii.asr;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class MyLocation {
    private static final String TAG = "MyLocation";

    /**
     * Gets the users most recent location
     */
    @Nullable
    static Location getMyLocation(@NonNull Context context) {
        Location myLocation = null;

        // Get location from GPS if it's available
        final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Log.w(TAG, "Permission denied for GPS location");
        }

        // Location wasn't found, check the next most accurate place for the current location
        if (myLocation == null) {
            final Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            // Finds a provider that matches the criteria
            final String provider = lm.getBestProvider(criteria, true);
            if (provider != null) {
                // Use the provider to get the last known location
                try {
                    myLocation = lm.getLastKnownLocation(provider);
                } catch (SecurityException e) {
                    Log.w(TAG, "Permission denied for GPS location");
                }
            } else {
                Log.w(TAG, "No location provider found");
            }
        }

        if (myLocation == null) {
            return null;
        } else if (Math.abs(myLocation.getLatitude()) < 0.01 && Math.abs(myLocation.getLongitude()) < 0.01) {
            // Unlikely coordinate
            return null;
        } else {
            return myLocation;
        }
    }

}
