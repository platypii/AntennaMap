package com.platypii.asr;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;

class ConsentScreen {
    private static final String TAG = "Consent";
    private static final String PREF_CONSENT = "consent_is_sexy";

    static void onStart(@NonNull Activity activity) {
        if (!consented(activity)) {
            showInfo(activity);
        }
    }

    static boolean consented(@NonNull Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_CONSENT, false);
    }

    private static void showInfo(@NonNull Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage(R.string.welcome_message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> setConsent(activity))
                .setOnCancelListener(dialog -> activity.finish())
                .show();
    }

    private static void setConsent(@NonNull Activity activity) {
        Log.i(TAG, "User consented");
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_CONSENT, true);
        editor.apply();

        // Request location access after user has consented
        Permissions.requestLocationPermissions(activity);
    }
}
