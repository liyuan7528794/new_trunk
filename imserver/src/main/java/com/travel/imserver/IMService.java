package com.travel.imserver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;

import com.travel.Constants;
import com.travel.imserver.bean.ClientData;
import com.travel.imserver.receiver.AbstractReceiver;
import com.travel.lib.utils.MLog;

import java.lang.reflect.Constructor;

/**
 * IM的通信主服务
 * Created by ldkxingzhe on 2016/12/8.
 */
public class IMService extends Service{
    @SuppressWarnings("unused")
    private static final String TAG = "IMService";
    MessageDispatcher mMsgDispatcher;
    NettyClient mNettyClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "IMService onCreate");
        // 网络的端口号与地址
        mNettyClient = new NettyClient(Constants.Chat_Ip, 8082);
        mMsgDispatcher = mNettyClient.getClientHandler().getMsgDispatcher();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNettyClient.onDestroy();
        Log.d(TAG, "IMServer destroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IMServiceInterface.Stub(){

            @Override
            public void register(String parserName, String className, ResultReceiver resultReceiver) throws RemoteException {
                IMService.this.register(parserName, className, resultReceiver);
            }

            @Override
            public void unRegister(String parseName) throws RemoteException {
                mMsgDispatcher.unRegisterReceiver(parseName);
            }

            @Override
            public void login(String msgId, ClientData clientData) throws RemoteException {
//                if(mNettyClient.mConnectionContext.mClientData == null
//                        || !mNettyClient.mConnectionContext.mClientData.getUserId().equals(clientData.getUserId())){
//                     新登录的用户回到room房间
//                    mNettyClient.mConnectionContext.mCurrentRoom = "home";
//                }
                MLog.d(TAG, "onLogin msgId is %s", msgId);
                mNettyClient.mConnectionContext.mClientData = clientData;
                mNettyClient.doLoginAsync(msgId);
            }

            @Override
            public boolean changeRoom(String msgId, String roomName) throws RemoteException {
                return mNettyClient.doChangeRoom(msgId, roomName);
            }

            @Override
            public boolean sendMessage(BaseMessage baseMessage) throws RemoteException {
                return mNettyClient.sendMessage(baseMessage);
            }
        };
    }

    @SuppressWarnings("unchecked cast")
    public void register(String parserName, String className, ResultReceiver resultReceiver) throws RemoteException{
        try {
            Log.e(TAG, "打印类型: " + resultReceiver.getClass().getName());
            if(ConnectionContextReceiver.class.getName().equals(className)){
                // 连接状态同步器
                ConnectionContextReceiver contextReceiver = new ConnectionContextReceiver(mNettyClient);
                contextReceiver.setResultReceiver(resultReceiver);
                mMsgDispatcher.registerReceiver(parserName, contextReceiver);
            }else {
                Class<? extends AbstractReceiver> receiverClass =
                        (Class<? extends AbstractReceiver>) Class.forName(className);
                Constructor receiverConstructor = receiverClass.getConstructor(Context.class, ResultReceiver.class);
                Receiver receiver = (Receiver) receiverConstructor.newInstance(this, resultReceiver);
                mMsgDispatcher.registerReceiver(parserName, receiver);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
//            throw new RemoteException(e.getMessage());
        }
    }
}
