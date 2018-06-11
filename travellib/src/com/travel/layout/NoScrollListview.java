package com.travel.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class NoScrollListview extends ListView{  
	  
    public NoScrollListview(Context context, AttributeSet attrs) {  
            super(context, attrs);  
    }  
      
    /** 
     * 设置不滚动 
     */  
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)  
    {  
            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,  
                            MeasureSpec.AT_MOST);  
            super.onMeasure(widthMeasureSpec, expandSpec);  

    }  
  //通过重新dispatchTouchEvent方法来禁止滑动
  	@Override
  	public boolean dispatchTouchEvent(MotionEvent ev) {
  		//TODO Auto-generated method stub
  		if(ev.getAction() == MotionEvent.ACTION_MOVE){
  		        return true;//禁止Gridview进行滑动
  		    }
  		return super.dispatchTouchEvent(ev);
  	}
}
