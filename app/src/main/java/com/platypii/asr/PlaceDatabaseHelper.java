package com.platypii.asr;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

class PlaceDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "PlaceDatabaseHelper";

    private static final String DATABASE_NAME = "places.db";
    private static final int DATABASE_VERSION = 3;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "CREATE TABLE place (" +
            "latitude FLOAT," +
            "longitude FLOAT," +
            "altitude FLOAT," +
            "url VARCHAR(80)" +
            ")";

    PlaceDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase database) {
        Log.w(TAG, "Creating database");
        database.execSQL(DATABASE_CREATE);
        database.execSQL("CREATE INDEX latitude_index ON place(latitude)");
        database.execSQL("CREATE INDEX longitude_index ON place(longitude)");
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS place");
        onCreate(database);
    }

} 
