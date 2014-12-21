package com.platypii.basemap;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

class ASRFile {
	private static final String fileUrl = "https://s3-us-west-1.amazonaws.com/platypii.asrdata/asr-min.csv";
	private static File cacheFile;

    public static void init(Context appContext) {
        final File cacheDir = appContext.getExternalCacheDir();
        cacheFile = new File(cacheDir, "asr.csv");
    }

    public static boolean isCached() {
        if(cacheFile == null) {
            Log.e("ASRFile", "Not initialized");
            return false;
        } else {
            return cacheFile.exists();
        }
    }

    public static void downloadAsync(final Context context, final MapsActivity activity) {
        new AsyncTask<Void, Integer, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage("Downloading data...");
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... params) {
                Log.w("ASRFile", "Downloading asr.csv");
                try {
                    final URL url = new URL(fileUrl);

                    Log.w("ASRFile", "Downloading URL: " + url);
                    final HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                    final InputStream inputStream = urlConnection.getInputStream();

                    //this is the total size of the file
                    final int totalSize = urlConnection.getContentLength();
                    mProgressDialog.setMax(totalSize);
                    //variable to store total downloaded bytes
                    int downloadedSize = 0;

                    //create a buffer...
                    final byte[] buffer = new byte[1024];
                    int bufferLength = 0; //used to store a temporary size of the buffer

                    //now, read through the input buffer and write the contents to the file
                    final FileOutputStream fileOutput = new FileOutputStream(cacheFile);
                    while( (bufferLength = inputStream.read(buffer)) > 0 ) {
                        //add the data in the buffer to the file in the file output stream (the file on the sd card
                        fileOutput.write(buffer, 0, bufferLength);
                        //add up the size so we know how much is downloaded
                        downloadedSize += bufferLength;
                        Log.i("ASRFile", "Download progress " + downloadedSize + " / " + totalSize);
                        final int progress = 100 * downloadedSize / totalSize;
                        publishProgress(progress);
                    }
                    //close the output stream when done
                    fileOutput.close();
                    Log.w("ASRFile", "Downloaded asr.csv");
                } catch(IOException e) {
                    Log.e("ASRFile", "Download error: ", e);
                }
                return null;
            }
            @Override
            protected void onProgressUpdate(Integer... progress) {
                super.onProgressUpdate(progress);
                // if we get here, length is known, now set indeterminate to false
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMax(100);
                mProgressDialog.setProgress(progress[0]);
            }
            @Override
            protected void onPostExecute(Void result) {
                mProgressDialog.dismiss();
                ASR.fileLoaded(context, activity);
            }
        }.execute();
    }

    public static Iterator<ASRRecord> iterator() {
        if(cacheFile == null) {
            Log.e("ASRFile", "Not initialized");
            return null;
        } else {
            return new Iterator<ASRRecord>() {
                BufferedReader reader;
                String nextLine;

                {
                    try {
                        reader = new BufferedReader(new FileReader(cacheFile));
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
