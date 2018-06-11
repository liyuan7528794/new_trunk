package com.travel.map.factory.location.listener;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;

/**
 * 高德定位模块接口
 * Created by Administrator on 2017/7/24.
 */

public interface IGaodeMapLocation extends IMapLocation, AMapLocationListener {
    @Override
    public void onLocationChanged(AMapLocation aMapLocation);
}
