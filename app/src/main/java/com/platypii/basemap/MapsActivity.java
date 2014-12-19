package com.platypii.basemap;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    private GoogleMap map; // Might be null if Google Play services APK is not available.
    private ProgressBar progressSpinner;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Find view elements
        progressSpinner = (ProgressBar) this.findViewById(R.id.progressSpinner);

        // Initialize map
        setUpMapIfNeeded();

        // Initialize Services in the background
        new AsyncTask<Void,Void,Void>() {
            @Override protected void onPreExecute() {
                mProgressDialog = ProgressDialog.show(MapsActivity.this, "","Loading data...");
            }
            @Override protected Void doInBackground(Void... params) {
                ASR.init(MapsActivity.this);
                return null;
            }
            @Override protected void onPostExecute(Void param) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
                populateMap();
            }
        }.execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
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
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    private void setUpMap() {
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        final LatLng home = new LatLng(47.61, -122.34);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(home, 10));
        map.addMarker(new MarkerOptions().position(home).title("Home"));

        // Drag listener
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                populateMap();
            }
        });

//        populateMap();
    }

    private boolean querying = false;
    private long lastQuery = 0;
    private static final long QUERY_WAIT_TIME = 5000; // millis
    private void populateMap() {
        final LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        if(!querying && System.currentTimeMillis() - lastQuery > QUERY_WAIT_TIME) {
            querying = true;

            // Query in the background
            new AsyncTask<Void,Void,List<ASRRecord>>() {
                @Override protected void onPreExecute() {
                    progressSpinner.setVisibility(ProgressBar.VISIBLE);
                }
                @Override protected List<ASRRecord> doInBackground(Void... params) {
                    try {
                        Log.w("MAP", "Querying ASR");
                        return ASR.query(bounds);
                    } catch (IOException e) {
                        Log.e("MAP", "ASR query exception", e);
                        return null;
                    }
                }
                @Override protected void onPostExecute(List<ASRRecord> towers) {
                    map.clear();
                    if (towers != null) {
                        for (ASRRecord tower : towers) {
                            map.addMarker(new MarkerOptions().position(tower.latLng()).title(Convert.toFeet(tower.height)));
                        }
                    }
                    progressSpinner.setVisibility(ProgressBar.GONE);
                    lastQuery = System.currentTimeMillis();
                    querying = false;
                }
            }.execute();
        }
    }
}
