package com.travel.communication.dao;

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

    private final DaoConfig userDataDaoConfig;
    private final DaoConfig messageDaoConfig;
    private final DaoConfig lastMessageDaoConfig;

    private final UserDataDao userDataDao;
    private final MessageDao messageDao;
    private final LastMessageDao lastMessageDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        userDataDaoConfig = daoConfigMap.get(UserDataDao.class).clone();
        userDataDaoConfig.initIdentityScope(type);

        messageDaoConfig = daoConfigMap.get(MessageDao.class).clone();
        messageDaoConfig.initIdentityScope(type);

        lastMessageDaoConfig = daoConfigMap.get(LastMessageDao.class).clone();
        lastMessageDaoConfig.initIdentityScope(type);

        userDataDao = new UserDataDao(userDataDaoConfig, this);
        messageDao = new MessageDao(messageDaoConfig, this);
        lastMessageDao = new LastMessageDao(lastMessageDaoConfig, this);

        registerDao(UserData.class, userDataDao);
        registerDao(Message.class, messageDao);
        registerDao(LastMessage.class, lastMessageDao);
    }
    
    public void clear() {
        userDataDaoConfig.getIdentityScope().clear();
        messageDaoConfig.getIdentityScope().clear();
        lastMessageDaoConfig.getIdentityScope().clear();
    }

    public UserDataDao getUserDataDao() {
        return userDataDao;
    }

    public MessageDao getMessageDao() {
        return messageDao;
    }

    public LastMessageDao getLastMessageDao() {
        return lastMessageDao;
    }

}
