package com.travel.imserver;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 用于消息发送
 * Created by ldkxingzhe on 2016/12/7.
 */
public class BaseMessage implements Parcelable{
    @SuppressWarnings("unused")
    private static final String TAG = "BaseMessage";

    private String mId, mMessageContent;

    public BaseMessage(String id, String content){
        mId = id;
        mMessageContent = content;
    }

    public BaseMessage(String messageContent){
        this(JsonClientHandler.DISCARD_ID, messageContent);
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getMessageContent() {
        return mMessageContent;
    }

    public void setMessageContent(String messageContent) {
        this.mMessageContent = messageContent;
    }

    private BaseMessage(Parcel in) {
        mId = in.readString();
        mMessageContent = in.readString();
    }

    public static final Creator<BaseMessage> CREATOR = new Creator<BaseMessage>() {
        @Override
        public BaseMessage createFromParcel(Parcel in) {
            return new BaseMessage(in);
        }

        @Override
        public BaseMessage[] newArray(int size) {
            return new BaseMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return mMessageContent;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mMessageContent);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this
                || obj instanceof BaseMessage && mId.equals(((BaseMessage) obj).getId());
    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }
}
