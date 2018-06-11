package com.travel.video.playback_video;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.tencent.qcloud.suixinbo.views.LVBPlayerControler;
import com.tencent.qcloud.suixinbo.views.customviews.HeartLayout;
import com.travel.VideoConstant;
import com.travel.activity.HomeActivity;
import com.travel.activity.OneFragmentActivity;
import com.travel.bean.VideoInfoBean;
import com.travel.fragment.PersonalHomeFragment;
import com.travel.fragment.PlayOutFragment;
import com.travel.http_helper.GetCountHttp;
import com.travel.layout.DialogTemplet;
import com.travel.lib.ui.LoadingDialog;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.PopWindowUtils;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.activity.GoodsActivity;
import com.travel.shop.helper.CCTVVideoHttpHelper;
import com.travel.video.adapter.BarrageAdapter;
import com.travel.video.bean.BarrageInfo;
import com.travel.video.help.LiveHttpRequest;
import com.travel.video.help.PlaybackHttpHelper;
import com.travel.video.layout.ProductPopupWindow;
import com.travel.video.layout.SmallVideoWindow;
import com.travel.video.layout.VideoCommentPopupWindow;
import com.travel.video.layout.VideoMenuPopupWindow;
import com.travel.video.sql.VideoVoteUtil;
import com.travel.video.widget.ActivitysVoteLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 回放播放器的界面业务更新操作，不包括播放器的基本操作
 *
 * @author Administrator 更新打签1.0
 */
public class PlaybackPlayerHandler extends Handler implements PlaybackHttpHelper.VideoHttpListener {
    private PlaybackHttpHelper httpHelper;
    private Activity activity = null;
    private int screenWidth;

    // 跳转页面时是否要暂停直播
    public static boolean isPause = true;

    private ActivitysVoteLayout vote;
    private DialogTemplet reportDialog;// 举报弹框
    private ProductPopupWindow productWindow;

    private RelativeLayout videoContain;
    private LVBPlayerControler mLvbPlayerControler;
    private LinearLayout layoutHead;
    protected HeartLayout mHeartLayout;
    private RelativeLayout zanLayout;// 点赞动画区域布局
    private RelativeLayout barrageLayout;// 弹幕显示区域布局
    private LinearLayout zanLinearLayout;
    private LinearLayout progressLayout;

    private TextView titleLive;// 直播主题
    private TextView videoNameOrType;// 视频用户名和类型
    private ImageView videoHeadImg;
    private ImageView finishLive;// 直播结束
    private ImageView report;// 举报按钮

    private ImageView zan;// 点赞
    private TextView zanNum;// 点赞数
    private TextView totalNum;// 累计人数
    private RelativeLayout fooderLayout;// 底部按钮容器
    private ImageView shield;// 屏蔽其他控件
    private ImageView shareVideo;// 分享视频
    private ImageView openProduct;// 打开产品弹框
    private ImageView changeOrientation;// 切换横竖屏
    private ImageView commentText;// 点评按钮
    private ListView barrageListView;// 弹幕列表

    private Boolean isEnd = true;// 判断列表是否在底部
    private BarrageAdapter barrageAdapter;
    private List<BarrageInfo> barrageInfosVisible = new ArrayList<BarrageInfo>();
    private List<BarrageInfo> barrageInfoContainer = new ArrayList<BarrageInfo>();

    public final static int MSG_NETWORK_DISCONNECT = 11003;
    public final static int CLOSE_LIVE = 11005;
    public final static int UPDATE_BARRAGE = 11002;// 刷新弹幕
    public final static int SUBMIT_PRAISE_DATA = 11008;// 提交点赞数据

    private VideoInfoBean videoBean;
    private String userId = "";
    private String videoId = "";
    private String nicknames = "";
    private int vedioType = 1;
    private String videoUserImg = "";
    private String share = "";
    private String activityId = "-1";

