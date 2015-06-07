package com.platypii.basemap;

import android.os.AsyncTask;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

class ASRDownload {
    private static final String fileUrl = "https://platypii.s3.amazonaws.com/asr/asr.csv.gz";
    // private static final String fileUrl = "https://platypii.s3.amazonaws.com/asr/asr-dev.csv.gz";

    public static void updateAsync() {
        if(ASRFile.cacheFile == null) {
            Log.e("ASRDownload", "Download failed: cache file not initialized");
        } else if(ASRFile.cacheFile.exists()) {
            Log.w("ASRDownload", "Checking for latest ASR file");
            // Check for newer version in S3
            new CheckETagTask().execute();
        } else {
            Log.w("ASRDownload", "Cache file not found");
            ASRDownload.downloadAsync();
        }
        Log.i("ASRDownload", "Returning from updateAsync()");
    }

    /** Download ASR file */
    private static void downloadAsync() {
        // Get reference to cache file
        if(ASRFile.cacheFile == null) {
            Log.e("ASRFile", "Download failed: cache file not initialized");
        } else {
            Log.w("ASRFile", "Downloading latest ASR file");
            // Download ASR file from S3
            new DownloadTask().execute();
        }
        Log.i("ASRDownload", "Returning from downloadAsync()");
    }

    private static class CheckETagTask extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                final URL url = new URL(fileUrl);

                Log.w("ASRDownload", "Checking eTag for URL: " + url);
                final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.connect();

                // Check size and eTag
                final int remoteSize = connection.getContentLength();
                final String remoteTag = connection.getHeaderField("ETag").replaceAll("\"", "");

                Log.i("ASRDownload", "Remote: size = " + remoteSize + ", eTag = " + remoteTag);

                final int localSize = (int) ASRFile.size();
                final String localTag = ASRFile.md5();

                Log.i("ASRDownload", "Local:  size = " + localSize + ", eTag = " + localTag);

                if(remoteTag == null) {
                    Log.e("ASRDownload", "Missing eTag");
                    return false;
                } else if(localSize != remoteSize) {
                    Log.w("ASRDownload", "Cache length and remote length differ");
                    return false;
                } else {
                    if(!remoteTag.equals(localTag)) {
                        Log.w("ASRDownload", "MD5 does not match " + remoteTag + " != " + localTag);
                        return false;
                    } else {
                        Log.w("ASRDownload", "Local file matches remote file");
                        return true;
                    }
                }
            } catch(IOException e) {
                Log.e("ASRFile", "Download error: " + e, e);
                return null;
            }
        }
        @Override
        protected void onPostExecute(Boolean eTagMatches) {
            if(eTagMatches != null && !eTagMatches) {
                Log.w("ASRDownload", "New ASR file found");
                ASRDownload.downloadAsync();
            }
        }
    }

    private static class DownloadTask extends AsyncTask<Void, Integer, Void> {
        private int totalSize = -1;

        @Override
        protected void onPreExecute() {
            MapsActivity.startProgress("Downloading data...");
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                final URL url = new URL(fileUrl);

                Log.w("ASRDownload", "Downloading URL: " + url);
                final HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                final InputStream inputStream = urlConnection.getInputStream();

                // the total size of the file
                totalSize = urlConnection.getContentLength();
                int downloadedSize = 0;
                publishProgress(0);

                // create a buffer...
                final byte[] buffer = new byte[1024];
                int bufferLength; // used to store a temporary size of the buffer

                // now, read through the input buffer and write the contents to the file
                final FileOutputStream fileOutput = new FileOutputStream(ASRFile.cacheFile);
                while( (bufferLength = inputStream.read(buffer)) > 0 ) {
                    // add the data in the buffer to the file in the file output stream (the file on the sd card
                    fileOutput.write(buffer, 0, bufferLength);
                    // add up the size so we know how much is downloaded
                    downloadedSize += bufferLength;
                    // Log.d("ASRDownload", "Download progress " + downloadedSize + " / " + totalSize);
                    publishProgress(downloadedSize);
                }
                fileOutput.close();
                Log.w("ASRDownload", "Downloaded asr.csv");
            } catch(IOException e) {
                Log.e("ASRDownload", "Download error: ", e);
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            MapsActivity.updateProgress("Downloading data...", progress[0], totalSize);
        }
        @Override
        protected void onPostExecute(Void result) {
            MapsActivity.dismissProgress();
            ASR.fileLoaded();
        }
    }

}
