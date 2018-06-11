package com.travel.shop.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.travel.AdapterJoiner.AdapterJoiner;
import com.travel.AdapterJoiner.JoinableAdapter;
import com.travel.AdapterJoiner.JoinableLayout;
import com.travel.ShopConstant;
import com.travel.activity.OneFragmentActivity;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.GoodsDetailBean;
import com.travel.bean.GoodsServiceBean;
import com.travel.bean.UDPSendInfoBean;
import com.travel.layout.VideoViewFragment;
import com.travel.layout.WebVideoPopWindow;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.R;
import com.travel.shop.activity.GoodsInfoActivity;
import com.travel.shop.adapter.CommandGoodsAdapter;
import com.travel.shop.adapter.NewGoodsInfoAdapter;
import com.travel.shop.http.StoryInfoH5;
import com.travel.shop.tools.ShopTool;
import com.travel.shop.tools.SuperPlayer;
import com.travel.shop.tools.SuperPlayerManage;
import com.travel.shop.widget.MyRecyclerView;
import com.travel.shop.widget.MyViewPager;

import java.io.IOException;
import java.util.ArrayList;

import tv.danmaku.ijk.media.player.media.IjkVideoView;

/**
 * 故事详情页/纪录片（纪录片结构已废弃）
 */
public class StoryOrVideoFragment extends Fragment {

    private int flag;// 1:纪录片 其他:故事 2: 顶部固定三个视频的模板(在原来上面是图片文字的基础上改的)
    private GoodsDetailBean mGoodsDetailBean;
    private MyViewPager vp;
    private int position;// 点击的选项卡的位置
    private boolean isFirst = true;// 是否是首次进入

    private Context mContext;
    private View view;

    // 刷新控件
    private MyRecyclerView mrv_story_video;
    private AdapterJoiner joiner;
    private LinearLayoutManager manager;

    // 商品(非H5)相关的信息
    private View viewLocal;
    private ImageView iv_goodsinfo_img;
    private TextView tv_goodsinfo_title, tv_goodsinfo_subhead;
    private ArrayList<GoodsServiceBean> goodsStory;
    private NewGoodsInfoAdapter mGoodsInfoAdapter;
    private SuperPlayer playerVoice, playerVideo;
    private int full_position = -1;
    private int lastPostion = -1;

    // h5数据相关
    private WebView wv_h5;
    private FrameLayout fl_h5;
    private boolean isH5;
    private String descriptionUrl;
    private JoinableLayout noH5;
    private AudioManager mAudioManager;
    private View myView;

    // 纪录片的页面推荐视频相关
    private ArrayList<GoodsBasicInfoBean> commandGoods;
    private CommandGoodsAdapter mCommandGoodsAdapter;

    public static StoryOrVideoFragment newInstance(int flag, GoodsDetailBean mGoodsDetailBean, ArrayList<GoodsBasicInfoBean> commandGoods, MyViewPager vp, int position) {
        StoryOrVideoFragment fragment = new StoryOrVideoFragment(vp);
        Bundle args = new Bundle();
        args.putInt("flag", flag);
        args.putSerializable("info", mGoodsDetailBean);
        args.putSerializable("commandGoods", commandGoods);
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    public StoryOrVideoFragment() {

    }
    public StoryOrVideoFragment(MyViewPager vp) {
        this.vp = vp;
    }

    public SuperPlayer getPlayerVoice() {
        return playerVoice;
    }

    public SuperPlayer getPlayerVideo() {
        return playerVideo;
    }

    public LinearLayoutManager getManager() {
        return manager;
    }

    public RecyclerView getRecyclerView() {
        return mrv_story_video;
    }

    public int getPosition() {
        return full_position;
    }

    private static final String TAG = "StoryOrVideoFragment";
    /**
     * 判断是否是初始化Fragment
     */
    private boolean hasStarted = false;
    private String beginTime, endTime;
    private GoodsInfoActivity activity;
    private boolean isVisibleToUser;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
//        activity = (GoodsInfoActivity) getActivity();
        this.isVisibleToUser = isVisibleToUser;
        if (isVisibleToUser) {
            hasStarted = true;
            beginTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
            Log.e(TAG, "setUserVisibleHint: true" + beginTime);
        } else {
            if (hasStarted) {
                endTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
                UDPSendInfoBean bean = new UDPSendInfoBean();
                bean.getData("003_1_" + activity.storyId, mGoodsDetailBean.getGoodsBasicInfoBean().getGoodsTitle() + "_小城故事",
                        ShopConstant.STORY_INFO + "id=" + activity.storyId, beginTime, endTime);
                activity.sendData(bean);
                Log.e(TAG, "setUserVisibleHint: false" + endTime);
            }
        }
    }

