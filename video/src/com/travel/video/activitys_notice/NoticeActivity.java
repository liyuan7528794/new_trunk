package com.travel.video.activitys_notice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.ctsmedia.hltravel.R;
import com.travel.bean.GoodsDetailBean;
import com.travel.bean.NotifyBean;
import com.travel.bean.UDPSendInfoBean;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.MyWebViewClient;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.PopWindowUtils;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.http.CommitHttp;

import java.util.Date;

@SuppressLint("SetJavaScriptEnabled")
public class NoticeActivity extends TitleBarBaseActivity {

    private Context mContext;
    private WebView web;
    private NotifyBean notifyBean;
    private String shareUrl;
    private String tag;
    private String uId;

    public interface Listener {
        /**
         * 当获取商品信息
         *
         * @param mGoodsDetailBean
         */
        void onGoodInfoBeanGot(GoodsDetailBean mGoodsDetailBean);
    }

    public interface CouponListener {
        /**
         * 当获取到优惠券
         */
        void onCouponGet();

        /**
         * 未获取到优惠券
         */
        void onCouponError();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        init();
        uId = UserSharedPreference.isLogin() ? UserSharedPreference.getUserId() : "-1";
        if (notifyBean != null) {
            GoodsInfo goodsInfo = new GoodsInfo(mContext, tag, this);
            goodsInfo.setVoteListener(voteListener);
            web = (WebView) findViewById(R.id.noticeWeb);
            web.setWebChromeClient(new WebChromeClient());
            web.setWebViewClient(new MyWebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if (UserSharedPreference.isLogin() && notifyBean != null && TextUtils.equals("正阳", notifyBean.getTitle().substring(0, 2)))
                        web.post(new Runnable() {
                            @Override
                            public void run() {
                                web.loadUrl("javascript:getVote('" + UserSharedPreference.getUserId() + "')");
                            }
                        });
                }
            });
            web.getSettings().setJavaScriptEnabled(true);// 可以与h5交互
            web.loadUrl(notifyBean.getWebUrl() + "?id=" + new Date().getTime() + "&app=app&uId=" + uId);
            web.addJavascriptInterface(goodsInfo, "notice");
        }

        group_chat.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PopWindowUtils.sharePopUpWindow(mContext, notifyBean.getTitle(), notifyBean.getTitle(), notifyBean.getImgUrl(),
                        shareUrl);
            }
        });
    }

    private void init() {
        mContext = this;
        Bundle bundle = getIntent().getBundleExtra("notice_bean");
        notifyBean = (NotifyBean) bundle.get("notice_bean");
        tag = bundle.getString("tag");
        OSUtil.setShareParam(group_chat, "share", mContext);
        group_chat.setVisibility(View.GONE);
        shareUrl = notifyBean.getShareUrl() + "?web=web";

        if (TextUtils.equals("activity", tag))
            CommitHttp.getActivityInfo(mContext, notifyBean.getId(), mListener);
        else {
            setTitle(notifyBean.getTitle());
            if (TextUtils.equals(notifyBean.getType(), "2") && !TextUtils.isEmpty(notifyBean.getImgUrl()))
                group_chat.setVisibility(View.VISIBLE);
        }

    }

    CommitHttp.ActivityListener mListener = new CommitHttp.ActivityListener() {
        @Override
        public void getActivityData(NotifyBean mNotifyBean) {
            if (mNotifyBean == null)
                return;
            notifyBean = mNotifyBean;
            setTitle(notifyBean.getTitle());
            if (TextUtils.equals(notifyBean.getType(), "2") && !TextUtils.isEmpty(notifyBean.getImgUrl()))
                group_chat.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(notifyBean.getImgUrl()))
                group_chat.setVisibility(View.VISIBLE);
            web.loadUrl(notifyBean.getWebUrl() + "?id=" + new Date().getTime() + "&app=app&uId=" + uId);
        }
    };

    // 投票成功
    GoodsInfo.VoteListener voteListener = new GoodsInfo.VoteListener() {
        @Override
        public void onSuccess(final String cityId) {
            web.post(new Runnable() {
                @Override
                public void run() {
                    web.loadUrl("javascript:addShow('" + cityId + "')");
                }
            });
        }
    };

    @Override
    protected void onPause() {
        if (web != null)
            web.reload();
        super.onPause();
    }

    String beginTime = "";

    @Override
    protected void onStart() {
        super.onStart();
        beginTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
    }

    String endTime = "";

    @Override
    protected void onStop() {
        super.onStop();
        endTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
        UDPSendInfoBean bean = new UDPSendInfoBean();
        bean.getData("001_" + notifyBean.getId(), notifyBean.getTitle(),
                notifyBean.getWebUrl(), beginTime, endTime);
        sendData(bean);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        web.clearCache(true);
        web.destroyDrawingCache();
    }
}
