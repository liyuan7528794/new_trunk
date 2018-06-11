package com.travel.video.layout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ctsmedia.hltravel.R;
import com.travel.activity.OneFragmentActivity;
import com.travel.activity.PublishTalkActivity;
import com.travel.layout.BaseBellowPopupWindow;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.video.tools.LiveUtils;

/**
 * Created by Administrator on 2017/7/28.
 */

public class VideoMenuPopupWindow extends BaseBellowPopupWindow {
    private View view;
    private Context context;
    private ClickProductItemListener listener;
    private ImageView close;
    private LinearLayout photoLL, liveLL, recordeLL;

    private int activityId = -1;

    public interface ClickProductItemListener {
        public void notifyIntentActivity();
    }

    public VideoMenuPopupWindow(Context context, ClickProductItemListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.video_menu_popwindow, null);
        initListView();
        SetContentView(view);
        show();
    }

    private void initListView() {
        close = (ImageView) view.findViewById(R.id.iv_close);
        photoLL = (LinearLayout) view.findViewById(R.id.ll_photo);
        liveLL = (LinearLayout) view.findViewById(R.id.ll_live);
        recordeLL = (LinearLayout) view.findViewById(R.id.ll_recorde);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        photoLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 发布说说
                PublishTalkActivity.startIntent(context);
                dismiss();
            }
        });

        liveLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                if(activityId != -1)
                    bundle.putString("activity_id", activityId+"");
                LiveUtils.GoLiveClick((Activity) context, bundle);
                dismiss();
            }
        });

        recordeLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UserSharedPreference.isLogin()) {
                    OSUtil.intentLogin(context);
                    return;
                }

                Bundle bundle = new Bundle();
                Intent intent = new Intent(context,OneFragmentActivity.class);
                if(activityId != -1)
                    bundle.putString("activity_id", activityId+"");
                bundle.putInt("type", 2);
                intent.putExtra("class", "com.travel.localfile.NewCameraFragment");
                intent.putExtra("bundle", bundle);
                context.startActivity(intent);

                dismiss();
            }
        });
    }

    public void hidePhoto(){
        photoLL.setVisibility(View.GONE);
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }
}
