package com.travel.layout;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.travel.lib.R;
import com.travel.lib.utils.OSUtil;

/**
 * 黑白天转换的动画的dialog
 * Created by wyp on 2017/10/25.
 */

public class Day2NightAnimDialog extends Dialog {

    private Context context;

    // 动画相关
    private RelativeLayout rl_anim;
    private ImageView iv_oval_day, iv_sun, iv_boat, iv_star;
    private Animation animationD2NSun, animationD2NBoat, animationD2NNightStar, animationBgAlphaDay;


    public Day2NightAnimDialog(@NonNull Context context) {
        super(context, R.style.Transparent);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_anim_window);

        init();
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        day2Night();

    }

    private void init() {
        rl_anim = (RelativeLayout) findViewById(R.id.rl_anim);
        iv_oval_day = (ImageView) findViewById(R.id.iv_oval_day);
        iv_sun = (ImageView) findViewById(R.id.iv_sun);
        iv_boat = (ImageView) findViewById(R.id.iv_boat);
        iv_star = (ImageView) findViewById(R.id.iv_star);
    }

    private void day2Night() {
        rl_anim.setVisibility(View.VISIBLE);
        if (!OSUtil.isDayTheme()) {
            // 白-->黑
            rl_anim.setBackgroundColor(ContextCompat.getColor(context, R.color.white_FAFFFE));
            iv_oval_day.setImageResource(R.drawable.bg_day);
            iv_sun.setImageResource(R.drawable.icon_day_sun);
            iv_boat.setImageResource(R.drawable.icon_day_boat);
            iv_star.setImageResource(android.R.color.transparent);
            animationD2NSun = AnimationUtils.loadAnimation(context, R.anim.day_sun);
            animationD2NNightStar = AnimationUtils.loadAnimation(context, R.anim.night_star);
            animationD2NBoat = AnimationUtils.loadAnimation(context, R.anim.day_boat);
            animationBgAlphaDay = AnimationUtils.loadAnimation(context, R.anim.bg_alpha_day);
            iv_sun.startAnimation(animationD2NSun);
            animationD2NSun.setAnimationListener(animationListener);
        } else {
            // 黑-->白
            rl_anim.setBackgroundColor(ContextCompat.getColor(context, R.color.black_1D242F));
            iv_oval_day.setImageResource(R.drawable.bg_night);
            iv_sun.setImageResource(android.R.color.transparent);
            iv_boat.setImageResource(R.drawable.icon_night_boat);
            iv_star.setImageResource(R.drawable.icon_night_star);
            animationD2NSun = AnimationUtils.loadAnimation(context, R.anim.day_sun_reverse);
            animationD2NNightStar = AnimationUtils.loadAnimation(context, R.anim.night_star);
            animationD2NBoat = AnimationUtils.loadAnimation(context, R.anim.night_boat);
            animationBgAlphaDay = AnimationUtils.loadAnimation(context, R.anim.bg_alpha_day);
            iv_star.startAnimation(animationD2NNightStar);
            animationD2NNightStar.setAnimationListener(animationListener);
        }
        rl_anim.startAnimation(animationBgAlphaDay);
        iv_boat.startAnimation(animationD2NBoat);
        animationD2NBoat.setAnimationListener(animationListener);
    }

    private Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!OSUtil.isDayTheme() && animation == animationD2NSun) {
                rl_anim.setBackgroundColor(ContextCompat.getColor(context, R.color.black_1D242F));
                iv_oval_day.setImageResource(R.drawable.bg_night);
                iv_sun.setImageResource(android.R.color.transparent);
                iv_star.setImageResource(R.drawable.icon_night_star);
                iv_boat.setImageResource(R.drawable.icon_night_boat);
                iv_star.startAnimation(animationD2NNightStar);
            } else if (OSUtil.isDayTheme() && animation == animationD2NNightStar) {
                rl_anim.setBackgroundColor(ContextCompat.getColor(context, R.color.white_FAFFFE));
                iv_oval_day.setImageResource(R.drawable.bg_day);
                iv_sun.setImageResource(R.drawable.icon_day_sun);
                iv_star.setImageResource(android.R.color.transparent);
                iv_boat.setImageResource(R.drawable.icon_day_boat);
                iv_sun.startAnimation(animationD2NSun);
            }
            if (animation == animationD2NBoat) {
                dismiss();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
}
