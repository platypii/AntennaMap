package com.platypii.asr;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConvertSpec {

    @Test
    public void toFeet() {
        assertEquals("0 ft", Convert.toFeet(0));
        assertEquals("1 ft", Convert.toFeet(0.3048));
        assertEquals("3 ft", Convert.toFeet(1));
        assertEquals("32 ft", Convert.toFeet(10));
    }
}
