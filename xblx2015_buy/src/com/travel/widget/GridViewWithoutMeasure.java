package com.travel.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class GridViewWithoutMeasure extends GridView {

	public GridViewWithoutMeasure(Context context) {
		super(context);
	}

	public GridViewWithoutMeasure(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public GridViewWithoutMeasure(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int tmpHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, tmpHeightMeasureSpec);
	}

}
