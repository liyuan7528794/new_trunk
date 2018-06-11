package com.travel.shop.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.travel.ShopConstant;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;
import com.travel.shop.tools.ZhaoHangPay;

import org.apache.http.util.EncodingUtils;

import cmb.pb.util.CMBKeyboardFunc;

/**
 * 招行支付页面
 * 
 * @author wyp
 *
 */
public class ZhaoHangPayActivity extends TitleBarBaseActivity {

	private WebView wv_zhaohangpay;
	private Intent intent;
	private String tag;
	private long ordersId;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zhaohangpay);

		init();
		ordersId = intent.getLongExtra("billNo", 0);

		// 1.WebView加载web资源
		wv_zhaohangpay.postUrl(ShopConstant.ZHAOHANG_PAY,
				EncodingUtils.getBytes(
						"ordersId=" + ordersId + "&amount=" + intent.getStringExtra("amount")
								+ "&userId=" + UserSharedPreference.getUserId() + "&mobile="
								+ UserSharedPreference.getMobile() + "&body=" + intent.getStringExtra("body"),
						"UTF-8"));
		wv_zhaohangpay.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// 使用当前的WebView加载页面
				CMBKeyboardFunc kbFunc = new CMBKeyboardFunc(ZhaoHangPayActivity.this);
				if (kbFunc.HandleUrlCall(wv_zhaohangpay, url) == false) {
					return super.shouldOverrideUrlLoading(view, url);
				} else {
					return true;
				}
			}
		});

		// 设置
		wv_zhaohangpay.requestFocus();// 获取焦点
		wv_zhaohangpay.requestFocusFromTouch();// 获取手式
		wv_zhaohangpay.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);// 优先缓存
		WebSettings webSettings = wv_zhaohangpay.getSettings();
		webSettings.setLoadsImagesAutomatically(true);// 自动加载图片
		webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR); // html页面大小自适应
		webSettings.setJavaScriptEnabled(true);
		wv_zhaohangpay.addJavascriptInterface(new ZhaoHangPay(ZhaoHangPayActivity.this, tag, ordersId), "closeH5");
	}

	private void init() {
		wv_zhaohangpay = findView(R.id.wv_zhaohangpay);
		setTitle("银行卡支付");
		intent = getIntent();
		tag = intent.getStringExtra("tag");

		leftButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OSUtil.intentOrderInfo(ZhaoHangPayActivity.this, ordersId);
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		OSUtil.intentOrderInfo(this, ordersId);
		return super.onKeyDown(keyCode, event);
	}
}
