package com.travel.shop.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * 实现ScrollView嵌套listView
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/03/07
 */
public class MyListView extends ListView {
	public MyListView(Context context) {
		super(context);
	}

	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}