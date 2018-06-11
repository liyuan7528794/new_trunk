package com.travel.imserver;

import android.util.Log;

import com.google.gson.Gson;
import com.travel.imserver.bean.BaseBean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * 发送的客户端,
 * 1. 实现连接功能， 连接， 断开
 * 2. 连接状态的判断， 是否连接
 * 3. 断线重连功能
 * Created by ldkxingzhe on 2016/12/7.
 */
public class NettyClient {
    @SuppressWarnings("unsed")
    private static final String TAG = "NettyClient";

    private ScheduledExecutorService mGroup = Executors.newScheduledThreadPool(2);
    private volatile JsonClientHandler mClientHandler;
    private volatile SocketClient mSocketClient;
    private String mHost;
    private int mPort;
    private volatile boolean mIsConnecting = false;
    ConnectionContext mConnectionContext;
    public NettyClient(String host, int port){
        mHost = host;
        mPort = port;
        mConnectionContext = new ConnectionContext();
        mClientHandler = new JsonClientHandler(this, null);
        doConnectAsync();
    }

    // for test
    public NettyClient(){
        this("localhost", 8080);
    }

    public JsonClientHandler getClientHandler(){
        return mClientHandler;
    }

    public void reConnectBy(String reason){
        // 由于异常的原因断线重连
        Log.e(TAG, reason);
        doReconnect(1);
    }

    void doReconnect(final long delay){
        if(mIsConnecting){
            Log.d(TAG, "正在重连...， 此处异常");
            return;
        }
        if(mConnectionContext.mIsDestroy){
            Log.d(TAG, "已经destroy， 关闭");
            return;
        }
        if(mSocketClient != null){
            mSocketClient.disConnect();
            mSocketClient = null;
            mGroup.schedule(new Runnable() {
                @Override
                public void run() {
                    doConnect();
                }
            }, delay, TimeUnit.SECONDS);
        }else{
            mGroup.schedule(new Runnable() {
                @Override
                public void run() {
                    doConnect();
                }
            }, delay, TimeUnit.SECONDS);
        }
    }

    void doConnectAsync(){
        mGroup.submit(new Runnable() {
            @Override
            public void run() {
                doConnect();
            }
        });
    }

    void doConnect() {
        try {
            if(mConnectionContext.mIsLogout){
                Log.e(TAG, "异地登录状态, 拒绝再次登录");
                return;
            }

            if(mSocketClient != null){
                Log.d(TAG, "已经有一个连接了, 异常状态, 忽略");
                return;
            }
            synchronized (this){
                if(mSocketClient == null){
                    Log.v(TAG, "连接socket");
                    mIsConnecting = true;
                    mSocketClient = new SocketClient(mHost, mPort, mGroup, mClientHandler);
                    mSocketClient.connect();
                    mIsConnecting = false;
                }else{
                    Log.d(TAG, "已经有一个连接了, 异常状态, 忽略");
                }
            }
        } catch (Exception e) {
            mIsConnecting = false;
            Log.e(TAG, e.getMessage(), e);
            Log.e(TAG, "连接异常， 启用重连机制");
            doReconnect(10);
        }
    }

    // 仅仅断开连接, 会触发inactive
    void disConnect(){
        if(mSocketClient != null){
            mSocketClient.disConnect();
        }
    }

    void onDestroy(){
        mConnectionContext.mIsDestroy = true;
        if(mSocketClient != null){
            mSocketClient.disConnect();
            mSocketClient = null;
        }else{
            shutdownAll();
        }
    }

    void shutdownAll(){
        if(mGroup != null){
            Log.v(TAG, "shutDownAll");
            mGroup.shutdown();
            mGroup = null;
        }
    }

    public boolean sendMessage(final BaseMessage baseMessage){
        Log.v(TAG, "sendMessage id is: " + baseMessage.getId());
        if(mSocketClient == null || !mSocketClient.isActive()){
            Log.v(TAG, "缓存消息");
            return false;
        }
        mSocketClient.sendMsg(baseMessage.getMessageContent() + "\n", new Callback() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "send message over: " + baseMessage.getMessageContent());
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                // 写入失败
                Log.e(TAG, "sendMessage failed: " + baseMessage.getId());
                mClientHandler.onSendMessageFailed(baseMessage.getId());
            }
        });
        return true;
    }

    void doLoginAsync(final String msgId){
        mGroup.submit(new Runnable() {
            @Override
            public void run() {
                doLogin(msgId);
            }
        });
    }

    void doLogin(String msgId) {
        if(mConnectionContext.mClientData == null) return;
        if(mSocketClient == null){
            // 如果没有连接， 这种情况下在logout中可能出现， 首先连接它.
            mConnectionContext.mIsLogout = false;
            doConnect();
        }
        Gson gson = new Gson();
        BaseBean baseBean = new BaseBean();
        baseBean.setClientData(mConnectionContext.mClientData);
        baseBean.setType(0);
        baseBean.setSendUser(mConnectionContext.mClientData.getUserId());
        mConnectionContext.mLoginID = msgId;
        baseBean.setId(mConnectionContext.mLoginID);
        baseBean.setMsgHead("login");
        baseBean.setMsgBody(mConnectionContext.mCurrentRoom +
                "+" + gson.toJson(mConnectionContext.mClientData));
        BaseMessage baseMessage = new BaseMessage(baseBean.getId(), gson.toJson(baseBean));
        sendMessage(baseMessage);
        mConnectionContext.mClientData.clearLoginInfo();
    }

    public boolean doChangeRoom(String msgId, String roomName) {
        if(mConnectionContext.mCurrentRoom.equals(roomName)){
            return true;
        }

        mConnectionContext.mChangingRoom = roomName;
        mConnectionContext.mChangingRoomMsgID = msgId;
        Gson gson = new Gson();
        BaseBean baseBean = new BaseBean();
        baseBean.setClientData(mConnectionContext.mClientData);
        baseBean.setSendUser(mConnectionContext.mClientData.getUserId());
        baseBean.setType(0);
        baseBean.setId(mConnectionContext.mChangingRoomMsgID);
        baseBean.setMsgHead("change_room");
        baseBean.setRoom(roomName);
        BaseMessage baseMessage = new BaseMessage(baseBean.getId(), gson.toJson(baseBean));
        sendMessage(baseMessage);
        return false;
    }
}
