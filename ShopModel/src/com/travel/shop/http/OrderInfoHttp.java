package com.travel.shop.http;

import android.content.Context;
import android.text.TextUtils;

import com.travel.ShopConstant;
import com.travel.bean.EvaluateInfoBean;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.NotifyBean;
import com.travel.bean.PersonalInfoBean;
import com.travel.bean.PhotoModel;
import com.travel.bean.ReasonBean;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.shop.bean.AttachGoodsBean;
import com.travel.shop.bean.GoodsOrderBean;
import com.travel.shop.bean.OrdersBasicInfoBean;
import com.travel.shop.bean.RefundBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单详情中请求网络数据的方法的类
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/30
 */
public class OrderInfoHttp {

    private static Map<String, Object> map;

    private static ArrayList<ReasonBean> reasons;

    public interface Listener {
        /**
         * 当订单信息获得到
         */
        void onOrderDataFine(GoodsOrderBean goodsOrderBean);

        /**
         * 当订单已被删除 error == 1
         */
        void onErrorNotZero(int error, String msg);

        /**
         * 附加服务的获取
         */
        void onAttachGoodsGot(ArrayList<AttachGoodsBean> attachGoods);
    }

    /**
     * 获取网络数据
     *
     * @param ordersId
     * @param mContext
     */
    public static void getManageOrderData(long ordersId, final Context mContext, final Listener listener) {
        getOrdersInfoById(ordersId, ShopConstant.ORDER_INFO, mContext, listener);
    }

