package com.travel.shop.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.travel.lib.utils.OSUtil;
import com.travel.shop.R;
import com.travel.shop.http.GoodsInfoHttp;

/**
 * 故事详情页发送评论
 * Created by wyp on 2017/2/27.
 */
public class SendDialog extends Dialog {

    private Context mContext;
    private SendDialog instance;
    private EditText mEdittext;
    private TextView mText;
    private String storyId;
    private GoodsInfoHttp.GoodsInfoListener mGoodsInfoListener;


    /**
     * 弹出框初始化
     *
     * @param mContext 上下文
     */
    public SendDialog(Context mContext, String storyId, GoodsInfoHttp.GoodsInfoListener mGoodsInfoListener) {
        super(mContext, R.style.SendDialogStyle);
        this.mContext = mContext;
        instance = this;
        this.storyId = storyId;
        this.mGoodsInfoListener = mGoodsInfoListener;
    }

    private static boolean isWait = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isWait) {
            isWait = false;
            return;
        }
        isWait = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    isWait = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Window window = getWindow();
        setContentView(R.layout.popwindow_send);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = OSUtil.getScreenWidth();
        params.height = OSUtil.dp2px(mContext, 90);
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        this.setCanceledOnTouchOutside(true);

        mEdittext = (EditText) findViewById(R.id.et_comment_edit);
        mText = (TextView) findViewById(R.id.tv_comment_send);

        initButtonListener();
    }

    private void initButtonListener() {
        mText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.dismiss();
                String comment = mEdittext.getText().toString().trim();
                if (!TextUtils.isEmpty(comment)) {
                    GoodsInfoHttp.sendComment(mContext, storyId, comment, mGoodsInfoListener);
                }
            }
        });

    }
}
