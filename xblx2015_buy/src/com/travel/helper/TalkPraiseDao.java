package com.travel.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/8/2.
 */

public class TalkPraiseDao {
    private TalkPraiseSqlHelper myDBHelper;
    public String tag = "TalkPraiseDao.class";
    private String tabeName;

    //在new出来的时候就实现myDBHelper初始化
    public TalkPraiseDao(Context context, String id) {
        tabeName = "talk" + id;
        myDBHelper = new TalkPraiseSqlHelper(context, tabeName);
    }

    //增加
    public void addPerson(int videoId, String userId) {
        if(findPersonFormVideoId(videoId)){
            updatePerson(videoId, userId);
            return;
        }
        SQLiteDatabase database = myDBHelper.getWritableDatabase();
        //先判断数据库是否可用
        if (database.isOpen()) {
            //推荐如下写法
            database.execSQL("insert into " + tabeName + " (videoid,userid) values(?,?)", new Object[]{videoId, userId});
            database.close();
        }
    }

    //查找
    public boolean findPersonFormVideoId(int videoId) {
        boolean result = false;
        SQLiteDatabase database = myDBHelper.getReadableDatabase();
        if (database.isOpen()) {
//            database.execSQL("select * from " + tabeName +"='"+videoId+"'");

            Cursor cursor = database.rawQuery("select * from "+ tabeName +" where videoid=?", new String[]{videoId+""});
            if (cursor.moveToFirst()) {//游标是否移动到下一行,如果是,那说明有数据返回
                int videoid = cursor.getColumnIndex("videoid");
                cursor.close();
                result = true;
            } else {
                result = false;

            }
            database.close();
        }
        return result;
    }
    //查找
    public boolean findPerson(String phone) {
        boolean result = false;
        SQLiteDatabase database = myDBHelper.getReadableDatabase();
        if (database.isOpen()) {
            database.execSQL("select * from " + tabeName +"='"+phone+"'");

            Cursor cursor = database.rawQuery("select * from "+ tabeName +" where phone=?", new String[]{phone});
            if (cursor.moveToFirst()) {//游标是否移动到下一行,如果是,那说明有数据返回
                Log.d(tag, "count:" + cursor.getColumnCount());
                int nameIndex = cursor.getColumnIndex("name");
                Log.d(tag, "name:" + cursor.getString(nameIndex));
                cursor.close();
                result = true;
            } else {
                result = false;

            }
            database.close();
        }
        return result;
    }

    //删除一条数据
    public void deletePerson(String phone) {
        SQLiteDatabase database = myDBHelper.getWritableDatabase();
        if (database.isOpen()) {
            database.execSQL("delete from " + tabeName +" where videoid=?", new Object[]{phone});
        }
        database.close();
    }

    //更新一条数据
    public void updatePerson(int videoId, String userId) {
        SQLiteDatabase database = myDBHelper.getWritableDatabase();
        if (database.isOpen()) {
            ContentValues cv = new ContentValues();
            cv.put("userid", userId);
            database.update(tabeName, cv, "videoid=?", new String[]{videoId+""});
//            database.execSQL("update person set userid=? where videoid=?", new Object[]{userId, videoId});
        }
        database.close();
    }

    //查找所有person
    public HashMap<Integer, String> findAllPerson(){
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        SQLiteDatabase database = myDBHelper.getReadableDatabase();
        if(!tableIsExists(myDBHelper.getReadableDatabase(), tabeName))
            myDBHelper.onCreate(myDBHelper.getWritableDatabase());
        if(database.isOpen()){
//            Cursor cursor = database.query(tabeName, null, null, null, null, null, null);
            Cursor cursor = database.rawQuery("select * from "+tabeName, null);
            while(cursor.moveToNext()){
                int nameIndex = cursor.getColumnIndex("videoid");
                int phoneIndex = cursor.getColumnIndex("userid");
                int videoId = cursor.getInt(nameIndex);
                String userId = cursor.getString(phoneIndex);

                map.put(videoId, userId);
            }

        }
        database.close();
        return map;
    }

    public boolean tableIsExists(SQLiteDatabase db, String name){
        boolean result = false;
        if(name == null){
            result = false;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from Sqlite_master where type='table' and name='" + name.trim() + "'";
            cursor = db.rawQuery(sql,null);
            if(cursor.moveToNext()){
                int count = cursor.getInt(0);
                if(count > 0)
                    result = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
