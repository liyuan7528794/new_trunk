package com.travel.lib.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.travel.Constants;
import com.travel.lib.TravelApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.SoftReference;

/**
 * �?��用于获取关于用户信息的工具类
 * 
 * @author Administrator
 */
public class UserSharedPreference {
	private static final String TAG = "UserSharedPreference";

	private static SoftReference<SharedPreferences> userSharedPreference;

	public static final String USER_ID = "user_id";
	private static final String TOKEN = "token";
	private static final String USER_HEADING = "user_headimg";
	public static final String VERIFY = "verify";
	private static final String NICK_NAME = "user_nickname";
	private static final String MOBILE = "user_mobile";
	private static final String TABLE_NAME = "user";
	private static final String COVER_ID = "user_headimg_coverId";
	private static final String USER_TYPE = "user_type";
	private static final String LIVE_TYPE = "allow_live_type";
	private static volatile boolean USER_IS_LOGIN = false;

	/**
	 * 获取保存用户信息的SharedPreferences *NOTE:* 不要保留此引用， �?��时调用此方法即可
	 */
	public static synchronized SharedPreferences getUserSharedPreference() {
		if (userSharedPreference == null || userSharedPreference.get() == null) {
			userSharedPreference = new SoftReference<SharedPreferences>(generateUserSharedPreference());
		}
		return userSharedPreference.get();
	}

	private static SharedPreferences generateUserSharedPreference() {
		return TravelApp.appContext.getSharedPreferences(TABLE_NAME, Context.MODE_PRIVATE);
	}

	public static void ClearInfo(Activity activity) {
		final SharedPreferences preferences = activity.getSharedPreferences("user", Context.MODE_PRIVATE);
		String user_id = preferences.getString("user_id", "");
		if (user_id == null) {
			return;
		}
		Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
	}

	/**
	 * 保存密码
	 * 
	 * @param password
	 */
	public static void savePassword(String password) {
		getUserSharedPreference().edit().putString("password", password).commit();
	}
	/**
	 * 保存ctsCid，用于显示头像下的id
	 *
	 * @param ctsCid
	 */
	public static void saveCtsCid(String ctsCid) {
		getUserSharedPreference().edit().putString("ctsCid", ctsCid).commit();
	}

	/**
	 * 修改昵称
	 */
	public static void saveNickName(String nickName) {
		getUserSharedPreference().edit().putString(NICK_NAME, nickName).commit();
	}

	/**
	 * 修改昵称
	 */
	public static void saveHeadImg(String headImg) {
		getUserSharedPreference().edit().putString("user_headimg", headImg).commit();
	}

	/**
	 * 保存地址
	 */
	public static void saveAddress(String address) {
		getUserSharedPreference().edit().putString("user_address", address).commit();
	}

	public static String getAddress() {
		return getUserSharedPreference().getString("user_address", "中国");
	}

	/**
	 * 获取密码
	 * 
	 * @return
	 */
	public static String getPassword() {
		return getUserSharedPreference().getString("password", null);
	}

	public static String mCookie;

	public static void setCookie(String cookie) {
		mCookie = cookie;
		getUserSharedPreference().edit().putString("Cookie", cookie).commit();
		MLog.v(TAG, "setCookie and cookie is " + cookie);
	}

	public static String getCookie() {
		if (true) {
			mCookie = getUserSharedPreference().getString("Cookie", "");
		}
		MLog.v(TAG, "getCookie, and Cookie is " + mCookie);
		return mCookie;
	}

	public static void saveCoverId(String corverId) {
		getUserSharedPreference().edit().putString(COVER_ID, corverId).commit();
	}

	/**
	 * 保存用户信息
	 * json对象
	 */
	public static void saveUserInfo(JSONObject userJson) {
		Editor editor = getUserSharedPreference().edit();
		editor.putString("user_id", JsonUtil.getJson(userJson, "id"));
		editor.putString("user_account", JsonUtil.getJson(userJson, "xblxAccount"));
		editor.putString("user_password", JsonUtil.getJson(userJson, "password"));
		editor.putString("user_sex", JsonUtil.getJson(userJson, "sex"));
		editor.putString(NICK_NAME, JsonUtil.getJson(userJson, "nickName"));
		editor.putString("user_status", JsonUtil.getJson(userJson, "status"));
		editor.putString("user_myIntroduction", JsonUtil.getJson(userJson, "myIntroduction"));
		editor.putString(USER_TYPE, JsonUtil.getJson(userJson, USER_TYPE));
		editor.putInt(LIVE_TYPE, JsonUtil.getJsonInt(userJson, "allowLiveType"));
		String imgUrl = JsonUtil.getJson(userJson, "imgUrl");
		if ((imgUrl == null) || (imgUrl != null && imgUrl.length() < 10)) {
			imgUrl = Constants.DefaultHeadImg;
		}
		editor.putString("user_headimg", imgUrl);

		editor.putString(COVER_ID, JsonUtil.getJson(userJson, "headImg"));
		editor.putString("user_type", JsonUtil.getJson(userJson, "userType"));
		editor.putString("user_address", JsonUtil.getJson(userJson, "address"));
		editor.putString(MOBILE, JsonUtil.getJson(userJson, "mobile"));
		editor.putString("user_email", JsonUtil.getJson(userJson, "email"));
		editor.putString("user_ischeckemail", JsonUtil.getJson(userJson, "isCheckemail"));
		editor.putString("user_realName", JsonUtil.getJson(userJson, "realName"));
		editor.putString("user_birthday", JsonUtil.getJson(userJson, "birthday"));
		editor.putString("user_area", JsonUtil.getJson(userJson, "area"));
		editor.putString("user_qq", JsonUtil.getJson(userJson, "qq"));
		editor.putString("user_weixin", JsonUtil.getJson(userJson, "weixin"));
		editor.putString("user_weibo", JsonUtil.getJson(userJson, "weibo"));
		editor.putString("user_updateTime", JsonUtil.getJson(userJson, "updateTime"));
		editor.commit();
	}

