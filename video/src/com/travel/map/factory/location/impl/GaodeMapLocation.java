package com.travel.map.factory.location.impl;

import android.content.Context;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.travel.map.factory.location.AbsMapLocation;
import com.travel.map.factory.location.listener.IGaodeMapLocation;
import com.travel.map.factory.location.listener.IMapLocation;

/**
 * 高德定位功能实现类
 * Created by Administrator on 2017/7/24.
 */

public class GaodeMapLocation extends AbsMapLocation{
    private Context context;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private IGaodeMapLocation listener;

    private boolean isOnlyOnce = false;
    private int intervalTimes = 5000;
    @Override
    public void init(Context context,IMapLocation listener) {
        this.context = context;
        this.listener = (IGaodeMapLocation) listener;
    }

    private void setOption(){
        mlocationClient = new AMapLocationClient(context);
        mLocationOption = new AMapLocationClientOption();
//        //设置定位监听
        mlocationClient.setLocationListener(this.listener);
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(isOnlyOnce);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);

        //			//设置定位间隔,单位毫秒,默认为2000ms
        			mLocationOption.setInterval(intervalTimes);
        //			//设置是否允许模拟位置,默认为false，不允许模拟位置
        //			mLocationOption.setMockEnable(false);
        //设置定位参数

        mlocationClient.setLocationOption(mLocationOption);
    }

    @Override
    public void startLocation() {
        if (mlocationClient == null) {
            setOption();
        }

        mlocationClient.startLocation();
    }

    @Override
    public void stopLocation() {
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
        }
    }

    @Override
    public void onDestroy() {
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    public boolean isOnlyOnce() {
        return isOnlyOnce;
    }

    public void setOnlyOnce(boolean onlyOnce) {
        isOnlyOnce = onlyOnce;
    }

    public int getIntervalTimes() {
        return intervalTimes;
    }

    public void setIntervalTimes(int intervalTimes) {
        this.intervalTimes = intervalTimes;
    }
}
