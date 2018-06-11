package com.travel.shop.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;
import com.travel.shop.activity.ManagerOrderActivity;
import com.travel.shop.bean.OrderBean;
import com.travel.shop.tools.ShopTool;

import java.util.ArrayList;

/**
 * 订单管理的适配器
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/15
 */
public class OrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private Resources rs;
    private ArrayList<OrderBean> listData;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnTextViewClickListener textClickListener;

    public OrderAdapter(Context context, ArrayList<OrderBean> listData) {
        this.mContext = context;
        rs = mContext.getResources();
        this.listData = listData;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.adapter_manage, null);
        final ManageOrderHolder holder = new ManageOrderHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    int position = holder.getAdapterPosition();
                    mOnItemClickListener.onItemClick(listData.get(position).getOrdersId(), position);
                }
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                /*if (mOnItemLongClickListener != null) {
                    int position = holder.getAdapterPosition();
                    mOnItemLongClickListener.onItemLongClick(listData.get(position).getOrdersId(),
                            listData.get(position).getSalerId(), position);
                }*/
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        int h = OSUtil.dp2px(mContext, 15);
        int c = h / 2;
        holder.itemView.setPadding(0, h, 0, 0);
        if (position == listData.size() - 1) {
            holder.itemView.setPadding(0, h, 0, h);
        }

        if (holder instanceof ManageOrderHolder) {
            OrderBean goodsBean = listData.get(position);
            // 小红点
            if (goodsBean.isBrowsed())
                ((ManageOrderHolder) holder).tv_orders_list_red_point.setVisibility(View.VISIBLE);
            else
                ((ManageOrderHolder) holder).tv_orders_list_red_point.setVisibility(View.GONE);
            // 扫描状态
            if (TextUtils.equals(ManagerOrderActivity.orderType, "supplier")) {
                if (goodsBean.getCheckStatus() == 0) {
                    ((ManageOrderHolder) holder).tv_status.setText("未扫码");
                } else {
                    ((ManageOrderHolder) holder).tv_status.setText("已扫码");
                }
            }
            // 商品图片
            ImageDisplayTools.displayImage(goodsBean.getGoodsImg(), ((ManageOrderHolder) holder).iv_goods);
            if (!OSUtil.isDayTheme())
                ((ManageOrderHolder) holder).iv_goods.setColorFilter(TravelUtil.getColorFilter(mContext));
            if (TextUtils.equals(ManagerOrderActivity.orderType, "supplier")) {
                // 标题
                ((ManageOrderHolder) holder).tv_goods_title.setText(goodsBean.getAttachGoodsBean().getAttachName());
                ((ManageOrderHolder) holder).tv_payment_price_show.setText(" ￥" + ShopTool.getMoney(goodsBean.getAttachGoodsBean().getTotalPrice()));
                // 支付金额
            } else {
                // 标题
                ((ManageOrderHolder) holder).tv_goods_title.setText(goodsBean.getDestCity() + " | " + goodsBean.getGoodsTitle());
                // 支付金额
                if (goodsBean.getPaymentPrice() == goodsBean.getTotalPrice())
                    ((ManageOrderHolder) holder).tv_total_price_show.setVisibility(View.GONE);
                else {
                    ((ManageOrderHolder) holder).tv_total_price_show.setVisibility(View.VISIBLE);
                    ((ManageOrderHolder) holder).tv_total_price_show.setText("￥" + ShopTool.getMoney(goodsBean.getTotalPrice() + ""));
                    ((ManageOrderHolder) holder).tv_total_price_show.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                }
                ((ManageOrderHolder) holder).tv_payment_price_show.setText(" ￥" + ShopTool.getMoney(goodsBean.getPaymentPrice() + ""));
            }
            // 出行时间
            if (goodsBean.getGoodsType() != 6) {
                ((ManageOrderHolder) holder).tv_peple_num.setText("人员：成人x" + goodsBean.getAdultNum());
                ((ManageOrderHolder) holder).tv_start_time.setText(rs.getString(R.string.manage_start_time) + goodsBean.getStartTime());
                ((ManageOrderHolder) holder).tv_start_time.setVisibility(View.VISIBLE);
                ((ManageOrderHolder) holder).tv_start_place.setVisibility(View.VISIBLE);
            } else {
                ((ManageOrderHolder) holder).tv_peple_num.setText("张数：" + goodsBean.getAdultNum() + "张");
                ((ManageOrderHolder) holder).tv_start_time.setVisibility(View.INVISIBLE);
                ((ManageOrderHolder) holder).tv_start_place.setVisibility(View.INVISIBLE);
                ((ManageOrderHolder) holder).tv_goods_title.setText(goodsBean.getGoodsTitle());
            }

            ((ManageOrderHolder) holder).tv_name.setText("订单号：" + goodsBean.getOrdersId());
            // 新功能
            ((ManageOrderHolder) holder).tv_start_place.setText("出发地：" + goodsBean.getDepartCity());
            ((ManageOrderHolder) holder).tv_refuse.setVisibility(View.GONE);
            ((ManageOrderHolder) holder).tv_accept.setVisibility(View.VISIBLE);
            ((ManageOrderHolder) holder).tv_refuse.setText("拒绝");
            ((ManageOrderHolder) holder).tv_accept.setText("");
            String status = ShopTool.getStatusOrder(goodsBean.getStatus(), goodsBean.getRefundStatus(), goodsBean.getPublicStatus());
            boolean isBuyer = TextUtils.equals(goodsBean.getBuyerId(), UserSharedPreference.getUserId());
            if (!TextUtils.equals(ManagerOrderActivity.orderType, "supplier")) {
                ((ManageOrderHolder) holder).tv_status.setText(status);

                switch (status) {
                    case "待支付":
                        if (isBuyer)
                            ((ManageOrderHolder) holder).tv_accept.setText("支付");
                        break;
                    case "等待卖家确认": //支付成功,等待商家确认
                        if (!isBuyer) {
                            // 商家显示
                            ((ManageOrderHolder) holder).tv_refuse.setVisibility(View.VISIBLE);
                            ((ManageOrderHolder) holder).tv_accept.setText("接受");
                        }
                        break;
                    case "待出行":
                        if (isBuyer){
                            ((ManageOrderHolder) holder).tv_accept.setVisibility(View.GONE);
                            ((ManageOrderHolder) holder).tv_accept.setText("行程安排");}
                        break;
                    case "待确认付款":

                        break;
                    case "待评价":
                        if (isBuyer)
                            ((ManageOrderHolder) holder).tv_accept.setText("订单评价");
                        break;
                    case "已评价":
                        if (!isBuyer)
                            ((ManageOrderHolder) holder).tv_accept.setText("用户评价");
                        break;
                    case "买家取消订单":
                        break;
                    case "卖家拒绝订单":
                        break;
                    case "支付超时已取消":
                        break;
                    case "订单取消":
                        break;
                    case "退款中":
                        break;
                    case "退款成功":
                        break;
                    case "退款失败":
                        break;
                    case "众投申请中":
                        break;
                    case "众投进行中":
                        break;
                    case "众投成功(买家赢)":
                        break;
                    case "众投成功(卖家赢)":
                        break;
                }
            }
            if (TextUtils.isEmpty(((ManageOrderHolder) holder).tv_accept.getText().toString())) {
                ((ManageOrderHolder) holder).tv_accept.setVisibility(View.GONE);
            }
            ((ManageOrderHolder) holder).tv_refuse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textClickListener.onClick((TextView) v, position);
                }
            });
            ((ManageOrderHolder) holder).tv_accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textClickListener.onClick((TextView) v, position);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    class ManageOrderHolder extends RecyclerView.ViewHolder {

        public ImageView iv_goods;
        public TextView tv_status, tv_goods_title, tv_total_price_show, tv_payment_price_show, tv_start_time, tv_name, tv_orders_list_red_point;

        public TextView tv_start_place, tv_peple_num, tv_refuse, tv_accept;

        public ManageOrderHolder(View itemView) {
            super(itemView);
            ImageDisplayTools.initImageLoader(mContext);
            iv_goods = (ImageView) itemView.findViewById(R.id.iv_goods);
            tv_status = (TextView) itemView.findViewById(R.id.tv_status);
            tv_goods_title = (TextView) itemView.findViewById(R.id.tv_goods_title);
            tv_total_price_show = (TextView) itemView.findViewById(R.id.tv_total_price_show);
            tv_payment_price_show = (TextView) itemView.findViewById(R.id.tv_payment_price_show);
            tv_start_time = (TextView) itemView.findViewById(R.id.tv_start_time);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_orders_list_red_point = (TextView) itemView.findViewById(R.id.tv_orders_list_red_point);

            tv_start_place = (TextView) itemView.findViewById(R.id.tv_start_place);
            tv_peple_num = (TextView) itemView.findViewById(R.id.tv_peple_num);
            tv_refuse = (TextView) itemView.findViewById(R.id.tv_refuse);
            tv_accept = (TextView) itemView.findViewById(R.id.tv_accept);
        }
    }

    /**
     * 列表的点击事件的监听
     */
    public interface OnItemClickListener {
        void onItemClick(long ordersId, int position);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    /**
     * 列表的长点击事件的监听
     */
    public interface OnItemLongClickListener {
        void onItemLongClick(long ordersId, String sellerId, int position);
    }

    public void setmOnItemLongClickListener(OnItemLongClickListener mOnItemLongClickListener) {
        this.mOnItemLongClickListener = mOnItemLongClickListener;
    }

    /**
     * 列表的点击事件的监听
     */
    public interface OnTextViewClickListener {
        void onClick(TextView v, int position);
    }

    public void setTextClickListener(OnTextViewClickListener textClickListener) {
        this.textClickListener = textClickListener;
    }


}
