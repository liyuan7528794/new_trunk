package com.travel.lib.helper;

import android.support.annotation.Nullable;

import com.handmark.pulltorefresh.library.PullToRefreshAdapterViewBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.travel.lib.utils.DateFormatUtil;

import java.util.Date;

/**
 * 下拉上拉辅助类,
 *  初始化以及更新时间自动计时
 */
public class PullToRefreshHelper {
	@SuppressWarnings("unused")
	private static final String TAG = "PullToRefreshHelper";

	private Date mLastUpdatePullDownTime;
	private Date mLastUpdatePullUpTime;
	private PullToRefreshBase.OnRefreshListener mRefreshListener;
	private PullToRefreshBase.OnRefreshListener2 mRefreshListener2;


	private PullToRefreshBase mPullToRefreshView;

	public PullToRefreshHelper(PullToRefreshBase absListView){
		mPullToRefreshView = absListView;
	}

	/**
	 * 设置下拉更新的样式
	 * @param lastUpdateTime  上次更新时间, 可以为null
	 */
	public void initPullDownToRefreshView(Date lastUpdateTime){
		mPullToRefreshView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
		mPullToRefreshView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新…");
		mPullToRefreshView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开刷新…");
		if(lastUpdateTime != null){
			mLastUpdatePullUpTime = lastUpdateTime;
			mPullToRefreshView.getLoadingLayoutProxy().setLastUpdatedLabel(
					"上次更新时间：" + DateFormatUtil.formatTime(mLastUpdatePullDownTime,
							DateFormatUtil.FORMAT_DTAE2_TIME2));
		}
	}

	/**
	 * 初始化上拉加载更多的
     */
	public void initPullUpToRefreshView(@Nullable  Date lastUpdateTime){
		mPullToRefreshView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载");
		mPullToRefreshView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载…");
		mPullToRefreshView.getLoadingLayoutProxy(false, true).setReleaseLabel("放开加载…");
		if(lastUpdateTime != null){
			mLastUpdatePullDownTime = lastUpdateTime;
			mPullToRefreshView.getLoadingLayoutProxy(false, true)
					.setLastUpdatedLabel("上次更新时间："
							+ DateFormatUtil.formatTime(mLastUpdatePullUpTime, DateFormatUtil.FORMAT_DTAE2_TIME2));
		}
	}

	/**
	 * 模仿PullToRefreshBase的方法, 这两个方法用于添加自动记录时间的功能
	 * @param onRefreshListener
     */
	public void setOnRefreshListener(PullToRefreshBase.OnRefreshListener onRefreshListener){
		mRefreshListener = onRefreshListener;
		mPullToRefreshView.setOnRefreshListener(_RefreshListener);
	}

	public void setOnRefreshListener(PullToRefreshBase.OnRefreshListener2 onRefreshListener2){
		mRefreshListener2 = onRefreshListener2;
		mPullToRefreshView.setOnRefreshListener(_RefreshListener2);
	}

	private PullToRefreshBase.OnRefreshListener _RefreshListener = new PullToRefreshBase.OnRefreshListener() {
		@Override
		public void onRefresh(PullToRefreshBase refreshView) {
			updatePullDownUpdateTime((PullToRefreshAdapterViewBase) refreshView);
			if(mRefreshListener != null){
				mRefreshListener.onRefresh(refreshView);
			}
		}
	};

	void updatePullDownUpdateTime(PullToRefreshBase refreshView) {
		mLastUpdatePullDownTime = new Date();
		initPullDownToRefreshView(mLastUpdatePullDownTime);
	}

	private PullToRefreshBase.OnRefreshListener2 _RefreshListener2
			= new PullToRefreshBase.OnRefreshListener2() {
		@Override
		public void onPullDownToRefresh(PullToRefreshBase refreshView) {
			updatePullDownUpdateTime(refreshView);
			if(mRefreshListener2 != null){
				mRefreshListener2.onPullDownToRefresh(refreshView);
			}
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase refreshView) {
			mLastUpdatePullUpTime = new Date();
			initPullUpToRefreshView(mLastUpdatePullUpTime);
			if(mRefreshListener2 != null){
				mRefreshListener2.onPullUpToRefresh(refreshView);
			}
		}
	};
}
