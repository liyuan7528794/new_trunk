package com.travel.imserver;

import com.google.gson.Gson;
import com.travel.imserver.bean.BaseBean;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by ldkxingzhe on 2016/12/19.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class SocketClientTest {

    private SocketClient mSocketClient;
    private ScheduledExecutorService mService = Executors.newScheduledThreadPool(3);
    private String mGlobal;
    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        mSocketClient = new SocketClient("localhost", 8080, mService, new SocketClient.HandlerListener() {
            @Override
            public void channelRead(BaseBean baseBean) {
                System.out.println(baseBean);
            }

            @Override
            public void channelActive() {
                System.out.println("channelActive");
                mGlobal = "true";
            }

            @Override
            public void channelInactive() {
                System.out.println("channelInactive");
            }

            @Override
            public void exceptionCaught(Throwable e) {
                System.out.println("exceptionCaught");
            }

            @Override
            public void onTimeout(boolean isRead) {
                System.out.println("onTimeout, and isRead " + isRead);
            }
        });
    }

    @Test
    public void connection() throws Exception{
        mSocketClient.connect();
        Assert.assertEquals("true", mGlobal);
        BaseBean baseBean = new BaseBean();
        baseBean.setId("123e");
        baseBean.setType(3);
        baseBean.setSendUser("9");
        Gson gson = new Gson();
        String msg = gson.toJson(baseBean) + "\n";
        for(int i = 0; i < 10; i++){
        mSocketClient.sendMsg(msg, new Callback() {
            @Override
            public void onSuccess() {
                System.out.println("onSuccess");
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                System.out.println("onError");
            }
        });
            Thread.sleep(500);
        }
        Thread.sleep(7000);
    }

    @Test
    public void backgroundConnect() throws Exception{
        mService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocketClient.connect();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        });

        Thread.sleep(3000);
    }

    @After
    public void tearDown() throws Exception{
        if(mSocketClient != null){
            mSocketClient.disConnect();
        }
    }


    @Test
    public void sendMultiThread() throws Exception{
        mSocketClient.connect();
        for (int i = 0; i < 100; i++){
            final int finalI = i;
            mService.submit(new Runnable() {
                @Override
                public void run() {
                    Gson gson = new Gson();
                    final BaseBean baseBean = new BaseBean();
                    baseBean.setId("test" + finalI);;
                    baseBean.setType(3);
                    baseBean.setSendUser("9");
                    baseBean.setMsgHead("OK");
                    mSocketClient.sendMsg(gson.toJson(baseBean, BaseBean.class) + "\n", new Callback() {
                        @Override
                        public void onSuccess() {
                            System.out.println(baseBean.getId() + " success");
                        }

                        @Override
                        public void onError(int errorCode, String errorMsg) {
                            System.out.println(baseBean.getId() + " " + errorMsg);
                        }
                    });
//                    System.out.println("current thread is: " + Thread.currentThread());
                }
            });
            Thread.sleep(400);
        }

        Thread.sleep(8000);
    }

    @Test
    public void timeout() throws Exception{
        mSocketClient.setTimeout(2000, 3000);
        mSocketClient.connect();
        Gson gson = new Gson();
        for(int i = 0; i < 10; i++){
            BaseBean baseBean = new BaseBean();
            baseBean.setId("chat" + i);
            mSocketClient.sendMsg(gson.toJson(baseBean, BaseBean.class) + "\n", new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(int errorCode, String errorMsg) {

                }
            });
            Thread.sleep(500);
        }
        Thread.sleep(8000);
    }
}