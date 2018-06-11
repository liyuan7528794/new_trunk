package com.travel.localfile.pk.fragment;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.ctsmedia.hltravel.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.travel.Constants;
import com.travel.activity.HomeActivity;
import com.travel.activity.OneFragmentActivity;
import com.travel.communication.entity.UserData;
import com.travel.layout.ImageViewPopupWindow;
import com.travel.lib.helper.PullToRefreshHelper;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.PopWindowUtils;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.CameraFragment;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.pk.adapter.EvidenceAdapter;
import com.travel.localfile.pk.entity.EvidencePacket;
import com.travel.localfile.pk.others.PublicVoteCommentsHelper;
import com.travel.shop.bean.AttachGoodsBean;
import com.travel.shop.bean.GoodsOrderBean;
import com.travel.shop.bean.OrdersBasicInfoBean;
import com.travel.shop.http.OrderInfoHttp;
import com.travel.video.layout.VideoViewPopWindow;
import com.travel.video.widget.VoteLVBFullScreenView;
import com.travel.video.widget.VoteLVBMediaView;
import com.travel.widget.ListViewOverrideTouch;
import com.travel.widget.RecorderPlayerDialog;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.player.media.IjkVideoView;

/**
 * 众投界面 Created by ldkxingzhe on 2016/7/4.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class VoteFragment extends Fragment
		implements EvidenceAdapter.Listener, OneFragmentActivity.OneFragmentInterface {
	@SuppressWarnings("unused")
	private static final String TAG = "VoteFragment";

	public static final int WIN_BUYER = 1; // buyer胜
	public static final int WIN_SELLER = 2; // seller胜
	public static final int WIN_VOTING = 3; // 投票ing
	public static final int WIN_UNKNOWN = 4; // 结果未知
	public static final int WIN_MUST_ADD_ONE_EVIDENCE = 5; // 必须提交一个证据阶段
	public static final int WIN_UNDER_REVIEWING = 6; // 审核中...
	public static final int WIN_APPLICATION_FAILED = 7; // 审核失败
	public static final String USER_ID_SELLER = "seller_user_id";
	public static final String USER_ID_BUYER = "buyer_user_id";
	public static final String GOTO_HOME_ACTIIVTY = "goto_home_activity";

	private View mRootView;
	private LinearLayout voteContain;
	private ImageView mLeftHeaderImage, mRightHeaderImage;
	private TextView mVoteStartTimeTextView;
	private ListViewOverrideTouch mEvidenceListView;
	private View mTitleCheckLayout;

	private View mUploadView, mGotoLive, mMiddleLine; // 我要上传, 我要直播, 中间的线
	private View mHeaderView, mFooterView, mSupportWhoView;

	private ImageView mOrderImageView;
	private View mOrderLayout;

	private FrameLayout mFragmentContainer;
	private ListViewOverrideTouch mCommentListView;
	private Button mCommentsSendBtn;
	private EditText mCommentsEditText;
	private PublicVoteCommentsHelper mCommentsHelper;

	private EvidenceAdapter mAdapter;
	private List<EvidencePacket> mEvidencePacketList = new ArrayList<EvidencePacket>();

	private HttpRequest mHttpRequest;
	private SupportWhoPartHelper mSupportWhoPartHelper;

	// 传值的内容, 卖家Id, 买家Id
	// 之所以不适用UserData对象是其他项目文件需要引用这个
	private String mSellerId = "", mBuyerId = "", mUserId = "";
	private String mSellerNickName, mBuyerNickName;
	private String mSellerHeaderImage = "", mBuyerHeaderImage = "";
	private int mVoteStatus = WIN_BUYER; // 众投状态: 1 -- 买家胜; 2 -- 买家胜; 3 ---
											// 众投进行中;
	private int mNumOfSupportBuyer = 0, mNumOfSupportSeller = 0; // 支持买家和卖家的人数
	private String mOrderCreateTime = "2017-06-23", mVoteReason = "";
	private double mVoteClaimCount; // 索赔金额
	private int mVoteRedCoin; // 投票所需金币数
	private GoodsOrderBean mGoodsOrderBean;
	private int mVoteId = 1; // 众投Id

	private boolean mCanShowLiveVideo = true; // 是否可以显示直播界面
	private boolean mGotoHomeActivity = false;

	public static final String BUNDLE_VOTE_ID = "vote_id";
	public static final String BUNDLE_VOTE_STATUS = "vote_status";

	// pop
	private ImageViewPopupWindow mImageViewPopupWindow;
	private VideoViewPopWindow mVideoViewPopWindow;
	private RecorderPlayerDialog mRecorderPlayerDialog;
	private MediaController.MediaPlayerControl mVideoView;

	// 是否是众投页, true -- 是众投页
	private boolean mIsPublicPage = true;
	private int mVoteFlags;

	public static final int M_HAS_VOTED = 1; // 已经投过票了
	public static final int THE_RESULT_OF_VOTED_HAS_GOT = 1 << 1; // 已经从网络获取是否投票的结构

	// pk视频视频
	private VoteLVBMediaView voteMediaView;
	private VoteLVBFullScreenView voteLVBFullScreenView;
	private int liveButtonShowCode = 0;//0表示第一次，1表示初始化视频时显示按钮，2表示不显示按钮
	private int mLiveViewHeight = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 屏幕常亮
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		initIntentData();
		mAdapter = new EvidenceAdapter(mEvidencePacketList, this);
		mHttpRequest = new HttpRequest();
		mSupportWhoPartHelper = new SupportWhoPartHelper();
	}

	private void initIntentData() {
		Bundle bundle = getArguments();
		if (bundle == null)
			return;
		mVoteId = bundle.getInt(BUNDLE_VOTE_ID, mVoteId);
		mVoteStatus = bundle.getInt(BUNDLE_VOTE_STATUS, mVoteStatus);
		mSellerId = bundle.getString(USER_ID_SELLER, mSellerId);
		mBuyerId = bundle.getString(USER_ID_BUYER, mBuyerId);
		mGotoHomeActivity = bundle.getBoolean(GOTO_HOME_ACTIIVTY, false);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_vote, container, false);
		mHeaderView = inflater.inflate(R.layout.include_order_layout, null, false);
		mFooterView = inflater.inflate(R.layout.include_vote_fragment_footer, null, false);
		voteContain = (LinearLayout) mRootView.findViewById(R.id.vote_contain);
		mLeftHeaderImage = (ImageView) mRootView.findViewById(R.id.iv_pk_header_left);
		mRightHeaderImage = (ImageView) mRootView.findViewById(R.id.iv_pk_header_right);
		mEvidenceListView = (ListViewOverrideTouch) mRootView.findViewById(R.id.lv_evidence);
		mTitleCheckLayout = mRootView.findViewById(R.id.ll_title_layout);
		mFragmentContainer = (FrameLayout) mRootView.findViewById(R.id.fl_fragment_container);
		mCommentListView = (ListViewOverrideTouch) mFragmentContainer.findViewById(R.id.lv_comments_list);
		mCommentsSendBtn = (Button) mFragmentContainer.findViewById(R.id.btn_send);
		mCommentsEditText = (EditText) mFragmentContainer.findViewById(R.id.et_comments);

		mSupportWhoView = inflater.inflate(R.layout.include_support_who, null, false);
		mSupportWhoPartHelper.initView(mSupportWhoView);

		mEvidenceListView.getRefreshableView().addHeaderView(mHeaderView, null, false);
		mEvidenceListView.getRefreshableView().addHeaderView(mSupportWhoView, null, false);
		mEvidenceListView.getRefreshableView().addFooterView(mFooterView, null, false);

		mEvidenceListView.setAdapter(mAdapter);
		setIsPublicVotePage(false);
		setIsPublicVotePage(true);
		mTitleCheckLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setIsPublicVotePage(!mIsPublicPage);
				TextView publicVoteTextView = (TextView) mTitleCheckLayout.findViewById(R.id.tv_public_vote);
				TextView commentsVoteTextView = (TextView) mTitleCheckLayout.findViewById(R.id.tv_comments);
				int white = Color.WHITE;
				int black = ContextCompat.getColor(getContext(), R.color.red_EC6262);
				publicVoteTextView.setTextColor(mIsPublicPage ? white : black);
				commentsVoteTextView.setTextColor(mIsPublicPage ? black : white);
			}
		});

		// int dp280 = OSUtil.dp2px(getActivity(), 280);
		int dp280 = OSUtil.getScreenWidth() * 3 / 2 / 2;
		int dp280s = -dp280;

		voteMediaView = (VoteLVBMediaView) mRootView.findViewById(R.id.voteMediaView);
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp280);
		voteMediaView.setLayoutParams(param);
		voteMediaView.setListener(pkVideoListener);
		voteMediaView.initShowLayout(mVoteId, mBuyerId + "", mSellerId + "");

		voteLVBFullScreenView = (VoteLVBFullScreenView) mRootView.findViewById(R.id.vote_full_screen);
		voteLVBFullScreenView.setCallBackListener(voteLVBFullScreenListener);
		voteLVBFullScreenView.setVisibility(View.GONE);

		mRootView.setPadding(0, -dp280, 0, 0);
		mRootView.scrollTo(0, mCanShowLiveVideo ? dp280s : 0);
		mCommentListView.setInterceptTouchListener(mListViewInterceptTouchListener);
		mEvidenceListView.setInterceptTouchListener(mListViewInterceptTouchListener);
		mEvidenceListView.setOnRefreshListener(mEvidenceRefreshListener);
		mEvidenceListView.getRefreshableView().setDivider(null);
		mEvidenceListView.getRefreshableView().setSelector(new ColorDrawable(Color.TRANSPARENT));
		new PullToRefreshHelper(mEvidenceListView).initPullUpToRefreshView(null);
		return mRootView;
	}

	private VoteLVBMediaView.PKVideoListener pkVideoListener = new VoteLVBMediaView.PKVideoListener(){

		@Override
		public void playStatus(boolean isPlaying) {
			
		}

		@Override
		public void hideLiveButton() {
			liveButtonShowCode = 2;
			mGotoLive.setVisibility(View.GONE);
			mMiddleLine.setVisibility(View.GONE);
		}

		@Override
		public void showLiveButton() {
			liveButtonShowCode = 1;
			mGotoLive.setVisibility(View.VISIBLE);
			mMiddleLine.setVisibility(View.VISIBLE);
		}

		@Override
		public void videoFullScreen(boolean isHost) {
			voteLVBFullScreenView.isHost(isHost);
			lvbVideoFullScreen();
		}

		@Override
		public void videoZoomScreen() {
			lvbVideoZoomScreen();
		}
	};

	private VoteLVBFullScreenView.VoteLVBFullScreenListener voteLVBFullScreenListener =
			new VoteLVBFullScreenView.VoteLVBFullScreenListener() {
		@Override
		public void changeCamera() {
			voteMediaView.changeCamera();
		}

		@Override
		public void changeLight() {
			voteMediaView.changeCamera();
		}

		@Override
		public void closeVideo() {
			lvbVideoZoomScreen();
			voteMediaView.stopLive();
		}

		@Override
		public void zoom() {
			lvbVideoZoomScreen();
		}

		@Override
		public void sendBarrage(String barrageContent) {

		}
	};

	private boolean isWaiting = false;
	private void lvbVideoZoomScreen(){
		if(isWaiting || voteLVBFullScreenView.getVisibility() == View.GONE)
			return;
		isWaiting = true;
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				isWaiting = false;
			}
		},500);

//		mRootView.scrollTo(0, 0);
		View view = voteLVBFullScreenView.removeVideoView();
		voteMediaView.addVideoView(view);
		voteContain.setVisibility(View.VISIBLE);
		voteLVBFullScreenView.setVisibility(View.GONE);
	}

	private void lvbVideoFullScreen(){
		if(isWaiting || voteLVBFullScreenView.getVisibility() == View.VISIBLE)
			return;
		isWaiting = true;
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				isWaiting = false;
			}
		},500);

//		mRootView.scrollTo(0, dp280s);
		View view = voteMediaView.removeVideoView();
		voteLVBFullScreenView.addVideoView(view);

		voteLVBFullScreenView.setVisibility(View.VISIBLE);
		voteContain.setVisibility(View.GONE);
	}

	private PullToRefreshBase.OnRefreshListener mEvidenceRefreshListener = new PullToRefreshBase.OnRefreshListener() {
		@Override
		public void onRefresh(PullToRefreshBase refreshView) {
			mHttpRequest.getVoteDataList(mVoteId);
		}
	};

	@Override
	public void onStart() {
		super.onStart();
		mHttpRequest.getPublicVoteDetail(mVoteId);
	}

	@Override
	public void onDestroy() {
		MLog.d(TAG, "onDestroy");
		super.onDestroy();
		if (voteMediaView != null)
			voteMediaView.destroy();
	}

	private ListViewOverrideTouch.InterceptTouchListener mListViewInterceptTouchListener = new ListViewOverrideTouch.InterceptTouchListener() {
		@Override
		public boolean onTouchEvent(int deltaY, boolean isFirstPosition) {
			if (!mCanShowLiveVideo)
				return false;
			if (mLiveViewHeight == -1) {
				mLiveViewHeight = voteMediaView.getHeight();
			}
			int scrollY = mRootView.getScrollY();
			if (!isFirstPosition && scrollY > -mLiveViewHeight && deltaY > 0) {
				// 向下滑动, listView不可以再次下滑, 没有下滑到最大
				if (scrollY + mLiveViewHeight < 50 || scrollY - deltaY < -mLiveViewHeight) {
					mRootView.scrollTo(0, -mLiveViewHeight);
				} else {
					mRootView.scrollBy(0, -deltaY);
				}
				return true;
			}
			if (deltaY < 0 && scrollY < 0) {
				// 向上滑动,
				if (scrollY > -50 || scrollY - deltaY > 0) {
					mRootView.scrollTo(0, 0);
				} else {
					mRootView.scrollBy(0, -deltaY);
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean onActionUp() {
			if (mLiveViewHeight == -1)
				return false;
			int scrollY = mRootView.getScrollY();
			int endScrollY = scrollY < -mLiveViewHeight * 2 / 3 ? -mLiveViewHeight : 0;
			ObjectAnimator animator = ObjectAnimator.ofInt(mRootView, "scrollY", scrollY, endScrollY);
			animator.setInterpolator(new AccelerateInterpolator());
			animator.setDuration(200);
			animator.start();
			return false;
		}

		@Override
		public boolean onInterceptTouchEvent(float deltaY, boolean isFirstPosition) {
			if (deltaY == 0)
				return false;
			if (mLiveViewHeight == -1)
				mLiveViewHeight = voteMediaView.getHeight();
			if (deltaY < 0 && isFirstPosition) {
				// 向上滑
				return mRootView.getScrollY() >= -mLiveViewHeight && mRootView.getScrollY() < 0;
			}
			if (deltaY > 0 && isFirstPosition) {
				// 向下滑
				return mRootView.getScrollY() <= 0 && mRootView.getScrollY() > -mLiveViewHeight;
			}
			return false;
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		mUserId = UserSharedPreference.isLogin() ? UserSharedPreference.getUserId() : null;
		refreshAllUI();
		mHttpRequest.getVoteDataList(mVoteId);
		if (UserSharedPreference.isLogin())
			mHttpRequest.getIsVoted(mVoteId);

		if (voteMediaView != null)
			voteMediaView.resume();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (voteMediaView != null)
			voteMediaView.pause();
	}

	private void refreshAllUI() {
		initOrder();
		initHeaders();
		initFooter();
		mAdapter.notifyDataSetChanged();
		mSupportWhoPartHelper.setSupportNum(mNumOfSupportBuyer, mNumOfSupportSeller);
		switch (mVoteStatus) {
		case WIN_VOTING:
			// 众投进行中
			if (!isBuyer() && !isSeller()) {
				mSupportWhoPartHelper.stateVoting(mVoteFlags);
			} else {
				if ((mVoteFlags & THE_RESULT_OF_VOTED_HAS_GOT) != 0) {
					mEvidenceListView.getRefreshableView().removeHeaderView(mSupportWhoView);
				}
			}
			break;
		case WIN_BUYER:
			mSupportWhoPartHelper.stateVoteOver(true, false);
			break;
		case WIN_SELLER:
			mSupportWhoPartHelper.stateVoteOver(false, true);
			break;
		default:
			mSupportWhoPartHelper.stateVoteOver(false, false);
		}

	}

	private boolean isBuyer() {
		return mUserId != null && mUserId.equals(mBuyerId);
	}

	private boolean isSeller() {
		return mUserId != null && mUserId.equals(mSellerId);
	}

	private void initFooter() {
		View footerView = mFooterView;
		View leftUpload = footerView.findViewById(R.id.include_upload_left);
		View rightUpload = footerView.findViewById(R.id.include_upload_right);
		View visibleView = null;
		if (isBuyer()) {
			// 左边
			leftUpload.setVisibility(View.VISIBLE);
			rightUpload.setVisibility(View.GONE);
			visibleView = leftUpload;
		} else if (isSeller()) {
			// 右边
			leftUpload.setVisibility(View.GONE);
			rightUpload.setVisibility(View.VISIBLE);
			visibleView = rightUpload;
		}
		if (!TextUtils.isEmpty(mUserId) && visibleView != null) {
			visibleView.setVisibility(View.VISIBLE);
			mUploadView = visibleView.findViewById(R.id.tv_evidence_upload);
			mGotoLive = visibleView.findViewById(R.id.tv_start_live);
			mMiddleLine = visibleView.findViewById(R.id.line_second);
			mUploadView.setOnClickListener(mUploadEvidenceListener);
			mGotoLive.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					voteMediaView.getUrlAndStartLive();
					showLiveVideoView();
					MLog.v(TAG, "start scroll");
				}
			});

			int middleLineAndGotoLiveVisibility = mVoteStatus == WIN_VOTING ? View.VISIBLE : View.GONE;
			mMiddleLine.setVisibility(middleLineAndGotoLiveVisibility);
			mGotoLive.setVisibility(middleLineAndGotoLiveVisibility);
			if(liveButtonShowCode == 1){
				mMiddleLine.setVisibility(View.VISIBLE);
				mGotoLive.setVisibility(View.VISIBLE);
			}else if(liveButtonShowCode == 2){
				mMiddleLine.setVisibility(View.GONE);
				mGotoLive.setVisibility(View.GONE);
			}
			if (isBuyer()) {
				TextView status = (TextView) footerView.findViewById(R.id.tv_status);
				status.setBackgroundResource(R.drawable.circle2_d);
				status.setTextColor(ContextCompat.getColor(getContext(), R.color.black_3));
				status.setOnClickListener(null);
				switch (mVoteStatus) {
				case WIN_MUST_ADD_ONE_EVIDENCE:
					if (mEvidencePacketList.isEmpty()) {
						status.setText("至少上传一次证据");
					} else {
						status.setText("发布众投");
						status.setBackgroundResource(R.drawable.circle5_3);
						status.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
						status.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								// 发布众投
								mHttpRequest.publishVote(mVoteId);
//								voteMediaView.createRoom();
							}
						});
					}
					status.setVisibility(View.VISIBLE);
					break;
				case WIN_UNDER_REVIEWING:
					status.setText("平台审核中...");
					status.setVisibility(View.VISIBLE);
					break;
				case WIN_APPLICATION_FAILED:
					status.setText("审核未通过");
					status.setVisibility(View.VISIBLE);
					visibleView.setVisibility(View.GONE);
					break;
				default:
					// ignore
				}
			}
		}

		if (mVoteStatus == WIN_BUYER || mVoteStatus == WIN_SELLER) {
			if (visibleView != null) {
				visibleView.setVisibility(View.GONE);
			}
		}
	}

	private void showLiveVideoView() {
		ObjectAnimator animator = ObjectAnimator.ofInt(mRootView, "scrollY", mRootView.getScrollY(), -mLiveViewHeight);
		animator.setDuration(500);
		animator.setInterpolator(new AccelerateInterpolator());
		animator.start();
	}

	private void showFollowWindow(String userId, String nickName, String imgUrl) {
		// 显示关注窗口
		PopWindowUtils.followPopUpWindow(getActivity(), userId, nickName, imgUrl, 1);
	}

	private void hideLiveVideoView() {
		if (mLiveViewHeight <= 0)
			mLiveViewHeight = voteMediaView.getHeight();
		ObjectAnimator animator = ObjectAnimator.ofInt(mRootView, "scrollY", mRootView.getScrollY(), 0);
		animator.setDuration(500);
		animator.setInterpolator(new AccelerateInterpolator());
		animator.start();
	}

	/* 设置是否是众投页 */
	private void setIsPublicVotePage(boolean isPublicPage) {
		mIsPublicPage = isPublicPage;
		View publicVote = mTitleCheckLayout.findViewById(R.id.tv_public_vote);
		View commentsTab = mTitleCheckLayout.findViewById(R.id.tv_comments);
		publicVote.setBackgroundResource(
				mIsPublicPage ? R.drawable.bg_tab_left_select : R.drawable.bg_tab_left_non_select);
		commentsTab.setBackgroundResource(
				mIsPublicPage ? R.drawable.bg_tab_right_non_select : R.drawable.bg_tab_right_select);

		int visibility = mIsPublicPage ? View.VISIBLE : View.GONE;
		mEvidenceListView.setVisibility(visibility);
		/*
		 * mLeftLine.setVisibility(visibility);
		 * mRightLine.setVisibility(visibility);
		 */

		int otherVisibility = mIsPublicPage ? View.GONE : View.VISIBLE;
		mFragmentContainer.setVisibility(otherVisibility);
		if (!isPublicPage) {
			if (mCommentsHelper == null) {
				mCommentsHelper = new PublicVoteCommentsHelper(getActivity(), mCommentListView, mCommentsSendBtn,
						mCommentsEditText);
				mCommentsHelper.setInfo(mVoteId, mUserId);
			}
			mCommentsHelper.scrollToStart();
		}
	}

	private void initOrder() {
		View headerView = mHeaderView;
		mVoteStartTimeTextView = (TextView) headerView.findViewById(R.id.include_timer).findViewById(R.id.tv_time);
		mOrderImageView = (ImageView) headerView.findViewById(R.id.iv_order_info);
		mOrderLayout = headerView.findViewById(R.id.rl_order_layout);
		TextView name, destination, totalPrice, compensate, introduction;
		name = (TextView) headerView.findViewById(R.id.tv_order_name);
		destination = (TextView) headerView.findViewById(R.id.tv_order_destination);
		totalPrice = (TextView) headerView.findViewById(R.id.tv_total_price);
		compensate = (TextView) headerView.findViewById(R.id.tv_compensate);
		introduction = (TextView) headerView.findViewById(R.id.tv_introduction);

		mVoteStartTimeTextView.setText(mOrderCreateTime);
		if (mGoodsOrderBean != null && !TextUtils.isEmpty(mGoodsOrderBean.getmGoodsBasicInfoBean().getGoodsTitle())) {
			name.setText(mGoodsOrderBean.getmGoodsBasicInfoBean().getGoodsTitle());
			destination.setText("出发地: " + mGoodsOrderBean.getmGoodsBasicInfoBean().getGoodsAddress());
			totalPrice.setText("总价:" + mGoodsOrderBean.getmOrdersBasicInfoBean().getPaymentPrice() + "元");
			ImageDisplayTools.displayImage(mGoodsOrderBean.getmGoodsBasicInfoBean().getGoodsImg(), mOrderImageView);
		}
		compensate.setText("要求赔付: " + mVoteClaimCount + "元");
		if (TextUtils.isEmpty(mVoteReason)) {
			introduction.setVisibility(View.GONE);
		} else {
			introduction.setVisibility(View.VISIBLE);
		}
		introduction.setText(mVoteReason);
	}

	private void initHeaders() {
		ImageDisplayTools.displayHeadImage(mBuyerHeaderImage, mLeftHeaderImage);
		ImageDisplayTools.displayHeadImage(mSellerHeaderImage, mRightHeaderImage);
		mLeftHeaderImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showFollowWindow(mBuyerId, mBuyerNickName, mBuyerHeaderImage);
			}
		});
		mRightHeaderImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showFollowWindow(mSellerId, mSellerNickName, mSellerHeaderImage);
			}
		});
	}

	private View.OnClickListener mUploadEvidenceListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			MLog.v(TAG, "证据上传");
			Bundle bundle = new Bundle();
			bundle.putInt(BUNDLE_VOTE_ID, mVoteId);
			bundle.putBoolean("is_seller", isSeller());
			OneFragmentActivity.startNewActivity(getActivity(), "选择证据", EvidenceSelectFragment.class, bundle);
		}
	};

	@Override
	public void onEvidencePacketClick(EvidencePacket evidencePacket, int position) {
		LocalFile localFile = evidencePacket.getMultipleMediaList().get(position);
		switch (localFile.getType()) {
		case CameraFragment.TYPE_PHOTO:
			if (mImageViewPopupWindow == null)
				mImageViewPopupWindow = new ImageViewPopupWindow();
			mImageViewPopupWindow.show(getActivity(), mOrderImageView, localFile.getRemotePath());
			break;
		case CameraFragment.TYPE_AUDIO:
			if (mRecorderPlayerDialog != null)
				return;
			mRecorderPlayerDialog = new RecorderPlayerDialog(getActivity(), localFile.getRemotePath(),
					evidencePacket.isLeft() ? mBuyerHeaderImage : mSellerHeaderImage);
			mRecorderPlayerDialog.show();
			mRecorderPlayerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					mRecorderPlayerDialog = null;
				}
			});
			break;
		case CameraFragment.TYPE_VIDEO:
			if (mVideoViewPopWindow == null) {
				mVideoViewPopWindow = new VideoViewPopWindow();
				mVideoView = new VideoView(getActivity());
			}
			if (mVideoView instanceof IjkVideoView) {
				((IjkVideoView) mVideoView).stopPlayback();
				mVideoView = new VideoView(getActivity());
			}
			((VideoView) mVideoView).setVideoPath(localFile.getRemotePath());
			mVideoView.start();
			mVideoViewPopWindow.show(getActivity(), mOrderImageView, mVideoView);
			break;
		case CameraFragment.TYPE_LIVE:
			if (mVideoViewPopWindow == null) {
				mVideoViewPopWindow = new VideoViewPopWindow();
				mVideoView = new IjkVideoView(getActivity());
			}
			if (mVideoView instanceof VideoView) {
				((VideoView) mVideoView).stopPlayback();
				mVideoView = new IjkVideoView(getActivity());
			}
			((IjkVideoView) mVideoView).setVideoPath(localFile.getRemotePath());
			mVideoView.start();
			mVideoViewPopWindow.show(getActivity(), mOrderImageView, mVideoView);
			break;
		}
	}

	@Override
	public boolean onBackPressed() {
		if (mVideoViewPopWindow != null && mVideoViewPopWindow.isShowing()) {
			mVideoViewPopWindow.dismiss();
			if (mVideoView instanceof VideoView) {
				((VideoView) mVideoView).stopPlayback();
			} else {
				((IjkVideoView) mVideoView).stopPlayback();
			}
			return true;
		}
		if (mImageViewPopupWindow != null && mImageViewPopupWindow.isShowing()) {
			mImageViewPopupWindow.dismissPopupWindow();
			return true;
		}

		if (mRecorderPlayerDialog != null && mRecorderPlayerDialog.isShowing()) {
			mRecorderPlayerDialog.dismiss();
			return true;
		}

		if (mGotoHomeActivity) {
			Intent intent = new Intent(getActivity(), HomeActivity.class);
			intent.putExtra("position", HomeActivity.PUBLIC_INVERSTMENT);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			getActivity().finish();
			return true;
		}
		return false;
	}

	@Override
	public void onTouchDown() {
		// nothing to do
	}

	private class HttpRequest {

		private Context context;

		public HttpRequest() {
			context = getActivity();
		}

		/** 获取证据列表数据 */
		public void getVoteDataList(int voteId) {
			String url = Constants.Root_Url + "/VoteData/voteDataList.do";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("publicVoteId", voteId);
			NetWorkUtil.postForm(context, url, new MResponseListener() {

				@Override
				protected void onDataFine(JSONArray data) {
					if (data == null)
						return;
					new AsyncTask<JSONArray, Void, List<EvidencePacket>>() {

						/**
						 * {"id":54,"publicVoteId":17,"userid":9,"reason":"视频地址"
						 * ,"createTime":"2016-07-19","status":0,
						 * "dataArray":[{"type":"video","content":
						 * "http:\/\/hltravel.oss-cn-hangzhou.aliyuncs.com\/2016\/7\/19\/76172856.mp4",
						 * "cover":
						 * "http:\/\/hltravel.img-cn-hangzhou.aliyuncs.com\/2016\/7\/19\/76172857.jpg"}
						 */
						@Override
						protected List<EvidencePacket> doInBackground(JSONArray... params) {
							JSONArray jsonArray = params[0];
							List<EvidencePacket> evidencePackets = new ArrayList<EvidencePacket>();
							for (int i = 0, length = jsonArray.length(); i < length; i++) {

								JSONObject jsonObject = JsonUtil.getJSONObject(jsonArray, i);
								EvidencePacket evi = EvidencePacket.generateFromJSONObject(jsonObject, mBuyerId,
										mSellerId);
								int packetUserId = JsonUtil.getJsonInt(jsonObject, "userid");
								evi.setIsLeft(mBuyerId.equals(String.valueOf(packetUserId)));
								evidencePackets.add(evi);
							}
							return evidencePackets;
						}

						@Override
						protected void onPostExecute(List<EvidencePacket> evidencePackets) {
							updateEvidenceList(evidencePackets);
						}
					}.execute(data);
				}
			}, map);
		}

		/**
		 * 获取众投详情
		 */
		public void getPublicVoteDetail(int publicVoteId) {
			String url = Constants.Root_Url + "/publicVote/detailPublicVote.do";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", publicVoteId);
			NetWorkUtil.postForm(context, url, new MResponseListener() {
				@Override
				protected void onDataFine(JSONObject data) {
					if (data == null)
						return;
					long orderId = JsonUtil.getJsonLong(data, "ordersId");
					// int sellerId = JsonUtil.getJsonInt(data, "sellerId");
					// int buyerId = JsonUtil.getJsonInt(data, "buyerId");
					double claimAmount = JsonUtil.getJsonDouble(data, "claimAmount");
					String reason = JsonUtil.getJson(data, "reason");
					String createTime = JsonUtil.getJson(data, "createTime");
					int buyerPoll = JsonUtil.getJsonInt(data, "buyerPoll");
					int sellerPoll = JsonUtil.getJsonInt(data, "sellerPoll");
					double totalPrice = JsonUtil.getJsonDouble(data, "totalMoney");
					long snapshotId = JsonUtil.getJsonLong(data, "snapshotId");
					int voteRedCoin = JsonUtil.getJsonInt(data, "voteRedCoin");
					int status = JsonUtil.getJsonInt(data, "status");
					int checkStatus = JsonUtil.getJsonInt(data, "checkStatus");
					String victory = JsonUtil.getJson(data, "victory");
					try {
						UserData buyer = UserData.generateUserData((JSONObject) data.get("buyer"));
						if (buyer != null) {
							buyer.setId(JsonUtil.getJson(data, "buyerId"));
						}
						UserData seller = UserData.generateUserData((JSONObject) data.get("seller"));
						if (seller != null) {
							seller.setId(JsonUtil.getJson(data, "sellerId"));
						}

						GoodsOrderBean goodsOrderBean = new GoodsOrderBean();
						OrdersBasicInfoBean ordersBasicInfoBean = new OrdersBasicInfoBean();
						ordersBasicInfoBean.setOrdersId(orderId);
						ordersBasicInfoBean.setTotalPrice((float) totalPrice);
						ordersBasicInfoBean.setGoodsSnapshootId(String.valueOf(snapshotId));
						goodsOrderBean.setmOrdersBasicInfoBean(ordersBasicInfoBean);
						onVoteDetailGot(buyer, seller, goodsOrderBean, createTime, reason, claimAmount, buyerPoll,
								sellerPoll, voteRedCoin, status, checkStatus, victory);
					} catch (JSONException e) {
						MLog.e(TAG, e.getMessage(), e);
					}
				}
			}, map);
		}

		/* 获取本人是否已经投过票 */
		public void getIsVoted(int voteId) {
			String url = Constants.Root_Url + "/orders/isVote.do";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("publicVoteId", voteId);
			NetWorkUtil.postForm(context, url, new MResponseListener() {
				@Override
				public void onResponse(JSONObject response) {
					MLog.v(TAG, "onResponse, and response is " + response);
					int error = JsonUtil.getJsonInt(response, "error");
					if (error != 0)
						return;
					String msg = JsonUtil.getJson(response, "msg");
					if (TextUtils.isEmpty(msg) || !msg.toUpperCase().equals("OK"))
						return;

					int result = JsonUtil.getJsonInt(response, "data");
					if (result == 1)
						mVoteFlags |= M_HAS_VOTED;
					mVoteFlags |= THE_RESULT_OF_VOTED_HAS_GOT;
				}
			}, map);
		}

		public void getOrderInfo(long orderId) {
			OrderInfoHttp.getOrdersInfoById(orderId, Constants.Root_Url + "/public/ordersDetails.do",
					context, new OrderInfoHttp.Listener() {
				@Override
				public void onOrderDataFine(GoodsOrderBean goodsOrderBean) {
					mGoodsOrderBean = goodsOrderBean;
					refreshAllUI();
					mOrderLayout.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
//							// 跳往商品详情页
//							Intent intent = new Intent(context, NewGoodsInfoActivity.class);
//							intent.putExtra("goodsId", String.valueOf(mGoodsOrderBean.getmGoodsBasicInfoBean().getGoodsId()));
//							intent.putExtra("goodsSnapshootId", String.valueOf(mGoodsOrderBean.getmOrdersBasicInfoBean().getGoodsSnapshootId()));
//							startActivity(intent);
						}
					});
				}

				@Override
				public void onErrorNotZero(int error, String msg) {
					Toast.makeText(context, "出现问题啦....", Toast.LENGTH_SHORT).show();
				}

						@Override
						public void onAttachGoodsGot(ArrayList<AttachGoodsBean> attachGoods) {

						}
					});
		}

		/**
		 * 投票
		 * 
		 * @param voteId
		 *            众投Id
		 * @param supportUserId
		 *            你支持的用户的Id
		 */
		public void vote(int voteId, String supportUserId) {
			String url = Constants.Root_Url + "/orders/addVote.do";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("publicVoteId", voteId);
			map.put("selectedId", supportUserId);
			NetWorkUtil.postForm(context, url, new MResponseListener() {
				@Override
				public void onResponse(JSONObject response) {
					super.onResponse(response);
					if (response.optInt("error") == 0) {
						Toast.makeText(context, "投票成功", Toast.LENGTH_SHORT).show();
						mVoteFlags |= M_HAS_VOTED;
						mSupportWhoPartHelper.stateVoting(mVoteFlags);
					}
				}
			}, map);
		}

		/**
		 * 众投发表
		 */
		public void publishVote(int voteId) {
			String url = Constants.Root_Url + "/publicVote/publish.do";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("publicVoteId", voteId);
			NetWorkUtil.postForm(context, url, new MResponseListener() {
				@Override
				public void onResponse(JSONObject response) {
					super.onResponse(response);
					if(response.optInt("error") == 0){
						onPublishVoteResult(true);
					}
				}

				@Override
				protected void onErrorNotZero(int error, String msg) {
					onPublishVoteResult(false);
				}

				@Override
				public void onErrorResponse(VolleyError error) {
					onPublishVoteResult(false);
				}
			}, map);
		}

		/** 网络回调区 */
		private void updateEvidenceList(List<EvidencePacket> list) {
			if (mEvidenceListView != null) {
				mEvidenceListView.postDelayed(new Runnable() {
					@Override
					public void run() {
						mEvidenceListView.onRefreshComplete();
					}
				}, 1000);
			}
			mEvidencePacketList.clear();
			mEvidencePacketList.addAll(list);
			mAdapter.notifyDataSetChanged();
/*			// 如果没有上传证据,则只有上传证据选项, 否则拥有直播选项
			if (mMiddleLine == null)
				return;
			if (mEvidencePacketList.size() == 0 || !mCanShowLiveVideo) {
				mMiddleLine.setVisibility(View.GONE);
				mGotoLive.setVisibility(View.GONE);
			} else if (mVoteStatus == WIN_VOTING ){
				mMiddleLine.setVisibility(View.VISIBLE);
				mGotoLive.setVisibility(View.VISIBLE);
			}*/
		}

		/* 发布众投申请结果返回 */
		private void onPublishVoteResult(boolean isSuccess) {
			if (isSuccess) {
				mVoteStatus = WIN_UNDER_REVIEWING;
				refreshAllUI();
			}
		}

		private void onVoteDetailGot(UserData buyer, UserData seller, GoodsOrderBean goodsOrderBean, String createTime, String reason,
				double claimAmount, int buyerPoll, int sellPoll, int voteRedCoin,
				int showStatus, int checkStatus, String victory) {
			mGoodsOrderBean = goodsOrderBean;
			mOrderCreateTime = createTime;
			mVoteReason = reason;
			mVoteClaimCount = claimAmount;
			mBuyerId = buyer.getId();
			mBuyerHeaderImage = buyer.getImgUrl();
			mBuyerNickName = buyer.getNickName();
			mSellerId = seller.getId();
			mSellerHeaderImage = seller.getImgUrl();
			mSellerNickName = seller.getNickName();
			mNumOfSupportBuyer = buyerPoll;
			mNumOfSupportSeller = sellPoll;
			mVoteRedCoin = voteRedCoin;
			mVoteStatus = getVoteStatus(showStatus, checkStatus, mSellerId, mBuyerId,victory);
			refreshAllUI();
			mHttpRequest.getOrderInfo(mGoodsOrderBean.getmOrdersBasicInfoBean().getOrdersId());
			MLog.e(TAG, "初始化数据");
			// 根据用户类型以及用户Id判断
			// voteMediaView.initShowLayout(mVoteId, mBuyerId+"", mSellerId+"");
			voteMediaView.initData(mBuyerNickName, mBuyerHeaderImage, mSellerNickName, mSellerHeaderImage);
			mVoteFlags |= THE_RESULT_OF_VOTED_HAS_GOT;
		}
	}

	/* 根据已有信息获取众投中的信息码 */
	public static int getVoteStatus(int showStatus, int checkStatus, String sellerId, String buyerId, String victory) {
		int resultStatus = 0;
		switch (showStatus) {
		case 0:
			resultStatus = WIN_MUST_ADD_ONE_EVIDENCE;
			break;
		case 1:
			switch (checkStatus) {
			case 0:
			case 1:
				resultStatus = WIN_UNDER_REVIEWING;
				break;
			case 2:
				resultStatus = WIN_APPLICATION_FAILED;
				break;
			}
			break;
		case 2:
			resultStatus = WIN_VOTING;
			break;
		case 3:
			if (sellerId.equals(victory)){
				resultStatus = WIN_SELLER;
			}else if (buyerId.equals(victory)){
				resultStatus = WIN_BUYER;
			}
			break;
		}
		return resultStatus;
	}

	/**
	 * 支持谁的部分
	 */
	private class SupportWhoPartHelper {
		private boolean mViewFound = false;

		private View mRootView;
		private TextView mSupportBuyerTextView, mSupportSellerTextView;
		private TextView mNumOfSupportBuyerTextView, mNumOfSupportSellerTextView;
		private View mBuyerWinView, mSellerWinView;

		public void initView(View rootView) {
			mRootView = rootView;
			mSupportBuyerTextView = (TextView) rootView.findViewById(R.id.tv_support_buyer);
			mSupportSellerTextView = (TextView) rootView.findViewById(R.id.tv_support_seller);
			mNumOfSupportBuyerTextView = (TextView) rootView.findViewById(R.id.tv_num_of_support_buyer);
			mNumOfSupportSellerTextView = (TextView) rootView.findViewById(R.id.tv_num_of_support_seller);
			mBuyerWinView = rootView.findViewById(R.id.tv_buyer_win);
			mSellerWinView = rootView.findViewById(R.id.tv_seller_win);
			mViewFound = true;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				// 编译时忽略该异常 zhx
				mSupportBuyerTextView.setTranslationZ(4);
				mSupportSellerTextView.setTranslationZ(4);
			}
		}

		/*
		 * 投票进行中... 只显示两个投票按钮
		 */
		public void stateVoting(int voteFlags) {
			if ((voteFlags & THE_RESULT_OF_VOTED_HAS_GOT) == 0)
				return;
			mRootView.setVisibility(View.VISIBLE);
			mNumOfSupportBuyerTextView.setVisibility(View.INVISIBLE);
			mNumOfSupportSellerTextView.setVisibility(View.INVISIBLE);
			mBuyerWinView.setVisibility(View.INVISIBLE);
			mSellerWinView.setVisibility(View.INVISIBLE);
			if((voteFlags & M_HAS_VOTED) == 0){
				mSupportBuyerTextView.setEnabled(true);
				mSupportSellerTextView.setEnabled(true);

				mSupportBuyerTextView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickSupport(true);
					}
				});
				mSupportSellerTextView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickSupport(false);
					}
				});
			}
		}

		/**
		 * 投票结束 显示票数与胜利方
		 */
		public void stateVoteOver(boolean isBuyerWin, boolean isSellerWin) {
			mRootView.setVisibility(View.VISIBLE);
			mSupportSellerTextView.setEnabled(false);
			mSupportBuyerTextView.setEnabled(false);
			mBuyerWinView.setVisibility(isBuyerWin ? View.VISIBLE : View.INVISIBLE);
			mSellerWinView.setVisibility(isSellerWin ? View.VISIBLE : View.INVISIBLE);
			mNumOfSupportBuyerTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					gotoVoteDetailPage(true);
				}
			});
			mNumOfSupportSellerTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					gotoVoteDetailPage(false);
				}
			});
		}

		/**
		 * 设置支持数
		 * 
		 * @param numOfSupportBuyer
		 *            支持买家的票数
		 * @param numOfSupportSeller
		 *            支持卖家的票数
		 */
		@SuppressLint("DefaultLocale")
		public void setSupportNum(int numOfSupportBuyer, int numOfSupportSeller) {
			mNumOfSupportBuyerTextView.setText(String.format("%d票", numOfSupportBuyer));
			mNumOfSupportSellerTextView.setText(String.format("%d票", numOfSupportSeller));
		}

		// =======================> 此类中用到操作父类的方法 >>>>>>>>>>>>>>>>>>>>>>>>>
		// true -- 支持买家
		private void onClickSupport(final boolean isBuyer) {
			if ((mVoteFlags & M_HAS_VOTED) != 0) {
				Toast.makeText(getActivity(), "您已经投过票了\n请不要重复投票", Toast.LENGTH_SHORT).show();
				return;
			}
			mHttpRequest.vote(mVoteId, isBuyer ? mBuyerId : mSellerId);
		}

		private void gotoVoteDetailPage(boolean isBuyer) {
			Bundle bundle = new Bundle();
			bundle.putInt(VoteDetailFragment.BUNDLE_VOTE_ID, mVoteId);
			bundle.putString(VoteDetailFragment.BUNDLE_USER_ID, isBuyer ? mBuyerId : mSellerId);
			OneFragmentActivity.startNewActivity(getActivity(), "投票详情", VoteDetailFragment.class, bundle);
		}
	}
}
