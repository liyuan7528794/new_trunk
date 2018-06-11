package com.travel.video.gift;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.OSUtil;

import java.util.ArrayList;
import java.util.List;

public class GiftAdapter extends BaseAdapter {
	private List<GiftBean> mList;
	private Context mContext;
	public static final int PAGE_SIZE = 3;
	private int  selectItem = -1;  
	private int width;
	
	public GiftAdapter(Context context, List<GiftBean> list, int page) {
		mContext = context;
		width = OSUtil.getScreenWidth();
		
		mList = new ArrayList<>();
		int i = page * PAGE_SIZE;
		int iEnd = i+PAGE_SIZE;
		while ((i<list.size()) && (i<iEnd)) {
			mList.add(list.get(i));
			i++;
		}
	}
	public int getCount() {
		return mList.size();
	}

	public Object getItem(int position) {
		return mList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		GiftBean info = mList.get(position);
		GiftItem giftItem;
		if (convertView == null) {
			View v = LayoutInflater.from(mContext).inflate(R.layout.gift_item, null);
			
			giftItem = new GiftItem();
			
			giftItem.itemLayout = (RelativeLayout) v.findViewById(R.id.itemLayout);
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width/3, ViewGroup.LayoutParams.WRAP_CONTENT);
			giftItem.itemLayout.setLayoutParams(params);
			
			giftItem.giftLayout = (LinearLayout) v.findViewById(R.id.giftLayout);
			
			giftItem.image = (ImageView)v.findViewById(R.id.image);
			giftItem.name = (TextView)v.findViewById(R.id.name);
			giftItem.num = (TextView)v.findViewById(R.id.num);
			
			v.setTag(giftItem);
			convertView = v;
		} else {
			giftItem = (GiftItem)convertView.getTag();
		}
		giftItem.image.setImageResource(info.getImage());
		giftItem.name.setText(info.getName());
		giftItem.num.setText(info.getPrice()+"");
		if (position == selectItem) {  
			giftItem.giftLayout.setBackgroundResource(R.drawable.gift_background);  
        }   
        else {  
        	giftItem.giftLayout.setBackgroundResource(android.R.color.transparent);
        }     
		return convertView;
	}
	
	public void setSelectItem(int selectItem) {  
		if(this.selectItem == selectItem){
			this.selectItem = -1;
		}else{
			this.selectItem = selectItem;
		}
    }
	/**
	 * 每个应用显示的内容，包括图标和名称
	 * @author Yao.GUET
	 *
	 */
	private class GiftItem {
		ImageView image;
		TextView name,num;
		RelativeLayout itemLayout;
		LinearLayout giftLayout;
	}
}
