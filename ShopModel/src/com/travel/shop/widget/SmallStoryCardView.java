package com.travel.shop.widget;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.travel.Constants;
import com.travel.ShopConstant;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;
import com.travel.shop.activity.GoodsActivity;
import com.travel.shop.activity.StoryCardActivity;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by Administrator on 2017/11/8.
 */

public class SmallStoryCardView {
    private TextView name, card, price, go;
    private ImageView cover;
    private View view;
    private String id ;
    private WeakReference<Context> mContext;
    public SmallStoryCardView(final Context context) {
        view = View.inflate(context, R.layout.layout_order_success_active, null);
        mContext = new WeakReference(context);
        init();
        card.setText("故事卡");
        name.setText("小城");
        price.setText("千元小城玩去！");
        go.setText("立即购买");
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UserSharedPreference.isLogin()) {
                    Intent loginIntent = new Intent();
                    loginIntent.setAction(Constants.ACTION_LOGIN);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.get().startActivity(loginIntent);
                    return;
                }
                getOrderId();
            }
        });
    }

    private void getOrderId() {
        HashMap<String, Object> map = new HashMap<>();
        NetWorkUtil.postForm(mContext.get(), ShopConstant.GET_STORY_CARD_ID, new MResponseListener() {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0){
                    id = response.optString("data");
                    if(TextUtils.isEmpty(id)){
                        Toast.makeText(mContext.get(), "未找到相关卡券", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(TextUtils.equals(go.getText().toString(), "点击查看")) {
                        Intent intent = new Intent(go.getContext(), StoryCardActivity.class);
                        mContext.get().startActivity(intent);
                    }else if(TextUtils.equals(go.getText().toString(), "续次")){
                        Intent intent = new Intent(mContext.get(), GoodsActivity.class);
                        intent.putExtra("goodsId", id);
                        mContext.get().startActivity(intent);
                    }else{
                        Intent intent = new Intent(mContext.get(), GoodsActivity.class);
                        intent.putExtra("goodsId", id);
                        mContext.get().startActivity(intent);
                    }
                }
            }
        }, map);
    }

    private void init() {
        name = (TextView) view.findViewById(R.id.tv_name);
        card = (TextView) view.findViewById(R.id.tv_card);
        price = (TextView) view.findViewById(R.id.tv_price);
        go = (TextView) view.findViewById(R.id.tv_go);
        cover = (ImageView) view.findViewById(R.id.iv_cover);
    }

    public TextView getName() {
        return name;
    }

    public void setName(TextView name) {
        this.name = name;
    }

    public TextView getCard() {
        return card;
    }

    public void setCard(TextView card) {
        this.card = card;
    }

    public TextView getPrice() {
        return price;
    }

    public void setPrice(TextView price) {
        this.price = price;
    }

    public TextView getGo() {
        return go;
    }

    public void setGo(TextView go) {
        this.go = go;
    }

    public ImageView getCover() {
        return cover;
    }

    public void setCover(ImageView cover) {
        this.cover = cover;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
