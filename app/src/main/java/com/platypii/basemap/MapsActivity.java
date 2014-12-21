package com.platypii.basemap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ProgressBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnCameraChangeListener, GoogleMap.OnInfoWindowClickListener{

    private static MapsActivity instance = null;

    private final Handler handler = new Handler();

    private GoogleMap map; // Might be null if Google Play services APK is not available.
    private ProgressBar progressSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Find view elements
        progressSpinner = (ProgressBar) this.findViewById(R.id.progressSpinner);
        progressSpinner.setVisibility(ProgressBar.GONE);

        // Initialize map
        setUpMapIfNeeded();

        // Export this
        MapsActivity.instance = this;

        // Initialize Services in the background
        ASR.init(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressDialog.dismiss();
        mProgressDialog = null;
        MapsActivity.instance = null;
    }

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
            if(instance.mProgressDialog == null) {
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
        if (instance != null) {
            if(instance.mProgressDialog != null && instance.mProgressDialog.isShowing()) {
                instance.mProgressDialog.dismiss();
            }
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #map} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    private void setUpMap() {
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        final LatLng home = new LatLng(47.61, -122.34);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(home, 10));
//        map.addMarker(new MarkerOptions().position(home).title("Home"));

        // Drag listener
        map.setOnCameraChangeListener(this);
        map.setOnInfoWindowClickListener(this);
    }

    // Drag listener
    private static final long QUERY_WAIT_TIME = 500; // millis
    private boolean running = false;
    private long lastDrag = 0;

    /**
     * Class to update map only once every QUERY_WAIT_TIME milliseconds
     */
    private final Runnable runnable = new Runnable() {
        @Override public void run() {
            if(System.currentTimeMillis() < lastDrag + QUERY_WAIT_TIME) {
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
    public void onCameraChange(CameraPosition position) {
        lastDrag = System.currentTimeMillis();
        if(!running) {
            running = true;
            runnable.run();
        }
    }

    private final HashMap<ASRRecord,Marker> markers = new HashMap<>();
    private boolean querying = false;
    private void mUpdateMap() {
        final LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        if(!querying) {
            querying = true;

            // Query in the background
            new AsyncTask<Void,Void,List<ASRRecord>>() {
                @Override protected void onPreExecute() {
                    progressSpinner.setVisibility(ProgressBar.VISIBLE);
                }
                @Override protected List<ASRRecord> doInBackground(Void... params) {
                    return ASR.query(bounds);
                }
                @Override protected void onPostExecute(List<ASRRecord> towers) {
                    if (towers != null) {
                        // Remove stale markers
                        final Set<ASRRecord> toDelete = new HashSet<>();
                        for(ASRRecord tower : markers.keySet()) {
                            if (!towers.contains(tower)) {
                                markers.get(tower).remove();
                                toDelete.add(tower);
                            }
                        }
                        for(ASRRecord record : toDelete) {
                            markers.remove(record);
                        }
                        // Add new markers
                        for (ASRRecord tower : towers) {
                            if(!markers.containsKey(tower)) {
                                // Create new marker
                                final float alpha = (float)(tower.height) / 1260f + 0.5f;
                                final BitmapDescriptor icon = Assets.getSizedIcon(MapsActivity.this, tower.height);
                                final Marker marker = map.addMarker(
                                        new MarkerOptions()
                                                .position(tower.latLng())
                                                .icon(icon)
                                                .title(Convert.toFeet(tower.height))
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
            }.execute();
        }
    }
    public static void updateMap() {
        if(instance != null) {
            instance.mUpdateMap();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.w("Map", "Clicked marker info");
        // Find which marker
        for(Map.Entry<ASRRecord,Marker> entry : markers.entrySet()) {
            if(entry.getValue().equals(marker)) {
                final ASRRecord record = entry.getKey();
                // Open url
                Log.w("Map", "Opening url for tower " + record.id);
                final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(record.url()));
                startActivity(browserIntent);
            }
        }
    }
}
