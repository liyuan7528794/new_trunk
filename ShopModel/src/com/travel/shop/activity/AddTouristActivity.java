package com.travel.shop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.travel.ShopConstant;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;
import com.travel.shop.bean.TouristInfo;
import com.travel.shop.tools.ShopTool;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 添加出行人
 * Created by Administrator on 2017/11/1.
 */

public class AddTouristActivity extends TitleBarBaseActivity {
    private RadioGroup radioGroup;
    private RadioButton rb_man, rb_woman;
    private TextView tv_save;
    private EditText et_name, et_idcard, et_telephone;
    private TouristInfo info;
    private boolean isUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toutist_add);
        setTitle("填写信息");
        if (getIntent().hasExtra("modify")) {
            isUpdate = true;
            if (getIntent().hasExtra("info")) {
                info = (TouristInfo) getIntent().getSerializableExtra("info");
            }
        }

        initview();

        if (info != null) {
            et_name.setText(info.getName());
            et_telephone.setText(info.getTelephone());
            et_idcard.setText(info.getIDCard());
            if (TextUtils.equals(info.getSex(), "女")) {
                radioGroup.check(rb_woman.getId());
            } else {
                radioGroup.check(rb_man.getId());
            }
        } else {
            info = new TouristInfo();
        }
        /*radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == rb_woman.getId()){
                    info.setSex("女");
                }else {
                    info.setSex("男");
                }
            }
        });*/
        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSave();
            }
        });

    }

    private void clickSave() {
        String name = et_name.getText().toString();
        String phone = et_telephone.getText().toString();
        String idcard = et_idcard.getText().toString();
        if (TextUtils.isEmpty(name)) {
            showToast("请输入姓名");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            showToast("请输入手机号码");
            return;
        }
        if (TextUtils.isEmpty(idcard)) {
            showToast("请输入身份证号");
            return;
        }
        if (!phone.matches("1[3|4|5|7|8|][0-9]{9}")) {
            showToast("您输入的手机号有误");
            return;
        }
        if (!ShopTool.IDCardValidate(idcard)) {
            showToast("您输入的身份证号有误");
            return;
        }
        String sex = radioGroup.getCheckedRadioButtonId() == rb_woman.getId() ? "女" : "男";

        // 保存到网络，成功后返回到前一页
        if (isUpdate) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", info.getId());
            if (!TextUtils.equals(name, info.getName())) {
                map.put("name", name);
                info.setName(name);
            }
            if (!TextUtils.equals(sex, info.getSex())) {
                map.put("sex", TextUtils.equals(sex, "男") ? 1 : 0);
                info.setSex(sex);
            }
            if (!TextUtils.equals(phone, info.getTelephone())) {
                map.put("phone", phone);
                info.setTelephone(phone);
            }
            if (!TextUtils.equals(idcard, info.getIDCard())) {
                map.put("idNumber", idcard);
                info.setIDCard(idcard);
            }
            updateTourist(map);
        } else {
            info.setSex(sex);
            info.setTelephone(phone);
            info.setIDCard(idcard);
            info.setName(name);

            Map<String, Object> map = new HashMap<>();
            map.put("userId", UserSharedPreference.getUserId());
            map.put("name", name);
            map.put("sex", TextUtils.equals(sex, "男") ? 1 : 0);
            map.put("phone", phone);
            map.put("idNumber", idcard);
            //            String url = ShopConstant.ADD_TOURIST + "?userId=" + UserSharedPreference.getUserId() + "&name=" + name + "&sex=" + sex + "&phone=" + phone + "&idNumber" + idcard;
            addTourist(map);
        }
    }

    private void addTourist(Map<String, Object> map) {

        String url = ShopConstant.ADD_TOURIST;
        // 获取网络数据
        NetWorkUtil.postForm(this, url, new MResponseListener(this) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    Intent intent = new Intent(AddTouristActivity.this, TouristsActivity.class);
                    intent.putExtra("info_result", info);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }, map);
    }

    private void updateTourist(Map<String, Object> map) {

        String url = ShopConstant.UPDATE_TOURIST;
        // 获取网络数据
        NetWorkUtil.postForm(this, url, new MResponseListener(this) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    Intent intent = new Intent(AddTouristActivity.this, TouristsActivity.class);
                    intent.putExtra("info_result", info);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }, map);
    }

    private void initview() {
        tv_save = (TextView) findViewById(R.id.tv_save);
        et_name = (EditText) findViewById(R.id.et_name);
        et_telephone = (EditText) findViewById(R.id.et_phone);
        et_idcard = (EditText) findViewById(R.id.et_idcard);
        radioGroup = (RadioGroup) findViewById(R.id.rg_sex);
        rb_man = (RadioButton) findViewById(R.id.rb_man);
        rb_woman = (RadioButton) findViewById(R.id.rb_woman);
        rb_man.setCompoundDrawablesWithIntrinsicBounds(
                ImageDisplayTools.createDrawableSelector(
                        this, R.drawable.icon_use_yes, R.drawable.icon_use_no),
                null, null, null);
        rb_woman.setCompoundDrawablesWithIntrinsicBounds(
                ImageDisplayTools.createDrawableSelector(
                        this, R.drawable.icon_use_yes, R.drawable.icon_use_no),
                null, null, null);
    }
}
