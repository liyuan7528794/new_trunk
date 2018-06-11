package com.travel.shop.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.travel.layout.MyGridView;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.shop.R;
import com.travel.shop.adapter.CalendarAdapter;
import com.travel.shop.adapter.SelectStartPlaceAdapter;
import com.travel.shop.bean.CalendarBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * 日历
 */
public class NewCalendarFragment extends Fragment {

    private View view;
    // 月份选择
    private TextView tv_selected_month;
    private RecyclerView rv_month;
    // 月份数据
    private SelectStartPlaceAdapter monthAdapter;
    private ArrayList<HashMap<String, Object>> months;
    private ArrayList<CalendarBean> orderData;// 所有的数据
    private String defaultDate;

    // 日历信息相关
    private TextView tv_selected_date, tv_sum_day;
    private MyGridView gv_date;
    private CalendarAdapter mAdapter;
    // 可以出行的日期，选择的日期
    private ArrayList<CalendarBean> startDates, curData;
    private int selectedDays;// 选择的天数
    private ArrayList<Integer> flags;// 0:未选 1：已选
    private int startYear, endYear, startMonth, endMonth, monthCount;// 一共有几个月
    private boolean isCheckBox;// 判断是否能多选
    // 当前日期
    private String currentDate;
    private int current_year = 0;
    private int current_month = 0;
    private int current_day = 0;

    private ArrayList<CalendarBean> oldStartDates, oldOrderData, oldCurData;
    // 商品类型
    private int goodsType;

    private OnDateChangeListener mListener;

    public NewCalendarFragment() {
    }

    public void setList(ArrayList<CalendarBean> orderData, int goodsType) {
        this.goodsType = goodsType;
        this.orderData.clear();
        this.orderData.addAll(orderData);
        if (orderData.size() != 0)
            getMonthsData();
        else{
            this.months.clear();
            monthAdapter.notifyDataSetChanged();
            startDates.clear();
            mAdapter = new CalendarAdapter(getContext(), current_year, current_month, current_day,
                    orderData, startDates, flags);
            gv_date.setAdapter(mAdapter);
            tv_selected_month.setText("");
        }
    }

