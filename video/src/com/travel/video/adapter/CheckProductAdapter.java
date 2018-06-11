package com.travel.video.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.video.bean.ProductInfo;

import java.util.List;

public class CheckProductAdapter extends BaseAdapter{
	
	private List<ProductInfo> list;
	private LayoutInflater mInflater;
	private int  selectItem = -1;  
	public CheckProductAdapter(Context context,List<ProductInfo> list) {
		this.list = list;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ProductInfo productInfo =  list.get(position);
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.activity_product_check_item, null);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.checkImage);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.name.setText(productInfo.getName());
		if(position == selectItem){
			holder.image.setImageResource(R.drawable.icon_use_yes);
		}else{
			holder.image.setImageResource(R.drawable.icon_use_no);
		}
		return convertView;
	}
	
	/**
	 * 设置选中项标识
	 * @param selectItem
	 */
	public void setSelectItem(int selectItem) {  
		if(this.selectItem == selectItem){
			this.selectItem = -1;
		}else{
			this.selectItem = selectItem;
		}
    }  
	
	class ViewHolder {
		ImageView image;
		TextView name;
	}
}
