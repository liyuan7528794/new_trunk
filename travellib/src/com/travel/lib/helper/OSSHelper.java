package com.travel.lib.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.model.OSSResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.travel.Constants;
import com.travel.lib.TravelApp;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * 阿里云服务器的上传辅助类
 * @author ldkxingzhe
 */
public class OSSHelper {
	private static final String TAG = "OSSHelper";
	
	private static final String END_POINTS = "http://oss-cn-hangzhou.aliyuncs.com";
	private static final String ACCESS_KEY_ID = "accessKeyId";
	private static final String BUCKET_NAME = "bucketName";
	private static final String ACCESS_KEY_SECRET = "accessKeySecret";
	private static final String OSS_KEY = "OSS_KEY";

	public static String OSS_ROOT_URL = "http://hltravel.img-cn-hangzhou.aliyuncs.com/";
	private static String OSS_NORMAL_ROOT_URL = null;
	
	private String mAccessKeyId, mBucketName, mAccessKeySecret;
	private volatile OSS mOss;
	
	public OSSHelper(){
		if(!loadKeyFromLocal()){
			loadKeyFromNet();
			return;
		}
		initOSS();
	}

	/**
	 * 异步上传文件
	 * @param objectkey 对应的阿里的key
	 * @param filePath  上传文件的本地目录
	 * @param listener  阿里云sdk的上传回调接口, 可以为null
	 * @param progressCallback 进度回调显示
	 * @return          true -- 已经准备好进入上传, false -- 尚未初始化, 不能进入上传
	 */
	public boolean uploadFile(final String objectkey, String filePath,
							  final OSSCompletedCallback<PutObjectRequest, OSSResult> listener,
							  OSSProgressCallback<PutObjectRequest> progressCallback){
		if(mOss == null){
			loadKeyFromNet();
			MLog.e(TAG, "mOss has not been inited, and return false");
			return false;
		}

		PutObjectRequest put = new PutObjectRequest(mBucketName, objectkey, filePath);
		if(progressCallback != null){
			put.setProgressCallback(progressCallback);
		}
		mOss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
			@Override
			public void onSuccess(PutObjectRequest request, PutObjectResult result) {
				MLog.d("PutObject", "UploadSuccess");

				MLog.d("ETag", result.getETag());
				MLog.d("RequestId", result.getRequestId());

				if(listener != null){
					listener.onSuccess(request, result);
				}
			}

			@Override
			public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
				// 请求异常
				if (clientExcepion != null) {
					// 本地异常如网络异常等
					Log.e(TAG, clientExcepion.getMessage(), clientExcepion);
				}
				if (serviceException != null) {
					// 服务异常
					MLog.e("ErrorCode", serviceException.getErrorCode());
					MLog.e("RequestId", serviceException.getRequestId());
					MLog.e("HostId", serviceException.getHostId());
					MLog.e("RawMessage", serviceException.getRawMessage());
					Log.e(TAG, serviceException.getMessage(), serviceException);
				}

				if(listener != null){
					listener.onFailure(request, clientExcepion, serviceException);
				}
			}
		});
		return true;
	}
	/**
	 * 异步上传文件
	 * @param objectkey 对应的阿里的key
	 * @param filePath  上传文件的本地目录
	 * @param listener  阿里云sdk的上传回调接口, 可以为null
	 * @return          true -- 已经准备好进入上传, false -- 尚未初始化, 不能进入上传
	 */
	public boolean uploadFile(final String objectkey, String filePath, final OSSCompletedCallback<PutObjectRequest, OSSResult> listener){
		return uploadFile(objectkey, filePath, listener, null);
	}

	public boolean uploadFileSync(String objectKey, String path){
		return uploadFileSync(objectKey, path, null);
	}
	/**
	 * 根据用户id生成object key, 同步上传文件
     */
	public boolean uploadFileSync(String objectKey, String path, OSSProgressCallback<PutObjectRequest> progressCallback){
		if(mOss == null){
			loadKeyFromNet();
			MLog.e(TAG, "mOss has not been inited, and return false");
			return false;
		}

		PutObjectRequest put = new PutObjectRequest(mBucketName, objectKey, path);
		if(progressCallback != null){
			put.setProgressCallback(progressCallback);
		}
		try {
			mOss.putObject(put);
			return true;
		} catch (Exception e) {
			MLog.e(TAG, e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * 根据userId与当前时间自动生成目录key上传特定路径的问价
	 * @param userId   用户id 
	 * @param path     上传文件的本地路径名 
	 * @param listener 监听 
	 * @return
	 */
	public boolean uploadFileByUserId(String userId, String path, OSSCompletedCallback<PutObjectRequest, OSSResult> listener){
		return uploadFile(generateObjectKey(userId), path, listener);
	}
	
	public String getImageUrlByObjectKey(String objectKey, String postFix){
		String wholeUrl = OSS_ROOT_URL + objectKey;
		if(TextUtils.isEmpty(postFix)){
			return wholeUrl;
		}
		return wholeUrl + postFix;
	}

	/**
	 * 返回普通对象的地址
     */
	public String getUrlByObjectKey(String objectKey){
		if(TextUtils.isEmpty(OSS_NORMAL_ROOT_URL)){
			OSS_NORMAL_ROOT_URL = OSS_ROOT_URL/*.replace("img", "oss")*/;
		}
		return OSS_NORMAL_ROOT_URL + objectKey;
	}
	
	/**
	 * 生成一个objectkey, 默认使用jpg
	 * 或许是一个临时方案, 最初约定的一个文件夹与名字
	 * @return
	 */
	public String generateObjectKey(String userId){
		return generateObjectKey(userId, ".jpg");
	}

	public String generateObjectKey(String userId, String postFix){
		Calendar now = Calendar.getInstance();
		String year = String.valueOf(now.get(Calendar.YEAR));
		String month = String.valueOf(now.get(Calendar.MONTH) + 1);
		String day = String.valueOf(now.get(Calendar.DAY_OF_MONTH));
		String hour = String.valueOf(now.get(Calendar.HOUR_OF_DAY));
		String minute = String.valueOf(now.get(Calendar.MINUTE));
		String second = String.valueOf(now.get(Calendar.SECOND));
		String millSecond = String.valueOf(now.get(Calendar.MILLISECOND));
		StringBuilder builder = new StringBuilder();
		builder.append(year).append("/")
				.append(month).append("/")
				.append(day).append("/")
				.append(userId)
				.append(hour)
				.append(minute)
				.append(second).append(millSecond).append(postFix);
		String result = builder.toString();
		MLog.v(TAG, "generateObjectKey and key is " + result);
		return result;
	}
	
	/**
	 * 初始化阿里云
	 */
	private void initOSS(){
		OSSCredentialProvider credentialProvider
			= new OSSPlainTextAKSKCredentialProvider(mAccessKeyId, mAccessKeySecret);
		ClientConfiguration conf = new ClientConfiguration();
		conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
		conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
		conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
		conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
		mOss = new OSSClient(TravelApp.appContext, END_POINTS, credentialProvider, conf);
	}
	
	
	
	// 从本地加载key 
	// @return true -- 表示加载成功, false -- 需要访问网络
	private boolean loadKeyFromLocal(){
		if(!TextUtils.isEmpty(mAccessKeyId)
				&& !TextUtils.isEmpty(mAccessKeySecret)
				&& !TextUtils.isEmpty(mBucketName)){
			MLog.v(TAG, "acesskey already in memory, and return true");
			return true;
		}
		
		SharedPreferences sharedPreferences = TravelApp.appContext.getSharedPreferences(OSS_KEY, Context.MODE_PRIVATE);
		String accessKeyId = sharedPreferences.getString(ACCESS_KEY_ID, null);
		String accessKeySecret = sharedPreferences.getString(ACCESS_KEY_SECRET, null);
		String buicketName = sharedPreferences.getString(BUCKET_NAME, null);
		if(TextUtils.isEmpty(accessKeyId)
				|| TextUtils.isEmpty(accessKeySecret)
				|| TextUtils.isEmpty(buicketName)){
			MLog.d(TAG, "access key is null");
			sharedPreferences.edit().clear().commit();
			return false;
		}
		
		decode(accessKeyId, accessKeySecret, buicketName);
		return true;
	}
	private void decode(String accessKeyId, String accessKeySecret, String buicketName) {
		mAccessKeyId = decodeString(accessKeyId);
		mAccessKeySecret = decodeString(accessKeySecret);
		mBucketName = decodeString(buicketName);
		
		MLog.v(TAG, "in decode, and mAccessKeyId is " + mAccessKeyId);
	}
	
	private boolean parseAndSaveKey(JSONObject json){
		String accessKeyId = JsonUtil.getJson(json, ACCESS_KEY_ID);
		String accessKeySecret = JsonUtil.getJson(json, ACCESS_KEY_SECRET);
		String bucketName = JsonUtil.getJson(json, BUCKET_NAME);
		if(TextUtils.isEmpty(accessKeyId)
				|| TextUtils.isEmpty(accessKeySecret)
				|| TextUtils.isEmpty(bucketName)){
			MLog.v(TAG, "parseAndSaveKey, and resulut is bad");
			return false;
		}
		
		SharedPreferences sharedPreferences = TravelApp.appContext.getSharedPreferences(OSS_KEY, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putString(ACCESS_KEY_ID, accessKeyId);
		editor.putString(ACCESS_KEY_SECRET, accessKeySecret);
		editor.putString(BUCKET_NAME, bucketName);
		editor.commit();
		
		decode(accessKeyId, accessKeySecret, bucketName);
		return true;
	}
	
	private void loadKeyFromNet(){
		StringBuilder builder = new StringBuilder();
		builder.append(Constants.Root_Url);
		builder.setLength(Constants.Root_Url.lastIndexOf("/"));
		builder.append("/upload/getBucketName.do");
		String url = builder.toString();
		NetWorkUtil.postForm(TravelApp.appContext, url, new MResponseListener() {
			
			@Override
			protected void onDataFine(JSONObject data) {
				MLog.v(TAG, "loadKeyFromNet, and dataFine");
				if(parseAndSaveKey(data)){
					MLog.v(TAG, "accesskey has already got");
					initOSS();
				}
			}
		}, null);
		MLog.d(TAG, "loadKeyFromNet and url is " + url);
	}
	
	private String decodeString(String str) {
		byte[] baKeyword = new byte[str.length()/2];
		for(int i = 0; i < baKeyword.length; i++)
		{
			try
			{
				baKeyword[i] = (byte)(0xff & Integer.parseInt(str.substring(i*2, i*2+2),16));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			str = new String(baKeyword, "utf-8");//UTF-16le:Not
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		
		String key = "hllx";
		return str.split(key)[1];
	}
}
