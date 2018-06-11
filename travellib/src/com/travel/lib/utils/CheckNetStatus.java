package com.travel.lib.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.travel.lib.TravelApp;

public class CheckNetStatus {
	private static final String TAG = "CheckNetStatus";
	
	//网络状况标识
	public final static String unNetwork = "UN_NET";//没网
	public final static String wifiNetwork = "WIFI_NET";//wifi网络
	public final static String mobileNetwork = "UNNET";//移动网络
	public final static String MOBIBLE_NET_3G = "3G";
	public static final String MOBIBLE_NET_2G = "2G";
	public static final String MOBIBLE_NET_4G = "4G"; 
	
	
	/**
	 * 查看当前网络状况
	 */
	public static String checkNetworkConnection(){
	    final ConnectivityManager connMgr = 
	    		(ConnectivityManager)TravelApp.appContext.getSystemService(Context.CONNECTIVITY_SERVICE);   
	    final NetworkInfo wifi =connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    final NetworkInfo mobile =connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);   
	    if(wifi.isConnected()){
	    	return wifiNetwork;
	    }else if(!wifi.isConnected() && mobile.isConnected()){
	        return getNetworkType(mobile);   
	    }else if(!wifi.isConnected() && !mobile.isConnected()){
	    	return unNetwork;
	    }
		return unNetwork;
	} 
	
	private static String getNetworkType(NetworkInfo networkInfo){
	    String strNetworkType = "";
	   
	    if (networkInfo != null && networkInfo.isConnected())
	    {
	        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
	        {
	            String _strSubTypeName = networkInfo.getSubtypeName();
	            
	            Log.e(TAG, "Network getSubtypeName : " + _strSubTypeName);
	            
	            // TD-SCDMA   networkType is 17
	            int networkType = networkInfo.getSubtype();
	            switch (networkType) {
	                case TelephonyManager.NETWORK_TYPE_GPRS:
	                case TelephonyManager.NETWORK_TYPE_EDGE:
	                case TelephonyManager.NETWORK_TYPE_CDMA:
	                case TelephonyManager.NETWORK_TYPE_1xRTT:
	                case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
	                    strNetworkType = MOBIBLE_NET_2G;
	                    break;
	                case TelephonyManager.NETWORK_TYPE_UMTS:
	                case TelephonyManager.NETWORK_TYPE_EVDO_0:
	                case TelephonyManager.NETWORK_TYPE_EVDO_A:
	                case TelephonyManager.NETWORK_TYPE_HSDPA:
	                case TelephonyManager.NETWORK_TYPE_HSUPA:
	                case TelephonyManager.NETWORK_TYPE_HSPA:
	                case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
	                case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
	                case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
	                    strNetworkType = MOBIBLE_NET_3G;
	                    break;
	                case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
	                    strNetworkType = MOBIBLE_NET_4G;
	                    break;
	                default:
	                    // http://baike.baidu.com/item/TD-SCDMA 中国移动 移动 电信 三种3G制式
	                    if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) 
	                    {
	                        strNetworkType = MOBIBLE_NET_3G;
	                    }
	                    else
	                    {
	                        strNetworkType = _strSubTypeName;
	                    }
	                    
	                    break;
	             }
	             
	            Log.e(TAG, "Network getSubtype : " + Integer.valueOf(networkType).toString());
	        }
	    }
	    
	    Log.e(TAG, "Network Type : " + strNetworkType);
	    
	    return strNetworkType;
	}
}
