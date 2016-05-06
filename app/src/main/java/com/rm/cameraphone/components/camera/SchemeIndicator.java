package com.rm.cameraphone.components.camera;

import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.rm.cameraphone.constants.ColorConstants;
import com.rm.cameraphone.util.Animators;

import static com.rm.cameraphone.constants.SchemeIndicatorConstants.RADIUS_CIRCLE_INDICATOR;
import static com.rm.cameraphone.constants.SchemeIndicatorConstants.SHAPE_STRETCHER_BOTTOM;
import static com.rm.cameraphone.constants.SchemeIndicatorConstants.SHAPE_STRETCHER_TOP;
import static com.rm.cameraphone.util.Animators.calculateAnimatedValue;
import static com.rm.cameraphone.util.DimenUtils.dp;
import static com.rm.cameraphone.util.Interpolators.DECELERATE;

/**
 * Created by alex
 */
public class SchemeIndicator extends View {

    // general data
    private int mHeight;
    private int mWidth;

    private float mCenterX;
    private float mCenterY;

    private int mLeftCenterX;
    private int mRightCenterX;

    // painting
    private Paint mEmptyPaint;
    private Paint mStretcherPaint;
    private Paint mIndicatorPaint;

    // animations
    private AnimatorListenerAdapter mIndicatorListener;
    private RectF mStretcherShape;
    private float mStretcherWidth;
    private float mIndicatorCenterX;
    private float mRightCircleRadius;
    private float mLeftCircleRadius;
    private boolean mIsEnabled;

    public SchemeIndicator(Context context) {
        super(context);
        initialize();
    }

    public SchemeIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public SchemeIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SchemeIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        setWillNotDraw(false);
        mIsEnabled = true;

        mEmptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEmptyPaint.setStyle(Paint.Style.FILL);
        mEmptyPaint.setColor(ColorConstants.COLOR_INDICATOR_EMPTY);

        mStretcherPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStretcherPaint.setStyle(Paint.Style.FILL);
        mStretcherPaint.setColor(ColorConstants.COLOR_INDICATOR_EMPTY);

        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setStyle(Paint.Style.FILL);
        mIndicatorPaint.setColor(ColorConstants.COLOR_INDICATOR_FILLED);

        mIndicatorCenterX = dp(RADIUS_CIRCLE_INDICATOR);

        mRightCircleRadius = dp(RADIUS_CIRCLE_INDICATOR);
        mLeftCircleRadius = 0;

        mStretcherWidth = dp(RADIUS_CIRCLE_INDICATOR) * 2;
        mStretcherShape = new RectF(
                0,
                dp(SHAPE_STRETCHER_TOP),
                mStretcherWidth,
                dp(SHAPE_STRETCHER_BOTTOM)
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(mLeftCenterX, mCenterY, mLeftCircleRadius, mEmptyPaint);
        canvas.drawCircle(mRightCenterX, mCenterY, mRightCircleRadius, mEmptyPaint);

        canvas.drawRoundRect(mStretcherShape, dp(RADIUS_CIRCLE_INDICATOR), dp(RADIUS_CIRCLE_INDICATOR), mStretcherPaint);
        canvas.drawCircle(mIndicatorCenterX, mCenterY, dp(RADIUS_CIRCLE_INDICATOR), mIndicatorPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        mWidth = w;

        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;

        mLeftCenterX = dp(RADIUS_CIRCLE_INDICATOR);
        mRightCenterX = mWidth - dp(RADIUS_CIRCLE_INDICATOR);
    }

    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
    }

    public void hide() {
        animateGone(true);
    }

    public void show() {
        animateGone(false);
    }

    /**
     * sets the animation chunk
     * @param fraction 0 to 1 value for the transition
     */
    public void setFraction(float fraction, boolean reverse) {
        if (!mIsEnabled) return;

        mIndicatorCenterX = calculateAnimatedValue(mLeftCenterX, mRightCenterX, fraction, reverse);

        if (reverse) {
            mStretcherShape.left = calculateAnimatedValue(mWidth - mStretcherWidth, 0, fraction, false);
        } else {
            mStretcherShape.right = calculateAnimatedValue(mStretcherWidth, mWidth, fraction, false);
        }

        invalidate();
    }

    public void animateToState(final boolean swipingToLeft, float fraction) {
        final float shapeRightFrom = mStretcherShape.right;
        final float shapeLeftFrom = mStretcherShape.left;

        final float shapeRightTranslationTo = mWidth;
        final float shapeRightCollapseTo = mStretcherWidth;
        final float shapeLeftTranslationTo = 0;
        final float shapeLeftCollapseTo = mWidth - mStretcherWidth;

        final float indicatorCenterFrom = mIndicatorCenterX;
        final float indicatorCenterTo = swipingToLeft ? mRightCenterX : mLeftCenterX;

        final float leftRadiusFrom = mLeftCircleRadius;
        final float rightRadiusFrom = mRightCircleRadius;

        final float leftRadiusTo = swipingToLeft ? dp(RADIUS_CIRCLE_INDICATOR) : 0;
        final float rightRadiusTo = !swipingToLeft ? dp(RADIUS_CIRCLE_INDICATOR) : 0;

        ValueAnimator translationAnimator = Animators.animateValue(200, DECELERATE, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float fraction = animation.getAnimatedFraction();
                mIndicatorCenterX = calculateAnimatedValue(indicatorCenterFrom, indicatorCenterTo, fraction, false);

                if (swipingToLeft) {
                    mStretcherShape.right = calculateAnimatedValue(shapeRightFrom, shapeRightTranslationTo, fraction, false);
                } else {
                    mStretcherShape.left = calculateAnimatedValue(shapeLeftFrom, shapeLeftTranslationTo, fraction, false);
                }

                invalidate();
            }
        });

        ValueAnimator collapseAnimator = Animators.animateValue(200, DECELERATE, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float fraction = animation.getAnimatedFraction();

                if (swipingToLeft) {
                    mStretcherShape.left = calculateAnimatedValue(shapeLeftFrom, shapeLeftCollapseTo, fraction, false);
                } else {
                    mStretcherShape.right = calculateAnimatedValue(shapeRightFrom, shapeRightCollapseTo, fraction, false);
                }

                invalidate();
            }
        });

        ValueAnimator radiusAnimator = Animators.animateValue(200, DECELERATE, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float fraction = animation.getAnimatedFraction();

                mLeftCircleRadius = calculateAnimatedValue(leftRadiusFrom, leftRadiusTo, fraction, false);
                mRightCircleRadius = calculateAnimatedValue(rightRadiusFrom, rightRadiusTo, fraction, false);
                invalidate();
            }
        });

        AnimatorSet translationSet = new AnimatorSet();
        if (fraction < 0.5) translationSet.playTogether(translationAnimator, collapseAnimator);
        else translationSet.playSequentially(translationAnimator, collapseAnimator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(translationSet, radiusAnimator);
        animatorSet.addListener(mIndicatorListener);
        animatorSet.start();
    }

    public void animateGone(boolean toHide) {
        setEnabled(!toHide);
        animate()
                .alpha(toHide ? 0 : 1)
                .setDuration(200)
                .setInterpolator(DECELERATE)
                .start();
    }

    public void setIndicatorListener(AnimatorListenerAdapter indicatorListener) {
        mIndicatorListener = indicatorListener;
    }
}
