package com.travel.communication.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.ctsmedia.hltravel.R;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.travel.activity.OneFragmentActivity;
import com.travel.app.TravelApp;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.communication.entity.MessageEntity;
import com.travel.communication.fragment.SystemOrderMessageFragment;
import com.travel.communication.helper.DateParseHelper;
import com.travel.communication.helper.ListViewOnScrollChangedListener;
import com.travel.communication.helper.SQliteHelper;
import com.travel.communication.utils.GoodsInfoBeanJsonUtil;
import com.travel.communication.wrapper.OnClickListenerWrapper;
import com.travel.fragment.WebViewFragment;
import com.travel.layout.ChatImageView;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.activity.GoodsActivity;

import java.util.List;

public class MessageAdapter extends ListBaseAdapter<MessageEntity> {
    @SuppressWarnings("unused")
    private static final String TAG = "MessageAdapter";

    private long mCurrentShowTime = 0;
    private DateParseHelper mDateParseHelper = new DateParseHelper();
    private VideoView mVideoView;

    public interface MessageListener {
        /**
         * 头像被点击事件,
         */
        void onHeaderClick(MessageEntity entity);

        /**
         * 录音被点击
         */
        void onVoiceClick(MessageEntity entity, ImageView voiceImage);

        /**
         * 图片信息被点击
         *
         * @param entity
         */
        void onImageClick(MessageEntity entity);

        void onVideoClick(MessageEntity entity, String videoPath);

        void onItemLongClick(View view, int position, float lastRawX, float lastRawY);
    }

    private MessageListener mMessageLister;

    public void setOnHeaderClickListener(MessageListener listener) {
        mMessageLister = listener;
    }

    private SQliteHelper mSQliteHelper;

    public MessageAdapter(List<MessageEntity> list, @NonNull SQliteHelper sQliteHelper) {
        super(list);
        mSQliteHelper = sQliteHelper;
    }

    public void setVideoView(VideoView videoView) {
        mVideoView = videoView;
    }

    @Override
    public int getViewTypeCount() {
        return MessageEntity.TYPE_COUNT * 2;
    }

    @Override
    public int getItemViewType(int position) {
        MessageEntity entity = getItem(position);
        if (entity.isMine()) {
            // 是我发送的消息
            return entity.getMessageType();
        } else {
            return entity.getMessageType() + MessageEntity.TYPE_COUNT;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageEntity entity = getItem(position);
        switch (getItemViewType(position) % MessageEntity.TYPE_COUNT) {
            case MessageEntity.TYPE_TEXT:
                convertView = dealWithTextMessage(entity, convertView, parent, position);
                break;
            case MessageEntity.TYPE_IMAGE:
                convertView = dealWithImageMessage(entity, convertView, parent, position);
                break;
            case MessageEntity.TYPE_SOUND:
                convertView = dealWithSoudMessage(entity, convertView, parent, position);
                break;
            case MessageEntity.TYPE_GOODS_INFO:
                convertView = dealWithGoodsInfoMessage(entity, convertView, parent, position);
                break;
            case MessageEntity.TYPE_VIDEO:
                convertView = dealWithVideoMessage(entity, convertView, parent, position);
                break;
            default:
                MLog.e(TAG, "type is wrong");
        }
        return convertView;
    }

    private View dealWithVideoMessage(final MessageEntity entity, View convertView,
                                      ViewGroup parent, int position) {
        VideoViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = generateConvertView(parent, entity.isMine());
            viewHolder = new VideoViewHolder(convertView);
            convertView.setTag(viewHolder);
            viewHolder.videoLayout.setVisibility(View.VISIBLE);
        } else {
            viewHolder = (VideoViewHolder) convertView.getTag();
        }
        final String[] paths = entity.getContent().split(",");
        if (paths.length != 2 || !paths[1].equals(viewHolder.mVideoContainer.getTag())) {
            viewHolder.mVideoContainer.removeAllViews();
            viewHolder.playImageView.setVisibility(View.VISIBLE);
            viewHolder.mVideoContainer.setVisibility(View.INVISIBLE);
        }
        viewHolder.thumbnailImageLeft.setVisibility(View.GONE);
        viewHolder.thumbnailImageRight.setVisibility(View.GONE);
        if (paths.length == 2) {
            if (entity.isMine()) {
                viewHolder.thumbnailImageRight.setVisibility(View.VISIBLE);
                ImageDisplayTools.displayImage(paths[1], viewHolder.thumbnailImageRight);
                if (!OSUtil.isDayTheme())
                    viewHolder.thumbnailImageRight.setColorFilter(TravelUtil.getColorFilter(parent.getContext()));
            } else {
                viewHolder.thumbnailImageLeft.setVisibility(View.VISIBLE);
                ImageDisplayTools.displayImage(paths[1], viewHolder.thumbnailImageLeft);
                if (!OSUtil.isDayTheme())
                    viewHolder.thumbnailImageLeft.setColorFilter(TravelUtil.getColorFilter(parent.getContext()));
            }
            //			ImageDisplayTools.disPlayRoundDrawable(paths[1], viewHolder.thumbnailImage, 20);
            viewHolder.mVideoContainer.setTag(paths[0]);
            if (mVideoView != null) {
                final VideoViewHolder finalViewHolder = viewHolder;
                viewHolder.playImageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //						v.setVisibility(View.INVISIBLE);
                        ListViewOnScrollChangedListener.videoPlay(mVideoView, finalViewHolder.mVideoContainer);
                    }
                });

