package com.travel.imserver;

import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;

import com.travel.imserver.bean.BaseBean;
import com.travel.imserver.receiver.AbstractReceiver;

/**
 * 上下文接收器
 * 主要用于接受并反馈Context与异地登录的处理
 * Created by ldkxingzhe on 2016/12/9.
 */
public class ConnectionContextReceiver implements Receiver<BaseBean>{
    @SuppressWarnings("unused")
    private static final String TAG = "ConnectionContextReceiver";
    private ConnectionContext mConnectionContext;
    private NettyClient mNettyClient;
    private ResultReceiver mResultReceiver;

    public ConnectionContextReceiver(NettyClient nettyClient){
        mNettyClient = nettyClient;
        mConnectionContext = mNettyClient.mConnectionContext;
    }

    public void setResultReceiver(ResultReceiver resultReceiver){
        mResultReceiver = resultReceiver;
        if(resultReceiver != null){
            // 第一次就同步信息
            syncConnectionContext();
        }
    }

    @Override
    public boolean isDealWith(BaseBean obj) {
        String msgId = obj.getId();
        boolean isSuccess = obj.getType() == BaseBean.SUCCESS;
        if(TextUtils.isEmpty(msgId)){
            msgId = "tmp";
        }
        boolean result = false;
        if(msgId.equals(mConnectionContext.mLoginID) && !"logout".equals(obj.getMsgBody())){
            // 登录成功
            mConnectionContext.mIsLogin = isSuccess;
            mConnectionContext.mIsLogout = false;
            result = true;
        }else if(msgId.equals(mConnectionContext.mLogoutID)){
            // 登出成功
            result = true;
        }else if(msgId.equals(mConnectionContext.mChangingRoomMsgID)){
            // 改变用户
            if(isSuccess){
                mConnectionContext.mCurrentRoom = mConnectionContext.mChangingRoom;
                mConnectionContext.mChangingRoomMsgID = null;
                result = true;
            }
        }else if("login".equals(obj.getMsgHead()) && "logout".equals(obj.getMsgBody())){
            // 异地登录
            mConnectionContext.mIsLogout = true;
            mConnectionContext.mIsLogin = false;
            mNettyClient.disConnect();
        }
        result &= mResultReceiver != null;
        return result;
    }

    @Override
    public void dealWith(BaseBean obj) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(TAG, mConnectionContext);
        mResultReceiver.send(0, bundle);
    }

    public void syncConnectionContext(){
        if(mResultReceiver == null) return;
        dealWith(null);
    }

    public static class MyResultReceiver extends AbstractReceiver.MyResultReceiver{
        private ResultCallback<ConnectionContext> mCallback;

        public MyResultReceiver(ResultCallback<ConnectionContext> callback){
            mCallback = callback;
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if(mCallback == null) return;
            resultData.setClassLoader(ConnectionContext.class.getClassLoader());
            ConnectionContext connectionContext = resultData.getParcelable(TAG);
            mCallback.onResult(connectionContext);
        }
    }
}
