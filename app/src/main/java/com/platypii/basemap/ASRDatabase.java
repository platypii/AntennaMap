package com.platypii.basemap;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ASRDatabase {

    private static ASRDatabaseHelper helper;
    private static SQLiteDatabase database;
    private static boolean started = false;
    private static boolean loaded = false;

    public static void start(Context context) {
        helper = new ASRDatabaseHelper(context);
        database = helper.getReadableDatabase();
        Log.w("ASRDatabase", "Database started");
        started = true;
        loaded = !isEmpty();
    }

    public static void loadDataAsync(final Iterator<ASRRecord> asrIterator, final Context appContext, final MapsActivity activity) {
        new AsyncTask<Void, Integer, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setMax(1);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage("Loading data...");
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... params) {
                Log.w("ASRDatabase", "Loading database");
                // Get row count
                mProgressDialog.setMax(ASRFile.rowCount());
                mProgressDialog.setIndeterminate(false);

                // Write to database
                database.close();
                database = null;
                final SQLiteDatabase writableDatabase = helper.getWritableDatabase();
                int count = 0;
                writableDatabase.execSQL("BEGIN TRANSACTION");
                while (asrIterator.hasNext()) {
                    final ASRRecord record = asrIterator.next();
                    if(record != null) {
                        // Add to database
                        writableDatabase.execSQL("INSERT OR IGNORE INTO asr VALUES (" + record.id + "," + record.latitude + "," + record.longitude + "," + record.height + ")");
                        if (count % 100 == 0) {
                            Log.i("ASRDatabase", "Populating database row " + count);
                            publishProgress(count);
                        }
                        count++;
                    }
                }
                writableDatabase.execSQL("COMMIT");
                writableDatabase.close();
                database = helper.getReadableDatabase();
                loaded = true;
                Log.w("ASRDatabase", "Database loaded from file");
                return null;
            }
            @Override
            protected void onProgressUpdate(Integer... progress) {
                super.onProgressUpdate(progress);
                // if we get here, length is known, now set indeterminate to false
                mProgressDialog.setProgress(progress[0]);
            }
            @Override
            protected void onPostExecute(Void result) {
                mProgressDialog.dismiss();
                ASR.databaseLoaded(appContext, activity);
            }
        }.execute();
    }

    public static boolean isEmpty() {
        final Cursor cursor = database.rawQuery("SELECT COUNT(id) FROM asr", null);
        cursor.moveToFirst();
        final int count = cursor.getInt(0);
        return count == 0;
    }

    /**
     * Search for the N tallest towers in view
     */
    public static List<ASRRecord> query(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude, int limit) {
        if(loaded) {
            final String params[] = {"" + minLatitude, "" + maxLatitude, "" + minLongitude, "" + maxLongitude, "" + limit};
            final String longitudeQuery = (minLongitude < maxLongitude)?
                    " AND ? < longitude AND longitude < ?" :
                    " AND ? < longitude OR longitude < ?";
            final Cursor cursor = database.rawQuery(
                    "SELECT * FROM asr" +
                            " WHERE ? < latitude AND latitude < ?" +
                            longitudeQuery +
                            " ORDER BY height DESC LIMIT ?", params);
            ArrayList<ASRRecord> records = new ArrayList<>();
            while (cursor.moveToNext()) {
                final long id = cursor.getLong(0);
                final double latitude = cursor.getDouble(1);
                final double longitude = cursor.getDouble(2);
                final double height = cursor.getDouble(3);
                ASRRecord record = new ASRRecord(id, latitude, longitude, height);
                records.add(record);
            }
            return records;
        } else {
            Log.e("ASRDatabase", "Query attempted on uninitialized database");
            return null;
        }
    }

}
