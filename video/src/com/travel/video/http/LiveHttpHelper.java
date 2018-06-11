package com.travel.video.http;

import android.content.Context;

import com.travel.Constants;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.volley.VolleyError;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/11.
 */
public class LiveHttpHelper {
    private Context context;
    private HttpListener listener;

    public interface HttpListener{
        void getLiveInfo(VideoInfoBean videoInfoBean);
    }

    public LiveHttpHelper(Context context, HttpListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void getLiveInfo(String liveId){
        String url = Constants.Root_Url + "/live/getLive.do";
        Map<String, Object> map = new HashMap<>();
        map.put("liveId", liveId);
        NetWorkUtil.postForm(context, url, new MResponseListener() {
            @Override
            protected void onDataFine(JSONObject data) {
                VideoInfoBean mVideoBean = new VideoInfoBean().getVideoInfoBean(data);
                // 成功
                listener.getLiveInfo(mVideoBean);
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                listener.getLiveInfo(null);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                listener.getLiveInfo(null);
            }
        }, map);

    }
}
