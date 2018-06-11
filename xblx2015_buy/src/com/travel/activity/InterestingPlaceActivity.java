/*
package com.travel.activity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.chatserver.client.SubscriberKeys;
import com.travel.chatserver.client.SubscriberTypes;
import com.travel.communication.MessageServiceAIDLNOException;
import com.travel.communication.callbackimpl.GroupCallBack;
import com.travel.communication.callbackimpl.GroupCallBackInteface;
import com.travel.communication.entity.MessageEntity;
import com.travel.communication.entity.UserData;
import com.travel.communication.fragment.ChatFragment;
import com.travel.communication.helper.SQliteHelper;
import com.travel.communication.helper.SendCommandHelper;
import com.travel.communication.service.MessageService;
import com.travel.communication.service.aidl.MessageServiceAIDL.Stub;
import com.travel.entity.ScenicLiveEntity;
import com.travel.fragment.InterestingPlaceIntroductionFragment;
import com.travel.fragment.ScenicSpecialtyFragment;
import com.travel.layout.DialogTemplet;
import com.travel.layout.DialogTemplet.DialogLeftButtonListener;
import com.travel.layout.DialogTemplet.DialogRightButtonListener;
import com.travel.layout.HorizontalListView;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.utils.InterestingPlaceHttpNetHelper;
import com.travel.video.fragment.LiveHomePageFragment;
import com.travel.video.layout.VideoViewPopWindow;
import com.travel.widget.FragmentWithTitleContainer;
import com.travel.widget.LiveCollectView;
import com.travel.widget.ViewPagerWithNavigation;
import com.travel.widget.ViewPagerWithNavigation.ImageUrlEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.player.media.IjkVideoView;

*/
/**
 * 景区界面,
 * 包含视频的横屏与竖屏, 此类中主要操作竖屏的UI与所有数据与交互
 *//*

public class InterestingPlaceActivity extends TitleBarBaseActivity 
	implements InterestingFakeLandacapeActivityInterface, GroupCallBackInteface{
	private static final String TAG = "InterestingPlaceActivity";
	
	public static final String ROOM_NUM = "room_number";
	public static final String ROOM_NAME = "room_name";
	public static final String FOLLOW_NUM = "follow_number";
	// 房间号码(用于socket通信), 房间名, 关注人数, 房间Id(用于http通信)
	private String mRoomName, mRoomNum, mScenicId;
	private String mFollowNum;
	private String mLiveId;
	private int mCurrentLiveNumber = 0; // 当前所在的视频屏数
	// 是否关注, 是否是黑名单
	private boolean mIsFollowed, mIsBlackUser;

	// 是否调理了本界面返回
	private boolean mIsLeaved = false;

	private TextView mTitleText, mFollowNumTextView;
	private TextView mFollowsText;

	// 播放器轮滚图部分的UI部分
	// 左右操作按钮, 播放按钮, 全屏按钮, 头像
	private ImageView mFullScreenBtn, mHeaderImage;
	// 内容描述, 讲解者
	private TextView mDesc, mExplainerText;
	private ViewGroup mUpContainer, mExplainerLayout,  mPlayerContainer;
	private ViewPagerWithNavigation mViewPagerWithNavigation; // 图片轮滚图
	private HorizontalListView mCoversHListView; // 封面的缩略图
	// 景区视频播放器, 解说者播放器
	private IjkVideoView mVideoViewScenic, mVideoViewExplainer;
	// 录像器
	private LiveCollectView mLiveCollectView;

    // Fragment容器
    private FragmentWithTitleContainer mFragmentContainer;
    private FrameLayout mFragmentLayout;
	private Class<?>[] mFragmentClass =
			new Class<?>[]{InterestingPlaceIntroductionFragment.class, ChatFragment.class,
					LiveHomePageFragment.class, ScenicSpecialtyFragment.class};
	private Fragment[] mFragments = new Fragment[4];
	private ChatFragment mChatFragment;

	// 辅助类, http部分的辅助类, 核心功能区
	private InterestingPlaceHttpNetHelper mHttpHelper;
	private SQliteHelper mSQLiteHelper;
	private SendCommandHelper mSendCommandHelper;
	private GroupCallBack<InterestingPlaceActivity> mGroupCallBack;
	private MessageServiceAIDLNOException mMessageServiceAIDLNOException;
	
	// 数据集合区 
	private List<ScenicLiveEntity> mScenicLiveList;    
	private Map<String, List<UserData>> mExplainerUserMap; //解说者列表, key是liveId, 排队列表, 不包含正在解说的人
	private List<UserData> mCurrentViewerList;   // 当前观众列表
	private String mUserId;
	private CoverImageAdapter mCoverImageAdapter;
	
	// 一个假的activity, 便于管理横屏情况下的UI
	private InterestingFakeLandacapeActivity<InterestingPlaceActivity> mLandacapeActivity;
	private Handler mHandler;
	*/
