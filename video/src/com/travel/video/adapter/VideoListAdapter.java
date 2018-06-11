package com.travel.video.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.ShopConstant;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.video.sql.VideoVoteUtil;

import java.util.List;

/**
 * Created by Administrator on 2017/3/13.
 */
public class VideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NORMAL = 1;
    private Context context;
    private List<VideoInfoBean> list;
    private OnItemListener onItemListener;
    private VIDEO_TYPE showType;

    private View mHeaderView;

    public enum VIDEO_TYPE {
        MY,
        OTHER,
        SEARCH
    }

    public VideoListAdapter(Context context, List<VideoInfoBean> list, VIDEO_TYPE showType) {
        this.context = context;
        this.list = list;
        this.showType = showType;
    }

    public interface OnItemListener {
        public void onItemClick(View view, int position);

        public void onItemLongClick(View view, int position);
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

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return getItemViewType(position) == TYPE_HEADER
                            ? gridManager.getSpanCount() : 1;
                }
            });
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

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderView != null && viewType == TYPE_HEADER)
            return new MyViewHolder(mHeaderView);

        MyViewHolder holder = null;
        if (showType == VIDEO_TYPE.OTHER || showType == VIDEO_TYPE.SEARCH) {
            holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_video_item, parent, false));
        } else {
            holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_video_list_item, parent, false));
        }
        return holder;
    }

    public int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return mHeaderView == null ? position : position - 1;
    }

    private int num = 0;
    private boolean isLive = false;

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == TYPE_HEADER)
            return;
        final int pos = getRealPosition(holder);
        if (pos == 0) {
            num = 0;
            isLive = false;
        }
        int outMargin = OSUtil.dp2px(context, 15);
        int inMargin = OSUtil.dp2px(context, 5.5f);
        if (pos % 2 == 0) {
            if (pos == 0)
                ((MyViewHolder) holder).itemView.setPadding(0, mHeaderView == null ? outMargin : 0, inMargin, 0);
            else if (pos == list.size() - 2 || pos == list.size() - 1)
                ((MyViewHolder) holder).itemView.setPadding(0, 0, inMargin, outMargin);
            else
                ((MyViewHolder) holder).itemView.setPadding(0, 0, inMargin, 0);
        } else {
            if (pos == 1)
                ((MyViewHolder) holder).itemView.setPadding(inMargin, mHeaderView == null ? outMargin : 0, 0, 0);
            else if (pos == list.size() - 1)
                ((MyViewHolder) holder).itemView.setPadding(inMargin, 0, 0, outMargin);
            else
                ((MyViewHolder) holder).itemView.setPadding(inMargin, 0, 0, 0);
        }
        final VideoInfoBean bean = list.get(pos);

        // 视频图片
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) ((MyViewHolder) holder).iv_video_photo.getLayoutParams();
        params.height = ((MyViewHolder) holder).iv_video_photo.getWidth();
        ((MyViewHolder) holder).iv_video_photo.setLayoutParams(params);
        ImageDisplayTools.displayImageRound(bean.getVideoImg(), ((MyViewHolder) holder).iv_video_photo);
        if (!OSUtil.isDayTheme())
            ((MyViewHolder) holder).iv_video_photo.setColorFilter(TravelUtil.getColorFilter(context));
        ((MyViewHolder) holder).iv_video_photo.post(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) ((MyViewHolder) holder).iv_video_photo.getLayoutParams();
                params.height = ((MyViewHolder) holder).iv_video_photo.getWidth();
                ((MyViewHolder) holder).iv_video_photo.setLayoutParams(params);
            }

        });
        // 播放状态
        if (bean.getVideoStatus() == 1) { // 直播
            isLive = true;
            ((MyViewHolder) holder).iv_play_mark.setVisibility(View.VISIBLE);
            ((MyViewHolder) holder).iv_play_mark.setBackgroundResource(R.drawable.icon_mark_live);
            ((MyViewHolder) holder).tv_watch_num.setText(bean.getWatchCount() + "人在看");
        } else {  // 回放
            if (isLive)
                num = pos;
            isLive = false;
            ((MyViewHolder) holder).iv_play_mark.setVisibility(View.GONE);
            ((MyViewHolder) holder).tv_watch_num.setText(bean.getWatchCount() + "人看过");
        }

        if (VideoVoteUtil.getInstance().getDatas().containsKey(Integer.parseInt(bean.getVideoId()))) {
            // 我想去
            ((MyViewHolder) holder).iv_iswant_go.setImageResource(R.drawable.icon_want_go);
        } else {
            // 我不想去
            ((MyViewHolder) holder).iv_iswant_go.setImageResource(R.drawable.icon_unwant_go);
        }

        // 视频标题
        ((MyViewHolder) holder).tv_video_title.setText(bean.getVideoTitle());
        ((MyViewHolder) holder).tv_address.setText(bean.getShareAddress());
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

        if (showType == VIDEO_TYPE.OTHER || showType == VIDEO_TYPE.SEARCH) {
            ((MyViewHolder) holder).tv_rank.setVisibility(View.INVISIBLE);
            if(!isLive && pos < num + 3  && showType != VIDEO_TYPE.SEARCH){
                ((MyViewHolder) holder).tv_rank.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).tv_rank.setText("NO." + (pos - num + 1));
            }
            ((MyViewHolder) holder).tv_want_num.setText(bean.getVoteNum() + "");
            ((MyViewHolder) holder).iv_iswant_go.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!UserSharedPreference.isLogin()){
                        context.startActivity(new Intent(ShopConstant.LOG_IN_ACTION));
                    }
                    if (VideoVoteUtil.getInstance().getDatas().containsKey(bean.getVideoId())) {
                        return;
                    }
                    // 投票
                    VideoVoteUtil.getInstance().voteVideo(bean.getVideoId(), UserSharedPreference.getUserId(),
                            new VideoVoteUtil.VideoVoteListener() {
                                @Override
                                public void onSuccess(int error, boolean isSuccess) {
                                    if (isSuccess) {
                                        ((MyViewHolder) holder).tv_want_num.setText((Integer.parseInt(((MyViewHolder) holder).tv_want_num.getText().toString()) + 1) + "");
                                        ((MyViewHolder) holder).iv_iswant_go.setImageResource(R.drawable.icon_want_go);
                                    }else if(error == 1){
                                        ((MyViewHolder) holder).iv_iswant_go.setImageResource(R.drawable.icon_want_go);
                                    }
                                }
                            });
                }
            });

        } else {
            ((MyViewHolder) holder).tv_want_num.setText(bean.getVoteNum() + "赞");
            ((MyViewHolder) holder).ll_want.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!UserSharedPreference.isLogin()){
                        context.startActivity(new Intent(ShopConstant.LOG_IN_ACTION));
                    }
                    if (VideoVoteUtil.getInstance().getDatas().containsKey(bean.getVideoId())) {
                        return;
                    }

                    // 投票
                    VideoVoteUtil.getInstance().voteVideo(bean.getVideoId(), UserSharedPreference.getUserId(),
                            new VideoVoteUtil.VideoVoteListener() {
                                @Override
                                public void onSuccess(int error, boolean isSuccess) {
                                    if (isSuccess) {
                                        ((MyViewHolder) holder).tv_want_num.setText((Integer.parseInt(((MyViewHolder) holder).tv_want_num.getText().toString().replace("赞", "")) + 1) + "赞");
                                        ((MyViewHolder) holder).iv_iswant_go.setImageResource(R.drawable.icon_want_go);
                                    }else if(error == 1){
                                        ((MyViewHolder) holder).iv_iswant_go.setImageResource(R.drawable.icon_want_go);
                                    }
                                }
                            });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mHeaderView == null ? list.size() : list.size() + 1;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView iv_video_photo, iv_play_mark, iv_iswant_go;
        public TextView tv_video_title, tv_address, tv_watch_num, tv_want_num;

        public LinearLayout ll_want;

        public TextView tv_rank;

        public MyViewHolder(View view) {
            super(view);
            iv_video_photo = (ImageView) view.findViewById(R.id.iv_video_photo);
            iv_play_mark = (ImageView) view.findViewById(R.id.iv_play_mark);
            iv_iswant_go = (ImageView) view.findViewById(R.id.iv_iswant_go);
            tv_video_title = (TextView) view.findViewById(R.id.tv_video_title);
            tv_address = (TextView) view.findViewById(R.id.tv_address);
            tv_watch_num = (TextView) view.findViewById(R.id.tv_watch_num);
            tv_want_num = (TextView) view.findViewById(R.id.tv_want_num);

            if (showType == VIDEO_TYPE.OTHER || showType == VIDEO_TYPE.SEARCH) {
                tv_rank = (TextView) view.findViewById(R.id.tv_rank);
            } else {
                ll_want = (LinearLayout) view.findViewById(R.id.ll_want);
            }
        }
    }

}