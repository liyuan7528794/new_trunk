package com.travel.widget;

import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.video.tools.SmallWindowMoveTouchListener;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.media.IjkVideoView;

/**
 * 里面封装了VideoView的拖拽
 *
 */
public class ViewWithDrag extends RelativeLayout {
	private static final String TAG = "VideoViewWithDrag";
	private FrameLayout mPlayerContainer;
	
	private View mDragView;
	private View mDecorationView;
	
	private GestureDetector mGestureDetector;
	private OnClickListener mListener;

	public ViewWithDrag(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ViewWithDrag(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ViewWithDrag(Context context) {
		this(context, null);
	}
	
	public void setMyClickListener(OnClickListener listener){
		mListener = listener;
	}

	@SuppressWarnings("deprecation")
	private void init() {
		mPlayerContainer = new FrameLayout(getContext());
		int padding = OSUtil.dp2px(getContext(), 2);
		mPlayerContainer.setPadding(padding, padding, padding, padding);
		mPlayerContainer.setBackgroundColor(Color.WHITE);
		int height = OSUtil.getScreenHeight() * 2 / 5;
		int width = height * 3 / 2;
		MLog.v(TAG, "width is " + width + ", and height is " + height);
		addView(mPlayerContainer, new LayoutParams(width, height));
		mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener(){
			@Override
			public boolean onDown(MotionEvent e) {
				return false;
			}
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if(mListener != null){
					mListener.onClick(mPlayerContainer);
				}
				return true;
			}
		});
		initVideoViewListener();
	}
	/**
	 * 设置播放器容器的位置
	 */
	public void setPlayerContainerPosition(){
		getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				MLog.v(TAG, "onGlobalLayout");
				int width = getMeasuredWidth();
				int height = getMeasuredHeight();
				int playerContainerWidth = mPlayerContainer.getMeasuredWidth();
				int playerContainerHeight = mPlayerContainer.getMeasuredHeight();
				LayoutParams params = (LayoutParams) mPlayerContainer.getLayoutParams();
				params.leftMargin = width - playerContainerWidth - 40;
				params.topMargin = (height - playerContainerHeight)/2;
				mPlayerContainer.setLayoutParams(params);
				getViewTreeObserver().removeGlobalOnLayoutListener(this);
				invalidate();
			}
		});
	}
/*	*//**
	 * 设置窗口方向 16 : 9, 默认为横屏模式
	 * @param isPortail true -- 是竖屏形式
	 *//*
	public void forcePortail(boolean isPortail){
		LayoutParams params = (LayoutParams) mPlayerContainer.getLayoutParams();
		int height = OSUtil.getScreenHeight() * 2 / 5;
		int width = height * 16 / 9;
		if(isPortail){
			params.height = width;
			params.width = height;
		}else{
			params.height = height;
			params.width = width;
		}
		mPlayerContainer.setLayoutParams(params);
	}*/
	/**
	 * 设置窗口的宽高比
	 * @param aspectX 宽
	 * @param aspectY 高
	 */
	public void setWidthHeightRadio(int aspectX, int aspectY){
		LayoutParams params = (LayoutParams) mPlayerContainer.getLayoutParams();
		params.height = params.width * aspectY / aspectX;
		mPlayerContainer.setLayoutParams(params);
	}
	
	
	/**
	 * 设置拖拉的视图
	 * @param dragView
	 */
	public void setDragView(View dragView) {
		ViewGroup viewGroup = (ViewGroup) dragView.getParent();
		if(viewGroup != null){
			viewGroup.removeView(dragView);
		}
		mDragView = dragView;
		mPlayerContainer.addView(dragView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	
	public void removeAllViewsFromPlayerContainer() {
		mPlayerContainer.removeAllViews();
	}
	
	/**
	 * 设置装饰遮罩层
	 * @param view
	 */
	public void setDecorationView(View view){
		if(mDecorationView != null){
			removeView(mDecorationView);
		}
		if(view == null) return;
		mDecorationView = view;
		mPlayerContainer.addView(mDecorationView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	
	private SmallWindowMoveTouchListener mSmallWindowMoveTouchListener;
	private void initVideoViewListener() {
		mPlayerContainer.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(mSmallWindowMoveTouchListener == null){
					mSmallWindowMoveTouchListener = new SmallWindowMoveTouchListener(mPlayerContainer);
				}
				MLog.v(TAG, "mPlayerContainer onTouch");
				mSmallWindowMoveTouchListener.onTouch(v, event);
				return mGestureDetector.onTouchEvent(event);
			}
		});
	}
	
	/**
	 * 获取播放器的容器
	 * @return
	 */
	public FrameLayout getPlayerContainer(){
		return mPlayerContainer;
	}

	/**
	 * 获取脸面VideoView的引用
	 * @return
	 */
	public View getDragView(){
		return mDragView;
	}
}
