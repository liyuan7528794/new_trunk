package com.travel.video.help;

import android.content.Context;

import com.travel.VideoConstant;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.video.bean.BarrageInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/11/17.
 */

public class PlaybackHttpHelper {
    private Context context;
    private VideoHttpListener videoHttpListener;

    public interface VideoHttpListener{
        public void GetHttpInitData(List<BarrageInfo> barrageInfos, Long createVideoTime);
    }
    public PlaybackHttpHelper(Context context, VideoHttpListener videoHttpListener) {
        this.context = context;
        this.videoHttpListener = videoHttpListener;
    }

    public void submitPraiseData(String videoId, int addedPraiseNum) {
        if (addedPraiseNum <= 0)
            return;

        Map<String, Object> map = new HashMap<String, Object>();
        if (videoId != null && !"".equals(videoId))
            map.put("videoId", videoId);
        map.put("praiseNum", addedPraiseNum);
        NetWorkUtil.postForm(context, VideoConstant.HISTORY_VIDEO_ADD_PRAISE,
                new MResponseListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        super.onResponse(response);
                    }
                }, map);
    }

    public void initVideoData(final String videoId) {
        if (videoId==null || "".equals(videoId))
            return;
        Map<String, Object> paramap = new HashMap<String, Object>();
        paramap.put("videoId", videoId);
        NetWorkUtil.postForm(context, VideoConstant.HISTORY_VIDEO_DATA_UPDATA, new MResponseListener() {
//
//            @Override
//            protected void onDataFine(JSONObject jsondata) {
//                try {
//                    List<BarrageInfo> barrageInfos = new ArrayList<BarrageInfo>();
//                    Long createTime = JsonUtil.getJsonLong(jsondata,"createdAt");
//                    if (jsondata.has("barrageInfos") && jsondata.getJSONArray("barrageInfos").length() != 0) {
//                        JSONArray jsonInfo = jsondata.getJSONArray("barrageInfos");
//                        for (int i = 0; i < jsonInfo.length(); i++) {
//                            JSONObject info = jsonInfo.getJSONObject(i);
//                            BarrageInfo barrageInfo = new BarrageInfo();
//                            barrageInfo.setUserId(JsonUtil.getJson(info, "userId"));
//                            barrageInfo.setNickName(JsonUtil.getJson(info, "nickName"));
//                            barrageInfo.setUserImg(JsonUtil.getJson(info, "imgUrl"));
//                            barrageInfo.setContent(JsonUtil.getJson(info, "content"));
//                            barrageInfo.setSubmitTime(JsonUtil.getJsonLong(info, "submitTime"));
//                            barrageInfos.add(barrageInfo);
//                        }
//                    }
//                    videoHttpListener.GetHttpInitData(barrageInfos,createTime);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }

            @Override
            protected void onDataFine(JSONArray jsonInfo) {
                List<BarrageInfo> barrageInfos = new ArrayList<BarrageInfo>();
                try {
                    for (int i = 0; i < jsonInfo.length(); i++) {
                        JSONObject info = null;
                        info = jsonInfo.getJSONObject(i);
                        BarrageInfo barrageInfo = new BarrageInfo();
                        barrageInfo.setUserId(JsonUtil.getJson(info, "userId"));
                        barrageInfo.setNickName(JsonUtil.getJson(info, "nickName"));
                        barrageInfo.setUserImg(JsonUtil.getJson(info, "imgUrl"));
                        barrageInfo.setContent(JsonUtil.getJson(info, "content"));
                        barrageInfo.setSubmitTime(JsonUtil.getJsonLong(info, "submitTime"));
                        barrageInfos.add(barrageInfo);

                    }
                    videoHttpListener.GetHttpInitData(barrageInfos,232323L);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, paramap);

    }
}
