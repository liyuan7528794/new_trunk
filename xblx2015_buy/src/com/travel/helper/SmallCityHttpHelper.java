package com.travel.helper;

import android.content.Context;

import com.google.gson.Gson;
import com.travel.ShopConstant;
import com.travel.lib.ui.LoadingDialog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.shop.bean.CityBean;
import com.travel.shop.bean.SmallCityBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 发现页的小城标签下的数据的获取
 * Created by wyp on 2018/5/15.
 */

public class SmallCityHttpHelper {

    public interface CityDataListener {
        void onSuccess(ArrayList<SmallCityBean> datas);

        void onFail();
    }

    public static void getCityData(final Context context, int type, final CityDataListener cityDataListener) {
        getCityData(context, type, 1, cityDataListener);

    }
    public static void getCityData(final Context context, int type, int page, final CityDataListener cityDataListener) {
        Map<String, Object> map = new HashMap<>();
        map.put("cityType", type);
        map.put("pageNo", page);
        NetWorkUtil.postForm(context, ShopConstant.DISCOVER_CITY_LIST, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                ArrayList<SmallCityBean> cityData = new ArrayList<>();
                try {
                    if (data != null && data.length() > 0) {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject dataObject = data.getJSONObject(i);
                            SmallCityBean smallCityBean = new SmallCityBean();
                            // id为1取得是小城，2为推荐故事，3为大城小事
                            smallCityBean.setId(dataObject.optString("id"));
                            if (!dataObject.isNull("list") && dataObject.getJSONArray("list").length() > 0) {
                                JSONArray jsonArray = dataObject.getJSONArray("list");
                                ArrayList<CityBean> cityBeans = new ArrayList<>();
                                for (int j = 0; j < jsonArray.length(); j++) {
                                    Gson gson = new Gson();
                                    CityBean cityBean = gson.fromJson(jsonArray.get(j).toString(), CityBean.class);
                                    cityBeans.add(cityBean);
                                }
                                smallCityBean.setCityBeans(cityBeans);
                            }
                            cityData.add(smallCityBean);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    cityDataListener.onSuccess(cityData);
                }
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                cityDataListener.onFail();
            }

            @Override
            protected void onDataFine(String data) {
                super.onDataFine(data);
                if (data == null) {
                    cityDataListener.onFail();
                }
            }

            @Override
            protected void onNetComplete() {
                LoadingDialog.getInstance(context).hideProcessDialog(0);
            }
        }, map);
    }

}
