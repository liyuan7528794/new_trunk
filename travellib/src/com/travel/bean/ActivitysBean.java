package com.travel.bean;

import com.travel.lib.utils.JsonUtil;

import org.json.JSONObject;

/**
 * Created by Administrator on 2016/11/23.
 */

public class ActivitysBean extends NotifyBean{
    private String subhead;
    private String content;
    private String startTime;
    private String endTime;

    public String getSubhead() {
        return subhead;
    }

    public void setSubhead(String subhead) {
        this.subhead = subhead;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public ActivitysBean getActivityBean(JSONObject json){
        getNotifyBean(json);
        setSubhead(JsonUtil.getJson(json, "subhead"));
        setContent(JsonUtil.getJson(json, "content"));
        setStartTime(JsonUtil.getJson(json, "startTime"));
        setEndTime(JsonUtil.getJson(json, "endTime"));
        return this;
    }
}
