package com.travel.shop.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.travel.Constants;
import com.travel.ShopConstant;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.GoodsDetailBean;
import com.travel.bean.GoodsOtherInfoBean;
import com.travel.bean.GoodsServiceBean;
import com.travel.bean.NotifyBean;
import com.travel.bean.UDPSendInfoBean;
import com.travel.bean.VideoInfoBean;
import com.travel.http_helper.SlideHelper;
import com.travel.layout.ArcView;
import com.travel.layout.DialogTemplet;
import com.travel.layout.HeadZoomScrollView;
import com.travel.layout.SharePopupWindow;
import com.travel.lib.R;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.PopWindowUtils;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.adapter.GetCouponAdapter;
import com.travel.shop.bean.CommitBean;
import com.travel.shop.bean.CouponInfoBean;
import com.travel.shop.fragment.GoodsEvaluateFragment;
import com.travel.shop.fragment.GoodsInfoFragment;
import com.travel.shop.fragment.TextFragment;
import com.travel.shop.http.GoodsHttp;
import com.travel.shop.http.OrderInfoHttp;
import com.travel.shop.tools.ShopTool;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品详情页----仿抖音版，暂且去掉所有的小城卡与优惠
 *
 * @author wyp
 * @created 2017/10/30
 */
public class GoodsActivity extends TitleBarBaseActivity implements View.OnClickListener {

    private Context mContext;

    // 商品下架相关
    private TextView tv_goods_undercarriage;
    private RelativeLayout rl_goods_control;

    private HeadZoomScrollView dzsv_root;
    // 商品图片
    private ImageView iv_down_picture, iv_up_picture;
    private ArcView av_arc;
    // 商品信息
    private TextView tv_card_count, tv_goods_title, tv_original_cost, tv_current_cost_desc, tv_current_cost;
    private View ll_card_control, ll_card_minus, ll_card_plus;
    private ImageView iv_card_minus;
    private TextView tv_card_counts;
    // 小城故事卡
    private CardView cd_card;
    private TextView tv_cards, tv_card_price, tv_buy_card, tv_card_intro;
    // 代金券
    private CardView cd_coupon;
    private RecyclerView rv_coupon;
    private GetCouponAdapter getCouponAdapter;
    private ArrayList<CouponInfoBean> coupons;
    // 我们的保障
    private CardView cd_ensure;
    private TextView tv_ensure_left, tv_ensure_right;
    private SlideHelper ensureHelper;
    private ArrayList<NotifyBean> ensures;
    // 可选标签
    private FrameLayout fl_container, fl_layout;
    private CardView cd_selectable;
    private LinearLayout layout1, layout2;
    private TextView tv1_1, tv1_2, tv1_3, tv2_1, tv2_2, tv2_3;

    private FragmentManager fragmentManager;
    private FragmentTransaction ft;

    // 底部按钮
    private TextView tv_goods_consult, tv_goods_order;

    // 数据相关
    private String goodsId;
    private GoodsBasicInfoBean mGoodsBasicInfoBean;
    private GoodsOtherInfoBean mGoodsOtherInfoBean;
    private int advanceDays;
    private ArrayList<GoodsServiceBean> travelPlans;
    private String sellerId;
    private VideoInfoBean mPersonalVideoBean;
    private int goodsType;

