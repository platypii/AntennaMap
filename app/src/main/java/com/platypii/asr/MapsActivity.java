package com.platypii.asr;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ProgressBar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends Activity implements GoogleMap.OnCameraMoveListener, GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback, GoogleMap.OnCameraIdleListener {
    private static final String TAG = "Map";

    private static MapsActivity instance = null;

    private final Handler handler = new Handler();

    private GoogleMap map; // Might be null if Google Play services APK is not available.
    private ProgressBar progressSpinner;

    private static boolean firstLoad = true;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Find view elements
        progressSpinner = this.findViewById(R.id.progressSpinner);
        progressSpinner.setVisibility(ProgressBar.GONE);

        // Initialize map
        final MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Save this instance to a static variable to make it possible to refresh map
        MapsActivity.instance = this;

        // Initialize Services in the background
        ASR.init(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        // Don't leak this instance
        MapsActivity.instance = null;
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Enable location on map
            try {
                map.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                Log.e(TAG, "Error enabling location", e);
            }
        } else {
            // request the missing permissions
            final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_LOCATION);
        }
        final LatLng center = map.getCameraPosition().target;
        if (firstLoad || (Math.abs(center.latitude) < 0.1 && Math.abs(center.longitude) < 0.1)) {
            final Location myLocation = getMyLocation();
            if (myLocation != null
                    && 8 < myLocation.getLatitude() && myLocation.getLatitude() < 76
                    && -170 < myLocation.getLongitude() && myLocation.getLongitude() < -60) {
                final LatLng home = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                Log.w(TAG, "Centering map on " + home);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 8));
            } else {
                // final LatLng seattle = new LatLng(47.61, -122.34);
                final LatLng usa = new LatLng(41.2, -120.5);
                Log.w(TAG, "Centering map on default view " + usa);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(usa, 5));
            }
            firstLoad = false;
        }

        // Drag listener
        map.setOnCameraMoveListener(this);
        map.setOnCameraIdleListener(this);
        map.setOnInfoWindowClickListener(this);

        // Get initial update
        mUpdateMap();

        Log.w("Map", "Map ready");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length == 1 &&
                    Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[0]) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (map != null) {
                    try {
                        map.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        Log.e(TAG, "Error enabling location", e);
                    }
                }
            }
        }
    }

    /**
     * Gets the users most recent location
     */
    private Location getMyLocation() {
        Location myLocation = null;

        // Get location from GPS if it's available
        final LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Log.w(TAG, "Permission denied for GPS location");
        }

        // Location wasn't found, check the next most accurate place for the current location
        if (myLocation == null) {
            final Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            // Finds a provider that matches the criteria
            final String provider = lm.getBestProvider(criteria, true);
            if (provider != null) {
                // Use the provider to get the last known location
                try {
                    myLocation = lm.getLastKnownLocation(provider);
                } catch (SecurityException e) {
                    Log.w(TAG, "Permission denied for GPS location");
                }
            } else {
                Log.w(TAG, "No location provider found");
            }
        }

        if (myLocation == null) {
            return null;
        } else if (Math.abs(myLocation.getLatitude()) < 0.01 && Math.abs(myLocation.getLongitude()) < 0.01) {
            // Unlikely coordinate
            return null;
        } else {
            return myLocation;
        }
    }

    // Drag listener
    private static final long QUERY_WAIT_TIME = 500; // millis
    private boolean running = false;
    private long lastDrag = System.currentTimeMillis();

    /**
     * Class to update map only once every QUERY_WAIT_TIME milliseconds
     */
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() < lastDrag + QUERY_WAIT_TIME) {
                mUpdateMap();
                // Schedule again
                handler.postDelayed(runnable, QUERY_WAIT_TIME);
            } else {
                // Let it die
                running = false;
            }
        }
    };

    @Override
    public void onCameraMove() {
//        Log.d(TAG, "onCameraMove()");
        mapMoved();
    }

    @Override
    public void onCameraIdle() {
//        Log.d(TAG, "onCameraIdle()");
        handler.removeCallbacks(runnable);
        mUpdateMap();
    }

    /**
     * Call this when the map has moved, or should be updated
     */
    private void mapMoved() {
        lastDrag = System.currentTimeMillis();
        if (!running) {
            running = true;
            runnable.run();
        }
    }

    private final HashMap<Place, Marker> markers = new HashMap<>();
    private boolean querying = false;

    private void mUpdateMap() {
        if (map == null) {
            Log.e(TAG, "Update called, but map not ready");
        } else if (querying) {
            Log.w(TAG, "Update called, but still querying");
            mapMoved(); // Ensure that we retry
        } else {
            final LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            if (bounds.southwest.latitude == 0 && bounds.southwest.longitude == 0 && bounds.northeast.latitude == 0 && bounds.northeast.longitude == 0) {
                Log.e(TAG, "Update called on empty view bounds");
            } else {
                Log.d(TAG, "Updating antennas");
                querying = true;

                // Query in the background
                new UpdateMapTask(bounds).execute();
            }
        }
    }

    public static void updateMap() {
        if (instance != null) {
            instance.mUpdateMap();
        }
    }

    private class UpdateMapTask extends AsyncTask<Void, Void, List<Place>> {
        private final LatLngBounds bounds;
        UpdateMapTask(LatLngBounds bounds) {
            this.bounds = bounds;
        }
        @Override
        protected void onPreExecute() {
            progressSpinner.setVisibility(ProgressBar.VISIBLE);
        }
        @Override
        protected List<Place> doInBackground(Void... params) {
            return ASR.query(bounds);
        }
        @Override
        protected void onPostExecute(List<Place> towers) {
            if (towers != null) {
                // Remove stale markers
                final Set<Place> toDelete = new HashSet<>();
                for (Place tower : markers.keySet()) {
                    if (!towers.contains(tower)) {
                        markers.get(tower).remove();
                        toDelete.add(tower);
                    }
                }
                for (Place record : toDelete) {
                    markers.remove(record);
                }
                // Add new markers
                for (Place tower : towers) {
                    if (!markers.containsKey(tower)) {
                        // Create new marker
                        final float alpha = ((float) (tower.altitude) / 630f) * 0.4f + 0.6f;
                        final BitmapDescriptor icon = Assets.getSizedIcon(MapsActivity.this, tower.altitude);
                        final Marker marker = map.addMarker(
                                new MarkerOptions()
                                        .position(tower.latLng())
                                        .icon(icon)
                                        .title(Convert.toFeet(tower.altitude))
                                        .alpha(alpha)
                        );
                        markers.put(tower, marker);
                    }
                }
            } else {
                map.clear();
            }
            progressSpinner.setVisibility(ProgressBar.GONE);
            querying = false;
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.w(TAG, "Clicked marker info");
        // Find which marker
        for (Map.Entry<Place, Marker> entry : markers.entrySet()) {
            if (entry.getValue().equals(marker)) {
                final Place place = entry.getKey();
                // Open url
                Log.w(TAG, "Opening url for tower " + place.id);
                final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(place.url()));
                startActivity(browserIntent);
            }
        }
    }

    /**
     * Progress spinner stuff
     */
    private ProgressDialog mProgressDialog;

    public static void startProgress(String message) {
        if (instance != null) {
            instance.mProgressDialog = new ProgressDialog(instance);
            instance.mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            instance.mProgressDialog.setIndeterminate(true);
            instance.mProgressDialog.setMax(1);
            instance.mProgressDialog.setCancelable(false);
            instance.mProgressDialog.setMessage(message);
            instance.mProgressDialog.show();
        }
    }

    public static void updateProgress(String message, int progress, int total) {
        if (instance != null) {
            if (instance.mProgressDialog == null) {
                instance.mProgressDialog = new ProgressDialog(instance);
                instance.mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                instance.mProgressDialog.setCancelable(false);
                instance.mProgressDialog.setIndeterminate(false);
                instance.mProgressDialog.setMax(total);
                instance.mProgressDialog.setProgress(progress);
                instance.mProgressDialog.setMessage(message);
                instance.mProgressDialog.show();
            } else {
                instance.mProgressDialog.setIndeterminate(false);
                instance.mProgressDialog.setMax(total);
                instance.mProgressDialog.setProgress(progress);
                instance.mProgressDialog.setMessage(message);
            }
        }
    }

    public static void dismissProgress() {
        if (instance != null && instance.mProgressDialog != null && instance.mProgressDialog.isShowing()) {
            instance.mProgressDialog.dismiss();
        }
    }

}
