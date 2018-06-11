package com.travel.map.utils;

import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.travel.app.TravelApp;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.video.bean.XpaiCofig;

/**
 * Created by Administrator on 2017/2/15.
 */

public class LocationTools {
    private static LocationTools _instans = null;
    private Context context;
    //声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    private AMapLocationClientOption mLocationOption = null;

    private LocationTools(){

    }

    public static LocationTools getInstans(){
        if(_instans == null){
            _instans = new LocationTools();
        }
        return _instans;
    }

    public void init(){
        //初始化定位
        mLocationClient = new AMapLocationClient(TravelApp.appContext);
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //高精度模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(true);

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true);

        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        mLocationOption.setInterval(600000);
        mLocationOption.setNeedAddress(true);
    }

    public void startLocation(){
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    public void destroyLocation(){
        if(mLocationClient.isStarted()){
            mLocationClient.onDestroy();
        }
    }

    //声明定位回调监听器
    private AMapLocationListener mLocationListener = new AMapLocationListener(){

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    UserSharedPreference.saveAddress(aMapLocation.getAddress());
                    XpaiCofig.latitude = aMapLocation.getLatitude();
                    XpaiCofig.longitude = aMapLocation.getLongitude();
                }else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError","location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }

                if(listener != null){
                    listener.onLocation(XpaiCofig.latitude, XpaiCofig.longitude);
                }
            }
        }
    };

    private OnLocationListener listener;
    public void setListener(OnLocationListener listener){
        this.listener = listener;
    }

    public interface OnLocationListener{
        void onLocation(double latitude, double longitude);
    }
}
