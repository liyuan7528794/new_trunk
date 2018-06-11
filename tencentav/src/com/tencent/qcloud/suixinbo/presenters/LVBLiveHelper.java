package com.tencent.qcloud.suixinbo.presenters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tencent.qcloud.suixinbo.R;
import com.tencent.qcloud.suixinbo.interfaces.LVBLiveInterface;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.travel.lib.utils.ImageUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * LVB直播功能
 * Created by Administrator on 2016/10/26.
 */

public class LVBLiveHelper{
    public final static int LVB_LIVE = 1;// 直播
    public final static int LVB_WATCH_LIVE = 2;// 观看直播
    private int type;
    private Context context;
    private boolean isDestroy = true;
    private TXLivePushConfig mLivePushConfig = null;
    private TXLivePusher mLivePusher = null;
    private TXCloudVideoView videoView = null ;

    private TXLivePlayConfig mPlayConfig = null;
    private TXLivePlayer mLivePlayer = null;
    private boolean isPause = false; //是否暂停
    private boolean isPushed = true; //是否已经推流

    private LVBLiveInterface mLvbLiveInterface;
    public LVBLiveHelper(Context context,LVBLiveInterface mLvbLiveInterface){
        this.context = context;
        this.mLvbLiveInterface = mLvbLiveInterface;
    }

