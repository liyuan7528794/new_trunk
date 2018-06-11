package com.travel.localfile.pk.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.localfile.CameraFragment;
import com.travel.localfile.pk.entity.EvidencePacket;

import java.util.List;

/**
 * Created by Administrator on 2017/2/10.
 * 众投页面中的证据列表Adapter
 */

public class EvidencePacketAdapter extends RecyclerView.Adapter<EvidencePacketAdapter.MyHodler> {

    private Context mContext;
    private List<EvidencePacket> listData;
    private OnLikeClickListener mOnLikeClickListener;
    private EvidenceMediaAdapter.OnMediaItemClickListener mediaListener;

    public EvidencePacketAdapter(Context context, List<EvidencePacket> listData, EvidenceMediaAdapter.OnMediaItemClickListener mediaListener) {
        this.listData = listData;
        mContext = context;
        this.mediaListener = mediaListener;
    }

    @Override
    public MyHodler onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.adapter_evidence, null);
        return new MyHodler(view);
    }

    @Override
    public void onBindViewHolder(MyHodler holder, int position) {
        EvidencePacket bean = listData.get(position);
        setData(holder, bean);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    class MyHodler extends RecyclerView.ViewHolder {
        View leftView, rightView, leftLine, rightLine;
        ImageView leftHeadImg, rightHeadImg;
        TextView leftName, rightName, leftTime, rightTime, leftContent, rightContent;
        RecyclerView leftRv, rightRv;

        public MyHodler(View itemView) {
            super(itemView);
            leftView = itemView.findViewById(R.id.leftLayout);
            leftLine = leftView.findViewById(R.id.view);
            leftName = (TextView) leftView.findViewById(R.id.nickName);
            leftTime = (TextView) leftView.findViewById(R.id.time);
            leftContent = (TextView) leftView.findViewById(R.id.content);
            leftHeadImg = (ImageView) leftView.findViewById(R.id.headImg);
            leftRv = (RecyclerView) leftView.findViewById(R.id.rv);


            rightView = itemView.findViewById(R.id.rightLayout);
            rightLine = rightView.findViewById(R.id.view);
            rightName = (TextView) rightView.findViewById(R.id.nickName);
            rightTime = (TextView) rightView.findViewById(R.id.time);
            rightContent = (TextView) rightView.findViewById(R.id.content);
            rightHeadImg = (ImageView) rightView.findViewById(R.id.headImg);
            rightRv = (RecyclerView) rightView.findViewById(R.id.rv);

            leftHeadImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnLikeClickListener != null)
                        mOnLikeClickListener.onLikeClick(MyHodler.this);
                }
            });
            rightHeadImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnLikeClickListener != null)
                        mOnLikeClickListener.onLikeClick(MyHodler.this);
                }
            });
        }
    }

    private void setData(MyHodler holder, EvidencePacket bean) {
        EvidenceMediaAdapter adapter;
        if (bean.isLeft()) {
            holder.leftView.setVisibility(View.VISIBLE);
            holder.rightView.setVisibility(View.GONE);
            ImageDisplayTools.disPlayRoundDrawableHead(bean.getUserData().getImgUrl(), holder.leftHeadImg, OSUtil.dp2px(mContext, 2));
            if(!OSUtil.isDayTheme())
                holder.leftHeadImg.setColorFilter(TravelUtil.getColorFilter(mContext));
            holder.leftName.setText("买方 " + bean.getUserData().getNickName());
            holder.leftTime.setText(bean.getCreateTime());
            if (bean.getIntroduction() == null || "".equals(bean.getIntroduction())) {
                holder.leftContent.setVisibility(View.GONE);
                holder.leftLine.setVisibility(View.GONE);
            } else {
                holder.leftContent.setVisibility(View.VISIBLE);
                holder.leftLine.setVisibility(View.VISIBLE);
            }
            holder.leftContent.setText(bean.getIntroduction());

            // 判断有无媒体列表
            if (bean.getMultipleMediaList() == null || bean.getMultipleMediaList().size() == 0) {
                holder.leftLine.setVisibility(View.GONE);
                holder.leftRv.setVisibility(View.GONE);
                return;
            }

            holder.leftRv.setVisibility(View.VISIBLE);
            adapter = new EvidenceMediaAdapter(mContext, bean.getMultipleMediaList(), holder.leftRv, mediaListener);
            if (bean.getMultipleMediaList().get(0).getType() == CameraFragment.TYPE_PHOTO
                    && bean.getMultipleMediaList().size() > 1) {
                holder.leftRv.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayout.VERTICAL, false));
//                holder.leftRv.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.BOTH_SET,
//                        OSUtil.dp2px(mContext, 1), mContext.getResources().getColor(R.color.transparent)));
            } else {
                holder.leftRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            }
            holder.leftRv.setAdapter(adapter);
        } else {
            holder.leftView.setVisibility(View.GONE);
            holder.rightView.setVisibility(View.VISIBLE);
            ImageDisplayTools.disPlayRoundDrawableHead(bean.getUserData().getImgUrl(), holder.rightHeadImg, OSUtil.dp2px(mContext, 2));
            if (!OSUtil.isDayTheme())
                holder.rightHeadImg.setColorFilter(TravelUtil.getColorFilter(mContext));
            holder.rightName.setText("卖方 " + bean.getUserData().getNickName());
            holder.rightTime.setText(bean.getCreateTime());
            if (bean.getIntroduction() == null || "".equals(bean.getIntroduction())){
                holder.rightContent.setVisibility(View.GONE);
                holder.rightLine.setVisibility(View.GONE);
            } else {
                holder.rightContent.setVisibility(View.VISIBLE);
                holder.rightLine.setVisibility(View.VISIBLE);
            }
            holder.rightContent.setText(bean.getIntroduction());

            // 判断有无图片，视频或音频列表
            if (bean.getMultipleMediaList() == null || bean.getMultipleMediaList().size() == 0) {
                holder.rightLine.setVisibility(View.GONE);
                holder.rightRv.setVisibility(View.GONE);
                return;
            }

            holder.rightRv.setVisibility(View.VISIBLE);
            adapter = new EvidenceMediaAdapter(mContext, bean.getMultipleMediaList(), holder.rightRv, mediaListener);
            if (bean.getMultipleMediaList().get(0).getType() == CameraFragment.TYPE_PHOTO
                    && bean.getMultipleMediaList().size() > 1) {
                holder.rightRv.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayout.VERTICAL, false));
            } else {
                holder.rightRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            }
            holder.rightRv.setAdapter(adapter);
        }
        if (bean.getMultipleMediaList() != null && bean.getMultipleMediaList().size() > 0)
            adapter.notifyDataSetChanged();
    }


    public interface OnLikeClickListener {
        void onLikeClick(RecyclerView.ViewHolder viewHolder);
    }

    public void setLikeClick(OnLikeClickListener mOnLikeClickListener) {
        this.mOnLikeClickListener = mOnLikeClickListener;
    }
}
