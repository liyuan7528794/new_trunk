package com.travel.video.live;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.views.customviews.HeartLayout;
import com.travel.communication.entity.UserData;
import com.travel.layout.HorizontalListView;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.utils.HLLXLoginHelper;
import com.travel.video.gift.GiftRelativeLayout;
import com.travel.video.help.HeartBeatHelper;
import com.travel.video.help.LiveHttpRequest;
import com.travel.video.layout.BeautifyPopupView;
import com.travel.video.tools.HostWindowTimerTask;
import com.travel.widget.IntercutDialog;
import com.travel.widget.WaitIntercutDialog;

import java.util.ArrayList;
import java.util.List;


/**
 * 直播界面 UI的handler处理
 * @author Administrator
 */
public class HostWindowHandler extends AbstractLiveHandler implements LiveHttpRequest.ServerListView {
	private static final String TAG = "Live";

	public final static int TIMER_COUNT = 11004;
	public final static int START_LIVE = 11001;
	public final static int CLOSE_LIVE = 11005;
	public final static int INSER_CUT_REFUSE = 11008;//拒绝插播
	public final static int INSER_CUT_ACCEPT = 11009;//接受插播
	public final static int CLOSE_IMAGEVIEW_SHOW = 11015;//设置关闭按钮的点击事件为true

	//直播 类型（输入景区还是个人或目的地？）
	private int liveType = 0;
	private FragmentManager fragmentManager;
	private IntercutDialog interCutDialog;//控制插播窗口
	
	//主播个人基本信息
	private Bundle mBundle = null;
	private String userId = "";
	private String headImgUrl = "";
	
	//准备直播界面
	private LiveReadyFragment liveReadyFragment = null;

	private RelativeLayout liveFinishLayout;
	
	private LinearLayout layoutHead;
	private ImageView liveHeadImage;//主播头像
	private TextView timeLive;//直播时间
	private ImageView finishLive ;//直播结束按钮
	private ImageView mBeautifyImage;// 美颜按钮
	private ImageView chat_write;//打开发弹幕编辑栏
	private ImageView shield;//屏蔽其他控件
	private ImageView shareVideo;//分享视频

    private RelativeLayout callLayout;
	private ImageView inserCutCall;	//插播控制按钮
	private TextView inserCutNum;	//插播人数控件
	
    private List<UserData> mInterCutWaitList = null;//插播列表
	private WaitIntercutDialog waitDialog ;

	private int mIdeNum;  // 当前闲置的插播数目

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
		case START_LIVE:
			if(mHostWindowActivity.liveType == HostWindowActivity.LIVE_TYPE_PACK && TextUtils.isEmpty(mHostWindowActivity.mPackLiveUrl)){
				Toast.makeText(mHostWindowActivity, "尚未获取到背包视频,\n 请稍后重试", Toast.LENGTH_SHORT).show();
				return;
			}
			if(mHostWindowActivity.liveType == HostWindowActivity.LIVE_TYPE_LVB && TextUtils.isEmpty(mHostWindowActivity.mLvbLiveUrl)){
				Toast.makeText(mHostWindowActivity, "正在初始化直播,\n 请稍后重试", Toast.LENGTH_SHORT).show();
				return;
			}
			if(!mHostWindowActivity.mEnterLiveHelper.isInAVRoom()){
				Toast.makeText(mHostWindowActivity, "正在初始化直播,\n 看官慢些点", Toast.LENGTH_SHORT).show();
				return;
			}
			if(liveReadyFragment != null){
				mHostWindowActivity.getFragmentManager()
						.beginTransaction()
						.remove(liveReadyFragment)
						.commit();
			}
			reLayout.setVisibility(View.VISIBLE);
			liveFinishLayout.removeAllViews();
			liveFinishLayout.setVisibility(View.INVISIBLE);
			String liveId = MySelfInfo.getInstance().getId();
			HostWindowTimerTask.timer(this, TIMER_COUNT);
			//上传信息位置到服务器
			System.out.println("homeLiveBundle:"+mBundle);
			if(mBundle!=null && mBundle.containsKey("map_type")){
				mLiveHttpRequest.addLiveToMap( 3,CurLiveInfo.getInstance().getTitle(), "", CurLiveInfo.getInstance().getCoverurl(),
						mBundle.getDouble("latitude"), mBundle.getDouble("longitude"), liveId, UserSharedPreference.getChatUserJson());
			}
			if(mHostWindowActivity.liveType == HostWindowActivity.LIVE_TYPE_NORMAL){
				mHostWindowActivity.mLiveHelper.pushAction();
				mHostWindowActivity.mLiveHelper.startRecord();
			}else if(mHostWindowActivity.liveType == HostWindowActivity.LIVE_TYPE_PACK){
				// 背包Http
				mLiveHttpRequest.notifyServerPackLiveStart();
			}else if(mHostWindowActivity.liveType == HostWindowActivity.LIVE_TYPE_LVB){
//				mHostWindowActivity.lvbLiveHelper.startPush("rtmp://4361.livepush.myqcloud.com/live/4361_742781b9a6?bizid=4361&txSecret=1584512e26f61d96d9a498f73fe7dc86&txTime=58C17BFF");
				mHostWindowActivity.lvbLiveHelper.startPush(mHostWindowActivity.mLvbLiveUrl);
			}
			break;

