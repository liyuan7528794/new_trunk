package com.travel.lib.ui;

import com.travel.lib.R;
import com.travel.lib.utils.MLog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;


/**
 * 一个简单的iconIndicator
 * 请直接在xml中使用， xml中必须制定width， 目前版本不支持代码中更改width。  
 * 适用于API 11以上
 * @author ldkxingzhe
 */
final public class IconIndicator extends LinearLayout{
    @SuppressWarnings("unused")
    private static final String TAG = "IconIndicator";
    // 设置drawable, 此drawable 必须设置bounds
    private Drawable selectDrawable;
    private Drawable nonSelectDrawable;

    private int iconWidth = 0;
    private int iconCount = 0;
    private int selectPosition = -1;


    public IconIndicator(Context context) {
        this(context, null);
    }

    public IconIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("NewApi")
	public IconIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.icon_indicator);
        selectDrawable = array.getDrawable(R.styleable.icon_indicator_select_drawable);
        nonSelectDrawable = array.getDrawable(R.styleable.icon_indicator_non_select_drawable);
        iconWidth = array.getDimensionPixelSize(R.styleable.icon_indicator_icon_radius, 10) * 2;
        setGravity(Gravity.CENTER);

        Log.v(TAG, "iconWidth is " + iconWidth);
        setDrawableSize(nonSelectDrawable);
        setDrawableSize(selectDrawable);
        int count  = array.getInteger(R.styleable.icon_indicator_icon_count, 0);
        setIconCount(count);
        Log.v(TAG, "count is " + count);
    }

    private void setDrawableSize(Drawable drawable){
        if(drawable != null){
            drawable.setBounds(0, 0, iconWidth, iconWidth);
        }
    }

    /**
     * 设置图片指示数目
     * @param count
     */
    public void setIconCount(int count){
        if(count < 0) throw new IllegalStateException("iconCount can't not be negative");
        if(count == iconCount) return;

        if(count < iconCount){
            for(int i = iconCount - 1; i >= count; i--){
                removeViewAt(i);
            }
        }else{
            for(int i = iconCount; i < count; i++){
                ImageView imageView = new ImageView(getContext());
                imageView.setImageDrawable(nonSelectDrawable);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconWidth, iconWidth);
                params.setMargins(0, 0, iconWidth, 0);
                super.addView(imageView, params);
            }
        }
        iconCount = count;
    }

    public void setSelectPosition(int position){
        if(isOutBound(position) || position == selectPosition){
        	MLog.v(TAG, "not changed, return");
        	return;
        }

        if(!isOutBound(selectPosition)){
        	MLog.v(TAG, "old select changed");
            ImageView oldSelectImageView = (ImageView) getChildAt(selectPosition);
            oldSelectImageView.setImageDrawable(nonSelectDrawable);
        }
        if(!isOutBound(position)){
        	MLog.v(TAG, "new ImageView set drawable");
            ImageView newImageView = (ImageView) getChildAt(position);
            newImageView.setImageDrawable(selectDrawable);
        }
        selectPosition = position;
    }

    private boolean isOutBound(int position){
        if(position < 0 || position >= getChildCount()) return true;
        return false;
    }

    public void setSelectDrawable(Drawable selectDrawable) {
        this.selectDrawable = selectDrawable;
//        iconWidth = selectDrawable.getBounds().width();
        Log.v(TAG, "set select drawable, and icon width is " + iconWidth);

        if(!isOutBound(selectPosition)){
            ImageView selectView = (ImageView) getChildAt(selectPosition);
            selectView.setImageDrawable(selectDrawable);
        }
    }

    public void setNonSelectDrawable(Drawable nonSelectDrawable) {
        this.nonSelectDrawable = nonSelectDrawable;
//        iconWidth = nonSelectDrawable.getBounds().width();
        Log.v(TAG, "set nonSelect drawable, and icon width is " + iconWidth);
        for(int i = 0; i < iconCount; i++){
            if(i != selectPosition){
                ImageView imageView = (ImageView) getChildAt(i);
                imageView.setImageDrawable(nonSelectDrawable);
            }
        }
    }

    @Override
    public void addView(View child) {
        throw new IllegalStateException("not implement, don't use this method");
    }
}
