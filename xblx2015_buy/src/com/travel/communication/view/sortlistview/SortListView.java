package com.travel.communication.view.sortlistview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;

import java.util.Collections;
import java.util.List;


/**
 * 自动排序并且有侧边栏的的listView
 * Created by ldkxingzhe on 2016/3/21.
 */
public class SortListView<T> extends RelativeLayout implements AbsListView.OnScrollListener {

    @SuppressWarnings("unused")
    private static final String TAG = "SortListView";
    // UI部分, 侧边栏, 列表项, 文本框
    private SideBar mSideBar;
    private ListView mListView;
    private TextView mTextView;
    private TextView mAlphaTextView;

    // 列表数据, 与adapter
    private List<SortModel<T>> mListData;
    private SortListViewAdapter mAdapter;

    private int mCurrentFirstPosition;
    private Handler mHandler;
    private CharacterParser mPinyinParser;

    // 是否是由点击sidebar引起的自动滑动 true -- 是
    private boolean isSmoothScroll = false;

    public interface OnItemClickListener<T> {
        /**
         * 列表项被点击
         * @param view      当前SortListView
         * @param object    被单击的列表项代表的数据T
         */
        void onItemClick(SortListView view,T object );
    }
    private OnItemClickListener<T> onItemClickListener;
    /**
     * 设置ItemClick点击事件
     */
    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface AdapterListener<T>{
        /**
         * 可选的自定义列表项外观回调接口, 此接口可选, 有默认效果
         * @param model          对应的model
         * @param convertView    convertView, 可用于重复利用
         * @param viewParent     viewParent
         * @return  返回的视图
         */
        View getView(SortModel<T> model, View convertView, ViewGroup viewParent);
    }
    private AdapterListener<T> mAdapterListener;

    /**
     * 设置adatper
     */
    public void setAdapterListener(AdapterListener<T> adapterListener) {
        this.mAdapterListener = adapterListener;
    }

    /**
     * 设置列表项数据
     * @param data    数据, 为List<SortModel<T>>型
     */
    public void setListData(List<SortModel<T>> data){
        this.mListData = data;
        setEachDataUpperCase();

        Collections.sort(mListData, new PinyinComparator());

        for(int i = 0; i < SideBar.UPPERCASE.length; i++){
            int position = getFirstPositionOfUpperCase(String.valueOf(SideBar.UPPERCASE[i]));
            if(position == -1){
                mSideBar.setPositionEnable(i, false);
            }else{
                mSideBar.setPositionEnable(i, true);
            }
        }
        if(mAdapter == null){
            mAdapter = new SortListViewAdapter();
            mListView.setAdapter(mAdapter);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void setEachDataUpperCase() {
        for(SortModel<T> model : mListData){
            String pinyin = mPinyinParser.getSelling(model.getName());
            String capital = pinyin.substring(0, 1).toUpperCase();
            if(capital.matches("[A-Z]")){
                model.setCapitalLetters(pinyin);
            }else{
                model.setCapitalLetters("#");
            }
        }
    }

    public SortListView(Context context) {
        this(context, null);
    }

    public SortListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SortListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHandler = new Handler();
        mPinyinParser = CharacterParser.getInstance();
        initView();
        initListener();
    }
    // 初始化监听
    private void initListener() {
        mListView.setOnScrollListener(this);
        mSideBar.setListener(new SideBar.SideBarListener() {
            @Override
            public void onItemTouched(SideBar view, int choosedPosition, int action) {
                String upperCase = String.valueOf(SideBar.UPPERCASE[choosedPosition]);
                setTextView(upperCase);
                int firstPosition = getProperPositionOfUpperCase(upperCase);
                if (firstPosition >= 0) {
                    int targetPosition = firstPosition;
                    if (choosedPosition > view.getChoosePosition()) {
                        // 防止需要滑动的条目滑动到最下面
                        targetPosition += mListView.getLastVisiblePosition() - mListView.getFirstVisiblePosition() - 1;
                        Log.v(TAG, "targetPosition added number : ");
                    }
                    mListView.smoothScrollToPosition(targetPosition);
                    isSmoothScroll = true;
                } else {
                    Log.e(TAG, "There is no category: " + upperCase);
                }
            }
        });
    }

    private int getProperPositionOfUpperCase(String c){
        int position = -1;
        for(int i = 0; i < SideBar.UPPERCASE.length; i++){
            if(String.valueOf(SideBar.UPPERCASE[i]).equals(c)){
                position = i;
                break;
            }
        }
        if(position == -1) throw new IllegalStateException("position is -1");

        for(int i = 0; i < SideBar.UPPERCASE.length; i++ ){
            if(mSideBar.isEnableOfPosition(position - i)){
                return getFirstPositionOfUpperCase(String.valueOf(SideBar.UPPERCASE[position - i]));
            }
            if(mSideBar.isEnableOfPosition(position + i)){
                return getFirstPositionOfUpperCase(String.valueOf(SideBar.UPPERCASE[position + i]));
            }
        }
        return -1;
    }

    private int getFirstPositionOfUpperCase(String c) {
        for(int i = 0, length = mListData.size(); i < length; i++){
            SortModel<T> model = mListData.get(i);
            if(model.getCapitalLetters().toUpperCase().startsWith(c)){
                return i;
            }
        }
        return -1;
    }

    private void initView() {
        mSideBar = new SideBar(getContext());
        LayoutParams params
                = new LayoutParams(80, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        int margin = OSUtil.dp2px(getContext(), 5);
		params.topMargin = margin;
		params.bottomMargin = margin;
        mSideBar.setId(R.id.about_version_code);
        addView(mSideBar, params);

        // 添加ListView
        mListView = new ListView(getContext());
        LayoutParams listViewParams
                = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        listViewParams.addRule(RelativeLayout.LEFT_OF, R.id.about_version_code);
        addView(mListView, listViewParams);
        mListView.setVerticalScrollBarEnabled(false);
        mListView.setDivider(null);
        mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if(onItemClickListener != null){
					onItemClickListener.onItemClick(SortListView.this, mListData.get(arg2).getRealObject());
				}
			}
		});

