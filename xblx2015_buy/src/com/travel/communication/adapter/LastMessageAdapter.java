package com.travel.communication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.communication.dao.LastMessage;
import com.travel.communication.dao.Message;
import com.travel.communication.entity.MessageEntity;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 消息回话的显示最后一条消息的Adapter
 *
 * @author ldkxingzhe
 */
public class LastMessageAdapter extends ListBaseAdapter<LastMessage> {
    @SuppressWarnings("unused")
    private static final String TAG = "LastMessageAdapter";

    public LastMessageAdapter(List<LastMessage> list) {
        super(list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.last_message_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.headerImage = (ImageView) convertView.findViewById(R.id.iv_header);
            viewHolder.name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.lastMessageText = (TextView) convertView.findViewById(R.id.tv_last_message);
            viewHolder.unReadNumber = (TextView) convertView.findViewById(R.id.tv_unread_number);
            viewHolder.time = (TextView) convertView.findViewById(R.id.tv_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        LastMessage lastMessage = getItem(position);
        Message realMessage = lastMessage.getMessage();
        if (MessageEntity.TYPE_ORDERS == realMessage.getMessageType()) {
            if(OSUtil.isDayTheme())
            viewHolder.headerImage.setImageResource(R.drawable.message_pic_notice_day);
           else
            viewHolder.headerImage.setImageResource(R.drawable.message_pic_notice_night);
            viewHolder.name.setText("公告通知");
        }else {
            ImageDisplayTools.disPlayRoundDrawableHead(
                    lastMessage.getSender().getImgUrl(),
                    viewHolder.headerImage,
                    OSUtil.dp2px(convertView.getContext(), 4));
            if(!OSUtil.isDayTheme())
                viewHolder.headerImage.setColorFilter(TravelUtil.getColorFilter(parent.getContext()));
            viewHolder.name.setText(lastMessage.getSender().getNickName());
        }
        if (realMessage == null)
            return convertView;
        switch (realMessage.getMessageType()) {
            case MessageEntity.TYPE_IMAGE:
                viewHolder.lastMessageText.setText("图片");
                break;
            case MessageEntity.TYPE_SOUND:
                viewHolder.lastMessageText.setText("语音");
                break;
            case MessageEntity.TYPE_VIDEO:
                viewHolder.lastMessageText.setText("视频");
                break;
            case MessageEntity.TYPE_ORDERS:
                try {
                    JSONObject object = new JSONObject(realMessage.getContent());
                    viewHolder.lastMessageText.setText(JsonUtil.getJson(object, "content"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    viewHolder.lastMessageText.setText("订单详情");
                }
                break;
            case MessageEntity.TYPE_GOODS_INFO:
                viewHolder.lastMessageText.setText("商品详情");
                break;
            default:
                viewHolder.lastMessageText.setText(realMessage.getContent());
        }
        int unReadNO = lastMessage.getUnReadNumber();
        if (unReadNO <= 0) {
            viewHolder.unReadNumber.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.unReadNumber.setVisibility(View.VISIBLE);
//            viewHolder.unReadNumber.setText(String.valueOf(unReadNO));
        }
        viewHolder.time.setText(DateFormatUtil.getChatTime(DateFormatUtil
                .UTCTime2LocalTime(realMessage.getCreate()).getTime(), DateFormatUtil.FORMAT_DATE));
        return convertView;
    }

    private class ViewHolder {
        // 头像
        ImageView headerImage;
        // 名称, 最后一条消息时间, 最后一条消息内容, 未读消息条数
        TextView name, time, lastMessageText, unReadNumber;
    }
}
