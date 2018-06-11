package com.travel.shop.http;

import android.content.Context;

import com.travel.ShopConstant;
import com.travel.bean.EvaluateInfoBean;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.GoodsDetailBean;
import com.travel.bean.GoodsOtherInfoBean;
import com.travel.bean.GoodsServiceBean;
import com.travel.bean.PersonalInfoBean;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.UserSharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 故事页相关的网络请求
 * Created by wyp on 2017/1/19.
 */

public class GoodsInfoHttp {

    public interface GoodsInfoListener {
        // 获取是否被收藏
        void getIsNotCollect(boolean isCollected);

        // 故事收藏操作
        void collectControl();

        // 获取故事详情数据
        void getStoryInfo(GoodsDetailBean mGoodsDetailBean);

        // 获取故事评论数据
        void getStoryComment(ArrayList<EvaluateInfoBean> comments);

        // 发表评论成功
        void sendSuccess(String comment, int commentId);

        // 点赞成功
        void likeSuccess();
    }

    private static HashMap<String, Object> map;

    /**
     * 获取该故事是否已被收藏
     */
    public static void getIsNotCollect(String storyId, final Context mContext, final GoodsInfoListener mListener) {
        if (UserSharedPreference.isLogin()) {
            map = new HashMap<>();
            map.put("storyId", storyId);
            map.put("userId", UserSharedPreference.getUserId());
            NetWorkUtil.postForm(mContext, ShopConstant.STORY_ISATTENTION, new MResponseListener(mContext) {

                @Override
                public void onResponse(JSONObject response) {
                    super.onResponse(response);
                    if (response.optInt("error") == 0)
                        mListener.getIsNotCollect(response.optBoolean("data"));
                }
            }, map);
        } else
            mListener.getIsNotCollect(false);
    }

    /**
     * 获取故事详情数据
     */
    public static void getStoryInfo(final Context mContext, String storyId, final GoodsInfoListener mListener) {
        map = new HashMap<>();
        map.put("id", storyId);
        NetWorkUtil.postForm(mContext, ShopConstant.STORY_INFO, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONObject data) {
                if (data != null) {
                    GoodsDetailBean mGoodsDetailBean = new GoodsDetailBean();
                    GoodsBasicInfoBean mGoodsBasicInfoBean = new GoodsBasicInfoBean();
                    GoodsOtherInfoBean mGoodsOtherInfoBean = new GoodsOtherInfoBean();
                    PersonalInfoBean mPersonalInfoBean = new PersonalInfoBean();
                    // 此故事关联的商品Id
                    mGoodsBasicInfoBean.setGoodsId(data.optString("goodsId"));
                    // 此故事的类型
                    mGoodsBasicInfoBean.setType(data.optInt("type"));
                    // 故事封面
                    mGoodsBasicInfoBean.setGoodsImg(data.optString("imgUrl"));
                    // 标题
                    mGoodsBasicInfoBean.setGoodsTitle(data.optString("title"));
                    // 副标题
                    mGoodsBasicInfoBean.setSubhead(data.optString("subhead"));
                    // h5地址
                    mGoodsBasicInfoBean.setDescriptionUrl(data.optString("descriptionUrl"));
                    // 富文本数据 （type=3显示此数据）
                    mGoodsBasicInfoBean.setIntroduceGoods(data.optString("content"));
                    // 纪录片同款
                    mGoodsBasicInfoBean.setContent(data.optString("introduceGoods"));
                    mGoodsDetailBean.setGoodsBasicInfoBean(mGoodsBasicInfoBean);
                    // 故事内容
                    try {
                        if (!data.isNull("list")) {
                            ArrayList<GoodsServiceBean> list = new ArrayList<>();
                            JSONArray listArray = data.getJSONArray("list");
                            for (int i = 0; i < listArray.length(); i++) {
                                GoodsServiceBean mGoodsServiceBean = new GoodsServiceBean();
                                JSONObject listObject = listArray.getJSONObject(i);
                                // 类型
                                mGoodsServiceBean.setType(listObject.optInt("type"));
                                // 内容（文字，标题，图片和音视频地址）
                                mGoodsServiceBean.setContent(listObject.optString("content"));
                                if (listObject.optInt("type") == 2) {
                                    if (listObject.optString("content").contains("?")) {
                                        String params = listObject.optString("content").split("[?]")[1];
                                        mGoodsServiceBean.setWidth(Integer.parseInt(params.split("_")[0]));
                                        mGoodsServiceBean.setHeight(Integer.parseInt(params.split("_")[1]));
                                    } else {
                                        mGoodsServiceBean.setWidth(3);
                                        mGoodsServiceBean.setHeight(2);
                                    }
                                }
                                if (listObject.optInt("type") == 3) {
                                    mGoodsServiceBean.setTitle(listObject.optString("title"));
                                    mGoodsServiceBean.setTime(listObject.optString("time"));
                                }
                                if (listObject.optInt("type") == 4) {
                                    mGoodsServiceBean.setBackImage(listObject.optString("img"));
                                    mGoodsServiceBean.setTitle(listObject.optString("title"));
                                }
                                // 6是顶部第一个视频 7是推荐的两条数据
                                if (listObject.optInt("type") == 6 || listObject.optInt("type") == 7) {
                                    mGoodsServiceBean.setBackImage(listObject.optString("img"));
                                    mGoodsServiceBean.setTitle(listObject.optString("title"));
                                }
                                list.add(mGoodsServiceBean);
                            }
                            mGoodsOtherInfoBean.setTravelPlans(list);
                        }
                        // 发布故事的UserId
                        mPersonalInfoBean.setUserId(data.optString("userId"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        mGoodsDetailBean.setGoodsOtherInfoBean(mGoodsOtherInfoBean);
                        mGoodsDetailBean.setPersonalInfoBean(mPersonalInfoBean);
                        mListener.getStoryInfo(mGoodsDetailBean);
                    }
                } else {
                    mListener.getStoryInfo(null);
                }
            }
        }, map);
    }

