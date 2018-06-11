package com.tencent.qcloud.suixinbo.avcontrollers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.tencent.av.opengl.gesturedetectors.MoveGestureDetector;
import com.tencent.av.opengl.gesturedetectors.MoveGestureDetector.OnMoveGestureListener;
import com.tencent.av.opengl.ui.GLView;
import com.tencent.av.utils.QLog;
import com.tencent.qcloud.suixinbo.R;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.utils.Constants;
import com.tencent.qcloud.suixinbo.utils.SxbLog;

/**
 * AVSDK UI控制类
 */
public class AVUIControl extends AbstractAVUIControl{
    static final String TAG = "VideoLayerUI";

    int mTopOffset = 0;
    int mBottomOffset = 0;

    int mTargetIndex = -1;
    OnTouchListener mTouchListener = null;
    GestureDetector mGestureDetector = null;
    MoveGestureDetector mMoveDetector = null;
    ScaleGestureDetector mScaleGestureDetector = null;

    private volatile boolean mIsSupportMultiVideo;

    public interface AVControlListener{
        /**
         * 当小窗口位置更新时
         */
        void onLayoutSmallVideoArea(int index, int left, int top, int right, int bottom);
        void onVideoOrderChanged(String[] ids);
    }
    private AVControlListener mListener;
    public void setListener(AVControlListener listener){
        mListener = listener;
    }

