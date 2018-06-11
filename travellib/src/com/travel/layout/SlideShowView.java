package com.travel.layout;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.travel.bean.NotifyBean;
import com.travel.lib.R;
import com.travel.lib.TravelApp;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class SlideShowView extends FrameLayout implements View.OnTouchListener {
    public static final int MARKE_ACTIVITYS = 1;
    public static final int MARKE_TRAILER = 2;
    public static final int MARKE_ADVERTISING = 3;
    public static final int MARKE_CURLIVE = 4;
    public static final int MARKE_NOTICE = 5;

    // 自动轮播启用开关
    private final static boolean isAutoPlay = true;

    private List<NotifyBean> list = new ArrayList<NotifyBean>();
    private RadioGroup rg;
    private ViewPager viewPager;

    // 定时任务
    private ScheduledExecutorService scheduledExecutorService;

    private OnItemClickListener listener;
    private boolean isMap = false;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (list == null || list.size() == 0)
                return;
            // 切换pager页
            int position = viewPager.getCurrentItem() + 1;
            viewPager.setCurrentItem(position, true);
            ((RadioButton) rg.getChildAt(position % list.size())).setChecked(true);
        }

    };

    public SlideShowView(Context context) {
        this(context, null);
    }

    public SlideShowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideShowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initUI(context);
    }

    private void initUI(final Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_slideshow, this, true);

        rg = (RadioGroup) findViewById(R.id.dotLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
    }

    public void isMap() {
        isMap = true;
    }

    /**
     * 设置轮滚数据
     */
    public void setList(List<NotifyBean> list) {
        this.list = list;
        if (list == null || list.size() == 0)
            return;

        RadioGroup.LayoutParams param = new RadioGroup.LayoutParams(OSUtil.dp2px(TravelApp.appContext, 10),
                OSUtil.dp2px(TravelApp.appContext, 10));
        param.leftMargin = OSUtil.dp2px(TravelApp.appContext, 3);
        param.rightMargin = OSUtil.dp2px(TravelApp.appContext, 3);
        rg.removeAllViews();
        // 热点个数与图片特殊相等
        for (int i = 0; i < list.size(); i++) {
            // 添加小圆点
            RadioButton rb = new RadioButton(TravelApp.appContext);
            rb.setId(i + 1);
            rb.setButtonDrawable(R.drawable.slide_radiobutton);
            if (i == 0) {
                rb.setChecked(true);
            }
            rg.addView(rb, param);
        }

        viewPager.setAdapter(new MyPagerAdapter());
        viewPager.setOnPageChangeListener(new MyPageChangeListener());
        viewPager.setOnTouchListener(this);//占用触摸事件，以免其他触摸 事件运行
    }

    /**
     * 开始轮播图切换
     */
    public void startPlay() {
        /*if (scheduledExecutorService == null || scheduledExecutorService.isShutdown()) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
			scheduledExecutorService.scheduleAtFixedRate(new SlideShowTask(), 4, 4, TimeUnit.SECONDS);
		}*/
    }

    /**
     * 停止轮播图切换
     */
    public void stopPlay() {
        /*if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
			scheduledExecutorService.shutdown();
		}*/

    }

    /**
     * item点击事件
     *
     * @param listener
     */
    public void setOnItemClick(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    private class MyPageChangeListener implements OnPageChangeListener {

        boolean isAutoPlay = false;

        @Override
        public void onPageScrollStateChanged(int arg0) {
            switch (arg0) {
                case 1:// 手势滑动，空闲中
                    isAutoPlay = false;
                    break;
                case 2:// 界面切换中
                    isAutoPlay = true;
                    break;
                case 0:// 滑动结束，即切换完毕或者加载完毕
                    // 当前为最后一张，此时从右向左滑，则切换到第一张
                    if (list == null || list.size() == 0)
                        return;
                    if (!isAutoPlay) {
                        viewPager.setCurrentItem(viewPager.getCurrentItem() % list.size());
                    }
                    if (mOnPositionSlideListener != null)
                        mOnPositionSlideListener.onSlide(viewPager.getCurrentItem() % list.size());
                    break;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int pos) {
            if (list == null || list.size() == 0)
                return;
            ((RadioButton) rg.getChildAt(pos % list.size())).setChecked(true);
        }

    }

    /**
     * 执行轮播图切换任务
     */
    private class SlideShowTask implements Runnable {

        @Override
        public void run() {
            synchronized (viewPager) {
                handler.obtainMessage().sendToTarget();
            }
        }

    }

    /**
     * 销毁ImageView资源，回收内存
     */
    private void destoryBitmaps() {

    }


    /**
     * 填充ViewPager的页面适配器
     */
    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
            //			super.destroyItem(container,position,object);
        }

        @Override
        public Object instantiateItem(View container, final int position) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            RelativeLayout relativeLayout = createItemView(position % (list.size() == 0 ? 1 : list.size()));
            ((ViewGroup) container).addView(relativeLayout, params);
            return relativeLayout;
        }

        @Override
        public int getCount() {
            return isInfinet ? Integer.MAX_VALUE : list.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {

        }

        @Override
        public void finishUpdate(View arg0) {

        }
    }

    private RelativeLayout createItemView(final int position) {
        RelativeLayout layout = new RelativeLayout(getContext());
        ImageView imageView = new ImageView(getContext());
        imageView.setScaleType(ScaleType.CENTER_CROP);
        imageView.setFitsSystemWindows(true);
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(null, v, position, position);
                }
            }
        });
        ImageDisplayTools.displayImage(list.get(position).getImgUrl(), imageView);
        layout.addView(imageView, imageParams);

        ImageView markImage = new ImageView(getContext());
        RelativeLayout.LayoutParams markParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        markParams.topMargin = OSUtil.dp2px(getContext(), 15);
        markParams.rightMargin = OSUtil.dp2px(getContext(), 15);
        markParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        String type = list.get(position).getType();
        if (("" + MARKE_ACTIVITYS).equals(type)) {
            markImage.setImageResource(R.drawable.activitys_mark);
        } else if (("" + MARKE_TRAILER).equals(type)) {
            markImage.setImageResource(R.drawable.trailer_mark);
        } else if (("" + MARKE_ADVERTISING).equals(type)) {
            markImage.setImageResource(R.drawable.advertising_mark);
        } else if (("" + MARKE_CURLIVE).equals(type)) {
            markImage.setImageResource(R.drawable.curlive_mark);
        } else if (("" + MARKE_NOTICE).equals(type)) {
            markImage.setImageResource(R.drawable.notice_mark);
        }
        layout.addView(markImage, markParams);
        markImage.setVisibility(View.GONE);

        TextView titleText = new TextView(getContext());
        titleText.setVisibility(View.GONE);
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                OSUtil.dp2px(getContext(), 40));
        titleParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        titleText.setPadding(OSUtil.dp2px(getContext(), 16), 0, OSUtil.dp2px(getContext(), 50), 0);
        titleText.setGravity(Gravity.CENTER_VERTICAL);
        titleText.setLines(1);
        titleText.setEllipsize(TextUtils.TruncateAt.END);
        titleText.setTextSize(15);
        titleText.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.black_alpha30));
        titleText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        titleText.setText(list.get(position).getTitle());
        layout.addView(titleText, titleParams);
        if (isMap) {
            markImage.setVisibility(View.GONE);
            titleText.setVisibility(View.GONE);
        }
        return layout;
    }

    /**
     * 隐藏小圆点
     */
    public void hidePoint() {
        rg.setVisibility(INVISIBLE);
    }

    // 是否可无限滑动
    private boolean isInfinet = true;

    public void setInfinet(boolean isInfinet) {
        this.isInfinet = isInfinet;
    }

    public interface OnPositionSlideListener {
        void onSlide(int position);
    }

    private OnPositionSlideListener mOnPositionSlideListener;

    public void setmOnPositionSlideListener(OnPositionSlideListener mOnPositionSlideListener) {
        this.mOnPositionSlideListener = mOnPositionSlideListener;
    }

    /**
     * 设置当前显示页
     *
     * @param currentPage
     */
    public void setCurrentPage(int currentPage) {
        viewPager.setCurrentItem(currentPage);
    }
}