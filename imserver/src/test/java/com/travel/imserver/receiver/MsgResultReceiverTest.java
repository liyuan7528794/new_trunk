package com.travel.imserver.receiver;

import com.google.gson.Gson;
import com.travel.imserver.BaseMessage;
import com.travel.imserver.BuildConfig;
import com.travel.imserver.MessageDispatcher;
import com.travel.imserver.MessageTimer;
import com.travel.imserver.NettyClient;
import com.travel.imserver.bean.BaseBean;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by ldkxingzhe on 2016/12/9.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class MsgResultReceiverTest {

    private NettyClient mNettyClient;
    private String mGlobal;

    @Test
    public void testResultSuccess() throws Exception{
        mNettyClient = new NettyClient();
        BaseBean baseBean = new BaseBean();
        baseBean.setId("test");
        baseBean.setType(-1);
        Gson gson = new Gson();
        BaseMessage baseMessage = new BaseMessage("test", gson.toJson(baseBean));
        MessageDispatcher msgDispatcher = mNettyClient.getClientHandler().getMsgDispatcher();
        msgDispatcher.registerReceiver("test", new MsgResultReceiver(null, new MsgResultReceiver.MyResultReceiver(new MessageTimer(){
            @Override
            public void onMessageFailed(String id, int errorCode, String errorMsg) {
                Assert.assertEquals("test", id);
            }

            @Override
            public void onMessageSuccess(String id) {
                Assert.assertEquals("test", id);
                mGlobal = "from success";
            }
        })));
        boolean result = mNettyClient.sendMessage(baseMessage);
        Assert.assertEquals(true, result);
        Thread.sleep(3000);
        Assert.assertEquals("from success", mGlobal);
    }
}