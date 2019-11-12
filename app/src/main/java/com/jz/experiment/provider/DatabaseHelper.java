package com.jz.experiment.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jz.experiment.provider.DBHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1000;
    private static DBHelper instance = null;

    public DatabaseHelper(Context arg2) {
        super(arg2, "anitoa.db", null, DATABASE_VERSION, null);
    }

    public synchronized static DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE experiment(_id INTEGER PRIMARY KEY AUTOINCREMENT)");
        final int FIRST_DATABASE_VERSION = 1000;
        onUpgrade(db, FIRST_DATABASE_VERSION, DATABASE_VERSION);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion; i < newVersion; i++) {
            switch (i) {
                default:
                    break;
            }
        }
    }

}