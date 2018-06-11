package com.travel.lib.utils;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 时间类的工具类
 */
public class DateFormatUtil {
	private static final String TAG = "DateFormatUtil";

	public static final String FORMAT_DATE = "yyyy-MM-dd";
	public static final String FORMAT_DATE_MM_DD_CN = "MM月dd日";
	public static final String FORMAT_DATE_ZH_CN = "yyyy年MM月dd日";
	public static final String FORMAT_DTAE_TIME_ZH_CN = "yyyy年MM月dd日 HH:mm:ss";
	public static final String FORMAT_TIME = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT_TIME_NO_SECOND = "yyyy-MM-dd HH:mm";
	public static final String FORMAT_DTAE2_TIME2 = "MM月dd日 HH:mm";
	public static final String FORMAT_TIME2 = "HH:mm";
	public static final String FORMAR_HMS = "HH:mm:ss";


	private static SimpleDateFormat getDateFormat(String format) {
		return new SimpleDateFormat(format);
	}

	/**
	 * 格式化时间
	 * 
	 * @param date
	 *            时间
	 * @param pattern
	 *            pattern
	 * @return 对应的字符串
	 */
	public static String formatTime(Date date, String pattern) {
		return getDateFormat(pattern).format(date);
	}

	/**
	 * 获取当前时间的格式化字符串
	 */
	public static String formatCurrenttime(String pattern) {
		return formatTime(new Date(), pattern);
	}

	/**
	 * 解析时间
	 * 
	 * @return 解析出的时间， or null if exception catched.
	 */
	public static Date parseTime(String time, String pattern) {
		try {
			return getDateFormat(pattern).parse(time);
		} catch (ParseException e) {
			MLog.e(TAG, e.getMessage());
			return null;
		}
	}

	/**
	 * 将本地的时间转化为UTC时间
	 * 
	 * @param date
	 *            本地时间
	 * @return
	 */
	public static Date localTime2UTCTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
		// 取得夏令时差
		int dstOffset = calendar.get(Calendar.DST_OFFSET);
		calendar.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		return calendar.getTime();
	}

	public static String longMillisecondsFormat(long milliseconds){
		Date date = new Date(milliseconds);
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+000"));
		return dateFormat.format(date);
	}


	/**
	 * 将UTC时间转化为本地时间
	 * 
	 * @param date
	 *            UTC时间
	 * @return
	 */
	public static Date UTCTime2LocalTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
		int dstOffset = calendar.get(Calendar.DST_OFFSET);
		calendar.add(Calendar.MILLISECOND, +(zoneOffset + dstOffset));
		return calendar.getTime();
	}

	/**
	 * 计算两个日期的月份差(格式里不能包含汉字)
	 * 
	 * @param smallDate
	 * @param bigDate
	 * @param pattern
	 * @return int
	 * @throws ParseException
	 */
	public static int getMonthSpace(String smallDate, String bigDate, String pattern) throws ParseException {

		int resultYear = 0;
		int resultMonth = 0;

		SimpleDateFormat sdf = new SimpleDateFormat(pattern);

		Calendar small = Calendar.getInstance();
		Calendar big = Calendar.getInstance();

		small.setTime(sdf.parse(smallDate));
		big.setTime(sdf.parse(bigDate));

		resultYear = big.get(Calendar.YEAR) - small.get(Calendar.YEAR);

		resultMonth = resultYear * 12 - small.get(Calendar.MONTH) + big.get(Calendar.MONTH);

		return resultMonth;
	}

	public static String longToStringByhhmmss(long seconds) {
		int h = (int) seconds / 3600;
		int m = (int) (seconds % 3600) / 60;
		int s = (int) seconds % 60;
		if (h == 0) {
			return String.format("%02d:%02d", m, s);
		}else
			return String.format("%d:%02d:%02d", h, m, s);
	}

	public static Long getLongByStringDate(String format,String date){
		SimpleDateFormat sdf= new SimpleDateFormat(format);
		try {
			Date dt = sdf.parse(date);
			return dt.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			return 0L;
		}
	}

	/**
	 * 已经过去多长时间了
	 * @return
     */
	public static String getPastTime(String oldTime){
		if (TextUtils.isEmpty(oldTime)) {
			return "";
		}
		long time = DateFormatUtil.getLongByStringDate(DateFormatUtil.FORMAT_TIME, oldTime);
		String str = "";
		Date oldDate = new Date(time);
		Date curDate = new Date();
		long curTime = curDate.getTime();
		long s = (curTime - time) / 1000; //秒
		if(s>=60*60 && s<=48*60*60 && curDate.getDay()-oldDate.getDay() == 1){
			str = "昨天";
//			str = "昨天" + formatTime(new Date(time), FORMAR_HMS);
			return str;
		}else if(s>=60*60 && s<=48*60*60 && curDate.getDay()-oldDate.getDay() == 2){
			str = "前天";
//			str = "前天" + formatTime(new Date(time), FORMAR_HMS);
			return str;
		}

		if(s < 30){ // 30s内
			str = "刚刚";
		}else if(s>=30 && s<60){ // 30s 到60s
			str = s + "秒前";
		}else if(s>=60 && s<60*60){ // 1m到1h
			str = s/60 + "分钟前";
		}else if(s>=60*60 && s<12*60*60){ // 1h 到 半天
			str = s/60/60 + "小时前";
		}else if(s>=12*60*60 && s<24*60*60){ // 半天 到 1天
			str = formatTime(new Date(time), FORMAT_TIME2);
		}else if(s>=24*60*60){ // 显示年月日时分秒
			str = formatTime(new Date(time), FORMAT_DATE_MM_DD_CN);
		}

		return str;
	}

	/**
	 * 模仿 QQ聊天 的时间格式化
	 * @param oldTime
	 * @param formatStr
     * @return
     */
	public static String getChatTime(long oldTime, String formatStr){
		String str = "";
		Date oldDate = new Date(oldTime);
		Date curDate = new Date();
		long curTime = curDate.getTime();
		long s = (curTime - oldTime) / 1000; //秒
		if(s>=60*60 && curDate.getDay()==oldDate.getDay()){
			str = formatTime(new Date(oldTime), FORMAT_TIME2);
			return str;
		}else if(s>=60*60 && curDate.getDay()-oldDate.getDay() == 1){
			str = "昨天";
			return str;
		}else if(curDate.getDay()-oldDate.getDay() > 1 && curDate.getDay()-oldDate.getDay() < 5){
			str = getWeek(oldDate);
			return str;
		}

		if(s < 30){ // 30s内
			str = "刚刚";
		}else if(s>=30 && s<60){ // 30s 到60s
			str = s + "秒前";
		}else if(s>=60 && s<60*60){ // 1m到1h
			str = s/60 + "分钟前";
		}else{ // 显示年月日时分秒
			str = formatTime(new Date(oldTime), FORMAT_DATE);
		}

		return str;
	}

	public static String getWeek(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		String week = sdf.format(date);
		return week;
	}

	public static String getDate_M_D(String date){
		String slipDate = date;
		if(date!=null && date.contains("年") && date.contains("日"))
//			slipDate = date.substring(date.indexOf("年")-2, date.indexOf("日")+1);
			slipDate = date.substring(date.indexOf("年")+1);
		else if(date!=null && date.contains("-")){
			slipDate = date.substring(date.indexOf("-")+1);
			if(slipDate.contains(" +0000"))
				slipDate = slipDate.replace(" +0000", "");
		}
		return slipDate;
	}
}
