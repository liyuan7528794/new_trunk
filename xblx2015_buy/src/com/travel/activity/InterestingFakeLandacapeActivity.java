/*
package com.travel.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.adapter.ExplainerAdapter;
import com.travel.adapter.MessageAdapterLanscape;
import com.travel.communication.entity.MessageEntity;
import com.travel.communication.entity.UserData;
import com.travel.communication.view.ChatKeyboard;
import com.travel.communication.view.ChatKeyboard.ChatKeyboardListener;
import com.travel.layout.HorizontalListView;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.video.bean.XpaiCofig;
import com.travel.video.widget.VideoDecorationStartAnimationView;
import com.travel.widget.ImageHorizontalListView;
import com.travel.widget.LiveCollectView;
import com.travel.widget.LiveCollectView.LiveColleectViewListener;
import com.travel.widget.SupportView;
import com.travel.widget.SupportView.SupportViewClickListener;
import com.travel.widget.ViewWithDrag;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.media.IjkVideoView;

*/
/**
 * 景区界面群的横屏状态, 控制,
 * 这是一个假的activity, 只是为了方便代码的管理
 *//*

public class InterestingFakeLandacapeActivity<T extends Activity & InterestingFakeLandacapeActivityInterface>{
	private static final String TAG = "InterestingFakeLandacapeActivity";
	private T mActivity;
	
	// UI部分
	private FrameLayout mRootView, mPlayerContainer;
	private ImageView mCloseImage, mWriteImage,mShieldImage;
	private IjkVideoView mVideoViewScenic, mVideoViewExplainer;
	private LiveCollectView mLiveCollectView;
	private ChatKeyboard mChatKeyboard;
	private SupportView mSupportView;  // 点赞的view
	private View mExplainerLayout;     // 讲解的layout
	private ViewWithDrag mViewDragLayout; // 可拖动的视图层
	private ListView mChatMessageListView;
	private TextView mExplainerBtn;  // 说说按钮
	private HorizontalListView mExplainerListView;  // 讲解者列表
	private ImageView mExplainerImage;    // 讲解图标
	private ImageHorizontalListView mViewerListView;  // 当前观众列表
	private ImageView mExplainerImageHeader;
	
	private RelativeLayout mSmallWindowsDecorationLayout;
	
	private Handler mHandler;
	
	// 两个视频的位置状态, 具体状态值查看下面的常量
	private int mTwoVideoState = 0;
	private final static int DEFAULT_VIDEO = 0; // 全屏的视频为景区视频, 小窗口播放的是视频
	private final static int DEFAULT_LIVE = 1; // 全屏视频为景区视频, 小窗口是采集端的视频
	private final static int REVERSE_VIDEO = 2; // 两个视频已经反转, 大窗口是解说者视频
	private final static int REVERSE_LIVE = 3; // 两个视频已经反转, 大窗口是视频采集端
	
	volatile boolean mCanApplyExplain;  // 是否可以申请讲解 
	
	private MessageAdapterLanscape mMessageAdapter;
	private ExplainerAdapter mExplainerAdapter;
	
	private List<String> mViewerUrlLists;
	boolean isPrivilegeUser = false;
	
	public InterestingFakeLandacapeActivity(T activity){
		mActivity = activity;
		mViewerUrlLists = new ArrayList<String>();
	}
	*/
