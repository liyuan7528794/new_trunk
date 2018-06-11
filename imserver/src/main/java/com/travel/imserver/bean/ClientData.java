package com.travel.imserver.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * 用户信息
 * Created by ldkxingzhe on 2016/12/7.
 */
public class ClientData implements Serializable, Parcelable{
    @SuppressWarnings("unused")
    private static final String TAG = "ClientData";

    private String userId;
    private String nickName;
    private String imgUrl;
    private String type;
    private String netWork;
    private String model;
    private String system;

    protected ClientData(Parcel in) {
        userId = in.readString();
        nickName = in.readString();
        imgUrl = in.readString();
        type = in.readString();
        netWork = in.readString();
        model = in.readString();
        system = in.readString();
    }

    public ClientData(){}

    public static final Creator<ClientData> CREATOR = new Creator<ClientData>() {
        @Override
        public ClientData createFromParcel(Parcel in) {
            return new ClientData(in);
        }

        @Override
        public ClientData[] newArray(int size) {
            return new ClientData[size];
        }
    };

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getNetWork() {
        return netWork;
    }

    public void setNetWork(String netWork) {
        this.netWork = netWork;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    /*
    * 清楚登录时的特有信息
    * */
    public void clearLoginInfo(){
        setNetWork(null);
        setSystem(null);
        setModel(null);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(nickName);
        dest.writeString(imgUrl);
        dest.writeString(type);
        dest.writeString(netWork);
        dest.writeString(model);
        dest.writeString(system);
    }
}
