package com.rm.cameraphone.components;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.rm.cameraphone.events.OnSwipeListener;

import static com.rm.cameraphone.util.DimenUtils.dp;

/**
 * Created by alex
 */
public class SwipingFrameLayout extends FrameLayout {

    private OnSwipeListener mOnSwipeListener;
    private OnClickListener mOnClickListener;

    private float mInitialX = Float.NaN;
    private boolean mIsEnabled;

    private int mWidth;
    private int mHeight;

    private int mCenterX;
    private int mCenterY;

    private RectF mClickableArea;

    public SwipingFrameLayout(Context context) {
        super(context);
    }

    public SwipingFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipingFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SwipingFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIsEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsEnabled) return false;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mInitialX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                onMove(event.getX(), true);
                break;
            case MotionEvent.ACTION_UP:
                if (mInitialX == event.getX()) {
                    if (mClickableArea.contains(event.getX(), event.getY())) onClick();
                } else {
                    onMove(event.getX(), false);
                    mInitialX = Float.NaN;
                }
                break;
        }

        return true;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            mWidth = right - left + 1;
            mHeight = bottom - top + 1;

            mCenterY = mHeight / 2;
            mCenterX = mWidth / 2;

            mClickableArea = new RectF(
                    mCenterX - dp(60),
                    mCenterY - dp(60),
                    mCenterX + dp(60),
                    mCenterY + dp(60)
            );

        }
    }

    private void onClick() {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this);
        }
    }

    private void onMove(float currentX, boolean isDown) {
        if (mOnSwipeListener == null) return;

        final float way = currentX - mInitialX;
        final boolean toLeft = way < 0;

        float wayFraction = Math.abs(way) / (mWidth / 2);
        wayFraction = wayFraction > 1 ? 1 : wayFraction;

        if (!isDown) {
            mOnSwipeListener.onSwipeStopped(wayFraction, toLeft);
            return;
        }

        if (toLeft) mOnSwipeListener.onSwipeLeft(wayFraction);
        else mOnSwipeListener.onSwipeRight(wayFraction);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
    }

    public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
        mOnSwipeListener = onSwipeListener;
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }
}
