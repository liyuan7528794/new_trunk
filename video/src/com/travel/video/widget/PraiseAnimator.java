package com.travel.video.widget;

import java.util.Random;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.PointF;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
/**
 * 点赞动画处理类
 * @author Administrator
 *
 */
public class PraiseAnimator {
	/**
	 * 点赞动画
	 * @param imageView
	 * @param h
	 */
	public static void startAnimation(final ImageView imageView ,final float h) {
		ValueAnimator valueAnimator = new ValueAnimator();
		valueAnimator.setDuration(2000);
		valueAnimator.setObjectValues(new PointF(imageView.getX(), imageView.getY()));
		valueAnimator.setInterpolator(new LinearInterpolator());//设置变化的速度，当前为恒定
		final float a;
		int b = new Random().nextInt();
		if(b>0.5){
			a = new Random().nextFloat();
		}else{
			a = -new Random().nextFloat();
		}
		valueAnimator.setEvaluator(new TypeEvaluator<PointF>(){
			// fraction = t / duration
			@Override
			public PointF evaluate(float fraction, PointF startValue,PointF endValue){
				// x方向200px/s ，则y方向0.5 * g * t (g = 100px / s*s)
				startValue = new PointF();
				startValue.set(imageView.getX(), imageView.getY());
				PointF point = new PointF();
				point.y = -200 * fraction * 4;
				point.x = (float) (20*Math.sin(2.0*Math.PI*point.y/1000+a*10));
				return point;
			}
		});

		valueAnimator.addUpdateListener(new AnimatorUpdateListener(){
			@Override
			public void onAnimationUpdate(ValueAnimator animation){
				PointF point = (PointF) animation.getAnimatedValue();
				imageView.setX(point.x);
				imageView.setY(point.y+h*8/10);
			}
		});
		
		ObjectAnimator anim2 = ObjectAnimator.ofFloat(imageView, "alpha", 0f).setDuration(1500);
		anim2.addListener(new AnimatorListenerAdapter(){
			@Override
			public void onAnimationEnd(Animator animation){
				ViewGroup parent = (ViewGroup) imageView.getParent();
				if (parent != null)
					parent.removeView(imageView);
			}
		});
		
		ObjectAnimator anim3 = ObjectAnimator.ofFloat(imageView, "scaleX",0.1f, 0.5f).setDuration(500);
		ObjectAnimator anim4 = ObjectAnimator.ofFloat(imageView, "scaleY",0.1f, 0.5f).setDuration(500);
		
		AnimatorSet set = new AnimatorSet();
		set.play(valueAnimator).with(anim3);
		set.play(anim3).with(anim4);
		set.play(anim2).after(500L);
		set.start();
	}
}