    // 实时更新的数据
    private int commentNum = 0;// 点评数
    private int tatlWatchNum = 0;// 总共人数
    private int praiseNum = 0;// 总共点赞数
    private int myPraiseNum = 0;// 我点赞增加数
    private int addPraiseNum = 0;// 服务器点赞增加数addPraiseNum = praiseNum=屏幕显示的点赞数
    private long createTime = 0;// 直播创建时间

    // 抖音版页面相关
    private View hidecontainer, tik_tok_layout, layout_goods;
    private ImageView iv_release, iv_player_photo, iv_goods_picture;
    private TextView tv_follow, tv_zan, tv_play_comment, tv_play_share,
            tv_play_name, tv_place, tv_description, tv_city_name;
    private boolean isTik;// 是否是从首页进入
    private int shareNum = 0;// 分享数
    // 关注相关
    private GetCountHttp getCountHelper;
    private GetCountHttp.CountListener countListener = new GetCountHttp.CountListener() {

        @Override
        public void OnGetVideoCount(boolean isResult, int videoCount) {
        }

        @Override
        public void OnGetFollowCount(boolean isResult, int followCount) {
        }

        @Override
        public void OnGetFollowerCount(boolean isResult, int followerCount) {
        }

        @Override
        public void OnGetIsFollow(boolean isResult, boolean isFollowStatus) {
            tv_follow.setText(isFollowStatus ? "已关注" : "关注");
        }

        @Override
        public void onGetPlace(boolean isResult, String place) {
        }

        @Override
        public void onFollowControl(boolean isResult) {
            LoadingDialog.getInstance(activity).hideProcessDialog(0);
            if (isResult) {
                if (TextUtils.equals(tv_follow.getText().toString(), "关注")) {
                    tv_follow.setText("已关注");
                } else {
                    tv_follow.setText("关注");
                }
                TravelUtil.showToast("操作成功");
            }
        }
    };
    // 点赞相关
    private boolean isVideoLike;
    private CCTVVideoHttpHelper.CCTVVideoLikeListener cctvVideoLikeListener = new CCTVVideoHttpHelper.CCTVVideoLikeListener() {
        @Override
        public void onSuccessGet(boolean isLike) {
            isVideoLike = isLike;
            setZanIcon(isVideoLike ? R.drawable.home_ico_fab_pre : R.drawable.home_ico_fab);
        }

        @Override
        public void onLikeClick() {
            isVideoLike = !isVideoLike;
            setZanIcon(isVideoLike ? R.drawable.home_ico_fab_pre : R.drawable.home_ico_fab);
            TravelUtil.showToast("操作成功");
            tv_zan.setText((isVideoLike ? ++praiseNum : --praiseNum) + "");
            if (countListeners != null) {
                countListeners.onZanCount(praiseNum);
            }
        }

        @Override
        public void onSuccessGetLikeCount(int count) {
        }
    };

    public PlaybackPlayerHandler(final Activity a, VideoInfoBean videoBean) {
        isTik = false;
        this.videoBean = videoBean;
        this.activity = a;
        createTime = videoBean.getTimestamp();

        initView();
        initData();
        initListener();
        initBarrageListView();

//		if (!isVisibleProductButton()) {
//			openProduct.setVisibility(View.GONE);
//		}

        httpHelper = new PlaybackHttpHelper(a, this);
        httpHelper.initVideoData(videoBean.getVideoId());
    }

    public PlaybackPlayerHandler(final Activity a, View view, VideoInfoBean videoBean) {
        isTik = true;
        this.videoBean = videoBean;
        this.activity = a;
        createTime = videoBean.getTimestamp();

        initView(view);
        initData();
        initListener();
//        initBarrageListView();

//        httpHelper = new PlaybackHttpHelper(a, this);
//        httpHelper.initVideoData(videoBean.getVideoId());
    }

