package com.travel.communication.helper;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.tencent.TIMCallBack;
import com.tencent.TIMConversation;
import com.tencent.TIMConversationType;
import com.tencent.TIMCustomElem;
import com.tencent.TIMGroupDetailInfo;
import com.tencent.TIMGroupManager;
import com.tencent.TIMManager;
import com.tencent.TIMMessage;
import com.tencent.TIMValueCallBack;
import com.tencent.qcloud.suixinbo.presenters.AbstractEnterLiveHelper;
import com.travel.communication.entity.MessageEntity;
import com.travel.communication.entity.UserData;
import com.travel.imserver.ResultCallback;
import com.travel.lib.utils.MLog;

import java.util.Arrays;
import java.util.List;

/**
 * 群聊消息的辅助类
 * Created by ldkxingzhe on 2017/1/9.
 */
public class GroupMessageSenderHelper extends MessageSenderHelper
        implements ResultCallback<MessageEntity>{
    @SuppressWarnings("unused")
    private static final String TAG = "GroupMessageSender";
    private TIMConversation mGroupConversation;
    private GroupMessageEnterHelper mEnterHelper;
    private TIMGroupMessageReceiver mMsgReceiver;
    private boolean mIsAlreadyInGroup = false;
    private LocalBroadcastManager mLocalBroadcastManager;

    public static final String ACTION_GROUP_INFO = "action:group_message_sender_group_info";
    public static final String ACTION_IS_FOLLOW_THIS_GROUP = "action:group_message_is_follow_group";

    protected GroupMessageSenderHelper(Context context, int chatType, String senderId, String receiverId) {
        super(context, chatType, senderId, receiverId);
        mMsgReceiver = new TIMGroupMessageReceiver(context);
        mMsgReceiver.setResultCallback(this);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @Override
    protected void initIMServer(final String receiverId) {
        if(mEnterHelper == null){
            mEnterHelper = new GroupMessageEnterHelper();
        }
        mReceiverId = receiverId;
        mEnterHelper.joinIMChatRoom(receiverId, new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                MLog.d(TAG, "joinIMChatRoom failed: %d, %s", i, s);
                Toast.makeText(mContext, "进入群组失败， 重试多次， 已放弃治疗", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                MLog.d(TAG, "joinIMChatRoom %s success.", receiverId);
                mGroupConversation = TIMManager.getInstance().getConversation(TIMConversationType.Group, receiverId);
                TIMManager.getInstance().addMessageListener(mMsgReceiver);
                getGroupInfo();
                mIsAlreadyInGroup = mEnterHelper.isInChatRoomInOrigin();
                if (!mIsAlreadyInGroup){
                    Intent intent = new Intent(ACTION_IS_FOLLOW_THIS_GROUP);
                    intent.putExtra("group-id", mReceiverId);
                    mLocalBroadcastManager.sendBroadcast(intent);
                }
            }
        });
    }

    private void getGroupInfo() {
        List<String> groupList = Arrays.asList(mReceiverId);
        TIMGroupManager.getInstance().getGroupPublicInfo(groupList, new TIMValueCallBack<List<TIMGroupDetailInfo>>() {
            @Override
            public void onError(int i, String s) {
                MLog.e(TAG, "获取群资料失败");
            }

            @Override
            public void onSuccess(List<TIMGroupDetailInfo> timGroupDetailInfos) {
                if (timGroupDetailInfos == null || timGroupDetailInfos.size() != 1) return;

                TIMGroupDetailInfo info = timGroupDetailInfos.get(0);
                UserData userData = new UserData();
                userData.setId(info.getGroupId());
                userData.setNickName(info.getGroupName());
                userData.setImgUrl(info.getFaceUrl());
                mSQliteHelper.inserOrReplace(userData);
                Intent intent = new Intent(ACTION_GROUP_INFO);
                intent.putExtra("group-name", userData.getNickName());
                intent.putExtra("group-id", mReceiverId);
                mLocalBroadcastManager.sendBroadcast(intent);
            }
        });
    }

    public void followGroup(boolean isFollow){
        this.mIsAlreadyInGroup = isFollow;
    }

    @Override
    protected void imServerSend(MessageEntity entity, long id) {
        final String messageStr = entity.generateJson(mSQliteHelper);
        sendCustomStr(messageStr);
    }

    public void sendCustomStr(final String messageStr) {
        if (mGroupConversation != null){
            TIMMessage msg = new TIMMessage();
            TIMCustomElem elem = new TIMCustomElem();
            elem.setData(messageStr.getBytes());
            msg.addElement(elem);
            mGroupConversation.sendMessage(msg, new TIMValueCallBack<TIMMessage>() {
                @Override
                public void onError(int i, String s) {
                    MLog.e(TAG, "消息发送失败：" + messageStr);
                }

                @Override
                public void onSuccess(TIMMessage timMessage) {
                    MLog.v(TAG, "消息发送成功: %s", messageStr);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        if (mGroupConversation != null){
            mGroupConversation.setReadMessage();
        }
        TIMManager.getInstance().removeMessageListener(mMsgReceiver);
        if(mEnterHelper != null && !mIsAlreadyInGroup){
            mEnterHelper.quiteIMChatRoom(false, mReceiverId);
            mSQliteHelper.lastMessageRemoveConversation(mSenderId, mReceiverId);
        }
    }

    @Override
    public void onResult(MessageEntity obj) {
        super.onResult(obj);
    }

    private void dealWithLastMessage(MessageEntity entity){
        // 待完善
    }

    private class GroupMessageEnterHelper extends AbstractEnterLiveHelper{

        @Override
        protected void joinIMChatRoom(String chatRoomId, TIMCallBack callBack) {
            super.joinIMChatRoom(chatRoomId, callBack);
        }

        @Override
        protected void quiteIMChatRoom(boolean deleteGroup, String roomNum) {
            super.quiteIMChatRoom(deleteGroup, roomNum);
        }

        public TIMConversation getConversation(){
            return mGroupConversation;
        }

        @Override
        public void onDestroy() {

        }
    }
}
