package com.travel.shop.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.travel.shop.R;

/**
 * Created by Administrator on 2017/2/8.
 * 个人主页个性签名的 Fragment
 */

public class PersonalHomeAboutFragment extends Fragment {
    private View rootView;
    private TextView tv_about;
    private String about = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.personal_home_about_layout, null);
        tv_about = (TextView) rootView.findViewById(R.id.about);
        tv_about.setText(about);
        return rootView;
    }

    public void setAbout(final String src) {
        about = src;
        if(tv_about!=null){
            tv_about.setText(about);
        }
    }
}