    // 小城卡购买相关
    private int cardCount = 0;
    private String cardId;
    private boolean isSupportCard;// 是否可以使用小城卡
    private int cardRemainCount;// 小城卡剩余数量
    private int count;// 如购买过小城卡，剩余次数

    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods);

        initView();
        initData();
        initListener();

        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            GoodsHttp.getGoodsData(mContext, goodsId, goodsInfoListener);
            ensureHelper.getSlideData(2, SlideHelper.TAG_ACTIVITY_AND_NOTICE);
        }

    }

    private void initView() {
        tv_goods_undercarriage = findView(R.id.tv_goods_undercarriage);
        rl_goods_control = findView(R.id.rl_goods_control);

        dzsv_root = findView(R.id.dzsv_root);
        iv_down_picture = findView(R.id.iv_down_picture);
        iv_up_picture = findView(R.id.iv_up_picture);
        av_arc = findView(R.id.av_arc);

        tv_card_count = findView(R.id.tv_card_count);
        tv_goods_title = findView(R.id.tv_goods_title);
        tv_original_cost = findView(R.id.tv_original_cost);
        tv_current_cost_desc = findView(R.id.tv_current_cost_desc);
        tv_current_cost = findView(R.id.tv_current_cost);
        ll_card_control = findView(R.id.ll_card_control);
        ll_card_minus = ll_card_control.findViewById(R.id.ll_m_click);
        ll_card_plus = ll_card_control.findViewById(R.id.ll_p_click);
        iv_card_minus = (ImageView) ll_card_control.findViewById(R.id.iv_minus);
        tv_card_counts = (TextView) ll_card_control.findViewById(R.id.tv_count);

        cd_card = findView(R.id.cd_card);
        tv_cards = findView(R.id.tv_cards);
        tv_card_price = findView(R.id.tv_card_price);
        tv_buy_card = findView(R.id.tv_buy_card);
        tv_card_intro = findView(R.id.tv_card_intro);

        cd_coupon = findView(R.id.cd_coupon);
        rv_coupon = findView(R.id.rv_coupon);

        cd_ensure = findView(R.id.cd_ensure);
        tv_ensure_left = findView(R.id.tv_ensure_left);
        tv_ensure_right = findView(R.id.tv_ensure_right);

        fl_container = findView(R.id.fl_container_goods);
        fl_layout = findView(R.id.fl_layout);
        cd_selectable = findView(R.id.cd_selectable);
        layout1 = findView(R.id.ll_layout1);
        layout2 = findView(R.id.ll_layout2);
        tv1_1 = findView(R.id.tv1_1);
        tv1_2 = findView(R.id.tv1_2);
        tv1_3 = findView(R.id.tv1_3);
        tv2_1 = findView(R.id.tv2_1);
        tv2_2 = findView(R.id.tv2_2);
        tv2_3 = findView(R.id.tv2_3);

        tv_goods_consult = findView(R.id.tv_goods_consult);
        tv_goods_order = findView(R.id.tv_goods_order);
    }

    private void initData() {
        mContext = this;
        setTitle("商品详情");
        OSUtil.setShareParam(group_chat, "share", mContext);
        dzsv_root.setZoomView(iv_up_picture);
        dzsv_root.setZoomViewArc(av_arc);
        ImageDisplayTools.initImageLoader(mContext);
        setLine(tv2_1, tv2_2, tv2_3);
        setLine(tv1_1, tv1_2, tv1_3);
        fragmentManager = getSupportFragmentManager();

        goodsId = getIntent().getStringExtra("goodsId");
        mGoodsBasicInfoBean = new GoodsBasicInfoBean();
        mGoodsOtherInfoBean = new GoodsOtherInfoBean();
        travelPlans = new ArrayList<>();
        mPersonalVideoBean = new VideoInfoBean();

        rv_coupon.setLayoutManager(new LinearLayoutManager(mContext));
        coupons = new ArrayList<>();
        getCouponAdapter = new GetCouponAdapter(coupons, mContext);
        rv_coupon.setAdapter(getCouponAdapter);
        ensureHelper = new SlideHelper(mContext, ensureListener);
        ensures = new ArrayList<>();
    }

    private void initListener() {
        group_chat.setOnClickListener(this);
        tv_buy_card.setOnClickListener(this);
        ll_card_minus.setOnClickListener(this);
        ll_card_plus.setOnClickListener(this);

//        getCouponAdapter.setOnCouponClickListener(new GetCouponAdapter.OnCouponClickListener() {// TODO 仿抖音去掉
//            @Override
//            public void onClick(int position) {
//
//                GoodsHttp.getCoupon(mContext, coupons.get(position).getCouponId(), position, couponInfoListener);
//            }
//        });

        tv_ensure_left.setOnClickListener(this);
        tv_ensure_right.setOnClickListener(this);
        tv_goods_consult.setOnClickListener(this);
        tv_goods_order.setOnClickListener(this);

        dzsv_root.setOnScrollListener(new HeadZoomScrollView.OnScrollListener() {
            @Override
            public void onScroll(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY >= fl_layout.getTop() + OSUtil.dp2px(GoodsActivity.this, 148)) {
                    cd_selectable.setCardElevation(0);
                    layout1.setVisibility(View.VISIBLE);
                    fl_layout.setBackgroundResource(android.R.color.white);
                } else {
                    cd_selectable.setCardElevation(2);
                    layout1.setVisibility(View.GONE);
                    fl_layout.setBackgroundResource(R.color.gray_F5);
                }
            }
        });
        tv1_1.setOnClickListener(selectableClickListener);
        tv1_2.setOnClickListener(selectableClickListener);
        tv1_3.setOnClickListener(selectableClickListener);
        tv2_1.setOnClickListener(selectableClickListener);
        tv2_2.setOnClickListener(selectableClickListener);
        tv2_3.setOnClickListener(selectableClickListener);
    }

    @Override
    public void onClick(View v) {

        if (v == group_chat) {// 分享
            PopWindowUtils.sharePopUpWindow(mContext, "红了旅行", mGoodsBasicInfoBean.getGoodsTitle(),
                    mGoodsBasicInfoBean.getGoodsImg(), ShopConstant.GOODS_SHARE + goodsId);
        } else if (v == tv_buy_card) {// 小城卡购买
            startActivity(new Intent(mContext, GoodsActivity.class).putExtra("goodsId", cardId));
        } else if (v == ll_card_minus) {// 小城卡购买"-"
            if (cardCount > 0) {
                --cardCount;
                tv_card_counts.setText(cardCount + "");
                iv_card_minus.setImageResource(cardCount == 0 ?
                        R.drawable.icon_card_minus_no : R.drawable.icon_card_minus_yes);
            }
        } else if (v == ll_card_plus) {// 小城卡购买"+"
            if (cardCount <= cardRemainCount) {
                ++cardCount;
                tv_card_counts.setText(cardCount + "");
                iv_card_minus.setImageResource(R.drawable.icon_card_minus_yes);
            }
        } else if (v == tv_ensure_left || v == tv_ensure_right) {// 先旅游后买单 该不该买单
            Bundle bundle = new Bundle();
            bundle.putSerializable("notice_bean", ensures.get(v == tv_ensure_left ? 0 : 1));
            bundle.putString("tag", "ensure");
            Intent intent = new Intent();
            intent.setAction(Constants.NOTICE_ACTION);
            intent.setType(Constants.VIDEO_TYPE);
            intent.putExtra("notice_bean", bundle);
            startActivity(intent);
        } else if (v == tv_goods_consult) {// 咨询
            if (ShopTool.isSeller(sellerId)) {// 如果是卖家，则提示不可
                showToast("商家不可咨询！");
                return;
            }
            if (mPersonalVideoBean == null) {
                finish();
                return;
            }
            if (UserSharedPreference.isLogin()) {
                // 有直播
                if (!TextUtils.isEmpty(mPersonalVideoBean.getPersonalInfoBean().getLiveId())
                        && !TextUtils.equals(mPersonalVideoBean.getPersonalInfoBean().getLiveId(), "-1")) {
                    ShopTool.play(mPersonalVideoBean, mContext, 0);
                } else {// 没直播 直接聊天
                    Intent intent = new Intent(ShopConstant.COMMUNICATION_ACTION);
                    intent.putExtra("id", mPersonalVideoBean.getPersonalInfoBean().getUserId());
                    intent.putExtra("nick_name", mPersonalVideoBean.getPersonalInfoBean().getUserName());
                    intent.putExtra("img_url", mPersonalVideoBean.getPersonalInfoBean().getUserPhoto());
                    intent.putExtra("goods_info", mGoodsBasicInfoBean);
                    startActivity(intent);
                }
            } else {
                startActivity(new Intent(ShopConstant.LOG_IN_ACTION).putExtra("refresh", "refresh"));
            }
        } else if (v == tv_goods_order) {// 预订
            if (ShopTool.isSeller(sellerId)) {// 如果是卖家，则提示不可
                showToast("商家不可购买自己的商品！");
                return;
            }
            if (UserSharedPreference.isLogin()) {
                if (goodsType == 6) {
                    if (cardCount != 0) {
                        CommitBean commitBean = new CommitBean();
                        commitBean.setGoodsId(goodsId);
                        commitBean.setAdultNum(cardCount);
                        commitBean.setGoodsTitle(mGoodsBasicInfoBean.getGoodsTitle());
                        commitBean.setGoodsType(goodsType);
                        commitBean.setRemainCount(count);
                        startActivity(new Intent(mContext, FilloutInfoActivity.class).putExtra("info", commitBean));
                    } else
                        showToast("卡数不可为0");
                } else
                    OrderActivity.actionStart(mContext, mGoodsBasicInfoBean, count, cardId);
            } else {
                startActivity(new Intent(ShopConstant.LOG_IN_ACTION).putExtra("refresh", "refresh"));
            }
        }
    }

    private void setLine(TextView... tv) {
        Drawable img = ContextCompat.getDrawable(mContext, R.drawable.line_6c6f73);
        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
        tv[0].setCompoundDrawables(null, null, null, img);
        tv[0].setTextColor(ContextCompat.getColor(mContext, R.color.black_6C6F73));
        tv[1].setCompoundDrawables(null, null, null, null);
        tv[1].setTextColor(ContextCompat.getColor(mContext, R.color.gray_BDC0C4));
        tv[2].setCompoundDrawables(null, null, null, null);
        tv[2].setTextColor(ContextCompat.getColor(mContext, R.color.gray_BDC0C4));
    }

    interface SelectableClickListener extends View.OnClickListener {
    }

    private SelectableClickListener selectableClickListener = new SelectableClickListener() {
        @Override
        public void onClick(View v) {
            TextView view = (TextView) v;
            ft = fragmentManager.beginTransaction();
            if (view == tv1_1 || view == tv2_1) {// 玩去旅程
                setLine(tv1_1, tv1_2, tv1_3);
                setLine(tv2_1, tv2_2, tv2_3);
                if (view == tv1_1)
                    fl_container.post(new Runnable() {
                        @Override
                        public void run() {
                            dzsv_root.scrollTo(0, fl_layout.getTop() + OSUtil.dp2px(mContext, 148));
                        }
                    });
                ft.replace(R.id.fl_container_goods, GoodsInfoFragment.newInstance(travelPlans, "", "", mGoodsOtherInfoBean.getCostImplications()));
            } else if (view == tv1_2 || view == tv2_2) {//预定须知
                setLine(tv1_2, tv1_1, tv1_3);
                setLine(tv2_2, tv2_1, tv2_3);
                if (view == tv1_2)
                    fl_container.post(new Runnable() {
                        @Override
                        public void run() {
                            dzsv_root.scrollTo(0, fl_layout.getTop() + OSUtil.dp2px(mContext, 148));
                        }
                    });
                ft.replace(R.id.fl_container_goods, TextFragment.newInstance(mGoodsOtherInfoBean));
            } else if (view == tv1_3 || view == tv2_3) {//商品评价
                setLine(tv1_3, tv1_2, tv1_1);
                setLine(tv2_3, tv2_2, tv2_1);
                if (view == tv1_3)
                    fl_container.post(new Runnable() {
                        @Override
                        public void run() {
                            dzsv_root.scrollTo(0, fl_layout.getTop() + OSUtil.dp2px(mContext, 148));
                        }
                    });
                ft.replace(R.id.fl_container_goods, GoodsEvaluateFragment.newInstance(goodsId));
            }
            ft.commit();
        }
    };

    // 获取到商品详情数据
    GoodsHttp.GoodsInfoListener goodsInfoListener = new GoodsHttp.GoodsInfoListener() {
        @Override
        public void getGoodsData(GoodsDetailBean mGoodsDetailBean) {
            mGoodsBasicInfoBean = mGoodsDetailBean.getGoodsBasicInfoBean();
            mGoodsOtherInfoBean = mGoodsDetailBean.getGoodsOtherInfoBean();
            travelPlans.addAll(mGoodsOtherInfoBean.getTravelPlans());
            // 小城卡购买
            goodsType = mGoodsBasicInfoBean.getGoodsType();
            isSupportCard = mGoodsBasicInfoBean.isSupportCard();
//            GoodsHttp.isBuyCard(mContext, cardInfoListener); // TODO 仿抖音去掉
//            GoodsHttp.getGoodsSupportCoupon(mContext, goodsId, couponInfoListener); // TODO 仿抖音去掉
            // 获取卖家的个人信息，包含他的直播信息
            OrderInfoHttp.getPersonalInfo(mContext, mGoodsDetailBean.getPersonalInfoBean().getUserId(), 2, onGetPersonalInfoListener);
            advanceDays = mGoodsBasicInfoBean.getGoodsReserveDays();
            sellerId = mGoodsDetailBean.getPersonalInfoBean().getUserId();
            rl_goods_control.setVisibility(View.VISIBLE);
            // 背景图片
            ImageDisplayTools.displayImage(mGoodsBasicInfoBean.getGoodsImg(), iv_down_picture);
            ImageDisplayTools.displayImage(mGoodsBasicInfoBean.getGoodsImg(), iv_up_picture);
            // 标题
            tv_goods_title.setText((goodsType == 6 ? "" : (mGoodsBasicInfoBean.getGoodsAddress() + "|")) + mGoodsBasicInfoBean.getGoodsTitle());
            // 价格
            if (goodsType == 6) {
                tv_original_cost.setText("原价：￥20000");
                tv_original_cost.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                tv_current_cost.setVisibility(View.VISIBLE);
                tv_current_cost.setText("￥" + ShopTool.getMoney(mGoodsBasicInfoBean.getGoodsPrice()));
            } else {
                tv_original_cost.setText("￥" + ShopTool.getMoney(mGoodsBasicInfoBean.getGoodsPrice()) + "/城");
//                tv_original_cost.setText("原价：￥" + ShopTool.getMoney(mGoodsBasicInfoBean.getGoodsPrice()) + "/城");
//                tv_original_cost.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
//                tv_current_cost_desc.setVisibility(View.VISIBLE);
//                tv_current_cost.setVisibility(View.VISIBLE);
//                tv_current_cost.setText("￥1000/城");
            }
            ft = fragmentManager.beginTransaction();
            ft.replace(R.id.fl_container_goods, GoodsInfoFragment.newInstance(travelPlans, "", "", mGoodsOtherInfoBean.getCostImplications()));
            ft.commit();

            if (goodsType == 6) {
                cardRemainCount = mGoodsBasicInfoBean.getRemainCount();
                tv_card_count.setText("还剩" + cardRemainCount + "张");
                tv_current_cost_desc.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onErrorNotZero() {
            tv_goods_undercarriage.setVisibility(View.VISIBLE);
            rl_goods_control.setVisibility(View.GONE);
        }
    };

    // 获取小城卡的数据
    GoodsHttp.CardInfoListener cardInfoListener = new GoodsHttp.CardInfoListener() {
        @Override
        public void isBuyCard(boolean isBuyCard, int count, String date) {
            GoodsActivity.this.count = count;
            if (goodsType == 6) {
//                ll_card_control.setVisibility(View.VISIBLE);// TODO 仿抖音去掉
            } else {
//                cd_card.setVisibility(View.VISIBLE);// TODO 仿抖音去掉
                GoodsHttp.getCardId(mContext, this);
                if (isSupportCard) {
                    if (isBuyCard)
                        tv_cards.setText("还剩" + count + "次");
//                    cd_card.setVisibility(View.VISIBLE);// TODO 仿抖音去掉
                } else
                    cd_card.setVisibility(View.GONE);
            }
        }

        @Override
        public void getCardId(String cardId) {
            GoodsActivity.this.cardId = cardId;
        }
    };

    int index = 0;// 计数，是否所有的couponId都判断是否领取了
    // 获取优惠券的数据
    GoodsHttp.CouponInfoListener couponInfoListener = new GoodsHttp.CouponInfoListener() {
        @Override
        public void getGoodsSupportCoupon(ArrayList<CouponInfoBean> coupons) {
            GoodsActivity.this.coupons.addAll(coupons);
            for (int i = 0; i < coupons.size(); i++) {
                GoodsHttp.isGetCoupon(mContext, coupons.get(i).getCouponId(), i, this);
            }
        }

        @Override
        public void isGetCoupon(boolean isGetCoupon, int i) {
            ++index;
            coupons.get(i).setGet(isGetCoupon);
            if (index == coupons.size()) {
                //                for (int j = coupons.size() - 1; j >= 0; j--) {
                //                    if (coupons.get(j).isGet()) {
                //                        coupons.remove(j);
                //                    }
                //                }
                //                if (coupons.size() == 0)
                //                    cd_coupon.setVisibility(View.GONE);
                //                else {
//                cd_coupon.setVisibility(View.VISIBLE); // TODO 仿抖音去掉
                getCouponAdapter.notifyDataSetChanged();
                //                }
            }
        }

        @Override
        public void getCouponSuccess(int position) {
            coupons.get(position).setGet(true);
            getCouponAdapter.notifyItemChanged(position);
            DialogTemplet dialog = new DialogTemplet(mContext, true, "领取成功！", "确定", "", "");
            dialog.show();
        }
    };

    // 获取到卖家数据
    OrderInfoHttp.OnGetPersonalInfoListener onGetPersonalInfoListener = new OrderInfoHttp.OnGetPersonalInfoListener() {
        @Override
        public void onDataFine(VideoInfoBean videoInfoBean, int flag) {
            mPersonalVideoBean = videoInfoBean;
        }
    };
    SlideHelper.SlideHelperListener ensureListener = new SlideHelper.SlideHelperListener() {

        @Override
        public void onGetSlideData(List<NotifyBean> noticeList) {
            // 保障
            if (noticeList.size() != 0) {
                ensures.addAll(noticeList);
                cd_ensure.setVisibility(View.VISIBLE);
            } else
                cd_ensure.setVisibility(View.GONE);
        }
    };

    String beginTime = "";

    @Override
    protected void onStart() {
        super.onStart();
        beginTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
    }

    String endTime = "";

    @Override
    protected void onStop() {
        super.onStop();
        endTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
        UDPSendInfoBean bean = new UDPSendInfoBean();
        bean.getData("004_" + goodsId, mGoodsBasicInfoBean.getGoodsTitle(),
                ShopConstant.GOODS_INFO + "goodsId=" + goodsId, beginTime, endTime);
        sendData(bean);
    }
}
