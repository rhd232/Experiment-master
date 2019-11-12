package com.jz.experiment.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.HashMap;

public class DataProvider extends ContentProvider {
    public final static String TABLE_EXPERIMENT = "experiment";

    public static final String AUTHORITY = "com.jz.experiment";
    public static final String PARAM_GROUP_BY = "groupBy";
    public static final String PARAM_LIMIT = "limit";

    static final String Cursor_DIR = "vnd.andorid.cursor.dir/";
    static final String Cursor_ITEM = "vnd.andorid.cursor.item/";

    static final int EXPERIMENT = 1000;
    static final int EXPERIMENT_ID = 1001;
    static final HashMap<String, String> EXPERIMENT_MAP;


    static final UriMatcher URI_MATCHER = new UriMatcher(-1);
    static SQLiteOpenHelper mOpenHelper;

    static {
        EXPERIMENT_MAP = new HashMap<String, String>();
        UriMatcher localUriMatcher = URI_MATCHER;
		
        localUriMatcher.addURI(AUTHORITY, TABLE_EXPERIMENT, EXPERIMENT);
        localUriMatcher.addURI(AUTHORITY, TABLE_EXPERIMENT + "/#", EXPERIMENT_ID);
  
        HashMap<String, String> localHashMap = EXPERIMENT_MAP;
        localHashMap.put(Experiment._ID, Experiment._ID);


    }
    public boolean onCreate() {
        mOpenHelper = getDatabaseHelper(getContext());
        return true;
    }

    private void notifyUri(Uri paramUri) {
        getContext().getContentResolver().notifyChange(paramUri, null);
    }

    public SQLiteOpenHelper getDatabaseHelper(Context paramContext) {
        try {
            if (mOpenHelper == null)
                mOpenHelper = new DatabaseHelper(paramContext);
            SQLiteOpenHelper localSQLiteOpenHelper = mOpenHelper;
            return localSQLiteOpenHelper;
        } finally {
        }
    }


    public String getType(Uri paramUri) {
        switch (URI_MATCHER.match(paramUri)) {
            default:
                return null;
            case EXPERIMENT:
                return Experiment.CONTENT_TYPE;
            case EXPERIMENT_ID:
                return Experiment.CONTENT_TYPE_ITEM;
        }
    }

    public Uri insert(Uri paramUri, ContentValues paramContentValues) {
        SQLiteDatabase localSQLiteDatabase = mOpenHelper.getWritableDatabase();
        int i = URI_MATCHER.match(paramUri);
        long l = -1L;

        while (true) {
            switch (i) {
                default:
                case EXPERIMENT:
                    l = localSQLiteDatabase.insert("experiment", null, paramContentValues);
                    break;
            }
            boolean bool = l < 0L;
            Uri localUri = null;
            if (!bool) {
                notifyUri(paramUri);
                localUri = ContentUris.withAppendedId(paramUri, l);
            }
            return localUri;
        }
    }

    public int delete(Uri paramUri, String paramString, String[] paramArrayOfString) {
        SQLiteDatabase localSQLiteDatabase = mOpenHelper.getWritableDatabase();
        int i = URI_MATCHER.match(paramUri);
        int j = 0;

        while (true) {
            switch (i) {
                default:
                    break;
                case EXPERIMENT:
                    j = localSQLiteDatabase.delete("experiment", paramString, paramArrayOfString);
                    break;
            }
            if (j > 0)
                notifyUri(paramUri);
            return j;
        }
    }

    public int update(Uri paramUri, ContentValues paramContentValues, String paramString, String[] paramArrayOfString) {
        SQLiteDatabase localSQLiteDatabase = mOpenHelper.getWritableDatabase();
        int i = URI_MATCHER.match(paramUri);
        int j = 0;
        switch (i) {
            default:
                break;
            case EXPERIMENT:
                j = localSQLiteDatabase.update("experiment", paramContentValues, paramString, paramArrayOfString);
                break;
            case EXPERIMENT_ID:
                String multifunctionId = paramUri.getPathSegments().get(1);
                j = localSQLiteDatabase.update("experiment", paramContentValues, "id = ?", new String[]{multifunctionId});
                break;
        }

        if (j > 0)
            notifyUri(paramUri);
        return j;
    }

    public Cursor query(Uri paramUri, String[] paramArrayOfString1, String paramString1, String[] paramArrayOfString2, String paramString2) {
        SQLiteDatabase localSQLiteDatabase = mOpenHelper.getReadableDatabase();
        int i = URI_MATCHER.match(paramUri);
        SQLiteQueryBuilder localSQLiteQueryBuilder = new SQLiteQueryBuilder();
        while (true) {
            switch (i) {
                default:
                    break;
                case EXPERIMENT: {
                    localSQLiteQueryBuilder.setTables("multifunction");
                    localSQLiteQueryBuilder.setProjectionMap(EXPERIMENT_MAP);
                    break;
                }
            }
            Cursor localCursor = localSQLiteQueryBuilder.query(localSQLiteDatabase, paramArrayOfString1, paramString1, paramArrayOfString2, null, null, paramString2);
            localCursor.setNotificationUri(getContext().getContentResolver(), paramUri);
            return localCursor;
        }
    }

    public static final class Experiment {
        public static final Uri CURRENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_EXPERIMENT);
        public static final String CONTENT_TYPE = Cursor_DIR+TABLE_EXPERIMENT;
        public static final String CONTENT_TYPE_ITEM = Cursor_ITEM +TABLE_EXPERIMENT;
        public static final String _ID = "_id";
    }
}