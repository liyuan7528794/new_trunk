package com.travel.adapter;

import java.util.List;

import com.travel.Constants;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.lib.R;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ProductAdapter extends BaseAdapter {
	private Context context;
	private List<GoodsBasicInfoBean> list;
	private LayoutInflater inflater;

	public ProductAdapter(Context context, List<GoodsBasicInfoBean> list) {
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
		GoodsBasicInfoBean goodsBean =  list.get(position);
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.product_pop_window_item, null);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.productImage);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.image.getLayoutParams();
			params.width = OSUtil.getScreenWidth() - OSUtil.dp2px(context, 30);
			params.height = params.width * 2 /5;
			holder.image.setLayoutParams(params);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.price = (TextView) convertView.findViewById(R.id.price);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String imageUrl = goodsBean.getGoodsImg();
		if("".equals(imageUrl)){
			imageUrl = Constants.DefaultHeadImg;
		}
		ImageDisplayTools.displayImage(imageUrl, holder.image);
		holder.name.setText(goodsBean.getGoodsTitle());
		holder.price.setText("￥"+goodsBean.getGoodsPrice()+"元");
		
		return convertView;
	}

	class ViewHolder {
		ImageView image;
		TextView name;
		TextView price;
	}
}