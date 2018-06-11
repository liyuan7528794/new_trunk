package com.travel.shop.http;

import android.content.Context;

import com.travel.ShopConstant;
import com.travel.bean.EvaluateInfoBean;
import com.travel.bean.PhotoModel;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wyp on 2017/3/9.
 */

public class GoodsEvaluateHttp {
    public interface GoodsEvaluateListener {
        void onSuccess(ArrayList<EvaluateInfoBean> list);
    }

    /**
     * 获取评价的数据
     */
    public static void evaluateData(final Context mContext, String goodsId, final GoodsEvaluateListener mListener) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("times", 1);// TODO 不分页
        map.put("goodsId", goodsId);
        NetWorkUtil.postForm(mContext, ShopConstant.EVALUATE_INFO, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                ArrayList<EvaluateInfoBean> mList = new ArrayList<>();
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject dataObject = data.getJSONObject(i);
                        EvaluateInfoBean mEvaluateInfoBean = new EvaluateInfoBean();
                        // 时间
                        mEvaluateInfoBean.setEvaluateTime(dataObject.optString("createTime"));
                        // 星评
                        mEvaluateInfoBean.setEvaluateStar(dataObject.optInt("evaluateType"));
                        // 评论
                        mEvaluateInfoBean.setEvaluateContent(dataObject.optString("content"));
                        // 图片
                        if (!"".equals(dataObject.optString("imgs"))) {
                            ArrayList<PhotoModel> pictures = new ArrayList<PhotoModel>();
                            String pics[] = dataObject.optString("imgs").split(",");
                            for (int j = 0; j < pics.length; j++) {
                                PhotoModel pic = new PhotoModel();
                                pic.setOriginalPath(pics[j]);
                                pic.setOriginalPathBig(pics[j]);
                                pictures.add(pic);
                            }
                            mEvaluateInfoBean.setEvaluatePictures(pictures);
                        }
                        JSONObject userObject = dataObject.getJSONObject("user");
                        // 评价者id
                        mEvaluateInfoBean.setEvaluateUserId(userObject.optString("id"));
                        // 头像
                        mEvaluateInfoBean.setEvaluateUserPhoto(userObject.optString("imgUrl"));
                        // 姓名
                        mEvaluateInfoBean.setEvaluateUserName(userObject.optString("nickName"));
                        mList.add(mEvaluateInfoBean);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mListener.onSuccess(mList);
                }
            }
        }, map);
    }
}
