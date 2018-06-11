package com.travel.layout;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.travel.http_helper.GetCountHttp;
import com.travel.layout.DialogTemplet.DialogLeftButtonListener;
import com.travel.layout.DialogTemplet.DialogRightButtonListener;
import com.travel.lib.R;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;

import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * 分享弹框
 *
 * @author wyp
 */
public class SharePopupWindow extends BaseBellowPopupWindow implements OnClickListener {
    private Context mContext;
    private View rootView;
    private TextView share_wechat, share_wechatmoments, share_qq, share_qzone, share_cancel;
    private String title;
    private String text;
    private String imageUrl;
    private String url;

    public SharePopupWindow(Context context, String title, String text, String imageUrl, String url) {
        super(context);
        this.mContext = context;
        this.title = title;
        this.text = text;
        this.imageUrl = imageUrl;
        this.url = url;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.layout_share, null);
        initView();
        SetContentView(rootView);
        show();
    }

    private void initView() {
        share_wechat = rootView.findViewById(R.id.share_wechat);
        share_wechatmoments = rootView.findViewById(R.id.share_wechatmoments);
        share_qq = rootView.findViewById(R.id.share_qq);
        share_qzone = rootView.findViewById(R.id.share_qzone);
        share_cancel = rootView.findViewById(R.id.share_cancel);
        share_wechat.setOnClickListener(this);
        share_wechatmoments.setOnClickListener(this);
        share_qq.setOnClickListener(this);
        share_qzone.setOnClickListener(this);
        share_wechat.setOnClickListener(this);
        share_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        String platform = "";
        if (v == share_wechat) {
            platform = Wechat.NAME;
        } else if (v == share_wechatmoments) {
            platform = WechatMoments.NAME;
        } else if (v == share_qq) {
            platform = QQ.NAME;
        } else {
            platform = QZone.NAME;
        }
        OSUtil.showShare(platform, title, text, imageUrl, url, mContext);
        dismiss();
    }
}
