package com.travel.shop.adapter;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * 所有ListView的适配器的共通类
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/03/02
 * 
 * @param <T>
 */
public abstract class MyBaseAdapter<T> extends BaseAdapter {

	private ArrayList<T> mList;

	public MyBaseAdapter(ArrayList<T> listData) {
		this.mList = listData;
	}

	// 返回集合的大小，之前请求过的数据的条目个数
	@Override
	public int getCount() {
		return mList.size();
	}

	// 获取集合中指定的条目
	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	// 强制要求子类去实现获得每一个条目的方法
	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);

}