    private String storyId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (GoodsInfoActivity) getActivity();
        if (getArguments() != null) {
            flag = getArguments().getInt("flag");
            mGoodsDetailBean = (GoodsDetailBean) getArguments().getSerializable("info");
            commandGoods = (ArrayList<GoodsBasicInfoBean>) getArguments().getSerializable("commandGoods");
            position = getArguments().getInt("position");
            storyId = mGoodsDetailBean.getGoodsBasicInfoBean().getStoryId();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_story_or_video, container, false);
        init();
        initSwipeRefreshView();
        initListener();
        vp.setObjectForPosition(view, position);
        return view;
    }

    private void initListener() {
        mCommandGoodsAdapter.setmOnItemClickListener(new CommandGoodsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String storyId) {
                if (mOnBackListener != null)
                    mOnBackListener.setItSelf(true);
//                GoodsInfoActivity.actionStart(mContext, storyId);
            }
        });
        mGoodsInfoAdapter.setExpandListener(new NewGoodsInfoAdapter.OnExpandListener() {
            @Override
            public void onExpand(boolean isExpand) {
                if (isExpand)
                    goodsStory.get(1).setFlag(2);
                else
                    goodsStory.get(1).setFlag(1);
                mGoodsInfoAdapter.notifyDataSetChanged();
            }
        });
        mGoodsInfoAdapter.setPlayClick(new NewGoodsInfoAdapter.onPlayClick() {
            @Override
            public void onPlayclick(RecyclerView.ViewHolder holder, RelativeLayout image) {
                Log.e(TAG, "视频开始");
                full_position = holder.getAdapterPosition();
                image.setVisibility(View.GONE);
                if (playerVoice != null) {
                    if (playerVoice.isPlaying() || playerVoice.getVideoStatus() == IjkVideoView.STATE_PAUSED)
                        doPlayer(playerVoice);
                    if (playerVoice.isPlaying()) {
                        playerVoice.stopPlayVideo();
                        playerVoice.release();
                    }
                }
                doPlayer(playerVideo);
                playerVideo.play(goodsStory.get(full_position).getContent());
                playerVideo.setTitle(goodsStory.get(full_position).getTitle());
                playerVideo.setFullScreenBackgroud(goodsStory.get(full_position).getBackImage());
                lastPostion = full_position;
                if (scrollToPositionListener != null) {
                    scrollToPositionListener.scrollToPosition(full_position);
                }
            }

            @Override
            public void onVoicePlayclick(RecyclerView.ViewHolder holder, RelativeLayout image) {
                full_position = holder.getAdapterPosition();
                image.setVisibility(View.GONE);
                if (playerVideo != null) {
                    if (playerVideo.isPlaying() || playerVideo.getVideoStatus() == IjkVideoView.STATE_PAUSED)
                        doPlayer(playerVideo);
                    if (playerVideo.isPlaying()) {
                        playerVideo.stopPlayVideo();
                        playerVideo.release();
                    }
                }
                doPlayer(playerVoice);
                playerVoice.play(goodsStory.get(full_position).getContent());
                // 时长和标题
                playerVoice.setTitleAndTime(goodsStory.get(full_position).getTitle(), goodsStory.get(full_position).getTime());
                lastPostion = full_position;
                if (scrollToPositionListener != null) {
                    scrollToPositionListener.scrollToPosition(full_position);
                }
            }
        });

        mGoodsInfoAdapter.setVideoClickListener(new NewGoodsInfoAdapter.VideoClickListener() {
            @Override
            public void onClick(String videoUrl) {
                Bundle bundle = new Bundle();
                bundle.putString("path", videoUrl);
                OneFragmentActivity.startNewActivity(getContext(), "", VideoViewFragment.class, bundle);
            }
        });

    }

    private void initData() {
        if (flag == 1) {// 纪录片
            fl_h5.setVisibility(View.GONE);
            storyInfo();// 简介
        } else {// 故事
            int type = mGoodsDetailBean.getGoodsBasicInfoBean().getType();
            if (type == 3) {// 富文本数据
//                setBaseData();// TODO 这个类型没有说清是否要上面的部分，暂且隐去
                wv_h5.loadDataWithBaseURL(null, mGoodsDetailBean.getGoodsBasicInfoBean().getIntroduceGoods(), "text/html", "utf-8", null);
            } else {
                descriptionUrl = mGoodsDetailBean.getGoodsBasicInfoBean().getDescriptionUrl();
                isH5 = TextUtils.isEmpty(descriptionUrl) || descriptionUrl.length() < 10 ? false : true;
                if (isH5) {
//                    noH5.hide();
                    wv_h5.getSettings().setJavaScriptEnabled(true);
                    wv_h5.getSettings().setPluginState(WebSettings.PluginState.ON);
                    wv_h5.setWebViewClient(new WebViewClient() {
                        boolean isLoadUrl = false;

                        @Override
                        public void onReceivedError(WebView view, int errorCode,
                                                    String description, String failingUrl) {
                            view.getSettings().setDefaultTextEncodingName("UTF-8");
                            super.onReceivedError(view, errorCode, description, failingUrl);
                            String errorHtml = "<div style='padding-top:200px;text-align:center;color:#666;'>未打开无线网络</div>";
                            view.loadDataWithBaseURL("", errorHtml, "text/html", "UTF-8", "");
                        }

                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            if (!isLoadUrl) {
                                isLoadUrl = true;
                                view.loadUrl(url);

                            }
                            return super.shouldOverrideUrlLoading(view, url);
                        }

                        @Override
                        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                            return super.shouldOverrideKeyEvent(view, event);
                        }

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                            if (!isLoadUrl) {
                                isLoadUrl = true;
                                view.loadUrl(url);

                            }
                            super.onPageStarted(view, url, favicon);
                        }
                    });
                    wv_h5.setWebChromeClient(new WebChromeClient() {
                        private CustomViewCallback myCallback = null;

                        @Override
                        public void onShowCustomView(View view, CustomViewCallback callback) {
                            //                        super.onShowCustomView(view, callback);
                            // 设置webView原父容器的的高度保持不变
                            fl_h5.getLayoutParams().height = fl_h5.getMeasuredHeight();
                            if (myCallback != null) {
                                myCallback.onCustomViewHidden();
                                myCallback = null;
                                Log.e("Media", "myCallback.onCustomViewHidden()...");
                                return;
                            }
                            activity.setZoomListener(new GoodsInfoActivity.WindowUtilsListener() {
                                @Override
                                public void onClose(View view) {
                                    if (myCallback != null)
                                        myCallback.onCustomViewHidden();
                                }
                            });
                            activity.addVideoView(view);
                            /*WebVideoPopWindow.getInstance().setZoomListener(new WebVideoPopWindow.WindowUtilsListener() {
                                @Override
                                public void onClose(View view) {
                                    if (myCallback != null)
                                        myCallback.onCustomViewHidden();
                                }
                            });
                            WebVideoPopWindow.getInstance().showPopupWindow();
                            WebVideoPopWindow.getInstance().addVideoView(view);*/

                            myView = view;
                            myCallback = callback;
                        }

                        @Override
                        public void onReceivedTitle(WebView view, String title) {
                            super.onReceivedTitle(view, title);
                        }

                        @Override
                        public void onHideCustomView() {
                            //                        super.onHideCustomView();
                            if (myView != null) {

                                if (myCallback != null) {
                                    myCallback.onCustomViewHidden();
                                    myCallback = null;
                                }

                                ViewGroup parent = (ViewGroup) myView.getParent();
                                if (parent != null)
                                    parent.removeView(myView);
                                myView = null;
                                fl_h5.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

                                /*if (WebVideoPopWindow.getInstance().isShow()) {
                                    WebVideoPopWindow.getInstance().hidePopupWindow();
                                }*/
                                activity.removeVideoView();
                            }
                        }
                    });
                    wv_h5.setFocusable(false);
                    wv_h5.loadUrl(descriptionUrl + "?app=app");
                    wv_h5.addJavascriptInterface(new StoryInfoH5(mContext), "notice");

                } else {
                    fl_h5.setVisibility(View.GONE);
//                    setBaseData();
                    // 故事内容
                    storyInfo();
                }
            }
        }
    }

    /**
     * 故事：不是h5的情况下，故事详情
     * 纪录片：纪录片的简介
     */
    private void storyInfo() {
        goodsStory.clear();
        goodsStory.addAll(mGoodsDetailBean.getGoodsOtherInfoBean().getTravelPlans());
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < goodsStory.size(); i++) {// 获取时间
                    int type = goodsStory.get(i).getType();
                    if (type == 3) {
                        if (TextUtils.isEmpty(goodsStory.get(i).getTime()))
                            try {
                                MediaPlayer mp = new MediaPlayer();
                                mp.setDataSource(goodsStory.get(i).getContent());
                                mp.prepare();
                                goodsStory.get(i).setTime(ShopTool.generateTime(mp.getDuration()));
                                mp.release();
                            } catch (IOException e) {
                                goodsStory.get(i).setTime("00:00");
                                e.printStackTrace();
                            }
                        else
                            goodsStory.get(i).setTime(ShopTool.secondToTime(goodsStory.get(i).getTime()));
                    }
                }
            }
        }).start();
        mGoodsInfoAdapter.notifyDataSetChanged();
    }

    private void init() {
        mContext = getContext();
        wv_h5 = (WebView) view.findViewById(R.id.wv_h5);
        fl_h5 = (FrameLayout) view.findViewById(R.id.fl_h5);

        goodsStory = new ArrayList<>();
        mGoodsInfoAdapter = new NewGoodsInfoAdapter(mContext, goodsStory, flag + "");
        ImageDisplayTools.initImageLoader(mContext);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mCommandGoodsAdapter = new CommandGoodsAdapter(commandGoods, mContext);

        playerVideo = SuperPlayerManage.getSuperManage(getContext()).initializeVideoCommit();
        playerVideo.setShowTopControl(true).setSupportGesture(false);
        playerVoice = SuperPlayerManage.getSuperManage(getContext()).initializeVoiceCommit();
    }

    private void initSwipeRefreshView() {
        mrv_story_video = (MyRecyclerView) view.findViewById(R.id.mrv_story_video);
        manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mrv_story_video.setLayoutManager(manager);
        joiner = new AdapterJoiner();
//        if (flag != 1) {
//            viewLocal = View.inflate(mContext, R.layout.activity_goodsinfo, null);
//            iv_goodsinfo_img = (ImageView) viewLocal.findViewById(R.id.iv_goodsinfo_img);
//            tv_goodsinfo_title = (TextView) viewLocal.findViewById(R.id.tv_goodsinfo_title);
//            tv_goodsinfo_subhead = (TextView) viewLocal.findViewById(R.id.tv_goodsinfo_subhead);
//            noH5 = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
//                @Override
//                public View onNeedLayout(Context context) {
//                    return viewLocal;
//                }
//            });
//            joiner.add(noH5);
//        }
        joiner.add(new JoinableAdapter(mGoodsInfoAdapter, 7));
        if (flag == 1) {
            joiner.add(new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {

                @Override
                public View onNeedLayout(Context context) {
                    return View.inflate(context, R.layout.layout_story_video_line, null);
                }
            }));
            joiner.add(new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {

                @Override
                public View onNeedLayout(Context context) {
                    View view = View.inflate(mContext, R.layout.layout_recyclerview, null);
                    RecyclerView rv_layout = (RecyclerView) view.findViewById(R.id.rv_layout);
                    rv_layout.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                    rv_layout.setAdapter(mCommandGoodsAdapter);
                    return rv_layout;
                }
            }));
        }
        mrv_story_video.setAdapter(joiner.getAdapter());
        initData();
    }

    private void doPlayer(SuperPlayer player) {
        if (player.isPlaying() && lastPostion == full_position) {
            return;
        }

        if (player.getVideoStatus() == IjkVideoView.STATE_PAUSED) {
            if (full_position != lastPostion) {
                player.stopPlayVideo();
                player.release();
            }
        }
        if (lastPostion != -1) {
            player.showView(R.id.adapter_player_control);
        }

        View view = mrv_story_video.getChildAt(full_position);
        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.adapter_super_video);
        frameLayout.removeAllViews();
        player.showView(R.id.adapter_player_control);
        frameLayout.addView(player);
    }

    @Override
    public void onPause() {
        super.onPause();
        isPause = true;
        requestAudioFocus();
        if (playerVideo != null)
            playerVideo.onPause();
        if (playerVoice != null)
            playerVoice.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        isFirst = false;
        beginTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
        Log.e(TAG, "onResume: " + beginTime);
        isPause = false;
        if (wv_h5 != null && !isFirst) {
            wv_h5.onResume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (playerVideo != null && (playerVideo.isPlaying() || playerVideo.getVideoStatus() == IjkVideoView.STATE_PAUSED))
            Log.e(TAG, "视频结束");
        if (wv_h5 != null) {
            wv_h5.clearCache(true);
            wv_h5.destroy();
        }
        if (playerVideo != null)
            playerVideo.onDestroy();
        if (playerVoice != null)
            playerVoice.onDestroy();
        mAudioManager.abandonAudioFocus(audioFocusChangeListener);
    }

    private boolean isPause;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {

            if (isPause && focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                requestAudioFocus();
            }
        }
    };

    private void requestAudioFocus() {
        int result = mAudioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.e("aaa", "audio focus been granted");
        }
    }


    private OnBackListener mOnBackListener;

    public interface OnBackListener {
        void setSelectedFragment(StoryOrVideoFragment selectedFragment);

        void setItSelf(boolean isClick);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBackListener) {
            mOnBackListener = (OnBackListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBackListener");
        }
        if (context instanceof ScrollToPositionListener) {
            scrollToPositionListener = (ScrollToPositionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //告诉FragmentActivity，当前Fragment在栈顶
        mOnBackListener.setSelectedFragment(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnBackListener = null;
        scrollToPositionListener = null;
    }

    public boolean onBackPressed() {
        if (myView != null) {
            WebVideoPopWindow.getInstance().hidePopupWindow();
            return true;
        } else if (playerVideo != null && playerVideo.onBackPressed()) {
            return true;
        } else if (playerVoice != null && playerVoice.onBackPressed()) {
            return true;
        } else
            return false;

    }

    @Override
    public void onStop() {
        super.onStop();
        if (isVisibleToUser) {
            endTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
            UDPSendInfoBean bean = new UDPSendInfoBean();
            bean.getData("003_1_" + activity.storyId, mGoodsDetailBean.getGoodsBasicInfoBean().getGoodsTitle() + "_小城故事",
                    ShopConstant.STORY_INFO + "id=" + activity.storyId, beginTime, endTime);
            activity.sendData(bean);
            Log.e(TAG, "onStop: " + endTime);
        }
    }

    /**
     * 图文和富文本的情况需要赋值的字段
     */
    private void setBaseData() {
        // 故事封面
        ImageDisplayTools.displayImage(mGoodsDetailBean.getGoodsBasicInfoBean().getGoodsImg(), iv_goodsinfo_img);
        ShopTool.setLLParamsWidth(iv_goodsinfo_img, 12, 7, 0);
        if (!OSUtil.isDayTheme())
            iv_goodsinfo_img.setColorFilter(TravelUtil.getColorFilter(mContext));
        // 标题
        tv_goodsinfo_title.setText(mGoodsDetailBean.getGoodsBasicInfoBean().getGoodsTitle());
        // 副标题
        tv_goodsinfo_subhead.setText(mGoodsDetailBean.getGoodsBasicInfoBean().getSubhead());
    }

    // 为了使点击视频后，显示的位置正确而添加的
    private ScrollToPositionListener scrollToPositionListener;
    public interface ScrollToPositionListener {
        void scrollToPosition(int position);
    }

}
