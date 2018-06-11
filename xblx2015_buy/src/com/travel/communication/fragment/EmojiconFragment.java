package com.travel.communication.fragment;

import java.util.List;

import com.ctsmedia.hltravel.R;
import com.travel.communication.adapter.ListBaseAdapter;
import com.travel.communication.entity.DisplayRules;
import com.travel.communication.entity.Emojicon;
import com.travel.lib.utils.MLog;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
/**
 * Emojicon表情的fragment
 */
public class EmojiconFragment extends Fragment {
	@SuppressWarnings("unused")
	private static final String TAG = "EmojiconFragment";
	
	private static final int ITEM_PER_PAGE = 28;
	
	private ViewPager mViewPager;
	private RadioGroup mPoints;
	
	private GridView[] mGridViews;
	private RadioButton[] mRadioButtons;
	
	private List<Emojicon> mData;
	
	public interface OnEmojiconClickListener{
		/**
		 * emojicon图标被点击
		 * @param emojicon    被点击的emojicon图标
		 * @param isBackSpace  是否是删除图标 true -- 是删除
		 */
		void onEmojiconClick(Emojicon emojicon, boolean isBackSpace);
	}
	
	private OnEmojiconClickListener mListener;
	public void setOnEmojiconClickListener(OnEmojiconClickListener listener){
		this.mListener = listener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_emojicon, container, false);
		mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
		mPoints = (RadioGroup) rootView.findViewById(R.id.rg_point);
		
		initData();
		return rootView;
	}

	private void initData() {
		mData = DisplayRules.getAllByType();
		int size = mData.size();
		int pages = size / ITEM_PER_PAGE 
				+ (size % ITEM_PER_PAGE == 0 ? 0 : 1);
		MLog.v(TAG, "initData, and size is " + size + ", pages is " + pages);
		mGridViews = new GridView[pages];
		mRadioButtons = new RadioButton[pages];
		for(int i = 0; i < pages; i++){
			final int start = i * ITEM_PER_PAGE;
			int end = (start + ITEM_PER_PAGE) >= size ? size : (start + ITEM_PER_PAGE);
			final List<Emojicon> itemData = mData.subList(start, end);
			GridView gridView = new GridView(getActivity());
			gridView.setNumColumns(7);
			gridView.setHorizontalSpacing(1);
			gridView.setVerticalSpacing(1);
			gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			gridView.setCacheColorHint(0);
			gridView.setPadding(2, 0, 2, 0);
			gridView.setVerticalFadingEdgeEnabled(false);
			gridView.setAdapter(new EmojiconGridViewAdapter(itemData));
			gridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if(mListener != null){
						Emojicon emojicon = mData.get(position + start);
						mListener.onEmojiconClick(emojicon, DisplayRules.isDeleteEmojicon(emojicon));
					}
				}
			});
			mGridViews[i] = gridView;
			
			RadioButton radioButton = new RadioButton(getActivity());
			radioButton.setBackgroundResource(R.drawable.selector_bg_tip);
			RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(8, 8);
			params.leftMargin = 10;
			mPoints.addView(radioButton, params);
			if(i == 0) radioButton.setChecked(true);
			mRadioButtons[i] = radioButton;
		}
		
		mViewPager.setAdapter(new MViewPagerAdapter());
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				MLog.v(TAG, "onPageSelected, and positon is " + arg0);
				mRadioButtons[arg0].setChecked(true);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
	}
	
	// GridView 的 Adapter
	private class EmojiconGridViewAdapter extends ListBaseAdapter<Emojicon>{

		public EmojiconGridViewAdapter(List<Emojicon> list) {
			super(list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.chat_emojicon_grid_item, parent ,false);
			}
			((TextView)convertView).setText(getItem(position).getValue());
			return convertView;
		}
		
	}
	
	private class MViewPagerAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			return mGridViews.length;
		}
			
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mGridViews[position], 
					new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			return mGridViews[position];
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mGridViews[position]);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
		
	}
}
