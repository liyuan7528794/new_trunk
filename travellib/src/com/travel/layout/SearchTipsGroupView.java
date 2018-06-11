package com.travel.layout;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.lib.R;
import com.travel.lib.utils.OSUtil;

import java.util.List;

/**
 * 搜索热搜的布局
 * Created by Administrator on 2017/11/21.
 */

public class SearchTipsGroupView extends LinearLayout {

    private Context context;

    public SearchTipsGroupView(Context context) {
        super(context);
        this.context = context;
        setOrientation(VERTICAL);//设置方向
    }

    public SearchTipsGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setOrientation(VERTICAL);//设置方向
    }

    public SearchTipsGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setOrientation(VERTICAL);//设置方向
    }


    /**
     * 外部接口调用
     *
     * @param items
     * @param onItemClick
     */
    public void initViews(List<String> items, final OnItemClick onItemClick) {
        int length = 0;//一行加载item 的宽度

        LinearLayout layout = null;

        LayoutParams layoutLp = null;

        boolean isNewLine = true;//是否换行

        int screenWidth = getScreenWidth();//屏幕的宽度

        int size = items.size();
        for (int i = 0; i < size; i++) {//便利items
            if (isNewLine) {//是否开启新的一行
                layout = new LinearLayout(context);
                layout.setOrientation(HORIZONTAL);
                layoutLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            View view = LayoutInflater.from(context).inflate(R.layout.item_textview, null);
            TextView itemView = (TextView) view.findViewById(R.id.text);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, OSUtil.dp2px(context, 33));
            params.bottomMargin = OSUtil.dp2px(context, 15);
            params.rightMargin = OSUtil.dp2px(context, 15);
            itemView.setLayoutParams(params);
            itemView.setText(items.get(i));

            final int j = i;
            itemView.setOnClickListener(new OnClickListener() {//给每个item设置点击事件
                @Override
                public void onClick(View v) {
                    if (null != onItemClick) {
                        onItemClick.onClick(j);
                    }
                }
            });

            //设置item的参数
            LayoutParams itemLp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            itemLp.rightMargin = OSUtil.dp2px(context, 15);
            itemLp.bottomMargin = OSUtil.dp2px(context, 15);

            //得到当前行的长度
            length += OSUtil.dp2px(context, 15) + getViewWidth(itemView);
            if (length > screenWidth) {//当前行的长度大于屏幕宽度则换行
                length = 0;
                addView(layout, layoutLp);
                isNewLine = true;
                i--;
            } else {//否则添加到当前行
                isNewLine = false;
                layout.addView(view, itemLp);
            }
        }
        addView(layout, layoutLp);
    }

    /**
     * 得到手机屏幕的宽度
     *
     * @return
     */
    private int getScreenWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels - OSUtil.dp2px(context, 30);
    }

    /**
     * 得到view控件的宽度
     *
     * @param view
     * @return
     */
    private int getViewWidth(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        return view.getMeasuredWidth();
    }

    public interface OnItemClick{
        void onClick(int position);
    }
}
