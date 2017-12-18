package com.platypii.asr;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

class PlaceFile {
    private static final String TAG = "PlaceFile";

    static File cacheFile;

    static void start(@NonNull Context appContext) {
        // Get reference to cache file
        if(cacheFile != null) {
            Log.e(TAG, "Already loaded");
        } else {
            final File cacheDir = appContext.getExternalCacheDir();
            cacheFile = new File(cacheDir, "places.csv.gz");
            Log.i(TAG, "Using place file " + cacheFile);
            // Check if file exists
            if(!cacheFile.exists()) {
                // Fresh install, load default file from resources
                Log.w(TAG, "Cache file does not exist, using default");
                try {
                    final InputStream defaultCacheFile = appContext.getResources().openRawResource(R.raw.asr_csv_gz);
                    Util.copy(defaultCacheFile, cacheFile);
                    ASR.reloadRequired = true;
                    Log.i(TAG, "Copied default cache file from resources");
                } catch (IOException e) {
                    Log.e(TAG, "Error copying default cache file from resources", e);
                    Crashlytics.logException(e);
                }
            }
        }
    }

    /** Scan the cache file for length and MD5 */
    static String md5() {
        if (cacheFile == null) {
            Log.e(TAG, "Not initialized");
            return null;
        } else {
            try {
                final MessageDigest md = MessageDigest.getInstance("MD5");
                final InputStream inputStream = new DigestInputStream(new FileInputStream(cacheFile), md);
                final byte[] buffer = new byte[1024];
                while(inputStream.read(buffer) != -1) {
                    // Do nothing
                }
                inputStream.close();
                // Format digest as hex
                return String.format("%1$032x", new BigInteger(1, md.digest()));
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "Failed to compute MD5", e);
                return null;
            } catch(IOException e) {
                Log.e(TAG, "Failed to read place file", e);
                return null;
            }
        }
    }

    static long size() {
        return cacheFile.length();
    }

    static int rowCount() {
        if (cacheFile == null) {
            Log.e(TAG, "Not initialized");
            return -1;
        } else {
            // Count rows in cache file
            try {
                final InputStream inputStream = new GZIPInputStream(new FileInputStream(cacheFile));
                final byte[] buffer = new byte[4096];
                int bufferLength;
                int count = 0;
                while((bufferLength = inputStream.read(buffer)) != -1) {
                    for(int i = 0; i < bufferLength; i++) {
                        if(buffer[i] == '\n') {
                            count++;
                        }
                    }
                }
                inputStream.close();
                return count;
            } catch(IOException e) {
                Log.e(TAG, "Failed to read place file", e);
                return -1;
            }
        }
    }

    static Iterator<Place> iterator() {
        if(cacheFile == null) {
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
        final String[] split = line.split(",");
        if(split.length < 4 || split[0].equals("") || split[1].equals("") || split[2].equals("") || split[3].equals("")) {
            Log.w(TAG, "Failed to parse line " + line);
            return null;
        }
        try {
            final long id = Long.parseLong(split[0]);
            final double latitude = Double.parseDouble(split[1]) / 3600.0;
            final double longitude = Double.parseDouble(split[2]) / 3600.0;
            final double altitude = Double.parseDouble(split[3]);
            return new Place(id, latitude, longitude, altitude);
        } catch(Exception e) {
            Log.e(TAG, "Failed to parse line " + line, e);
            Crashlytics.logException(e);
            return null;
        }
    }

}
