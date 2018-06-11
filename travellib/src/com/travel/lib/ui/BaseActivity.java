package com.travel.lib.ui;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.travel.lib.fragment_interface.Functions;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.MLog;

import java.lang.ref.SoftReference;

public abstract class BaseActivity extends FragmentActivity{
	@SuppressWarnings("unused")
	private static final String TAG = "BaseActivity";
	
	private ProgressDialog proDialog;
	private MHandler mHandler;
	private MReceiver mReceiver;
	
	protected static final int MSG_ON_NET_CHANGED = 56777888;
	
	protected Handler getHandler(){
		if(mHandler == null){
			synchronized(this){
				if(mHandler == null){
					mHandler = new MHandler(this);
				}
			}
		}
		return mHandler;
	}
	protected void netNotifyShow(){
		MLog.e(TAG, "netNotifyShow not define");
	}
	
	protected void netNotifyHide(){
		MLog.e(TAG, "netNotifyHide not define");
	}
	
	private static class MHandler extends Handler{
		private SoftReference<BaseActivity> mActivity;
		public MHandler(BaseActivity activity){
			mActivity = new SoftReference<BaseActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			BaseActivity baseActivity;
			if(mActivity != null && (baseActivity = mActivity.get()) != null){
				if(baseActivity.isFinishing()){
					return;
				}
				baseActivity.handleMessage(msg);
			}
		}
	}
	
	protected void handleMessage(Message msg){
		switch (msg.what) {
		case MSG_ON_NET_CHANGED:
			onNetChanged((String) msg.obj);
			break;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(mReceiver == null){
			mReceiver = new MReceiver();
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mReceiver, filter);
		onNetChanged(CheckNetStatus.checkNetworkConnection());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}
	
	protected void onNetChanged(String status){
		if(CheckNetStatus.unNetwork.equals(status)){
			netNotifyShow();
		}else{
			netNotifyHide();
		}
	}
	
	/**
	 * finish, 用于xml的点击事件
	 */
	public void onBack(View view){
		finish();
	}
	
	/**
	 * 显示Toast
	 * @param id String值对应的id
	 */
	protected void showToast(int id){
		showToast(getString(id));
	}
	/**
	 * 显示Toast
	 * @param str 需要显示的String
	 */
	protected void showToast(String str){
		Toast.makeText(this, str, Toast.LENGTH_LONG).show();
	}
	/**
	 * 居中显示Toast
	 * @param str 需要显示的String
	 */
	protected void showCenterToast(String str){
		Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	protected <T> T findView(int id){
		return (T)findViewById(id);
	}
	/**
	 * 显示或隐藏进度对话框
	 */
	protected void showProgressDialog(String title, String message){
		if(proDialog == null){
			proDialog = ProgressDialog.show(this, title, message);
			OnKeyListener onKeyListener = new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog,
						int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_HOME
							|| keyCode == KeyEvent.KEYCODE_SEARCH
							|| keyCode == KeyEvent.KEYCODE_BACK) {
						hideProgressDialog();
						return true;
					}
					return false;
				}
			};
			// TODO: 考虑是否需要监听
		}else{
			proDialog.dismiss();
			proDialog.setTitle(title);
			proDialog.setMessage(message);
			proDialog.show();
		}
	}
	protected void hideProgressDialog(){
		if(proDialog != null && proDialog.isShowing()){
			proDialog.dismiss();
		}
	}
	
	
	private class MReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
				// 网络状态改变
				onNetChanged(CheckNetStatus.checkNetworkConnection());
			}
		}
	}


	private Functions functions;
	/** 添加和Fragment通信的接口的方法，不用接口不添加 */
	protected abstract void addFunction(Functions functions);
	public void bindFunction(BaseFragment baseFragment){
		if(functions == null)
			initFunctions();
		baseFragment.setFunctions(functions);
	}
	protected void initFunctions() {
		functions = new Functions();
		addFunction(functions);
	}
	
}
