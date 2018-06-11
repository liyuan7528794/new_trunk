package com.travel.map.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.nearby.NearbyInfo;
import com.amap.api.services.nearby.NearbySearch;
import com.amap.api.services.nearby.NearbySearchFunctionType;
import com.amap.api.services.nearby.NearbySearchResult;
import com.amap.api.services.nearby.UploadInfo;
import com.ctsmedia.hltravel.R;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.map.bean.MarkerBean;
import com.travel.VideoConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapHelper {
	private final static String TAG = "MapHelper";
	private Context context;
	private List<MarkerBean> list;
	private HelperListener listener;
	private NearbySearch mNearbySearch;
	public MapHelper(Context context,HelperListener listener) {
		this.context = context;
		this.listener = listener;
		list = new ArrayList<MarkerBean>();
		NearbySearch mNearbySearch = NearbySearch.getInstance(context.getApplicationContext());
		//设置附近监听
		NearbySearch.getInstance(context.getApplicationContext()).addNearbyListener(new MyNearByListener());
	}
	public interface HelperListener{
		void GetListSeccess(List<VideoInfoBean> list);
	}
	
	/**
	 * 在地图上添加marker
	 */
	private void addMarkersToMap(final AMap amap,List<VideoInfoBean> beans){
		amap.clear(true);
		if(beans == null || beans.size()==0) return;
		
		for (int i = 0; i < beans.size(); i++) {
				
			final VideoInfoBean bean = beans.get(i);
			
			final View view = View.inflate(context,R.layout.marker_layout, null);
			
			TextView text = (TextView) view.findViewById(R.id.name);
			if(bean.getPersonalInfoBean().getUserName()!=null && bean.getPersonalInfoBean().getUserName().length()>8){
				text.setText(bean.getPersonalInfoBean().getUserName().substring(0, 8)+"...");
			}else{
				text.setText(bean.getPersonalInfoBean().getUserName());
			}
			ImageView headImg = (ImageView) view.findViewById(R.id.headImg);
			if(bean.getVideoStatus() == 1){
				ImageDisplayTools.displayHeadImage(bean.getPersonalInfoBean().getUserPhoto(), headImg,new ImageDisplayTools.LoadingCompleteListener(){
					@Override
					public void onLoadingComplete() {

						Bitmap bitmap = convertViewToBitmap(view);
						MarkerOptions markerOption = new MarkerOptions().anchor(0.5f, 1.0f)
								.position(new LatLng(Double.valueOf(bean.getLatitude()), Double.valueOf(bean.getLongitude())))
								.title(bean.getPersonalInfoBean().getUserName())
								.snippet(bean.getVideoTitle()).draggable(false);
						markerOption.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
//					markerOptionlst.add(markerOption);
						markerOption.setFlat(true);
						markerOption.setGps(true);
//					markerOptionlst.add(markerOption);
						Marker marker = amap.addMarker(markerOption);

						marker.setObject(bean);
					}
				});
			}else{
				headImg.setImageResource(R.drawable.icon_map_player);

				Bitmap bitmap = convertViewToBitmap(view);
				MarkerOptions markerOption = new MarkerOptions().anchor(0.5f, 1.0f)
						.position(new LatLng(Double.valueOf(bean.getLatitude()), Double.valueOf(bean.getLongitude())))
						.title(bean.getPersonalInfoBean().getUserName())
						.snippet(bean.getVideoTitle()).draggable(false);
				markerOption.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
//					markerOptionlst.add(markerOption);
				markerOption.setFlat(true);
				markerOption.setGps(true);
//					markerOptionlst.add(markerOption);
				Marker marker = amap.addMarker(markerOption);

				marker.setObject(bean);
			}

		}
//		List<Marker> markerlst = amap.addMarkers(markerOptionlst, true);
	}

	/**
	 * 将view'生成Bitmap
	 * @param view
	 * @return
	 */
	private Bitmap convertViewToBitmap(View view) {

		  view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

		  view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

		  view.buildDrawingCache();

		  Bitmap bitmap = view.getDrawingCache();

		  return bitmap;
	}
	
	/**
	 * 从服务短获取mark数据列表
	 */
	public void getNetMarkersData(final AMap amap,int type){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("times", 1);
		map.put("statusShow", 1);
//		map.put("type", 1);
		
		System.out.println("map:"+map.toString());
		NetWorkUtil.postForm(context, VideoConstant.VIDEO_LIST, new MResponseListener() {
			@Override
			protected void onDataFine(JSONArray data) {
				
				List<VideoInfoBean> list = new ArrayList<VideoInfoBean>();
				try {
					for (int i = 0; i < data.length(); i++) {
						JSONObject live = data.getJSONObject(i);
						VideoInfoBean bean = new VideoInfoBean().getVideoInfoBean(live);
						
						list.add(bean);
					}
					addMarkersToMap(amap, list);
					listener.GetListSeccess(list);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
		}, map);
		
//		addMarkersToMap(amap, new MarkerBean().getMarkers());
	}
	

    private static boolean outOfChina(double lat, double lon) {
            if (lon < 72.004 || lon > 137.8347)
                    return true;
            if (lat < 0.8293 || lat > 55.8271)
                    return true;
            return false;
    }

	public void upLoadData(LatLonPoint latLonPoint,String userId){
		//构造上传位置信息
		UploadInfo loadInfo = new UploadInfo();
		//设置上传位置的坐标系支持AMap坐标数据与GPS数据
		loadInfo.setCoordType(NearbySearch.AMAP);
		//设置上传数据位置,位置的获取推荐使用高德定位sdk进行获取
		loadInfo.setPoint(latLonPoint);
		//设置上传用户id
		loadInfo.setUserID(userId);
		//调用异步上传接口
		NearbySearch.getInstance(context.getApplicationContext())
				.uploadNearbyInfoAsyn(loadInfo);
	}

	public void getNearByData(LatLonPoint latLonPoint){
		//设置搜索条件
		NearbySearch.NearbyQuery query = new NearbySearch.NearbyQuery();
		//设置搜索的中心点
		query.setCenterPoint(latLonPoint);
		//设置搜索的坐标体系
		query.setCoordType(NearbySearch.AMAP);
		//设置搜索半径
		query.setRadius(100000000);
		//设置查询的时间
		query.setTimeRange(3600*24);
		//设置查询的方式驾车还是距离
		query.setType(NearbySearchFunctionType.DRIVING_DISTANCE_SEARCH);
		//调用异步查询接口
		NearbySearch.getInstance(context.getApplicationContext())
				.searchNearbyInfoAsyn(query);

	}

	public void destroy(){
		NearbySearch.destroy();
	}

	public void clearUser(String userId){
		//获取附近实例，并设置要清楚用户的id
		NearbySearch.getInstance(context.getApplicationContext()).setUserID(userId);
		//调用异步清除用户接口
		NearbySearch.getInstance(context.getApplicationContext())
				.clearUserInfoAsyn();
	}
    private class MyNearByListener implements NearbySearch.NearbyListener{

		@Override
		public void onUserInfoCleared(int i) {
			Log.e(TAG,"onUserInfoCleared:"+i);
		}

		@Override
		public void onNearbyInfoSearched(NearbySearchResult nearbySearchResult, int resultCode) {
			//搜索周边附近用户回调处理
			if(resultCode == 1000){
				if (nearbySearchResult != null
						&& nearbySearchResult.getNearbyInfoList() != null
						&& nearbySearchResult.getNearbyInfoList().size() > 0) {
					for (int i = 0; i<nearbySearchResult.getNearbyInfoList().size() ;i++) {
						NearbyInfo nearbyInfo = nearbySearchResult.getNearbyInfoList().get(i);
						Log.e(TAG, "周边搜索结果为 " +
								"first：" + nearbyInfo.getUserID() + "  " + nearbyInfo.getDistance() + "  "
								+ nearbyInfo.getDrivingDistance() + "  " + nearbyInfo.getTimeStamp() + "  " +
								nearbyInfo.getPoint().toString());
					}
				} else {
					Log.e(TAG,"周边搜索结果为空");
				}
			}
			else{
				Log.e(TAG,"周边搜索出现异常，异常码为："+resultCode);
			}
		}

		@Override
		public void onNearbyInfoUploaded(int i) {
			Log.e(TAG,"onNearbyInfoUploaded："+i);
		}
	}

}
