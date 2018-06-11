package com.tencent.qcloud.suixinbo.avcontrollers;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.tencent.av.opengl.ui.GLView;
import com.tencent.av.sdk.AVView;

/**
 * 背包的UIControl
 * Created by ldkxingzhe on 2016/10/13.
 */
public class PackAVUIControl extends AbstractAVUIControl {
    @SuppressWarnings("unused")
    private static final String TAG = "PackAVUIControl";
    private GLVideoView mVideoView;

    public PackAVUIControl(Context context, View rootView) {
        super(context, rootView);
        mVideoView = mGlVideoView[1];
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        mVideoView.layout(0, 0, getWidth(), getHeight());
    }

    @Override
    public int setHasRemoteVideo(boolean isRemoteHasVideo, String remoteIdentifier, int videoSrcType) {
        if(isRemoteHasVideo){
            showVideoView(remoteIdentifier);
        }
        return 0;
    }

    @Override
    public int getIdelNum() {
        if(TextUtils.isEmpty(mVideoView.getIdentifier())){
            return 1;
        }

        return 0;
    }

    private void showVideoView(String identifier){
        mVideoView.setRender(identifier, AVView.VIDEO_SRC_TYPE_CAMERA);
        mVideoView.setIsPC(false);
        mVideoView.setMirror(false);
        mVideoView.enableLoading(true);
        mVideoView.setVisibility(GLView.VISIBLE);
        id_view.put(1, identifier);
    }

    @Override
    public boolean closeMemberVideoView(String identifier) {
        String id = mVideoView.getIdentifier();
        if(!TextUtils.isEmpty(id) && id.equals(identifier)){
            closeGLVideoView(1);
            return true;
        }

        return false;
    }

    @Override
    public boolean setLocalHasVideo(boolean isLocalHasVideo, boolean forceToBigView, String identifier) {
        if(checkContextAndVersion()) return false;
        if(isLocalHasVideo){
            showVideoView(identifier);
        }
        return true;
    }
}
