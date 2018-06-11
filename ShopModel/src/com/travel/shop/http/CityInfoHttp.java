package com.travel.shop.http;

import android.content.Context;

import com.travel.ShopConstant;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.shop.bean.CityBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 城市页相关的网络请求
 * Created by wyp on 2017/1/19.
 */

public class CityInfoHttp {

    public interface CityInfoListener {
        // 获取城市数据
        void getCityInfo(CityBean mCityBean);

        // 获取城市天气数据
        void getCityWeatherInfo(CityBean mCityBean);

        // 获取城市故事列表数据
        void getCityStoryList(ArrayList<GoodsBasicInfoBean> mList);
    }

    private static HashMap<String, Object> map;

    /**
     * 获取城市信息
     */
    public static void getCityInfo(final Context mContext, String cityId, final CityInfoListener mListener) {
        map = new HashMap<>();
        map.put("cityId", cityId);
        NetWorkUtil.postForm(mContext, ShopConstant.CITY_INFO, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONObject data) {
                CityBean mCityBean = new CityBean();
                // 城市图片
                mCityBean.setImgUrl(data.optString("imgUrl"));
                mCityBean.setGroupId(data.optString("groupId"));
                // 城市介绍
                mCityBean.setCityDescribe(data.optString("cityDescribe"));
                // 群聊名称
                mCityBean.setGroupName(data.optString("groupName"));
                // 群聊图标
                mCityBean.setGroupImageUrl(data.optString("groupImgUrl"));
                mListener.getCityInfo(mCityBean);
            }
        }, map);
    }

    /**
     * 获取城市天气信息
     */
    public static void getCityWeatherInfo(final Context mContext, String cityId, String city, final CityInfoListener mListener) {
        map = new HashMap<>();
        map.put("cityId", cityId);
        map.put("city", city);
        NetWorkUtil.postForm(mContext, ShopConstant.CITY_WEATHER_INFO, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONObject data) {
                CityBean mCityBean = new CityBean();
                // 温度
                mCityBean.setTemperature(data.optString("temp"));
                // 天气
                mCityBean.setWeather(data.optString("weather"));
                // 风向
                mCityBean.setWind(data.optString("winddirect"));
                // 风向等级
                mCityBean.setWindLevel(data.optString("windpower"));
                // 湿度
                mCityBean.setHumidity(data.optString("humidity"));
                // 空气质量
                mCityBean.setQuality(data.optString("quality"));
                mListener.getCityWeatherInfo(mCityBean);
            }
        }, map);
    }

    /**
     * 获取城市故事列表
     */
    public static void getStoryList(final Context mContext, String userId, String city, final int page, final CityInfoListener mListener) {
        map = new HashMap<>();
        if (city != null && !"".equals(city)) {
            map.put("keyWord", city);
            map.put("topStatus", 0);
            map.put("showStatus", 1);
        }
        if (userId != null && !"".equals(userId))
            map.put("userId", userId);
        if (page != 0)
            map.put("pageNo", page);
        NetWorkUtil.postForm(mContext, ShopConstant.CITY_STORY_LIST, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                ArrayList<GoodsBasicInfoBean> mList = new ArrayList<>();
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject dataObject = data.getJSONObject(i);
                        GoodsBasicInfoBean mGoodsBasicInfoBean = new GoodsBasicInfoBean();
                        // 故事Id
                        mGoodsBasicInfoBean.setStoryId(dataObject.optString("id"));
                        // 商品Id
                        mGoodsBasicInfoBean.setGoodsId(dataObject.optString("goodsId"));
                        // 故事封面
                        mGoodsBasicInfoBean.setGoodsImg(dataObject.optString("imgUrl"));
                        // 故事标题
                        mGoodsBasicInfoBean.setGoodsTitle(dataObject.optString("title"));
                        // 故事副标题
                        mGoodsBasicInfoBean.setSubhead(dataObject.optString("subhead"));
                        // 阅读数
                        mGoodsBasicInfoBean.setReadCount(dataObject.optInt("watchNum"));
                        // 评论数
                        mGoodsBasicInfoBean.setCommentCount(dataObject.optInt("commentNum"));
                        mList.add(mGoodsBasicInfoBean);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mListener.getCityStoryList(mList);
                }
            }
        }, map);
    }

    /**
     * 获取城市故事列表
     */
    public static void getBoxRommList(final Context mContext, String userId, final int page, final CityInfoListener mListener) {
        map = new HashMap<>();
        if (userId != null && !"".equals(userId))
            map.put("userId", userId);
        if (page != 0)
            map.put("pageNo", page);
        NetWorkUtil.postForm(mContext, ShopConstant.BOX_ROOM_LIST, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                ArrayList<GoodsBasicInfoBean> mList = new ArrayList<>();
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject dataObject = data.getJSONObject(i);
                        GoodsBasicInfoBean mGoodsBasicInfoBean = new GoodsBasicInfoBean();
                        // 故事Id
                        mGoodsBasicInfoBean.setStoryId(dataObject.optString("id"));
                        // 商品Id
                        mGoodsBasicInfoBean.setGoodsId(dataObject.optString("goodsId"));
                        // 故事封面
                        mGoodsBasicInfoBean.setGoodsImg(dataObject.optString("imgUrl"));
                        // 故事标题
                        mGoodsBasicInfoBean.setGoodsTitle(dataObject.optString("title"));
                        // 阅读数
                        mGoodsBasicInfoBean.setReadCount(dataObject.optInt("watchNum"));
                        // 评论数
                        mGoodsBasicInfoBean.setCommentCount(dataObject.optInt("commentNum"));
                        mList.add(mGoodsBasicInfoBean);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mListener.getCityStoryList(mList);
                }
            }
        }, map);
    }
}