/**
	 * 仿照Activity的onCreate方法, 用于重新构建UI
	 *//*

	public void onCreate(IjkVideoView videoViewScenic, IjkVideoView videoExplainer, LiveCollectView liveCollectView){		
		OSUtil.setFullScreen(mActivity, true);
		OSUtil.enableStatusBar(mActivity, false);
		// 这样做的的目的是, 此mActivity重写了setContentView(int)方法
		View rootView = mActivity.getLayoutInflater().inflate(R.layout.activity_interesting_fack_landacape, null);
		mCanApplyExplain = true;
		mActivity.setContentView(rootView);
		mVideoViewScenic = videoViewScenic;
		mVideoViewExplainer = videoExplainer;
		mLiveCollectView = liveCollectView;
		mHandler = new Handler();
		initView();
		mPlayerContainer.addView(mVideoViewScenic, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		initListener();
		initAdapter();
		initExplainerLayout();
		
		if(mVideoViewExplainer != null){
			MLog.v(TAG, "explainer video is playing");
			initDragView();
			mViewDragLayout.setDragView(mVideoViewExplainer);
			setVideoZorder(mVideoViewScenic, mVideoViewExplainer);
			setViewDragSizeExplainer();
		}
	}
	
	*/
/**
	 * 初始化解说者列表布局
	 *//*

	private void initExplainerLayout() {
		mExplainerImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MLog.v(TAG, "onExplainerImage was clicked");
				if(mExplainerBtn.getVisibility() == View.VISIBLE){
					setExplainerListViewVisibility(View.GONE);
				}else{
					setExplainerListViewVisibility(View.VISIBLE);
				}
			}
		});
		mExplainerAdapter = new ExplainerAdapter(null);
		mExplainerListView.setAdapter(mExplainerAdapter);
		mExplainerListView.setDividerWidth(OSUtil.dp2px(mActivity, 2));
		mExplainerBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCanApplyExplain){
					if(UserSharedPreference.isLogin()){
						mActivity.getSendCommandHelper().applyExplain(isPrivilegeUser);
						mCanApplyExplain = false;
					}else{
						Toast.makeText(mActivity, "未登录用户不能进行此操作, 亲, 请登录哟", Toast.LENGTH_LONG).show();
					}
				}else{
					Toast.makeText(mActivity, "您现在不能再次申请讲解 ", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	private void setExplainerListViewVisibility(int visibility){
		// 设置解说者列表与说说按钮的可见性
		mExplainerBtn.setVisibility(visibility);
		mExplainerListView.setVisibility(visibility);
	}
	*/
