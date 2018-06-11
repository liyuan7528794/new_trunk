package com.tencent.qcloud.suixinbo.presenters;


import android.text.TextUtils;

import com.tencent.TIMCallBack;
import com.tencent.TIMConversationType;
import com.tencent.TIMFriendshipManager;
import com.tencent.TIMGroupManager;
import com.tencent.TIMGroupMemberInfo;
import com.tencent.TIMManager;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;
import com.tencent.av.sdk.AVContext;
import com.tencent.av.sdk.AVError;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.qcloud.suixinbo.avcontrollers.QavsdkControl;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.utils.Constants;
import com.tencent.qcloud.suixinbo.utils.LogConstants;
import com.tencent.qcloud.suixinbo.utils.SxbLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 出入房间的辅助类, 包括功能如下:
 * - IM房间功能: 创建房间, 进入房间, 退出房间, 删除房间
 * - AV房间功能: 创建房间, 进入房间, 退出房间, 删除房间
 * - 音频功能: 初始化音频服务, 反初始化音频服务
 * Created by ldkxingzhe on 2016/8/11.
 */
public abstract class AbstractEnterLiveHelper extends Presenter {
    @SuppressWarnings("unused")
    private static final String TAG = "AbstractEnterLiveHelper";

    private static final String FTS = "A_N_D";

    protected volatile boolean isInAVRoom = false;
    private volatile boolean isInChatRoom = false;
    private volatile boolean isAlreadyInChatRoom = false; // 是否仍在聊天群中

    /**
     * 创建一个用于AV直播的聊天室
     */
    protected void createIMChatRoom(final String groupId, final TIMValueCallBack<String> createIMCallback) {
        final ArrayList<String> list = new ArrayList<String>();
        final String roomName = "this is a  test";
        TIMGroupManager.getInstance().createGroup("AVChatRoom", list, roomName, groupId, new TIMValueCallBack<String>() {
            @Override
            public void onError(int i, String s) {
                SxbLog.i(TAG, "onError " + i + "   " + s);
                //已在房间中,重复进入房间
                if (i == 10025) {
                    onSuccess("已在房间, 重复进入房间");
                    return;
                }
                // 创建IM房间失败，提示失败原因，并关闭等待对话框
                SxbLog.standardEnterRoomLog(TAG, "create live im group",
                        "" + LogConstants.STATUS.FAILED, "code：" + i + " msg:" + s);
                createIMCallback.onError(i, s);
            }

            @Override
            public void onSuccess(String s) {
                SxbLog.standardEnterRoomLog(TAG, "create live im group",
                        "" + LogConstants.STATUS.SUCCEED, "group id " + groupId);
                isInChatRoom = true;
                //创建AV房间
                createIMCallback.onSuccess(s);
            }
        });
    }

    /* 返回是否在AV房间里面 */
    public boolean isInAVRoom(){
        return isInAVRoom;
    }

    public boolean isInIMRoom(){
        return isInChatRoom;
    }

