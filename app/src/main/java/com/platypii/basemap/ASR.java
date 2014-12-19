package com.platypii.basemap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import javax.net.ssl.HttpsURLConnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLngBounds;

public class ASR {

	private static final String fileUrl = "https://s3-us-west-1.amazonaws.com/platypii.asrdata/asr.csv";
	private static final int LIMIT = 4;
	private static File file = null;

    private static ProgressDialog mProgressDialog;

	public static void init(final Context context) {

        // Check cache
        final File cacheDir = context.getExternalCacheDir();
        final File cacheFile = new File(cacheDir, "asr.csv");
        if(cacheFile.exists()) {
            ASR.file = cacheFile;
            Toast.makeText(context, "Using cached ASR data", Toast.LENGTH_LONG).show();
            return;
        }

		new AsyncTask<Void,Void,Void>() {
		    @Override protected void onPreExecute() {
		        mProgressDialog = ProgressDialog.show(context, "","Please wait, Download for " + fileUrl);
		    }
			@Override protected Void doInBackground(Void... params) {
		        try {
		            final URL url = new URL(fileUrl);

		            Log.w("DOWNLOAD", "URL: " + url);
		            final HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
		            final FileOutputStream fileOutput = new FileOutputStream(cacheFile);
		            final InputStream inputStream = urlConnection.getInputStream();

		            //this is the total size of the file
		            final int totalSize = urlConnection.getContentLength();
		            //variable to store total downloaded bytes
		            int downloadedSize = 0;

		            //create a buffer...
		            final byte[] buffer = new byte[1024];
		            int bufferLength = 0; //used to store a temporary size of the buffer

		            //now, read through the input buffer and write the contents to the file
		            while( (bufferLength = inputStream.read(buffer)) > 0 ) {
		                //add the data in the buffer to the file in the file output stream (the file on the sd card
		                fileOutput.write(buffer, 0, bufferLength);
		                //add up the size so we know how much is downloaded
		                downloadedSize += bufferLength;
		                //this is where you would do something to report the prgress, like this maybe
		                //updateProgress(downloadedSize, totalSize);
		                Log.w( "DOWNLOAD" , "progress " + downloadedSize + " / " + totalSize);

		            }
		            //close the output stream when done
		            fileOutput.close();

		            ASR.file = cacheFile;
		        } catch(IOException e) {
		            Log.e("DOWNLOAD", "ERROR : ", e);
		        }
				return null;
			}
		    @Override protected void onPostExecute(Void param) {
	            mProgressDialog.dismiss();
		    }			
		}.execute();
	}

    public static List<ASRRecord> query(LatLngBounds bounds) throws IOException {
        return query(bounds.southwest.latitude, bounds.northeast.latitude, bounds.southwest.longitude, bounds.northeast.longitude);
    }

    /**
     * Search for the N tallest towers in view
     * @param minLatitude
     * @param maxLatitude
     * @param minLongitude
     * @param maxLongitude
     * @return
     * @throws IOException
     */
	public static List<ASRRecord> query(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) throws IOException {
        if(file != null) {
//            final List<ASRRecord> records = new ArrayList<>();
            final TopN top = new TopN(LIMIT);

            // Search CSV file for top N antennas
            final BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while((line = reader.readLine()) != null) {

//            final Scanner sc = new Scanner(file);
//            sc.nextLine();
//            while (sc.hasNextLine()) {
//                final String line = sc.nextLine();
                final String[] split = line.split(",");
                // Check if lat/long is within bounding box
                final double latitude = Double.parseDouble(split[3]);
                final double longitude = Double.parseDouble(split[4]);
                if (minLatitude <= latitude && latitude <= maxLatitude && minLongitude <= longitude && longitude <= maxLongitude) {
                    final double height = Double.parseDouble(split[13]);
                    final ASRRecord record = new ASRRecord(latitude, longitude, height);
                    top.add(record);
                    // Stop after N records
//                    if (records.size() >= LIMIT) {
//                        break;
//                    }
                }
            }
            reader.close();
            return top.records;
        } else {
            return null;
        }
	}
	
}
