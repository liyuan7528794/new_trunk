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
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.usercenter.MoreFragmentActivity;
import com.travel.usercenter.PersonalDataActivity;
import com.travel.utils.HLLXLoginHelper;
import com.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegistActivity extends TitleBarBaseActivity{
	private static final String TAG = "RegistActivity";
	
	
	private static final int GET_CODE_ERROR=1;//获取验证码失败
	private static final int GET_CODE_ALREADY=2;//已被注册
	private static final int REGIST_CODE_ERROR=6;//手机号验证码不匹配
	private static final int REGIST_ERROR=3;//注册失败
	private static final int CHECK_CODE_SUCCESS=4;//验证成功
	private static final int REGIST_SUCCESS=5;//验证成功
	
	private EditText telephoneEdit;
	private EditText checkCodeEdit;
	private EditText pwdEdit;
	private Button getCodeButton;
	private Button registButton;
//	private CheckBox agreeCheckbox;
	private TextView textView_agree;
	
	private static String telephoneNum;
	private static String passWord;

	private HLLXLoginHelper mLoginHelper;
	
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_CODE_ERROR:
				
				break;
			case GET_CODE_ALREADY:
				String obj = (String) msg.obj;
				if("1".equals(obj))
					Toast.makeText(RegistActivity.this, "该账号已被注册！", Toast.LENGTH_SHORT).show();
				stopTimeCount();
				break;
			case CHECK_CODE_SUCCESS:
				Toast.makeText(RegistActivity.this, "验证成功！", Toast.LENGTH_SHORT).show();
				userRegist();
				break;
			case REGIST_CODE_ERROR:
				Toast.makeText(RegistActivity.this, "验证码错误！", Toast.LENGTH_SHORT).show();
				break;
			case REGIST_ERROR:
				if(msg.obj==null || "".equals(msg.obj)){
					break;
				}
				JSONObject json = (JSONObject) msg.obj;
				if(json==null || "".equals(json)){
					break;
				}
				try {
					Toast.makeText(RegistActivity.this, json.getString("msg"), Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				stopTimeCount();
				hideProgressDialog();
				break;
			case REGIST_SUCCESS:
				Intent intent = new Intent(RegistActivity.this,PersonalDataActivity.class);
				startActivity(intent);
				hideProgressDialog();
				LoginActivity.finishActivity();
				finish();
				break;
			default:
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regist);
		mLoginHelper = new HLLXLoginHelper(this);
		initView();
		initViewListener();
	}
	
	private void initView() {
		titleText.setText("注册");
		findViewById(R.id.parentLayout).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				OSUtil.hideKeyboard(RegistActivity.this);
			}
		});
		telephoneEdit = (EditText) findViewById(R.id.telephone_edit);
		checkCodeEdit = (EditText) findViewById(R.id.check_code_edit);
		pwdEdit = (EditText) findViewById(R.id.et_password);
		registButton = (Button) findViewById(R.id.regist_button);
		getCodeButton = (Button) findViewById(R.id.get_code_button);
//		agreeCheckbox = (CheckBox) findViewById(R.id.checkBox1);
		textView_agree = (TextView) findViewById(R.id.textView_agree);
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
		textView_agree.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 发送请求查询注册协议，添加到TextView中
				startActivity(new Intent(RegistActivity.this,MoreFragmentActivity.class).putExtra("type", "regist"));
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
			Toast.makeText(RegistActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(TextUtils.isEmpty(checkCodeEdit.getText())){
			Toast.makeText(RegistActivity.this, "请输入短信验证码", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(TextUtils.isEmpty(pwdEdit.getText())){
			Toast.makeText(RegistActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
			return;
		}
		passWord = pwdEdit.getText().toString();
		
		String code = checkCodeEdit.getText().toString();
		telephoneNum = telephoneEdit.getText().toString();
		String pwd = pwdEdit.getText().toString();
		if(!telephoneNum.matches("1[3|4|5|7|8|][0-9]{9}")){
			Toast.makeText(RegistActivity.this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
			return;
		}
		String url = Constants.Root_Url+"/checkTelephone.do?telephoneCode="+telephoneNum+"&telephoneCheckCode="+code;
		NetWorkUtil.get(url, new MResponseListener() {
			@Override
			public void onResponse(JSONObject response) {
				super.onResponse(response);
				if (response.optInt("error") == 0) {
					handler.sendEmptyMessage(CHECK_CODE_SUCCESS);
				}
			}
			@Override
			protected void onMsgWrong(String msgStatu) {
				if(msgStatu.equals("1")){
					//手机号不存在
				}
				if(msgStatu.equals("2")){
					//密码错误
				}
				if(msgStatu.equals("3")){
					//账号被禁用
				}
				handler.sendEmptyMessage(REGIST_CODE_ERROR);
			}
			@Override
			protected void onNetComplete() {
				hideProgressDialog();
			}
		});
		showProgressDialog(null, "验证中 ...");
	}
	
	private void userRegist() {
		String url = Constants.Root_Url+"/regist.do";
		
		Map<String, Object> paraMap = new HashMap<String, Object>();
		paraMap.put("telephoneCode", telephoneNum);
		paraMap.put("password", passWord);
		paraMap.put("telephoneCheckCode", checkCodeEdit.getText().toString());
		paraMap.put("phoneModel", OSUtil.getPhoneModel());
		paraMap.put("osVersion", OSUtil.getOSVersion());
		NetWorkUtil.postForm(this, url, new MResponseListener() {
			@Override
			protected void onDataFine(JSONObject data) {
				mLoginHelper.userLoginSuccess(data);
//				startService(new Intent(RegistActivity.this, MessageService.class));
				handler.sendEmptyMessage(REGIST_SUCCESS);
			}
			
			@Override
			protected void onNetComplete() {
				hideProgressDialog();
			}

			@Override
			protected void onMsgWrong(String msg) {
				handler.sendEmptyMessage(REGIST_ERROR);
			}
		}, paraMap);
		showProgressDialog(null, "正在注册 ...");
	}
	
	
	/**
	 * 获取手机验证码
	 */
	private void getCode(){
		if(telephoneEdit.getText()==null||"".equals(telephoneEdit.getText().toString())){
			Toast.makeText(RegistActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
			return;
		}
		final String mobile = telephoneEdit.getText().toString();
		if(!mobile.matches("1[3|4|5|7|8|][0-9]{9}")){
			Toast.makeText(RegistActivity.this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
			return;
		}
		startTimeCount();
		String url = Constants.Root_Url+"/registCheckTelephone.do?telephoneCode="+mobile;
		NetWorkUtil.postJson(url, null, new MResponseListener("getCode") {

			@Override
			protected void onMsgWrong(String msg) {
				Message message = handler.obtainMessage();
				message.what = GET_CODE_ALREADY;
				message.obj = msg;
				handler.sendMessage(message);
			}
			
			protected void onNetComplete() {
				hideProgressDialog();
			}
			
			@Override
			public void onErrorResponse(VolleyError error) {
				super.onErrorResponse(error);
				hideProgressDialog();
			}
		});		
		showProgressDialog(null, "获取验证码...");
	}
}
