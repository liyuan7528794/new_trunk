package com.travel.video.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.photoselector.ui.PhotoPreviewActivity;
import com.travel.bean.PhotoModel;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.widget.SquareImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/2.
 */

public class PictureAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NORMAL = 1;
    private Context mContext;
    private View mHeaderView;
    private List<String> listData;
    private ClickListener clickListener;

    private ArrayList<PhotoModel> photoModels;

    public interface ClickListener{
        void onClick(int position);
    }

    public PictureAdapter(Context context, List<String> listData) {
        this.mContext = context;
        this.listData = listData;
        photoModels = new ArrayList<>();
        for(String photo : listData){
            PhotoModel photoModel = new PhotoModel();
            photoModel.setOriginalPathBig(photo + "?x-oss-process=image/resize,p_100");
            photoModels.add(photoModel);
        }
    }

    public void setClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FrameLayout frameLayout = new FrameLayout(mContext);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(OSUtil.dp2px(mContext, 5), OSUtil.dp2px(mContext, 5), OSUtil.dp2px(mContext, 5), OSUtil.dp2px(mContext, 5));
        SquareImageView imageView = new SquareImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        frameLayout.addView(imageView, params);
        return new MyHolder(frameLayout);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final MyHolder myHolder = (MyHolder) holder;
        String url = listData.get(position);
        ImageDisplayTools.displayImage(url, myHolder.iv_head);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 预览图片
                Bundle bundle = new Bundle();
                bundle.putSerializable("photos", photoModels);
                bundle.putInt("position", holder.getAdapterPosition());
                bundle.putString("tag", "http");
                TravelUtil.launchActivity(mContext, PhotoPreviewActivity.class, bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView iv_head;
        public MyHolder(View itemView) {
            super(itemView);
            iv_head = (ImageView) ((FrameLayout) itemView).getChildAt(0);
        }
    }
}
