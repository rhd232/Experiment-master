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

    private static final int VERSION = 3;
    public static DbOpenHelper instance;

    public static DbOpenHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DbOpenHelper.class) {
                if (instance == null) {
                    instance = new DbOpenHelper(context);
                }
            }
        }
        return instance;
    }

    @Inject
    public DbOpenHelper(@NonNull final Context context) {
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

        //int FIRST_VERSION=1;
        // onUpgrade(db,FIRST_VERSION,VERSION);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        System.out.println("onUpgrade:oldVersion" + oldVersion);


        switch (oldVersion) {
            case 1:
                switch (newVersion) {
                    case 2:
                        //version为2 时channel_info表新增了一个字段integration_time
                        upgradeToVersion2(db);
                        break;
                    case 3:
                        upgradeToVersion2(db);
                        upgradeToVersion3(db);
                        break;
                    default:
                        break;
                }
                break;
            case 2:
                switch (newVersion) {
                    case 3:
                        upgradeToVersion3(db);
                        break;
                    default:
                        break;
                }
                break;
        }


    }

    private void upgradeToVersion2(SQLiteDatabase db) {
        String sql = "ALTER TABLE " + ChannelInfo.TABLE_NAME + " ADD COLUMN integration_time INTEGER";
        db.execSQL(sql);
    }

    /**
     *    version为3 时expe_info表新增了一个字段autoIntTime
     * @param db
     */
    private void upgradeToVersion3(SQLiteDatabase db) {
        String sql = "ALTER TABLE " + ExpeInfo.TABLE_NAME + " ADD COLUMN autoIntTime INTEGER";
        db.execSQL(sql);
    }
}