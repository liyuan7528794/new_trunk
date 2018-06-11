package com.travel.localfile;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.ctsmedia.hltravel.R;
import com.travel.activity.OneFragmentActivity;
import com.travel.bean.VideoInfoBean;
import com.travel.communication.helper.PlayerHelper;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.localfile.dao.LocalFile;
import com.travel.shop.tools.ShopTool;
import com.travel.video.layout.VideoViewPopWindow;
import com.travel.video.playback_video.PlaybackVideoPlayerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import tv.danmaku.ijk.media.player.media.IjkVideoView;

/**
 * List<LocalFile> localFileList = mSQLiteHelper.loadAllFile(mUserId, null);
 * 查看本地图片的轮滚图
 * Created by ldkxingzhe on 2016/6/29.
 */
public class LocalFileLookFragment extends Fragment
        implements ViewPager.OnPageChangeListener,
        OneFragmentActivity.OneFragmentInterface {
    @SuppressWarnings("unused")
    private static final String TAG = "LocalFileLookFragment";

    /* 资料列表 */
    public static final String LOCAL_FILE_LIST = "local_file_list";
    /* 选中的文件列表 */
    public static final String SELECTED_LOCAL_FILE_LIST = "selected_position";
    /* 当前点击的位置 */
    public static final String CURRENT_POSITION = "current_position";
    /* 拥有选择的特性 */
    public static final String HAS_FEATURE_SELECT = "has_feature_select";
    /**
     * 是否拥有删除的功能
     */
    public static final String HAS_FEATURE_DELETE = "has_feature_delete";

    private ViewPager mViewPager;
    private IjkVideoView mVideoView;
    private VideoViewPopWindow mVideoViewPopWindow;
    private View mBottomBar, mTitleBar;
    private TextView mTitle;
    private TextView mTitleBarRight;
    private ToggleButton mSelectedCheckBox;
    private View mEmptyView;

    private View mAudioPlayerContainer;
    private VisualizerView visualizerView;
    private ArcProgress mArcProgress;
    private PlayerProgressControl progressControl;
    private ImageView closeVoice;
    private TextView mAudioPlayerTimer;
    private PlayerHelper mAudioPlayer;
    private Visualizer mVisualizer;
    private TimeCount mAudioTimer;
    private TextView mSpeakTextView;

    private ArrayList<LocalFile> mLocalFileList;
    private ArrayList<LocalFile> mSelectedFileList;
    private int mCurrentPosition;
    private boolean mHasFeatureSelected;
    private boolean mHasFeatureDelete;
    private boolean mIsSpeakMode = true;
    private boolean mHasFeatureDeleteFromLocal;
    private int mAllCanSelectedNum;

    private LocalFileSQLiteHelper mSQFileSQLiteHelper;

    private MViewPagerAdapter mAdapter;
    private Handler handler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new MViewPagerAdapter();
        Bundle args = getArguments();
        if (args == null)
            throw new IllegalStateException("args is null");
        mLocalFileList = (ArrayList<LocalFile>) args.getSerializable(LOCAL_FILE_LIST);
        mCurrentPosition = args.getInt(CURRENT_POSITION, 0);
        mSelectedFileList = (ArrayList<LocalFile>) args.getSerializable(SELECTED_LOCAL_FILE_LIST);
        if (mSelectedFileList == null)
            mSelectedFileList = new ArrayList<LocalFile>();
        mHasFeatureSelected = args.getBoolean(HAS_FEATURE_SELECT, false);
        mHasFeatureDelete = args.getBoolean(HAS_FEATURE_DELETE, false);
        mHasFeatureDeleteFromLocal = args.getBoolean(LocalFileGridFragment.HAS_FEATURE_DELETE_FROM_LOCAL, false);
        mAllCanSelectedNum = args.getInt(LocalFileGridFragment.ALL_CAN_SELECTED_NUM, 0);
        mSQFileSQLiteHelper = new LocalFileSQLiteHelper(getActivity());
        mSQFileSQLiteHelper.init();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_local_look, container, false);
        mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        mTitleBar = rootView.findViewById(R.id.title_bar);
        mBottomBar = rootView.findViewById(R.id.ll_bottom_bar);
        mAudioPlayerContainer = rootView.findViewById(R.id.rl_audio_player_container);
        mEmptyView = rootView.findViewById(R.id.tv_empty_view);
        initBottomBar();
        initTitleBar();
        initAudioPlayerLayout();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setCurrentItem(mCurrentPosition);
        // 消耗View的点击事件
        mEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        return rootView;
    }

    private void initAudioPlayerLayout() {
        mArcProgress = (ArcProgress) mAudioPlayerContainer.findViewById(R.id.circle_progress);
        visualizerView = (VisualizerView) mAudioPlayerContainer.findViewById(R.id.visualizer_view);
        progressControl = (PlayerProgressControl) mAudioPlayerContainer.findViewById(R.id.progressControler);
        closeVoice = (ImageView) mAudioPlayerContainer.findViewById(R.id.close_voice);
        mAudioPlayerTimer = (TextView) mAudioPlayerContainer.findViewById(R.id.tv_audio_timer);
        mAudioPlayerContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                releaseAudioPlayer();
            }
        });
        mSpeakTextView = (TextView) mAudioPlayerContainer.findViewById(R.id.tv_speak);

        progressControl.setProgressListener(new PlayerProgressControl.ProgressListener() {
            @Override
            public void onProgressChanged(int progress) {
                if(mAudioPlayer != null && mAudioPlayer.getPlayer() != null)
                    mAudioPlayer.getPlayer().seekTo(mAudioPlayer.getPlayer().getDuration() * progress / 1000);
            }

            @Override
            public void onStartPlay() {
                if(mAudioPlayer != null && mAudioPlayer.getPlayer() != null) {
                    mAudioPlayer.getPlayer().start();
                    progressControl.startPlayStatus();
                    mVisualizer.setEnabled(true);
                }
            }

            @Override
            public void onPausePlay() {
                if(mAudioPlayer != null && mAudioPlayer.getPlayer() != null) {
                    mAudioPlayer.getPlayer().pause();
                    progressControl.stopPlayStatus();
                    mVisualizer.setEnabled(false);
                }
            }
        });

        closeVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressControl.stopPlayStatus();
                releaseAudioPlayer();
            }
        });

        mSpeakTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsSpeakMode = !mIsSpeakMode;
                // TODO: 与服务器交互可能有问题
                startPlayRecorder(mLocalFileList.get(mCurrentPosition).getLocalPath());
            }
        });
    }

    private void releaseAudioPlayer() {
        if (mAudioPlayer == null)
            return;
        mAudioPlayer.release();
        mAudioTimer.reset();
        mAudioTimer = null;
        mAudioPlayer = null;
        mVisualizer.setEnabled(false);
        handler.post(new Runnable() {
            @Override
            public void run() {
                setAudioContainerVisible(false);
            }
        });
    }

    private void startPlayRecorder(String filePath) {
        releaseAudioPlayer();
        setAudioContainerVisible(true);
        mAudioTimer = new TimeCount(new TimeCount.Callback() {
            @Override
            public void onTimerCallback(long totalMilliseconds, boolean isWholeSeconds) {
                if (mAudioPlayer == null) {
                    releaseAudioPlayer();
                    return;
                }
                MediaPlayer mediaPlayer = mAudioPlayer.getPlayer();
                if (mediaPlayer == null || !mediaPlayer.isPlaying())
                    return;
//                mAudioPlayerTimer.setText(mAudioTimer.formatString() + "/" +
//                        DateFormatUtil.longMillisecondsFormat(
//                                mAudioPlayer.getPlayer().getDuration()));
                int currentPosition = mAudioPlayer.getPlayer().getCurrentPosition();
                int duration = mAudioPlayer.getPlayer().getDuration();
//                mArcProgress.setProgress(currentPosition * 100 / duration);
                progressControl.setStartTime(ShopTool.secondToTime(currentPosition / 1000 + ""));
                progressControl.setEndTime(ShopTool.secondToTime(duration / 1000 + ""));
                progressControl.updateProgress((int) (currentPosition * 1000f / duration ));
            }

            @Override
            public void onTimerReset() {

            }
        });
        mAudioPlayer = PlayerHelper.getInstance(getActivity(), null);
        mAudioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                releaseAudioPlayer();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressControl.stopPlayStatus();
                    }
                });
            }
        });
        mAudioPlayer.playerFullPathAudio(filePath, mIsSpeakMode);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressControl.startPlayStatus();
                mAudioTimer.start();

                // 获取频谱
                mVisualizer = new Visualizer(mAudioPlayer.getPlayer().getAudioSessionId());
                mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
                mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                    boolean isTag = true;
                    //这个回调应该采集的是波形数据
                    @Override
                    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform,
                                                      int samplingRate) {
                        //waveformView 是一个自定义的view用来按照波形来画图 一会后面再讲
                        if(isTag) {
                            visualizerView.updateAmplitude(OSUtil.createRandom() * 0.3f);
                            isTag = false;
                        }else{
                            isTag = true;
                        }
                    }

                    //这个回调应该采集的是快速傅里叶变换有关的数据，没试过，回头有空了再试试
                    @Override
                    public void onFftDataCapture(Visualizer visualizer, byte[] fft,
                                                 int samplingRate) {
                        // TODO Auto-generated method stub
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
                mVisualizer.setEnabled(true);
            }
        }, 500);
        mSpeakTextView.setVisibility(View.GONE);
        //        mSpeakTextView.setText(mIsSpeakMode ? "外放模式" : "耳机模式");

    }


    private boolean mIsNeedLoadAudioPlayerBg = true;

    private void setAudioContainerVisible(final boolean visible) {
        if (visible && mIsNeedLoadAudioPlayerBg) {
            mIsNeedLoadAudioPlayerBg = false;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                mAudioPlayerContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        });

        int otherVisibility = visible ? View.GONE : View.VISIBLE;

        mTitleBar.setVisibility(otherVisibility);
        if (mHasFeatureSelected) {
            mBottomBar.setVisibility(otherVisibility);
        }
        mViewPager.setVisibility(otherVisibility);
    }

    private void initBottomBar() {
        mSelectedCheckBox = (ToggleButton) mBottomBar.findViewById(R.id.cb_select);
        if (mHasFeatureSelected) {
            mBottomBar.setVisibility(View.VISIBLE);
        } else {
            mBottomBar.setVisibility(View.GONE);
        }

        mSelectedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!mSelectedFileList.contains(mLocalFileList.get(mCurrentPosition))) {
                        mSelectedFileList.add(mLocalFileList.get(mCurrentPosition));
                    }
                } else {
                    mSelectedFileList.remove(mLocalFileList.get(mCurrentPosition));
                }
                onChecked(mCurrentPosition, isChecked);
            }
        });
    }

    private boolean onChecked(int position, boolean isChecked) {
        if (isChecked) {
            if (mSelectedFileList.size() > mAllCanSelectedNum
                    && mAllCanSelectedNum > 0) {
                mSelectedFileList.remove(mLocalFileList.get(position));
                mSelectedCheckBox.setChecked(false);
                Toast.makeText(getActivity(),
                        "单次提交最多只能选取" + mAllCanSelectedNum + "项资料", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    private void setCurrentPosition(int currentPosition) {
        mCurrentPosition = currentPosition;
        if (mLocalFileList.size() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
            mTitleBarRight.setEnabled(false);
            onDone(true);
        } else {
            mTitleBarRight.setEnabled(true);
            mEmptyView.setVisibility(View.GONE);
        }
        int tmpPosition = mLocalFileList.size() == 0 ? 0 : mCurrentPosition + 1;
        mTitle.setText(tmpPosition + "/" + mLocalFileList.size());
        if (mLocalFileList.size() == 0)
            return;
        if (mSelectedFileList.contains(mLocalFileList.get(mCurrentPosition))) {
            mSelectedCheckBox.setChecked(true);
        } else {
            mSelectedCheckBox.setChecked(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseAudioPlayer();
    }

    private void initTitleBar() {
        mTitleBar.findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mTitle = (TextView) mTitleBar.findViewById(R.id.tv_page);
        mTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mTitleBarRight = (TextView) mTitleBar.findViewById(R.id.tv_title_right);
        setCurrentPosition(mCurrentPosition);
        if (mHasFeatureDelete) {
            mTitleBarRight.setText("删除");
        } else {
            mTitleBarRight.setText("完成");
            mTitleBarRight.setVisibility(View.GONE);
        }
        mTitleBarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocalFileList == null || mLocalFileList.size() == 0)
                    return;
                if (mHasFeatureDelete) {
                    deleteClick();
                } else {
                    onDone(true);
                }
            }
        });
        //        mTitleBarRight.setVisibility(View.VISIBLE);
    }

    private void setBarVisible(boolean isVisible) {
        int visible = isVisible ? View.VISIBLE : View.GONE;
        OSUtil.setFullScreen(getActivity(), !isVisible);
        mTitleBar.setVisibility(visible);
        if (mHasFeatureSelected) {
            mBottomBar.setVisibility(visible);
        }
    }

    private View.OnClickListener mPlayerImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = mViewPager.getCurrentItem();
            LocalFile localFile = mLocalFileList.get(position);
            switch (localFile.getType()) {
                case CameraFragment.TYPE_VIDEO:
                    if (mVideoViewPopWindow == null) {
                        mVideoViewPopWindow = new VideoViewPopWindow();
                        mVideoView = new IjkVideoView(getActivity());
                    }
                    mVideoView.setVideoPath(localFile.getLocalPath());
                    mVideoView.start();
                    mVideoViewPopWindow.show(getActivity(), mViewPager, mVideoView);
                    break;
                case CameraFragment.TYPE_AUDIO:
                    startPlayRecorder(localFile.getLocalPath());
                    break;
                case CameraFragment.TYPE_LIVE:
                    livePlayBack(localFile);
                    break;
            }
        }
    };

    private void livePlayBack(LocalFile localFile) {
        VideoInfoBean entity = (VideoInfoBean) localFile.getTag();
        Bundle bundle = new Bundle();
        bundle.putSerializable("video_info", entity);
        String netType = CheckNetStatus.checkNetworkConnection();
        if (CheckNetStatus.unNetwork.equals(netType)) {// 没网
            Toast.makeText(getActivity(), "当前无网络，请检查网络！", Toast.LENGTH_SHORT).show();
        } else if (!CheckNetStatus.unNetwork.equals(netType) && !CheckNetStatus.wifiNetwork.equals(netType)) {
            if ("UNKNOWN".equals(netType)) {
                Toast.makeText(getActivity(), "当前无网络，请检查网络！", Toast.LENGTH_SHORT).show();
            } else {
                AlertDialogUtils.netNotifyDialog(netType, bundle, getActivity());
            }
        } else if (CheckNetStatus.wifiNetwork.equals(netType)) {
            Intent intent = new Intent(getActivity(), PlaybackVideoPlayerActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    private View.OnClickListener mImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //            setBarVisible(mTitleBar.getVisibility() != View.VISIBLE);
        }
    };

    private void deleteClick() {
        if (mLocalFileList.size() <= 0) {
            return;
        }
        AlertDialogUtils.alertDialog(getActivity(),
                !mHasFeatureDeleteFromLocal ? "是否移除该项证据?" : "是否删除该项资料",
                new Runnable() {
                    @Override
                    public void run() {
                        mCurrentPosition = mViewPager.getCurrentItem();
                        MLog.v(TAG, "click delete, and position is %d.", mCurrentPosition);
                        LocalFile localFile = mLocalFileList.get(mCurrentPosition);
                        if (localFile.getType() != CameraFragment.TYPE_LIVE && mHasFeatureDeleteFromLocal) {
                            mSQFileSQLiteHelper.delete(localFile);
                            File file = new File(localFile.getLocalPath());
                            file.deleteOnExit();
                        }
                        mLocalFileList.remove(mCurrentPosition);
                        mAdapter.notifyDataSetChanged();
                        setCurrentPosition(mCurrentPosition);
                    }
                });
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        setCurrentPosition(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onBackPressed() {
        if (mVideoViewPopWindow != null && mVideoViewPopWindow.isShowing()) {
            mVideoViewPopWindow.dismiss();
            mVideoView.stopPlayback();
        } else {
            onDone(false);
        }
        return true;
    }

    private void onDone(boolean finishGridFragment) {
        Activity activity = getActivity();
        Intent intent = new Intent();
        intent.putExtra(LocalFileGridFragment.LOCAL_FILE_LIST, mLocalFileList);
        intent.putExtra(LocalFileGridFragment.SELECTED_LOCAL_FILE_LIST, mSelectedFileList);
        if (!mHasFeatureDelete) {
            intent.putExtra("finish_local_file_grid_fragment", finishGridFragment);
        }
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    @Override
    public void onTouchDown() {

    }

    private class MViewPagerAdapter extends PagerAdapter {
        // 奇数, 偶数
        private ImageView[] mImageViews;
        private ImageView[] mTypeViews;
        private TextView[] mTimeViews;
        private View[] mViews;

        public MViewPagerAdapter() {
            mViews = new View[3];
            mImageViews = new ImageView[3];
            mTypeViews = new ImageView[3];
            mTimeViews = new TextView[3];
            for (int i = 0; i < 3; i++) {
                View rootView = getActivity().getLayoutInflater().inflate(R.layout.view_pager_item_local_file, null);
                mImageViews[i] = (ImageView) rootView.findViewById(R.id.iv_bg);
                mTypeViews[i] = (ImageView) rootView.findViewById(R.id.iv_type);
                mTimeViews[i] = (TextView) rootView.findViewById(R.id.tv_time);
                mViews[i] = rootView;
                mTypeViews[i].setOnClickListener(mPlayerImageClickListener);
                mImageViews[i].setOnClickListener(mImageClickListener);
            }
        }

        @Override
        public int getCount() {
            return mLocalFileList == null ? 0 : mLocalFileList.size();
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
/*            if(getCount() == 0){
                mDeleteBtn.setEnabled(false);
            }else{
                mDeleteBtn.setEnabled(true);
            }*/
        }

        @Override
        public int getItemPosition(Object object) {
            MLog.v(TAG, "getItemPosition");
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            MLog.v(TAG, "instantiateItem, and position is %d.", position);
            LocalFile localFile = mLocalFileList.get(position);
            int index = getCurrentViewIndex(position);
            ViewGroup viewGroups = (ViewGroup) mViews[index].getParent();
            if (viewGroups != null)
                viewGroups.removeView(mViews[index]);
            String imagePath = null;
            int drawableId = -1;
            boolean typeViewVisible = true;
            switch (localFile.getType()) {
                case CameraFragment.TYPE_AUDIO:
                    imagePath = "";
//                    drawableId = R.drawable.camera_icon_play_voice;
                    drawableId = R.drawable.detail_icon_voice_black;
                    break;
                case CameraFragment.TYPE_VIDEO:
                    imagePath = "file://" + localFile.getLocalPath() + "_thumbnail";
                    drawableId = R.drawable.camera_icon_play;
                    break;
                case CameraFragment.TYPE_PHOTO:
                    imagePath = "file://" + localFile.getLocalPath();
                    typeViewVisible = false;
                    break;
                case CameraFragment.TYPE_LIVE:
                    imagePath = localFile.getThumbnailPath();
                    typeViewVisible = true;
                    drawableId = R.drawable.camera_icon_play;
                    break;
            }
            if (typeViewVisible) {
                if (localFile != null) {
                    String time = DateFormatUtil.formatTime(new Date(localFile.getCreateTime()), DateFormatUtil.FORMAT_TIME_NO_SECOND);
//                    String time = DateFormatUtil.longMillisecondsFormat(localFile.getDuration());
                    mTimeViews[index].setText(time);
                }
                mTypeViews[index].setImageResource(drawableId);
            }
            mTypeViews[index].setVisibility(typeViewVisible ? View.VISIBLE : View.GONE);
            mTimeViews[index].setVisibility(typeViewVisible ? View.VISIBLE : View.GONE);
            if (TextUtils.isEmpty(imagePath))
//                mImageViews[index].setImageResource(R.drawable.bg_record_voice);
                mImageViews[index].setImageResource(R.color.white);
            else
                ImageDisplayTools.displayImage(imagePath, mImageViews[index]);
            container.addView(mViews[index]);
            return mViews[index];
        }

        private int getCurrentViewIndex(int position) {
            return position % mImageViews.length;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            MLog.v(TAG, "destroyItem, and position is %d.", position);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
