package com.travel.map.factory.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.travel.map.factory.view.listener.IMapView;

/**
 * 地图 API抽象类
 *  （比如百度和高德地图的一些相似的功能方法）
 * Created by Administrator on 2017/7/24.
 */

public abstract class AbsMapView {

    public abstract <T extends View> void onCreate(Context context, T view, Bundle savedInstanceState);

    /**
     * 设置回调接口
     * @param iMapView
     */
    public abstract void setListener(IMapView iMapView);

    public abstract void onResume();

    public abstract void onPause();

    public abstract void onDestroy();

    public abstract void onSaveInstanceState(Bundle bundle);

}
