package com.travel.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.travel.layout.HorizontalListView;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;

import java.util.ArrayList;
import java.util.List;

public final class ImageHorizontalListView extends HorizontalListView {
	private static final String TAG = "ImageHorizontalListView";

	private ImageAdapter adapter;
	public ImageHorizontalListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		adapter = new ImageAdapter(null);
		setAdapter(adapter);
		setDividerWidth(OSUtil.dp2px(context, 10));
	}
	
	/**
	 * 设置横向显示图片的地址
	 * @param urls 地址列表
	 */
	public void setUrls(List<String> urls){
		adapter.notifyDataSetChanged(urls);
	}
	/**
	 * 向列表中添加一个url串
	 */
	public void addOneUrl(String url){
		adapter.addOneUrl(url);
	}
	
	static class ImageAdapter extends BaseAdapter{
		
		private List<String> mUrls = new ArrayList<String>();
		
		public ImageAdapter(List<String> urls) {
			mUrls = urls;
		}
		
		public void addOneUrl(String url){
			mUrls.add(url);
			notifyDataSetChanged();
		}
		
		public void notifyDataSetChanged(List<String> urls) {
			mUrls = urls;
			super.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if(mUrls == null){
				return 0;
			}else{
				return mUrls.size();
			}
		}

		@Override
		public String getItem(int position) {
			return mUrls.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Context context = parent.getContext();
			ViewHolder viewHolder = null;
			if(convertView == null){
				convertView = new ImageView(context);
				viewHolder = new ViewHolder();
				viewHolder.image = (ImageView) convertView;
				convertView.setTag(viewHolder);
				int value = OSUtil.dp2px(context, 30);
				LayoutParams params = new LayoutParams(value, value);
				convertView.setLayoutParams(params);
			}else{
				viewHolder = (ViewHolder) convertView.getTag();
			}
			ImageDisplayTools.displayCircleImage(
					getItem(position), viewHolder.image, OSUtil.dp2px(context, 1));
			return convertView;
		}
	}
	
	static class ViewHolder{
		ImageView image;
	}
}
