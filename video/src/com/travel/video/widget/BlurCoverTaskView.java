package com.travel.video.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;

import java.lang.ref.WeakReference;

/**
 * 模糊背景任务布局
 * @author Administrator
 */
public class BlurCoverTaskView extends RelativeLayout{

	public final static String NOTIFY_END_BLUR_COVER = "notify_end_blur_cover";
	
	private WeakReference<Activity>  context;
	private View rootView;
	private ImageView mBlurBackgroundImg;
	private BlurCoverListener mBlurCoverListener;

	private boolean mUserTmpLeave = false;
	
	public interface BlurCoverListener{
		public void blurNotify(String notifyType);
	}
	
	public void blurNotify(String notifyType){
		Toast.makeText(getContext(), "类型："+notifyType, Toast.LENGTH_SHORT).show();
	}
	
	public BlurCoverTaskView(Context context, AttributeSet attrs, int defStyleAttr,BlurCoverListener mBlurCoverListener) {
		super(context, attrs, defStyleAttr);
		this.context = new WeakReference<Activity>((Activity) context);
		this.mBlurCoverListener = mBlurCoverListener;
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				init();
			}
		},500);

	}

	public BlurCoverTaskView(Context context, AttributeSet attrs,BlurCoverListener mBlurCoverListener) {
		this(context, attrs, 0,mBlurCoverListener);
	}
	
	public BlurCoverTaskView(Context context,BlurCoverListener mBlurCoverListener) {
		this(context, null,mBlurCoverListener);
	}
	
	@SuppressLint("InflateParams")
	private void init(){
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){ // 竖屏
			// doSomrthing
			rootView = context.get().getLayoutInflater().inflate(R.layout.blur_cover_view, null);
		} else {
			// 横屏时dosomething
			rootView = context.get().getLayoutInflater().inflate(R.layout.blur_cover_view_land, null);
		}
		addView(rootView, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		mBlurBackgroundImg = (ImageView) rootView.findViewById(R.id.iv_blur_background);
		initView();
	}
	
	private ImageView inserCutClose;
	private RelativeLayout closeInterCutNotifyLayout;
	private TextView interCutNickname;
	private Button closeButton,calseButton;
	private void initView() {
		inserCutClose = (ImageView) rootView.findViewById(R.id.inserCut_close);
		interCutNickname = (TextView) rootView.findViewById(R.id.interCutNickname);
		closeInterCutNotifyLayout = (RelativeLayout) rootView.findViewById(R.id.closeInterCutNotifyLayout);
		calseButton = (Button) rootView.findViewById(R.id.calseButton);
		closeButton = (Button) rootView.findViewById(R.id.closeButton);
		
		closeButton.setOnClickListener(new CloseListener());
		setCloseButtonClickable(true);
		inserCutClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeInterCutNotifyLayout.setVisibility(View.VISIBLE);
				mBlurBackgroundImg.setVisibility(View.VISIBLE);
				mBlurBackgroundImg.setImageDrawable(null);
				setCloseVisibility(true);
			}
		});
		calseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeInterCutNotifyLayout.setVisibility(View.GONE);
				if(mUserTmpLeave){
					mBlurBackgroundImg.setImageResource(R.drawable.icon_liver_tmp_leave);
				}else{
					mBlurBackgroundImg.setVisibility(View.GONE);
				}
				setCloseVisibility(false);
			}
		});
		
	}
	
	public void setViewData(String nickName,String ImageUrl){
		interCutNickname.setText(nickName);
	}
	
	public void setInterCutName(String nickName){
		interCutNickname.setText(nickName);
	}

	public void onUserTmpLeave(){
		mUserTmpLeave = true;
		if(closeInterCutNotifyLayout.getVisibility() != View.VISIBLE){
			mBlurBackgroundImg.setImageResource(R.drawable.icon_liver_tmp_leave);
			mBlurBackgroundImg.setVisibility(View.VISIBLE);
		}
	}

	public void onUserTmpLeaveBack(){
		mUserTmpLeave = false;
		if(closeInterCutNotifyLayout.getVisibility() != View.VISIBLE){
			mBlurBackgroundImg.setVisibility(View.GONE);
		}
	}
	
	private class CloseListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			closeBlurCover(NOTIFY_END_BLUR_COVER);
		}
	}
	
	/**
	 * 插播结束时的模糊背景倒计时任务
	 * @param type
	 */
	public void closeBlurCover(String type){
		closeInterCutNotifyLayout.setVisibility(View.GONE);
		mBlurCoverListener.blurNotify(type);
	}
	
	/** 关闭按钮是否给予点击事件 */
	public void setCloseButtonClickable(boolean clickable){
		inserCutClose.setClickable(clickable);
	}
	
	public void hideBlurCover() {
		closeInterCutNotifyLayout.setVisibility(View.GONE);
	}
	
	/** 隐藏关闭按钮 */
	public void setCloseVisibility(boolean isGone){
		inserCutClose.setVisibility(isGone ? View.GONE : View.VISIBLE);
	}
}
