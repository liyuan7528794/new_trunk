package com.travel.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * 重写了measure方法, 自动测量最大高度
 * Created by ldkxingzhe on 2016/6/13.
 */
public class WebViewWithoutMeasure extends WebView{

    public WebViewWithoutMeasure(Context context) {
        super(context);
        init();
    }

    public WebViewWithoutMeasure(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WebViewWithoutMeasure(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setVerticalScrollBarEnabled(false);
        setScrollContainer(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int tmpHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, tmpHeightMeasureSpec);
    }
}