/**
	 * 更新聊天信息
	 *//*

	public void updateChatMessage(){
		if(mMessageAdapter == null || mChatMessageListView == null) return;
		mMessageAdapter.notifyDataSetChanged();
		mChatMessageListView.smoothScrollToPosition(mActivity.getMessageEntityList().size());
	}
	
	private void initAdapter() {
		mMessageAdapter = new MessageAdapterLanscape(mActivity.getMessageEntityList(), mActivity.getSQliteHelper());
		mChatMessageListView.setAdapter(mMessageAdapter);
		mHandler.postDelayed(new Runnable() {
			public void run() {
				updateChatMessage();
				mChatMessageListView.setSelection(Integer.MAX_VALUE);
			}
		}, 600);
		mMessageAdapter.setPhotoListener(new MessageAdapterLanscape.MessageAdapterListener() {
			@Override
			public void onHeaderClick(MessageEntity messageEntity) {
				mActivity.onMessageHeaderClick(messageEntity);
			}

			@Override
			public void onImageClick(MessageEntity messageEntity) {
				mActivity.onImageClick(messageEntity);
			}
		});
		mChatMessageListView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					MLog.v(TAG, "list onTouch");
					setChatKeyBoardVisibility(View.GONE);
				}
				return false;
			}
		});
	}
	
	
	private void initLiveCollectView() {
		initDragView();
		mLiveCollectView = new LiveCollectView(mActivity);
		mViewDragLayout.setDragView(mLiveCollectView);
		mLiveCollectView.init(mActivity, null);
		mLiveCollectView.setPhotoListener(new LiveColleectViewListener() {
			@Override
			public void onGetHashId(String hashId) {
				MLog.v(TAG, "onGetHashId, and hashId is " + hashId);
//				mActivity.getSendCommandHelper().prepareComplete(hashId);
			}
		});
		mLiveCollectView.changeCamera(true);
		setVideoDragSize(XpaiCofig.videoWidth, XpaiCofig.videoHeight);
		mSupportView.setVisibility(View.VISIBLE);
	}
	
	void changedExplainerVideo(final String videoPath, final UserData userData){
		MLog.v(TAG, "changedExplainerVide, and videoPath is:" + videoPath);
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				initDragView();
				if(mVideoViewExplainer == null){
					mVideoViewExplainer = new IjkVideoView(mActivity);
				}
				mVideoViewExplainer.setVideoPath(videoPath);
				mVideoViewExplainer.start();
				setVideoZorder(mVideoViewScenic, mVideoViewExplainer);
				setVideoDecorationStartAnimationView(userData);
				setViewDragSizeExplainer();
			}
		});
	}
	
	private void setViewDragSizeExplainer() {
		// 设置解说者视频窗口的大小
		int videoWidth = mVideoViewExplainer.getVideoWith();
		if(videoWidth <= 0){
			mVideoViewExplainer.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(IMediaPlayer mp) {
					int width = mp.getVideoWidth();
					int height = mp.getVideoHeight();
					if(width < height){
						int tmp = width;
						height = width;
						width = tmp;
					}
					setVideoDragSize(width, height);
				}
			});
			return;
		}
		int videoHeight = mVideoViewExplainer.getVideoHeight();
		setVideoDragSize(videoWidth, videoHeight);
	}
	
	private void setVideoDragSize(int with, int height){
		MLog.v(TAG, "setVideoDragSize, and width is " + with + ", height is " + height);
		if(mViewDragLayout != null){
			mViewDragLayout.setWidthHeightRadio(with, height);
		}
	}
	
	private void initDragView(){
		// TODO: 解决这个SurfaceView create事件的不调用问题
		// 目前怀疑是setVisibility方法需要时间调用, 
		// 而在invisible期间, 添加View是不会触发SurfaceView的create方法
		// 或许需要进入源码了解一下Android的View系统
		if(mViewDragLayout != null){
			mViewDragLayout.setVisibility(View.VISIBLE);
			removeViewFromParent(mViewDragLayout);
			mViewDragLayout.removeAllViewsFromPlayerContainer();
		}
		mViewDragLayout = new ViewWithDrag(mActivity);
		mRootView.addView(mViewDragLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		mViewDragLayout.getPlayerContainer().setClickable(true);
		mViewDragLayout.setMyClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MLog.v(TAG, "drag view was clicked, and current status is " + mTwoVideoState);
				// 开始大小屏切换
				onChangeTwoWindow();
			}
		});
		mViewDragLayout.setPlayerContainerPosition();
		mSupportView.setVisibility(View.VISIBLE);
	}
	
	private void onChangeTwoWindow() {
		View bigView = null, smallView = null;

		switch (mTwoVideoState) {
		case DEFAULT_VIDEO:
			bigView = mVideoViewExplainer;
			smallView = mVideoViewScenic;
			mTwoVideoState = REVERSE_VIDEO;
			break;
		case DEFAULT_LIVE:
			bigView = mLiveCollectView;
			smallView = mVideoViewScenic;
			mTwoVideoState = REVERSE_LIVE;
			break;
		case REVERSE_VIDEO:
			bigView = mVideoViewScenic;
			smallView = mVideoViewExplainer;
			mTwoVideoState = DEFAULT_VIDEO;
			break;
		case REVERSE_LIVE:
			bigView = mVideoViewScenic;
			smallView = mLiveCollectView;
			mTwoVideoState = DEFAULT_LIVE;
			break;
		default:
			MLog.e(TAG, "error, you should not come here");
		}
		setVideoZorder(bigView, smallView);
	}
	
	*/
