package com.travel.video.layout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.PopWindowUtils;
import com.travel.video.adapter.BarrageAdapter;
import com.travel.video.bean.BarrageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/22.
 */
public class VideoPopWindow extends PopupWindow implements View.OnClickListener{
    private static final String TAG = "BeautifyPopupView";
    private Activity activity;
    private PopWindowListener popWindowListener;

    private View mRootView;
    private RelativeLayout videoContain;
    private RelativeLayout barrageSendLayout;
    private ImageView headImg;
    private ImageView changeCamera, changeLight, closeVideo;
    private ImageView zoom;
    private ImageView chat, shield, share;
    private TextView nickName;
    private ListView barrageListView;
    private Button barrageSendButton;
    private EditText barrageEdit;

    private View videoView;

    private BarrageAdapter barrageAdapter;
    private List<BarrageInfo> barrageInfos;

    public interface PopWindowListener{
        public void changeCamera();
        public void changeLight();
        public void closeVideo();
        public void zoom();
        public void sendBarrage(String barrageContent);
    }

    public VideoPopWindow(Context context, PopWindowListener popWindowListener){
        activity = (Activity) context;
        this.popWindowListener = popWindowListener;
        mRootView = LayoutInflater.from(context).inflate(R.layout.video_popwindow_layout, null);
        initView();
        setListener();
        setContentView(mRootView);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                MLog.v(TAG, "onGlobalLayout");
                int rootViewHeight = mRootView.getHeight();
                TranslateAnimation animation = new TranslateAnimation(0, 0, rootViewHeight, 0);
                animation.setDuration(500);
                mRootView.startAnimation(animation);
                mRootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void initView() {
        videoContain = (RelativeLayout) mRootView.findViewById(R.id.video_contain);
        barrageSendLayout = (RelativeLayout) mRootView.findViewById(R.id.barrage_send_layout);
        headImg = (ImageView) mRootView.findViewById(R.id.head_img);
        changeCamera = (ImageView) mRootView.findViewById(R.id.change_camera);
        changeLight = (ImageView) mRootView.findViewById(R.id.change_light);
        changeLight.setVisibility(View.GONE);
        closeVideo = (ImageView) mRootView.findViewById(R.id.close_video);
        zoom = (ImageView) mRootView.findViewById(R.id.iv_zoom);
        chat = (ImageView) mRootView.findViewById(R.id.chat_write);
        shield = (ImageView) mRootView.findViewById(R.id.shield);
        share = (ImageView) mRootView.findViewById(R.id.share);
        nickName = (TextView) mRootView.findViewById(R.id.nick_name);
        barrageListView = (ListView) mRootView.findViewById(R.id.barrageListView);
        barrageSendButton = (Button) mRootView.findViewById(R.id.barrage_send_button);
        barrageEdit = (EditText) mRootView.findViewById(R.id.barrage_content_edit);

        barrageInfos = new ArrayList<BarrageInfo>();
        barrageAdapter = new BarrageAdapter(activity,barrageInfos);
        barrageListView.setAdapter(barrageAdapter);
    }

    private void setListener() {
        changeCamera.setOnClickListener(this);
        changeLight.setOnClickListener(this);
        closeVideo.setOnClickListener(this);
        zoom.setOnClickListener(this);
        chat.setOnClickListener(this);
        shield.setOnClickListener(this);
        share.setOnClickListener(this);
        barrageSendButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.video_contain :
                OSUtil.hideKeyboard(activity);
                break;
            case R.id.chat_write :
                barrageSendLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.shield :
                shield();
                break;
            case R.id.share :
//                OSUtil.showShare("", "", "", "", "", "", activity);
                break;
            case R.id.change_camera :
                popWindowListener.changeCamera();
                break;
            case R.id.change_light :
                popWindowListener.changeLight();
                break;
            case R.id.close_video :
                popWindowListener.closeVideo();
                break;
            case R.id.iv_zoom :
                popWindowListener.zoom();
                break;
            case R.id.barrage_send_button :
                popWindowListener.sendBarrage(barrageEdit.getText().toString());
                barrageSendLayout.setVisibility(View.VISIBLE);
                barrageEdit.setText("");
                break;
        }
    }



    private void shield() {
        boolean isShow = (chat.getVisibility() == View.GONE);

        headImg.setVisibility(isShow ? View.VISIBLE : View.GONE);
        changeCamera.setVisibility(isShow ? View.VISIBLE : View.GONE);
//        changeLight.setVisibility(isShow ? View.VISIBLE : View.GONE);
        zoom.setVisibility(isShow ? View.VISIBLE : View.GONE);
        chat.setVisibility(isShow ? View.VISIBLE : View.GONE);
        share.setVisibility(isShow ? View.VISIBLE : View.GONE);
        nickName.setVisibility(isShow ? View.VISIBLE : View.GONE);
        barrageListView.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void show(){
        showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @Override
    public void dismiss() {
        Animation dismissAnimation = new TranslateAnimation(0, 0, 0, mRootView.getHeight());
        dismissAnimation.setDuration(500);
        dismissAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                VideoPopWindow.super.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mRootView.startAnimation(dismissAnimation);
    }

    public void initData(String name, String headImage, String shareUrl){
        barrageInfos.clear();
        nickName.setText(name);
        ImageDisplayTools.displayHeadImage(headImage, headImg);
    }

    public void isHost(boolean isHost){
        changeCamera.setVisibility(isHost ? View.VISIBLE : View.GONE);
        changeLight.setVisibility(isHost ? View.VISIBLE : View.GONE);
    }

    public void addVideoView(View view){
        videoView = view;
        videoContain.addView(view,new RelativeLayout.LayoutParams(OSUtil.dp2px(activity,200), OSUtil.dp2px(activity,300)));
    }

    public View removeVideoView(){
        videoContain.removeView(videoView);
        return videoView;
    }

    public void setBarrageInfos(BarrageInfo barrageInfo){
        barrageInfos.add(barrageInfo);
        barrageAdapter.notifyDataSetChanged();
    }

}

