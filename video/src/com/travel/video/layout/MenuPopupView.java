package com.travel.video.layout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.ctsmedia.hltravel.R;
import com.travel.activity.OneFragmentActivity;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;

/**
 * Created by Administrator on 2017/1/22.
 */

public class MenuPopupView extends PopupWindow implements View.OnClickListener{
    private static final String TAG = "BeautifyPopupView";

    private Context mContext;
    private View mRootView;
    private Listener mListener;
    private RelativeLayout rl_contrainer;
    private LinearLayout ll_container;
    private ImageView menu_close, menu_voice, menu_video, menu_photo, menu_live;

    public interface Listener{
        void hide();
    }

    public MenuPopupView(Context context, Listener listener){
        mListener = listener;
        mContext = context;
        initView();
        setContentView(mRootView);
        setClickListener();
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void initView() {
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.pop_menu_layout, null);
        rl_contrainer = (RelativeLayout) mRootView.findViewById(R.id.rl_contrainer);
        ll_container = (LinearLayout) mRootView.findViewById(R.id.ll_container);
        menu_close = (ImageView) mRootView.findViewById(R.id.menu_close);
        menu_voice = (ImageView) mRootView.findViewById(R.id.menu_voice);
        menu_video = (ImageView) mRootView.findViewById(R.id.menu_video);
        menu_photo = (ImageView) mRootView.findViewById(R.id.menu_photo);
    }

    @Override
    public void onClick(View v) {
        mListener.hide();
        dismiss();

        if (!UserSharedPreference.isLogin()) {
            Intent intent = new Intent();
            intent.setAction("com.travel.login");
            mContext.startActivity(intent);
            return;
        }

        Bundle bundle = new Bundle();
        Intent intent = new Intent(mContext,OneFragmentActivity.class);
        switch (v.getId()){
            case R.id.rl_contrainer :

                break;
            case R.id.menu_close :

                break;
            case R.id.menu_voice :
                bundle.putInt("type", 1);
                intent.putExtra("class", "com.travel.localfile.CameraFragment");
                intent.putExtra("bundle", bundle);
                mContext.startActivity(intent);
                break;
            case R.id.menu_video :
                bundle.putInt("type", 2);
                intent.putExtra("class", "com.travel.localfile.NewCameraFragment");
                intent.putExtra("bundle", bundle);
                mContext.startActivity(intent);
                break;
            case R.id.menu_photo :
                bundle.putInt("type", 0);
                intent.putExtra("class", "com.travel.localfile.CameraFragment");
                intent.putExtra("bundle", bundle);
                mContext.startActivity(intent);
                break;
        }
    }

    private void setClickListener() {
        rl_contrainer.setOnClickListener(this);
        menu_close.setOnClickListener(this);
        menu_voice.setOnClickListener(this);
        menu_video.setOnClickListener(this);
        menu_photo.setOnClickListener(this);
    }


    public void show(View parent){
        int[] location = new int[2];
        parent.getLocationOnScreen(location);
        ll_container.setX(parent.getX() + OSUtil.dp2px(mContext, 204));
        int magin = OSUtil.getScreenHeight() - (int)parent.getY() - (parent.getHeight()-OSUtil.dp2px(mContext,3))/2;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ll_container.getLayoutParams();
        params.bottomMargin = magin;
        showAtLocation(parent, Gravity.RIGHT, 0, 0);
    }

}
