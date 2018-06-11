package com.travel.imserver;

import android.util.Log;

import com.google.gson.Gson;
import com.travel.imserver.bean.BaseBean;
import com.travel.imserver.bean.ClientData;


/**
 * 消息接受的处理器
 * 1. 消息回馈， 收到消息后向服务器发送ACK
 * 2. 消息回馈处理， 收到服务器的ACK后的动作
 * 3. 消息根据订阅者分门别类的处理
 *     - 服务中的处理： 消息入库处理等。
 *     - 返回发送结果
 * 4. 服务端心跳的处理
 * 5. 断线检测
 * 6. 异常捕获
 * Created by ldkxingzhe on 2016/12/7.
 */
public class JsonClientHandler implements SocketClient.HandlerListener{
    @SuppressWarnings("unused")
    private static final String TAG = "JsonClientHandler";

    public static final String DISCARD_ID = "discard id";

    private volatile NettyClient mNettyClient;
    private ReceiverACK mReceiverACK;
    private HeartBeatHandler mHeartBeatHandler;
    private volatile MessageDispatcher mMessageDispatcher;

    public JsonClientHandler(NettyClient client, JsonClientHandler old) {
        mNettyClient = client;
        mReceiverACK = new ReceiverACK();
        mHeartBeatHandler = new HeartBeatHandler();
        if(old != null){
            mMessageDispatcher = old.mMessageDispatcher;
        }else{
            mMessageDispatcher = new MessageDispatcher();
        }
    }

    @Override
    public void channelRead(BaseBean msg) {
//        Log.v(TAG, "read: " + msg);
/*        if(mReceiverACK.isDealWith(msg)){
            // 需要发送收到回执的ACK
            mReceiverACK.dealWith(msg);
        }*/
        if(mHeartBeatHandler.isDealWith(msg)){
            mHeartBeatHandler.dealWith(msg);
        }else{
            mMessageDispatcher.dealWith(msg);
        }
    }

    public void onSendMessageFailed(String id){
        if(DISCARD_ID.equals(id)) return;
        BaseBean baseBean = new BaseBean();
        baseBean.setType(BaseBean.FAILED);
        mMessageDispatcher.dealWith(baseBean);
    }

    @Override
    public void onTimeout(boolean isRead){
        if (isRead){
            mHeartBeatHandler.sendHeartBeat();
        }
    }

    @Override
    public void channelActive(){
        Log.v(TAG, "channelActive");
        mNettyClient.mConnectionContext.mIsActive = true;
        syncConnectionContext();
        if(mNettyClient.mConnectionContext.shouldReconnection()){
            // 没有被异地登录的情况下， 首先登录
            mNettyClient.doLogin(mNettyClient.mConnectionContext.mLoginID);
        }
    }

    @Override
    public void channelInactive(){
        Log.v(TAG, "channelInactive");
        if(mNettyClient.mConnectionContext.shouldReconnection()){
            mNettyClient.reConnectBy("连接断开， 开启重连");
        }
        mNettyClient.mConnectionContext.mIsActive = false;
        mNettyClient.mConnectionContext.mIsLogin = false;
        syncConnectionContext();
    }

    private void syncConnectionContext(){
        ConnectionContextReceiver receiver =  (ConnectionContextReceiver) mMessageDispatcher.getReceiver("Connection");
        if(receiver != null) receiver.syncConnectionContext();
    }

    @Override
    public void exceptionCaught(Throwable cause){
        Log.e(TAG, "exceptionCaught: " + cause.getMessage(), cause);
    }

    public MessageDispatcher getMsgDispatcher(){
        return mMessageDispatcher;
    }

    // 客户端不在需要回执
    private class ReceiverACK implements Receiver<BaseBean>{

        @Override
        public boolean isDealWith(BaseBean obj) {
            // 所有类型非负的值均回执
            return obj.getType() >= 0;
        }

        @Override
        public void dealWith(BaseBean obj) {
            BaseBean baseBean = new BaseBean();
            baseBean.setId(obj.getId());
            baseBean.setType(-1);
            Gson gson = new Gson();
            mNettyClient.sendMessage(new BaseMessage(gson.toJson(baseBean)));
        }
    }

    // 心跳的接受处理器
    private class HeartBeatHandler implements Receiver<BaseBean>{
        private int mHeartTimes;

        @Override
        public boolean isDealWith(BaseBean obj) {
            mHeartTimes = 0;
            if ("heartBeat".equals(obj.getMsgHead())
                    || BaseBean.ACTION_HEART_BEAT.equals(obj.getId())){
                return true;
            }
            return false;
        }

        @Override
        public void dealWith(BaseBean obj) {
            // ignore
        }

        public void sendHeartBeat(){
            mHeartTimes++;
            if(mHeartTimes > 4){
//                mNettyClient.reConnectBy("心跳次数大于4此， 立即重连");
                mNettyClient.disConnect();
            }else{
                ClientData clientData = mNettyClient.mConnectionContext.mClientData;
                String heartBeatStr = "{\"type\":0,\"sendUser\":\""
                        + (clientData == null ? "null" : clientData.getUserId())
                        +"\",\"msgHead\":\"heartBeat\"}";
                BaseMessage baseMessage = new BaseMessage(BaseBean.ACTION_HEART_BEAT, heartBeatStr);
                mNettyClient.sendMessage(baseMessage);
                Log.v(TAG, "sendHeartBeat: " + heartBeatStr + ". and heartTimes is " + mHeartTimes);
            }
        }
    }
}
