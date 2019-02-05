package com.platypii.asr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlaceFileIteratorSpec {

    private final Place place1 = new Place(47.342222, -97.289167, 628.8, "https://wireless2.fcc.gov/UlsApp/AsrSearch/asrRegistration.jsp?regKey=608746");
    private final Place place2 = new Place(47.279167,-97.340833, 627.8, "https://wireless2.fcc.gov/UlsApp/AsrSearch/asrRegistration.jsp?regKey=602919");
    private final Place place3 = new Place(38.240000,-121.501944, 624.5, "https://wireless2.fcc.gov/UlsApp/AsrSearch/asrRegistration.jsp?regKey=115436");

    private File placeFile;

    // Write test gzip file and trick PlaceFile into it
    @Before
    public void writePlaceFile() throws IOException {
        placeFile = PlaceFileHelper.writePlaceFile();
    }

    @Test
    public void iterateAll() {
        final Iterator<Place> it = new PlaceFileIterator(placeFile);
        final List<Place> places = new ArrayList<>();
        while (it.hasNext()) {
            final Place place = it.next();
            places.add(place);
        }
        assertEquals(3, places.size());
        assertEquals(place1, places.get(0));
        assertEquals(place2, places.get(1));
        assertEquals(place3, places.get(2));
    }

    @Test(expected = NoSuchElementException.class)
    public void iteratorNoBrakes() {
        final Iterator<Place> it = new PlaceFileIterator(placeFile);
        while (it.hasNext()) {
            it.next();
        }
        // End of the iterator should throw
        it.next();
    }

}
