package com.travel.map.factory.impl;

import com.travel.map.factory.AbsMapFactory;
import com.travel.map.factory.location.AbsMapLocation;
import com.travel.map.factory.location.impl.GaodeMapLocation;
import com.travel.map.factory.view.AbsMapView;
import com.travel.map.factory.view.impl.GaodeMapView;

/**
 * 高德各功能模块的工厂类
 * Created by Administrator on 2017/7/24.
 */

public class GaodeMapFactory extends AbsMapFactory{
    @Override
    public AbsMapView createMapView() {
        return new GaodeMapView();
    }

    @Override
    public AbsMapLocation createLocation() {
        return new GaodeMapLocation();
    }
}
