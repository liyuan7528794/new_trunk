package com.travel.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.travel.bean.ReasonBean;
import com.travel.lib.R;
import java.util.ArrayList;
import java.util.List;

/**
 * 取消订单原因的选取弹框
 * Created by Administrator on 2017/11/1.
 */
public class CancelOrderReasonPopWindow extends BaseBellowPopupWindow {
    private View rootView;
    private Context context;
    private ListView listView;
    private MyAdapter adapter;
    private List<ReasonBean> list;
    private TextView tv_notify;
    private ImageView iv_close;

    private CancelOrderPopWindowListenre listener;
    public interface CancelOrderPopWindowListenre{
        void onReason(ReasonBean reason, int position);
    }

    public CancelOrderReasonPopWindow(Context context, List<ReasonBean> list, CancelOrderPopWindowListenre listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.list = list;

        this.list = new ArrayList<>();
        this.list = list;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.cancel_order_reason_window, null);
        initView();
        SetContentView(rootView);
        initData();

        show();
    }

    private void initView() {
        listView = (ListView) rootView.findViewById(R.id.cancleListView);
        iv_close = (ImageView) rootView.findViewById(R.id.iv_close);
        tv_notify = (TextView) rootView.findViewById(R.id.tv_notify);
        adapter = new MyAdapter();
        listView.setAdapter(adapter);
    }

    private void initData() {
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onReason(list.get(position), position);
                dismiss();
            }
        });

        if(list.size() > 0) {
            tv_notify.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }else{
            tv_notify.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
    }

    class MyAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        public MyAdapter (){
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.adapter_cancel_order_reason, null);
                holder = new ViewHolder();
                holder.nickname = (TextView) convertView.findViewById(R.id.tv_map);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.nickname.setText(list.get(position).getReason());

            return convertView;
        }
    }

    private class ViewHolder {
        public TextView nickname;
    }

}