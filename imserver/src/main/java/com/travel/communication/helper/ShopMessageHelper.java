package com.travel.communication.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 第二个选项卡“商城”的数据库
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/07/26
 */
public class ShopMessageHelper extends SQLiteOpenHelper {

	public static final String NAME = "ShopMessage.db";
	public static final int VERSION = 4;
	public static final String TABLENAME_MESSAGE = "Message";

	/**
	 * 订单消息的数据
	 * 
	 * @param context
	 */
	public ShopMessageHelper(Context context) {
		super(context, NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		String createTableMessage = "create table " + TABLENAME_MESSAGE
				+ "(ordersId text,userId text,userType text,status text);";
		// 运行数据库的语言
		db.execSQL(createTableMessage);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (newVersion > oldVersion) {
			db.execSQL("drop table " + TABLENAME_MESSAGE + ";");

			String createTableMessage = "create table " + TABLENAME_MESSAGE
					+ "(ordersId text,userId text,userType text,status text);";
			// 运行数据库的语言
			db.execSQL(createTableMessage);
		}
	}

	public void insert(String table, String ordersId, String userType, String userId, String status) {

		// 得到数据库的可写权限的对象
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("ordersId", ordersId);
		cv.put("userId", userId);
		cv.put("userType", userType);
		cv.put("status", status);
		db.insert(TABLENAME_MESSAGE, null, cv);
		db.close();

	}

	public Cursor query(String table, String userType, String userId, String status) {

		SQLiteDatabase db = getReadableDatabase();
		String selection = "userType = ? and userId = ? and status = ?";
		String selectionArgs[] = { userType, userId, status };
		Cursor c = db.query(TABLENAME_MESSAGE, null, selection, selectionArgs, null, null, null);

		return c;
	}

	public Cursor query(String table, String userType, String userId) {

		SQLiteDatabase db = getReadableDatabase();
		String selection = "userType = ? and userId = ?";
		String selectionArgs[] = { userType, userId };
		Cursor c = db.query(TABLENAME_MESSAGE, null, selection, selectionArgs, null, null, null);

		return c;
	}

	public void delete(String table, String ordersId, String userId) {
		SQLiteDatabase db = getWritableDatabase();
		String whereClause = "ordersId = ? and userId = ?";
		String whereArgs[] = { ordersId, userId };
		// 表名 条件 参数
		db.delete(TABLENAME_MESSAGE, whereClause, whereArgs);
		db.close();
	}

}
