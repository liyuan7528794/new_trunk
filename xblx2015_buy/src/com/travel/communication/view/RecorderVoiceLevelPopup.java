package com.travel.communication.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;

/**
 * 聊天界面的， 录音时的显示音量大小的界面
 * Created by ldkxingzhe on 2017/1/18.
 */
public class RecorderVoiceLevelPopup {
    @SuppressWarnings("unused")
    private static final String TAG = "RecorderVoiceLevelPopup";

    private PopupWindow mPopupWindow;
    private ImageView mImageView;
    private TextView tv_timer;
    private final int[] VOICE_DRAWABLE = {
            R.drawable.microphone1, R.drawable.microphone2, R.drawable.microphone3,
            R.drawable.microphone4, R.drawable.microphone4, R.drawable.microphone5
    };

    public void show(@NonNull Context context, @NonNull View anchorView) {
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow();
            mPopupWindow.setWidth(OSUtil.dp2px(context, 130));
            mPopupWindow.setHeight(OSUtil.dp2px(context, 130));
            mPopupWindow.setTouchable(false);
            mPopupWindow.setFocusable(false);

            View rootView = LayoutInflater.from(context).inflate(R.layout.popwindow_recorder_voice_level, null);
            mImageView = (ImageView) rootView.findViewById(R.id.iv_recorder_voice);
            tv_timer = (TextView) rootView.findViewById(R.id.tv_timer);
            mPopupWindow.setContentView(rootView);
        }

        if (mPopupWindow.isShowing())
            return;
        mPopupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
    }

    public void setVoiceLevel(int level) {
        level = level - 1;
        if (level >= VOICE_DRAWABLE.length)
            level = VOICE_DRAWABLE.length - 1;
        if (level < 0)
            level = 0;
        MLog.d(TAG, "setVoiceLevel %d.", level);
        mImageView.setImageResource(VOICE_DRAWABLE[level]);
    }

    public void setVoiceTimer(int second) {
        if (second > 50)
            tv_timer.setText("您还可以说 " + (60 - second) + " 秒");
        else
            tv_timer.setText("手指上滑，取消发送");
    }

    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }
}
