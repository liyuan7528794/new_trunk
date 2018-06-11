package com.travel.shop.http;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.travel.Constants;

public class StoryInfoH5 {

    private Context mContext;

    public StoryInfoH5(Context mContext) {
        this.mContext = mContext;
    }

    // 购买小城卡
    @JavascriptInterface
    public void goodsDetail(int goodsId) {
        Intent intent = new Intent();
        intent.setAction(Constants.GOODS_ACTION);
        intent.setType(Constants.VIDEO_TYPE);
        intent.putExtra("goodsId", goodsId + "");
        mContext.startActivity(intent);
    }

}
