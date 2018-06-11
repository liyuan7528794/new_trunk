package com.travel.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片拾取辅助类
 * 以及其相关处理
 */
public class PictureHelper {
	private static final String TAG = "PictureHelper";
	
	private int mAspectX = 2;
	private int mAspectY = 3;
	private int mOutPutX = 782;
	private int mOutPutY = 1080;
	
	/**
	 * 拾取图片, action为ACTION_GET_CONTENT
	 * 此方法中调用context.startActivityForResult方法
	 * @param context     上下文
	 * @param requestCode 请求码 
	 */
	public void pickImage(Activity context, int requestCode){
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		context.startActivityForResult(intent, requestCode);
	}
	
	/**
	 * 从Fragment中startActivity
	 * @param fragment
	 * @param requestCode
	 */
	public void pickImage(Fragment fragment, int requestCode){
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		fragment.startActivityForResult(intent, requestCode);
	}
	/**
	 * 裁剪图片
	 * @param context      上下文
	 * @param requestCode  请求码
	 * @param uri          图片的来源
	 * @param path         裁剪后的图片存放位置
	 */
	public void cropImage(Activity context, int requestCode, Uri uri, String path){
		File file = new File(path);
		makeSureFileParentExist(file);
		
		context.startActivityForResult(OSUtil.getPerformCrop(uri, mAspectX, mAspectY, Uri.fromFile(file), mOutPutX, mOutPutY), requestCode);
	}
	
	/**
	 * 确保文件的父文件存在
	 * @param file
	 */
	private void makeSureFileParentExist(File file) {
		File parentFile = file.getParentFile();
		if(!parentFile.exists()){
			parentFile.mkdirs();
		}
	}
	/**
	 * 拾取图片结果到达后的处理
	 * @param context 上下文
	 * @param tmpPath  临时路径名
	 * @param intent 在activityresult中的intent
	 */
	public void onPickResult(Context context, String tmpPath, Intent intent){
		MLog.v(TAG, "onPickResult, and tmpPath is " + tmpPath);
		Uri uri = intent.getData();
		if(uri == null){
			MLog.e(TAG, "onPickResult and uri is null");
			// TODO:做一些处理 
		}else{
			InputStream inputStream = null;
			try {
				inputStream = context.getContentResolver().openInputStream(uri);
				File file = new File(tmpPath);
				/*if(!file.exists()){
					file.createNewFile();
				}*/
				// TODO: 检查效率, 考虑开线程
				compressPicture(inputStream, tmpPath);
			}catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}finally {
				if(inputStream != null){
					try {
						inputStream.close();
					} catch (IOException e) {
						MLog.e(TAG, e.getMessage());
					}
				}
			}
		}
	}
	
	/**
	 * 将流数据保存到文件中
	 * inputStream在函数中不进行关闭
	 * @param inputStream 输入流, 此流在此函数中不关闭
	 * @param file        file文件, 应保障file文件存在
	 * @throws IOException 
	 */
	private void saveInputStreamToFile(InputStream inputStream, File file) throws IOException{
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			byte[] bytes = new byte[1024];
			int readCount = 0;
			while((readCount = inputStream.read(bytes)) != -1){
				fileOutputStream.write(bytes, 0, readCount);
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
		}finally {
			if(fileOutputStream != null){
				fileOutputStream.close();
			}
		}
	}
	
	/**
	 * 压缩图片
	 * @param inputStream 输入流
	 * @param path        压缩后的图片的位置
	 */
	private void compressPicture(InputStream inputStream, String path){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inTempStorage = new byte[100 * 1024];
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inPurgeable = true;
		options.inSampleSize = 2;
		options.inInputShareable = true;
		Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
		
		File file = new File(path);
		makeSureFileParentExist(file);
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream);
			fileOutputStream.flush();
		} catch (FileNotFoundException e) {
			MLog.e(TAG, e.getMessage());
		} catch (IOException e) {
			MLog.e(TAG, e.getMessage());
		} finally {
			if(fileOutputStream != null){
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					//ignore
				}
			}
		}
	}
}
