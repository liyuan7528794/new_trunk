package com.travel.communication.helper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.communication.entity.MessageEntity;
import com.travel.communication.fragment.ChatFunctionFragment;
import com.travel.communication.utils.DirUtils;
import com.travel.communication.view.ChatKeyboard;
import com.travel.communication.view.RecorderVoiceLevelPopup;
import com.travel.communication.view.VideoInputDialog;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.localfile.module.VideoModule;
import com.travel.utils.PictureHelper;
import com.travel.video.live.HostWindowActivity;

import java.io.File;
import java.util.UUID;

/**
 * 默认的聊天输入的辅助类
 * Created by ldkxingzhe on 2017/1/10.
 */
public class DefaultChatInputHelper implements ChatKeyboard.ChatKeyboardListener {
    @SuppressWarnings("unused")
    private static final String TAG = "DefaultChatInputHelper";

    public interface AfterSendMessageListener {
        void onSendMessage(MessageEntity entity);
    }

    private final MessageSenderHelper mMsgSenderHelper;
    private RecorderVoiceLevelPopup mRecorderVoiceLevelPopup;
    private LocalBroadcastManager mBroadcastManager;
    private final ChatKeyboard mChatKeyboard;
    private final Context mContext;
    private final PictureHelper mPictureHelper;
    private final RecorderHelper mRecorderHelper;
    private static final int REQUEST_PICK_IMAGE = 0;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;
    private boolean mIsEnable = true;
    private FragmentActivity mActivity;
    private Fragment mFragment;
    private AfterSendMessageListener mListener;

    private HandlerTimer mHandlerTimer;
    private boolean isVoice60 = false;

    private DefaultChatInputHelper(Context context, MessageSenderHelper msgSenderHelper, ChatKeyboard chatKeyboard) {
        this.mMsgSenderHelper = msgSenderHelper;
        this.mChatKeyboard = chatKeyboard;
        mContext = context;
        mPictureHelper = new PictureHelper();
        mRecorderHelper = RecorderHelper.getInstance(DirUtils.getRecorderCachePath());
        mChatKeyboard.setListener(this);
        mBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        mRecorderVoiceLevelPopup = new RecorderVoiceLevelPopup();
    }

    public DefaultChatInputHelper(FragmentActivity activity, MessageSenderHelper msgSenderHelper, ChatKeyboard chatKeyboard) {
        this((Context) activity, msgSenderHelper, chatKeyboard);
        mActivity = activity;
    }

    public DefaultChatInputHelper(Fragment fragment, MessageSenderHelper msgSenderHelper, ChatKeyboard chatKeyboard) {
        this((Context) fragment.getActivity(), msgSenderHelper, chatKeyboard);
        mFragment = fragment;
    }


    public void setAfterSendMessageListener(AfterSendMessageListener listener) {
        mListener = listener;
    }

    /**
     * 设置是否允许发送消息
     */
    public void setIsEnable(boolean isEnable) {
        mIsEnable = isEnable;
    }

