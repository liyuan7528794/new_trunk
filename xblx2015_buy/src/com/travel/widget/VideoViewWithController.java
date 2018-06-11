package com.travel.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.travel.layout.PlayerControllerView;

import tv.danmaku.ijk.media.player.media.IjkVideoView;

/**
 * 带控制器的播放器,
 * 暂时不想写了, 在这里标出, 如果再次需要播放器, 把这里的补全. OKK
 * @author ldkxingzhe
 */
public class VideoViewWithController extends RelativeLayout {
	private static final String TAG = "VideoViewWithController";
	
	private IjkVideoView mVideoView;
	private PlayerControllerView mPlayerControllerView;

	public VideoViewWithController(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	public void setVideoView(IjkVideoView ijkVideoView){
		mVideoView = ijkVideoView;
		addView(mVideoView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	public VideoViewWithController(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VideoViewWithController(Context context) {
		this(context, null);
	}
	
	
}
