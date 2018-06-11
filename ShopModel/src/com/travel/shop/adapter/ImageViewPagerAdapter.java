package com.travel.shop.adapter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.travel.bean.ReasonBean;
import com.travel.lib.TravelApp;
import com.travel.lib.utils.FastBlurUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片轮滚的Adpter， 简单实现ImageView
 */
public class ImageViewPagerAdapter extends PagerAdapter {
    private static final String TAG = "ImageViewPagerAdapter";

    private List<FrameLayout> imageViewList = new ArrayList<FrameLayout>();
    private List<ReasonBean> mUrlList;
    private ImageView imageView;
    private String pictureUrl;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            imageView.setImageBitmap((Bitmap) msg.obj);
        }
    };

    public interface ViewPagerOnItemClickListener {
        /**
         * 子view点击事件， 模拟仿真onItemClick
         */
        void onItemClick(ViewPager viewPager, View view, int position);
    }

    public interface ViewPagerOnItemLongClickListener {
        boolean onItemLongClick(ViewPager viewPager, View view, int position);
    }

    private ViewPagerOnItemClickListener mListener;
    private ViewPagerOnItemLongClickListener mLongClickListener;

    /**
     * 设置点击事件
     *
     * @param listener
     */
    public void setOnItemClickListener(ViewPagerOnItemClickListener listener) {
        mListener = listener;
    }

    /**
     * 设置长按点击事件
     *
     * @param listener
     */
    public void setOnItemLongClickListener(ViewPagerOnItemLongClickListener listener) {
        mLongClickListener = listener;
    }

    /**
     * 构造函数， 引用传递
     *
     * @param urlList
     */
    public ImageViewPagerAdapter(List<ReasonBean> urlList) {
        mUrlList = urlList;
    }

    @Override
    public void notifyDataSetChanged() {
        for (int i = 0, length = Math.min(imageViewList.size(), getCount()); i < length; i++) {
            FrameLayout frameLayoutView = imageViewList.get(i);
            pictureUrl = mUrlList.get(i).getReason();
            if (frameLayoutView != null) {
                imageView = new ImageView(TravelApp.appContext);
                imageView.setScaleType(ScaleType.CENTER_CROP);
                frameLayoutView.addView(imageView);
                setBlurPicture();
                addLiveIcon(i, frameLayoutView);
            }
        }
        MLog.v(TAG, "imageViewPager notifyDataSetChanged");
        super.notifyDataSetChanged();
    }

    /**
     * 直播的图标的添加
     *
     * @param i
     * @param frameLayoutView
     */
    private void addLiveIcon(int i, FrameLayout frameLayoutView) {
        if (i == 0 && mUrlList.get(i).getFlag() != -1) {
            ImageView imgPlayButton = new ImageView(TravelApp.appContext);
            imgPlayButton.setImageResource(R.drawable.icon_video_play);
            FrameLayout.LayoutParams lpButton = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            lpButton.gravity = Gravity.CENTER;
            imgPlayButton.setLayoutParams(lpButton);
            ImageView imgPlayIcon = new ImageView(TravelApp.appContext);
            imgPlayIcon.setImageResource(R.drawable.live_mark);
            frameLayoutView.addView(imgPlayButton);
            FrameLayout.LayoutParams lpIcon = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            lpIcon.gravity = Gravity.RIGHT;
            lpIcon.rightMargin = OSUtil.dp2px(TravelApp.appContext, 5);
            lpIcon.topMargin = OSUtil.dp2px(TravelApp.appContext, 5);
            imgPlayIcon.setLayoutParams(lpIcon);
            frameLayoutView.addView(imgPlayIcon);
        }
    }

    @Override
    public Object instantiateItem(final View container, final int position) {
        if (imageViewList.size() <= position) {
            imageViewList.add(position, null);
        }

        FrameLayout layout = imageViewList.get(position);
        if (layout == null) {
            layout = new FrameLayout(container.getContext());
            imageView = new ImageView(container.getContext());
            imageView.setScaleType(ScaleType.CENTER_CROP);
            imageView.setTag(position);
            imageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {
                    // 禁止双击
                    v.setEnabled(false);
                    v.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            v.setEnabled(true);
                        }
                    }, 500);
                    if (mListener != null) {
                        mListener.onItemClick((ViewPager) container, imageViewList.get(position), position);
                    }
                }
            });
            imageView.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    if (mLongClickListener != null) {
                        return mLongClickListener.onItemLongClick((ViewPager) container, imageViewList.get(position),
                                position);
                    }
                    return false;
                }
            });
            layout.addView(imageView);
            pictureUrl = mUrlList.get(position).getReason();
            setBlurPicture();
        }

        addLiveIcon(position, layout);
        ((ViewGroup) container).addView(layout);
        imageViewList.set(position, layout);
        return layout;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        ((ViewGroup) container).removeView(imageViewList.get(position));
    }

    @Override
    public int getCount() {
        if (mUrlList == null) {
            return 0;
        } else {
            return mUrlList.size();
        }
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }


    private void setBlurPicture(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int scaleRatio = 5;
                Bitmap blurBitmap2 = FastBlurUtil.GetUrlBitmap(pictureUrl, scaleRatio);
                Message message = handler.obtainMessage();
                message.obj = blurBitmap2;
                handler.sendMessage(message);
            }
        }).start();
    }
}