/**
	 * 打开此界面
	 *//*

	public static void startActivity(Context context, String roomNum, String roomName, String follow){
		Intent intent = new Intent(context, InterestingPlaceActivity.class);
		intent.putExtra(ROOM_NAME, roomName);
		intent.putExtra(ROOM_NUM, roomNum);
		intent.putExtra(FOLLOW_NUM, follow);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new Handler();
		initHelper();
		initDataSet();
		initIntentData();
		initChatFragment();
		mLandacapeActivity = new InterestingFakeLandacapeActivity<InterestingPlaceActivity>(this);
		mVideoViewScenic = new IjkVideoView(this);
		portraitOnCreate();
		playerContainerAddView();
		bindService(new Intent(this, MessageService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
	}


	private void initChatFragment() {
		mChatFragment = new ChatFragment();
		mChatFragment.setChatInfo(mUserId,null, mRoomNum);
		mChatFragment.setSQLiteHelper(mSQLiteHelper);
		mFragments[1] = mChatFragment;
		mChatFragment.setPhotoListener(new ChatFragment.Listener() {
			@Override
			public void onMessageComing(MessageEntity messageEntity) {
				mLandacapeActivity.updateChatMessage();
			}

			@Override
			public void loadHistoryMessageFromNet() {
				mHttpHelper.getHistoryMessageList(mRoomNum, mSQLiteHelper);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(mIsLeaved){
			mIsLeaved = false;
			initIntentData();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mIsLeaved = true;
		mSendCommandHelper.changeRoom("home");
	}

	// 初始化数据集合
	private void initDataSet() {
		mScenicLiveList = new ArrayList<ScenicLiveEntity>();
		mExplainerUserMap = new HashMap<String, List<UserData>>();
		mCurrentViewerList = new ArrayList<UserData>();
	}

	// 初始化一些辅助类, 其顺序几乎没有先后
	private void initHelper() {
		mHttpHelper = new InterestingPlaceHttpNetHelper(this);
		mSQLiteHelper = new SQliteHelper(this);
		mSendCommandHelper = new SendCommandHelper();
		mGroupCallBack = new GroupCallBack<InterestingPlaceActivity>(this);
	}
	
	private void playerContainerAddView(){
		mPlayerContainer.addView(mVideoViewScenic, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	private void portraitOnCreate() {
		OSUtil.setFullScreen(this, false);
		setContentView(R.layout.activity_interesting_place);
		setTitleLayout();
		setTitleTextAndIsFollow();
		initView();
        initFragmentLayout();
		initViewPager();
		initFullScreenListener();
		initVideoViewListener();
		initExplainerLayout();
		initVideoState();
	}

    private void initFragmentLayout() {
		if(mFragmentContainer == null){
			// 首次创建时, 初始化FragmentContainer容器
			mFragmentContainer = new FragmentWithTitleContainer(this);
			mFragmentContainer.setTabTitle("介绍", "群聊", "视频", "特色商品");
			mFragmentContainer.setPhotoListener(getSupportFragmentManager(),
					new FragmentWithTitleContainer.Listener() {
				@Override
				public Fragment getItem(int position) {
					if(mFragments[position] == null){
						try {
							mFragments[position] = (Fragment) mFragmentClass[position].newInstance();
						}catch (Exception e){
							MLog.e(TAG, e.getMessage(), e);
						}
					}
					return mFragments[position];
				}
			});
		}else{
			((ViewGroup)(mFragmentContainer.getContainer().getParent()))
					.removeView(mFragmentContainer.getContainer());
		}

		mFragmentLayout.addView(mFragmentContainer.getContainer(),
				new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
		));
    }

    private void setTitleTextAndIsFollow() {
		// 设置标题
		mTitleText.setText(mRoomName);
		mFollowNumTextView.setText("(" + mFollowNum + "人)");
		if(mIsFollowed){
			mFollowsText.setText("已关注");
		}else{
			mFollowsText.setText("关注");
		}
	}

	private void initExplainerLayout() {
		UserData currentUserData = null;
		if(mScenicLiveList != null && !mScenicLiveList.isEmpty()) {
			ScenicLiveEntity entity = mScenicLiveList.get(mViewPagerWithNavigation.getCurrentItem());
			currentUserData = entity.getExplainerUser();
		}
		initExplainerLayout(currentUserData);
	}
	
	*/
