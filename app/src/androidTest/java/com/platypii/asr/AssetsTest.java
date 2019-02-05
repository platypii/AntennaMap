package com.platypii.asr;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AssetsTest {

    @Before
    public void init() {
        final Context context = InstrumentationRegistry.getTargetContext();
        MapsInitializer.initialize(context);
    }

    @Test
    public void getIcon() {
        final Context context = InstrumentationRegistry.getTargetContext();

        assertNotNull(Assets.getSizedIcon(context, -100));
        assertNotNull(Assets.getSizedIcon(context, 0));
        assertNotNull(Assets.getSizedIcon(context, 100));
        assertNotNull(Assets.getSizedIcon(context, 200));
        assertNotNull(Assets.getSizedIcon(context, 400));
        assertNotNull(Assets.getSizedIcon(context, 800));
        assertNotNull(Assets.getSizedIcon(context, 1600));

        // Assert type
        assertTrue(Assets.getSizedIcon(context, 400) instanceof BitmapDescriptor);
    }

    @Test
    public void nanHeight() {
        final Context context = InstrumentationRegistry.getTargetContext();

        // Handle non reals
        final BitmapDescriptor small = Assets.getSizedIcon(context, 0);
        final BitmapDescriptor large = Assets.getSizedIcon(context, 1000);
        assertEquals(Assets.getSizedIcon(context, Double.NaN), small);
        assertEquals(Assets.getSizedIcon(context, Double.POSITIVE_INFINITY), large);
        assertEquals(Assets.getSizedIcon(context, Double.NEGATIVE_INFINITY), small);
    }
}