    /**
     * 改变群名片信息
     */
    public void changeGroupUserInfo(){
        String roomId = String.valueOf(CurLiveInfo.getInstance().getRoomNum());
        String userId = MySelfInfo.getInstance().getId();
        String nickName = MySelfInfo.getInstance().getNickName();
        String imgUrl = MySelfInfo.getInstance().getAvatar();
        if(TextUtils.isEmpty(nickName)){
            SxbLog.e(TAG, "nickName must be not null");
            nickName = "NickName Must Be Not Null";
        }
        if(TextUtils.isEmpty(imgUrl)){
            SxbLog.e(TAG, "imgUrl Must be Not NUll");
            imgUrl = "IMG_URL MUST BE NOT NULL";
        }
        String nameCardAndImgUrl = nickName + FTS + imgUrl;
        TIMGroupManager.getInstance().modifyGroupMemberInfoSetNameCard(roomId, userId, nameCardAndImgUrl, new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                SxbLog.e(TAG, "modifyGroupMemberInfoSetNameCard failed: error code is " + i + ", error is " + s);
            }

            @Override
            public void onSuccess() {
                SxbLog.v(TAG, "modifyGroupMemberInfoSetNameCard success");
            }
        });
    }

    /* 获取群组成员信息 */
    public void getGroupMembers(){
        String roomId = String.valueOf(CurLiveInfo.getInstance().getRoomNum());
        TIMGroupManager.getInstance().getGroupMembers(roomId,
                new TIMValueCallBack<List<TIMGroupMemberInfo>>() {
            @Override
            public void onError(int i, String s) {
                SxbLog.e(TAG, "mGroupMemberCallback, and error code is " + i + ", and reason is " + s);
            }

            @Override
            public void onSuccess(List<TIMGroupMemberInfo> timGroupMemberInfos) {
                // 成功获取成员信息
                List<String> userIds = new ArrayList<String>();
                for(TIMGroupMemberInfo info : timGroupMemberInfos){
                    String userId = info.getUser();
                    if(!CurLiveInfo.getInstance().getHostID().equals(userId)){
                        userIds.add(userId);
                    }
                }

                getUsersProfile(userIds, new TIMValueCallBack<List<TIMUserProfile>>() {
                    @Override
                    public void onError(int i, String s) {
                        SxbLog.e(TAG, "getUsersProfile failed: error code is "
                                + i + ", and error reason is " + s);
                    }

                    @Override
                    public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                        onGetGroupMembers(timUserProfiles);
                    }
                });
            }
        });
    }

    /**
     * 根据Id获取用户信息
     * @param userIds ids
     */
    public static void getUsersProfile(List<String> userIds, TIMValueCallBack<List<TIMUserProfile>> cb){
        TIMFriendshipManager.getInstance().getUsersProfile(userIds, cb);
    }
    /* 可选继承 */
    protected void onGetGroupMembers(List<TIMUserProfile> groupMemberInfoList){
    }

    /**
     * 加入一个聊天室
     */
    protected void joinIMChatRoom(final String chatRoomId, final TIMCallBack callBack) {
        SxbLog.standardEnterRoomLog(TAG, "join im chat room", "", "room id " + chatRoomId);
        TIMGroupManager.getInstance().applyJoinGroup("" + chatRoomId, Constants.APPLY_CHATROOM + chatRoomId, new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                //已经在是成员了
                if (i == Constants.IS_ALREADY_MEMBER) {
                    isAlreadyInChatRoom = true;
                    onSuccess();
                } else {
                    SxbLog.standardEnterRoomLog(TAG, "join im chat room", "" + LogConstants.STATUS.FAILED, "code:" + i + " msg:" + s);
                    callBack.onError(i, s);
                }
            }

            @Override
            public void onSuccess() {
                SxbLog.standardEnterRoomLog(TAG, "join im chat room", "" + LogConstants.STATUS.SUCCEED, "room id " + chatRoomId);
                isInChatRoom = true;
                callBack.onSuccess();
            }
        });
    }

    /**
     * 在调用进入房间指令前， 是否已经在聊天群中
     */
    public boolean isInChatRoomInOrigin(){
        return isAlreadyInChatRoom;
    }

    /**
     * 退出IM房间
     */
    protected void quiteIMChatRoom(boolean deleteGroup, final String roomNum) {
        if ((isInChatRoom)) {
            //主播解散群
            if (deleteGroup) {
                TIMGroupManager.getInstance().deleteGroup(roomNum, new TIMCallBack() {
                    @Override
                    public void onError(int i, String s) {
                        SxbLog.standardQuiteRoomLog(TAG, "delete im room", "" + LogConstants.STATUS.FAILED, "code:" + i + " msg:" + s);
                    }

                    @Override
                    public void onSuccess() {
                        SxbLog.standardQuiteRoomLog(TAG, "delete im room", "" + LogConstants.STATUS.SUCCEED, "room id " + roomNum);
                        isInChatRoom = false;
                    }
                });
                TIMManager.getInstance().deleteConversation(TIMConversationType.Group, "" + MySelfInfo.getInstance().getMyRoomNum());
            } else {
                //成员退出群
                TIMGroupManager.getInstance().quitGroup(roomNum, new TIMCallBack() {
                    @Override
                    public void onError(int i, String s) {
                        SxbLog.standardQuiteRoomLog(TAG, "quite im room", "" + LogConstants.STATUS.FAILED, "code:" + i + " msg:" + s);
                    }

                    @Override
                    public void onSuccess() {
                        SxbLog.standardQuiteRoomLog(TAG, "quite im room", "" + LogConstants.STATUS.SUCCEED, "room id " + roomNum);
                        isInChatRoom = false;
                    }
                });
            }
        }
    }

    /**
     * 进入或创建AV房间
     * @param param 进入房间的参数
     * @param delegate 进入房间的回调
     */
    protected void enterAVRoom(AVRoomMulti.EventListener delegate, AVRoomMulti.EnterParam param) {
        AVContext avContext = QavsdkControl.getInstance().getAVContext();
        if (avContext != null) {
            int ret = avContext.enterRoom(delegate, param);
            SxbLog.i(TAG, "EnterAVRoom " + ret);
        }
        QavsdkControl.getInstance().clearVideoMembers();
    }

    /**
     * 退出一个AV房间
     */
    protected boolean quiteAVRoom() {
        SxbLog.standardQuiteRoomLog(TAG, "quit av room", "", "");
        if (isInAVRoom) {
            AVContext avContext = QavsdkControl.getInstance().getAVContext();
            if(avContext == null)
                return false;

            int result = avContext.exitRoom();
            return result == AVError.AV_OK;
        }
        return false;
    }

    /**
     * 初始化音频服务
     */
    protected void initAudioService() {
        if ((QavsdkControl.getInstance() != null) && (QavsdkControl.getInstance().getAVContext() != null) && (QavsdkControl.getInstance().getAVContext().getAudioCtrl() != null)) {
            QavsdkControl.getInstance().getAVContext().getAudioCtrl().startTRAEService();
        }
    }

    /* 反初始化音频服务 */
    protected void unInitAudioService() {
        if ((QavsdkControl.getInstance() != null) && (QavsdkControl.getInstance().getAVContext() != null) && (QavsdkControl.getInstance().getAVContext().getAudioCtrl() != null)) {
            QavsdkControl.getInstance().getAVContext().getAudioCtrl().stopTRAEService();
        }
    }
}