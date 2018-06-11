package com.travel.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.ctsmedia.hltravel.R;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.map.utils.LocationTools;
import com.travel.video.bean.XpaiCofig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/6.
 */

public class NearbyLocationListActivity extends TitleBarBaseActivity {
    private TextView tv_isshow;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_location);
        setTitle("选择位置");
        initNearBy();

        initRecyclerView();
        if(XpaiCofig.latitude == -1 || XpaiCofig.longitude == -1){
            LocationTools.getInstans().destroyLocation();
            LocationTools.getInstans().setListener(new com.travel.map.utils.LocationTools.OnLocationListener() {
                @Override
                public void onLocation(double latitude, double longitude) {
                    if(latitude == -1 || longitude == -1){
                        showToast("获取位置出错！");
                    }
                    search(latitude, longitude);
                }
            });
            LocationTools.getInstans().startLocation();
        }else{
            search(XpaiCofig.latitude, XpaiCofig.longitude);
        }
    }

    private PoiSearch.Query query ;
    private PoiSearch poiSearch;

    public void initNearBy(){
//        150000 交通
// 190000 地址信息
        query = new PoiSearch.Query("", "", "");
        //keyWord表示搜索字符串，
        //第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
        //cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
        query.setPageSize(50);// 设置每页最多返回多少条poiitem
        query.setPageNum(1);//设置查询页码

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int i) {
                poiItemList.clear();
                poiItemList.addAll(poiResult.getPois());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {

            }
        });
    }

    private void search(double latitude, double longitude){
        poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(latitude, longitude), 10000));
        poiSearch.searchPOIAsyn();
    }

    private LinearLayoutManager linearLayoutManager;
    private List<PoiItem> poiItemList;
    private AmapLocationAdapter adapter;


    private void initRecyclerView() {
        tv_isshow = (TextView) findViewById(R.id.tv_isshow);
        tv_isshow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                Bundle bundle = new Bundle();
                bundle.putDouble("latitude", -1);
                bundle.putDouble("longitude", -1);
                bundle.putString("address", "请选择位置");
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setEnabled(false);
//        recyclerView.addItemDecoration(new DividerItemDecoration(this,
//                DividerItemDecoration.HORIZONTAL_LIST, OSUtil.dp2px(this,1),getResources().getColor(R.color.gray_E6)));
        recyclerView.setLayoutManager(linearLayoutManager);

        poiItemList = new ArrayList<>();
        adapter = new AmapLocationAdapter(this, poiItemList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemListener(new AmapLocationAdapter.OnItemListener() {
            @Override
            public void onItemClick(View view, int position) {
                PoiItem poiItem = poiItemList.get(position);
                Intent intent = getIntent();
                Bundle bundle = new Bundle();
                bundle.putDouble("latitude", poiItem.getLatLonPoint().getLatitude());
                bundle.putDouble("longitude", poiItem.getLatLonPoint().getLongitude());
                bundle.putString("address", poiItem.getCityName() + poiItem.getTitle());
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }
}
