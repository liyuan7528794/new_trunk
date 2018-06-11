package com.travel.video.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Administrator on 2017/8/2.
 */

public class VideoVoteSqlHelper extends SQLiteOpenHelper {
    private final String TAG = "VideoVoteSqlHelper";
    private String tabeName;
    /**
     * 创建数据库的构造方法
     * @param context 应用程序上下文
     * name 数据库的名字
     * factory 查询数据库的游标工厂一般情况下用sdk默认的
     * version 数据库的版本一般大于0
     */
    public VideoVoteSqlHelper(Context context, String tabeName) {
        super(context, "vote.db", null, 1);
        this.tabeName = tabeName;
    }

    /**
     * 在数据库第一次创建时会执行
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate.....");
        //创建一个数据库
        db.execSQL("create table " + tabeName + " (videoid integer primary key autoincrement ,userid varchar(30) );");
    }

    /**
     * 更新数据的时候调用的方法
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG,"onUpgrade*******");
        //增加一列
        db.execSQL("alter table "+ tabeName + " add videoid varchar(13) null");

    }


}