    public AVUIControl(Context context, View rootView) {
        super(context, rootView);
        mScaleGestureDetector = new ScaleGestureDetector(mContext, new ScaleGestureListener());
        mGestureDetector = new GestureDetector(mContext, new GestureListener());
        mMoveDetector = new MoveGestureDetector(mContext, new MoveListener());
        mTouchListener = new TouchListener();
        setOnTouchListener(mTouchListener);

        mIsSupportMultiVideo = true;

        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "mIsSupportMultiVideo: " + mIsSupportMultiVideo);
        }
    }


    @Override
    protected void onLayout(boolean flag, int left, int top, int right, int bottom) {
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "onLayout|left: " + left + ", top: " + top + ", right: " + right + ", bottom: " + bottom);
        }
        layoutVideoView(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTouchListener = null;
        mGestureDetector = null;
        mMoveDetector = null;
        mScaleGestureDetector = null;
    }

    /*public void setRemoteHasVideo(String identifier, int videoSrcType, boolean isRemoteHasVideo, boolean forceToBigView, boolean isPC) {
        boolean needForceBig = forceToBigView;
        if (mContext == null)
            return;
        if (Utils.getGLVersion(mContext) == 1) {
            isRemoteHasVideo = false;
            return;
        }
        if (!forceToBigView && !isLocalFront()) {
            forceToBigView = true;
        }

        if (isRemoteHasVideo) {// 打开对方画面
            GLVideoView view = null;
            int index = getViewIndexById(identifier, videoSrcType);
            if (index < 0) {
                index = getIdleViewIndex(0);
                if (index >= 0) {
                    view = mGlVideoView[index];
                    view.setRender(identifier, videoSrcType);
                    remoteViewIndex = index;
                    mRemoteIdentifier = identifier;
                }
            } else {
                view = mGlVideoView[index];
            }
            if (view != null) {
                view.setIsPC(isPC);
                view.setMirror(false);
                view.enableLoading(true);
                view.setVisibility(GLView.VISIBLE);
            }
            if (forceToBigView && index > 0) {
                switchVideo(0, index);
            }
        } else {// 关闭对方画面
            int index = getViewIndexById(identifier, videoSrcType);
            if (index >= 0) {
                closeVideoView(index);
                remoteViewIndex = -1;
            }
        }
    }*/

    public void setOffset(int topOffset, int bottomOffset) {
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "setOffset topOffset: " + topOffset + ", bottomOffset: " + bottomOffset);
        }
        mTopOffset = topOffset;
        mBottomOffset = bottomOffset;
        // refreshUI();
        layoutVideoView(true);
    }

    public void setText(String identifier, int videoSrcType, String text, float textSize, int color) {
        int index = getViewIndexById(identifier, videoSrcType);
        if (index < 0) {
            index = getIdleViewIndex(0);
            if (index >= 0) {
                GLVideoView view = mGlVideoView[index];
                view.setRender(identifier, videoSrcType);
            }
        }
        if (index >= 0) {
            GLVideoView view = mGlVideoView[index];
            view.setVisibility(GLView.VISIBLE);
            view.setText(text, textSize, color);
        }
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "setText identifier: " + identifier + ", videoSrcType: " + videoSrcType + ", text: " + text + ", textSize: " + textSize + ", color: " + color + ", index: " + index);
        }
    }


    /**
     * 小窗口布局
     *
     * @param virtical
     */
    void layoutVideoView(boolean virtical) {
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "layoutVideoView virtical: " + virtical);
        }
        if (mContext == null)
            return;

        int width = getWidth();
        int height = getHeight();

        SxbLog.d(TAG, "width: " + getWidth() + "height: " + getHeight());

        mGlVideoView[0].layout(0, 0, width, height);
        mGlVideoView[0].setBackgroundColor(Color.BLACK);
        //
        int edgeX = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetX);
        int edgeY = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetY);

        final int margintop = mContext.getResources().getDimensionPixelSize(R.dimen.small_area_margin_top);
        final int areaW = mContext.getResources().getDimensionPixelSize(R.dimen.small_area_width);
        final int areaH = mContext.getResources().getDimensionPixelSize(R.dimen.small_area_height);
        final int marginRight = mContext.getResources().getDimensionPixelSize(R.dimen.small_area_marginright);
        final int marginbetween = mContext.getResources().getDimensionPixelSize(R.dimen.small_area_marginbetween);
        //
        int left = 0;
        int right = 0;
        int top = edgeY;
        int bottom = height - edgeY - mBottomOffset;

        if (mIsSupportMultiVideo) {
            if (QLog.isColorLevel()) {
                QLog.d(TAG, QLog.CLR, "SupportMultiVideo");
            }

            //多人画面的位置为了不与下面的关闭免提，打开麦克风等按钮重复，需要重新设计其位置，暂时置于视图中间

            left = width - marginRight - areaW;
            right = width - marginRight;

            if (!virtical) {
                left = mGlVideoView[1].getBounds().left;
                right = mGlVideoView[1].getBounds().right;
            } else {
                top = margintop;
                bottom = margintop + areaH;
            }
            mGlVideoView[1].layout(left, top, right, bottom);
            if(mListener != null){
                mListener.onLayoutSmallVideoArea(1, left, top, right, bottom);
            }
            if (!virtical) {
                left = mGlVideoView[2].getBounds().left;
                right = mGlVideoView[2].getBounds().right;
            } else {
                top = bottom + marginbetween;
                bottom = top + areaH;

            }
            mGlVideoView[2].layout(left, top, right, bottom);
            if(mListener != null){
                mListener.onLayoutSmallVideoArea(2, left, top, right, bottom);
            }
            if (!virtical) {
                left = mGlVideoView[3].getBounds().left;
                right = mGlVideoView[3].getBounds().right;
            } else {
                top = bottom + marginbetween;
                bottom = top + areaH;
            }
            mGlVideoView[3].layout(left, top, right, bottom);
            if(mListener != null){
                mListener.onLayoutSmallVideoArea(3, left, top, right, bottom);
            }
            mGlVideoView[1].setBackgroundColor(Color.WHITE);
            mGlVideoView[2].setBackgroundColor(Color.WHITE);
            mGlVideoView[3].setBackgroundColor(Color.WHITE);
            mGlVideoView[1].setPaddings(2, 3, 3, 3);
            mGlVideoView[2].setPaddings(2, 3, 2, 3);
            mGlVideoView[3].setPaddings(2, 3, 2, 3);
        } else {
            int wRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_width);
            int hRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_height);
            int edgeXRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetX);
            int edgeYRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetY);
            left = edgeXRemote;
            right = left + wRemote;
            top = edgeYRemote + mTopOffset;
            bottom = top + hRemote;

            mGlVideoView[1].layout(left, top, right, bottom);
            if(mListener != null){
                mListener.onLayoutSmallVideoArea(1, left, top, right, bottom);
            }
            mGlVideoView[1].setBackgroundColor(Color.WHITE);
        }

        invalidate();
    }

    protected void closeVideoView(int index) {
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "closeVideoView index: " + index);
        }
        int ajIndex = uppderViews(index);
        if(ajIndex < 0) return;
        closeGLVideoView(ajIndex);
        //请求连接数减一
        CurLiveInfo.getInstance().setCurrentRequestCount(CurLiveInfo.getInstance().getCurrentRequestCount() - 1);
        layoutVideoView(true);
    }


    /**
     * 次序上移算法
     *
     * @param index 本来要删除的位置
     * @return 返回最终删除位置
     */
    private int uppderViews(int index) {
        if (index == 1 && getVisibleViewCount() == 4) {//删除1,count＝4；
            switchVideo(1, 2);
            switchVideo(2, 3);
            return 3;
        }
        if (index == 2 && getVisibleViewCount() == 4) {
            switchVideo(2, 3);
            return 3;
        }
        if (index == 1 && getVisibleViewCount() == 3) {
            switchVideo(1, 2);
            return 2;
        }
        return index;
    }

    @Override
    protected void switchVideo(int index1, int index2) {
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "switchVideo index1: " + index1 + ", index2: " + index2);
        }
        if (index1 == index2 || index1 < 0 || index1 >= mGlVideoView.length || index2 < 0 || index2 >= mGlVideoView.length) {
            return;
        }

        if (GLView.INVISIBLE == mGlVideoView[index1].getVisibility() || GLView.INVISIBLE == mGlVideoView[index2].getVisibility()) {
            SxbLog.d("switchVideo", "can not switchVideo");
            return;
        }

        String identifier1 = mGlVideoView[index1].getIdentifier();
        int videoSrcType1 = mGlVideoView[index1].getVideoSrcType();
        boolean isPC1 = mGlVideoView[index1].isPC();
        boolean isMirror1 = mGlVideoView[index1].isMirror();
        boolean isLoading1 = mGlVideoView[index1].isLoading();
        String identifier2 = mGlVideoView[index2].getIdentifier();
        int videoSrcType2 = mGlVideoView[index2].getVideoSrcType();
        boolean isPC2 = mGlVideoView[index2].isPC();
        boolean isMirror2 = mGlVideoView[index2].isMirror();
        boolean isLoading2 = mGlVideoView[index2].isLoading();

        mGlVideoView[index1].setRender(identifier2, videoSrcType2);
        mGlVideoView[index1].setIsPC(isPC2);
        mGlVideoView[index1].setMirror(isMirror2);
        mGlVideoView[index1].enableLoading(isLoading2);
        mGlVideoView[index2].setRender(identifier1, videoSrcType1);
        mGlVideoView[index2].setIsPC(isPC1);
        mGlVideoView[index2].setMirror(isMirror1);
        mGlVideoView[index2].enableLoading(isLoading1);

        int temp = localViewIndex;
        localViewIndex = remoteViewIndex;
        remoteViewIndex = temp;

        switchMapIndex(index1, index2);
        mListener.onVideoOrderChanged(getIdentifiers());
    }

    class Position {
        final static int CENTER = 0;
        final static int LEFT_TOP = 1;
        final static int RIGHT_TOP = 2;
        final static int RIGHT_BOTTOM = 3;
        final static int LEFT_BOTTOM = 4;
    }

    @Override
    public int setHasRemoteVideo(boolean isRemoteHasVideo, String remoteIdentifier, int videoSrcType) {
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "setHasRemoteVideo position: " + mPosition);
        }
        if (mContext == null) {
            return -1;
        }

        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;
        int width = getWidth();
        int height = getHeight();
        int w = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_width);
        int h = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_height);
        int edgeX = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetX);
        int edgeY = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetY);
        if (mBottomOffset == 0) {
            edgeY = edgeX;
        }

        switch (mPosition) {
            case Position.LEFT_TOP:
                left = edgeX;
                right = left + w;
                // if (mBottomOffset != 0) {
                // top = height - h - edgeY - mBottomOffset;
                // bottom = top + h;
                // } else {
                top = edgeY + mTopOffset;
                bottom = top + h;
                // }
                break;
            case Position.RIGHT_TOP:
                left = width - w - edgeX;
                right = left + w;
                // if (mBottomOffset != 0) {
                // top = height - h - edgeY - mBottomOffset;
                // bottom = top + h;
                // } else {
                top = edgeY + mTopOffset;
                bottom = top + h;
                // }
                break;
            case Position.LEFT_BOTTOM:
                left = edgeX;
                right = left + w;
                top = height - h - edgeY - mBottomOffset;
                bottom = top + h;
                break;
            case Position.RIGHT_BOTTOM:
                left = width - w - edgeX;
                top = height - h - edgeY - mBottomOffset;
                right = left + w;
                bottom = top + h;
                break;
        }


        if (isRemoteHasVideo) {// 远端是否有视频数据
            GLVideoView view = null;
            mRemoteIdentifier = remoteIdentifier;
            int index = getViewIndexById(remoteIdentifier, videoSrcType);

/*            if (!mIsSupportMultiVideo) {
                if (remoteViewIndex != -1) {
                    closeVideoView(remoteViewIndex);
                }
            }*/
            //不存在分配一个空的
            if (index < 0) {
                if (mRemoteIdentifier.equals(CurLiveInfo.getInstance().getHostID())) {
                    index = 0;
                } else {
                    index = getIdleViewIndex(1);//0 大屏保留给主播数据
                }

                if (index >= 0) {
                    view = mGlVideoView[index];
                    view.setRender(remoteIdentifier, videoSrcType);
                    id_view.put(index, mRemoteIdentifier);//存index，对应的ID
                    remoteViewIndex = index;
                }

            } else {//存在用已有的
                view = mGlVideoView[index];
            }

            Log.d(TAG, "setRemoteVideo, and index is " + index);
            if (view != null) {
                view.setIsPC(false);
                view.setMirror(false);
                view.enableLoading(true);
                view.setVisibility(GLView.VISIBLE);
            }
            return index;
        } else {// 关闭摄像头
            int index = getViewIndexById(remoteIdentifier, videoSrcType);
            if (index >= 0) {
                closeVideoView(index);
                remoteViewIndex = -1;
            }
        }
        return -2;
    }

    int mPosition = Position.LEFT_TOP;
    boolean mDragMoving = false;

    public int getPosition() {
        return mPosition;
    }

    void checkAndChangeMargin(int index, int deltaX, int deltaY) {
        if (mContext == null) {
            return;
        }
        int width = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_width);
        int height = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_height);

        Rect outRect = getBounds();
        int minOffsetX = 0;
        int minOffsetY = 0;
        int maxOffsetX = outRect.width() - width;
        int maxOffsetY = outRect.height() - height;

        int left = mGlVideoView[index].getBounds().left + deltaX;
        int top = mGlVideoView[index].getBounds().top + deltaY;
        if (left < minOffsetX) {
            left = minOffsetX;
        } else if (left > maxOffsetX) {
            left = maxOffsetX;
        }
        if (top < minOffsetY) {
            top = minOffsetY;
        } else if (top > maxOffsetY) {
            top = maxOffsetY;
        }
        int right = left + width;
        int bottom = top + height;
        mGlVideoView[mTargetIndex].layout(left, top, right, bottom);
        if(mListener != null){
            mListener.onLayoutSmallVideoArea(mTargetIndex, left, top, right, bottom);
        }
    }

    int getSmallViewPosition() {
        int position = Position.CENTER;
        Rect visableRect = getBounds();
        int screenCenterX = visableRect.centerX();
        int screenCenterY = visableRect.centerY();
        int viewCenterX = mGlVideoView[1].getBounds().centerX();
        int viewCenterY = mGlVideoView[1].getBounds().centerY();
        if (viewCenterX < screenCenterX && viewCenterY < screenCenterY) {
            position = Position.LEFT_TOP;
        } else if (viewCenterX < screenCenterX && viewCenterY > screenCenterY) {
            position = Position.LEFT_BOTTOM;
        } else if (viewCenterX > screenCenterX && viewCenterY < screenCenterY) {
            position = Position.RIGHT_TOP;
        } else if (viewCenterX > screenCenterX && viewCenterY > screenCenterY) {
            position = Position.RIGHT_BOTTOM;
        }

        return position;
    }

    class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(GLView view, MotionEvent event) {
            if (view == mGlVideoView[0]) {
                mTargetIndex = 0;
            } else if (view == mGlVideoView[1]) {
                mTargetIndex = 1;
            } else if (view == mGlVideoView[2]) {
                mTargetIndex = 2;
            } else if (view == mGlVideoView[3]) {
                mTargetIndex = 3;
            } else {
                mTargetIndex = -1;
            }
            if (mGestureDetector != null) {
                mGestureDetector.onTouchEvent(event);
            }

            if(mMoveDetector != null){
                mMoveDetector.onTouchEvent(event);
            }
//            if (mTargetIndex == 1 && mMoveDetector != null) {
//                mMoveDetector.onTouchEvent(event);
//            } else if (mTargetIndex == 0 && mGlVideoView[0].getVideoSrcType() == AVView.VIDEO_SRC_TYPE_SCREEN) {
//                if (mScaleGestureDetector != null) {`
//                    mScaleGestureDetector.onTouchEvent(event);
//                }
//                if (mMoveDetector != null) {
//                    mMoveDetector.onTouchEvent(event);
//                }
//            }
            return true;
        }
    }

    ;

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            if (QLog.isColorLevel())
                QLog.d(TAG, QLog.CLR, "GestureListener-->mTargetIndex=" + mTargetIndex);
            if (mTargetIndex <= 0) {
                // 显示控制层
            } else {

                String selectedId = id_view.get(mTargetIndex);
                mContext.sendBroadcast(new Intent(
                        Constants.ACTION_SWITCH_VIDEO).putExtra(
                        Constants.EXTRA_IDENTIFIER, selectedId));

                switchVideo(0, mTargetIndex); //mTargetIndex 放置主屏

            }
            return true;
        }


