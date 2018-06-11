/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 mob.com. All rights reserved.
 */

package com.ctsmedia.hltravel.wxapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.travel.Constants;
import com.travel.lib.R;
import com.travel.lib.utils.OSUtil;

/**
 * 微信客户端回调activity示例
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq arg0) {

    }

    @Override
    public void onResp(BaseResp resp) {

        if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
            Toast.makeText(this, "code = " + ((SendAuth.Resp) resp).code, Toast.LENGTH_SHORT).show();
        }

        int result = 0;

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                sendBroadcast(new Intent("SERVICECODE"));
                sendBroadcast(new Intent(Constants.REDMONEY_ALERT));
                sendBroadcast(new Intent(Constants.FINISH_RECHARGE));
                sendBroadcast(new Intent(Constants.MANAGE_PAY));
                result = R.string.errcode_success;
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = R.string.errcode_cancel;
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = R.string.errcode_deny;
                break;
            default:
                result = R.string.errcode_unknown;
                break;
        }

        SharedPreferences sp = getSharedPreferences("ordersId", Context.MODE_PRIVATE);
        long ordersId = sp.getLong("ordersId", 0);
        if (result == R.string.errcode_success) {
            OSUtil.intentOrderSuccess(this, ordersId);
        } else {
            OSUtil.intentOrderInfo(this, ordersId);
        }
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        finish();
    }

}
