package com.platypii.basemap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

class Assets {

    // sizedIcons[0] = 0..100m
    // sizedIcons[1] = 1..200m
    private static final BitmapDescriptor sizedIcons[] = new BitmapDescriptor[7];

    public static BitmapDescriptor getSizedIcon(Context context, double height) {
        final int index = (int)(height / 100);
        if(sizedIcons[index] == null) {
            // Generate icon
            final int size = 40 + index * 16;
            final Drawable drawable = ContextCompat.getDrawable(context, R.drawable.tower_yellow);
            if(drawable != null) {
                final Bitmap bitmap = ((BitmapDrawable) (drawable)).getBitmap();
                final Bitmap bitmapScaled = Bitmap.createScaledBitmap(bitmap, size, size, false);
                sizedIcons[index] = BitmapDescriptorFactory.fromBitmap(bitmapScaled);
            } else {
                return null; // Should never happen
            }
        }
        return sizedIcons[index];
    }

}
