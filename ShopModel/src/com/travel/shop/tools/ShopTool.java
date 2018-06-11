package com.travel.shop.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.travel.Constants;
import com.travel.ShopConstant;
import com.travel.bean.VideoInfoBean;
import com.travel.layout.DialogTemplet;
import com.travel.layout.DialogTemplet.DialogLeftButtonListener;
import com.travel.layout.DialogTemplet.DialogRightButtonListener;
import com.travel.lib.TravelApp;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;
import com.travel.shop.bean.OrderBean;
import com.travel.shop.http.OrderInfoHttp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 第二个选项卡用到的工具类
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/02
 */
public class ShopTool {

    private static DialogTemplet dialog;
    private static Resources rs;


    /**
     * 判断收费方式
     *
     * @param chargeMode 1.线路游 元/人
     *                   2.自由行 元/人
     *                   3.宾馆客栈 元/间/天
     *                   4.向导服务 元/天
     *                   5.门票 元/张
     *                   6.卡劵 元/张
     * @return
     */
    public static String getChargeMode(int chargeMode) {

        String result = "";
        switch (chargeMode) {
            case 1:
            case 2:
                result = "人";
                break;
            case 3:
                result = "间/天";
                break;
            case 4:
                result = "天";
                break;
            case 5:
            case 6:
                result = "张";
                break;
        }
        return result;
    }

    /**
     * 根据给定的价格来判断是否显示小数
     *
     * @param money
     * @return 小数点后是0，则不显示，反之，则显示
     */
    public static String getMoney(String money) {
        if (!TextUtils.isEmpty(money)) {
            float moneyF = Float.parseFloat(money);
            int moneyI = (int) moneyF;// 整数部分
            float moneyFl = moneyF - moneyI;// 小数部分
            DecimalFormat df = new DecimalFormat();
            String style = "0.00";
            df.applyPattern(style);
            return moneyFl == 0 ? moneyI + "" : df.format(moneyF);
        } else
            return "0";
    }

    /**
     * 用于文本分段
     *
     * @param text
     * @return
     */
    public static String paraBreak(String text, String tag) {
        String[] paraArray = text.split("\\n");

        String result = "";
        for (int i = 0; i < paraArray.length; i++) {
            String data = paraArray[i].replace("　", " ").trim();
            if (!"".equals(data)) {
                // 最后一行不加“\n”
                if ("".equals(tag)) {
                    result += data + (i == paraArray.length - 1 ? "" : "\n");
                } else {
                    result += "\u3000\u3000" + data + (i == paraArray.length - 1 ? "" : "\n");
                }
            }
        }
        return result;
    }

    /**
     * "2016年 03月11日" → "2016-03-11"(转换前有空格)
     *
     * @param word
     * @return
     */
    public static String wordChangeSignSpace(String word) {
        String year = word.substring(0, 4);
        String month = word.substring(6, 8);
        String day = word.substring(9, 11);
        return year + "-" + month + "-" + day;
    }

    /**
     * "2016年03月11日" → "2016-03-11"(转换无空格)
     *
     * @param word
     * @return
     */
    public static String wordChangeSign(String word) {
        String year = word.substring(0, 4);
        String month = word.substring(5, 7);
        String day = word.substring(8, 10);
        return year + "-" + month + "-" + day;
    }

    /**
     * "2016-03-11" → "2016年03月11日"
     *
     * @param sign
     * @return
     */
    public static String signChangeWord(String sign) {
        String year = sign.substring(0, 4);
        String month = sign.substring(5, 7);
        String day = sign.substring(8, 10);
        return year + "年" + month + "月" + day + "日";
    }

    /**
     * 判断是否是卖家
     *
     * @param id 卖家的userId
     * @return true 卖家 false 买家
     */
    public static boolean isSeller(String id) {
        if (TextUtils.equals(UserSharedPreference.getUserId(), id))
            return true;
        else
            return false;
    }

