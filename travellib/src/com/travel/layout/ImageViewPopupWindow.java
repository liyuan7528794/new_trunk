package com.travel.layout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.travel.lib.utils.MLog;

import java.io.File;


/**
 * Created by ke on 16-5-15.
 */
public class ImageViewPopupWindow{
    @SuppressWarnings("unused")
    private static final String TAG = "ImageViewPopupWindow";

    private PopupWindow mPopupWindow;
    private ImageViewTouch mImageViewTouch;

    public boolean isShowing(){
        return mPopupWindow != null && mPopupWindow.isShowing();
    }

    public void show(Context context, View anchorView ,String url){
        if(mPopupWindow == null){
            mPopupWindow = new PopupWindow(context);
            mPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            mPopupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            mPopupWindow.setClippingEnabled(false);
            mPopupWindow.setBackgroundDrawable(null);
            mImageViewTouch = new ImageViewTouch(context);
        }

        if(mImageViewTouch.getParent() != null){
            ((ViewGroup)mImageViewTouch.getParent()).removeView(mImageViewTouch);
        }
        mPopupWindow.setContentView(mImageViewTouch);
        mPopupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, 0, 0);
        mImageViewTouch.setListener(new ImageViewTouch.ImageViewTouchListener() {
            @Override
            public void onSingleTap(ImageViewTouch imageViewTouch) {
                MLog.v(TAG, "onSingleTap");
                dismissPopupWindow();
            }
        });
        Bitmap bitmap = null;
        @SuppressWarnings("deprecation")
		File file = ImageLoader.getInstance().getDiscCache().get(url);
        if(file != null && file.exists()){
        	bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        }
        if(bitmap != null){
        	MLog.v(TAG, "bitmpa is not null");
        	mImageViewTouch.setImageBitmap(bitmap);
        }
        MLog.v(TAG, "show, and url is %s.", url);
    }

    public void dismissPopupWindow(){
        if(mPopupWindow == null) return;

        mImageViewTouch.recycleBitmap();
        mPopupWindow.dismiss();
        mImageViewTouch = null;
        mPopupWindow = null;
    }
}
