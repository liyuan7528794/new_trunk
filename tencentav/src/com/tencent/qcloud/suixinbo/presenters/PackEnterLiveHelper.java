package com.tencent.qcloud.suixinbo.presenters;

import android.content.Context;

import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.presenters.viewinface.EnterQuiteRoomView;
import com.tencent.qcloud.suixinbo.utils.Constants;
import com.tencent.qcloud.suixinbo.utils.SxbLog;

/**
 * 背包的进出房间的辅助类
 * Created by ldkxingzhe on 2016/10/13.
 */
public class PackEnterLiveHelper extends EnterLiveHelper{
    @SuppressWarnings("unused")
    private static final String TAG = "PackEnterLiveHelper";

    public PackEnterLiveHelper(Context context, EnterQuiteRoomView view, LiveHelper liveHelper) {
        super(context, view, liveHelper);
    }

/*    @Override
    protected void EnterAVRoom(int roomNum) {
        SxbLog.i(TAG, "createlive joinLiveRoom enterAVRoom " + roomNum);
        byte[] authBuffer = null;//权限位加密串；TODO：请业务侧填上自己的加密串

        AVRoomMulti.EnterParam.Builder enterRoomBuilder = new AVRoomMulti.EnterParam.Builder(roomNum);
        enterRoomBuilder.auth(Constants.NORMAL_MEMBER_AUTH, authBuffer)
                .isEnableSpeaker(true);
        if(MySelfInfo.getInstance().getIdStatus() == Constants.HOST){
            enterRoomBuilder.autoCreateRoom(true)
                    .avControlRole(Constants.HOST_ROLE);
        }else{
            enterRoomBuilder.autoCreateRoom(false)
                    .avControlRole(Constants.NORMAL_MEMBER_ROLE);
        }
        enterRoomBuilder.audioCategory(Constants.AUDIO_VOICE_CHAT_MODE)
                .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO)
                .isDegreeFixed(true);
        enterAVRoom(mRoomDelegate, enterRoomBuilder.build());
    }*/
}
