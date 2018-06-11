package com.travel.video.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;

import java.util.List;

public class VideoAdapter extends BaseAdapter {
    private Context context;
    private List<VideoInfoBean> list;
    private LayoutInflater inflater;

    public VideoAdapter(Context context, List<VideoInfoBean> list) {
        this.context = context;
        this.list = list;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        VideoInfoBean bean = list.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_video_item, null);
            holder = new ViewHolder();
            holder.coverImage = (ImageView) convertView.findViewById(R.id.liveImage);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.coverImage.getLayoutParams();
            params.width = (OSUtil.getScreenWidth() - OSUtil.dp2px(context, 15)) / 2;
            params.height = params.width;
            holder.coverImage.setLayoutParams(params);
            holder.liveTitle = (TextView) convertView.findViewById(R.id.liveTitleName);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.watchNum = (TextView) convertView.findViewById(R.id.watchNum);
            holder.commentNum = (TextView) convertView.findViewById(R.id.commentNum);
            holder.mark = (ImageView) convertView.findViewById(R.id.mark);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String imageUrl = bean.getVideoImg();
        if ("".equals(imageUrl)) {
            imageUrl = Constants.DefaultHeadImg;
        }
        ImageDisplayTools.displayImage(imageUrl, holder.coverImage);
        holder.liveTitle.setText(bean.getVideoTitle());
        holder.date.setText(bean.getCreateTime());

        if (1 == bean.getVideoStatus()) {    //直播
            holder.mark.setImageResource(R.drawable.live_mark);
            convertView.findViewById(R.id.image2).setVisibility(View.GONE);
            convertView.findViewById(R.id.image3).setVisibility(View.GONE);
            holder.watchNum.setVisibility(View.GONE);
            holder.commentNum.setVisibility(View.GONE);
        } else if (2 == bean.getVideoStatus()) {
            holder.watchNum.setText(bean.getWatchCount() + "");
            holder.commentNum.setText(bean.getCommentCount() + "");
            holder.mark.setImageResource(R.drawable.playback_mark);
        } else {
            holder.mark.setVisibility(View.GONE);
        }

        return convertView;
    }

    class ViewHolder {
        ImageView coverImage, mark;
        TextView liveTitle;
        TextView date, watchNum, commentNum;
    }
}