package com.travel.shop.http;

import android.content.Context;
import android.text.TextUtils;

import com.travel.ShopConstant;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.GoodsDetailBean;
import com.travel.bean.GoodsOtherInfoBean;
import com.travel.bean.GoodsServiceBean;
import com.travel.bean.NotifyBean;
import com.travel.bean.PersonalInfoBean;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.bean.AttachGoodsBean;
import com.travel.shop.bean.CalendarBean;
import com.travel.shop.bean.CommitBean;
import com.travel.shop.bean.CouponInfoBean;
import com.travel.shop.tools.ShopTool;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2016/10/17.
 */

public class CommitHttp {

    public interface CommitOrderListener {
        // 获取附加服务
        void getAttachGoods(ArrayList<AttachGoodsBean> goodsList);

        // 获取到商品信息
        void getGoodsData(GoodsDetailBean mGoodsDetailBean);

        // 商品下架
        void onErrorNotZero();

        // 获取到套餐信息
        void getPackageData(ArrayList<HashMap<String, Object>> packages);

        // 获取到日历信息
        void getCalendarData(ArrayList<CalendarBean> orderData);
    }

    public interface CouponListener {

        // 获取到卡券信息
        void getCouponData(ArrayList<CouponInfoBean> couponData);
    }

    public interface CommitListener {

        void getOrdersId(long ordersId);
    }

    private static HashMap<String, Object> map;

    /**
     * 商品信息的获取
     */
    public static void getGoodsData(final Context mContext, String goodsId, final CommitOrderListener mListener) {
        map = new HashMap<>();
        map.put("goodsId", goodsId);
        NetWorkUtil.postForm(mContext, ShopConstant.GOODS_INFO, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONObject data) {
                GoodsDetailBean mGoodsDetailBean = new GoodsDetailBean();
                GoodsBasicInfoBean mGoodsBasicInfoBean = new GoodsBasicInfoBean();
                GoodsOtherInfoBean mGoodsOtherInfoBean = new GoodsOtherInfoBean();
                PersonalInfoBean mPersonalInfoBean = new PersonalInfoBean();
                try {
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
                    if (data.optInt("type") == 1 || data.optInt("type") == 2)
                        // 商品出发地
                        mGoodsBasicInfoBean.setGoodsAddress(data.optString("place"));
                    else
                        // 商品目的地
                        mGoodsBasicInfoBean.setGoodsAddress(data.optString("destCity"));
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
                    // 需填写的人员信息
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


    /**
     * 套餐的获取
     */
    public static void getPackagData(final Context mContext, String goodsId, final CommitOrderListener mListener) {
        map = new HashMap<>();
        map.put("goodsId", goodsId);
        NetWorkUtil.postForm(mContext.getApplicationContext(), ShopConstant.COMMIT_ORDER_BY_PEOPLE_SET, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                ArrayList<HashMap<String, Object>> packages = new ArrayList<>();
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject dataObject = data.getJSONObject(i);
                        HashMap<String, Object> dataMap = new HashMap<>();
                        dataMap.put("name", dataObject.opt("setName"));
                        dataMap.put("id", dataObject.opt("id"));
                        dataMap.put("isChecked", i == 0 ? true : false);
                        packages.add(dataMap);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mListener.getPackageData(packages);
                }
            }
        }, map);
    }

