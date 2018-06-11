package com.travel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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
import com.travel.lib.utils.OSUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgetPasswordActivity extends TitleBarBaseActivity{
private static final String TAG = "ForgetPasswordActivity";
	
	
	private static final int GET_CODE_ERROR=1;//获取验证码失败
	private static final int REGIST_CODE_ERROR=6;//手机号验证码不匹配
	private static final int CHECK_CODE_SUCCESS=4;//验证成功
	
	private EditText telephoneEdit;
	private EditText checkCodeEdit;
	private Button getCodeButton;
	private Button registButton;
	
	
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_CODE_ERROR:
				
				break;
			case CHECK_CODE_SUCCESS:
				Map<String,String> map = (Map<String, String>) msg.obj;
				if(map!=null){
					String pwdCode = map.get("pwd_code");
					String userId = map.get("user_id");
					String telephoneCode = map.get("telephone_code");
					Toast.makeText(ForgetPasswordActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(ForgetPasswordActivity.this,SettingPasswordActivity.class);
					intent.putExtra("password_code", pwdCode);
					intent.putExtra("user_id", userId);
					intent.putExtra("telephone_code", telephoneCode);
					startActivity(intent);
					finish();
				}else{
					Toast.makeText(ForgetPasswordActivity.this, "验证失败", Toast.LENGTH_SHORT).show();
					stopTimeCount();
				}
				break;
			case REGIST_CODE_ERROR:
				Toast.makeText(ForgetPasswordActivity.this, "验证码错误！", Toast.LENGTH_SHORT).show();
				stopTimeCount();
				break;
			default:
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forget_password);
		initView();
		initViewListener();
	}
	
	private void initView() {
		findViewById(R.id.parentLayout).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				OSUtil.hideKeyboard(ForgetPasswordActivity.this);
			}
		});
		telephoneEdit = (EditText) findViewById(R.id.telephone_edit);
		checkCodeEdit = (EditText) findViewById(R.id.check_code_edit);
		registButton = (Button) findViewById(R.id.regist_button);
		getCodeButton = (Button) findViewById(R.id.get_code_button);
		titleLine.setVisibility(View.GONE);
	}
	
	private void initViewListener() {
		
		getCodeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//获取验证码功能
				getCode();
			}
		});
		registButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//注册
				registCode();
			}
		});
	}

	private CountDownTimer timer = new CountDownTimer(60000, 1000) {

		@Override
		public void onTick(long millisUntilFinished) {
			getCodeButton.setText(millisUntilFinished/1000 + "秒");
		}

		@Override
		public void onFinish() {
			getCodeButton.setEnabled(true);
			getCodeButton.setText("获取验证码");
		}
	};

	protected void startTimeCount() {
		getCodeButton.setEnabled(false);
		timer.start();
	}

	private void stopTimeCount(){
		timer.cancel();
		getCodeButton.setEnabled(true);
		getCodeButton.setText("获取验证码");
	}

	/**
	 *验证验证码
	 */
	private void registCode(){
		if(TextUtils.isEmpty(telephoneEdit.getText())){
			Toast.makeText(ForgetPasswordActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(TextUtils.isEmpty(checkCodeEdit.getText())){
			Toast.makeText(ForgetPasswordActivity.this, "请输入短信验证码", Toast.LENGTH_SHORT).show();
			return;
		}
		
		final String code = checkCodeEdit.getText().toString();
		String telephoneNum = telephoneEdit.getText().toString();
		if(!telephoneNum.matches("1[3|4|5|7|8|][0-9]{9}")){
			Toast.makeText(ForgetPasswordActivity.this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
			return;
		}
		String url = Constants.Root_Url+"/findPassWordByTelephone.do";
		Map<String,Object> paramap = new HashMap<String,Object>();
		paramap.put("telephoneCode", telephoneNum);
		paramap.put("telephoneCheckCode", code);
		NetWorkUtil.postForm(getApplication(), url, new MResponseListener() {
			@Override
			protected void onDataFine(JSONObject data) {
				try {
					if(data!=null){
						String pwdCode = data.getString("password");
						String userId = data.getString("id");
						Map<String, String> map = new HashMap<String, String>();
						map.put("pwd_code", pwdCode);
						map.put("user_id", userId);
						map.put("telephone_code", code);
						Message msg = new Message();
						msg.what = CHECK_CODE_SUCCESS;
						msg.obj = map;
						handler.sendMessage(msg);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			@Override
			protected void onMsgWrong(String msgStatu) {
				if(msgStatu.equals("1")){
					//手机号不存在
					showToast("手机号不存在");
				}
				if(msgStatu.equals("2")){
					//密码错误
					showToast("请求失败");
				}
				if(msgStatu.equals("3")){
					//账号被禁用
					showToast("账号被禁用");
				}
				handler.sendEmptyMessage(REGIST_CODE_ERROR);
			}

			@Override
			protected void onNetComplete() {
				hideProgressDialog();
			}
			
		}, paramap);
		
		showProgressDialog(null, "验证中 ...");
	}
	
	
	/**
	 * 获取手机验证码
	 */
	private void getCode(){
		if(telephoneEdit.getText()==null||"".equals(telephoneEdit.getText().toString())){
			Toast.makeText(ForgetPasswordActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
			return;
		}
		final String mobile = telephoneEdit.getText().toString();
		if(!mobile.matches("1[3|4|5|7|8|][0-9]{9}")){
			Toast.makeText(ForgetPasswordActivity.this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
			return;
		}
		startTimeCount();
		String urls = Constants.Root_Url + "/send_Sms_check.do";
		Map<String, Object> map = new HashMap<>();
		map.put("telephoneCode", mobile);
		map.put("type", "update_pwd");
		NetWorkUtil.postForm(ForgetPasswordActivity.this, urls, new MResponseListener() {

			@Override
			protected void onErrorNotZero(int error, String msg) {
				switch (error){
					case 1002:
						Toast.makeText(ForgetPasswordActivity.this, "手机号码未注册", Toast.LENGTH_SHORT).show();
						break;
					default:
						Toast.makeText(ForgetPasswordActivity.this, "获取出错", Toast.LENGTH_SHORT).show();
						break;
				}
				stopTimeCount();
			}
			protected void onNetComplete() {
				hideProgressDialog();
			}

		}, map);
	}
}