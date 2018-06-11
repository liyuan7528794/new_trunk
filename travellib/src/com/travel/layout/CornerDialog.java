package com.travel.layout;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * 圆角Dialog
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/03/11
 * 
 */
public class CornerDialog extends Dialog {

	private EditText editText;

	public CornerDialog(Context context, int width, int height, View view,
			int style) {
		super(context, style);
		setContentView(view);
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		params.width = width;
		params.height = height;
		params.gravity = Gravity.CENTER;
		window.setAttributes(params);
	}

	public CornerDialog(Context context, int width, int height, View view,
			int style, EditText editText) {
		super(context, style);
		setContentView(view);
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		params.width = width;
		params.height = height;
		params.gravity = Gravity.CENTER;
		window.setAttributes(params);
		this.editText = editText;
	}

	public void showKeyboard() {
		if (editText != null) {
			// 设置可获得焦点
			editText.setFocusable(true);
			editText.setFocusableInTouchMode(true);
			// 请求获得焦点
			editText.requestFocus();
			// 调用系统输入法
			InputMethodManager inputManager = (InputMethodManager) editText
					.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.showSoftInput(editText, 0);
		}
	}

}