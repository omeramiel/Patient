package com.lessons.android.se.omeram.patients;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PatientDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "patient.db";
    private static final int DATABASE_VERSION = 1;
    private String mDbCreate;
    private String mTable;

    public PatientDbHelper(Context context, String dbCreate, String table) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mDbCreate = dbCreate;
        mTable = table;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(mDbCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + mTable);
        onCreate(db);
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}
