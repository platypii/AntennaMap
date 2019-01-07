package com.platypii.asr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;
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

    @Test
    public void countLines() throws Exception {
        // Write test gzip file
        File gzFile = File.createTempFile("testfile", ".txt.gz");
        OutputStream os = new GZIPOutputStream(new FileOutputStream(gzFile));
        os.write(data.getBytes());
        os.write('\n');
        os.write(data.getBytes());
        os.write('\n');
        os.write(data.getBytes());
        os.write('\n');
        os.close();

        assertEquals(3, Util.lineCountGzip(gzFile));
    }
}
