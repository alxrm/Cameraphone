package com.rm.cameraphone.components.camera;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.rm.cameraphone.constants.ColorConstants;
import com.rm.cameraphone.util.Animators;

import static com.rm.cameraphone.constants.CaptureWrapperConstants.STATE_OPAQUE;
import static com.rm.cameraphone.util.Interpolators.DECELERATE;

/**
 * Created by alex
 */
public class CaptureWrapper extends RelativeLayout {

    private View mBackgroundView;

    private int mHeight;
    private int mCurrentState;
    private float mInitialTop;

    public CaptureWrapper(Context context) {
        super(context);
        initialize();
    }

    public CaptureWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CaptureWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CaptureWrapper(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        setWillNotDraw(false);
        mCurrentState = STATE_OPAQUE;

        mBackgroundView = new View(getContext());
        mBackgroundView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));

        mBackgroundView.setBackgroundColor(ColorConstants.COLOR_PRIMARY_DARK);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        addView(mBackgroundView, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = getMeasuredHeight();
        mInitialTop = mBackgroundView.getY();
    }

    public void hide() {
        animateGone(false);
    }

    public void show() {
        animateGone(true);
    }

    public void animateToState(int state) {
        if (mCurrentState == state) return;
        mCurrentState = state;

        final boolean toOpaque = state == STATE_OPAQUE;
        final int colorFrom = toOpaque ? ColorConstants.COLOR_OVERLAY : ColorConstants.COLOR_PRIMARY_DARK;
        final int colorTo = toOpaque ? ColorConstants.COLOR_PRIMARY_DARK : ColorConstants.COLOR_OVERLAY;

        Animators.animateColor(colorFrom, colorTo, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mBackgroundView.setBackgroundColor((int) animator.getAnimatedValue());
            }
        }).setDuration(400).start();
    }

    private void animateGone(boolean reverse) {
        mBackgroundView.animate()
                .y(reverse ? mInitialTop : mInitialTop + mHeight)
                .setDuration(300)
                .setInterpolator(DECELERATE)
                .start();
    }
}
