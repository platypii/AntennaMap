package com.platypii.basemap;

import android.content.Context;
import android.util.Log;

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

class ASRFile {
	public static File cacheFile;

    public static void start(Context appContext) {
        // Get reference to cache file
        if(cacheFile != null) {
            Log.e("ASRFile", "Already loaded");
        } else {
            final File cacheDir = appContext.getExternalCacheDir();
            cacheFile = new File(cacheDir, "asr.csv.gz");
        }
    }

    /** Scan the cache file for length and MD5 */
    public static String md5() {
        if (cacheFile == null) {
            Log.e("ASRFile", "Not initialized");
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
                Log.e("ASRFile", "Failed to compute MD5", e);
                return null;
            } catch(IOException e) {
                Log.e("ASRFile", "Failed to read ASR file", e);
                return null;
            }
        }
    }

    public static long size() {
        return cacheFile.length();
    }

    public static int rowCount() {
        if (cacheFile == null) {
            Log.e("ASRFile", "Not initialized");
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
                Log.e("ASRFile", "Failed to read ASR file", e);
                return -1;
            }
        }
    }

    public static Iterator<ASRRecord> iterator() {
        if(cacheFile == null) {
            Log.e("ASRFile", "Not initialized");
            return null;
        } else {
            return new Iterator<ASRRecord>() {
                private BufferedReader reader;
                private String nextLine;

                {
                    try {
                        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(cacheFile))));
                        // Skip first line
                        reader.readLine();
                        nextLine = reader.readLine();
                    } catch (IOException e) {
                        Log.e("ASRFile", "Error reading file", e);
                    }
                }

                @Override
                public boolean hasNext() {
                    return nextLine != null;
                }

                @Override
                public ASRRecord next() {
                    final ASRRecord record = parseLine(nextLine);
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
     * Parse a CSV line into an ASRRecord
     */
    private static ASRRecord parseLine(String line) {
        final String[] split = line.split(",");
        if(split.length < 4 || split[0].equals("") || split[1].equals("") || split[2].equals("") || split[3].equals("")) {
            Log.i("ASRFile", "Failed to parse line " + line);
            return null;
        }
        try {
            final long id = Long.parseLong(split[0]);
            final double latitude = Double.parseDouble(split[1]) / 3600.0;
            final double longitude = - Double.parseDouble(split[2]) / 3600.0;
            final double height = Double.parseDouble(split[3]);
            return new ASRRecord(id, latitude, longitude, height);
        } catch(Exception e) {
            Log.e("ASRFile", "Failed to parse line " + line, e);
            return null;
        }
    }

}
