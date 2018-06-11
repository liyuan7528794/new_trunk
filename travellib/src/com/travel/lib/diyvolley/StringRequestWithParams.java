package com.travel.lib.diyvolley;

import java.util.Map;

import com.volley.AuthFailureError;
import com.volley.Response.ErrorListener;
import com.volley.Response.Listener;
import com.volley.toolbox.StringRequest;

/**
 * 仿照Volley的StringRequest请求, 不同之处, 在此类使用的是post方法的表单提交
 * 其他与其相似
 *
 */
public class StringRequestWithParams extends StringRequest {
	private static final String TAG = "PostStringRequest";
	
	private Map<String, String> mMap;
	
	public StringRequestWithParams(int method, String url, Listener<String> listener,
			ErrorListener errorListener, Map<String, String> map) {
		super(method, url, listener, errorListener);
		mMap = map;
	}
	
	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return mMap;
	}
}
