package com.travel.bean;

import com.travel.lib.utils.JsonUtil;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/11/23.
 */

public class NotifyBean implements Serializable{
    private String type;
    private String id;
    private String title;
    private String imgUrl;
    private String shareUrl;
    private String webUrl;
    private int status;// 1:该活动是正常的 其他：都不正常

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public NotifyBean getNotifyBean(JSONObject json){
        setId(JsonUtil.getJson(json, "id"));
        setTitle("".equals(JsonUtil.getJson(json, "name")) ? JsonUtil.getJson(json, "title") : JsonUtil.getJson(json, "name"));
        setImgUrl(JsonUtil.getJson(json, "imgUrl"));
        setWebUrl(JsonUtil.getJson(json, "staticHTML"));
        setShareUrl(JsonUtil.getJson(json,"staticHTML"));
        return this;
    }
}
