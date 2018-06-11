package com.travel.layout;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.travel.lib.R;

/**
 * 图片选择时的弹出窗
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/05/12
 *
 */
public class PhotoChooseDialog extends Dialog {

	private Activity activity;
	private PhotoChooseDialog instance;
	private Button takePhoto, mapStorage, cancle;
	private String up, down;

	/**
	 * 弹出框初始化
	 * 
	 * @param activity
	 *            上下文
	 */
	public PhotoChooseDialog(Activity activity, String up, String down) {
		super(activity, android.R.style.Theme_Dialog);
		this.activity = activity;
		this.up = up;
		this.down = down;

		instance = this;
	}

	public interface DialogTakePhotoListener {
		public void takePhotoClick(View view);
	}

	public interface DialogMapStorageListener {
		public void mapStorageClick(View view);
	}

	public interface DialogCancleListener {
		public void cancleClick(View view);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_choose_dialog);

		Window window = getWindow();
		// 设置显示动画
		window.setWindowAnimations(R.style.main_menu_animstyle);
		window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.x = 0;
		wl.y = activity.getWindowManager().getDefaultDisplay().getHeight();
		// 以下这两句是为了保证按钮可以水平满屏
		wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
		wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		// 设置显示位置
		onWindowAttributesChanged(wl);
		window.setAttributes(wl);

		takePhoto = (Button) findViewById(R.id.btn_take_photo);
		mapStorage = (Button) findViewById(R.id.btn_map_storage);
		cancle = (Button) findViewById(R.id.btn_cancle);

		mapStorage.setText(up);
		takePhoto.setText(down);

	}

	public void setTakePhotoClick(final DialogTakePhotoListener listener) {
		takePhoto.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				instance.dismiss();
				listener.takePhotoClick(takePhoto);
			}
		});
	}

	public void setMapStorageClick(final DialogMapStorageListener listener) {
		mapStorage.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				instance.dismiss();
				listener.mapStorageClick(mapStorage);
			}
		});
	}

	public void setCancleClick(final DialogCancleListener listener) {
		cancle.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				instance.dismiss();
				listener.cancleClick(cancle);
			}
		});
	}
}
