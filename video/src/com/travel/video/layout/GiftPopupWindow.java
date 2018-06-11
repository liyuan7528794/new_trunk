package com.travel.video.layout;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.VideoConstant;
import com.travel.layout.HorizontalListView;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.video.gift.GiftAdapter;
import com.travel.video.gift.GiftBean;
import com.travel.video.gift.widget.PageControlView;
import com.travel.video.gift.widget.ScrollLayout;
import com.travel.video.gift.widget.ScrollLayout.OnScreenChangeListenerDataLoad;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GiftPopupWindow extends PopupWindow{
	
	private Context context;
	private View rootView;
	private GiftListener listener;
	
	private ScrollLayout scrollLayout;	//滚动页面
	private PageControlView pageControl;	//页显示状态
	
	private TextView changeMoney,giftNumText ;
	private ImageView minusImageView,plusImageView,sendGift;
	
	//礼物列表
	private int curPage = 0;	//当前页码
	private int pageNo = 0;		//总共有多少页
	private List<GiftAdapter> adapterList = new ArrayList<GiftAdapter>();
	private List<GiftBean> giftList = new ArrayList<GiftBean>();
	
	private int num = 0;
	
	// 当前选取的礼物对象
	private GiftBean curGiftBean = null;
	private int giftNum = 0;
	
	public GiftPopupWindow(Context context,List<GiftBean> list,GiftListener listener) {
		this.context = context;
		this.listener = listener;
		this.giftList = list;
		init();
		initData();
		initPopupWindow();
	}
	
	/**
	 * 初始化红币
	 */
	private void initData() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userid", UserSharedPreference.getUserId());
		NetWorkUtil.postForm(context, VideoConstant.GET_MY_RED_COIN, new MResponseListener() {
			
			@Override
			protected void onDataFine(JSONObject data) {
				if(data!=null){
					try {
						num = data.getInt("myRedcoinTotal");
						if(num < 100){
							changeMoney.setText(num + "红币" +" 充值>");
						}else{
							changeMoney.setText(num + "红币");
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}, map);
	}

	public interface GiftListener {
		void hideListener();
		void sendListener(GiftBean giftBean);
	}
	
	private void initPopupWindow() {
		//设置SelectPicPopupWindow的View  
        this.setContentView(rootView);
        //设置SelectPicPopupWindow弹出窗体的宽  
        this.setWidth(LayoutParams.MATCH_PARENT);  
        //设置SelectPicPopupWindow弹出窗体的高  
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击  
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果 
        this.setAnimationStyle(R.style.BelowPupWindowAnimation);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        rootView.setOnTouchListener(new OnTouchListener() {
             
            public boolean onTouch(View v, MotionEvent event) {
                int height = rootView.findViewById(R.id.giftLinearLayout).getTop();
                int y=(int) event.getY();
                if(event.getAction()==MotionEvent.ACTION_UP){
                    if(y<height){
                        dismiss();
                        listener.hideListener();
                    }
                }             
                return true;
            }  
        });  
	}
	
	private void init(){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.gift_pop_window, null);
		
		scrollLayout = (ScrollLayout) rootView.findViewById(R.id.scrollLayout);
		pageControl = (PageControlView) rootView.findViewById(R.id.pageControl);
		
		changeMoney = (TextView) rootView.findViewById(R.id.changeMoney);
		giftNumText = (TextView) rootView.findViewById(R.id.giftNum);
		minusImageView = (ImageView) rootView.findViewById(R.id.minusImageView);
		plusImageView = (ImageView) rootView.findViewById(R.id.addImageView);
		sendGift = (ImageView) rootView.findViewById(R.id.sendGift);
		
		changeMoney.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Constants.Recharge_ACTION);
				intent.setType(Constants.VIDEO_TYPE);
				intent.putExtra("withdraw_money", num+"");
				context.startActivity(intent);
			}
		});
		
		minusImageView.setOnClickListener(minusListener);
		plusImageView.setOnClickListener(addListener);
		sendGift.setOnClickListener(senListener);
		
		adapterList.clear();
		pageNo = (int)Math.ceil( giftList.size() / (GiftAdapter.PAGE_SIZE * 1.0f));
		for (int i = 0; i < pageNo; i++) {
			HorizontalListView giftPage = new HorizontalListView(context,null);
			GiftAdapter giftAdapter = new GiftAdapter(context, giftList, i);
			adapterList.add(giftAdapter);
			giftPage.setAdapter(giftAdapter);
			giftPage.setOnItemClickListener(itemClickListener);
			scrollLayout.addView(giftPage);
		}
		//加载分页
		pageControl.bindScrollViewGroup(scrollLayout);
		
		//加载分页数据
		scrollLayout.setOnScreenChangeListenerDataLoad(new OnScreenChangeListenerDataLoad() {
			public void onScreenChange(int currentIndex) {
				curPage = currentIndex;
			}
		});
	}
	
	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
			
			for(int j = 0; j<pageNo ;j++){
				
				if(j == curPage){
					adapterList.get(j).setSelectItem(position);
				}else{
					adapterList.get(j).setSelectItem(-1);
				}
				
				adapterList.get(j).notifyDataSetInvalidated(); 
			}
			
			// 当前是否选中礼物
			if(curGiftBean != null && giftList.get((curPage)*GiftAdapter.PAGE_SIZE + position).getId() == curGiftBean.getId()){
				curGiftBean = null;
				giftNum = 0;
				sendGift.setImageResource(R.drawable.gift_send_null);
			}else{
				curGiftBean = giftList.get((curPage)*GiftAdapter.PAGE_SIZE + position);
				giftNum = 1;
				sendGift.setImageResource(R.drawable.gift_send_icon);
			}
			giftNumText.setText(""+giftNum);
		}
		
	};
	
	private OnClickListener minusListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			if(giftNum > 1){
				giftNum = giftNum - 1;
				giftNumText.setText(""+giftNum);
			}
		}
	};
	
	private OnClickListener addListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(curGiftBean != null && num<curGiftBean.getPrice()*(giftNum+1)){
				Toast.makeText(context, "您的红币余额不足！", Toast.LENGTH_SHORT).show();
				return;
			}
			if(curGiftBean != null && curGiftBean.getId()!=3){
				giftNum = giftNum + 1;
				giftNumText.setText(""+giftNum);
			}
		}
	};
	
	private OnClickListener senListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(curGiftBean != null && num<curGiftBean.getPrice()*giftNum){
				Toast.makeText(context, "您的红币余额不足！", Toast.LENGTH_SHORT).show();
				return;
			}
			if(curGiftBean != null){
				curGiftBean.setNum(giftNum);
				listener.sendListener(curGiftBean);
			}
			
			initData();
		}
	};
	
}
