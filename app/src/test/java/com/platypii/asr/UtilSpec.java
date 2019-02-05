package com.platypii.asr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilSpec {

    private static final String data = "This is the data.";

    @Test
    public void copyInputStreamToFile() throws IOException {
        final InputStream is = new ByteArrayInputStream(data.getBytes());
        final File outfile = File.createTempFile("testfile", ".txt");

        // Copy
        Util.copy(is, outfile);

        // Validate
        final String contents = new Scanner(outfile).nextLine();
        assertEquals(data, contents);
    }

    @Test
    public void computeMD5() throws Exception {
        final InputStream is = new ByteArrayInputStream(data.getBytes());
        final File outfile = File.createTempFile("testfile", ".txt");
        Util.copy(is, outfile);

        assertEquals("2170fd3c6e38537a865e210604a1b6dd", Util.md5(outfile));
    }

    @Test
    public void countLines() throws Exception {
        // Write test gzip file
        final File gzFile = File.createTempFile("testfile", ".txt.gz");
        final OutputStream os = new GZIPOutputStream(new FileOutputStream(gzFile));
        os.write(data.getBytes());
        os.write('\n');
        os.write(data.getBytes());
        os.write('\n');
        os.write(data.getBytes());
        os.write('\n');
        os.close();
        assertEquals(3, Util.lineCountGzip(gzFile));

        // Test null file
        assertEquals(0, Util.lineCountGzip(null));
    }
}
