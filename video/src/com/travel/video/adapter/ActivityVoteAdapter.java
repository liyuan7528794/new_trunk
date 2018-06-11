package com.travel.video.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.video.bean.ActivityVoteInfo;

import java.util.List;

public class ActivityVoteAdapter extends BaseAdapter {
	private List<ActivityVoteInfo> list;
	private LayoutInflater inflater;

	public ActivityVoteAdapter(Context context, List<ActivityVoteInfo> list) {
		this.list = list;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public int getCount() {

		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ActivityVoteInfo bean =  list.get(position);
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.activity_vote_item, null);
			holder = new ViewHolder();
			holder.headImg = (ImageView) convertView.findViewById(R.id.headImg);
			holder.name = (TextView) convertView.findViewById(R.id.nickName);
			holder.num = (TextView) convertView.findViewById(R.id.num);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String imageUrl = bean.getImgUrl();
		if("".equals(imageUrl)){
			imageUrl = Constants.DefaultHeadImg;
		}
		ImageDisplayTools.displayHeadImage(imageUrl, holder.headImg);
		holder.name.setText(bean.getNickName());
		holder.num.setText(bean.getVoteNum()+"");
		
		return convertView;
	}

	class ViewHolder {
		ImageView headImg;
		TextView name;
		TextView num;
	}
}