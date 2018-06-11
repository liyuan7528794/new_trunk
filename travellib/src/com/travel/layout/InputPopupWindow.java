package com.travel.layout;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.travel.lib.R;
import com.travel.lib.utils.KeyboardChangeStatus;
import com.travel.lib.utils.OSUtil;

import java.lang.ref.WeakReference;

/**
 * 点评向上弹窗
 *
 * @author Administrator
 */
public class InputPopupWindow extends BaseBellowPopupWindow {
    private View rootView;
    private WeakReference<Activity> activity;

    private EditText commentContentEdit;

    private String tag = "-1";

    public InputPopupWindow(final Activity activity, String tag) {
        super(activity);
        this.activity = new WeakReference<Activity>(activity);
        this.tag = tag;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.input_popwindow, null);
        initListView();

        SetContentView(rootView);
        new KeyboardChangeStatus().setOnKeyboardListener(activity, new KeyboardChangeStatus.OnKeyboardListener() {
            @Override
            public void onKeyboardStatus(boolean isShow, int keyHeight) {
                if(!isShow) {// 隐藏
                    if(isShowing())// 判断popWindow是否显示
                        dismiss();
                }
            }
        });
        show();
    }

    private void initListView() {
        commentContentEdit = (EditText) rootView.findViewById(R.id.comment_content_edit);
        commentContentEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // 如果不加&& event.getAction() == KeyEvent.ACTION_UP条件，会执行两次，因为keyCode == KeyEvent.KEYCODE_ENTER在up和down事件都执行
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP ){
                    sendComment();
                    dismiss();
                }
                return false;
            }
        });

        commentContentEdit.setFocusable(true);
        commentContentEdit.setFocusableInTouchMode(true);
        commentContentEdit.requestFocus();
        OSUtil.showKeyboard(activity.get(), commentContentEdit);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        OSUtil.hideKeyboardPopWindow(activity.get(), commentContentEdit);
        commentContentEdit.setFocusable(false);
        commentContentEdit.clearFocus();
    }

    /**
     * 发送评论
     */
    private void sendComment() {

        OSUtil.hideKeyboardPopWindow(activity.get(), commentContentEdit);

        if (commentContentEdit.getText() == null || "".equals(commentContentEdit.getText().toString().trim())) {
            Toast.makeText(activity.get(), "请输入评论内容", Toast.LENGTH_SHORT).show();
            return;
        }
        String content = commentContentEdit.getText().toString();
        listener.onInputText(content, tag);
    }

    private OnListener listener;
    public void setListener(OnListener listener) {
        this.listener = listener;
    }

    public interface OnListener{
        void onInputText(String content, String tag);
    }
}
