package com.travel.video.widget;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

/**
 * 视频直播的开始的自定义view
 */
public class VideoDecorationStartAnimationView extends RelativeLayout {
	private static final String TAG = "VideoDecorationStartAnimationView";
	
	private ImageView mBlurImage, mBlackImage, mBlurHeadImage;
	private TextView mInterCutName, mStartText, mBlurNotify;
	
	/**
	 * 设置头像
	 * @param headImgUrl
	 */
	public void setHeaderImg(String headImgUrl){
		ImageDisplayTools.displayHeadImage(headImgUrl, mBlurHeadImage);
	}
	
	public void setHeaderImgVisibity(int visible){
		mBlurHeadImage.setVisibility(visible);
	}
	/**
	 * 设置模糊图
	 * @param backgroundImg
	 */
	public void setBlurBackgroudImg(String backgroundImg){
		ImageDisplayTools.displayImage(backgroundImg, mBlurImage);
	}
	/**
	 * 设置开始 3, 2, 1
	 * @param countDownTime 倒计时时长, 单位秒
	 */
	public void setStartText(int countDownTime){
		new CountDownTimer(countDownTime * 1000, 1000) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				int leftTime = (int) (millisUntilFinished / 1000);
				mStartText.setText(String.valueOf(leftTime));
			}
			
			@Override
			public void onFinish() {
				setVisibility(View.INVISIBLE);
			}
		}.start();
	}
	/**
	 * 设置nickName
	 * @param nickName
	 */
	public void setNickName(String nickName){
		mInterCutName.setText(nickName);
	}
	
	public void setBlurNotifyText(String notifyText){
		mBlurNotify.setText(notifyText);
	}

	public VideoDecorationStartAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public VideoDecorationStartAnimationView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VideoDecorationStartAnimationView(Context context) {
		this(context, null);
	}
	
	private void init() {
		initBlurImage();
		initBlackImage();
		initBlurHeadImage();
		initInterCutName();
		initStartText();
		initBlurNotify();
	}

	private void initBlurNotify() {
		mBlurNotify = new TextView(getContext());
		mBlurNotify.setTextSize(13);
		mBlurNotify.setTextColor(Color.WHITE);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.bottomMargin = OSUtil.dp2px(getContext(), 25);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		addView(mBlurNotify, params);
	}

	private void initStartText() {
		mStartText = new TextView(getContext());
		mStartText.setTextColor(Color.WHITE);
		mStartText.setTextSize(13);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.topMargin = OSUtil.dp2px(getContext(), 20);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.addRule(RelativeLayout.BELOW, android.R.string.copyUrl);
		addView(mStartText, params);
	}

	private void initInterCutName() {
		mInterCutName = new TextView(getContext());
		mInterCutName.setTextSize(13);
		mInterCutName.setTextColor(Color.WHITE);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.topMargin = OSUtil.dp2px(getContext(), 13);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.addRule(RelativeLayout.BELOW, android.R.string.copy);
		addView(mInterCutName, params);
		mInterCutName.setId(R.id.id_intercut_name);
	}

	private void initBlurHeadImage() {
		mBlurHeadImage = new ImageView(getContext());
		mBlurHeadImage.setScaleType(ScaleType.FIT_XY);
		LayoutParams params = new LayoutParams(OSUtil.dp2px(getContext(), 50), OSUtil.dp2px(getContext(), 50));
		params.topMargin = OSUtil.dp2px(getContext(), 30);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		addView(mBlurHeadImage, params);
		mBlurHeadImage.setId(R.id.id_blur_head_image);
	}

	private void initBlackImage() {
		mBlackImage = new ImageView(getContext());
		mBlackImage.setBackgroundColor(Color.BLACK);
		mBlackImage.setAlpha(0.5f);
		addView(mBlackImage, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	private void initBlurImage() {
		mBlurImage = new ImageView(getContext());
		mBlurImage.setScaleType(ScaleType.FIT_XY);
		addView(mBlurImage, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
}