    public static void getOrdersInfoById(long orderId, String url, final Context mContext, final Listener listener) {
        map = new HashMap<>();
        map.put("ordersId", orderId);
        NetWorkUtil.postForm(mContext, url, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONObject data) {
                GoodsOrderBean mGoodsOrderBean = new GoodsOrderBean();
                OrdersBasicInfoBean mOrdersBasicInfoBean = new OrdersBasicInfoBean();
                GoodsBasicInfoBean mGoodsBasicInfoBean = new GoodsBasicInfoBean();
                // 订单状态
                mOrdersBasicInfoBean.setStatus(data.optInt("status"));
                // 退款状态
                mOrdersBasicInfoBean.setRefundStatus(data.optInt("refundStatus"));
                // 众投状态
                mOrdersBasicInfoBean.setPublicStatus(data.optInt("publicStatus"));
                // 买家Id
                mOrdersBasicInfoBean.setBuyerId(data.optString("buyerId"));
                // 卖家Id
                mOrdersBasicInfoBean.setSellerId(data.optString("sellerId"));
                // 商品类型
                mGoodsBasicInfoBean.setGoodsType(data.optInt("type"));

                // 商品标题
                mGoodsBasicInfoBean.setGoodsTitle(data.optString("goodsName"));
                // 商品出发地
                mGoodsBasicInfoBean.setGoodsAddress(data.optString("departCity"));
                // 成人数
                mOrdersBasicInfoBean.setAdultCount(data.optInt("adultNum"));
                // 出行日期
                mOrdersBasicInfoBean.setStartDate(data.optString("departTime"));
                // 应付金额
                mOrdersBasicInfoBean.setPaymentPrice(Float.parseFloat(data.optString("payment")));
                // 退款金额
                mOrdersBasicInfoBean.setRefundMoney(Float.parseFloat(data.optString("refundMoney")));

                // 联系人姓名
                mOrdersBasicInfoBean.setBuyerName(data.optString("buyerName"));
                // 联系人电话
                mOrdersBasicInfoBean.setBuyerPhone(data.optString("buyerTelephone"));
                // 联系人留言
                mOrdersBasicInfoBean.setRemarkInfoBuyer(TextUtils.isEmpty(data.optString("remarks")) ? "暂无留言" : data.optString("remarks"));

                // 旅客信息
                mOrdersBasicInfoBean.setUserinfo(data.optString("userinfo"));

                // 价格明细
                // 成人价
                mOrdersBasicInfoBean.setAdultPrice(Float.parseFloat(data.optString("price")));
                // 单房差价
                mOrdersBasicInfoBean.setSinglePrice(Float.parseFloat(data.optString("roomPrice")));
                // 单房差数
                mOrdersBasicInfoBean.setSingleCount(data.optInt("roomNum"));
                // todo 旅行险未取
                // 小城卡数
                mOrdersBasicInfoBean.setCardCount(data.optInt("cityCard"));
                // 优惠券金额
                mOrdersBasicInfoBean.setTerraceDiscountPrice(Float.parseFloat(data.optString("terraceDiscountPrice")));
                // 优惠金额（商家改价）
                mOrdersBasicInfoBean.setSellerDiscountPrice(Float.parseFloat(data.optString("sellerDiscountPrice")));

                // 订单信息
                mOrdersBasicInfoBean.setCreateTime(data.optString("createTime"));
                try {
                    mOrdersBasicInfoBean.setPayTime(data.getString("payTime"));
                    mOrdersBasicInfoBean.setRefundTime(data.getString("refundTime"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 商品Id
                mGoodsBasicInfoBean.setGoodsId(data.optString("goodsId"));
                // 总价
                mOrdersBasicInfoBean.setTotalPrice(Float.parseFloat(data.optString("totalPrice")));

                mGoodsOrderBean.setmOrdersBasicInfoBean(mOrdersBasicInfoBean);
                mGoodsOrderBean.setmGoodsBasicInfoBean(mGoodsBasicInfoBean);
                //                    // 商品快照Id
                //                    mOrdersBasicInfoBean.setGoodsSnapshootId(data.getString("snapshootId"));
                //                    // 订单管理状态
                //                    mOrdersBasicInfoBean.setStatusManage(data.optInt("status"));
                //                    // 服务Id
                //                    mOrdersBasicInfoBean.setServiceId(data.optString("serviceId"));
                //                    // 返回日期
                //                    mOrdersBasicInfoBean.setTravelDays(data.optString("dayNum") + "天");
                //                    // 儿童数
                //                    mOrdersBasicInfoBean.setChildrenCount(data.optInt("childNum"));
                //                    // 备注信息(卖家)
                //                    mOrdersBasicInfoBean.setRemarkInfoSeller(data.optString("goodsRemark"));
                //                    // 是否被扫 0：未扫 1:扫过
                //                    mOrdersBasicInfoBean.setIsCheck(data.optInt("countersignCheck"));
                //                    // 是否有附加服务
                //                    mOrdersBasicInfoBean.setAttachPrice(data.optString("attachPrice"));
                //                    mGoodsOrderBean.setmOrdersBasicInfoBean(mOrdersBasicInfoBean);
                //
                //                    // 故事Id
                //                    mGoodsBasicInfoBean.setStoryId(data.optString("storyId"));
                //                    // 商品编号
                //                    mGoodsBasicInfoBean.setGoodsNum(data.optString("goodsNumber"));
                //                    // 商品图片
                //                    mGoodsBasicInfoBean.setGoodsImg(data.optString("imgUrl"));
                //                    // 选择的套餐
                //                    mGoodsBasicInfoBean.setGoodsSelectedPackage(data.optString("priceExplain"));
                //                    mGoodsOrderBean.setmGoodsBasicInfoBean(mGoodsBasicInfoBean);
                //                    // 时间轴的数据
                //                    String[] paths = data.optString("flow").split(",");
                //                    ArrayList<OrderPathBean> pathList = new ArrayList<OrderPathBean>();
                //                    // 已完成的状态
                //                    for (int i = 0; i < paths.length; i += 2) {
                //                        OrderPathBean mOrderPathBean = new OrderPathBean();
                //                        mOrderPathBean.setStatusManage(mOrdersBasicInfoBean.getStatusManage());
                //                        // 退款金额
                //                        mOrderPathBean.setRefundMoney(data.optString("refundMoney"));
                //                        mOrderPathBean.setTime(paths[i]);
                //                        mOrderPathBean.setStatusPath(paths[i + 1]);
                //                        // 正常完成
                //                        if (i == paths.length - 2 && mOrdersBasicInfoBean.getStatusManage() == 2)
                //                            // 状态
                //                            mOrderPathBean.setOverStatus(mOrdersBasicInfoBean.getStatus());
                //                        // 异常完成
                //                        if (!TextUtils.isEmpty(data.optString("refund"))) {
                //                            // 理由
                //                            mOrderPathBean.setReason(data.optString("refund"));
                //                            if (TextUtils.equals("买家申请退款", mOrderPathBean.getStatusPath()))
                //                                mOrderPathBean.setOrderStatus(Integer.parseInt(data.optString("orderStatus")));
                //                        }
                //                        pathList.add(mOrderPathBean);
                //                    }
                //                    // 将要执行的操作
                //                    if (mOrdersBasicInfoBean.getStatusManage() == 1
                //                            || (mOrdersBasicInfoBean.getStatusManage() == 2 && mOrdersBasicInfoBean.getStatus() == 1)) {
                //                        OrderPathBean mOrderPathBean = new OrderPathBean();
                //                        mOrderPathBean.setTime("");
                //                        mOrderPathBean.setStatusManage(mOrdersBasicInfoBean.getStatusManage());
                //                        mOrderPathBean.setOrderStatus(Integer.parseInt(data.optString("orderStatus")));
                //                        mOrderPathBean.setOverStatus(Integer.parseInt(data.optString("overStatus")));
                //                        pathList.add(mOrderPathBean);
                //                    }
                //                    mGoodsOrderBean.setPathList(pathList);
                listener.onOrderDataFine(mGoodsOrderBean);

            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                listener.onErrorNotZero(error, msg);
            }
        }, map);
    }


    public interface ControlListener {
        // 支付0元时
        void onPayZero(long ordersId);
    }

    /**
     * 当支付金额是0元时
     *
     * @param ordersId
     * @param mContext
     */
    public static void payZero(final long ordersId, final Context mContext, final ControlListener mListener) {
        map = new HashMap<>();
        map.put("ordersId", ordersId);
        NetWorkUtil.postForm(mContext, ShopConstant.PAY_ZERO, new MResponseListener(mContext) {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    mListener.onPayZero(ordersId);
                }
            }

        }, map);
    }

    /**
     * 获取附加服务
     *
     * @param mContext
     * @param ordersId
     */
    public static void getAttachGoods(final Context mContext, final long ordersId,
                                      final Listener mListener) {
        map = new HashMap<>();
        map.put("ordersId", ordersId);
        NetWorkUtil.postForm(mContext, ShopConstant.ORDER_INFO_ATTACH_GOODS,
                new MResponseListener(mContext) {

                    @Override
                    protected void onDataFine(JSONArray data) {
                        ArrayList<AttachGoodsBean> attachGoods = new ArrayList<>();
                        try {
                            for (int i = 0; i < data.length(); i++) {
                                AttachGoodsBean mAttachGoodsBean = new AttachGoodsBean();
                                JSONObject dataObject = data.getJSONObject(i);
                                // 名称
                                mAttachGoodsBean.setAttachName(dataObject.optString("attachName"));
                                // 价格
                                mAttachGoodsBean.setPrice(dataObject.optString("attachPrice"));
                                // 单位
                                mAttachGoodsBean.setUnit(dataObject.optString("attachUnit"));
                                // 数量
                                mAttachGoodsBean.setCount(dataObject.optInt("attachNum"));
                                attachGoods.add(mAttachGoodsBean);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            mListener.onAttachGoodsGot(attachGoods);
                        }
                    }
                }, map);
    }

    public interface OnGetPersonalInfoListener {
        /**
         * 获取到个人信息
         *
         * @param videoInfoBean
         * @param flag          // 1:卖家 2:买家
         */
        void onDataFine(VideoInfoBean videoInfoBean, int flag);
    }

    /**
     * 获取个人信息
     *
     * @param mContext
     */
    public static void getPersonalInfo(Context mContext, String sellerId,
                                       OnGetPersonalInfoListener mListener) {
        getPersonalInfo(mContext, sellerId, 2, mListener);
    }

    /**
     * 获取个人信息
     *
     * @param mContext
     * @param userId
     * @param flag
     */
    public static void getPersonalInfo(final Context mContext, String userId, final int flag,
                                       final OnGetPersonalInfoListener mListener) {
        map = new HashMap<>();
        map.put("id", userId);
        NetWorkUtil.postForm(mContext, ShopConstant.PERSONAL_INFO,
                new MResponseListener(mContext) {

                    @Override
                    protected void onDataFine(JSONObject data) {
                        VideoInfoBean mVideoBean = new VideoInfoBean();
                        PersonalInfoBean bean = new PersonalInfoBean();
                        // userId
                        bean.setUserId(data.optString("id"));
                        // 头像
                        bean.setUserPhoto(data.optString("imgUrl"));
                        // 名字
                        bean.setUserName(data.optString("nickName"));
                        // 电话
                        bean.setUserPhone(data.optString("mobile"));
                        // 个人介绍
                        bean.setUserInfo(data.optString("myIntroduction"));
                        // 是否在线
                        bean.setLogin(data.optBoolean("loginStatus"));
                        // 是否有直播
                        bean.setLiveId(data.optString("liveId"));
                        mVideoBean.setPersonalInfoBean(bean);
                        mListener.onDataFine(mVideoBean, flag);
                    }
                }, map);
    }

    public interface OnActivityInfoListener {
        /**
         * 获取活动信息
         *
         * @param list
         */
        void onDataFine(ArrayList<NotifyBean> list);
    }

    /**
     * 获取活动信息
     *
     * @param mContext
     * @param goodsId
     */
    public static void getActivityInfo(final Context mContext, String goodsId,
                                       final OnActivityInfoListener mListener) {
        map = new HashMap<>();
        map.put("goodsId", goodsId);
        NetWorkUtil.postForm(mContext, ShopConstant.COMMIT_ORDER_ACTIVITY,
                new MResponseListener(mContext) {

                    @Override
                    protected void onDataFine(JSONArray data) {
                        super.onDataFine(data);
                        ArrayList<NotifyBean> infos = new ArrayList<>();
                        try {
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject dataObject = data.getJSONObject(i);
                                NotifyBean notifyBean = new NotifyBean();
                                notifyBean.setId(dataObject.optString("id"));
                                notifyBean.setTitle(dataObject.optString("name"));
                                notifyBean.setType(dataObject.optString("type"));
                                notifyBean.setWebUrl(dataObject.optString("staticHTML"));
                                notifyBean.setStatus(dataObject.optInt("status"));
                                infos.add(notifyBean);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            mListener.onDataFine(infos);
                        }
                    }
                }, map);
    }

    public interface ControlOrderSuccessListener {
        void onSuccess();
    }

    /**
     * 卖家修改价格
     *
     * @param ordersId
     * @param mContext
     * @param price
     */
    public static void alterPrice(long ordersId, final Context mContext, String price,
                                  final ControlOrderSuccessListener controlOrderSuccessListener) {
        DecimalFormat df = new DecimalFormat("##0.00");
        map = new HashMap<>();
        map.put("ordersId", ordersId);
        map.put("price", df.format(Float.parseFloat(price)));
        NetWorkUtil.postForm(mContext, ShopConstant.SALLER_ALTER_PRICE, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    controlOrderSuccessListener.onSuccess();
                }
            }
        }, map);
    }

    private static String url;

    /**
     * 订单操作
     *
     * @param ordersId
     * @param mContext
     */
    public static void controlOrders(long ordersId, final Context mContext, int flag, final ControlOrderSuccessListener controlOrderSuccessListener) {
        switch (flag) {
            case 1:
                url = ShopConstant.SELLER_RECEIVE_ORDER;
                break;
            case 2:
                url = ShopConstant.BUYER_PAY;
                break;
            case 3:
                url = ShopConstant.BUYER_CANCEL_ORDER;
                break;
            case 4:
                url = ShopConstant.SELLER_REFUSE;
                break;
        }
        map = new HashMap<>();
        map.put("ordersId", ordersId);
        NetWorkUtil.postForm(mContext, url, new MResponseListener(mContext) {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    controlOrderSuccessListener.onSuccess();
                }
            }
        }, map);
    }

    public interface GetRefundReasonListener {
        void getReason(ArrayList<ReasonBean> reasons);
    }

    /**
     * 获取退订理由
     *
     * @param mContext
     * @param getRefundReasonListener
     */
    public static void getReason(final Context mContext, final GetRefundReasonListener getRefundReasonListener) {
        reasons = new ArrayList<>();
        map = new HashMap<>();
        map.put("type", 2);
        NetWorkUtil.postForm(mContext, ShopConstant.BUYER_CANCLE_ORDER_REASON, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject dataObject = data.getJSONObject(i);
                        ReasonBean rb = new ReasonBean();
                        // 默认不选
                        rb.setFlag(0);
                        // 理由
                        rb.setReason(dataObject.optString("content"));
                        reasons.add(rb);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    getRefundReasonListener.getReason(reasons);
                }
            }
        }, map);

    }

    /**
     * 买家申请退订
     *
     * @param mContext
     * @param controlOrderSuccessListener
     */
    public static void applyRefund(final Context mContext, long ordersId, String refundReason, final ControlOrderSuccessListener controlOrderSuccessListener) {
        reasons = new ArrayList<>();
        map = new HashMap<>();
        map.put("ordersId", ordersId);
        map.put("refundReason", refundReason);
        NetWorkUtil.postForm(mContext, ShopConstant.BUYER_APPLICATION_REFUND, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    controlOrderSuccessListener.onSuccess();
                }
            }
        }, map);

    }

    public interface RefundInfoListener {
        void getRefundInfo(RefundBean rb);
    }

    /**
     * 获取退款详情
     *
     * @param mContext
     * @param ordersId
     */
    public static void getRefundInfo(final Context mContext, long ordersId, final RefundInfoListener refundInfoListener) {
        map = new HashMap<>();
        map.put("ordersId", ordersId);
        NetWorkUtil.postForm(mContext, ShopConstant.REFUND_INFO, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONObject data) {
                if (data == null || data.length() == 0)
                    return;
                RefundBean rb = new RefundBean();
                // 申请退款时间
                rb.setDate(data.optString("createTime"));
                rb.setOver(data.optInt("status"));
                rb.setRefund(Float.parseFloat(data.optString("refundMoney")));
                rb.setReason(data.optString("refundReason"));
                refundInfoListener.getRefundInfo(rb);
            }
        }, map);

    }

    /**
     * 退款相关操作（除了卖家修改退款金额）
     *
     * @param ordersId
     * @param mContext
     * @param reason
     * @param flag
     * @param refundMoney
     */
    public static void refundControl(long ordersId, final Context mContext, String reason, int flag, String refundMoney, final ControlOrderSuccessListener controlOrderSuccessListener) {
        map = new HashMap<>();
        map.put("ordersId", ordersId);
        // agreeOrReject 1同意2投诉
        if (flag == 5 || flag == 6) {
            map.put("agreeOrReject", 2);
        } else if (flag == 7 || flag == 8) {
            map.put("agreeOrReject", 1);
        }
        map.put("refundMoney", refundMoney);
        //        if (TextUtils.equals("refundNo", tag))
        //            map.put("complain", reason);
        NetWorkUtil.postForm(mContext, ShopConstant.SALLER_SURE_REFUND, new MResponseListener(mContext) {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    controlOrderSuccessListener.onSuccess();
                }
            }

        }, map);
    }

    /**
     * 卖家修改退款金额
     *
     * @param ordersId
     * @param mContext
     */
    public static void changeRefund(long ordersId, final Context mContext, final String refundMoney, final ControlOrderSuccessListener controlOrderSuccessListener) {
        map = new HashMap<>();
        map.put("ordersId", ordersId);
        map.put("refundMoney", refundMoney);
        NetWorkUtil.postForm(mContext, ShopConstant.SALLER_CHANGE_REFUND, new MResponseListener(mContext) {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    controlOrderSuccessListener.onSuccess();
                }
            }
        }, map);
    }

    public interface GetEvaluateListener {
        void getSuccess(EvaluateInfoBean evaluateInfoBean);

        void getFail();
    }

    /**
     * 获取评价
     *
     * @param mContext
     * @param ordersId
     * @param getEvaluateListener
     */
    public static void getEvaluate(final Context mContext, long ordersId, final GetEvaluateListener getEvaluateListener) {
        map = new HashMap<>();
        map.put("ordersId", ordersId);
        NetWorkUtil.postForm(mContext, ShopConstant.ORDER_EVALUATE, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONObject data) {
                EvaluateInfoBean mEvaluateInfoBean = new EvaluateInfoBean();
                // 星级
                mEvaluateInfoBean.setEvaluateStar(data.optInt("evaluateType"));
                // 评价
                mEvaluateInfoBean.setEvaluateContent(data.optString("content"));
                // 评价日期
                mEvaluateInfoBean.setEvaluateTime(data.optString("createTime"));
                // 图片
                if (!"".equals(data.optString("imgs"))) {
                    ArrayList<PhotoModel> pictures = new ArrayList<PhotoModel>();
                    String pics[] = data.optString("imgs").split(",");
                    for (int j = 0; j < pics.length; j++) {
                        PhotoModel pic = new PhotoModel();
                        pic.setOriginalPath(pics[j]);
                        pic.setOriginalPathBig(pics[j]);
                        pictures.add(pic);
                    }
                    mEvaluateInfoBean.setEvaluatePictures(pictures);
                }
                // 用户信息
                try {
                    JSONObject userObject = data.getJSONObject("user");
                    // uid
                    mEvaluateInfoBean.setEvaluateUserId(userObject.optString("id"));
                    // 头像
                    mEvaluateInfoBean.setEvaluateUserPhoto(userObject.optString("imgUrl"));
                    // 名字
                    mEvaluateInfoBean.setEvaluateUserName(userObject.optString("nickName"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    getEvaluateListener.getSuccess(mEvaluateInfoBean);
                }
            }
        }, map);
    }

}
