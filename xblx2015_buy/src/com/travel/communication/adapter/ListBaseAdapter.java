package com.travel.communication.adapter;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
/**
 * 一个简单的BaseAdapter封装
 * 主要用于List对象的呈现
 *
 */
public abstract class ListBaseAdapter<T> extends BaseAdapter {
	
	private List<T> mList;
	
	public ListBaseAdapter(List<T> list) {
		mList = list;
	}

	@Override
	public int getCount() {
		if(mList == null) return 0;
		return mList.size();
	}
	
	public void notifyDataSetChanged(List<T> list) {
		mList = list;
		notifyDataSetChanged();
	}

	@Override
	public T getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
