package com.tencent.qcloud.suixinbo.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.qcloud.suixinbo.R;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.travel.lib.ui.LoadingDialog;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.FormatUtils;
import com.travel.lib.utils.OSUtil;

/**
 * Created by Administrator on 2016/11/1.
 */
public class LVBPlayerControler extends RelativeLayout implements ITXLivePlayListener {
    private String Tag = "LVBPlayerControler";
    public static boolean isClickGoodsVideo = false; //是否是從購物車中的商品點擊進來的
    private Context context;
    private View rootView;
    private LinearLayout progressLayout;
    private ImageView startBtn;
    private TextView updateTime,totalTime;
    private SeekBar seekBar;

    private TXCloudVideoView videoView;
    private TXLivePlayConfig playConfig = null;
    private TXLivePlayer livePlayer = null;
    private int mPlayType = -1;
    private String playUrl = "-1";
    private boolean isPause = false;
    private long mTrackingTouchTS = 0;
    private LVBPlayerListener mLvbPlayerListener;

    public LVBPlayerControler(Context context) {
        this(context, null);
    }
    public LVBPlayerControler(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public LVBPlayerControler(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initClickListener();
        initPlayerData();
    }

    public interface LVBPlayerListener{
        public void updateProgress(int progress,int duration);
        public void endPlay();
    }

    private void initPlayerData() {
        livePlayer = new TXLivePlayer(context);
        playConfig = new TXLivePlayConfig();
        livePlayer.setPlayerView(videoView);
        livePlayer.setPlayListener(this);
        livePlayer.setConfig(playConfig);
        livePlayer.setRenderRotation(0);
        livePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
//        livePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
        livePlayer.enableHardwareDecode(true);
    }

    private void initView() {
        rootView = LayoutInflater.from(context).inflate(R.layout.lvb_player_layout,null);
        videoView = (TXCloudVideoView) rootView.findViewById(R.id.video_view);
        progressLayout = (LinearLayout) rootView.findViewById(R.id.progressLLayout);
        startBtn = (ImageView) rootView.findViewById(R.id.start_button);
        updateTime = (TextView) rootView.findViewById(R.id.update_time);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);
        seekBar = (SeekBar) rootView.findViewById(R.id.seek_bar);
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.outWidth = OSUtil.dp2px(context, 16);
        option.outHeight = OSUtil.dp2px(context, 16);
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.seek_thumb, option);
        seekBar.setThumb(new BitmapDrawable(originalBitmap));
        addView(rootView,new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
    }
    private void initClickListener() {
        startBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPause){
                    resume();
                }else if(livePlayer.isPlaying()){
                    pause();
                }else {
                    startVideo();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean bFromUser) {
//                updateTime.setText(String.format("%02d:%02d",progress/60, progress%60));
                updateTime.setText(DateFormatUtil.longToStringByhhmmss(progress));
                if(mLvbPlayerListener != null)
                    mLvbPlayerListener.updateProgress(progress,0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                mStartSeek = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setSeek(seekBar.getProgress());
//                mStartSeek = false;
            }
        });
    }

    public void setListener(LVBPlayerListener listener){
        this.mLvbPlayerListener = listener;
    }

    public boolean isPlaying(){
        return livePlayer.isPlaying();
    }

    public boolean isPause(){
        return isPause;
    }

    /**
     * 拖动进度条后设置视频进度
     * @param progress
     */
    public void setSeek(int progress){
        if ( livePlayer != null) {
            livePlayer.seek(progress);
        }
        mTrackingTouchTS = System.currentTimeMillis();
    }
    /**
     * 是否隐藏控制播放器按钮
     * @param isHide true表示隐藏，false表示显示
     */
    public void hideControlerView(boolean isHide){
        progressLayout.setVisibility(isHide ? View.GONE : View.VISIBLE);
    }

    /**
     * 设置进度条布局离底部的距离
     * @param dp
     */
    public void setProgressLayoutMaginBottom(int dp){
        if(progressLayout==null) return;
        RelativeLayout.LayoutParams params = (LayoutParams) progressLayout.getLayoutParams();
        params.bottomMargin = OSUtil.dp2px(context,dp);
        progressLayout.setLayoutParams(params);
    }

    public void startVideo() {
        if("-1".equals(playConfig)){
            Toast.makeText(context,"播放地址为空！",Toast.LENGTH_SHORT).show();
            return;
        }
        if("-1".equals(checkPlayUrl(playUrl))){
            Toast.makeText(context,"未找到播放地址！",Toast.LENGTH_SHORT).show();
            return;
        }
        LoadingDialog.getInstance(context).showProcessDialog();
        int result = livePlayer.startPlay(playUrl,mPlayType); // result返回值：0 success;  -1 empty url; -2 invalid url; -3 invalid playType;
        if (result == -2) {
            Toast.makeText(context, "非腾讯云链接地址，不能播放！", Toast.LENGTH_SHORT).show();
            LoadingDialog.getInstance(context).hideProcessDialog(1);
        }
        if (result != 0) {
            LoadingDialog.getInstance(context).hideProcessDialog(1);
            startBtn.setImageResource(R.drawable.vp_play);
        }
    }

    public void pause(){
        isPause = true;
        livePlayer.pause();
        startBtn.setImageResource(R.drawable.vp_play);
    }

    public void resume(){
        if("-1".equals(playUrl)) return;

        if (isPause) {
            if (livePlayer != null) {
                livePlayer.resume();
                startBtn.setImageResource(R.drawable.vp_pause);
            }
        }

        if (videoView != null){
            videoView.onResume();
        }
        isPause = false;

    }

    public void stop(){
        startBtn.setImageResource(R.drawable.vp_play);
        if (livePlayer != null) {
            livePlayer.stopPlay(true);
        }

    }

    public void destroy(){
        if (livePlayer != null) {
            livePlayer.stopPlay(true);
            livePlayer.setPlayListener(null);
        }
        if (videoView != null){
            videoView.onDestroy();
        }
        setListener(null);
    }

    public void setPlayUrl(String url){
        this.playUrl = url;
    }

    /**
     * 开始视屏
     * 注：先设置地址，再调用开始；
     */
    public void start(){
        startVideo();
    }

    /**
     * 判断视频地址
     * @param playUrl
     * @return
     */
    private int checkPlayUrl(String playUrl) {
        if (TextUtils.isEmpty(playUrl) || (!playUrl.startsWith("http:") && !playUrl.startsWith("https:") && !playUrl.startsWith("rtmp:"))) {
            Toast.makeText(context, "播放地址不合法，目前仅支持rtmp,flv,hls,mp4播放方式!", Toast.LENGTH_SHORT).show();
            return -1;
        }

        if (playUrl.startsWith("http:") || playUrl.startsWith("https:")) {
            if (playUrl.contains(".flv")) {
                mPlayType = TXLivePlayer.PLAY_TYPE_VOD_FLV;
            } else if (playUrl.contains(".m3u8")) {
                mPlayType = TXLivePlayer.PLAY_TYPE_VOD_HLS;
            } else if (playUrl.toLowerCase().contains(".mp4")) {
                mPlayType = TXLivePlayer.PLAY_TYPE_VOD_MP4;
            } else {
                Toast.makeText(context, "播放地址不合法，点播目前仅支持flv,hls,mp4播放方式!", Toast.LENGTH_SHORT).show();
                return -1;
            }
        } else {
            Toast.makeText(context, "播放地址不合法，点播目前仅支持flv,hls,mp4播放方式!", Toast.LENGTH_SHORT).show();
            return -1;
        }
        return mPlayType;
    }

    @Override
    public void onPlayEvent(int event, Bundle bundle) {
        if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            // 开始
            isPause = false;
            startBtn.setImageResource(R.drawable.vp_pause);
            LoadingDialog.getInstance(context).hideProcessDialog(0);

        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS ) {
            // 进度条
            int progress = bundle.getInt(TXLiveConstants.EVT_PLAY_PROGRESS);
            int duration = bundle.getInt(TXLiveConstants.EVT_PLAY_DURATION);
            updateProgress(progress, duration);
            return;
        } else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
            LoadingDialog.getInstance(context).hideProcessDialog(0);
            stop();
            isPause = false;
            if (updateTime != null)
                updateTime.setText("00:00");

            if (seekBar != null)
                seekBar.setProgress(0);
            startBtn.setImageResource(R.drawable.vp_play);
            if(mLvbPlayerListener != null)
                mLvbPlayerListener.updateProgress(-1,-1);
        } else if(event == TXLiveConstants.PLAY_EVT_PLAY_END){
            seekBar.setProgress(seekBar.getMax());
            if(mLvbPlayerListener != null)
                mLvbPlayerListener.endPlay();
            startBtn.setImageResource(R.drawable.vp_play);

        }else if (event == TXLiveConstants.PLAY_EVT_PLAY_LOADING){
            // 加载中

        }

    }

    private boolean isVertical = true;
    public void changeOrientation(){
        if(isVertical){
            livePlayer.setRenderRotation(270);
            isVertical = false;
        }else{
            livePlayer.setRenderRotation(0);
            isVertical = true;
        }
    }

    private void updateProgress(int progress, int duration) {
        Log.e(Tag,"progress= " + progress + " , " + "duration= " +duration);
        long curTS = System.currentTimeMillis();
        // 避免滑动进度条松开的瞬间可能出现滑动条瞬间跳到上一个位置
        if (Math.abs(curTS - mTrackingTouchTS) < 500)
            return;

        mTrackingTouchTS = curTS;

        if (seekBar != null) {
            seekBar.setMax(duration);
            seekBar.setProgress(progress);
        }

        if (updateTime != null)
            updateTime.setText(DateFormatUtil.longToStringByhhmmss(progress));

        if (totalTime != null)
            totalTime.setText("/" + DateFormatUtil.longToStringByhhmmss(duration));

        if(mLvbPlayerListener != null)
            mLvbPlayerListener.updateProgress(progress,duration);
    }

    @Override
    public void onNetStatus(Bundle bundle) {

    }

}
