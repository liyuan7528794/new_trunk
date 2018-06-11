package com.travel.video.widget.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.OSUtil;

/**
 * Created by linroid on 15/3/10.
 */
public class FilterMenuDrawable extends Drawable {
    private Context ctx;
    private Paint paint;
    private IconState state = IconState.COLLAPSED;
    private int radius;

    private int lineWidth = 8;
    private float expandProgress = 0;

    public FilterMenuDrawable(Context ctx, int color, int radius) {
        this.ctx = ctx;
        this.radius = OSUtil.dp2px(ctx, 72);

        paint = new Paint();
        paint.setAntiAlias(true);
//        paint.setColor(color);
//        paint.setStrokeWidth(lineWidth);

    }
    public enum IconState{
        COLLAPSED,
        EXPANDED
    }

    public float getExpandProgress() {
        return expandProgress;
    }

    public void setExpandProgress(float expandProgress) {
        this.expandProgress = expandProgress;
        invalidateSelf();
    }

    @Override
    public int getIntrinsicWidth() {
//        return (int) (radius*0.8f);
//        return OSUtil.dp2px(ctx, 28);
        return radius;
    }

    @Override
    public int getIntrinsicHeight() {
//        return (int) (radius*0.8f);
//    	return OSUtil.dp2px(ctx, 14);
    	return radius;
    }

    @SuppressLint("ResourceAsColor")
	@Override
    public void draw(Canvas canvas) {
        //draw three line
//        paint.setColor(Color.BLACK);
//        canvas.drawRect(getBounds(), paint);
//        paint.setColor(Color.WHITE);
//        if(expandProgress<=0.5f){
//            drawTopLine(canvas, expandProgress);
//            drawMiddleLine(canvas, expandProgress);
//            drawBottomLine(canvas, expandProgress);
        	
        	Resources res= ctx.getResources(); 
        	Bitmap bmp=BitmapFactory.decodeResource(res, R.drawable.menu_icon);
        	canvas.drawBitmap(bmp, null, getBounds(), paint);
//        	paint.setTextSize(OSUtil.dp2px(ctx, 14));
//        	paint.setColor(ctx.getResources().getColor(R.color.white));
//        	canvas.drawText("取证", getBounds().left, getBounds().bottom, paint);
        // draw cancel
        /*}else{
            drawTopLeftLine(canvas, expandProgress);
            drawBottomLeftLine(canvas, expandProgress);
        }*/
    }

    private void drawBottomLeftLine(Canvas canvas, float progress) {
        int ly = (int) (getBounds().bottom-getIntrinsicHeight()*progress);
        int ry = (int) (getBounds().top+ getIntrinsicHeight()*progress);
        canvas.drawLine(getBounds().left, ly, getBounds().right, ry, paint);
    }

    private void drawTopLeftLine(Canvas canvas, float progress) {
        int ry = (int) (getBounds().bottom-getIntrinsicHeight()*progress);
        int ly = (int) (getBounds().top+ getIntrinsicHeight()*progress);
        canvas.drawLine(getBounds().left, ly, getBounds().right, ry, paint);
    }



    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
}
