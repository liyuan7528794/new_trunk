package com.travel.video.layout;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.model.Marker;
import com.ctsmedia.hltravel.R;
import com.travel.layout.BaseBellowPopupWindow;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.widget.MyListView;
import com.travel.video.bean.RouteBean;
import com.travel.video.bean.XpaiCofig;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.ctsmedia.hltravel.R.id.tv_go;
import static com.travel.video.bean.XpaiCofig.latitude;

/**
 * Created by Administrator on 2017/9/22.
 */

public class RouteMapPopupWindow extends BaseBellowPopupWindow{
    private View rootView;
    private Context context;
    private Marker marker;
    private ListView listView;
    private MyAdapter adapter;
    private List<String> list;
    private TextView tv_notify;
    private TextView tv_cacle;

    private String[] paks = new String[]{"com.baidu.BaiduMap"        //百度
            , "com.autonavi.minimap" // 高德
            , "com.google.android.apps.maps"}; // 谷歌

    private RouteBean startBean;
    private RouteBean endBean;

    private RouteMarkerPopupWindowListenre listener;
    public interface RouteMarkerPopupWindowListenre{
        void hideWindow(Marker marker);
    }

    public RouteMapPopupWindow(Context context, Marker marker) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.marker = marker;
        if(marker == null) return;
        endBean = new RouteBean();
        endBean.setAddress(marker.getTitle());
        endBean.setLatitude(marker.getPosition().latitude);
        endBean.setLongitude(marker.getPosition().longitude);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.route_map_window, null);
        initView();
        SetContentView(rootView);
        initData();

        show();
    }

    private void initView() {
        listView = (ListView) rootView.findViewById(R.id.mapListView);
        tv_cacle = (TextView) rootView.findViewById(R.id.tv_cacle);
        tv_notify = (TextView) rootView.findViewById(R.id.tv_notify);
        list = new ArrayList<>();
        adapter = new MyAdapter();
        listView.setAdapter(adapter);
    }

    private void initData() {
        tv_cacle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (list.get(position)){
                    case "com.baidu.BaiduMap" :
                        startNative_Baidu(endBean);
                        break;
                    case "com.autonavi.minimap" :
                        startNative_Gaode(endBean);
                        break;
                    case "com.google.android.apps.maps" :
                        startNative_Google(endBean);
                        break;
                }

                dismiss();
            }
        });

        list.clear();
        for (int i = 0; i < paks.length; i++){
            if(OSUtil.isAvilible(context, paks[i])){
                list.add(paks[i]);
            }
        }
        if(list.size() > 0) {
            tv_notify.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }else{
            tv_notify.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
    }

    class MyAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        public MyAdapter (){
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.adapter_local_map, null);
                holder = new ViewHolder();
                holder.nickname = (TextView) convertView.findViewById(R.id.tv_map);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            switch (list.get(position)){
                case "com.baidu.BaiduMap" :
                    holder.nickname.setText("百度");
                    break;
                case "com.autonavi.minimap" :
                    holder.nickname.setText("高德");
                    break;
                case "com.google.android.apps.maps" :
                    holder.nickname.setText("谷歌");
                    break;
            }

            return convertView;
        }
    }

    private class ViewHolder {
        public TextView nickname;
    }


    private void startNative_Baidu(RouteBean loc2){
        if (loc2==null) {
            return;
        }
        if (loc2.getAddress()==null || "".equals(loc2.getAddress())) {
            loc2.setAddress("目的地");
        }
        if(OSUtil.isAvilible(context,"com.baidu.BaiduMap")) {
            try {
                Intent intent = Intent.getIntent(
                        "intent://map/direction?"
                                + (XpaiCofig.latitude==-1 ? ("origin=latlng:" + XpaiCofig.latitude + "," + XpaiCofig.longitude + "|name:" + "我的位置") : "")
                                + "&destination=latlng:" + loc2.getLatitude() + "," + loc2.getLongitude() + "|name:" + loc2.getAddress()
                                + "&mode=driving&src=" + "红了旅行" + "|CC房车-车主#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "地址解析错误", Toast.LENGTH_SHORT).show();
            }
        }else{//未安装
            //market为路径，id为包名
            //显示手机上所有的market商店
            Toast.makeText(context, "您尚未安装百度地图", Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("market://details?id=com.baidu.BaiduMap");
            Intent intents = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intents);
        }
    }

    private void startNative_Gaode(RouteBean loc){
        if (loc==null) {
            return;
        }
        if (loc.getAddress()==null || "".equals(loc.getAddress())) {
            loc.setAddress("目的地");
        }

        if (OSUtil.isAvilible(context, "com.autonavi.minimap")) {
            try {
                Intent intent = new Intent("android.intent.action.VIEW",
                        android.net.Uri.parse("androidamap://navi?sourceApplication=红了旅行&poiname=" + loc.getAddress() + "&lat=" + loc.getLatitude() + "&lon=" + loc.getLongitude() + "&dev=1&style=2"));
                intent.setPackage("com.autonavi.minimap");
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "地址解析错误", Toast.LENGTH_SHORT).show();
            }
        }else{//未安装
            Toast.makeText(context, "您尚未安装高德地图", Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("market://details?id=com.autonavi.minimap");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }
    }

    private void startNative_Google(RouteBean loc){
        if (OSUtil.isAvilible(context,"com.google.android.apps.maps")) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+loc.getLatitude()+","+loc.getLongitude() +", + Sydney +Australia");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            context.startActivity(mapIntent);
        }else {
            Toast.makeText(context, "您尚未安装谷歌地图", Toast.LENGTH_LONG).show();

            Uri uri = Uri.parse("market://details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }
    }

    private void startGoogle(){
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://ditu.google.cn/maps?hl=zh&mrt=loc&q=31.1198723,121.1099877(上海青浦大街100号)"));

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        intent.setClassName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity");

        context.startActivity(intent);
    }
}

