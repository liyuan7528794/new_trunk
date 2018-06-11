package com.travel.shop.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.travel.ShopConstant;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.PersonalInfoBean;
import com.travel.bean.ReasonBean;
import com.travel.bean.VideoInfoBean;
import com.travel.communication.helper.ShopMessageHelper;
import com.travel.layout.CancelOrderReasonPopWindow;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;
import com.travel.shop.adapter.OrderTimeAdapter;
import com.travel.shop.adapter.TouristShowAdapter;
import com.travel.shop.bean.AttachGoodsBean;
import com.travel.shop.bean.GoodsOrderBean;
import com.travel.shop.bean.OrderBean;
import com.travel.shop.bean.OrdersBasicInfoBean;
import com.travel.shop.bean.RefundBean;
import com.travel.shop.bean.TouristInfo;
import com.travel.shop.http.OrderInfoHttp;
import com.travel.shop.tools.OrderInfoDialogTool;
import com.travel.shop.tools.ShopTool;
import com.travel.shop.widget.PayMethodPopWindow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 订单详情页
 *
 * @author wyp
 * @created 2017/11/06
 */
public class OrderInfoActivity extends TitleBarBaseActivity implements View.OnClickListener {

    private static final String TAG = "OrderInfoActivity";
    private Context mContext;
    private long ordersId;
    private int status;// 订单状态
    private int refund_status;// 退款状态
    private int public_status;// 众投状态
    private String sellerId;// 卖家的ID
    private boolean isSeller;// 是否是卖家
    private int goodsType;// 商品类型
    private String paymentPrice;// 应付金额
    private ShopMessageHelper shopMessageHelper;// 小红点的删除
    private GoodsBasicInfoBean goodsBasicInfoBean;
    private OrdersBasicInfoBean ordersBasicInfoBean;
    private int status_refund;// 退款中的情况，用来判断买卖双方的提示语以及按钮的显示
    private String refundMoney;// 退款金额(退款中)
    private boolean isViewer;// 是否是观看者
    private static String SELLER_REFUSE_REFUND = "商家拒绝退订";
    private static String BUYER_ACCEPT_SERVICE = "买家同意（等待客服处理）";
    private static String BUYER_VOTE_SERVICE = "买家投诉（等待客服处理）";
    private boolean isPay;// 是否是点击的支付按钮

    // 订单不存在
    private TextView tv_deleted;
    private ScrollView sv_order_info;

    // 商品信息
    private TextView tv_status, tv_order_goods_title, tv_order_start_place, tv_order_tourist, tv_order_start_date,
            tv_order_actual_pay;
    private ImageView iv_barCode;
    private LinearLayout ll_order_notify, ll_refund_money;
    private TextView tv_order_refund_money, tv_order_notify, tv_left, tv_right;

    // 客服
    private TextView tv_net_service, tv_service_phone;
    private PersonalInfoBean personalData;

    // 联系人信息
    private TextView tv_contact_name, tv_contact_phone, tv_contact_remark;

    // 旅客信息
    private CardView cd_tourist_show;
    private RecyclerView rv_tourist_show;
    private ArrayList<TouristInfo> tourists;
    private TouristShowAdapter touristShowAdapter;

    // 价格明细
    private CardView cd_price_info;
    private TextView tv_adult_price_info, tv_coupon_card, tv_coupon_card_price_info;
    private RelativeLayout rl_coupon_card_price_info, rl_single_price_info, rl_insurance_price_info;
    private TextView tv_single_price_info, tv_insurance_price_info, tv_pay_info;
    private RelativeLayout rl_discount_price_info;// 卖家给的优惠
    private TextView tv_discount_price_info;

    // 订单信息
    private TextView tv_order_ordersId;
    private RecyclerView rv_order_time;
    private ArrayList<HashMap<String, String>> orderTimes;
    private OrderTimeAdapter orderTimeAdapter;

