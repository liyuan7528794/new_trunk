package com.travel.video.layout;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.travel.Constants;
import com.travel.adapter.ProductAdapter;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.layout.BaseBellowPopupWindow;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.VideoConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductPopupWindow extends BaseBellowPopupWindow {
    private View rootView;
    private Context context;
    private String userId = "";
    private int times = 1;
    private ListView listView;
    private TextView tv_no_product;
    private PullToRefreshListView pullRefreshListView;
    private List<GoodsBasicInfoBean> cList;
    private ProductAdapter myAdapter;
    private ClickProductItemListener listener;

    public interface ClickProductItemListener {
        public void notifyIntentActivity();
    }

    public ProductPopupWindow(Context context,  String userId, ClickProductItemListener listener) {
        super(context);
        if (!UserSharedPreference.isLogin()) {
            Toast.makeText(context, "请先登录！", Toast.LENGTH_SHORT).show();
            return;
        }
        this.context = context;
        this.userId = userId;
        this.listener = listener;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.product_pop_window, null);
        tv_no_product = (TextView) rootView.findViewById(R.id.tv_no_product);
        initListView();
        SetContentView(rootView);
        show();
        loadingData();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void initListView() {
        pullRefreshListView = (PullToRefreshListView) rootView.findViewById(R.id.productList);
        cList = new ArrayList<GoodsBasicInfoBean>();
        pullRefreshListView.setMode(Mode.BOTH);
        // Set a listener to be invoked when the list should be refreshed.
        pullRefreshListView.setOnRefreshListener(new OnRefreshListener2() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase refreshView) {
                String label = DateUtils.formatDateTime(context.getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                times = 1;
                cList.clear();
                loadingData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase refreshView) {
                String label = DateUtils.formatDateTime(context.getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                if (cList != null && cList.size() > 0 && cList.size() % Constants.ItemNum == 0) {
                    times = times + 1;
                    loadingData();
                } else if (cList != null && cList.size() > 0 && cList.size() % Constants.ItemNum != 0) {
                    Toast.makeText(context, R.string.no_more, Toast.LENGTH_SHORT).show();
                    Handler handler = new Handler();
                    handler.postAtTime(new Runnable() {

                        @Override
                        public void run() {
                            pullRefreshListView.onRefreshComplete();
                        }
                    }, 1000);

                } else if (cList != null && cList.size() == 0) {
                    times = 1;
                    loadingData();
                }

            }
        });

        listView = pullRefreshListView.getRefreshableView();

        myAdapter = new ProductAdapter(context, cList);
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.notifyIntentActivity();
                Intent intent = new Intent();
                intent.setAction(Constants.GOODS_ACTION);
                intent.setType(Constants.VIDEO_TYPE);
                intent.putExtra("goodsId", cList.get(position - 1).getGoodsId());
                intent.putExtra("sourceType", 1);
                intent.putExtra("sourceId", userId);
                context.startActivity(intent);
            }
        });
        listView.setDivider(null);
    }

    private void loadingData() {
        if ("0".equals(userId)) {
            pullRefreshListView.onRefreshComplete();
            return;
        }

        Map<String, Object> paramap = new HashMap<String, Object>();
        paramap.put("userId", userId);
        paramap.put("times", times);
        NetWorkUtil.postForm(context, VideoConstant.GET_PRODUCT_LIST, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                try {
                    if (data != null && data.length() > 0) {

                        int listSize = cList.size() % Constants.ItemNum;
                        if (listSize > 0) {
                            for (int i = 0; i < listSize; i++) {
                                if (cList.size() > 0)
                                    cList.remove(cList.size() - 1);
                            }
                        }

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
                            goodsBean.setGoodsAddress(dataObject.optString("place"));
                            cList.add(goodsBean);
                        }
                        //					} else {
                        //						Toast.makeText(context, R.string.no_more, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (cList.size() > 0)
                    tv_no_product.setVisibility(View.GONE);
                else
                    tv_no_product.setVisibility(View.VISIBLE);

                pullRefreshListView.onRefreshComplete();
                myAdapter.notifyDataSetChanged();
            }
        }, paramap);
    }

}
