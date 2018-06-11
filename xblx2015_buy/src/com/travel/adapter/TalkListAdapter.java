package com.travel.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.activity.OneFragmentActivity;
import com.travel.bean.VideoInfoBean;
import com.travel.entity.TalkBean;
import com.travel.layout.CustormTouchRecyclerView;
import com.travel.layout.VideoViewFragment;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.utils.TalkPraiseUtil;
import com.travel.video.adapter.PictureAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 说说列表适配器
 * Created by wyp on 2018/5/11.
 */

public class TalkListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NORMAL = 1;
    private Context mContext;
    private View mHeaderView;
    private List<TalkBean> listData;
    private ClickListener clickListener;

    public interface ClickListener {
        void onClick(int position);

        void onClickComments(int position);

        /**
         * @param praiseType 点赞状态
         * @param index      所操作列表数据的位置，用于修改列表数据
         * @param pos        所操作的item的位置
         */
        void onClickPraise(int praiseType, int index, int pos);
    }

    public TalkListAdapter(Context context, List<TalkBean> listData) {
        this.mContext = context;
        this.listData = listData;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
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
    }

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null)
            return TYPE_NORMAL;
        if (position == 0)
            return TYPE_HEADER;
        return TYPE_NORMAL;
    }

    public int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return mHeaderView == null ? position : position - 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderView != null && viewType == TYPE_HEADER)
            return new MyHolder(mHeaderView);

        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_talk_list, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int pos) {
        if (getItemViewType(pos) == TYPE_HEADER)
            return;
        final int position = getRealPosition(holder);

        final MyHolder myHolder = (MyHolder) holder;
        final TalkBean bean = listData.get(position);
        if (bean.getUser() != null) {
            ImageDisplayTools.displayHeadImage(bean.getUser().getImgUrl(), myHolder.iv_head);
            myHolder.iv_sex.setImageResource(bean.getUser().getSex() == 1 ? R.drawable.icon_sex_man : R.drawable.icon_sex_woman);
            myHolder.tv_name.setText(bean.getUser().getNickName());
            myHolder.tv_place.setText(bean.getUser().getPlace());
        }
        myHolder.tv_time.setText(DateFormatUtil.getPastTime(bean.getTime()));
        myHolder.tv_zan_num.setText(bean.getPraiseNum() + "");
        myHolder.tv_comment_num.setText(bean.getCommentNum() + "");

        myHolder.recyclerView.setVisibility(View.GONE);
        myHolder.layout_video_picture.setVisibility(View.GONE);
        myHolder.tv_content_all.setVisibility(View.GONE);
        myHolder.tv_content.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(bean.getVideoUrl())) {
            myHolder.layout_video_picture.setVisibility(View.VISIBLE);
            ImageDisplayTools.disPlayRoundDrawable(bean.getImgUrl(), myHolder.iv_video_picture, 8);
            TravelUtil.setFLParamsWidthPart(myHolder.iv_video_picture, 1, 90, 16, 9);
        } else if (!TextUtils.isEmpty(bean.getImgUrl())) {// 图片列表
            myHolder.recyclerView.setVisibility(View.VISIBLE);
            myHolder.tv_content.setVisibility(View.VISIBLE);
            myHolder.tv_content.setText(bean.getContent());
            myHolder.tv_content.setMaxLines(5);
            myHolder.tv_content.post(new Runnable() {
                @Override
                public void run() {
                    Layout l = myHolder.tv_content.getLayout();
                    if (l != null) {
                        int lines = l.getLineCount();
                        if (lines > 0) {
                            if (l.getEllipsisCount(lines - 1) > 0) {
                                myHolder.tv_content_all.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            });
            myHolder.tv_content_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myHolder.tv_content.setMaxLines(Integer.MAX_VALUE);
                    myHolder.tv_content_all.setVisibility(View.GONE);
                }
            });
            List<String> imgs = new ArrayList<>();
            if (bean.getImgUrl().contains(",")) {
                String[] img = bean.getImgUrl().split(",");
                imgs.addAll(Arrays.asList(img));
            } else {
                imgs.add(bean.getImgUrl());
            }
            myHolder.recyclerView.setVisibility(View.VISIBLE);
//            myHolder.recyclerView.addItemDecoration(
//                    new DividerItemDecoration(mContext,
//                            DividerItemDecoration.HORIZONTAL_LIST,
//                            OSUtil.dp2px(mContext, 10),
//                            android.R.color.transparent));
            GridLayoutManager manager = new GridLayoutManager(mContext, 3);
            myHolder.recyclerView.setLayoutManager(manager);

            PictureAdapter adapter = new PictureAdapter(mContext, imgs);
            myHolder.recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
//            myHolder.recyclerView.setFocusable(false);
//            myHolder.recyclerView.setClickable(false);
        } else if (!TextUtils.isEmpty(bean.getContent())) {
            myHolder.tv_content.setVisibility(View.VISIBLE);
            myHolder.tv_content.setText(bean.getContent());
            myHolder.tv_content.setMaxLines(5);
            myHolder.tv_content.post(new Runnable() {
                @Override
                public void run() {
                    Layout l = myHolder.tv_content.getLayout();
                    if (l != null) {
                        int lines = l.getLineCount();
                        if (lines > 0) {
                            if (l.getEllipsisCount(lines - 1) > 0) {
                                myHolder.tv_content_all.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            });
        }
        /*if (bean.getPraiseType() == 0) {
            myHolder.iv_zan.setImageResource(R.drawable.icon_want_go);
            myHolder.iv_zan.setTag("zan");
        } else {
            myHolder.iv_zan.setImageResource(R.drawable.icon_unwant_go);
            myHolder.iv_zan.setTag("unzan");
        }*/

        if (TalkPraiseUtil.getInstance().getDatas().containsKey(Integer.parseInt(bean.getId()))) {
            myHolder.iv_zan.setImageResource(R.drawable.find_ico_fabulous_pre);
            myHolder.iv_zan.setTag("zan");
        } else {
            myHolder.iv_zan.setImageResource(R.drawable.find_ico_fabulous_nor);
            myHolder.iv_zan.setTag("unzan");
        }

        myHolder.ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(position);
            }
        });

        myHolder.ll_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClickComments(position);
            }
        });

        myHolder.ll_zan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UserSharedPreference.isLogin()) {
                    OSUtil.intentLogin(mContext);
                }
                String praiseType = "0";
                if (TextUtils.equals((CharSequence) myHolder.iv_zan.getTag(), "zan")) {
                    praiseType = "1";
                }
                // 投票
                TalkPraiseUtil.getInstance().voteVideo(mContext, bean.getId(), praiseType,
                        new TalkPraiseUtil.VideoVoteListener() {
                            @Override
                            public void onSuccess(int error, boolean isSuccess) {
                                if (TextUtils.equals((CharSequence) myHolder.iv_zan.getTag(), "zan")) {
                                    if (isSuccess) {
                                        clickListener.onClickPraise(1, position, pos);
                                    } else if (error == 1) {
                                        Toast.makeText(mContext, "取消失败", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (isSuccess) {
                                        clickListener.onClickPraise(0, position, pos);
                                    } else if (error == 1) {
                                        Toast.makeText(mContext, "点赞失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
        });
        myHolder.layout_video_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("path", listData.get(position).getVideoUrl());
                TravelUtil.goPlay(mContext, bundle);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mHeaderView == null ? listData.size() : listData.size() + 1;
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_time, tv_content, tv_content_all, tv_place, tv_zan_num, tv_comment_num;
        ImageView iv_head, iv_sex, iv_zan;
        CustormTouchRecyclerView recyclerView;
        LinearLayout ll, ll_zan, ll_comment, ll_share;
        private View layout_video_picture;
        private ImageView iv_video_picture;

        public MyHolder(View itemView) {
            super(itemView);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            tv_content = (TextView) itemView.findViewById(R.id.tv_content);
            tv_content_all = (TextView) itemView.findViewById(R.id.tv_content_all);
            tv_place = (TextView) itemView.findViewById(R.id.tv_place);
            tv_zan_num = (TextView) itemView.findViewById(R.id.tv_zan_num);
            tv_comment_num = (TextView) itemView.findViewById(R.id.tv_comment_num);
            iv_zan = (ImageView) itemView.findViewById(R.id.iv_zan);
            iv_head = (ImageView) itemView.findViewById(R.id.iv_head);
            iv_sex = (ImageView) itemView.findViewById(R.id.iv_sex);
            recyclerView = (CustormTouchRecyclerView) itemView.findViewById(R.id.rv_picture);
            ll = (LinearLayout) itemView.findViewById(R.id.ll);
            ll_zan = (LinearLayout) itemView.findViewById(R.id.ll_zan);
            ll_comment = (LinearLayout) itemView.findViewById(R.id.ll_comment);
            ll_share = (LinearLayout) itemView.findViewById(R.id.ll_share);
            layout_video_picture = itemView.findViewById(R.id.layout_video_picture);
            iv_video_picture = (ImageView) itemView.findViewById(R.id.iv_video_picture);
        }
    }
}