	/**
	 * 返回昵称
	 * 
	 * @return
	 */
	public static String getNickName() {
		return getUserSharedPreference().getString(NICK_NAME, getRandomNickName());
	}

	/**
	 * 返回用户类型
	 * 
	 * @return
	 */
	public static String getUserType() {
		return getUserSharedPreference().getString(USER_TYPE, null);
	}

	/**
	 * 反回用户直播类型
	 */
	public static int getLiveType(){
		return getUserSharedPreference().getInt(LIVE_TYPE, 0);
	}

	/**
	 * 获取电话号
	 */
	public static String getMobile() {
		return getUserSharedPreference().getString(MOBILE, null);
	}

	/**
	 * 判断是否登录状�?
	 * 
	 * @return true -- 登录状�?
	 */
	public static boolean isLogin() {
		return USER_IS_LOGIN;
	}

	public static void setIsLogin(boolean isLogin){
		USER_IS_LOGIN = isLogin;
	}

	/**
	 * 判断是否需要改变用户头像
	 */
	public static boolean isChangeHead() {
		if (!"1".equals(getCoverId())) {
			return false;
		}
		return true;
	}

	/**
	 * 头像图片的coverId
	 * 
	 * @return
	 */
	public static String getCoverId() {
		return getUserSharedPreference().getString(COVER_ID, "-1");
	}

	public static String getUserId() {
		return getUserSharedPreference().getString(USER_ID, getRandomUserId());
	}

	public static String getRandomUserId() {
		SharedPreferences sharedPreferences = TravelApp.appContext.getSharedPreferences("RandomUserId",
				Context.MODE_PRIVATE);
		String userId = sharedPreferences.getString("userId", null);
		if (TextUtils.isEmpty(userId)) {
			userId = String.valueOf(Math.round(Math.random() * 10000000) + 100000000);
			sharedPreferences.edit().putString("userId", userId).commit();
		}
		return userId;
	}

	private static String getRandomNickName() {
		SharedPreferences sharedPreferences = TravelApp.appContext.getSharedPreferences("RandomUserId",
				Context.MODE_PRIVATE);
		String nickName = sharedPreferences.getString("nickName", null);
		if (TextUtils.isEmpty(nickName)) {
			nickName = "游客" + getRandomUserId();
			sharedPreferences.edit().putString("nickName", nickName).commit();
		}
		return nickName;
	}

	/**
	 * 获取verify
	 * 
	 * @return verify字段
	 */
	public static String getVerify() {
		return getUserSharedPreference().getString(VERIFY, null);
	}

	/**
	 * 获取用户头像信息链接地址
	 */
	public static String getUserHeading() {
		if (getUserSharedPreference().getString(USER_HEADING, Constants.DefaultHeadImg).length() < 10) {
			return Constants.DefaultHeadImg;
		}
		return getUserSharedPreference().getString(USER_HEADING, Constants.DefaultHeadImg);
	}

	/**
	 * 获取Bitmap用户头像数据
	 */
	public static Bitmap getUserHeadImg() {
		return ImageDisplayTools.getBitMap(getUserHeading());
	}

	/**
	 * 清空保存用户信息的SharedPreferences
	 */
	public static void clearContent() {
		Editor editor = getUserSharedPreference().edit();
		editor.clear();
		editor.commit();
		USER_IS_LOGIN = false;
	}

	/**
	 * 聊天室接口中需要聊天信息, 这里将此方法抽出来独立成一个方法
	 * 
	 * @return null if jsonException
	 */
	public static String getChatUserJson() {
		SharedPreferences sp = getUserSharedPreference();
		JSONObject json = new JSONObject();
		try {
			String userId = sp.getString("user_id", getRandomUserId());
			json.put("userId", userId);
			String headImg = sp.getString("user_headimg", Constants.DefaultHeadImg);
			json.put("imgUrl", headImg);
			json.put("nickName", sp.getString("user_nickname", getRandomNickName()));
			json.put("system", OSUtil.getOSVersion());
			json.put("ip", OSUtil.getLocalIpAddress());
			json.put("model", OSUtil.getPhoneModel());
			json.put("network", CheckNetStatus.checkNetworkConnection());
			return json.toString();
		} catch (JSONException e) {
			MLog.v(TAG, e.getMessage());
			return null;
		}
	}
}
