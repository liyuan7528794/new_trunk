package com.travel.shop.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.travel.Constants;
import com.travel.ShopConstant;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.R;
import com.travel.shop.activity.OrderInfoActivity;
import com.travel.shop.activity.OrderSuccessActivity;
import com.travel.shop.activity.ZhaoHangPayActivity;
import com.travel.shop.pay.Keys;
import com.travel.shop.pay.PayResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付功能需要用到的方法
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/21
 */
public class PayTool {
    public static final String TAG = "alipay-sdk";
    public static final int ZFB_PAY = 2;
    public static final int ZFB_SIGN = 1;
    private long ordersId;
    // 获取签名
    private String orderInfo;
    private Activity mActivity;
    private Context mContext;
    private String tag;
    private String payMethod;

    // 微信支付
    private IWXAPI api;

    public PayTool(String title, String body, Activity activity, long ordersId, String payPrice, String tag,
                   String payMethod) {
        this.mContext = activity.getApplicationContext();
        this.tag = tag;
        this.ordersId = ordersId;
        this.payMethod = payMethod;
        payDialog(title, body, activity, ordersId, payPrice);
    }

    // 支付
    private Handler payHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ZFB_SIGN:// 签名
                    String sign = msg.obj.toString();
                    try {
                        sign = URLEncoder.encode(sign, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    /** 完整的符合支付宝参数规范的订单信息 */
                    final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();
                    new Thread() {
                        public void run() {
                            // 构造PayTask 对象
                            PayTask alipay = new PayTask(mActivity);
                            // 调用支付接口，获取支付结果
                            String result = alipay.pay(payInfo, true);
                            Message msg = new Message();
                            msg.what = ZFB_PAY;
                            msg.obj = result;
                            payHandler.sendMessage(msg);
                        }

                        ;
                    }.start();
                    break;
                case ZFB_PAY:// 支付结果
                    PayResult payResult = new PayResult((String) msg.obj);
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 向导游支付
                        if (TextUtils.equals(tag, "goodsGuide")) {
                            mContext.sendBroadcast(new Intent("SERVICECODE"));
                            // 红币充值
                        } else if (TextUtils.equals(tag, "redmoney")) {
                            mContext.sendBroadcast(new Intent(Constants.REDMONEY_ALERT));
                            mContext.sendBroadcast(new Intent(Constants.FINISH_RECHARGE));
                            // 线路游支付
                        } else if (TextUtils.equals(tag, "orderRoute")) {
                            mContext.sendBroadcast(new Intent(Constants.MANAGE_PAY));
                        }
                        TravelUtil.showToast(R.string.orderinfo_pay_success, mContext);
                        Intent intent = new Intent(mContext, OrderSuccessActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("ordersId", ordersId);
                        mContext.startActivity(intent);
                    } else {
                        // 判断resultStatus 为非"9000"则代表可能支付失败
                        // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            TravelUtil.showToast(R.string.orderinfo_pay_result_confirming, mContext);
                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            TravelUtil.showToast(R.string.orderinfo_pay_fail, mContext);
                            Intent intent = new Intent(mContext, OrderInfoActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("ordersId", ordersId);
                            mContext.startActivity(intent);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 支付的dialog
     *
     * @param title
     * @param body
     * @param activity
     * @param ordersId
     * @param payPrice
     */
    private void payDialog(String title, String body, Activity activity, long ordersId, String payPrice) {
        mActivity = activity;
        if (TextUtils.equals(payMethod, "alipay")) {
            // 订单详情
            orderInfo = getOrderInfo(title, body, payPrice, ordersId);
            // 签名
            getSign(orderInfo, mContext);
        } else if (TextUtils.equals(payMethod, "wechat")) {
            api = WXAPIFactory.createWXAPI(mActivity, Constants.APP_ID);
            wechatPay(title.length() > 42 ? title.substring(0, 42) : title,
                    body.length() > 42 ? body.substring(0, 42) : body, payPrice, ordersId);
        } else if (TextUtils.equals(payMethod, "zhaohangpay")) {
            Intent intent = new Intent(activity, ZhaoHangPayActivity.class);
            intent.putExtra("billNo", ordersId);
            intent.putExtra("amount", payPrice);
            intent.putExtra("body", body);
            intent.putExtra("title", title);
            intent.putExtra("tag", tag);
            activity.startActivity(intent);
        }

    }

    /**
     * 获取订单详情，用于生成签名
     *
     * @param subject
     * @param body
     * @param price
     * @param ordersId
     * @return
     */
    private String getOrderInfo(String subject, String body, String price, long ordersId) {

        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + Keys.DEFAULT_PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + Keys.DEFAULT_SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + ordersId + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=\"" + ShopConstant.NOTIFY_PAY + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        // orderInfo += "&return_url=\"m.alipay.com\"";
        orderInfo += "&return_url=\"" + ShopConstant.RETURN_PAY + "\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }

    /**
     * 获取签名
     *
     * @param signStr
     * @param mContext
     */
    private void getSign(String signStr, final Context mContext) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("signStr", signStr);
        NetWorkUtil.postForm(mContext, ShopConstant.SIGN, new MResponseListener() {

            @Override
            public void onResponse(JSONObject response) {
                if (response.optInt("error") == 0) {
                    String sign = response.optString("data");
                    Message msg = new Message();
                    msg.obj = sign;
                    msg.what = ZFB_SIGN;
                    payHandler.sendMessage(msg);
                }
            }

        }, map);
    }

    /**
     * 获取签名类型
     *
     * @return
     */
    private String getSignType() {
        return "sign_type=\"RSA\"";
    }

    /**
     * 微信支付
     *
     * @param body
     * @param attach
     * @param payPrice
     * @param ordersId
     */
    private void wechatPay(String body, String attach, String payPrice, final long ordersId) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("out_trade_no", ordersId);
        map.put("total_fee", (int) Float.parseFloat(payPrice));
        map.put("body", body);
        map.put("attach", attach);
        NetWorkUtil.postForm(mContext, ShopConstant.WECHAT_PAY, new MResponseListener() {
            @Override
            protected void onDataFine(JSONObject data) {
                PayReq req = new PayReq();
                try {
                    SharedPreferences sp = mContext.getSharedPreferences("ordersId", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putLong("ordersId", ordersId);
                    editor.commit();
                    req.appId = data.getString("appid");
                    req.partnerId = data.getString("partnerid");
                    req.prepayId = data.getString("prepayid");
                    req.nonceStr = data.getString("noncestr");
                    req.timeStamp = data.getString("timestamp");
                    req.packageValue = data.getString("package");
                    req.sign = data.getString("sign");
                    req.extData = "app data"; // optional
                    api.sendReq(req);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, map);
    }

}
