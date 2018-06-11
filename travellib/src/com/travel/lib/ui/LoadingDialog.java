package com.travel.lib.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Handler;
import android.view.KeyEvent;

import com.travel.lib.R;

import java.lang.ref.WeakReference;

public class LoadingDialog {
	private static WeakReference<Context> context;
	private AlertDialog pd;
	private static WeakReference<LoadingDialog> instance = null;

	public static LoadingDialog getInstance(Context mContext) {
		if (instance == null || instance.get() == null || mContext != context.get()) {
			instance = new WeakReference<LoadingDialog>(new LoadingDialog());
		}
		context = new WeakReference<Context>(mContext);
		return instance.get();
	}

	public void showProcessDialog() {
		OnKeyListener keyListener = new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_SEARCH) {
					pd.dismiss();
					return true;
				}
				return false;
			}
		};
		if (pd == null || !pd.isShowing()) {
			pd = new AlertDialog.Builder(context.get(),R.style.DialogStyle).create();
			pd.setOnKeyListener(keyListener);
			pd.setCancelable(false);
			pd.show();
			pd.setContentView(R.layout.loading_process_dialog);
			mHandler.sendEmptyMessageDelayed(1,10000);
		}
	}

	public void showProcessDialogNotStop() {
		OnKeyListener keyListener = new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_SEARCH) {
					pd.dismiss();
					return true;
				}
				return false;
			}
		};
		if (pd == null || !pd.isShowing()) {
			pd = new AlertDialog.Builder(context.get(), R.style.MyDialogStyle).create();
			pd.setOnKeyListener(keyListener);
			pd.setCancelable(false);
			pd.show();
			pd.setContentView(R.layout.loading_process_dialog);
			mHandler.sendEmptyMessageDelayed(1, 30000);
		}
	}

	public void hideProcessDialog(int flag) {
		if (pd != null && pd.isShowing()) {

			if (flag == 0) {
				pd.dismiss();
			} else if (flag == 1) {
				pd.dismiss();

			}
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				if (pd != null && pd.isShowing()) {
					hideProcessDialog(1);
				}
				break;

			default:
				break;
			}
		}
	};
}