package com.travel.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SettingPasswordActivity extends TitleBarBaseActivity{
	private final static int MODIFY_PWD_SUCCESS = 0;//修改成功
	private final static int MODIFY_PWD_FAIL = 1;//修改失败
	
	private EditText passwordEdit,rePasswordEdit;
	private Button pwdSubmit;
	private String userId,pwdCode,telephoneCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		userId = getIntent().getStringExtra("user_id");
		pwdCode = getIntent().getStringExtra("password_code");
		telephoneCode = getIntent().getStringExtra("telephone_code");
		setContentView(R.layout.activity_setting_password);
		initView();
	}
	private void initView() {
		passwordEdit = (EditText) findViewById(R.id.pwdEdit);
		rePasswordEdit = (EditText) findViewById(R.id.rePwdEdit);
		pwdSubmit = (Button) findViewById(R.id.modifySubmit);

		titleLine.setVisibility(View.GONE);

		pwdSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//确认注册
				settingPwd();
			}
		});
	}
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MODIFY_PWD_SUCCESS:
				Toast.makeText(SettingPasswordActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
				hideProgressDialog();
				finish();
				break;
			case MODIFY_PWD_FAIL:
				Toast.makeText(SettingPasswordActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};
	
	/**
	 * 注册请求
	 */
	private void settingPwd() {
		if(passwordEdit.getText() == null || rePasswordEdit.getText() == null){
			Toast.makeText(SettingPasswordActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		String pwd = passwordEdit.getText().toString();
		String rePwd = rePasswordEdit.getText().toString();
		if(!pwd.equals(rePwd)){
			Toast.makeText(SettingPasswordActivity.this, "密码不一致！", Toast.LENGTH_SHORT).show();
			return;
		}
		String url = Constants.Root_Url+"/user/updatePassword.do";
		Map paraMap = new HashMap();
		paraMap.put("id", userId);
		paraMap.put("oldPassword", pwdCode);
		paraMap.put("newPassword", pwd);
		paraMap.put("telephoneCheckCode", telephoneCode);
		paraMap.put("isFind", "0");
		showProgressDialog(null, "正在设置密码...");
		NetWorkUtil.postForm(getApplication(), url, new MResponseListener() {
			
			@Override
			public void onResponse(JSONObject response) {
				super.onResponse(response);
				System.out.println("response---->"+response);
				try {
					if("0".equals(response.get("error")+"") && "OK".equals(response.get("msg")))
						handler.sendEmptyMessage(MODIFY_PWD_SUCCESS);
				} catch (JSONException e) {
					e.printStackTrace();
					hideProgressDialog();
				}
			}
			@Override
			protected void onMsgWrong(String msg) {
				super.onMsgWrong(msg);
				handler.sendEmptyMessage(MODIFY_PWD_FAIL);
				hideProgressDialog();
			}
			@Override
			public void onErrorResponse(VolleyError error) {
				super.onErrorResponse(error);
				hideProgressDialog();
			}
		}, paraMap);
		
	}
}
