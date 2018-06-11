package com.volley;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.travel.lib.utils.MLog;
import com.travel.lib.utils.UserSharedPreference;
import com.volley.Response.ErrorListener;
import com.volley.Response.Listener;
import com.volley.toolbox.HttpHeaderParser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

public class CookiePostRequest extends NormalPostRequest {
	private Context context;
	public CookiePostRequest(Context context, String url, Listener<JSONObject> listener, ErrorListener errorListener,
			Map map) {
		super(context, url, listener, errorListener, map);
		this.context = context;
	}

	private Map<String, String> mHeaders = new HashMap<String, String>();
		
    public void setCookie(String cookie){
        mHeaders.put("Cookie", cookie);
    }
    
    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
    	try {
			String jsonString = new String(response.data,HttpHeaderParser.parseCharset(response.headers));
			String mHeader = response.headers.toString();
            Log.w("LOG","get headers in parseNetworkResponse "+response.headers.toString());
            //使用正则表达式从reponse的头中提取cookie内容的子串
            Pattern pattern = Pattern.compile("Set-Cookie.*?;");
            Matcher m=pattern.matcher(mHeader);
            if(m.find()){
                String cookieFromResponse =m.group();
                Log.w("Cookie","cookie from server "+ cookieFromResponse);
                //去掉cookie末尾的分号
                cookieFromResponse = cookieFromResponse.substring(11,cookieFromResponse.length()-1);
                Log.w("Cookie","cookie substring "+ cookieFromResponse);
                //将cookie字符串添加到jsonObject中，该jsonObject会被deliverResponse递交，调用请求时则能在onResponse中得到
                JSONObject jsonObject = new JSONObject(jsonString);
                jsonObject.put("Cookie",cookieFromResponse);
                Log.w("Cookie","jsonObject "+ jsonObject.toString());
                UserSharedPreference.setCookie(cookieFromResponse);
                setCookie(cookieFromResponse);
                return Response.success(jsonObject,HttpHeaderParser.parseCacheHeaders(response));
            }
			return Response.success(new JSONObject(jsonString),HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (JSONException je) {
			return Response.error(new ParseError(je));
		}
    }
    
    @Override
    public Map getHeaders() throws AuthFailureError {
    	if(mHeaders.size() == 0 || TextUtils.isEmpty(mHeaders.get("Cookie"))){
    		setCookie(UserSharedPreference.getCookie());
    	}
    	MLog.v("Cookie", "getHeaders cookie is " + mHeaders.get("Cookie"));
        return mHeaders;
    }
}
