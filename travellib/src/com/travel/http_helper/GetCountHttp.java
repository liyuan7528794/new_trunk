package com.travel.http_helper;

import android.content.Context;
import android.widget.Toast;

import com.travel.Constants;
import com.travel.ShopConstant;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.volley.VolleyError;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取直播数和关注数
 * Created by Administrator on 2016/11/14.
 */

public class GetCountHttp {
    private CountListener listener;

    public GetCountHttp(CountListener listener) {
        this.listener = listener;
    }

    /**
     * 获取数量或
     * isResult表示获取是否获取到网络结果 true-表示获取正常， false-表示网络错误或。。。
     */
    public interface CountListener {
        void OnGetVideoCount(boolean isResult, int videoCount);

        /**
         * 我关注别人的数量
         *
         * @param isResult
         * @param followCount
         */
        void OnGetFollowCount(boolean isResult, int followCount);

        /**
         * 粉丝数，别人关注我的
         *
         * @param isResult
         * @param followerCount
         */
        void OnGetFollowerCount(boolean isResult, int followerCount);

        void OnGetIsFollow(boolean isResult, boolean isFollowStatus);

        void onGetPlace(boolean isResult, String place);

        void onFollowControl(boolean isResult);
    }


    /**
     * 获取点击的用户播放的数量
     *
     * @param id
     */
    public void getVideoCount(final String id, Context mContext) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", id);
        NetWorkUtil.postForm(mContext, ShopConstant.PERSONAL_VIDEO_COUNT, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    listener.OnGetVideoCount(true, response.optInt("data"));
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                listener.OnGetVideoCount(false, 0);
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                listener.OnGetVideoCount(false, 0);
            }
        }, map);
    }

    /**
     * 获取粉丝数
     *
     * @param id
     */
    public void getFollowerCount(final String id, Context mContext) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", id);
        NetWorkUtil.postForm(mContext, ShopConstant.PERSONAL_FOLLOWER_COUNT, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    listener.OnGetFollowerCount(true, response.optInt("data"));
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                listener.OnGetFollowerCount(false, 0);
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                listener.OnGetFollowerCount(false, 0);
            }
        }, map);
    }

    /**
     * 获取我关注的人数
     *
     * @param id
     */
    public void getFollowCount(final String id, Context mContext) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", id);
        NetWorkUtil.postForm(mContext, ShopConstant.PERSONAL_FOLLOW_COUNT, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    listener.OnGetFollowCount(true, response.optInt("data"));
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                listener.OnGetFollowCount(false, 0);
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                listener.OnGetFollowCount(false, 0);
            }
        }, map);
    }

    public void getIsFollow(String id, Context mContext) {
        Map<String, Object> paramap = new HashMap<String, Object>();
        paramap.put("userId", id);
        NetWorkUtil.postForm(mContext, ShopConstant.ISFOLLOW_CONTROL, new MResponseListener(mContext) {
            @Override
            public void onResponse(JSONObject response) {
                if (response.optInt("error") == 0) {
                    listener.OnGetIsFollow(true, response.optBoolean("data"));
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                listener.OnGetIsFollow(false, false);
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                listener.OnGetIsFollow(false, false);
            }
        }, paramap);
    }

    /**
     * 获取个人信息
     *
     * @param userId
     */
    public void getPlace(String userId, Context mContext) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", userId);
        NetWorkUtil.postForm(mContext, ShopConstant.PERSONAL_INFO,
                new MResponseListener(mContext) {

                    @Override
                    protected void onDataFine(JSONObject data) {
                        String place = data == null ? "中国" : data.optString("place");
                        listener.onGetPlace(true, place);
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        super.onErrorResponse(error);
                        listener.onGetPlace(false, "中国");
                    }

                    @Override
                    protected void onErrorNotZero(int error, String msg) {
                        listener.onGetPlace(false, "中国");
                    }
                }, map);
    }

    public  void followNet(final Context mContext, String followStatus, String userId){
//        if(!UserSharedPreference.isLogin()){
//            Toast.makeText(mContext, "请先登录！", Toast.LENGTH_SHORT).show();
//            return;
//        }
        if(UserSharedPreference.getUserId().equals(userId)){
            Toast.makeText(mContext, "自己不能关注自己！", Toast.LENGTH_SHORT).show();
            return;
        }
        if(OSUtil.isVisitor(userId)){
            Toast.makeText(mContext, "不能关注游客！", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String,Object> paramap = new HashMap<String,Object>();
        paramap.put("myId", UserSharedPreference.getUserId());
        paramap.put("toId", userId);
        paramap.put("type", 1);
        paramap.put("status", followStatus);
        NetWorkUtil.postForm(mContext, ShopConstant.FOLLOW_CONTROL, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    listener.onFollowControl(true);
                } else {
                    listener.onFollowControl(false);
                }
            }

        }, paramap);
    }
}