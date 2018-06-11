package com.travel.usercenter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;

import com.travel.Constants;
import com.ctsmedia.hltravel.R;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.MyWebViewClient;

/**
 * 
 * <p>
 * Title: SettingActivity.java
 * </p>
 * 
 * <p>
 * Description: 关于。logo展示。下载二维码。Copyright 。
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2014
 * </p>
 * 
 * <p>
 * Company: 沈阳网之商科技有限公司 www.iskyshop.com
 * </p>
 * 
 * @author lixiaoyang
 * 
 * @date 2014-7-17
 * 
 * @version 1.0
 */

@SuppressLint("ValidFragment")
public class MoreFragmentActivity extends TitleBarBaseActivity {
	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regist_doc);
		Intent intent = getIntent();
		String type = intent.getStringExtra("type");

		String address = "";

		// 帮助
		if ("help".equals(type)) {
			address = Constants.Root_Url.replace("/xblx", "") + "/help.html";
			setTitle(getString(R.string.about_help));
			// 关于小白旅行
		} else if ("about".equals(type)) {
			address = Constants.Root_Url.replace("/xblx", "") + "/about.html";
			setTitle(getString(R.string.about_xblx));
			// 用户协议
		} else if ("deal".equals(type)) {
//			address = Constants.Root_Url.replace("/xblx", "") + "/UserAgreement.html";
			address = Constants.Root_Url.replace("/xblx", "") + "/html/userXieYi/classType.html";
			setTitle(getString(R.string.about_deal));
			// 注册协议
		} else if ("regist".equals(type)) {
			address = Constants.Root_Url.replace("/xblx", "") + "/html/userXieYi/hongle.htm";
			setTitle("《注册协议》");
			// 常见问题
		} else if (getString(R.string.liveincome_question).equals(type)) {
			// 我的红币界面的“常见问题”
			if ("live_income".equals(getIntent().getStringExtra("tag"))) {
				address = Constants.Root_Url.replace("/xblx", "") + "/UserAgreement.html";
				// 提现界面的“常见问题”
			} else if ("withdraw_cash".equals(getIntent().getStringExtra("tag"))) {
				address = Constants.Root_Url.replace("/xblx", "") + "/about.html";
				// 电商收入界面的“常见问题”
			} else if ("business_income".equals(getIntent().getStringExtra("tag"))) {
				address = Constants.Root_Url.replace("/xblx", "") + "/help.html";
			}
			setTitle(getString(R.string.liveincome_question));
			// 众投页面的头：解释众投玩法
		} else if ("publicVote".equals(type)) {
			address = Constants.Root_Url.replace("/xblx", "") + "/UserAgreement.html";
			setTitle("该不该买单");
		}

		webView = (WebView) findViewById(R.id.doc_content);
		webView.setWebViewClient(new MyWebViewClient());
		webView.loadUrl(address);

		leftButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(webView.canGoBack()){
					webView.goBack();
				}else{
					finish();
				}
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(webView.canGoBack()){
			webView.goBack();
			return true;
		}else{
			return super.onKeyDown(keyCode, event);
		}
	}
}