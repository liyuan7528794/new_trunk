package com.travel.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.tencent.TIMValueCallBack;
import com.travel.activity.ForgetPasswordActivity;
import com.travel.activity.HomeActivity;
import com.travel.activity.LoginActivity;
import com.travel.lib.ui.LoadingDialog;
import com.travel.lib.utils.OSUtil;
import com.travel.utils.HLLXLoginHelper;

/**
 * Created by Administrator on 2017/5/3.
 */

public class LoginFragment extends Fragment{
    private View rootView;

    private Button goLogin;
    private EditText userNameEditText;
    private EditText passwordEditText;
    private TextView forgetPwd;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_login, container, false);
        initView();
        setListener();
        return rootView;
    }

    private void initView() {
        forgetPwd = (TextView) rootView.findViewById(R.id.forgetPwdLayout);
        goLogin = (Button) rootView.findViewById(R.id.login);
        userNameEditText = (EditText) rootView.findViewById(R.id.userName);
        passwordEditText = (EditText) rootView.findViewById(R.id.password);
    }

    private void setListener() {
        forgetPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ForgetPasswordActivity.class);
                startActivity(intent);
            }
        });

        goLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // check userName
                final String userName = userNameEditText.getText().toString()
                        .trim();
                if (TextUtils.isEmpty(userName)) {
                    Toast.makeText(getContext(), "用户名不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check password
                final String password = passwordEditText.getText().toString()
                        .trim();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getContext(), "密码不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }

                LoadingDialog.getInstance(getContext()).showProcessDialog();
                userLogin(userName, password);
            }
        });
    }

    private void userLogin(final String telephoneCode, final String password) {
        HLLXLoginHelper loginHelper = new HLLXLoginHelper(getContext());
        loginHelper.login(telephoneCode, password, new TIMValueCallBack<String>() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                LoadingDialog.getInstance(getContext()).hideProcessDialog(1);
            }

            @Override
            public void onSuccess(String s) {
                Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                LoadingDialog.getInstance(getContext()).hideProcessDialog(0);
                ((LoginActivity)getActivity()).onBackPressed();
            }
        });
    }

}
