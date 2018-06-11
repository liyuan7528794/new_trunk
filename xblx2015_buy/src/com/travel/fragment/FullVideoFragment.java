package com.travel.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ctsmedia.hltravel.R;
import com.tencent.qcloud.suixinbo.views.LVBPlayerControler;
import com.travel.activity.HomeActivity;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.utils.TravelUtil;
import com.travel.video.layout.SmallVideoWindow;
import com.travel.video.playback_video.PlaybackPlayerHandler;
import com.travel.video.tools.RandomSetColor;
import com.travel.widget.VerticalAdapter;
import com.travel.widget.VerticalViewPager;

import java.util.ArrayList;

/**
 * 仿抖音版的页面（新版首页中先展示）
 * Created by wyp on 2018/4/27.
 */

public class FullVideoFragment extends Fragment implements LVBPlayerControler.LVBPlayerListener {
    private View mView;
    private VerticalViewPager vvp_slide_video;
    private VerticalAdapter mVerticalAdapter;
    private ArrayList<View> list;
    // 视频数据相关
    private ArrayList<VideoInfoBean> videos = new ArrayList<>();
    private VideoInfoBean videoBean;
    // 视频控件相关
    private ImageView iv_play_pause;
    private LVBPlayerControler mLvbPlayerControler;
    // 视频源相关
    private int curVideoIndex = 0;
    private String[] urls = new String[1];

    private PlaybackPlayerHandler playBackHandler;
    private View view;
    private boolean isReget = false;
    private boolean isFirst = true;
    // 刷新相关
    private HomeActivity activity;
    private PlayOutFragment playOutFragment;

    public static FullVideoFragment newInstance(ArrayList<VideoInfoBean> videoInfoBeans) {
        FullVideoFragment fullVideoFragment = new FullVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("videoInfo", videoInfoBeans);
        fullVideoFragment.setArguments(bundle);
        return fullVideoFragment;
    }

