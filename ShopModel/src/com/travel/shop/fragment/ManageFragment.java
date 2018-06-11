package com.travel.shop.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.AdapterJoiner.AdapterJoiner;
import com.travel.AdapterJoiner.JoinableAdapter;
import com.travel.AdapterJoiner.JoinableLayout;
import com.travel.ShopConstant;
import com.travel.communication.helper.ShopMessageHelper;
import com.travel.layout.DialogTemplet;
import com.travel.layout.DialogTemplet.DialogLeftButtonListener;
import com.travel.layout.DialogTemplet.DialogRightButtonListener;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;
import com.travel.shop.activity.EvaluateActivity;
import com.travel.shop.activity.EvaluationInfoActivity;
import com.travel.shop.activity.ManagerOrderActivity;
import com.travel.shop.activity.OrderInfoActivity;
import com.travel.shop.adapter.OrderAdapter;
import com.travel.shop.bean.AttachGoodsBean;
import com.travel.shop.bean.GoodsOrderBean;
import com.travel.shop.bean.OrderBean;
import com.travel.shop.http.OrderInfoHttp;
import com.travel.shop.tools.ShopTool;
import com.travel.shop.widget.PayMethodPopWindow;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单管理页的Fragment
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/15
 */
public class ManageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshAdapterView.OnListLoadListener {

    private Context mContext;
    private View mView;

    // 刷新控件
    private AdapterJoiner joiner;
    private SwipeRefreshRecyclerView srrv_manage;
    private JoinableLayout noViewLayout;
    // 显示相关
    private OrderAdapter mAdapter;
    private ArrayList<OrderBean> mList;
    private int mPage = 1;
    private int status = 0;// 订单状态 0表示全部
    private int refundStatus = -1;// 退款状态
    private DialogTemplet dialog;
    private int pos = -1;
    private long ordersId;

    // 无数据时
    private Handler mHandler;
    private View noView;

    // 小红点的显示相关
    private ArrayList<String> ordersList;
    private ShopMessageHelper hepler;

    // 扫描后刷新当前页
    private RefreshOrdersBroadcastReceiver receiver;

    public static Fragment newInstance(int status) {
        ManageFragment fragment = new ManageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("status", status);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_manage, null);

