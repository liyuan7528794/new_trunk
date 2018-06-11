package com.travel.video.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.communication.entity.UserData;
import com.travel.lib.utils.ImageDisplayTools;

import java.util.List;

/**
 * Created by Administrator on 2016/10/24.
 */

public class DialogWaitIntercutAdapter  extends BaseAdapter {
    private List<UserData> list;
    private Context context;
    private LayoutInflater inflater;
    private Listerner listerner;

    public interface Listerner{
        public void Click(boolean isReceive,int position);
    }
    public DialogWaitIntercutAdapter(Context context,List<UserData> list,DialogWaitIntercutAdapter.Listerner listener) {
        this.context = context;
        this.list = list;
        this.listerner = listener;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        DialogWaitIntercutAdapter.ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.dialog_wait_intercut_item, null);
            holder = new DialogWaitIntercutAdapter.ViewHolder();
            holder.headImg = (ImageView) convertView.findViewById(R.id.head_img);
            holder.nickname = (TextView) convertView.findViewById(R.id.name);
            holder.cancle = (ImageView) convertView.findViewById(R.id.cancleButton);
            holder.receive = (ImageView) convertView.findViewById(R.id.receiveButton);
            convertView.setTag(holder);
        } else {
            holder = (DialogWaitIntercutAdapter.ViewHolder) convertView.getTag();
        }
        UserData data = list.get(position);
        ImageDisplayTools.displayHeadImage(data.getImgUrl(),holder.headImg);
        holder.nickname.setText(data.getNickName());
        holder.cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listerner.Click(false,position);
            }
        });
        holder.receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listerner.Click(true,position);
            }
        });
        return convertView;
    }
    private class ViewHolder {
        public ImageView headImg,cancle,receive;
        public TextView nickname;
    }

}
