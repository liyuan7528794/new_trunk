package com.travel.video.gift;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.FormatUtils;

import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
@SuppressLint("ResourceAsColor")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GiftRelativeLayout extends RelativeLayout{
	private final static int QUEUE1 = 0x10001;
	private final static int QUEUE2 = 0x10002;
	private final static int QUEUE3 = 0x10003;
	
	private final static int QUEUEHL = 0x10004;
	
	private boolean isNullAima1 = true;
	private boolean isNullAima2 = true;
	private boolean isNullAima3 = true;
	
	private boolean ishlNullAima = true;
	
	private Context context ;
	private View rootView;
	
	private ViewTreeObserver vto2;
	
	
	private RelativeLayout hlUserLayout,hlAnimLayout,animation1,animation2,animation3;
	private TextView hlGiftName,hlUserNameText;
	private ImageView hlUserImage ;

	private float userLayoutWidth = 0;
	
	private LinkedList<GiftBean> simpleGiftList = new LinkedList<GiftBean>();
	private LinkedList<GiftBean> hlGiftList = new LinkedList<GiftBean>();
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
			public void handleMessage (Message msg) {//3、定义处理消息的方法
				GiftBean giftBean = (GiftBean) msg.obj;
                switch(msg.what) {
                case QUEUE1:
                    initData(giftBean,1);
                    break;
                case QUEUE2:
                    initData(giftBean,2);
                    break;
                case QUEUE3:
                    initData(giftBean,3);
                    break;
                case QUEUEHL:
                	initHlData(giftBean);
                	break;
                }
            }};
	public GiftRelativeLayout(Context context) {
		this(context, null);
	}
	
	public GiftRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public GiftRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
		
		init();
	}
	
	@SuppressLint("InflateParams")
	private void init(){
		rootView = LayoutInflater.from(context).inflate(R.layout.gift_relativelayout, null);
		addView(rootView, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		
		animation1 = (RelativeLayout) rootView.findViewById(R.id.animation1);
		animation2 = (RelativeLayout) rootView.findViewById(R.id.animation2);
		animation3 = (RelativeLayout) rootView.findViewById(R.id.animation3);

		hlAnimLayout = (RelativeLayout) rootView.findViewById(R.id.hlAnimation);
		hlUserLayout = (RelativeLayout) rootView.findViewById(R.id.hlUserLayout);
		hlUserLayout.setVisibility(View.GONE);
		hlUserImage = (ImageView) rootView.findViewById(R.id.userImage);
		hlGiftName = (TextView) rootView.findViewById(R.id.giftName);
		hlUserNameText = (TextView) rootView.findViewById(R.id.userName);
		
		msgThread.start();
		hlThread.start();
	}

	@SuppressWarnings({"deprecation", "ResourceType"})
	private void  initHlData(GiftBean giftBean){
		final ImageView giftIcon = new ImageView(context);
        
		AnimationDrawable animationDrawable = (AnimationDrawable) ContextCompat.getDrawable(context,
				R.drawable.anim_gift_hl);
		giftIcon.setBackgroundDrawable(animationDrawable);
		animationDrawable.start();
		
		hlAnimLayout.addView(giftIcon);
		
		hlUserLayout.setVisibility(View.VISIBLE);
		ImageDisplayTools.displayCircleImage(giftBean.getUserImage(), hlUserImage,0,android.R.color.transparent);
		hlGiftName.setText(giftBean.getName());
		hlUserNameText.setText(giftBean.getUserName());
		
		final ViewTreeObserver vto = giftIcon.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				userLayoutWidth = giftIcon.getMeasuredWidth();
				giftIcon.setTranslationX(-giftIcon.getMeasuredWidth());
				
				vto.removeGlobalOnLayoutListener(this);
				
				startHlAnimation(giftIcon);
			}
		});
		
	}
	
	@SuppressWarnings({"deprecation", "resourceType"})
	@SuppressLint({ "ResourceAsColor", "NewApi" })
	private void initData(GiftBean giftBean,int type) {
		//送礼人信息
		final RelativeLayout userLayout = new RelativeLayout(context);
		userLayout.setBackgroundResource(R.drawable.gift_user_background);
		LayoutParams userLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		userLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		
		//头像
		ImageView headImage = new ImageView(context);
		LayoutParams headImageParams = new LayoutParams(
				OSUtil.dp2px(context, 35),OSUtil.dp2px(context, 35));
		headImageParams.setMargins(OSUtil.dp2px(context, 2.5F), OSUtil.dp2px(context, 2.5F),
				OSUtil.dp2px(context, 2.5F), OSUtil.dp2px(context, 2.5F));
		userLayout.addView(headImage,headImageParams);
		headImage.setId(R.id.id_header_img);
		
		
		//用户名和礼物名的布局
		RelativeLayout linear = new RelativeLayout(context);
		LayoutParams linearLParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		linearLParams.addRule(RelativeLayout.CENTER_VERTICAL);
		linearLParams.addRule(RelativeLayout.RIGHT_OF, headImage.getId());
		linearLParams.leftMargin = OSUtil.dp2px(context, 10);
		linearLParams.rightMargin = OSUtil.dp2px(context, 45);
		userLayout.addView(linear, linearLParams);
		
		//用户名
		TextView userName = new TextView(context);
		userName.setId(R.id.id_user_name);
		userName.setTextSize(11);
		linear.addView(userName, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		//礼物名称
		TextView giftName = new TextView(context);
		giftName.setTextSize(11);
		LayoutParams giftParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		giftParams.addRule(RelativeLayout.BELOW, userName.getId());
		linear.addView(giftName, giftParams);
		
		
		//显示礼物
		final RelativeLayout giftShowLayout = new RelativeLayout(context);
		LayoutParams giftShowParams = new LayoutParams(LayoutParams.WRAP_CONTENT, OSUtil.dp2px(getContext(), 60));
		giftShowParams.addRule(RelativeLayout.CENTER_VERTICAL);
		
		//礼物图标
		ImageView giftIcon = new ImageView(context);
		giftIcon.setId(R.id.id_gif_icon);
		LayoutParams giftIconParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		giftIconParams.addRule(RelativeLayout.CENTER_VERTICAL);
		giftShowLayout.addView(giftIcon,giftIconParams);
		
		//数量
		final LinearLayout giftNumLayout = new LinearLayout(context);
		LayoutParams giftNumLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		giftNumLayoutParams.rightMargin = OSUtil.dp2px(getContext(), 100);
		giftNumLayoutParams.bottomMargin = OSUtil.dp2px(getContext(), 13);
		giftNumLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		giftNumLayoutParams.addRule(RelativeLayout.RIGHT_OF, giftIcon.getId());
		giftShowLayout.addView(giftNumLayout,giftNumLayoutParams);
		
		//x符号
		GiftHelper giftHelper = new GiftHelper(context, giftNumLayout, giftBean.getNum());
		giftNumLayout.setTag(giftHelper);
		
		//初始化数据
		userName.setText(giftBean.getUserName());
		SpannableStringBuilder spBuilder = FormatUtils.StringSetSpanColor(context, "送出了"+giftBean.getName(),
				giftBean.getName(),R.color.yellow_F1E600);
		giftName.setText(spBuilder);
		
//        //开始  
		if(giftBean.getId() == 1){
			giftIcon.setImageResource(R.drawable.gift_01);
		}else if(giftBean.getId() == 2){
			AnimationDrawable animationDrawable = (AnimationDrawable) ContextCompat.getDrawable(context,
					R.drawable.anim_gift2);
			giftIcon.setBackgroundDrawable(animationDrawable);
			animationDrawable.start();
		}
		
		ImageDisplayTools.displayCircleImage(giftBean.getUserImage(), headImage,0,android.R.color.transparent);
		
		if(type == 1){
			animation1.removeAllViews();
			animation1.addView(userLayout,userLayoutParams);
			animation1.addView(giftShowLayout,giftShowParams);
			animation1.setTag(giftBean);
			giftShowLayout.setTag(1);//通道识别
		}else if(type == 2){
			animation2.removeAllViews();
			animation2.addView(userLayout,userLayoutParams);
			animation2.addView(giftShowLayout,giftShowParams);
			animation2.setTag(giftBean);
			giftShowLayout.setTag(2);
		}else if(type == 3){
			animation3.removeAllViews();
			animation3.addView(userLayout,userLayoutParams);
			animation3.addView(giftShowLayout,giftShowParams);
			animation3.setTag(giftBean);//用来连击的识别
			giftShowLayout.setTag(3);
		}else{
			return;
		}
		giftName.setTextColor(context.getResources().getColorStateList(android.R.color.white));
		userName.setTextColor(context.getResources().getColorStateList(android.R.color.white));
		vto2 = userLayout.getViewTreeObserver();
		vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				userLayoutWidth = userLayout.getMeasuredWidth();
				userLayout.setTranslationX(-userLayout.getMeasuredWidth());
				giftShowLayout.setTranslationX(-userLayout.getMeasuredWidth());
				
				vto2.removeGlobalOnLayoutListener(this);
				
				startSimpleAnimation(userLayout,giftShowLayout,giftNumLayout);
			}
		});
		
	}
	
	/**普通动画开始*/
	private void startSimpleAnimation(final View userView,final View giftView,final LinearLayout numView){
		numView.setVisibility(View.INVISIBLE);
		//userView
		ObjectAnimator userViewStartAnimation = ObjectAnimator.ofFloat(userView, "translationX", 
				userView.getTranslationX(), 
				userView.getTranslationX() + userLayoutWidth + OSUtil.dp2px(context, 10)
				).setDuration(200);
		
		//giftView
		ObjectAnimator giftViewStartAnimation = ObjectAnimator.ofFloat(giftView, "translationX", 
				userView.getTranslationX(), 
				userView.getTranslationX() + 2*userLayoutWidth - OSUtil.dp2px(context, 40)
				).setDuration(500);
		
		giftViewStartAnimation.addListener(new AnimatorListenerAdapter(){
			@Override
			public void onAnimationEnd(Animator animation){
				numView.setVisibility(View.VISIBLE);
				numSimpleAnimation(numView,userView,giftView);
				Log.d("Animation",giftView.getTag()+"" );
			}
		});
		
		userViewStartAnimation.start();
		giftViewStartAnimation.start();
		
	}
	/**数量动画*/
	private void numSimpleAnimation(final LinearLayout view,final View userView,final View giftView){
		
		final ObjectAnimator numAnim = ObjectAnimator.ofFloat(view, "scaleY",1f, 1f).setDuration(100);
		
		Animation animation=AnimationUtils.loadAnimation(context, R.anim.gift_scale_decelerate_animation);
		final Animation animation1=AnimationUtils.loadAnimation(context, R.anim.gift_scale_accelerate_animation);
        view.startAnimation(animation);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {}
            
            @Override
            public void onAnimationRepeat(Animation arg0) {}
            
            @Override
            public void onAnimationEnd(Animation arg0) {
                numAnim.start();
            }
        });
        
        numAnim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation){
				view.startAnimation(animation1);
			}
		});
		
        animation1.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {}
            
            @Override
            public void onAnimationRepeat(Animation arg0) {}
            
            @Override
            public void onAnimationEnd(Animation arg0) {
            	GiftHelper giftHelper = (GiftHelper) view.getTag();
				int num = giftHelper.getNum();
				int count = giftHelper.getCount();
				if(count <= num){
					giftHelper.setNum(count);
					numSimpleAnimation(view,userView,giftView);
				}else{
					endSimpleAnimation(userView, giftView);
				}
            }
        });
		
	}
	/**普通动画结束*/
	private void endSimpleAnimation(final View userView,final View giftView){
		final ObjectAnimator userViewAlphaAnimation = ObjectAnimator.ofFloat(userView, "alpha", 0f).setDuration(1500);
		final ObjectAnimator giftViewAlphaAnimation = ObjectAnimator.ofFloat(giftView, "alpha", 0f).setDuration(1500);
		userViewAlphaAnimation.addListener(new AnimatorListenerAdapter(){
			@Override
			public void onAnimationEnd(Animator animation){
				if(giftView.getTag().equals(1)){
					isNullAima1 = true;
					notifyGift();
				}else if(giftView.getTag().equals(2)){
					isNullAima2 = true;
					notifyGift();
				}else if(giftView.getTag().equals(3)){
					isNullAima3 = true;
					notifyGift();
				}
				ViewGroup parent1 = (ViewGroup) userView.getParent();
				if (parent1 != null)
					parent1.removeView(userView);
				
				ViewGroup parent2 = (ViewGroup) giftView.getParent();
				if (parent2 != null)
					parent2.removeView(giftView);
			}
		});
		userViewAlphaAnimation.start();
		giftViewAlphaAnimation.start();
	}
	
	private void startHlAnimation(final View view){
		ObjectAnimator hlStartAnimation = ObjectAnimator.ofFloat(view, "translationX", 
				view.getTranslationX(), OSUtil.getScreenWidth())
				.setDuration(4000);
		
		hlStartAnimation.addListener(new AnimatorListenerAdapter(){
			@Override
			public void onAnimationEnd(Animator animation){
				hlUserLayout.setVisibility(View.GONE);
				ishlNullAima = true;
				notifyhlGift();
				ViewGroup parent1 = (ViewGroup) view.getParent();
				if (parent1 != null)
					parent1.removeView(view);
			}
		});
		
		hlStartAnimation.start();
	}
	
	private boolean isStop = false;
	public void stop(){
		isStop = true;
	}
	
	/** 显示动画 */
	public void showGiftAnimation(GiftBean giftBean){
		if(giftBean!=null && giftBean.getId() == 3){
			hlGiftList.add(giftBean);
			notifyhlGift();
		}else{
			simpleGiftList.add(giftBean);
			notifyGift();
		}
		
	}
	
	private Thread hlThread = new Thread(new Runnable() {
		
		@Override
		public void run() {
			while (!isStop) {
				if(hlGiftList.size()>0){
					Lock listlock = new ReentrantLock();
					try {
						listlock.lock();
						Message msg = new Message();
						if(ishlNullAima){
							msg.what = QUEUEHL;
							msg.obj = hlGiftList.removeFirst();
							ishlNullAima = false;
						}
	                    
	                    mHandler.sendMessage(msg);
	                    Thread.sleep(100);
	                    Log.e("giftSize1---------->", simpleGiftList.size()+"");
					} catch (Exception e) {
					}finally {
						listlock.unlock();
					}
					
				}else{
					Log.e("挂起线程1","1");
					synchronized (hlGiftList) {
						try {
							hlGiftList.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	});
	
	private void notifyhlGift(){
		synchronized (hlGiftList) {
			Log.e("giftSize:", simpleGiftList.size()+"");
			hlGiftList.notify();
		}
	}
	
	private void notifyGift(){
		synchronized (simpleGiftList) {
			Log.e("giftSize:", simpleGiftList.size()+"");
			simpleGiftList.notify();
		}
	}
	private Thread msgThread = new Thread(new Runnable() {
		
		@Override
		public void run() {
			while (!isStop) {
				if(simpleGiftList.size()>0){
					Lock listlock = new ReentrantLock();
					try {
						listlock.lock();
						Message msg = new Message();
						if(isNullAima1){
							msg.what = QUEUE1;
							msg.obj = simpleGiftList.removeFirst();
							isNullAima1 = false;
						}else if(isNullAima2){
							msg.what = QUEUE2;
							msg.obj = simpleGiftList.removeFirst();
							isNullAima2 = false;
						}else if(isNullAima3){
							msg.what = QUEUE3;
							msg.obj = simpleGiftList.removeFirst();
							isNullAima3 = false;
						}
	                    
	                    mHandler.sendMessage(msg);
	                    Thread.sleep(100);
	                    Log.e("giftSize1---------->", simpleGiftList.size()+"");
					} catch (Exception e) {
					}finally {
						listlock.unlock();
					}
					
				}else{
					Log.e("挂起线程1","1");
					synchronized (simpleGiftList) {
						try {
							simpleGiftList.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	});
	
}