    /**
     * 判断是否是买家
     *
     * @param id 买家的userId
     * @return true 买家 false 卖家
     */
    public static boolean isBuyer(String id) {
        if (TextUtils.equals(UserSharedPreference.getUserId(), id))
            return true;
        else
            return false;
    }

    /**
     * 获取定金
     *
     * @param unit  单价
     * @param order 定金比例
     * @param count 商品数
     * @return
     */
    public static String getOrder(float unit, int order, int count) {
        return getMoney(unit + "") + "*" + (count == 1 ? "" : count + "*") + order + "%="
                + getMoney((unit * count * order / 100.0) + "");
    }

    public static String PicControl(String path, String bitName) throws Exception {
        // 1.加载位图
        InputStream is = new FileInputStream(path);
        // 2.为位图设置100K的缓存
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTempStorage = new byte[100 * 1024];
        // 3.设置位图颜色显示优化方式
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        // 4.设置图片可以被回收，创建Bitmap用于存储Pixel的内存空间在系统内存不足时可以被回收
        opts.inPurgeable = true;
        // 5.设置位图缩放比例
        opts.inSampleSize = 8;
        // 6.设置解码位图的尺寸信息
        opts.inInputShareable = true;
        // 7.解码位图
        Bitmap btp = BitmapFactory.decodeStream(is, null, opts);
        // 8.保存到指定路径
        File f = new File(ShopConstant.SMALL_IMAGE_CACHE);
        if (!f.exists())
            f.mkdirs();
        f = new File(ShopConstant.SMALL_IMAGE_CACHE + bitName + ".png");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        btp.compress(Bitmap.CompressFormat.PNG, 80, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ShopConstant.SMALL_IMAGE_CACHE + bitName + ".png";
    }

    /**
     * 将map对象转成json字符串
     *
     * @param jsonMap
     * @return json字符串
     */
    public static String mapToJson(Object jsonMap) {
        Gson gson = new Gson();
        String jsonString = "";
        try {
            jsonString = gson.toJson(jsonMap, jsonMap.getClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    /**
     * 获取剩余时间
     *
     * @param createTime
     * @param currentTime
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static long getTimeSecond(String createTime, String currentTime) {
        SimpleDateFormat format = new SimpleDateFormat(DateFormatUtil.FORMAT_TIME);
        try {
            // 毫秒ms
            long diff = ShopConstant.PUBLIC_VOTE_DAY * 24 * 60 * 60 * 1000
                    - (format.parse(currentTime).getTime() - format.parse(createTime).getTime());
            return diff < 0 ? 0 : diff;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 将秒转成“00:00:00”的形式
     *
     * @param time
     * @return
     */
    public static String secondToTime(String time) {
        String result;
        if (TextUtils.isEmpty(time)) {
            result = "00:00";
        } else {
            int second = 0;
            if (time.contains("."))
                second = Integer.parseInt(time.split("\\.")[0]);
            else
                second = Integer.parseInt(time);

            if (second < 60) {
                result = "00:" + isAdd0(second);
            } else if (second < 3600) {
                int min = second / 60;
                int sec = second % 60;
                result = isAdd0(min) + ":" + isAdd0(sec);
            } else {
                int hour = second / 3600;
                int min = second % 3600 / 60;
                int sec = second % 3600 % 60;
                result = isAdd0(hour) + ":" + isAdd0(min) + ":" + isAdd0(sec);
            }
        }

        return result;
    }

    /**
     * 判断给定的时间是否大于10
     *
     * @param count
     * @return 小于10：0x
     * 大于等于10：x
     */
    private static String isAdd0(int count) {
        String result = "";
        if (count < 10)
            result = "0" + count;
        else
            result = count + "";
        return result;
    }

    /**
     * 将时间转换成xx天xx小时xx分的形式
     *
     * @param time
     * @return
     */
    public static String getTimeDay(long time) {
        long miniteTotal = time / (60 * 1000);// 总分数
        long day = miniteTotal / (24 * 60);
        long hour = (miniteTotal - day * 24 * 60) / 60;
        long minite = (miniteTotal - day * 24 * 60) % 60;
        return day + "天" + hour + "小时" + minite + "分";
    }

    /**
     * 获取两个日期的时间差
     *
     * @param startTime
     * @param refundTime
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getTime(String startTime, String refundTime, String flag, Context context) {
        String time = "";
        SimpleDateFormat format = new SimpleDateFormat(DateFormatUtil.FORMAT_TIME);
        try {
            // 毫秒ms
            long diff = format.parse(startTime + " 00:00:00").getTime()
                    - format.parse("day".equals(flag) ? refundTime + " 00:00:00" : refundTime).getTime();
            long diffHours = diff / (60 * 60 * 1000);
            if (diffHours < 0) {
                time = "0";
            } else {
                time = diffHours + "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hour2time(Integer.parseInt(time), flag, context);
    }

    /**
     * 获取具体的时间差
     *
     * @param hour
     * @return
     */
    public static String hour2time(int hour, String flag, Context context) {
        return "day".equals(flag) ? hour / 24 + "天"
                : (context.getResources().getString(R.string.orderinfo_service_date_remainder) + hour / 24
                + "天" + hour % 24 + "小时");
    }

    /**
     * 格式化显示的时间
     *
     * @param time
     * @return
     */
    public static String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes,
                seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * 询问用户是否删除图片
     *
     * @param mContext
     */
    public static void deletePicture(final Context mContext) {
        rs = mContext.getResources();
        dialog = new DialogTemplet(mContext, false, rs.getString(R.string.evaluate_delete_picture), "",
                rs.getString(R.string.cancle), rs.getString(R.string.sure));
        dialog.show();
        dialog.setLeftClick(new DialogLeftButtonListener() {

            @Override
            public void leftClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setRightClick(new DialogRightButtonListener() {

            @Override
            public void rightClick(View view) {
                // 发送修改主界面图片展示的广播
                Intent intentPlay = new Intent();
                intentPlay.setAction("delete");
                mContext.sendBroadcast(intentPlay);
                dialog.dismiss();
            }
        });
    }

    /**
     * 设置LinearLayout中View的宽高(w:h=1:1)
     *
     * @param v
     */
    public static void setLL1w1h(View v) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
        params.width = OSUtil.getScreenWidth() / 7;
        params.height = params.width;
        v.setLayoutParams(params);
    }

    /**
     * 设置LinearLayout中View的宽高(w:h=3:2)
     *
     * @param v
     */
    public static void setLL345w2h193(View v, int padding) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
        params.width = OSUtil.getScreenWidth() - padding;
        params.height = params.width * 193 / 345;
        v.setLayoutParams(params);
    }

    /**
     * 设置RelativeLayout中View的宽高(w:h=3:2)
     *
     * @param v
     */
    public static void setRL3w2h(View v) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.height = v.getHeight();
        params.width = v.getHeight() * 3 / 2;
        v.setLayoutParams(params);
    }

    /**
     * 设置RelativeLayout中view的宽高
     *
     * @param v
     */
    public static void setRLParamsWidth(View v, int widthR, int heightR, int margin) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.width = OSUtil.getScreenWidth() - OSUtil.dp2px(TravelApp.appContext, margin);
        params.height = params.width * heightR / widthR;
        v.setLayoutParams(params);
    }

    /**
     * 设置LinearLayout中view的宽高
     *
     * @param v
     */
    public static void setLLParamsWidth(View v, int widthR, int heightR, int margin) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
        params.width = OSUtil.getScreenWidth() - OSUtil.dp2px(TravelApp.appContext, margin);
        params.height = params.width * heightR / widthR;
        v.setLayoutParams(params);
        v.measure(params.width, params.height);
    }

    /**
     * 设置LinearLayout中view的等分宽,并且宽高比为 x : y
     *
     * @param v
     * @param part   分成几等分
     * @param margin 边距
     * @param x      宽比例
     * @param y      高比例
     */
    public static void setLLParamsWidthPart(View v, int part, int margin, int x, int y) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
        params.width = (OSUtil.getScreenWidth() - OSUtil.dp2px(TravelApp.appContext, margin)) / part;
        params.height = params.width * y / x;
        v.setLayoutParams(params);
    }

