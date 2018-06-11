package com.photoselector.ui;

/**
 * @author Aizaz AZ
 */

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;

import com.example.photo.R;
import com.travel.bean.PhotoModel;
import com.travel.lib.ui.TitleBarBaseActivity;

import java.util.List;

public class BasePhotoPreviewActivity extends TitleBarBaseActivity implements OnPageChangeListener {

    private ViewPager mViewPager;
    protected List<PhotoModel> photos;
    protected int current;
    protected String tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photopreview);
        mViewPager = (ViewPager) findViewById(R.id.vp_base_app);
        rightButton.setVisibility(View.VISIBLE);
        mViewPager.setOnPageChangeListener(this);

    }

    /**
     * 绑定数据，更新界面
     */
    protected void bindData() {
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(current);
    }

    private PagerAdapter mPagerAdapter = new PagerAdapter() {

        @Override
        public int getCount() {
            if (photos == null) {
                return 0;
            } else {
                return photos.size();
            }
        }

        @Override
        public View instantiateItem(final ViewGroup container, final int position) {
            PhotoPreview photoPreview = new PhotoPreview(BasePhotoPreviewActivity.this);
            container.addView(photoPreview);
            photoPreview.loadImage("http".equals(tag) ? photos.get(position).getOriginalPathBig()
                    : "file://" + photos.get(position).getOriginalPath());
            return photoPreview;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    };

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {
        current = arg0;
        updatePercent();
    }

    protected void updatePercent() {
        rightButton.setText((current + 1) + "/" + photos.size());
    }
}
