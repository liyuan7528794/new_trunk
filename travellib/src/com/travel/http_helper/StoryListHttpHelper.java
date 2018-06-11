package com.travel.http_helper;

import android.content.Context;
import android.text.TextUtils;

import com.travel.ShopConstant;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by wyp on 2017/1/17.
 */

public class StoryListHttpHelper {
    public interface OutStoriesHttpListener {
        // 成功获取数据
        void getStoriesList(ArrayList<GoodsBasicInfoBean> goodsList, int flag);

    }

    /**
     * 获取故事列表
     *
     * @param mContext
     * @param flag      1:获取首页的故事 2:获取推荐的故事 3:主页中获取故事
     * @param value
     * @param mListener
     */
    public static void getStoriesList(final Context mContext, final int flag, String value, int page, final OutStoriesHttpListener mListener) {
        Map<String, Object> map = new HashMap<>();
        String url = ShopConstant.MORE_STORY;
        if (flag == 1) {
            map.put("topStatus", 1);
            if (TextUtils.equals("command", value))
                url = ShopConstant.MORE_STORY_NO_SORT;
        }else if(flag == 2){
            map.put("topStatus", 2);
            map.put("pageNo", page);
        }else if(flag == 3){
            map.put("userId", value);
            map.put("pageNo", page);
        }
        NetWorkUtil.postForm(mContext, url, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                ArrayList<GoodsBasicInfoBean> goodsList = new ArrayList<>();
                try {
                    // 商品数据
                    for (int i = 0; i < data.length(); i++) {
                        GoodsBasicInfoBean mGoodsBasicInfoBean = new GoodsBasicInfoBean();
                        JSONObject shopCoversObject = data.getJSONObject(i);
                        // 故事Id
                        mGoodsBasicInfoBean.setStoryId(shopCoversObject.optString("id"));
                        // 背景图片
                        mGoodsBasicInfoBean.setGoodsImg(shopCoversObject.optString("imgUrl"));
                        // 标题
                        mGoodsBasicInfoBean.setGoodsTitle(shopCoversObject.optString("title"));
                        // 副标题
                        mGoodsBasicInfoBean.setSubhead(shopCoversObject.optString("subhead"));
                        // 首页显示的图片
                        mGoodsBasicInfoBean.setTopImage(shopCoversObject.optString("topImg"));
                        mGoodsBasicInfoBean.setKeyWord(shopCoversObject.optString("keyWord"));
                        // 阅读数
                        mGoodsBasicInfoBean.setReadCount(shopCoversObject.optInt("watchNum"));
                        // 评论数
                        mGoodsBasicInfoBean.setCommentCount(shopCoversObject.optInt("commentNum"));
                        goodsList.add(mGoodsBasicInfoBean);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mListener.getStoriesList(goodsList, flag);
                }

            }

        }, map);
    }
}
