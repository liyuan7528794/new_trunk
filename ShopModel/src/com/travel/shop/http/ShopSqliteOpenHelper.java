package com.travel.shop.http;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.travel.shop.bean.CalendarBean;

import java.util.ArrayList;

/**
 * 第二个选项卡“出发”的数据库
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/04
 */
public class ShopSqliteOpenHelper extends SQLiteOpenHelper {

    public static final String NAME = "Shop.db";
    public static final int VERSION = 3;
    public static final String TABLENAME_SEARCH = "Search";
    public static final String TABLENAME_CALENDAR = "Calendar";
    public static final String TABLENAME_PEOPLE_INFO = "PeopleInfo";

    private String mSearch;
    private ArrayList<CalendarBean> calendars;
    private int position;
    private String peopleInfo;

    /**
     * 存储搜索的数据
     *
     * @param context
     * @param search
     */
    public ShopSqliteOpenHelper(Context context, String search) {
        super(context, NAME, null, VERSION);
        this.mSearch = search;
    }

    /**
     * 存储日历的数据
     *
     * @param context
     * @param calendars
     */
    public ShopSqliteOpenHelper(Context context, ArrayList<CalendarBean> calendars) {
        super(context, NAME, null, VERSION);
        this.calendars = calendars;
    }

    public ShopSqliteOpenHelper(Context context, int position, String peopleInfo) {
        super(context, NAME, null, VERSION);
        this.position = position;
        this.peopleInfo = peopleInfo;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableSearch = "create table " + TABLENAME_SEARCH + "(search text,time text);";
        String createTableCalendar = "create table " + TABLENAME_CALENDAR + "(calendarId text,goodsId text,startDate text,adultsPrice text,childrenPrice text,roomPrice text,year text,month text,indexId integer,packageId text);";
        String createTablePeopleInfo = "create table " + TABLENAME_PEOPLE_INFO + "(position integer,peopleInfo text);";
        // 运行数据库的语言
        db.execSQL(createTableSearch);
        db.execSQL(createTableCalendar);
        db.execSQL(createTablePeopleInfo);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("drop table if exists " + TABLENAME_SEARCH + ";");
            db.execSQL("drop table if exists " + TABLENAME_CALENDAR + ";");
            db.execSQL("drop table if exists " + TABLENAME_PEOPLE_INFO + ";");

            String createTableSearch = "create table " + TABLENAME_SEARCH + "(search text,time text);";
            String createTableCalendar = "create table " + TABLENAME_CALENDAR + "(calendarId text,goodsId text,startDate text,adultsPrice text,childrenPrice text,roomPrice text,year text,month text,indexId integer,packageId text);";
            String createTablePeopleInfo = "create table " + TABLENAME_PEOPLE_INFO + "(position integer,peopleInfo text);";
            // 运行数据库的语言
            db.execSQL(createTableSearch);
            db.execSQL(createTableCalendar);
            db.execSQL(createTablePeopleInfo);
        }
    }

    public void insert(String table) {
        // 得到数据库的可写权限的对象
        SQLiteDatabase db = getWritableDatabase();
        if (TextUtils.equals(table, TABLENAME_CALENDAR)) {// 日历
            for (int i = 0; i < calendars.size(); i++) {
                CalendarBean cb = calendars.get(i);
                ContentValues cv = new ContentValues();
                cv.put("calendarId", cb.getCalendarId());
                cv.put("goodsId", cb.getGoodsId());
                cv.put("startDate", cb.getDate());
                cv.put("adultsPrice", cb.getAdult_price());
                cv.put("childrenPrice", cb.getChildren_price());
                cv.put("roomPrice", cb.getSingle_room_price());
                cv.put("year", cb.getYear());
                cv.put("month", cb.getMonth());
                cv.put("indexId", cb.getIndex());
                cv.put("packageId", cb.getPackageId());
                db.insert(TABLENAME_CALENDAR, null, cv);
            }
        } else if (TextUtils.equals(table, TABLENAME_PEOPLE_INFO)) {// 人员信息
            ContentValues cv = new ContentValues();
            cv.put("position", position);
            cv.put("peopleInfo", peopleInfo);
            db.insert(TABLENAME_PEOPLE_INFO, null, cv);
        } else {// 搜索
            ContentValues cv = new ContentValues();
            cv.put("search", mSearch);
            cv.put("time", System.currentTimeMillis());
            db.insert(TABLENAME_SEARCH, null, cv);
        }
        db.close();

    }

    public Cursor query() {

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLENAME_SEARCH, null, null, null, null, null, "time desc");

        return c;
    }

    public Cursor query(String packageId, String startDate, String endDate) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        if (TextUtils.isEmpty(startDate)) {
            String selection = "packageId = ?";
            String selectionArgs[] = {packageId};
            c = db.query(TABLENAME_CALENDAR, null, selection, selectionArgs, null, null, "startDate");
        } else {
            String selection = "packageId = ? and startDate >= ? and startDate <= ?";
            String selectionArgs[] = {packageId, startDate, endDate};
            c = db.query(TABLENAME_CALENDAR, null, selection, selectionArgs, null, null, "startDate");
        }
        return c;
    }

    public Cursor query(int position) {

        SQLiteDatabase db = getReadableDatabase();
        String selection = "position = ?";
        String selectionArgs[] = {position + ""};
        Cursor c = db.query(TABLENAME_PEOPLE_INFO, null, selection, selectionArgs, null, null, null);

        return c;
    }
    public Cursor query(String peopleInfo) {

        SQLiteDatabase db = getReadableDatabase();
        String selection = "peopleInfo = ?";
        String selectionArgs[] = {peopleInfo};
        Cursor c = db.query(TABLENAME_PEOPLE_INFO, null, selection, selectionArgs, null, null, null);

        return c;
    }

    public void delete(String table, String flag) {
        SQLiteDatabase db = getWritableDatabase();
        if (TABLENAME_SEARCH.equals(table)) {
            String whereClause = "search = ?";
            String whereArgs[] = {mSearch};
            if (TextUtils.equals(flag, "all"))
                db.delete(TABLENAME_SEARCH, null, null);
            else
                db.delete(TABLENAME_SEARCH, whereClause, whereArgs);
        } else if (TABLENAME_CALENDAR.equals(table)) {
            db.delete(TABLENAME_CALENDAR, null, null);
        } else if (TABLENAME_PEOPLE_INFO.equals(table)) {
            String whereClause = "position = ?";
            String whereArgs[] = {position + ""};
            if (TextUtils.equals(flag, "all"))
                db.delete(TABLENAME_PEOPLE_INFO, null, null);
            else
                db.delete(TABLENAME_PEOPLE_INFO, whereClause, whereArgs);
        }
        db.close();
    }

}
