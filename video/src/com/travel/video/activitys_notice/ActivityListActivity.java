package com.travel.video.activitys_notice;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.travel.Constants;
import com.travel.bean.ActivitysBean;
import com.travel.bean.VideoInfoBean;
import com.travel.layout.MyGridView;
import com.travel.lib.helper.PullToRefreshHelper;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.video.adapter.VideoAdapter;
import com.travel.video.help.VideoIntentHelper;
import com.travel.VideoConstant;
import com.travel.video.tools.LiveUtils;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 活动内容页，包括活动介绍，活动列表等
 * @author sheng
 */
public class ActivityListActivity extends TitleBarBaseActivity {
	private ImageView startLive,activityCover, ellipImage,goVoteImage;
	private TextView activityTimes, activityContent, activityContents;
	private PullToRefreshScrollView pullScrollView;
	private MyGridView gridview;
	private List<VideoInfoBean> activityList;
	private VideoAdapter gridAdapter;

	private ActivitysBean activitysBean;
	private int screenWidth;

	private int times = 1;
	private String activityId = "";
	private boolean isOpenText = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activitys);
		screenWidth = OSUtil.getScreenWidth();
		activitysBean = (ActivitysBean) getIntent().getBundleExtra("activitys_bean").get("activitys_bean");
		activityId = activitysBean.getId();

		initView();
		scrollViewInit();
		setListener();
		
		gridview.setFocusable(false);
		gridview.setFocusableInTouchMode(false);
		activityCover.requestFocus();
		
		if(activityList!=null){
			activityList.clear();
		}
		times = 1;
		getActivitysVideos();
	}

	private void setListener() {
		ellipImage.setOnClickListener(new EllipImageListener());
		goVoteImage.setOnClickListener(new GoVoteListener());

		ViewTreeObserver vto = activityContents.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (activityContents.getLineCount() <= 4) {
					ellipImage.setVisibility(View.GONE);
					activityContent.setVisibility(View.GONE);
				}else{
					activityContents.setVisibility(View.GONE);
				}
			}
		});

