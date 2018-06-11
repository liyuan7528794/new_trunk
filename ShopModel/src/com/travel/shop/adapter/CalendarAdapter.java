package com.travel.shop.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.R;
import com.travel.shop.bean.CalendarBean;
import com.travel.shop.tools.CalendarTool;
import com.travel.shop.tools.ShopTool;

import java.util.ArrayList;

/**
 * 日历的适配器
 *
 * @author WYP
 * @version 1.0
 * @created 2016/06/06
 */
public class CalendarAdapter extends BaseAdapter {
    private boolean isLeapyear = false; // 是否为闰年
    private int daysOfMonth = 0; // 某月的天数
    private int dayOfWeek = 0; // 具体某一天是星期几
    private int lastDaysOfMonth = 0; // 上一个月的总天数
    private Context context;
    private Integer[] dayNumber = new Integer[42]; // 一个gridview中的日期存入此数组中
    private CalendarTool sc = null;

    private String currentYear = "";
    private String currentMonth = "";
    private String currentDay = "";

    private String showYear = ""; // 用于在头部显示的年份
    private String showMonth = ""; // 用于在头部显示的月份

    private DateHolder mHolder;
    private ArrayList<CalendarBean> orderDates = new ArrayList<>();
    private ArrayList<CalendarBean> startDates = new ArrayList<>();// 当前月的出行日期
    private ArrayList<String> startDays = new ArrayList<>();// 当前月的具体出行的日子
    private ArrayList<Integer> positions = new ArrayList<>();// 当前月的出行日期的位置
    private ArrayList<String[]> pricesList = new ArrayList<>();

    private int jumpMonth;

    public CalendarAdapter(Context context, int jumpMonth, int jumpYear, int year_c, int month_c, int day_c,
                           ArrayList<CalendarBean> orderDates, ArrayList<CalendarBean> startDates, ArrayList<Integer> flags) {
        this.context = context;
        sc = new CalendarTool();
        this.orderDates = orderDates;
        this.startDates = startDates;
        this.jumpMonth = jumpMonth;

        int stepYear = year_c + jumpYear;
        int stepMonth = month_c + jumpMonth;
        if (stepMonth > 0) {
            // 往下一个月滑动
            if (stepMonth % 12 == 0) {
                stepYear = year_c + stepMonth / 12 - 1;
                stepMonth = 12;
            } else {
                stepYear = year_c + stepMonth / 12;
                stepMonth = stepMonth % 12;
            }
        } else {
            // 往上一个月滑动
            stepYear = year_c - 1 + stepMonth / 12;
            stepMonth = stepMonth % 12 + 12;
        }

        // 得到当前的年份
        currentYear = String.valueOf(stepYear);
        currentMonth = String.valueOf(stepMonth); // 得到本月
        currentDay = String.valueOf(day_c); // 得到当前日期是哪天

        getCalendar(Integer.parseInt(currentYear), Integer.parseInt(currentMonth));

        // 截取日期的后两位与日历上的日期对比
        for (int i = 0; i < startDates.size(); i++) {
            String day = startDates.get(i).getDate().substring(8, 10);
            if (day.substring(0, 1).equals("0")) {
                day = day.substring(1, 2);
            }
            startDays.add(day);
        }

    }

    public CalendarAdapter(Context context, int year_c, int month_c, int day_c,
                           ArrayList<CalendarBean> orderDates, ArrayList<CalendarBean> startDates, ArrayList<Integer> flags) {
        this.context = context;
        sc = new CalendarTool();
        this.orderDates = orderDates;
        this.startDates = startDates;

        // 得到当前的年份
        currentYear = String.valueOf(year_c);
        currentMonth = String.valueOf(month_c); // 得到本月
        currentDay = String.valueOf(day_c); // 得到当前日期是哪天

        getCalendar(Integer.parseInt(currentYear), Integer.parseInt(currentMonth));

        // 截取日期的后两位与日历上的日期对比
        for (int i = 0; i < startDates.size(); i++) {
            String day = startDates.get(i).getDate().substring(8, 10);
            if (day.substring(0, 1).equals("0")) {
                day = day.substring(1, 2);
            }
            startDays.add(day);
        }

    }


    @Override
    public int getCount() {
        return dayNumber.length;
    }

