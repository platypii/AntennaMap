package com.platypii.asr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class Util {

    public static void copy(InputStream inputStream, File outputFile) throws IOException {
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

}

