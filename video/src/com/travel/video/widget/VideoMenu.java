package com.travel.video.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.video.layout.VideoMenuPopupWindow;

/**
 * Created by Administrator on 2017/7/28.
 */

public class VideoMenu extends RelativeLayout{
    private Context mContext;
    private View rootView;
    private TextView textView;
    private ImageView imageView;
    private VideoMenuPopupWindow popupWindow;
    private int activityId = -1;

    public VideoMenu(Context context) {
        this(context, null);
    }
    public VideoMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public VideoMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;

        imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.play_ico_release);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow = new VideoMenuPopupWindow(mContext, new VideoMenuPopupWindow.ClickProductItemListener() {

                    @Override
                    public void notifyIntentActivity() {

                    }
                });
                popupWindow.setActivityId(activityId);
            }
        });
        LayoutParams param = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(imageView, param);

        /*initView();
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(rootView, param);*/
    }

    private void initView() {
        rootView = LayoutInflater.from(mContext).inflate(R.layout.video_menu, null);
        imageView = (ImageView) rootView.findViewById(R.id.go_live);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow = new VideoMenuPopupWindow(mContext, new VideoMenuPopupWindow.ClickProductItemListener() {

                    @Override
                    public void notifyIntentActivity() {

                    }
                });
                popupWindow.setActivityId(activityId);
            }
        });
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }
}
