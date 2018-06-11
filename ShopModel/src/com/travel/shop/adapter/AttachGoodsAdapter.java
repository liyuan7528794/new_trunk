package com.travel.shop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.travel.shop.R;
import com.travel.shop.bean.AttachGoodsBean;
import com.travel.shop.tools.ShopTool;

import java.util.ArrayList;
import java.util.List;

/**
 * 附加服务的适配器
 *
 * @author WYP
 * @version 1.0
 * @created 2017/04/28
 */
public class AttachGoodsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<AttachGoodsBean> listData;
    private AttachGoodsBean goods;
    private OnCountChangeListener mOnCountChangeListener;
    private boolean isEdit;

    public AttachGoodsAdapter(ArrayList<AttachGoodsBean> listData, boolean isEdit, Context context) {
        this.mContext = context;
        this.listData = listData;
        this.isEdit = isEdit;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.adapter_attach_goods, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof Holder) {
            goods = listData.get(position);
            // 商品名称
            ((Holder) holder).tv_attach_goods_name.setText(goods.getAttachName());
            // 价格
            ((Holder) holder).tv_attach_goods_price.setText("￥" + ShopTool.getMoney(goods.getPrice()) +
                    "/" + goods.getUnit());
            ((Holder) holder).ll_editable.setVisibility(View.GONE);
            ((Holder) holder).tv_attach_count.setVisibility(View.GONE);
            if (isEdit) {// 可编辑
                ((Holder) holder).ll_editable.setVisibility(View.VISIBLE);
                // 数量
                ((Holder) holder).tv_goods_count.setText(goods.getCount() + "");
                // 减号
                if (goods.getCount() == 0)
                    ((Holder) holder).iv_goods_minus.setImageResource(R.drawable.icon_card_minus_no);
                else
                    ((Holder) holder).iv_goods_minus.setImageResource(R.drawable.icon_card_minus_yes);
                ((Holder) holder).ll_goods_minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnCountChangeListener != null)
                            mOnCountChangeListener.onMinus(position);
                    }
                });
                ((Holder) holder).ll_goods_plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnCountChangeListener != null)
                            mOnCountChangeListener.onPlus(position);
                    }
                });
            } else {// 不可编辑
                ((Holder) holder).tv_attach_count.setVisibility(View.VISIBLE);
                // 数量
                ((Holder) holder).tv_attach_count.setText(goods.getCount() + goods.getUnit());
                if (position == listData.size() - 1)
                    ((Holder) holder).v_last_line.setVisibility(View.INVISIBLE);
                else
                    ((Holder) holder).v_last_line.setVisibility(View.VISIBLE);
            }

        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (payloads != null) {
            // 数量
            ((Holder) holder).tv_goods_count.setText(goods.getCount() + "");
            // 减号
            if (goods.getCount() == 0 || (payloads.size() != 0 && TextUtils.equals(payloads.get(0).toString(), "1") && goods.getCount() == 1))
                ((Holder) holder).iv_goods_minus.setImageResource(R.drawable.icon_card_minus_no);
            else
                ((Holder) holder).iv_goods_minus.setImageResource(R.drawable.icon_card_minus_yes);
        } else {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return listData != null ? listData.size() : 0;
    }

    class Holder extends RecyclerView.ViewHolder {
        public TextView tv_attach_goods_name, tv_attach_goods_price, tv_goods_count, tv_attach_count;
        public View ll_editable, ll_goods_minus, ll_goods_plus, v_last_line;
        public ImageView iv_goods_minus;

        public Holder(View itemView) {
            super(itemView);
            tv_attach_goods_name = (TextView) itemView.findViewById(R.id.tv_attach_goods_name);
            tv_attach_goods_price = (TextView) itemView.findViewById(R.id.tv_attach_goods_price);
            tv_attach_count = (TextView) itemView.findViewById(R.id.tv_attach_count);
            ll_editable = itemView.findViewById(R.id.ll_editable);
            ll_goods_minus = ll_editable.findViewById(R.id.ll_m_click);
            iv_goods_minus = (ImageView) ll_editable.findViewById(R.id.iv_minus);
            tv_goods_count = (TextView) ll_editable.findViewById(R.id.tv_count);
            ll_goods_plus = ll_editable.findViewById(R.id.ll_p_click);
            v_last_line = itemView.findViewById(R.id.v_last_line);
        }
    }

    public interface OnCountChangeListener {
        void onPlus(int position);

        void onMinus(int position);
    }

    public void setmOnEvaluateClickListener(OnCountChangeListener mOnCountChangeListener) {
        this.mOnCountChangeListener = mOnCountChangeListener;
    }
}
