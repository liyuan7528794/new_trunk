package com.travel.usercenter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("CutPasteId")
public class ModifyPassword extends TitleBarBaseActivity{
	private EditText newEdit,oldEdit,reNewEdit;
	private Button pwdSubmit;
	@SuppressLint("CutPasteId")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_password);

		newEdit = (EditText)findViewById(R.id.newEdit);
		oldEdit = (EditText)findViewById(R.id.oldEdit);
		reNewEdit = (EditText)findViewById(R.id.reEdit);
		pwdSubmit = (Button) findViewById(R.id.submit_pwd);
		titleLine.setVisibility(View.GONE);

		newEdit.setHint("请输入新密码");
		oldEdit.setHint("请输入旧密码");
		reNewEdit.setHint("请确认新密码");

		pwdSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				modifyPwdNet();
			}
		});
	}
	private void modifyPwdNet() {
		//判断是否为空
		if(oldEdit.getText()==null || newEdit.getText().toString()==null || reNewEdit.getText().toString()==null ){
			Toast.makeText(getApplication(), "密码不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		
		//判断密码是否一样
		if(!newEdit.getText().toString().equals(reNewEdit.getText().toString())){
			Toast.makeText(getApplication(), "两次输入的密码不一样", Toast.LENGTH_SHORT).show();
			return;
		}
		
		//提交数据
		
		Map<String,Object> paramap = new HashMap<String,Object>();
		paramap.put("id", UserSharedPreference.getUserId());
		paramap.put("oldPassword", oldEdit.getText().toString());
		paramap.put("newPassword", reNewEdit.getText().toString());
//		paramap.put("msg", "");
		paramap.put("isFind", "1");
		
		showProgressDialog(null, "正在修改密码...");

		//网络访问
		NetWorkUtil.postForm(getApplication(), com.travel.Constants.Root_Url + "/user/updatePassword.do" , new MResponseListener() {
			
			@Override
			public void onResponse(JSONObject response) {
				super.onResponse(response);
				try {
					if(response.getInt("error") == 0 && "OK".equals(response.getString("msg"))){
						UserSharedPreference.savePassword(reNewEdit.getText().toString());
						finish();
					}else if(response.getInt("error") == 1 && "password error".equals(response.getString("msg"))){
						Toast.makeText(getApplication(), "旧密码错误，请重新输入", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				hideProgressDialog();
				
			}
			@Override
			public void onErrorResponse(VolleyError error) {
				hideProgressDialog();
				super.onErrorResponse(error);
			}
			
		}, paramap);
		
	}
	
}
