package com.travel.communication.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "USER_DATA".
*/
public class UserDataDao extends AbstractDao<UserData, String> {

    public static final String TABLENAME = "USER_DATA";

    /**
     * Properties of entity UserData.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, String.class, "id", true, "ID");
        public final static Property NickName = new Property(1, String.class, "nickName", false, "NICK_NAME");
        public final static Property ImgUrl = new Property(2, String.class, "imgUrl", false, "IMG_URL");
    };


    public UserDataDao(DaoConfig config) {
        super(config);
    }
    
    public UserDataDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"USER_DATA\" (" + //
                "\"ID\" TEXT PRIMARY KEY NOT NULL ," + // 0: id
                "\"NICK_NAME\" TEXT NOT NULL ," + // 1: nickName
                "\"IMG_URL\" TEXT);"); // 2: imgUrl
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"USER_DATA\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, UserData entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.getId());
        stmt.bindString(2, entity.getNickName());
 
        String imgUrl = entity.getImgUrl();
        if (imgUrl != null) {
            stmt.bindString(3, imgUrl);
        }
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.getString(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public UserData readEntity(Cursor cursor, int offset) {
        UserData entity = new UserData( //
            cursor.getString(offset + 0), // id
            cursor.getString(offset + 1), // nickName
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2) // imgUrl
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, UserData entity, int offset) {
        entity.setId(cursor.getString(offset + 0));
        entity.setNickName(cursor.getString(offset + 1));
        entity.setImgUrl(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(UserData entity, long rowId) {
        return entity.getId();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(UserData entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