    private UpdateData updateData;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context != null) {
            HomeActivity homeActivity = (HomeActivity) context;
            updateData = homeActivity.getInstance();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_full_video, container, false);
        initView();
        //初始化弹幕颜色
        RandomSetColor.randomColorAdapter();
        return mView;
    }

    private void initView() {
        activity = (HomeActivity) getActivity();
        playOutFragment = activity.getInstance();
        list = new ArrayList<>();
        vvp_slide_video = (VerticalViewPager) mView.findViewById(R.id.vvp_slide_video);
        vvp_slide_video.setOnPageChangeListener(new OnPageChangeListenerImpl());
        videos = (ArrayList<VideoInfoBean>) getArguments().getSerializable("videoInfo");
        for (int i = 0; i < videos.size(); i++) {
            view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_playback_video_player, null);
            list.add(view);
        }
        mVerticalAdapter = new VerticalAdapter(list);
        vvp_slide_video.setAdapter(mVerticalAdapter);
        if (videos.size() > 0) {
            playVideo(0, false);
        }
    }

    private boolean isStop;
    private View.OnClickListener StartPlayListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isStop) {
                reOpenPlayer(curVideoIndex);
//                iv_play_pause.setImageResource(R.drawable.vp_pause);
                iv_play_pause.setVisibility(View.INVISIBLE);
            } else if (mLvbPlayerControler.isPause()) {
                mLvbPlayerControler.resume();
                iv_play_pause.setVisibility(View.INVISIBLE);
//                iv_play_pause.setImageResource(R.drawable.vp_pause);
            } else if (mLvbPlayerControler.isPlaying()) {
                mLvbPlayerControler.pause();
                iv_play_pause.setImageResource(R.drawable.icon_video_play);
                iv_play_pause.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public void updateProgress(int progress, int duration) {

    }

    @Override
    public void endPlay() {
        iv_play_pause.setImageResource(R.drawable.icon_video_play);
        iv_play_pause.setVisibility(View.VISIBLE);
        isStop = true;
        if (curVideoIndex < urls.length - 1) {
            reOpenPlayer(curVideoIndex + 1);
        } else {
            reOpenPlayer(0);
        }
    }

    private void reOpenPlayer(int index) {
        try {
            Thread.currentThread().sleep(1000);
        } catch (Exception e) {
        }
        newPlayer(index);
    }

    private void newPlayer(int index) {
        iv_play_pause.setVisibility(View.INVISIBLE);
        curVideoIndex = index;
        mLvbPlayerControler.setPlayUrl(urls[index]);
        mLvbPlayerControler.startVideo();
        isStop = false;
    }

    class OnPageChangeListenerImpl implements VerticalViewPager.OnPageChangeListener {
        private int pos = 0;// 记录当前位置
        private int count = 0;// 记录为0的次数
        private boolean canGo = true;
        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
            if (canGo) {
                if (position == 0) {
                    if (positionOffset == positionOffsetPixels) {
                        ++count;
                    }
                    if (count > 2) {
                        playOutFragment.refresh();
                        canGo = false;
                    }
//                } else if (position == videos.size() -1) {
//                    if (positionOffset == positionOffsetPixels) {
//                        ++count;
//                    }
//                    if (count > 2) {
//                        playOutFragment.load();
//                        canGo = false;
//                    }
                }

            }
        }

        @Override
        public void onPageSelected(int position) {
            if (pos != position) {
                playOutFragment.stopRefresh();
                updatePersonalData(position);
                playVideo(pos, true);
                playVideo(position, false);
            } else  if (count > 2) {

            }
            pos = position;// 记录新的位置
            count = 0;
            canGo = true;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    private void updatePersonalData(int position) {
        String id = videos.get(position).getPersonalInfoBean().getUserId();
        updateData.update(id);
    }

    private void playVideo(int position, boolean isPause) {
        videoBean = videos.get(position);
        view = list.get(position);
        iv_play_pause = (ImageView) view.findViewById(R.id.iv_play_pause);
        mLvbPlayerControler = (LVBPlayerControler) view.findViewById(R.id.lvb_player_controler);
        mLvbPlayerControler.hideControlerView(true);
        mLvbPlayerControler.setListener(FullVideoFragment.this);
        mLvbPlayerControler.setOnClickListener(StartPlayListener);
        playBackHandler = new PlaybackPlayerHandler(getActivity(), view, videoBean);
        playBackHandler.setCountListener(new PlaybackPlayerHandler.CountListener() {
            @Override
            public void onZanCount(int count) {
                videoBean.setPraiseNum(count);
            }

            @Override
            public void onCommantCount(int count) {
                videoBean.setCommentCount(count);
            }
        });
        if (isPause) {
            pauseVideo();
        } else {
            if (videoBean.isNullVideoUrl()) {
                TravelUtil.showToast("未找到视频源！");
                return;
            }
            if (videoBean.getUrl().contains(",")) {
                urls = videoBean.getUrl().split(",");
            } else {
                urls[0] = videoBean.getUrl();
            }
            newPlayer(0);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (PlaybackPlayerHandler.isPause) {
            pauseVideo();
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        SmallVideoWindow.getInstance().hidePopupWindow();

//        if (PlaybackPlayerHandler.isPause) {
//            if (!isStop && iv_play_pause != null) {
////                iv_play_pause.setImageResource(R.drawable.vp_pause);
//                iv_play_pause.setVisibility(View.INVISIBLE);
//                mLvbPlayerControler.resume();
//            }
//        }
//        PlaybackPlayerHandler.isPause = true;
        if (!isFirst) {
            updateData();
        }
        isFirst = false;
    }

    @Override
    public void onDestroy() {
        SmallVideoWindow.getInstance().hidePopupWindow();
        if (mLvbPlayerControler != null)
            mLvbPlayerControler.destroy();
        super.onDestroy();
    }

    public interface UpdateData {
        void update(String id);
    }

    /**
     * 登录变换后的关注和点赞的数据的更新
     */
    public void updateData() {
        if (!isReget) {
            isReget = true;
            playBackHandler.getFollowZanData();
        } else {
            isReget = false;
        }
    }

    private void pauseVideo() {
        if (mLvbPlayerControler != null && mLvbPlayerControler.isPlaying()) {
            mLvbPlayerControler.pause();
            iv_play_pause.setImageResource(R.drawable.icon_video_play);
            iv_play_pause.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseVideo();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (!isVisibleToUser) {
            pauseVideo();
        }
    }
}
