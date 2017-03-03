package com.example.gridTest.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

/**
 * Created by tjy on 2015/11/27.
 * 支持手势滑动
 */
public abstract class TouchGroup extends ViewGroup {
    public static final String TAG = TouchGroup.class.getSimpleName();
    public TouchGroup(Context context) {
        this(context, null);
    }

    public TouchGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mMaxVelocity = 4000;
        mScroller = new Scroller(context, new DecelerateInterpolator());
//        reboundLen = ResolutionUtil.dip2px(context, 100);
//        failover = ResolutionUtil.dip2px(context, 1);
    }

    /////////////////////////////

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private int orientation = -1;
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getOrientation() {
        if(orientation == -1){
            throw new UnsupportedOperationException("请在使用前先执行 setOrientation！");
        }
        return orientation;
    }

    protected abstract int getScrollWidth(); // 可以滚动的宽度
    protected abstract int getScrollHeight(); // 可以滚动的高度

    private VelocityTracker mVelocityTracker;
    private int mMaxVelocity;
    private int mPointerId;
    protected Scroller mScroller;
    protected int reboundLen; // 回弹距离
    private int touchDuration = 1600;
//    protected int failover; // 容错范围

    public void setTouchDuration(int touchDuration) {
        this.touchDuration = touchDuration;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        acquireVelocityTracker(event);
        final VelocityTracker verTracker = mVelocityTracker;
        float velocityX = 0, velocityY = 0;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //求第一个触点的id， 此时可能有多个触点，但至少一个
                mPointerId = event.getPointerId(0);
                velocityX = 0;
                velocityY = 0;
                break;

            case MotionEvent.ACTION_MOVE:
                //求伪瞬时速度
                mPointerId = event.getPointerId(0);
                verTracker.computeCurrentVelocity(10, mMaxVelocity);
                velocityX = verTracker.getXVelocity(mPointerId);
                velocityY = verTracker.getYVelocity(mPointerId);
                return recodeInfo(velocityX, velocityY, 0, event.getAction());

            case MotionEvent.ACTION_UP:
                //求伪瞬时速度
                mPointerId = event.getPointerId(0);
                verTracker.computeCurrentVelocity(touchDuration, mMaxVelocity);
                float velocityX1 = verTracker.getXVelocity(mPointerId);
                float velocityY1 = verTracker.getYVelocity(mPointerId);
                if(velocityX1 != velocityX || velocityY1 != velocityY) {
                    recodeInfo(velocityX1, velocityY1, 800, event.getAction());
                    releaseVelocityTracker();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                releaseVelocityTracker();
                return true;

            default:
                break;
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(super.dispatchTouchEvent(ev)) {
            return true;
        }
        Log.i(TAG, this.getClass().getSimpleName() + " dispatchTouchEvent " + ev.getAction());
        return onInterceptTouchEvent(ev);
    }

    /**
     *
     * @param event 向VelocityTracker添加MotionEvent
     *
     * @see VelocityTracker#obtain()
     * @see VelocityTracker#addMovement(MotionEvent)
     */
    private void acquireVelocityTracker(final MotionEvent event) {
        if(null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * 释放VelocityTracker
     *
     * @see VelocityTracker#clear()
     * @see VelocityTracker#recycle()
     */
    private void releaseVelocityTracker() {
        if(null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * 记录当前速度
     *
     * @param velocityX x轴速度
     * @param velocityY y轴速度
     */
    protected boolean recodeInfo(float velocityX, float velocityY, int duration, int action) {
//        if(Math.abs(velocityX) > failover || Math.abs(velocityY) > failover) {
            return move(-(int) velocityX, -(int) velocityY, duration, action);
//        }
//        return true;
    }

    protected boolean move(int xto, int yto, int duration, int action) {
        Log.i(TAG, "move... xto="+xto+", yto="+yto);
        if(orientation == HORIZONTAL) {
            mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(), xto, mScroller.getCurrY(), duration);
            Log.i(TAG, "-----------  "+mScroller.getFinalX()+"   "+reboundLen);
            // x
            if (mScroller.getFinalX() < 0) {
                if(mScroller.getFinalX() < 0 - reboundLen) {
                    mScroller.setFinalX(0 - reboundLen);
                    onLeftBoundary();
                }
                if (action == MotionEvent.ACTION_UP) {
                    Log.i(TAG, "-----------" + 0);
                    mScroller.setFinalX(0);
                }
            }
            Log.i(TAG, "scroll width="+getScrollWidth());
            if (mScroller.getFinalX() > getScrollWidth()) {
                if(mScroller.getFinalX() > getScrollWidth() + reboundLen) {
                    mScroller.setFinalX(getScrollWidth() + reboundLen);
                    onRightBoundary();
                }
                if (action == MotionEvent.ACTION_UP) {
                    mScroller.setFinalX(getScrollWidth());
                }
            }
        }
        else if(orientation == VERTICAL) {
            mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(), mScroller.getCurrX(), yto, duration);
            // y
            if (mScroller.getFinalY() < 0) {
                if (mScroller.getFinalY() < 0 - reboundLen) {
                    mScroller.setFinalY(0 - reboundLen);
                    onTopBoundary();
                }
                if (action == MotionEvent.ACTION_UP) {
                    mScroller.setFinalY(0);
                }
            }
            Log.i(TAG, "scroll height = "+getScrollHeight());
            if (mScroller.getFinalY() > getScrollHeight()) {
                if (mScroller.getFinalY() > getScrollHeight() + reboundLen) {
                    mScroller.setFinalY(getScrollHeight() + reboundLen);
                    onBottomBoundary();
                }
                if (action == MotionEvent.ACTION_UP) {
                    mScroller.setFinalY(getScrollHeight());
                }
            }
        }
        postInvalidate();
        boolean finished = mScroller.getCurrX()==mScroller.getFinalX()&&mScroller.getCurrY()==mScroller.getFinalY();
        return !finished;
    }

    @Override
    public void computeScroll() {
        if(!mScroller.isFinished() || mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    public Scroller getScroller() {
        return mScroller;
    }

    /**
     * 设置回弹距离
     * @param reboundLen
     */
    public void setReboundLen(int reboundLen) {
        this.reboundLen = reboundLen;
    }

    protected void onLeftBoundary(){

    }

    protected void onTopBoundary(){

    }

    protected void onRightBoundary(){

    }

    protected void onBottomBoundary(){

    }
}
