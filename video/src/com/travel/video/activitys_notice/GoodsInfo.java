package com.travel.video.activitys_notice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import com.travel.Constants;
import com.travel.ShopConstant;
import com.travel.VideoConstant;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.activity.GoodsInfoActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GoodsInfo {

    private Context mContext;
    private String tag;
    private Activity activity;

    private int couponId = 0;
    private NoticeActivity.CouponListener mCouponListener = new NoticeActivity.CouponListener() {


        @Override
        public void onCouponGet() {
            TravelUtil.showToast("恭喜您，领取成功，已放入红钱袋！");
        }

        @Override
        public void onCouponError() {
            TravelUtil.showToast("您已参与过此活动！");
        }
    };

    public GoodsInfo(Context mContext, String tag, Activity activity) {
        this.mContext = mContext;
        this.tag = tag;
        this.activity = activity;
    }

    // 购买小城卡
    @JavascriptInterface
    public void goodsDetail(int goodsId) {
        Intent intent = new Intent();
        intent.setAction(Constants.GOODS_ACTION);
        intent.setType(Constants.VIDEO_TYPE);
        intent.putExtra("goodsId", goodsId + "");
        mContext.startActivity(intent);
    }

    // 领取优惠券
    @JavascriptInterface
    public void getCoupon(int couponId) {
        this.couponId = couponId;
        getCoupon();
    }

    // 领券后立即体验
    @JavascriptInterface
    public void tryBuy() {
        if (TextUtils.equals(tag, "for_result"))
            activity.finish();
        else {
            Intent intent = new Intent();
            intent.setAction(Constants.STORY_LIST_USE);
            mContext.startActivity(intent);
        }
    }

    public void getCoupon() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", UserSharedPreference.getUserId());
        map.put("couponId", couponId);
        NetWorkUtil.postForm(mContext, VideoConstant.COUPON_GET, new MResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    mCouponListener.onCouponGet();
                }
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                mCouponListener.onCouponError();
            }
        }, map);
    }

    // 根据StoryId进入故事详情
    @JavascriptInterface
    public void showStory(String storyId) {
        GoodsInfoActivity.actionStart(mContext, storyId, 1);
    }

    // 给城市投票
    @JavascriptInterface
    public void addVote(String cityId) {
        voteCity(cityId);
    }

    private void voteCity(final String cityId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("cityId", cityId);
        NetWorkUtil.postForm(mContext, ShopConstant.CITY_VOTE, new MResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    if (voteListener != null) {
                        voteListener.onSuccess(cityId);
                        TravelUtil.showToast("投票成功！");
                    }
                }
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                TravelUtil.showToast("您已参与过投票!");
            }
        }, map);
    }

    public interface VoteListener {
        /**
         * 投票成功
         */
        void onSuccess(String cityId);
    }

    private VoteListener voteListener;

    public void setVoteListener(VoteListener voteListener) {
        this.voteListener = voteListener;
    }


    // 根据StoryId进入故事详情
    @JavascriptInterface
    public void sendCoupon(String couponIds) {
        Map<String, Object> map = new HashMap<>();
        map.put("couponIds", couponIds);
        NetWorkUtil.postForm(mContext, ShopConstant.THOUSAND_CITY_COUPON, new MResponseListener() {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    TravelUtil.showToast(response.optString("msg"));
                }
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                super.onErrorNotZero(error, msg);
            }
        }, map);
    }
}
