package com.travel.imserver.receiver;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.travel.imserver.BuildConfig;
import com.travel.imserver.Callback;
import com.travel.imserver.bean.BaseBean;

import junit.framework.Assert;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by ldkxingzhe on 2016/12/8.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class AbstractReceiverTest extends AbstractReceiver{
    private static final String TAG = AbstractReceiver.class.getName();
    public AbstractReceiverTest(Context context, ResultReceiver resultReceiver) {
        super(context, resultReceiver);
    }

    @Override
    public boolean isDealWith(BaseBean obj) {
        return true;
    }

    @Override
    public void dealWith(BaseBean obj) {
        Log.v(TAG, "dealWith");
        Bundle bundle = new Bundle();
        bundle.putParcelable("bean", obj);
        mResultReceiver.send(0, bundle);
    }

    public static class MyResultReceiver extends AbstractReceiver.MyResultReceiver{
        private Callback mCallback;
        public MyResultReceiver(Callback callback){
            mCallback = callback;
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            Log.v(TAG, "onReceiverResult");
            Assert.assertEquals(0, resultCode);
            BaseBean baseBean = resultData.getParcelable("bean");
            mCallback.onError(0, String.valueOf(baseBean.getType()));
        }
    }
}
