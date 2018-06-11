package com.travel.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * 继承自OneFragment, singleTop
 * Created by ldkxingzhe on 2016/7/27.
 */
public class OneFragmentSingleTopActivity extends OneFragmentActivity{

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
        Intent intent = new Intent(context, OneFragmentSingleTopActivity.class);
        intent.putExtra(TITLE, title);
        intent.putExtra(CLASS, clazz.getName());
        intent.putExtra(BUNDLE, bundle);
        context.startActivity(intent);
    }
}
