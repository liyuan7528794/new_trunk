package com.travel.shop.bean;

import java.util.ArrayList;

/**
 * 发现页的小城的实体类
 * Created by wyp on 2018/5/15.
 */

public class SmallCityBean {

    private String id;// 分类 1:小城故事 2:推荐城市 3:大城小事
    private ArrayList<CityBean> cityBeans;

    public SmallCityBean() {
        cityBeans = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<CityBean> getCityBeans() {
        return cityBeans;
    }

    public void setCityBeans(ArrayList<CityBean> cityBeans) {
        this.cityBeans = cityBeans;
    }
}