/*
	 * viewBig -- 在下面的视频 
	 * viewSmall -- 位于viewBig之上的视频
	 *//*

	private void setVideoZorder(View viewBig, View viewSmall){
		removeViewFromParent(viewBig);
		removeViewFromParent(viewSmall);
		mPlayerContainer.addView(viewBig);
		mViewDragLayout.setDragView(viewSmall);
		setVideoZorderMediaOverlay(viewBig, false);
		setVideoZorderMediaOverlay(viewSmall, true);
		
		boolean isNeedDecoration = false;
		if(viewBig == mVideoViewScenic){
			if(viewSmall == mVideoViewExplainer){
				mTwoVideoState = DEFAULT_VIDEO;
			}else if(viewSmall == mLiveCollectView){
				mTwoVideoState = DEFAULT_LIVE;
			}
			isNeedDecoration = true;
		}else if(viewBig == mVideoViewExplainer){
			mTwoVideoState = REVERSE_VIDEO;
		}else {
			mTwoVideoState = REVERSE_LIVE;
		}
		
		if(isNeedDecoration){
			setSmallWindowDecoration(viewSmall);
		}
	}	
	// 设置解说者窗口的可见性
	// visible -- true 窗口可见 
	private void setExplainerVideoViewIsVisible(boolean visible){
		if(visible){
			mExplainerImageHeader.setVisibility(View.GONE);
			if(mViewDragLayout == null || mVideoViewExplainer == null) return;
			mViewDragLayout.setDragView(mVideoViewExplainer);
			mViewDragLayout.setVisibility(View.VISIBLE);
			setVideoZorder(mVideoViewScenic, mVideoViewExplainer);
		}else{
			removeViewFromParent(mVideoViewExplainer);
			mViewDragLayout.setVisibility(View.INVISIBLE);
			mExplainerImageHeader.setVisibility(View.VISIBLE);
			UserData userData = mActivity.getExplainer();
			if(userData == null) return;
			ImageDisplayTools.displayCircleImage(userData.getImgUrl(), mExplainerImageHeader, 2);
		}
	}
	
	// 设置小窗口视频的装饰物
	private void setSmallWindowDecoration(View viewSmall){
		if(mSmallWindowsDecorationLayout == null){
			mSmallWindowsDecorationLayout = new RelativeLayout(mActivity);
		}
		mSmallWindowsDecorationLayout.removeAllViews();
		ImageView imageView = new ImageView(mActivity);
		if(viewSmall instanceof IjkVideoView){
			imageView.setBackgroundResource(R.drawable.interesting_place_explainer_window_small);
			imageView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					MLog.v(TAG, "minimum");
					setExplainerVideoViewIsVisible(false);
				}
			});
			
			TextView textView = new TextView(mActivity);
			textView.setTextColor(Color.BLUE);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			UserData explainer = mActivity.getExplainer();
			if(explainer == null) return;
			textView.setText(explainer.getNickName());
			mSmallWindowsDecorationLayout.addView(textView, params);
		}else{
			imageView.setBackgroundResource(R.drawable.inser_cut_close);
			imageView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(mCountDownTimer != null  && mMillisUnitsFinised <= 1000){
						Toast.makeText(mActivity, "正在关闭中..., 可能需要几秒钟", Toast.LENGTH_SHORT).show();
						mHandler.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								destoryLiveCollectView();
							}
						}, 3000);
					}else{
						destoryLiveCollectView();
					}
				}
			});
			final ImageView camera = new ImageView(mActivity);
			camera.setBackgroundResource(R.drawable.live_point_change_carame);
			RelativeLayout.LayoutParams cameraParas = new RelativeLayout.LayoutParams(OSUtil.dp2px(mActivity, 30), OSUtil.dp2px(mActivity, 30));
			cameraParas.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			cameraParas.addRule(RelativeLayout.LEFT_OF, R.id.fake_id);
			cameraParas.rightMargin = OSUtil.dp2px(mActivity, 5);
			cameraParas.topMargin = OSUtil.dp2px(mActivity, 5);
			mSmallWindowsDecorationLayout.addView(camera, cameraParas);
			camera.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					MLog.v(TAG, "onCamera is Front change");
					// 前后屏切换
					if(mLiveCollectView != null){
						mLiveCollectView.changeCamera();
						camera.setEnabled(false);
						mLiveCollectView.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								camera.setEnabled(true);
							}
						}, 500);
					}
				}
			});
		}
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(OSUtil.dp2px(mActivity, 30), OSUtil.dp2px(mActivity, 30));
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.rightMargin = params.topMargin = OSUtil.dp2px(mActivity, 5);
		imageView.setId(R.id.fake_id);
		// other
		mSmallWindowsDecorationLayout.addView(imageView, params);
		removeViewFromParent(mSmallWindowsDecorationLayout);
		mViewDragLayout.setDecorationView(mSmallWindowsDecorationLayout);
	}
	
	private void setVideoZorderMediaOverlay(View view, boolean isMediaLayout){
		if(view instanceof IjkVideoView){
			((IjkVideoView)view).setZOrderMediaOverlay(isMediaLayout);
		}else if(view instanceof LiveCollectView){
			ViewGroup parent = (ViewGroup) view.getParent();
			if(parent == null){
				MLog.e(TAG, "livecollect view has no parent");
				return;
			}
			
			parent.removeView(view);
			((LiveCollectView)view).setZOrderMediaOverlay(isMediaLayout);
			parent.addView(view);
		}else{
			MLog.e(TAG, "wrong type, check your code");
		}
	}
	
	// 初始化按钮的点击监听
	private void initListener() {
		// 关闭按钮
		mCloseImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MLog.v(TAG, "the btn close video was clicked");
				mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		});
		mWriteImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MLog.v(TAG, "onWriteImageClick");
				setChatKeyBoardVisibility(View.VISIBLE);
			}
		});
		mShieldImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mChatMessageListView.getVisibility() == View.VISIBLE){
					setOtherVisible(View.GONE);
					mShieldImage.setImageResource(R.drawable.live_point_shield);
				}else{
					setOtherVisible(View.VISIBLE);
					mShieldImage.setImageResource(R.drawable.live_point_shield_none);
				}
				
			}
			
			private void setOtherVisible(int visibility){
				mChatMessageListView.setVisibility(visibility);
				mWriteImage.setVisibility(visibility);
				mExplainerLayout.setVisibility(visibility);
				mViewerListView.setVisibility(visibility);
				if(visibility != View.VISIBLE){
					mSupportView.setVisibility(visibility);
				}else{
					if(mVideoViewExplainer != null || mLiveCollectView != null){
						mSupportView.setVisibility(View.VISIBLE);
					}else{
						mSupportView.setVisibility(View.GONE);
					}
				}
			}
		});
		mChatKeyboard.setPhotoListener(new ChatKeyboardListener() {
			
			@Override
			public void onFunctionClick(int functionId) {
				
			}
			
			@Override
			public void onSendClick(String message) {
				setChatKeyBoardVisibility(View.INVISIBLE);
				mActivity.onSendMessage(message);
				updateChatMessage();
			}
			
			@Override
			public void onPicClick() {	
				mActivity.onPictureClick();
				updateChatMessage();
			}

			@Override
			public void onVoiceTouchStart() {

			}

			@Override
			public void onVoiceTouchEnd() {

			}
		});
		
		mSupportView.setPhotoListener(new SupportViewClickListener() {
			
			@Override
			public void onSupportClick(SupportView supportView) {
				MLog.v(TAG, "praise");
				mSupportView.setHeartColorRed();
				mActivity.getSendCommandHelper().praise("1");
			}
		});
		mVideoViewScenic.setOnClickListener(null);
		mExplainerImageHeader.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setExplainerVideoViewIsVisible(true);
				mSupportView.setVisibility(View.VISIBLE);
			}
		});
		mPlayerContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 点击屏幕
				MLog.v(TAG, "mPlayerContainer click");
				setChatKeyBoardVisibility(View.GONE);
			}
		});
	}
	
	private void setChatKeyBoardVisibility(int visibility){
		if(mViewerListView.getVisibility() != View.VISIBLE) return;
		mChatKeyboard.setVisibility(visibility);
		int otherVisibility;
		if(visibility == View.VISIBLE){
			otherVisibility = View.INVISIBLE;
		}else{
			otherVisibility = View.VISIBLE;
		}
		mWriteImage.setVisibility(otherVisibility);
		mExplainerLayout.setVisibility(otherVisibility);
//		mSupportView.setVisibility(otherVisibility);
	}
	private void initView() {
		mRootView = (FrameLayout) mActivity.findViewById(R.id.fl_root_view);
		mCloseImage = (ImageView) mRootView.findViewById(R.id.iv_close);
		mPlayerContainer = (FrameLayout) mRootView.findViewById(R.id.fl_player_container);
		mWriteImage = (ImageView) mRootView.findViewById(R.id.iv_point_write);
		mShieldImage = (ImageView) mRootView.findViewById(R.id.iv_point_shield);
		mChatKeyboard = (ChatKeyboard) mRootView.findViewById(R.id.chat_key_board);
		mSupportView = (SupportView) mRootView.findViewById(R.id.support_view);
		mExplainerLayout = mRootView.findViewById(R.id.ll_explainer_layout);
		mChatMessageListView = (ListView) mRootView.findViewById(R.id.lv_chat_message);
		
		mExplainerBtn = (TextView) mRootView.findViewById(R.id.tv_explainer);
		mExplainerListView = (HorizontalListView) mRootView.findViewById(R.id.lv_explainer_list);
		mExplainerImage = (ImageView) mRootView.findViewById(R.id.iv_explainer);
		mViewerListView = (ImageHorizontalListView) mRootView.findViewById(R.id.image_horizontal_list);
		mExplainerImageHeader = (ImageView) mRootView.findViewById(R.id.iv_explainer_header);
		mSupportView.setSupportNumber(mActivity.getPraiseNum());
	}
	
	public void beforeConfigurationChanged(){
		MLog.v(TAG, "onConfigurationChanged");
		// 需要切换成竖屏模式
		removeViewFromParent(mVideoViewScenic);
		removeViewFromParent(mVideoViewExplainer);
		destoryLiveCollectView();
		if(!mCanApplyExplain){
			mActivity.getSendCommandHelper().cancelApply();
		}
		if(mVideoViewExplainer != null){
			mActivity.updateVideoView(mVideoViewScenic, mVideoViewExplainer, mLiveCollectView);
		}
	}
	
	private void removeViewFromParent(View view){
		if(view != null && view.getParent() != null){
			((ViewGroup)view.getParent()).removeView(view);
		}
	}
	
	*/
