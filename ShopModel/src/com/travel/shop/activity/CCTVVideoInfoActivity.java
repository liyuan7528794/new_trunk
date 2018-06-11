package com.travel.shop.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.AdapterJoiner.AdapterJoiner;
import com.travel.AdapterJoiner.JoinableAdapter;
import com.travel.AdapterJoiner.JoinableLayout;
import com.travel.ShopConstant;
import com.travel.bean.CCTVVideoInfoBean;
import com.travel.bean.EvaluateInfoBean;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.PopWindowUtils;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;
import com.travel.shop.adapter.GoodsInfoCommentAdapter;
import com.travel.shop.adapter.SmallVideoAdapter;
import com.travel.shop.helper.CCTVVideoHttpHelper;
import com.travel.shop.tools.SuperPlayer;
import com.travel.shop.tools.SuperPlayerManage;

import java.util.ArrayList;

/**
 * 央视全纪录片与细分视频页
 */
public class CCTVVideoInfoActivity extends TitleBarBaseActivity implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, SwipeRefreshAdapterView.OnListLoadListener {

    private Context mContext;
    private CCTVVideoInfoBean mCCTVVideoInfoBean;
    private String videoId;

    // 视频相关
    private View layout_cctv_video;
    private RelativeLayout videoControl;
    private SuperPlayer playerVideo;
    private LinearLayout ll_cctv_container;
    private RelativeLayout cctv_full_screen;
    private boolean isRecover;// 只有从推荐的视频进入是ture
    private SwipeRefreshRecyclerView srrv_video;
    private AdapterJoiner joiner;
    private JoinableLayout videoInfoLayout, listLabel;
    private TextView textView;
    // 视频简介
    private TextView tv_cctv_video_name, cctv_goods_buy, cctv_video_intro;
    private String title, imgUrl;

    // 按钮的操作
    private View layout_like;
    private ImageView video_share, video_like;
    private TextView video_like_count;
    private int likeCount;// 点赞数

    private int flag = 1;// 1:全视频 2:短视频
    // 全视频展示
    private ArrayList<CCTVVideoInfoBean> smallVideos;
    private SmallVideoAdapter smallVideoAdapter;
    // 短视频展示
    private LinearLayout layout_evaluate;
    private EditText send_evaluate;
    private int page = 1;
    private ArrayList<EvaluateInfoBean> evaluates;
    private GoodsInfoCommentAdapter goodsInfoCommentAdapter;
    private int evaluateCount = 0;// 评论条数

    /**
     * 跳转
     *
     * @param mCCTVVideoInfoBean 视频相关内容
     * @param flag               1:全视频 2:短视频
     */
    public static void actionStart(Context mContext, CCTVVideoInfoBean mCCTVVideoInfoBean, int flag) {
        Intent intent = new Intent(mContext, CCTVVideoInfoActivity.class);
        intent.putExtra("videoInfoBean", mCCTVVideoInfoBean);
        intent.putExtra("flag", flag);
        mContext.startActivity(intent);
    }