    /**
     * 获取可选的月份数据
     */
    private void getMonthsData() {
        this.months.clear();
        startYear = Integer.parseInt(orderData.get(0).getYear());
        startMonth = Integer.parseInt(orderData.get(0).getMonth());
        endYear = Integer.parseInt(orderData.get(orderData.size() - 1).getYear());
        endMonth = Integer.parseInt(orderData.get(orderData.size() - 1).getMonth());
        defaultDate = startYear + "-" + startMonth + "：";
        if (startYear == endYear) {
            monthCount = endMonth - startMonth + 1;
            for (int i = 0; i < monthCount; i++) {
                HashMap<String, Object> data = new HashMap<>();
                data.put("year", startYear);
                data.put("month", startMonth + i);
                data.put("isChecked", i == 0 ? true : false);
                this.months.add(data);
            }
        } else if (startYear < endYear) {
            monthCount = 12 - startMonth + 1 + 12 * (endYear - startYear - 1) + endMonth;
            for (int i = 0; i < monthCount - 1; i++) {
                HashMap<String, Object> data = new HashMap<>();
                // TODO 默认只会跨一年
                boolean isNext = startMonth + i > 12 ? true : false;
                data.put("year", startYear + (isNext ? 1 : 0));
                data.put("month", startMonth + i - (isNext ? 12 * ((startMonth + i) / 12) : 0));
                data.put("isChecked", i == 0 ? true : false);
                this.months.add(data);
            }
        }
        monthAdapter.notifyDataSetChanged();
        tv_selected_month.setText(defaultDate);
        // 默认获取第一个月的日历数据
        getOtherStartData(startYear, startMonth);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new_calendar, container, false);
        initView();
        initData();
        initListener();
        return view;
    }

    private void initView() {
        tv_selected_month = (TextView) view.findViewById(R.id.tv_selected_month);
        rv_month = (RecyclerView) view.findViewById(R.id.rv_month);

        tv_selected_date = (TextView) view.findViewById(R.id.tv_selected_date);
        tv_sum_day = (TextView) view.findViewById(R.id.tv_sum_day);
        gv_date = (MyGridView) view.findViewById(R.id.gv_date);
    }

    private void initData() {
        currentDate = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_DATE);
        current_year = Integer.parseInt(currentDate.split("-")[0]);
        current_month = Integer.parseInt(currentDate.split("-")[1]);
        current_day = Integer.parseInt(currentDate.split("-")[2]);

        months = new ArrayList<>();
        orderData = new ArrayList<>();
        startDates = new ArrayList<>();
        curData = new ArrayList<>();
        rv_month.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        monthAdapter = new SelectStartPlaceAdapter(months, getContext(), 2);
        rv_month.setAdapter(monthAdapter);

        flags = new ArrayList<>();
        for (int i = 0; i < 42; i++)
            flags.add(0);// 初始化标识位
        mAdapter = new CalendarAdapter(getContext(), current_year, current_month, current_day,
                orderData, startDates, flags);
        gv_date.setAdapter(mAdapter);
        gv_date.setFocusable(false);

        oldStartDates = new ArrayList<>();
        oldOrderData = new ArrayList<>();
        oldCurData = new ArrayList<>();
    }

    private void initListener() {
        monthAdapter.setmOnItemClickListener(new SelectStartPlaceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                for (int i = 0; i < months.size(); i++) {
                    if (i == position) {
                        int year = Integer.parseInt(months.get(i).get("year").toString());
                        int month = Integer.parseInt(months.get(i).get("month").toString());
                        months.get(i).put("isChecked", true);
                        defaultDate = year + "-" + month + "：";
                        tv_selected_month.setText(defaultDate);
                        getOtherStartData(year, month);
                    } else
                        months.get(i).put("isChecked", false);
                }
                monthAdapter.notifyDataSetChanged();
            }
        });

        gv_date.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 有旅游团那天可以点击
                if (mAdapter.getPositions().contains(position)) {
                    // 选的第二个日期不合法时
                    if (curData.size() == 2 && !curData.get(0).isStartCheck())
                        curData.remove(0);
                    // 判断是否能多选
                    isCheckBox = goodsType == 3 || goodsType == 4;
                    // 不可多选或者是可多选但是已经选择两个了
                    if ((curData.size() == 1 && goodsType != 3 && goodsType != 4) || curData.size() == 2) {
                        orderData.get(curData.get(0).getIndex()).setStartCheck(false);
                        if (curData.size() == 2) {
                            orderData.get(curData.get(1).getIndex()).setStartCheck(false);
                            orderData.get(curData.get(0).getIndex()).setEndCheck(false);
                            orderData.get(curData.get(1).getIndex()).setEndCheck(false);
                            orderData.get(curData.get(0).getIndex()).setCheckBox(false);
                            orderData.get(curData.get(1).getIndex()).setCheckBox(false);
                            tv_selected_date.setText("");
                            tv_sum_day.setText("");
                        }
                        curData.clear();
                    }
                    // 酒店不可选择同一天
                    if (!(curData.size() == 1 && curData.get(0).getIndex() == Integer.valueOf(mAdapter.getCalendarInfo(position)[4]) && goodsType == 3))
                        curData.add(orderData.get(Integer.valueOf(mAdapter.getCalendarInfo(position)[4])));
                    // 选择的日期
                    if (curData.size() == 1) {
                        orderData.get(curData.get(0).getIndex()).setCheckBox(true);
                        orderData.get(curData.get(0).getIndex()).setStartCheck(true);
                        if (!isCheckBox) {
                            orderData.get(curData.get(0).getIndex()).setCheckBox(false);
                            tv_selected_date.setText(tv_selected_month.getText().toString()
                                    + (mAdapter.getDateByClickItem(position) < 10 ? "0" : "")
                                    + mAdapter.getDateByClickItem(position) + "日");
                        } else {
                            tv_selected_date.setText("");
                            tv_sum_day.setText("");
                        }
                    } else if (curData.size() > 1) {
                        // ①日期是升序的 ②日期没有中断
                        if (judgeDate(curData.get(0).getDate(),
                                orderData.get(Integer.valueOf(mAdapter.getCalendarInfo(position)[4])).getDate(), position)
                                ) {
                            orderData.get(curData.get(1).getIndex()).setCheckBox(true);
                            orderData.get(curData.get(1).getIndex()).setEndCheck(true);
                            tv_selected_date.setText(curData.get(0).getDate() + " ~ " + curData.get(1).getDate());
                            tv_sum_day.setText("共" + (selectedDays + (goodsType == 3 ? 0 : 1)) + "天");
                        } else {
                            orderData.get(curData.get(0).getIndex()).setStartCheck(false);
                            orderData.get(curData.get(1).getIndex()).setStartCheck(true);
                            orderData.get(curData.get(1).getIndex()).setCheckBox(true);
                            tv_selected_date.setText("");
                            tv_sum_day.setText("");
                        }
                    }
                    saveOldData();
                    mAdapter.notifyDataSetChanged();
                    if (mListener != null)
                        mListener.onDateChange(mAdapter.getCalendarInfo(position), curData, selectedDays);
                }
            }
        });
    }


    /**
     * 获取其他月份的日历
     *
     * @param year
     * @param month
     */
    private void getOtherStartData(int year, int month) {
        startDates.clear();
        for (int i = 0; i < orderData.size(); i++)
            // 同年同月
            if (year == Integer.parseInt(orderData.get(i).getYear())
                    && month == Integer.parseInt(orderData.get(i).getMonth())) {
                orderData.get(i).setChildIndex(i);
                startDates.add(orderData.get(i));
            }
        mAdapter = new CalendarAdapter(getContext(), year, month, (year == current_year
                && month == current_month ? current_day : 0),
                orderData, startDates, flags);
        gv_date.setAdapter(mAdapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * 日期的判断
     *
     * @param startDate
     * @param endDate
     * @return
     */
    private boolean judgeDate(String startDate, String endDate, int position) {
        selectedDays = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dateStart = simpleDateFormat.parse(startDate);
            Date dateEnd = simpleDateFormat.parse(endDate);
            int dd = (int) ((dateEnd.getTime() - dateStart.getTime()) / (1000 * 3600 * 24));
            // 日期是升序的
            if (dd < 0)
                return false;
            selectedDays = Math.abs(dd);
            int positions = Math.abs(orderData.get(Integer.valueOf(mAdapter.getCalendarInfo(position)[4])).getIndex() -
                    curData.get(0).getIndex());
            if (selectedDays != positions)
                return false;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 存储当前选择的数据
     */
    private void saveOldData() {
        oldStartDates.clear();
        oldStartDates.addAll(startDates);
        oldCurData.clear();
        oldCurData.addAll(curData);
        oldOrderData.clear();
        oldOrderData.addAll(orderData);
    }


    public interface OnDateChangeListener {
        void onDateChange(String[] positionData, ArrayList<CalendarBean> curData, int selectedDays);
    }

    public void setListener(OnDateChangeListener mListener) {
        this.mListener = mListener;
    }
}
