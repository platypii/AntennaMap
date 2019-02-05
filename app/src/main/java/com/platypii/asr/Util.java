package com.platypii.asr;

import android.support.annotation.NonNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;

class Util {

    static void copy(@NonNull InputStream inputStream, @NonNull File outputFile) throws IOException {
        // create a buffer...
        final byte[] buffer = new byte[1024];
        int bufferLength; // used to store temporary size of the buffer

        // now, read through the input buffer and write the contents to the file
        final FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        while ((bufferLength = inputStream.read(buffer)) > 0) {
            // add the data in the buffer to the file in the file output stream (the file on the sd card
            fileOutputStream.write(buffer, 0, bufferLength);
        }
        fileOutputStream.close();
    }

    @NonNull
    static String md5(File file) throws NoSuchAlgorithmException, IOException {
        final MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream inputStream = new DigestInputStream(new FileInputStream(file), md)) {
            final byte[] buffer = new byte[1024];
            while (inputStream.read(buffer) != -1) {
                // Do nothing
            }
            // Format digest as hex
            return String.format("%1$032x", new BigInteger(1, md.digest()));
        }
    }

    /**
     * Count the number of lines in a gzip file (as if it was unzipped).
     */
    static int lineCountGzip(File gzFile) throws IOException {
        try (InputStream inputStream = new GZIPInputStream(new FileInputStream(gzFile))) {
            final byte[] buffer = new byte[4096];
            int bufferLength;
            int count = 0;
            while ((bufferLength = inputStream.read(buffer)) != -1) {
                for (int i = 0; i < bufferLength; i++) {
                    if (buffer[i] == '\n') {
                        count++;
                    }
                }
            }
            return count;
        }
    }

}
