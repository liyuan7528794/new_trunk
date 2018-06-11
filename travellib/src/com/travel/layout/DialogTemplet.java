package com.travel.layout;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.lib.R;
import com.travel.lib.utils.OSUtil;

public class DialogTemplet extends Dialog {

	private Context context;
	private DialogTemplet instance;
	private boolean isOnlyOneButton = false;
	private Button leftButton, rightButton, confirmButton;
	private LinearLayout doubleButtonLayout, oneButtonLayout;
	private String contents, leftText, rightText, confirmText;
	private TextView contentText;

	private DialogLeftButtonListener mLeftListener;
	private DialogRightButtonListener mRightListener;
	private DialogConfirmButtonListener mConfirmListener;

	/**
	 * 弹出框初始化
	 * 
	 * @param context
	 *            上下文
	 * @param isOnlyOneButton
	 *            是否只有一个按钮，true表示一个按钮，false表示两个按钮
	 * @param content
	 *            弹框提示内容
	 * @param confirmText
	 *            只有一个按钮的时候的按钮内容
	 * @param leftText
	 *            左按钮内容
	 * @param rightText
	 *            右按钮内容
	 */
	public DialogTemplet(Context context, boolean isOnlyOneButton,
			String content, String confirmText, String leftText,
			String rightText) {
		super(context, R.style.MyDialogStyle);
		this.context = context;
		this.isOnlyOneButton = isOnlyOneButton;
		this.contents = content;
		this.confirmText = confirmText;
		this.leftText = leftText;
		this.rightText = rightText;
		
		instance = this;
	}

	public interface DialogLeftButtonListener {
		public void leftClick(View view);
	}

	public interface DialogRightButtonListener {
		public void rightClick(View view);
	}

	public interface DialogConfirmButtonListener {
		public void confirmClick(View view);
	}
	private static boolean isWait = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		if(isWait){
//			isWait = false;
//			return;
//		}
//		isWait = true;
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(500);
//					isWait = false;
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}).start();
		
		setContentView(R.layout.dialog_templet_layout);
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		params.width = OSUtil.getScreenWidth()-OSUtil.dp2px(context, 90);
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.gravity = Gravity.CENTER;
		window.setAttributes(params);

		oneButtonLayout = (LinearLayout) findViewById(R.id.oneButtonLayout);
		doubleButtonLayout = (LinearLayout) findViewById(R.id.doubleButtonLayout);
		contentText = (TextView) findViewById(R.id.contents);
		leftButton = (Button) findViewById(R.id.leftButton);
		rightButton = (Button) findViewById(R.id.rightButton);
		confirmButton = (Button) findViewById(R.id.confirmButton);

		confirmButton.setText(confirmText);
		contentText.setText(contents);
		leftButton.setText(leftText);
		rightButton.setText(rightText);

		if (isOnlyOneButton) {
			oneButtonLayout.setVisibility(View.VISIBLE);
			doubleButtonLayout.setVisibility(View.GONE);
		} else {
			oneButtonLayout.setVisibility(View.GONE);
			doubleButtonLayout.setVisibility(View.VISIBLE);
		}

		initButtonListener();
	}

	private void initButtonListener() {
		leftButton.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				instance.dismiss();
				if(mLeftListener != null){
					mLeftListener.leftClick(leftButton);
				}
			}
		});

		rightButton.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				instance.dismiss();
				if(mRightListener != null){
					mRightListener.rightClick(rightButton);
				}
			}
		});

		confirmButton.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				instance.dismiss();
				if(mConfirmListener != null){
					mConfirmListener.confirmClick(confirmButton);
				}
			}
		});
	}

	public void setLeftClick(final DialogLeftButtonListener listener) {
		mLeftListener = listener;
	}

	public void setRightClick(final DialogRightButtonListener listener) {
		mRightListener = listener;
	}

	public void setConfirmClick(final DialogConfirmButtonListener listener) {
		mConfirmListener = listener;
	}
}
