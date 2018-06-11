package com.travel.shop.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.photoselector.ui.GdAdapter;
import com.photoselector.ui.PhotoPreviewActivity;
import com.travel.bean.EvaluateInfoBean;
import com.travel.layout.ImageViewPopupWindow;
import com.travel.layout.MyGridView;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.R;
import com.travel.shop.tools.ShopTool;

import java.util.ArrayList;

/**
 * 商品评价的适配器
 *
 * @author WYP
 * @version 1.0
 * @created 2017/01/17
 */
public class EvaluateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<EvaluateInfoBean> listData;
    private EvaluateInfoBean mEvaluateInfoBean;
    private OnEvaluateClickListener mOnEvaluateClickListener;
    private ImageViewPopupWindow mImageViewPopupWindow;

    public EvaluateAdapter(ArrayList<EvaluateInfoBean> listData, Context context) {
        this.mContext = context;
        this.listData = listData;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.adapter_all_evaluate, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (position == 0)
            holder.itemView.setPadding(OSUtil.dp2px(mContext, 15), 0, OSUtil.dp2px(mContext, 15), 0);
        else
            holder.itemView.setPadding(OSUtil.dp2px(mContext, 15), OSUtil.dp2px(mContext, 28), OSUtil.dp2px(mContext, 15), 0);
        if (holder instanceof Holder) {
            mEvaluateInfoBean = listData.get(position);
            // 头像
            ImageDisplayTools.displayHeadImage(mEvaluateInfoBean.getEvaluateUserPhoto(), ((Holder) holder).iv_visitor_photo);
            if (!OSUtil.isDayTheme())
                ((Holder) holder).iv_visitor_photo.setColorFilter(TravelUtil.getColorFilter(mContext));
            ((Holder) holder).iv_visitor_photo.setTag(mEvaluateInfoBean);
            // 名字
            ((Holder) holder).tv_visitor_name.setText(mEvaluateInfoBean.getEvaluateUserName());
            // 日期
            ((Holder) holder).tv_date.setText(ShopTool.signChangeWord(mEvaluateInfoBean.getEvaluateTime()));
            // 星评
            ((Holder) holder).rb_evaluate.setRating(mEvaluateInfoBean.getEvaluateStar());
            // 内容
            ((Holder) holder).tv_evaluate.setText(mEvaluateInfoBean.getEvaluateContent());
            // 图片
            if (listData.get(position).getEvaluatePictures().size() > 1) {
                ((Holder) holder).gv_evaluate.setVisibility(View.VISIBLE);
                GdAdapter gdAdapter = new GdAdapter(mContext, mEvaluateInfoBean.getEvaluatePictures(), "goods");
                ((Holder) holder).gv_evaluate.setAdapter(gdAdapter);
                ((Holder) holder).gv_evaluate.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int pos = holder.getAdapterPosition();
                        // 预览图片
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("photos", listData.get(pos).getEvaluatePictures());
                        bundle.putInt("position", position);
                        bundle.putString("tag", "http");
                        TravelUtil.launchActivity(mContext, PhotoPreviewActivity.class, bundle);
                    }
                });
            } else if (listData.get(position).getEvaluatePictures().size() == 1) {
                ((Holder) holder).iv_one.setVisibility(View.VISIBLE);
                ImageDisplayTools.displayImage(
                        listData.get(position).getEvaluatePictures().get(0).getOriginalPath(), ((Holder) holder).iv_one);
                ((Holder) holder).iv_one.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mImageViewPopupWindow.show(mContext, ((Holder) holder).iv_one, listData.get(position).getEvaluatePictures().get(0).getOriginalPath());
                    }
                });
            }
            if (mEvaluateInfoBean.getEvaluatePictures().size() == 0) {
                ((Holder) holder).gv_evaluate.setVisibility(View.GONE);
            } else {
                ((Holder) holder).gv_evaluate.setVisibility(View.VISIBLE);
            }

            if (position == listData.size() - 1) {
                ((Holder) holder).v_line_evaluate.setVisibility(View.GONE);
                mOnEvaluateClickListener.onMeasureHeight();
            } else {
                ((Holder) holder).v_line_evaluate.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return listData != null ? listData.size() : 0;
    }

    class Holder extends RecyclerView.ViewHolder {
        public ImageView iv_visitor_photo, iv_one;
        public TextView tv_visitor_name, tv_date, tv_evaluate;
        public RatingBar rb_evaluate;
        public MyGridView gv_evaluate;
        private View v_line_evaluate;

        public Holder(View itemView) {
            super(itemView);
            iv_visitor_photo = (ImageView) itemView.findViewById(R.id.iv_visitor_photo);
            iv_one = (ImageView) itemView.findViewById(R.id.iv_one);
            tv_visitor_name = (TextView) itemView.findViewById(R.id.tv_visitor_name);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            tv_evaluate = (TextView) itemView.findViewById(R.id.tv_evaluate);
            rb_evaluate = (RatingBar) itemView.findViewById(R.id.rb_evaluate);
            gv_evaluate = (MyGridView) itemView.findViewById(R.id.gv_evaluate);
            v_line_evaluate = itemView.findViewById(R.id.v_line_evaluate);
            ImageDisplayTools.initImageLoader(mContext);
            mImageViewPopupWindow = new ImageViewPopupWindow();
            // 头像点击
            iv_visitor_photo.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
//                    // 禁止双击
//                    v.setEnabled(false);
//                    v.postDelayed(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            v.setEnabled(true);
//                        }
//                    }, 500);
                    if (mOnEvaluateClickListener != null)
                        mOnEvaluateClickListener.onPhotoClick((EvaluateInfoBean) iv_visitor_photo.getTag());
                }
            });
        }
    }

    public interface OnEvaluateClickListener {
        void onPhotoClick(EvaluateInfoBean mEvaluateInfoBean);

        void onMeasureHeight();
    }

    public void setmOnEvaluateClickListener(OnEvaluateClickListener mOnEvaluateClickListener) {
        this.mOnEvaluateClickListener = mOnEvaluateClickListener;
    }
}
