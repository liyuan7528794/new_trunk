package com.travel.layout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.travel.lib.R;
import com.travel.lib.utils.OSUtil;

/**
 * 播放器的控制视图部分
 * 有播放暂停, 进度条, 快进快退, 全屏与退出全屏组成
 * @author ldkxingzhe
 *
 */
public class PlayerControllerView extends LinearLayout {
	@SuppressWarnings("unused")
	private static final String TAG = "PlayerControllerView";


	public interface PlayerControllerListener{
		/**
		 * 播放停止按钮被点击
		 * @param isPlaying true -- 视频正在播放
		 */
		void onPlayStopClick(boolean isPlaying);
		/**
		 * 全屏被点击
		 */
		void onFullScreenClick();
		/**
		 * 当进度条更改后, 必须是用户手动拖动的结果
		 */
		void onProgressChanged(int progress);
	}
	private PlayerControllerListener mListener;
	/**
	 * 设置监听
	 */
	public void setListener(PlayerControllerListener listener){
		mListener = listener;
	}
	
	private ImageView mPlayStopImage, mFullScreenImage;
	private TextView mCurrentTimeTextView, mTotalTimeTextView;
	private SeekBar mSeekBar;
	private boolean isPlaying = true; // 是否正在播放
	
	public PlayerControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView();
		initListener();
	}

	public void setPlayComplete(){
		mPlayStopImage.setImageResource(R.drawable.vp_play);
	}

	private void initListener() {
		mPlayStopImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPlayStopImage.setImageResource(isPlaying ? R.drawable.vp_play : R.drawable.vp_pause);
				if(mListener != null){
					mListener.onPlayStopClick(isPlaying);
				}
				isPlaying = !isPlaying;
			}
		});
		
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser && mListener != null){
					mListener.onProgressChanged(progress);
				}
			}
		});
		
		mFullScreenImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mListener != null){
					mListener.onFullScreenClick();
				}
			}
		});
	}

	public PlayerControllerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PlayerControllerView(Context context) {
		this(context, null);
	}
	
	private void initView(){
		View rootView = LayoutInflater.from(getContext()).inflate(R.layout.player_controller_view, this, false);
		mPlayStopImage = (ImageView) rootView.findViewById(R.id.iv_stop);
		mCurrentTimeTextView = (TextView) rootView.findViewById(R.id.tv_current_time);
		mTotalTimeTextView = (TextView) rootView.findViewById(R.id.tv_total_time);
		mSeekBar = (SeekBar) rootView.findViewById(R.id.seek_bar);
		mFullScreenImage = (ImageView) rootView.findViewById(R.id.iv_full_screen);
		addView(rootView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		BitmapFactory.Options option = new BitmapFactory.Options();
		option.outWidth = OSUtil.dp2px(getContext(), 16);
		option.outHeight = OSUtil.dp2px(getContext(), 16);
		Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.seek_thumb, option);
		mSeekBar.setThumb(new BitmapDrawable(originalBitmap));
	}

	public void hideFullScreenImage(boolean hide){
		mFullScreenImage.setVisibility(View.GONE);
	}

	/* 显示时间 */
	public void showTime(){
		mCurrentTimeTextView.setVisibility(View.VISIBLE);
		mTotalTimeTextView.setVisibility(View.VISIBLE);
	}

	/* 设置总时间 */
	public void setTotalTime(String totalTime){
		mTotalTimeTextView.setText(totalTime);
	}

	/* 设置当前时间 */
	public void setCurrentTime(String currentTime){
		mCurrentTimeTextView.setText(currentTime);
	}
	
	public void setISFullScreen(boolean isFullScreen){
		int drawableResource;
		if(isFullScreen){
			drawableResource = R.drawable.icon_small_screen;
		}else{
			drawableResource = R.drawable.icon_full_screen;
		}
		mFullScreenImage.setImageResource(drawableResource);
	}
	
	/**
	 * 设置进度条进度
	 * 0 - 100
	 * @param progress
	 */
	public void setProgress(int progress){
		progress = Math.min(100, progress < 0 ? 0 : progress);
		mSeekBar.setProgress(progress);
	}
}
