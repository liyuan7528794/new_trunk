package com.tencent.qcloud.suixinbo.avcontrollers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.tencent.av.opengl.GraphicRendererMgr;
import com.tencent.av.opengl.ui.GLRootView;
import com.tencent.av.opengl.ui.GLView;
import com.tencent.av.opengl.ui.GLViewGroup;
import com.tencent.av.opengl.utils.Utils;
import com.tencent.av.sdk.AVVideoCtrl;
import com.tencent.av.sdk.AVView;
import com.tencent.av.utils.QLog;
import com.tencent.qcloud.suixinbo.R;
import com.tencent.qcloud.suixinbo.interfaces.AVUIControlInterface;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.utils.Constants;
import com.tencent.qcloud.suixinbo.utils.SxbLog;

import java.util.HashMap;
import java.util.List;

/**
 * AVSDK UI控制类 抽象类
 * Created by ldkxingzhe on 2016/8/8.
 */
public abstract class AbstractAVUIControl extends GLViewGroup implements AVUIControlInterface{
    @SuppressWarnings("unused")
    private static final String TAG = "AbstractAVUIControl";

    boolean mIsLocalHasVideo = false;// 自己是否有视频画面

    protected Context mContext = null;
    protected GraphicRendererMgr mGraphicRenderMgr = null;

    private SurfaceView mSurfaceView = null;
    protected QavsdkControl qavsdk;
    protected HashMap<Integer, String> id_view = new HashMap<Integer, String>();

    private boolean mCameraSurfaceCreated = false;

    View mRootView = null;
    GLRootView mGlRootView = null;
    GLVideoView mGlVideoView[] = null;


    int mClickTimes = 0;
    int mRotation = 0;
    int mCacheRotation = 0;

    protected int localViewIndex = -1;
    protected int remoteViewIndex = -1;
    protected String mRemoteIdentifier = "";

