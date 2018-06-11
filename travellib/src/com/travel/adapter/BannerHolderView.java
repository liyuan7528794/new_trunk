package com.travel.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bigkoo.convenientbanner.holder.Holder;
import com.travel.bean.NotifyBean;
import com.travel.lib.utils.ImageDisplayTools;

/**
 * Created by Administrator on 2017/7/7.
 */

public class BannerHolderView implements Holder<NotifyBean>{
    private ImageView imageView;
    @Override
    public View createView(Context context) {
        imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }

    @Override
    public void UpdateUI(Context context, int position, NotifyBean data) {
        ImageDisplayTools.displayImage(data.getImgUrl(),imageView);
    }
}