/**
	 * 点赞
	 *//*

	void onPraise(String roomNum, String bgLiveId, UserData userData) {
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				mSupportView.animationAddOne();
			}
		});
	}
	
	*/
/**
	 * 准备讲解
	 *//*

	void onPrepareExplain(){
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				initLiveCollectView();
				String diyParams = mActivity.getLiveDIYParams();
				MLog.v(TAG, "startPreview, and diyParams is:" + diyParams);
				mLiveCollectView.startPreview(diyParams);
				setSmallWindowDecoration(mLiveCollectView);
				mTwoVideoState = DEFAULT_LIVE;
				mLiveCollectView.setZOrderMediaOverlay(true);
			}
		});
	}
	
	private void setVideoDecorationStartAnimationView(UserData userData){
		VideoDecorationStartAnimationView videoDecorationStartAnimationView
			= new VideoDecorationStartAnimationView(mActivity);
		mSmallWindowsDecorationLayout.addView(videoDecorationStartAnimationView,
			new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		videoDecorationStartAnimationView.setBlurBackgroudImg(userData.getImgUrl());
		videoDecorationStartAnimationView.setHeaderImgVisibity(View.GONE);
		videoDecorationStartAnimationView.setBlurNotifyText("即将开始直播");
		videoDecorationStartAnimationView.setStartText(5);
	}

	*/
