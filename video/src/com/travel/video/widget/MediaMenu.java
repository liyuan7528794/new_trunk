package com.travel.video.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.OSUtil;
import com.travel.video.layout.MenuPopupView;
import com.travel.video.tools.LiveUtils;

/**
 * Created by Administrator on 2017/1/22.
 */

public class MediaMenu extends LinearLayout{
    private MediaMenu menu;
    private RelativeLayout relativeLayout;
    private ImageView iv;
    private ImageView iv_vote;
    private ImageView iv_live;
    public MediaMenu(Context context) {
        this(context, null);
    }
    public MediaMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public MediaMenu(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        menu = this;
        relativeLayout = new RelativeLayout(getContext());
        LinearLayout.LayoutParams reParams = new LinearLayout.LayoutParams(OSUtil.dp2px(getContext(), 272), OSUtil.dp2px(getContext(), 68));
        addView(relativeLayout, reParams);

        // 取证按钮
        iv_vote = new ImageView(getContext());
        RelativeLayout.LayoutParams voteParams = new RelativeLayout.LayoutParams(OSUtil.dp2px(getContext(), 68), OSUtil.dp2px(getContext(), 68));
        voteParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        relativeLayout.addView(iv_vote, voteParams);

        // 取证提示按钮
        iv = new ImageView(getContext());
        RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(OSUtil.dp2px(getContext(), 204), OSUtil.dp2px(getContext(), 54));
        ivParams.setMargins(0, OSUtil.dp2px(getContext(), 7), 0, 0);
        relativeLayout.addView(iv, ivParams);

        // 直播按钮
        iv_live = new ImageView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(OSUtil.dp2px(getContext(), 68), OSUtil.dp2px(getContext(), 68));
        params.setMargins(0, OSUtil.dp2px(getContext(), 3), 0, 0);
        params.gravity = Gravity.RIGHT;
        addView(iv_live, params);

        iv_vote.setImageResource(R.drawable.menu_packup);
        iv_live.setImageResource(R.drawable.menu_live);
        iv.setImageResource(R.drawable.play_pic_noticean);
        iv_vote.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new MenuPopupView(context, new MenuPopupView.Listener() {
                    @Override
                    public void hide() {
                        iv_vote.setVisibility(View.VISIBLE);
                    }
                }).show(menu);
                iv_vote.setVisibility(View.GONE);
            }
        });

        iv_live.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                LiveUtils.GoLiveClick((Activity) getContext(), bundle);
            }
        });

        hideVoteMenu();
    }

    public void hideVoteMenu(){
        relativeLayout.setVisibility(View.GONE);
    }

    public void showVoteMenu(){
        relativeLayout.setVisibility(View.VISIBLE);
        start();

    }

    private boolean isEnd = true;
    private void start(){
        if (!isEnd) return;
        iv.setAlpha(1F);
        isEnd = false;

        final ValueAnimator tabAnimator = ValueAnimator.ofFloat(1.0F, 0.0F);
        tabAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                iv.setAlpha((Float) (animation.getAnimatedValue()));
            }

        });
        tabAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isEnd = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tabAnimator.setDuration(300).start();
            }
        },2000);
    }
}
