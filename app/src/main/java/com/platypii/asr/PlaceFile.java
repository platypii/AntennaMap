package com.platypii.asr;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

// Would implement Iterable<Place> if it wasn't static
class PlaceFile {
    private static final String TAG = "PlaceFile";

    @Nullable
    static File cacheFile;

    static void start(@NonNull Context appContext) {
        // Get reference to cache file
        if (cacheFile != null) {
            Log.e(TAG, "Already loaded");
        } else {
            final File cacheDir = appContext.getExternalCacheDir();
            cacheFile = new File(cacheDir, "places.csv.gz");
            Log.i(TAG, "Using place file " + cacheFile);
            // Check if file exists
            if (!cacheFile.exists()) {
                // Fresh install, load default file from resources
                Log.w(TAG, "Cache file does not exist, using default");
                try {
                    final InputStream defaultCacheFile = appContext.getResources().openRawResource(R.raw.antennas_csv_gz);
                    Util.copy(defaultCacheFile, cacheFile);
                    Antennas.reloadRequired = true;
                    Log.i(TAG, "Copied default cache file from resources");
                } catch (IOException e) {
                    Log.e(TAG, "Error copying default cache file from resources", e);
                    Crashlytics.logException(e);
                }
            }
        }
    }

    /**
     * Compute MD5 checksum of cache file
     */
    @NonNull
    static String md5() {
        return Util.md5(cacheFile);
    }

    /**
     * Return cache file size in bytes
     */
    static long size() {
        return cacheFile == null ? 0 : cacheFile.length();
    }

    /**
     * Return number of lines in cache file.
     * Returns 0 if doesn't exist.
     */
    static int rowCount() {
        return Util.lineCountGzip(cacheFile);
    }

    @Nullable
    static Iterator<Place> iterator() {
        if (cacheFile == null) {
            Log.e(TAG, "Not initialized");
            return null;
        } else {
            return new PlaceFileIterator(cacheFile);
        }
    }

}
