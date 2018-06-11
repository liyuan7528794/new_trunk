package com.travel.imserver.receiver;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;

import com.travel.imserver.MessageTimer;
import com.travel.imserver.bean.BaseBean;
import com.travel.lib.utils.MLog;

/**
 * 消息回执的接收器
 * Created by ldkxingzhe on 2016/12/9.
 */
public class MsgResultReceiver extends AbstractReceiver{
    @SuppressWarnings("unused")
    private static final String TAG = "MsgResultReceiver";

    public MsgResultReceiver(Context context, ResultReceiver resultReceiver) {
        super(context, resultReceiver);
    }

    @Override
    public boolean isDealWith(BaseBean obj) {
        // 处理消息成功与否的回执
        return obj.getType() == BaseBean.FAILED
                || obj.getType() == BaseBean.SUCCESS;
    }

    @Override
    public void dealWith(BaseBean obj) {
        Bundle bundle = new Bundle();
        bundle.putString(TAG, obj.getId());
        mResultReceiver.send(obj.getType(), bundle);
    }

    public static class MyResultReceiver extends AbstractReceiver.MyResultReceiver{
        private MessageTimer mMsgTimer;

        public MyResultReceiver(@NonNull MessageTimer msgTimer){
            mMsgTimer = msgTimer;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            String msgId = resultData.getString(TAG);
            MLog.v(TAG, "MyResultReceiver, msgId is %s, and resultCode is %d.", msgId, resultCode);
            switch (resultCode){
                case BaseBean.SUCCESS:
                    mMsgTimer.onMessageSuccess(msgId);
                    break;
                case BaseBean.FAILED:
                    mMsgTimer.onMessageFailed(msgId, -1, "failed by send");
                    break;
                default:
                    throw new IllegalStateException("not handle result code");
            }
        }
    }
}
