package com.platypii.asr.augmented;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
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
    private static final ASRRecord tower = new ASRRecord(0, 47.61219, -122.34534, 135);
    private static final float towerBearing = 106; // degrees

    public ExitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setTextSize(64);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // use roll for screen rotation
        canvas.rotate((float) -Math.toDegrees(roll));

        // Draw horizon line
        drawHorizon(canvas);

        // draw our point -- we've rotated and translated this to the right spot already
        drawPoint(canvas, 0, "N", 0xffdddddd);
        drawPoint(canvas, 90, "E", 0xffdddddd);
        drawPoint(canvas, 180, "S", 0xffdddddd);
        drawPoint(canvas, 270, "W", 0xffdddddd);

        drawPoint(canvas, 45, "NE", 0xffdddddd);
        drawPoint(canvas, 135, "SE", 0xffdddddd);
        drawPoint(canvas, 225, "SW", 0xffdddddd);
        drawPoint(canvas, 315, "NW", 0xffdddddd);

        // TODO: take distance and height as parameter
        drawPoint(canvas, towerBearing, "B", 0xffee1111);
    }

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

    public void update(float[] orientation) {
        yaw = orientation[0];
        pitch = orientation[1];
        roll = orientation[2];
        invalidate();
    }
}
