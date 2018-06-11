package com.travel.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.travel.lib.R;
import com.travel.lib.ui.TitleBarBaseActivity;

/**
 * 有一个返回按钮, 一个Fragment的界面
 * Created by ldkxingzhe on 2016/6/14.
 */
public class OneFragmentActivity extends TitleBarBaseActivity{
    @SuppressWarnings("unused")
    private static final String TAG = "OneFragmentActivity";

    public static final String TITLE = "title";
    public static final String CLASS = "class";

    public static final String BUNDLE = "bundle";

    private String mTitle;
    private String mClassName;

    private Fragment mCurrentFragment;

    /**
     * 打开这个新的界面
     * @param context
     * @param title     界面的标题
     * @param clazz     Fragment的类名, 利用反射打开这个方法
     */
    public static void startNewActivity(@NonNull Context context,
                                        @NonNull String title,
                                        @NonNull Class<? extends Fragment> clazz,
                                        Bundle bundle){
        Intent intent = new Intent(context, OneFragmentActivity.class);
        intent.putExtra(TITLE, title);
        intent.putExtra(CLASS, clazz.getName());
        intent.putExtra(BUNDLE, bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);
        Intent intent = getIntent();
        mTitle = intent.getStringExtra(TITLE);
        mClassName = intent.getStringExtra(CLASS);
        if(TextUtils.isEmpty(mTitle)){
            hideOriginTitleLayout();
        }else{
            setTitle(mTitle);
        }

        mCurrentFragment = null;
        try {
            mCurrentFragment = (Fragment) Class.forName(mClassName).newInstance();
            Bundle bundle = intent.getBundleExtra(BUNDLE);
            if(bundle != null){
                mCurrentFragment.setArguments(bundle);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fl_fragment_container, mCurrentFragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if(mCurrentFragment instanceof OneFragmentInterface){
            if(((OneFragmentInterface)mCurrentFragment).onBackPressed()){
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(mCurrentFragment instanceof  OneFragmentInterface){
                ((OneFragmentInterface)mCurrentFragment).onTouchDown();
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /** Fragment实现的接口 可选*/
    public interface OneFragmentInterface{
        /**
         * 返回键被点击
         * @return
         */
        boolean onBackPressed();
        void onTouchDown();
    }
}
