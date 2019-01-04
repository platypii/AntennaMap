package com.platypii.asr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

class Assets {

    // sizedIcons[0] = 0..100m
    // sizedIcons[1] = 1..200m
    private static final BitmapDescriptor sizedIcons[] = new BitmapDescriptor[7];

    static BitmapDescriptor getSizedIcon(@NonNull Context context, double height) {
        final float density = context.getResources().getDisplayMetrics().density;
        final int index = (int) (height / 100);
        if (sizedIcons[index] == null) {
            // Generate icon
            final int size = (int) ((14 + index * 5) * density);
            final Drawable drawable = ContextCompat.getDrawable(context, R.drawable.a);
            if (drawable instanceof BitmapDrawable) {
                final BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                final Bitmap bitmap = bitmapDrawable.getBitmap();
                final Bitmap bitmapScaled = Bitmap.createScaledBitmap(bitmap, size * 3 / 4, size, false);
                sizedIcons[index] = BitmapDescriptorFactory.fromBitmap(bitmapScaled);
            } else {
                return null; // Should never happen
            }
        }
        return sizedIcons[index];
    }

}
