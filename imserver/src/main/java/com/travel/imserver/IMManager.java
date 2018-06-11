package com.travel.imserver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.travel.communication.entity.MessageEntity;
import com.travel.imserver.bean.BaseBean;
import com.travel.imserver.bean.ClientData;
import com.travel.imserver.receiver.AbstractReceiver;
import com.travel.imserver.receiver.ChatMessageReceiver;
import com.travel.imserver.receiver.MsgResultReceiver;
import com.travel.lib.TravelApp;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 通信的Manager
 * 1. 初始化与反初始化: init unInit
 * Created by ldkxingzhe on 2016/12/8.
 */
public final class IMManager {
    @SuppressWarnings("unused")
    private static final String TAG = "IMManager";

    private static IMManager s_Instance;
    private Context mContext;
    private IMServiceInterface mService;
    private MessageTimer mMsgTimer;
    private ChatMessageReceiver.MyResultReceiver mChatReceiver;
    private ResultReceiver mMsgResultReceiver;
    private volatile ConnectionContext mConnectionContext = new ConnectionContext();
    private volatile boolean mHasBeenLogout = false;
    private ClientData mPendingClientData;
    private Gson mGson;
    private ResultCallback<Boolean> mLogoutListener;

    private Map<String, ReceiverRegisterItem> mCustomReceiver;

    private IMManager(Context context) {
        mContext = context;
        mMsgTimer = new MessageTimer();
        mChatReceiver = new ChatMessageReceiver.MyResultReceiver();
        mGson = new Gson();
        mCustomReceiver = new HashMap<>();
        mMsgResultReceiver = new MsgResultReceiver.MyResultReceiver(mMsgTimer);
        bindService();
    }

    public static IMManager getInstance() {
        if (s_Instance == null) {
            s_Instance = new IMManager(TravelApp.appContext);
        }
        return s_Instance;
    }

    public static void init(Context context) {
        if (s_Instance != null) {
            Log.e(TAG, "IMManager has been inited");
        } else {
            s_Instance = new IMManager(context.getApplicationContext());
        }
    }

    public void setLogoutListener(ResultCallback<Boolean> logoutListener) {
        mLogoutListener = logoutListener;
    }

    public void registerChatReceiver(String peerId, final ResultCallback<MessageEntity> callback) {
        mChatReceiver.registerReceiver(peerId, new ChatMessageReceiver.SingleChatReceiver(peerId, new ResultCallback<MessageEntity>() {
            @Override
            public void onResult(MessageEntity obj) {
                callback.onResult(obj);
            }
        }));
    }

