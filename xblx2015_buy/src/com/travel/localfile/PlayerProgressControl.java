package com.travel.localfile;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;

/**
 * Created by Administrator on 2017/4/26.
 */

public class PlayerProgressControl extends FrameLayout{
    private Context mContext;
    private ImageView start;
    private TextView startTime, endTime;
    private SeekBar seekBar;
    private boolean isPlaying = false;

    private ProgressListener mListener;

    public PlayerProgressControl(Context context) {
        this(context, null);
    }
    public PlayerProgressControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public PlayerProgressControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    public interface ProgressListener{
        void onProgressChanged(int progress);
        void onStartPlay();
        void onPausePlay();
    }

    private void initView() {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.layout_player_control, null);
        start = (ImageView) rootView.findViewById(R.id.iv_start);
        startTime = (TextView) rootView.findViewById(R.id.tv_start_time);
        endTime = (TextView) rootView.findViewById(R.id.tv_end_time);
        seekBar = (SeekBar) rootView.findViewById(R.id.seek_bar);
        addView(rootView);

        setListener();
    }

    private void setListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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

        start.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener == null) return;
                if(isPlaying){
                    mListener.onPausePlay();
                }else{
                    mListener.onStartPlay();
                }
            }
        });
    }

    public void setProgressListener(ProgressListener listener){
        this.mListener = listener;
    }

    /**
     * 设置进度条进度
     * 0 - 100
     * @param progress
     */
    public void updateProgress(int progress){
        progress = Math.min(1000, progress < 0 ? 0 : progress);
        seekBar.setProgress(progress);
    }

    public void setStartTime(String time){
        startTime.setText(time);
    }

    public void setEndTime(String time){
        endTime.setText(time);
    }

    public void startPlayStatus(){
        start.setImageResource(R.drawable.camera_icon_pause_black);
        isPlaying = true;
    }

    public void stopPlayStatus(){
        start.setImageResource(R.drawable.camera_icon_play_black);
        isPlaying = false;
    }

}
