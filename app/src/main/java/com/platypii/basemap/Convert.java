package com.platypii.basemap;

/**
 * Convert units
 */
public class Convert {
    public static String toFeet(double meters) {
        return (int)(3.28084 * meters) + "ft";
    }
}
