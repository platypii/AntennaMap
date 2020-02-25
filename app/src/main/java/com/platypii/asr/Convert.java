package com.platypii.asr;

import androidx.annotation.NonNull;

/**
 * Convert units
 */
class Convert {
    @NonNull
    static String toFeet(double meters) {
        return (int) (3.28084 * meters) + " ft";
    }
}
