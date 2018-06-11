package com.travel.usercenter;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.ShopConstant;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 意见反馈
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/04/22
 * 
 */
public class FeedbackActivity extends TitleBarBaseActivity {

	private Context mContext;
	private LinearLayout ll_feedback_focus;
	private EditText et_feedback;
	private TextView tv_yes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);

		init();

		tv_yes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!"".equals(et_feedback.getText().toString().trim())) {
					commitFeedback();
				} else
					showToast(R.string.feedback_content_cannot_empty);
			}

		});

		ll_feedback_focus.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				OSUtil.hideKeyboard(FeedbackActivity.this);
				return false;
			}
		});
	}

	/**
	 * 控件初始化
	 */
	private void init() {
		mContext = this;

		ll_feedback_focus = findView(R.id.ll_feedback_focus);
		et_feedback = findView(R.id.et_feedback);
		tv_yes = findView(R.id.tv_yes);

		setTitle(getString(R.string.feedback));
	}

	/**
	 * 提交反馈
	 */
	private void commitFeedback() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", "-1".equals(UserSharedPreference.getUserId()) ? "" : UserSharedPreference.getUserId());
		map.put("content", et_feedback.getText().toString().trim());
		NetWorkUtil.postForm(mContext, ShopConstant.MY_PAGE_FEEDBACK, new MResponseListener(mContext) {

			@Override
			public void onResponse(JSONObject response) {
				super.onResponse(response);
				if (response.optInt("error") == 0) {
					showToast(R.string.feedback_success);
					finish();
				}
			}
		}, map);

	}

}
