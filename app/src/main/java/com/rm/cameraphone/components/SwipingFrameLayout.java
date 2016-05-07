package com.rm.cameraphone.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.rm.cameraphone.events.OnSwipeListener;

import static com.rm.cameraphone.util.DimenUtils.width;

/**
 * Created by alex
 */
public class SwipingFrameLayout extends FrameLayout {

    private OnSwipeListener mOnSwipeListener;
    private float mInitialX = Float.NaN;
    private float mScreenWidth;
    private boolean mIsEnabled;

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
        mScreenWidth = width();
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
                onMove(event.getX(), false);
                mInitialX = Float.NaN;
                break;
        }

        return true;
    }

    private void onMove(float currentX, boolean isDown) {
        if (mOnSwipeListener == null) return;

        final float way = currentX - mInitialX;
        final boolean toLeft = way < 0;

        float wayFraction = Math.abs(way) / (mScreenWidth / 2);
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
}
