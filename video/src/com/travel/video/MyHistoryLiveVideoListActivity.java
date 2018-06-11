package com.travel.video;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.OSUtil;
import com.travel.localfile.UnPublishedVideoFragment;
import com.travel.video.fragment.VideoListFragment;

import java.util.ArrayList;

/**
 * 菜单：我的直播
 * <p>
 * 我的直播和本地视频上传页
 *
 * @author Administrator
 */
public class MyHistoryLiveVideoListActivity extends TitleBarBaseActivity implements View.OnClickListener {

    // 选项卡
    private TextView tv_doing, tv_done;
    private ArrayList<TextView> tvs = new ArrayList<TextView>();
    private ViewPager vp_manage;
    private static final Class[] fragmentClass = {VideoListFragment.class, UnPublishedVideoFragment.class};

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_my_video);
        init();

        vp_manage.setAdapter(new ManageAdapter(getSupportFragmentManager()));
        tv_doing.setOnClickListener(this);
        tv_done.setOnClickListener(this);
        leftButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        vp_manage.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                // 选项卡字的颜色
                for (TextView tview : tvs) {
                    tview.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray_9));
                }
                // 滑动完成的颜色
                if (OSUtil.isDayTheme())
                    tvs.get(position).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black_3));
                else
                    tvs.get(position).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray_C0));
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
        });

    }

    /**
     * 对控件初始化
     */
    private void init() {
        tv_doing = findView(R.id.tv_doing);
        tv_done = findView(R.id.tv_done);
        vp_manage = findView(R.id.vp_manage);
        setTitle("我的直播");
        tvs.add(tv_doing);
        tvs.add(tv_done);
    }

    /**
     * 订单管理选项卡的适配器
     */
    class ManageAdapter extends FragmentStatePagerAdapter {


        public ManageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0) {
            try {
                return (Fragment) fragmentClass[arg0].newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public int getCount() {
            return fragmentClass.length;
        }

        @Override
        public Object instantiateItem(ViewGroup arg0, int arg1) {
            return super.instantiateItem(arg0, arg1);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }
    }

    /**
     * 选项卡的点击事件
     */
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
        if (OSUtil.isDayTheme()) {
            tv_doing.setTextColor(ContextCompat.getColor(this, R.color.black_3));
            tv_done.setTextColor(ContextCompat.getColor(this, R.color.black_3));
        }else{
            tv_doing.setTextColor(ContextCompat.getColor(this, R.color.gray_C0));
            tv_done.setTextColor(ContextCompat.getColor(this, R.color.gray_C0));
        }
        if (v.getId() == R.id.tv_doing) {
            vp_manage.setCurrentItem(0);
        } else if (v.getId() == R.id.tv_done) {
            vp_manage.setCurrentItem(1);
        }

    }

    /**
     * 按下键盘的返回键 确保无论什么情况从订单列表返回都是返回到“我”的界面
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK)
            finish();
        return true;
    }

}
