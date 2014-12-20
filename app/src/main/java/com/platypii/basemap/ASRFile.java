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
    // Non-instantiable
    private ASRFile(File file) {
        this.file = file;
    }

	private static final String fileUrl = "https://s3-us-west-1.amazonaws.com/platypii.asrdata/asr.csv";
	private final File file;

    /**
     * Load the ASR file
     */
	public static synchronized ASRFile load(final Context context) {
        // Check cache
        final File cacheDir = context.getExternalCacheDir();
        final File cacheFile = new File(cacheDir, "asr.csv");
        if(cacheFile.exists()) {
            Log.w("ASRFile", "Found existing asr.csv");
            return new ASRFile(cacheFile);
        } else {
            Log.w("ASRFile", "Downloading asr.csv");
            download(cacheFile);
            Log.w("ASRFile", "Downloaded asr.csv");
            return new ASRFile(cacheFile);
        }
	}

    private static void download(File cacheFile) {
        try {
            final URL url = new URL(fileUrl);

            Log.w("ASRFile", "Downloading URL: " + url);
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
                Log.i("ASRFile", "Download progress " + downloadedSize + " / " + totalSize);

            }
            //close the output stream when done
            fileOutput.close();
        } catch(IOException e) {
            Log.e("ASRFile", "Download error: ", e);
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

}
