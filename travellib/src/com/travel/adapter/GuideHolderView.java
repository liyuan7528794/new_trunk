package com.travel.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bigkoo.convenientbanner.holder.Holder;

/**
 * 引导页使用
 * Created by wyp on 2018/5/23.
 */

public class GuideHolderView implements Holder<Integer> {
    private ImageView imageView;

    @Override
    public View createView(Context context) {
        imageView = new ImageView(context);
        return imageView;
    }

    @Override
    public void UpdateUI(Context context, int position, Integer resId) {
        imageView.setBackgroundResource(resId);
    }
}
