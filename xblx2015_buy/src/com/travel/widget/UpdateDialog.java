package com.travel.widget;

import java.util.List;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.OSUtil;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

public class UpdateDialog extends Dialog {
	private static final String TAG = "UpdateDialog";
	
	private String mTitle, mContent;
	
	private TextView mTitleTextView, mContentTextView;
	private TextView mUpdateNowButton;
	private ImageView mCloseImage;
	
	public interface UpdateDialogListener{
		void onUpdateNowClick();
		void onCloseClick();
	}
	private UpdateDialogListener mListener;
	
	public UpdateDialog(Context context, String title, String content, UpdateDialogListener listener) {
		super(context, R.style.MyDialogStyle);
		mTitle = title;
		mContent = content;
		mListener = listener;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_update);
		mTitleTextView = (TextView) findViewById(R.id.tv_title);
		mContentTextView = (TextView) findViewById(R.id.tv_content);
		mUpdateNowButton = (TextView) findViewById(R.id.tv_update_now);
		mCloseImage = (ImageView) findViewById(R.id.iv_close);
		
		if(!TextUtils.isEmpty(mTitle)){
			mTitleTextView.setText(mTitle);
		}
		mContentTextView.setText(mContent);
		mUpdateNowButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mListener != null){
					mListener.onUpdateNowClick();
				}
				dismiss();
			}
		});
		mCloseImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mListener != null){
					mListener.onCloseClick();
				}
				dismiss();
			}
		});

		LayoutParams layoutParams = getWindow().getAttributes();
		layoutParams.width = Math.max(OSUtil.dp2px(getContext(), 250), layoutParams.width);
		getWindow().setAttributes(layoutParams);
	}
}
