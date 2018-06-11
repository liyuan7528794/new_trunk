package com.travel.lib.utils;

import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * 原来项目中对webviewClient的简单处理
 * @author Administrator
 */
public class MyWebViewClient extends WebViewClient{
	// 重写错误页面
	@Override
	public void onReceivedError(WebView view, int errorCode,
			String description, String failingUrl) {
		view.getSettings().setDefaultTextEncodingName("UTF-8");
		super.onReceivedError(view, errorCode, description, failingUrl);
		String errorHtml = "<div style='padding-top:200px;text-align:center;color:#666;'>未打开无线网络</div>";
		view.loadDataWithBaseURL("", errorHtml, "text/html", "UTF-8", "");
	}

	// 重写back按键
	@Override
	public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
		return super.shouldOverrideKeyEvent(view, event);
	}

	// 重写点击链接不在浏览器中打开
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		view.loadUrl(url);
		return true;
	}
}
