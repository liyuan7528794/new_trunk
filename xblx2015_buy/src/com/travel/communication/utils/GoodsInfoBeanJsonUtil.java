package com.travel.communication.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.travel.bean.GoodsBasicInfoBean;

/**
 * 用于GoodsInfoBean这个对象与Json的转换
 * Created by ldkxingzhe on 2016/9/7.
 */
public class GoodsInfoBeanJsonUtil {
    @SuppressWarnings("unused")
    private static final String TAG = "GoodsInfoBeanJsonUtil";

    private static final String GOODS_IMG = "goods_img";
    private static final String GOODS_NAME = "goods_name";
    private static final String GOODS_ADDRESS = "goods_address";
    private static final String GOODS_ID = "goods_id";

    private GoodsInfoBeanJsonUtil(){/*util class*/}

    /**
     * 根据string类获得GoodsInfoBean
     * @param goodsInfoBeanStr
     * @return
     */
    public static GoodsBasicInfoBean from(String goodsInfoBeanStr){
    	GoodsBasicInfoBean mGoodsBasicInfoBean = new GoodsBasicInfoBean();
        JsonParser parser = new JsonParser();
        JsonObject goodsInfoObject = parser.parse(goodsInfoBeanStr).getAsJsonObject();
        mGoodsBasicInfoBean.setGoodsImg(goodsInfoObject.get(GOODS_IMG).getAsString());
        mGoodsBasicInfoBean.setGoodsTitle(goodsInfoObject.get(GOODS_NAME).getAsString());
        mGoodsBasicInfoBean.setGoodsId(goodsInfoObject.get(GOODS_ID).getAsString());
        mGoodsBasicInfoBean.setGoodsAddress(goodsInfoObject.get(GOODS_ADDRESS).getAsString());
        return mGoodsBasicInfoBean;
    }

    public static String toJsonStr(GoodsBasicInfoBean mGoodsBasicInfoBean){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(GOODS_IMG, mGoodsBasicInfoBean.getGoodsImg());
        jsonObject.addProperty(GOODS_ID, mGoodsBasicInfoBean.getGoodsId());
        jsonObject.addProperty(GOODS_NAME, mGoodsBasicInfoBean.getGoodsTitle());
        jsonObject.addProperty(GOODS_ADDRESS, mGoodsBasicInfoBean.getGoodsAddress());
        return jsonObject.toString();
    }
}
