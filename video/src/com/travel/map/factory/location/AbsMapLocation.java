package com.travel.map.factory.location;

import android.content.Context;

import com.travel.map.factory.location.listener.IMapLocation;

/**
 * 定位功能模块抽象类
 * Created by Administrator on 2017/7/24.
 */

public abstract class AbsMapLocation {
    public abstract void init(Context context, IMapLocation listener);

    public abstract void startLocation();

    public abstract void stopLocation();

    public abstract void onDestroy();
}
