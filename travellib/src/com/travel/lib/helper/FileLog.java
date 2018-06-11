package com.travel.lib.helper;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于打印景区的活动log
 * */
public class FileLog {
	private static String TAG = "FileLog";
	private List<String> mBufferList = new ArrayList<String>();
	private static final int BUFFER_MAX = 20;
	private static FileLog s_Instance;
	
	public static FileLog getInstance(){
		if(s_Instance == null){
			synchronized (FileLog.class) {
				if(s_Instance == null){
					s_Instance = new FileLog();
				}
			}
		}
		return s_Instance;
	}
	
	private FileLog(){
		new WriteThread().start();
	}
	
	public void log(String tag, String content){
		if(TextUtils.isEmpty(tag) || !tag.startsWith("Inter")) return;
		logWithoutFilter(tag, content);
	}

	public void logWithoutFilter(String tag, String content){
		StringBuilder build = new StringBuilder();
		build.append(tag).append(":    ").append(System.currentTimeMillis()).append("\t");
		build.append(content).append("\n");
		synchronized (mBufferList) {
			mBufferList.add(build.toString());
			if(mBufferList.size() > BUFFER_MAX){
				mBufferList.notify();
			}
		}
	}
	
	public void flush(){
		synchronized (mBufferList) {
			if(mBufferList.size() > 0){
				mBufferList.notify();
			}
		}
	}
	
	private class WriteThread extends Thread{
		@Override
		public void run() {
			while (true) {
				List<String> content = new ArrayList<String>();
				synchronized (mBufferList) {
					if(mBufferList.size() == 0){
						try {
							mBufferList.wait();
						} catch (InterruptedException e) {
							Log.e(TAG, e.getMessage(), e);
							continue;
						}
					}else{
						content.addAll(mBufferList);
						mBufferList.clear();
					}
				}
				if(content.size() == 0) continue;
				try {
					Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getLogFile(), true)));
					for(String str : content){
						writer.write(str);
					}
					writer.flush();
					writer.close();
					Thread.sleep(300);
				} catch (Exception e) {
					Log.e(TAG,  e.getMessage(), e);
				}
			}
		}
	}
	
	
	
	private File getLogFile(){
		String path = Environment.getExternalStorageDirectory() + "/xblx/xblx.log";
		File file = new File(path);
		if(!file.exists()){
			File parent = file.getParentFile();
			if(!parent.exists()){
				parent.mkdirs();
			}
		}else{
			if(file.length() > 5000){
				// 大于10k
				file.delete();
			}
		}
		return file;
	}
}
