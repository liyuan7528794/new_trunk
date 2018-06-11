package com.travel.video.tools;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.travel.lib.utils.FormatUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 时间处理类
 * @author Administrator
 */
public class HostWindowTimerTask {
	private static Timer timer = null;
	public static void instance(Context context){
	}
	
	public static void timer(final Handler mHandler,final int what){
		timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			int i = 0;
			@Override
			public void run() {
				Message msg = new Message();
				msg.what = what;
				msg.obj = i;
				mHandler.sendMessage(msg);
				i++;
			}
		};
		timer.schedule(timerTask, 0, 1000);
	}
	
	public static void closeTimer(){
		if(timer!=null){
			timer.cancel();
			timer = null;
		}
	}
	
}