    private void initData() {
        userId = videoBean.getPersonalInfoBean().getUserId();
        videoId = videoBean.getVideoId();
        videoUserImg = videoBean.getPersonalInfoBean().getUserPhoto();
        nicknames = videoBean.getPersonalInfoBean().getUserName();
        vedioType = videoBean.getVideoType();
        activityId = videoBean.getActivityId();
        tatlWatchNum = videoBean.getWatchCount();
        commentNum = videoBean.getCommentCount();
        praiseNum = videoBean.getPraiseNum();
        shareNum = videoBean.getShareNum();
        share = videoBean.getShare();
        screenWidth = OSUtil.getScreenWidth();

        if (isTik) {
            // 头像
            ImageDisplayTools.displayCircleImage(videoUserImg, iv_player_photo, OSUtil.dp2px(activity, 1), Color.WHITE);
            getCountHelper = new GetCountHttp(countListener);
            getFollowZanData();
            // 点赞数
            tv_zan.setText(OSUtil.getLikeCount(praiseNum));
            // 评论数
            tv_play_comment.setText(OSUtil.getLikeCount(commentNum));
            // 分享数
            tv_play_share.setText(OSUtil.getLikeCount(shareNum));
            // 视频名
            tv_play_name.setText(videoBean.getVideoTitle());
            // 地址
            tv_place.setText(videoBean.getReleaseAddress() + " " + DateFormatUtil.getDate_M_D(videoBean.getReleaseTime()));
            // 描述
            tv_description.setText(videoBean.getVideoDescription());
            // 城市名
            tv_city_name.setText(videoBean.getCityName());
            // 商品图片
            ImageDisplayTools.displayCircleImage(videoBean.getCityImg(), iv_goods_picture, OSUtil.dp2px(activity, 1), Color.WHITE);

        } else {
            totalNum.setText(OSUtil.getLikeCount(tatlWatchNum));

            if (UserSharedPreference.isLogin() && VideoVoteUtil.getInstance().getDatas().containsKey(Integer.parseInt(videoId))) {
                // 我想去
                zan.setImageResource(R.drawable.icon_want_go);
            } else {
                // 我不想去
                zan.setImageResource(R.drawable.icon_unwant_go);
            }
//		zanNum.setText(praiseNum + "");
            zanNum.setText(OSUtil.getLikeCount(videoBean.getVoteNum()));
            titleLive.setText(videoBean.getVideoTitle());
            videoNameOrType.setText(nicknames);
            ImageDisplayTools.displayHeadImage(videoUserImg, videoHeadImg);
            if (!OSUtil.isDayTheme())
                videoHeadImg.setColorFilter(TravelUtil.getColorFilter(activity));

//		vote.setInitData(userId, activityId);
        }
    }

    public void getFollowZanData() {
        // 关注
        if (TravelUtil.isHomePager(userId)) {
            tv_follow.setVisibility(View.INVISIBLE);
        } else {
            tv_follow.setVisibility(View.VISIBLE);
            if (UserSharedPreference.isLogin()) {
                getCountHelper.getIsFollow(userId, activity);
            } else {
                tv_follow.setText("关注");
            }
        }
        // 点赞
        if (UserSharedPreference.isLogin()) {
            CCTVVideoHttpHelper.getVideoLike(activity, videoId, cctvVideoLikeListener);
        } else {
            setZanIcon(R.drawable.home_ico_fab);
        }
    }

