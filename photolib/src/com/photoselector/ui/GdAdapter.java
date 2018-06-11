package com.photoselector.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.photo.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.travel.bean.PhotoModel;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;

import java.util.List;

public class GdAdapter extends BaseAdapter {
    private Context mContext;
    private List<PhotoModel> mLists;
    private String tag = "";

    public GdAdapter(Context mContext, List<PhotoModel> mLists) {
        this.mLists = mLists;
        this.mContext = mContext;
    }

    public GdAdapter(Context mContext, List<PhotoModel> mLists, String tag) {
        this.mLists = mLists;
        this.mContext = mContext;
        this.tag = tag;
    }
    private GdListener listener;
    public void setListener(GdListener listener){
        this.listener = listener;
    }

    public interface GdListener{
        void onDelete(int position, PhotoModel photoModel);
    }

    @Override
    public int getCount() {
        return mLists == null ? 0 : mLists.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mLists == null ? null : mLists.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View view, ViewGroup group) {
        Holder holder;
        if (view == null) {
            holder = new Holder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.activity_slidingmenu_albums_item_item, null);
            holder.img = (ImageView) view.findViewById(R.id.img);
            holder.iv_delete = (ImageView) view.findViewById(R.id.iv_delete);
            holder.ll_add = (LinearLayout) view.findViewById(R.id.ll_add);
            holder.tv_checked_num = (TextView) view.findViewById(R.id.tv_checked_num);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        final PhotoModel info = mLists.get(position);
        ImageDisplayTools.initImageLoader(mContext);
        if (info != null) {
            // 提交评价中的图片选择
            if ("".equals(tag) || TextUtils.equals("talk", tag)) {
                if (info.getOriginalPath().equals("default")) {
                    holder.img.setVisibility(View.GONE);
                    holder.iv_delete.setVisibility(View.GONE);
                    holder.ll_add.setVisibility(View.VISIBLE);
                    ViewGroup.LayoutParams params = holder.ll_add.getLayoutParams();
                    if(TextUtils.equals("talk", tag))
                        params.width = (OSUtil.getScreenWidth() - OSUtil.dp2px(mContext, 15*4+40))/4;
                    else
                        params.width = (OSUtil.getScreenWidth() - OSUtil.dp2px(mContext, 15*4+78))/4;
                    params.height = params.width;
                    holder.ll_add.setLayoutParams(params);
//                    TravelUtil.setFLParamsWidthPart(holder.ll_add, 4, 60, 1, 1);
                    holder.tv_checked_num.setText((mLists.size()-1) + "/9");
                } else {
                    holder.ll_add.setVisibility(View.GONE);
                    holder.img.setVisibility(View.VISIBLE);
                    holder.iv_delete.setVisibility(View.VISIBLE);
                    ImageLoader.getInstance().displayImage("file://" + info.getOriginalPath(), holder.img);
                    ViewGroup.LayoutParams paramss = holder.img.getLayoutParams();
                    if(TextUtils.equals("talk", tag))
                        paramss.width = (OSUtil.getScreenWidth() - OSUtil.dp2px(mContext, 15*4+40))/4;
                    else
                        paramss.width = (OSUtil.getScreenWidth() - OSUtil.dp2px(mContext, 15*4+78))/4;
                    paramss.height = paramss.width;
                    holder.img.setLayoutParams(paramss);
//                    TravelUtil.setFLParamsWidthPart(holder.img, 4, 60, 1, 1);
                    holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onDelete(position, info);
                        }
                    });
                    if (!OSUtil.isDayTheme())
                        holder.img.setColorFilter(TravelUtil.getColorFilter(mContext));
                }
                // 商品详情页中评价的图�?
            } else {
                holder.iv_delete.setVisibility(View.GONE);
                ImageDisplayTools.displayImage(info.getOriginalPath(), holder.img);
                if (TextUtils.equals("orders", tag))
                    TravelUtil.setFLParamsWidthPart(holder.img, 4, 65, 1, 1);
                else
                    TravelUtil.setFLParamsWidthPart(holder.img, 3, 111, 1, 1);
                if (!OSUtil.isDayTheme())
                holder.img.setColorFilter(TravelUtil.getColorFilter(mContext));
            }
        }
        return view;
    }

    class Holder {
        ImageView img, iv_delete;
        LinearLayout ll_add;
        TextView tv_checked_num;
    }

}