    @Override
    public Object getItem(int position) {
        return startDates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mHolder = new DateHolder();
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_calendar, null);
        }
        mHolder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
        mHolder.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
        mHolder.tv_start_lable = (TextView) convertView.findViewById(R.id.tv_start_lable);
        mHolder.tv_end_lable = (TextView) convertView.findViewById(R.id.tv_end_lable);
        mHolder.fl_date = (FrameLayout) convertView.findViewById(R.id.fl_date);
        SpannableString sp = new SpannableString(dayNumber[position] + "");
        sp.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, sp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new RelativeSizeSpan(1.2f), 0, sp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (!TextUtils.equals(sp.toString(), "null")) {
            mHolder.tv_date.setText(dayNumber[position] + "");
            if (position < daysOfMonth + dayOfWeek && position >= dayOfWeek) {
                // 在当月的日期以后进行操作
                if (Integer.parseInt(currentDay) == 0 ||
                        (Integer.parseInt(currentDay) != 0 && Integer.parseInt(currentDay) <= dayNumber[position])) {
                    for (int day = 0; day < startDays.size(); day++) {
                        // 当日期是可选的出行日期的时候
                        if (startDays.get(day).equals(dayNumber[position] + "")) {
                            // 成人价
                            pricesList.get(position)[0] = ShopTool.getMoney(startDates.get(day).getAdult_price());
                            // 儿童价
                            pricesList.get(position)[1] = ShopTool.getMoney(startDates.get(day).getChildren_price());
                            // 单房差
                            pricesList.get(position)[2] = ShopTool.getMoney(startDates.get(day).getSingle_room_price());
                            // 日历Id
                            pricesList.get(position)[3] = startDates.get(day).getCalendarId();
                            // 记录可选日期列表中的游标
                            pricesList.get(position)[4] = startDates.get(day).getIndex() + "";
                            positions.add(position);
                            mHolder.tv_price.setText("￥" + ShopTool.getMoney(startDates.get(day).getAdult_price()));
                            mHolder.tv_price.setVisibility(View.VISIBLE);
                            boolean isStartCheck = orderDates.get(startDates.get(day).getIndex()).isStartCheck();
                            boolean isEndCheck = orderDates.get(startDates.get(day).getIndex()).isEndCheck();
                            boolean isCheckBox = orderDates.get(startDates.get(day).getIndex()).isCheckBox();
                            // 日期标识
                            mHolder.tv_start_lable.setVisibility(View.GONE);
                            mHolder.tv_end_lable.setVisibility(View.GONE);
                            if (!isStartCheck && !isEndCheck) {
                                if (OSUtil.isDayTheme()) {
                                    convertView.setBackgroundColor(Color.WHITE);
                                    mHolder.tv_date.setTextColor(ContextCompat.getColor(context, R.color.black_6C6F73));
                                    mHolder.tv_price.setTextColor(ContextCompat.getColor(context, R.color.red_FA7E7F));
                                } else {
                                    convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.black_3));
                                    mHolder.tv_date.setTextColor(ContextCompat.getColor(context, R.color.gray_C0));
                                    mHolder.tv_price.setTextColor(ContextCompat.getColor(context, R.color.gray_C0));
                                }
                            } else {
                                convertView.setBackgroundResource(R.drawable.circle2_fa7e7f);
                                mHolder.tv_date.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                                mHolder.tv_price.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                                // 只有在多选的情况才显示“起”、“止”
                                if (isStartCheck && isCheckBox)
                                    mHolder.tv_start_lable.setVisibility(View.VISIBLE);
                                if (isEndCheck && isCheckBox)
                                    mHolder.tv_end_lable.setVisibility(View.VISIBLE);
                            }
                            break;
                        } else {
                            if (OSUtil.isDayTheme())
                                mHolder.tv_date.setTextColor(ContextCompat.getColor(context, R.color.black_6C6F73));
                            else
                                mHolder.tv_date.setTextColor(ContextCompat.getColor(context, R.color.gray_C0));
                        }
                    }
                } else {
                    mHolder.tv_date.setTextColor(ContextCompat.getColor(context, R.color.gray_BDC0C4));
                }
            }
            setPositions(positions);
        }
        TravelUtil.setLLParamsWidthPart(mHolder.fl_date, 7, 70, 1, 1);
        return convertView;
    }

    class DateHolder {
        TextView tv_date, tv_price, tv_start_lable, tv_end_lable;
        FrameLayout fl_date;
    }

    /**
     * 得到某年的某月的天数且这个月的第一天是星期几
     *
     * @param year
     * @param month
     */

    private void getCalendar(int year, int month) {
        isLeapyear = sc.isLeapYear(year); // 是否为闰年
        daysOfMonth = sc.getDaysOfMonth(isLeapyear, month); // 某月的总天数
        dayOfWeek = sc.getWeekdayOfMonth(year, month); // 某月第一天为星期几
        lastDaysOfMonth = sc.getDaysOfMonth(isLeapyear, month - 1); // 上一个月的总天数
        getweek(year, month);
    }

    /**
     * 将一个月中的每一天的值添加入数组dayNumber中
     *
     * @param year
     * @param month
     */
    private void getweek(int year, int month) {
        int j = 1;
        // 得到当前月的所有日期
        for (int i = 0; i < dayNumber.length; i++) {
            if (i < dayOfWeek) { // 前一个月
                int temp = lastDaysOfMonth - dayOfWeek + 1;
                //                dayNumber[i] = temp + i;
            } else if (i >= dayOfWeek && i < daysOfMonth + dayOfWeek) { // 本月
                dayNumber[i] = i - dayOfWeek + 1;
                setShowYear(String.valueOf(year));
                setShowMonth(String.valueOf(month).length() == 1 ? "0" + month : String.valueOf(month));
            } else { // 下一个月
                //                dayNumber[i] = j;
                j++;
            }
            String[] prices = new String[5];
            pricesList.add(prices);
        }

    }

    /**
     * 点击每一个item时返回item中的日期
     *
     * @param position
     * @return
     */
    public int getDateByClickItem(int position) {
        return dayNumber[position];
    }

    public String getShowYear() {
        return showYear;
    }

    public void setShowYear(String showYear) {
        this.showYear = showYear;
    }

    public String getShowMonth() {
        return showMonth;
    }

    public void setShowMonth(String showMonth) {
        this.showMonth = showMonth;
    }

    public ArrayList<Integer> getPositions() {
        return positions;
    }

    public void setPositions(ArrayList<Integer> positions) {
        this.positions = positions;
    }

    /**
     * 点击每一个item时返回item中的信息
     * 0:成人价 1:儿童价 2:单房差 3:日历Id 4:记录日期列表的游标
     *
     * @param position
     * @return
     */
    public String[] getCalendarInfo(int position) {
        return pricesList.get(position);
    }

}