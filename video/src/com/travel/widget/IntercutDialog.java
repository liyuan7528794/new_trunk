package com.travel.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.communication.entity.UserData;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.FormatUtils;

public class IntercutDialog extends Dialog {

	private Context context;
	private boolean isOnlyOneButton = false;
	private Button leftButton, rightButton, confirmButton;
	private LinearLayout doubleButtonLayout, oneButtonLayout;
	private String contents, leftText, rightText, confirmText;
	private UserData userData;
	private TextView contentText;

	/**
	 * 弹出框初始化
	 * 
	 * @param context
	 *            上下文
	 * @param isOnlyOneButton
	 *            是否只有一个按钮，true表示一个按钮，false表示两个按钮
	 * @param confirmText
	 *            只有一个按钮的时候的按钮内容
	 * @param leftText
	 *            左按钮内容
	 * @param rightText
	 *            右按钮内容
	 */
	public IntercutDialog(Context context, boolean isOnlyOneButton,
						  UserData userData, String confirmText, String leftText,
						  String rightText) {
		super(context, R.style.MyDialogStyle);
		this.userData = userData;
		this.context = context;
		this.isOnlyOneButton = isOnlyOneButton;
		this.confirmText = confirmText;
		this.leftText = leftText;
		this.rightText = rightText;
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
		
		if(isWait){
			isWait = false;
			return;
		}
		isWait = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(500);
					isWait = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		params.width = OSUtil.getScreenWidth()-OSUtil.dp2px(context, 90);
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.gravity = Gravity.CENTER;
		window.setAttributes(params);
		setContentView(R.layout.dialog_templet_layout);
		oneButtonLayout = (LinearLayout) findViewById(R.id.oneButtonLayout);
		doubleButtonLayout = (LinearLayout) findViewById(R.id.doubleButtonLayout);
		contentText = (TextView) findViewById(R.id.contents);
		leftButton = (Button) findViewById(R.id.leftButton);
		rightButton = (Button) findViewById(R.id.rightButton);
		confirmButton = (Button) findViewById(R.id.confirmButton);

		confirmButton.setText(confirmText);
		
		contents = userData.getNickName();
		SpannableStringBuilder spBuilder = FormatUtils.StringSetSpanColor(context, "是否同意 "+contents+" 连线？",
				contents,R.color.blue_3023AE);
		if(spBuilder!=null)
			contentText.setText(spBuilder);

		leftButton.setText(leftText);
		rightButton.setText(rightText);

		if (isOnlyOneButton) {
			oneButtonLayout.setVisibility(View.VISIBLE);
			doubleButtonLayout.setVisibility(View.GONE);
		} else {
			oneButtonLayout.setVisibility(View.GONE);
			doubleButtonLayout.setVisibility(View.VISIBLE);
		}

	}
	
	public String getCurId(){
		return userData.getId();
	}
	
	public void setLeftClick(final DialogLeftButtonListener listener) {
		if(leftButton==null){
			return;
		}
		leftButton.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				listener.leftClick(leftButton);
			}
		});
	}

	public void setRightClick(final DialogRightButtonListener listener) {
		if(rightButton==null){
			return;
		}
		rightButton.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				listener.rightClick(rightButton);
			}
		});
	}

	public void setConfirmClick(final DialogConfirmButtonListener listener) {
		confirmButton.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
						listener.confirmClick(confirmButton);
					}
				});
	}
}
