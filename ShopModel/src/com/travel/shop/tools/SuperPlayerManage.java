package com.travel.shop.tools;

import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * 类描述：获取唯一的视频控制器
 *
 * @author Super南仔
 * @time 2016-9-19
 */
public class SuperPlayerManage {
    public static SuperPlayerManage videoPlayViewManage;
    private WeakReference<Context> mContext;
    private SuperPlayer videoPlayView, voicePlayView;
    private SuperPlayer videoPlayViewCommit, voicePlayViewCommit;

    private SuperPlayerManage(WeakReference<Context> context) {
        this.mContext = context;
    }

    public static SuperPlayerManage getSuperManage(Context context) {
        if (videoPlayViewManage == null) {
            videoPlayViewManage = new SuperPlayerManage(new WeakReference(context));
        }
        return videoPlayViewManage;
    }

    public SuperPlayer initializeVideo(boolean isCommandVideo) {
        if (videoPlayView == null) {
            videoPlayView = new SuperPlayer(mContext.get(), 1, isCommandVideo);
        }
        return videoPlayView;
    }

    public SuperPlayer initializeVoice() {
        if (voicePlayView == null) {
            voicePlayView = new SuperPlayer(mContext.get(), 2, false);
        }
        return voicePlayView;
    }
    public SuperPlayer initializeVideoCommit() {
        if (videoPlayViewCommit == null) {
            videoPlayViewCommit = new SuperPlayer(mContext.get(), 1, false);
        }
        return videoPlayViewCommit;
    }

    public SuperPlayer initializeVoiceCommit() {
        if (voicePlayViewCommit == null) {
            voicePlayViewCommit = new SuperPlayer(mContext.get(), 2, false);
        }
        return voicePlayViewCommit;
    }

}
