package com.travel.localfile.pk.entity;

import com.travel.communication.entity.UserData;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.localfile.dao.LocalFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 一条证据项包裹
 * Created by ldkxingzhe on 2016/7/6.
 */
public class EvidencePacket {
    @SuppressWarnings("unused")
    private static final String TAG = "EvidencePacket";

    /** 多媒体类型, 本次取证的资料集合 */
    public static final int TYPE_MULTIPLE_MEDIA = 0;

    private String mIntroduction; // 词条列证据的简单说明
    private int mType;  // 类型 @see TYPE_*;
    private String mCreateTime;  // 此条证据包裹的创建时间
    private UserData userData;

    private ArrayList<LocalFile> mMultipleMediaList; // 取证资料之多媒体

    transient boolean mIsLeft;


    public static EvidencePacket generateFromJSONObject(JSONObject jsonObject,
                                                  String buyerId, String sellerId){
        EvidencePacket packet = new EvidencePacket();
        packet.setCreateTime(JsonUtil.getJson(jsonObject, "createTime"));
        packet.setIntroduction(JsonUtil.getJson(jsonObject, "reason"));
        String userId = JsonUtil.getJson(jsonObject, "userId");
        if(buyerId.equals(userId)){
            packet.mIsLeft = true;
        }else{
            packet.mIsLeft = false;
        }
        packet.setType(EvidencePacket.TYPE_MULTIPLE_MEDIA);
        JSONArray jsonArray = (JSONArray) JsonUtil.get(jsonObject, "dataArray");
        if(jsonArray != null){
            packet.mMultipleMediaList = new ArrayList<LocalFile>();
            for(int i = 0, size = jsonArray.length(); i < size; i++){
                try {
                    JSONObject localFile = (JSONObject) jsonArray.get(i);
                    packet.mMultipleMediaList.add(LocalFileUtil.toObjectFromJSONObject(localFile));
                } catch (JSONException e) {
                    MLog.e(TAG, e.getMessage(), e);
                }
            }
        }
        return packet;
    }

    public ArrayList<LocalFile> getMultipleMediaList(){
        return mMultipleMediaList;
    }

    public void setMultipleMediaList(ArrayList<LocalFile> list){
        mMultipleMediaList = list;
    }

    public void setIsLeft(boolean isLeft){
        mIsLeft = isLeft;
    }

    public boolean isLeft(){
        return mIsLeft;
    }

    public void setType(int type){
        mType = type;
    }

    public void setIntroduction(String introduction){
        mIntroduction = introduction;
    }

    public void setCreateTime(String createTime){
        mCreateTime = createTime;
    }

    public int getType(){
        return mType;
    }

    public String getIntroduction(){
        return mIntroduction;
    }

    public String getCreateTime(){
        return mCreateTime;
    }

    public UserData getUserData() {
        return userData;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }
}
