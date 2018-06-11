package com.travel.video.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.video.adapter.BarrageAdapter;
import com.travel.video.bean.BarrageInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */

public class VoteLVBFullScreenView extends RelativeLayout implements View.OnClickListener{
    private static final String TAG = "VoteLVBFullScreenLayout";
    private Activity activity;
    private VoteLVBFullScreenListener listener;

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

    public interface VoteLVBFullScreenListener{
        public void changeCamera();
        public void changeLight();
        public void closeVideo();
        public void zoom();
        public void sendBarrage(String barrageContent);
    }

    public VoteLVBFullScreenView(Context context){
        this(context, null);
    }
    public VoteLVBFullScreenView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public VoteLVBFullScreenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        activity = (Activity) context;

        initView();
        setListener();
    }

    private void initView() {
        mRootView = LayoutInflater.from(activity).inflate(R.layout.video_popwindow_layout, null);
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

        addView(mRootView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,OSUtil.getScreenHeight()-OSUtil.dp2px(activity,44)-OSUtil.getStatusHeight(activity)));
    }

    private void setListener() {
        videoContain.setOnClickListener(this);
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
                barrageSendLayout.setVisibility(View.GONE);
                break;
            case R.id.chat_write :
                barrageSendLayout.setVisibility(View.VISIBLE);
                OSUtil.showKeyboard(activity);
                break;
            case R.id.shield :
                shield();
                break;
            case R.id.share :
//                OSUtil.showShare("", "", "", "", "", "", activity);
                break;
            case R.id.change_camera :
                listener.changeCamera();
                break;
            case R.id.change_light :
                listener.changeLight();
                break;
            case R.id.close_video :
                listener.closeVideo();
                break;
            case R.id.iv_zoom :
                listener.zoom();
                break;
            case R.id.barrage_send_button :
                listener.sendBarrage(barrageEdit.getText().toString());
                barrageSendLayout.setVisibility(View.VISIBLE);
                barrageEdit.setText("");
                break;
        }
    }

    public void setCallBackListener(VoteLVBFullScreenListener listener){
        this.listener = listener;
    }


    private void shield() {
        boolean isShow = (zoom.getVisibility() == View.GONE);

        headImg.setVisibility(isShow ? View.VISIBLE : View.GONE);
//        changeCamera.setVisibility(isShow ? View.VISIBLE : View.GONE);
//        changeLight.setVisibility(isShow ? View.VISIBLE : View.GONE);
        zoom.setVisibility(isShow ? View.VISIBLE : View.GONE);
//        chat.setVisibility(isShow ? View.VISIBLE : View.GONE);
//        share.setVisibility(isShow ? View.VISIBLE : View.GONE);
//        nickName.setVisibility(isShow ? View.VISIBLE : View.GONE);
        barrageListView.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }


    public void initData(String name, String headImage, String shareUrl){
        barrageInfos.clear();
        nickName.setText(name);
        ImageDisplayTools.displayHeadImage(headImage, headImg);
    }

    public void isHost(boolean isHost){
        changeCamera.setVisibility(isHost ? View.VISIBLE : View.GONE);
        closeVideo.setVisibility(isHost ? View.VISIBLE : View.GONE);
//        changeLight.setVisibility(isHost ? View.VISIBLE : View.GONE);
    }

    public void addVideoView(View view){
        videoView = view;
        videoContain.addView(view,new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
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

