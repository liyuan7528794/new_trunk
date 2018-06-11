package com.travel.shop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.travel.Constants;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.MyWebViewClient;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.R;
import com.travel.shop.http.GoodsHttp;
import com.travel.shop.widget.SmallStoryCardView;
import com.travel.shop.widget.SmallStoryTitleLayout;

/**
 * Created by Administrator on 2017/11/14.
 */

public class StoryCardActivity extends TitleBarBaseActivity{
    private FrameLayout fl_title, fl_card;
    private WebView webView;
    private String url = "";

    private SmallStoryCardView cardView;
    private SmallStoryTitleLayout titleLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_card);
        setTitle("小城故事卡");
        rightButton.setText("激活");
        rightButton.setVisibility(View.VISIBLE);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StoryCardActivity.this, ActivateCouponActivity.class));
            }
        });

        init();

        url = Constants.Root_Url_ShareStory + "/html/storyCardIntro.html";
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) webView.getLayoutParams();
                params.height = (int) (webView.getContentHeight() * webView.getScale());
                webView.setLayoutParams(params);
            }
        });
        webView.loadUrl(url);

        GoodsHttp.isBuyCard(this, cardInfoListener);
    }

    private void init() {
        fl_title = findView(R.id.fl_title);
        fl_card = findView(R.id.fl_card);
        webView = findView(R.id.web);
        cardView = new SmallStoryCardView(this);
        titleLayout = new SmallStoryTitleLayout(this);
        fl_title.addView(titleLayout.getView());
        fl_card.addView(cardView.getView());
        titleLayout.getNum().setVisibility(View.VISIBLE);
        cardView.getGo().setText("续次");
        if(OSUtil.isDayTheme()){
            titleLayout.getLl().setBackgroundColor(getResources().getColor(R.color.gray_F5));
        }else {
            titleLayout.getLl().setBackgroundColor(getResources().getColor(R.color.black_2));
        }
    }
    GoodsHttp.CardInfoListener cardInfoListener = new GoodsHttp.CardInfoListener() {
        @Override
        public void isBuyCard(boolean isBuyCard, int count, String date) {
            titleLayout.getNum().setText("剩余" + count + "次");
            titleLayout.getTitle().setText(TextUtils.isEmpty(date) ? "" : ("有效期至"+date));
        }

        @Override
        public void getCardId(String cardId) {

        }
    };
}
