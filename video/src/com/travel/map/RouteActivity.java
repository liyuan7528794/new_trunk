package com.travel.map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.ctsmedia.hltravel.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.travel.ShopConstant;
import com.travel.bean.VideoInfoBean;
import com.travel.layout.DialogTemplet;
import com.travel.lib.ui.LoadingDialog;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.map.factory.impl.GaodeMapFactory;
import com.travel.map.factory.location.impl.GaodeMapLocation;
import com.travel.map.factory.location.listener.IGaodeMapLocation;
import com.travel.map.factory.view.impl.GaodeMapView;
import com.travel.map.factory.view.listener.IGaodeMapView;
import com.travel.usercenter.entity.PlanEntity;
import com.travel.video.bean.XpaiCofig;
import com.travel.video.layout.RouteMarkerPopupWindow;
import com.volley.VolleyError;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/29.
 */

public class RouteActivity extends TitleBarBaseActivity{
    private GaodeMapView gaodeMapView ;
    private LocationSource.OnLocationChangedListener mListener;
    private GaodeMapLocation mapLocation;

    private String journeyId, orderId;
    private double latitude;
    private double longitude;

    private Bitmap myLocationBitmap = null;

    private Marker checkMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        setTitle("行程安排");

        if(getIntent().hasExtra("journeyId"))
            journeyId = getIntent().getStringExtra("journeyId");
        if(getIntent().hasExtra("orderId"))
            orderId = getIntent().getStringExtra("orderId");

        // 定位功能
        mapLocation = (GaodeMapLocation) new GaodeMapFactory().createLocation();
        mapLocation.init(this, iMapLocation);
        mapLocation.setOnlyOnce(true);
//        mapLocation.startLocation();

        gaodeMapView = (GaodeMapView) new GaodeMapFactory().createMapView();
        gaodeMapView.setListener(iMapView);
        gaodeMapView.onCreate(this, findViewById(R.id.map), savedInstanceState);

        if(myLocationBitmap == null){
            createMyLocationBitmap(UserSharedPreference.getUserHeading());
        }

