package com.travel.imserver.receiver;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;

import com.travel.imserver.ResultCallback;
import com.travel.imserver.bean.BaseBean;
import com.travel.lib.utils.MLog;

/**
 * 订单推送类的接收器
 * Created by ldkxingzhe on 2016/12/15.
 */
public class OrderMsgReceiver extends AbstractReceiver{
    @SuppressWarnings("unused")
    private static final String TAG = "OrderMsgReceiver";

    public OrderMsgReceiver(Context context, ResultReceiver resultReceiver) {
        super(context, resultReceiver);
    }

    @Override
    public boolean isDealWith(BaseBean obj) {
        return obj.getType() == 4;
    }

    @Override
    public void dealWith(BaseBean obj) {
        String orderMessageStr = obj.getMsgBody();
        MLog.d(TAG, "收到订单推送的消息: %s.", orderMessageStr);
        Bundle bundle = new Bundle();
        bundle.putString(TAG, orderMessageStr);
        mResultReceiver.send(0, bundle);
    }

    public static class MyResultReceiver extends AbstractReceiver.MyResultReceiver{
        private ResultCallback<String> mCallback;
        public MyResultReceiver(@NonNull ResultCallback<String> callback){
            mCallback = callback;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            String orderMessageStr = resultData.getString(TAG);
            mCallback.onResult(orderMessageStr);
        }
    }
}
