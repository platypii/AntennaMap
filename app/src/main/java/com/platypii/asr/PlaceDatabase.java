package com.platypii.asr;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class PlaceDatabase {
    private static final String TAG = "PlaceDatabase";

    private static PlaceDatabaseHelper helper;
    private static SQLiteDatabase database;
    private static boolean started = false;
    private static boolean loading = false;

    static synchronized void start(Context appContext) {
        // Start database
        if (!started) {
            helper = new PlaceDatabaseHelper(appContext);
            database = helper.getReadableDatabase();
            Log.w(TAG, "Database started");
            started = true;
        } else {
            Log.e(TAG, "Already started");
            if (database == null) {
                database = helper.getReadableDatabase();
            }
        }
    }

    /**
     * Return true iff there is data ready to query
     */
    static boolean isReady() {
        if (!started) {
            Log.i(TAG, "Not ready: database is not started");
            return false;
        } else if (loading) {
            Log.i(TAG, "Not ready: database is loading");
            return false;
        } else {
            // Return true iff there is data ready to query
            final int rows = getRows();
            Log.w(TAG, "Database ready with " + rows + " rows");
            return rows > 0;
        }
    }

    private static int getRows() {
        // Assumes started and not loading
        final Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM place", null);
        cursor.moveToFirst();
        final int rows = cursor.getInt(0);
        cursor.close();
        return rows;
    }

    static void loadDataAsync(final Iterator<Place> places) {
        if (started && !loading) {
            loading = true;
            new LoadDataTask(places).execute();
        } else {
            Log.e(TAG, "Unexpected load data callback");
        }
    }

    private static class LoadDataTask extends AsyncTask<Void, Integer, Void> {
        private final Iterator<Place> places;
        private int totalSize = -1;

        LoadDataTask(Iterator<Place> places) {
            this.places = places;
        }

        @Override
        protected void onPreExecute() {
            MapsActivity.startProgress("Loading data...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.w(TAG, "Loading database from cache file");
            // Get row count
            totalSize = PlaceFile.rowCount();
            Log.w(TAG, "Rows: " + totalSize);
            int count = 0;
            publishProgress(0);

            // Write to database
            database.close();
            database = null;
            final SQLiteDatabase writableDatabase = helper.getWritableDatabase();
            writableDatabase.beginTransaction();
            writableDatabase.execSQL("DELETE FROM place");
            // Prepared statement
            final String insertString = "INSERT INTO place (latitude, longitude, altitude, url) VALUES (?,?,?,?)";
            final SQLiteStatement insertStatement = writableDatabase.compileStatement(insertString);
            while (places.hasNext()) {
                final Place record = places.next();
                if (record != null) {
                    // Add to database
                    insertStatement.bindDouble(1, record.latitude);
                    insertStatement.bindDouble(2, record.longitude);
                    insertStatement.bindDouble(3, record.altitude);
                    insertStatement.bindString(4, record.url);
                    insertStatement.executeInsert();
                    // Update progress dialog
                    if (count % 100 == 0) {
                        if (count % 1000 == 0) {
                            Log.i(TAG, "Populating database row " + count);
                        }
                        publishProgress(count);
                    }
                    count++;
                }
            }
            writableDatabase.setTransactionSuccessful();
            writableDatabase.endTransaction();
            writableDatabase.close();
            database = helper.getReadableDatabase();
            loading = false;
            Log.w(TAG, "Database loaded from file");
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            MapsActivity.updateProgress("Loading data...", progress[0], totalSize);
        }

        @Override
        protected void onPostExecute(Void result) {
            MapsActivity.dismissProgress();
            ASR.ready();
        }
    }

    /**
     * Search for the N tallest towers in view
     */
    static List<Place> query(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude, int limit) {
        if (started && !loading) {
            final String params[] = {
                    Double.toString(minLatitude),
                    Double.toString(maxLatitude),
                    Double.toString(minLongitude),
                    Double.toString(maxLongitude),
                    Integer.toString(limit)
            };
            final String longitudeQuery = (minLongitude <= maxLongitude) ?
                    " AND ? < longitude AND longitude < ?" :
                    " AND ? < longitude OR longitude < ?";
            final Cursor cursor = database.rawQuery(
                    "SELECT latitude, longitude, altitude, url FROM place" +
                            " WHERE ? < latitude AND latitude < ?" +
                            longitudeQuery +
                            " ORDER BY altitude DESC LIMIT ?", params);
            final ArrayList<Place> records = new ArrayList<>();
            while (cursor.moveToNext()) {
                final double latitude = cursor.getDouble(0);
                final double longitude = cursor.getDouble(1);
                final double altitude = cursor.getDouble(2);
                final String url = cursor.getString(3);
                final Place record = new Place(latitude, longitude, altitude, url);
                records.add(record);
            }
            cursor.close();
            return records;
        } else if (loading) {
            Log.w(TAG, "Query attempted while still loading");
            return null;
        } else {
            Log.e(TAG, "Query attempted on uninitialized database");
            return null;
        }
    }

}