    private void initView() {
        videoContain = (RelativeLayout) activity.findViewById(R.id.video_contain);
        mLvbPlayerControler = (LVBPlayerControler) activity.findViewById(R.id.lvb_player_controler);
        hidecontainer = activity.findViewById(R.id.hidecontainer);
        tik_tok_layout = activity.findViewById(R.id.tik_tok_layout);
        hidecontainer.setVisibility(View.VISIBLE);
        tik_tok_layout.setVisibility(View.GONE);

        layoutHead = (LinearLayout) activity.findViewById(R.id.layoutHead);
        zanLinearLayout = (LinearLayout) activity.findViewById(R.id.zanLinearLayout);
        progressLayout = (LinearLayout) activity.findViewById(R.id.progressLayout);
        titleLive = (TextView) activity.findViewById(R.id.videoNameOrType);
        videoHeadImg = (ImageView) activity.findViewById(R.id.videoHeadImg);
        videoNameOrType = (TextView) activity.findViewById(R.id.videoNameOrType);
        finishLive = (ImageView) activity.findViewById(R.id.closeVideo);
        report = (ImageView) activity.findViewById(R.id.report);
        fooderLayout = (RelativeLayout) activity.findViewById(R.id.layoutFooter);
        zan = (ImageView) activity.findViewById(R.id.zan_click);
        shield = (ImageView) activity.findViewById(R.id.videoShield);
        shareVideo = (ImageView) activity.findViewById(R.id.shareVideo);
        openProduct = (ImageView) activity.findViewById(R.id.openProduct);
        changeOrientation = (ImageView) activity.findViewById(R.id.changeOrientation);
        commentText = (ImageView) activity.findViewById(R.id.commentText);
        zanLayout = (RelativeLayout) activity.findViewById(R.id.zanLayout);
        mHeartLayout = (HeartLayout) activity.findViewById(R.id.heart_layout);
        zanNum = (TextView) activity.findViewById(R.id.zan_num);
        totalNum = (TextView) activity.findViewById(R.id.total_num);
        barrageListView = (ListView) activity.findViewById(R.id.barrageListView);
        barrageLayout = (RelativeLayout) activity.findViewById(R.id.barrageLayout);
        vote = (ActivitysVoteLayout) activity.findViewById(R.id.vote);
        vote.setVisibility(View.GONE);
    }

    private void initView(View activity) {
        videoContain = (RelativeLayout) activity.findViewById(R.id.video_contain);
        mLvbPlayerControler = (LVBPlayerControler) activity.findViewById(R.id.lvb_player_controler);
        hidecontainer = activity.findViewById(R.id.hidecontainer);
        tik_tok_layout = activity.findViewById(R.id.tik_tok_layout);
        hidecontainer.setVisibility(View.GONE);
        tik_tok_layout.setVisibility(View.VISIBLE);

        layout_goods = activity.findViewById(R.id.layout_goods);
        iv_release = (ImageView) activity.findViewById(R.id.iv_release);
        iv_player_photo = (ImageView) activity.findViewById(R.id.iv_player_photo);
        iv_goods_picture = (ImageView) activity.findViewById(R.id.iv_goods_picture);
        tv_follow = (TextView) activity.findViewById(R.id.tv_follow);
        tv_zan = (TextView) activity.findViewById(R.id.tv_zan);
        tv_play_comment = (TextView) activity.findViewById(R.id.tv_play_comment);
        tv_play_share = (TextView) activity.findViewById(R.id.tv_play_share);
        tv_play_name = (TextView) activity.findViewById(R.id.tv_play_name);
        tv_place = (TextView) activity.findViewById(R.id.tv_place);
        tv_description = (TextView) activity.findViewById(R.id.tv_description);
        tv_city_name = (TextView) activity.findViewById(R.id.tv_city_name);
    }

