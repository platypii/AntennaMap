package com.platypii.asr;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

class PlaceFile {
    private static final String TAG = "PlaceFile";

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
     * Scan the cache file for length and MD5
     */
    @Nullable
    static String md5() {
        try {
            return Util.md5(cacheFile);
        } catch (Exception e) {
            Log.e(TAG, "Failed to compute MD5 of place file", e);
            return null;
        }
    }

    static long size() {
        return cacheFile.length();
    }

    static int rowCount() {
        try {
            return Util.lineCountGzip(cacheFile);
        } catch (IOException e) {
            Log.e(TAG, "Failed to count lines of place file", e);
            return -1;
        }
    }

    static Iterator<Place> iterator() {
        if (cacheFile == null) {
            Log.e(TAG, "Not initialized");
            return null;
        } else {
            return new Iterator<Place>() {
                private BufferedReader reader;
                private String nextLine;

                {
                    try {
                        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(cacheFile))));
                        // Skip first line
                        reader.readLine();
                        nextLine = reader.readLine();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading file", e);
                        Crashlytics.logException(e);
                    }
                }

                @Override
                public boolean hasNext() {
                    return nextLine != null;
                }

                @Override
                public Place next() {
                    final Place record = parseLine(nextLine);
                    try {
                        nextLine = reader.readLine();
                    } catch (IOException e) {
                        nextLine = null;
                    }
                    return record;
                }

                @Override
                public void remove() {
                }
            };
        }
    }

    /**
     * Parse a CSV line into a place
     */
    private static Place parseLine(@NonNull String line) {
        final String[] split = line.split(",", 4);
        if (split[0].equals("") || split[1].equals("") || split[2].equals("")) {
            Log.w(TAG, "Failed to parse line " + line);
            return null;
        }
        try {
            final double latitude = Double.parseDouble(split[0]);
            final double longitude = Double.parseDouble(split[1]);
            final double altitude = Double.parseDouble(split[2]);
            final String url = split[3];
            return new Place(latitude, longitude, altitude, url);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse line " + line, e);
            Crashlytics.logException(e);
            return null;
        }
    }

}
