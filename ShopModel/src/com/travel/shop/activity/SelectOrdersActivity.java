package com.travel.shop.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.ShopConstant;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;
import com.travel.shop.adapter.SelectOrdersAdapter;
import com.travel.shop.bean.OrderBean;
import com.travel.shop.tools.ShopTool;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SelectOrdersActivity extends TitleBarBaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshAdapterView.OnListLoadListener {

    private Context mContext;
    private TextView tv_no_orders;

    private SwipeRefreshRecyclerView srrv_select;
    private ArrayList<OrderBean> mList;
    private SelectOrdersAdapter mSelectOrdersAdapter;
    private int mPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_orders);
        init();

        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            getOrdersData();
        }
        mSelectOrdersAdapter.setmOnItemClickListener(new SelectOrdersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(OrderBean mOrderBean) {
                Intent intent = new Intent(mContext, ApplicationPublicVoteActivity.class);
                intent.putExtra("orderBean", mOrderBean);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void init() {
        mContext = this;

        srrv_select = findView(R.id.srrv_select);
        tv_no_orders = findView(R.id.tv_no_orders);

        setTitle("选择订单");
        mList = new ArrayList<>();
        mSelectOrdersAdapter = new SelectOrdersAdapter(mList, mContext);
        srrv_select.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        srrv_select.setOnListLoadListener(this);
        srrv_select.setOnRefreshListener(this);
        srrv_select.setAdapter(mSelectOrdersAdapter);
    }

    @Override
    public void onRefresh() {
        mPage = 1;
        mList.clear();
        getOrdersData();
        srrv_select.setRefreshing(false);
    }

    @Override
    public void onListLoad() {
        ++mPage;
        getOrdersData();
        srrv_select.setLoading(false);
    }

    /**
     * 获取网络数据
     */
    private void getOrdersData() {
        Map<String, Object> map = new HashMap<>();
        map.put("buyerId", UserSharedPreference.getUserId());
        map.put("status", 4);
        map.put("times", mPage);
        NetWorkUtil.postForm(mContext, ShopConstant.ORDER_MANAGE, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                if (data.length() == 0 && mPage == 1) {
                    tv_no_orders.setVisibility(View.VISIBLE);
                    srrv_select.setVisibility(View.GONE);
                } else {
                    try {
                        for (int i = 0; i < data.length(); i++) {
                            OrderBean mOrderBean = new OrderBean();
                            JSONObject dataObject = data.getJSONObject(i);
                            if (dataObject.optInt("publicStatus") != 0)
                                continue;
                            // 订单号
                            mOrderBean.setOrdersId(dataObject.optLong("id"));
                            // 商品类型
                            mOrderBean.setGoodsType(dataObject.optInt("type"));
                            // 买家Id
                            mOrderBean.setBuyerId(dataObject.optString("buyerId"));
                            // 卖家Id
                            mOrderBean.setSalerId(dataObject.optString("sellerId"));
                            // 订单状态
                            mOrderBean.setStatus(dataObject.optInt("status"));
                            // 商品图片
                            mOrderBean.setGoodsImg(dataObject.optString("imgUrl"));
                            // 商品名称
                            mOrderBean.setGoodsTitle(dataObject.optString("goodsName"));
                            // 商品地址
                            //                            if (dataObject.optInt("type") == 1 || dataObject.optInt("type") == 2)
                            //                                // 商品出发地
                            //                                mOrderBean.setGoodsAddress(dataObject.optString("place"));
                            //                            else
                            // 商品目的地
                            mOrderBean.setGoodsAddress(dataObject.optString("destCity"));
                            // 总价
                            mOrderBean.setTotalPrice(Float.parseFloat(dataObject.optString("totalPrice")));
                            // 支付金额
                            mOrderBean.setPaymentPrice(Float.parseFloat(dataObject.optString("payment")));
                            // 出行时间
                            mOrderBean.setStartTime(ShopTool.signChangeWord(dataObject.optString("departTime")));
                            // 卖家
                            if (ShopTool.isSeller(mOrderBean.getSalerId())) {
                                mOrderBean.setName(dataObject.optString("buyerName"));
                                // 买家
                            } else {
                                mOrderBean.setName(dataObject.getJSONObject("user").optString("nickName"));
                            }
                            mList.add(mOrderBean);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        if (mList.size() == 0) {
                            tv_no_orders.setVisibility(View.VISIBLE);
                            srrv_select.setVisibility(View.GONE);
                        } else
                            mSelectOrdersAdapter.notifyDataSetChanged();
                        // 拉取时没有更多数据了
                        if (data.length() == 0) {
                            showToast(R.string.no_more);
                        }
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
}
