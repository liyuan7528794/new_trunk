package com.travel.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.convenientbanner.holder.Holder;
import com.ctsmedia.hltravel.R;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.utils.ImageDisplayTools;

/**
 * Created by Administrator on 2017/7/7.
 */

public class VideoHolderView implements Holder<VideoInfoBean>{
    private Context context;
    private ImageView iv_cover;
    private TextView tv_title;
    private View view;
    @Override
    public View createView(Context context) {
        this.context = context;
        view = View.inflate(context, R.layout.layout_video_banner_item, null);
        iv_cover = (ImageView) view.findViewById(R.id.iv_cover);
        tv_title = (TextView) view.findViewById(R.id.tv_title);
        return view;
    }

    @Override
    public void UpdateUI(Context context, int position, VideoInfoBean data) {
        if(view != null) {
            ImageDisplayTools.displayImage(data.getVideoImg(), iv_cover);
            tv_title.setText(data.getVideoTitle());
            view.invalidate();
        }
    }

    public Context getContext() {
        return context;
    }
}