    private void initListener() {
        if (isTik) {
            iv_release.setOnClickListener(new VideoReleaseListener());
            iv_player_photo.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(PersonalHomeFragment.IS_HOME_PAGER, TravelUtil.isHomePager(userId));
                    bundle.putString("id", userId);
                    OneFragmentActivity.startNewActivity(activity, "", PersonalHomeFragment.class, bundle);
                }
            });
            tv_follow.setOnClickListener(new FollowListener("follow"));
            tv_zan.setOnClickListener(new FollowListener("zan"));
            tv_play_comment.setOnClickListener(new CommentTextListener());
            tv_play_share.setOnClickListener(new ShareLiveListener());
            layout_goods.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, GoodsActivity.class);
                    intent.putExtra("goodsId", videoBean.getGoodsId());
                    activity.startActivity(intent);
                }
            });
        } else {
            report.setOnClickListener(new ReportListener());
            zanLinearLayout.setOnClickListener(new ZanListener());
            shield.setOnClickListener(new ShieldListener());
            shareVideo.setOnClickListener(new ShareLiveListener());
//		openProduct.setOnClickListener(new OpenProductListener());
            changeOrientation.setOnClickListener(new ChangeOrientationListener());
            commentText.setOnClickListener(new CommentTextListener());
            videoHeadImg.setOnClickListener(new FollowWindowListener());
        }

    }

    private void initBarrageListView() {
        barrageLayout.getLayoutParams().height = (int) (OSUtil.getScreenHeight() / 3);
        barrageAdapter = new BarrageAdapter(activity, barrageInfosVisible);
        barrageListView.setAdapter(barrageAdapter);
        barrageAdapter.notifyDataSetChanged();

        barrageListView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 当不滚动时
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    // 判断是否滚动到底部
                    isEnd = (view.getLastVisiblePosition() == view.getCount() - 1) ? true : false;
                    barrageScrollToEnd();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    private void barrageScrollToEnd() {
        barrageAdapter.notifyDataSetChanged();
        if (isEnd)
            barrageListView.setSelection(barrageAdapter.getCount());
    }

    @Override
    public void GetHttpInitData(List<BarrageInfo> barrageInfos, Long createVideoTime) {
        myPraiseNum = 0;
        barrageInfoContainer.addAll(barrageInfos);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case MSG_NETWORK_DISCONNECT:
                Toast.makeText(activity, "失去网络连接！error：" + msg.arg1, Toast.LENGTH_SHORT).show();
                break;
            case UPDATE_BARRAGE:
                barrageScrollToEnd();
                break;
            case CLOSE_LIVE:
                activity.finish();
                break;
            case SUBMIT_PRAISE_DATA:
                /*if ("".equals(zanNum.getText().toString()))
                    return;

				int addPraiseNum = Integer.parseInt(zanNum.getText().toString().trim()) - praiseNum;
				httpHelper.submitPraiseData(videoId,addPraiseNum);*/
                break;
            default:
                break;
        }
    }

    private void addOnePraiseToUI() {
        //点赞动画
        mHeartLayout.addFavor();
    }

    /**
     * 根据进度条来计算弹幕的更新
     *
     * @param seekBarTime     进度条当前时间
     * @param updataRate      更新频率
     * @param isSeekBarChange 精度条是否被拖动，true表示手动拖动，false表示未拖动
     */
    public void updataBarrage(final long seekBarTime, final long updataRate, final boolean isSeekBarChange) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (barrageInfosVisible) {
                    if (barrageInfoContainer == null || barrageInfoContainer.size() == 0)
                        return;
                    if (createTime == 0)
                        return;

                    boolean isClearStatus = isSeekBarChange;
                    for (int i = 0; i < barrageInfoContainer.size(); i++) {
                        BarrageInfo barrageInfo = barrageInfoContainer.get(i);
                        Long barrageTimes = barrageInfo.getSubmitTime() - createTime;
                        if (isClearStatus) {
                            barrageInfosVisible.clear();
                            isClearStatus = false;
                        }

                        // 假如手动拖动进度条则初始化弹幕状态加载
                        if (isSeekBarChange) {
                            if (seekBarTime - barrageTimes >= 0) {
                                barrageInfoContainer.get(i).setShowStatus(barrageInfo.STATUS_ALREADY);
                                barrageInfosVisible.add(barrageInfoContainer.get(i));
                            } else {
                                barrageInfoContainer.get(i).setShowStatus(barrageInfo.STATUS_NO);
                            }
                        }
                        // 根据精度条的更新时间段判断与弹幕的匹配
                        if ((seekBarTime - barrageTimes) < updataRate && (seekBarTime - barrageTimes) > -updataRate) {
                            // value值already表示已经显示 no表示还没有显示
                            if (barrageInfoContainer.get(i).getShowStatus() != BarrageInfo.STATUS_ALREADY) {
                                barrageInfoContainer.get(i).setShowStatus(BarrageInfo.STATUS_ALREADY);
                                barrageInfosVisible.add(barrageInfoContainer.get(i));
                            }
                        }
                    }
                    sendEmptyMessage(UPDATE_BARRAGE);
                }
            }
        }).start();
    }

    private class ShieldListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (zanLinearLayout.getVisibility() == View.VISIBLE) {
                layoutHead.setVisibility(View.INVISIBLE);
                zanLayout.setVisibility(View.INVISIBLE);
                barrageLayout.setVisibility(View.INVISIBLE);
                zanLinearLayout.setVisibility(View.INVISIBLE);
                shareVideo.setVisibility(View.INVISIBLE);
                report.setVisibility(View.INVISIBLE);
                shareVideo.setVisibility(View.INVISIBLE);
                changeOrientation.setVisibility(View.INVISIBLE);
//				if (openProduct.getVisibility() != View.GONE && PlaybackVideoPlayerActivity.isShowShopButton)
//					openProduct.setVisibility(View.INVISIBLE);
                commentText.setVisibility(View.GONE);
                progressLayout.setVisibility(View.INVISIBLE);
                shield.setImageResource(R.drawable.live_point_shield);
            } else if (zanLinearLayout.getVisibility() == View.INVISIBLE) {
                layoutHead.setVisibility(View.VISIBLE);
                zanLayout.setVisibility(View.VISIBLE);
                zanLinearLayout.setVisibility(View.VISIBLE);
                report.setVisibility(View.VISIBLE);
                barrageLayout.setVisibility(View.VISIBLE);
                changeOrientation.setVisibility(View.VISIBLE);
                shareVideo.setVisibility(View.VISIBLE);
//				if (openProduct.getVisibility() != View.GONE && PlaybackVideoPlayerActivity.isShowShopButton)
//					openProduct.setVisibility(View.VISIBLE);
                commentText.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.VISIBLE);
                shield.setImageResource(R.drawable.live_point_shield_none);
            }
        }
    }

    private class OpenProductListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            productWindow = new ProductPopupWindow(activity, userId,
                    new ProductPopupWindow.ClickProductItemListener() {

                        @Override
                        public void notifyIntentActivity() {
                            isPause = false;
                            showSmallVideoPopupWindow();
                        }
                    });
        }
    }

    private class ChangeOrientationListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            mLvbPlayerControler.changeOrientation();
        }
    }

    private class ReportListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            reportDialog = new DialogTemplet(activity, false, "该直播有不当内容，我要举报", "", "取消", "确认");
            reportDialog.show();

            reportDialog.setLeftClick(new DialogTemplet.DialogLeftButtonListener() {
                @Override
                public void leftClick(View view) {

                }
            });
            reportDialog.setRightClick(new DialogTemplet.DialogRightButtonListener() {
                @Override
                public void rightClick(View view) {
                    LiveHttpRequest.reportVideoRequest(activity, userId, videoId, "1");
                }
            });
        }
    }

    private class ZanListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            /*zan.setImageResource(R.drawable.icon_red_zan);
            if (zanNum.getText() != null && !"".equals(zanNum.getText().toString())) {
				zanNum.setText(Integer.parseInt(zanNum.getText().toString()) + 1 + "");
			}
			myPraiseNum = myPraiseNum + 1;
			addOnePraiseToUI();*/
            if (!UserSharedPreference.isLogin()) {
                Toast.makeText(activity, "请登录", Toast.LENGTH_SHORT).show();
                return;
            }
            if (VideoVoteUtil.getInstance().getDatas().containsKey(videoId)) {
                Toast.makeText(activity, "您已点赞", Toast.LENGTH_SHORT).show();
                return;
            }
            VideoVoteUtil.getInstance().voteVideo(videoId, UserSharedPreference.getUserId(), new VideoVoteUtil.VideoVoteListener() {
                @Override
                public void onSuccess(int error, boolean isSuccess) {
                    if (isSuccess) {
                        zanNum.setText((Integer.parseInt(zanNum.getText().toString().replace("赞", "")) + 1) + "赞");
                        zan.setImageResource(R.drawable.icon_want_go);
                    } else if (error == 1) {
                        zan.setImageResource(R.drawable.icon_want_go);
                    }
                }
            });
        }
    }

    private class ShareLiveListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            isPause = true;
            String shareUrl;
            if (isTik) {
                shareUrl = VideoConstant.TIK_SHARE_VIDEO_URL + share;
            } else {
                shareUrl = VideoConstant.SHARE_VIDEO_URL + share;
            }
            PopWindowUtils.sharePopUpWindow(activity, "红了旅行", videoBean.getVideoTitle(), videoBean.getVideoImg(), shareUrl);
