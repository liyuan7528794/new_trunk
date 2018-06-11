package com.tencent.qcloud.suixinbo.presenters;

import android.content.Context;
import android.content.Intent;

import com.tencent.TIMCallBack;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.presenters.viewinface.EnterQuiteRoomView;
import com.tencent.qcloud.suixinbo.utils.Constants;
import com.tencent.qcloud.suixinbo.utils.LogConstants;
import com.tencent.qcloud.suixinbo.utils.SxbLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/5.
 */
public class LeftRightEnterLiveHelper extends AbstractEnterLiveHelper {
    private EnterQuiteRoomView mStepInOutView;
    private Context mContext;
    private static final String TAG = LeftRightEnterLiveHelper.class.getSimpleName();
    private ArrayList<String> video_ids = new ArrayList<String>();

    private LeftRightLiveHelper mLiveHelper;

    private static final int TYPE_MEMBER_CHANGE_IN = 1;//进入房间事件。
    private static final int TYPE_MEMBER_CHANGE_OUT = 2;//退出房间事件。
    private static final int TYPE_MEMBER_CHANGE_HAS_CAMERA_VIDEO = 3;//有发摄像头视频事件。
    private static final int TYPE_MEMBER_CHANGE_NO_CAMERA_VIDEO = 4;//无发摄像头视频事件。
    private static final int TYPE_MEMBER_CHANGE_HAS_AUDIO = 5;//有发语音事件。
    private static final int TYPE_MEMBER_CHANGE_NO_AUDIO = 6;//无发语音事件。
    private static final int TYPE_MEMBER_CHANGE_HAS_SCREEN_VIDEO = 7;//有发屏幕视频事件。
    private static final int TYPE_MEMBER_CHANGE_NO_SCREEN_VIDEO = 8;//无发屏幕视频事件。


    public LeftRightEnterLiveHelper(Context context, EnterQuiteRoomView view, LeftRightLiveHelper liveHelper) {
        mContext = context;
        mStepInOutView = view;
        mLiveHelper = liveHelper;
    }


    /**
     * 进入一个直播房间流程
     */
    public void startEnterRoom() {
        SxbLog.i(TAG, "joinLiveRoom startEnterRoom ");
        joinLive(CurLiveInfo.getInstance().getRoomNum());
    }

    /**
     * 创建一个IM房间
     */
    public void  createIMRoom(){
        createLive();
    }

    /**
     * 房间回调
     */
    private AVRoomMulti.EventListener mRoomDelegate = new AVRoomMulti.EventListener() {
        // 创建房间成功回调
        public void onEnterRoomComplete(int result) {
            if (result == 0) {
                SxbLog.standardEnterRoomLog(TAG, "enterAVRoom", "" + LogConstants.STATUS.SUCCEED, "room id" + MySelfInfo.getInstance().getMyRoomNum());

                //只有进入房间后才能初始化AvView
                isInAVRoom = true;
                initAudioService();
                String roomId = String.valueOf(CurLiveInfo.getInstance().getRoomNum());
                mLiveHelper.initTIMListener(roomId);
//                changeGroupUserInfo();
                if(MySelfInfo.getInstance().getIdStatus() != Constants.HOST){
                    getGroupMembers();
                }
                mStepInOutView.enterRoomComplete(MySelfInfo.getInstance().getIdStatus(), true);
            } else {
                quiteAVRoom();
                SxbLog.standardEnterRoomLog(TAG, "enterAVRoom", "" + LogConstants.STATUS.FAILED, "result " + result);
            }

        }


        @Override
        public void onRoomDisconnect(int i) {

        }

        // 离开房间成功回调
        @Override
        public void onExitRoomComplete() {
            SxbLog.standardQuiteRoomLog(TAG, "exitRoom", "" + LogConstants.STATUS.SUCCEED, "result ");
            isInAVRoom = false;
            quiteIMChatRoom();
            CurLiveInfo.getInstance().setCurrentRequestCount(0);
            unInitAudioService();
            if (mStepInOutView != null)
                mStepInOutView.quiteRoomComplete(MySelfInfo.getInstance().getIdStatus(), true, true);

        }

        //房间成员变化回调
        public void onEndpointsUpdateInfo(int eventid, String[] updateList) {
            SxbLog.d(TAG, "onEndpointsUpdateInfo. eventid = " + eventid);

            switch (eventid) {
                case TYPE_MEMBER_CHANGE_IN:
                    SxbLog.i(TAG, "stepin id  " + updateList.length);
                    mStepInOutView.memberJoinLive(updateList);

                    break;
                case TYPE_MEMBER_CHANGE_HAS_CAMERA_VIDEO:
                    video_ids.clear();
                    for (String id : updateList) {
                        video_ids.add(id);
                        SxbLog.i(TAG, "camera id " + id);
                    }
                    Intent intent = new Intent(Constants.ACTION_CAMERA_OPEN_IN_LIVE);
                    intent.putStringArrayListExtra("ids", video_ids);
                    mContext.sendBroadcast(intent);
                    break;
                case TYPE_MEMBER_CHANGE_NO_CAMERA_VIDEO: {

                    ArrayList<String> close_ids = new ArrayList<String>();
                    String ids = "";
                    for (String id : updateList) {
                        close_ids.add(id);
                        ids = ids + " " + id;

                    }
                    SxbLog.standardMemberShowLog(TAG,"close camera callback",""+LogConstants.STATUS.SUCCEED ,"close ids " + ids);

                    Intent closeintent = new Intent(Constants.ACTION_CAMERA_CLOSE_IN_LIVE);
                    closeintent.putStringArrayListExtra("ids", close_ids);
                    mContext.sendBroadcast(closeintent);
                }
                break;
                case TYPE_MEMBER_CHANGE_HAS_AUDIO:
                    break;

                case TYPE_MEMBER_CHANGE_OUT:
                    mStepInOutView.memberQuiteLive(updateList);
                    break;

                // TODO 需要更多的事件
                default:
                    break;
            }

        }

        @Override
        public void onPrivilegeDiffNotify(int i) {

        }

        @Override
        public void onCameraSettingNotify(int i, int i1, int i2) {

        }

        @Override
        public void onRoomEvent(int i, int i1, Object o) {

        }

        public void OnPrivilegeDiffNotify(int privilege) {
            SxbLog.d(TAG, "OnPrivilegeDiffNotify. privilege = " + privilege);
        }

        @Override
        public void onSemiAutoRecvCameraVideo(String[] strings) {

            mStepInOutView.alreadyInLive(strings);
        }
    };