    /**
     * 初始化控件参数
     * @param type 直播或播放器类型
     * @param videoView 显示view
     */
    public void initLVBLive(int type, final TXCloudVideoView videoView){
        if(!isDestroy) destroy();

        isDestroy = false;
        this.type = type;
        this.videoView = videoView;
        switch (type){
            case LVB_LIVE: // 直播
                mLivePusher = new TXLivePusher(context);
                mLivePushConfig = new TXLivePushConfig();
                mLivePusher.setConfig(mLivePushConfig);
                int customModeType = 0;
                mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_540_960);
                mLivePushConfig.setVideoFPS(20);
                mLivePushConfig.setVideoBitrate(1000);
                mLivePushConfig.setCustomModeType(customModeType);
                //设置主播暂时离开时的封面
                mLivePushConfig.setPauseImg(300,10);
                Bitmap bitmap = ImageUtils.decodeResourceToBitmap(context.getResources(), R.drawable.pause_publish);
                mLivePushConfig.setPauseImg(bitmap);
                mLivePushConfig.setTouchFocus(false);
                mLivePusher.setPushListener(mLvbLiveInterface);
//                mLivePusher.startCameraPreview(videoView);
                break;
            case LVB_WATCH_LIVE: // 观看直播
                mLivePlayer = new TXLivePlayer(context);
                mPlayConfig = new TXLivePlayConfig();
                mLivePlayer.setPlayerView(videoView);
                mLivePlayer.setPlayListener(mLvbLiveInterface);
                mLivePlayer.setConfig(mPlayConfig);
                mLivePlayer.setRenderRotation(0); // 转换方向
                mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
                mLivePlayer.enableHardwareDecode(true);// 硬件加速
                break;
        }
    }

    public void startLive() {
        mLivePusher.startCameraPreview(videoView);
    }

    /**
     * 是否正在直播
     */
    public boolean isPushing(){
        if(mLivePusher!=null)
            return mLivePusher.isPushing();
        return false;
    }

    public boolean isPause(){
        return isPause;
    }

    public void startVideo(String url){
        int result = mLivePlayer.startPlay(url,TXLivePlayer.PLAY_TYPE_LIVE_RTMP); // result返回值：0 success;  -1 empty url; -2 invalid url; -3 invalid playType;
        if (result != 0) {
            Toast.makeText(context, "播放失败！", Toast.LENGTH_SHORT).show();
        }
//        mLivePlayer.setLogLevel(TXLiveConstants.LOG_LEVEL_DEBUG);
    }

    public void startPush(String url) {
        mLivePusher.startPusher(url);
//        mLivePusher.setLogLevel(TXLiveConstants.LOG_LEVEL_DEBUG);
//        isPushed = true;
    }

    public void pause() {
        if (videoView != null) videoView.onPause();

        if (mLivePusher != null && isPushed) {
            mLivePusher.stopCameraPreview(false);
            mLivePusher.pausePusher();
        }

        // 播放器操作
        if (mLivePlayer != null) mLivePlayer.pause();

        isPause = true;
    }

    public void resume(){
        if(!isPause) return; // 不执行resume导致视频显示不出来

        if (videoView != null) videoView.onResume();

        if (mLivePusher != null && isPushed) {
            mLivePusher.resumePusher();
            mLivePusher.startCameraPreview(videoView);
        }

        if (mLivePlayer != null) mLivePlayer.resume();
        isPause = false;
    }

    public void stop(){
        if(mLivePusher != null){
            mLivePusher.stopCameraPreview(true);
            mLivePusher.stopScreenCapture();
            mLivePusher.stopPusher();
        }
//        isPushed = false;

        // 播放器停止
//        if(videoView != null) videoView.onStop();
        if(mLivePlayer != null) mLivePlayer.stopPlay(false);

    }

    public void destroy(){
        stop();

        if(mLivePushConfig != null) {
            mLivePushConfig.setPauseImg(null);
        }

        if(mLivePusher != null){
            mLivePusher.setPushListener(null);
        }

        if (mLivePlayer != null) {
            mLivePlayer.stopPlay(true);
            mLivePlayer.setPlayListener(null);
        }
        if (videoView != null) {
            videoView.onDestroy();
        }
        removeViewFromParent(videoView);

        mLivePushConfig = null;
        mLivePusher = null;
        mPlayConfig = null;
        mLivePlayer = null;
        videoView = null;
        isDestroy = true;
    }

    public void changeCamera(){
        if (mLivePusher!=null && mLivePusher.isPushing()) {
            mLivePusher.switchCamera();
        }
    }
    private boolean mFlashTurnOn = false;
    public boolean turnOnFlashLight(){
        if (mLivePusher==null) return false;
        mFlashTurnOn = !mFlashTurnOn;
        return mLivePusher.turnOnFlashLight(mFlashTurnOn);
    }

    private boolean isMetu = false;
    public boolean setMetu(){
        if (mLivePusher==null) return false;
        isMetu = !isMetu;
        mLivePusher.setMute(isMetu);
        return isMetu;
    }

    private int faceValue = 0;
    private int faceWhite = 0;
    public void setBeauty(int i, int i1){
        if(i != -1){
            faceValue = i;
        }
        if(i1 != -1){
            faceWhite = i;
        }
        mLivePusher.setBeautyFilter(faceValue, faceWhite);
    }

    private void removeViewFromParent(View view){
        if(view != null && view.getParent() != null){
            ((ViewGroup)view.getParent()).removeView(view);
        }
    }

    public SurfaceView getSurfaceView() {
        Class temp = videoView.getClass();    // 获取Class类的对象的方法之一

        try {
            Field[] fa = temp.getDeclaredFields();
            for (int i = 0; i < fa.length; i++) {
                if("mSWVideoView".equals(fa[i].getName())){
                    Field field = temp.getDeclaredField(fa[i].getName());    // 属性的值
                    field.setAccessible(true);    // Very Important
//                    f value = (f) field.get(videoView);
//                    value.onSurfaceCreated(null,null);
                    Class cl = fa[i].getType();    // 属性的类型
                    int md = fa[i].getModifiers();    // 属性的修饰域
                    System.out.println(Modifier.toString(md) + " " + cl + " : " + fa[i].getName());
//                    return value;
                }


                /*Class cl = fa[i].getType();    // 属性的类型

                int md = fa[i].getModifiers();    // 属性的修饰域

                Field field = temp.getDeclaredField(fa[i].getName());    // 属性的值
                field.setAccessible(true);    // Very Important
                f value = (f) field.get(videoView);

                if (value == null) {
                    System.out.println(Modifier.toString(md) + " " + cl + " : " + fa[i].getName());
                }
                else {
                    System.out.println(Modifier.toString(md) + " " + cl + " : " + fa[i].getName() + " = " + value.toString());
                }*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}