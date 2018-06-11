package com.travel.video.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.model.Marker;
import com.ctsmedia.hltravel.R;
import com.travel.layout.BaseBellowPopupWindow;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.usercenter.entity.PlanEntity;

/**
 * Created by Administrator on 2017/9/22.
 */

public class RouteMarkerPopupWindow extends BaseBellowPopupWindow{
    private View rootView;
    private Context context;
    private Marker marker;
    private PlanEntity.PlanLocation planLocation = new PlanEntity.PlanLocation();
    private ImageView iv_cover, iv_close;
    private TextView tv_title, tv_content, tv_go;

    private RouteMarkerPopupWindowListenre listener;
    public interface RouteMarkerPopupWindowListenre{
        void hideWindow(Marker marker);
    }

    public RouteMarkerPopupWindow(Context context, Marker marker, RouteMarkerPopupWindowListenre listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.marker = marker;
        planLocation = (PlanEntity.PlanLocation) marker.getObject();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.route_marker_window, null);
        initView();
        SetContentView(rootView);
        initData();

        show();
    }

    private void initView() {
        iv_cover = (ImageView) rootView.findViewById(R.id.iv_cover);
        iv_close = (ImageView) rootView.findViewById(R.id.iv_close);
        tv_title = (TextView) rootView.findViewById(R.id.tv_title);
        tv_content = (TextView) rootView.findViewById(R.id.tv_content);
        tv_go = (TextView) rootView.findViewById(R.id.tv_go);
    }

    private void initData() {
        ImageDisplayTools.displayImage(planLocation.getImgUrl(), iv_cover);
        tv_title.setText(planLocation.getName());
        tv_content.setText(planLocation.getContent());
        tv_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RouteMapPopupWindow(context, marker);
                dismiss();
            }
        });
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
        listener.hideWindow(marker);
    }
}

