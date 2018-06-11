package com.travel.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.ctsmedia.hltravel.R;
import com.travel.activity.OneFragmentActivity;
import com.travel.lib.utils.MyWebViewClient;

/**
 * 包含了一个网页的Fragment
 * Created by ldkxingzhe on 2016/6/16.
 */
public class WebViewFragment extends Fragment implements OneFragmentActivity.OneFragmentInterface{
    private static final String TAG = "WebViewFragment";

    /** 需要加载的url */
    public static final String LOAD_URL = "load_url";
    private String mLoadUrl;
    private WebView mWebView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mLoadUrl = args.getString(LOAD_URL);
        if(TextUtils.isEmpty(mLoadUrl))
            throw new IllegalStateException("mLoadUrl is null");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_webview, container, false);
        mWebView = (WebView) rootView.findViewById(R.id.webview);
        mWebView.setWebViewClient(new MyWebViewClient());
        if(getActivity() instanceof OneFragmentActivity){
            mWebView.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onReceivedTitle(WebView view, String title) {
                    super.onReceivedTitle(view, title);
                    ((OneFragmentActivity)getActivity()).setTitle(title);
                }
            });
        }
        mWebView.loadUrl(mLoadUrl);
        return rootView;
    }

    @Override
    public boolean onBackPressed() {
/*        if(mWebView.canGoBack()){
            mWebView.goBack();
            return true;
        }*/
        return false;
    }

    @Override
    public void onTouchDown() {

    }
}
