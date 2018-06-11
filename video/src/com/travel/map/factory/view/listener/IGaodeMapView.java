package com.travel.map.factory.view.listener;

import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.poisearch.PoiSearch;

/**
 * 继承高德地图模块的接口
 * Created by Administrator on 2017/7/24.
 */

public interface IGaodeMapView extends IMapView, LocationSource, AMap.OnMarkerClickListener,
        AMap.OnInfoWindowClickListener, AMap.OnMapLoadedListener, AMap.InfoWindowAdapter, PoiSearch.OnPoiSearchListener {

    @Override
    public View getInfoWindow(Marker marker);

    @Override
    public View getInfoContents(Marker marker);

    @Override
    public void onInfoWindowClick(Marker marker);

    @Override
    public void onMapLoaded();

    @Override
    public boolean onMarkerClick(Marker marker);

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener);

    @Override
    public void deactivate();
}
