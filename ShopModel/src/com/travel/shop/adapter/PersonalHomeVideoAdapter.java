package com.travel.shop.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.travel.bean.VideoInfoBean;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.R;
import com.travel.shop.tools.ShopTool;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/1/19.
 */
public class PersonalHomeVideoAdapter extends MyBaseAdapter<VideoInfoBean> {

    private Context mContext;
    private String tag;

    public PersonalHomeVideoAdapter(Context context, ArrayList<VideoInfoBean> listData, String tag) {
        super(listData);
        this.mContext = context;
        this.tag = tag;
    }

    @Override
    public int getCount() {
        if ("goodsInfo".equals(tag)) {
            return 2;
        }
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyHolder mHolder;
        if (convertView == null) {
            mHolder = new MyHolder();
            convertView = View.inflate(mContext, R.layout.adapter_personal_home_video, null);
            mHolder.iv_video_photo = (ImageView) convertView.findViewById(R.id.iv_video_photo);
            mHolder.iv_play_mark = (ImageView) convertView.findViewById(R.id.iv_play_mark);
            mHolder.tv_video_title = (TextView) convertView.findViewById(R.id.tv_video_title);
            mHolder.tv_address = (TextView) convertView.findViewById(R.id.tv_address);
            mHolder.tv_watch_num = (TextView) convertView.findViewById(R.id.tv_watch_num);
            convertView.setTag(mHolder);
        } else {
            mHolder = (MyHolder) convertView.getTag();
        }

        VideoInfoBean mVideoInfoBean = (VideoInfoBean) getItem(position);
        ImageDisplayTools.initImageLoader(mContext);
        // 视频图片
        ImageDisplayTools.displayImageRoundCity(mVideoInfoBean.getVideoImg(), mHolder.iv_video_photo);
        ShopTool.setLL1w1hVideo(mHolder.iv_video_photo);
        if (!OSUtil.isDayTheme())
            mHolder.iv_video_photo.setColorFilter(TravelUtil.getColorFilter(mContext));
        // 播放状态
        if (mVideoInfoBean.getVideoStatus() == 1) { // 直播
            mHolder.iv_play_mark.setVisibility(View.VISIBLE);
            mHolder.iv_play_mark.setBackgroundResource(R.drawable.icon_mark_live);
            mHolder.tv_watch_num.setText(mVideoInfoBean.getWatchCount() + "人在看");
        }else {  // 回放
            mHolder.iv_play_mark.setVisibility(View.GONE);
            mHolder.tv_watch_num.setText(mVideoInfoBean.getWatchCount() + "人看过");
        }
        // 视频标题
        mHolder.tv_video_title.setText(mVideoInfoBean.getVideoTitle());
        mHolder.tv_address.setText(mVideoInfoBean.getShareAddress());
        return convertView;
    }

    class MyHolder {
        public ImageView iv_video_photo, iv_play_mark;
        public TextView tv_video_title , tv_address, tv_watch_num;
    }
}
