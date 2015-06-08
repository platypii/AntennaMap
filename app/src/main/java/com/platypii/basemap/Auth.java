package com.platypii.basemap;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A login screen that offers login via email/password.
 */
class Auth {

    public static boolean checkAuth(Context context) {
        // Check if user has authenticated
        final SharedPreferences prefs = context.getSharedPreferences("baseline", Context.MODE_PRIVATE);
        return prefs.getBoolean("basemap_authenticated", false);
    }

    /** Validate against baseline server */
    public static boolean validate(CharSequence password) {
        final String postUrl = "https://base-line.ws/auth/token";
        final byte[] content = password.toString().getBytes();
        final long contentLength = content.length;
        try {
            Log.i("Auth", "Validating token " + password + " at url " + postUrl);
            final URL url = new URL(postUrl);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setDoOutput(true);
                // Write to OutputStream
                conn.setFixedLengthStreamingMode((int) contentLength);
                final OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(content);
                os.close();
                // Read response
                final int status = conn.getResponseCode();
                if (status == 200) {
                    return true;
                } else {
                    Log.e("Auth", "Token validation failed " + status);
                    return false;
                }
            } finally {
                conn.disconnect();
            }
        } catch(IOException e) {
            Log.e("Auth", "Token validation failed", e);
            return false;
        }
    }
}



