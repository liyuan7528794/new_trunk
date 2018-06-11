package com.travel.layout;

import com.travel.lib.R;
import com.travel.lib.utils.OSUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class XpaiRoundView extends View{
	public XpaiRoundView(Context context) {
		super(context);	
	}
	@SuppressWarnings("deprecation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//		Paint paint = new Paint();
//		/*去锯齿*/
//        paint.setAntiAlias(true);
//        /*设置paint的颜色*/
//        paint.setColor(Color.RED);
//        /*设置paint 的style为 FILL：实心*/
//        paint.setStyle(Paint.Style.FILL);
//        /*画一个实心圆*/
//		paint.setStrokeWidth(context.dp2px(context, 60));
        int screenWidth,screenHeight;    
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();    
        screenWidth = display.getWidth();    
        screenHeight = display.getHeight(); 
        canvas.drawBitmap(new int[]{R.drawable.mine_go_live}, 0, 400, 
        		screenWidth/2, screenHeight*12/20, OSUtil.dp2px(getContext(), 60), OSUtil.dp2px(getContext(), 60), false, null);
        canvas.drawBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.mine_go_live),
        		screenWidth/2, screenHeight*12/20, null);
	}
}
