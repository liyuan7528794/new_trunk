package com.travel.communication.view.sortlistview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * 右侧a-z列
 */
public class SideBar extends View{
    private static final String TAG = "SlideBar";

    public static final char[] UPPERCASE = new char[]{
            'A', 'B', 'C', 'D', 'E', 'F', 'G',
            'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', '#'
    };

    private boolean[] isEnable = new boolean[UPPERCASE.length];
    public interface SideBarListener{
        /**
         * sidebar listener
         * @param view which sidebar was touched
         * @param choosedPosition    the touched position
         * @param action             action
         */
        void onItemTouched(SideBar view, int choosedPosition, int action);
    }
    private SideBarListener mListener;
    public void setListener(SideBarListener listener) {
        this.mListener = listener;
    }

    // 不适用RectF的原因是节省内存
    private final float[] mLetterPositionX = new float[UPPERCASE.length];
    private final float[] mLetterPositionY = new float[UPPERCASE.length];
    // 当前选中的字母
    private int mChoosePosition = -1;
    private float mSingleHeight;
    private Paint mNormalTextPaint = new Paint();
    private Paint mChooseTextPaint = new Paint();
    private Paint mChooseBGPaint = new Paint();

    // 点击后的背景颜色， 与 正常情况的背景颜色
    private Drawable mNormalBGColor, mTouchBGColor;

    public SideBar(Context context) {
        this(context, null);
    }

    public SideBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SideBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // 初始化
    private void init() {
        initPaint();
        mNormalBGColor = getBackground();
        mTouchBGColor = mNormalBGColor;
    }
    // 初始化画笔
    private void initPaint() {
        mNormalTextPaint.setColor(Color.parseColor("#4c4c4c"));
        mNormalTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mNormalTextPaint.setAntiAlias(true);
        mNormalTextPaint.setTextSize(30);

        mChooseTextPaint.setColor(Color.BLUE);
        mChooseTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mChooseTextPaint.setAntiAlias(true);
        mChooseTextPaint.setTextSize(30);

        mChooseBGPaint.setColor(Color.parseColor("#EEEEEE"));
        mChooseBGPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 在这里进行一次计算一次绘制位置
        mSingleHeight =
                (getMeasuredHeight() - getPaddingBottom() - getPaddingTop())/UPPERCASE.length;
        for(int i = 0; i < UPPERCASE.length; i++){
            mLetterPositionX[i]
                    = (getMeasuredWidth() - mNormalTextPaint.measureText(String.valueOf(UPPERCASE[i])))/2.0f;
            mLetterPositionY[i] = (i + 1) * mSingleHeight;
        }
        Log.v(TAG,"onLayout, and measure LetterPosition once");
    }

    /**
     * 获取当前选择位置
     */
    public int getChoosePosition() {
        return mChoosePosition;
    }

    /**
     * 设置相应位置对内内容是否存在
     * 例如L, 如果列表中没有L开头的, 则设为false
     * @param position 对应的位置
     * @param enable   true -- 有数据
     */
    public void setPositionEnable(int position, boolean enable){
        isEnable[position] = enable;
    }

    /**
     * position处是否有效
     * @param position
     * @return
     */
    public boolean isEnableOfPosition(int position){
        if(position < 0 || position >= UPPERCASE.length){
            return false;
        }
        return isEnable[position];
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                setBackgroundDrawable(mTouchBGColor);
                break;
            case MotionEvent.ACTION_UP:
                setBackgroundDrawable(mNormalBGColor);
                break;
        }
        // 判断位置
        int position = (int) (event.getY()/mSingleHeight);
        if(position < 0)
            position = 0;
        position = position >= UPPERCASE.length ? UPPERCASE.length-1 : position;
        if(position != mChoosePosition){
            if(mListener != null){
                mListener.onItemTouched(this, position, action);
            }
            mChoosePosition = position;
        }
        invalidate();
        return true;
    }

    /**
     * 设置当前选中位置
     * @param choosePosition
     */
    public void setChoosePosition(int choosePosition) {
        this.mChoosePosition = choosePosition;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(int i = 0; i < UPPERCASE.length; i++){
            Paint paint = null;
            if(i == mChoosePosition){
                paint = mChooseTextPaint;
                canvas.drawCircle(
                        getWidth() / 2,
                        mLetterPositionY[i] - mChooseTextPaint.getTextSize() / 2f,
                        mSingleHeight / 2f,
                        mChooseBGPaint);
            }else{
                paint = mNormalTextPaint;
            }
            canvas.drawText(
                    String.valueOf(UPPERCASE[i]),
                    mLetterPositionX[i],
                    mLetterPositionY[i],
                    paint);
        }
    }
}
