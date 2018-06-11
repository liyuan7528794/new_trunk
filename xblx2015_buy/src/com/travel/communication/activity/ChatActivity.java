package com.travel.communication.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ctsmedia.hltravel.R;
import com.google.gson.JsonObject;
import com.travel.activity.GroupMemberActivity;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.VideoInfoBean;
import com.travel.communication.dao.Message;
import com.travel.communication.entity.UserData;
import com.travel.communication.fragment.ChatFragment;
import com.travel.communication.helper.SQliteHelper;
import com.travel.communication.utils.GoodsInfoBeanJsonUtil;
import com.travel.imserver.ResultCallback2;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.http.LiveInfoHttp;
import com.travel.video.live.HostWindowActivity;

import java.util.ArrayList;
import java.util.List;

import static com.tencent.connect.common.Constants.ACTIVITY_OK;
import static com.travel.communication.helper.GroupMessageSenderHelper.ACTION_GROUP_INFO;
import static com.travel.communication.helper.GroupMessageSenderHelper.ACTION_IS_FOLLOW_THIS_GROUP;
import static com.travel.communication.helper.TIMGroupMessageReceiver.ACTION_ANTHER_END_LIVE;
import static com.travel.communication.helper.TIMGroupMessageReceiver.ACTION_ANTHER_START_LIVE;
import static com.travel.communication.helper.TIMGroupMessageReceiver.ACTION_END_LIVE;
import static com.travel.communication.helper.TIMGroupMessageReceiver.ACTION_START_LIVE;

/**
 * 聊天界面
 */
public class ChatActivity extends TitleBarBaseActivity /*implements MessageListener */{
	private static final String TAG = "ChatActivity";

	/**
	 * 用于传值, 暂时用作发送者ID
	 */
	public static final String ID = "id";
	public static final String NICK_NAME = "nick_name";
	public static final String IMG_URL = "img_url";
	public static final String GOODS = "goods_info";
	public static final String IS_GROUP_CHAT = "is_group_chat";

	private ChatFragment mChatFragment;
	private SQliteHelper mSQliteHelper;

	private GoodsBasicInfoBean mGoodsBasicInfoBean;
	// 我要发送给对方的Id
	private String mReceiverId;
	private boolean mIsGroupChat = true; // 是否是群聊
	private String mGroupName;

	private LocalBroadcastManager mLocalBroadcastManager;

