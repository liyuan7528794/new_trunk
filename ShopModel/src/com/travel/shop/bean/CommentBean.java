package com.travel.shop.bean;

import com.travel.communication.dao.UserData;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2018/3/8.
 */

public class CommentBean implements Serializable{
    /*"id": 98,
            "content": "fdcds",
            "businessId": 48,
            "toname": 7,
            "pid": 1,
            "sendTime": 1520438400000,
            "userId": 6,
            "businessType": 1,
            "user": {
        "id": 6,
                "nickName": "测试账号6",
                "imgUrl": "http:\/\/img.honglelx.com\/2016\/5\/9\/1462765625625330.jpg",
                "mobile": "13900000006",
                "sex": 1,
                "myIntroduction": "",
                "userType": 1,
                "loginStatus": false,
                "followStatus": false,
                "place": "中国",
                "allowLiveType": 2
    },
            "toNameUser": {
        "id": 7,
                "nickName": "测试账号7",
                "imgUrl": "http:\/\/img.honglelx.com\/2016\/4\/29\/1461926764973374.jpg",
                "mobile": "13900000007",
                "sex": 1,
                "myIntroduction": "",
                "userType": 2,
                "loginStatus": false,
                "followStatus": false,
                "place": "中国",
                "allowLiveType": 2
        },
            "subComment": []
    }*/
    private int id;
    private String content;
    private int businessId;
    private int toname;
    private int pid;
    private int userId;
    private int businessType;
    private String sendTime;

    private UserData user;
    private UserData toNameUser;
    private List<CommentBean> subComment;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getBusinessId() {
        return businessId;
    }

    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }

    public int getToname() {
        return toname;
    }

    public void setToname(int toname) {
        this.toname = toname;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBusinessType() {
        return businessType;
    }

    public void setBusinessType(int businessType) {
        this.businessType = businessType;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    public UserData getToNameUser() {
        return toNameUser;
    }

    public void setToNameUser(UserData toNameUser) {
        this.toNameUser = toNameUser;
    }

    public void setSubComment(List<CommentBean> subComment) {
        this.subComment = subComment;
    }

    public List<CommentBean> getSubComment() {
        return subComment;
    }
}
