package com.travel.widget;

import android.app.Dialog;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.ctsmedia.hltravel.R;
import com.travel.communication.entity.UserData;
import com.travel.lib.utils.OSUtil;
import com.travel.video.adapter.DialogWaitIntercutAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * 插播列表
 */
public class WaitIntercutDialog extends Dialog {

    private Context context;
    private List<UserData> list = new ArrayList<UserData>();
    private ListView listView;
    private DialogWaitIntercutAdapter adapter;
    private WaitIntercutDialog.OnItemClickListener listener;

    private ImageView closeImage;

    public interface OnItemClickListener{
        public void onClick(boolean isReceive,int position);
    }

    /**
     * 等待连线列表
     *
     * @param context
     *            上下文
     */
    public WaitIntercutDialog(Context context,WaitIntercutDialog.OnItemClickListener listener) {
        super(context, R.style.MyDialogStyle);
        this.context = context;
        this.listener = listener;
    }

    private static boolean isWait = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(isWait){
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
        WindowManager.LayoutParams params = window.getAttributes();
//        params.width = OSUtil.getScreenWidth()-OSUtil.dp2px(context, 90);
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM;
        window.setAttributes(params);
        setContentView(R.layout.dialog_wait_intercut);
        init();
    }

    private void init() {
        closeImage = (ImageView) findViewById(R.id.closeImage);
        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        listView = (ListView) findViewById(R.id.dialogListView);
        adapter = new DialogWaitIntercutAdapter(context,list,new DialogWaitIntercutAdapter.Listerner(){

            @Override
            public void Click(boolean isReceive, int position) {
                listener.onClick(isReceive,position);
            }
        });
        listView.setAdapter(adapter);

    }

    public void setData(List<UserData> list){
        if(list == null) return;

        this.list = list;
        if(adapter!=null)
            adapter.notifyDataSetChanged();

    }

}
