package com.travel.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.VideoConstant;
import com.travel.adapter.ProductListAdapter;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.shop.activity.GoodsActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 产品体验
 * Created by Administrator on 2017/12/8.
 */
public class ProductListActivity extends TitleBarBaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshAdapterView.OnListLoadListener {
    private SwipeRefreshRecyclerView recyclerView;
    private TextView rl_no_collect;
    private ProductListAdapter adapter;
    private ArrayList<GoodsBasicInfoBean> goods = new ArrayList<>();
    private int times = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipe_refresh_recycle_layout);
        setTitle("央视同款产品");
        initLayout();
        refresh();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initLayout() {
        rl_no_collect = (TextView) findViewById(R.id.none_notify);
        rl_no_collect.setText("这里黎明静悄悄！");
        recyclerView = (SwipeRefreshRecyclerView) findViewById(R.id.swipeRefresh);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setOnListLoadListener(this);
        recyclerView.setOnRefreshListener(this);
        adapter = new ProductListAdapter(this, goods);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemListener(new ProductListAdapter.OnItemListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(ProductListActivity.this, GoodsActivity.class);
                intent.putExtra("goodsId", goods.get(position).getGoodsId());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public void onListLoad() {
        load();
    }

    public void refresh() {
        times = 1;
        loadingData(times);
    }

    public void load() {
        ++times;
        loadingData(times);
    }

    private void loadingData(final int pageNo) {
        Map<String, Object> paramap = new HashMap<String, Object>();
        paramap.put("times", pageNo);
        paramap.put("showStatus", 1);
        NetWorkUtil.postForm(this, VideoConstant.GET_PRODUCT_LIST, new MResponseListener(this) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                if(pageNo == 1){
                    goods.clear();
                }
                try {
                    if (data != null && data.length() > 0) {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject dataObject = data.getJSONObject(i);
                            GoodsBasicInfoBean goodsBean = new GoodsBasicInfoBean();
                            // 商品id
                            goodsBean.setGoodsId(dataObject.optString("id"));
                            // 背景图片
                            goodsBean.setGoodsImg(dataObject.optString("imgUrl"));
                            // 标题
                            goodsBean.setGoodsTitle(dataObject.optString("goodsName"));
                            // 价格
                            goodsBean.setGoodsPrice(dataObject.optString("price"));
                            // 地点
                            goodsBean.setGoodsAddress(dataObject.optString("destCity"));
                            goods.add(goodsBean);
                        }
                    }else if (data.length() == 0 && times != 1) {// 拉取没有更多数据了
                        --times;
                        showToast(R.string.no_more);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (goods.size() > 0)
                    rl_no_collect.setVisibility(View.GONE);
                else
                    rl_no_collect.setVisibility(View.VISIBLE);

                stopRefresh();
                adapter.notifyDataSetChanged();
            }
        }, paramap);
    }

    private void stopRefresh() {
        recyclerView.setRefreshing(false);
        recyclerView.setLoading(false);
    }
}
