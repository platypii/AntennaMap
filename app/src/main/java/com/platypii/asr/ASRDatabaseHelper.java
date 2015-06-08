package com.platypii.asr;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class ASRDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "asr.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "CREATE TABLE asr (" +
            "id INTEGER PRIMARY KEY," +
            "latitude FLOAT," +
            "longitude FLOAT," +
            "height INTEGER" +
            ")";

    public ASRDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
        database.execSQL("CREATE INDEX latitude_index ON asr(latitude)");
        database.execSQL("CREATE INDEX longitude_index ON asr(longitude)");
        database.execSQL("CREATE INDEX height_index ON asr(height)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w("ASRDatabase", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS asr;");
        onCreate(database);
    }

} 