//		if(!"-1".equals(activitysBean.getShareUrl())){
//			OSUtil.setShareParam(rightButton, "share");
//			rightButton.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					OSUtil.showShare(null, "红了旅行", activitysBean.getTitle(),
//							activitysBean.getImgUrl(), activitysBean.getShareUrl(), activitysBean.getShareUrl());
//				}
//			});
//		}

		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				new VideoIntentHelper(ActivityListActivity.this).intentWatchVideo(activityList.get(arg2),gridview);
			}
		});

		startLive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("activity_id", activityId);
				LiveUtils.GoLiveClick(ActivityListActivity.this, bundle);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	/**
	 * 初始化布局控件
	 */
	private void initView() {
		activityCover = (ImageView) findViewById(R.id.activityCover);
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) activityCover.getLayoutParams();
		params.width = screenWidth;
		params.height = screenWidth * 2 / 3;
		activityCover.setLayoutParams(params);

		startLive = (ImageView) findViewById(R.id.startLive);
		activityTimes = (TextView) findViewById(R.id.activityTimes);
		activityContent = (TextView) findViewById(R.id.activityContent);
		activityContents = (TextView) findViewById(R.id.activityContents);
		ellipImage = (ImageView) findViewById(R.id.ellipImage);
		goVoteImage = (ImageView) findViewById(R.id.goVoteImage);
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, 
				(OSUtil.getScreenWidth()-OSUtil.dp2px(getApplicationContext(), 20))*340/991);
		goVoteImage.setLayoutParams(param);

		long data = new Date().getTime();
		long startTime = DateFormatUtil.getLongByStringDate(DateFormatUtil.FORMAT_TIME, activitysBean.getStartTime() + " 00:00:00");
		long endTime = DateFormatUtil.getLongByStringDate(DateFormatUtil.FORMAT_TIME, activitysBean.getEndTime() + " 00:00:00");
		if(data<startTime || data>endTime){
			startLive.setVisibility(View.GONE);
		}
		activityList = new ArrayList<VideoInfoBean>();
		gridview = (MyGridView) findViewById(R.id.activitysGridView);
		gridview.setColumnWidth(gridview.getWidth() / 2);// 设置条目的宽度
		gridAdapter = new VideoAdapter(this, activityList);

		pullScrollView = (PullToRefreshScrollView) findViewById(R.id.pull_refresh_scrollview_activitys);
		titleText.setText(activitysBean.getTitle());
		ImageDisplayTools.displayImage(activitysBean.getImgUrl(), activityCover);
		activityTimes.setText(" 活动时间： " + activitysBean.getStartTime() + " ~ "
				+ activitysBean.getEndTime());
		activityContent.setText(" 活动介绍： " + activitysBean.getContent());
		activityContents.setText(" 活动介绍： " + activitysBean.getContent());
	}

	private class EllipImageListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (!isOpenText) {// 没有展开
				isOpenText = true;
				activityContent.setEllipsize(null); // 展开
				activityContent.setSingleLine(false);
				ellipImage.setImageResource(R.drawable.ellip_close_icon);
			} else {// 展开
				isOpenText = false;
				activityContent.setEllipsize(TextUtils.TruncateAt.END); // 收缩
				// activityContent.setSingleLine(true);
				activityContent.setLines(4);
				ellipImage.setImageResource(R.drawable.ellip_open_icon);
			}
		}
	}
	
	private class GoVoteListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(ActivityListActivity.this, ActivitysVoteRankActivity.class);
			intent.putExtra("activityId", activityId);
			startActivity(intent);
		}
	}
	
	private void scrollViewInit() {
		PullToRefreshHelper ptrHelper = new PullToRefreshHelper(pullScrollView);
		ptrHelper.initPullDownToRefreshView(null);
		ptrHelper.initPullUpToRefreshView(null);
		pullScrollView.setMode(PullToRefreshBase.Mode.BOTH);
		ptrHelper.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				pullDownToRefresh();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				pullUpToRefresh();
			}
		});
		gridview.setAdapter(gridAdapter);
		gridAdapter.notifyDataSetChanged();
		pullScrollView.onRefreshComplete();
	}

	private void pullDownToRefresh() {
		times = 1;
		activityList.clear();
		getActivitysVideos();
	}

	private void pullUpToRefresh() {
		if (activityList != null && activityList.size() > 0 && activityList.size() % Constants.ItemNum == 0) {
			times = times + 1;
			getActivitysVideos();
		} else if (activityList != null && activityList.size() > 0
				&& activityList.size() % Constants.ItemNum != 0) {
			showToast(R.string.no_more);
			pullScrollView.onRefreshComplete();
		} else if (activityList != null && activityList.size() == 0) {
			times = 1;
			getActivitysVideos();
		}
	}

	private void getActivitysVideos() {
		Map<String, Object> paramap = new HashMap<String, Object>();
		paramap.put("times", times + "");
		paramap.put("activityId", activityId);
		NetWorkUtil.postForm(ActivityListActivity.this, VideoConstant.VIDEO_LIST, new MResponseListener() {
			@Override
			protected void onDataFine(JSONArray data) {
				if (data.length() < 1) {
					pullScrollView.onRefreshComplete();
					return;
				}
				try {
					int listSize = activityList.size() % Constants.ItemNum;
					if (listSize > 0) {
						for (int i = 0; i < listSize; i++) {
							if (activityList.size() > 0)
								activityList.remove(activityList.size() - 1);
						}
					}
					JSONArray live_list = data;// 普通商品
					for (int i = 0; i < live_list.length(); i++) {
						JSONObject live = live_list.getJSONObject(i);
						VideoInfoBean bean = new VideoInfoBean().getVideoInfoBean(live);
						activityList.add(bean);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				pullScrollView.onRefreshComplete();
				gridAdapter.notifyDataSetChanged();
			}
			
			@Override
			protected void onNetComplete() {
				super.onNetComplete();
				pullScrollView.onRefreshComplete();
			}
			
			@Override
			public void onErrorResponse(VolleyError error) {
				super.onErrorResponse(error);
				pullScrollView.onRefreshComplete();
			}
			
		}, paramap);
	}
}