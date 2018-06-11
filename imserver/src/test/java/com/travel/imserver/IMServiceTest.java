package com.travel.imserver;

import com.travel.imserver.bean.BaseBean;
import com.travel.imserver.receiver.AbstractReceiverTest;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

/**
 * Created by ldkxingzhe on 2016/12/8.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class IMServiceTest {
    private IMService mService;
    private MessageDispatcher mMsgDispatcher;
    private String mGlboal;
    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        mService = Robolectric.buildService(IMService.class).create().get();
        mGlboal = "";
        mMsgDispatcher = mService.mMsgDispatcher;
    }

    @Test
    public void register() throws Exception{
        mService.register("test", AbstractReceiverTest.class.getName(), new AbstractReceiverTest.MyResultReceiver(new Callback() {
            @Override
            public void onSuccess() {
                throw new IllegalStateException("can come here");
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Assert.assertEquals(0, errorCode);
                mGlboal = errorMsg;
            }
        }));
        BaseBean baseBean = new BaseBean();
        baseBean.setType(32);
        mMsgDispatcher.dealWith(baseBean);
        Assert.assertEquals("32", mGlboal);
        mGlboal = null;
        mMsgDispatcher.unRegisterReceiver("test");
        mMsgDispatcher.dealWith(baseBean);
        Assert.assertEquals(null, mGlboal);
    }
}