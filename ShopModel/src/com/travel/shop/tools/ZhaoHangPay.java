package com.travel.shop.tools;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import com.travel.Constants;
import com.travel.lib.utils.OSUtil;

public class ZhaoHangPay {

    private Activity mActivity;
    private String tag;
    private long ordersId;

    public ZhaoHangPay(Activity mActivity, String tag, long ordersId) {
        this.mActivity = mActivity;
        this.tag = tag;
        this.ordersId = ordersId;
    }

    @JavascriptInterface
    public void close() {
        OSUtil.intentOrderSuccess(mActivity, ordersId);
        mActivity.finish();
        // 向导游支付
        if (TextUtils.equals(tag, "goodsGuide")) {
            mActivity.sendBroadcast(new Intent("SERVICECODE"));
            // 红币充值
        } else if (TextUtils.equals(tag, "redmoney")) {
            mActivity.sendBroadcast(new Intent(Constants.REDMONEY_ALERT));
            mActivity.sendBroadcast(new Intent(Constants.FINISH_RECHARGE));
            // 线路游支付
        } else if (TextUtils.equals(tag, "orderRoute")) {
            mActivity.sendBroadcast(new Intent(Constants.MANAGE_PAY));
        }
    }
}
