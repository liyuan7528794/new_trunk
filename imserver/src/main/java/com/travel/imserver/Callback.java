package com.travel.imserver;

/**
 * 回调的接口
 * Created by ldkxingzhe on 2016/12/7.
 */
public interface Callback {
    void onSuccess();
    void onError(int errorCode, String errorMsg);
}