//        @Override
//        public boolean onDoubleTap(MotionEvent e) {
//            if (mTargetIndex == 0 && mGlVideoView[0].getVideoSrcType() == AVView.VIDEO_SRC_TYPE_SCREEN) {
//                mClickTimes++;
//                if (mClickTimes % 2 == 1) {
//                    mGlVideoView[0].setScale(GLVideoView.MAX_SCALE + 1, 0, 0, true);
//                } else {
//                    mGlVideoView[0].setScale(GLVideoView.MIN_SCALE, 0, 0, true);
//                }
//                return true;
//            }
//            return super.onDoubleTap(e);
//        }
    }


    private void switchMapIndex(int index1, int index2) {
        String id1 = id_view.get(index1);
        String id2 = id_view.get(index2);
        id_view.put(index1, id2);
        id_view.put(index2, id1);
    }


    public void selectIdViewToBg(int indexview) {
        String identifier = id_view.get(indexview);
        SxbLog.d(TAG, "selectIdViewToBg " + identifier);
        if (identifier == null) return;
        mContext.sendBroadcast(new Intent(
                Constants.ACTION_SWITCH_VIDEO).putExtra(
                Constants.EXTRA_IDENTIFIER, identifier));
    }


    class MoveListener implements OnMoveGestureListener {
        int startX = 0;
        int startY = 0;
        int endX = 0;
        int endY = 0;
        int startPosition = 0;

        @Override
        public boolean onMoveBegin(MoveGestureDetector detector) {
            if (mTargetIndex == 0) {
                // ignore
                return false;
            } else {
                startX = (int) detector.getFocusX();
                startY = (int) detector.getFocusY();
                startPosition = getSmallViewPosition();
            }
            return true;
        }

        @Override
        public boolean onMove(MoveGestureDetector detector) {
            PointF delta = detector.getFocusDelta();
            int deltaX = (int) delta.x;
            int deltaY = (int) delta.y;
            if (mTargetIndex == 0) {
//                mGlVideoView[0].setOffset(deltaX, deltaY, false);
            } else if(mTargetIndex != -1){
                if (Math.abs(deltaX) > Constants.VIDEO_VIEW_MAX || Math.abs(deltaY) > Constants.VIDEO_VIEW_MAX) {
                    mDragMoving = true;
                }
                // 修改拖动窗口的位置
                checkAndChangeMargin(mTargetIndex, deltaX, deltaY);
            }
            return true;
        }

        @Override
        public void onMoveEnd(MoveGestureDetector detector) {
            PointF delta = detector.getFocusDelta();
            int deltaX = (int) delta.x;
            int deltaY = (int) delta.y;
            if (mTargetIndex == 0) {
//                mGlVideoView[0].setOffset(deltaX, deltaY, true);
            } else if(mTargetIndex != -1){
                // 修改拖动窗口的位置
                checkAndChangeMargin(mTargetIndex, deltaX, deltaY);
                endX = (int) detector.getFocusX();
                endY = (int) detector.getFocusY();
                mPosition = getSmallViewDstPosition(startPosition, startX, startY, endX, endY);
                afterDrag(mPosition);
            }
        }
    }

    ;

    class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float x = detector.getFocusX();
            float y = detector.getFocusY();
            float scale = detector.getScaleFactor();
            float curScale = mGlVideoView[0].getScale();
            mGlVideoView[0].setScale(curScale * scale, (int) x, (int) y, false);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            float x = detector.getFocusX();
            float y = detector.getFocusY();
            float scale = detector.getScaleFactor();
            float curScale = mGlVideoView[0].getScale();
            mGlVideoView[0].setScale(curScale * scale, (int) x, (int) y, true);
        }

    }

    enum MoveDistanceLevel {
        e_MoveDistance_Min, e_MoveDistance_Positive, e_MoveDistance_Negative
    }

    ;

    int getSmallViewDstPosition(int startPosition, int nStartX, int nStartY, int nEndX, int nEndY) {
        int thresholdX = mContext.getApplicationContext().getResources().getDimensionPixelSize(R.dimen.video_smallview_move_thresholdX);
        int thresholdY = mContext.getApplicationContext().getResources().getDimensionPixelSize(R.dimen.video_smallview_move_thresholdY);
        int xMoveDistanceLevelStandard = thresholdX;
        int yMoveDistanceLevelStandard = thresholdY;

        MoveDistanceLevel eXMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Min;
        MoveDistanceLevel eYMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Min;

        if (nEndX - nStartX > xMoveDistanceLevelStandard) {
            eXMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Positive;
        } else if (nEndX - nStartX < -xMoveDistanceLevelStandard) {
            eXMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Negative;
        } else {
            eXMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Min;
        }

        if (nEndY - nStartY > yMoveDistanceLevelStandard) {
            eYMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Positive;
        } else if (nEndY - nStartY < -yMoveDistanceLevelStandard) {
            eYMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Negative;
        } else {
            eYMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Min;
        }

        int eBeginPosition = startPosition;
        int eEndPosition = Position.LEFT_TOP;
        int eDstPosition = Position.LEFT_TOP;
        eEndPosition = getSmallViewPosition();

        if (eEndPosition == Position.RIGHT_BOTTOM) {
            if (eBeginPosition == Position.LEFT_TOP) {
                eDstPosition = Position.RIGHT_BOTTOM;
            } else if (eBeginPosition == Position.RIGHT_TOP) {
                eDstPosition = Position.RIGHT_BOTTOM;
            } else if (eBeginPosition == Position.LEFT_BOTTOM) {
                eDstPosition = Position.RIGHT_BOTTOM;
            } else if (eBeginPosition == Position.RIGHT_BOTTOM) {
                if (eXMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
                        eDstPosition = Position.LEFT_TOP;
                    } else {
                        eDstPosition = Position.LEFT_BOTTOM;
                    }
                } else {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
                        eDstPosition = Position.RIGHT_TOP;
                    } else {
                        eDstPosition = Position.RIGHT_BOTTOM;
                    }
                }
            }
        } else if (eEndPosition == Position.RIGHT_TOP) {
            if (eBeginPosition == Position.LEFT_TOP) {
                eDstPosition = Position.RIGHT_TOP;
            } else if (eBeginPosition == Position.RIGHT_BOTTOM) {
                eDstPosition = Position.RIGHT_TOP;
            } else if (eBeginPosition == Position.LEFT_BOTTOM) {
                eDstPosition = Position.RIGHT_TOP;
            } else if (eBeginPosition == Position.RIGHT_TOP) {
                if (eXMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
                        eDstPosition = Position.LEFT_BOTTOM;
                    } else {
                        eDstPosition = Position.LEFT_TOP;
                    }
                } else {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
                        eDstPosition = Position.RIGHT_BOTTOM;
                    } else {
                        eDstPosition = Position.RIGHT_TOP;
                    }
                }
            }
        } else if (eEndPosition == Position.LEFT_TOP) {
            if (eBeginPosition == Position.RIGHT_TOP) {
                eDstPosition = Position.LEFT_TOP;
            } else if (eBeginPosition == Position.RIGHT_BOTTOM) {
                eDstPosition = Position.LEFT_TOP;
            } else if (eBeginPosition == Position.LEFT_BOTTOM) {
                eDstPosition = Position.LEFT_TOP;
            } else if (eBeginPosition == Position.LEFT_TOP) {
                if (eXMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
                        eDstPosition = Position.RIGHT_BOTTOM;
                    } else {
                        eDstPosition = Position.RIGHT_TOP;
                    }
                } else {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
                        eDstPosition = Position.LEFT_BOTTOM;
                    } else {
                        eDstPosition = Position.LEFT_TOP;
                    }
                }
            }
        } else if (eEndPosition == Position.LEFT_BOTTOM) {
            if (eBeginPosition == Position.LEFT_TOP) {
                eDstPosition = Position.LEFT_BOTTOM;
            } else if (eBeginPosition == Position.RIGHT_TOP) {
                eDstPosition = Position.LEFT_BOTTOM;
            } else if (eBeginPosition == Position.RIGHT_BOTTOM) {
                eDstPosition = Position.LEFT_BOTTOM;
            } else if (eBeginPosition == Position.LEFT_BOTTOM) {
                if (eXMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
                        eDstPosition = Position.RIGHT_TOP;
                    } else {
                        eDstPosition = Position.RIGHT_BOTTOM;
                    }
                } else {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
                        eDstPosition = Position.LEFT_TOP;
                    } else {
                        eDstPosition = Position.LEFT_BOTTOM;
                    }
                }
            }
        }
        return eDstPosition;
    }

    void afterDrag(int position) {
        int width = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_width);
        int height = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_height);
        int edgeX = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetX);
        int edgeY = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetY);
        if (mBottomOffset == 0) {
            edgeY = edgeX;
        }
        Rect visableRect = getBounds();

        int fromX = mGlVideoView[1].getBounds().left;
        int fromY = mGlVideoView[1].getBounds().top;
        int toX = 0;
        int toY = 0;

        switch (position) {
            case Position.LEFT_TOP:
                toX = edgeX;
                toY = edgeY;
                break;
            case Position.RIGHT_TOP:
                toX = visableRect.width() - edgeX - width;
                toY = edgeY;
                break;
            case Position.RIGHT_BOTTOM:
                toX = visableRect.width() - edgeX - width;
                toY = visableRect.height() - edgeY - height;
                break;
            case Position.LEFT_BOTTOM:
                toX = edgeX;
                toY = visableRect.height() - edgeY - height;
                break;
            default:
                break;
        }
    }
}
