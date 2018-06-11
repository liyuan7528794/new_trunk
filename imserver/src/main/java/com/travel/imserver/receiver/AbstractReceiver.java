package com.travel.imserver.receiver;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;

import com.travel.imserver.Receiver;
import com.travel.imserver.bean.BaseBean;

/**
 * 接收器的抽象类
 * Created by ldkxingzhe on 2016/12/8.
 */
public abstract class AbstractReceiver implements Receiver<BaseBean>{
    @SuppressWarnings("unused")
    private static final String TAG = "AbstractReceiver";

    protected ResultReceiver mResultReceiver;
    protected Context mContext;
    public AbstractReceiver(Context context, ResultReceiver resultReceiver){
        this.mContext = context;
        this.mResultReceiver = resultReceiver;
    }

    public static class MyResultReceiver extends ResultReceiver{
        public MyResultReceiver(Handler handler) {
            super(handler);
        }

        public MyResultReceiver(){
            this(new Handler(Looper.getMainLooper()));
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
        }
    }
}
