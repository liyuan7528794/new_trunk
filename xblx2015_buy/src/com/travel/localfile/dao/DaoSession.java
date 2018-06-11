package com.travel.localfile.dao;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig localFileDaoConfig;

    private final LocalFileDao localFileDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        localFileDaoConfig = daoConfigMap.get(LocalFileDao.class).clone();
        localFileDaoConfig.initIdentityScope(type);

        localFileDao = new LocalFileDao(localFileDaoConfig, this);

        registerDao(LocalFile.class, localFileDao);
    }
    
    public void clear() {
        localFileDaoConfig.getIdentityScope().clear();
    }

    public LocalFileDao getLocalFileDao() {
        return localFileDao;
    }

}
