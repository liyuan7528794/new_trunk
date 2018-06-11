package com.travel.video.adapter;

import android.app.Activity;
import android.content.Context;
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
import com.travel.communication.entity.UserData;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.PopWindowUtils;
import com.travel.lib.utils.TravelUtil;

import java.util.List;
/**
 * 可滑动的 横向的 头像列表
 * 【主要用于直播，视频中的头像显示
 * @author Administrator
 *
 */
public class HorizontalHeadListViewAdapter extends BaseAdapter{
    private List<UserData> list;
    private Activity mContext;  
    private LayoutInflater mInflater;
    private int selectIndex = -1;
    private boolean isClickImage = true;
  
    /**
     * 横向头像列表适配器
     * @param context
     * @param list
     * @param isClickImage 表示是否可点击图片，true表示可点击，false表示不可点击	（有些地方逻辑处理要放到点击条目而不是头像）
     */
    public HorizontalHeadListViewAdapter(Activity context, List<UserData> list, boolean isClickImage){
        this.mContext = context;  
        this.list = list;  
        this.isClickImage = isClickImage;
        mInflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//LayoutInflater.from(mContext);  
    }  
    @Override  
    public int getCount() {  
    	if(list!=null)
    		return list.size();
    	else
    		return 0;
    }  
    @Override  
    public Object getItem(int position) {  
        return position;  
    }  
  
    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
  
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
    	final UserData userData = list.get(position);
        ViewHolder holder;  
        if(convertView==null){  
            holder = new ViewHolder();  
            convertView = mInflater.inflate(R.layout.horizontal_head_list_item, null);  
            holder.mImage=(ImageView)convertView.findViewById(R.id.head_item);  
            holder.nickname = (TextView) convertView.findViewById(R.id.nickname_item);
            convertView.setTag(holder);
        }else{  
            holder=(ViewHolder)convertView.getTag();  
        }
        String imageUrl = userData.getImgUrl();
		if("".equals(imageUrl)){
			imageUrl = Constants.DefaultHeadImg;
		}
        ImageDisplayTools.displayHeadImage(imageUrl,holder.mImage);
        if (!OSUtil.isDayTheme())
            holder.mImage.setColorFilter(TravelUtil.getColorFilter(mContext));
        holder.nickname.setText(userData.getNickName());
        
        if(isClickImage){
        	holder.mImage.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				
    				if(userData.getId().length() < 8)
    					PopWindowUtils.followPopUpWindow(mContext, userData.getId(), userData.getNickName(), userData.getImgUrl(),1);
    				else
    					Toast.makeText(mContext, "该用户未登录，没有用户信息", Toast.LENGTH_SHORT).show();
    				
    			}
    		});
        }
        return convertView;  
    }

    private static class ViewHolder {  
        private ImageView mImage; 
        private TextView nickname;
    }
    public void setSelectIndex(int i){  
        selectIndex = i;
    }  
}