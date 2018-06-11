package com.travel.video.live;

import android.content.Intent;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.tencent.TIMCallBack;
import com.tencent.TIMValueCallBack;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.utils.Constants;
import com.tencent.qcloud.suixinbo.views.customviews.HeartLayout;
import com.travel.communication.entity.UserData;
import com.travel.layout.DialogTemplet;
import com.travel.layout.HorizontalListView;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.PopWindowUtils;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.utils.HLLXLoginHelper;
import com.travel.video.gift.GiftBean;
import com.travel.video.gift.GiftRelativeLayout;
import com.travel.video.help.GiftHttpHelper;
import com.travel.video.help.LiveHttpRequest;
import com.travel.VideoConstant;
import com.travel.video.layout.GiftPopupWindow;
import com.travel.video.layout.ProductPopupWindow;

import java.util.ArrayList;
import java.util.List;

import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * 观看直播页面的数据、UI的更新
 * @author Administrator
 */
public class ViewerWindowHandler extends AbstractLiveHandler{
	private static final String TAG = "Live_Normal";

	public final static int ACTIVITY_RESUME = 11013;//activity的resume方法
	public final static int ACTIVITY_PAUSE = 11014;//activity的pause方法
	public final static int REFUSE_INSER_CUT = 11011;//被拒绝插播
	public final static int INSER_CUT_CACLE_WAIT = 11015;//取消插播排队等待

	private LinearLayout layoutHead;
	//开始，结束显示效果
	private ImageView finishImage;
	private LinearLayout finishLayout;
	private View finishLayout_land;
	private TextView totalWatchNum, totalWatchNumLand;
	//视频直播人信息，以及视频进出人等显示
	private TextView videoNameOrType;//视频用户名和类型
	private ImageView videoHeadImg;//直播视频个人头像
	private ImageView closeVideo;//关闭按钮
	private ImageView report;//举报按钮
	private Button finishButton, finishButtonLand;//点击结束直播按钮，显示结束操作界面
	private RelativeLayout fooderLayout;//底部按钮容器
	private ImageView shield;//屏蔽其他控件
	private ImageView shareVideo;//分享视频
	private ImageView chat_write;//打开发弹幕编辑栏
	private ImageView sendGift; //送礼
	private ImageView openProduct;//打开产品弹框
	private TextView mHostLeaveView; // 主播离开提示语
	private ImageView interCutButton;//插播按钮

	private DialogTemplet interWaitDialog;//插播等待中点击黄色按钮的弹窗
	private DialogTemplet refuseDialog;//被拒绝弹框
	private DialogTemplet reportDialog;//举报弹框
	private GiftPopupWindow mFollowPopupWindow;
	private ProductPopupWindow productWindow;

	private List<GiftBean> giftList;
	private GiftHttpHelper giftHttpHelper;

	// 结束分享按钮
	private ImageView wechatFavorite,wechat,qq,qZone;
	private ImageView wechatFavoriteLand, wechatLand, qqLand, qZoneLand;

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
		case REFUSE_INSER_CUT:
			setInterCutButtonSize( "white");
			hideDilog();

			refuseDialog = new DialogTemplet(mHostWindowActivity,true,"非常遗憾，您的连线申请被拒绝了！","知道了", "", "");
			refuseDialog.show();

