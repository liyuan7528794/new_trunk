package com.travel.shop.http;

import android.content.Context;

import com.google.gson.Gson;
import com.travel.ShopConstant;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.shop.bean.CityBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/17.
 */

public class OutCityHttpHelper {
    private Context context;
    private CityNetListener cityNetListener;

    public OutCityHttpHelper(Context context, CityNetListener cityNetListener) {
        this.context = context;
        this.cityNetListener = cityNetListener;
    }

    public interface CityNetListener {
        public void getCitys(List<CityBean> bigCities);
    }

    public void getNetCities() {
        String url = ShopConstant.OUT_CITY_LIST;
        Map<String, Object> map = new HashMap<>();
        NetWorkUtil.postForm(context, url, new MResponseListener(context) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                List<CityBean> bigList = new ArrayList<CityBean>();
                try {
                    if (data != null && data.length() > 0) {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject dataObject = data.getJSONObject(i);
                            // id为1取得是大城，2为小城；
                            if (dataObject.has("id") && dataObject.getInt("id") == 2 && dataObject.getJSONArray("list").length() > 0) {
                                JSONArray jsonArray = dataObject.getJSONArray("list");
                                for (int j = 0; j < jsonArray.length(); j++) {
                                    Gson gson = new Gson();
                                    CityBean bigBean = gson.fromJson(jsonArray.get(j).toString(), CityBean.class);
                                    bigList.add(bigBean);
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    cityNetListener.getCitys(bigList);
                }
            }

            @Override
            protected void onNetComplete() {
                cityNetListener.getCitys(null);
            }
        }, map);
    }

}
