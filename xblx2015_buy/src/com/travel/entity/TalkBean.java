package com.travel.entity;

import com.travel.communication.dao.UserData;

import java.io.Serializable;

/**
 * 说说列表的bean
 * Created by wyp on 2018/5/10.
 */

public class TalkBean implements Serializable{
    private String id;
    private String imgUrl;
    private String content;
    private String location;
    private int status;
    private String time;
    private int praiseNum;
    private int praiseType;
    private int commentNum;
    private UserData user;

    private String videoUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getPraiseNum() {
        return praiseNum;
    }

    public int getPraiseType() {
        return praiseType;
    }

    public void setPraiseType(int praiseType) {
        this.praiseType = praiseType;
    }

    public void setPraiseNum(int praiseNum) {
        this.praiseNum = praiseNum;
    }

    public int getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(int commentNum) {
        this.commentNum = commentNum;
    }

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
