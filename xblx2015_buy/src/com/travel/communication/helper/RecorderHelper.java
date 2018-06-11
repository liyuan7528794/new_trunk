package com.travel.communication.helper;

import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OutputFormat;
import android.support.annotation.NonNull;

import com.travel.lib.utils.MLog;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 录音的辅助类
 * 需要注意的是必须在不在使用此方法时调用release函数 **IMPORTANT**
 * @author ldkxingzhe
 */
public class RecorderHelper {
	@SuppressWarnings("unused")
	private static final String TAG = "RecorderHelper";

	private String mDirPath; // 录音地址
	private MediaRecorder mRecorder;
	private String mCurrentRecorderPath;
	private long mStartTime;

	private boolean mIsRecording = false;

	private RecorderHelper(String dirPath){
		try{
			mRecorder = new MediaRecorder();
		}catch (RuntimeException e){
			MLog.e(TAG, e.getMessage(), e);
			mRecorder = null;
		}
		mDirPath = dirPath;
	}

	public static synchronized RecorderHelper getInstance(@NonNull String filePath){
		return new RecorderHelper(filePath);
	}
	/**
	 * 准备并开始录音
	 */
	public String prepareAndStartRecorder(){
		if(mRecorder == null) return null;
		File file = new File(mDirPath);
		if(!file.exists()){
			file.mkdirs();
		}
		mRecorder.reset();

		mCurrentRecorderPath = new File(file, generateFileName()).getAbsolutePath();
		mRecorder.setOutputFile(mCurrentRecorderPath);
		mRecorder.setAudioSource(AudioSource.MIC);
		mRecorder.setOutputFormat(OutputFormat.MPEG_4);
		mRecorder.setAudioEncoder(AudioEncoder.AAC);
		try {
			mRecorder.prepare();
			mRecorder.start();
			mStartTime = System.currentTimeMillis();
		} catch (IllegalStateException e) {
			MLog.e(TAG, e.getMessage());
			return null;
		} catch (IOException e) {
			MLog.e(TAG, e.getMessage());
			return null;
		}
		mIsRecording = true;
		return mCurrentRecorderPath;
	}

	public int getVoiceLevel(int maxLevel){
		try {
			return maxLevel * mRecorder.getMaxAmplitude() / 32768 + 1;
		} catch (IllegalStateException e) {
			MLog.e(TAG, e.getMessage());
			return 1;
		}
	}

	public boolean isRecording(){
		return mIsRecording;
	}

	public boolean stop(){
		mIsRecording = false;
		try{
			mRecorder.stop();
			if(System.currentTimeMillis() - mStartTime < 2000){
				// 小于2s
				return false;
			}
			return true;
		}catch (RuntimeException e){
			MLog.e(TAG, "stop failed. " + e.getMessage(), e);
			File file = new File(mCurrentRecorderPath);
			file.deleteOnExit();
		}
		return false;
	}

	public long getTimeLong(){
		return System.currentTimeMillis() - mStartTime > 60000 ? 60000 : System.currentTimeMillis() - mStartTime;
	}

	public String getCurrentRecorderPath(){
		return mCurrentRecorderPath;
	}
	/**
	 * 释放recorder
	 * 此方法最后必须调用
	 */
	public void release(){
		mRecorder.release();
		mRecorder = null;
	}

	private String generateFileName(){
		return UUID.randomUUID().toString() + ".m4a";
	}
}