    // 供应商订单相关
    private AttachGoodsBean attachGoodsBeen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_info2);

        initView();
        initData();
        initListener();

        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            OrderInfoHttp.getManageOrderData(ordersId, mContext, mOrderInfoHttpListener);
        }
    }

    private void initView() {
        tv_deleted = findView(R.id.tv_deleted);
        sv_order_info = findView(R.id.sv_order_info);

        tv_status = findView(R.id.tv_status);
        tv_order_goods_title = findView(R.id.tv_order_goods_title);
        tv_order_start_place = findView(R.id.tv_order_start_place);
        tv_order_tourist = findView(R.id.tv_order_tourist);
        tv_order_start_date = findView(R.id.tv_order_start_date);
        tv_order_actual_pay = findView(R.id.tv_order_actual_pay);
        iv_barCode = findView(R.id.iv_barCode);
        ll_order_notify = findView(R.id.ll_order_notify);
        ll_refund_money = findView(R.id.ll_refund_money);
        tv_order_refund_money = findView(R.id.tv_order_refund_money);
        tv_order_notify = findView(R.id.tv_order_notify);
        tv_left = findView(R.id.tv_left);
        tv_right = findView(R.id.tv_right);

        tv_net_service = findView(R.id.tv_net_service);
        tv_service_phone = findView(R.id.tv_service_phone);

        tv_contact_name = findView(R.id.tv_contact_name);
        tv_contact_phone = findView(R.id.tv_contact_phone);
        tv_contact_remark = findView(R.id.tv_contact_remark);

        cd_tourist_show = findView(R.id.cd_tourist_show);
        rv_tourist_show = findView(R.id.rv_tourist_show);

        cd_price_info = findView(R.id.cd_price_info);
        tv_adult_price_info = findView(R.id.tv_adult_price_info);
        tv_coupon_card = findView(R.id.tv_coupon_card);
        tv_coupon_card_price_info = findView(R.id.tv_coupon_card_price_info);
        rl_coupon_card_price_info = findView(R.id.rl_coupon_card_price_info);
        rl_single_price_info = findView(R.id.rl_single_price_info);
        rl_insurance_price_info = findView(R.id.rl_insurance_price_info);
        tv_single_price_info = findView(R.id.tv_single_price_info);
        tv_insurance_price_info = findView(R.id.tv_insurance_price_info);
        tv_pay_info = findView(R.id.tv_pay_info);
        rl_discount_price_info = findView(R.id.rl_discount_price_info);
        tv_discount_price_info = findView(R.id.tv_discount_price_info);

        tv_order_ordersId = findView(R.id.tv_order_ordersId);
        rv_order_time = findView(R.id.rv_order_time);
    }

    private void initData() {
        mContext = this;
        setTitle("订单详情");
        deleteOrdersId();

        ordersId = getIntent().getLongExtra("ordersId", 0);
        goodsBasicInfoBean = new GoodsBasicInfoBean();
        ordersBasicInfoBean = new OrdersBasicInfoBean();
        personalData = new PersonalInfoBean();

        rv_tourist_show.setLayoutManager(new LinearLayoutManager(mContext));
        tourists = new ArrayList<>();
        touristShowAdapter = new TouristShowAdapter(tourists, mContext);
        rv_tourist_show.setAdapter(touristShowAdapter);

        tv_order_ordersId.setText(ordersId + "");
        rv_order_time.setLayoutManager(new LinearLayoutManager(mContext));
        orderTimes = new ArrayList<>();
        orderTimeAdapter = new OrderTimeAdapter(orderTimes, mContext);
        rv_order_time.setAdapter(orderTimeAdapter);

        isViewer = TextUtils.isEmpty(getIntent().getStringExtra("identity")) ? false : true;

        attachGoodsBeen = (AttachGoodsBean) getIntent().getSerializableExtra("attachData");

    }

    private void initListener() {
        if (!isViewer) {
            leftButton.setOnClickListener(this);
            tv_net_service.setOnClickListener(this);
            tv_service_phone.setOnClickListener(this);
            tv_left.setOnClickListener(this);
            tv_right.setOnClickListener(this);
        }
        iv_barCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == leftButton) {// 返回到订单列表
            ManagerOrderActivity.actionStart(mContext, "my");
        } else if (v == tv_net_service) {// 线上客服
            Intent intent = new Intent(ShopConstant.COMMUNICATION_ACTION);
            intent.putExtra("id", personalData.getUserId());
            intent.putExtra("nick_name", personalData.getUserName());
            intent.putExtra("img_url", personalData.getUserPhoto());
            startActivity(intent);
        } else if (v == tv_service_phone) {// 电话客服
            Intent intent = new Intent(Intent.ACTION_DIAL);
            Uri data = Uri.parse("tel:" + personalData.getUserPhone());
            intent.setData(data);
            startActivity(intent);
        } else if (v == tv_left) {
            if (refund_status != 1) {// 没有申请退订时按钮的点击事件
                if (isSeller) {
                    if (status == 2) {// 拒绝
                        ShopTool.setTwiceSureDialog(ordersId, mContext, 4, controlOrderSuccessListener);
                    }
                } else {
                    switch (status) {
                        case 1:// 取消订单
                            ShopTool.setTwiceSureDialog(ordersId, mContext, 3, controlOrderSuccessListener);
                            break;
                        case 3://行程安排
//                            OSUtil.intentPlan(OrderInfoActivity.this, "orderId", ordersId + "");
                            break;
                        case 4:// 满意付款
                            ShopTool.setTwiceSureDialog(ordersId, mContext, 2, controlOrderSuccessListener);
                            break;
                    }
                }
            } else {
                switch (status_refund) {
                    case 1:// 卖家输入退款金额
                        if (isSeller) {
                            String remainDay = ShopTool.getTime(ordersBasicInfoBean.getStartDate(),
                                    ordersBasicInfoBean.getRefundTime().subSequence(0, 10).toString(), "day", mContext);
                            OrderInfoDialogTool.sellerInputRefundMoney(mContext, remainDay,
                                    ShopTool.getMoney(paymentPrice + ""), ordersId, true,
                                    "", controlOrderSuccessListener);
                        }
                        break;
                    case 2:
                    case 3:// 买家投诉
                        if (!isSeller) {
                            ShopTool.setTwiceSureDialog(ordersId, mContext, 5, controlOrderSuccessListener);
                        }
                        break;
                }

            }
        } else if (v == tv_right) {
            if (refund_status != 1) {
                if (isSeller) {
                    switch (status) {
                        case 1:// 修改价格
                            OrderInfoDialogTool.alterPrice(mContext, ordersBasicInfoBean.getPaymentPrice(),
                                    ordersId, controlOrderSuccessListener);
                            break;
                        case 2:// 接受
                            ShopTool.setTwiceSureDialog(ordersId, mContext, 1, controlOrderSuccessListener);
                            break;
                        case 6:// 用户评价
                            startActivity(new Intent(mContext, EvaluationInfoActivity.class).putExtra("ordersId", ordersId));
                            break;
                    }
                } else {
                    switch (status) {
                        case 1:// 支付
                            isPay = true;
                            OrderInfoHttp.getManageOrderData(ordersId, mContext, mOrderInfoHttpListener);
                            break;
                        case 2:// 申请退订
                        case 3:
                            // 获取退订理由数据
                            OrderInfoHttp.getReason(mContext, getRefundReasonListener);
                            break;
                        case 4:// 不满意众投
                            OrderBean mOrderBean = new OrderBean();
                            mOrderBean.setOrdersId(ordersId);
                            mOrderBean.setGoodsType(goodsType);
                            mOrderBean.setPaymentPrice(ordersBasicInfoBean.getPaymentPrice());
                            mOrderBean.setSalerId(sellerId);
                            mOrderBean.setBuyerId(ordersBasicInfoBean.getBuyerId());
                            mOrderBean.setGoodsTitle(goodsBasicInfoBean.getGoodsTitle());
                            mOrderBean.setGoodsAddress(goodsBasicInfoBean.getGoodsAddress());
                            Intent intentOrder = new Intent(mContext, ApplicationPublicVoteActivity.class);
                            intentOrder.putExtra("orderInfo", mOrderBean);
                            startActivity(intentOrder);
                            break;
                        case 5:// 订单评价
                            Intent intent = new Intent(mContext, EvaluateActivity.class);
                            intent.putExtra("ordersId", ordersId);
                            intent.putExtra("goodsId", goodsBasicInfoBean.getGoodsId());
                            startActivity(intent);
                            break;
                    }
                }
            } else {
                switch (status_refund) {
                    case 1:// 卖家不同意退款
                        if (isSeller)
                            ShopTool.setTwiceSureDialog(ordersId, mContext, 6, controlOrderSuccessListener);
                        break;
                    case 2:
                        if (isSeller) {// 修改退款金额
                            String remainDay = ShopTool.getTime(ordersBasicInfoBean.getStartDate(),
                                    ordersBasicInfoBean.getRefundTime().subSequence(0, 10).toString(), "day", mContext);
                            OrderInfoDialogTool.sellerInputRefundMoney(mContext, remainDay,
                                    ShopTool.getMoney(paymentPrice + ""), ordersId, false,
                                    refundMoney, controlOrderSuccessListener);
                        } else {// 同意退款
                            ShopTool.setTwiceSureDialog(ordersId, mContext, 7, controlOrderSuccessListener);
                        }
                        break;
                    case 3:
                        if (!isSeller) {// 同意退款
                            ShopTool.setTwiceSureDialog(ordersId, mContext, 7, controlOrderSuccessListener);
                        }
                        break;
                }
            }
        } else if (v == iv_barCode) {
            Intent intent = new Intent(OrderInfoActivity.this, CreateBarCodeActivity.class);
            intent.putExtra("orderId", ordersId + "");
            startActivity(intent);
        }
    }

    // 获取到订单详情
    OrderInfoHttp.Listener mOrderInfoHttpListener = new OrderInfoHttp.Listener() {

        @Override
        public void onOrderDataFine(GoodsOrderBean goodsOrderBean) {
            goodsBasicInfoBean = goodsOrderBean.getmGoodsBasicInfoBean();
            ordersBasicInfoBean = goodsOrderBean.getmOrdersBasicInfoBean();
            status = ordersBasicInfoBean.getStatus();
            refund_status = ordersBasicInfoBean.getRefundStatus();
            public_status = ordersBasicInfoBean.getPublicStatus();
            sellerId = ordersBasicInfoBean.getSellerId();
            isSeller = ShopTool.isSeller(sellerId);
            goodsType = goodsBasicInfoBean.getGoodsType();
            paymentPrice = ShopTool.getMoney(ordersBasicInfoBean.getPaymentPrice() + "");
            if (isPay) {// 点击支付按钮后的逻辑
                isPay = false;
                if (TextUtils.equals("0", paymentPrice)) {
                    OrderInfoHttp.payZero(ordersId, mContext, zeroPayListener);
                } else {
                    new PayMethodPopWindow(mContext, goodsBasicInfoBean.getGoodsTitle(), OrderInfoActivity.this, ordersId,
                            paymentPrice + "", "orderRoute", 2, 2);
                }
            } else {
                OrderInfoHttp.getPersonalInfo(mContext, sellerId, personalListener);// 客服信息
                if (attachGoodsBeen != null && !TextUtils.isEmpty(attachGoodsBeen.getAttachName())) {// 供应商订单
                    // 商品标题
                    tv_order_goods_title.setText(attachGoodsBeen.getAttachName());
                    tv_order_start_place.setVisibility(View.VISIBLE);
                    tv_order_tourist.setVisibility(View.VISIBLE);
                    // 出发地
                    tv_order_start_place.setText("出发地：" + goodsBasicInfoBean.getGoodsAddress());
                    tv_order_tourist.setText("人员：成人 * " + attachGoodsBeen.getCount());
                    // 出发日期
                    tv_order_start_date.setText("出发日期：" + ordersBasicInfoBean.getStartDate());
                    // 应付金额
                    tv_order_actual_pay.setText("应付金额：￥" + ShopTool.getMoney(attachGoodsBeen.getTotalPrice() + ""));
                } else {
                    // 状态
                    tv_status.setText(ShopTool.getStatusOrder(status, refund_status, public_status));
                    // 商品标题
                    tv_order_goods_title.setText(goodsBasicInfoBean.getGoodsTitle());
                    // 二维码
                    if (goodsType != 6 && !isSeller)
                        if (status > 2)
                            iv_barCode.setVisibility(View.VISIBLE);
                        else
                            iv_barCode.setVisibility(View.GONE);
                    // 人员/张数
                    int adultCount = ordersBasicInfoBean.getAdultCount();
                    if (goodsType != 6) {
                        tv_order_start_place.setVisibility(View.VISIBLE);
                        tv_order_tourist.setVisibility(View.VISIBLE);
                        // 出发地
                        tv_order_start_place.setText("出发地：" + goodsBasicInfoBean.getGoodsAddress());
                        tv_order_tourist.setText("人员：成人 * " + adultCount);
                        // 出发日期
                        tv_order_start_date.setText("出发日期：" + ordersBasicInfoBean.getStartDate());
                    } else {
                        tv_order_start_place.setVisibility(View.GONE);
                        tv_order_tourist.setVisibility(View.INVISIBLE);
                        // 张数
                        tv_order_start_date.setText("张数：" + adultCount + "张");
                    }
                    // 应付金额
                    tv_order_actual_pay.setText("应付金额：￥" + paymentPrice);

                    ll_order_notify.setVisibility(View.GONE);
                    ll_refund_money.setVisibility(View.GONE);
                    tv_order_notify.setVisibility(View.GONE);
                    tv_left.setVisibility(View.GONE);
                    tv_right.setVisibility(View.GONE);
                    switch (refund_status) {// 退款相关
                        case 0:// 无退款申请
                            if (!isViewer && public_status == 0)
                                setButtonShow();
                            break;
                        case 1:// 退款中
                            OrderInfoHttp.getRefundInfo(mContext, ordersId, refundInfoListener);
                            break;
                        case 2:// 退款成功
                            ll_order_notify.setVisibility(View.VISIBLE);
                            ll_refund_money.setVisibility(View.VISIBLE);
                            tv_order_refund_money.setText("￥" + ShopTool.getMoney(ordersBasicInfoBean.getRefundMoney() + ""));
                            break;
                        case 3:// 退款失败（按照原流程继续往下走）
                            //                    ll_order_notify.setVisibility(View.VISIBLE);
                            //                    tv_order_notify.setVisibility(View.VISIBLE);
                            //                    tv_order_notify.setText(SELLER_REFUSE_REFUND);
                            setButtonShow();
                            break;
                    }

                    if (goodsType != 6) {
                        // 旅客信息
                        if (!TextUtils.isEmpty(ordersBasicInfoBean.getUserinfo())) {
                            try {
                                tourists.clear();
                                cd_tourist_show.setVisibility(View.VISIBLE);
                                JSONArray dataArray = new JSONArray(ordersBasicInfoBean.getUserinfo());
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject touristObject = dataArray.getJSONObject(i);
                                    TouristInfo tourist = new TouristInfo();
                                    tourist.setName(touristObject.optString("name"));
                                    tourist.setSex(touristObject.optString("sex"));
                                    tourist.setTelephone(touristObject.optString("telephone"));
                                    tourist.setIDCard(touristObject.optString("IDCard"));
                                    tourists.add(tourist);
                                }
                                touristShowAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        // 价格明细
                        cd_price_info.setVisibility(View.VISIBLE);
                        // 成人旅客
                        tv_adult_price_info.setText("￥" + ShopTool.getMoney(ordersBasicInfoBean.getAdultPrice() + "") + " * " + adultCount);
                        // 单房差
                        int singleCount = ordersBasicInfoBean.getSingleCount();
                        if (singleCount != 0) {
                            rl_single_price_info.setVisibility(View.VISIBLE);
                            tv_single_price_info.setText("￥" + ShopTool.getMoney(ordersBasicInfoBean.getSinglePrice() + "") + " * " + singleCount);
                        }
                        // 旅行险 TODO 暂无
                        // 小城卡
                        int cardCount = ordersBasicInfoBean.getCardCount();
                        if (cardCount != 0) {
                            rl_coupon_card_price_info.setVisibility(View.VISIBLE);
                            tv_coupon_card.setText("小城故事卡");
                            tv_coupon_card_price_info.setText("-￥2000 * " + cardCount);
                        } else {
                            // 优惠券
                            float coupon = ordersBasicInfoBean.getTerraceDiscountPrice();
                            if (coupon != 0) {
                                rl_coupon_card_price_info.setVisibility(View.VISIBLE);
                                tv_coupon_card.setText("优惠券");
                                tv_coupon_card_price_info.setText("-￥" + ShopTool.getMoney(coupon + ""));
                            }
                        }
                        // 优惠金额（商家改价）
                        float discount = ordersBasicInfoBean.getSellerDiscountPrice();
                        if (discount != 0) {
                            rl_discount_price_info.setVisibility(View.VISIBLE);
                            if (discount > 0)
                                tv_discount_price_info.setText("-￥" + ShopTool.getMoney(discount + ""));
                            else
                                tv_discount_price_info.setText("+￥" + ShopTool.getMoney(discount * (-1) + ""));
                        }
                        // 应付金额
                        tv_pay_info.setText("￥" + paymentPrice);
                    }
                }
                // 联系人姓名
                tv_contact_name.setText(ordersBasicInfoBean.getBuyerName());
                // 联系人电话
                tv_contact_phone.setText(ordersBasicInfoBean.getBuyerPhone());
                // 联系人留言
                tv_contact_remark.setText(ordersBasicInfoBean.getRemarkInfoBuyer());
                // 订单信息
                orderTimes.clear();
                for (int i = 0; i < 3; i++) {
                    HashMap<String, String> time = new HashMap<>();
                    if (i == 0) {
                        time.put("label", "下单时间");
                        time.put("show", ordersBasicInfoBean.getCreateTime());
                    } else if (i == 1) {
                        String payTime = ordersBasicInfoBean.getPayTime();
                        if (TextUtils.isEmpty(payTime))
                            break;
                        else {
                            time.put("label", "支付时间");
                            time.put("show", payTime);
                        }
                    } else if (i == 2) {
                        String refundTime = ordersBasicInfoBean.getRefundTime();
                        if (TextUtils.isEmpty(refundTime))
                            break;
                        else {
                            time.put("label", "退订时间");
                            time.put("show", refundTime);
                        }
                    }
                    orderTimes.add(time);
                }
                orderTimeAdapter.notifyDataSetChanged();
            }
        }

        private void setButtonShow() {
            if (isSeller)// 卖家
                switch (status) {
                    case 1:// 待支付
                        tv_right.setVisibility(View.VISIBLE);
                        tv_right.setText("修改价格");
                        break;
                    case 2:// 待卖家确认
                        tv_left.setVisibility(View.VISIBLE);
                        tv_right.setVisibility(View.VISIBLE);
                        tv_left.setText("拒绝");
                        tv_right.setText("接受");
                        break;
                    case 6:// 已评价
                        tv_right.setVisibility(View.VISIBLE);
                        tv_right.setText("用户评价");
                        break;
                }
            else {// 买家
                switch (status) {
                    case 1:// 待支付
                        tv_left.setVisibility(View.VISIBLE);
                        tv_right.setVisibility(View.VISIBLE);
                        tv_left.setText("取消订单");
                        tv_right.setText("支付");
                        break;
                    case 2:// 待卖家确认
                        if (refund_status == 0) {
                            tv_right.setVisibility(View.VISIBLE);
                            tv_right.setText("申请退订");
                        }
                        break;
                    case 3:// 待出行
//                        tv_left.setVisibility(View.VISIBLE);
//                        tv_left.setText("行程安排");
                        if (refund_status == 0) {
                            tv_right.setVisibility(View.VISIBLE);
                            tv_right.setText("申请退订");
                        }
                        break;
                    case 4:// 待确认付款
                        tv_left.setVisibility(View.VISIBLE);
                        tv_right.setVisibility(View.VISIBLE);
                        tv_left.setText("满意付款");
                        tv_right.setText("不满意众投");
                        break;
                    case 5:// 待评价
                        tv_right.setVisibility(View.VISIBLE);
                        tv_right.setText("订单评价");
                        break;
                }
            }
        }

        @Override
        public void onErrorNotZero(int error, String msg) {
            if (error == 1) {// 此订单已被删除
                tv_deleted.setVisibility(View.VISIBLE);
                sv_order_info.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAttachGoodsGot(ArrayList<AttachGoodsBean> attachGoods) {
        }

    };

    // 获取到个人信息
    OrderInfoHttp.OnGetPersonalInfoListener personalListener = new OrderInfoHttp.OnGetPersonalInfoListener() {
        @Override
        public void onDataFine(VideoInfoBean videoInfoBean, int flag) {
            personalData = videoInfoBean.getPersonalInfoBean();
        }
    };
    // 操作成功刷新页面
    OrderInfoHttp.ControlOrderSuccessListener controlOrderSuccessListener = new OrderInfoHttp.ControlOrderSuccessListener() {
        @Override
        public void onSuccess() {
            OrderInfoHttp.getManageOrderData(ordersId, mContext, mOrderInfoHttpListener);
        }
    };
    // 0元支付
    OrderInfoHttp.ControlListener zeroPayListener = new OrderInfoHttp.ControlListener() {
        @Override
        public void onPayZero(long ordersId) {
            OSUtil.intentOrderSuccess(mContext, ordersId);
        }
    };
    // 获取到退订理由
    OrderInfoHttp.GetRefundReasonListener getRefundReasonListener = new OrderInfoHttp.GetRefundReasonListener() {
        @Override
        public void getReason(ArrayList<ReasonBean> reasons) {
            new CancelOrderReasonPopWindow(mContext, reasons, cancelOrderPopWindowListenre);
        }
    };
    // 获取到选择的理由
    CancelOrderReasonPopWindow.CancelOrderPopWindowListenre cancelOrderPopWindowListenre = new CancelOrderReasonPopWindow.CancelOrderPopWindowListenre() {
        @Override
        public void onReason(ReasonBean reason, int position) {
            OrderInfoHttp.applyRefund(mContext, ordersId, reason.getReason(), controlOrderSuccessListener);
        }
    };
    // 获取到退款详情
    OrderInfoHttp.RefundInfoListener refundInfoListener = new OrderInfoHttp.RefundInfoListener() {
        @Override
        public void getRefundInfo(RefundBean rb) {
            ll_order_notify.setVisibility(View.VISIBLE);
            refundMoney = ShopTool.getMoney(rb.getRefund() + "");
            status_refund = rb.getOver();// TODO status后台没有，暂用over代替
            switch (status_refund) {
                case 1:// 买家申请退款
                    tv_order_notify.setVisibility(View.VISIBLE);
                    if (isSeller) {
                        tv_left.setVisibility(View.VISIBLE);
                        tv_right.setVisibility(View.VISIBLE);
                        tv_left.setText("输入退款金额");
                        tv_right.setText("不同意");
                        tv_order_notify.setText("用户理由:" + rb.getReason());
                    } else
                        tv_order_notify.setText("卖家会在2个工作日内确认退订申请");
                    break;
                case 2:// 卖家同意退款
                    ll_refund_money.setVisibility(View.VISIBLE);
                    tv_order_refund_money.setText("￥" + refundMoney);
                    tv_right.setVisibility(View.VISIBLE);
                    if (isSeller) {
                        tv_right.setText("修改退款金额");
                    } else {
                        tv_left.setVisibility(View.VISIBLE);
                        tv_left.setText("投诉");
                        tv_right.setText("同意退款");
                    }
                    break;
                case 3:// 卖家拒绝退款
                    tv_order_notify.setVisibility(View.VISIBLE);
                    tv_order_notify.setText(SELLER_REFUSE_REFUND);
                    if (!isSeller) {
                        tv_left.setVisibility(View.VISIBLE);
                        tv_right.setVisibility(View.VISIBLE);
                        tv_left.setText("投诉");
                        tv_right.setText("同意");
                    }
                    break;
                case 4:// 买家投诉
                    tv_order_notify.setVisibility(View.VISIBLE);
                    tv_order_notify.setText(BUYER_VOTE_SERVICE);
                    break;
                case 5:// 买家同意退款
                    tv_order_notify.setVisibility(View.VISIBLE);
                    tv_order_notify.setText(BUYER_ACCEPT_SERVICE);
                    break;
                case 6:// 卖家处理超时
                    tv_order_notify.setVisibility(View.VISIBLE);
                    tv_order_notify.setText(SELLER_REFUSE_REFUND);
                    break;
                case 7:// 买家处理超时
                    tv_order_notify.setVisibility(View.VISIBLE);
                    tv_order_notify.setText(BUYER_ACCEPT_SERVICE);
                    break;
            }

        }

    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isViewer)
            ManagerOrderActivity.actionStart(mContext, "my");
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 从数据库中删除数据
     */
    private void deleteOrdersId() {
        // 消除小红点
        shopMessageHelper = new ShopMessageHelper(mContext);
        shopMessageHelper.delete(ShopMessageHelper.TABLENAME_MESSAGE, ordersId + "", UserSharedPreference.getUserId());
    }
}
