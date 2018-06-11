package com.travel.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.lib.TravelApp;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;

import java.util.List;

/**
 * Created by Administrator on 2017/4/25.
 */

public class StoryHomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context mContext;
    private List<GoodsBasicInfoBean> datas;
    private OnItemListener onItemListener;
    public StoryHomeAdapter(Context context, List<GoodsBasicInfoBean> datas){
        this.mContext = context;
        this.datas = datas;
    }

    public void setOnItemClick(OnItemListener itemClick){
        this.onItemListener = itemClick;
    }

    public interface OnItemListener {
        public void onItemClick(View view, int position);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_story_home_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        int outMargin = OSUtil.dp2px(mContext, 15);
        int inMargin = OSUtil.dp2px(mContext, 5.5f);
        if (position % 2 == 0) {
            if (position == 0)
                ((MyHolder) holder).itemView.setPadding(outMargin, inMargin*2, inMargin, 0);
            else if (position == datas.size() - 2 || position == datas.size() - 1)
                ((MyHolder) holder).itemView.setPadding(outMargin, 0, inMargin, 0);
            else
                ((MyHolder) holder).itemView.setPadding(outMargin, 0, inMargin, 0);
        } else {
            if (position == 1)
                ((MyHolder) holder).itemView.setPadding(inMargin, inMargin*2, outMargin, 0);
            else if (position == datas.size() - 1)
                ((MyHolder) holder).itemView.setPadding(inMargin, 0, outMargin, 0);
            else
                ((MyHolder) holder).itemView.setPadding(inMargin, 0, outMargin, 0);
        }
        
        GoodsBasicInfoBean bean = datas.get(position);
        ImageDisplayTools.displayImageRoundCity(bean.getGoodsImg(), ((MyHolder)holder).cover);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ((MyHolder)holder).cover.getLayoutParams();
        params.width = (OSUtil.getScreenWidth() - OSUtil.dp2px(TravelApp.appContext, 41)) / 2;
        params.height = params.width;
        ((MyHolder)holder).cover.setLayoutParams(params);
        if (!OSUtil.isDayTheme())
        ((MyHolder)holder).cover.setColorFilter(TravelUtil.getColorFilter(mContext));

//        ((MyHolder) holder).title.setText(bean.getKeyWord() + " · " + bean.getGoodsTitle());
        String text = bean.getKeyWord() + " · " + bean.getGoodsTitle();
        if( TextUtils.isEmpty(bean.getKeyWord())){
            ((MyHolder) holder).title.setText(bean.getGoodsTitle());
        }else {

            SpannableString spannableString = new SpannableString(text);
            spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    0, bean.getKeyWord().length()+2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
//                    bean.getKeyWord().length()+1, bean.getKeyWord().length()+2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            spannableString.setSpan(new AbsoluteSizeSpan(50), bean.getKeyWord().length()+1, bean.getKeyWord().length()+2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((MyHolder) holder).title.setText(spannableString);
        }

        ((MyHolder)holder).itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemListener.onItemClick(v,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView cover;
        TextView title;
        public MyHolder(View itemView) {
            super(itemView);
            cover = (ImageView) itemView.findViewById(R.id.iv_cover);
            title = (TextView) itemView.findViewById(R.id.title);
        }
    }
}