/** 根据用户信息初始化解说者Layout *//*

	private void initExplainerLayout(UserData explainerData){
		if(explainerData == null || TextUtils.isEmpty(explainerData.getId())){
			mExplainerLayout.setVisibility(View.INVISIBLE);
			return;
		}else{
			mExplainerLayout.setVisibility(View.VISIBLE);
		}
		MLog.v(TAG, "explainer's imgUrl is " + explainerData.getImgUrl());
		ImageDisplayTools.displayHeadImage(explainerData.getImgUrl(), mHeaderImage);
	}

	private void initVideoViewListener() {
		mPlayerContainer.setClickable(true);
		mPlayerContainer.setOnTouchListener(new View.OnTouchListener() {

			private float originX;
			private boolean isEventFinished;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if(action == MotionEvent.ACTION_DOWN){
					originX = event.getX();
					isEventFinished = false;
				}
				if(action != MotionEvent.ACTION_UP || isEventFinished){
					mViewPagerWithNavigation.dispatchTouchEvent(event);
				}
				if(isEventFinished) return true;
				switch (action){
					case MotionEvent.ACTION_MOVE:
						float deltaX = event.getX() - originX;
						if(Math.abs(deltaX) > 20){
							MLog.v(TAG, "action move");
							setVideoVisibility(View.INVISIBLE);
							isEventFinished = true;
						}
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						MLog.v(TAG, "mVideoViewScenic click");
						mVideoViewScenic.suspend();
						if(mVideoViewExplainer != null) mVideoViewExplainer.suspend();
						setVideoVisibility(View.GONE);
						break;
				}
				return true;
			}
		});
	}

	private void initFullScreenListener() {
		mFullScreenBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 全屏
				MLog.v(TAG, "full screen btn was clicked");
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
		});
	}

	private void initViewPager() {
		if(mCoverImageAdapter == null){
			mCoverImageAdapter = new CoverImageAdapter();
		}
		mCoversHListView.setAdapter(mCoverImageAdapter);
		mViewPagerWithNavigation.setOnItemClickListener(new ViewPagerWithNavigation.OnItemClickListener() {
			
			@Override
			public void onItemClick(ViewPagerWithNavigation viewPagerWithNavigation, int position,
					ImageUrlEntity imageUrlEntity) {
				final ScenicLiveEntity scenicLiveEntity = (ScenicLiveEntity) imageUrlEntity;
				final String videoUrl = scenicLiveEntity.getVideoUrl();
				MLog.v(TAG, "the video url is " + videoUrl);
				if(TextUtils.isEmpty(videoUrl)){
					showToast("视频流出现问题, 请稍后重试");
					return;
				}

				AlertDialogUtils.runNeedWifiOperation(InterestingPlaceActivity.this, new Runnable(){

					@Override
					public void run() {
						mVideoViewScenic.setVideoPath(videoUrl);
						mVideoViewScenic.start();
						setVideoVisibility(View.VISIBLE);
						if(scenicLiveEntity.getExplainerUser() == null){
							MLog.v(TAG, "explainerUser is null");
							if(mVideoViewExplainer != null){
								mVideoViewExplainer.stopPlayback();
								mVideoViewExplainer = null;
							}
							return;
						}
						if(!TextUtils.isEmpty(scenicLiveEntity.getExplainLiveUrl())){
							MLog.v(TAG, "explainer live url is " + scenicLiveEntity.getExplainLiveUrl());
							if(mVideoViewExplainer != null){
								mVideoViewExplainer.stopPlayback();
								mVideoViewExplainer = null;
							}
							mVideoViewExplainer = new IjkVideoView(InterestingPlaceActivity.this);
							mVideoViewExplainer.setVideoPath(scenicLiveEntity.getExplainLiveUrl());
							mVideoViewExplainer.start();
						}
					}
				});
			}
		});

		mCoversHListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mViewPagerWithNavigation.setCurrentItem(position);
			}
		});
		
		mViewPagerWithNavigation.setOnPagerChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				if(mScenicLiveList.size() == 0) return;
				if(mCurrentLiveNumber != position){
					stopVideo(mVideoViewScenic);
					stopVideo(mVideoViewExplainer);
				}
				mCurrentLiveNumber = position;
				ScenicLiveEntity entity = mScenicLiveList.get(position);
				mDesc.setText(entity.getName());
				mLiveId = String.valueOf(entity.getId());
				initExplainerLayout(entity.getExplainerUser());
				mSendCommandHelper.updateHelperInfo(mRoomNum, mLiveId, UserSharedPreference.getChatUserJson());
				//noinspection SuspiciousMethodCalls
				if(mExplainerUserMap.get(entity.getId()) == null){
					mHttpHelper.getExplainUserList(mRoomNum, mLiveId);
				}
				mCoverImageAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
	}
	// 根据播放器的状态, 判断可见性 
	private void initVideoState(){
		if(mVideoViewScenic == null) {
			setVideoVisibility(View.GONE);
			mFullScreenBtn.setVisibility(View.INVISIBLE);
			return;
		}
		
		if(mVideoViewScenic.isPlaying()){
			setVideoVisibility(View.VISIBLE);
			mFullScreenBtn.setVisibility(View.VISIBLE);
		}else{
			setVideoVisibility(View.GONE);
			mFullScreenBtn.setVisibility(View.GONE);
		}
		int currentLiveNumber = mCurrentLiveNumber;
		mViewPagerWithNavigation.setList(mScenicLiveList);
		if(mScenicLiveList != null && mScenicLiveList.size() > 0){
			mViewPagerWithNavigation.setCurrentItem(currentLiveNumber);
		}
	}
	
	private void setVideoVisibility(int visibility) {
		mPlayerContainer.setVisibility(visibility);
		mFullScreenBtn.setVisibility(visibility);
		int viewPagerVisibility;
		if(visibility == View.VISIBLE){
			viewPagerVisibility = View.GONE;
		}else{
			viewPagerVisibility = View.VISIBLE;
		}
		mViewPagerWithNavigation.setVisibility(viewPagerVisibility);
		mDesc.setVisibility(viewPagerVisibility);
		mCoversHListView.setVisibility(viewPagerVisibility);
	}
	
	@Override
	public void onImageClick(MessageEntity entity){
		mChatFragment.onImageClick(entity);
	}
	
	@Override
	public void onMessageClick(int position){
		mChatFragment.onMessageHeaderClick(position);
	}
	
	@Override
	public void onMessageHeaderClick(MessageEntity entity) {
		mChatFragment.onMessageHeaderClick(entity);
	}

	@Override
	public void showFollowPopWindow(UserData userData) {
		mChatFragment.showFollowPopWindow(userData);
	}

	private void initView(){
		mFullScreenBtn = findView(R.id.iv_fullscrenn);
		mHeaderImage = findView(R.id.iv_header);
		mDesc = findView(R.id.tv_desc);
		mExplainerText = findView(R.id.tv_explain_text);
		mUpContainer = findView(R.id.rl_container_up);
		mExplainerLayout = findView(R.id.ll_explain_layout);
		mViewPagerWithNavigation = findView(R.id.view_pager_with_navi);
		mPlayerContainer = findView(R.id.fl_player_container);
        mFragmentLayout = findView(R.id.fl_fragment_container);
		mCoversHListView = findView(R.id.hl_covers);
		setPlayerContainerHeight();
	}
	private void setPlayerContainerHeight() {
		LayoutParams params = mUpContainer.getLayoutParams();
		params.height = OSUtil.getScreenWidth() * 9 / 16;
		mUpContainer.setLayoutParams(params);
		MLog.v(TAG, "the container height is " + params.height);
	}

	private void initIntentData() {
		Intent intent = getIntent();
		mRoomNum = intent.getStringExtra(ROOM_NUM);
		mRoomName = intent.getStringExtra(ROOM_NAME);
		mFollowNum = intent.getStringExtra(FOLLOW_NUM);
		initMUserId();		
		if(TextUtils.isEmpty(mRoomNum) || TextUtils.isEmpty(mRoomName)){
			throw new IllegalArgumentException("roomId or roomName is null");
		}
		MLog.v(TAG, "enter room, and room name is " + mRoomNum + "mUserId is " + mUserId);
		mSendCommandHelper.changeRoom(mRoomNum);
	}

	public void initMUserId() {
		String tmpUserId = UserSharedPreference.getUserId();
		if(tmpUserId.equals(mUserId)) return;
		mUserId = tmpUserId;
		if(TextUtils.isEmpty(mUserId) || "-1".equals(mUserId)){
			mUserId = UserSharedPreference.getRandomUserId();
		}
		MessageEntity.mUserId = mUserId;
		mHttpHelper.getScenicInfo(mRoomNum, mUserId);
		mHttpHelper.getScenicUserList(mRoomNum);
		mSendCommandHelper.changeRoom(mRoomNum);
		if(mChatFragment != null){
			mChatFragment.setChatInfo(mUserId, null, mRoomNum);
		}
	}
	// 设置标题区
	private void setTitleLayout() {
		View viewRoot = getLayoutInflater().inflate(R.layout.title_bar_interesting_place, null);
		Button backBtn = (Button) viewRoot.findViewById(R.id.leftButton);
		mTitleText = (TextView) viewRoot.findViewById(R.id.tabTitle);
		mFollowsText = (TextView) viewRoot.findViewById(R.id.tv_follow);
		ImageView groupImage = (ImageView) viewRoot.findViewById(R.id.iv_group);
		mFollowNumTextView = (TextView) viewRoot.findViewById(R.id.tv_follow_num);
		addNewTitleLayout(viewRoot);
		
		backBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mFollowsText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!UserSharedPreference.isLogin()){
					// 未登录
					startActivity(new Intent(InterestingPlaceActivity.this, LoginActivity.class));
					return;
				}
				if("已关注".equals(mFollowsText.getText().toString().trim())){
					
					final DialogTemplet dialogTemplet = new DialogTemplet(InterestingPlaceActivity.this, false, "您确认要取消对群的关注？", "", "不取消", "确认");
					dialogTemplet.show();
					dialogTemplet.setRightClick(new DialogRightButtonListener() {
						
						@Override
						public void rightClick(View view) {
							int type = 2;
							mFollowsText.setText("关注");
							mHttpHelper.followAction(mUserId, mRoomNum, 2, type);
							dialogTemplet.dismiss();
						}
					});
					dialogTemplet.setLeftClick(new DialogLeftButtonListener() {
						
						@Override
						public void leftClick(View view) {
							dialogTemplet.dismiss();
						}
					});
				}else{
					if(!UserSharedPreference.isLogin()){
						showToast("未登陆用户无法进行关注操作, 请登录");
						return;
					}
					int type = 1;
					mFollowsText.setText("已关注");
					mHttpHelper.followAction(mUserId, mRoomNum, 2, type);
				}
			}
		});
		groupImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 显示群成员
				GroupMemberActivity.startActivity(InterestingPlaceActivity.this, mRoomNum);
			}
		});
	}


	private VideoViewPopWindow mVideoViewPopWindow;
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE 
				|| newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_USER){
			ScenicLiveEntity entity = getScenicLiveEntityByLiveId(mLiveId);
			if(entity.getLiveType() == ScenicLiveEntity.TYPE_ADVERTISING_VIDEO){
				OSUtil.setFullScreen(this, true);
				mVideoViewPopWindow = new VideoViewPopWindow();
				mVideoViewPopWindow.show(InterestingPlaceActivity.this, mUpContainer, mVideoViewScenic);
				return;
			}
			MLog.v(TAG, "onConfigurationChanged, and orientation is landascape");
			mPlayerContainer.removeView(mVideoViewScenic);
			mLandacapeActivity.onCreate(mVideoViewScenic, mVideoViewExplainer, mLiveCollectView);
			mLandacapeActivity.setViewerList(getCurrentUserList());
			// 是否是特权用户
			mLandacapeActivity.isPrivilegeUser = mUserId.equals(getScenicLiveEntityByLiveId(mLiveId).getPrivilegedUser());
		}else if(newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
			MLog.v(TAG, "onConfigurationChanged, and orientation is portrait");
			mLandacapeActivity.beforeConfigurationChanged();
			beforeChangedCancelExplainer();
			portraitOnCreate();
			playerContainerAddView();
		}else{
			MLog.v(TAG, "onConfiguration changed, and orientation is " + newConfig.orientation);
		}
	}
	
	private void beforeChangedCancelExplainer(){
		// 用户切回竖屏前, 停止排队
		List<UserData> explainers = mExplainerUserMap.get(mLiveId);
		if(explainers == null || explainers.size() == 0) return;
		for(int i = 0; i < explainers.size(); i++){
			UserData userData = explainers.get(i);
			if(mUserId.equals(userData.getId())){
				mSendCommandHelper.cancelApply();
				break;
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		if(mChatFragment.onBackPressed())return;

		if(!isPortrait()){
			if(mVideoViewPopWindow != null && mVideoViewPopWindow.isShowing()){
				mVideoViewPopWindow.dismiss();
			}
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}else{
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onDestroy() {
		MLog.v(TAG, "onDestroy");
		if(mSendCommandHelper != null){
			mSendCommandHelper.changeRoom("home");
		}
		stopVideo(mVideoViewScenic);
		stopVideo(mVideoViewExplainer);
		if(mLiveCollectView != null){
			if(mLiveCollectView.isPreviewing()){
				mSendCommandHelper.stopExplain();
			}
			mLiveCollectView.onDestory(this);
		}
		mServiceConnection.onServiceDisconnected(null);
		unbindService(mServiceConnection);
		super.onDestroy();
	}

	private void stopVideo(IjkVideoView ijkVideoView){
		if(ijkVideoView != null){
			ijkVideoView.stopPlayback();
		}
	}
	
	@Override
	public void onSendMessage(String message) {
		mChatFragment.onSendMessage(message);
	}

	@Override
	public void onPictureClick() {
		mChatFragment.onPictureClick();
	}
	
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mMessageServiceAIDLNOException = null;
			mMessageServiceAIDLNOException.unRegisterCallBack(String.valueOf(SubscriberTypes.ExplainProtocol));
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MLog.v(TAG, "onServiceConnected");
			mMessageServiceAIDLNOException = new MessageServiceAIDLNOException(Stub.asInterface(service));
			// 群讲解指令回调
			mMessageServiceAIDLNOException.registerCallBack(mGroupCallBack,
					String.valueOf(SubscriberTypes.ExplainProtocol),
					SubscriberKeys.getGroupExplainKeys(mRoomNum));
			mSendCommandHelper.setMessageService(mMessageServiceAIDLNOException);
		}
	};
	
	// =========== 网络回调区 ===============
	public void onFollowResult(boolean isFine){
		// 关注动作的结果, 
		MLog.v(TAG, "onFloowResult, and isFine is " + isFine);
		if("已关注".equals(mFollowsText.getText().toString().trim())){
			showToast("关注成功");
		}else{
			showToast("取消关注成功");
		}
	}
	public void onGetScenicInfoResult(String roomId, boolean isFollow, boolean isBlackUser){
		// 获取景区信息数据结果返回
		mScenicId = roomId;
		mIsFollowed = isFollow;
		mIsBlackUser = isBlackUser;
		MLog.v(TAG, "onGetScenicInfoResult, scenicId is %s, and list.size is %d"
				, mScenicId, mScenicLiveList.size());
		setTitleTextAndIsFollow();
		mHttpHelper.getScenicLiveList(mRoomNum, mUserId, mScenicId);

		LiveHomePageFragment liveHomePageFragment = (LiveHomePageFragment) mFragments[2];
		if(liveHomePageFragment == null){
			liveHomePageFragment = new LiveHomePageFragment();
			mFragments[2] = liveHomePageFragment;
		}
		ScenicSpecialtyFragment scenicSpecialtyFragment = (ScenicSpecialtyFragment) mFragments[3];
		if(scenicSpecialtyFragment == null){
			scenicSpecialtyFragment = new ScenicSpecialtyFragment();
			mFragments[3] = scenicSpecialtyFragment;
		}
		scenicSpecialtyFragment.setScenicId(mScenicId);
		*/
