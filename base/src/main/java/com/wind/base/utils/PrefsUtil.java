package com.wind.base.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PrefsUtil {
	private static final String DEFAULT_FILE_NAME = "config";
	//protected static final String DEFAULT_PREFERENCE="default_preference";

	public static String getString(Context context, String file, String key, String defValue) {
		SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
		if (sp != null) {
			String value = sp.getString(key, defValue);
			return value;
		}
		return defValue;
	}

	public static void setString(Context context, String file, String key, String value) {
		SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}
	public static String getString(Context context, String key, String defValue) {
		SharedPreferences sp = context.getSharedPreferences(DEFAULT_FILE_NAME, Context.MODE_PRIVATE);
		if (sp != null) {
			String value = sp.getString(key, defValue);
			return value;
		}
		return defValue;
	}

	public static void setString(Context context, String key, String value) {
		SharedPreferences sp = context.getSharedPreferences(DEFAULT_FILE_NAME, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}
	public static boolean getBoolean(Context context, String key, boolean defValue) {
		SharedPreferences sp = context.getSharedPreferences(DEFAULT_FILE_NAME, Context.MODE_PRIVATE);
		if (sp != null) {
			return sp.getBoolean(key, defValue);
		}
		return defValue;
	}

	public static void setBoolean(Context context, String key, boolean value) {
		SharedPreferences sp = context.getSharedPreferences(DEFAULT_FILE_NAME, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public static int getInt(Context context, String file, String key, int defValue) {
		SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
		if (sp != null) {
			return sp.getInt(key, defValue);
		}
		return 0;
	}

	public static long getLong(Context context, String key, long defValue) {
		SharedPreferences sp = context.getSharedPreferences(DEFAULT_FILE_NAME, Context.MODE_PRIVATE);
		if (sp != null) {
			return sp.getLong(key, defValue);
		}
		return 0;
	}
	public static int getInt(Context context, String key, int defValue) {
		SharedPreferences sp = context.getSharedPreferences(DEFAULT_FILE_NAME, Context.MODE_PRIVATE);
		if (sp != null) {
			return sp.getInt(key, defValue);
		}
		return 0;
	}
	public static void setInt(Context context, String file, String key, int value) {
		SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt(key, value);
		editor.commit();
	}


	public static void setLong(Context context, String key, long value) {
		SharedPreferences sp = context.getSharedPreferences(DEFAULT_FILE_NAME, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putLong(key, value);
		editor.commit();
	}
	public static void setInt(Context context, String key, int value) {
		SharedPreferences sp = context.getSharedPreferences(DEFAULT_FILE_NAME, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	public static int getDouble(Context context, String file, String key, int defValue) {
		SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
		if (sp != null) {
			return sp.getInt(key, defValue);
		}
		return 0;
	}

	public static void setDouble(Context context, String file, String key, int value) {
		SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt(key, value);
		editor.commit();
	}

}
