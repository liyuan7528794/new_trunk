package com.travel.map.factory;

import com.travel.map.factory.location.AbsMapLocation;
import com.travel.map.factory.view.AbsMapView;

/**
 * 地图的抽象工厂类
 * Created by Administrator on 2017/7/24.
 */
public abstract class AbsMapFactory {

    // 获取MapView模块
    public abstract AbsMapView createMapView();

    // 获取MapView模块
    public abstract AbsMapLocation createLocation();
}
