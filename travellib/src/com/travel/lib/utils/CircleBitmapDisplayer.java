package com.travel.lib.utils;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import android.graphics.Bitmap;
import android.graphics.Color;

public class CircleBitmapDisplayer implements BitmapDisplayer {

    protected  final int margin ;
    private int marginColor;

    public CircleBitmapDisplayer() {
        this(0, Color.WHITE);
    }

    public CircleBitmapDisplayer(int margin, int marginColor) {
        this.margin = margin;
        this.marginColor = marginColor;
    }

    @Override
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        if (!(imageAware instanceof ImageViewAware)) {
            throw new IllegalArgumentException("ImageAware should wrap ImageView. ImageViewAware is expected.");
        }

        imageAware.setImageDrawable(new CircleDrawable(bitmap,marginColor,margin));
    }


}