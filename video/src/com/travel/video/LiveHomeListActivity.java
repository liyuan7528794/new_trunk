package com.travel.video;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.VideoConstant;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.map.HomeMapFragment;
import com.travel.video.fragment.VideoListFragment;
import com.travel.video.fragment.VideoListWaterfallFragment;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/13.
 */
public class LiveHomeListActivity extends TitleBarBaseActivity implements View.OnClickListener {
    private Context mContext;

    private static final String TAG = "LiveHomeListActivity";
    private LinearLayout tab_live, tab_about, tab_newyear;
    private TextView tv_live, tv_about, tv_newyear;
//    private View liveLine, aboutLine, newyearLine;
    private ArrayList<TextView> tvs = new ArrayList<TextView>();
    private VideoListFragment newYearFragment;
    private VideoListWaterfallFragment videoFragment;
    private HomeMapFragment aboutFragment;
    private int currentTag = -1;
    private Fragment currentFragment = null;
    // 判断网络
    private int tag = 0;

    private View rl_livehome_layout, v_livehome_line;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        mContext = this;
        OSUtil.enableStatusBar(this, true);
        setContentView(R.layout.live_home_navigation_fragment);
        newYearFragment = new VideoListFragment();
        Bundle bundles = new Bundle();
        bundles.putString("videoType", VideoListFragment.INTENT_NEWYEAR);
        newYearFragment.setArguments(bundles);

        videoFragment = new VideoListWaterfallFragment();
        Bundle bundle = new Bundle();
        bundle.putString("videoType", VideoListFragment.INTENT_RANK);
        videoFragment.setArguments(bundle);

        aboutFragment = new HomeMapFragment();
        currentFragment = newYearFragment;
        init();
        getActivitysVideos();
    }

    /**
     * 对控件初始化
     */
    private void init() {
        ll_title.setVisibility(View.GONE);
        titleLine.setVisibility(View.GONE);
        rl_livehome_layout = findView(R.id.rl_livehome_layout);
        v_livehome_line = findView(R.id.v_livehome_line);
        try {
            Method method = View.class.getMethod("setTranslationZ", float.class);
            method.invoke(rl_livehome_layout, 10f);
        } catch (Exception e) {
            v_livehome_line.setVisibility(View.VISIBLE);
            MLog.e(TAG, e.getMessage(), e);
        }

        hideOriginTitleLayout();
        tab_newyear = findView(R.id.tab_newyear);
        tab_live = findView(R.id.tab_live);
        tab_about = findView(R.id.tab_about);
        tv_newyear = findView(R.id.tv_newyear);
        tv_live = findView(R.id.tv_live);
        tv_about = findView(R.id.tv_about);
//        newyearLine = findView(R.id.newyearLine);
//        liveLine = findView(R.id.liveLine);
//        aboutLine = findView(R.id.aboutLine);
        tvs.add(tv_newyear);
        tvs.add(tv_live);
        tvs.add(tv_about);

        tab_newyear.setOnClickListener(this);
        tab_live.setOnClickListener(this);
        tab_about.setOnClickListener(this);
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

        if (v.getId() == R.id.tab_newyear) {
            addFragment(0);
        } else if (v.getId() == R.id.tab_live) {
            addFragment(1);
        } else if (v.getId() == R.id.tab_about) {
            addFragment(2);
        }

    }

    private void addFragment(int index) {
        if (currentTag != index) {
            tv_newyear.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
            tv_live.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
            tv_about.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
//            newyearLine.setVisibility(View.INVISIBLE);
//            liveLine.setVisibility(View.INVISIBLE);
//            aboutLine.setVisibility(View.INVISIBLE);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (index == 0) {
                if (!newYearFragment.isAdded()) { // 先判断是否被add过
                    transaction.hide(currentFragment).add(R.id.fl_fragment_container, newYearFragment).hide(newYearFragment).show(newYearFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
                } else {
                    transaction.hide(currentFragment).show(newYearFragment).commit(); // 隐藏当前的fragment，显示下一个
                }
                tv_newyear.setTextColor(ContextCompat.getColor(mContext, R.color.red_F199B5));
//                newyearLine.setVisibility(View.VISIBLE);
                currentFragment = newYearFragment;
            } else if (index == 1) {
                if (!videoFragment.isAdded()) { // 先判断是否被add过
                    transaction.hide(currentFragment).add(R.id.fl_fragment_container, videoFragment).hide(videoFragment).show(videoFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
                } else {
                    transaction.hide(currentFragment).show(videoFragment).commit(); // 隐藏当前的fragment，显示下一个
                }
                tv_live.setTextColor(ContextCompat.getColor(mContext, OSUtil.isDayTheme() ? R.color.black_3 : R.color.white));
//                liveLine.setVisibility(View.VISIBLE);
                currentFragment = videoFragment;
            } else if (index == 2) {
                if (!aboutFragment.isAdded()) { // 先判断是否被add过
                    transaction.hide(currentFragment).add(R.id.fl_fragment_container, aboutFragment).hide(aboutFragment).show(aboutFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
                } else {
                    transaction.hide(currentFragment).show(aboutFragment).commit(); // 隐藏当前的fragment，显示下一个
                }
                tv_about.setTextColor(ContextCompat.getColor(mContext, OSUtil.isDayTheme() ? R.color.black_3 : R.color.white));
//                aboutLine.setVisibility(View.VISIBLE);
                currentFragment = aboutFragment;
            }
        }
        currentTag = index;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void getActivitysVideos() {
        Map<String, Object> paramap = new HashMap<String, Object>();
        NetWorkUtil.postForm(LiveHomeListActivity.this, VideoConstant.GET_ACTIVITY_INFO, new MResponseListener(this) {

            @Override
            protected void onDataFine(JSONArray data) {
                if(data!=null && data.length() > 0){
                    try {
                        int id = JsonUtil.getJsonInt(data.getJSONObject(0), "id");
                        String title = JsonUtil.getJson(data.getJSONObject(0), "title");
                        tvs.get(0).setText(title);

                        Bundle bundles = new Bundle();
                        bundles.putString("videoType", VideoListFragment.INTENT_NEWYEAR);
                        bundles.putInt("activityId", id);
                        newYearFragment.setArguments(bundles);
                        addFragment(0);
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    tab_newyear.setVisibility(View.GONE);
                    addFragment(1);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                tab_newyear.setVisibility(View.GONE);
                addFragment(1);
            }

        }, paramap);
    }

}
