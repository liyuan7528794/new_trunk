package com.travel.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.holder.Holder;
import com.travel.bean.CCTVVideoInfoBean;
import com.travel.lib.R;
import com.travel.lib.utils.ImageDisplayTools;

/**
 * Created by Administrator on 2017/7/7.
 */

public class VideoBannerHolderView implements Holder<CCTVVideoInfoBean> {
    private View rootView;
    private ImageView imageView;
    private TextView textView;

    public VideoBannerHolderView() {
    }

    @Override
    public View createView(Context context) {
        rootView = View.inflate(context, R.layout.layout_cctv_banner, null);
        imageView = (ImageView) rootView.findViewById(R.id.iv_background);
        textView = (TextView) rootView.findViewById(R.id.tv_cctv_video_title);
        RelativeLayout rl_title = (RelativeLayout) rootView.findViewById(R.id.rl_title);
        rl_title.setVisibility(View.VISIBLE);
        return rootView;
    }

    @Override
    public void UpdateUI(Context context, int position, CCTVVideoInfoBean data) {
        ImageDisplayTools.displayImage(data.getImgUrl(), imageView);
        textView.setText(data.getTitle());
    }
}
