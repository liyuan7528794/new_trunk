package com.travel.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.views.LVBPlayerControler;
import com.travel.app.TravelApp;
import com.travel.bean.VideoInfoBean;
import com.travel.layout.DialogTemplet;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.video.playback_video.PlaybackPlayerHandler;

import java.util.List;

/**
 * 首页视频的适配器
 * Created by wyp on 2018/5/23.
 */

public class MainVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<VideoInfoBean> list;
    private VideoInfoBean videoInfoBean;

    public MainVideoAdapter(Context context, List<VideoInfoBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.activity_playback_video_player, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        lvbPlayerControler = ((MyViewHolder) holder).lvb_player_controller;
        pause = ((MyViewHolder) holder).iv_play_pause;
        videoInfoBean = list.get(position);
        ((MyViewHolder) holder).iv_play_pause.setVisibility(View.GONE);
        ((MyViewHolder) holder).lvb_player_controller.hideControlerView(true);
        ((MyViewHolder) holder).lvb_player_controller.setPlayUrl(videoInfoBean.getUrl());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(OSUtil.getScreenWidth(), OSUtil.getScreenHeight());
        ((MyViewHolder) holder).frameLayout.setLayoutParams(layoutParams);
        ((MyViewHolder) holder).lvb_player_controller.setListener(new LVBPlayerControler.LVBPlayerListener() {
            @Override
            public void updateProgress(int progress, int duration) {

            }

            @Override
            public void endPlay() {
                isCanPlay();
            }
        });
        if (videoInfoBean.getPlayStatus() == 0) {
            isCanPlay();
        } else {
            ((MyViewHolder) holder).iv_play_pause.setVisibility(View.VISIBLE);
            ((MyViewHolder) holder).lvb_player_controller.stop();
        }

        ((MyViewHolder) holder).lvb_player_controller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((MyViewHolder) holder).lvb_player_controller.isPause()) {
                    ((MyViewHolder) holder).iv_play_pause.setVisibility(View.GONE);
                    ((MyViewHolder) holder).lvb_player_controller.resume();
                } else if (((MyViewHolder) holder).lvb_player_controller.isPlaying()) {
                    ((MyViewHolder) holder).iv_play_pause.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).lvb_player_controller.pause();
                } else {
                    isCanPlay();
                }
            }
        });

        playbackPlayerHandler = new PlaybackPlayerHandler((Activity) context, holder.itemView, videoInfoBean);
        // 赞数和评论数的更新
        playbackPlayerHandler.setCountListener(new PlaybackPlayerHandler.CountListener() {
            @Override
            public void onZanCount(int count) {
                if (countListener != null) {
                    countListener.onZanCountChanged(holder.getLayoutPosition(), count);
                }
            }

            @Override
            public void onCommantCount(int count) {
                if (countListener != null) {
                    countListener.onCommentCountChanged(holder.getLayoutPosition(), count);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        View frameLayout;
        LVBPlayerControler lvb_player_controller;
        ImageView iv_play_pause;

        public MyViewHolder(View view) {
            super(view);
            frameLayout = view.findViewById(R.id.frameLayout);
            lvb_player_controller = (LVBPlayerControler) view.findViewById(R.id.lvb_player_controler);
            iv_play_pause = (ImageView) view.findViewById(R.id.iv_play_pause);
        }
    }


    private LVBPlayerControler lvbPlayerControler;

    public LVBPlayerControler getLvbPlayerControler() {
        return lvbPlayerControler;
    }

    private ImageView pause;

    public ImageView getPauseIcon() {
        return pause;
    }

    private PlaybackPlayerHandler playbackPlayerHandler;

    public PlaybackPlayerHandler getPlaybackPlayerHandler() {
        return playbackPlayerHandler;
    }

    public interface CountListener {
        void onZanCountChanged(int position, int count);

        void onCommentCountChanged(int position, int count);
    }

    private CountListener countListener;

    public void setCountListener(CountListener countListener) {
        this.countListener = countListener;
    }

    private void isCanPlay() {
        String netType = CheckNetStatus.checkNetworkConnection();
        if (CheckNetStatus.wifiNetwork.equals(netType)) {
            pause.setVisibility(View.GONE);
            lvbPlayerControler.start();
        }

        if (CheckNetStatus.unNetwork.equals(netType)) {// 没网
            TravelUtil.showToast("当前无网络，请检查网络！");
            pause.setVisibility(View.VISIBLE);
            lvbPlayerControler.stop();
        }

        if (!CheckNetStatus.unNetwork.equals(netType) && !CheckNetStatus.wifiNetwork.equals(netType)) {
            if ("UNKNOWN".equals(netType)) {
                TravelUtil.showToast("当前无网络，请检查网络！");
                pause.setVisibility(View.VISIBLE);
                lvbPlayerControler.stop();
            }
            //弹框提醒
            final DialogTemplet dialog = AlertDialogUtils.getNetStatusDialog(netType, context);
            dialog.setLeftClick(new DialogTemplet.DialogLeftButtonListener() {

                @Override
                public void leftClick(View view) {
                    pause.setVisibility(View.VISIBLE);
                    lvbPlayerControler.stop();
                    dialog.dismiss();
                }
            });
            dialog.setRightClick(new DialogTemplet.DialogRightButtonListener() {

                @Override
                public void rightClick(View view) {
                    pause.setVisibility(View.GONE);
                    lvbPlayerControler.start();
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }


}