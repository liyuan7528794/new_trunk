package com.travel.localfile.pk.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.localfile.CameraFragment;
import com.travel.localfile.dao.LocalFile;

import java.util.List;

/**
 * Created by Administrator on 2017/2/13.
 * 证据列表中的图片、音频及视频显示的Adapter
 */

public class EvidenceMediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<LocalFile> list;
    private OnMediaItemClickListener listener;
    private RecyclerView recyclerView;

    public EvidenceMediaAdapter(Context context, List<LocalFile> list, RecyclerView recyclerView, OnMediaItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
        this.recyclerView = recyclerView;
    }

    public interface OnMediaItemClickListener {
        public void onClick(LocalFile localFile, ImageView view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case CameraFragment.TYPE_PHOTO: //0
                if (list.size() > 1) {
                    view = View.inflate(context, R.layout.adapter_evidence_photo2, null);
                    return new Phone2Hodler(view);
                } else {
                    view = View.inflate(context, R.layout.adapter_evidence_photo1, null);
                    return new Phone1Hodler(view);
                }

            case CameraFragment.TYPE_AUDIO: //1
                view = View.inflate(context, R.layout.adapter_evidence_audio, null);
                return new AudioHolder(view);

            case CameraFragment.TYPE_VIDEO: //2
                view = View.inflate(context, R.layout.adapter_evidence_video, null);
                return new VideoHolder(view);
            case CameraFragment.TYPE_LIVE:
                view = View.inflate(context, R.layout.adapter_evidence_video, null);
                return new VideoHolder(view);
            default:
                view = View.inflate(context, R.layout.adapter_evidence_photo2, null);
                return new Phone2Hodler(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        int type = list.get(position).getType();
        return type;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        int spanCount = getSpanCount(recyclerView);
        boolean isLastColum = isLastColum(recyclerView, position, spanCount, list.size());
        boolean isLastRaw = isLastRaw(recyclerView, position, spanCount, list.size());

        int colum = 0;
        int raw = 0;
        if (!isLastColum) {
            colum = OSUtil.dp2px(context, 1);
        }
        if (!isLastRaw) {
            raw = OSUtil.dp2px(context, 1);
        }
        if (spanCount == 1) {
            colum = 0;
        }
        holder.itemView.setPadding(0, 0, colum, raw);

        final LocalFile bean = list.get(position);
        String createTime = DateFormatUtil.getDate_M_D(bean.getCreateTimeFormat());
        switch (bean.getType()) {
            case CameraFragment.TYPE_PHOTO:
                if (list.size() > 1) {
                    Phone2Hodler p2hodler = (Phone2Hodler) holder;
                    ImageDisplayTools.displayImage(bean.getThumbnailPath(), p2hodler.cover);
                    if (!OSUtil.isDayTheme())
                        p2hodler.cover.setColorFilter(TravelUtil.getColorFilter(context));
//                    ImageDisplayTools.disPlayRoundDrawable(bean.getThumbnailPath(),p2hodler.cover,
//                            OSUtil.dp2px(context, 2), ImageDisplayTools.ROUND_BELOW);
                    p2hodler.cover.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onClick(bean, (ImageView) v);
                        }
                    });
                } else {
                    Phone1Hodler p1hodler = (Phone1Hodler) holder;
                    if (position == list.size() - 1) {
                        ImageDisplayTools.disPlayRoundDrawable(bean.getThumbnailPath(), p1hodler.cover,
                                OSUtil.dp2px(context, 2), ImageDisplayTools.ROUND_BELOW);
                    } else {
                        ImageDisplayTools.displayImage(bean.getThumbnailPath(), p1hodler.cover);
                    }
                    if (!OSUtil.isDayTheme())
                        p1hodler.cover.setColorFilter(TravelUtil.getColorFilter(context));
                    p1hodler.date.setText("".equals(createTime) ? "" : ("拍摄日期：" + createTime));
                    p1hodler.cover.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onClick(bean, (ImageView) v);
                        }
                    });
                }
                break;
            case CameraFragment.TYPE_AUDIO: // 录音
                final AudioHolder aHolder = (AudioHolder) holder;
                aHolder.date.setText( "".equals(createTime) ? "" : ("录音日期：" + createTime));
                aHolder.time.setText(bean.getDurationFormat());
                aHolder.start.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onClick(bean, (ImageView) v);
                    }
                });
                break;
            case CameraFragment.TYPE_LIVE:
                VideoHolder lHolder = (VideoHolder) holder;
                lHolder.start.setVisibility(View.GONE);
                lHolder.mark.setVisibility(View.VISIBLE);
                if (position == list.size() - 1) {
                    ImageDisplayTools.disPlayRoundDrawable(bean.getThumbnailPath(), lHolder.cover,
                            OSUtil.dp2px(context, 2), ImageDisplayTools.ROUND_BELOW);
                } else {
                    ImageDisplayTools.displayImage(bean.getThumbnailPath(), lHolder.cover);
                }
                if (!OSUtil.isDayTheme())
                    lHolder.cover.setColorFilter(TravelUtil.getColorFilter(context));

                lHolder.date.setText("".equals(createTime) ? "" : ("直播日期：" + createTime));
                lHolder.time.setText(bean.getDurationFormat());
                lHolder.cover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onClick(bean, (ImageView) v);
                    }
                });
                break;
            case CameraFragment.TYPE_VIDEO:
                VideoHolder vHolder = (VideoHolder) holder;
                vHolder.start.setVisibility(View.VISIBLE);
                vHolder.mark.setVisibility(View.GONE);
                if (position == list.size() - 1) {
                    ImageDisplayTools.disPlayRoundDrawable(bean.getThumbnailPath(), vHolder.cover,
                            OSUtil.dp2px(context, 2), ImageDisplayTools.ROUND_BELOW);
                } else {
                    ImageDisplayTools.displayImage(bean.getThumbnailPath(), vHolder.cover);
                }
                if (!OSUtil.isDayTheme())
                    vHolder.cover.setColorFilter(TravelUtil.getColorFilter(context));
                vHolder.date.setText("".equals(createTime) ? "" : ("录像日期：" + createTime));
                vHolder.time.setText(bean.getDurationFormat());
                vHolder.cover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onClick(bean, (ImageView) v);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Phone1Hodler extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView date;

        public Phone1Hodler(View itemView) {
            super(itemView);
            cover = (ImageView) itemView.findViewById(R.id.cover);
            date = (TextView) itemView.findViewById(R.id.date);
        }
    }

    class Phone2Hodler extends RecyclerView.ViewHolder {
        ImageView cover;

        public Phone2Hodler(View itemView) {
            super(itemView);
            cover = (ImageView) itemView.findViewById(R.id.cover);
        }
    }

    class VideoHolder extends RecyclerView.ViewHolder {
        ImageView cover, start, mark;
        TextView date, time;

        public VideoHolder(View itemView) {
            super(itemView);
            cover = (ImageView) itemView.findViewById(R.id.cover);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.time);
            mark = (ImageView) itemView.findViewById(R.id.mark);
            start = (ImageView) itemView.findViewById(R.id.startButton);
        }
    }

    class AudioHolder extends RecyclerView.ViewHolder {
        ImageView start;
        TextView date, time;

        public AudioHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.time);
            start = (ImageView) itemView.findViewById(R.id.startButton);
        }
    }

    private int getSpanCount(RecyclerView parent) {
        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof LinearLayoutManager) {
            spanCount = 1;
        }
        return spanCount;
    }

    private boolean isLastColum(RecyclerView parent, int pos, int spanCount,
                                int childCount) {
        if (spanCount == 1)
            return true;

        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int orientation = ((GridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                // 如果是最后一列，则不需要绘制右边
                if ((pos + 1) % spanCount == 0)
                    return true;
            } else {
                childCount = childCount - childCount % spanCount;
                // 如果是最后一列，则不需要绘制右边
                if (pos >= childCount)
                    return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                // 如果是最后一列，则不需要绘制右边
                if ((pos + 1) % spanCount == 0)
                    return true;
            } else {
                childCount = childCount - childCount % spanCount;
                // 如果是最后一列，则不需要绘制右边
                if (pos >= childCount)
                    return true;
            }
        }
        return false;
    }

    private boolean isLastRaw(RecyclerView parent, int pos, int spanCount,
                              int childCount) {
        if (spanCount == 1) {
            if (pos == (list.size() - 1))
                return true;
            else
                return false;
        }

        int orientation;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            childCount = childCount - childCount % spanCount;
            orientation = ((GridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                // 如果是最后一行，则不需要绘制底部
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount)
                    return true;
            } else {// StaggeredGridLayoutManager 横向滚动
                // 如果是最后一行，则不需要绘制底部
                if ((pos + 1) % spanCount == 0)
                    return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                // 如果是最后一行，则不需要绘制底部
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount)
                    return true;
            } else {// StaggeredGridLayoutManager 横向滚动
                // 如果是最后一行，则不需要绘制底部
                if ((pos + 1) % spanCount == 0)
                    return true;
            }
        }
        return false;
    }

}
