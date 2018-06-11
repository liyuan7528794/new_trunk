package com.travel.video.help;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import com.tencent.TIMCallBack;
import com.tencent.TIMValueCallBack;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.travel.Constants;
import com.travel.bean.PersonalInfoBean;
import com.travel.bean.VideoInfoBean;
import com.travel.communication.helper.TIMGroupMessageReceiver;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.VideoConstant;
import com.travel.video.live.HostWindowActivity;
import com.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用腾讯云直播中需要的Http请求
 * Created by ldkxingzhe on 2016/8/26.
 */
public class LiveHttpRequest {
    @SuppressWarnings("unused")
    private static final String TAG = "LiveHttpRequest";

    private Context mContext;
    private ServerListView mView;
    private boolean mServerKnowThisLive = false;

    /* 开始背包直播直播 */
    public void notifyServerPackLiveStart() {
        CurLiveInfo.getInstance().setLiveType(HostWindowActivity.LIVE_TYPE_PACK);
        notifyServerLiveStart();
    }

    public void notifyServerPackLiveStop() {
        Map<String, Object> map = convertToMap();
        map.put("url", "-1");
        notifyServerLiveStop(map);
    }

    public void notifyServerGroupLiveStop(){
        Map<String, Object> map = convertToMap();
        map.put("url", "-1");
        notifyServerLiveStop(map);

        Intent intent = new Intent(TIMGroupMessageReceiver.ACTION_END_LIVE);
        intent.putExtra("VideoInfoBean", getVideoInfoBean());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public interface ServerListView{
        /**
         * 通知服务器开始直播的结果返回
         * @param isSuccess true -- 成功
         */
        void onNotifyServerLiveStartResult(boolean isSuccess);
        void onNotifyServerLiveStopResult(boolean isSuccess);
    }

    public LiveHttpRequest(Context context, ServerListView view){
        mContext = context;
        mView = view;
    }

    private Map<String, Object> convertToMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("hashedId", String.valueOf(CurLiveInfo.getInstance().getRoomNum()));
        map.put("coverId", CurLiveInfo.getInstance().getCoverId());
        map.put("imgUrl", CurLiveInfo.getInstance().getCoverurl());
        map.put("title", CurLiveInfo.getInstance().getTitle());
        map.put("goodsid", CurLiveInfo.getInstance().getGoodsId());
        map.put("userId", UserSharedPreference.getUserId());
        map.put("longitude", CurLiveInfo.getInstance().getLongitude());
        map.put("latitude", CurLiveInfo.getInstance().getLatitude());
        map.put("place", CurLiveInfo.getInstance().getAddress());
        map.put("share", CurLiveInfo.getInstance().getShare());
        map.put("activityId", CurLiveInfo.getInstance().getActivityId());
        map.put("address", CurLiveInfo.getInstance().getM3u8Address());// 分享
        map.put("liveType", CurLiveInfo.getInstance().getLiveType());
        if(!TextUtils.isEmpty(CurLiveInfo.getInstance().getRtmpAddress())){
//            map.put("packageRtmpUrl", CurLiveInfo.getInstance().getM3u8Address()); // 背包观看直播地址
            map.put("url", CurLiveInfo.getInstance().getRtmpAddress());
        }
        if(!TextUtils.isEmpty(CurLiveInfo.getInstance().getStreamId()))
            map.put("streamId", CurLiveInfo.getInstance().getStreamId());
        return map;
    }

    public boolean isServerKnowThisLive(){
        return mServerKnowThisLive;
    }


