package com.travel.video.help;

import android.content.Context;

import com.travel.ShopConstant;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/16.
 */

public class VideoHttpHelper {

    private Context context;
    private VideoHttpHelperListener videoHttpHelperListener;
    public interface VideoHttpHelperListener{
        public void getVideos(int times, ArrayList<VideoInfoBean> videos, int liveNum);
    }

    public VideoHttpHelper(Context context, VideoHttpHelperListener videoHttpHelperListener){
        this.context = context;
        this.videoHttpHelperListener = videoHttpHelperListener;
    }

    /**
     * 获取视频列表
     * @param times
     * @param status 0表示获取全部，1表示获取直播 2录像
     */
    public void getNetVideos(final int times, final int status) {
        Map<String, Object> map = new HashMap<>();
        if(status != 0)
            map.put("status", status);
        map.put("statusShow", 1);// 1:所有人可见 2:自己可见
        map.put("times", times);
        NetWorkUtil.postForm(context, ShopConstant.VIDEOS_INFO, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                ArrayList<VideoInfoBean> list = new ArrayList<VideoInfoBean>();
                if(data!=null && data.length() > 0 && status == 1) {
                    videoHttpHelperListener.getVideos(times,null, data.length());
                    return;
                }
                try {
                    if(data!=null && data.length() > 0) {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject dataObject = data.getJSONObject(i);
                            VideoInfoBean mVideoInfoBean = new VideoInfoBean();
                            list.add(mVideoInfoBean.getVideoInfoBean(dataObject));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                videoHttpHelperListener.getVideos(times,list, -1);
            }
        }, map);
    }
}
