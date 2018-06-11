package com.travel.widget;

import com.travel.layout.HorizontalListView;
import com.travel.lib.utils.MLog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * 水平的listView, 只是measure出自身高度
 * @author ldkxingzhe
 *
 */
public class HorizonListViewWithoutMeasure extends HorizontalListView {
	private static final String TAG = "HorizonListViewWithoutMeasure";
	
	private int mDividerWidth = 0;

	public HorizonListViewWithoutMeasure(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void setDividerWidth(int width) {
		super.setDividerWidth(width);
		mDividerWidth = width;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    int widthSize =  0;
	    int itemCount = mAdapter == null ? 0 : mAdapter.getCount();
	    int tmp = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE, MeasureSpec.UNSPECIFIED);
	    super.onMeasure(tmp, heightMeasureSpec);
	    if(itemCount > 0){
	    	for(int i = 0; i < itemCount; i++){
	    		View view = mAdapter.getView(i, null, this);
	    		view.measure(tmp, heightMeasureSpec);
	    		widthSize += view.getMeasuredWidth() + mDividerWidth;
	    	}
	    	widthSize -= mDividerWidth;
	    }
	    MLog.v(TAG, "widthSize is " + widthSize);
	    int tmpWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
	    super.onMeasure(tmpWidthMeasureSpec, heightMeasureSpec);
	}
}
