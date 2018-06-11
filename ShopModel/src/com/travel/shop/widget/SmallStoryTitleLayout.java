package com.travel.shop.widget;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.shop.R;

/**
 * Created by Administrator on 2017/11/14.
 */

public class SmallStoryTitleLayout {
    private View view;
    private TextView title;
    private TextView num;
    private LinearLayout ll;
    public SmallStoryTitleLayout(Context context) {
        view = View.inflate(context, R.layout.layout_order_success_title, null);
        title = (TextView) view.findViewById(R.id.tv_title);
        num = (TextView) view.findViewById(R.id.tv_num);
        ll = (LinearLayout) view.findViewById(R.id.ll);
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public TextView getTitle() {
        return title;
    }

    public void setTitle(TextView title) {
        this.title = title;
    }

    public TextView getNum() {
        return num;
    }

    public void setNum(TextView num) {
        this.num = num;
    }

    public LinearLayout getLl() {
        return ll;
    }

    public void setLl(LinearLayout ll) {
        this.ll = ll;
    }
}