    /**
     * 设置RelativeLayout中view的等分宽,并且宽高比为 x : y
     *
     * @param v
     * @param part   分成几等分
     * @param margin 边距
     * @param x      宽比例
     * @param y      高比例
     */
    public static void setRLParamsWidthPart(View v, int part, int margin, int x, int y) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.width = (OSUtil.getScreenWidth() - OSUtil.dp2px(TravelApp.appContext, margin)) / part;
        params.height = params.width * y / x;
        v.setLayoutParams(params);
    }


    /**
     * 设置FrameLayout中Video的View的宽高(w:h=1:1)
     *
     * @param v
     */
    public static void setLL1w1hVideo(View v) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
        params.width = (OSUtil.getScreenWidth() - OSUtil.dp2px(TravelApp.appContext, 42)) / 2;
        params.height = params.width;
        v.setLayoutParams(params);
    }

    /**
     * 判断字符串中是否含有emoJi表情图
     *
     * @param source
     * @return
     */
    public static boolean containsEmoji(String source) {
        int len = source.length();
        boolean isEmoji = false;
        for (int i = 0; i < len; i++) {
            char hs = source.charAt(i);
            if (0xd800 <= hs && hs <= 0xdbff) {
                if (source.length() > 1) {
                    char ls = source.charAt(i + 1);
                    int uc = ((hs - 0xd800) * 0x400) + (ls - 0xdc00) + 0x10000;
                    if (0x1d000 <= uc && uc <= 0x1f77f) {
                        return true;
                    }
                }
            } else {
                // non surrogate
                if (0x2100 <= hs && hs <= 0x27ff && hs != 0x263b) {
                    return true;
                } else if (0x2B05 <= hs && hs <= 0x2b07) {
                    return true;
                } else if (0x2934 <= hs && hs <= 0x2935) {
                    return true;
                } else if (0x3297 <= hs && hs <= 0x3299) {
                    return true;
                } else if (hs == 0xa9 || hs == 0xae || hs == 0x303d || hs == 0x3030 || hs == 0x2b55 || hs == 0x2b1c
                        || hs == 0x2b1b || hs == 0x2b50 || hs == 0x231a) {
                    return true;
                }
                if (!isEmoji && source.length() > 1 && i < source.length() - 1) {
                    char ls = source.charAt(i + 1);
                    if (ls == 0x20e3) {
                        return true;
                    }
                }
            }
        }
        return isEmoji;
    }

    /**
     * 点击播放视频
     */
    public static void play(VideoInfoBean mVideoBean, Context mContext, int sourceType) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("video_info", mVideoBean);
        if (!TextUtils.isEmpty(mVideoBean.getPersonalInfoBean().getLiveId()))
            bundle.putString("liveId", mVideoBean.getPersonalInfoBean().getLiveId());
        if (sourceType == 1)// 边看边买没有购物车
            bundle.putString("intent_source", "shop");
        String netType = CheckNetStatus.checkNetworkConnection();
        if (CheckNetStatus.unNetwork.equals(netType)) {// 没网
            TravelUtil.showToast(mContext.getResources().getString(R.string.net_fail));
        } else if (!CheckNetStatus.unNetwork.equals(netType) && !CheckNetStatus.wifiNetwork.equals(netType)) {
            if ("UNKNOWN".equals(netType)) {
                TravelUtil.showToast(mContext.getResources().getString(R.string.net_fail));
            } else {
                AlertDialogUtils.netNotifyDialog(netType, bundle, mContext);
            }
        } else if (CheckNetStatus.wifiNetwork.equals(netType)) {
            Intent intent = new Intent();
            boolean isLiveId = !TextUtils.isEmpty(mVideoBean.getPersonalInfoBean().getLiveId());
            intent.setAction(isLiveId ? Constants.LIVE_VIDEO_ACTION : (mVideoBean.getVideoStatus() == 1 ? Constants.LIVE_VIDEO_ACTION : Constants.VIDEO_ACTION));
            intent.setType(Constants.VIDEO_TYPE);
            intent.putExtras(bundle);
            mContext.startActivity(intent);
        }

    }

    /**
     * 判断当前字符串是否是HTML
     *
     * @param str
     * @return
     */
    public static boolean isHtml(String str) {
        if (TextUtils.isEmpty(str))
            return false;
        String[] arr = new String[]{"div", "<p>", "<br/>", "<br>"};
        for (String s : arr) {
            if (str.contains(s))
                return true;
        }
        return false;
    }

    /**
     * 功能：身份证的有效验证
     *
     * @param IDStr 身份证号
     * @return 有效：返回"" 无效：返回String信息
     */
    public static boolean IDCardValidate(String IDStr) {
        String errorInfo = "";// 记录错误信息
        String[] ValCodeArr = {"1", "0", "x", "9", "8", "7", "6", "5", "4",
                "3", "2"};
        String[] Wi = {"7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7",
                "9", "10", "5", "8", "4", "2"};
        String Ai = "";
        // ================ 号码的长度 15位或18位 ================
        if (IDStr.length() != 15 && IDStr.length() != 18) {
            errorInfo = "身份证号码长度应该为15位或18位。";
            return false;
        }
        // =======================(end)========================

        // ================ 数字 除最后以为都为数字 ================
        if (IDStr.length() == 18) {
            Ai = IDStr.substring(0, 17);
        } else if (IDStr.length() == 15) {
            Ai = IDStr.substring(0, 6) + "19" + IDStr.substring(6, 15);
        }
        if (isNumeric(Ai) == false) {
            errorInfo = "身份证15位号码都应为数字 ; 18位号码除最后一位外，都应为数字。";
            return false;
        }
        // =======================(end)========================

        // ================ 出生年月是否有效 ================
        String strYear = Ai.substring(6, 10);// 年份
        String strMonth = Ai.substring(10, 12);// 月份
        String strDay = Ai.substring(12, 14);// 月份
        if (isDataFormat(strYear + "-" + strMonth + "-" + strDay) == false) {
            errorInfo = "身份证生日无效。";
            return false;
        }
        GregorianCalendar gc = new GregorianCalendar();
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) > 150
                    || (gc.getTime().getTime() - s.parse(
                    strYear + "-" + strMonth + "-" + strDay).getTime()) < 0) {
                errorInfo = "身份证生日不在有效范围。";
                return false;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        if (Integer.parseInt(strMonth) > 12 || Integer.parseInt(strMonth) == 0) {
            errorInfo = "身份证月份无效";
            return false;
        }
        if (Integer.parseInt(strDay) > 31 || Integer.parseInt(strDay) == 0) {
            errorInfo = "身份证日期无效";
            return false;
        }
        // =====================(end)=====================

        // ================ 地区码时候有效 ================
        Hashtable h = GetAreaCode();
        if (h.get(Ai.substring(0, 2)) == null) {
            errorInfo = "身份证地区编码错误。";
            return false;
        }
        // ==============================================

        return true;
    }

    /**
     * 功能：判断字符串是否为数字
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (isNum.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 功能：设置地区编码
     *
     * @return Hashtable 对象
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Hashtable GetAreaCode() {
        Hashtable hashtable = new Hashtable();
        hashtable.put("11", "北京");
        hashtable.put("12", "天津");
        hashtable.put("13", "河北");
        hashtable.put("14", "山西");
        hashtable.put("15", "内蒙古");
        hashtable.put("21", "辽宁");
        hashtable.put("22", "吉林");
        hashtable.put("23", "黑龙江");
        hashtable.put("31", "上海");
        hashtable.put("32", "江苏");
        hashtable.put("33", "浙江");
        hashtable.put("34", "安徽");
        hashtable.put("35", "福建");
        hashtable.put("36", "江西");
        hashtable.put("37", "山东");
        hashtable.put("41", "河南");
        hashtable.put("42", "湖北");
        hashtable.put("43", "湖南");
        hashtable.put("44", "广东");
        hashtable.put("45", "广西");
        hashtable.put("46", "海南");
        hashtable.put("50", "重庆");
        hashtable.put("51", "四川");
        hashtable.put("52", "贵州");
        hashtable.put("53", "云南");
        hashtable.put("54", "西藏");
        hashtable.put("61", "陕西");
        hashtable.put("62", "甘肃");
        hashtable.put("63", "青海");
        hashtable.put("64", "宁夏");
        hashtable.put("65", "新疆");
        hashtable.put("71", "台湾");
        hashtable.put("81", "香港");
        hashtable.put("82", "澳门");
        hashtable.put("91", "国外");
        return hashtable;
    }

    /**
     * 验证日期字符串是否是YYYY-MM-DD格式
     *
     * @param str
     * @return
     */
    private static boolean isDataFormat(String str) {
        boolean flag = false;
        String str1 = "19\\d\\d-[0-1]\\d-(([0-2]\\d)|(3[0-1]))";
        Pattern pattern1 = Pattern.compile(str1);
        Matcher isNo1 = pattern1.matcher(str);
        String str2 = "20\\d\\d-[0-1]\\d-(([0-2]\\d)|(3[0-1]))";
        Pattern pattern2 = Pattern.compile(str2);
        Matcher isNo2 = pattern2.matcher(str);
        if (isNo1.matches() || isNo2.matches()) {
            flag = true;
        }
        return flag;
    }

    public static String formatTime(long time) {
        return DateFormatUtil.formatTime(new Date(time), "mm:ss");
    }

    /**
     * 获取时间轴的下一步要进行的状态
     *
     * @param orderStatus
     * @param overStatus
     * @return
     */
    public static String getStatusRoute(int orderStatus, int overStatus, Context context) {
        rs = context.getResources();
        String status = "";
        // 进行中
        switch (orderStatus) {
            case 1:// 下单成功(待支付)
                status = rs.getString(R.string.orderinfo_ing_wait_pay);
                break;
            case 2:// 支付成功(待确认)
                status = rs.getString(R.string.orderinfo_ing_wait_sure);
                break;
            case 3:// ①卖家确认(待出行)②无需二次确认--支付成功(待出行)
                status = rs.getString(R.string.orderinfo_ing_wait_start);
                break;
            //            case 4:// 行程开始(出行中)
            //                status = rs.getString(R.string.orderinfo_ing_starting);
            //                break;
            case 4:// 申请退订(退订中)
                status = rs.getString(R.string.orderinfo_ing_unsubscribing);
                break;
            case 5:// 行程结束(待确认付款)
                status = rs.getString(R.string.orderinfo_ing_wait_sure_pay);
                break;
            //            case 7:// 众投申请(众投申请中)
            //                status = rs.getString(R.string.orderinfo_ing_public_invest_applying);
            //                break;
            //            case 8:// 发起众投(众投中)
            //                status = rs.getString(R.string.orderinfo_ing_public_investing);
            //                break;
            default:
                switch (overStatus) {
                    case 1:// 待评价
                        status = rs.getString(R.string.orderinfo_done_wait_evaluate);
                        break;
                    case 2:// 已评价
                        status = rs.getString(R.string.orderinfo_done_evaluate);
                        break;
                    case 3:// 已取消
                        status = "已取消";
                        break;
                    // 商家拒绝
                    case 4:
                        status = rs.getString(R.string.orderinfo_done_seller_refuse);
                        break;
                    // 超时未支付
                    case 5:
                        status = rs.getString(R.string.orderinfo_overtime_pay);
                        break;
                    // 买家退订
                    case 6:
                        status = rs.getString(R.string.orderinfo_done_buyer_unsubscribe);
                        break;
                    //                    // 众投完成
                    //                    case 7:
                    //                    case 8:
                    //                        status = rs.getString(R.string.orderinfo_done_public_invested);
                    //                        break;
                }
                break;
        }
        return status;
    }

    /**
     * 获取时间轴的下一步要进行的状态
     *
     * @param orderStatus
     * @return
     */
    public static String getStatusOrder(int orderStatus, int refundStatus, int publicStatus) {
        String status = "";
        // 进行中
        switch (orderStatus) {
            case OrderBean.STATUS_1:
                status = "待支付";
                break;
            case OrderBean.STATUS_2: //支付成功,等待商家确认
                status = "等待卖家确认";
                break;
            case OrderBean.STATUS_3:
                status = "待出行";
                break;
            case OrderBean.STATUS_4:
                status = "待确认付款";
                break;
            case OrderBean.STATUS_5:
                status = "待评价";
                break;
            case OrderBean.STATUS_6:
                status = "已评价";
                break;
            case OrderBean.STATUS_7:
                status = "买家取消订单";
                break;
            case OrderBean.STATUS_8:
                status = "卖家拒绝订单";
                break;
            case OrderBean.STATUS_9:
                status = "支付超时已取消";
                break;
            case OrderBean.STATUS_10:
                status = "退款成功";
                break;
        }
        //        if (orderStatus < OrderBean.STATUS_7)
        switch (refundStatus) {
            case 1:
                status = "退款中";
                break;
            //                case 2:
            //                    status = "退款成功";
            //                    break;
            //                case 3:
            //                    status = "退款失败";
            //                    break;
        }
        switch (publicStatus) {
            case 1:
                status = "众投申请中";
                break;
            case 2:
                status = "众投进行中";
                break;
            case 3:
                status = "众投成功(买家赢)";
                break;
            case 4:
                status = "众投成功(卖家赢)";
                break;
        }
        return status;
    }

    public static long getSomeTimeMillions(String time) {
        SimpleDateFormat formatter = new SimpleDateFormat(DateFormatUtil.FORMAT_TIME);
        Date date = null;
        try {
            date = formatter.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    public static int setSelectableDrawableResource(int viewId, boolean isChecked) {
        int result = 0;
        if (OSUtil.isDayTheme()) {
            if (viewId == R.id.iv_story_collect) {// 故事详情页的收藏
                result = isChecked ? com.travel.lib.R.drawable.icon_story_collect_yes_day : com.travel.lib.R.drawable.icon_story_collect_no_day;
            }
        } else {
            if (viewId == R.id.iv_story_collect) {// 故事详情页的收藏
                result = isChecked ? com.travel.lib.R.drawable.icon_story_collect_yes_night : com.travel.lib.R.drawable.icon_story_collect_no_night;
            }
        }
        return result;
    }

    private static String result;
    private static DialogTemplet dialogTemplet;

    public static void setTwiceSureDialog(final long ordersId, final Context mContext, final int flag, final OrderInfoHttp.ControlOrderSuccessListener controlOrderSuccessListener) {
        switch (flag) {
            case 1:
                result = "您确定接受此订单吗?";
                break;
            case 2:
                result = "您确定满意付款吗?";
                break;
            case 3:
                result = "您确定取消此订单吗?";
                break;
            case 4:
                result = "您确定拒绝此订单吗?";
                break;
            case 5:
                result = "您确定投诉此订单吗?";
                break;
            case 6:
                result = "您确定拒绝此次退款吗?";
                break;
            case 7:
                result = "您确定同意此次退款吗?";
                break;
        }
        dialogTemplet = new DialogTemplet(mContext, false, result, "", "取消", "确定");
        dialogTemplet.setLeftClick(new DialogTemplet.DialogLeftButtonListener() {
            @Override
            public void leftClick(View view) {
                dialogTemplet.dismiss();
            }
        });
        dialogTemplet.setRightClick(new DialogTemplet.DialogRightButtonListener() {
            @Override
            public void rightClick(View view) {
                if (flag < 5)
                    OrderInfoHttp.controlOrders(ordersId, mContext, flag, controlOrderSuccessListener);
                else
                    OrderInfoHttp.refundControl(ordersId, mContext, "", flag, "", controlOrderSuccessListener);

            }
        });
        dialogTemplet.show();
    }
}
