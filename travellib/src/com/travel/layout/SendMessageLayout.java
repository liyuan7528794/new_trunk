package com.travel.layout;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.travel.lib.R;

/**
 * 发送评论，点赞，分享
 * Created by wyp on 2018/3/6.
 */

public class SendMessageLayout extends LinearLayout {

    // 发评论
    private TextView edit_message;
    // 点赞
    private LinearLayout city_like_layout;
    private ImageView city_like;
    private TextView city_like_count;
    private SendListener sendListener;

    // 分享
    private RelativeLayout city_share;

    public SendMessageLayout(final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_send_message, this);
        edit_message = (TextView) findViewById(R.id.edit_message);
        city_like_layout = (LinearLayout) findViewById(R.id.city_like_layout);
        city_like = (ImageView) findViewById(R.id.city_like);
        city_like_count = (TextView) findViewById(R.id.city_like_count);
        city_share = (RelativeLayout) findViewById(R.id.city_share);
        edit_message.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputPopupWindow inputPopupWindow = new InputPopupWindow((Activity) context, "");
                inputPopupWindow.setListener(new InputPopupWindow.OnListener() {
                    @Override
                    public void onInputText(String content, String tag) {
                        if (sendListener != null) {
                            sendListener.sendMessage(content);
                        }
                    }
                });
            }
        });
        city_like_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                v.setClickable(false);
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        v.setClickable(true);
                    }
                }, 300);

                if (sendListener != null) {
                    sendListener.cityLike();
                }
            }
        });
        city_share.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendListener != null) {
                    sendListener.cityShare();
                }
            }
        });
    }

    public void setLikeContent(boolean isLike, int likeNum) {
        city_like.setImageResource(isLike ? R.drawable.cityrank_ico_fab_pre : R.drawable.cityrank_ico_fab_nor);
        city_like_count.setText(likeNum + "");
    }

    public void setLikeImg(boolean isLike) {
        city_like.setImageResource(isLike ? R.drawable.cityrank_ico_fab_pre : R.drawable.cityrank_ico_fab_nor);
    }

    public void setLikeNum(int likeNum) {
        city_like_count.setText(likeNum + "");
    }

    public void addOneLikeNum() {
        int num = 0;
        if (city_like_count.getText() != null && !city_like_count.getText().toString().isEmpty()) {
            num = Integer.parseInt(city_like_count.getText().toString());
        }
        num = num + 1;
        setLikeContent(true, num);
    }

    public void minusOneLikeNum() {
        int num = 0;
        if (city_like_count.getText() != null && !city_like_count.getText().toString().isEmpty()) {
            num = Integer.parseInt(city_like_count.getText().toString());
        }
        num = Math.max(num - 1, 0);
        setLikeContent(false, num);
    }

    public void setShareVisibility(int visibility){
        city_share.setVisibility(visibility);
    }

    public interface SendListener {
        void sendMessage(String message);

        void cityLike();

        void cityShare();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void setSendListener(SendListener sendListener) {
        this.sendListener = sendListener;
    }
}
