package com.travel.usercenter;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;

import com.ctsmedia.hltravel.R;
import com.travel.fragment.MyFollowsFragment;
import com.travel.lib.ui.TitleBarBaseActivity;

/**
 * 关注人和关注商品的管理
 */
public class MyFollowActivity extends TitleBarBaseActivity {

    private MyFollowsFragment myFollowsFragment;
    // 判断网络
    public static boolean isNet;
    private int type = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().hasExtra("type"))
            type = getIntent().getIntExtra("type",1);
        setContentView(R.layout.framelayouts);

        if(type == 1)
            setTitle("我的关注");
        else if(type == 2) {
            setTitle("我的粉丝");
        }

        myFollowsFragment = new MyFollowsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        myFollowsFragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.frame_layouts, myFollowsFragment);
        transaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void init() {
        if(type == 1)
            setTitle("我的关注");
        else if(type == 2) {
            setTitle("我的粉丝");
        }
    }

    /**
     * 显示网络断开
     */
    protected void netNotifyShow() {
        super.netNotifyShow();
        isNet = false;
    }

    /**
     * 隐藏网络断开
     */
    protected void netNotifyHide() {
        super.netNotifyHide();
        isNet = true;
    }
}
