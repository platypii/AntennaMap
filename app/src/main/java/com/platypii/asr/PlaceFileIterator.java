package com.platypii.asr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

class PlaceFileIterator implements Iterator<Place> {
    private static final String TAG = "PlaceFileIterator";

    private BufferedReader reader;
    @Nullable
    private String nextLine;

    PlaceFileIterator(@NonNull File cacheFile) {
        try {
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(cacheFile))));
            // Skip first line
            reader.readLine();
            nextLine = reader.readLine();
        } catch (IOException e) {
            Log.e(TAG, "Error reading file", e);
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public boolean hasNext() {
        return nextLine != null;
    }

    @Override
    public Place next() {
        if (nextLine == null) {
            throw new NoSuchElementException();
        }
        final Place record = parseLine(nextLine);
        try {
            nextLine = reader.readLine();
        } catch (IOException e) {
            nextLine = null;
        }
        return record;
    }

    @Override
    public void remove() {}

    /**
     * Parse a CSV line into a place
     */
    @Nullable
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
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        }
    }

}
