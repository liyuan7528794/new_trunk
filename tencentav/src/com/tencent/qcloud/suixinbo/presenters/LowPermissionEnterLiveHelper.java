package com.tencent.qcloud.suixinbo.presenters;

import android.content.Context;

import com.tencent.qcloud.suixinbo.presenters.viewinface.EnterQuiteRoomView;
import com.tencent.qcloud.suixinbo.utils.SxbLog;

/**
 * 用于低权限用户的EnterLiveHelper
 * Created by ldkxingzhe on 2016/11/3.
 */
public class LowPermissionEnterLiveHelper extends EnterLiveHelper{
    @SuppressWarnings("unused")
    private static final String TAG = "LowPermissionEnterLiveHelper";

    public LowPermissionEnterLiveHelper(Context context, EnterQuiteRoomView view, LiveHelper liveHelper) {
        super(context, view, liveHelper);
    }

    @Override
    protected void createAVRoom(int roomNum) {
        // ignore
        joinAVRoom(roomNum);
    }

    @Override
    protected void joinAVRoom(int avRoomNum) {
        isInAVRoom = true;
        // ignore
        if(mRoomDelegate != null){
            mRoomDelegate.onEnterRoomComplete(0);
        }
    }

    @Override
    protected boolean quiteAVRoom() {
        SxbLog.v(TAG, "quiteAVRoom");
        isInAVRoom = false;
        if(mRoomDelegate != null){
            mRoomDelegate.onExitRoomComplete();
        }
        return true;
    }



    @Override
    public boolean isInAVRoom() {
        return super.isInAVRoom();
    }
}
