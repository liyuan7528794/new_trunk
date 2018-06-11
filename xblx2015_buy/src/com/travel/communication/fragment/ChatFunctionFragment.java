package com.travel.communication.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.TravelUtil;

/**
 * 聊天盘上的更多页面
 * 功能页
 *
 * @author ldkxingzhe
 */
public class ChatFunctionFragment extends Fragment {
    @SuppressWarnings("unused")
    private static final String TAG = "ChatFunctionFragment";

    public static final int INDEX_VIDEO = 0;
    public static final int INDEX_PICTURE = 1;
    public static final int INDEX_TAKE_PHOTO = 2;
    public static final int INDEX_LIVE = 3;

    private ViewGroup rootView;

    // 功能页面的所有接口回调
    public interface ChatFunctionListener {
        /**
         * 某个功能按钮被单击
         *
         * @param functionId 功能按钮的Id
         */
        void onFunctionClick(int functionId);
    }

    private ChatFunctionListener mListener;

    public void setListener(ChatFunctionListener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_chat_function, container, false);
        dealWithSingleChatAndGroupChat(rootView);
        for (int i = 0; i < rootView.getChildCount(); i++) {
            View child = rootView.getChildAt(i);
            final int finalI = i;
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        if (finalI == 1) {
                            mListener.onFunctionClick(finalI);
                        } else {
                            String[] permissions = new String[]{"", ""};
                            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                                    != PackageManager.PERMISSION_GRANTED) {
                                permissions[0] = Manifest.permission.CAMERA;
                            }
                            if (finalI == 2) {
                                ChatFunctionFragment.this.requestPermissions(permissions, 1);
                            } else {
                                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    permissions[1] = Manifest.permission.RECORD_AUDIO;
                                }
                                if (finalI == 0)
                                    ChatFunctionFragment.this.requestPermissions(permissions, 0);
                                else
                                    ChatFunctionFragment.this.requestPermissions(permissions, 3);
                            }
                        }
                    }
                }
            });
        }

        return rootView;
    }

    private void dealWithSingleChatAndGroupChat(View rootView) {
        View liveView = rootView.findViewById(R.id.ll_live);
        liveView.setVisibility(ChatFragment.mIsGroupChat ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED || TextUtils.isEmpty(permissions[0])) {
                    mListener.onFunctionClick(2);
                } else {
                    TravelUtil.showToast("您已禁止拍照权限");
                }
                break;
            case 0:
            case 3:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED
                            && TextUtils.equals(permissions[0], Manifest.permission.CAMERA) && !TextUtils.isEmpty(permissions[0])) {
                        TravelUtil.showToast("您已禁止拍照权限");
                        break;
                    }
                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED
                            && TextUtils.equals(permissions[1], Manifest.permission.RECORD_AUDIO) || TextUtils.isEmpty(permissions[1])) {
                        mListener.onFunctionClick(requestCode);
                    } else {
                        TravelUtil.showToast("您已禁止录音权限");
                    }
                    break;

                }
            default:
        }
    }
}