        getData();
    }

    private void show(String str){
        Toast.makeText(RouteActivity.this, str, Toast.LENGTH_SHORT).show();
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapLocation.stopLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gaodeMapView.onDestroy();
        mapLocation.onDestroy();

        if(smallBitmap!=null)
            smallBitmap.recycle();
        if(myLocationBitmap != null)
            myLocationBitmap.recycle();
    }

    List<PlanEntity.PlanLocation> list = new ArrayList<>();
    private IGaodeMapView iMapView = new IGaodeMapView() {

        @Override
        public void onPoiSearched(PoiResult poiResult, int i) {

        }

        @Override
        public void onPoiItemSearched(PoiItem poiItem, int i) {

        }

        @Override
        public View getInfoWindow(Marker marker) {
            View infoWindow = LayoutInflater.from(RouteActivity.this).inflate(
                        R.layout.custom_info_window, null);
            render(marker, infoWindow);
            return infoWindow;
        }

        private void render(Marker marker, View infoWindow) {
            TextView text = (TextView) infoWindow.findViewById(R.id.title);
            text.setText(marker.getTitle());
        }


        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public void onInfoWindowClick(Marker marker) {
            // 弹出窗口点击事件

        }

        @Override
        public void onMapLoaded() {
            //设置中心点和缩放比例
            if(latLng1 != null && latLng2 != null) {
                LatLngBounds latLngBounds = new LatLngBounds(latLng1, latLng2);
                gaodeMapView.getaMap().moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50));
            }else {
                if(list.size() > 0 || list.get(0).getLocations().length > 1){
                    LatLng marker1 = new LatLng(list.get(0).getLocations()[0], list.get(0).getLocations()[1]);
                    gaodeMapView.getaMap().moveCamera(CameraUpdateFactory.changeLatLng(marker1));
                }
                gaodeMapView.getaMap().moveCamera(CameraUpdateFactory.zoomTo(12));
            }
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            if(TextUtils.isEmpty(marker.getTitle())) return true;
            if(checkMarker != null){
                // 使用最原始的图片进行大小计算
                checkMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap
                        .createScaledBitmap(smallBitmap, OSUtil.dp2px(RouteActivity.this, 36),OSUtil.dp2px(RouteActivity.this, 39), true)));
                checkMarker.setVisible(true);
            }
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap
                    .createScaledBitmap(smallBitmap, OSUtil.dp2px(RouteActivity.this, 46),OSUtil.dp2px(RouteActivity.this, 50), true)));
            marker.setVisible(true);
            marker.showInfoWindow();
            checkMarker = marker;

            new RouteMarkerPopupWindow(RouteActivity.this, marker, new RouteMarkerPopupWindow.RouteMarkerPopupWindowListenre() {
                @Override
                public void hideWindow(Marker marker) {
//                    if(marker != null)
                    checkMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap
                            .createScaledBitmap(smallBitmap, OSUtil.dp2px(RouteActivity.this, 36),OSUtil.dp2px(RouteActivity.this, 39), true)));
                    checkMarker.setVisible(true);
                    checkMarker.hideInfoWindow();
                }
            });
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
//            mapLocation.onDestroy();
        }
    };

    private IGaodeMapLocation iMapLocation = new IGaodeMapLocation() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            // 定位成功后回调函数
            if (mListener != null && aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                    if(myLocationBitmap == null){
                        createMyLocationBitmap(UserSharedPreference.getUserHeading());
                    }
                    gaodeMapView.setMyLocationStyle(myLocationBitmap);

                    int locationType = aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    XpaiCofig.latitude = latitude = aMapLocation.getLatitude();//获取纬度
                    XpaiCofig.longitude = longitude = aMapLocation.getLongitude();//获取经度
                    Log.e("myLocation:", latitude + "," + longitude);

                    aMapLocation.getAccuracy();//获取精度信息
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date(aMapLocation.getTime());
                    df.format(date);//定位时间
                } else {
                    String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                    Log.e("AmapErr", errText);
                    //如果程序没有定位权限
                    if (aMapLocation.getErrorCode() == 12) {
                        DialogTemplet dialogTemplet = new DialogTemplet(RouteActivity.this, false,
                                "请在 权限管理 打开 定位权限！", "", "取消", "去设置");
                        dialogTemplet.setRightClick(new DialogTemplet.DialogRightButtonListener() {
                            @Override
                            public void rightClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                            }
                        });
                        dialogTemplet.show();

                    }
                }
            }
        }
    };

    private View markView;
    private void createMyLocationBitmap(String headUrl){
        if(markView == null)
            markView = View.inflate(this,R.layout.marker_layout_route, null);
        ImageView headImg = (ImageView) markView.findViewById(R.id.headImg);
        ImageDisplayTools.displayHeadImage(headUrl, headImg,new ImageDisplayTools.LoadingCompleteListener(){
            @Override
            public void onLoadingComplete() {
                myLocationBitmap = OSUtil.layoutToBitmap(markView);
            }
        });
    }

    private Bitmap smallBitmap;
    private LatLng latLng1;
    private LatLng latLng2;
    private void addMarkersToMap(){
        if(list == null || list.size()==0) return;
        gaodeMapView.getaMap().clear(true);
        if(smallBitmap == null) {
            smallBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_marker_route_small_day);
        }

        for (int i = 0; i < list.size(); i++) {
            if(list.get(i) == null){
                list.remove(i);
                continue;
            }
            LatLonPoint bean = new LatLonPoint(list.get(i).getLocations()[0], list.get(i).getLocations()[1]);
            addMarker(smallBitmap, list.get(i));
            if(latLng1 == null)
                latLng1 = new LatLng(bean.getLatitude(), bean.getLongitude());
            if(latLng2 == null)
                latLng2 = new LatLng(bean.getLatitude(), bean.getLongitude());

            if(latLng1.latitude > bean.getLatitude()){
                latLng1 = new LatLng(bean.getLatitude(), latLng1.longitude);
            }
            if(latLng1.longitude > bean.getLongitude()){
                latLng1 = new LatLng(latLng1.latitude, bean.getLongitude());
            }

            if(latLng2.latitude < bean.getLatitude()){
                latLng2 = new LatLng(bean.getLatitude(), latLng2.longitude);
            }
            if(latLng2.longitude < bean.getLongitude()){
                latLng2 = new LatLng(latLng2.latitude, bean.getLongitude());
            }
        }
    }

    private void addMarker(Bitmap bitmap, PlanEntity.PlanLocation bean){
        MarkerOptions markerOption = new MarkerOptions().anchor(0.5f, 1.0f)
                .position(new LatLng(bean.getLocations()[0], bean.getLocations()[1]))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .setFlat(true)
                .setGps(true)
                .title(bean.getName())
                .snippet(bean.getContent()) // 点标记的内容
                .draggable(false);
        Marker marker = gaodeMapView.getaMap().addMarker(markerOption);
        marker.setObject(bean);
    }

    public void getData() {
        Map<String, Object> map = new HashMap<String, Object>();
        String url = "";
        if(!TextUtils.isEmpty(journeyId)){
            url = ShopConstant.PLAN_INFO;
            map.put("journeyId", journeyId);
        }else if(!TextUtils.isEmpty(orderId)){
            url = ShopConstant.PLAN_INFO_ORDER;
            map.put("ordersId", orderId);
        }
        NetWorkUtil.postForm(RouteActivity.this, url, new MResponseListener() {

            @Override
            protected void onDataFine(JSONObject data) {
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<PlanEntity>() {}.getType();
                PlanEntity entity = gson.fromJson(data.toString(), type);

                entity.setBackground("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1506074708131&di=1deed3781eef1a0123d32bbce14defb2&imgtype=0&src=http%3A%2F%2Fpic67.nipic.com%2Ffile%2F20150514%2F21036787_181947848862_2.jpg");
                entity.setPhoto("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1506074799100&di=e7934785633cc34811949296f65517b6&imgtype=0&src=http%3A%2F%2Fm.3fantizi.com%2Farticle%2Fpic%2FTX11164_05.jpg");
                list.clear();
                list.addAll(entity.getLocationList());
                addMarkersToMap();
            }


            @Override
            public void onErrorResponse(VolleyError error) {
                TravelUtil.showToast(R.string.net_fail, RouteActivity.this);
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                super.onErrorNotZero(error, msg);
                TravelUtil.showToast(msg);
            }
        }, map);
    }

}