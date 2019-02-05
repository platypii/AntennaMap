package com.platypii.asr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

class PlaceFileHelper {

    private static final String data = "lat,lon,height,url\n" +
            "47.342222,-97.289167,628.8,https://wireless2.fcc.gov/UlsApp/AsrSearch/asrRegistration.jsp?regKey=608746\n" +
            "47.279167,-97.340833,627.8,https://wireless2.fcc.gov/UlsApp/AsrSearch/asrRegistration.jsp?regKey=602919\n" +
            "38.240000,-121.501944,624.5,https://wireless2.fcc.gov/UlsApp/AsrSearch/asrRegistration.jsp?regKey=115436\n";

    static File writePlaceFile() throws IOException {
        final File gzFile = File.createTempFile("testfile", ".txt.gz");
        final OutputStream os = new GZIPOutputStream(new FileOutputStream(gzFile));
        os.write(data.getBytes());
        os.close();
        return gzFile;
    }
}
