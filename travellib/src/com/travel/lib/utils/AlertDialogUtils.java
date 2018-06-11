package com.travel.lib.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.travel.Constants;
import com.travel.layout.DialogTemplet;
import com.travel.lib.R;

/**
 * 警告提示的对话框的工具类
 * 例如网络情况提醒等等
 * Created by ldkxingzhe on 2016/6/12.
 */
public class AlertDialogUtils {
    @SuppressWarnings("unused")
    private static final String TAG = "AlertDialogUtils";

    private AlertDialogUtils() {

    }



    /**
     * 执行耗费流量的操作, 可能出现的情况:
     * - wifi情况: 直接执行runnable, 不进行下不判断
     * - 2G, 3G, 4G情况: 弹出Dialog, 提示非wifi模式, 是否继续
     * - 取消: 不进行任何操作
     * - 继续:继续进行操作，　执行runnable
     *
     * @param runnable 需要执行的任务
     */
    public static void runNeedWifiOperation(Context context, final Runnable runnable) {
        String netType = CheckNetStatus.checkNetworkConnection();
        if (CheckNetStatus.unNetwork.equals(netType)) {
            Toast.makeText(context, "网络无法连接", Toast.LENGTH_SHORT).show();
            return;
        }
        if (CheckNetStatus.wifiNetwork.equals(netType)) {
            runnable.run();
            return;
        }

        DialogTemplet dialogTemplet = getNetStatusDialog(netType, context);
        dialogTemplet.setLeftClick(new DialogTemplet.DialogLeftButtonListener() {

            @Override
            public void leftClick(View view) {
                runnable.run();
            }
        });
    }

    /**
     * 普通的AlertDialog 为保持App对话框的一致性
     *
     */
    public static void alertDialog(Context context, String content, final Runnable yesRunnable) {
        alertDialog(context, content, yesRunnable, null);
    }

    public static void alertDialog(Context context, String content, final Runnable yesRunnable, final Runnable noRunnable) {
        DialogTemplet dialogTemplet = new DialogTemplet(context, false, content, "", "取消", "确认");
        if (yesRunnable != null){
            dialogTemplet.setRightClick(new DialogTemplet.DialogRightButtonListener() {
                @Override
                public void rightClick(View view) {
                    yesRunnable.run();
                }
            });
        }

        if(noRunnable != null){
            dialogTemplet.setLeftClick(new DialogTemplet.DialogLeftButtonListener() {
                @Override
                public void leftClick(View view) {
                    noRunnable.run();
                }
            });
            dialogTemplet.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    noRunnable.run();
                }
            });
        }
        dialogTemplet.show();
    }


    /**
     * 是显示确认按钮， runnable必定执行
     */
    public static void alertDialogOneButton(Context context, String content, @Nullable final Runnable runnable){
        DialogTemplet dialogTemplet = new DialogTemplet(context, true, content, "确认", "", "");
        if(runnable != null){
            dialogTemplet.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    runnable.run();
                }
            });
        }
        dialogTemplet.show();

    }

    /**
     * 需要登录的操作
     */
    public static void needLoginOperator(final Context context, final Runnable runnable) {
        if (UserSharedPreference.isLogin()) {
            runnable.run();
            return;
        }
        DialogTemplet dialogTemplet = new DialogTemplet(context, false,
                "此操作需要登录, \n是否前往登录页面?", "", "取消", "登录");
        dialogTemplet.setRightClick(new DialogTemplet.DialogRightButtonListener() {
            @Override
            public void rightClick(View view) {
                Intent intent = new Intent();
                intent.setAction("com.travel.login");
                context.startActivity(intent);
            }
        });
        dialogTemplet.show();
    }


    /**
     * 提示用户网络不是处于WiFi状态
     *
     * @param netType
     * @param bundle
     * @ldkxingzhe: 仅仅用于兼容性
     */
    public static void netNotifyDialog(String netType, final Bundle bundle, final Context mContext) {
        final DialogTemplet dialog = getNetStatusDialog(netType, mContext);
        dialog.setLeftClick(new DialogTemplet.DialogLeftButtonListener() {

            @Override
            public void leftClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setRightClick(new DialogTemplet.DialogRightButtonListener() {

            @Override
            public void rightClick(View view) {
                dialog.dismiss();
                if (bundle != null) {
                    Intent intent = new Intent();
                    intent.setAction(Constants.VIDEO_ACTION);
                    intent.setType(Constants.VIDEO_TYPE);
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                }
            }
        });
    }

    /**
     * 提示用户网络不是处于WiFi状态(用于商品详情页的视频点击)
     *
     * @param netType
     * @ldkxingzhe: 仅仅用于兼容性
     */
    public static void netNotifyDialogGoods(String netType, final Context mContext, final StartListener mStartListener,final View v,final View converView) {
        final DialogTemplet dialog = getNetStatusDialog(netType, mContext);
        dialog.setLeftClick(new DialogTemplet.DialogLeftButtonListener() {

            @Override
            public void leftClick(View view) {
                dialog.dismiss();
                mStartListener.setCustomChoose(false,v,converView);
            }
        });
        dialog.setRightClick(new DialogTemplet.DialogRightButtonListener() {

            @Override
            public void rightClick(View view) {
                dialog.dismiss();
                mStartListener.setCustomChoose(true,v,converView);
            }
        });
    }

    private static DialogTemplet mDialog;

    @NonNull
    public static DialogTemplet getNetStatusDialog(String netType, Context mContext) {
        Resources rs = mContext.getResources();
//        if (mDialog == null ) {
            final DialogTemplet dialog = new DialogTemplet(mContext, false,
                    rs.getString(R.string.current) + netType + rs.getString(R.string.net_continue), "",
                    rs.getString(R.string.cancle), rs.getString(R.string.go_on));
            mDialog = dialog;
//        }
        try {
            mDialog.show();
        }catch (Exception e){

        }
        return mDialog;
    }

     public interface StartListener {

        void setCustomChoose(boolean choose,View v,View converView);
    }
}
