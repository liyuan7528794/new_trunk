package com.travel.shop.http;

import android.content.Context;

import com.travel.ShopConstant;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * 激活页相关的网络请求
 * Created by wyp on 2017/2/6.
 */

public class ActivateCouponHttp {

    public interface ActivateCouponListener {
        // 发送成功
        void sendSuccess();

        // 激活卡券
        void activateCoupon();
    }

    private static HashMap<String, Object> map;

    /**
     * 获取验证码
     */
    public static void getVerificationCode(final Context mContext, HashMap<String, Object> map, final ActivateCouponListener mListener) {
        NetWorkUtil.postForm(mContext, ShopConstant.ACTIVATE_VERIFICATION_CODE, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0)
                    mListener.sendSuccess();
            }
        }, map);
    }

    /**
     * 激活卡券
     */
    public static void onActivateCoupon(final Context mContext, HashMap<String, Object> map, final ActivateCouponListener mListener) {
        NetWorkUtil.postForm(mContext, ShopConstant.ACTIVATE, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0)
                    mListener.activateCoupon();
            }
        }, map);
    }

}
