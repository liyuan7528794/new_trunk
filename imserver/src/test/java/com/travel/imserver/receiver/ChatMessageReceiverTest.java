package com.travel.imserver.receiver;

import android.os.Bundle;

import com.travel.communication.dao.Message;
import com.travel.communication.entity.MessageEntity;
import com.travel.communication.helper.SQliteHelper;
import com.travel.imserver.BuildConfig;
import com.travel.imserver.bean.BaseBean;
import com.travel.lib.TravelApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by ldkxingzhe on 2016/12/13.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class ChatMessageReceiverTest {

    private SQliteHelper mSQliteHelper;
    private ChatMessageReceiver mChatMessageReceiver;
    @Before
    public void setUp() throws Exception{
        mSQliteHelper = new SQliteHelper(RuntimeEnvironment.application);
        mChatMessageReceiver = new ChatMessageReceiver(RuntimeEnvironment.application, myResultReceiver);
        TravelApp.appContext = RuntimeEnvironment.application;
    }

    private ChatMessageReceiver.MyResultReceiver myResultReceiver = new ChatMessageReceiver.MyResultReceiver(){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            System.out.println("resultCode");
        }
    };

    @Test
    public void testSql() throws Exception{
        BaseBean baseBean = new BaseBean();
        baseBean.setType(BaseBean.TYPE_SINGLE_CHAT);
        baseBean.setId("test");
        baseBean.setSendUser("9");
        MessageEntity entity = new MessageEntity.MessageBuilder()
                .setContent("Who are you?")
                .setType(MessageEntity.TYPE_TEXT)
                .setReceiverId("4")
                .setSenderId("9")
                .setSendTime(new Date())
                .build();
        baseBean.setMsgBody(entity.generateJson(mSQliteHelper));

        mChatMessageReceiver.dealWith(baseBean);
        List<Message> messageList = mSQliteHelper.getCommunicationMessage("4", "9", 10);
        assertEquals(1, messageList.size());
        assertEquals("Who are you?", messageList.get(0).getContent());
    }
}