        mTextView = new TextView(getContext());
        LayoutParams textViewParams
                = new LayoutParams(200, 200);
        textViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(mTextView, textViewParams);
        mTextView.setBackgroundColor(Color.GREEN);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setTextSize(20);
        mTextView.setVisibility(INVISIBLE);

        mAlphaTextView = new TextView(getContext());
        ViewGroup.LayoutParams alphaParam =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(mAlphaTextView, alphaParam);
        mAlphaTextView.setBackgroundColor(Color.GRAY);
        mAlphaTextView.setVisibility(View.GONE);
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // none
        switch (scrollState){
            case SCROLL_STATE_FLING:
                Log.v(TAG,"state fling");
                break;
            case SCROLL_STATE_IDLE:
                Log.v(TAG, "state idle");
                break;
            case SCROLL_STATE_TOUCH_SCROLL:
                Log.v(TAG, "state touch scroll");
                isSmoothScroll = false;
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(mCurrentFirstPosition == firstVisibleItem) return;
        if(isSmoothScroll) return;
        mCurrentFirstPosition = firstVisibleItem;
        Log.v(TAG, "onScroll, and position chanaged, new value is " + firstVisibleItem);
        char upper = mAdapter.getItem(mCurrentFirstPosition).getCapitalLetters().charAt(0);
        upper = Character.toUpperCase(upper);
        setTextView(String.valueOf(upper));
        if(upper >= 'A' && upper <= 'Z'){
        	MLog.v(TAG, "onScroll, and new upperCase is " + upper);
        	mSideBar.setChoosePosition(upper - 'A'); 
        }
        mAlphaTextView.setText(String.valueOf(upper));
    }

    private void setTextView(String text) {
        mTextView.setText(text);
        mTextView.setVisibility(VISIBLE);
        mHandler.removeMessages(0);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mTextView.setVisibility(INVISIBLE);
            }
        }, 1000);
    }

    private class SortListViewAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            if(mListData == null){
                return 0;
            }else{
                return mListData.size();
            }
        }

        @Override
        public SortModel<T> getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(mAdapterListener != null) return mAdapterListener.getView(getItem(position), convertView, parent);
            if(convertView == null){
                convertView = new TextView(parent.getContext());
                ((TextView)convertView).setTextSize(20);
                convertView.setPadding(20, 20, 20, 20);
            }
            ((TextView)convertView).setText(getItem(position).getName());
            return convertView;
        }
    }
}