    /**
     * 获取故事评论数据
     */
    public static void getStoryComment(final Context mContext, String storyId, int page, final GoodsInfoListener mListener) {
        map = new HashMap<>();
        map.put("storyId", storyId);
        map.put("pageNo", page);
        NetWorkUtil.postForm(mContext, ShopConstant.STORY_COMMENT, new MResponseListener(mContext) {

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
                        mEvaluateInfoBean.setEvaluateTime(dataObject.optString("createTime"));
                        // 赞数
                        mEvaluateInfoBean.setLikeCount(dataObject.optInt("praiseNum"));
                        // 是否点赞
                        //                        mEvaluateInfoBean.setLike(dataObject.optBoolean(""));
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
                    mListener.getStoryComment(list);
                }
            }
        }, map);
    }

    /**
     * 发送评论
     */
    public static void sendComment(final Context mContext, String storyId, final String content, final GoodsInfoListener mListener) {
        map = new HashMap<>();
        map.put("userId", UserSharedPreference.getUserId());
        map.put("storyId", storyId);
        map.put("content", content);
        NetWorkUtil.postForm(mContext, ShopConstant.SEND_COMMENT, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0)
                    mListener.sendSuccess(content, response.optInt("data"));
            }
        }, map);
    }

    /**
     * 点赞
     */
    public static void sendLike(final Context mContext, String storyCommentId, final GoodsInfoListener mListener) {
        map = new HashMap<>();
        map.put("storyCommentId", storyCommentId);
        NetWorkUtil.postForm(mContext, ShopConstant.STORY_COMMENT_LIKE, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0)
                    mListener.likeSuccess();
            }
        }, map);
    }

    /**
     * 收藏故事
     */
    public static void collectControl(final Context mContext, String storyId, boolean isStoryCollect, final GoodsInfoListener mListener) {
        map = new HashMap<>();
        map.put("storyId", storyId);
        map.put("userId", UserSharedPreference.getUserId());
        map.put("status", isStoryCollect ? 2 : 1);// 1：收藏 2：取消
        NetWorkUtil.postForm(mContext, ShopConstant.STORY_ATTENTION, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    mListener.collectControl();
                }
            }
        }, map);
    }

}
