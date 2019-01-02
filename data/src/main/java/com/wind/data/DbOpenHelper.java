package com.wind.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.wind.data.base.bean.User;
import com.wind.data.expe.table.ChannelInfo;
import com.wind.data.expe.table.ExpeInfo;
import com.wind.data.expe.table.SampleInfo;
import com.wind.data.expe.table.StageInfo;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DbOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "w_db";

    private static final int VERSION = 1;
    public static DbOpenHelper instance;
    public static DbOpenHelper getInstance(Context context){
        if (instance==null){
            synchronized (DbOpenHelper.class){
                if (instance==null){
                    instance=new DbOpenHelper(context);
                }
            }
        }
        return instance;
    }
    @Inject
    public  DbOpenHelper(@NonNull final Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    // Better than static final field -> allows VM to unload useless String
    // Because you need this string only once per application life on the device

    @Override
    public void onCreate(final SQLiteDatabase db) {

       //用户表
        db.execSQL(User.CREATE_TABLE);
        //历史实验简表
        db.execSQL(ExpeInfo.CREATE_TABLE);
        db.execSQL(ChannelInfo.CREATE_TABLE);
        db.execSQL(SampleInfo.CREATE_TABLE);
        db.execSQL(StageInfo.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // no impl
    }
}