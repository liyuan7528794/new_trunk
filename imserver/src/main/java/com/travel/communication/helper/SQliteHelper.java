package com.travel.communication.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.travel.communication.dao.DaoMaster;
import com.travel.communication.dao.DaoSession;
import com.travel.communication.dao.LastMessage;
import com.travel.communication.dao.LastMessageDao;
import com.travel.communication.dao.Message;
import com.travel.communication.dao.MessageDao;
import com.travel.communication.dao.MessageDao.Properties;
import com.travel.communication.entity.MessageEntity;
import com.travel.communication.entity.UserData;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MessageBroadcastHelper;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * 数据库的辅助类
 * 涉及了消息,最后一条消息, 用户信息三张表
 * Created by ldkxingzhe on 2016/3/29.
 */
public class SQliteHelper {
    @SuppressWarnings("unused")
    private static final String TAG = "MessageHelper";

    /*
    * 将此helper设为单例模式的原因是:
    * DevOpenHelper继承了SQLiteOpenHelper, 了解SQLiteOpenHelper的应该知道, 它是线程安全的
    * 但是这种安全是建立在SQLiteOpenHelper是单例的基础上的.
    * 为了使用线程安全将其写成单例模式.
    * 写成单例有一个弊端, 就是使用Roboletric单元测试时, 会出现错误, 需要使用反射将其手动置空.
    * */
    private static DaoMaster.DevOpenHelper mHelper;
    private SQLiteDatabase mDB;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private MessageDao mMessageDao;
    private LastMessageDao mLastMessageDao;

    private final WeakReference<Context> mContext;

    // 无奈之举, 在这里缓存用户信息
    private LruCache<String, UserData> mUserDataCache;
    private static SoftReference<LruCache<String, UserData>> sUserDataCache;

    public SQliteHelper(Context context) {
        mContext = new WeakReference<Context>(context);
        generateOpenHelper(context);
        mDB = mHelper.getWritableDatabase();
        mDaoMaster = new DaoMaster(mDB);
        // 这里使用IdentityScopeType.None是因为GreenDao默认对数据库操作进行了session缓存,
        // 这里不需要, 不然就只能写成单例了. 暂时不想写成单例, 写成这种形式
        mDaoSession = mDaoMaster.newSession(IdentityScopeType.None);
        mMessageDao = mDaoSession.getMessageDao();
        mLastMessageDao = mDaoSession.getLastMessageDao();
        mUserDataCache = getLruCache();

        Class a = LastMessageDao.Properties.UnReadNumber.getClass();
        Class b = LastMessageDao.Properties.MessageId.getClass();
        MLog.v(TAG, a.toString() + b.toString() + "");
    }

    private LruCache<String, UserData> getLruCache() {
        if (sUserDataCache == null || sUserDataCache.get() == null) {
            synchronized (mHelper) {
                if (sUserDataCache == null) {
                    sUserDataCache = new SoftReference<LruCache<String, UserData>>(new LruCache<String, UserData>(5));
                }
            }
        }
        return sUserDataCache.get();
    }

    private void generateOpenHelper(Context context) {
        if (mHelper == null) {
            synchronized (this) {
                if (mHelper == null) {
                    mHelper = new DaoMaster.DevOpenHelper(context, "TravelDB", null);
                }
            }
        }
    }

    /**
     * 获取相应的消息列表
     *
     * @param senderId   发送者id
     * @param receiverId 接受者id
     * @param numCount   需要显示的行数
     * @param offSet 从多少条开始
     * @return
     */
    public List<Message> getMessageList(String senderId, String receiverId, int numCount, int offSet) {
        QueryBuilder<Message> queryBuilder = mMessageDao.queryBuilder();
        queryBuilder
                .where(MessageDao.Properties.ReceiverId.eq(receiverId), MessageDao.Properties.SenderId.eq(senderId))
                .orderDesc(MessageDao.Properties.Id)
                .limit(numCount)
                .offset(offSet);
        List<Message> result = queryBuilder.list();
        Collections.reverse(result);
        return result;
    }

    /**
     * 获取两个人的回话列表
     *
     * @param firstId  第一id
     * @param secondId 第二个id
     * @param numCount 显示的行数, 最后多少行, 正序
     */
    public List<Message> getCommunicationMessage(String firstId, String secondId, int numCount) {
        return getCommunicationMessage(firstId, secondId, numCount, 0);
    }

