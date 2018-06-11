package com.travel.lib.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2018/4/2.
 */

public class KeyboardChangeStatus {
    public interface OnKeyboardListener{
        /**
         * 键盘改变时监听状态
         * @param isShow
         * @param keyHeight // 键盘的高度（TODO:暂时还未处理）
         */
        void onKeyboardStatus(boolean isShow, int keyHeight);
    }
    private int displayHeight = 0;// 内容页面可见高度
    public void setOnKeyboardListener(final Activity activity, final OnKeyboardListener listener){
        final WeakReference<Activity> weakActivity = new WeakReference<Activity>(activity);
        final int screenHeight = weakActivity.get().getWindow().getDecorView().getHeight();
        final int barHeight = OSUtil.getSoftButtonsBarHeight(weakActivity.get());
        weakActivity.get().getWindow().getDecorView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Rect rect = new Rect();
                weakActivity.get().getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

                if(oldBottom == 0 || displayHeight == 0) {
                    displayHeight = rect.bottom;
                    return;
                }
                if(Math.abs(rect.bottom - displayHeight) <= barHeight ){// 等于0表示没有改变底部虚拟导航键，等于barHeight表示改变了导航键高度
                    displayHeight = rect.bottom;
                    return;
                }
                displayHeight = rect.bottom;
                //获取View可见区域的bottom

                if(screenHeight - displayHeight <= barHeight ){
                    // hide
                    listener.onKeyboardStatus(false, 0);
                }else {
                    // show
                    listener.onKeyboardStatus(true, 0);
                }
            }
        });
    }


}
