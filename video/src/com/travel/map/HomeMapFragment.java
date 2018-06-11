package com.travel.map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.ctsmedia.hltravel.R;
import com.travel.activity.OneFragmentActivity;
import com.travel.bean.NotifyBean;
import com.travel.bean.VideoInfoBean;
import com.travel.http_helper.SlideHelper;
import com.travel.layout.SlideShowView;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.map.utils.MapHelper;
import com.travel.map.utils.MapHelper.HelperListener;
import com.travel.map.factory.impl.GaodeMapFactory;
import com.travel.map.factory.location.impl.GaodeMapLocation;
import com.travel.map.factory.location.listener.IGaodeMapLocation;
import com.travel.map.factory.view.impl.GaodeMapView;
import com.travel.map.factory.view.listener.IGaodeMapView;
import com.travel.video.bean.XpaiCofig;
import com.travel.video.help.VideoIntentHelper;
import com.travel.video.live.HostWindowActivity;
import com.travel.video.tools.LiveUtils;
import com.travel.video.widget.menu.FilterMenu;
import com.travel.video.widget.menu.FilterMenuLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeMapFragment extends SupportMapFragment implements HelperListener {

    private GaodeMapView gaodeMapView ;
    private LocationSource.OnLocationChangedListener mListener;
    private GaodeMapLocation mapLocation;

    private View rootView;
    private Context context;

    private double latitude;
    private double longitude;

    private MapHelper mapHelper;

    private ImageView upDownButton;
    private LinearLayout activityLayout;
    private RelativeLayout reLayout;
    private SlideShowView slideshowView;
    private List<NotifyBean> activityList;
    private SlideHelper slideHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        rootView = inflater.inflate(R.layout.home_map_fragment, null);
        // 地图功能
        gaodeMapView = (GaodeMapView) new GaodeMapFactory().createMapView();
        gaodeMapView.setListener(iMapView);
        gaodeMapView.onCreate(getContext(), rootView.findViewById(R.id.map), savedInstanceState);
        // 定位功能
        mapLocation = (GaodeMapLocation) new GaodeMapFactory().createLocation();
        mapLocation.init(getContext(), iMapLocation);
        mapLocation.startLocation();

        mapHelper = new MapHelper(context, this);
        slideHelper = new SlideHelper(context, slideListener);
        initMenuView();
        initActivityView();

        return rootView;
    }

    private void initActivityView() {
        activityList = new ArrayList<>();
        upDownButton = (ImageView) rootView.findViewById(R.id.up_down_button);
        activityLayout = (LinearLayout) rootView.findViewById(R.id.layout);
        reLayout = (RelativeLayout) rootView.findViewById(R.id.activity_layout);
        upDownButton.setTag(R.drawable.activity_allow_icon);

        upDownButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (R.drawable.activity_allow_icon == (Integer) v.getTag()) {
                    upDownButton.setImageResource(R.drawable.activity_up_icon);
                    upDownButton.setTag(R.drawable.activity_up_icon);
                    reLayout.setVisibility(View.VISIBLE);
                } else {
                    upDownButton.setImageResource(R.drawable.activity_allow_icon);
                    upDownButton.setTag(R.drawable.activity_allow_icon);
                    reLayout.setVisibility(View.GONE);
                }
            }
        });
        slideshowView = (SlideShowView) rootView.findViewById(R.id.slideshowView);
        slideshowView.isMap();
        slideshowView.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                int pos = (position) % activityList.size();
                slideHelper.intentBySlide(activityList.get(pos));
            }
        });

    }

    private void initMenuView() {
        FilterMenuLayout layout5 = (FilterMenuLayout) rootView.findViewById(R.id.filter_menu);
        attachMenu(layout5);
    }

    private FilterMenu attachMenu(FilterMenuLayout layout) {
        return new FilterMenu.Builder(context)
                .addItem(R.drawable.menu_photo_icon).addItem(R.drawable.menu_video_icon)
                .addItem(R.drawable.menu_live_icon).addItem(R.drawable.menu_audio_icon)
                .attach(layout).withListener(menuListener).build();
    }

    FilterMenu.OnMenuChangeListener menuListener = new FilterMenu.OnMenuChangeListener() {
        @Override
        public void onMenuItemClick(View view, int position) {
            Bundle bundle = new Bundle();
            if (position == 0) {// 拍照
                Intent intent = new Intent(context, OneFragmentActivity.class);
                bundle.putInt("type", 0);
                intent.putExtra("class", "com.travel.localfile.CameraFragment");
                intent.putExtra("bundle", bundle);
                startActivity(intent);
            } else if (position == 1) {// 录像
                Intent intent = new Intent(context, OneFragmentActivity.class);
                bundle.putInt("type", 2);
                intent.putExtra("class", "com.travel.localfile.CameraFragment");
                intent.putExtra("bundle", bundle);
                startActivity(intent);
            } else if (position == 2) {// 直播
                LiveUtils.GoLiveClick(getActivity(), bundle);
            } else if (position == 3) {// 录音
                Intent intent = new Intent(context, OneFragmentActivity.class);
                bundle.putInt("type", 1);
                intent.putExtra("class", "com.travel.localfile.CameraFragment");
                intent.putExtra("bundle", bundle);
                startActivity(intent);
            }
        }

        @Override
        public void onMenuCollapse() {

        }

        @Override
        public void onMenuExpand() {

        }
    };

    SlideHelper.SlideHelperListener slideListener = new SlideHelper.SlideHelperListener() {
        @Override
        public void onGetSlideData(List<NotifyBean> noticeList) {
            if (noticeList == null || noticeList.size() < 1)
                return;

            if (activityList == null)
                activityList = new ArrayList<>();
            activityList.clear();
            activityList.addAll(noticeList);
            slideshowView.setList(activityList);
            slideshowView.startPlay();
            reLayout.setVisibility(View.VISIBLE);
            activityLayout.setVisibility(View.VISIBLE);
            upDownButton.setVisibility(View.VISIBLE);
            upDownButton.setImageResource(R.drawable.activity_up_icon);
        }
    };

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        // 获取轮滚图活动列表
        //		slideHelper.getSlideData(1,SlideHelper.TAG_ACTIVITYS);
        gaodeMapView.onResume();
        isPause = false;
        getMarkerData();

    }

    private boolean isPause = false;

    private void getMarkerData() {
        if (mapHelper != null) {
            mapHelper.getNetMarkersData(gaodeMapView.getaMap(), -1);
        }
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (!isPause)
                    getMarkerData();
            }
        }, 10000);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        gaodeMapView.onPause();
        isPause = true;
        iMapView.deactivate();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        gaodeMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapHelper.clearUser(UserSharedPreference.getUserId());
        mapHelper.destroy();

        gaodeMapView.onDestroy();
        mapLocation.onDestroy();
    }

    private IGaodeMapView iMapView = new IGaodeMapView() {

        @Override
        public void onPoiSearched(PoiResult poiResult, int i) {

        }

        @Override
        public void onPoiItemSearched(PoiItem poiItem, int i) {

        }

        @Override
        public View getInfoWindow(Marker marker) {
            //		View infoWindow = inflater.inflate(R.layout.custom_info_window, null);
            //		render(marker, infoWindow);
            //		return infoWindow;
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
            Intent intent = new Intent();
            VideoInfoBean bean = (VideoInfoBean) marker.getObject();
            new VideoIntentHelper(context).intentWatchVideo(bean, null);
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
                //			Log.e("坐标类型s:",amapLocation.getLocationType()+"");
                //			AMapLocation location = mapHelper.fromGpsToAmap(amapLocation);
                if (aMapLocation.getErrorCode() == 0) {
                    mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
//                    onLocationChanged(aMapLocation);// 显示系统小蓝点
                    gaodeMapView.setMyLocationStyle(R.drawable.mylocation_icon);

                    int locationType = aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    XpaiCofig.latitude = latitude = aMapLocation.getLatitude();//获取纬度
                    XpaiCofig.longitude = longitude = aMapLocation.getLongitude();//获取经度
                    mapHelper.upLoadData(new LatLonPoint(latitude, longitude), UserSharedPreference.getUserId());
                    Log.e("myLocation:", latitude + "," + longitude);
                    //		        LatLng latLng = mapHelper.transformFromWGSToGCJ(new LatLng(latitude, longitude));
                    //		        LatLng latLng = mapHelper.fromGpsToAmap(new LatLng(latitude, longitude));
                    //		        latitude = latLng.latitude;
                    //		        longitude = latLng.longitude;
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
                        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                        startActivity(intent);
                        Toast.makeText(context, "请打开定位权限！", Toast.LENGTH_SHORT).show();
                    }

                    //选择定位模式
                /*PackageManager pm = context.getPackageManager();
                boolean flag = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.ACCESS_FINE_LOCATION", "packageName"));
				if (flag) {
				//有这个权限，做相应处理
				}else {              //没有权限
					Intent intent = null;
                    // 先判断当前系统版本
                    if(android.os.Build.VERSION.SDK_INT > 10){  // 3.0以上
                        intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    }else{
                        intent = new Intent();
                        intent.setClassName("com.android.settings", "com.android.settings.WirelessSettings");
                    }
                    context.startActivity(intent);
				}*/
                }
            }
        }
    };

    @Override
    public void GetListSeccess(List<VideoInfoBean> list) {

    }
}