/*liveHomePageFragment.setSeachTerm(mScenicId,new LiveHomePageFragment.LeaveListener() {
			
			@Override
			public void leaveNotice(String type) {
				mIsLeaved = true;
			}
		});*//*

	}

	*/
/**
	 * 当获取景区的信息返回结果, 返回通知与介绍的url
	 * @param notice          公告
	 * @param introduce       介绍
     *//*

	public void onGetScenicInfoResult(String notice, String introduce){
		MLog.v(TAG, "onGetScenicInfoResult: notice is %s.", notice);
		InterestingPlaceIntroductionFragment introductionFragment;
		if(mFragments[0] == null){
			introductionFragment = new InterestingPlaceIntroductionFragment();
			mFragments[0] = introductionFragment;
		}else{
			introductionFragment = (InterestingPlaceIntroductionFragment) mFragments[0];
		}
		introductionFragment.setNoticeAndIntroduce(notice, introduce);
	}

	*/
/**
	 * 当景区直播监控聊表信息返回时
	 *//*

	public void onGetScenicLiveResult(List<ScenicLiveEntity> list){
		mScenicLiveList.clear();
		mScenicLiveList.addAll(list);
		mCoverImageAdapter.notifyDataSetChanged();
		mViewPagerWithNavigation.setList(mScenicLiveList);
		mViewPagerWithNavigation.setCurrentItem(0);
		initExplainerLayout();
	}
	*/
