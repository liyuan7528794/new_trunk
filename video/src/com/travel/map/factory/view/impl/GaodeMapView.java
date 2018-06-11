package com.travel.map.factory.view.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.poisearch.PoiSearch;
import com.travel.map.factory.view.AbsMapView;
import com.travel.map.factory.view.listener.IGaodeMapView;
import com.travel.map.factory.view.listener.IMapView;

/**
 * 高德地图功能实现类
 * Created by Administrator on 2017/7/24.
 */
public class GaodeMapView extends AbsMapView{
    private Context context;
    private MapView mapView;
    private AMap aMap;
    private IGaodeMapView IMapView;

    @Override
    public <T extends View> void onCreate(Context context, T view, Bundle savedInstanceState) {
        this.context = context;
        if(view!=null)
            mapView = (MapView) view;
        else
            mapView = new MapView(context);

        mapView.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void setListener(IMapView iMapView) {
        this.IMapView = (IGaodeMapView) iMapView;
    }


    @Override
    public void onResume() {
        mapView.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        aMap = null;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        mapView.onSaveInstanceState(bundle);
    }

    /**
     * 初始化
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.moveCamera(CameraUpdateFactory.zoomTo(3));
        aMap.setLocationSource(IMapView);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location arg0) {
                Log.e("myLocation哈哈:", arg0.getLatitude() + "," + arg0.getLongitude());
            }
        });

        aMap.setOnMapLoadedListener(IMapView);// 设置amap加载成功事件监听器
        aMap.setOnMarkerClickListener(IMapView);// 设置点击marker事件监听器
        aMap.setOnInfoWindowClickListener(IMapView);// 设置点击infoWindow事件监听器
        aMap.setInfoWindowAdapter(IMapView);// 设置自定义InfoWindow样式

    }
    private PoiSearch.Query query ;
    private PoiSearch poiSearch;
    public void nearByLatLog(){
//        150000 交通
// 190000 地址信息
        query = new PoiSearch.Query("", "", "");
        //keyWord表示搜索字符串，
        //第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
        //cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(1);//设置查询页码

        poiSearch = new PoiSearch(context, query);
        poiSearch.setOnPoiSearchListener(IMapView);


        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                LatLng latLng = cameraPosition.target;
                poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(latLng.latitude,
                        latLng.longitude), 10000));
                poiSearch.searchPOIAsyn();

            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {

            }
        });
    }

    /**
     * 自定义我的当前位置的图标
     * @param id
     */
    public void setMyLocationStyle(int id){

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
                fromResource(id));
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 圆圈范围
        // 自定义精度范围的圆形边框颜色
        //				 myLocationStyle.strokeColor(Color.BLACK);
        //自定义精度范围的圆形边框宽度
        //				myLocationStyle.strokeWidth(2);
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.setMyLocationStyle(myLocationStyle);
    }

    public void setMyLocationStyle(Bitmap bitmap){

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        aMap.setMyLocationStyle(myLocationStyle);
    }


    public AMap getaMap() {
        return aMap;
    }
}
