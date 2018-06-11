package com.travel.imserver;

import com.google.gson.Gson;
import com.travel.imserver.bean.BaseBean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

/**
 * Created by ldkxingzhe on 2016/12/7.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class NettyClientTest {
    private NettyClient mNettyClient;
    private Gson mGson;

    @Before
    public void initNettyClient(){
        ShadowLog.stream = System.out;
        mNettyClient = new NettyClient();
        mGson = new Gson();
    }

    @After
    public void tearDown(){
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mNettyClient.onDestroy();
    }

    @Test
    public void sendBaseMessage() throws Exception{
        Thread.sleep(2000);
        System.out.println("sendBaseMessage");
        BaseBean baseBean = new BaseBean();
        baseBean.setId("test");
        baseBean.setMsgBody("12345678");
        baseBean.setSendUser("9");
        baseBean.setType(-1);
        baseBean.setMsgHead("test");
        BaseMessage baseMessage = new BaseMessage(mGson.toJson(baseBean, BaseBean.class));
        mNettyClient.sendMessage(baseMessage);
    }

    @Test
    public void testACK(){
        BaseBean baseBean = new BaseBean();
        baseBean.setId("tesss");
        baseBean.setType(2);
        baseBean.setSendUser("9");
        mNettyClient.sendMessage(new BaseMessage(mGson.toJson(baseBean)));
    }

    @Test
    public void sendMsgAndClose() throws Exception{
        Thread.sleep(2000);
        MessageDispatcher msgDispatcher = mNettyClient.getClientHandler().getMsgDispatcher();
        msgDispatcher.registerReceiver("test", new Receiver<BaseBean>() {
            @Override
            public boolean isDealWith(BaseBean obj) {
                return true;
            }

            @Override
            public void dealWith(BaseBean obj) {
                System.out.println(obj);
            }
        });
        new Thread(){
            @Override
            public void run() {
                BaseMessage baseMessage = new BaseMessage("123", "jkdjfkdj");
                for(int i = 0; i < 10; i++){
                    mNettyClient.sendMessage(baseMessage);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        Thread.sleep(30);
        mNettyClient.onDestroy();
    }

    @Test
    public void testException() throws Exception{
        new Thread(){
            @Override
            public void run() {
                BaseMessage baseMessage = new BaseMessage("123", "{\"id\":\"支持格式化高亮折叠\"}");
                for(int i = 0; i < 20; i++){
                    mNettyClient.sendMessage(baseMessage);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        Thread.sleep(20000);
    }

    @Test
    public void shutDown() throws Exception{
        mNettyClient.onDestroy();
    }

    @Test
    public void testServerClose() throws Exception{
        BaseMessage baseMessage = new BaseMessage("test", "{\"id\":\"支持格式化高亮折叠\",\"type\":\"-1\"}");
        for (int i = 0; i < 20; i++){
            mNettyClient.sendMessage(baseMessage);
            Thread.sleep(500);
        }
    }

    @Test
    public void onDestroy() throws Exception{
        mNettyClient.sendMessage(new BaseMessage("heartBeat", "{\"type\":0,\"sendUser\":\"9\",\"msgHead\":\"heartBeat\"}"));
        mNettyClient.shutdownAll();
        Thread.sleep(4000);
    }
}
