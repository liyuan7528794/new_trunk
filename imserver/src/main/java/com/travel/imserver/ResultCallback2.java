package com.travel.imserver;

public interface ResultCallback2<T> extends ResultCallback<T>{
    void onError(int errorCode, String errorReason);
}
