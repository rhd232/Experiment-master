package com.jz.experiment.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	public static final String DB_NAME = "ldm_family"; // DB name
	private Context mcontext;
	private DBHelper mDbHelper;
	private SQLiteDatabase db;

	public DBHelper(Context context) {
		super(context, DB_NAME, null, 11);
		this.mcontext = context;
	}

	public DBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);

	}

	/**
	 * The operation that the user calls the first time he uses the software to get the database creation statement (SW) and then create the database 
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table if not exists family_bill(id integer primary key,time text,food text,use text,traffic text,travel text,clothes text,doctor text,laiwang text,baby text,live text,other text,remark text)";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	/* Open the database and use it if it has been opened, otherwise it will be created*/
	public DBHelper open() {
		if (null == mDbHelper) {
			mDbHelper = new DBHelper(mcontext);
		}
		db = mDbHelper.getWritableDatabase();
		return this;
	}

	/* close database  */
	public void close() {
		db.close();
		mDbHelper.close();
	}

	/**Add data */
	public long insert(String tableName, ContentValues values) {
		return db.insert(tableName, null, values);
	}

	/**QUERY DATA*/
	public Cursor findList(String tableName, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
		return db.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
	}

	public Cursor exeSql(String sql) {
		return db.rawQuery(sql, null);
	}
}