    // 短视频数据
    private CCTVVideoHttpHelper.CCTVSmallVideoListener smallVideoListener = new CCTVVideoHttpHelper.CCTVSmallVideoListener() {
        @Override
        public void onSuccessGet(ArrayList<CCTVVideoInfoBean> videos, int position) {
            if (videos.size() == 0) {
                textView.setText("暂无相关视频");
            } else {
                textView.setText("纪录片细分");
                smallVideos.addAll(videos);
                smallVideoAdapter.notifyDataSetChanged();
            }
        }
    };
    // 点赞数据
    private CCTVVideoHttpHelper.CCTVVideoLikeListener cctvVideoLikeListener = new CCTVVideoHttpHelper.CCTVVideoLikeListener() {
        @Override
        public void onSuccessGet(boolean isLike) {
            video_like.setSelected(isLike);
            if (video_like.isSelected()) {
                video_like.setImageResource(R.drawable.icon_video_liked);
            } else {
                video_like.setImageResource(R.drawable.icon_video_like);
            }
        }

        @Override
        public void onLikeClick() {
            video_like.setSelected(!video_like.isSelected());
            if (video_like.isSelected()) {
                ++likeCount;
                showToast("点赞成功");
                video_like.setImageResource(R.drawable.icon_video_liked);
            } else {
                --likeCount;
                showToast("您已取消点赞");
                video_like.setImageResource(R.drawable.icon_video_like);
            }
            video_like_count.setText(OSUtil.getLikeCount(likeCount) + "");
        }

        @Override
        public void onSuccessGetLikeCount(int count) {
            likeCount = count;
            video_like_count.setText(OSUtil.getLikeCount(likeCount) + "");
        }
    };
    // 评论数据
    private CCTVVideoHttpHelper.CCTVVideoEvaluateListener cctvVideoEvaluateListener = new CCTVVideoHttpHelper.CCTVVideoEvaluateListener() {
        @Override
        public void onSuccessGet(ArrayList<EvaluateInfoBean> evaluates) {
            layout_evaluate.setVisibility(View.VISIBLE);
            if (evaluates.size() == 0 && page != 1) {
                showToast("没有更多了");
                srrv_video.setEnabledLoad(false);
            } else {
                if (evaluates.size() == 0) {
                    srrv_video.setEnabledLoad(false);
                    textView.setText("暂无评论");
                } else {
                    if (evaluates.size() % 10 != 0) {
                        srrv_video.setEnabledLoad(false);
                    } else {
                        srrv_video.setEnabledLoad(true);
                    }
                    CCTVVideoInfoActivity.this.evaluates.addAll(evaluates);
                    goodsInfoCommentAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onSuccessGetCount(int count) {
            evaluateCount = count;
            if (count != 0)
                textView.setText("评论 " + count);
        }

        @Override
        public void onSuccessSend(String content) {
            send_evaluate.setText("");
            OSUtil.hideKeyboard(CCTVVideoInfoActivity.this);
            srrv_video.getScrollView().smoothScrollToPosition(0);
            EvaluateInfoBean bean = new EvaluateInfoBean();
            bean.setEvaluateUserPhoto(UserSharedPreference.getUserHeading());
            bean.setEvaluateUserName(UserSharedPreference.getNickName());
            bean.setEvaluateTime(DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME));
            bean.setEvaluateContent(content);
            evaluates.add(0, bean);
            goodsInfoCommentAdapter.notifyItemInserted(0);
            if (evaluates.size() == 1) {
                textView.setText("评论 1");
            } else {
                textView.setText("评论 " + ++evaluateCount);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OSUtil.enableStatusBar(this, true);
        setContentView(R.layout.activity_cctv_video_info);
        initView();
        initData();
        initSwipeRefreshRecyclerView();
        initListener();
        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            CCTVVideoHttpHelper.getVideoLikeCount(mContext, videoId, cctvVideoLikeListener);
            if (UserSharedPreference.isLogin())
                CCTVVideoHttpHelper.getVideoLike(mContext, videoId, cctvVideoLikeListener);
            if (flag == 1)
                CCTVVideoHttpHelper.getSmallVideoList(mContext, 0, videoId, smallVideoListener, 1);
            else {
                CCTVVideoHttpHelper.getVideoEvaluateList(mContext, videoId, cctvVideoEvaluateListener, page);
                CCTVVideoHttpHelper.getEvaluateCount(mContext, videoId, cctvVideoEvaluateListener);
            }
        }
    }

    private void initView() {
        layout_cctv_video = findView(R.id.layout_cctv_video);
        videoControl = (RelativeLayout) layout_cctv_video.findViewById(R.id.adapter_player_control);
        videoControl.setVisibility(View.GONE);
        ll_cctv_container = findView(R.id.ll_cctv_container);
        cctv_full_screen = findView(R.id.cctv_full_screen);
        srrv_video = findView(R.id.srrv_video);

        layout_evaluate = findView(R.id.layout_evaluate);
        send_evaluate = findView(R.id.send_evaluate);

    }

    private void initData() {
        mContext = this;
        hideOriginTitleLayout();
        flag = getIntent().getIntExtra("flag", 1);
        mCCTVVideoInfoBean = (CCTVVideoInfoBean) getIntent().getSerializableExtra("videoInfoBean");
        videoId = mCCTVVideoInfoBean.getId();
        title = mCCTVVideoInfoBean.getTitle();
        imgUrl = mCCTVVideoInfoBean.getImgUrl();

        playerVideo = SuperPlayerManage.getSuperManage(mContext).initializeVideo(true);
        playerVideo.setShowTopControl(false).setSupportGesture(false);
        FrameLayout frameLayout = (FrameLayout) layout_cctv_video.findViewById(R.id.adapter_super_video);
        frameLayout.removeAllViews();
        playerVideo.showView(R.id.adapter_player_control);
        frameLayout.addView(playerVideo);
        playerVideo.play(mCCTVVideoInfoBean.getVideoUrl());
        playerVideo.setFullScreenBackgroud(imgUrl);

        // 全视频展示
        smallVideos = new ArrayList<>();
        smallVideoAdapter = new SmallVideoAdapter(smallVideos, mContext);
        // 短视频展示
        evaluates = new ArrayList<>();
        goodsInfoCommentAdapter = new GoodsInfoCommentAdapter(evaluates, mContext);
    }

    private void initSwipeRefreshRecyclerView() {
        videoInfoLayout = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                View view = View.inflate(context, R.layout.layout_cctv_video_info, null);
                tv_cctv_video_name = (TextView) view.findViewById(R.id.tv_cctv_video_name);
                cctv_goods_buy = (TextView) view.findViewById(R.id.cctv_goods_buy);
                cctv_video_intro = (TextView) view.findViewById(R.id.cctv_video_intro);

                layout_like = view.findViewById(R.id.layout_like);
                video_share = (ImageView) view.findViewById(R.id.video_share);
                video_like = (ImageView) view.findViewById(R.id.video_like);
                video_like_count = (TextView) view.findViewById(R.id.video_like_count);

                // 标题
                tv_cctv_video_name.setText(title);
                // 简介
                cctv_video_intro.setText(mCCTVVideoInfoBean.getContent());

                cctv_goods_buy.setOnClickListener(CCTVVideoInfoActivity.this);
                video_share.setOnClickListener(CCTVVideoInfoActivity.this);
                layout_like.setOnClickListener(CCTVVideoInfoActivity.this);
                return view;
            }
        });

        listLabel = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                textView = new TextView(context);
                textView.setTextSize(16);
                textView.setTextColor(ContextCompat.getColor(context, R.color.black_6C6F73));
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = OSUtil.dp2px(context, 30);
                layoutParams.leftMargin = OSUtil.dp2px(context, 15);
                layoutParams.rightMargin = OSUtil.dp2px(context, 38);
                textView.setLayoutParams(layoutParams);
                return textView;
            }
        });
        srrv_video.setLayoutManager(new LinearLayoutManager(mContext));
        joiner = new AdapterJoiner();
        joiner.add(videoInfoLayout);
        joiner.add(listLabel);
        joiner.add(new JoinableAdapter(flag == 1 ? smallVideoAdapter : goodsInfoCommentAdapter));
        srrv_video.setAdapter(joiner.getAdapter());
        srrv_video.setOnRefreshListener(this);
        srrv_video.setOnListLoadListener(this);
        if (flag == 1) {
            srrv_video.getSwipeRefreshLayout().setEnabled(false);
            srrv_video.setEnabledLoad(false);
        }
    }

    private void initListener() {
        smallVideoAdapter.setmOnItemClickListener(new SmallVideoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                isRecover = true;
                CCTVVideoInfoActivity.actionStart(mContext, smallVideos.get(position), 2);
            }
        });

        send_evaluate.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String content = send_evaluate.getText().toString();
                    if (TextUtils.isEmpty(content)) {
                        showToast("评论内容不可为空");
                    } else {
                        CCTVVideoHttpHelper.onSendEvaluate(mContext, videoId, content, cctvVideoEvaluateListener);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (playerVideo != null) {
            playerVideo.onConfigurationChanged(newConfig);
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {// 小屏
                OSUtil.enableStatusBar(this, true);
                cctv_full_screen.setVisibility(View.GONE);
                cctv_full_screen.removeAllViews();
                ll_cctv_container.setVisibility(View.VISIBLE);
                FrameLayout frameLayout = (FrameLayout) layout_cctv_video.findViewById(R.id.adapter_super_video);
                frameLayout.removeAllViews();
                ViewGroup last = (ViewGroup) playerVideo.getParent();//找到videoitemview的父类，然后remove
                if (last != null) {
                    last.removeAllViews();
                }
                frameLayout.addView(playerVideo);
            } else {//全屏
                ll_cctv_container.setVisibility(View.GONE);
                cctv_full_screen.setVisibility(View.VISIBLE);
                ViewGroup viewGroup = (ViewGroup) playerVideo.getParent();
                if (viewGroup == null)
                    return;
                viewGroup.removeAllViews();
                cctv_full_screen.addView(playerVideo);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRecover) {
            isRecover = false;
            if (playerVideo != null && playerVideo.isPlaying()) {
                playerVideo.pause();
            }
            SuperPlayerManage.videoPlayViewManage = null;
        } else if (playerVideo != null) {
            if (playerVideo != null && playerVideo.isPlaying()) {
                playerVideo.onPause();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (UserSharedPreference.isLogin())
            CCTVVideoHttpHelper.getVideoLike(mContext, videoId, cctvVideoLikeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playerVideo != null) {
            playerVideo.onDestroy();
        }
        SuperPlayerManage.videoPlayViewManage = null;
    }

    @Override
    public void onBackPressed() {
        if (playerVideo != null && playerVideo.onBackPressed()) {
            return;
        } else
            super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if (v == cctv_goods_buy) {// 购买
            startActivity(new Intent(mContext, GoodsActivity.class).putExtra("goodsId", mCCTVVideoInfoBean.getGoodsId()));
        } else if (v == video_share) {// 分享
            PopWindowUtils.sharePopUpWindow(mContext, "红了旅行", title, imgUrl, ShopConstant.CCTV_VIDEO_SHARE + videoId);
        } else if (v == layout_like) {// 点赞
            int type = video_like.isSelected() ? 2 : 1;
            CCTVVideoHttpHelper.onLikeClick(mContext, videoId, type, cctvVideoLikeListener);
        }
    }

    @Override
    public void onRefresh() {
        page = 1;
        evaluates.clear();
        CCTVVideoHttpHelper.getVideoEvaluateList(mContext, videoId, cctvVideoEvaluateListener, page);
        srrv_video.setRefreshing(false);
    }

    @Override
    public void onListLoad() {
        ++page;
        CCTVVideoHttpHelper.getVideoEvaluateList(mContext, videoId, cctvVideoEvaluateListener, page);
        srrv_video.setLoading(false);
    }
}
