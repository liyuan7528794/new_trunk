package com.tencent.qcloud.suixinbo.interfaces;

/**
 * AVSDK UI控制类应该事先的功能
 * Created by ldkxingzhe on 2016/8/8.
 */
public interface AVUIControlInterface {
    /**
     * 图像是否镜像, (主要用于前置摄像头的处理)
     * @param isMirror   true --- 是前置摄像头
     * @param identifier id ----- 用户id
     */
    void setMirror(boolean isMirror, String identifier);

/*    *//**
     * 远端有视频
     * @param identifier        id-- 用户Id
     * @param videoSrcType      视频类型
     * @param isRemoteHasVideo  是否是远端的视频
     * @param forceToBigView    是否强制大屏
     * @param isPC               是否是Pc端的
     *//*
    void setRemoteHasVideo(String identifier, int videoSrcType, boolean isRemoteHasVideo, boolean forceToBigView, boolean isPC);*/

    /**
     * onResume()
     */
    void onResume();

    /* onPause */
    void onPause();

    /*  onDestroy */
    void onDestroy();

    /**
     * 本地是否有视频
     * @param isLocalHasVideo  true 本地有视频(打开了摄像头)
     * @param forceToBigView   true -- 强制大屏
     * @param identifier        id -- 用户Id
     * @return  true -- 一切正常, false -- 失败
     */
    boolean setLocalHasVideo(boolean isLocalHasVideo, boolean forceToBigView, String identifier);

    /**
     * 设置远端视频(也就是小窗口视频)
     * @param isRemoteHasVideo     远端是否有视频
     * @param remoteIdentifier     远端视频所有者的用户Id
     * @param videoSrcType         视频源类型: @see AVView
     * @return 返回glView的index
     */
    int setHasRemoteVideo(boolean isRemoteHasVideo, String remoteIdentifier, int videoSrcType);

    /**
     * 设置自己的id
     * @param key    自身的用户Id
     */
    void setSelfId(String key);

    /**
     * 设置旋转
     * @param rotation 单位: 度数制
     */
    void setRotation(int rotation);

    /**
     * 获取一个闲置的视频位置
     * @param start 开始轮训的位置
     * @return  返回选择的视频view的位置
     */
    int getIdleViewIndex(int start);

    /**
     * 关闭小窗口
     * @param identifier  关闭者的用户id
     */
    boolean closeMemberVideoView(String identifier);

    /**
     * 根据用户Id与视频源类型获取视频view的index
     */
    int getViewIndexById(String identifier, int videoSrcType);
}