    /**
     * 注册自定义的Receiver， 这些receiver必须继承自AbstractReceiver
     *
     * @param receiverName   接收器的名字， 必须唯一
     * @param receiverClass  接收器的类名
     * @param resultReceiver resultReceiver， 请实现其相应的内部静态类
     */
    public void registerReceiver(@NonNull String receiverName,
                                 @NonNull Class<? extends AbstractReceiver> receiverClass,
                                 @NonNull AbstractReceiver.MyResultReceiver resultReceiver) {
        ReceiverRegisterItem item = new ReceiverRegisterItem();
        item.receiverName = receiverName;
        item.receiverClass = receiverClass;
        item.resultReceiver = resultReceiver;
        mCustomReceiver.put(receiverName, item);
        if (mService != null) {
            try {
                registerReceiver(mService, receiverName, receiverClass, resultReceiver);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * 反注册这些接收器
     *
     * @param receiverName
     */
    public void unRegisterReceiver(@NonNull String receiverName) {
        mCustomReceiver.remove(receiverName);
        if (mService != null) {
            try {
                mService.unRegister(receiverName);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    public void unRegisterChatReceiver(String peerId) {
        mChatReceiver.unRegisterReceiver(peerId);
    }

    public void unInit() {
        s_Instance = null;
        unBindService();
        mChatReceiver.clearReceiver();
    }

    public void setClientData(String userId, String userName, String userImg, Callback callback) {
        ClientData clientData = mConnectionContext.mClientData;
        if (clientData != null && clientData.getUserId().equals(userId) && !mConnectionContext.mIsLogout) {
            Log.v(TAG, "setClientData, 已经在此状态");
            callback.onSuccess();
            return;
        }
        BaseMessage baseMessage = new BaseMessage(BaseBean.ACTION_LOGIN, "");
        mMsgTimer.addMessage(baseMessage, callback);
        ClientData clientDataTmp = new ClientData();
        clientDataTmp.setUserId(userId);
        clientDataTmp.setImgUrl(userImg);
        clientDataTmp.setNickName(userName);
        clientDataTmp.setSystem(OSUtil.getOSVersion());
        clientDataTmp.setModel(OSUtil.getPhoneModel());
        clientDataTmp.setNetWork(CheckNetStatus.checkNetworkConnection());
        if (mService != null) {
            try {
                mService.login(BaseBean.ACTION_LOGIN, clientDataTmp);
                mHasBeenLogout = false;
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
                mPendingClientData = clientDataTmp;
            }
        } else {
            mPendingClientData = clientDataTmp;
        }
    }

    public void changeRoom(String room, Callback callback) {
        try {
            if (mService != null) {

                boolean result = mService.changeRoom(BaseBean.ACTION_CHANGE_ROOM, room);
                if (result) {
                    callback.onSuccess();
                    return;
                }
            }
            BaseMessage baseMessage = new BaseMessage(BaseBean.ACTION_CHANGE_ROOM, "");
            mMsgTimer.addMessage(baseMessage, callback);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void sendBaseBean(BaseBean baseBean, Callback callback) {
//        baseBean.setClientData(mConnectionContext.mClientData);
        baseBean.setRoom(mConnectionContext.mCurrentRoom);
        String messageContent = mGson.toJson(baseBean);
        MLog.v(TAG, "sendMessage: %s.", messageContent);
        BaseMessage baseMessage = new BaseMessage(baseBean.getId(), messageContent);
        sendMessage(baseMessage, callback);
    }

    void sendMessage(BaseMessage message, Callback callback) {
        boolean isSend = false;
        if (mService != null && mConnectionContext.mIsActive && mConnectionContext.mIsLogin) {
            try {
                Log.v(TAG, "sendMessage service id: " + message.getId());
                isSend = mService.sendMessage(message);
            } catch (RemoteException e) {
                MLog.e(TAG, e.getMessage(), e);
            }
        }
        if (callback != null) {
            mMsgTimer.addMessage(message, callback);
        }
        if (!isSend) {
            // 没有发送成功
            mMsgTimer.mMsgCache.add(message);
        }
    }

    private void bindService() {
        Log.v(TAG, "绑定服务");
        mContext.bindService(new Intent(mContext, IMService.class),
                mServiceConnection,
                Context.BIND_ABOVE_CLIENT | Context.BIND_AUTO_CREATE);
    }

    private void unBindService() {
        mContext.unbindService(mServiceConnection);
    }

    void sendCacheMessage() {
        BaseMessage baseMessage;
        while ((baseMessage = mMsgTimer.mMsgCache.poll()) != null) {
            sendMessage(baseMessage, null);
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MLog.d(TAG, "onServiceConnected. thread %s.", Thread.currentThread());
            mService = IMServiceInterface.Stub.asInterface(service);
            try {
                registerReceiver(mService, "ChatMessage", ChatMessageReceiver.class, mChatReceiver);
                registerReceiver(mService, "MsgResultReceiver", MsgResultReceiver.class, mMsgResultReceiver);
                registerReceiver(mService, "Connection", ConnectionContextReceiver.class, mConnectionReceiver);
                for (ReceiverRegisterItem item : mCustomReceiver.values()) {
                    registerReceiver(mService, item.receiverName, item.receiverClass, item.resultReceiver);
                }
                if (mPendingClientData != null) {
                    mService.login(BaseBean.ACTION_LOGIN, mPendingClientData);
                }
                sendCacheMessage();
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MLog.d(TAG, "onServiceDisconnected");
            mService = null;
            mPendingClientData = mConnectionContext.mClientData;
            bindService();
        }
    };

    private ResultReceiver mConnectionReceiver = new ConnectionContextReceiver.MyResultReceiver(new ResultCallback<ConnectionContext>() {
        @Override
        public void onResult(ConnectionContext obj) {
            mConnectionContext = obj;
            if (mConnectionContext.mIsLogout) {
                if (!mHasBeenLogout) {
                    // 异地登录， 触发一定登录机制
                    mHasBeenLogout = true;
                    Log.d(TAG, "IMManager 触发logout机制");
                    if (mLogoutListener != null) {
                        mLogoutListener.onResult(true);
                    }
                }
                return;
            }
            if (mConnectionContext.mIsActive && mConnectionContext.mIsLogin) {
                sendCacheMessage();
            }
            MLog.v(TAG, "syncConnectionContext, %s", mConnectionContext);
        }
    });

    private void registerReceiver(IMServiceInterface service, String receiverName,
                                  Class clazz, ResultReceiver resultReceiver) throws RemoteException {
        String clazzName = clazz.getName();
        service.register(receiverName, clazzName, resultReceiver);
    }

    private class ReceiverRegisterItem {
        String receiverName;
        Class receiverClass;
        AbstractReceiver.MyResultReceiver resultReceiver;
    }
}