//            OSUtil.showShare(null, titleLive.getText().toString(), videoBean.getVideoTitle(), videoBean.getVideoImg(), shareUrl, shareUrl, activity);
        }
    }

    private VideoCommentPopupWindow mVideoCommentPopupWindow = null;

    private class CommentTextListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            mVideoCommentPopupWindow = new VideoCommentPopupWindow(activity, videoId, commentNum, isTik);
            mVideoCommentPopupWindow.setUpdateCommentCountListener(new VideoCommentPopupWindow.UpdateCommentCountListener() {
                @Override
                public void updateCommentCount(int count) {
                    commentNum = count;
                    if (isTik) {
                        tv_play_comment.setText(count + "");
                        if (countListeners != null) {
                            countListeners.onCommantCount(count);
                        }
                    }
                }
            });
        }
    }

    private class FollowWindowListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            PopWindowUtils.followPopUpWindow(activity, userId, nicknames, videoUserImg, 1);
        }
    }

    private void showSmallVideoPopupWindow() {
        ((ViewGroup) mLvbPlayerControler.getParent()).removeView(mLvbPlayerControler);
        SmallVideoWindow.getInstance().setZoomListener(new SmallVideoWindow.WindowUtilsListener() {
            @Override
            public void onClose(View view) {
                if (view != null) {
                    videoContain.addView(view);
                }
                activity.finish();
            }

            @Override
            public void onZoom(View view) {
                if (view == null) return;
                videoContain.addView(view);
            }
        });
        SmallVideoWindow.getInstance().showPopupWindow();
        SmallVideoWindow.getInstance().addVideoView(mLvbPlayerControler);
    }

    /**
     * 视频发布
     */
    private class VideoReleaseListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            VideoMenuPopupWindow popupWindow = new VideoMenuPopupWindow(activity, new VideoMenuPopupWindow.ClickProductItemListener() {

                @Override
                public void notifyIntentActivity() {

                }
            });
            popupWindow.setActivityId(-100);// 为了区分是从首页进入的还是从城会玩进入的
        }
    }

    /**
     * 关注和点赞
     */
    private class FollowListener implements OnClickListener {

        private String tag;

        public FollowListener(String tag) {
            this.tag = tag;
        }

        @Override
        public void onClick(View v) {
            if (TextUtils.equals(tag, "follow")) {
                if (tv_follow.getVisibility() == View.VISIBLE) {
                    if (TextUtils.equals(tv_follow.getText().toString(), "关注")) {
                        getCountHelper.followNet(activity, "1", userId);
                    } else {
                        getCountHelper.followNet(activity, "2", userId);
                    }
                }
            } else {
                if (isVideoLike) {
                    CCTVVideoHttpHelper.onLikeClick(activity, videoId, 2, cctvVideoLikeListener);
                } else {
                    CCTVVideoHttpHelper.onLikeClick(activity, videoId, 1, cctvVideoLikeListener);
                }
            }
        }
    }

    private void setZanIcon(int imgRes) {
        Drawable img = ContextCompat.getDrawable(activity, imgRes);
        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
        tv_zan.setCompoundDrawables(null, img, null, null);
    }

    public interface CountListener {
        void onZanCount(int count);

        void onCommantCount(int count);
    }
    private CountListener countListeners;

    public void setCountListener(CountListener countListeners) {
        this.countListeners = countListeners;
    }

}