                viewHolder.thumbnailImageLeft.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListViewOnScrollChangedListener.stopAnotherVideo(mVideoView, null);
                        if (mMessageLister != null) {
                            mMessageLister.onVideoClick(entity, paths[0]);
                        }
                    }
                });
                viewHolder.thumbnailImageRight.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListViewOnScrollChangedListener.stopAnotherVideo(mVideoView, null);
                        if (mMessageLister != null) {
                            mMessageLister.onVideoClick(entity, paths[0]);
                        }
                    }
                });
            }
        }
        commmonSetting(entity, viewHolder, position);
        return convertView;
    }

    private View dealWithGoodsInfoMessage(MessageEntity entity, View convertView, ViewGroup parent, final int position) {
        SystemOrderMessageFragment.SystemOrderAdapter.SystemOrderMessageViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflateView(parent, R.layout.list_item_system_order);
            viewHolder = new SystemOrderMessageFragment.SystemOrderAdapter.SystemOrderMessageViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SystemOrderMessageFragment.SystemOrderAdapter.SystemOrderMessageViewHolder) convertView.getTag();
        }
        final GoodsBasicInfoBean bean = GoodsInfoBeanJsonUtil.from(entity.getContent());
        viewHolder.getContent().setText(bean.getGoodsTitle());
        viewHolder.getCreateTime().setText(mDateParseHelper.parserTime(entity.getCreate()));
        viewHolder.getCover().setVisibility(View.VISIBLE);
        ImageDisplayTools.displayImage(bean.getGoodsImg(), viewHolder.getCover());
        if (!OSUtil.isDayTheme())
            viewHolder.getCover().setColorFilter(TravelUtil.getColorFilter(parent.getContext()));

        convertView.setOnTouchListener(new OnClickListenerWrapper(new OnClickListenerWrapper.Listener() {
            @Override
            public void onClick(View view, float rawX, float rawY) {
                MLog.d(TAG, "onGoodsMessageClick");
                Intent intent = new Intent(view.getContext(), GoodsActivity.class);
                intent.putExtra("goodsId", String.valueOf(bean.getGoodsId()));
                view.getContext().startActivity(intent);
//                CommitOrderActivity.actionStart(view.getContext(), String.valueOf(bean.getGoodsId()), "", 2);
            }

            @Override
            public void onLongClick(View view, float rawX, float rawY) {
                mMessageLister.onItemLongClick(view, position, rawX, rawY);
            }
        }));
        return convertView;
    }

    private View dealWithImageMessage(final MessageEntity entity, View convertView, final ViewGroup parent, final int position) {
        final ImageViewHolder viewHolder;
        if (convertView == null) {
            if (entity.isMine()) {
                convertView = inflateView(parent, R.layout.chat_measge_list_item_right);
            } else {
                convertView = inflateView(parent, R.layout.chat_measge_list_item_left);
            }
            viewHolder = new ImageViewHolder(convertView);
            convertView.setTag(viewHolder);
            viewHolder.messageImage.setVisibility(View.VISIBLE);
        } else {
            viewHolder = (ImageViewHolder) convertView.getTag();
        }

        commmonSetting(entity, viewHolder, position);
        ImageDisplayTools.displayDrawableChat(entity.getContent(), viewHolder.messageImage, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                ViewGroup.LayoutParams params = viewHolder.messageImage.getLayoutParams();
                params.height = (int) (params.width * 1.0f / loadedImage.getWidth() * loadedImage.getHeight());
                viewHolder.messageImage.setLayoutParams(params);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
        if(!OSUtil.isDayTheme())
        viewHolder.messageImage.setColorFilter(TravelUtil.getColorFilter(TravelApp.appContext));
        viewHolder.messageImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMessageLister != null) {
                    mMessageLister.onImageClick(entity);
                }
            }
        });
        return convertView;
    }

    private View dealWithSoudMessage(final MessageEntity entity, View convertView, ViewGroup parent, int position) {
        VoiceViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = generateConvertView(parent, entity.isMine());
            viewHolder = new VoiceViewHolder(convertView);
            convertView.setTag(viewHolder);
            viewHolder.voiceLayout.setVisibility(View.VISIBLE);
        } else {
            viewHolder = (VoiceViewHolder) convertView.getTag();
        }
        commmonSetting(entity, viewHolder, position);
        if (!entity.isMine()) {
            if (entity.getState() == MessageEntity.STATE_SENDING) {
                viewHolder.voiceProgressBar.setVisibility(View.VISIBLE);
            } else {
                viewHolder.voiceProgressBar.setVisibility(View.GONE);
            }
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.rl_voice_width.getLayoutParams();
        params.width = OSUtil.dp2px(com.travel.lib.TravelApp.appContext,(52 + 3 * entity.getTimeLong()));
        viewHolder.rl_voice_width.setLayoutParams(params);

        viewHolder.voiceTimeText.setText(entity.getTimeLong() + "\"");
        viewHolder.voiceLayout.setTag(viewHolder.voiceImage);
        viewHolder.voiceLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMessageLister != null) {
                    mMessageLister.onVoiceClick(entity, (ImageView) v.getTag());
                }
            }
        });
        return convertView;
    }

    private View dealWithTextMessage(MessageEntity entity, View convertView, ViewGroup parent, final int position) {
        TextViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = generateConvertView(parent, entity.isMine());
            viewHolder = new TextViewHolder(convertView);
            convertView.setTag(viewHolder);
            viewHolder.messageText.setVisibility(View.VISIBLE);
        } else {
            viewHolder = (TextViewHolder) convertView.getTag();
        }

        commmonSetting(entity, viewHolder, position);
        viewHolder.messageText.setText(entity.getContent());
        viewHolder.messageText.setOnTouchListener(new OnClickListenerWrapper(new OnClickListenerWrapper.Listener() {
            @Override
            public void onClick(View view, float rawX, float rawY) {
                mMessageTextClickListener.onClick(view);
            }

            @Override
            public void onLongClick(View view, float rawX, float rawY) {
                mMessageLister.onItemLongClick(view, position, rawX, rawY);
            }
        }));
        return convertView;
    }

    private OnClickListener mMessageTextClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!(v instanceof TextView))
                return;

            CharSequence charSequence = ((TextView) v).getText();
            Spannable spannable;
            if (charSequence instanceof Spannable) {
                spannable = (Spannable) charSequence;
            } else {
                spannable = new SpannableString(charSequence);
            }

            URLSpan[] urls = spannable.getSpans(0, charSequence.length(), URLSpan.class);
            if (urls.length >= 1) {
                Bundle bundle = new Bundle();
                bundle.putString(WebViewFragment.LOAD_URL, urls[0].getURL());
                OneFragmentActivity.startNewActivity(v.getContext(), "加载中...", WebViewFragment.class, bundle);
            }
        }
    };

    private View generateConvertView(ViewGroup parent, boolean isRight) {
        if (isRight) {
            return inflateView(parent, R.layout.chat_measge_list_item_right);
        } else {
            return inflateView(parent, R.layout.chat_measge_list_item_left);
        }
    }

    private void commmonSetting(final MessageEntity entity, ViewHolder viewHolder, int position) {
        if ((Math.abs(entity.getCreate().getTime() - mCurrentShowTime) > 3600000)
                || position == 0) {
            viewHolder.dateText.setText(mDateParseHelper.parserTime(entity.getCreate()));
            viewHolder.dateText.setVisibility(View.VISIBLE);
            mCurrentShowTime = entity.getCreate().getTime();
        } else {
            viewHolder.dateText.setVisibility(View.GONE);
        }
        viewHolder.nameText.setText(mSQliteHelper.getUserData(entity.getSenderId()).getNickName());
        ImageDisplayTools.disPlayRoundDrawableHead(
                mSQliteHelper.getUserData(entity.getSenderId()).getImgUrl(),
                viewHolder.headerImage, OSUtil.dp2px(TravelApp.appContext, 3));
        if(!OSUtil.isDayTheme())
            viewHolder.headerImage.setColorFilter(TravelUtil.getColorFilter(TravelApp.appContext));
        if (entity.isMine()) {
            int progressBarVisibility = View.INVISIBLE;
            int failedImageVisibility = View.INVISIBLE;
            switch (entity.getState()) {
                case MessageEntity.STATE_SENDING:
                    progressBarVisibility = View.VISIBLE;
                    break;
                case MessageEntity.STATE_FAILED:
                    failedImageVisibility = View.VISIBLE;
                    break;
                default:
                    break;
            }
            //			viewHolder.progressBar.setVisibility(progressBarVisibility);
            //			viewHolder.failedImage.setVisibility(failedImageVisibility);
        }
        viewHolder.headerImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMessageLister != null) {
                    mMessageLister.onHeaderClick(entity);
                }
            }
        });
    }

    private View inflateView(ViewGroup parent, int layoutId) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return inflater.inflate(layoutId, parent, false);
    }

    private class ViewHolder {
        ImageView headerImage, failedImage;
        TextView dateText, nameText;
        ProgressBar progressBar;

        public ViewHolder(View view) {
            headerImage = (ImageView) view.findViewById(R.id.iv_header);
            failedImage = (ImageView) view.findViewById(R.id.iv_chat_failed);
            progressBar = (ProgressBar) view.findViewById(R.id.pb_chat_progress);
            dateText = (TextView) view.findViewById(R.id.tv_chat_message_date);
            nameText = (TextView) view.findViewById(R.id.tv_name);
        }
    }

    private class TextViewHolder extends ViewHolder {
        TextView messageText;

        public TextViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.tv_message);
        }
    }

    private class ImageViewHolder extends ViewHolder {
        ChatImageView messageImage;

        public ImageViewHolder(View view) {
            super(view);
            messageImage = (ChatImageView) view.findViewById(R.id.iv_chat_image);
        }
    }

    private class VideoViewHolder extends ViewHolder {
        ChatImageView thumbnailImageLeft, thumbnailImageRight;
        ImageView playImageView;

        View videoLayout;
        RelativeLayout mVideoContainer;

        public VideoViewHolder(View view) {
            super(view);
            videoLayout = view.findViewById(R.id.fl_video_layout);
            thumbnailImageLeft = (ChatImageView) view.findViewById(R.id.iv_video_backgroud_left);
            thumbnailImageRight = (ChatImageView) view.findViewById(R.id.iv_video_backgroud_right);
            playImageView = (ImageView) view.findViewById(R.id.iv_play);
            mVideoContainer = (RelativeLayout) view.findViewById(R.id.fl_video_view_container);
        }
    }

    private class VoiceViewHolder extends ViewHolder {
        // 录音时常
        TextView voiceTimeText;
        ProgressBar voiceProgressBar;
        ImageView voiceImage;
        View voiceLayout;
        RelativeLayout rl_voice_width;

        public VoiceViewHolder(View view) {
            super(view);
            voiceTimeText = (TextView) view.findViewById(R.id.tv_voice_time);
            voiceProgressBar = (ProgressBar) view.findViewById(R.id.pb_voice);
            voiceImage = (ImageView) view.findViewById(R.id.iv_voice);
            voiceLayout = view.findViewById(R.id.ll_voice_layout);
            rl_voice_width = (RelativeLayout)view.findViewById(R.id.rl_voice_width);
        }

    }
}
