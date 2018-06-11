package com.travel.lib.utils;

import com.travel.layout.FollowPopupWindow;
import com.travel.layout.SharePopupWindow;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;

public class PopWindowUtils {
    public static void followPopUpWindow(Activity context, String userId, String nickName, String imgUrl, int type) {
        new FollowPopupWindow(context, userId, nickName, imgUrl, type);
    }

    public static void sharePopUpWindow(Context context, String title, String text, String imageUrl, String url) {
        new SharePopupWindow(context, title, text, imageUrl, url);
    }
}
