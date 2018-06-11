package com.travel.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bigkoo.convenientbanner.holder.Holder;
import com.travel.bean.NotifyBean;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;

/**
 * 发现页轮播图
 * Created by wyp on 2018/5/10.
 */

public class DiscoverBannerHolderView implements Holder<NotifyBean> {
    private ImageView imageView;

    @Override
    public View createView(Context context) {
        imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }

    @Override
    public void UpdateUI(Context context, int position, NotifyBean data) {
        ImageDisplayTools.disPlayRoundDrawable(data.getImgUrl(), imageView, 16);
    }
}