    /**
     * 获取两人的回话信息,  包含offset
     *
     * @param firstId
     * @param secondId
     * @param numCount
     * @param offSet   从多少条开始
     * @return
     */
    public List<Message> getCommunicationMessage(String firstId, String secondId, int numCount, int offSet) {
        QueryBuilder<Message> queryBuilder = mMessageDao.queryBuilder();
        queryBuilder.whereOr(queryBuilder.and(MessageDao.Properties.ReceiverId.eq(firstId), MessageDao.Properties.SenderId.eq(secondId), Properties.OwnerId.eq(MessageEntity.mUserId))
                , queryBuilder.and(MessageDao.Properties.ReceiverId.eq(secondId), MessageDao.Properties.SenderId.eq(firstId), Properties.OwnerId.eq(MessageEntity.mUserId)))
                .orderDesc(MessageDao.Properties.Id)
                .offset(offSet)
                .limit(numCount);
        List<Message> result = queryBuilder.list();
        return result;
    }

    /**
     * 获取群主消息
     *
     * @param roomNum
     * @param numCount 查询的条数
     * @param offSet   从多少条开始
     * @return
     */
    public List<Message> getGroupMessage(@NonNull String roomNum, int numCount, int offSet) {
        QueryBuilder<Message> queryBuilder = mMessageDao.queryBuilder();
        queryBuilder.where(Properties.ReceiverId.eq(roomNum))
                .orderDesc(MessageDao.Properties.Id)
                .limit(numCount)
                .offset(offSet);
        List<Message> result = queryBuilder.list();
        return result;
    }

    /**
     * 获取群组消息
     */
    public List<Message> getGroupMessage(@NonNull String roomNum, int numCount) {
        return getGroupMessage(roomNum, numCount, 0);
    }

    /**
     * 获取所有最后一条数据的列表
     *
     * @param receiverId 接受者Id
     * @return 所有的最后一条数据的列表
     */
    public List<LastMessage> getLastMessageList(String receiverId) {
        List<LastMessage> result = mLastMessageDao.queryBuilder()
                .where(LastMessageDao.Properties.ReceiverId.eq(receiverId), LastMessageDao.Properties.IsVisible.eq(true))
                .orderDesc(LastMessageDao.Properties.Id)
                .list();
        return result;
    }

    /**
     * 获取某个群的最后一条数据的列表
     *
     * @param senderId 群Id
     * @return 所有的最后一条数据的列表
     */
    public List<LastMessage> getLastMessageListSenderId(String senderId) {
        List<LastMessage> result = mLastMessageDao.queryBuilder()
                .where(LastMessageDao.Properties.SenderId.eq(senderId), LastMessageDao.Properties.IsVisible.eq(true))
                .orderDesc(LastMessageDao.Properties.MessageId)
                .list();
        return result;
    }

    /**
     * 获取最后一条数据, 满足发送者Id与接受者Id
     *
     * @param receiverId 接受者Id
     * @param senderId   发送者Id
     * @return null if not exist
     */
    public LastMessage getLastMessage(String receiverId, String senderId) {
        LastMessage lastMessage = mLastMessageDao.queryBuilder()
                .where(LastMessageDao.Properties.ReceiverId.eq(receiverId), LastMessageDao.Properties.SenderId.eq(senderId))
                .unique();
        return lastMessage;
    }

    /**
     * 插入一条数据
     *
     * @param message
     */
    public long insertMessage(Message message) {
        message.setOwnerId(MessageEntity.mUserId);
        return mMessageDao.insert(message);
    }

    public long inserOrReplace(LastMessage lastMessage) {
        return mLastMessageDao.insertOrReplace(lastMessage);
    }

    /**
     * 根据id, 删除消息
     *
     * @param id
     */
    public void deleteMessage(long id) {
        mMessageDao.deleteByKey(id);
    }

    /**
     * 设置会话的可见性
     *
     * @param senderId 发送者的Id
     * @param visible  可见性
     */
    public void setChatInVisible(String senderId, boolean visible) {
        LastMessage message = mLastMessageDao.queryBuilder()
                .where(LastMessageDao.Properties.SenderId.eq(senderId))
                .unique();
        if (message == null) {
            Log.e(TAG, "this should not happen");
        } else {
            message.setIsVisible(visible);
            mLastMessageDao.update(message);
        }
    }

