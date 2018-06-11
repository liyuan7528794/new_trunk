package com.travel.video.bean;

/**
 * Created by Administrator on 2016/11/17.
 */

public class BarrageInfo {
    public final static int STATUS_NO = 0;
    public final static int STATUS_ALREADY = 1;
    public final static int NONE = -1;
    private String userId;
    private String content;
    private String nickName;
    private String userImg;
    private Long submitTime;
    private int showStatus = -1;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public Long getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Long submitTime) {
        this.submitTime = submitTime;
    }

    public int getShowStatus() {
        return showStatus;
    }

    public void setShowStatus(int showStatus) {
        this.showStatus = showStatus;
    }
}
