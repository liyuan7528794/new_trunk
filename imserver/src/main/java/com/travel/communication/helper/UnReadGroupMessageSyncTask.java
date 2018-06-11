package com.travel.communication.helper;

import android.content.Context;

import com.tencent.TIMConversation;
import com.tencent.TIMConversationType;
import com.tencent.TIMGroupBaseInfo;
import com.tencent.TIMGroupManager;
import com.tencent.TIMManager;
import com.tencent.TIMMessage;
import com.tencent.TIMValueCallBack;
import com.travel.communication.entity.UserData;
import com.travel.lib.utils.MLog;

import java.util.Collections;
import java.util.List;

/**
 * 未读群消息同步设置
 * Created by ldkxingzhe on 2017/1/13.
 */
public class UnReadGroupMessageSyncTask implements Runnable{
    @SuppressWarnings("unused")
    private static final String TAG = "UnReadGroupMessageSyncTask";
    private TIMGroupMessageReceiver mParser;
    private SQliteHelper mSQliteHelper;

    public UnReadGroupMessageSyncTask(Context context){
        mParser = new TIMGroupMessageReceiver(context);
        mSQliteHelper = new SQliteHelper(context);
    }

    @Override
    public void run() {
        TIMGroupManager.getInstance().getGroupList(new TIMValueCallBack<List<TIMGroupBaseInfo>>() {
            @Override
            public void onError(int i, String s) {
                MLog.e(TAG, s + ":" + i);
            }

            @Override
            public void onSuccess(List<TIMGroupBaseInfo> timGroupBaseInfos) {
                for (TIMGroupBaseInfo info : timGroupBaseInfos){
                    dealWithGroupMessage(info);
                }
            }
        });
    }

    private void dealWithGroupMessage(TIMGroupBaseInfo info) {
        final String groupId = info.getGroupId();
        final TIMConversation groupConversation = TIMManager.getInstance().getConversation(TIMConversationType.Group,
                groupId);
        int unReadNum = (int) groupConversation.getUnreadMessageNum();
        MLog.v(TAG, "groupId is %s. and unReadNum is %d.", groupId, unReadNum);
        groupConversation.getMessage(unReadNum, null, new TIMValueCallBack<List<TIMMessage>>() {
            @Override
            public void onError(int i, String s) {
                MLog.e(TAG, s);
            }

            @Override
            public void onSuccess(List<TIMMessage> timMessages) {
                MLog.v(TAG, "groupId: %s fine", groupId);
                Collections.reverse(timMessages);
                mParser.onNewMessages(timMessages);
            }
        });
        groupConversation.setReadMessage();
        // 群资料的入库处理
        UserData userData = new UserData();
        userData.setId(groupId);
        userData.setImgUrl(info.getFaceUrl());
        userData.setNickName(info.getGroupName());
        mSQliteHelper.inserOrReplace(userData);
    }
}
