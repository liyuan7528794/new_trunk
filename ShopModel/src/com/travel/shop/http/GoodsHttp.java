package com.travel.shop.http;

import android.content.Context;

import com.travel.ShopConstant;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.GoodsDetailBean;
import com.travel.bean.GoodsOtherInfoBean;
import com.travel.bean.GoodsServiceBean;
import com.travel.bean.PersonalInfoBean;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.bean.CouponInfoBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * 商品详情页的网络获取
 * Created by wyp on 2017/11/6.
 */

public class GoodsHttp {
    private static HashMap<String, Object> map;

    public interface GoodsInfoListener {

        // 获取到商品信息
        void getGoodsData(GoodsDetailBean mGoodsDetailBean);

        // 商品下架
        void onErrorNotZero();
    }

    /**
     * 商品信息的获取
     */
    public static void getGoodsData(final Context mContext, String goodsId, final GoodsInfoListener mListener) {
        map = new HashMap<>();
        map.put("goodsId", goodsId);
        NetWorkUtil.postForm(mContext, ShopConstant.GOODS_INFO, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONObject data) {
                GoodsDetailBean mGoodsDetailBean = new GoodsDetailBean();
                GoodsBasicInfoBean mGoodsBasicInfoBean = new GoodsBasicInfoBean();
                GoodsOtherInfoBean mGoodsOtherInfoBean = new GoodsOtherInfoBean();
                PersonalInfoBean mPersonalInfoBean = new PersonalInfoBean();
                // 是否可以使用小城卡
                mGoodsBasicInfoBean.setSupportCard(data.optInt("productType") == 0 ? false : true);
                // 小城卡剩余数量
                mGoodsBasicInfoBean.setRemainCount(data.optInt("goodsNumber"));
                // goosId
                mGoodsBasicInfoBean.setGoodsId(data.optString("id"));
                // 标题
                mGoodsBasicInfoBean.setGoodsTitle(data.optString("goodsName"));
                // 价格
                mGoodsBasicInfoBean.setGoodsPrice(data.optString("price"));
                // 活动Id
                mGoodsBasicInfoBean.setActivityId(data.optInt("activityId"));
                // 活动名称
                mGoodsBasicInfoBean.setActivityName(data.optString("activityName"));
                // 商品图片
                mGoodsBasicInfoBean.setGoodsImg(data.optString("imgUrl"));
                // 商品类型
                mGoodsBasicInfoBean.setGoodsType(data.optInt("type"));
                //                if (data.optInt("type") == 1 || data.optInt("type") == 2)
                //                    // 商品出发地
                //                    mGoodsBasicInfoBean.setGoodsAddress(data.optString("place"));
                //                else
                // 商品目的地
                mGoodsBasicInfoBean.setGoodsAddress(data.optString("destCity"));
                try {
                    // 产品特色/行程安排
                    ArrayList<GoodsServiceBean> mTravelPlans = new ArrayList<>();
                    JSONArray goodsItemsListArray = data.optJSONArray("goodsItemsList");
                    if (goodsItemsListArray != null)
                        for (int i = 0; i < goodsItemsListArray.length(); i++) {
                            GoodsServiceBean gsb = new GoodsServiceBean();
                            JSONObject goodsItemsListObject = goodsItemsListArray.getJSONObject(i);
                            gsb.setType(goodsItemsListObject.optInt("type"));
                            // 内容（文字，标题，图片和音视频地址）
                            gsb.setContent(goodsItemsListObject.optString("content"));
                            if (goodsItemsListObject.optInt("type") == 2) {
                                if (goodsItemsListObject.optString("content").contains("?")) {
                                    String params = goodsItemsListObject.optString("content").split("[?]")[1];
                                    gsb.setWidth(Integer.parseInt(params.split("_")[0]));
                                    gsb.setHeight(Integer.parseInt(params.split("_")[1]));
                                } else {
                                    gsb.setWidth(3);
                                    gsb.setHeight(2);

                                }
                            }
                            if (goodsItemsListObject.optInt("type") == 3)
                                gsb.setTitle(goodsItemsListObject.optString("title"));
                            if (goodsItemsListObject.optInt("type") == 4)
                                gsb.setBackImage(goodsItemsListObject.optString("img"));
                            mTravelPlans.add(gsb);
                        }
                    mGoodsOtherInfoBean.setTravelPlans(mTravelPlans);
                    // 卖家Id
                    JSONObject userObject = data.getJSONObject("user");
                    mPersonalInfoBean.setUserId(userObject.optString("id"));
                    // 费用说明
                    mGoodsOtherInfoBean.setCostImplications(data.optString("priceExplain"));
                    // 预定须知
                    mGoodsOtherInfoBean.setBookingNotes(data.optString("returnsExplain"));
                    // 商品是否需要二次确认
                    mGoodsBasicInfoBean.setTwiceSure(data.optInt("isSure"));
                    // 商品预定天数
                    mGoodsBasicInfoBean.setGoodsReserveDays(data.optInt("reserve"));
                    // 需填写的人员信息 0: 不需要 1：需要
                    mGoodsBasicInfoBean.setInfoNeed(data.optString("tripInfo"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mGoodsDetailBean.setGoodsBasicInfoBean(mGoodsBasicInfoBean);
                    mGoodsDetailBean.setGoodsOtherInfoBean(mGoodsOtherInfoBean);
                    mGoodsDetailBean.setPersonalInfoBean(mPersonalInfoBean);
                    mListener.getGoodsData(mGoodsDetailBean);
                }
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                mListener.onErrorNotZero();
            }
        }, map);
    }

