package com.travel.communication.helper;

import java.util.Calendar;
import java.util.Date;

import com.travel.lib.utils.DateFormatUtil;

/**
 * 格式化时间的辅助类
 * @author ldkxingzhe
 * 时间常用的格式化可以使用DataFormatUtil工具类
 * 这个辅助类主要用于聊天中对消息发送时间的展示:
 * 早上, 下午, 中午, 昨天, 前天, 普通时间
 * 
 * TODO: 考虑是否将其作为辅助类
 */
public class DateParseHelper {
	private static final String TAG = "DateParseHelper";
	private static final String TIME = "HH时mm分";
	
	public String parserTime(Date date){
		Calendar currentCalendar = Calendar.getInstance();
		Date localDate = DateFormatUtil.UTCTime2LocalTime(date);
		currentCalendar.setTime(date);
		Calendar todayZero = Calendar.getInstance();
		todayZero.setTime(new Date());
		todayZero.set(Calendar.HOUR_OF_DAY, 0);
		todayZero.set(Calendar.MINUTE, 0);
		todayZero.set(Calendar.SECOND, 0);
		String format = "";
		if(currentCalendar.after(todayZero)){
			format = TIME;
		}else{
			format = "MM月dd日 HH时mm分";
		}
		return DateFormatUtil.formatTime(localDate, format);
	}
}
