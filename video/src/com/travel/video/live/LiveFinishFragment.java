package com.travel.video.live;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.travel.VideoConstant;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.video.playback_video.PlaybackVideoPlayerActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * 主播结束直播时的页面显示
 * @author Administrator
 */
public class LiveFinishFragment extends Fragment{
	private Activity context;
	private View view;
	private Bundle bundle;
	private Button closeLive,shareButton;
	private ImageView cover, cancle;
	private TextView titleText,timeText,totalText,zanText,barrageNumText;
	
	private ImageView wechatFavorite,wechat,qq,qZone;
	private String shareUrl = "";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.live_finish, container, false);
		context = getActivity();
		bundle = getArguments();
		shareUrl = VideoConstant.SHARE_VIDEO_URL + CurLiveInfo.getInstance().getShare();
		initView();
		return view;
	}
	private void initView() {
		view.findViewById(R.id.live_finish_relayout).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

			}
		});
		cancle = (ImageView) view.findViewById(R.id.cancle);
		closeLive = (Button) view.findViewById(R.id.closeLive);
		cover = (ImageView) view.findViewById(R.id.liveCoverImg);
		titleText = (TextView) view.findViewById(R.id.liveTitle);
		timeText = (TextView) view.findViewById(R.id.liveTime);
		totalText = (TextView) view.findViewById(R.id.liveTotalNum);
		zanText = (TextView) view.findViewById(R.id.liveZanNum);
		barrageNumText = (TextView) view.findViewById(R.id.liveBarrageNum);
		shareButton = (Button) view.findViewById(R.id.shareButton);

//		ImageDisplayTools.initImageLoader(activity);
		ImageDisplayTools.displayImage(bundle.getString("live_cover"), cover);
		titleText.setText(bundle.getString("live_title"));
//		timeText.setText(bundle.getString("live_time"));
		totalText.setText(bundle.getString("live_totalNum"));
		zanText.setText(bundle.getString("live_zanNum"));
//		barrageNumText.setText(bundle.getString("live_barrageNum"));
		closeLive.setOnClickListener(new CloseLiveListener());
		cancle.setOnClickListener(new CancleListener());
//		shareButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				view.findViewById(R.id.shareLayout).setVisibility(View.VISIBLE);
//			}
//		});
		
		wechatFavorite = (ImageView) view.findViewById(R.id.wechatFavorite);
		wechat = (ImageView) view.findViewById(R.id.wechat);
		qq = (ImageView) view.findViewById(R.id.qq);
		qZone = (ImageView) view.findViewById(R.id.qZone);
		wechatFavorite.setOnClickListener(new ShareListener());
		wechat.setOnClickListener(new ShareListener());
		qq.setOnClickListener(new ShareListener());
		qZone.setOnClickListener(new ShareListener());
	}
	
	private class CloseLiveListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if(getActivity() instanceof HostWindowActivity){
				((HostWindowActivity)getActivity()).mUIHandler.sendEmptyMessage(HostWindowHandler.CLOSE_LIVE);
			}
		}
	}
	private class CancleListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			LiveFinishFragment.this.onDestroy();
		}
	}
	
	private class ShareListener implements OnClickListener{
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

			OSUtil.showShare(platform,titleText.getText().toString(), titleText.getText().toString(), bundle.getString("live_cover"), shareUrl, shareUrl, context);
		}
		
	}
	
	private class PlaybackListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			getPlaybackVideoUrl();
		}
	}
	
	private void getPlaybackVideoUrl(){
		Map<String,Object> paramap = new HashMap<String,Object>();
		paramap.put("hashId", bundle.get("live_id"));
		NetWorkUtil.postForm(context, VideoConstant.GET_VIDEO_URL, new MResponseListener() {
			@Override
			protected void onDataFine(JSONObject data) {
				System.out.println(data.toString());
				String videoId = JsonUtil.getJson(data, "videoId");
				String url = JsonUtil.getJson(data, "url");
				if("-1".equals(videoId) || "-1".equals(url)){
					Toast.makeText(context, "直播时间太短，请稍后重试！", Toast.LENGTH_SHORT).show();
					return;
				}
				Intent intent = new Intent();
				Bundle mBundle = new Bundle();
				mBundle.putString("id", videoId);
				mBundle.putString("coverImage", bundle.getString("live_cover"));
				mBundle.putString("liveTitleName", bundle.getString("live_title"));
				mBundle.putString("liveType", "1");
				mBundle.putString("userName", UserSharedPreference.getNickName());
				mBundle.putString("url", url);
				mBundle.putString("size", "");
				mBundle.putString("headImg", UserSharedPreference.getUserHeading());
				mBundle.putString("videoUserId",UserSharedPreference.getUserId());
				mBundle.putString("userType", UserSharedPreference.getUserType());
				intent.putExtras(mBundle);
				intent.setClass(context, PlaybackVideoPlayerActivity.class);
				startActivity(intent);
				if(getActivity() instanceof HostWindowActivity){
					((HostWindowActivity)getActivity()).mUIHandler.sendEmptyMessage(HostWindowHandler.CLOSE_LIVE);
				}
			}
		}, paramap);
	}
	
	@Override
	public void onDestroy() {
		if(getActivity() instanceof HostWindowActivity){
			((HostWindowActivity)getActivity()).mUIHandler.sendEmptyMessage(HostWindowHandler.CLOSE_IMAGEVIEW_SHOW);
		}
		super.onDestroy();
	}
}
