package com.travel.utils;

import android.content.Context;
import android.text.TextUtils;

import com.travel.ShopConstant;
import com.travel.helper.TalkPraiseDao;
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

public class TalkPraiseUtil {
    private static TalkPraiseUtil videoVoteUtil;
    private Context context;
    private TalkPraiseDao voteDao;
    private HashMap<Integer, String> hashMap = new HashMap<>();
    private TalkPraiseUtil(){
        this.context = TravelApp.appContext;
        init();
    }

    public static TalkPraiseUtil getInstance(){
        if(videoVoteUtil == null){
            videoVoteUtil = new TalkPraiseUtil();
        }
        return videoVoteUtil;
    }

    private void init(){
        if(UserSharedPreference.isLogin()) {
            voteDao = new TalkPraiseDao(context, UserSharedPreference.getUserId());
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

    public void deleteData(int videoId){
        if (voteDao != null) {
            voteDao.deletePerson(videoId+"");
            hashMap.remove(videoId);
        }
    }

    public void cacheData(int videoId, String userId){
        if (voteDao != null) {
            voteDao.addPerson(videoId, userId);
            hashMap.put(videoId, userId);
        }
    }

    public void voteVideo(Context context, final String talkId, final String praiseType, final VideoVoteListener listener) {
        final Map<String, Object> map = new HashMap<>();
        map.put("praiseType", praiseType);
        map.put("talkId", talkId);
        NetWorkUtil.postForm(context, ShopConstant.TALK_PRAISR_ADD, new MResponseListener(context) {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if(response.optInt("error") == 0){
                    if(TextUtils.equals(praiseType,"0")) {
                        getInstance().cacheData(Integer.parseInt(talkId), UserSharedPreference.getUserId());
                    }else{
                        getInstance().deleteData(Integer.parseInt(talkId));
                    }
                    listener.onSuccess(0, true);
                }
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                listener.onSuccess(error, false);
            }
        }, map);
    }

    private boolean isRead = false;
    public HashMap<Integer, String> getDatas(){
        if(context != null && voteDao == null){
            init();
        }
        if((hashMap.size()<=0 || !isRead) && voteDao != null) {
            hashMap = voteDao.findAllPerson();
            isRead = true;
        }
        return hashMap;
    }

    public interface VideoVoteListener{
        void onSuccess(int error, boolean isSuccess);
    }
}
