package com.platypii.asr.augmented;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;
import com.platypii.asr.ASRRecord;

/**
 * Render exits
 */
public class ExitView extends View {

    // Current phone orientation
    private float pitch;
    private float roll;
    private float yaw;

    private final Paint paint = new Paint();

    // TODO: Get from camera
    private static final float h_fov = 40;
    private static final float v_fov = 90;

    // TODO: Test location
    private static final Location tower = new Location("DB");

    // Current location from GPS
    private Location currentLocation;

    public ExitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setTextSize(64);
        tower.setLatitude(47.61219);
        tower.setLongitude(-122.34534);
        tower.setAltitude(135);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // use roll for screen rotation
        canvas.rotate((float) -Math.toDegrees(roll));

        // Draw horizon line
        drawHorizon(canvas);

        // draw cardinal directions
        drawPoint(canvas, 0, "N", 0xffdddddd);
        drawPoint(canvas, 90, "E", 0xffdddddd);
        drawPoint(canvas, 180, "S", 0xffdddddd);
        drawPoint(canvas, 270, "W", 0xffdddddd);

        drawPoint(canvas, 45, "NE", 0xffdddddd);
        drawPoint(canvas, 135, "SE", 0xffdddddd);
        drawPoint(canvas, 225, "SW", 0xffdddddd);
        drawPoint(canvas, 315, "NW", 0xffdddddd);

        if(currentLocation != null) {
            // TODO: Iterate over towers
            drawTower(canvas, tower);
        }
    }

    private void drawTower(Canvas canvas, Location tower) {
        // Compute bearing from current location to object
        final double bearing = currentLocation.bearingTo(tower);
        drawPoint(canvas, bearing, "B", 0xffee1111);
    }

    // TODO: take distance and height as parameter
    private void drawPoint(Canvas canvas, double bearing, String name, int color) {
        // Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
        final double relativeBearing = (Math.toDegrees(yaw) - bearing + 540.0) % 360.0 - 180.0;
        final float dx = (float) ( (-canvas.getWidth() / h_fov) * relativeBearing);
        final float dy = (float) ( (-canvas.getHeight() / v_fov) * Math.toDegrees(pitch) + canvas.getHeight() / 2);
        paint.setColor(color);
        canvas.drawText(name, dx + 10, dy - 10, paint);
        canvas.drawCircle(dx, dy, 10.0f, paint);
    }

    private void drawHorizon(Canvas canvas) {
        final float dy = (float) ( (-canvas.getHeight() / v_fov) * Math.toDegrees(pitch) + canvas.getHeight() / 2);
        paint.setColor(0xffdddddd);
        canvas.drawLine(0f - canvas.getHeight(), dy, canvas.getWidth()+canvas.getHeight(), dy, paint);
    }

    public void updateOrientation(float[] orientation) {
        yaw = orientation[0];
        pitch = orientation[1];
        roll = orientation[2];
        invalidate();
    }

    public void updateLocation(Location location) {
        currentLocation = location;
        invalidate();
    }
}