    /**
     * 通知服务器直播开始
     */
    public void notifyServerLiveStart(){
        String url = Constants.Root_Url + "/live/startInteractionLive.do";
        NetWorkUtil.postForm(mContext, url, new MResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    // 成功
                    mServerKnowThisLive = true;
                    mView.onNotifyServerLiveStartResult(true);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                // 失败
                mView.onNotifyServerLiveStartResult(false);
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                // 失败
                mView.onNotifyServerLiveStartResult(false);
            }
        }, convertToMap());

        HeartBeatHelper.getInstance().setRoomNum(CurLiveInfo.getInstance().getRoomNum())
                .setUserId(MySelfInfo.getInstance().getId())
                .setStatus("1")
                .startSendHeartBeat();

        Intent intent = new Intent();
        intent.setAction(TIMGroupMessageReceiver.ACTION_START_LIVE);
        intent.putExtra("VideoInfoBean", getVideoInfoBean());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @NonNull
    private VideoInfoBean getVideoInfoBean() {
        VideoInfoBean videoInfoBean = new VideoInfoBean();
        videoInfoBean.setShareAddress(CurLiveInfo.getInstance().getM3u8Address());
        videoInfoBean.setRtmpAddress(CurLiveInfo.getInstance().getRtmpAddress());
        String videoUrl = String.valueOf(CurLiveInfo.getInstance().getRoomNum());
        if (CurLiveInfo.getInstance().getLiveType() == 4){
            videoUrl += "," + CurLiveInfo.getInstance().getM3u8Address();
        }
        videoInfoBean.setUrl(String.valueOf(CurLiveInfo.getInstance().getRoomNum()));
        videoInfoBean.setShare(CurLiveInfo.getInstance().getShare());
        videoInfoBean.setVideoTitle(CurLiveInfo.getInstance().getTitle());
        videoInfoBean.setVideoImg(CurLiveInfo.getInstance().getCoverurl());
        videoInfoBean.setVideoType(CurLiveInfo.getInstance().getLiveType());
        PersonalInfoBean personalInfoBean = new PersonalInfoBean();
        personalInfoBean.setUserId(String.valueOf(CurLiveInfo.getInstance().getHostHLLXUserId()));
        personalInfoBean.setUserName(CurLiveInfo.getInstance().getHostName());
        personalInfoBean.setUserPhoto(CurLiveInfo.getInstance().getHostAvator());
        videoInfoBean.setPersonalInfoBean(personalInfoBean);
        return videoInfoBean;
    }

    public void notifyServerLiveStop(String roomId, List<String> urlList){
        StringBuilder urlParams = new StringBuilder();
        if(urlList != null){
            for(String tmp : urlList){
                urlParams.append(Base64.encodeToString(tmp.getBytes(), Base64.DEFAULT));
                urlParams.append(',');
            }
        }
        Map<String, Object> map = convertToMap();
        map.put("url", urlParams.toString().replace("\n", ""));
        notifyServerLiveStop(map);
        Intent intent = new Intent(TIMGroupMessageReceiver.ACTION_END_LIVE);
        intent.putExtra("VideoInfoBean", getVideoInfoBean());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void notifyServerLiveStop(Map<String, Object> map) {
        String url = Constants.Root_Url + "/live/stopInteractionLive.do";
        NetWorkUtil.postForm(mContext, url, new MResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    // 成功
                    mView.onNotifyServerLiveStopResult(true);
                }
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                // 成功
                mView.onNotifyServerLiveStopResult(false);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                // 成功
                mView.onNotifyServerLiveStopResult(false);
            }
        }, map);
    }

    /* 获取自己的背包对应的url地址 */
    public void getPackLiveUrl(String id, final TIMValueCallBack<String> callBack){
        String url = Constants.Root_Url + "/live/packageAddress.do";
        Map<String, Object> map = new HashMap<>();
        map.put("userId", id);
        if(CurLiveInfo.getInstance().getLiveType() == HostWindowActivity.LIVE_TYPE_PACK)
            map.put("type", 2);
        else
            map.put("type", 1);
        NetWorkUtil.postForm(mContext, url, new MResponseListener(){

            @Override
            protected void onDataFine(JSONObject data) {
                String pushRTMPURL = JsonUtil.getJson(data,"pushRTMPURL");
                String m3u8Address = JsonUtil.getJson(data, "HLSAddress");
                String rtmpAddress = JsonUtil.getJson(data, "RTMPAddress");
                String streamId = JsonUtil.getJson(data,"streamId");
                CurLiveInfo.getInstance().setRtmpStreamUrl(pushRTMPURL);
                CurLiveInfo.getInstance().setRtmpAddress(rtmpAddress);
                CurLiveInfo.getInstance().setM3u8Address(m3u8Address);
                CurLiveInfo.getInstance().setStreamId(streamId);
                callBack.onSuccess(pushRTMPURL);
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                callBack.onError(error, msg);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.onError(-1, "网络错误");
            }
        }, map);
    }

    /**
     * 获取直播的基本信息
     */
    public void getLiveCacheBaseInfo(String roomNum, final TIMValueCallBack<LiveBaseInfo> callBack){
        String url = Constants.Root_Url + "/live/getLiveCacheBaseInfo.do";
        Map<String, Object> map = new HashMap<>();
        map.put("hashedId", roomNum);
        NetWorkUtil.postForm(mContext, url, new MResponseListener() {
            @Override
            protected void onDataFine(JSONObject data) {
                LiveBaseInfo info = new LiveBaseInfo();
                info.setPraiseNum(JsonUtil.getJsonLong(data, "praiseNum"));
                info.setNowWatchNum(JsonUtil.getJsonLong(data, "nowWatchNum"));
                info.setTotalWatchNum(JsonUtil.getJsonLong(data, "tatlWatchNum"));
                callBack.onSuccess(info);
                MLog.d(TAG, "onGotLiveCacheBaseInfo, result is %s.", info.toString());
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                MLog.e(TAG, "onGotLiveCacheBaseInfo, result is error");
            }
        }, map);
    }

    private int mapId = 0;

    /**
     * 将直播添加到地图列表
     * @param type
     * @param title
     * @param subTitle
     * @param iconPath
     * @param latitude
     * @param longitude
     * @param objId
     * @param objInfo
     */
    public void addLiveToMap(int type, String title, String subTitle, String iconPath,
                             double latitude, double longitude, String objId, String objInfo){
        String url = VideoConstant.MAP_ADD_ACTIONLIVE;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("objType", type);
        map.put("title", title);
        map.put("subtitle", subTitle);
        map.put("iconPath", iconPath);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("objId", objId);
        map.put("objInfo", objInfo);
        System.out.println("live_map:"+map);
        NetWorkUtil.postForm(mContext, url, new MResponseListener() {
            @Override
            protected void onDataFine(JSONObject data) {
                if(data!=null){
                    try {
                        mapId = data.getInt("id");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    Toast.makeText(mContext, "您当前的直播，添加到地图失败！", Toast.LENGTH_SHORT).show();
                }
            }
        }, map);

    }

    /**
     * 举报视频
     * @param context
     * @param userId
     * @param videoId
     * @param type  0表示举报直播，1表示举报回放
     */
    public static void reportVideoRequest(final Context context, String userId, String videoId, String type) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (!TextUtils.isEmpty(userId))
            map.put("userId", userId);
        if (videoId != null && !"".equals(videoId))
            map.put("videoId", videoId);
        map.put("type", type);
        map.put("content", "");
        NetWorkUtil.postForm(context, VideoConstant.REPORT_LIVE, new MResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    Toast.makeText(context, "举报成功！", Toast.LENGTH_SHORT).show();
                }
            }
        }, map);
    }


    public void applyFlowControlOperationPermission(String roomId, String userId, int optType, @NonNull final TIMCallBack callBack){
        String url = Constants.Root_Url + "/live/ApplyFlowControlOperationPermissions.do";
        Map<String, Object> map = new HashMap<>();
        if(!TextUtils.isEmpty(roomId)){
            map.put("roomId", roomId);
        }
        map.put("userId", userId);
        map.put("optType", optType);
        NetWorkUtil.postForm(mContext, url, new MResponseListener() {

            @Override
            protected void onDataFine(int data) {
                if(data == 1){
                    callBack.onSuccess();
                }else{
                    callBack.onError(1, "验证失败");
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.onError(0, "网络失败");
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                callBack.onError(error, msg);
            }
        }, map);
    }


    public static class LiveBaseInfo{
        private long praiseNum, nowWatchNum, totalWatchNum;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("praiseNum:")
                    .append(praiseNum)
                    .append(", nowWatchNum:")
                    .append(nowWatchNum)
                    .append(", totalWatchNum:")
                    .append(totalWatchNum);
            return builder.toString();
        }

        public long getPraiseNum() {
            return praiseNum;
        }

        public void setPraiseNum(long praiseNum) {
            this.praiseNum = praiseNum;
        }

        public long getNowWatchNum() {
            return nowWatchNum;
        }

        public void setNowWatchNum(long nowWatchNum) {
            this.nowWatchNum = nowWatchNum;
        }

        public long getTotalWatchNum() {
            return totalWatchNum;
        }

        public void setTotalWatchNum(long totalWatchNum) {
            this.totalWatchNum = totalWatchNum;
        }
    }
}
