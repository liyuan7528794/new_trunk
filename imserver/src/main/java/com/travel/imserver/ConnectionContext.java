package com.travel.imserver;

import android.os.Parcel;
import android.os.Parcelable;

import com.travel.imserver.bean.ClientData;

import java.io.Serializable;

/**
 * 连接的上下文部分
 * 包含连接的通用信息， 具体含义请参见属性
 * Created by ldkxingzhe on 2016/12/9.
 */
public class ConnectionContext implements Serializable, Parcelable{
    boolean mIsActive;  // socket是否联通
    boolean mIsLogin;
    boolean mIsLogout;   // 是否被异地登录
    boolean mIsDestroy;  // 是否已经被销毁
    String mCurrentRoom; // 我的当前房间
    String mChangingRoom;
    String mChangingRoomMsgID;
    String mLoginID;     // 发送登录指令的命令Id, null表示没有
    String mLogoutID;    // 发送退出指令的命令Id, null 表示没有
    ClientData mClientData; // 我的用户信息

    public ConnectionContext(){
        // 默认待在home房间
        mCurrentRoom = "home";
    }

    protected ConnectionContext(Parcel in) {
        mIsActive = in.readByte() != 0;
        mIsLogin = in.readByte() != 0;
        mIsLogout = in.readByte() != 0;
        mIsDestroy = in.readByte() != 0;
        mCurrentRoom = in.readString();
        mChangingRoom = in.readString();
        mChangingRoomMsgID = in.readString();
        mLoginID = in.readString();
        mLogoutID = in.readString();
        mClientData = in.readParcelable(ClientData.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (mIsActive ? 1 : 0));
        dest.writeByte((byte) (mIsLogin ? 1 : 0));
        dest.writeByte((byte) (mIsLogout ? 1 : 0));
        dest.writeByte((byte) (mIsDestroy ? 1 : 0));
        dest.writeString(mCurrentRoom);
        dest.writeString(mChangingRoom);
        dest.writeString(mChangingRoomMsgID);
        dest.writeString(mLoginID);
        dest.writeString(mLogoutID);
        dest.writeParcelable(mClientData, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ConnectionContext> CREATOR = new Creator<ConnectionContext>() {
        @Override
        public ConnectionContext createFromParcel(Parcel in) {
            return new ConnectionContext(in);
        }

        @Override
        public ConnectionContext[] newArray(int size) {
            return new ConnectionContext[size];
        }
    };

    public boolean shouldReconnection(){
        return !mIsLogout && !mIsDestroy;
    }

    public String getCurrentRoom(){
        return mCurrentRoom;
    }


    @Override
    public String toString() {
        return "mIsActive: " + mIsActive
                + ", mIsLogin: " + mIsLogin
                + ", mCurrentRoom: " + mCurrentRoom
                + ", mUserId: " + (mClientData == null ? null : mClientData.getUserId());
    }
}
