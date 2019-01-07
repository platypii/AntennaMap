package com.platypii.asr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilSpec {

    String data = "This is the data.";

    @Test
    public void copyInputStreamToFile() throws IOException {
        InputStream is = new ByteArrayInputStream(data.getBytes());
        File outfile = File.createTempFile("testfile", ".txt");

        // Copy
        Util.copy(is, outfile);

        // Validate
        String contents = new Scanner(outfile).nextLine();
        assertEquals(data, contents);
    }

    @Test
    public void computeMD5() throws Exception {
        InputStream is = new ByteArrayInputStream(data.getBytes());
        File outfile = File.createTempFile("testfile", ".txt");
        Util.copy(is, outfile);

        assertEquals("2170fd3c6e38537a865e210604a1b6dd", Util.md5(outfile));
    }
}
