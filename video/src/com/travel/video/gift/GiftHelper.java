package com.travel.video.gift;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.OSUtil;

public class GiftHelper {
	private ImageView signImage;
	private ImageView oneImage;
	private ImageView twoImage;
	private ImageView threeImage;
	private ImageView fourImage;
	private ImageView fiveImage;
	
	
	private int num = 0;
	private int count = 1;
	public GiftHelper(Context context,LinearLayout giftNumLayout,int num) {
		// TODO Auto-generated constructor stub
		this.num = num;
		createNumImageView(context, giftNumLayout);
		
	}
	
	private static int mateNumIcon(int num){
		int resours = 0;
		switch (num) {
		case 0:
			resours = R.drawable.n_0;
			break;
		case 1:
			resours = R.drawable.n_1;	
			break;
		case 2:
			resours = R.drawable.n_2;
			break;
		case 3:
			resours = R.drawable.n_3;
			break;
		case 4:
			resours = R.drawable.n_4;
			break;
		case 5:
			resours = R.drawable.n_5;
			break;
		case 6:
			resours = R.drawable.n_6;
			break;
		case 7:
			resours = R.drawable.n_7;
			break;
		case 8:
			resours = R.drawable.n_8;
			break;
		case 9:
			resours = R.drawable.n_9;
			break;
		default:
			break;
		}
		return resours;
	}
	
	public void createNumImageView(Context context,LinearLayout giftNumLayout) {
		giftNumLayout.removeAllViews();
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(OSUtil.dp2px(context, 20), OSUtil.dp2px(context, 20));
			
			oneImage = new ImageView(context);
			twoImage = new ImageView(context);
			threeImage = new ImageView(context);
			fourImage = new ImageView(context);
			fiveImage = new ImageView(context);
			signImage = new ImageView(context);
			signImage.setImageResource(R.drawable.multiple);
			
			setNum(1);
			
			giftNumLayout.addView(signImage,params);
			giftNumLayout.addView(fiveImage,params);
			giftNumLayout.addView(fourImage,params);
			giftNumLayout.addView(threeImage,params);
			giftNumLayout.addView(twoImage,params);
			giftNumLayout.addView(oneImage,params);
			
	}
	
	public void setNum(int nums){
		if(nums>300 && nums+15 < num){
			nums = num - 10;
		}
		if(nums<10){
			oneImage.setVisibility(View.VISIBLE);
			oneImage.setImageResource(mateNumIcon(nums));
			twoImage.setVisibility(View.GONE);
			threeImage.setVisibility(View.GONE);
			fourImage.setVisibility(View.GONE);
			fiveImage.setVisibility(View.GONE);
			
		}else if(nums>=10 && nums <100){
			
			oneImage.setVisibility(View.VISIBLE);
			oneImage.setImageResource(mateNumIcon(nums%10));
			twoImage.setVisibility(View.VISIBLE);
			twoImage.setImageResource(mateNumIcon(nums/10%10));
			threeImage.setVisibility(View.GONE);
			fourImage.setVisibility(View.GONE);
			fiveImage.setVisibility(View.GONE);
			
		}else if(nums>=100 && nums<1000){
			
			oneImage.setVisibility(View.VISIBLE);
			oneImage.setImageResource(mateNumIcon(nums%10));
			twoImage.setVisibility(View.VISIBLE);
			twoImage.setImageResource(mateNumIcon(nums/10%10));
			threeImage.setVisibility(View.VISIBLE);
			threeImage.setImageResource(mateNumIcon(nums/100%10));
			fourImage.setVisibility(View.GONE);
			fiveImage.setVisibility(View.GONE);
			
		}else if(nums>=1000 && nums<10000){
			
			oneImage.setVisibility(View.VISIBLE);
			oneImage.setImageResource(mateNumIcon(mateNumIcon(nums%10)));
			twoImage.setVisibility(View.VISIBLE);
			twoImage.setImageResource(mateNumIcon(mateNumIcon(nums/10%10)));
			threeImage.setVisibility(View.VISIBLE);
			threeImage.setImageResource(mateNumIcon(mateNumIcon(nums/100%10)));
			fourImage.setVisibility(View.VISIBLE);
			fourImage.setImageResource(mateNumIcon(mateNumIcon(nums/1000%10)));
			fiveImage.setVisibility(View.GONE);
		}else if(nums>=10000 && nums<100000){
			
			oneImage.setVisibility(View.VISIBLE);
			oneImage.setImageResource(mateNumIcon(mateNumIcon(nums%10)));
			twoImage.setVisibility(View.VISIBLE);
			twoImage.setImageResource(mateNumIcon(mateNumIcon(nums/10%10)));
			threeImage.setVisibility(View.VISIBLE);
			threeImage.setImageResource(mateNumIcon(mateNumIcon(nums/100%10)));
			fourImage.setVisibility(View.VISIBLE);
			fourImage.setImageResource(mateNumIcon(mateNumIcon(nums/1000%10)));
			fiveImage.setVisibility(View.VISIBLE);
			fourImage.setImageResource(mateNumIcon(mateNumIcon(nums/10000%10)));
		}
		count = nums+1;
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getNum(){
		return num;
	}
}
