package com.travel.shop.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
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
import com.travel.bean.EvaluateInfoBean;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.GoodsDetailBean;
import com.travel.bean.GoodsServiceBean;
import com.travel.bean.UDPSendInfoBean;
import com.travel.http_helper.StoryListHttpHelper;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.PopWindowUtils;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;
import com.travel.shop.adapter.GoodsInfoCommentAdapter;
import com.travel.shop.bean.AttachGoodsBean;
import com.travel.shop.bean.CalendarBean;
import com.travel.shop.fragment.GoodsInfoFragment;
import com.travel.shop.fragment.StoryOrVideoFragment;
import com.travel.shop.http.CommitHttp;
import com.travel.shop.http.GoodsInfoHttp;
import com.travel.shop.tools.ShopTool;
import com.travel.shop.tools.SuperPlayer;
import com.travel.shop.tools.SuperPlayerManage;
import com.travel.shop.widget.MyViewPager;
import com.travel.shop.widget.SendDialog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 商品详情页
 *
 * @author WYP
 * @version 1.0
 * @created 2017/06/14
 */
public class GoodsInfoActivity extends TitleBarBaseActivity implements OnClickListener, /*SwipeRefreshLayout.OnRefreshListener,*/
        SwipeRefreshAdapterView.OnListLoadListener, ViewPager.OnPageChangeListener, StoryOrVideoFragment.OnBackListener, StoryOrVideoFragment.ScrollToPositionListener {

    private static final String TAG = "GoodsInfoActivity";
    private static final String SMALL_CITY = "小城故事";
    private static final String BIG_CITY = "大城小事";
    private static final String COMMAND_CITY = "推荐城市";
    private Context mContext;
    // 选项卡以及下划线
    private HorizontalScrollView hs_tabs_control;
    private LinearLayout ll_content;
    private TextView tv_content, tv_product;
    private LinearLayout ll_control_line;
    private ArrayList<View> tvs = new ArrayList<>();
    private int tabWidth;// 下划线宽度
    private LinearLayout.LayoutParams lpLine;
    private int flag;// 1:纪录片 其他:故事
    private String content;// 纪录片同款

    private MyViewPager vp_goods;
    private ManageGoodsAdapter mManageGoodsAdapter;
    private ArrayList<Fragment> viewList;
    // 评论相关
    private SwipeRefreshRecyclerView srrv_goods_info;
    private AdapterJoiner joiner;
    private LinearLayoutManager manager;
    private JoinableLayout no_comment, comment_title;
    private GoodsInfoCommentAdapter mGoodsInfoCommentAdapter;
    private ArrayList<EvaluateInfoBean> comments;
    private int mPage = 1;
    private int evaluatePosition;
    private boolean isComment;// 是否有评论

    // 动画效果相关
    // 标识导航栏当前是否显示
    private boolean isShowing = true;
    // 标识动画是否结束
    private boolean isEnd = true;
    private LinearLayout ll_title_layout;
    private LinearLayout ll_goods_container;

    // 底部按钮相关
    private View rl_story_info;
    private ImageView iv_story_share, iv_story_collect, iv_story_comment;
    private TextView tv_story_try;
    private boolean isCollected;
    private boolean isFirst;// 是否是首次进入此页 用于判断：重新登录后，获取是否收藏该故事

    // 判断网络
    public static boolean isNet;
    public String storyId, goodsId;

    // 可滑动部分的数据
    private GoodsDetailBean goodsDetailBean;
    private ArrayList<GoodsBasicInfoBean> commandGoods;
    private boolean isH5;
    private String descriptionUrl;
    private StoryOrVideoFragment mStoryOrVideoFragment;

    // 固定视频相关
    private View videoView;
    private RelativeLayout rlayPlayerControl;
    private RelativeLayout rlayPlayer, video, voice;
    private ImageView adapter_super_video_iv_cover;
    private SuperPlayer playerVideo;
    private GoodsServiceBean goodsService;
    private boolean isRecover;// 只有从推荐的视频进入是ture
    // 大小屏转换相关
    private RelativeLayout full_screen;
    private boolean isPortrait = true;// 是否是竖屏

    private String videoBeginTime, videoEndTime;

    // web页的全屏视频
    private FrameLayout video_frameLayout;
    private RelativeLayout videoContain = null;
    private View webVideo = null;

    // 仿抖音版的标签名称相关
    private int type = 1;// 1:小城故事 2:推荐城市  3:大城小事

    // 故事未发布的情况
    private LinearLayout ll_data_get;
    private TextView none_story;

    public static void actionStart(Context context, String storyId, int type) {
        Intent intent = new Intent(context, GoodsInfoActivity.class);
        intent.putExtra("storyId", storyId);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_goods_info);
        isFirst = true;
        init();
        initSwipeRefreshView();
        initListener();
        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection()))
            GoodsInfoHttp.getStoryInfo(mContext, storyId, mGoodsInfoListener);
    }

    /**
     * 对控件初始化
     */
    private void init() {
        video_frameLayout = findView(R.id.video_frameLayout);
        videoContain = findView(R.id.video_contain);
        mContext = this;
        hs_tabs_control = findView(R.id.hs_tabs_control);
        ll_content = findView(R.id.ll_content);
        tv_content = findView(R.id.tv_content);
        tv_product = findView(R.id.tv_product);
        ll_control_line = findView(R.id.ll_control_line);
        srrv_goods_info = findView(R.id.srrv_goods_info);
        ll_title_layout = findView(R.id.ll_title_layout);
        ll_goods_container = findView(R.id.ll_goods_container);
        rl_story_info = findView(R.id.rl_story_info);
        iv_story_share = findView(R.id.iv_story_share);
        iv_story_share.setImageResource(OSUtil.isDayTheme() ? com.travel.lib.R.drawable.icon_goods_share_day : com.travel.lib.R.drawable.icon_story_share_night);
        iv_story_comment = findView(R.id.iv_story_comment);
        tv_story_try = findView(R.id.tv_story_try);
        iv_story_collect = findView(R.id.iv_story_collect);
        iv_story_collect.setBackgroundResource(ShopTool.setSelectableDrawableResource(R.id.iv_story_collect, false));
        try {
            Method method = View.class.getMethod("setTranslationZ", float.class);
            method.invoke(ll_title_layout, 10f);
        } catch (Exception e) {
            MLog.e(TAG, e.getMessage(), e);
        }

        storyId = getIntent().getStringExtra("storyId");
        // 视频相关
        full_screen = findView(R.id.full_screen);
        videoView = findView(R.id.layout_video);
        rlayPlayerControl = (RelativeLayout) videoView.findViewById(R.id.adapter_player_control);
        rlayPlayer = (RelativeLayout) videoView.findViewById(R.id.adapter_super_video_layout);
        video = (RelativeLayout) videoView.findViewById(R.id.video);
        voice = (RelativeLayout) videoView.findViewById(R.id.voice);
        adapter_super_video_iv_cover = (ImageView) videoView.findViewById(R.id.adapter_super_video_iv_cover);
        voice.setVisibility(View.GONE);
        if (rlayPlayer != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rlayPlayer.getLayoutParams();
            layoutParams.height = (int) (OSUtil.getScreenWidth() * 0.5652f);//这值是网上抄来的，我设置了这个之后就没有全屏回来拉伸的效果，具体为什么我也不太清楚
            rlayPlayer.setLayoutParams(layoutParams);
        }
        goodsService = new GoodsServiceBean();

        ll_title.setVisibility(View.GONE);
        titleLine.setVisibility(View.GONE);
        tvs.add(tv_content);
        tvs.add(tv_product);
        goodsDetailBean = new GoodsDetailBean();
        commandGoods = new ArrayList<>();
        viewList = new ArrayList<>();
        comments = new ArrayList<>();
        mGoodsInfoCommentAdapter = new GoodsInfoCommentAdapter(comments, this);
        type = getIntent().getIntExtra("type", 1);
        switch (type) {
            case 1:// 小城故事
                tv_content.setText(SMALL_CITY);
                break;
            case 2:// 推荐城市
                tv_content.setText(COMMAND_CITY);
                break;
            default:// 大城小事
                tv_content.setText(BIG_CITY);
        }
        initTabLine();

        ll_data_get = findView(R.id.ll_data_get);
        none_story = findView(R.id.none_story);
    }

    private void initSwipeRefreshView() {
        srrv_goods_info = findView(R.id.srrv_goods_info);
        manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        srrv_goods_info.setLayoutManager(manager);
        srrv_goods_info.setOnListLoadListener(this);
        srrv_goods_info.setEnabled(false);
        if (OSUtil.isDayTheme())
            srrv_goods_info.setLoadViewBackground(ContextCompat.getColor(mContext, android.R.color.white));
        else
            srrv_goods_info.setLoadViewBackground(ContextCompat.getColor(mContext, R.color.black_3));
        //        srrv_goods_info.setOnRefreshListener(this);
        joiner = new AdapterJoiner();
        joiner.add(new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                View view = View.inflate(context, R.layout.layout_goods_viewpager, null);
                vp_goods = (MyViewPager) view.findViewById(R.id.vp_goods);
                mManageGoodsAdapter = new ManageGoodsAdapter(getSupportFragmentManager(), viewList);
                vp_goods.setAdapter(mManageGoodsAdapter);
                vp_goods.setOnPageChangeListener(GoodsInfoActivity.this);
                return view;
            }
        }));
        comment_title = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                return View.inflate(context, R.layout.layout_goodsinfo_comment, null);
            }
        });
        joiner.add(comment_title);
        comment_title.hide();
        joiner.add(new JoinableAdapter(mGoodsInfoCommentAdapter));
        no_comment = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                View v = View.inflate(context, R.layout.text_no_comment, null);
                TextView tv_goodsinfo_no_comment = (TextView) v.findViewById(R.id.tv_goodsinfo_no_comment);
                tv_goodsinfo_no_comment.setText("暂无评论");
                return v;
            }
        });
        joiner.add(no_comment);
        no_comment.hide();
        srrv_goods_info.setAdapter(joiner.getAdapter());

        srrv_goods_info.getScrollView().setOnTouchListener(new View.OnTouchListener() {
            private float lastY = 0;
            private float SCALE_DISTANCE = OSUtil.dp2px(mContext, 5);

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float y = event.getRawY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        if (isNewOnes) {
                            lastY = y;
                            isNewOnes = false;
                        }

                        if (y - lastY > SCALE_DISTANCE) {
                            tabAnimator(true);
                        } else if (y - lastY < -SCALE_DISTANCE) {
                            tabAnimator(false);
                        }
                        lastY = y;
                        break;
                    case MotionEvent.ACTION_DOWN:
                        isNewOnes = false;
                        lastY = y;
                        break;
                    case MotionEvent.ACTION_CANCEL | MotionEvent.ACTION_UP:
                        isNewOnes = true;
                        break;
                }
                return false;
            }
        });
    }

    private void initListener() {
        ll_content.setOnClickListener(this);
        tv_content.setOnClickListener(this);
        tv_product.setOnClickListener(this);
        rlayPlayerControl.setOnClickListener(this);
        mGoodsInfoCommentAdapter.setLikeClick(new GoodsInfoCommentAdapter.OnLikeClickListener() {
            @Override
            public void onLikeClick(RecyclerView.ViewHolder viewHolder) {
                int position = joiner.getAdapterPositionByViewHolder(viewHolder);
                EvaluateInfoBean mEvaluateInfoBean = comments.get(position);
                int likeCount = mEvaluateInfoBean.getLikeCount();
                if (mEvaluateInfoBean.isLike()) {
                    comments.get(position).setLike(false);
                    --likeCount;
                    comments.get(position).setLikeCount(likeCount);
                    mGoodsInfoCommentAdapter.notifyItemChanged(position);
                } else {
                    evaluatePosition = position;
                    GoodsInfoHttp.sendLike(mContext, comments.get(position).getStoryCommentId(), mGoodsInfoListener);
                }
            }
        });
        iv_story_share.setOnClickListener(this);
        iv_story_collect.setOnClickListener(this);
        iv_story_comment.setOnClickListener(this);
        tv_story_try.setOnClickListener(this);
    }

    //    @Override
    //    public void onRefresh() {
    //        mPage = 1;
    //        // 有网
    //        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
    //            comments.clear();
    //            GoodsInfoHttp.getStoryInfo(mContext, storyId, mGoodsInfoListener);
    //        }
    //        srrv_goods_info.setRefreshing(false);
    //    }

    @Override
    public void onListLoad() {
        ++mPage;
        GoodsInfoHttp.getStoryComment(mContext, storyId, mPage, mGoodsInfoListener);
        srrv_goods_info.setLoading(false);
    }

    /**
     * 网络数据获取
     */
    GoodsInfoHttp.GoodsInfoListener mGoodsInfoListener = new GoodsInfoHttp.GoodsInfoListener() {
        @Override
        public void getIsNotCollect(boolean isCollected) {
            GoodsInfoActivity.this.isCollected = isCollected;
            iv_story_collect.setBackgroundResource(ShopTool.setSelectableDrawableResource(R.id.iv_story_collect, isCollected));
        }

        @Override
        public void collectControl() {

            int num = isCollected ? 4 : 3;
            String collect = goodsDetailBean.getGoodsBasicInfoBean().getGoodsTitle() + (isCollected ? "_取消收藏" : "_收藏");
            int status = isCollected ? 2 : 1;
            String time = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
            iv_story_collect.setBackgroundResource(ShopTool.setSelectableDrawableResource(R.id.iv_story_collect, !isCollected));
            isCollected = !isCollected;
            showToast(isCollected ? "已收藏" : "取消收藏");
            UDPSendInfoBean bean = new UDPSendInfoBean();
            bean.getData("003_" + num + "_" + storyId, collect,
                    ShopConstant.STORY_ATTENTION + "storyId=" + storyId + "&userId="
                            + UserSharedPreference.getUserId() + "&status=" + status, time, time);
            sendData(bean);
        }

        @Override
        public void getStoryInfo(GoodsDetailBean mGoodsDetailBean) {
            ll_data_get.setVisibility(View.GONE);
            none_story.setVisibility(View.GONE);
            if (mGoodsDetailBean != null) {
                ll_data_get.setVisibility(View.VISIBLE);
                goodsDetailBean = mGoodsDetailBean;
                goodsId = mGoodsDetailBean.getGoodsBasicInfoBean().getGoodsId();
                descriptionUrl = mGoodsDetailBean.getGoodsBasicInfoBean().getDescriptionUrl();
                isH5 = TextUtils.isEmpty(descriptionUrl) || descriptionUrl.length() < 10 ? false : true;
                content = goodsDetailBean.getGoodsBasicInfoBean().getContent();
                flag = goodsDetailBean.getGoodsBasicInfoBean().getType();
//            if (flag == 1) {
//                tv_content.setVisibility(View.GONE);
//                ll_content.setVisibility(View.VISIBLE);
//            } else {
//                tv_content.setVisibility(View.VISIBLE);
//                ll_content.setVisibility(View.GONE);
//                tv_content.setText("小城故事");
//            }
                playerVideo = SuperPlayerManage.getSuperManage(GoodsInfoActivity.this).initializeVideo(flag == 1 ? true : false);
                playerVideo.setShowTopControl(false).setSupportGesture(false);
                playerVideo.setVideoTimeListener(mVideoTimeListener);
                if (flag == 1)
                    videoView.setVisibility(View.VISIBLE);

                GoodsInfoHttp.getStoryComment(mContext, storyId, mPage, this);
                GoodsInfoHttp.getIsNotCollect(storyId, mContext, this);
                if (flag == 1) {
                    ArrayList<GoodsServiceBean> goodsServices = mGoodsDetailBean.getGoodsOtherInfoBean().getTravelPlans();
                    for (int i = 0; i < goodsServices.size(); i++) {
                        if (goodsServices.get(i).getType() == 4) {
                            goodsService = goodsServices.get(i);
                            ImageDisplayTools.displayImage(goodsService.getBackImage(), adapter_super_video_iv_cover);
                            if (!OSUtil.isDayTheme())
                                adapter_super_video_iv_cover.setColorFilter(TravelUtil.getColorFilter(mContext));
                            goodsServices.remove(i);
                        }
                    }
                    // 简介
                    GoodsServiceBean gb1 = new GoodsServiceBean();
                    gb1.setContent("简介:");
                    gb1.setType(5);
                    goodsServices.add(gb1);
                    // 简介的内容
                    GoodsServiceBean gb2 = new GoodsServiceBean();
                    gb2.setContent(goodsService.getTitle());
                    gb2.setType(1);
                    goodsServices.add(gb2);
                    mGoodsDetailBean.getGoodsOtherInfoBean().setTravelPlans(goodsServices);
                }
                // 获取推荐数据
                StoryListHttpHelper.getStoriesList(mContext, 1, "command", 1, cityNetListener);
            } else {
                none_story.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void getStoryComment(ArrayList<EvaluateInfoBean> comments) {
            // 拉取没有更多数据了
            if (comments.size() == 0 && mPage != 1) {
                --mPage;
                showToast(R.string.no_more);
                srrv_goods_info.setEnabledLoad(false);
            } else {
                GoodsInfoActivity.this.comments.addAll(comments);
                if (GoodsInfoActivity.this.comments.size() == 0) {
                    no_comment.show();
                    isComment = false;
                    srrv_goods_info.setEnabledLoad(false);
                } else {
                    comment_title.show();
                    no_comment.hide();
                    isComment = true;
                    mGoodsInfoCommentAdapter.notifyDataSetChanged();
                    srrv_goods_info.setEnabledLoad(true);
                }
            }
        }

        @Override
        public void sendSuccess(String comment, int commentId) {
            EvaluateInfoBean bean = new EvaluateInfoBean();
            bean.setStoryCommentId(commentId + "");
            bean.setEvaluateUserPhoto(UserSharedPreference.getUserHeading());
            bean.setEvaluateUserName(UserSharedPreference.getNickName());
            bean.setEvaluateTime(DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME));
            bean.setLikeCount(0);
            bean.setLike(false);
            bean.setEvaluateContent(comment);
            comments.add(0, bean);
            mGoodsInfoCommentAdapter.notifyItemInserted(0);
            if (!isComment) {
                comment_title.show();
                no_comment.hide();
            }
        }

        @Override
        public void likeSuccess() {
            int likeCount = comments.get(evaluatePosition).getLikeCount();
            comments.get(evaluatePosition).setLike(true);
            ++likeCount;
            comments.get(evaluatePosition).setLikeCount(likeCount);
            mGoodsInfoCommentAdapter.notifyItemChanged(evaluatePosition);
        }

    };
    private StoryListHttpHelper.OutStoriesHttpListener cityNetListener = new StoryListHttpHelper.OutStoriesHttpListener() {
        @Override
        public void getStoriesList(ArrayList<GoodsBasicInfoBean> storyList, int flag) {
            for (int i = 0; i < storyList.size(); i++) {
                if (TextUtils.equals(storyId, storyList.get(i).getStoryId())) {
                    storyList.remove(i);
                    break;
                }
            }
            commandGoods.addAll(storyList);
            String gId = goodsId;
            if (TextUtils.isEmpty(gId) || TextUtils.equals("null", gId)) {
                setFragmentData(new ArrayList<GoodsServiceBean>());
                return;
            }
            if (gId.contains(",")) {// 如果是多个出发地，则默认用第一个出发地
                gId = gId.split(",")[0];
            }
            CommitHttp.getGoodsData(mContext, gId, mListener);
        }
    };

    CommitHttp.CommitOrderListener mListener = new CommitHttp.CommitOrderListener() {

        @Override
        public void getAttachGoods(ArrayList<AttachGoodsBean> goodsList) {
        }

        @Override
        public void getGoodsData(GoodsDetailBean mGoodsDetailBean) {// 获取到同款产品中的数据
            setFragmentData(mGoodsDetailBean.getGoodsOtherInfoBean().getTravelPlans());
        }

        @Override
        public void onErrorNotZero() {
            setFragmentData(new ArrayList<GoodsServiceBean>());
        }

        @Override
        public void getPackageData(ArrayList<HashMap<String, Object>> packages) {
        }

        @Override
        public void getCalendarData(ArrayList<CalendarBean> orderData) {
        }
    };

    private void setFragmentData(ArrayList<GoodsServiceBean> list) {
        viewList.clear();
        mStoryOrVideoFragment = StoryOrVideoFragment.newInstance(flag, goodsDetailBean, commandGoods, vp_goods, 0);
        viewList.add(mStoryOrVideoFragment);
        viewList.add(GoodsInfoFragment.newInstance(list, vp_goods, 1, content, goodsDetailBean.getGoodsBasicInfoBean().getGoodsTitle()));
        mManageGoodsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        hs_tabs_control.setScrollX((int) ((positionOffset + position) * tabWidth * 0.8));
        lpLine = (LinearLayout.LayoutParams) ll_control_line.getLayoutParams();
        lpLine.leftMargin = (int) ((positionOffset + position) * tabWidth);
        lpLine.width = tabWidth;

        // 将对下划线的布局调整应用到下划线本身
        ll_control_line.setLayoutParams(lpLine);

    }

    @Override
    public void onPageSelected(int position) {
        if (flag == 1) {
            if (position == 1) {
                if (OSUtil.isDayTheme())
                    tv_product.setTextColor(ContextCompat.getColor(mContext, R.color.black_3));
                else
                    tv_product.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
            } else
                tv_product.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
        } else {
            // 选项卡字的颜色
            for (View tview : tvs) {
                ((TextView) tview).setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
            }
            // 滑动完成的颜色
            if (OSUtil.isDayTheme())
                ((TextView) tvs.get(position)).setTextColor(ContextCompat.getColor(mContext, R.color.black_3));
            else
                ((TextView) tvs.get(position)).setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
        }
        if (position == 0 && flag == 1)
            videoView.setVisibility(View.VISIBLE);
        else
            videoView.setVisibility(View.GONE);
        vp_goods.resetHeight(position);

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void scrollToPosition(int position) {
        srrv_goods_info.getScrollView().scrollToPosition(position);
    }

    /**
     * 商品详情选项卡的适配器
     *
     * @author WYP
     */
    class ManageGoodsAdapter extends FragmentStatePagerAdapter {

        ArrayList<Fragment> viewList;

        public ManageGoodsAdapter(FragmentManager fm, ArrayList<Fragment> viewList) {
            super(fm);
            this.viewList = viewList;
        }

        @Override
        public Fragment getItem(int arg0) {
            return viewList.get(arg0);
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup arg0, int arg1) {
            return super.instantiateItem(arg0, arg1);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }
    }

    /**
     * 选项卡的点击事件
     */
    @Override
    public void onClick(final View v) {
        // 禁止双击
        v.setEnabled(false);
        v.postDelayed(new Runnable() {

            @Override
            public void run() {
                v.setEnabled(true);
            }
        }, 500);
        if (v == tv_content && flag != 1) {
            if (OSUtil.isDayTheme())
                tv_content.setTextColor(ContextCompat.getColor(mContext, R.color.black_3));
            else
                tv_content.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
            tv_product.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
            vp_goods.setCurrentItem(0, false);
        } else if (v == ll_content && flag == 1) {
            videoView.setVisibility(View.VISIBLE);
            tv_product.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
            vp_goods.setCurrentItem(0, false);
        } else if (v == tv_product) {
            videoView.setVisibility(View.GONE);
            vp_goods.resetHeight(1);
            tv_content.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
            if (OSUtil.isDayTheme())
                tv_product.setTextColor(ContextCompat.getColor(mContext, R.color.black_3));
            else
                tv_product.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
            vp_goods.setCurrentItem(1, false);
        } else if (v == iv_story_share) {// 分享
            if (!isH5)
                PopWindowUtils.sharePopUpWindow(mContext, "红了旅行", goodsDetailBean.getGoodsBasicInfoBean().getGoodsTitle(),
                        goodsDetailBean.getGoodsBasicInfoBean().getGoodsImg(), ShopConstant.STORY_SHARE + storyId);
            else
                PopWindowUtils.sharePopUpWindow(mContext, "红了旅行", goodsDetailBean.getGoodsBasicInfoBean().getGoodsTitle(),
                        goodsDetailBean.getGoodsBasicInfoBean().getGoodsImg(), descriptionUrl + "?web=web&storyId=" + storyId);
        } else if (v == rlayPlayerControl) {// 播放视频
            videoBeginTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
            rlayPlayerControl.setVisibility(View.GONE);
            FrameLayout frameLayout = (FrameLayout) videoView.findViewById(R.id.adapter_super_video);
            frameLayout.removeAllViews();
            playerVideo.showView(R.id.adapter_player_control);
            frameLayout.addView(playerVideo);
            playerVideo.play(goodsService.getContent());
            playerVideo.setTitle(goodsService.getTitle());
            playerVideo.setFullScreenBackgroud(goodsService.getBackImage());
        } else if (UserSharedPreference.isLogin()) {// 登录后
            if (v == iv_story_collect) {// 收藏
                GoodsInfoHttp.collectControl(mContext, storyId, isCollected, mGoodsInfoListener);
            } else if (v == iv_story_comment) {// 评论
                new SendDialog(mContext, storyId, mGoodsInfoListener).show();
            } else if (v == tv_story_try) {// 我要体验
                if (TextUtils.isEmpty(goodsId) || TextUtils.equals("null", goodsId)) {
                    showToast("暂无相关产品");
                    return;
                }
                startActivity(new Intent(mContext, GoodsActivity.class).putExtra("goodsId", goodsId));
            }
        } else
            startActivity(new Intent(ShopConstant.LOG_IN_ACTION));

    }

    /**
     * 显示网络断开
     */
    protected void netNotifyShow() {
        super.netNotifyShow();
        isNet = false;
    }

    /**
     * 隐藏网络断开
     */
    protected void netNotifyHide() {
        super.netNotifyHide();
        isNet = true;
    }

    private boolean isNewOnes = true;

    private void tabAnimator(final boolean isShow) {
        if ((isShowing && isShow) || (!isShowing && !isShow))
            return;

        //        isShowing = isShow;
        if (!isEnd) {
            /*new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tabAnimator(isShow);
                }
            }, 300);*/
            return;
        }
        isEnd = false;
        ValueAnimator tabAnimator;
        if (isShow) {
            tabAnimator = ValueAnimator.ofFloat(-(ll_title_layout.getHeight() + OSUtil.dp2px(this, 25)), 0);
        } else {
            tabAnimator = ValueAnimator.ofFloat(0, -(ll_title_layout.getHeight() + OSUtil.dp2px(this, 25)));
        }
        tabAnimator.setDuration(200).start();
        tabAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ll_goods_container.setPadding(0, Math.round((Float) animation.getAnimatedValue()), 0, Math.round((Float) animation.getAnimatedValue() * 20 / 23));
            }

        });
        tabAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isShowing = isShow;
                if (isShow)
                    ll_goods_container.setPadding(0, 0, 0, 0);
                else
                    ll_goods_container.setPadding(0,
                            -(ll_title_layout.getHeight() + OSUtil.dp2px(mContext, 25)),
                            0, -rl_story_info.getHeight());
                isEnd = true;
                isNewOnes = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * 下划线的编辑
     */
    private void initTabLine() {
        tabWidth = (OSUtil.getScreenWidth() / 18) * 5;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ll_control_line.getLayoutParams();
        lp.width = tabWidth;
        ll_control_line.setLayoutParams(lp);
        LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) ll_content.getLayoutParams();
        lp2.width = tabWidth;
        ll_content.setLayoutParams(lp2);
        tv_content.setWidth(tabWidth);
        tv_product.setWidth(tabWidth);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (playerVideo != null && flag == 1) {
            playerVideo.onConfigurationChanged(newConfig);
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {// 小屏
                isPortrait = true;
                full_screen.setVisibility(View.GONE);
                full_screen.removeAllViews();
                ll_goods_container.setVisibility(View.VISIBLE);
                rlayPlayerControl.setVisibility(View.GONE);
                FrameLayout frameLayout = (FrameLayout) videoView.findViewById(R.id.adapter_super_video);
                frameLayout.removeAllViews();
                ViewGroup last = (ViewGroup) playerVideo.getParent();//找到videoitemview的父类，然后remove
                if (last != null) {
                    last.removeAllViews();
                }
                frameLayout.addView(playerVideo);
            } else {//全屏
                isPortrait = false;
                ll_goods_container.setVisibility(View.GONE);
                ViewGroup viewGroup = (ViewGroup) playerVideo.getParent();
                if (viewGroup == null)
                    return;
                viewGroup.removeAllViews();
                full_screen.addView(playerVideo);
                full_screen.setVisibility(View.VISIBLE);
            }
        } else if (mStoryOrVideoFragment.getPlayerVideo() != null && flag != 1 && !isH5) {
            mStoryOrVideoFragment.getPlayerVideo().onConfigurationChanged(newConfig);
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {// 小屏
                full_screen.setVisibility(View.GONE);
                full_screen.removeAllViews();
                ll_goods_container.setVisibility(View.VISIBLE);
                rlayPlayerControl.setVisibility(View.GONE);
                if (mStoryOrVideoFragment.getPosition() <= mStoryOrVideoFragment.getManager().findLastVisibleItemPosition()
                        && mStoryOrVideoFragment.getPosition() >= mStoryOrVideoFragment.getManager().findFirstVisibleItemPosition()) {
                    View view = mStoryOrVideoFragment.getManager().findViewByPosition(mStoryOrVideoFragment.getPosition());
                    view.findViewById(R.id.adapter_player_control).setVisibility(View.GONE);
                    FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.adapter_super_video);
                    frameLayout.removeAllViews();
                    ViewGroup last = (ViewGroup) mStoryOrVideoFragment.getPlayerVideo().getParent();//找到videoitemview的父类，然后remove
                    if (last != null) {
                        last.removeAllViews();
                    }
                    frameLayout.addView(mStoryOrVideoFragment.getPlayerVideo());
                    srrv_goods_info.getScrollView()
                            .scrollToPosition(mStoryOrVideoFragment.getPosition());
                }
            } else {//全屏
                ll_goods_container.setVisibility(View.GONE);
                ViewGroup viewGroup = (ViewGroup) mStoryOrVideoFragment.getPlayerVideo().getParent();
                if (viewGroup == null)
                    return;
                viewGroup.removeAllViews();
                full_screen.addView(mStoryOrVideoFragment.getPlayerVideo());
                full_screen.setVisibility(View.VISIBLE);
            }
        } else {
            full_screen.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRecover) {
            isRecover = false;
            if (playerVideo != null && playerVideo.isPlaying()) {
                mVideoTimeListener.endTime();
                playerVideo.pause();
            }
            SuperPlayerManage.videoPlayViewManage = null;
        } else if (playerVideo != null) {
            if (playerVideo != null && playerVideo.isPlaying()) {
                mVideoTimeListener.endTime();
                playerVideo.onPause();
            }
        }
    }


    private String beginTime;

    @Override
    protected void onStart() {
        super.onStart();
        beginTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isFirst){
            isFirst  = false;
        }else{
            GoodsInfoHttp.getIsNotCollect(storyId, mContext, mGoodsInfoListener);
        }
        if (playerVideo != null) {
            playerVideo.onPause();
        }
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
        if (flag == 1)// 纪录片
            if (playerVideo != null && playerVideo.onBackPressed()) {
                isPortrait = true;
                return;
            } else
                super.onBackPressed();

        else {// 故事
            if (mStoryOrVideoFragment == null || !mStoryOrVideoFragment.onBackPressed()) {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    super.onBackPressed(); //退出
                } else {
                    getSupportFragmentManager().popBackStack(); //fragment 出栈
                }
            }
        }
    }


    @Override
    public void setSelectedFragment(StoryOrVideoFragment selectedFragment) {
        mStoryOrVideoFragment = selectedFragment;
    }

    @Override
    public void setItSelf(boolean isClick) {
        isRecover = isClick;
    }

    private String endTime;

    @Override
    protected void onStop() {
        super.onStop();
        endTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
        UDPSendInfoBean bean = new UDPSendInfoBean();
        bean.getData("003_" + storyId,
                goodsDetailBean.getGoodsBasicInfoBean().getGoodsTitle(),
                ShopConstant.STORY_INFO + "id=" + storyId, beginTime, endTime);
        sendData(bean);
    }

    SuperPlayer.VideoTimeListener mVideoTimeListener = new SuperPlayer.VideoTimeListener() {
        @Override
        public void beginTime() {
            videoBeginTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
        }

        @Override
        public void endTime() {
            videoEndTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
            UDPSendInfoBean beanVedio = new UDPSendInfoBean();
            beanVedio.getData("006_" + storyId, goodsService.getTitle(), goodsService.getContent(), videoBeginTime, videoEndTime);
            sendData(beanVedio);
        }
    };


    private WindowUtilsListener listener;

    public void setZoomListener(WindowUtilsListener listener){
        this.listener = listener;
    }

    public interface WindowUtilsListener{
        void onClose(View view);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(video_frameLayout.getVisibility() == View.VISIBLE && webVideo != null){
                listener.onClose(webVideo);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 添加webvideo
     */
    public void addVideoView(View view) {
        webVideo = view;
        if(videoContain == null) {
            removeVideoView();
            listener.onClose(webVideo);
            return;
        }
        videoContain.addView(view,new RelativeLayout
                .LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
        video_frameLayout.setVisibility(View.VISIBLE);
        y1 = ((RecyclerView)srrv_goods_info.getScrollView()).computeVerticalScrollOffset();
    }
    private int y1 = 0;
    /**
     * 删除webVideo
     */
    public void removeVideoView(){
        if(videoContain != null && videoContain.getChildCount() > 0){
            videoContain.removeView(webVideo);
            listener.onClose(webVideo);
            webVideo = null;
        }
        srrv_goods_info.getScrollView().scrollBy(0, y1);
        video_frameLayout.setVisibility(View.GONE);
    }
}