    /**
     * 1_1 创建一个直播
     */
    private void createLive() {
        createIMChatRoom();
    }

    /**
     * 1_2创建一个IM聊天室
     */
    protected void createIMChatRoom() {
        createIMChatRoom("" + MySelfInfo.getInstance().getMyRoomNum(), new TIMValueCallBack<String>() {
            @Override
            public void onError(int i, String s) {
                // 创建IM房间失败，提示失败原因，并关闭等待对话框
//                Toast.makeText(mContext, "create IM room fail " + s + " " + i, Toast.LENGTH_SHORT).show();
//                quiteLive();
                joinLive(CurLiveInfo.getInstance().getRoomNum());
            }

            @Override
            public void onSuccess(String s) {
                //创建AV房间
                createAVRoom(MySelfInfo.getInstance().getMyRoomNum());
            }
        });

    }

    @Override
    protected void onGetGroupMembers(List<TIMUserProfile> groupMemberInfoList) {
        mStepInOutView.onGetGroupMembersList(groupMemberInfoList);
    }

    /**
     * 1_3创建一个AV房间
     */
    private void createAVRoom(int roomNum) {
        SxbLog.standardEnterRoomLog(TAG, "create av room", "", "room id " + MySelfInfo.getInstance().getMyRoomNum());
        EnterAVRoom(roomNum);
    }

    /**
     * 2_1加入一个房间
     */
    private void joinLive(int roomNum) {
        joinIMChatRoom(roomNum);
    }

    /**
     * 2_2加入一个聊天室
     */
    protected void joinIMChatRoom(final int chatRoomId) {
        SxbLog.standardEnterRoomLog(TAG, "join im chat room", "", "room id " + chatRoomId);
        joinIMChatRoom("" + chatRoomId, new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                SxbLog.standardEnterRoomLog(TAG, "join im chat room", "" + LogConstants.STATUS.FAILED, "code:" + i + " msg:" + s);
//                Toast.makeText(mContext, "join IM room fail " + s + " " + i, Toast.LENGTH_SHORT).show();
                quiteLive();
            }

            @Override
            public void onSuccess() {
                SxbLog.standardEnterRoomLog(TAG, "join im chat room", "" + LogConstants.STATUS.SUCCEED, "room id " + chatRoomId);
                joinAVRoom(CurLiveInfo.getInstance().getRoomNum());
            }
        });

    }

    /**
     * 2_2加入一个AV房间
     */
    private void joinAVRoom(int avRoomNum) {
        SxbLog.standardEnterRoomLog(TAG, "join av room", "", "AV room id " + avRoomNum);
        EnterAVRoom(avRoomNum);
//        }
    }


    /**
     * 退出房间
     */
    public void quiteLive() {

        //退出AV房间
        quiteAVRoom();
        //退出IM房间
        quiteIMChatRoom();

    }

    @Override
    public void onDestroy() {
        quiteLive();
        mStepInOutView = null;
        mContext = null;
    }

    /**
     * 退出一个AV房间
     */
    protected boolean quiteAVRoom() {
        if (isInAVRoom) {
            return super.quiteAVRoom();
        } else {
            quiteIMChatRoom();
            CurLiveInfo.getInstance().setCurrentRequestCount(0);
            unInitAudioService();
            //通知结束
//            notifyServerLiveEnd();
            mStepInOutView.quiteRoomComplete(MySelfInfo.getInstance().getIdStatus(), true, false);
            return true;
        }
    }

    /**
     * 退出IM房间
     */
    private void quiteIMChatRoom() {
        quiteIMChatRoom(false,"" + CurLiveInfo.getInstance().getRoomNum());
    }


    /**
     * 进入AV房间
     *
     * @param roomNum
     */
    private void EnterAVRoom(int roomNum) {
        SxbLog.i(TAG, "createlive joinLiveRoom enterAVRoom " + roomNum);
        byte[] authBuffer = null;//权限位加密串；TODO：请业务侧填上自己的加密串

        AVRoomMulti.EnterParam.Builder enterRoomBuilder = new AVRoomMulti.EnterParam.Builder(roomNum);
        /*if (MySelfInfo.getInstance().getIdStatus() == Constants.HOST) {
            enterRoomBuilder.auth(Constants.HOST_AUTH, authBuffer)
                    .avControlRole(Constants.HOST_ROLE)
                    .autoCreateRoom(true)
                    .isEnableMic(true)
                    .isEnableSpeaker(true);
        } else {*/
            enterRoomBuilder.auth(Constants.NORMAL_MEMBER_AUTH, authBuffer)
                    .avControlRole(Constants.NORMAL_MEMBER_ROLE)
                    .autoCreateRoom(true)
                    .isEnableSpeaker(true);
//        }
        enterRoomBuilder.audioCategory(Constants.AUDIO_VOICE_CHAT_MODE)
                .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO)
                .isDegreeFixed(true);
        enterAVRoom(mRoomDelegate, enterRoomBuilder.build());
    }

}
