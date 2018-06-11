package com.travel.shop.widget;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.travel.Constants;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.R;
import com.travel.shop.tools.PayTool;

import java.math.BigDecimal;

public class PayMethodPopWindow extends PopupWindow {

    private static boolean isInit = false;

    private Context mContext;
    private String title;
    private Activity mActivity;
    private long ordersId;
    private String price;
    private String tag;
    private int type;
    private int flag;

    private View rootView;
    private RadioGroup rg_pay_method;
    private RadioButton rb_alipay;
    private RadioButton rb_wechatpay;
    private RadioButton rb_zhaohangpay;
    private RadioButton rb_actualpay;
    private TextView tv_pay_cancel;
    private int method;

    /**
     * 初始化
     *
     * @param mContext
     * @param title
     * @param mActivity
     * @param ordersId
     * @param price     付款金额
     * @param tag       "goodsGuide"：向导游支付 "orderRoute"：线路游支付 "redmoney"：红币充值
     * @param type      1：有线下支付 2：无线下支付
     * @param flag      1：下单时弹出 2：订单中弹出
     */
    public PayMethodPopWindow(Context mContext, String title, Activity mActivity, long ordersId, String price,
                              String tag, int type, int flag) {
        if (isInit) {
            isInit = false;
            return;
        }
        isInit = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    isInit = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        this.mContext = mContext;
        this.title = title;
        this.mActivity = mActivity;
        this.ordersId = ordersId;
        this.price = price;
        this.tag = tag;
        this.type = type;
        this.flag = flag;
        method = 1;

        rootView = View.inflate(mContext, R.layout.popwindow_pay_method, null);
        initView();
        initPop();
    }

    /**
     * 支付方式的选择的布局的初始化以及控件的点击
     */
    private void initView() {
        rg_pay_method = (RadioGroup) rootView.findViewById(R.id.rg_pay_method);
        rb_alipay = (RadioButton) rootView.findViewById(R.id.rb_alipay);
        rb_wechatpay = (RadioButton) rootView.findViewById(R.id.rb_wechatpay);
        rb_zhaohangpay = (RadioButton) rootView.findViewById(R.id.rb_zhaohangpay);
        rb_actualpay = (RadioButton) rootView.findViewById(R.id.rb_actualpay);
        tv_pay_cancel = (TextView) rootView.findViewById(R.id.tv_pay_cancel);
        if (type == 1)
            rb_actualpay.setVisibility(View.VISIBLE);
        else if (type == 2)
            rb_actualpay.setVisibility(View.GONE);
        rb_zhaohangpay.setText("招行支付");
        rb_wechatpay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundAlpha(1);
                dismiss();
                if (isWXAppInstalledAndSupported()) {
                    new PayTool(title, ("redmoney".equals(tag) ? "CZ-" : "DD-") + title, mActivity, ordersId,
                            // "1", tag, "wechat");
                            new BigDecimal(price).multiply(new BigDecimal(100)).toString(), tag, "wechat");
                } else
                    TravelUtil.showToast("请先安装微信");
            }
        });
        rb_alipay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundAlpha(1);
                dismiss();
                new PayTool(title, ("redmoney".equals(tag) ? "CZ-" : "DD-") + title, mActivity, ordersId, price,
                        tag, "alipay");
            }
        });
        rb_zhaohangpay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundAlpha(1);
                dismiss();
                new PayTool(title, ("redmoney".equals(tag) ? "CZ-" : "DD-") + title, mActivity, ordersId, price,
                        tag, "zhaohangpay");
            }
        });

        tv_pay_cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                backgroundAlpha(1);
                dismiss();
            }
        });
    }

    /**
     * 初始化popWindow
     */
    private void initPop() {

        this.setContentView(rootView);
        this.setWidth(OSUtil.getScreenWidth());
        this.setFocusable(true);
        this.setHeight(OSUtil.dp2px(mContext, 302));
        this.setBackgroundDrawable(null);
        this.setAnimationStyle(R.style.belowPupWindowAnimation);

        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1);
                if (flag == 1)
                    OSUtil.intentOrderInfo(mContext, ordersId);
            }
        });
        this.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        backgroundAlpha(0.4f);
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    private void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        mActivity.getWindow().setAttributes(lp);
    }

    private boolean isWXAppInstalledAndSupported() {
        IWXAPI msgApi = WXAPIFactory.createWXAPI(mActivity, null);
        msgApi.registerApp(Constants.APP_ID);

        boolean sIsWXAppInstalledAndSupported = msgApi.isWXAppInstalled() && msgApi.isWXAppSupportAPI();

        return sIsWXAppInstalledAndSupported;
    }
}
