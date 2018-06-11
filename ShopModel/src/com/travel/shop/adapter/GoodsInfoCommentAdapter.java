package com.travel.shop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.bean.EvaluateInfoBean;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 商品详情页的评论的适配器
 * Created by wyp on 2017/1/13.
 */

public class GoodsInfoCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<EvaluateInfoBean> listData;
    private OnLikeClickListener mOnLikeClickListener;
    private boolean isVoteComments = false;

    public void setHashMap(HashMap<Integer, Integer> hashMap) {
        this.hashMap.clear();
        this.hashMap = hashMap;
    }

    public void setIsVoteComments(boolean isVoteComments) {
        this.isVoteComments = isVoteComments;
    }

    private HashMap<Integer, Integer> hashMap;

    public GoodsInfoCommentAdapter(ArrayList<EvaluateInfoBean> listData, Context context) {
        this.listData = listData;
        mContext = context;
        hashMap = new HashMap<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.adapter_goods_comment_item, null);
        return new GoodsInfoCommentHodler(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GoodsInfoCommentHodler) {
            EvaluateInfoBean evaluateInfoBean;
            if (position == listData.size() - 1)
                ((GoodsInfoCommentHodler) holder).v_line_comment.setVisibility(View.GONE);
            else
                ((GoodsInfoCommentHodler) holder).v_line_comment.setVisibility(View.VISIBLE);
            evaluateInfoBean = listData.get(position);
            // 头像
            ImageDisplayTools.displayHeadImage(evaluateInfoBean.getEvaluateUserPhoto(), ((GoodsInfoCommentHodler) holder).iv_goodsinfo_comment_photo);
            if (!OSUtil.isDayTheme())
                ((GoodsInfoCommentHodler) holder).iv_goodsinfo_comment_photo.setColorFilter(TravelUtil.getColorFilter(mContext));
            // 名字
            ((GoodsInfoCommentHodler) holder).tv_goodsinfo_comment_name.setText(evaluateInfoBean.getEvaluateUserName());
            setNameColor(((GoodsInfoCommentHodler) holder).tv_goodsinfo_comment_name,
                    ((GoodsInfoCommentHodler) holder).tv_goodsinfo_comment_content,
                    evaluateInfoBean);

            // 时间
            if (OSUtil.isNum(evaluateInfoBean.getEvaluateTime()))
                ((GoodsInfoCommentHodler) holder).tv_goodsinfo_comment_time.setText(
                        DateFormatUtil.getPastTime(evaluateInfoBean.getEvaluateTime()));
            else
                ((GoodsInfoCommentHodler) holder).tv_goodsinfo_comment_time.setText(evaluateInfoBean.getEvaluateTime());
            // 赞数
            ((GoodsInfoCommentHodler) holder).tv_goodsinfo_comment_like.setText(evaluateInfoBean.getLikeCount() == 0 ? "" : evaluateInfoBean.getLikeCount() + "");
            // 是否点过赞
            if (evaluateInfoBean.isLike())
                ((GoodsInfoCommentHodler) holder).tv_goodsinfo_comment_like.setSelected(true);
            else
                ((GoodsInfoCommentHodler) holder).tv_goodsinfo_comment_like.setSelected(false);
            // 评论
            ((GoodsInfoCommentHodler) holder).tv_goodsinfo_comment_content.setText(evaluateInfoBean.getEvaluateContent());
            if(!isVoteComments){
                ((GoodsInfoCommentHodler) holder).tv_goodsinfo_comment_content.setPadding(0, 0, OSUtil.dp2px(mContext, 34), 0);
            }
        }
    }

    private void setNameColor(TextView tv, TextView tv1, EvaluateInfoBean bean) {
        if (OSUtil.isDayTheme()) {
            tv.setTextColor(mContext.getResources().getColor(R.color.black_3));
            tv1.setTextColor(mContext.getResources().getColor(R.color.black_3));
        }else{
            tv.setTextColor(mContext.getResources().getColor(R.color.gray_C0));
            tv1.setTextColor(mContext.getResources().getColor(R.color.gray_C0));
        }
        if (bean.getEvaluateUserId() == null)
            return;
        int userId = Integer.parseInt(bean.getEvaluateUserId());
        if (hashMap != null && hashMap.containsKey(userId)) {
            if (0 == hashMap.get(userId)) { // 买家 buyer
                tv.setTextColor(mContext.getResources().getColor(R.color.red_EC6262));
                tv1.setTextColor(mContext.getResources().getColor(R.color.red_EC6262));
            } else if (1 == hashMap.get(userId)) { // seller
                tv.setTextColor(mContext.getResources().getColor(R.color.blue_6DB7CA));
                tv1.setTextColor(mContext.getResources().getColor(R.color.blue_6DB7CA));
            }
        }
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    class GoodsInfoCommentHodler extends RecyclerView.ViewHolder {

        ImageView iv_goodsinfo_comment_photo;
        TextView tv_goodsinfo_comment_name, tv_goodsinfo_comment_time, tv_goodsinfo_comment_like, tv_goodsinfo_comment_content;
        LinearLayout ll;
        View v_line_comment;

        public GoodsInfoCommentHodler(View itemView) {
            super(itemView);
            ImageDisplayTools.initImageLoader(mContext);
            iv_goodsinfo_comment_photo = (ImageView) itemView.findViewById(R.id.iv_goodsinfo_comment_photo);
            tv_goodsinfo_comment_name = (TextView) itemView.findViewById(R.id.tv_goodsinfo_comment_name);
            tv_goodsinfo_comment_time = (TextView) itemView.findViewById(R.id.tv_goodsinfo_comment_time);
            tv_goodsinfo_comment_like = (TextView) itemView.findViewById(R.id.tv_goodsinfo_comment_like);
            tv_goodsinfo_comment_content = (TextView) itemView.findViewById(R.id.tv_goodsinfo_comment_content);
            ll = (LinearLayout) itemView.findViewById(R.id.ll);
            v_line_comment = itemView.findViewById(R.id.v_line_comment);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ll.getLayoutParams();
            params.width = OSUtil.getScreenWidth() - OSUtil.dp2px(mContext, 69);
            ll.setLayoutParams(params);
            if (isVoteComments){
                if(OSUtil.isDayTheme())
                    itemView.findViewById(R.id.ll_view).setBackgroundResource(R.color.white);
                else
                    itemView.findViewById(R.id.ll_view).setBackgroundResource(R.color.black_3);
            }else{
                tv_goodsinfo_comment_like.setVisibility(View.GONE);
            }
            tv_goodsinfo_comment_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnLikeClickListener != null)
                        mOnLikeClickListener.onLikeClick(GoodsInfoCommentHodler.this);
                }
            });
        }
    }

    public interface OnLikeClickListener {
        void onLikeClick(RecyclerView.ViewHolder viewHolder);
    }

    public void setLikeClick(OnLikeClickListener mOnLikeClickListener) {
        this.mOnLikeClickListener = mOnLikeClickListener;
    }
}
