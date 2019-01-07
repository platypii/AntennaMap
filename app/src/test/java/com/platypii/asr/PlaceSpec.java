package com.platypii.asr;

import com.google.android.gms.maps.model.LatLng;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PlaceSpec {

    private Place seattle = new Place(47.6, -122.3, 1000, "");

    @Test
    public void placeToString() {
        assertEquals("Place(47.600000, -122.300000, 1000.0)", seattle.toString());
    }

    @Test
    public void placeToLatLng() {
        assertEquals(new LatLng(47.6, -122.3), seattle.latLng());
    }

    @Test
    public void placeEquals() {
        assertEquals(seattle, new Place(47.6, -122.3, 1000, ""));
        assertNotEquals(seattle, new Place(10, 20, 1000, ""));
        assertNotEquals(seattle, null);
    }

    @Test
    public void placeHashCode() {
        assertEquals(-468226907, seattle.hashCode());
    }
}
