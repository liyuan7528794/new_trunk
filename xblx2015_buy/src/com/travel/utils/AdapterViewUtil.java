package com.travel.utils;

import java.lang.reflect.Field;

import com.travel.lib.utils.MLog;

import android.widget.AdapterView;

/**
 * AdapterView的一些工具类
 * @author ldkxingzhe
 *
 */
public class AdapterViewUtil {
	
	public static void addItemsToTop(AdapterView adapterView, int itemsNum){
		MLog.v("AdapterViewUtil", "itemsNum is %d.", itemsNum);
		try {
			Field firstPositionField = AdapterView.class.getDeclaredField("mFirstPosition");
			firstPositionField.setAccessible(true);
			int currentFirstPosition = firstPositionField.getInt(adapterView);
			firstPositionField.set(adapterView, currentFirstPosition + itemsNum);
			MLog.v("AddapterViewUtil", "currentFirstPosition is %d", currentFirstPosition);
		} catch (Exception e) {
			MLog.e("AdapterViewUtil",  e.getMessage(), e);
		}
	}
}
