package com.travel.imserver;

/**
 * 回调接口
 * Created by ldkxingzhe on 2016/12/8.
 */
public interface ResultCallback<T> {
    void onResult(T obj);
}
