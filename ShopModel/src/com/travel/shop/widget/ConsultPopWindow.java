package com.travel.shop.widget;

import android.widget.PopupWindow;

public class ConsultPopWindow extends PopupWindow {
//
//    private static boolean isInit = false;
//
//    private Context mContext;
//    private Activity mActivity;
//
//    private View rootView;
//    private ListView lv_custom_service;
//    private TextView tv_custom_service_none;
//    private CustomServiceAdapter mAdapter;
//    private ArrayList<VideoInfoBean> customServices;
//    private GoodsDetailBean mGoodsDetailBean;
//
//    /**
//     * 初始化
//     *
//     * @param mContext
//     * @param mActivity
//     */
//    public ConsultPopWindow(Context mContext, Activity mActivity, ArrayList<VideoInfoBean> customServices,
//                            GoodsDetailBean mGoodsDetailBean) {
//        if (isInit) {
//            isInit = false;
//            return;
//        }
//        isInit = true;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(500);
//                    isInit = false;
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//
//        this.mContext = mContext;
//        this.mActivity = mActivity;
//        this.customServices = customServices;
//        this.mGoodsDetailBean = mGoodsDetailBean;
//
//        rootView = View.inflate(mContext, R.layout.popwindow_consult, null);
//        initView();
//        initPop();
//
//        if (Build.VERSION.SDK_INT <= 22) {
//            getContentView().setFocusable(true);
//            getContentView().setFocusableInTouchMode(true);
//            getContentView().requestFocus();
//            getContentView().setOnKeyListener(new View.OnKeyListener() {
//                @Override
//                public boolean onKey(View v, int keyCode, KeyEvent event) {
//                    if (keyCode == KeyEvent.KEYCODE_BACK) {
//                        dismiss();
//                        backgroundAlpha(1);
//                        return true;
//                    }
//                    return false;
//                }
//            });
//        }
//    }
//
//    private void initView() {
//        lv_custom_service = (ListView) rootView.findViewById(R.id.lv_custom_service);
//        tv_custom_service_none = (TextView) rootView.findViewById(R.id.tv_custom_service_none);
//
//        if (customServices.size() == 0) {
//            tv_custom_service_none.setVisibility(View.VISIBLE);
//        } else {
//            lv_custom_service.setVisibility(View.VISIBLE);
//            mAdapter = new CustomServiceAdapter(customServices, mContext, 1, "");
//            lv_custom_service.setAdapter(mAdapter);
//            lv_custom_service.setDivider(null);
//
//            lv_custom_service.setOnItemClickListener(new OnItemClickListener() {
//
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    dismiss();
//                    backgroundAlpha(1);
//                    if (customServices.get(position).getVideoStatus() == 1) {
//                        ShopTool.play(customServices.get(position), mContext, 0);
//                    } else {
//                        if (UserSharedPreference.isLogin()) {
//                            Intent intent = new Intent(ShopConstant.COMMUNICATION_ACTION);
//                            intent.putExtra("id", customServices.get(position).getPersonalInfoBean().getUserId());
//                            intent.putExtra("nick_name", customServices.get(position).getPersonalInfoBean().getUserName());
//                            intent.putExtra("img_url", customServices.get(position).getPersonalInfoBean().getUserPhoto());
//                            intent.putExtra("goods_info", mGoodsDetailBean.getGoodsBasicInfoBean());
//                            mContext.startActivity(intent);
//                        } else {
//                            mContext.startActivity(new Intent(ShopConstant.LOG_IN_ACTION).putExtra("refresh", "refresh"));
//                        }
//                    }
//                }
//            });
//        }
//
//    }
//
//    /**
//     * 初始化popWindow
//     */
//    private void initPop() {
//
//        this.setContentView(rootView);
//        this.setWidth(OSUtil.getScreenWidth());
//        this.setFocusable(true);
//        this.setHeight(OSUtil.getScreenHeight() / 2);
//        this.setBackgroundDrawable(null);
//        this.setAnimationStyle(R.style.belowPupWindowAnimation);
//        if (Build.VERSION.SDK_INT > 22)
//            this.setOnDismissListener(new OnDismissListener() {
//                @Override
//                public void onDismiss() {
//                    backgroundAlpha(1);
//                }
//            });
//        else
//            rootView.setOnTouchListener(new View.OnTouchListener() {
//
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    int height = rootView.findViewById(R.id.ll_pop_consult).getTop();
//                    int y = (int) event.getY();
//                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                        if (y < height) {
//                            dismiss();
//                            backgroundAlpha(1);
//                        }
//                    }
//                    return true;
//                }
//            });
//        this.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
//        backgroundAlpha(0.4f);
//    }
//
//    /**
//     * 设置添加屏幕的背景透明度
//     *
//     * @param bgAlpha
//     */
//    private void backgroundAlpha(float bgAlpha) {
//        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
//        lp.alpha = bgAlpha;
//        mActivity.getWindow().setAttributes(lp);
//    }
}
