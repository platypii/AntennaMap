package com.platypii.asr.augmented;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;

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

        // translate 0,0 to center
        canvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);
        // use roll for screen rotation
        canvas.rotate((float) -Math.toDegrees(roll));

        // Draw horizon line
        drawHorizon(canvas);
        // Draw cardinal directions
        drawHorizonPoint(canvas, 0, "N", 0xffdddddd);
        drawHorizonPoint(canvas, 90, "E", 0xffdddddd);
        drawHorizonPoint(canvas, 180, "S", 0xffdddddd);
        drawHorizonPoint(canvas, 270, "W", 0xffdddddd);

        if(currentLocation != null) {
            // TODO: Iterate over towers
            drawTower(canvas, newLocation(47.61219, -122.34534, 135), "B");
            drawTower(canvas, newLocation(47.633333, -122.356667, 185), "A1");
            drawTower(canvas, newLocation(47.631944, -122.353889, 171), "A2");
            drawTower(canvas, newLocation(47.631667, -122.350833, 173), "A3");
        }
    }

    private static Location newLocation(double lat, double lon, double alt) {
        final Location loc = new Location("l");
        loc.setLatitude(lat);
        loc.setLongitude(lon);
        loc.setAltitude(alt);
        return loc;
    }

    private void drawTower(Canvas canvas, Location tower, String name) {
        // Compute bearing from current location to object
        final double bearing = currentLocation.bearingTo(tower);
        final double distance = currentLocation.distanceTo(tower);
        final double height = 100; // meters

        final float dx = getX(canvas.getWidth(), bearing);
        final float dy = getY(canvas.getHeight(), distance, height);
        final float dy0 = getY(canvas.getHeight(), distance, 0);

        paint.setColor(0xffee1111);
        canvas.drawText(name, dx + 10, dy - 10, paint);
        canvas.drawCircle(dx, dy, 10.0f, paint);
        canvas.drawLine(dx, dy0, dx, dy, paint);
    }

    private float getX(int width, double bearing) {
        // Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
        final double relativeBearing = (Math.toDegrees(yaw) - bearing + 540.0) % 360.0 - 180.0;
        return (float) ( (-width / h_fov) * relativeBearing);
    }

    private float getY(int height, double targetDistance, double targetHeight) {
        // Target angle above horizon
        final double targetPitch = Math.asin(targetHeight / targetDistance);
        return (float) ( (-height / v_fov) * Math.toDegrees(pitch + targetPitch));
    }
    // Horizon y offset
    private float getY(int height) {
        return (float) ( (-height / v_fov) * Math.toDegrees(pitch));
    }

    private void drawHorizon(Canvas canvas) {
        final float dy0 = getY(canvas.getHeight());
        paint.setColor(0xffdddddd);
        final float maxDimension = Math.max(canvas.getWidth(), canvas.getHeight());
        canvas.drawLine(-maxDimension, dy0, maxDimension, dy0, paint);
    }

    private void drawHorizonPoint(Canvas canvas, double bearing, String name, int color) {
        paint.setColor(color);
        final float dx = getX(canvas.getWidth(), bearing);
        final float dy = getY(canvas.getHeight());
        canvas.drawText(name, dx + 10, dy - 10, paint);
        canvas.drawCircle(dx, dy, 10.0f, paint);
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
