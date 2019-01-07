package com.platypii.asr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilSpec {

    @Test
    public void copyInputStreamToFile() throws IOException {
        String data = "This is the data.";
        InputStream is = new ByteArrayInputStream(data.getBytes());
        File outfile = File.createTempFile("testfile", ".txt");

        // Copy
        Util.copy(is, outfile);

        // Validate
        String contents = new Scanner(outfile).nextLine();
        assertEquals(data, contents);
    }
}
