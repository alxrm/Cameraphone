package com.rm.cameraphone.components;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.rm.cameraphone.R;
import com.rm.cameraphone.util.Animators;

import static com.rm.cameraphone.constants.CameraSwitcherConstants.INDICATOR_FILLED_RADIUS;
import static com.rm.cameraphone.constants.CameraSwitcherConstants.INDICATOR_FILLED_STROKE_WIDTH;
import static com.rm.cameraphone.constants.CameraSwitcherConstants.INDICATOR_INITIAL_RADIUS;
import static com.rm.cameraphone.constants.CameraSwitcherConstants.INDICATOR_INITIAL_STROKE_WIDTH;
import static com.rm.cameraphone.util.Animators.calculateAnimatedValue;
import static com.rm.cameraphone.util.DimenUtils.dp;
import static com.rm.cameraphone.util.Interpolators.DECELERATE;

/**
 * Created by alex
 */
public class CameraSwitcher extends FrameLayout {

    private ImageView mRotatableIcon;
    private FrameLayout.LayoutParams mRotatableIconParams;

    private Paint mIndicatorPaint;
    private float mIndicatorRadius;
    private float mIndicatorStrokeWidth;

    private int mWidth;
    private int mHeight;

    private int mCenterX;
    private int mCenterY;

    private boolean mIsFilled;

    public CameraSwitcher(Context context) {
        super(context);
        initialize();
    }

    public CameraSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CameraSwitcher(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CameraSwitcher(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        setWillNotDraw(false);

        mRotatableIcon = new ImageView(getContext());
        mRotatableIcon.setImageResource(R.drawable.camera_switch);
        mRotatableIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mRotatableIconParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        mRotatableIconParams.gravity = Gravity.CENTER;

        mRotatableIcon.setLayoutParams(mRotatableIconParams);
        mIndicatorStrokeWidth = dp(INDICATOR_INITIAL_STROKE_WIDTH);
        mIndicatorRadius = dp(INDICATOR_INITIAL_RADIUS);

        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setStyle(Paint.Style.STROKE);
        mIndicatorPaint.setColor(Color.WHITE);
        mIndicatorPaint.setStrokeWidth(mIndicatorStrokeWidth);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        addView(mRotatableIcon);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(mCenterX, mCenterY, mIndicatorRadius, mIndicatorPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;
    }

    public void toggle() {
        mRotatableIcon.animate().rotationBy(-180).setDuration(400).setInterpolator(DECELERATE).start();

        animateIndicator();
    }

    private void animateIndicator() {
        mIsFilled = !mIsFilled;

        Animators.animateValue(400, DECELERATE, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float fraction = animation.getAnimatedFraction();

                mIndicatorRadius = calculateAnimatedValue(
                        dp(INDICATOR_INITIAL_RADIUS), dp(INDICATOR_FILLED_RADIUS), fraction, !mIsFilled
                );

                mIndicatorStrokeWidth = calculateAnimatedValue(
                        dp(INDICATOR_INITIAL_STROKE_WIDTH), dp(INDICATOR_FILLED_STROKE_WIDTH), fraction, !mIsFilled
                );

                mIndicatorPaint.setStrokeWidth(mIndicatorStrokeWidth);

                invalidate();
            }
        }).start();
    }
}
