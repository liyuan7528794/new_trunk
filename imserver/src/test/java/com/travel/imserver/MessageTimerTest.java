package com.travel.imserver;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.ShadowLooper;

/**
 * Created by ldkxingzhe on 2016/12/7.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class MessageTimerTest {
    private MessageTimer mMessageTimer;
    private volatile String mGlobalMsg;
    private ShadowLooper mShadowLooper;
    private int mCount;

    @Before
    public void setUp(){
        mMessageTimer = new MessageTimer();
        mShadowLooper = (ShadowLooper) ShadowExtractor.extract(mMessageTimer.mHandler.getLooper());
        mCount = 0;
    }
    @Test
    public void addMessage() throws Exception {
        BaseMessage baseMessage = new BaseMessage("fjkdjkf", "ok");
        mMessageTimer.addMessage(baseMessage, new Callback() {
            @Override
            public void onSuccess() {
                Assert.assertEquals(false, true);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Assert.assertEquals(errorCode, MessageTimer.TIME_OUT);
                mGlobalMsg = "from error";
            }
        });
        mShadowLooper.runToEndOfTasks();
        mShadowLooper.runToEndOfTasks();
        Thread.sleep(6000);
        Assert.assertEquals(mGlobalMsg, "from error");
    }

    @Test
    public void onMessageSuccess() throws Exception{
        BaseMessage baseMessage = new BaseMessage("message success", "梁");
        MessageTimer.ID id = mMessageTimer.new ID(baseMessage);
        mMessageTimer.addMessageInl(id, new Callback() {
            @Override
            public void onSuccess() {
                mGlobalMsg = "from success";
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                throw new IllegalStateException("should come here");
            }
        });
        mMessageTimer.onMessageResultInl(baseMessage.getId(), true, 0, "");
        Assert.assertEquals(mGlobalMsg, "from success");
    }

    @Test
    public void onMessageFailed() throws Exception{
        BaseMessage baseMessage = new BaseMessage("message success", "梁");
        MessageTimer.ID id = mMessageTimer.new ID(baseMessage);
        mMessageTimer.addMessageInl(id, new Callback() {
            @Override
            public void onSuccess() {
                mGlobalMsg = "from success";
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Assert.assertEquals(-1, errorCode);
                Assert.assertEquals("", errorMsg);
                mGlobalMsg = "from error";
            }
        });
        mMessageTimer.onMessageResultInl(baseMessage.getId(), false, -1, "");
        Assert.assertEquals(mGlobalMsg, "from error");
    }

    @Test
    public void findNextTimeOutId() throws Exception{
        BaseMessage baseMessage = new BaseMessage("message1", "");
        MessageTimer.ID id = mMessageTimer.new ID(baseMessage);
        mMessageTimer.addMessageInl(id, new Callback() {
            @Override
            public void onSuccess() {
                throw new IllegalStateException("not come here");
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                mCount++;
                Assert.assertEquals(errorCode, MessageTimer.TIME_OUT);
            }
        });
        BaseMessage baseMessage2 = new BaseMessage("message2", "");
        MessageTimer.ID id2 = mMessageTimer.new ID(baseMessage2);
        mMessageTimer.addMessageInl(id2, new Callback() {
            @Override
            public void onSuccess() {
                throw new IllegalStateException("not come here");
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                mCount++;
                Assert.assertEquals(errorCode, MessageTimer.TIME_OUT);
            }
        });
        Thread.sleep(3000);

        BaseMessage baseMessage3 = new BaseMessage("message3", "");
        MessageTimer.ID id3 = mMessageTimer.new ID(baseMessage3);
        mMessageTimer.addMessageInl(id3, new Callback() {
            @Override
            public void onSuccess() {
                throw new IllegalStateException("not come here");
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                mCount++;
                Assert.assertEquals(errorCode, MessageTimer.TIME_OUT);
            }
        });
        Thread.sleep(1000);
        mMessageTimer.findNextTimeOutID();
        Assert.assertEquals(2, mCount);
    }
}