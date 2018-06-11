package com.travel.video.playback_video;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.tencent.qcloud.suixinbo.views.LVBPlayerControler;
import com.travel.bean.UDPSendInfoBean;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageUtils;
import com.travel.video.layout.SmallVideoWindow;
import com.travel.video.tools.RandomSetColor;

/**
 * Created by Administrator on 2016/11/7.
 */
public class PlaybackVideoPlayerActivity extends TitleBarBaseActivity implements LVBPlayerControler.LVBPlayerListener{
	private PlaybackPlayerHandler playBackHandler;
	private String TAG = "PlaybackVideoPlayerActivity";
	private boolean isStop = false;
	public static boolean isShowShopButton = true;

	private TextView currentTime, totalTime;
	private SeekBar mSeekBar;
	private ImageView startVideo,closeVideo;
	private LVBPlayerControler mLvbPlayerControler;

	private String[] urls = new String[1];
	private int curVideoIndex = 0;
	private VideoInfoBean videoBean;
	private int totalDuration = 0;
	private boolean isClickGoodsVideo = false; //是否是從購物車中的商品點擊進來的
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		videoBean = (VideoInfoBean) bundle.get("video_info");
		if(videoBean.isNullVideoUrl()){
			Toast.makeText(this, "未找到视频源！", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		isShowShopButton = true;
		if(bundle.containsKey("intent_source") && "shop".equals(bundle.getString("intent_source"))){
			isShowShopButton = false;
			LVBPlayerControler.isClickGoodsVideo = true;
			isClickGoodsVideo = true;
		}

		// ==================设置全屏=========================
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if(videoBean.getVideoType() == 4){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}else{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// ===================设置亮度========================
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = 0.9F;

		setContentView(R.layout.activity_playback_video_player);
		hideOriginTitleLayout();
		findViews();
		if(videoBean.getUrl().contains(",")){
			urls = videoBean.getUrl().split(",");
		}else{
			urls[0] = videoBean.getUrl();
		}

		//初始化弹幕颜色
		RandomSetColor.randomColorAdapter();

		playBackHandler = new PlaybackPlayerHandler(this,videoBean);
		newPlayer(0);
	}

	private void newPlayer(int index) {
		curVideoIndex = index;
		mLvbPlayerControler.setPlayUrl(urls[index]);
		mLvbPlayerControler.startVideo();
		playBackHandler.updataBarrage(0, 1000, true);
		isStop = false;
	}

	private void reOpenPlayer(int index) {
//        mLvbPlayerControler.stop();
		try {
			Thread.currentThread().sleep(1000);
		} catch (Exception e){
		}
		newPlayer(index);
	}

	private void findViews() {
		mSeekBar = (SeekBar) findViewById(R.id.videoProgressbar);
		Bitmap bitmap = ImageUtils.setResourcesToBitmapImageSize(this,R.drawable.seek_thumb,16,16);
		mSeekBar.setThumb(new BitmapDrawable(bitmap));

		currentTime = (TextView) findViewById(R.id.updateTime);
		totalTime = (TextView) findViewById(R.id.totalTime);
		startVideo = (ImageView) findViewById(R.id.videoStartImage);
		closeVideo = (ImageView) findViewById(R.id.closeVideo);

		mLvbPlayerControler = (LVBPlayerControler) findViewById(R.id.lvb_player_controler);
		mLvbPlayerControler.hideControlerView(true);
		mLvbPlayerControler.setListener(this);

		mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		startVideo.setOnClickListener(StartPlayListener);
		closeVideo.setOnClickListener(closeVideoListener);
	}


	private View.OnClickListener closeVideoListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};

	private View.OnClickListener StartPlayListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(isStop){
				reOpenPlayer(curVideoIndex);
				startVideo.setImageResource(R.drawable.vp_pause);
			}else if(mLvbPlayerControler.isPause()){
				mLvbPlayerControler.resume();
				startVideo.setImageResource(R.drawable.vp_pause);
			}else if(mLvbPlayerControler.isPlaying()){
				mLvbPlayerControler.pause();
				startVideo.setImageResource(R.drawable.vp_play);
			}
		}
	};

	private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			int progress = seekBar.getProgress();
			mLvbPlayerControler.setSeek(progress);
			playBackHandler.updataBarrage(progress * 1000, 1000, true);
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// 检测屏幕的方向：纵向或横向
		if (this.getResources().getConfiguration().orientation
				== Configuration.ORIENTATION_LANDSCAPE) {
			//当前为横屏， 在此处添加额外的处理代码
		}
		else if (this.getResources().getConfiguration().orientation
				== Configuration.ORIENTATION_PORTRAIT) {
			//当前为竖屏， 在此处添加额外的处理代码
		}
		//检测实体键盘的状态：推出或者合上
		if (newConfig.hardKeyboardHidden
				== Configuration.HARDKEYBOARDHIDDEN_NO){
			//实体键盘处于推出状态，在此处添加额外的处理代码
		}
		else if (newConfig.hardKeyboardHidden
				== Configuration.HARDKEYBOARDHIDDEN_YES){
			//实体键盘处于合上状态，在此处添加额外的处理代码
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(PlaybackPlayerHandler.isPause){
			mLvbPlayerControler.pause();
			startVideo.setImageResource(R.drawable.vp_play);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SmallVideoWindow.getInstance().hidePopupWindow();

		if(LVBPlayerControler.isClickGoodsVideo && !isClickGoodsVideo){
			reOpenPlayer(curVideoIndex);
			LVBPlayerControler.isClickGoodsVideo = false;
		}

		if(PlaybackPlayerHandler.isPause){
			if(!isStop) {
				startVideo.setImageResource(R.drawable.vp_pause);
				mLvbPlayerControler.resume();
			}
		}
		PlaybackPlayerHandler.isPause = true;
	}

	@Override
	protected void onDestroy() {
		SmallVideoWindow.getInstance().hidePopupWindow();
		if(playBackHandler!=null)
			playBackHandler.sendEmptyMessage(playBackHandler.SUBMIT_PRAISE_DATA);
		if(mLvbPlayerControler!=null)
			mLvbPlayerControler.destroy();
		isShowShopButton = true;
		super.onDestroy();
	}

	@Override
	public void updateProgress(int progress, int duration) {
		if(duration != -1 && progress != -1){
			Log.e(TAG,"progress= " +progress +" , duration= " +duration);
			totalDuration = duration;
			totalTime.setText(" / "+ DateFormatUtil.longToStringByhhmmss(totalDuration));
			mSeekBar.setMax(duration);
			currentTime.setText(DateFormatUtil.longToStringByhhmmss(progress));
			mSeekBar.setProgress(progress);
			playBackHandler.updataBarrage(progress*1000, 1000, false);
		}
	}

	@Override
	public void endPlay() {
		startVideo.setImageResource(R.drawable.vp_play);
		isStop = true;
		currentTime.setText("00:00");
		if(curVideoIndex < urls.length-1){
			reOpenPlayer(curVideoIndex+1);
		}
	}

	String beginTime = "";
	@Override
	protected void onStart() {
		super.onStart();
		beginTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
	}

	String endTime = "";
	@Override
	protected void onStop() {
		super.onStop();
		endTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
		UDPSendInfoBean bean = new UDPSendInfoBean();
		bean.getData("008_" + videoBean.getVideoId(), videoBean.getVideoTitle(),
				videoBean.getUrl(), beginTime, endTime);
		sendData(bean);
	}

}