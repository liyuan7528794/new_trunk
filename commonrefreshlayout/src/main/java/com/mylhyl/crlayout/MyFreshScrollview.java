package com.mylhyl.crlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

/**
 * 自定义滚动刷新加载的ScrollView
 * Created by Administrator on 2017/1/11.
 */
public class MyFreshScrollview extends ScrollView {

	private ScrollViewListener scrollViewListener = null;

	private int downX;
	private int downY;
	private int mTouchSlop;

	public MyFreshScrollview(Context context) {
		super(context);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}

	public MyFreshScrollview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}

	public MyFreshScrollview(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}

	/**
	 * 滚动监听
	 * @param scrollViewListener
     */
	public void setScrollViewListener(ScrollViewListener scrollViewListener) {
		this.scrollViewListener = scrollViewListener;
	}

	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		super.onScrollChanged(x, y, oldx, oldy);
		if (scrollViewListener != null) {
			scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
		}
	}

	interface ScrollViewListener {
		void onScrollChanged(ScrollView scroll, int x, int y, int oldx, int oldy);
	}

	/**
	 * 嵌套RecyclerView时，为防止滑动惯性消失（滑动不流畅）
	 * @param e
	 * @return
     */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {
		int action = e.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				downX = (int) e.getRawX();
				downY = (int) e.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				int moveY = (int) e.getRawY();
				if (Math.abs(moveY - downY) > mTouchSlop) {
					return true;
				}
		}
		return super.onInterceptTouchEvent(e);
	}
}
