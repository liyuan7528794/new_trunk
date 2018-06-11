package com.travel.shop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.travel.ShopConstant;
import com.travel.layout.DialogTemplet;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;
import com.travel.shop.bean.TouristInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 出行人列表
 * Created by Administrator on 2017/11/1.
 */

public class TouristsActivity extends TitleBarBaseActivity {

    private ListView listView;
    private TextView tv_add;
    private List<TouristInfo> list;
    private MyAdapter adapter;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourists);
        setTitle("选择出行人");
        rightButton.setVisibility(View.VISIBLE);
        rightButton.setText("完成");
        listView = (ListView) findViewById(R.id.myListView);
        tv_add = (TextView) findViewById(R.id.tv_add);

        list = new ArrayList<>();
        adapter = new MyAdapter();
        listView.setAdapter(adapter);
        count = getIntent().getIntExtra("count", 0);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getCheckedMap().size() != count) {
                    showToast("请选择" + count + "人");
                    return;
                }
                Iterator iter = adapter.getCheckedMap().entrySet().iterator();
                ArrayList<TouristInfo> touristChoosed = new ArrayList<TouristInfo>();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    TouristInfo value = (TouristInfo) entry.getValue();
                    touristChoosed.add(value);
                }

                Intent intent = new Intent(TouristsActivity.this, AddTouristActivity.class);
                intent.putExtra("touristChoosed", touristChoosed);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        tv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TouristsActivity.this, AddTouristActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        adapter.setOnLongListener(new ClickListener() {
            @Override
            public void onLongClick(final int position) {
                DialogTemplet dialogTemplet = new DialogTemplet(TouristsActivity.this, false, "是否删除", "", "取消", "删除");
                dialogTemplet.setRightClick(new DialogTemplet.DialogRightButtonListener() {
                    @Override
                    public void rightClick(View view) {
                        delete(position);
                    }
                });
                dialogTemplet.show();
            }
        });
        getData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            TouristInfo touristInfo = (TouristInfo) data.getSerializableExtra("info_result");
            //刷新数据
            list.clear();
            getData();
        }
    }

    private void getData() {
        String url = ShopConstant.TOURISTS;
        Map<String, Object> map = new HashMap<>();
        map.put("userId", UserSharedPreference.getUserId());
        // 获取网络数据
        NetWorkUtil.postForm(this, url, new MResponseListener(this) {

            @Override
            protected void onDataFine(JSONArray data) {
                try {
                    if (data != null && data.length() > 0) {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonObject = data.getJSONObject(i);
                            TouristInfo info = new TouristInfo();
                            info.setName(JsonUtil.getJson(jsonObject, "name"));
                            info.setIDCard(JsonUtil.getJson(jsonObject, "idNumber"));
                            info.setSex(TextUtils.equals(JsonUtil.getJson(jsonObject, "sex"), "1") ? "男" : "女");
                            info.setTelephone(JsonUtil.getJson(jsonObject, "phone"));
                            info.setId(JsonUtil.getJson(jsonObject, "id"));
                            list.add(info);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    adapter.notifyDataSetChanged();
                }
            }
        }, map);
    }

    private void delete(final int position) {
        String url = ShopConstant.DELETE_TOURIST;
        Map<String, Object> map = new HashMap<>();
        map.put("id", list.get(position).getId());
        // 获取网络数据
        NetWorkUtil.postForm(this, url, new MResponseListener(this) {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0){
                    if (position < list.size())
                        list.remove(position);
                    adapter.notifyDataSetChanged();
                }
            }
        }, map);
    }

    private class MyAdapter extends BaseAdapter {
        private HashMap<String, TouristInfo> checkedMap = new HashMap<>();
        private ClickListener listener;

        public void setOnLongListener(ClickListener listener) {
            this.listener = listener;
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

        public HashMap<String, TouristInfo> getCheckedMap() {
            return checkedMap;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Holder mHolder;
            if (convertView == null) {
                mHolder = new Holder();
                convertView = View.inflate(TouristsActivity.this, R.layout.adapter_tourists, null);
                mHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                mHolder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.relativeLayout);
                mHolder.tv_sex = (TextView) convertView.findViewById(R.id.tv_sex);
                mHolder.tv_idcard = (TextView) convertView.findViewById(R.id.tv_idcard);
                mHolder.iv_edit = (ImageView) convertView.findViewById(R.id.iv_edit);
                mHolder.v_line = convertView.findViewById(R.id.v_line);
                convertView.setTag(mHolder);
            } else {
                mHolder = (Holder) convertView.getTag();
            }
            final CheckBox radioButton = (CheckBox) convertView.findViewById(R.id.radioButton);
            radioButton.setTag(list.get(position));
            radioButton.setCompoundDrawablesWithIntrinsicBounds(
                    ImageDisplayTools.createDrawableSelector(
                            TouristsActivity.this, R.drawable.icon_use_yes, R.drawable.icon_use_no),
                    null, null, null);
            mHolder.radioButton = radioButton;
            mHolder.v_line.setVisibility(list.size() == position + 1 ? View.GONE : View.VISIBLE);

            mHolder.tv_name.setText(list.get(position).getName());
            mHolder.tv_sex.setText(list.get(position).getSex());
            mHolder.tv_idcard.setText(list.get(position).getIDCard());
            mHolder.iv_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TouristsActivity.this, AddTouristActivity.class);
                    intent.putExtra("info", list.get(position));
                    intent.putExtra("modify", true);
                    startActivityForResult(intent, 1);
                }
            });

            mHolder.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    TouristInfo info = list.get(position);
                    if (isChecked) {
                        checkedMap.put(info.getId(), info);
                        notifyDataSetChanged();
                    } else {
                        if (checkedMap.containsKey(info.getId()))
                            checkedMap.remove(info.getId());
                    }
                }
            });

            if (checkedMap.containsKey(list.get(position).getId())) {// 选中的条目和当前的条目是否相等
                mHolder.radioButton.setChecked(true);
            } else {
                mHolder.radioButton.setChecked(false);
            }

            mHolder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onLongClick(position);
                    return false;
                }
            });

            return convertView;
        }

        class Holder {
            public RelativeLayout relativeLayout;
            public TextView tv_name, tv_sex, tv_idcard;
            public CheckBox radioButton;
            public ImageView iv_edit;
            public View v_line;
        }

    }

    public interface ClickListener {
        void onLongClick(int position);
    }

}