			refuseDialog.setConfirmClick(new DialogTemplet.DialogConfirmButtonListener() {
				@Override
				public void confirmClick(View view) {
				}
			});
			break;
		case INSER_CUT_CACLE_WAIT:
			setInterCutButtonSize("white");
			mHostWindowActivity.mLiveHelper.sendCancelInsertWait();
			break;
		default:
			break;
		}
	}

	void startInsertCut(int index) {
		if(index <= -1) return;
		mBlurCoverTaskView[index - 1].hideBlurCover();
		changeCamera.setVisibility(View.VISIBLE);
		soundOn.setVisibility(View.VISIBLE);
		soundOn.setImageResource(R.drawable.live_point_sound_open);
		setInterCutButtonSize( "black");
	}

	void hideCameraAndSoundOn(){
		changeCamera.setVisibility(View.INVISIBLE);
		soundOn.setVisibility(View.INVISIBLE);
		setInterCutButtonSize("white");
	}

	/* 当主播关闭直播后 */
	void onHostCloseLive(){
		reLayout.setVisibility(View.INVISIBLE);
		mHostLeaveView.setVisibility(View.INVISIBLE);
		if(mHostWindowActivity.liveType != HostWindowActivity.LIVE_TYPE_PACK)
			finishLayout.setVisibility(View.VISIBLE);
		else
			finishLayout_land.setVisibility(View.VISIBLE);
		totalWatchNum.setText(String.valueOf(mTotalWatchNum));
		totalWatchNumLand.setText(String.valueOf(mTotalWatchNum));
		finishImage.setVisibility(View.VISIBLE);
		for(FrameLayout frameLayout: mSmallVideoContainer){
			frameLayout.setVisibility(View.INVISIBLE);
		}
	}
	/* 当主播暂时离开(如点击Home键, 接听电话) */
	void onHostTmpLeave(){
		mHostLeaveView.setVisibility(View.VISIBLE);
		postDelayed(mHostCloseRunnable, 180000);
	}

	/* 王者归来 */
	void onHostBack(){
		mHostLeaveView.setVisibility(View.GONE);
		stopHostCloseTimer();
	}

	/* 设置遮罩背景图片的可见性 true -- 可见 */
	void setBlurImageVisible(boolean isVisible){
		if(finishImage != null){
			finishImage.setVisibility(isVisible ? View.VISIBLE : View.GONE);
		}
	}

	public ViewerWindowHandler(HostWindowActivity activity){
		super(activity);
		giftList = new ArrayList<>();
		initView();
		setListener();
		afterInitView();
		initHttpHelper();
		if(UserSharedPreference.isLogin()){
			giftHttpHelper.initGiftList();
		}
		// 30s后关闭直播
//		postDelayed(mHostCloseRunnable, 30000);
	}

	private void initView(){
		View rootView = mHostWindowActivity.findViewById(R.id.include_video_live_normal);
		//视频容器，点击以隐藏输入法
		reLayout = (RelativeLayout) rootView.findViewById(R.id.relayout);
		finishImage = (ImageView) rootView.findViewById(R.id.finishImage);
		if(mHostWindowActivity.liveType != HostWindowActivity.LIVE_TYPE_PACK){
			ImageDisplayTools.displayImage(CurLiveInfo.getInstance().getCoverurl() + "?x-oss-process=image/resize,w_500/blur,r_8,s_2", finishImage);
			finishImage.setVisibility(View.VISIBLE);
		}
		finishLayout = (LinearLayout) rootView.findViewById(R.id.finishLayout);
		finishLayout.setVisibility(View.GONE);

		giftRelativeLayout = (GiftRelativeLayout) rootView.findViewById(R.id.giftLayout);
		totalWatchNum = (TextView) rootView.findViewById(R.id.totalWatchNum);
		layoutHead = (LinearLayout) rootView.findViewById(R.id.layoutHead);
		videoHeadImg = (ImageView) rootView.findViewById(R.id.videoHeadImg);
		videoNameOrType = (TextView) rootView.findViewById(R.id.videoNameOrType);
		totalNum = (TextView) rootView.findViewById(R.id.total_num);
		lookingNum = (TextView) rootView.findViewById(R.id.looking_num);
		//点赞
		mHeartLayout = (HeartLayout) rootView.findViewById(R.id.heart_layout);
		zanLinearLayout = (LinearLayout) rootView.findViewById(R.id.zanLinearLayout);
		zanLayout = (RelativeLayout) rootView.findViewById(R.id.zanLayout);
		zanNum = (TextView) rootView.findViewById(R.id.zan_num);
		zan = (ImageView) rootView.findViewById(R.id.zan_click);

		finishButton = (Button) rootView.findViewById(R.id.finishButton);
		changeCamera = (ImageView) rootView.findViewById(R.id.change_camera);
		closeVideo = (ImageView) rootView.findViewById(R.id.closeVideo);
		report = (ImageView) rootView.findViewById(R.id.report);

		fooderLayout = (RelativeLayout) rootView.findViewById(R.id.layoutFooter);
		shield = (ImageView) rootView.findViewById(R.id.videoShield);
		chat_write = (ImageView) rootView.findViewById(R.id.videoWrite);
		shareVideo = (ImageView) rootView.findViewById(R.id.shareVideo);
		sendGift = (ImageView) rootView.findViewById(R.id.sendGrid);
		sendGift.setClickable(false);
		openProduct = (ImageView) rootView.findViewById(R.id.openProduct);
		openProduct.setVisibility(View.GONE);
		soundOn = (ImageView) rootView.findViewById(R.id.sound_on);//自己插播控制插播声音

		barrageListView = (ListView) rootView.findViewById(R.id.barrageListView);
		barrageLayout = (RelativeLayout) rootView.findViewById(R.id.barrageLayout);
		barrageSendLayout = (RelativeLayout) rootView.findViewById(R.id.barrage_send_relayout);
		barrageSendEdit = (EditText) rootView.findViewById(R.id.barrage_content_edit);
		barrageSendButton = (Button) rootView.findViewById(R.id.barrage_send_button);
		mHostLeaveView = (TextView) rootView.findViewById(R.id.tv_host_leave);

		interCutButton = (ImageView) rootView.findViewById(R.id.inserCutButton);
		interCutButton.setTag("white");

		watchListView = (HorizontalListView) rootView.findViewById(R.id.watchListView);

		//初始化控件数据
		videoNameOrType.setText(CurLiveInfo.getInstance().getHostName());
		ImageDisplayTools.displayHeadImage(CurLiveInfo.getInstance().getHostAvator(), videoHeadImg);
		if (!OSUtil.isDayTheme())
			videoHeadImg.setColorFilter(TravelUtil.getColorFilter(mHostWindowActivity));

		barrageLayout.getLayoutParams().height = OSUtil.getScreenHeight()/3;

		wechatFavorite = (ImageView) rootView.findViewById(R.id.wechatFavorite);
		wechat = (ImageView) rootView.findViewById(R.id.wechat);
		qq = (ImageView) rootView.findViewById(R.id.qq);
		qZone = (ImageView) rootView.findViewById(R.id.qZone);

		finishLayout_land = rootView.findViewById(R.id.finishLayout_land);
		finishLayout_land.setVisibility(View.GONE);
		totalWatchNumLand = (TextView) finishLayout_land.findViewById(R.id.totalWatchNum);
		finishButtonLand = (Button) finishLayout_land.findViewById(R.id.finishButton);
		wechatFavoriteLand = (ImageView) finishLayout_land.findViewById(R.id.wechatFavorite);
		wechatLand = (ImageView) finishLayout_land.findViewById(R.id.wechat);
		qqLand = (ImageView) finishLayout_land.findViewById(R.id.qq);
		qZoneLand = (ImageView) finishLayout_land.findViewById(R.id.qZone);
	}

	private void setListener() {

		openProduct.setOnClickListener(new OpenProductListener());
		shield.setOnClickListener(new ShieldListener());
		shareVideo.setOnClickListener(new ShareLiveListener());
		chat_write.setOnClickListener(new ChatListener());
		interCutButton.setOnClickListener(new InserCutButtonListener());
		sendGift.setOnClickListener(new SendGiftListener());
		videoHeadImg.setOnClickListener(new HeadImgListener());
		finishButton.setOnClickListener(new FinishListener());
		finishButtonLand.setOnClickListener(new FinishListener());
		closeVideo.setOnClickListener(new FinishListener());
		//举报监听
		report.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideDilog();
				reportDialog = new DialogTemplet(mHostWindowActivity,false,"该直播有不当内容，我要举报","", "取消", "确认");
				reportDialog.show();

				reportDialog.setLeftClick(new DialogTemplet.DialogLeftButtonListener() {
					@Override
					public void leftClick(View view) {

					}
				});

				reportDialog.setRightClick(new DialogTemplet.DialogRightButtonListener() {
					@Override
					public void rightClick(View view) {
						String videoId = CurLiveInfo.getInstance().getRoomNum()<=0 ? "" : CurLiveInfo.getInstance().getRoomNum()+"";
						LiveHttpRequest.reportVideoRequest(mHostWindowActivity,
								CurLiveInfo.getInstance().getHostID(), videoId, "0");
					}
				});

			}
		});

		wechatFavorite.setOnClickListener(new ShareClickListener());
		wechat.setOnClickListener(new ShareClickListener());
		qq.setOnClickListener(new ShareClickListener());
		qZone.setOnClickListener(new ShareClickListener());
		wechatFavoriteLand.setOnClickListener(new ShareClickListener());
		wechatLand.setOnClickListener(new ShareClickListener());
		qqLand.setOnClickListener(new ShareClickListener());
		qZoneLand.setOnClickListener(new ShareClickListener());
	}

	private void initHttpHelper() {
		mLiveHttpRequest = new LiveHttpRequest(mHostWindowActivity, new LiveHttpRequest.ServerListView() {
			@Override
			public void onNotifyServerLiveStartResult(boolean isSuccess) {
				// ignore
			}

			@Override
			public void onNotifyServerLiveStopResult(boolean isSuccess) {
				// ignore
			}
		});

		mLiveHttpRequest.getLiveCacheBaseInfo(String.valueOf(CurLiveInfo.getInstance().getRoomNum()), new TIMValueCallBack<LiveHttpRequest.LiveBaseInfo>() {
			@Override
			public void onError(int i, String s) {
				// ignore
			}

			@Override
			public void onSuccess(LiveHttpRequest.LiveBaseInfo liveBaseInfo) {
				setLiveBaseInfo(liveBaseInfo);
			}
		});

		giftHttpHelper = new GiftHttpHelper(mHostWindowActivity, new GiftHttpHelper.GiftHttpListener() {
			@Override
			public void onGiftBeans(List<GiftBean> giftBeans) {
				if (giftList != null){
					giftList.clear();
					giftList.addAll(giftBeans);
				} else {
					giftList = giftBeans;
				}
			}

			@Override
			public void onSendGiftSuccess(GiftBean giftBean) {
				mHostWindowActivity.mLiveHelper.sendGif(giftBean.getJson());
				showGif(giftBean);
			}
		});
	}

	@Override
	protected void onCloseSmallVideoViewClick(int position) {
		String mId = MySelfInfo.getInstance().getId();
		mHostWindowActivity.mLiveHelper.sendCancelInteract(mId);
		mHostWindowActivity.mLiveHelper.changeAuthAndCloseMemberView(mId);
	}
	/**
	 * 隐藏弹框
	 */
	private void hideDilog(){
		if(interWaitDialog != null && interWaitDialog.isShowing()){
			interWaitDialog.dismiss();
		}
		if(refuseDialog != null && refuseDialog.isShowing()){
			refuseDialog.dismiss();
		}
		if(reportDialog != null && reportDialog.isShowing()){
			reportDialog.dismiss();
		}
		
		if(mFollowPopupWindow != null && mFollowPopupWindow.isShowing()){
			mFollowPopupWindow.dismiss();
		}
	}

	/**屏蔽监听*/
	private class ShieldListener implements OnClickListener{
		//屏蔽其他界面
		@Override
		public void onClick(View v) {
			if(zanLinearLayout.getVisibility() == View.VISIBLE){
				layoutHead.setVisibility(View.INVISIBLE);
				chat_write.setVisibility(View.GONE);
				zanLayout.setVisibility(View.INVISIBLE);
				barrageLayout.setVisibility(View.INVISIBLE);
				zanLinearLayout.setVisibility(View.INVISIBLE);
				shareVideo.setVisibility(View.INVISIBLE);
				watchListView.setVisibility(View.INVISIBLE);
				if(!mHostWindowActivity.mIsLowPermission && mHostWindowActivity.liveType!=HostWindowActivity.LIVE_TYPE_LVB){
					interCutButton.setVisibility(View.INVISIBLE);
				}
//				sendGift.setVisibility(View.INVISIBLE);
				report.setVisibility(View.INVISIBLE);
				giftRelativeLayout.setVisibility(View.INVISIBLE);
				
				if(View.GONE != changeCamera.getVisibility()){
					changeCamera.setVisibility(View.INVISIBLE);
					soundOn.setVisibility(View.INVISIBLE);
				}
//				if(openProduct.getVisibility() != View.GONE && mHostWindowActivity.isShowShopButton)
//					openProduct.setVisibility(View.INVISIBLE);
				shield.setImageResource(R.drawable.live_point_shield);
			}else if(zanLinearLayout.getVisibility() == View.INVISIBLE){
				chat_write.setVisibility(View.VISIBLE);
				layoutHead.setVisibility(View.VISIBLE);
				zanLayout.setVisibility(View.VISIBLE);
				zanLinearLayout.setVisibility(View.VISIBLE);
				barrageLayout.setVisibility(View.VISIBLE);
				shareVideo.setVisibility(View.VISIBLE);
				watchListView.setVisibility(View.VISIBLE);
				if(!mHostWindowActivity.mIsLowPermission && mHostWindowActivity.liveType!=HostWindowActivity.LIVE_TYPE_LVB){
					interCutButton.setVisibility(View.VISIBLE);
				}
//				sendGift.setVisibility(View.VISIBLE);
				report.setVisibility(View.VISIBLE);
//				if(openProduct.getVisibility() != View.GONE && mHostWindowActivity.isShowShopButton)
//					openProduct.setVisibility(View.VISIBLE);
				giftRelativeLayout.setVisibility(View.VISIBLE);
				if(View.INVISIBLE == changeCamera.getVisibility()){
					changeCamera.setVisibility(View.VISIBLE);
					soundOn.setVisibility(View.VISIBLE);
				}
				shield.setImageResource(R.drawable.live_point_shield_none);
			}
		}
	}

	@Override
	public boolean onBackPressed() {
		if(mFollowPopupWindow != null && mFollowPopupWindow.isShowing()){
			mFollowPopupWindow.dismiss();
			return true;
		}

		hideDilog();
		if(finishLayout.getVisibility() != View.VISIBLE || finishLayout_land.getVisibility() != View.VISIBLE){
			AlertDialogUtils.alertDialog(mHostWindowActivity, "是否确认离开直播", new Runnable() {
				@Override
				public void run() {
					mHostWindowActivity.onFinish();
				}
			});
			return true;
		}
		return false;
	}

	private class SendGiftListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!UserSharedPreference.isLogin()){
				Toast.makeText(mHostWindowActivity, "请先登录!", Toast.LENGTH_SHORT).show();
				return;
			}
			if(giftList==null || giftList.size()<1) {
				Toast.makeText(mHostWindowActivity, "未获取到礼物列表！", Toast.LENGTH_SHORT).show();
				return;
			}
				
			fooderLayout.setVisibility(View.INVISIBLE);
			mFollowPopupWindow = new GiftPopupWindow(mHostWindowActivity,giftList,new GiftPopupWindow.GiftListener() {

				@Override
				public void hideListener() {
						fooderLayout.setVisibility(View.VISIBLE);
					}
					@Override
					public void sendListener(GiftBean giftBean) {
						giftHttpHelper.sendGiftRequest(giftBean);
					}
	
				});
			mFollowPopupWindow.showAtLocation(mHostWindowActivity.findViewById(R.id.frameLayout),
					Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
		}
	}

	private class OpenProductListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			productWindow = new ProductPopupWindow(mHostWindowActivity,CurLiveInfo.getInstance().getHostID(), new ProductPopupWindow.ClickProductItemListener() {
				
				@Override
				public void notifyIntentActivity() {

				}
			});
		}
		
	}

	private class ChatListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			barrageSendLayout.setVisibility(View.VISIBLE);
			barrageSendEdit.setFocusable(true);
			barrageSendEdit.requestFocus();
			OSUtil.showKeyboard(mHostWindowActivity);
		}
	}

	private class HeadImgListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			PopWindowUtils.followPopUpWindow(mHostWindowActivity,
					CurLiveInfo.getInstance().getHostID().substring(HLLXLoginHelper.PREFIX.length()), CurLiveInfo.getInstance().getHostName(),CurLiveInfo.getInstance().getHostAvator(),1);
		}
	}

	private boolean isClickable = true;
	private class InserCutButtonListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if(!UserSharedPreference.isLogin()) {
				Toast.makeText(mHostWindowActivity, "请先登录！", Toast.LENGTH_SHORT).show();
				return;
			}
			if (!isClickable){
				return;
			}
			isClickable = false;
			postDelayed(new Runnable() {
				@Override
				public void run() {
					isClickable = true;
				}
			}, 500);

			if(interCutButton.getTag().equals("white")){
				hideDilog();
				mLiveHttpRequest.applyFlowControlOperationPermission(String.valueOf(CurLiveInfo.getInstance().getRoomNum()),
						UserSharedPreference.getUserId(), 2, new TIMCallBack() {
					@Override
					public void onError(int i, String s) {
						MLog.e(TAG, "ApplyFlowControlOperation failed");
						Toast.makeText(mHostWindowActivity, "当前系统连线人数已达到上限，请稍后再试", Toast.LENGTH_SHORT).show();
						setInterCutButtonSize("white");
					}
					@Override
					public void onSuccess() {
						AlertDialogUtils.alertDialog(mHostWindowActivity, "您确认要和主播连线互动吗？", new Runnable() {
							@Override
							public void run() {
								setInterCutButtonSize( "yelloy");
								mHostWindowActivity.mLiveHelper.sendApplyInsertLine(CurLiveInfo.getInstance().getHostID());
								UserData userData = new UserData();
								userData.setId(MySelfInfo.getInstance().getId().substring(HLLXLoginHelper.PREFIX.length()));
								userData.setImgUrl(MySelfInfo.getInstance().getAvatar());
								userData.setNickName(MySelfInfo.getInstance().getNickName());
								mInLivingUser.put(MySelfInfo.getInstance().getId(), userData);
							}
						});
					}
				});
			}else if(interCutButton.getTag().equals("yelloy")){
				hideDilog();
				if(interWaitDialog == null){
					applyInterWaitDialog();
				}
				interWaitDialog.show();
			}else if(interCutButton.getTag().equals("black")){

			}
		}
		
	}

	private void applyInterWaitDialog() {
		//此时为等待申请结果或自己已经在插播
		interWaitDialog = new DialogTemplet(mHostWindowActivity,false,"您的连线申请已提交，请耐心等待！","", "取消申请", "继续等待");

		interWaitDialog.setLeftClick(new DialogTemplet.DialogLeftButtonListener() {
            @Override
            public void leftClick(View view) {
                sendEmptyMessage(INSER_CUT_CACLE_WAIT);
            }
        });

		interWaitDialog.setRightClick(new DialogTemplet.DialogRightButtonListener() {
            @Override
            public void rightClick(View view) {

            }
        });
	}

	@Override
	protected void onLineInsertCut(){
		interCutButton.setVisibility(View.GONE);
	}


	/**
	 * 动态设置插播按钮的大小
	 */
	private void setInterCutButtonSize(String tag){
		if("yelloy".equals(tag)){
			interCutButton.setTag("yelloy");
			interCutButton.setImageResource(R.drawable.intercut_called_waiting_background);
			ViewGroup.LayoutParams interParams = interCutButton.getLayoutParams();
			((MarginLayoutParams) interParams).setMargins(OSUtil.dp2px(mHostWindowActivity, 5), OSUtil.dp2px(mHostWindowActivity, 5),
					OSUtil.dp2px(mHostWindowActivity, 5), OSUtil.dp2px(mHostWindowActivity, 5));
			interParams.width = OSUtil.dp2px(mHostWindowActivity, 85);
			interParams.height = OSUtil.dp2px(mHostWindowActivity, 30);
			interCutButton.setLayoutParams(interParams);
		}else if("white".equals(tag)){
			interCutButton.setTag("white");
			interCutButton.setImageResource(R.drawable.intercut_called_white_host);
			ViewGroup.LayoutParams interParams = interCutButton.getLayoutParams();
			((MarginLayoutParams) interParams).setMargins(OSUtil.dp2px(mHostWindowActivity, 5), OSUtil.dp2px(mHostWindowActivity, 5),
					OSUtil.dp2px(mHostWindowActivity, 5), OSUtil.dp2px(mHostWindowActivity, 5));
			interParams.width = OSUtil.dp2px(mHostWindowActivity, 60);
			interParams.height = OSUtil.dp2px(mHostWindowActivity, 30);
			interCutButton.setLayoutParams(interParams);
		}else if("black".equals(tag)){
			interCutButton.setTag("black");
			interCutButton.setImageResource(R.drawable.intercut_calling_out);
			ViewGroup.LayoutParams interParams = interCutButton.getLayoutParams();
			((MarginLayoutParams) interParams).setMargins(OSUtil.dp2px(mHostWindowActivity, 5), OSUtil.dp2px(mHostWindowActivity, 5),
					OSUtil.dp2px(mHostWindowActivity, 5), OSUtil.dp2px(mHostWindowActivity, 5));
			interParams.width = OSUtil.dp2px(mHostWindowActivity, 85);
			interParams.height = OSUtil.dp2px(mHostWindowActivity, 30);
			interCutButton.setLayoutParams(interParams);
		}
	}

	private class FinishListener implements OnClickListener{
		
		@Override
		public void onClick(View v) {
			mHostWindowActivity.close();
		}
		
	}

	private class ShareClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			int id = v.getId();
			String platform = "";
			if(id == R.id.wechatFavorite)
				platform = WechatMoments.NAME;
			else if(id == R.id.wechat)
				platform = Wechat.NAME;
			else if(id == R.id.qq)
				platform = QQ.NAME;
			else if(id == R.id.qZone)
				platform = QZone.NAME;
			String shareUrl = VideoConstant.SHARE_VIDEO_URL + CurLiveInfo.getInstance().getShare();
			OSUtil.showShare(platform, CurLiveInfo.getInstance().getTitle(), CurLiveInfo.getInstance().getTitle(), CurLiveInfo.getInstance().getCoverurl(), shareUrl, shareUrl, mHostWindowActivity);

		}

	}

	public void stopHostCloseTimer(){
		MLog.v(TAG, "stopHostCloseTimer");
		removeCallbacks(mHostCloseRunnable);
	}

	private final Runnable mHostCloseRunnable = new Runnable() {
		@Override
		public void run() {
			MLog.d(TAG, "mHostCloseRunnable run()");
			if(mHostWindowActivity == null || mHostWindowActivity.isFinishing()){
				return;
			}
			mHostWindowActivity.sendBroadcast(new Intent(Constants.ACTION_HOST_LEAVE));
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopHostCloseTimer();
	}
}