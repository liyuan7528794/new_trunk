package com.travel.lib.utils;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import android.graphics.Bitmap;

public class BlurBitmapDisplayer implements BitmapDisplayer {

	@Override
	public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom arg2) {
        if (!(imageAware instanceof ImageViewAware)) {
            throw new IllegalArgumentException("ImageAware should wrap ImageView. ImageViewAware is expected.");
        }
        
        imageAware.setImageDrawable(BlurFilter.BoxBlurFilter(bitmap));
	}

}