		case TIMER_COUNT:
			//直播时间更新
			int times =  (Integer) msg.obj;
			timeLive.setText(DateFormatUtil.longToStringByhhmmss(times));
			break;
		case CLOSE_LIVE://关闭直播主窗口
			if(mHostWindowActivity.mLiveHelper != null
					&& mHostWindowActivity.mLiveHelper.isRecording()){
				mHostWindowActivity.mLiveHelper.stopRecord();
				mHostWindowActivity.mLiveHelper.stopPushAction();
			}/*else{
				mHostWindowActivity.onFinish();
			}*/
			mHostWindowActivity.onFinish();
			break;

		case CLOSE_IMAGEVIEW_SHOW:
			//销毁关闭处理内容显示页
			if(liveFinishLayout.getChildCount()>0)
				liveFinishLayout.removeAllViews();
			finishLive.setClickable(true);
			break;

		case INSER_CUT_ACCEPT:
			//接受插播处理逻辑
			UserData userData = (UserData) msg.obj;
			//判断是否是
			for (int i = mInterCutWaitList.size() - 1; i >= 0; i--) {
				if(mInterCutWaitList.get(i).getId().equals(userData.getId())){
					mInterCutWaitList.remove(i);
					refreshInterCutQueueNum();
					if(mInterCutWaitList.size() == 0)
						waitDialog.dismiss();
					break;
				}
			}
			mHostWindowActivity.mLiveHelper.sendAnswerToApplyInsertLine(HLLXLoginHelper.PREFIX + userData.getId(), true);
			break;
		case INSER_CUT_REFUSE:
			//拒绝插播
			userData = (UserData) msg.obj;
			//踢出插播排队列表中被拒绝的用户
			for (int i = mInterCutWaitList.size() - 1; i >= 0; i--) {
				if(mInterCutWaitList.get(i).getId().equals(userData.getId())){
					mInterCutWaitList.remove(i);
					refreshInterCutQueueNum();
					if(mInterCutWaitList.size() == 0)
						waitDialog.dismiss();
					break;
				}
			}
			mHostWindowActivity.mLiveHelper.sendAnswerToApplyInsertLine(HLLXLoginHelper.PREFIX + userData.getId(), false);
			break;
		default:
			break;
		}
	}


	public HostWindowHandler(HostWindowActivity hostActivity , final Bundle bundle){
		super(hostActivity);
		this.mBundle = bundle;
        
        fragmentManager= hostActivity.getFragmentManager();
        //插播头像列表
        mInterCutWaitList =new ArrayList<UserData>();
        userId = UserSharedPreference.getUserId();
		
		if(userId==null || "".equals(userId.trim()))
			userId = "-1";
		headImgUrl = UserSharedPreference.getUserHeading();
		
		if(headImgUrl==null || "".equals(headImgUrl.trim()))
			headImgUrl = "-1";

        //初始化组件
        initView();
		afterInitView();
		addLiveReadyFragment(bundle);
        //添加监听器
        addListener();
		mLiveHttpRequest = new LiveHttpRequest(mHostWindowActivity, this);
	}

	public void onApplyInsertLine(UserData userData){
		boolean isUserDataInList = false;
		for(UserData tmp : mInterCutWaitList){
			if(userData.getId().equals(tmp.getId())){
				isUserDataInList = true;
				break;
			}
		}
		if(!isUserDataInList){
			mInterCutWaitList.add(userData);
			refreshInterCutQueueNum();
		}
		if(mHostWindowActivity.mAVUIControl.getIdelNum() == 0){
			Toast.makeText(mHostWindowActivity, "插播人数已达上限", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onUserExistRoom(UserData userData) {
		super.onUserExistRoom(userData);
		onCancelInsert(userData.getId());
	}

	public void onCancelInsert(final String userId) {
		post(new Runnable() {
			@Override
			public void run() {
				for(UserData tmp : mInterCutWaitList){
					if(userId.equals(tmp.getId())){
						mInterCutWaitList.remove(tmp);
						refreshInterCutQueueNum();
						break;
					}
				}
			}
		});
	}

	@Override
	public boolean onBackPressed() {
		if(mLiveHttpRequest.isServerKnowThisLive()){
			if(liveFinishLayout.getVisibility() == View.VISIBLE){
				hideFinishFragment();
				return true;
			}
			showLiveFinishLayout();
			return true;
		}else{
			return false;
		}
	}

	@Override
	protected void onCloseSmallVideoViewClick(int position) {
		String remoteId = mHostWindowActivity.mAVUIControl.getIdentifierByIndex(position);
		if(!TextUtils.isEmpty(remoteId)){
			mHostWindowActivity.mLiveView.closeMemberView(remoteId);
			mHostWindowActivity.mLiveHelper.sendCancelInteract(remoteId);
		}
	}
	
	private void addListener() {
		
//		barrageSendButton.setBackgroundResource(R.drawable.send_barrage_grey);
//		barrageSendEdit.addTextChangedListener(new TextWatcher() {
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				if(s.length()>0){
//					barrageSendButton.setBackgroundResource(R.drawable.send_barrage_icon);
//				}else{
//					barrageSendButton.setBackgroundResource(R.drawable.send_barrage_grey);
//				}
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//			}
//
//			@Override
//			public void afterTextChanged(Editable s) {
//
//			}
//		});
		shield.setOnClickListener(new ShieldListener());
		shareVideo.setOnClickListener(new ShareLiveListener());
		finishLive.setOnClickListener(new CloseRecordLictener());
		chat_write.setOnClickListener(new ChatListener());
		inserCutCall.setOnClickListener(new InserCutCallListener());
		mBeautifyImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BeautifyPopupView beautifyPopupView = new BeautifyPopupView(mHostWindowActivity, new BeautifyPopupView.Listener() {
					@Override
					public void onProgressChanged(boolean isBeautify, int value) {
						int realValue = value / 10;
						MLog.v(TAG, "onProgressChanged and isBeautify is %b, and value is %d.", isBeautify, value);
						if(isBeautify){
							if(mHostWindowActivity.liveType == HostWindowActivity.LIVE_TYPE_NORMAL)
								mHostWindowActivity.mLiveHelper.setBeautyParam(realValue);
							else
								mHostWindowActivity.lvbLiveHelper.setBeauty(realValue, -1);
						}else{
							if(mHostWindowActivity.liveType == HostWindowActivity.LIVE_TYPE_NORMAL)
								mHostWindowActivity.mLiveHelper.setWhiteBalanceParam(realValue);
							else
								mHostWindowActivity.lvbLiveHelper.setBeauty(-1, realValue);
						}
					}
				});
				int beatifyPara = (int) (mHostWindowActivity.mLiveHelper.getBeautyParam() * 10);
				int whitePara = (int) (mHostWindowActivity.mLiveHelper.getWhiteBalanceParam() * 10);
				beautifyPopupView.show(mBeautifyImage, beatifyPara, whitePara);
			}
		});
		
		//点击观看人头像监听
		watchListView.setOnItemClickListener(new OnItemClickListener() {
            @Override  
            public void onItemClick(AdapterView<?> parent, View view,  
                    int position, long id) {  
            
            }
        });
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		View hostUIView = mHostWindowActivity.findViewById(R.id.include_video_live_host);
		giftRelativeLayout = (GiftRelativeLayout) hostUIView.findViewById(R.id.giftLayout);
		reLayout = (RelativeLayout) hostUIView.findViewById(R.id.relayout);

		layoutHead = (LinearLayout) hostUIView.findViewById(R.id.layoutHead);
		liveHeadImage = (ImageView) hostUIView.findViewById(R.id.liveHeadImg);
		lookingNum = (TextView) hostUIView.findViewById(R.id.looking_num);
		totalNum = (TextView) hostUIView.findViewById(R.id.total_num);
		timeLive = (TextView) hostUIView.findViewById(R.id.time_live);

		finishLive = (ImageView) hostUIView.findViewById(R.id.finish_live);
		mBeautifyImage = (ImageView) hostUIView.findViewById(R.id.iv_beautify);
		changeCamera = (ImageView) hostUIView.findViewById(R.id.change_camera);
		changeLight = (ImageView) hostUIView.findViewById(R.id.change_light);

		mHeartLayout = (HeartLayout) hostUIView.findViewById(R.id.heart_layout);
		zanLinearLayout = (LinearLayout) hostUIView.findViewById(R.id.zanLinearLayout);
		zan = (ImageView) hostUIView.findViewById(R.id.zan_click);

		chat_write = (ImageView) hostUIView.findViewById(R.id.chat_write);
		shield = (ImageView) hostUIView.findViewById(R.id.shield);
		shareVideo = (ImageView) hostUIView.findViewById(R.id.shareVideo);
		soundOn = (ImageView) hostUIView.findViewById(R.id.sound_on);

		zanLayout = (RelativeLayout) hostUIView.findViewById(R.id.zanLayout);
		zanNum = (TextView) hostUIView.findViewById(R.id.zan_num);
		//弹幕布局
		barrageLayout = (RelativeLayout) hostUIView.findViewById(R.id.barrageLayout);
		barrageLayout.getLayoutParams().height = (int) (OSUtil.getScreenHeight()/3);
		barrageListView = (ListView) hostUIView.findViewById(R.id.barrageListView);
		//发送弹幕布局
		barrageSendLayout = (RelativeLayout) hostUIView.findViewById(R.id.barrage_send_layout);
		barrageSendEdit = (EditText) hostUIView.findViewById(R.id.barrage_content_edit);
		barrageSendButton = (Button) hostUIView.findViewById(R.id.barrage_send_button);
		liveFinishLayout = (RelativeLayout) hostUIView.findViewById(R.id.liveFinishLayout);
		//观看头像列表watchListView
		watchListView = (HorizontalListView) hostUIView.findViewById(R.id.watchListView);
		//插播布局
		callLayout = (RelativeLayout) hostUIView.findViewById(R.id.callLayout);
		inserCutNum = (TextView) hostUIView.findViewById(R.id.inserCutNum);
		inserCutNum.setVisibility(View.GONE);
		inserCutCall = (ImageView) hostUIView.findViewById(R.id.inserCutCalls);

		//插播排队列表
		waitDialog = new WaitIntercutDialog(mHostWindowActivity,new WaitIntercutDialog.OnItemClickListener(){
			@Override
			public void onClick(boolean isReceive,int position) {
				if(isReceive && mIdeNum <= 2){
					Toast.makeText(mHostWindowActivity, "连线用户数量已达到上限...", Toast.LENGTH_SHORT).show();
				}else{
					UserData map = mInterCutWaitList.get(position);
					if(isReceive){
						mIdeNum--;
						Message msg = new Message();
						msg.what = INSER_CUT_ACCEPT;
						msg.obj = map;
						sendMessage(msg);
					}else{
						Message msg = new Message();
						msg.what = INSER_CUT_REFUSE;
						msg.obj = map;
						sendMessage(msg);
					}
				}
			}
		});

		//头像
		ImageDisplayTools.displayHeadImage(headImgUrl, liveHeadImage);
		if (!OSUtil.isDayTheme())
			liveHeadImage.setColorFilter(TravelUtil.getColorFilter(mHostWindowActivity));
		if(mHostWindowActivity.liveType == HostWindowActivity.LIVE_TYPE_PACK){
			mBeautifyImage.setVisibility(View.GONE);
			changeCamera.setVisibility(View.GONE);
			changeLight.setVisibility(View.GONE);
			soundOn.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onLineInsertCut(){
		inserCutCall.setVisibility(View.GONE);
	}

	@Override
	public void onResume() {
		removeMessages(CLOSE_LIVE);
	}

	@Override
	public void onPause() {
		// 3分钟后关闭
		sendEmptyMessageDelayed(CLOSE_LIVE, 180000);
	}

	private void refreshInterCutQueueNum() {
		if(mInterCutWaitList.size() < 1){
			inserCutNum.setVisibility(View.GONE);
			inserCutCall.setImageResource(R.drawable.intercut_called_white_host);
		}else{
			inserCutNum.setText(mInterCutWaitList.size()+"");
			inserCutNum.setVisibility(View.VISIBLE);
		}
		waitDialog.setData(mInterCutWaitList);
	}

	/* 推流连接获得, 通知服务器 */
	public void onGotPushStreamUrl(String url){
		MLog.v(TAG, "onGotPushStreamUrl, and url is %s", url);
		CurLiveInfo.getInstance().setRtmpAddress(url);
		CurLiveInfo.getInstance().setLiveType(HostWindowActivity.LIVE_TYPE_NORMAL);
		mLiveHttpRequest.notifyServerLiveStart();
	}

	@Override
	public void onNotifyServerLiveStartResult(boolean isSuccess) {
		MLog.d(TAG,"onNotifyServerLiveStartResult, " + isSuccess);
		if(!isSuccess){
			Toast.makeText(mHostWindowActivity, "开始直播失败， 请稍后重试", Toast.LENGTH_SHORT).show();
			mHostWindowActivity.finish();
			return;
		}

		if(liveType == HostWindowActivity.LIVE_TYPE_LVB){
			HeartBeatHelper.getInstance().setStatus(1).startSendHeartBeat();
		}
	}

	@Override
	public void onNotifyServerLiveStopResult(boolean isSuccess) {
		MLog.d(TAG, "onNotifyServerLiveStopResult, " + isSuccess);
	}

	private class InserCutCallListener implements OnClickListener{
		//点击插播提示按钮
		@Override
		public void onClick(View v) {
			if(waitDialog == null) return;

			if(inserCutNum.getVisibility() == View.VISIBLE){
				int interCutWaiNum = mInterCutWaitList.size();
				inserCutNum.setText(interCutWaiNum+"");
				if(waitDialog.isShowing()){
					waitDialog.dismiss();
				}else{
					waitDialog.show();
					mIdeNum = mHostWindowActivity.mAVUIControl.getIdelNum();
					MLog.d(TAG, "showWaitDialog, and ideNum is %d.", mIdeNum);
				}
			}else{
				Toast.makeText(mHostWindowActivity, "当前没有人申请连线！", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private void addLiveReadyFragment(Bundle bundle){
		reLayout.setVisibility(View.GONE);
		
		finishLive.setClickable(false);
		FragmentTransaction finishTransaction = fragmentManager.beginTransaction();
		Fragment fragment  = fragmentManager.findFragmentByTag("liveReadyFragment");
		if(fragment == null){
			liveReadyFragment = new LiveReadyFragment();
		}else{
			liveReadyFragment = (LiveReadyFragment) fragment;
		}
		liveReadyFragment.setArguments(bundle);
        finishTransaction.replace(R.id.liveFinishLayout, liveReadyFragment, "liveReadyFragment");
        finishTransaction.addToBackStack(null);
        finishTransaction.commit();//提交事务
	}
	
	/**
	 * 当点击直播结束按钮时，显示关闭处理页内容
	 */
	private void showLiveFinishLayout(){
		MLog.v(TAG, "showLiveFinishLayout");
		liveFinishLayout.setVisibility(View.VISIBLE);
		if(liveFinishLayout.getChildCount()>0){
			liveFinishLayout.removeAllViews();
		}
		finishLive.setClickable(false);
		liveFinishLayout.setVisibility(View.VISIBLE);
		
		FragmentTransaction finishTransaction = fragmentManager.beginTransaction();
		liveFinishFragment = new LiveFinishFragment();
		Bundle bundle = new Bundle();
		bundle.putString("live_id", MySelfInfo.getInstance().getId());
		bundle.putString("live_share", CurLiveInfo.getInstance().getShare());
        bundle.putString("live_title", CurLiveInfo.getInstance().getTitle());
        bundle.putString("live_cover", CurLiveInfo.getInstance().getCoverurl());
        bundle.putString("live_time",timeLive.getText().toString());
        bundle.putString("live_totalNum",totalNum.getText().toString());
        bundle.putString("live_zanNum",zanNum.getText().toString());
        bundle.putString("live_barrageNum", barrageInfos.size()+"");
        liveFinishFragment.setArguments(bundle);
        //替换id为R.id.right_layout里面的内容
        finishTransaction.replace(R.id.liveFinishLayout, liveFinishFragment);
        finishTransaction.addToBackStack(null);
        finishTransaction.commit();//提交事务
	}

	private LiveFinishFragment liveFinishFragment;
	public void hideFinishFragment(){
		liveFinishLayout.setVisibility(View.GONE);
		if(liveFinishFragment!=null && liveFinishFragment.isAdded()) {
			FragmentTransaction finishTransaction = fragmentManager.beginTransaction();
			finishTransaction.remove(liveFinishFragment);
			liveFinishLayout.removeAllViews();
			liveFinishFragment.onDestroy();
		}
	}

	private class CloseRecordLictener implements OnClickListener{
		//停止直播按钮监听器
		@Override
		public void onClick(View v) {
			showLiveFinishLayout();
		}
	}
	
	private class ShieldListener implements OnClickListener{
		//屏蔽其他界面
		@Override
		public void onClick(View v) {
			if(zanLayout.getVisibility() == View.VISIBLE){
				layoutHead.setVisibility(View.INVISIBLE);
				if(mHostWindowActivity.liveType != HostWindowActivity.LIVE_TYPE_PACK){
					changeCamera.setVisibility(View.INVISIBLE);
					changeLight.setVisibility(View.INVISIBLE);
					soundOn.setVisibility(View.INVISIBLE);
				}
				zanLayout.setVisibility(View.INVISIBLE);
				zanLinearLayout.setVisibility(View.INVISIBLE);
				barrageLayout.setVisibility(View.INVISIBLE);
				callLayout.setVisibility(View.INVISIBLE);
				watchListView.setVisibility(View.INVISIBLE);
				shareVideo.setVisibility(View.INVISIBLE);
				giftRelativeLayout.setVisibility(View.INVISIBLE);
				if(mHostWindowActivity.liveType != HostWindowActivity.LIVE_TYPE_PACK){
					mBeautifyImage.setVisibility(View.INVISIBLE);
				}
				chat_write.setVisibility(View.GONE);
				shield.setImageResource(R.drawable.live_point_shield);
			}else if(zanLayout.getVisibility() == View.INVISIBLE){
				layoutHead.setVisibility(View.VISIBLE);
				if(mHostWindowActivity.liveType != HostWindowActivity.LIVE_TYPE_PACK){
					changeCamera.setVisibility(View.VISIBLE);
					changeLight.setVisibility(View.VISIBLE);
					soundOn.setVisibility(View.VISIBLE);
				}
				zanLayout.setVisibility(View.VISIBLE);
				zanLinearLayout.setVisibility(View.VISIBLE);
				giftRelativeLayout.setVisibility(View.VISIBLE);
				chat_write.setVisibility(View.VISIBLE);
				barrageLayout.setVisibility(View.VISIBLE);
				callLayout.setVisibility(View.VISIBLE);
				watchListView.setVisibility(View.VISIBLE);
				if(mHostWindowActivity.liveType != HostWindowActivity.LIVE_TYPE_PACK){
					mBeautifyImage.setVisibility(View.VISIBLE);
				}
				shareVideo.setVisibility(View.VISIBLE);
				shield.setImageResource(R.drawable.live_point_shield_none);
			}
			
		}
	}

	private class ChatListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			barrageSendLayout.setVisibility(View.VISIBLE);
			barrageSendEdit.requestFocus();
			OSUtil.showKeyboard(mHostWindowActivity,barrageSendEdit);
		}
	}
}