    /**
     * 获取日历数据
     */
    public static void getCalendarData(final Context mContext, String goodsId, final String packageId, int days, final CommitOrderListener mListener) {
        map = new HashMap<>();
        map.put("goodsId", goodsId);
        map.put("setId", packageId);
        map.put("reserveTime", days);
        NetWorkUtil.postForm(mContext.getApplicationContext(), ShopConstant.COMMIT_ORDER_BY_PEOPLE, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                ArrayList<CalendarBean> orderData = new ArrayList<>();
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject dataObject = data.getJSONObject(i);
                        CalendarBean cb = new CalendarBean();
                        // 日历Id
                        cb.setCalendarId(dataObject.optString("id"));
                        // 商品Id
                        cb.setGoodsId(dataObject.optString("goodsId"));
                        // 出发日期
                        cb.setDate(dataObject.optString("startDate"));
                        // 成人价
                        cb.setAdult_price(dataObject.optString("adultsPrice"));
                        // 儿童价
                        cb.setChildren_price(dataObject.optString("childrenPrice"));
                        // 单房差
                        cb.setSingle_room_price(dataObject.optString("roomPrice"));
                        // 年份
                        cb.setYear(cb.getDate().substring(0, 4));
                        // 月份
                        String month = cb.getDate().substring(5, 7);
                        cb.setMonth("0".equals(month.substring(0, 1)) ? month.substring(1, 2) : month);
                        // 下标数
                        cb.setIndex(i);
                        // 套餐Id
                        cb.setPackageId(packageId);
                        orderData.add(cb);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mListener.getCalendarData(orderData);
                }
            }
        }, map);

    }

    /**
     * 获取卡券数据
     *
     * @param mContext
     * @param status    0：未使用 1：已使用 2：已过期 -1:相当于不传此参数
     * @param mListener
     */
    public static void getCouponData(final Context mContext, int status, final CouponListener mListener) {
        map = new HashMap<>();
        map.put("userId", UserSharedPreference.getUserId());
        if (status != -1)
            map.put("status", status);
        NetWorkUtil.postForm(mContext.getApplicationContext(), ShopConstant.COUPON_LIST, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                ArrayList<CouponInfoBean> couponData = new ArrayList<>();
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject dataObject = data.getJSONObject(i);
                        CouponInfoBean coupon = new CouponInfoBean();
                        // id
                        coupon.setCouponId(dataObject.optString("id"));
                        // 卡券状态
                        coupon.setStatus(dataObject.optInt("status"));
                        // 是否是赠品
                        coupon.setPresent(dataObject.optInt("type") == 0 ? true : false);
                        JSONObject couponObject = dataObject.getJSONObject("coupon");
                        // 可用状态
                        coupon.setStatusCoupon(couponObject.optInt("status"));
                        // 需满足的使用金额
                        coupon.setRequirMoney(couponObject.optString("requireMoney"));
                        // 名字
                        coupon.setCouponName(couponObject.optString("discountName"));
                        // 现价
                        coupon.setCurrentPrice(ShopTool.getMoney(couponObject.optString("privilege")));
                        // 开始日期
                        coupon.setStartDate(dataObject.optString("startDate"));
                        // 结束日期
                        coupon.setEndDate(TextUtils.isEmpty(dataObject.optString("endDate")) ? "——" : dataObject.optString("endDate"));
                        // 可使用的商品Id
                        JSONArray goodsIdsArray = couponObject.getJSONArray("goodsIds");
                        ArrayList<String> goodsIds = new ArrayList<>();
                        for (int j = 0; j < goodsIdsArray.length(); j++) {
                            goodsIds.add(goodsIdsArray.opt(j).toString());
                        }
                        coupon.setGoodsIds(goodsIds);
                        // 是否可叠加
                        coupon.setAdded(couponObject.optInt("superposition"));
                        couponData.add(coupon);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mListener.getCouponData(couponData);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                mListener.getCouponData(new ArrayList<CouponInfoBean>());
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                super.onErrorNotZero(error, msg);
                mListener.getCouponData(new ArrayList<CouponInfoBean>());
            }
        }, map);

    }

    /**
     * 提交订单，获取订单号
     */
    public static void commintOrder(CommitBean cb, final Context mContext, final CommitListener mListener) {
        map = new HashMap<>();
        if (cb.getAttachGoods() != null && cb.getAttachGoods().size() != 0) {
            ArrayList<HashMap<String, Object>> commitAttachGoods = new ArrayList<>();
            for (AttachGoodsBean goodsBean : cb.getAttachGoods()) {
                if (goodsBean.getCount() != 0) {
                    HashMap<String, Object> goods = new HashMap<>();
                    goods.put("attachId", goodsBean.getAttachId());
                    goods.put("num", goodsBean.getCount());
                    commitAttachGoods.add(goods);
                }
            }
            if (commitAttachGoods.size() != 0)
                map.put("attachs", ShopTool.mapToJson(commitAttachGoods));
        }
        if (!TextUtils.isEmpty(cb.getSalesCode()))
            map.put("salesCode", cb.getSalesCode());
        map.put("goodsId", cb.getGoodsId());
        if (cb.getGoodsType() == 3 || cb.getGoodsType() == 4) {
            map.put("skuIds", cb.getSkuIds());
            map.put("returnTime", cb.getReturnTime());
        } else if (cb.getGoodsType() != 6)
            map.put("skuId", cb.getSkuId());// 日历id
        if (cb.getGoodsType() != 6) {
            map.put("priceExplain", cb.getPackageName());
            map.put("departTime", cb.getDepartTime());
        }
        map.put("userCoupnoIds", cb.getUserCoupnoIds());
        map.put("adultNum", cb.getAdultNum());
        if (cb.getGoodsType() == 1 || cb.getGoodsType() == 2) {
            map.put("childNum", cb.getChildNum());
            map.put("roomNum", cb.getRoomNum());
        }
        map.put("buyerName", cb.getBuyerName());
        map.put("buyerTelephone", cb.getBuyerTelephone());
        map.put("remarks", cb.getRemarks());
        if (!TextUtils.isEmpty(cb.getInvitationCode())) {
            map.put("invitationCode", cb.getInvitationCode());
            if (TextUtils.equals("1", cb.getSellerId()) || TextUtils.equals("21", cb.getSellerId())) {
                map.put("sellerDiscountPrice", 0);// 卖家优惠
                map.put("terraceDiscountPrice", new DecimalFormat("##0.00").format(cb.getTotalPrice() * 0.01f));// 平台优惠
            }
        }
        map.put("sourceType", cb.getSourceType());
        map.put("sourceId", cb.getSourceId());
        if (cb.getUserinfo() != null)
            map.put("userinfo", cb.getUserinfo());
        if (!ShopTool.containsEmoji(map.get("buyerName").toString())) {
            NetWorkUtil.postForm(mContext.getApplicationContext(), ShopConstant.COMMIT, new MResponseListener(mContext) {

                @Override
                public void onResponse(JSONObject response) {
                    super.onResponse(response);
                    // 提交订单成功
                    if (response.optInt("error") == 0) {
                        mListener.getOrdersId(response.optLong("data"));
                        ShopSqliteOpenHelper mSqlite = new ShopSqliteOpenHelper(mContext, 0, "");
                        mSqlite.delete(ShopSqliteOpenHelper.TABLENAME_PEOPLE_INFO, "all");
                    }
                }
            }, map);
        } else
            TravelUtil.showToast("买家姓名不可包含表情字符!!!");
    }

    /**
     * 提交订单，获取订单号
     */
    private static String url;

    public static void commintOrderNew(CommitBean cb, final Context mContext, final CommitListener mListener) {
        map = new HashMap<>();
        int goodsType = cb.getGoodsType();
        map.put("goodsId", cb.getGoodsId());
        map.put("adultNum", cb.getAdultNum());
        if (goodsType != 6) {
            map.put("skuId", cb.getSkuId());// 日历id
            map.put("roomNum", cb.getRoomNum());
            map.put("setName", cb.getPackageName());
        }
        map.put("buyerName", cb.getBuyerName());
        map.put("buyerTelephone", cb.getBuyerTelephone());
        map.put("remarks", TextUtils.isEmpty(cb.getRemarks()) ? "" : cb.getRemarks());
        if (!TextUtils.isEmpty(cb.getUserinfo()))
            map.put("userinfo", cb.getUserinfo());
        if (!TextUtils.isEmpty(cb.getUserCoupnoIds()))
            map.put("discountCoupon", cb.getUserCoupnoIds());
        if (cb.getAttachGoods() != null && cb.getAttachGoods().size() != 0) {
            ArrayList<HashMap<String, Object>> commitAttachGoods = new ArrayList<>();
            for (AttachGoodsBean goodsBean : cb.getAttachGoods()) {
                if (goodsBean.getCount() != 0) {
                    HashMap<String, Object> goods = new HashMap<>();
                    goods.put("attachId", goodsBean.getAttachId());
                    goods.put("num", goodsBean.getCount());
                    commitAttachGoods.add(goods);
                }
            }
            if (commitAttachGoods.size() != 0)
                map.put("attachs", ShopTool.mapToJson(commitAttachGoods));
        }
        map.put("cityCard", cb.getCardUseCount());
        if (!ShopTool.containsEmoji(map.get("buyerName").toString())) {
            if (goodsType == 1 || goodsType == 2 || goodsType == 5) {
                url = ShopConstant.SINGLE_COMMIT_ORDER;
            } else if (goodsType == 3 || goodsType == 4) {
                url = ShopConstant.MULTIPLE_COMMIT_ORDER;
            } else if (goodsType == 6) {
                url = ShopConstant.CARD_COMMIT_ORDER;
            }
            NetWorkUtil.postForm(mContext.getApplicationContext(), url, new MResponseListener(mContext) {

                @Override
                public void onResponse(JSONObject response) {
                    super.onResponse(response);
                    // 提交订单成功
                    if (response.optInt("error") == 0) {
                        mListener.getOrdersId(response.optLong("data"));
                    }
                }
            }, map);
        } else
            TravelUtil.showToast("买家姓名不可包含表情字符!!!");
    }

    /**
     * 附加服务的获取
     */
    public static void getAttachGoods(final Context mContext, String goodsId, final CommitOrderListener mListener) {
        map = new HashMap<>();
        map.put("goodsId", goodsId);
        NetWorkUtil.postForm(mContext.getApplicationContext(), ShopConstant.COMMIT_ORDER_ATTACH_GOODS, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                if (data != null) {
                    ArrayList<AttachGoodsBean> goodsList = new ArrayList<>();
                    try {
                        for (int i = 0; i < data.length(); i++) {
                            AttachGoodsBean goods = new AttachGoodsBean();
                            JSONObject dataObject = data.getJSONObject(i);
                            // id
                            goods.setAttachId(dataObject.optString("attachId"));
                            // 名称
                            goods.setAttachName(dataObject.optString("attachName"));
                            // 价格
                            goods.setPrice(dataObject.optString("price"));
                            // 类型
                            goods.setUnit(dataObject.optString("unit"));
                            goodsList.add(goods);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        mListener.getAttachGoods(goodsList);
                    }
                }

            }
        }, map);
    }

    public interface ActivityListener {

        // 获取到活动信息
        void getActivityData(NotifyBean mNotifyBean);
    }

    /**
     * 活动详细信息的获取
     */
    public static void getActivityInfo(final Context mContext, String noticeId, final ActivityListener mListener) {
        map = new HashMap<>();
        map.put("noticeId", noticeId);
        NetWorkUtil.postForm(mContext.getApplicationContext(), ShopConstant.COMMIT_ORDER_NOTICE, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONObject data) {
                if (data != null) {
                    NotifyBean mNotifyBean = new NotifyBean();
                    try {
                        JSONObject noticeObject = data.getJSONObject("notice");
                        mNotifyBean.getNotifyBean(noticeObject);
                        mNotifyBean.setType(data.optString("type"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        mListener.getActivityData(mNotifyBean);
                    }
                }
            }
        }, map);
    }

    public interface ShowDialogListener{
        // 获取是否有弹窗
        void isShowDialog(String notify);
    }

    /**
     * 是否显示弹窗的获取
     */
    public static void isShowDialog(final Context mContext, final ShowDialogListener mListener) {
        NetWorkUtil.postForm(mContext, ShopConstant.IS_SHOW_COMMIT_DIALOG, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    mListener.isShowDialog(response.optString("data").toString());
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                mListener.isShowDialog("");
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                mListener.isShowDialog("");
            }
        }, new HashMap<String, Object>());
    }

}
