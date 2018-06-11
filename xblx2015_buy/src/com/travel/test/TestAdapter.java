package com.travel.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;

/**
 * Created by Administrator on 2017/4/20.
 */

public class TestAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflater;
    private Integer[] imgs = { R.drawable.gift_01, R.drawable.gift_02, R.drawable.gift_03,
            R.drawable.gift_02, R.drawable.gift_01, R.drawable.gift_03,R.drawable.gift_02};

    public TestAdapter(Context context){
        mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return imgs.length;
    }

    @Override
    public Object getItem(int i) {
        return imgs[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        if(i == -1) return null;
        ViewGroup layout;
        if(convertView == null) {
            layout = (ViewGroup) inflater.inflate(R.layout.test_item, null);
        } else {
            layout = (ViewGroup) convertView;
        }
        ((ImageView)layout.findViewById(R.id.iv)).setImageResource(imgs[i]);
        ((TextView)layout.findViewById(R.id.tv)).setText("我是——"+i);
        return layout;
    }
}
