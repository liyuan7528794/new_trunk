package com.travel.imserver.receiver;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.travel.imserver.ResultCallback;
import com.travel.imserver.bean.BaseBean;

/**
 * 众投部分的接收器
 * Created by ldkxingzhe on 2016/12/13.
 */
public class PKVoteReceiver extends AbstractReceiver{
    @SuppressWarnings("unused")
    private static final String TAG = "PKVoteReceiver";

    public PKVoteReceiver(Context context, ResultReceiver resultReceiver) {
        super(context, resultReceiver);
    }

    @Override
    public boolean isDealWith(BaseBean obj) {
        return obj.getType() == 3;
    }

    @Override
    public void dealWith(BaseBean obj) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(TAG, obj);
        mResultReceiver.send(0, bundle);
    }

    public static class MyResultReceiver extends AbstractReceiver.MyResultReceiver{
        private ResultCallback<BaseBean> mCallback;
        public MyResultReceiver(ResultCallback<BaseBean> callback){
            mCallback = callback;
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            resultData.setClassLoader(BaseBean.class.getClassLoader());
            BaseBean baseBean = resultData.getParcelable(TAG);
            mCallback.onResult(baseBean);
        }
    }
}