        init();
        initListener();
        // 通知更新UI(无数据时)
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    // 有数据
                    case 1:
                        hideNoDataView();
                        break;
                    // 无数据
                    case 2:
                        showNoDataView("暂无订单");
                        break;
                    default:
                        break;
                }

            }
        };

        status = getArguments().getInt("status");
        // 有网
        if (ManagerOrderActivity.isNet) {
            mPage = 1;
            getOrderData();
        }

        return mView;
    }

    private void initListener() {
        // 进入订单详情
        mAdapter.setmOnItemClickListener(new OrderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(long ordersId, int position) {
                pos = position;
                Intent intent = new Intent(mContext, OrderInfoActivity.class);
                intent.putExtra("ordersId", ordersId);
                intent.putExtra("attachData", mList.get(position).getAttachGoodsBean());
                startActivity(intent);
                //                OrderInfoRouteActivity.actionStart(mContext, ordersId, "manage",
                //                        "", mList.get(position).getAttachGoodsBean());
            }
        });
        mAdapter.setmOnItemLongClickListener(new OrderAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(final long ordersId, final String sellerId, final int position) {
                // 完成的订单买卖双方都可删，未支付的订单都不可删
                if (status == 2) {
                    dialog = new DialogTemplet(mContext, false, "确定删除该订单？", "", "取消", "确定");
                    dialog.show();
                    dialog.setLeftClick(new DialogLeftButtonListener() {

                        @Override
                        public void leftClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setRightClick(new DialogRightButtonListener() {

                        @Override
                        public void rightClick(View view) {
                            int type = ShopTool.isSeller(sellerId) ? 2 : 1;
                            deleteOrders(type, ordersId, position);
                        }
                    });
                }
            }
        });
        mAdapter.setTextClickListener(new OrderAdapter.OnTextViewClickListener() {
            @Override
            public void onClick(TextView v, int position) {
                ordersId = mList.get(position).getOrdersId();
                switch (v.getText().toString()) {
                    case "拒绝":
                        ShopTool.setTwiceSureDialog(ordersId, mContext, 4, controlOrderSuccessListener);
                        break;
                    case "接受":
                        ShopTool.setTwiceSureDialog(ordersId, mContext, 1, controlOrderSuccessListener);
                        break;
                    case "支付":
                        OrderInfoHttp.getManageOrderData(ordersId, mContext, listener);
                        break;
                    case "行程安排":
                        OSUtil.intentPlan(getContext(), "orderId", ordersId + "");
                        break;
                    case "订单评价":
                        Intent intent = new Intent(mContext, EvaluateActivity.class);
                        intent.putExtra("ordersId", ordersId);
                        intent.putExtra("goodsId", mList.get(position).getGoodsId());
                        startActivity(intent);
                        break;
                    case "用户评价":
                        startActivity(new Intent(mContext, EvaluationInfoActivity.class).putExtra("ordersId", ordersId));
                        break;
                }
            }
        });
    }

    // 获取支付金额
    private OrderInfoHttp.Listener listener = new OrderInfoHttp.Listener() {
        @Override
        public void onOrderDataFine(GoodsOrderBean goodsOrderBean) {
            String paymentPrice = ShopTool.getMoney(goodsOrderBean.getmOrdersBasicInfoBean().getPaymentPrice() + "");
            if (TextUtils.equals("0", paymentPrice)) {
                OrderInfoHttp.payZero(ordersId, mContext, zeroPayListener);
            } else {
                new PayMethodPopWindow(mContext, goodsOrderBean.getmGoodsBasicInfoBean().getGoodsTitle(), getActivity(), ordersId,
                        paymentPrice + "", "orderRoute", 2, 2);
            }
        }

        @Override
        public void onErrorNotZero(int error, String msg) {

        }

        @Override
        public void onAttachGoodsGot(ArrayList<AttachGoodsBean> attachGoods) {

        }
    };

    /**
     * 控件初始化
     */
    private void init() {
        mContext = getActivity();
        noView = View.inflate(mContext, R.layout.layout_no_data, null);
        srrv_manage = (SwipeRefreshRecyclerView) mView.findViewById(R.id.srrv_manage);

        mList = new ArrayList<>();
        ordersList = new ArrayList<>();
        mAdapter = new OrderAdapter(mContext, mList);
        srrv_manage.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        srrv_manage.setOnListLoadListener(this);
        srrv_manage.setOnRefreshListener(this);
        joiner = new AdapterJoiner();
        noViewLayout = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                noView.setLayoutParams(params);
                return noView;
            }
        });
        joiner.add(noViewLayout);
        noViewLayout.hide();
        joiner.add(new JoinableAdapter(mAdapter));
        srrv_manage.setAdapter(joiner.getAdapter());

        receiver = new RefreshOrdersBroadcastReceiver();
        mContext.registerReceiver(receiver, new IntentFilter("refresh_orders"));
    }


    @Override
    public void onResume() {
        super.onResume();

        // 有网
        if (ManagerOrderActivity.isNet && pos != -1 && mList.size() != 0) {
            mList.get(pos).setBrowsed(false);
            mAdapter.notifyItemChanged(pos);// 更新单条的小红点
            getUpdateOrders(status);// 更新头部的小红点
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext.unregisterReceiver(receiver);
    }

    /**
     * 获取网络数据
     */
    private void getOrderData() {
        Map<String, Object> map = new HashMap<>();
        String url = "";
        // 供应商的订单数据
        if (TextUtils.equals(ManagerOrderActivity.orderType, "supplier")) {
            url = ShopConstant.ORDER_MANAGE_3;
        } else {
            url = ShopConstant.ORDER_MANAGE;
            if (TextUtils.equals(ManagerOrderActivity.orderType, "my")) {
                map.put("buyerId", UserSharedPreference.getUserId());
            } else {
                map.put("sellerId", UserSharedPreference.getUserId());
            }
        }
        if (status != 0)
            map.put("status", status);
        if (refundStatus != -1)
            map.put("refundStatus", refundStatus);
        map.put("times", mPage);
        NetWorkUtil.postForm(mContext, url, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                if (mPage == 1) {
                    mList.clear();
                }
                if (data.length() == 0 && mPage == 1) {
                    mHandler.sendEmptyMessage(2);
                    getUpdateOrders(status);
                } else {
                    try {
                        for (int i = 0; i < data.length(); i++) {
                            OrderBean mOrderBean = new OrderBean();
                            JSONObject dataOb;
                            JSONObject dataObject = data.getJSONObject(i);
                            if (TextUtils.equals(ManagerOrderActivity.orderType, "supplier")) {
                                JSONObject ordersObject = dataObject.getJSONObject("orders");
                                dataOb = ordersObject;
                            } else
                                dataOb = dataObject;
                            // 订单号
                            mOrderBean.setOrdersId(dataOb.optLong("id"));
                            // 商品Id
                            mOrderBean.setGoodsId(dataOb.optString("goodsId"));
                            // 买家Id
                            mOrderBean.setBuyerId(dataOb.optString("buyerId"));
                            // 卖家Id
                            mOrderBean.setSalerId(dataOb.optString("sellerId"));
                            // 订单管理状态
                            mOrderBean.setStatusManage(dataOb.optInt("status"));
                            // 订单状态
                            //                            mOrderBean.setStatus(dataOb.optInt(dataOb.optInt("status") == 1 ? "orderStatus" : "overStatus"));
                            mOrderBean.setStatus(dataOb.optInt("status"));
                            // 商品类型
                            mOrderBean.setGoodsType(dataOb.optInt("type"));
                            // 商品图片
                            mOrderBean.setGoodsImg(dataOb.optString("imgUrl"));
                            // 商品名称
                            mOrderBean.setGoodsTitle(dataOb.optString("goodsName"));
                            // 总价
                            mOrderBean.setTotalPrice(Float.parseFloat(dataOb.optString("totalPrice")));
                            // 支付金额
                            mOrderBean.setPaymentPrice(Float.parseFloat(dataOb.optString("payment")));
                            // 成人数量
                            mOrderBean.setAdultNum(dataOb.optInt("adultNum"));
                            // 儿童数量
                            mOrderBean.setChildNum(dataOb.optInt("childNum"));
                            // 总人数
                            mOrderBean.setTotalNum(dataOb.optInt("totalNum"));
                            // 出发地
                            mOrderBean.setDepartCity(dataOb.optString("departCity"));
                            // 目的地
                            mOrderBean.setDestCity(dataOb.optString("destCity"));
                            // 目的地
                            mOrderBean.setRefundStatus(dataOb.optInt("refundStatus"));
                            // 目的地
                            mOrderBean.setPublicStatus(dataOb.optInt("publicStatus"));
                            // 出行时间
                            mOrderBean.setStartTime(ShopTool.signChangeWord(dataOb.optString("departTime")));
                            // 卖家
                            //                            if (ShopTool.isSeller(mOrderBean.getSalerId())) {
                            mOrderBean.setName(dataOb.optString("buyerName"));
                            //                                // 买家
                            //                            } else {
                            //                                mOrderBean.setName(dataOb.getJSONObject("user").optString("nickName"));
                            //                            }

                            if (TextUtils.equals(ManagerOrderActivity.orderType, "supplier")) {
                                // 附加服务
                                AttachGoodsBean mAttachGoodsBean = new AttachGoodsBean();
                                mAttachGoodsBean.setAttachName(dataObject.optString("attachName"));
                                mAttachGoodsBean.setTotalPrice(dataObject.optString("attachTotalPrice"));
                                mAttachGoodsBean.setPrice(dataObject.optString("attachPrice"));
                                mAttachGoodsBean.setUnit(dataObject.optString("attachUnit"));
                                mAttachGoodsBean.setCount(dataObject.optInt("attachNum"));
                                mOrderBean.setAttachGoodsBean(mAttachGoodsBean);
                                mOrderBean.setCheckStatus(dataObject.optInt("checkStatus"));
                            }
                            mList.add(mOrderBean);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        // 拉取时没有更多数据了
                        if (data.length() == 0) {
                            TravelUtil.showToast(R.string.no_more, mContext);
                            srrv_manage.setEnabledLoad(false);
                        } else
                            srrv_manage.setEnabledLoad(true);
                        if (mList != null && mList.size() > 0)
                            //                            getUpdateOrders(mList.get(0).getStatusManage());
                            getUpdateOrders(status);
                        mHandler.sendEmptyMessage(1);
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                // 再次联网之前，初始化当前页面的控件和数据
                if (mPage != 1) {
                    --mPage;
                }
            }
        }, map);
    }

    /**
     * 设置订单列表中的小红点的显示
     */
    private void getUpdateOrders(int status) {
        if (!TextUtils.equals(ManagerOrderActivity.orderType, "supplier")) {
            getUnReadCount(0);
            getUnReadCount(OrderBean.STATUS_1);
            getUnReadCount(OrderBean.STATUS_3);
            getUnReadCount(OrderBean.STATUS_5);
        }
        mAdapter.notifyDataSetChanged();
    }

    // 获取未读数
    private void getUnReadCount(int status) {
        ArrayList<String> unReadList = new ArrayList<>();
        hepler = new ShopMessageHelper(mContext);
        Cursor c;
        if (status == 0) {
            c = hepler.query(ShopMessageHelper.TABLENAME_MESSAGE, ManagerOrderActivity.orderType,
                    UserSharedPreference.getUserId());
        } else {
            c = hepler.query(ShopMessageHelper.TABLENAME_MESSAGE, ManagerOrderActivity.orderType,
                    UserSharedPreference.getUserId(), status + "");
        }
        if (c.moveToFirst()) {
            do {
                unReadList.add(c.getString(c.getColumnIndex("ordersId")));
            } while (c.moveToNext());
        }
        c.close();
        hepler.close();
        if (this.status == status) {// 必须判断是否是当前页
            ordersList.clear();
            ordersList.addAll(unReadList);// 好像没用了
            for (int i = 0; i < unReadList.size(); i++) { // 数据库(变化)的订单
                for (int j = 0; j < mList.size(); j++) {// 网络获取的订单
                    // 两个list的orderId一样的订单，显示小红点
                    if (TextUtils.equals(unReadList.get(i), mList.get(j).getOrdersId() + "")) {
                        mList.get(j).setBrowsed(true);
                    }
                }
            }
        }
        Message msg = new Message();
        msg.obj = unReadList.size();
        msg.what = status;
        ManagerOrderActivity.mHandler.sendMessage(msg);
    }

    /**
     * 删除订单
     *
     * @param type
     * @param orderId
     */
    private void deleteOrders(int type, final long orderId, final int position) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("type", type);
        map.put("ordersId", orderId);
        NetWorkUtil.postForm(mContext, ShopConstant.ORDER_DELETE, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    mList.remove(position);
                    mAdapter.notifyDataSetChanged();
                    deleteOrdersId(orderId);
                    getUpdateOrders(status);
                }
            }
        }, map);
    }

    /**
     * 从数据库中删除数据
     */
    private void deleteOrdersId(long ordersId) {
        // 消除小红点
        ShopMessageHelper helper = new ShopMessageHelper(mContext);
        helper.delete(ShopMessageHelper.TABLENAME_MESSAGE, ordersId + "", UserSharedPreference.getUserId());
    }

    @Override
    public void onRefresh() {
        mPage = 1;
        //        status = ManagerOrderActivity.status;
        getOrderData();
        srrv_manage.setRefreshing(false);
    }

    @Override
    public void onListLoad() {
        ++mPage;
        /*switch (ManagerOrderActivity.status){
            case 0:
                status = 0;
                break;
            case 1:
                status = OrderBean.STATUS_1;
                break;
            case 2:
                status = OrderBean.STATUS_3;
                break;
            case 3:
                status = OrderBean.STATUS_5;
                break;
        }*/
        getOrderData();
        srrv_manage.setLoading(false);
    }

    private void showNoDataView(String notify) {
        TextView tv = (TextView) noView.findViewById(R.id.tv_no_data);
        tv.setText(notify);
        noViewLayout.show();
    }

    private void hideNoDataView() {
        noViewLayout.hide();
    }

    class RefreshOrdersBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 接受到扫描成功
            getOrderData();
        }
    }

    // 操作成功刷新页面
    OrderInfoHttp.ControlOrderSuccessListener controlOrderSuccessListener = new OrderInfoHttp.ControlOrderSuccessListener() {
        @Override
        public void onSuccess() {
            mPage = 1;
            getOrderData();
        }
    };
    // 0元支付
    OrderInfoHttp.ControlListener zeroPayListener = new OrderInfoHttp.ControlListener() {
        @Override
        public void onPayZero(long ordersId) {
            OSUtil.intentOrderSuccess(mContext, ordersId);
        }
    };

}
