package com.travel.shop.http;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.travel.ShopConstant;
import com.travel.bean.VideoInfoBean;
import com.travel.imserver.ResultCallback2;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 直播列表所需要的Http请求
 * Created by ldkxingzhe on 2017/2/10.
 */
public final class LiveInfoHttp {
    @SuppressWarnings("unused")
    private static final String TAG = "LiveInfoHttp";

    private LiveInfoHttp(){}

    private static class ParamEntity{
        String keyword; // 关键词
        String activityId; // 活动Id
        String scenicId; // 景区Id
        int type = -1; // 类型1用户视频, 2景区视频
        int status = -1; // 1直播, 2回放
        String userId; // 用户Id
        String statusSHow; // 1-所有人可见， 2- 仅自己可见
        int times = -1; // 页面
        String place; // 地址

        public Map<String, Object> toMap(){
            Map<String,Object> map = new HashMap<String, Object>();
            if (!TextUtils.isEmpty(keyword)){
                map.put("keyword", keyword);
            }

            if (!TextUtils.isEmpty(activityId)){
                map.put("activityId", activityId);
            }

            if(!TextUtils.isEmpty(scenicId)){
                map.put("scenicId", scenicId);
            }
            if (type == 1 || type == 2){
                map.put("type", type);
            }
            if (status ==1 || status == 2){
                map.put("status", status);
            }
            if (!TextUtils.isEmpty(userId)){
                map.put("userId", userId);
            }
            if(!TextUtils.isEmpty(statusSHow)){
                map.put("statusShow", statusSHow);
            }
            if (times != -1){
                map.put("times", times);
            }

            if (!TextUtils.isEmpty(place)){
                map.put("place", place);
            }

            return map;
        }
    }

    static void getLiveList(
            @NonNull final Context context,
            @NonNull ParamEntity paramEntity,
            @NonNull final ResultCallback2<List<VideoInfoBean>> callback){
        NetWorkUtil.postForm(context, ShopConstant.VIDEOS_INFO, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {

                try {
                    List<VideoInfoBean> videoList = new ArrayList<VideoInfoBean>();
                    if(data.length() > 0){
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject dataObject = data.getJSONObject(i);
                            VideoInfoBean videoBean = new VideoInfoBean();
                            videoList.add(videoBean.getVideoInfoBean(dataObject));
                        }
                    }
                    callback.onResult(videoList);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onError(-1, e.getMessage());
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(-2, error.getMessage());
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                callback.onError(error, msg);
            }
        }, paramEntity.toMap());
    }

    /**
     * 获取某人的userId
     * @param times   页码, 默认给-1
     * @param userId  userId
     */
    public static void getPersonalVideoList(Context context, int times,
                                            @Nullable String userId,
                                            @NonNull ResultCallback2<List<VideoInfoBean>> callback){
        ParamEntity entity = new ParamEntity();
        entity.times = times;
        entity.userId = userId;
        getLiveList(context, entity, callback);
    }

    public static void getGroupLiveList(Context context,
                                        @Nullable String place,
                                        @NonNull ResultCallback2<List<VideoInfoBean>> callback2){
        ParamEntity entity = new ParamEntity();
        entity.place = place;
        entity.status = 1;
        getLiveList(context, entity, callback2);
    }
}
