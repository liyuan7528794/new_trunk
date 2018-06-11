package com.travel.video.gift.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.OSUtil;
import com.travel.video.gift.widget.ScrollLayout.OnScreenChangeListener;


public class PageControlView extends LinearLayout {
	private Context context;

	private int count;

	public void bindScrollViewGroup(ScrollLayout scrollViewGroup) {
		this.count=scrollViewGroup.getChildCount();
		System.out.println("count="+count);
		generatePageControl(scrollViewGroup.getCurrentScreenIndex());
		
		scrollViewGroup.setOnScreenChangeListener(new OnScreenChangeListener() {
			
			public void onScreenChange(int currentIndex) {
				// TODO Auto-generated method stub
				generatePageControl(currentIndex);
			}
		});
	}

	public PageControlView(Context context) {
		super(context);
		this.init(context);
	}
	public PageControlView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init(context);
	}

	private void init(Context context) {
		this.context=context;
	}

	private void generatePageControl(int currentIndex) {
		this.removeAllViews();

		int pageNum = 6; // 显示多少个 
		int pageNo = currentIndex+1; //第几页
		int pageSum = this.count; //总共多少页
		
		
		if(pageSum>1){
			int currentNum = (pageNo % pageNum == 0 ? (pageNo / pageNum) - 1  
	                 : (int) (pageNo / pageNum))   
	                 * pageNum; 
			
			 if (currentNum < 0)   
	             currentNum = 0;   
			 
			 if (pageNo > pageNum){
				 /*ImageView imageView = new ImageView(context);
				 imageView.setImageResource(R.drawable.zuo);
				 this.addView(imageView);*/
			 }
			 
			 
			 
			 for (int i = 0; i < pageNum; i++) {   
	             if ((currentNum + i + 1) > pageSum || pageSum < 2)   
	                 break;   
	             
	             ImageView imageView = new ImageView(context);
	             LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	             params.leftMargin = OSUtil.dp2px(getContext(), 3);
	             params.rightMargin = OSUtil.dp2px(getContext(), 3);
	             imageView.setLayoutParams(params);
	             if(currentNum + i + 1 == pageNo){
	            	 imageView.setImageResource(R.drawable.oval_f_3);
	             }else{
	            	 imageView.setImageResource(R.drawable.oval_f30_3);
	             }
	             this.addView(imageView);
	         }  
			 
			 if (pageSum > (currentNum + pageNum)) {
				 /*ImageView imageView = new ImageView(context);
				 imageView.setImageResource(R.drawable.you);
				 this.addView(imageView);*/
			 }
		}
	}
}

