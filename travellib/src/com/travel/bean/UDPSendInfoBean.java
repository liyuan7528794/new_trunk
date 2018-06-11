package com.travel.bean;

import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/24.
 */

public class UDPSendInfoBean implements Serializable {

    private String msgHead;// 消息头
    private MsgBody msgBody;

    class MsgBody {
        private String userId;//用户id
        private String visitId;//访问连接id
        private String visitName;//访问连接名称
        private String visitURL;//访问连接url
        private String ip;//访问ip
        private String phoneModel;//访问手机型号
        private String beginTime;//访问开始时间
        private String endTime;//访问结束时间

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }

        public String getVisitId() {
            return visitId;
        }

        public void setVisitId(String visitId) {
            this.visitId = visitId;
        }

        public String getVisitName() {
            return visitName;
        }

        public void setVisitName(String visitName) {
            this.visitName = visitName;
        }

        public String getVisitURL() {
            return visitURL;
        }

        public void setVisitURL(String visitURL) {
            this.visitURL = visitURL;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getIp() {
            return ip;
        }

        public void setPhoneModel(String phoneModel) {
            this.phoneModel = phoneModel;
        }

        public String getPhoneModel() {
            return phoneModel;
        }

        public String getBeginTime() {
            return beginTime;
        }

        public void setBeginTime(String beginTime) {
            this.beginTime = beginTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }

    public void setMsgHead(String msgHead) {
        this.msgHead = msgHead;
    }

    public String getMsgHead() {
        return msgHead;
    }

    public MsgBody getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(MsgBody msgBody) {
        this.msgBody = msgBody;
    }

    public UDPSendInfoBean getData(String... data) {
        setMsgHead("log");
        MsgBody body = new MsgBody();
        body.setUserId(UserSharedPreference.isLogin() ? UserSharedPreference.getUserId() : "-1");
        body.setVisitId(data[0]);
        body.setVisitName(data[1]);
        body.setVisitURL(data[2]);
        body.setIp(TravelUtil.getIPAddress());
        body.setPhoneModel(android.os.Build.MODEL);
        body.setBeginTime(data[3]);
        body.setEndTime(data[4]);
        setMsgBody(body);
        return this;
    }
}
