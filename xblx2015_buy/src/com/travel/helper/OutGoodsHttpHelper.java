package com.travel.helper;

import android.content.Context;

import com.travel.ShopConstant;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取首页的推荐商品的数据
 * Created by wyp on 2017/3/2.
 */

public class OutGoodsHttpHelper {

    public interface OutGoodsHttpListener {
        // 成功获取数据
        void getGoodsList(ArrayList<GoodsBasicInfoBean> goodsList);

        // 获取失败
        void onError();
    }

    public static void getGoodsList(final Context mContext, final int mPage, final OutGoodsHttpListener mListener) {
        Map<String, Object> map = new HashMap<>();
        map.put("times", mPage);
        NetWorkUtil.postForm(mContext, ShopConstant.OUT_GOODS, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                ArrayList<GoodsBasicInfoBean> goodsList = new ArrayList<>();
                try {
                    // 商品数据
                    for (int i = 0; i < data.length(); i++) {
                        GoodsBasicInfoBean mGoodsBasicInfoBean = new GoodsBasicInfoBean();
                        JSONObject shopCoversObject = data.getJSONObject(i);
                        // 故事封面大小类型
                        mGoodsBasicInfoBean.setGoodsLayout(shopCoversObject.optInt("template"));
                        // 故事Id
                        mGoodsBasicInfoBean.setStoryId(shopCoversObject.optString("groupId"));
                        // 背景图片
                        mGoodsBasicInfoBean.setGoodsImg(shopCoversObject.optString("imgUrl"));
                        // 标题
                        mGoodsBasicInfoBean.setGoodsTitle(shopCoversObject.optString("name"));
                        // 副标题
                        mGoodsBasicInfoBean.setSubhead(shopCoversObject.optString("subhead"));
                        goodsList.add(mGoodsBasicInfoBean);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mListener.getGoodsList(goodsList);
                }

            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                mListener.onError();
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                mListener.onError();
            }
        }, map);
    }
}