    private boolean canSendMessage() {
        if (mIsEnable && CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            Toast.makeText(mContext, "当前无法连接网络, 无法发送消息", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!mIsEnable) {
            Toast.makeText(mContext, "黑名单用户禁止发言", Toast.LENGTH_SHORT).show();
        }
        return mIsEnable;
    }

    @Override
    public void onSendClick(String message) {
        if (canSendMessage()) {
            if (TextUtils.isEmpty(message))
                return;
            MessageEntity textMessage = new MessageEntity.MessageBuilder()
                    .setContent(message.replace(" ", ""))
                    .setType(MessageEntity.TYPE_TEXT)
                    .setId(-1)
                    .build();
            onSendMessage(mMsgSenderHelper.getGoodsMessageEntity(message));
            mMsgSenderHelper.sendTextMessage(message);
        }
    }

    @Override
    public void onPicClick() {
        if (canSendMessage()) {
            if (mActivity != null) {
                mPictureHelper.pickImage(mActivity, REQUEST_PICK_IMAGE);
            } else if (mFragment != null) {
                mPictureHelper.pickImage(mFragment, REQUEST_PICK_IMAGE);
            } else {
                Log.e(TAG, "no activity or fragments");
            }
        }
    }

    @Override
    public void onVoiceTouchStart() {
        if (canSendMessage()) {
            mRecorderHelper.prepareAndStartRecorder();
            mRecorderVoiceLevelPopup.show(mContext, mChatKeyboard);
            if (mHandlerTimer != null) {
                mHandlerTimer.cancel();
            }
            mHandlerTimer = new HandlerTimer(new Runnable() {
                @Override
                public void run() {
                    updateVoiceLevel();
                }
            });
            mHandlerTimer.start();
            isVoice60 = false;
        }
    }

    private void updateVoiceLevel() {
        if (mRecorderVoiceLevelPopup != null) {
            mRecorderVoiceLevelPopup.setVoiceLevel(mRecorderHelper.getVoiceLevel(6));
            mRecorderVoiceLevelPopup.setVoiceTimer((int)(mRecorderHelper.getTimeLong() / 1000));
        }
    }

    @Override
    public void onVoiceTouchCancel() {
        mRecorderVoiceLevelPopup.dismiss();
        if (mHandlerTimer != null) {
            mHandlerTimer.cancel();
            mHandlerTimer = null;
        }
        if (canSendMessage()) {
            mRecorderHelper.stop();
        }
    }

    @Override
    public void onVoiceTouchEnd() {
        mRecorderVoiceLevelPopup.dismiss();
        if (mHandlerTimer != null) {
            mHandlerTimer.cancel();
            mHandlerTimer = null;
        }
        if (!canSendMessage())
            return;
        if (mRecorderHelper.stop()) {
            MessageEntity entity = mMsgSenderHelper.sendAudioMessage(
                    mRecorderHelper.getCurrentRecorderPath(), mRecorderHelper.getTimeLong());
            onSendMessage(entity);
        } else if(!isVoice60){
            Toast.makeText(mContext, "录制音频太短", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onVoice60(Button mVoiceBtn) {
        isVoice60 = true;
        mVoiceBtn.setText("按住 说话");
        mVoiceBtn.setBackgroundResource(R.drawable.circle4_f_ponit5_d);
        OSUtil.vibrate(mContext, 100);
        onVoiceTouchEnd();
    }

    private void onSendMessage(MessageEntity entity) {
        if (mListener != null) {
            mListener.onSendMessage(entity);
        }
    }

    @Override
    public void onFunctionClick(int functionId) {
        switch (functionId) {
            case ChatFunctionFragment.INDEX_VIDEO:
                recordVideo();
                break;
            case ChatFunctionFragment.INDEX_PICTURE:
                onPicClick();
                break;
            case ChatFunctionFragment.INDEX_TAKE_PHOTO:
                onTakePhotoClick();
                break;
            case ChatFunctionFragment.INDEX_LIVE:
                onLiveClick();
                break;
        }
    }

    private void onLiveClick() {
        AlertDialogUtils.needLoginOperator(mContext, new Runnable() {
            @Override
            public void run() {
                AlertDialogUtils.runNeedWifiOperation(mContext, new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(mContext, HostWindowActivity.class);
                        intent.putExtra(HostWindowActivity.LIVE_IS_HOST, true);
                        intent.putExtra(HostWindowActivity.LIVE_IS_GROUP, true);
                        if (mActivity != null) {
                            mActivity.startActivity(intent);
                        } else {
                            mFragment.startActivity(intent);
                        }
                    }
                });
            }
        });
    }

    private void onTakePhotoClick() {
        mCurrentPhotoPath = DirUtils.getImageCachePath() + "/" + UUID.randomUUID() + ".jpg";
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCurrentPhotoPath)));
        if (mActivity == null) {
            mFragment.startActivityForResult(photoIntent, REQUEST_TAKE_PHOTO);
        } else {
            mActivity.startActivityForResult(photoIntent, REQUEST_TAKE_PHOTO);
        }
    }

    private void recordVideo() {
        FragmentManager fragmentManager = mActivity != null ? mActivity.getSupportFragmentManager() : mFragment.getChildFragmentManager();
        VideoInputDialog.show(fragmentManager);
    }

    public void onResume() {
        IntentFilter filter = new IntentFilter(VideoInputDialog.ACTION);
        mBroadcastManager.registerReceiver(mVideoInputBroadcastReceiver, filter);
    }

    public void onStop() {
        mBroadcastManager.unregisterReceiver(mVideoInputBroadcastReceiver);
    }

    private BroadcastReceiver mVideoInputBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new AsyncTask<Intent, Void, String[]>() {
                @Override
                protected String[] doInBackground(Intent... params) {
                    Intent intent = params[0];
                    String videoPath = intent.getStringExtra("result");
                    String thumbnailPath = videoPath + "_thumbnail";
                    VideoModule.generateVideoFirstFrame(videoPath, thumbnailPath);
                    String[] result = new String[2];
                    result[0] = videoPath;
                    result[1] = thumbnailPath;
                    return result;
                }

                @Override
                protected void onPostExecute(String[] strings) {
                    String videoPath = strings[0];
                    String thumbnailPath = strings[1];
                    MLog.d(TAG, "onReceiver, the videoPath is %s.", videoPath);
                    onSendMessage(mMsgSenderHelper.sendVideoMessage(videoPath, thumbnailPath));
                }
            }.execute(intent);
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == REQUEST_PICK_IMAGE) {
            String tmpCurrentPicturePath = DirUtils.getImageCachePath() + "/" + UUID.randomUUID() + ".jpg";
            mPictureHelper.onPickResult(mContext, tmpCurrentPicturePath, data);
            onSendMessage(mMsgSenderHelper.sendPictureMessage(tmpCurrentPicturePath));
        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            onSendMessage(mMsgSenderHelper.sendPictureMessage(mCurrentPhotoPath));
        }
    }

    public static class HandlerTimer {
        private Handler mHandler;
        private Runnable mRunnable;
        private boolean misCanceled = false;

        public HandlerTimer(@NonNull Runnable runnable) {
            mHandler = new Handler();
            mRunnable = runnable;
        }

        public void start() {
            mHandler.post(task);
        }

        public void cancel() {
            mHandler.removeCallbacks(task);
            misCanceled = true;
        }

        private Runnable task = new Runnable() {
            @Override
            public void run() {
                mHandler.removeCallbacks(task);
                if (!misCanceled) {
                    mRunnable.run();
                    mHandler.postDelayed(task, 500);
                }
            }
        };
    }
}