    public interface CardInfoListener {

        // 获取到是否购买过小城卡
        void isBuyCard(boolean isBuyCard, int count, String date);

        // 获取小城卡id
        void getCardId(String cardId);

    }

    /**
     * 是否购买过小城卡
     */
    public static void isBuyCard(final Context mContext, final CardInfoListener mListener) {
        map = new HashMap<>();
        NetWorkUtil.postForm(mContext, ShopConstant.IS_BUY_CARD, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 1) {
                    mListener.isBuyCard(false, 0, "");
                } else {
                    int count = 0;
                    String date = "";
                    try {
                        JSONObject object = response.getJSONObject("data");
                        count = object.optInt("countNum");
                        long lDate = object.optLong("endDate");
                        date = DateFormatUtil.formatTime(new Date(lDate), DateFormatUtil.FORMAT_DATE);
                        mListener.isBuyCard(true, count, date);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        mListener.isBuyCard(true, count, date);
                    }
                }
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
            }

        }, map);
    }

    /**
     * 小城卡id
     */
    public static void getCardId(final Context mContext, final CardInfoListener mListener) {
        map = new HashMap<>();
        NetWorkUtil.postForm(mContext, ShopConstant.CARD_ID, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    String id = response.optString("data");
                    mListener.getCardId(id);
                }
            }
        }, map);
    }


    public interface CouponInfoListener {

        // 获取商品支持优惠券
        void getGoodsSupportCoupon(ArrayList<CouponInfoBean> coupons);

        // 获取到是否领了某优惠券
        void isGetCoupon(boolean isGetCoupon, int i);

        // 获取到是否领了某优惠券
        void getCouponSuccess(int position);

    }

    /**
     * 获取商品支持优惠券
     */
    public static void getGoodsSupportCoupon(final Context mContext, String goodsId, final CouponInfoListener mListener) {
        map = new HashMap<>();
        map.put("goodsId", goodsId);
        NetWorkUtil.postForm(mContext, ShopConstant.GOODS_SUPPORT_COUPON, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                if (data != null && data.length() != 0) {
                    ArrayList<CouponInfoBean> coupons = new ArrayList<>();
                    try {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject couponObject = data.getJSONObject(i);
                            CouponInfoBean couponInfoBean = new CouponInfoBean();
                            // 优惠券id
                            couponInfoBean.setCouponId(couponObject.optString("id"));
                            // 优惠券名称
                            couponInfoBean.setCouponName(couponObject.optString("discountName"));
                            coupons.add(couponInfoBean);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        mListener.getGoodsSupportCoupon(coupons);
                    }
                }
            }
        }, map);
    }

    /**
     * 是否获取过某优惠券
     */
    public static void isGetCoupon(final Context mContext, String couponId, final int i, final CouponInfoListener mListener) {
        map = new HashMap<>();
        map.put("couponId", couponId);
        map.put("uid", UserSharedPreference.getUserId());
        NetWorkUtil.postForm(mContext, ShopConstant.IS_GET_COUPON, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0)
                    mListener.isGetCoupon(false, i);
                else
                    mListener.isGetCoupon(true, i);
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
            }
        }, map);
    }

    /**
     * 领取优惠券
     */
    public static void getCoupon(final Context mContext, String couponId, final int position, final CouponInfoListener mListener) {
        map = new HashMap<>();
        map.put("couponId", couponId);
        NetWorkUtil.postForm(mContext, ShopConstant.GET_COUPON, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0)
                    mListener.getCouponSuccess(position);
            }

        }, map);
    }

}
