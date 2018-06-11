package com.travel.video.live;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.VideoConstant;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.video.adapter.CheckProductAdapter;
import com.travel.video.bean.ProductInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckProductActivity extends TitleBarBaseActivity {
    private ListView listView;
    private List<ProductInfo> list;
    private ProductInfo curProductInfo;
    private CheckProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_check);
        setTitle("产品列表");
        rightButton.setText("确认");
        rightButton.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams param = (LayoutParams) rightButton.getLayoutParams();
        param.rightMargin = OSUtil.dp2px(getApplicationContext(), 10);
        rightButton.setLayoutParams(param);
        rightButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (curProductInfo == null) {
                    Toast.makeText(getApplicationContext(), "请选择商品！", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(CheckProductActivity.this, HostWindowActivity.class);
                if (curProductInfo != null) {
                    intent.putExtra("goods_id", curProductInfo.getId());
                    intent.putExtra("goods_name", curProductInfo.getName());
                }
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        listView = (ListView) findViewById(R.id.checkProductListView);
        list = new ArrayList<ProductInfo>();
        adapter = new CheckProductAdapter(this, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(itemClickListener);
        getProducts();

        leftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private OnItemClickListener itemClickListener = new OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (curProductInfo != null && curProductInfo.getId() == list.get(position).getId()) {
                adapter.setSelectItem(-1);
                adapter.notifyDataSetInvalidated();
                curProductInfo = null;
            } else {
                adapter.setSelectItem(position);
                adapter.notifyDataSetInvalidated();
                curProductInfo = list.get(position);
            }
        }

    };

    private void getProducts() {
        String url = VideoConstant.GET_MY_PRODUCT_LIST;
        Map paraMap = new HashMap();
        paraMap.put("userId", UserSharedPreference.getUserId());
        //		paraMap.put("times", 1);
        NetWorkUtil.postForm(CheckProductActivity.this, url, new MResponseListener(this) {

            @Override
            protected void onDataFine(JSONArray data) {
                try {
                    if (data != null && data.length() > 0) {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject json = data.getJSONObject(i);
                            ProductInfo product = new ProductInfo();
                            product.setId(json.getString("id"));
                            product.setName(json.getString("goodsName"));

                            list.add(product);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    adapter.notifyDataSetChanged();
                }
            }

        }, paraMap);
    }

}
