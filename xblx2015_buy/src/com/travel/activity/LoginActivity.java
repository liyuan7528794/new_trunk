package com.travel.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.fragment.LoginFragment;
import com.travel.fragment.RegistFragment;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.utils.HLLXLoginHelper;

/**
 * Description:安卓客户端用户登录activity。
 */
public class LoginActivity extends TitleBarBaseActivity implements OnClickListener{
	private static final String TAG = "LoginActivity";

	public static final String MSG = "msg";
	private static Context instance ;

	// 退出时是否前往主页
	private boolean isGotoHome = false;

	private LinearLayout loginLayout, registLayout;
	private TextView tv_login, tv_regist;
	private View loginLine, registLine;

	private LoginFragment loginFragment;
	private RegistFragment registFragment;
	private int currentTag = -1;
	private Fragment currentFragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		Intent intent = getIntent();
		if (intent.hasExtra(MSG)) {
			showToast(MSG);
		}

		setContentView(R.layout.activity_login);
		titleLine.setVisibility(View.GONE);

		init();
		setListener();

		loginFragment = new LoginFragment();
		registFragment = new RegistFragment();
		currentFragment = loginFragment;
		addFragment(1);

		isGotoHome = getIntent().getBooleanExtra("is_goto_home", false);

		UserSharedPreference.clearContent();
		new HLLXLoginHelper(this).visitorLogin();
	}

	private void init(){
		loginLayout = (LinearLayout) findViewById(R.id.loginLayout);
		registLayout = findView(R.id.registLayout);
		tv_login = findView(R.id.login_title);
		tv_regist = findView(R.id.regist_title);
		loginLine = findView(R.id.login_line);
		registLine = findView(R.id.regist_line);
	}

	private void setListener() {
		leftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		findViewById(R.id.parentLayout).setOnClickListener(this);
		loginLayout.setOnClickListener(this);
		registLayout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.parentLayout:
				OSUtil.hideKeyboard(LoginActivity.this);
				break;
			case R.id.loginLayout:
				addFragment(1);
				break;
			case R.id.registLayout:
				addFragment(2);
				break;
		}
	}

	private void addFragment(int index){
		if(currentTag != index) {
			tv_login.setTextColor(ContextCompat.getColor(this, R.color.gray_9));
			tv_regist.setTextColor(ContextCompat.getColor(this, R.color.gray_9));
			loginLine.setVisibility(View.INVISIBLE);
			registLine.setVisibility(View.INVISIBLE);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			if (index == 1) {
				if (!loginFragment.isAdded()) { // 先判断是否被add过
					transaction.hide(currentFragment).add(R.id.fl_container, loginFragment).show(loginFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
				} else {
					transaction.hide(currentFragment).show(loginFragment).commit(); // 隐藏当前的fragment，显示下一个
				}
				if(OSUtil.isDayTheme())
				tv_login.setTextColor(ContextCompat.getColor(this, R.color.black_3));
				else
				tv_login.setTextColor(ContextCompat.getColor(this, R.color.gray_C0));
				loginLine.setVisibility(View.VISIBLE);
				currentFragment = loginFragment;
			} else {
				if (!registFragment.isAdded()) {
					transaction.hide(currentFragment).add(R.id.fl_container, registFragment).show(registFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
				} else {
					transaction.hide(currentFragment).show(registFragment).commit();
				}
				if(OSUtil.isDayTheme())
					tv_regist.setTextColor(ContextCompat.getColor(this, R.color.black_3));
				else
					tv_regist.setTextColor(ContextCompat.getColor(this, R.color.gray_C0));
				registLine.setVisibility(View.VISIBLE);
				currentFragment = registFragment;
			}
		}
		currentTag = index;
	}

	@Override
	public void onBackPressed() {
		if(isGotoHome){
			startActivity(new Intent(this, HomeActivity.class));
		}
		finish();
	}

	public static void finishActivity(){
		((Activity) instance).finish();
	}
}
