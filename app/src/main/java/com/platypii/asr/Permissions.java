package com.platypii.asr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class Permissions {
    static final int REQUEST_LOCATION_CODE = 64;

    public static boolean hasLocationPermissions(@NonNull Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED;
    }

    /**
     * Return true if either coarse or fine location is granted
     */
    public static boolean isLocationGranted(@NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if ((permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                    || permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION))
                    && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    static void requestLocationPermissions(@NonNull Activity activity) {
        final String[] permissions = {android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_LOCATION_CODE);
    }
}
