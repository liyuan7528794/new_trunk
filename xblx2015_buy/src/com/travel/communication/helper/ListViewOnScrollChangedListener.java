package com.travel.communication.helper;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.ctsmedia.hltravel.R;
import com.travel.communication.adapter.MessageAdapter;
import com.travel.lib.utils.MLog;

/**
 * ListView滚动时显示视频的辅助类
 * Created by ldkxingzhe on 2017/1/12.
 */
public class ListViewOnScrollChangedListener implements AbsListView.OnScrollListener{
    @SuppressWarnings("unused")
    private static final String TAG = "ListViewOnScrollChangedListener";
    private final ListView mListView;
    private final MessageAdapter mMsgAdapter;
    private final VideoView mVideoView;

    public ListViewOnScrollChangedListener(@NonNull ListView listView, @NonNull MessageAdapter adapter){
        mListView = listView;
        mMsgAdapter = adapter;
        mVideoView = new VideoView(listView.getContext());
        mMsgAdapter.setVideoView(mVideoView);
        listView.setOnScrollListener(this);
    }

    public VideoView getVideoView(){
        return mVideoView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState == SCROLL_STATE_IDLE){
            if(mVideoView.getParent() != null
                    && isView1VerticalContainerView2(mListView, mVideoView)){
                return;
            }
            for (int i = 0; i < mListView.getChildCount(); i++){
                View child = mListView.getChildAt(i);
                View videoContainer = child.findViewById(R.id.fl_video_view_container);
                if(videoContainer != null && videoContainer instanceof RelativeLayout){
                    MLog.d(TAG, "找到VideoContainer");
                    final RelativeLayout videoParent = (RelativeLayout) videoContainer;
                    if (isView1VerticalContainerView2(mListView, videoParent)){
                        // 播放视频的位置
                        videoPlay(mVideoView, videoParent);
                        break;
                    }
                }

                if (i + 1== mListView.getChildCount()) MLog.d(TAG, "没有找到播放位置");
            }
        }
    }

    public static void videoPlay(VideoView videoView, final RelativeLayout videoParent){
        stopAnotherVideo(videoView, videoParent);
        if(videoParent.getTag() instanceof String){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            final String videoPath = (String) videoParent.getTag();
            if (!videoView.isPlaying()){
                if (videoView.getParent() != null){
                    ((ViewGroup) videoView.getParent()).removeView(videoView);
                }
                videoParent.addView(videoView, params);
                videoView.setVideoPath(videoPath);
            }
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if(videoPath.equals(videoParent.getTag())){

                    }
                }
            });
            videoParent.setVisibility(View.VISIBLE);

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(videoPath.equals(videoParent.getTag())){
                        videoParent.setVisibility(View.GONE);
                    }
                }
            });
            videoView.start();
            MLog.d(TAG, "startPlay video: %s.", videoPath);
        }
    }

    public static void stopAnotherVideo(VideoView videoView, @Nullable RelativeLayout videoParent) {
        ViewGroup currentVideoParent = (ViewGroup) videoView.getParent();
        if (currentVideoParent != videoParent
                && currentVideoParent != null){
            currentVideoParent.removeView(videoView);
            if(videoView.isPlaying()) videoView.stopPlayback();
            currentVideoParent.setVisibility(View.INVISIBLE);
        }
    }

    public static boolean isView1VerticalContainerView2(View view1, View view2){
        int[] view1Position = new int[2];
        int[] view2Position = new int[2];
        view1.getLocationOnScreen(view1Position);
        view2.getLocationOnScreen(view2Position);
        return view2Position[1] >= view1Position[1]
                && view2Position[1] + view2.getMeasuredHeight() <= view1Position[1] + view1.getMeasuredHeight();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