    /**
     * 向最后一条信息的表中插入一条数据,
     * 需要注意的是, message 的id必须设置
     *
     * @param message      最后一条消息内容, 消息实体, 其中id必须已经被数据库自动设置
     * @param isNeedUnRead 是否需要显示未读选项, true -- 未读条数加 1, 并显示
     */
    public void lastMessageAddOne(Message message, boolean isNeedUnRead, boolean isOther) {
        // 使用保险的方法, 设置Id
        String senderId = message.getSenderId();
        String receiverId = message.getReceiverId();
        if (!isOther) {
            // 此消息是本人发送, 将其接受者与发送者对调
            String tmp = senderId;
            senderId = receiverId;
            receiverId = tmp;
        }

        long messageId = message.getId();
        if (messageId <= 0) {
            throw new IllegalStateException("message id must be set");
        }

        LastMessage lastMessage = getLastMessage(receiverId, senderId);
        boolean isInsertLastMessage = false;
        if (lastMessage == null) {
            lastMessage = new LastMessage();
            lastMessage.setReceiverId(receiverId);
            lastMessage.setSenderId(senderId);
            isInsertLastMessage = true;
        }
        lastMessage.setMessageId(messageId);
        lastMessage.setIsVisible(true);
        if (isNeedUnRead) {
            Integer unReadNumber = lastMessage.getUnReadNumber();
            unReadNumber = unReadNumber == null ? 0 : unReadNumber;
            lastMessage.setUnReadNumber(unReadNumber + 1);
            playRingtone();
        } else {
            lastMessage.setUnReadNumber(0);
        }

        if (isInsertLastMessage) {
            mLastMessageDao.insertWithoutSettingPk(lastMessage);
        } else {
            mLastMessageDao.update(lastMessage);
        }

    }

    public void playRingtone() {
        MLog.v(TAG, "playRingtone");
        if (mContext.get() == null)
            return;
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(mContext.get(), notification);
        if (r != null) {
            r.play();
        }
        MessageBroadcastHelper.sendMessageComming(mContext.get());
    }

    /**
     * 将最后一个列表中满足要求的列项未读条数置为0
     *
     * @param senderId   发送者Id
     * @param receiverId 接受者ID
     */
    public void lastMessageResetUnRead(String senderId, String receiverId) {
        LastMessage lastMessage = null;
        if (senderId.startsWith("@TGS")) {
            List<LastMessage> msgList = getLastMessageListSenderId(senderId);
            if(msgList.size() > 0)
                lastMessage = msgList.get(0);
        } else if (!senderId.startsWith("@TGS"))
            lastMessage = getLastMessage(receiverId, senderId);

        if (lastMessage == null) {
            MLog.e(TAG, "lastMessge not exist");
            return;
        }
        lastMessage.setUnReadNumber(0);
        mLastMessageDao.update(lastMessage);
    }

    /*
    * 删除会话
    * */
    public void lastMessageRemoveConversation(String senderId, String receiverId) {
        LastMessage lastMessage = null;
        if (senderId.startsWith("@TGS") && getLastMessageListSenderId(senderId).size() > 0)
            lastMessage = getLastMessageListSenderId(senderId).get(0);
        else if (!senderId.startsWith("@TGS"))
            lastMessage = getLastMessage(receiverId, senderId);
        if (lastMessage != null) {
            mLastMessageDao.delete(lastMessage);
        }
    }

    public Message getLastGoodsMessage(String senderId, String receiverId) {
        QueryBuilder<Message> builder = mMessageDao.queryBuilder();
        builder.whereOr(builder.and(MessageDao.Properties.ReceiverId.eq(receiverId),
                MessageDao.Properties.SenderId.eq(senderId),
                Properties.MessageType.eq(MessageEntity.TYPE_GOODS_INFO)),
                builder.and(Properties.ReceiverId.eq(senderId),
                        Properties.SenderId.eq(receiverId),
                        Properties.MessageType.eq(MessageEntity.TYPE_GOODS_INFO)))
                .orderDesc(Properties.Id).limit(1);
        return builder.unique();
    }

    /**
     * 添加或者替换一个UserData数据
     * 大数据请勿使用, 请使用事务.
     */
    public void inserOrReplace(UserData userData) {
        UserData cache = mUserDataCache.get(userData.getId());
        boolean isNeedSQLite = false;
        if (cache == null) {
            isNeedSQLite = true;
        } else {
            if (TextUtils.equals(cache.getImgUrl(), userData.getImgUrl()) && TextUtils.equals(cache.getNickName(), userData.getNickName())) {
                return;
            } else {
                mUserDataCache.put(userData.getId(), userData);
                isNeedSQLite = true;
            }
        }
        if (isNeedSQLite) {
            mDaoSession.getUserDataDao().insertOrReplace(userData);
            //    		mDaoSession.clear();
        }
    }

    /**
     * 根据用户Id获取用户信息
     * 内部进行了缓存
     *
     * @param userId
     * @return
     */
    public UserData getUserData(String userId) {
        UserData result = mUserDataCache.get(userId);
        if (result == null) {
            com.travel.communication.dao.UserData userData = mDaoSession.getUserDataDao().load(userId);
            if (userData == null)
                return null;
            result = new UserData();
            result.setId(userData.getId());
            result.setImgUrl(userData.getImgUrl());
            result.setNickName(userData.getNickName());
            mUserDataCache.put(userId, result);
        }
        return result;
    }
}