	// 当前直播的用户部分
	private List<VideoInfoBean> mCurrentLiveList = new ArrayList<>();
	private View mLiveLayout;
	private MLiveAdapter mLiveAdapter;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		mLiveLayout = findViewById(R.id.ll_live_layout);
		RecyclerView mLiveListRecyclerView = findView(R.id.rc_live_list);
		mLiveListRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
		mLiveAdapter = new MLiveAdapter();
		mLiveListRecyclerView.setAdapter(mLiveAdapter);
		mChatFragment = new ChatFragment();
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_container, mChatFragment)
				.commit();
		init(getIntent());
	}

	private void init(Intent intent) {
		initUserData(intent);
		if (mIsGroupChat){
			// 广播初始化
			initBroadcastListener();
			// 获取正在直播的网络列表
			getCurrentLiveList();
		}
		mGoodsBasicInfoBean = (GoodsBasicInfoBean) intent.getSerializableExtra(GOODS);
		if(mGoodsBasicInfoBean == null) return;
		Message message = mSQliteHelper.getLastGoodsMessage(mReceiverId, UserSharedPreference.getUserId());
		boolean isSendGoodsMessage = message == null;
		if(!isSendGoodsMessage){
			GoodsBasicInfoBean bean = GoodsInfoBeanJsonUtil.from(message.getContent());
			if(!mGoodsBasicInfoBean.getGoodsId().equals(bean.getGoodsId())){
				isSendGoodsMessage = true;
			}
		}
		if(isSendGoodsMessage){
			mChatFragment.sendGoodsInfoMessage(mGoodsBasicInfoBean);
		}
	}

	private void getCurrentLiveList() {
		LiveInfoHttp.getGroupLiveList(this, mGroupName, new ResultCallback2<List<VideoInfoBean>>() {
			@Override
			public void onError(int errorCode, String errorReason) {
				MLog.d(TAG, "errorCode %d, errorReason:%s", errorCode, errorReason);
			}

			@Override
			public void onResult(List<VideoInfoBean> obj) {
				mCurrentLiveList.addAll(obj);
				onLiveListChanged();
			}
		});
	}

	private void initBroadcastListener() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_START_LIVE);
		intentFilter.addAction(ACTION_END_LIVE);
		intentFilter.addAction(ACTION_ANTHER_START_LIVE);
		intentFilter.addAction(ACTION_ANTHER_END_LIVE);
		intentFilter.addAction(ACTION_GROUP_INFO);
		intentFilter.addAction(ACTION_IS_FOLLOW_THIS_GROUP);
		mLocalBroadcastManager.registerReceiver(mLiveEventReceiver, intentFilter);
	}

	@Override
	public void finish() {
		OSUtil.hideKeyboard(this);
		super.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mIsGroupChat){
			unInitBroadcastListener();
		}
	}

	private void unInitBroadcastListener() {
		mLocalBroadcastManager.unregisterReceiver(mLiveEventReceiver);
	}

	private BroadcastReceiver mLiveEventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			VideoInfoBean videoInfoBean = (VideoInfoBean) intent.getSerializableExtra("VideoInfoBean");
			String groupId = intent.getStringExtra("group-id");
			if (groupId != null && !groupId.equals(mReceiverId)){
				// 不是该群的消息
				return;
			}
			if (ACTION_GROUP_INFO.equals(action)){
				String groupName = intent.getStringExtra("group-name");
				setTitle(groupName);
			}else if (ACTION_IS_FOLLOW_THIS_GROUP.equals(action)){
				// 第一次进入群， 没有关注群
				dealWithFirstEnterGroup();
			}else if (ACTION_START_LIVE.equals(action)){
				// 本人开始直播
				sendLiveInfo(videoInfoBean, true);
			}else if(ACTION_END_LIVE.equals(action)){
				// 本人结束直播
				sendLiveInfo(videoInfoBean, false);
			}else if(ACTION_ANTHER_START_LIVE.equals(action)){
				// 其他人开始直播
				for (VideoInfoBean videoInfo : mCurrentLiveList) {
					if (videoInfo.getPersonalInfoBean().getUserId().equals(videoInfoBean.getPersonalInfoBean().getUserId()))
						mCurrentLiveList.remove(videoInfo);
				}
				mCurrentLiveList.add(videoInfoBean);
				onLiveListChanged();
			}else if(ACTION_ANTHER_END_LIVE.equals(action)){
				for (VideoInfoBean bean : mCurrentLiveList){
					if (bean.getPersonalInfoBean() != null
							&& bean.getPersonalInfoBean().getUserId().equals(
							videoInfoBean.getPersonalInfoBean().getUserId())){
						mCurrentLiveList.remove(bean);
					}
				}
				onLiveListChanged();
			}
		}
	};

	private void dealWithFirstEnterGroup() {
		AlertDialogUtils.alertDialog(this, "是否进入群聊", new Runnable() {
			@Override
			public void run() {
				mChatFragment.followGroup(true);
			}
		}, new Runnable() {
			@Override
			public void run() {
				//  默认退出群聊
				finish();
			}
		});
	}

	private void sendLiveInfo(VideoInfoBean videoInfoBean, boolean isStartLive){
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("live-action", isStartLive ? "start-live" : "end-live");
		jsonObject.add("content", videoInfoBean.toJson());
		mChatFragment.sendLiveInfoMessage(jsonObject.toString());
	}

	private void onLiveListChanged(){
		mLiveAdapter.notifyDataSetChanged();
		if (mLiveAdapter.getItemCount() == 0){
			mLiveLayout.setVisibility(View.GONE);
		}else{
			mLiveLayout.setVisibility(View.VISIBLE);
		}
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		init(intent);
	}

	@Override
	public void onBackPressed() {
		if (!mChatFragment.onBackPressed()){
			super.onBackPressed();
		}
	}

	// 初始化自己的UserId, 并获取聊天的ID
	private void initUserData(Intent intent) {
		mReceiverId = intent.getStringExtra(ID);
		mIsGroupChat = intent.getBooleanExtra(IS_GROUP_CHAT, false);
		MLog.d(TAG, "enterChat, and mReceiverId is %s. mIsGroupChat is %b", mReceiverId, mIsGroupChat);
		if (mIsGroupChat){
			mGroupName = intent.getStringExtra(NICK_NAME);
			mChatFragment.setCityName(mGroupName);
			mChatFragment.setChatInfo(UserSharedPreference.getUserId(), null, mReceiverId);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rightButton.getLayoutParams();
			params.width = OSUtil.dp2px(this, 24);
			params.height = OSUtil.dp2px(this, 24);
			params.rightMargin = OSUtil.dp2px(this, 16);
			rightButton.setBackgroundResource(R.drawable.nav_icon_group);
			rightButton.setLayoutParams(params);
			rightButton.setVisibility(View.VISIBLE);
			rightButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					GroupMemberActivity.startActivityForResult(ChatActivity.this, 100, mReceiverId);
				}
			});
		}else{
			mChatFragment.setChatInfo(UserSharedPreference.getUserId(), mReceiverId, null);
		}

		if(TextUtils.isEmpty(mReceiverId)){
			throw new IllegalStateException("you must pass id to me");
		}
		String nickName = intent.getStringExtra(NICK_NAME);
		String imgUrl = intent.getStringExtra(IMG_URL);
		setTitle(nickName);
		mSQliteHelper = new SQliteHelper(this);
		UserData userData = mSQliteHelper.getUserData(mReceiverId);
		if(userData == null || TextUtils.isEmpty(nickName)){
			userData = new UserData();
			userData.setNickName(nickName);
			userData.setImgUrl(imgUrl);
			userData.setId(mReceiverId);
			mSQliteHelper.inserOrReplace(userData);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == ACTIVITY_OK && requestCode == 100){
			// 退出群组
			mChatFragment.followGroup(false);
			finish();
		}
	}

	private class MLiveAdapter extends RecyclerView.Adapter<MLiveViewHolder>{

		@Override
		public MLiveViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View rootView = getLayoutInflater().inflate(R.layout.list_item_live_list, parent, false);
			return new MLiveViewHolder(rootView);
		}

		@Override
		public void onBindViewHolder(final MLiveViewHolder holder, int position) {
			ImageDisplayTools.disPlayRoundDrawableHead(
					getItem(position).getPersonalInfoBean().getUserPhoto(),
					holder.headerView,
					OSUtil.dp2px(holder.itemView.getContext(), 2));
			if (!OSUtil.isDayTheme())
				holder.headerView.setColorFilter(TravelUtil.getColorFilter(holder.itemView.getContext()));
			holder.headerView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					VideoInfoBean videoInfoBean = getItem(holder.getAdapterPosition());
					Intent intent = new Intent(ChatActivity.this, HostWindowActivity.class);
					intent.putExtra(HostWindowActivity.LIVE_IS_HOST, false);
					intent.putExtra("video_info", videoInfoBean);
					startActivity(intent);
				}
			});
		}

		private VideoInfoBean getItem(int adapterPosition){
			return mCurrentLiveList.get(adapterPosition);
		}
		@Override
		public int getItemCount() {
			return mCurrentLiveList == null ? 0 : mCurrentLiveList.size();
		}
	}

	private class MLiveViewHolder extends RecyclerView.ViewHolder{
		ImageView headerView;
		public MLiveViewHolder(View itemView) {
			super(itemView);
			headerView = (ImageView) itemView.findViewById(R.id.iv_head_img);
		}
	}

	private OnGetVoicePermissionListener mOnGetVoicePermissionListener;
	public void setmOnGetVoicePermissionListener(OnGetVoicePermissionListener mOnGetVoicePermissionListener){
		this.mOnGetVoicePermissionListener = mOnGetVoicePermissionListener;
	}
	public interface OnGetVoicePermissionListener{
		void onGetPermission();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode){
			case 1:
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
					if(mOnGetVoicePermissionListener != null){
						mOnGetVoicePermissionListener.onGetPermission();
					}
				}else{
					showToast("您已禁止录音权限");
				}
				break;
			default:
		}
	}
}
