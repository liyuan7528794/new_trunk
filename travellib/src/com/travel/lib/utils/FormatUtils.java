package com.travel.lib.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

public class FormatUtils {

    /**
     * @param context
     * @param str 全部文字
     * @param changeColorStr 改变颜色的文字
     * @param color 颜色
     */
    public  static SpannableStringBuilder StringSetSpanColor(Context context, String str, String changeColorStr, int color){
        int start = 0, end = 0;
        SpannableStringBuilder spBuilder = null;

        if(!TextUtils.isEmpty(str) && !TextUtils.isEmpty(changeColorStr) && str.contains(changeColorStr)){
            /*
             *  返回highlightStr字符串wholeStr字符串中第一次出现处的索引。
             */
            start = str.indexOf(changeColorStr);
            end = start + changeColorStr.length();
            spBuilder=new SpannableStringBuilder(str);
            color = ContextCompat.getColor(context, color);
            CharacterStyle charaStyle=new ForegroundColorSpan(color);
            spBuilder.setSpan(charaStyle, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spBuilder;
    }

}