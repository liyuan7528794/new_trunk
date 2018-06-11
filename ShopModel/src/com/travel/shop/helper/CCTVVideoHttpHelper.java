package com.travel.shop.helper;

import android.content.Context;
import android.text.TextUtils;

import com.travel.ShopConstant;
import com.travel.bean.CCTVVideoInfoBean;
import com.travel.bean.EvaluateInfoBean;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * cctv视频相关接口
 * Created by wyp on 2017/12/5.
 */

public class CCTVVideoHttpHelper {

    private static Map<String, Object> map;

    // 获取全视频
    public interface CCTVAllVideoListener {
        void onSuccessGet(ArrayList<CCTVVideoInfoBean> videos);
    }

    public static void getvideoList(Context mContext, final CCTVAllVideoListener mListener) {
        map = new HashMap<>();
        NetWorkUtil.postForm(mContext, ShopConstant.CCTV_ALL_VIDEO, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                ArrayList<CCTVVideoInfoBean> videos = new ArrayList<>();
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject cctvObject = data.getJSONObject(i);
                        videos.add(CCTVVideoInfoBean.getJson(cctvObject));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mListener.onSuccessGet(videos);
                }

            }
        }, map);
    }

    // 获取视频标签
    public interface CCTVVideoTypeListener {
        void onSuccessGet(ArrayList<HashMap<String, String>> videos);
    }

    public static void getVideoTypeList(Context mContext, final CCTVVideoTypeListener mListener) {
        map = new HashMap<>();
        NetWorkUtil.postForm(mContext, ShopConstant.CCTV_VIDEO_TYPE, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                ArrayList<HashMap<String, String>> labels = new ArrayList<>();
                try {
                    for (int i = 0; i < data.length(); i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        JSONObject labelObject = data.getJSONObject(i);
                        map.put("id", labelObject.optString("id"));
                        map.put("name", labelObject.optString("name"));
                        labels.add(map);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mListener.onSuccessGet(labels);
                }

            }
        }, map);
    }

    // 获取短视频
    public interface CCTVSmallVideoListener {
        void onSuccessGet(ArrayList<CCTVVideoInfoBean> videos, int position);
    }

    public static void getSmallVideoList(Context mContext, int type, String parentId, final CCTVSmallVideoListener mListener, int page) {
        getSmallVideoList(mContext, type, parentId, mListener, -1, page);
    }

    public static void getSmallVideoList(Context mContext, int type, String parentId, final CCTVSmallVideoListener mListener, final int position, int page) {
        map = new HashMap<>();
        if (type != 0)
            map.put("type", type);
        if (!TextUtils.isEmpty(parentId))
            map.put("parentId", parentId);
        map.put("pageNo", page);
        NetWorkUtil.postForm(mContext, ShopConstant.CCTV_SMALL_VIDEO, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                ArrayList<CCTVVideoInfoBean> videos = new ArrayList<>();
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject cctvObject = data.getJSONObject(i);
                        videos.add(CCTVVideoInfoBean.getJson(cctvObject));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mListener.onSuccessGet(videos, position);
                }

            }
        }, map);
    }

    public interface CCTVVideoLikeListener {
        // 获取是否点赞
        void onSuccessGet(boolean isLike);

        // 点赞操作
        void onLikeClick();

        // 获取视频点赞数
        void onSuccessGetLikeCount(int count);
    }

    public static void getVideoLike(Context mContext, String videoId, final CCTVVideoLikeListener mListener) {
        map = new HashMap<>();
        map.put("shortVideoId", videoId);
        NetWorkUtil.postForm(mContext, ShopConstant.CCTV_VIDEO_LIKE_GET, new MResponseListener(mContext) {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    boolean isLike = response.optBoolean("data");
                    mListener.onSuccessGet(isLike);
                }
            }
        }, map);
    }

    public static void onLikeClick(Context mContext, String videoId, int type, final CCTVVideoLikeListener mListener) {
        map = new HashMap<>();
        map.put("shortVideoId", videoId);
        map.put("type", type);// 1:投票 2:取消投票
        NetWorkUtil.postForm(mContext, ShopConstant.CCTV_VIDEO_LIKE_CLICK, new MResponseListener(mContext) {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    mListener.onLikeClick();
                }
            }
        }, map);
    }

    public static void getVideoLikeCount(Context mContext, String videoId, final CCTVVideoLikeListener mListener) {
        map = new HashMap<>();
        map.put("cityVideoId", videoId);
        NetWorkUtil.postForm(mContext, ShopConstant.CCTV_VIDEO_LIKE_COUNT, new MResponseListener(mContext) {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    int count = response.optInt("data");
                    mListener.onSuccessGetLikeCount(count);
                }
            }
        }, map);
    }

    public interface CCTVVideoEvaluateListener {
        // 获取视频评论列表
        void onSuccessGet(ArrayList<EvaluateInfoBean> evaluates);

        // 获取视频评论个数
        void onSuccessGetCount(int count);

        // 发表视频评论
        void onSuccessSend(String content);
    }

    public static void getVideoEvaluateList(Context mContext, String videoId, final CCTVVideoEvaluateListener mListener, int page) {
        map = new HashMap<>();
        map.put("cityVideoId", videoId);
        map.put("pageNo", page);
        NetWorkUtil.postForm(mContext, ShopConstant.CCTV_VIDEO_EVALUATE, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                ArrayList<EvaluateInfoBean> list = new ArrayList<>();
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject dataObject = data.getJSONObject(i);
                        EvaluateInfoBean mEvaluateInfoBean = new EvaluateInfoBean();
                        // 评论Id
                        mEvaluateInfoBean.setStoryCommentId(dataObject.optString("id"));
                        // 评论内容
                        mEvaluateInfoBean.setEvaluateContent(dataObject.optString("content"));
                        // 评论时间
                        mEvaluateInfoBean.setEvaluateTime(dataObject.optString("submitTime"));
                        JSONObject userObject = dataObject.getJSONObject("user");
                        // 头像
                        mEvaluateInfoBean.setEvaluateUserPhoto(userObject.optString("imgUrl"));
                        // 名字
                        mEvaluateInfoBean.setEvaluateUserName(userObject.optString("nickName"));
                        list.add(mEvaluateInfoBean);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mListener.onSuccessGet(list);
                }
            }
        }, map);
    }

    public static void getEvaluateCount(Context mContext, String videoId, final CCTVVideoEvaluateListener mListener) {
        map = new HashMap<>();
        map.put("cityVideoId", videoId);
        NetWorkUtil.postForm(mContext, ShopConstant.CCTV_VIDEO_EVALUATE_COUNT, new MResponseListener(mContext) {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    int count = response.optInt("data");
                    mListener.onSuccessGetCount(count);
                }
            }
        }, map);
    }

    public static void onSendEvaluate(Context mContext, String videoId, final String content, final CCTVVideoEvaluateListener mListener) {
        map = new HashMap<>();
        map.put("cityVideoId", videoId);
        map.put("content", content);
        NetWorkUtil.postForm(mContext, ShopConstant.CCTV_VIDEO_EVALUATE_SEND, new MResponseListener(mContext) {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    mListener.onSuccessSend(content);
                }
            }
        }, map);
    }
}
