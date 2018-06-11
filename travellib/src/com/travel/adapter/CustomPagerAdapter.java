package com.travel.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bigkoo.convenientbanner.holder.Holder;
import com.travel.bean.NotifyBean;
import com.travel.lib.utils.ImageDisplayTools;

import java.util.ArrayList;


/**
 * viewpager的适配器
 *
 * @author WYP
 */
public class CustomPagerAdapter extends FragmentStatePagerAdapter {

    ArrayList<Fragment> viewList;

    public CustomPagerAdapter(FragmentManager fm, ArrayList<Fragment> viewList) {
        super(fm);
        this.viewList = viewList;
    }

    @Override
    public Fragment getItem(int arg0) {
        return viewList.get(arg0);
    }

    @Override
    public int getCount() {
        return viewList.size();
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
