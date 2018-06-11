package com.travel.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.ctsmedia.hltravel.R;
import com.travel.bean.NotifyBean;
import com.travel.lib.utils.ImageDisplayTools;

import java.util.List;

/**
 * 书的适配器
 * Created by Administrator on 2017/7/25.
 */

public class BookAdapter extends ArrayAdapter<NotifyBean> {

    int resource;
    Context context;

    public BookAdapter(Context context, int resource, List<NotifyBean> objects) {
        super(context, resource, objects);
        this.resource = resource;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(position >= getCount()) return convertView;
        NotifyBean notifyBean = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, resource, null);
            viewHolder = new ViewHolder();
            viewHolder.leftImage = (ImageView) convertView.findViewById(R.id.left_image);
            viewHolder.RightImage = (ImageView) convertView.findViewById(R.id.right_image);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();
        ImageDisplayTools.displayImage(notifyBean.getImgUrl(), viewHolder.leftImage);
        ImageDisplayTools.displayImage(notifyBean.getShareUrl(), viewHolder.RightImage);
        return convertView;
    }

    class ViewHolder {
        ImageView leftImage, RightImage;
    }
}
