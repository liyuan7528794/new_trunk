package com.travel.video.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.PopWindowUtils;
import com.travel.lib.utils.FormatUtils;
import com.travel.lib.utils.TravelUtil;
import com.travel.video.bean.BarrageInfo;
import com.travel.video.tools.RandomSetColor;

import java.util.List;
import java.util.Map;
/**
 * 弹幕适配器
 * @author Administrator
 *
 */
public class BarrageAdapter extends BaseAdapter{
	private List<BarrageInfo> list;
	private Activity context;
	private LayoutInflater inflater;
	public BarrageAdapter(Activity context,List<BarrageInfo> list) {
		this.context = context;
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
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.barrage_item, null);
			holder = new ViewHolder();
			holder.headImg = (ImageView) convertView.findViewById(R.id.barrage_headimg);
			holder.nickname = (TextView) convertView.findViewById(R.id.barrage_nickname);
//			holder.content = (TextView) convertView.findViewById(R.id.barrage_content);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if(list.size() <= position) return convertView;

		final BarrageInfo barrageInfo = list.get(position);
		final String userId = barrageInfo.getUserId();
		final String nickName = barrageInfo.getNickName();
		int mark;
		if("".equals(userId) || "-1".equals(userId)){
			mark = 0;
		}else{
			try {
				mark = Integer.parseInt(userId)%10;
			}catch (Exception e){
				mark = 0;
			}
		}

		holder.headImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(userId!=null && userId.length() < 8)
					PopWindowUtils.followPopUpWindow(context, userId, nickName,barrageInfo.getUserImg(),1);
				else
					Toast.makeText(context, "该用户未登录，没有用户信息", Toast.LENGTH_SHORT).show();
			}
		});
		
		Map<String ,Integer> colorMap = RandomSetColor.getColorMap();
		String imageUrl = barrageInfo.getUserImg();
		if("".equals(imageUrl)){
			imageUrl = Constants.DefaultHeadImg;
		}
		ImageDisplayTools.displayHeadImage(imageUrl, holder.headImg);
		if (!OSUtil.isDayTheme())
			holder.headImg.setColorFilter(TravelUtil.getColorFilter(context));

		SpannableStringBuilder spBuilder = FormatUtils.StringSetSpanColor(context, nickName+" "+barrageInfo.getContent(),
				nickName, (int)(colorMap.get(mark+"")));
		if(spBuilder != null){
			holder.nickname.setText(spBuilder);
		}else{
			holder.nickname.setText(nickName+" "+barrageInfo.getContent());
		}

		return convertView;
	}
	private class ViewHolder {
		public ImageView headImg;
		public TextView nickname;
	}

}
