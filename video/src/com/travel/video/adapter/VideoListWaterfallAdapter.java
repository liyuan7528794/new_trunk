package com.travel.video.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2017/3/13.
 */
public class VideoListWaterfallAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NORMAL = 1;
    private Context context;
    private List<VideoInfoBean> list;
    private OnItemListener onItemListener;
    private View mHeaderView;
    private HashMap<Integer, Integer> heightCache = new HashMap<>();


    public VideoListWaterfallAdapter(Context context, List<VideoInfoBean> list) {
        this.context = context;
        this.list = list;
    }

    public interface OnItemListener {
        public void onItemClick(View view, int position);

        public void onItemLongClick(View view, int position);

        void updateHeaderView();
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    // StaggeredGridLayoutManager.LayoutParams为我们提供了一个setFullSpan方法来设置占领全部空间
    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if(lp != null
                && lp instanceof StaggeredGridLayoutManager.LayoutParams
                && holder.getLayoutPosition() == 0) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null)
            return TYPE_NORMAL;
        if(position == 0)
            return TYPE_HEADER;
        return TYPE_NORMAL;
    }

    private int getheightCache(int position){
        if(!heightCache.containsKey(position)){
            return -1;
        }
        return heightCache.get(position);
    }

    public void clearCache(){
        heightCache.clear();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderView != null && viewType == TYPE_HEADER)
            return new MyViewHolder(mHeaderView);

        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_video_list_waterfall, parent, false));
        return holder;
    }

    public int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return mHeaderView == null ? position : position - 1;
    }
    private void  resizeCover(ImageView img, int height){
        ViewGroup.LayoutParams params = img.getLayoutParams();
        params.width = (OSUtil.getScreenWidth() - OSUtil.dp2px(context, 20)) / 2;
        if(height == 0 && height == -1) {
            params.height = params.width;
        }else{
            params.height = height;
        }
        img.setLayoutParams(params);
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            if(onItemListener != null){
                onItemListener.updateHeaderView();
            }
            return;
        }
        int outMargin = OSUtil.dp2px(context, 5);
        final int pos = getRealPosition(holder);
        ((MyViewHolder) holder).itemView.setPadding(outMargin, outMargin, outMargin, outMargin);

        final VideoInfoBean bean = list.get(pos);
        if(getheightCache(position) == -1 || getheightCache(position) == 0){
//            Bitmap bitmap = ImageDisplayTools.getBitMap(bean.getVideoImg());
//            if(bitmap != null) {
//               float scale = ((float) bitmap.getHeight()) / bitmap.getWidth();
                float scale = new Random().nextFloat() + 0.5f;
                int w = (OSUtil.getScreenWidth() - OSUtil.dp2px(context, 20)) / 2;
                int h = (int) (w * scale);
                heightCache.put(pos, h);
//            }
        }
        resizeCover(((MyViewHolder) holder).iv_video_photo, getheightCache(pos));
        ImageDisplayTools.disPlayRoundDrawable(bean.getVideoImg(), ((MyViewHolder) holder).iv_video_photo, OSUtil.dp2px(context, 5));
        ((MyViewHolder) holder).iv_video_photo.setTag(bean.getVideoImg());
        if (!OSUtil.isDayTheme())
            ((MyViewHolder) holder).iv_video_photo.setColorFilter(TravelUtil.getColorFilter(context));
        ((MyViewHolder) holder).iv_video_photo.post(new Runnable() {
            @Override
            public void run() {
//                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) ((MyViewHolder) holder).iv_video_photo.getLayoutParams();
//                params.height = ((MyViewHolder) holder).iv_video_photo.getWidth();
//                ((MyViewHolder) holder).iv_video_photo.setLayoutParams(params);
            }

        });

        // 视频标题
        ((MyViewHolder) holder).tv_video_title.setText(bean.getVideoTitle());
        ((MyViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemListener.onItemClick(v, pos);
            }
        });
        ((MyViewHolder) holder).itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onItemListener.onItemLongClick(v, pos);
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mHeaderView == null ? list.size() : list.size() + 1;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView iv_video_photo;
        public TextView tv_video_title;

        public MyViewHolder(View view) {
            super(view);
            if(itemView == mHeaderView) {
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                itemView.setLayoutParams(params);
                return;
            }
            iv_video_photo = (ImageView) view.findViewById(R.id.iv_video_photo);
            tv_video_title = (TextView) view.findViewById(R.id.tv_video_title);
        }
    }

}