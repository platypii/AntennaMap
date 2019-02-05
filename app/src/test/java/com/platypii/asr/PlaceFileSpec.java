package com.platypii.asr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlaceFileSpec {

    // Write test gzip file and trick PlaceFile into it
    @Before
    public void writePlaceFile() throws IOException {
        PlaceFile.cacheFile = PlaceFileHelper.writePlaceFile();
    }

    @Test
    public void md5() {
        assertEquals("43938ba87595650eb78eef067ccffeb9", PlaceFile.md5());
    }

    @Test
    public void rowCount() {
        assertEquals(4, PlaceFile.rowCount());
    }

    @Test
    public void size() {
        assertEquals(176, PlaceFile.size());
    }

    @Test
    public void iterator() {
        final Iterator<Place> it = PlaceFile.iterator();
        final List<Place> places = new ArrayList<>();
        while (it.hasNext()) {
            final Place place = it.next();
            places.add(place);
        }
        assertEquals(3, places.size());
    }

}
