package com.travel.imserver.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * 基本消息体类型
 * Created by ldkxingzhe on 2016/12/7.
 */
public class BaseBean implements Serializable, Parcelable{
    @SuppressWarnings("unused")
    private static final String TAG = "BaseBean";

    public final static int SUCCESS = -1;
    public final static int FAILED = -2;
    public final static int CONTEXT = -100;

    public final static String ACTION_LOGIN = "Android_Login";
    public final static String ACTION_CHANGE_ROOM = "Android_Change_Room";
    public final static String ACTION_HEART_BEAT = "Android_Heart";

    public static final int TYPE_SINGLE_CHAT = 1;
    public static final int TYPE_GROUP_CHAT = 2;

    private String id;
    private int type;
    private String sendUser;
    private String receive;
    private String room;
    private String msgHead;
    private String msgBody;
    private ClientData clientData;


    public BaseBean(){}
    protected BaseBean(Parcel in) {
        id = in.readString();
        type = in.readInt();
        sendUser = in.readString();
        receive = in.readString();
        room = in.readString();
        msgHead = in.readString();
        msgBody = in.readString();
        clientData = in.readParcelable(ClientData.class.getClassLoader());
    }

    public static final Creator<BaseBean> CREATOR = new Creator<BaseBean>() {
        @Override
        public BaseBean createFromParcel(Parcel in) {
            return new BaseBean(in);
        }

        @Override
        public BaseBean[] newArray(int size) {
            return new BaseBean[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSendUser() {
        return sendUser;
    }

    public void setSendUser(String sendUser) {
        this.sendUser = sendUser;
    }

    public String getReceive() {
        return receive;
    }

    public void setReceive(String receive) {
        this.receive = receive;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getMsgHead() {
        return msgHead;
    }

    public void setMsgHead(String msgHead) {
        this.msgHead = msgHead;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public ClientData getClientData() {
        return clientData;
    }

    public void setClientData(ClientData clientData) {
        this.clientData = clientData;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this, BaseBean.class);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(type);
        dest.writeString(sendUser);
        dest.writeString(receive);
        dest.writeString(room);
        dest.writeString(msgHead);
        dest.writeString(msgBody);
        dest.writeParcelable(clientData, flags);
    }

}
