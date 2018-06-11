package com.travel.shop.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.travel.ShopConstant;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.R;
import com.travel.shop.bean.OrderBean;
import com.travel.shop.tools.ShopTool;

import org.json.JSONObject;

import java.util.HashMap;


/**
 * 申请众投
 *
 * @author WYP
 * @version 1.0
 * @created 2016/07/11
 */
public class ApplicationPublicVoteActivity extends TitleBarBaseActivity implements View.OnClickListener {

    private Context mContext;
    private LinearLayout ll_app_vote;

    // 订单选择前显示相关
    private RelativeLayout rl_add_orders;
    private TextView tv_over_money, tv_public_vote_sure;
    // 订单选择后显示相关
    private LinearLayout ll_orders_info;
    private TextView tv_apply_vote_orders_num, tv_apply_vote_payment, tv_apply_vote_goods_name, tv_apply_vote_address;
    private EditText et_money_input, et_reason_input;

    // 数据显示相关
    private long ordersId;
    private String payForMoney;
    private float totalPrice = -1;
    private int counter;
    private String sellerId, buyerId;

    // 从订单详情页进入
    private OrderBean mOrderBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_public_vote);

        init();
        initListener();

        ll_app_vote.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                OSUtil.hideKeyboard(ApplicationPublicVoteActivity.this);
                return false;
            }
        });
    }

    /**
     * 控件以及数据初始化
     */
    private void init() {
        mContext = this;

        ll_app_vote = findView(R.id.ll_app_vote);
        rl_add_orders = findView(R.id.rl_add_orders);
        tv_apply_vote_orders_num = findView(R.id.tv_apply_vote_orders_num);
        tv_apply_vote_payment = findView(R.id.tv_apply_vote_payment);
        tv_apply_vote_goods_name = findView(R.id.tv_apply_vote_goods_name);
        tv_apply_vote_address = findView(R.id.tv_apply_vote_address);
        tv_over_money = findView(R.id.tv_over_money);
        tv_public_vote_sure = findView(R.id.tv_public_vote_sure);
        ll_orders_info = findView(R.id.ll_orders_info);
        et_money_input = findView(R.id.et_money_input);
        et_reason_input = findView(R.id.et_reason_input);

        ImageDisplayTools.initImageLoader(mContext);
        setTitle(getString(R.string.orderinfo_application_public_invest));
        // 保险不能超过金额 TODO 暂时用总价代替
        tv_over_money.setText(
                getString(R.string.application_public_not_over_money) + "您的支付金额");
        mOrderBean = (OrderBean) getIntent().getSerializableExtra("orderInfo");
        if (mOrderBean != null) {
            setData(mOrderBean);
        }
    }

    private void initListener() {
        rl_add_orders.setOnClickListener(this);
        tv_public_vote_sure.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        // 输入的赔偿金额的限制
        et_money_input.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (totalPrice != -1) {
                    payForMoney = s.toString();
                    counter = 0;
                    if (countStr(payForMoney, ".") > 1 || payForMoney.indexOf(".") == 0) {
                        showToast("请输入合法的金额");
                        et_money_input.setText("");
                    } else if (!TextUtils.isEmpty(payForMoney) && Float.parseFloat(payForMoney) > totalPrice) {
                        payForMoney = ShopTool.getMoney(totalPrice + "");
                        et_money_input.setText(payForMoney);
                    }
                } else
                    showToast("请先选择您要众投的订单");
            }
        });

    }

    /**
     * UI赋值
     */
    private void setData(OrderBean mOrderBean) {
        rl_add_orders.setVisibility(View.GONE);
        ll_orders_info.setVisibility(View.VISIBLE);
        OSUtil.setShareParam(rightButton, "alter", mContext);
        int goodsType = mOrderBean.getGoodsType();
        ordersId = mOrderBean.getOrdersId();
        totalPrice = mOrderBean.getPaymentPrice();
        sellerId = mOrderBean.getSalerId();
        buyerId = mOrderBean.getBuyerId();
        // 订单号
        tv_apply_vote_orders_num.setText(ordersId + "");
        // 总价-->支付金额
        tv_apply_vote_payment.setText("￥" + ShopTool.getMoney(totalPrice + ""));
        // 商品名称
        tv_apply_vote_goods_name.setText(mOrderBean.getGoodsTitle());
        // 商品地址
        if (goodsType != 6) {
            tv_apply_vote_address.setVisibility(View.VISIBLE);
            String address;
            //            if (goodsType == 1 || goodsType == 2)
            //                address = "出发地：";
            //            else
            address = "目的地：";
            tv_apply_vote_address.setText(address + mOrderBean.getGoodsAddress());
        }
        et_money_input.setText("");
        et_reason_input.setText("");
        // 保险不能超过金额 TODO 暂时用总价代替
        tv_over_money.setText(
                getString(R.string.application_public_not_over_money) + ShopTool.getMoney(totalPrice + "") + "元");
    }

    /**
     * 确定申请众投
     */
    private void applyPublicVote() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("ordersId", ordersId);
        map.put("sellerId", sellerId);
        map.put("buyerId", buyerId);
        map.put("claimAmount", payForMoney);
        map.put("reason", et_reason_input.getText().toString());
        NetWorkUtil.postForm(mContext, ShopConstant.APPLY_PUBLIC_VOTE, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    showToast("提交成功！");
                    finish();
                }
            }
        }, map);

    }

    /**
     * 判断输入的金额中包含“.”的个数
     *
     * @param str1
     * @param str2
     * @return counter
     */
    private int countStr(String str1, String str2) {
        if (str1.indexOf(str2) == -1) {
            return 0;
        } else {
            counter++;
            countStr(str1.substring(str1.indexOf(str2) + 1), str2);
            return counter;
        }
    }

    @Override
    public void onClick(final View v) {
        // 禁止双击
        v.setEnabled(false);
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                v.setEnabled(true);
            }
        }, 500);
        if (v == rl_add_orders || v == rightButton) {// 添加订单
            startActivityForResult(new Intent(mContext, SelectOrdersActivity.class), 0);
        } else if (v == tv_public_vote_sure) {// 确定发起众投
            // 赔偿金额不能为空
            if (!TextUtils.isEmpty(payForMoney)) {
                applyPublicVote();
            } else
                showToast(getString(R.string.application_public_money_input));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            OrderBean mOrderBean = (OrderBean) data.getSerializableExtra("orderBean");
            setData(mOrderBean);
        }
    }
}
