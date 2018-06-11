package com.travel.localfile.pk.entity;

import com.google.gson.JsonObject;
import com.travel.lib.utils.JsonUtil;
import com.travel.localfile.CameraFragment;
import com.travel.localfile.dao.LocalFile;

import org.json.JSONObject;

/**
 * 由于使用了Greendao自动生成数据库, 又不想继承LocalFIle
 * 写一个工具类中和中和
 * Created by ldkxingzhe on 2016/7/8.
 */
public class LocalFileUtil {

    private LocalFileUtil(){}

    /**
     * 由localfile生成jsonobject
     * gson库里面的
     */
    public static JsonObject toJsonObject(LocalFile localFile){
        JsonObject jsonObject = new JsonObject();
        String type = null;
        switch (localFile.getType()){
            case CameraFragment.TYPE_AUDIO:
                type = "audio";
                jsonObject.addProperty("timeLong", localFile.getDurationFormat());
                break;
            case CameraFragment.TYPE_PHOTO:
                type = "photo";
                break;
            case CameraFragment.TYPE_VIDEO:
                type = "video";
                jsonObject.addProperty("cover", localFile.getThumbnailPath());
                jsonObject.addProperty("timeLong", localFile.getDurationFormat());
                break;
            case CameraFragment.TYPE_LIVE:
                type = "live";
                jsonObject.addProperty("cover", localFile.getThumbnailPath());
                break;
        }
        jsonObject.addProperty("createTime", localFile.getCreateTimeFormat());
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("content", localFile.getRemotePath());
        return jsonObject;
    }

    /**
     * 从JSONObject中生成对象实体
     */
    public static LocalFile toObjectFromJSONObject(JSONObject jsonObject){
        LocalFile localFile = new LocalFile();
        localFile.setRemotePath(JsonUtil.getJson(jsonObject, "content"));
        String type = JsonUtil.getJson(jsonObject, "type");
        String thumbnail = JsonUtil.getJson(jsonObject, "cover");
        localFile.setDurationFormat(JsonUtil.getJson(jsonObject, "timeLong"));
        localFile.setCreateTimeFormat(JsonUtil.getJson(jsonObject, "createTime"));
        if("photo".equals(type)){
            localFile.setType(CameraFragment.TYPE_PHOTO);
            thumbnail = localFile.getRemotePath();
        }else if("video".equals(type)){
            localFile.setType(CameraFragment.TYPE_VIDEO);
        }else if("audio".equals(type)){
            localFile.setType(CameraFragment.TYPE_AUDIO);
        }else if("live".equals(type)){
            localFile.setType(CameraFragment.TYPE_LIVE);
        }else{
            throw new IllegalStateException("wrong type found");
        }
        localFile.setThumbnailPath(thumbnail);
        return localFile;
    }


    public static String toString(LocalFile localFile){
        return toJsonObject(localFile).toString();
    }
}
