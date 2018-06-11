package com.travel.communication.view.sortlistview;

/**
 * sortlistview允许的实体结构
 * 允许继承
 */
public class SortModel<T> {
    private T mObject;
    private String mCapitalLetters; // 数据所属的大写字母
    /**
     * 构造函数，
     * @param object 真是的object
     */
    public SortModel(T object){
        mObject = object;
    }

    /**
     * 或许首个大写字母
     * @return
     */
    public String getCapitalLetters() {
        return mCapitalLetters;
    }

    public void setCapitalLetters(String capitalLetters) {
        this.mCapitalLetters = capitalLetters;
    }

    public String getName(){
        return mObject.toString();
    }
    
    public T getRealObject(){
    	return mObject;
    }

    @Override
    public String toString() {
        return getName();
    }
}
