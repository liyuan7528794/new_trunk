package com.travel.imserver;

/**
 * 判断是否接受处理该内容
 * Created by ldkxingzhe on 2016/12/7.
 */
public interface Receiver<T> {
    /*
     * 判断是否处理内容
     * return true -- 处理该内容
   * */
    boolean isDealWith(T obj);

    /**
     * 处理该内容
     */
    void dealWith(T obj);
}
