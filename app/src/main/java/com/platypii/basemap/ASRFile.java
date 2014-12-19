package com.platypii.basemap;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class ASRFile implements Iterable<ASRRecord> {

	private static final String fileUrl = "https://s3-us-west-1.amazonaws.com/platypii.asrdata/asr.csv";
	private File file = null;

	public ASRFile(final Context context) {

        // Check cache
        final File cacheDir = context.getExternalCacheDir();
        final File cacheFile = new File(cacheDir, "asr.csv");
        if(cacheFile.exists()) {
            this.file = cacheFile;
//            Toast.makeText(context, "Using cached ASR data", Toast.LENGTH_LONG).show();
        } else {
            download(cacheFile);
        }
	}

    private void download(File cacheFile) {
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
                Log.w("DOWNLOAD", "progress " + downloadedSize + " / " + totalSize);

            }
            //close the output stream when done
            fileOutput.close();

            this.file = cacheFile;
        } catch(IOException e) {
            Log.e("DOWNLOAD", "ERROR: ", e);
        }
    }

    @Override
    public Iterator<ASRRecord> iterator() {
        return new Iterator<ASRRecord>() {
            BufferedReader reader;
            String nextLine;
            {
                try {
                    reader = new BufferedReader(new FileReader(file));
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
            public void remove() {}
        };
    }

    private ASRRecord parseLine(String line) {
        final String[] split = line.split(",");
        final double latitude = Double.parseDouble(split[1]);
        final double longitude = Double.parseDouble(split[2]);
        final double height = Double.parseDouble(split[9]);
        return new ASRRecord(latitude, longitude, height);
    }

    /**
     * Search for the N tallest towers in view
     */
	public List<ASRRecord> query(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude, int limit) throws IOException {
        if(file != null) {
            final TopN top = new TopN(limit);

            // Search CSV file for top N antennas
            final BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while((line = reader.readLine()) != null) {
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
