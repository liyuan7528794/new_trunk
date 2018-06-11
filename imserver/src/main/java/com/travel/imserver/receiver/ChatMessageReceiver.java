package com.travel.imserver.receiver;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.travel.communication.entity.MessageEntity;
import com.travel.communication.entity.UserData;
import com.travel.communication.helper.SQliteHelper;
import com.travel.imserver.Receiver;
import com.travel.imserver.ResultCallback;
import com.travel.imserver.bean.BaseBean;
import com.travel.lib.utils.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 聊天消息的接受处理者
 */
public class ChatMessageReceiver extends AbstractReceiver{
    @SuppressWarnings("unused")
    private static final String TAG = "ChatMessageReceiver";
    private SQliteHelper mSQliteHelper;
    private MessageCommonParser mMessageCommonParser;

    public ChatMessageReceiver(Context context, ResultReceiver resultReceiver) {
        super(context, resultReceiver);
        mSQliteHelper = new SQliteHelper(mContext);
        mMessageCommonParser = new MessageCommonParser(mSQliteHelper);
    }

    @Override
    public boolean isDealWith(BaseBean obj) {
        return obj.getType() == BaseBean.TYPE_SINGLE_CHAT
                || obj.getType() == BaseBean.TYPE_GROUP_CHAT;
    }

    @Override
    public void dealWith(BaseBean obj) {
        String msgBody = obj.getMsgBody();
        if(TextUtils.isEmpty(msgBody)){
            Log.e(TAG, "单聊消息发现为空");
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(msgBody);
            MessageEntity entity = mMessageCommonParser.parserMessageEntity(jsonObject);
            dealWithSql(obj.getType(), entity);
            Bundle bundle = new Bundle();
            bundle.putParcelable(TAG, entity);
            mResultReceiver.send(0, bundle);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void dealWithSql(int type, MessageEntity entity){
        // 入库处理等
        int chatType = type == BaseBean.TYPE_SINGLE_CHAT ? 0 : 1;
        entity.setChatType(chatType);
        long id = mSQliteHelper.insertMessage(entity.getMessage());
        entity.setId(id);
        if(type == BaseBean.TYPE_SINGLE_CHAT){
            mSQliteHelper.lastMessageAddOne(entity.getMessage(), true, true);
        }
    }

    public static class MessageCommonParser{
        private final SQliteHelper sQliteHelper;
        public MessageCommonParser(SQliteHelper sQliteHelper) {
            this.sQliteHelper = sQliteHelper;
        }

        private void dealWithSenderInfo(MessageEntity entity, UserData userData){
            // 发送者用户信息处理
            sQliteHelper.inserOrReplace(userData);
        }

        public MessageEntity parserMessageEntity(JSONObject messageJson){
            UserData senderUserData = new UserData();
            senderUserData.setId(JsonUtil.getJson(messageJson, MessageEntity.SENDER_ID));
            senderUserData.setImgUrl(JsonUtil.getJson(messageJson, MessageEntity.SENDER_IMG_URL));
            senderUserData.setNickName(JsonUtil.getJson(messageJson, MessageEntity.SENDER_NICK_NAME));
            UserData receiverUserData = new UserData();
            receiverUserData.setId(JsonUtil.getJson(messageJson, MessageEntity.RECEIVER_ID));
            receiverUserData.setImgUrl(JsonUtil.getJson(messageJson, MessageEntity.RECEIVER_IMG_URL));
            receiverUserData.setNickName(JsonUtil.getJson(messageJson, MessageEntity.RECEIVER_NICK_NAME));

            MessageEntity.MessageBuilder builder = new MessageEntity.MessageBuilder();
            String createTimeStr = JsonUtil.getJson(messageJson, MessageEntity.CREATE_TIME);
            Date createTime = TextUtils.isEmpty(createTimeStr) ? new Date() : new Date(Long.valueOf(createTimeStr) * 1000);
            builder.setContent(JsonUtil.getJson(messageJson, MessageEntity.CONTENT))
                    .setSendTime(createTime)
                    .setType(Integer.valueOf(JsonUtil.getJson(messageJson, MessageEntity.TYPE)))
                    .setState(MessageEntity.STATE_SUCCESS)
                    .setTimeLong((Long.valueOf("".equals(JsonUtil.getJson(messageJson, MessageEntity.TIME_LONG)) ? "0" : JsonUtil.getJson(messageJson, MessageEntity.TIME_LONG))))
                    .setId(-1); // 这样做的目的是使数据插入不报异常
            MessageEntity entity = builder.build();
            entity.setSenderId(senderUserData.getId());
            entity.setReceiverId(receiverUserData.getId());
            dealWithSenderInfo(entity, senderUserData);
            return entity;
        }

        public void dealWithGroupSql(@NonNull MessageEntity entity){
            
        }
    }


    // 所有消息
    public static class MyResultReceiver extends AbstractReceiver.MyResultReceiver{
        private Map<String, Receiver<MessageEntity>> mReceiver;
        public MyResultReceiver(){
            mReceiver = new HashMap<>();
        }

        public void registerReceiver(String receiverName, Receiver<MessageEntity> receiver){
            mReceiver.put(receiverName, receiver);
        }

        public void unRegisterReceiver(String receiverName){
            mReceiver.remove(receiverName);
        }

        public void clearReceiver(){
            mReceiver.clear();
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if(resultCode != 0){
                Log.e(TAG, "resultCode must be zero");
                return;
            }

            resultData.setClassLoader(MessageEntity.class.getClassLoader());
            MessageEntity entity = resultData.getParcelable(TAG);
            for(Receiver<MessageEntity> receiver : mReceiver.values()){
                if(receiver.isDealWith(entity)){
                    receiver.dealWith(entity);
                }
            }
        }
    }


    // 订阅特定的单聊, 群聊理论上也可以
    public static class SingleChatReceiver implements Receiver<MessageEntity>{
        private ResultCallback<MessageEntity> mCallback;
        private String mPeerId;
        public SingleChatReceiver(String pairId, ResultCallback<MessageEntity> callback) {
            mCallback = callback;
            mPeerId = pairId;
        }

        @Override
        public boolean isDealWith(MessageEntity obj) {
            return mPeerId.equals(obj.getSenderId());
        }

        @Override
        public void dealWith(MessageEntity obj) {
            mCallback.onResult(obj);
        }
    }
}
