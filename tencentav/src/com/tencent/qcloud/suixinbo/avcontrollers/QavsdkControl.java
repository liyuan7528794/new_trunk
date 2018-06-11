package com.tencent.qcloud.suixinbo.avcontrollers;

import android.content.Context;

import com.tencent.av.sdk.AVAudioCtrl;
import com.tencent.av.sdk.AVContext;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.av.sdk.AVVideoCtrl;
import com.tencent.qcloud.suixinbo.utils.Constants;
import com.tencent.qcloud.suixinbo.utils.SxbLog;

import java.util.ArrayList;

/**
 * AVSDK 总控制器类
 */
public class QavsdkControl {
    private static final String TAG = "QavsdkControl";
    private AVContextControl mAVContextControl = null;
    /* 持有私有静态实例，防止被引用，此处赋值为null，目的是实现延迟加载 */
    private static QavsdkControl instance = null;
    private static Context mContext;

    public static QavsdkControl getInstance() {
        if (instance == null) {
            instance = new QavsdkControl(mContext);
        }
        return instance;
    }

    public static QavsdkControl instance(){
        return instance;
    }


    public ArrayList<String> getRemoteVideoIds() {
        return remoteVideoIds;
    }

    private ArrayList<String> remoteVideoIds = new ArrayList<String>();


    public static void initQavsdk(Context context) {
        mContext = context;
    }


    private QavsdkControl(Context context) {
        mAVContextControl = new AVContextControl(context);
        SxbLog.d(TAG, "WL_DEBUG QavsdkControl");
    }


    public void addRemoteVideoMembers(String id) {
        remoteVideoIds.add(id);
    }

    public void removeRemoteVideoMembers(String id) {
        if (remoteVideoIds.contains(id))
            remoteVideoIds.remove(id);
    }

    public void clearVideoMembers() {
        remoteVideoIds.clear();
    }


    /**
     * 启动SDK系统
     */
    public int startContext() {
        if (mAVContextControl == null)
            return Constants.DEMO_ERROR_NULL_POINTER;
        return mAVContextControl.startContext();
    }

    /**
     * 设置AVSDK参数
     *
     * @param appid
     * @param accountype
     * @param identifier
     * @param usersig
     */
    public void setAvConfig(int appid, String accountype, String identifier, String usersig) {
        if (mAVContextControl == null)
            return;
        mAVContextControl.setAVConfig(appid, accountype, identifier, usersig);
    }


    /**
     * 关闭SDK系统
     */
    public void stopContext() {
        if (mAVContextControl != null) {
            mAVContextControl.stopContext();
        }
    }

    public boolean hasAVContext() {
        if (mAVContextControl == null)
            return false;
        return mAVContextControl.hasAVContext();
    }

    public String getSelfIdentifier() {
        if (mAVContextControl == null)
            return null;
        return mAVContextControl.getSelfIdentifier();
    }

    public AVRoomMulti getRoom() {
        AVContext avContext = getAVContext();
        return avContext != null ? avContext.getRoom() : null;
    }

    public boolean getIsInStartContext() {
        if (mAVContextControl == null)
            return false;

        return mAVContextControl.getIsInStartContext();
    }

    public boolean getIsInStopContext() {
        if (mAVContextControl == null)
            return false;

        return mAVContextControl.getIsInStopContext();
    }

    public AVContext getAVContext() {
        if (mAVContextControl == null)
            return null;
        return mAVContextControl.getAVContext();
    }

    public String getAudioQualityTips() {
        AVAudioCtrl avAudioCtrl;
        if (QavsdkControl.getInstance() != null && QavsdkControl.getInstance().getAVContext() != null) {
            avAudioCtrl = getAVContext().getAudioCtrl();
            return avAudioCtrl.getQualityTips();
        }

        return "";
    }

    public String getVideoQualityTips() {
        if (QavsdkControl.getInstance() != null) {
            AVVideoCtrl avVideoCtrl = QavsdkControl.getInstance().getAVContext().getVideoCtrl();
            return avVideoCtrl.getQualityTips();
        }
        return "";
    }


    public String getQualityTips() {
        QavsdkControl qavsdk = QavsdkControl.getInstance();
        String audioQos = "";
        String videoQos = "";
        String roomQos = "";

        if (qavsdk != null) {
            audioQos = getAudioQualityTips();

            videoQos = getVideoQualityTips();

            if (qavsdk.getRoom() != null) {
                roomQos = qavsdk.getRoom().getQualityTips();
            }
        }

        if (audioQos != null && videoQos != null && roomQos != null) {
            return audioQos + videoQos + roomQos;
        } else {
            return "";
        }

    }


}