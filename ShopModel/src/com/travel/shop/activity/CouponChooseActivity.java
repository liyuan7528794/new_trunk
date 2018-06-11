package com.travel.shop.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.R;
import com.travel.shop.adapter.CouponChooseAdapter;
import com.travel.shop.bean.CouponInfoBean;
import com.travel.shop.http.CommitHttp;
import com.travel.shop.tools.ShopTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * 卡券选择页以及红钱袋
 *
 * @author WYP
 * @version 1.0
 * @created 2017/01/17
 */
public class CouponChooseActivity extends TitleBarBaseActivity {

    private Context mContext;

    //支付金额
    private TextView tv_couponchoose_pay, tv_couponchoose_coupon_pay;
    private String pay;
    private float currentPrice;
    // 卡券相关
    private TextView tv_no_coupon;
    private RecyclerView rv_couponchoose;
    private ArrayList<CouponInfoBean> coupons;
    private CouponChooseAdapter mCouponChooseAdapter;
    private String couponId = "";// 选择的卡券的id
    private int oldPosition = -1;// 选的前一个卡券
    private float discountPrice = 0;// 优惠金额

    private LinearLayout ll_coupon;
    private int tag;// 0:卡券选择页 1:红钱袋页

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_choose);
        initView();
        initData();
        initListener();

        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection()) && tag == 1) {
            CommitHttp.getCouponData(mContext, -1, mListener);
        }
    }

    private void initView() {
        mContext = this;

        tv_couponchoose_pay = findView(R.id.tv_couponchoose_pay);
        tv_couponchoose_coupon_pay = findView(R.id.tv_couponchoose_coupon_pay);
        tv_no_coupon = findView(R.id.tv_no_coupon);
        rv_couponchoose = findView(R.id.rv_couponchoose);
        ll_coupon = findView(R.id.ll_coupon);
    }
    // 红钱袋新加

    private void initData() {
        tag = getIntent().getIntExtra("tag", 0);
        OSUtil.setShareParam(rightButton, "sure", mContext);
        rightButton.setTextSize(16);
        if (tag == 1) {
            setTitle("红钱袋");
            rightButton.setTextColor(ContextCompat.getColor(mContext, R.color.blue_508CEE));
            rightButton.setText("激活");
            coupons = new ArrayList<>();
            mCouponChooseAdapter = new CouponChooseAdapter(coupons, mContext);
            rv_couponchoose.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            rv_couponchoose.setAdapter(mCouponChooseAdapter);
        } else {
            setTitle("优惠券选择");
            rightButton.setTextColor(ContextCompat.getColor(mContext, R.color.red_EC6262));
            coupons = (ArrayList<CouponInfoBean>) getIntent().getSerializableExtra("couponData");
            couponId = getIntent().getStringExtra("couponId");
            pay = getIntent().getStringExtra("price");
            String temp = getIntent().getStringExtra("discountPrice");
            discountPrice = TextUtils.isEmpty(temp) ? 0 : Float.parseFloat(temp);
            if (coupons.size() == 0) {
                rv_couponchoose.setVisibility(View.GONE);
                tv_no_coupon.setVisibility(View.VISIBLE);
            } else {
                ArrayList<CouponInfoBean> couponData = new ArrayList<>();
                for (CouponInfoBean bean : coupons) {
                    // 只显示当前日期以后的券
                    if (System.currentTimeMillis() >= ShopTool.getSomeTimeMillions(bean.getStartDate() + " 00:00:00")) {
                        // 总价小于卡券面额时置灰或者总价小于最低使用金额时置灰
                        if (Float.parseFloat(pay) < Float.parseFloat(bean.getCurrentPrice()))
                            bean.setStatus(-1);
                        if (Float.parseFloat(pay) < Float.parseFloat(bean.getRequirMoney()))
                            bean.setStatus(-2);
                        couponData.add(bean);
                    }
                }
                coupons.clear();
                coupons.addAll(couponData);
                if (coupons.size() == 0) {
                    rv_couponchoose.setVisibility(View.GONE);
                    tv_no_coupon.setVisibility(View.VISIBLE);
                } else {
                    Collections.sort(coupons);
                    mCouponChooseAdapter = new CouponChooseAdapter(coupons, mContext);
                    rv_couponchoose.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                    rv_couponchoose.setAdapter(mCouponChooseAdapter);
                }
            }
            pay = ShopTool.getMoney(Float.parseFloat(pay) - discountPrice + "");
            ll_coupon.setVisibility(View.VISIBLE);
            tv_couponchoose_pay.setText("现金支付：￥" + pay);
            tv_couponchoose_coupon_pay.setText("优惠券支付：￥" + ShopTool.getMoney(discountPrice + ""));
            currentPrice = Float.parseFloat(pay);
        }
    }

    private void initListener() {
        if (mCouponChooseAdapter != null && tag == 0)
            mCouponChooseAdapter.setmOnItemClickListener(new CouponChooseAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(RecyclerView.ViewHolder holder) {
                    int position = holder.getAdapterPosition();
                    if (coupons.get(position).getStatus() == -2) {
                        showToast("此券需满" + coupons.get(position).getRequirMoney() + "元才可使用");
                        return;
                    }
                    float currentCouponPrice = Float.parseFloat(coupons.get(position).getCurrentPrice());
                    if (coupons.get(position).isChoosed()) {
                        coupons.get(position).setChoosed(false);
                        currentPrice += currentCouponPrice;
                        discountPrice -= currentCouponPrice;
                        couponId = couponId.replace(coupons.get(position).getCouponId() + ",", "");
                        if (position == oldPosition)
                            oldPosition = -1;
                    } else {
                        int oldAdded = 1;
                        int currentAdded = 1;
                        if (discountPrice == 0) {
                            oldAdded = 1;
                            currentAdded = 1;
                        }
                        if (oldPosition != -1) {// 首次进入多选
                            oldAdded = coupons.get(oldPosition).getAdded();
                            currentAdded = coupons.get(position).getAdded();
                        } else if (!TextUtils.isEmpty(couponId)) {// 再次进入多选
                            for (int i = 0; i < coupons.size(); i++) {
                                if (Arrays.asList(couponId.split(",")).contains(coupons.get(i).getCouponId())) {
                                    oldAdded = coupons.get(i).getAdded();
                                    currentAdded = coupons.get(position).getAdded();
                                    break;
                                }
                            }
                        }
                        if (oldAdded == 1 && currentAdded == 1)
                            if (currentPrice >= currentCouponPrice) {
                                oldPosition = position;
                                coupons.get(position).setChoosed(true);
                                currentPrice -= currentCouponPrice;
                                discountPrice += currentCouponPrice;
                                couponId += coupons.get(position).getCouponId() + ",";
                            } else
                                showToast("此券面值过大不可选");
                        else
                            showToast("不可叠加此优惠券");
                    }
                    tv_couponchoose_pay.setText("现金支付：￥" + ShopTool.getMoney(currentPrice + ""));
                    tv_couponchoose_coupon_pay.setText("优惠券支付：￥" + ShopTool.getMoney(discountPrice + ""));
                    mCouponChooseAdapter.notifyItemChanged(position);
                }
            });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tag == 1)
                    startActivity(new Intent(mContext, ActivateCouponActivity.class));
                else {
                    Intent intent = new Intent();
                    intent.putExtra("discountPrice", discountPrice);
                    intent.putExtra("currentPrice", currentPrice);
                    intent.putExtra("couponId", couponId);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    CommitHttp.CouponListener mListener = new CommitHttp.CouponListener() {
        @Override
        public void getCouponData(ArrayList<CouponInfoBean> couponData) {
            if (couponData.size() == 0) {// 无可用的优惠券
                rv_couponchoose.setVisibility(View.GONE);
                tv_no_coupon.setVisibility(View.VISIBLE);
            } else {
                coupons.clear();
                coupons.addAll(couponData);
                mCouponChooseAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection()) && tag == 1) {
            CommitHttp.getCouponData(mContext, -1, mListener);
        }
    }
}