/**
	 * 停止解说
	 *//*

	void onStopExplain(final boolean isMine){
		if(isMine){
			mCanApplyExplain = true;
		}
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				MLog.v(TAG, "landacape onStopExplain");
				if(mTwoVideoState == REVERSE_LIVE || mTwoVideoState == REVERSE_VIDEO){
					onChangeTwoWindow();
					mHandler.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							onStopExplain(isMine);							
						}
					}, 200);
					return;
				}
				if(mViewDragLayout != null){
					mViewDragLayout.setVisibility(View.INVISIBLE);
					mViewDragLayout.removeAllViewsFromPlayerContainer();
					mSupportView.setVisibility(View.GONE);
				}
				if(isMine){
					destoryLiveCollectView();
				}
				if(mVideoViewExplainer != null){
					mVideoViewExplainer.stopPlayback();
					mVideoViewExplainer = null;
				}
				mExplainerImageHeader.setVisibility(View.GONE);
			}
		});
	}
	*/
/*
	 * 更新解说者列表
	 * @params explainer
	 * @Run in main thread
	 * *//*

	void updateExplainerListView(final List<UserData> explainer){
		if(mHandler == null || mExplainerAdapter == null) return;
		mExplainerAdapter.notifyDataSetChanged(explainer);
	}
	
	void onPreviewFailed(){
		destoryLiveCollectView();
		mCanApplyExplain = true;
	}
	
	private void destoryLiveCollectView() {
		if(mLiveCollectView != null){
			mActivity.getSendCommandHelper().stopExplain();
			mLiveCollectView.onDestory(mActivity);
			removeViewFromParent(mLiveCollectView);
			mLiveCollectView = null;
			removeViewFromParent(mViewDragLayout);
		}
		if(mSupportView != null){
			mSupportView.setVisibility(View.GONE);
		}
	}
	
	void onPrepareEnd(final int countTime){
		// 准备结束, 进入倒计时 
		if(mLiveCollectView != null && mLiveCollectView.isPreviewing()){
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					liveCollectViewCountTime(countTime);
				}
			});
		}
	}
	
	private CountDownTimer mCountDownTimer;
	private long mMillisUnitsFinised;
	private boolean isCountDownStop = false;
	private void liveCollectViewCountTime(int countTime){
		if(mSmallWindowsDecorationLayout == null) return;
		final TextView textView = new TextView(mActivity);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mSmallWindowsDecorationLayout.addView(textView, params);
		mCountDownTimer = new CountDownTimer(countTime * 1000, 1000) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				mMillisUnitsFinised = millisUntilFinished;
				int leftTime = (int) (millisUntilFinished / 1000);
				textView.setText(String.valueOf(leftTime));
				
				if(isCountDownStop){
					textView.setVisibility(View.GONE);
					mCountDownTimer.cancel();
					mCountDownTimer = null;
					isCountDownStop = false;
				}
			}
			
			@Override
			public void onFinish() {
				MLog.v(TAG, "countDownTimer finished");
				textView.setVisibility(View.GONE);
			}
		};
		mCountDownTimer.start();
	}
	
	void stopCountDown(){
		if(mCountDownTimer != null){
			isCountDownStop = true;
		}
	}
	
	*/
/**
	 * 设置观众列表
	 *//*

	void setViewerList(final List<UserData> viewerList){
		if(mViewerListView == null) return;
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				mViewerUrlLists.clear();
				for(int i = 0; i < viewerList.size(); i++){
					mViewerUrlLists.add(viewerList.get(i).getImgUrl());
				}
				mViewerListView.setUrls(mViewerUrlLists);
				mViewerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						mActivity.showFollowPopWindow(viewerList.get(position));
					}
				});
			}
		});
	}
}*/
