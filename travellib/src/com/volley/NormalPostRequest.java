package com.volley;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.UserSharedPreference;
import com.volley.Response.ErrorListener;
import com.volley.Response.Listener;
import com.volley.toolbox.HttpHeaderParser;

public class NormalPostRequest extends Request<JSONObject> {
	private Map<String, String> mMap;
	private Listener<JSONObject> mListener;
	private Context context;
	
	// TODO: 去掉context参数， 使用其它机制， 否则影响GC
	public NormalPostRequest(Context context, String url,
			Listener<JSONObject> listener, ErrorListener errorListener, Map map) {
		super(Request.Method.POST, url, errorListener);
		this.context = context;
		mListener = listener;
		mMap = new HashMap<String, String>();
		if (map != null) {
			for (Object key : map.keySet()) {
				Object value = map.get(key);
				String vstr = "";
				if(value == null){
					Log.e("NormalPostRequest -- ", "The value of map is empty");
				}else{
					vstr = value.toString();
				}
				mMap.put(key.toString(), vstr);
			}
		}

	}
	
	public NormalPostRequest(Context context, String url, 
			MResponseListener listener, Map<String, Object> map) {
		this(context, url, listener, listener, map);
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> map = new HashMap<String, String>();
		map.put(UserSharedPreference.VERIFY, UserSharedPreference.getVerify());
		return map;
	}

	// mMap是已经按照前面的方式,设置了参数的实例
	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return mMap;
	}

	// 此处因为response返回值需要json数据,和JsonObjectRequest类一样即可
	@Override
	protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
		try {
			String jsonString = new String(response.data,
					HttpHeaderParser.parseCharset(response.headers));
//			if (jsonString.contains("验证信息错误")) {
//				UserSharedPreference.clearContent();
//				Intent intent = new Intent(context, LoginActivity.class);
//				intent.putExtra(LoginActivity.MSG, "登录已过期，请重新登陆");
//				context.startActivity(intent);
//				return Response.error(null);
//			}

			return Response.success(new JSONObject(jsonString),
					HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (JSONException je) {
			return Response.error(new ParseError(je));
		}
	}
	
	@Override
	protected void deliverResponse(JSONObject response) {
		mListener.onResponse(response);
	}
}
