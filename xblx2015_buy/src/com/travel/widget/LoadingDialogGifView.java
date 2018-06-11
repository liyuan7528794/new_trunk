package com.travel.widget;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;

public class LoadingDialogGifView {
	private static Context context;
	private AlertDialog pd;
	private static LoadingDialogGifView instance = null;
	public static LoadingDialogGifView getInstance(Context mContext) {
		context = mContext;
		if (instance == null) {
			instance = new LoadingDialogGifView();
		}
		return instance;
	}
	
	public void showProcessDialog() {
		 /**
		OnKeyListener keyListener = new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_HOME
						|| keyCode == KeyEvent.KEYCODE_SEARCH
						|| keyCode == KeyEvent.KEYCODE_BACK) {
					pd.dismiss();
					return true;
				}
				return false;
			}
		};
		if (pd == null || !pd.isShowing()) {
			pd = new AlertDialog.Builder(context).create();
			pd.setOnKeyListener(keyListener);
			pd.setCancelable(false);
			pd.show();
			pd.setContentView(R.layout.loading_process_dialog_anim);
			Window mWindow = pd.getWindow();
			GifView gifView = (GifView) mWindow.findViewById(R.id.gifView);  
		    // 设置Gif图片源  
			gifView.setGifImage(R.drawable.loading); 
			
		    // 添加监听器  
//			gifView.setOnClickListener(this);  
		    // 设置显示的大小，拉伸或者压缩  
			gifView.setShowDimension(dp_px.dp2px(context, 120), dp_px.dp2px(context, 120));  
//		    // 设置加载方式：先加载后显示、边加载边显示、只显示第一帧再显示  
//			gifView.setGifImageType(GifImageType.COVER);
			mHandler.sendEmptyMessageDelayed(1, 2000);
		}
		*/
	}
	
	public void hideProcessDialog(int flag) {
		/**
		if (pd != null && pd.isShowing()) {

			if (flag == 0) {
				pd.dismiss();
			} else if (flag == 1) {
				pd.dismiss();

			}
		}
		*/
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case 1 :
					if (pd != null && pd.isShowing()) {
						hideProcessDialog(1);
					}
					break;

				default :
					break;
			}
		}
	};
}