/**
	 * 当获取讲解队列列表
	 * @param liveId  背景视频id
	 *//*

	public void onGetExplainUserList(final String liveId, final List<UserData> list){
		runInMainThread(new Runnable() {
			@Override
			public void run() {
				mExplainerUserMap.put(liveId, list);
				if(isNotMineCommand(liveId)) return;
				mLandacapeActivity.updateExplainerListView(list);
			}
		});
	}
	*/
/**
	 * 获取群聊天用户信息
	 * @param list
	 *//*

	public void onGetScenicUserList(@NonNull List<UserData> list){
		MLog.v(TAG, "onGetScenicUserList, and list.length is " + list.size());
		mCurrentViewerList.clear();
		mCurrentViewerList.addAll(list);
	}
	
	private List<UserData> getCurrentUserList(){
		boolean containerMe = false;
		for(UserData userData : mCurrentViewerList){
			if(userData != null && userData.getId().equals(mUserId)){
				containerMe = true;
				break;
			}
		}
		List<UserData> result;
		if(!containerMe){
			result = new ArrayList<UserData>(mCurrentViewerList);
			result.add(getMUserData());
		}else{
			result = mCurrentViewerList;
		}
		return result;
	}

	public UserData getMUserData() {
		UserData userData = mSQLiteHelper.getUserData(mUserId);
		if(userData == null){
			userData = new UserData();
			userData.setId(mUserId);
			userData.setImgUrl(UserSharedPreference.getUserHeading());
			String nickName = UserSharedPreference.getNickName();
			nickName = TextUtils.isEmpty(nickName) ? "游客" : nickName;
			userData.setNickName(nickName);
			mSQLiteHelper.inserOrReplace(userData);
		}
		return userData;
	}
	
	public void onGetHistoryMessageList(List<MessageEntity> messageList){
		if(messageList == null) return;
		MLog.v(TAG, "onGetHistoryMessageList, adn messageList.size is " + messageList.size());
		mChatFragment.onLoadHistoryFromNet(messageList);
	}
	// =========== ^^网络回调区^^ ===============
	
	
	@Override
	public void updateVideoView(IjkVideoView videoScenic, IjkVideoView videoExplainer,
			LiveCollectView liveCollectView) {
		mVideoViewScenic = videoScenic;
		mVideoViewExplainer = videoExplainer;
		mLiveCollectView = liveCollectView;
	}
	
	// =================== 指令回调区 ===================================
	@Override
	public void onUserEnterGroup(String roomNum, UserData userData) {
		MLog.v(TAG, "onUserEnterGroup");
		mCurrentViewerList.add(userData);
		mLandacapeActivity.setViewerList(getCurrentUserList());
	}

	@Override
	public void onUserLeaveGroup(String roomNum, UserData userData) {
		MLog.v(TAG, "onUserLeaveGroup, and nickName is " + userData.getNickName());
		for(UserData tmp : mCurrentViewerList){
			if(tmp.getId().equals(userData.getId())){
				mCurrentViewerList.remove(tmp);
				break;
			}
		}
		mLandacapeActivity.setViewerList(getCurrentUserList());
	}

	@Override
	public void onPrepareExplain(final String roomNum, final String liveId) {
		MLog.v(TAG, "onPrepareExplain");
		runInMainThread(new Runnable() {
			@Override
			public void run() {
				mLandacapeActivity.onPrepareExplain();
				List<UserData> list = mExplainerUserMap.get(liveId);
				if(list == null || list.size() == 0){
					mHttpHelper.getExplainUserList(roomNum, liveId);
					return;
				}
				UserData firstData = list.get(0);
				if(mUserId.equals(firstData.getId())){
					list.remove(0);
					mLandacapeActivity.updateExplainerListView(list);
				}else{
					mHttpHelper.getExplainUserList(roomNum, liveId);
				}
			}
		});
	}

	@Override
	public void onPrepareEnd(String roomNum, String bgLiveId, int countTime) {
		MLog.v(TAG, "onPrepareEnd, and countTime is " + countTime);
		if(isNotMineCommand(bgLiveId)) return;
		if(isPortrait()){
			MLog.v(TAG, "I don't think you could come here");
		}else{
			mLandacapeActivity.onPrepareEnd(countTime);
		}
	}

	@Override
	public void onBeginExplain(String roomNum, final String bgLiveId, final String url, final UserData userData) {
		MLog.v(TAG, "onBeginExplain, and url is " + url);
		getScenicLiveEntityByLiveId(bgLiveId).setExplainerUser(userData);
		if(isNotMineCommand(bgLiveId)) return;
		runInMainThread(new Runnable() {
			@Override
			public void run() {
				if(isPortrait()){
					// 竖屏情况下
					if(mVideoViewExplainer == null){
						mVideoViewExplainer = new IjkVideoView(InterestingPlaceActivity.this);
					}
					mVideoViewExplainer.setVideoPath(url);
					mVideoViewExplainer.start();
					MLog.v(TAG, "start explain, portrait");
					initExplainerLayout();
				}else{
					// 横屏情况下
					mLandacapeActivity.changedExplainerVideo(url, userData);
				}

				List<UserData> explainers = mExplainerUserMap.get(bgLiveId);
				if(explainers == null
						|| explainers.size() == 0
						|| !explainers.get(0).getId().equals(userData.getId())){
					// 状态不正确, 重新获取数据
					mHttpHelper.getExplainUserList(mRoomNum, bgLiveId);
				}else{
					explainers.remove(0);
				}
				mLandacapeActivity.updateExplainerListView(explainers);
			}
		});
	}
	
	private boolean isPortrait(){
		int orientation = getRequestedOrientation();
		return orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || orientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
	}

	private boolean isNotMineCommand(String bgLiveId) {
		return mLiveId == null || !mLiveId.equals(bgLiveId);
	}

	@Override
	public void onPreviewFailed(String roomNum, String bgLiveId, String url) {
		MLog.v(TAG, "onPreviewFailed");
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				showToast("录制视频初始化失败, 请重试");
				mLandacapeActivity.onPreviewFailed();
			}
		});
		onCancelApply(mRoomNum, bgLiveId, getMUserData());
	}

	@Override
	public void onStopCountDown(String roomNum, String bgLiveId) {
		// 取消倒计时
		MLog.v(TAG, "onStopCountDown");
		mLandacapeActivity.stopCountDown();
	}
	
	@Override
	public void applyExplain(String roomNum, final String bgLiveId, final boolean isAppendTrail, final UserData userData) {
		runInMainThread(new Runnable() {
			@Override
			public void run() {
				MLog.v(TAG, "applyExplain, and userNick name is " + userData.getNickName());
				List<UserData> explainers = mExplainerUserMap.get(bgLiveId);
				if(explainers == null){
					explainers = new ArrayList<UserData>();
					mExplainerUserMap.put(bgLiveId, explainers);
				}
				for(UserData tmpUserData : explainers){
					if(userData.getId().equals(tmpUserData.getId())){
						explainers.remove(tmpUserData);
						break;
					}
				}
				if(!isAppendTrail){
					explainers.add(userData);
				}else{
					explainers.add(0, userData);
				}

				if(isNotMineCommand(bgLiveId)) return;
				if(!isPortrait()){
					// 横屏状态下
					MLog.v(TAG, "landascape, and need update explainer list");
					mLandacapeActivity.updateExplainerListView(explainers);
				}
			}
		});
	}

	@Override
	public void onCancelApply(String roomNum, final String bgLiveId, final UserData userData) {
		MLog.v(TAG, "onCancelApply");
		runInMainThread(new Runnable() {
			@Override
			public void run() {
				List<UserData> explainers = mExplainerUserMap.get(bgLiveId);
				if(explainers == null){
					MLog.e(TAG, "error, should fix up");
					explainers = new ArrayList<UserData>();
					mExplainerUserMap.put(bgLiveId, explainers);
				}
				for(UserData tmp : explainers){
					if(tmp.getId().equals(userData.getId())){
						explainers.remove(tmp);
						break;
					}
				}

				if(isNotMineCommand(bgLiveId)) return;
				if(!isPortrait()){
					MLog.v(TAG, "landascape, and need update explainer list");
					mLandacapeActivity.updateExplainerListView(explainers);
					for(UserData data : explainers){
						if(mUserId.equals(data.getId())){
							mLandacapeActivity.mCanApplyExplain = true;
						}
					}
				}
			}
		});
	}

	@Override
	public void onStopExplain(String roomNum, final String bgLiveId, UserData userData) {
		MLog.v(TAG, "onStopExplain, and userName is " + userData.getNickName());
		getScenicLiveEntityByLiveId(bgLiveId).setExplainerUser(null);
		if(isNotMineCommand(bgLiveId)) return;
		runInMainThread(new Runnable() {
			@Override
			public void run() {
				initExplainerLayout(getScenicLiveEntityByLiveId(bgLiveId).getExplainerUser());
			}
		});
		if(isPortrait()){
			// 竖屏
			MLog.e(TAG, "I don't think you could come here");
			if(mVideoViewExplainer != null){
				mVideoViewExplainer.stopPlayback();
				mVideoViewExplainer = null;
			}
		}else{
			// 横屏
			boolean isMine = false;
			if(mUserId.equals(userData.getId())){
				isMine = true;
			}
			mLandacapeActivity.onStopExplain(isMine);
		}
	}

	private void runInMainThread(Runnable runnable){
		if(Looper.getMainLooper().getThread() == Thread.currentThread()){
			runnable.run();
		}else{
			mHandler.post(runnable);
		}
	}
	
	// 根据背景视频Id获取当前的景区实体
	private ScenicLiveEntity getScenicLiveEntityByLiveId(String liveId){
		for(ScenicLiveEntity entity : mScenicLiveList){
			if(liveId.equals(String.valueOf(entity.getId()))){
				return entity;
			}
		}
		return null;
	}

	@Override
	public void onPraise(String roomNum, String bgLiveId, UserData userData) {
		MLog.v(TAG, "onPraise");
		ScenicLiveEntity scenicLiveEntity = getScenicLiveEntityByLiveId(bgLiveId);
		scenicLiveEntity.setPraiseNum(scenicLiveEntity.getPraiseNum() + 1);
		if(!isPortrait()){
			mLandacapeActivity.onPraise(roomNum, bgLiveId, userData);
		}
	}
	// ====================== ^^^指令回调区 ^^^=======================================

	@Override
	public List<MessageEntity> getMessageEntityList() {
		return mChatFragment.getMessageList();
	}

	@Override
	public SQliteHelper getSQliteHelper() {
		return mSQLiteHelper;
	}
	
	@Override
	public SendCommandHelper getSendCommandHelper() {
		return mSendCommandHelper;
	}

	@Override
	public String getLiveDIYParams() {
		return "Explain," + mRoomNum + "," + mLiveId;
	}
	
	@Override
	public UserData getExplainer() {
		return mScenicLiveList.get(mViewPagerWithNavigation.getCurrentItem()).getExplainerUser();
	}

	@Override
	public int getPraiseNum() {
		return getScenicLiveEntityByLiveId(mLiveId).getPraiseNum();
	}


	private class CoverImageAdapter extends BaseAdapter{
		private Drawable back50Alpha;

		@Override
		public int getCount() {
			return mScenicLiveList == null ? 0 : mScenicLiveList.size();
		}

		@Override
		public ScenicLiveEntity getItem(int position) {
			return mScenicLiveList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CoverViewHolder viewHolder;
			if(convertView == null){
				convertView = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.interesting_place_cover_list_item, parent, false);
				viewHolder = new CoverViewHolder();
				viewHolder.imageView = (ImageView) convertView.findViewById(R.id.iv_cover_item);
				viewHolder.frameLayout = (FrameLayout) convertView;
				convertView.setTag(viewHolder);
			}else{
				viewHolder = (CoverViewHolder) convertView.getTag();
			}
			if(mCurrentLiveNumber != position){
				if(back50Alpha == null){
					back50Alpha = new ColorDrawable(Color.BLACK);
					back50Alpha.setAlpha(128);
				}
				viewHolder.frameLayout.setForeground(back50Alpha);
			}else{
				viewHolder.frameLayout.setForeground(null);
			}
			String url = getItem(position).getImgUrl() + "@40w";
			ImageDisplayTools.displayImageEXACTLY(url, viewHolder.imageView);
			return convertView;
		}

		private class CoverViewHolder{
			FrameLayout frameLayout;
			ImageView imageView;
		}
	}
}
*/
