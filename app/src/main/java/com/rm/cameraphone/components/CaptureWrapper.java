package com.rm.cameraphone.components;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.rm.cameraphone.constants.StyleConstants;
import com.rm.cameraphone.util.DimenUtil;

import static com.rm.cameraphone.util.Interpolators.DECELERATE;
import static com.rm.cameraphone.util.Interpolators.OVERSHOOT;

/**
 * Created by alex
 */
public class CaptureWrapper extends RelativeLayout {

    private static final int STATE_GONE = 2;
    private static final int STATE_OPAQUE = 1;
    private static final int STATE_TRANSPARENT = 0;

    private int mHeight;
    private int mCurrentState;

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
        setBackgroundColor(StyleConstants.COLOR_OVERLAY);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        postDelayed(new Runnable() {
            @Override
            public void run() {
                animateToState(STATE_OPAQUE, false);
            }
        }, 3000);
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
    }

    public void animateToState(int state, boolean reverse) {
        mCurrentState = state;

        if (state == STATE_GONE) {
            animateSlide(reverse);
            return;
        }

        final boolean toOpaque = state == STATE_OPAQUE;
        final int colorFrom = toOpaque ? StyleConstants.COLOR_OVERLAY : StyleConstants.COLOR_PRIMARY_DARK;
        final int colorTo = toOpaque ? StyleConstants.COLOR_PRIMARY_DARK : StyleConstants.COLOR_OVERLAY;

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(250); // milliseconds
        colorAnimation.setInterpolator(DECELERATE.get());
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                setBackgroundColor((int) animator.getAnimatedValue());
            }

        });

        colorAnimation.start();
    }

    private void animateSlide(boolean reverse) {
        animate()
                .translationY(reverse ? DimenUtil.dp(-100) : DimenUtil.dp(100))
                .setDuration(200)
                .setInterpolator(OVERSHOOT.get())
                .start();
    }
}
