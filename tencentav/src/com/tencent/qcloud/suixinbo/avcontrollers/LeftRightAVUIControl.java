package com.tencent.qcloud.suixinbo.avcontrollers;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;

import com.tencent.av.opengl.ui.GLView;
import com.tencent.av.sdk.AVView;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;

/**
 * 左右两个视频
 *
 * 因为只有两个视频, 不涉及多视频互动问题, 再次做假设如下:
 * - 两个视频view mGlVideoView[0] mGlVideoView[1]
 * - 左边的视频mGlVideoView[0], 现有逻辑下为买家视频
 * - 右边视频mGlVideoView[1], 现有逻辑下的任何非买家视频.
 * Created by ldkxingzhe on 2016/8/8.
 */
public class LeftRightAVUIControl extends AbstractAVUIControl{
    @SuppressWarnings("unused")
    private static final String TAG = "LeftRightAVUIControl";

    private String mSelfId;  // 自己的用户Id
    private String mLeftId;

    private boolean mIsRightVideoBusying = false;

    public LeftRightAVUIControl(Context context, View rootView) {
        super(context, rootView);
    }

    @Override
    protected int getVideoViewCount() {
        // 此Control有两个视频图
        return 2;
    }

    @Override
    public void setSelfId(String key) {
        super.setSelfId(key);
        mSelfId = key;
    }

    /* 设置左边视频的用户Id */
    public void setLeftId(String leftId) {
        this.mLeftId = leftId;
    }

    /* 返回是否是左边的视频 */
    private boolean isLeft(String identifier){
        return !TextUtils.isEmpty(identifier) && identifier.equals(mLeftId);
    }

    @Override
    public boolean setLocalHasVideo(boolean isLocalHasVideo, boolean forceToBigView, String identifier) {
        if (checkContextAndVersion()) return false;
        if (isLocalHasVideo) {// 打开摄像头
            GLVideoView view = null;
            int index = getViewIndexById(identifier, AVView.VIDEO_SRC_TYPE_CAMERA);
            if (index < 0) {
                index = isLeft(identifier) ? 0 : 1;
                view = mGlVideoView[index];
                id_view.put(index, MySelfInfo.getInstance().getId());
                view.setRender(identifier, AVView.VIDEO_SRC_TYPE_CAMERA);
                localViewIndex = index;
            } else {
                view = mGlVideoView[index];
            }
            if (view != null) {
                view.setIsPC(false);
                view.enableLoading(false);
                view.setVisibility(GLView.VISIBLE);
            }
            setIsRightVideoBusying(index, true);
        } else {// 关闭摄像头
            int index = getViewIndexById(identifier, AVView.VIDEO_SRC_TYPE_CAMERA);
            if (index >= 0) {
                closeVideoView(index);
                localViewIndex = -1;
            }
            setIsRightVideoBusying(index, false);
        }
        mIsLocalHasVideo = isLocalHasVideo;
        return true;
    }

    @Override
    protected void closeVideoView(int index) {
        closeGLVideoView(index);
        if(index == 1) mIsRightVideoBusying = false;
    }

    private void setIsRightVideoBusying(int index, boolean isHasVideo){
        if(index == 0) return;
        mIsRightVideoBusying = isHasVideo;
    }

    @Override
    public int setHasRemoteVideo(boolean isRemoteHasVideo, String remoteIdentifier, int videoSrcType) {
        if (isRemoteHasVideo) {// 远端是否有视频数据
            GLVideoView view = null;
            mRemoteIdentifier = remoteIdentifier;
            int index = getViewIndexById(remoteIdentifier, videoSrcType);

            //请求多路画面用这个测试
//			if (remoteViewIndex != -1 && !mRemoteIdentifier.equals("") && !mRemoteIdentifier.equals(remoteIdentifier)) {
//				closeVideoView(remoteViewIndex);
//			}


            //不存在分配一个空的
            if (index < 0) {
                index = isLeft(remoteIdentifier) ? 0 : 1;

                if(mIsRightVideoBusying && index == 1){
                    closeVideoView(1);
                }

                view = mGlVideoView[index];
                view.setRender(remoteIdentifier, videoSrcType);
                id_view.put(index, mRemoteIdentifier);//存index，对应的ID
                remoteViewIndex = index;
            } else {//存在用已有的
                view = mGlVideoView[index];
            }
            if (view != null) {
                view.setIsPC(false);
                view.setMirror(false);
                view.enableLoading(false);
                view.setVisibility(GLView.VISIBLE);
            }
            setIsRightVideoBusying(index, true);
            return index;
        } else {// 关闭摄像头
            int index = getViewIndexById(remoteIdentifier, videoSrcType);
            if (index >= 0) {
                closeVideoView(index);
                remoteViewIndex = -1;
            }
            setIsRightVideoBusying(index, false);
        }
        return -2;
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        if(mContext == null) return;

        int width = getWidth();
        int height = getHeight();

        int halfWidth = width / 2;
        mGlVideoView[0].layout(0, 0, halfWidth, height);
        mGlVideoView[0].setBackgroundColor(Color.BLACK);
        mGlVideoView[1].layout(halfWidth, 0, width, height);
        mGlVideoView[1].setBackgroundColor(Color.BLACK);
        invalidate();
    }
}
