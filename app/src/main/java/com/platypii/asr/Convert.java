package com.platypii.asr;

/**
 * Convert units
 */
class Convert {
    static String toFeet(double meters) {
        return (int) (3.28084 * meters) + " ft";
    }
}
