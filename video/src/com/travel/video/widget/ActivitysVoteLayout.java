package com.travel.video.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.VideoConstant;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
public class ActivitysVoteLayout extends RelativeLayout{
	private Context context;
	private View rootView;
	private TextView voteNum,voteButton;
	private String userId,activityId;
	private int count = 0;
	private boolean isVote = true;
	public ActivitysVoteLayout(Context context) {
		this(context, null);
	}
	public ActivitysVoteLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public ActivitysVoteLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
		init();
	}
	
	private void init(){
		rootView = LayoutInflater.from(context).inflate(R.layout.activitys_vote_layout, null);
		voteNum = (TextView) rootView.findViewById(R.id.voteNum);
		voteButton = (TextView) rootView.findViewById(R.id.voteButton);
		voteButton.setOnClickListener(new VoteClickListener());
		addView(rootView, new LayoutParams(LayoutParams.WRAP_CONTENT, OSUtil.dp2px(context, 30)));
	}
	
	public void setVoteNum(){
		voteNum.setText(count+"票");
	}
	
	public void setInitData(String userId,String activityId){
		this.userId = userId;
		this.activityId = activityId;
		
		if(activityId == null || "".equals(activityId) || "-1".equals(activityId)){
			this.setVisibility(View.GONE);
			return;
		}
		initCount();
	}
	
	/** 是否显示投票 */
	public void isShowButton(boolean isShow){
		if(!isShow)
			voteButton.setVisibility(View.GONE);
		else
			voteButton.setVisibility(View.VISIBLE);
	}
	
	private class VoteClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!UserSharedPreference.isLogin()){
				Toast.makeText(context, "请登录后再投票！", Toast.LENGTH_SHORT).show();
				return;
			}
			vote();
		}
	}
	
	/**
	 * 访问网络获取当前投票数
	 */
	private void initCount() {
		
		Map<String, Object> paramap = new HashMap<String, Object>();
		
		if(UserSharedPreference.isLogin()){
			paramap.put("userId", UserSharedPreference.getUserId());
		}
		paramap.put("activityId", activityId);
		paramap.put("liveUser", userId);
		NetWorkUtil.postForm(context, VideoConstant.GET_ACTIVITYS_VOTE_COUNT, new MResponseListener() {
			@Override
			protected void onDataFine(JSONObject data) {
				if(data==null) return;
				count = JsonUtil.getJsonInt(data, "voteCount");
				isVote = JsonUtil.getJsonBoolean(data, "isVote", true);
				setVoteNum();
				if(isVote){
					voteButton.setVisibility(View.GONE);
				}
			}
		}, paramap);
	}
	
	/**
	 * 投票
	 */
	private void vote() {
		Map<String, Object> paramap = new HashMap<String, Object>();
		paramap.put("voteUser", UserSharedPreference.getUserId());
		paramap.put("activityId", activityId);
		paramap.put("liveUser", userId);
		NetWorkUtil.postForm(context, VideoConstant.ACTIVITYS_VOTE_COMMIT, new MResponseListener() {
			@Override
			public void onResponse(JSONObject response) {
				super.onResponse(response);
				if(response.optInt("error") == 0){
					count = count + 1;
					isVote = true;
					setVoteNum();
					voteButton.setVisibility(View.GONE);
				}
			}
		}, paramap);
	}
}
