package com.travel.video.help;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.travel.bean.VideoInfoBean;
import com.travel.layout.DialogTemplet;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.video.live.HostWindowActivity;
import com.travel.video.playback_video.PlaybackVideoPlayerActivity;

/**
 * Created by Administrator on 2016/10/17.
 */

public class VideoIntentHelper {
    private Context context;
    public final static String GO_LIVE = "go_live";
    public final static String GO_LOGIN = "go_login";
    public final static String LOOK_LIVE = "look_live";
    public final static String LOOK_VIDEO = "look_video";
    public VideoIntentHelper(Context context){
        this.context = context;
    }
    private LeaveListener leaveListener = null;
    public interface LeaveListener{
        public void leaveNotice(String type);
    }

    /**
     * 直播或视频列表intent处理
     * @param bean
     * @param view
     */
    public void intentWatchVideo(final VideoInfoBean bean, View view){
//        if(view != null)
//            view.setEnabled(false);
        try {
            final Bundle bundle = new Bundle();
            final Intent intent = new Intent();
            if(1 == bean.getVideoStatus()){
                bundle.putSerializable("video_info", bean);
                intent.setClass(context, HostWindowActivity.class);
            }else {
                bundle.putSerializable("video_info", bean);
                intent.setClass(context, PlaybackVideoPlayerActivity.class);
            }

            String netType = CheckNetStatus.checkNetworkConnection();
            if (CheckNetStatus.unNetwork.equals(netType)) {// 没网
                Toast.makeText(context, "当前无网络，请检查网络！", Toast.LENGTH_SHORT).show();
            } else if (!CheckNetStatus.unNetwork.equals(netType)
                    && !CheckNetStatus.wifiNetwork.equals(netType)) {
                if ("UNKNOWN".equals(netType)) {
                    Toast.makeText(context, "当前无网络，请检查网络！", Toast.LENGTH_SHORT).show();
                } else {

                    //弹框提醒
                    final DialogTemplet dialog = AlertDialogUtils.getNetStatusDialog(netType, context);
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

                            if(leaveListener!=null && 1 == bean.getVideoStatus())
                                leaveListener.leaveNotice(LOOK_LIVE);
                            else if(leaveListener!=null && 2 == bean.getVideoStatus())
                                leaveListener.leaveNotice(LOOK_VIDEO);
                            intent.putExtras(bundle);
                            context.startActivity(intent);
                        }
                    });

                }
            } else if (CheckNetStatus.wifiNetwork.equals(netType)) {

                if(leaveListener!=null && 1 == bean.getVideoStatus())
                    leaveListener.leaveNotice(LOOK_LIVE);
                else if(leaveListener!=null && 2 == bean.getVideoStatus())
                    leaveListener.leaveNotice(LOOK_VIDEO);

                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
//            if(view != null)
//                view.setEnabled(true);
        }
    }
}
