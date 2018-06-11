package com.travel.map;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.ctsmedia.hltravel.R;
import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.adapter.DividerItemDecoration;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.OSUtil;
import com.travel.map.factory.impl.GaodeMapFactory;
import com.travel.map.factory.location.impl.GaodeMapLocation;
import com.travel.map.factory.location.listener.IGaodeMapLocation;
import com.travel.map.factory.view.impl.GaodeMapView;
import com.travel.map.factory.view.listener.IGaodeMapView;
import com.travel.video.adapter.VideoListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/8/14.
 */

public class AmapLocationActivity extends TitleBarBaseActivity implements SwipeRefreshAdapterView.OnListLoadListener{
    public final static int REQUEST_CODE = 0x111;
    private GaodeMapView gaodeMapView ;
    private LocationSource.OnLocationChangedListener mListener;
    private GaodeMapLocation mapLocation;

    private SwipeRefreshRecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private List<PoiItem> poiItemList;
    private AmapLocationAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amap_location);
        setTitle("选择位置");
        initMap(savedInstanceState);

        initRecyclerView();

    }

    private void initMap(Bundle savedInstanceState) {
        gaodeMapView = (GaodeMapView) new GaodeMapFactory().createMapView();
        gaodeMapView.setListener(iMapView);
        gaodeMapView.onCreate(this, findViewById(R.id.mapView), savedInstanceState);
        // 定位功能
        mapLocation = (GaodeMapLocation) new GaodeMapFactory().createLocation();
        mapLocation.init(this, iMapLocation);
        mapLocation.startLocation();
        gaodeMapView.nearByLatLog();
    }

    private void initRecyclerView() {
        recyclerView = (SwipeRefreshRecyclerView) findViewById(R.id.sr_recylerView);
        linearLayoutManager = new LinearLayoutManager(this);
//        recyclerView.setOnListLoadListener(this);
        recyclerView.setEnabled(false);
        recyclerView.getScrollView().addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.HORIZONTAL_LIST, OSUtil.dp2px(this,1),getResources().getColor(R.color.gray_E6)));
        recyclerView.setLayoutManager(linearLayoutManager);

        poiItemList = new ArrayList<>();
        adapter = new AmapLocationAdapter(this, poiItemList);
        recyclerView.setAdapter(adapter);
        recyclerView.setEmptyText("定位失败!");
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

    @Override
    public void onListLoad() {

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        gaodeMapView.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gaodeMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gaodeMapView.onPause();
        iMapView.deactivate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gaodeMapView.onDestroy();
        mapLocation.onDestroy();
    }


    private IGaodeMapView iMapView = new IGaodeMapView() {

        @Override
        public void onPoiSearched(PoiResult poiResult, int i) {
            poiItemList.clear();
            poiItemList.addAll(poiResult.getPois());
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onPoiItemSearched(PoiItem poiItem, int i) {

        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public void onInfoWindowClick(Marker marker) {
            // 弹出窗口点击事件
            VideoInfoBean markerBean = (VideoInfoBean) marker.getObject();
            marker.hideInfoWindow();
        }

        @Override
        public void onMapLoaded() {

        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            return true;
        }

        @Override
        public void activate(OnLocationChangedListener onLocationChangedListener) {
            // 激活定位
            mListener = onLocationChangedListener;
            mapLocation.startLocation();
        }

        @Override
        public void deactivate() {
            // 停止定位
            mListener = null;
            mapLocation.onDestroy();
        }
    };

    private IGaodeMapLocation iMapLocation = new IGaodeMapLocation() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            // 定位成功后回调函数
            if (mListener != null && aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
//                    onLocationChanged(aMapLocation);// 显示系统小蓝点
                    gaodeMapView.setMyLocationStyle(R.drawable.mylocation_icon);

                    int locationType = aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
//                    latitude = aMapLocation.getLatitude();//获取纬度
//                    longitude = aMapLocation.getLongitude();//获取经度
//                    Log.e("myLocation:", latitude + "," + longitude);
                    aMapLocation.getAccuracy();//获取精度信息
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date(aMapLocation.getTime());
                    df.format(date);//定位时间
                } else {
                    String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                    Log.e("AmapErr", errText);
                    //如果程序没有定位权限
                    if (aMapLocation.getErrorCode() == 12) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.fromParts("package", AmapLocationActivity.this.getPackageName(), null));
                        startActivity(intent);
                        Toast.makeText(AmapLocationActivity.this, "请打开定位权限！", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
    };

}