    private SurfaceHolder.Callback mSurfaceHolderListener = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mCameraSurfaceCreated = true;
            if (qavsdk.getRoom() != null) {
                qavsdk.getAVContext().setRenderMgrAndHolder(mGraphicRenderMgr, holder);
            }
            mContext.sendBroadcast(new Intent(Constants.ACTION_SURFACE_CREATED));
            SxbLog.e(TAG, " surfaceCreated");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (holder.getSurface() == null) {
                return;
            }
            holder.setFixedSize(width, height);
            SxbLog.e(TAG, "memoryLeak surfaceChanged");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            SxbLog.e(TAG, "memoryLeak surfaceDestroyed");
        }
    };

    public AbstractAVUIControl(Context context, View rootView){
        mContext = context;
        mRootView = rootView;
        mGraphicRenderMgr = GraphicRendererMgr.getInstance();
        qavsdk = QavsdkControl.getInstance();
        initQQGlView();
        initCameraPreview();
        id_view.clear();
    }

    public void showGlView() {
        if (mGlRootView != null) {
            mGlRootView.setVisibility(View.VISIBLE);
        }
    }

    public void hideGlView() {
        if (mGlRootView != null) {
            mGlRootView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setMirror(boolean isMirror, String identifier) {
        GLVideoView view = null;
        int index = getViewIndexById(identifier, AVView.VIDEO_SRC_TYPE_CAMERA);
        if (index >= 0) {
            view = mGlVideoView[index];
            view.setMirror(isMirror);
        }else{
            SxbLog.e(TAG, "setMirror->fail index: "+index);
        }
    }

    @Override
    public void onResume() {
        if (mGlRootView != null) {
            mGlRootView.onResume();
        }

//        setRotation(mCacheRotation);
    }

    @Override
    public void onPause() {
        if (mGlRootView != null) {
            mGlRootView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        SxbLog.w(TAG, " AVUIControl onDestroy");
        unInitCameraPreview();
        mContext = null;
        mRootView = null;

        removeAllView();
        for (int i = 0; i < mGlVideoView.length; i++) {
            mGlVideoView[i].flush();
            mGlVideoView[i].clearRender();
            mGlVideoView[i] = null;
        }
        mGlRootView.setOnTouchListener(null);
        mGlRootView.setContentPane(null);

        mGraphicRenderMgr = null;

        mGlRootView = null;
        mGlVideoView = null;
    }

    protected void closeGLVideoView(int index){
        GLVideoView view = mGlVideoView[index];
        String id = view.getIdentifier();
        if(!TextUtils.isEmpty(id)){
            QavsdkControl.getInstance().removeRemoteVideoMembers(id);
        }
        view.setVisibility(GLView.INVISIBLE);
        view.setNeedRenderVideo(true);
        view.enableLoading(false);
        view.setIsPC(false);
        view.clearRender();
    }

    @Override
    public void setSelfId(String key) {
        if (mGraphicRenderMgr != null) {
            mGraphicRenderMgr.setSelfId(key + "_" + AVView.VIDEO_SRC_TYPE_CAMERA);
        }
    }

    @Override
    public boolean setLocalHasVideo(boolean isLocalHasVideo, boolean forceToBigView, String identifier) {
        if (checkContextAndVersion()) return false;

        if (isLocalHasVideo) {// 打开摄像头
            GLVideoView view = null;
            int index = getViewIndexById(identifier, AVView.VIDEO_SRC_TYPE_CAMERA);
            if (index < 0) {
                index = getIdleViewIndex(0);
                if (index >= 0) {
                    view = mGlVideoView[index];
                    id_view.put(index, MySelfInfo.getInstance().getId());
                    view.setRender(identifier, AVView.VIDEO_SRC_TYPE_CAMERA);
                    localViewIndex = index;
                }
            } else {
                view = mGlVideoView[index];
            }
            if (view != null) {
                view.setIsPC(false);
                view.enableLoading(false);
                // if (isFrontCamera()) {
                // view.setMirror(true);
                // } else {
                // view.setMirror(false);
                // }
                view.setVisibility(GLView.VISIBLE);
            }
            if (forceToBigView && index > 0) {
                switchVideo(0, index);
            }
        } else {// 关闭摄像头
            int index = getViewIndexById(identifier, AVView.VIDEO_SRC_TYPE_CAMERA);
            if (index >= 0) {
                closeVideoView(index);
                localViewIndex = -1;
            }
        }
        mIsLocalHasVideo = isLocalHasVideo;

        return true;
    }

    protected boolean checkContextAndVersion() {
        if (mContext == null)
            return true;

        if (Utils.getGLVersion(mContext) == 1) {
            return true;
        }
        return false;
    }

    /* 设置背景 */
    public void setBackground(String identifier, int videoSrcType, Bitmap bitmap, boolean needRenderVideo) {
        int index = getViewIndexById(identifier, videoSrcType);
        if (index < 0) {
            index = getIdleViewIndex(0);
            if (index >= 0) {
                GLVideoView view = mGlVideoView[index];
                view.setVisibility(GLView.VISIBLE);
                view.setRender(identifier, videoSrcType);
            }
        }
        if (index >= 0) {
            GLVideoView view = mGlVideoView[index];
            view.setBackground(bitmap);
            view.setNeedRenderVideo(needRenderVideo);
            if (!needRenderVideo) {
                view.enableLoading(false);
            }
        }
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "setBackground identifier: " + identifier + ", videoSrcType: " + videoSrcType + ", index: " + index + ", needRenderVideo: " + needRenderVideo);
        }
    }

    /**
     * TODO: 小心
     * @return
     */
    boolean isLocalFront() {
        boolean isLocalFront = true;
        String selfIdentifier = "";//TODO 没赋值？
        GLVideoView view = mGlVideoView[0];
        if (view.getVisibility() == GLView.VISIBLE && selfIdentifier.equals(view.getIdentifier())) {
            isLocalFront = false;
        }
        return isLocalFront;
    }

    protected int getVisibleViewCount() {
        int count = 0;
        for (int i = 0; i < mGlVideoView.length; i++) {
            GLVideoView view = mGlVideoView[i];
            if (view.getVisibility() == GLView.VISIBLE && null != view.getIdentifier()) {
                count++;
            }
        }
        return count;
    }

    public int getIdleViewIndex(int start) {
        int index = -1;
        for (int i = start; i < mGlVideoView.length; i++) {
            GLVideoView view = mGlVideoView[i];
            if (null == view.getIdentifier() || view.getVisibility() == GLView.INVISIBLE) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int getViewIndexById(String identifier, int videoSrcType) {
        int index = -1;
        if (null == identifier) {
            SxbLog.e(TAG, "getViewIndexById->id is empty!");
            return index;
        }
        for (int i = 0; i < mGlVideoView.length; i++) {
            GLVideoView view = mGlVideoView[i];
            if ((identifier.equals(view.getIdentifier()) && view.getVideoSrcType() == videoSrcType) && view.getVisibility() == GLView.VISIBLE) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 关闭小窗口
     */
    public boolean closeMemberVideoView(String identifier) {
        SxbLog.i(TAG, "closeMemberVideoView " + identifier);
        if (id_view.containsValue(identifier)) {
            int index = getViewIndexById(identifier, AVView.VIDEO_SRC_TYPE_CAMERA);
            if (index == -1) return false;
            if (index == 0) {//当前关闭页面在背景页面
                //先交换,再关闭
                int hostIndex = getViewIndexById(CurLiveInfo.getInstance().getHostID(), AVView.VIDEO_SRC_TYPE_CAMERA);
                switchVideo(index, hostIndex);
                mContext.sendBroadcast(new Intent(
                        Constants.ACTION_SWITCH_VIDEO).putExtra(
                        Constants.EXTRA_IDENTIFIER, CurLiveInfo.getInstance().getHostID()));
                closeVideoView(hostIndex);
            } else {
                //不在主界面上
                closeVideoView(index);
            }
            return true;
        }
        return false;
    }

    /**
     * @return  获取视频块对应的Id, 如果视频不可见则对应Id为""
     * */
    public String[] getIdentifiers(){
        String[] result = new String[Constants.VIDEO_VIEW_MAX];
        for(int i = 0; i < getVideoViewCount(); i++){
            result[i] = mGlVideoView[i].getVisibility() == GLView.VISIBLE ?
                    mGlVideoView[i].getIdentifier() : "";
        }
        return result;
    }

    /*
    * 获取屏幕被占用数
    * */
    public int getUseNum(){
        int result = 0;
        for(GLVideoView glVideoView : mGlVideoView){
            if(!TextUtils.isEmpty(glVideoView.getIdentifier())
                    && glVideoView.getVisibility() == GLView.VISIBLE){
                result++;
            }
        }
        return result;
    }

    public int getIdelNum(){
        return getVideoViewCount() - getUseNum();
    }

    /* 返回position 对应位置的对应视频的所有者的id  */
    public String getIdentifierByIndex(int position){
        return mGlVideoView[position].getIdentifier();
    }

    public void setRotation(int rotation) {
        if (mContext == null) {
            return;
        }

        if ((rotation % 90) != (mRotation % 90)) {
            mClickTimes = 0;
        }

        mRotation = rotation;
        mCacheRotation = rotation;

        // layoutVideoView(true);
        if (qavsdk != null) {
            AVVideoCtrl avVideoCtrl = qavsdk.getAVContext().getVideoCtrl();
            avVideoCtrl.setRotation(rotation);
        }
        switch (rotation) {
            case 0:
                for (int i = 0; i < getChildCount(); i++) {
                    GLView view = getChild(i);
                    if (view != null)
                        view.setRotation(0);
                }
                break;
            case 90:
                for (int i = 0; i < getChildCount(); i++) {
                    GLView view = getChild(i);
                    if (view != null)
                        view.setRotation(90);
                }
                break;
            case 180:
                for (int i = 0; i < getChildCount(); i++) {
                    GLView view = getChild(i);
                    if (view != null)
                        view.setRotation(180);
                }
                break;
            case 270:
                for (int i = 0; i < getChildCount(); i++) {
                    GLView view = getChild(i);
                    if (view != null)
                        view.setRotation(270);
                }
                break;
            default:
                break;
        }
    }

    protected void unInitCameraPreview() {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        try {
            windowManager.removeViewImmediate(mSurfaceView);
            mSurfaceView.destroyDrawingCache();
            mSurfaceView = null;
        } catch (Exception e) {
            if (QLog.isColorLevel()) {
                QLog.e(TAG, QLog.CLR, "remove camera view fail.", e);
            }
        }
    }

    public String getQualityTips() {
        String tipsAudio = "";
        String tipsVideo = "";
        String tipsRoom = "";

        if (qavsdk != null) {
            tipsAudio = qavsdk.getAudioQualityTips();
            tipsVideo = qavsdk.getVideoQualityTips();

            if (qavsdk.getRoom() != null) {
                tipsRoom = qavsdk.getRoom().getQualityTips();
            }
        }

        String tipsAll = "";

        if (tipsRoom != null && tipsRoom.length() > 0) {
            tipsAll += tipsRoom + "\n";
        }

        if (tipsAudio != null && tipsAudio.length() > 0) {
            tipsAll += tipsAudio + "\n";
        }

        if (tipsVideo != null && tipsVideo.length() > 0) {
            tipsAll += tipsVideo;
        }

        return tipsAll;
    }

    /* 返回远端的视频所有者Id */
    public String getRemoteIdentifier(){
        return mRemoteIdentifier;
    }

    protected void closeVideoView(int index) {
        List<String> idList = QavsdkControl.getInstance().getRemoteVideoIds();
        GLVideoView videoView = mGlVideoView[index];
        if(!TextUtils.isEmpty(videoView.getIdentifier())){
            idList.remove(videoView.getIdentifier());
        }
    }
    protected void switchVideo(int i, int index){};

    /**
     * 获取VideoView的数目
     */
    protected int getVideoViewCount(){
        return Constants.VIDEO_VIEW_MAX;
    }

    private void initQQGlView() {
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "initQQGlView");
        }
        if(mRootView instanceof GLRootView){
            mGlRootView = (GLRootView) mRootView;
        }else{
            mGlRootView = (GLRootView) mRootView.findViewById(R.id.av_video_glview);
        }
        mGlRootView.setZOrderMediaOverlay(true);
        mGlVideoView = new GLVideoView[getVideoViewCount()];
        for (int i = 0; i <= getVideoViewCount() - 1; i++) {
            mGlVideoView[i] = new GLVideoView(mContext.getApplicationContext(), mGraphicRenderMgr);
            mGlVideoView[i].setVisibility(GLView.INVISIBLE);
            addView(mGlVideoView[i]);
        }
        mGlRootView.setContentPane(this);
    }

    /* 初始化CameraPreview, 就是创建一个SurfaceView, 获取一个Holder */
    private void initCameraPreview() {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.width = 1;
        layoutParams.height = 1;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        // layoutParams.flags |= LayoutParams.FLAG_NOT_TOUCHABLE;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.windowAnimations = 0;// android.R.style.Animation_Toast;
//        layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        //layoutParams.setTitle("Toast");
        try {
            mSurfaceView = new SurfaceView(mContext);
            SurfaceHolder holder = mSurfaceView.getHolder();
            holder.addCallback(mSurfaceHolderListener);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 3.0以下必须在初始化时调用，否则不能启动预览
            mSurfaceView.setZOrderMediaOverlay(true);
            windowManager.addView(mSurfaceView, layoutParams);
        } catch (IllegalStateException e) {
            windowManager.updateViewLayout(mSurfaceView, layoutParams);
            if (QLog.isColorLevel()) {
                QLog.d(TAG, QLog.CLR, "add camera surface view fail: IllegalStateException." + e);
            }
        } catch (Exception e) {
            if (QLog.isColorLevel()) {
                QLog.d(TAG, QLog.CLR, "add camera surface view fail." + e);
            }
        }
        SxbLog.i(TAG, "initCameraPreview");
    }
}
