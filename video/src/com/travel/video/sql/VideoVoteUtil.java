package com.travel.video.sql;

import android.content.Context;

import com.travel.VideoConstant;
import com.travel.lib.TravelApp;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.UserSharedPreference;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/2.
 */

public class VideoVoteUtil {
    private static VideoVoteUtil videoVoteUtil;
    private Context context;
    private VideoVoteDao voteDao;
    private HashMap<Integer, String> hashMap = new HashMap<>();
    private VideoVoteUtil(){
        this.context = TravelApp.appContext;
        init();
    }

    public static VideoVoteUtil getInstance(){
        if(videoVoteUtil == null){
            videoVoteUtil = new VideoVoteUtil();
        }
        return videoVoteUtil;
    }

    private void init(){
        if(UserSharedPreference.isLogin()) {
            voteDao = new VideoVoteDao(context, UserSharedPreference.getUserId());
            isRead = false;
        }
    }

    public void start(){
        release();
        getInstance();
    }

    public void release(){
        hashMap.clear();
        voteDao = null;
        videoVoteUtil = null;
        isRead = false;
    }

    public void cacheData(int videoId, String userId){
        voteDao.addPerson(videoId, userId);
        hashMap.put(videoId, userId);
    }

    public void voteVideo(final String videoId, final String userId, final VideoVoteListener listener) {
        final Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("videoId", videoId);
        NetWorkUtil.postForm(context, VideoConstant.VIDEO_VOTE, new MResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if(response.optInt("error") == 0){
                    cacheData(Integer.parseInt(videoId), userId);
                    listener.onSuccess(0, true);
                }
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                listener.onSuccess(error, false);
                if(error == 1){
                    cacheData(Integer.parseInt(videoId), userId);
                }
            }
        }, map);
    }

    private boolean isRead = false;
    public HashMap<Integer, String> getDatas(){
        if(context != null && voteDao == null){
            init();
        }
        if(!isRead && voteDao != null) {
            hashMap = voteDao.findAllPerson();
            isRead = true;
        }
        return hashMap;
    }

    public interface VideoVoteListener{
        void onSuccess(int error, boolean isSuccess);
    }
}
