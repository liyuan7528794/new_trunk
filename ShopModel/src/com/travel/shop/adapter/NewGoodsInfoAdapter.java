package com.travel.shop.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.travel.bean.GoodsServiceBean;
import com.travel.lib.TravelApp;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.R;
import com.travel.shop.tools.ShopTool;

import java.util.ArrayList;

/**
 * 商品故事的适配器
 * Created by wyp on 2017/1/12.
 */

public class NewGoodsInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<GoodsServiceBean> listData;
    private boolean isCommandVideo;// 是否是从纪录片页面进入的
    private String tag;
    private boolean isFirst = true;
    private int count = 0;// 商品详情页的“玩去旅程【other的情况】”需要，计算进入文字形式的次数，只要不是第一次就在上面加一个15dp

    public enum ITEM_TYPE {
        TEXT,
        IMAGE,
        VOICE,
        VIDEO,
        TITLE,
        TOP,
        COMMAND
    }

    public NewGoodsInfoAdapter(Context context, ArrayList<GoodsServiceBean> listData, String tag) {
        this.mContext = context;
        this.listData = listData;
        this.isCommandVideo = TextUtils.equals(tag, "1") ? true : false;
        this.tag = tag;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.TEXT.ordinal())
            return new TextHodler(View.inflate(mContext, R.layout.adapter_goodsinfo_word_item, null));
        else if (viewType == ITEM_TYPE.IMAGE.ordinal())
            return new ImageHodler(View.inflate(mContext, R.layout.adapter_goodsinfo_image_item, null));
        else if (viewType == ITEM_TYPE.VOICE.ordinal())
            return new VoiceHolder(View.inflate(mContext, R.layout.adapter_listview_layout, null));
        else if (viewType == ITEM_TYPE.VIDEO.ordinal())
            return new VideoHolder(View.inflate(mContext, R.layout.adapter_listview_layout, null));
        else if (viewType == ITEM_TYPE.TOP.ordinal()) {
            return new TopHodler(View.inflate(mContext, R.layout.adapter_goodsinfo_top_item, null));}
        else if (viewType == ITEM_TYPE.COMMAND.ordinal()) {
            return new CommandHolder(View.inflate(mContext, R.layout.adapter_goodsinfo_command_item, null));
        }
            return new TitletHodler(View.inflate(mContext, R.layout.adapter_goodsinfo_title_item, null));
    }

    //等比例缩放
    public static Bitmap adaptive(Bitmap bitmap) {
        //背景缩放
        final float scalX = OSUtil.getScreenWidth() - OSUtil.dp2px(TravelApp.appContext, 30);//屏宽
        final float scalY = OSUtil.getScreenHeight();//屏高

        Matrix matrix = new Matrix();
        int width = bitmap.getWidth();// 获取资源位图的宽
        int height = bitmap.getHeight();// 获取资源位图的高
        float w = scalX / bitmap.getWidth();
        float h = scalY / bitmap.getHeight();
        matrix.postScale(w, w);// 获取缩放比例
        // 根据缩放比例获取新的位图
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newbmp;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        int margin = OSUtil.dp2px(mContext, 15);
        holder.itemView.setPadding(margin, 0, margin, 0);
        if (holder instanceof TextHodler) {// 文字
//            if (position - 1 > -1 && listData.get(position - 1).getType() == 5)
//                holder.itemView.setPadding(OSUtil.dp2px(mContext, 15), OSUtil.dp2px(mContext, -10), OSUtil.dp2px(mContext, 15), 0);
            if (TextUtils.equals(tag, "other")) {
                ++count;
                if (count > 1) {
                    holder.itemView.setPadding(margin, OSUtil.dp2px(mContext, 15), margin, 0);
                }
                ((TextHodler) holder).tv_content_word.setTextColor(ContextCompat.getColor(mContext, R.color.gray_97));
                ((TextHodler) holder).tv_content_word.setTextSize(13f);
            }else {
                holder.itemView.setPadding(margin, OSUtil.dp2px(mContext, 28), margin, 0);
            }
            if (ShopTool.isHtml(listData.get(position).getContent())) {
                ((TextHodler) holder).tv_content_word
                        .setText(Html.fromHtml(listData.get(position).getContent()).toString());
                ((TextHodler) holder).tv_content_word_part
                        .setText(Html.fromHtml(listData.get(position).getContent()).toString());
            } else {
                ((TextHodler) holder).tv_content_word.setText(listData.get(position).getContent());
                ((TextHodler) holder).tv_content_word_part.setText(listData.get(position).getContent());
            }
            if (isCommandVideo) {
                ((TextHodler) holder).tv_content_word_part.setVisibility(View.VISIBLE);
                ((TextHodler) holder).tv_content_word.setVisibility(View.VISIBLE);
                ((TextHodler) holder).tv_content_word.post(new Runnable() {
                    @Override
                    public void run() {
                        int line = ((TextHodler) holder).tv_content_word.getLineCount();
                        if (line > 1) {
                            if (listData.get(position).getFlag() == 1) {
                                ((TextHodler) holder).tv_content_word.setVisibility(View.GONE);
                                ((TextHodler) holder).iv_expand.setImageResource(R.drawable.icon_command_video_expand);
                                ((TextHodler) holder).ll_expand.setTag(true);
                            } else if (listData.get(position).getFlag() == 2) {
                                ((TextHodler) holder).tv_content_word_part.setVisibility(View.GONE);
                                ((TextHodler) holder).iv_expand.setImageResource(R.drawable.icon_command_video_retract);
                                ((TextHodler) holder).ll_expand.setTag(false);
                            }
                        } else {
                            ((TextHodler) holder).tv_content_word_part.setVisibility(View.GONE);
                            ((TextHodler) holder).ll_expand.setVisibility(View.GONE);
                        }
                    }
                });
            }
        } else if (holder instanceof ImageHodler) {// 图片
            ImageDisplayTools.displayImage(listData.get(position).getContent(), ((ImageHodler) holder).iv_content_image);
            ShopTool.setLLParamsWidth(((ImageHodler) holder).iv_content_image, listData.get(position).getWidth(), listData.get(position).getHeight(), 30);
            if (!OSUtil.isDayTheme())
                ((ImageHodler) holder).iv_content_image.setColorFilter(TravelUtil.getColorFilter(mContext));
            if (!TextUtils.equals(tag, "other"))
                holder.itemView.setPadding(margin, OSUtil.dp2px(mContext, 30), margin, 0);
            //            try {
            //                ImageView imageView = new ImageView(mContext);
            //
            //                AsynImageLoader asynImageLoader = new AsynImageLoader();
            //                int resId = R.drawable.abc_btn_borderless_material;
            //                asynImageLoader.showImageAsyn(((ImageHodler) holder).linearLayout, imageView, listData.get(position).getContent(), resId);
            //
            //                int imgViewW = OSUtil.getScreenWidth() - OSUtil.dp2px(TravelApp.appContext, 30);
            //                int imgViewH = listData.get(position).getHeight() * imgViewW / listData.get(position).getWidth();
            //                System.out.println("原始长图w: " + listData.get(position).getWidth() + "\n原始长图h: " + listData.get(position).getHeight());
            //
            //                ((ImageHodler) holder).linearLayout.setOrientation(LinearLayout.VERTICAL);
            //                ((ImageHodler) holder).linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, imgViewH));
            //                //((ImageHodler) holder).linearLayout.setBackgroundColor(Color.BLUE);
            //                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            //                //final int scalX = OSUtil.getScreenWidth()-OSUtil.dp2px(TravelApp.appContext, 30);//屏宽
            //                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imgViewH);
            //                if (!TextUtils.equals(tag, "other"))
            //                    params.topMargin = OSUtil.dp2px(mContext, 30);
            //                imageView.setLayoutParams(params);
            //                ((ImageHodler) holder).linearLayout.addView(imageView);
            //
            //                /**
            //                 }else {
            //                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //                 bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            //                 InputStream is = new ByteArrayInputStream(baos.toByteArray());
            //                 BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance(is, false);
            //                 for (int i = 1; i <= num; i++) {
            //                 Rect rect = new Rect(0, (i - 1) * imgH / num, imgW, i * imgH / num);
            //                 Bitmap dealBitmap1 = bitmapRegionDecoder.decodeRegion(rect, null);
            //                 if (dealBitmap1 != null) {
            //                 int dealImgW1 = dealBitmap1.getWidth();
            //                 int dealImgH1 = dealBitmap1.getHeight();
            //                 int heightFollwidth = imgViewW * dealImgH1 / dealImgW1;
            //                 ImageView imageView = new ImageView(mContext);
            //                 imageView.setLayoutParams(new LinearLayout.LayoutParams(imgViewW, heightFollwidth));
            //                 imageView.setImageBitmap(dealBitmap1);
            //                 ((ImageHodler) holder).linearLayout.addView(imageView);
            //                 }
            //                 }
            //                 }
            //                 */
            //            } catch (Exception e) {
            //                e.printStackTrace();
            //            }

        } else if (holder instanceof VoiceHolder) {// 音频
            // 标题
            ((VoiceHolder) holder).tv_voice_title.setText(listData.get(position).getTitle());
            // 时间
            ((VoiceHolder) holder).tv_voice_time.setText(listData.get(position).getTime());
            ShopTool.setRLParamsWidth(((VoiceHolder) holder).rlayPlayer, 5, 1, 30);
            ((VoiceHolder) holder).update();
        } else if (holder instanceof VideoHolder) {// 视频
            ImageDisplayTools.displayImage(listData.get(position).getBackImage(), ((VideoHolder) holder).adapter_super_video_iv_cover);
            if (!OSUtil.isDayTheme())
                ((VideoHolder) holder).adapter_super_video_iv_cover.setColorFilter(TravelUtil.getColorFilter(mContext));
            //            ShopTool.setRLParamsWidth(((VideoHolder) holder).rlayPlayer, 55, 32, 30);
            //            ShopTool.setRLParamsWidth(((VideoHolder) holder).video, 55, 32, 30);
            ((VideoHolder) holder).update();
        } else if (holder instanceof TitletHodler) {// 标题
            ((TitletHodler) holder).tv_title_word.setText(listData.get(position).getContent());
        } else if (holder instanceof TopHodler) {// 顶部视频
            holder.itemView.setPadding(margin, margin, margin, 0);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (videoClickListener != null) {
                        videoClickListener.onClick(listData.get(position).getContent());
                    }
                }
            });
            ImageDisplayTools.disPlayRoundDrawable(listData.get(position).getBackImage(), ((TopHodler)holder).iv_top_img, OSUtil.dp2px(mContext, 8));
            TravelUtil.setFLParamsWidthPart(((TopHodler) holder).iv_top_img, 1, 30, 16, 9);
        } else if (holder instanceof CommandHolder) {// 推荐视频
            if (isFirst) {
                isFirst = false;
                ((CommandHolder) holder).tv_title_description.setVisibility(View.VISIBLE);
            } else {
                ((CommandHolder) holder).tv_title_description.setVisibility(View.GONE);
            }
            // 图片
            ImageDisplayTools.disPlayRoundDrawable(listData.get(position).getBackImage(), ((CommandHolder)holder).iv_command_img, OSUtil.dp2px(mContext, 2));
            // 标题
            ((CommandHolder)holder).tv_command_title.setText(listData.get(position).getTitle());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (videoClickListener != null) {
                        videoClickListener.onClick(listData.get(position).getContent());
                    }
                }
            });
        }


    }

    @Override
    public int getItemViewType(int position) {
        if (listData.get(position).getType() == 1)
            return ITEM_TYPE.TEXT.ordinal();
        else if (listData.get(position).getType() == 2)
            return ITEM_TYPE.IMAGE.ordinal();
        else if (listData.get(position).getType() == 3)
            return ITEM_TYPE.VOICE.ordinal();
        else if (listData.get(position).getType() == 4)
            return ITEM_TYPE.VIDEO.ordinal();
        else if (listData.get(position).getType() == 6)
            return ITEM_TYPE.TOP.ordinal();
        else if (listData.get(position).getType() == 7)
            return ITEM_TYPE.COMMAND.ordinal();
        else
            return ITEM_TYPE.TITLE.ordinal();
    }

    @Override
    public int getItemCount() {
        return listData != null ? listData.size() : 0;
    }


    class TextHodler extends RecyclerView.ViewHolder {

        TextView tv_content_word_part, tv_content_word;
        LinearLayout ll_expand;
        ImageView iv_expand;

        public TextHodler(View itemView) {
            super(itemView);
            tv_content_word_part = (TextView) itemView.findViewById(R.id.tv_content_word_part);
            tv_content_word = (TextView) itemView.findViewById(R.id.tv_content_word);
            ll_expand = (LinearLayout) itemView.findViewById(R.id.ll_expand);
            iv_expand = (ImageView) itemView.findViewById(R.id.iv_expand);
            if (isCommandVideo) {
                ll_expand.setVisibility(View.VISIBLE);
                ll_expand.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnExpandListener != null)
                            mOnExpandListener.onExpand((Boolean) ll_expand.getTag());
                    }
                });
            }
        }

    }

    class TitletHodler extends RecyclerView.ViewHolder {

        TextView tv_title_word;

        public TitletHodler(View itemView) {
            super(itemView);
            tv_title_word = (TextView) itemView.findViewById(R.id.tv_title_word);
        }
    }

    class ImageHodler extends RecyclerView.ViewHolder {

        ImageView iv_content_image;
        LinearLayout linearLayout;

        public ImageHodler(View itemView) {
            super(itemView);
            ImageDisplayTools.initImageLoader(mContext);
            iv_content_image = (ImageView) itemView.findViewById(R.id.iv_content_image);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.ll);
        }
    }

    class VoiceHolder extends RecyclerView.ViewHolder {

        public RelativeLayout rlayPlayerControl;
        private RelativeLayout rlayPlayer, video, voice;
        TextView tv_voice_title, tv_voice_time;

        public VoiceHolder(View itemView) {
            super(itemView);
            rlayPlayerControl = (RelativeLayout) itemView.findViewById(R.id.adapter_player_control);
            rlayPlayer = (RelativeLayout) itemView.findViewById(R.id.adapter_super_video_layout);
            video = (RelativeLayout) itemView.findViewById(R.id.video);
            voice = (RelativeLayout) itemView.findViewById(R.id.voice);
            tv_voice_title = (TextView) itemView.findViewById(R.id.tv_voice_title);
            tv_voice_time = (TextView) itemView.findViewById(R.id.tv_voice_time);
            video.setVisibility(View.GONE);
            voice.setVisibility(View.VISIBLE);
        }

        public void update() {
            rlayPlayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (playclick != null) {
                        playclick.onVoicePlayclick(VoiceHolder.this, rlayPlayerControl);
                    }
                }
            });
        }

    }


    class VideoHolder extends RecyclerView.ViewHolder {

        public RelativeLayout rlayPlayerControl;
        private RelativeLayout rlayPlayer, video, voice;
        private ImageView adapter_super_video_iv_cover;

        public VideoHolder(View itemView) {
            super(itemView);
            rlayPlayerControl = (RelativeLayout) itemView.findViewById(R.id.adapter_player_control);
            rlayPlayer = (RelativeLayout) itemView.findViewById(R.id.adapter_super_video_layout);
            video = (RelativeLayout) itemView.findViewById(R.id.video);
            voice = (RelativeLayout) itemView.findViewById(R.id.voice);
            adapter_super_video_iv_cover = (ImageView) itemView.findViewById(R.id.adapter_super_video_iv_cover);
            video.setVisibility(View.VISIBLE);
            voice.setVisibility(View.GONE);
            if (rlayPlayer != null) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rlayPlayer.getLayoutParams();
                layoutParams.height = (int) (OSUtil.getScreenWidth() * 0.5652f);//这值是网上抄来的，我设置了这个之后就没有全屏回来拉伸的效果，具体为什么我也不太清楚
                rlayPlayer.setLayoutParams(layoutParams);
            }
        }

        public void update() {
            //点击回调 播放视频
            rlayPlayerControl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (playclick != null)
                        playclick.onPlayclick(VideoHolder.this, rlayPlayerControl);
                }
            });
        }


    }


    class TopHodler extends RecyclerView.ViewHolder {

        ImageView iv_top_img;

        public TopHodler(View itemView) {
            super(itemView);
            iv_top_img = (ImageView) itemView.findViewById(R.id.iv_top_img);
        }
    }


    class CommandHolder extends RecyclerView.ViewHolder {

        ImageView iv_command_img;
        TextView tv_command_title, tv_command_description, tv_title_description;

        public CommandHolder(View itemView) {
            super(itemView);
            iv_command_img = (ImageView) itemView.findViewById(R.id.iv_command_img);
            tv_command_title = (TextView) itemView.findViewById(R.id.tv_command_title);
            tv_command_description = (TextView) itemView.findViewById(R.id.tv_command_description);
            tv_title_description = (TextView) itemView.findViewById(R.id.tv_title_description);
        }
    }

    private onPlayClick playclick;

    public void setPlayClick(onPlayClick playclick) {
        this.playclick = playclick;
    }

    public interface onPlayClick {
        void onPlayclick(RecyclerView.ViewHolder holder, RelativeLayout image);

        void onVoicePlayclick(RecyclerView.ViewHolder holder, RelativeLayout image);
    }

    private OnExpandListener mOnExpandListener;

    public void setExpandListener(OnExpandListener mOnExpandListener) {
        this.mOnExpandListener = mOnExpandListener;
    }

    public interface OnExpandListener {
        void onExpand(boolean isExpand);
    }

    public interface VideoClickListener {
        void onClick(String videoUrl);
    }
    private VideoClickListener videoClickListener;
    public void setVideoClickListener(VideoClickListener videoClickListener) {
        this.videoClickListener = videoClickListener;
    }

}
