package com.rm.cameraphone.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.rm.cameraphone.constants.ColorConstants;
import com.rm.cameraphone.util.Animators;

import static com.rm.cameraphone.constants.CaptureButtonConstants.RADIUS_CENTER_PHOTO;
import static com.rm.cameraphone.constants.CaptureButtonConstants.RADIUS_CENTER_VIDEO;
import static com.rm.cameraphone.constants.CaptureButtonConstants.RADIUS_RECORD;
import static com.rm.cameraphone.constants.CaptureButtonConstants.RADIUS_RECORD_CENTER_DEFAULT;
import static com.rm.cameraphone.constants.CaptureButtonConstants.RADIUS_RECORD_CENTER_SHOWN;
import static com.rm.cameraphone.constants.CaptureButtonConstants.RADIUS_SHAPE_CORNERS;
import static com.rm.cameraphone.constants.CaptureButtonConstants.SHAPE_BOTTOM_RECORD;
import static com.rm.cameraphone.constants.CaptureButtonConstants.SHAPE_HEIGHT;
import static com.rm.cameraphone.constants.CaptureButtonConstants.SHAPE_LEFT_PHOTO;
import static com.rm.cameraphone.constants.CaptureButtonConstants.SHAPE_LEFT_RECORD;
import static com.rm.cameraphone.constants.CaptureButtonConstants.SHAPE_LEFT_VIDEO;
import static com.rm.cameraphone.constants.CaptureButtonConstants.SHAPE_RIGHT_PHOTO;
import static com.rm.cameraphone.constants.CaptureButtonConstants.SHAPE_RIGHT_RECORD;
import static com.rm.cameraphone.constants.CaptureButtonConstants.SHAPE_RIGHT_VIDEO;
import static com.rm.cameraphone.constants.CaptureButtonConstants.SHAPE_STROKE_WIDTH;
import static com.rm.cameraphone.constants.CaptureButtonConstants.SHAPE_TOP_RECORD;
import static com.rm.cameraphone.constants.CaptureButtonConstants.STATE_PHOTO;
import static com.rm.cameraphone.constants.CaptureButtonConstants.STATE_VIDEO;
import static com.rm.cameraphone.constants.ColorConstants.COLOR_CAPTURE_CENTER_PHOTO_FILL;
import static com.rm.cameraphone.constants.ColorConstants.COLOR_CAPTURE_CENTER_VIDEO_FILL;
import static com.rm.cameraphone.util.Animators.calculateAnimatedValue;
import static com.rm.cameraphone.util.DimenUtil.dp;
import static com.rm.cameraphone.util.Interpolators.DECELERATE;
import static com.rm.cameraphone.util.Interpolators.OVERSHOOT;

/**
 * Created by alex
 */
public class CaptureButton extends View {

    // outfit
    private int mCurrentState = STATE_PHOTO; // by default
    private boolean mIsRecording = false;
    private boolean mIsEnabled = true;

    private Runnable mLongTapTask;
    private EventListener mEventListener;

    // convenience vars
    private float mCenterY;
    private float mCenterX;
    private int mHeight;
    private int mWidth;

    // paints
    private Paint mFillPaint;
    private Paint mStrokePaint;
    private Paint mCenterFillPaint;
    private Paint mCenterStrokePaint;
    private Paint mRecordFillPaint;
    private Paint mRecordCenterPaint;

    // capture button
    private RectF mFillShape;
    private float mCenterRadius;

    // record button
    private float mRecordRadius;
    private RectF mRecordCenterShape;
    private float mRecordCenterRadius;

    public CaptureButton(Context context) {
        super(context);
        initialize();
    }

    public CaptureButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CaptureButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CaptureButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        setWillNotDraw(false);

        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setColor(ColorConstants.COLOR_ACCENT);

        mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(dp(SHAPE_STROKE_WIDTH));
        mStrokePaint.setColor(Color.WHITE);
        
        mCenterFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterFillPaint.setStyle(Paint.Style.FILL);
        mCenterFillPaint.setColor(COLOR_CAPTURE_CENTER_PHOTO_FILL);

        mCenterStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterStrokePaint.setStyle(Paint.Style.STROKE);
        mCenterStrokePaint.setColor(ColorConstants.COLOR_CAPTURE_CENTER_VIDEO_STROKE);

        mRecordFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRecordFillPaint.setStyle(Paint.Style.FILL);
        mRecordFillPaint.setColor(ColorConstants.COLOR_RECORD_FILL);

        mRecordCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRecordCenterPaint.setStyle(Paint.Style.FILL);
        mRecordCenterPaint.setColor(Color.WHITE);

        mCenterRadius = dp(RADIUS_CENTER_PHOTO);
        mRecordCenterRadius = dp(RADIUS_RECORD_CENTER_DEFAULT);
        mRecordRadius = 0;

        mRecordCenterShape = new RectF(
                mCenterX,
                mCenterY,
                mCenterX,
                mCenterY
        );

        mFillShape = new RectF(
                dp(SHAPE_LEFT_PHOTO),
                dp(SHAPE_STROKE_WIDTH),
                dp(SHAPE_RIGHT_PHOTO),
                dp(SHAPE_HEIGHT)
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRoundRect(mFillShape, dp(RADIUS_SHAPE_CORNERS), dp(RADIUS_SHAPE_CORNERS), mFillPaint);
        canvas.drawCircle(mCenterX, mCenterY, mCenterRadius, mCenterFillPaint);
        canvas.drawRoundRect(mFillShape, dp(RADIUS_SHAPE_CORNERS), dp(RADIUS_SHAPE_CORNERS), mStrokePaint);

        if (mCurrentState == STATE_VIDEO) {
            canvas.drawCircle(mCenterX, mCenterY, mCenterRadius, mCenterStrokePaint);
        }

        canvas.drawCircle(mCenterX, mCenterY, mRecordRadius, mRecordFillPaint);
        canvas.drawRoundRect(mRecordCenterShape, mRecordCenterRadius, mRecordCenterRadius, mRecordCenterPaint);
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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsEnabled || mEventListener == null) return super.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                handleTouch();
                return true;
            case MotionEvent.ACTION_UP:
                handleRelease(event);
                return false;
        }

        return super.onTouchEvent(event);
    }

    public void setEventListener(EventListener eventListener) {
        mEventListener = eventListener;
    }

    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
    }

    public void hide() {
        animateGone(false);
    }

    public void show() {
        animateGone(true);
    }

    public void animateToState(int nextState) {
        if (mCurrentState == nextState) return;
        mCurrentState = nextState;

        animateShape(false);
        animateCenter(false);
    }

    public void animateRecord(boolean toHide) {
        if (mIsRecording == !toHide) return;
        mIsRecording = !toHide;

        animateCenter(!toHide);
        animateShape(!toHide);
        animateRecordShape(toHide);
    }

    private void animateRecordShape(final boolean toHide) {
        Animators.animateValue(0, 1, 400, OVERSHOOT,
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float fraction = animation.getAnimatedFraction();

                        mRecordCenterShape.left =
                                calculateAnimatedValue(mCenterX, dp(SHAPE_LEFT_RECORD), fraction, toHide);
                        mRecordCenterShape.right =
                                calculateAnimatedValue(mCenterX, dp(SHAPE_RIGHT_RECORD), fraction, toHide);
                        mRecordCenterShape.top =
                                calculateAnimatedValue(mCenterY, dp(SHAPE_TOP_RECORD), fraction, toHide);
                        mRecordCenterShape.bottom =
                                calculateAnimatedValue(mCenterY, dp(SHAPE_BOTTOM_RECORD), fraction, toHide);

                        // radiuses
                        mRecordRadius =
                                calculateAnimatedValue(0, dp(RADIUS_RECORD), fraction, toHide);
                        mRecordCenterRadius =
                                calculateAnimatedValue(dp(RADIUS_RECORD_CENTER_DEFAULT), dp(RADIUS_RECORD_CENTER_SHOWN), fraction, toHide);

                        invalidate();
                    }
        }).start();
    }

    private void animateCenter(boolean toHide) {
        final boolean toPhoto = mCurrentState == STATE_PHOTO;
        int centerRadiusTo = toPhoto ? dp(RADIUS_CENTER_PHOTO) : dp(RADIUS_CENTER_VIDEO);
        AnimatorSet animatorSet = new AnimatorSet();

        if (toHide) centerRadiusTo = 0;

        ValueAnimator animatorClose = Animators.animateValue(mCenterRadius, 0, DECELERATE,
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mCenterRadius = (float) animation.getAnimatedValue();
                        invalidate();
                    }
                });

        animatorClose.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mCenterFillPaint.setColor(toPhoto ?
                                COLOR_CAPTURE_CENTER_PHOTO_FILL : COLOR_CAPTURE_CENTER_VIDEO_FILL
                );
            }
        });

        ValueAnimator animatorOpen = Animators.animateValue(0, centerRadiusTo, DECELERATE,
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mCenterRadius = (float) animation.getAnimatedValue();
                        invalidate();
                    }
                });

        animatorSet.playSequentially(animatorClose, animatorOpen);
        animatorSet.start();
    }

    private void animateShape(final boolean toHide) {
        boolean toPhoto = mCurrentState == STATE_PHOTO;
        int fillColorTo = toPhoto ? ColorConstants.COLOR_ACCENT : Color.WHITE;

        final int shapeTopMemo = (int) mFillShape.top;
        final int shapeBottomMemo = (int) mFillShape.bottom;
        final int shapeLeftMemo = (int) mFillShape.left;
        final int shapeRightMemo = (int) mFillShape.right;

        final int shapeTop;
        final int shapeBottom;
        final int shapeLeft;
        final int shapeRight;

        if (toHide) {
            shapeLeft = (int) mCenterX;
            shapeRight = (int) mCenterX;
            shapeTop = (int) mCenterY;
            shapeBottom = (int) mCenterY;
        } else {
            shapeLeft = toPhoto ? dp(SHAPE_LEFT_PHOTO) : dp(SHAPE_LEFT_VIDEO);
            shapeRight = toPhoto ? dp(SHAPE_RIGHT_PHOTO) : dp(SHAPE_RIGHT_VIDEO);
            shapeTop = dp(SHAPE_STROKE_WIDTH);
            shapeBottom = dp(SHAPE_HEIGHT);
        }

        AnimatorSet animatorSet = new AnimatorSet();

        ValueAnimator animatorColor = Animators.animateColor(mFillPaint.getColor(), fillColorTo,
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mFillPaint.setColor((int) animation.getAnimatedValue());
                        invalidate();
                    }
                });

        ValueAnimator animatorShape = Animators.animateValue(0, 1, DECELERATE,
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float fraction = animation.getAnimatedFraction();

                        mFillShape.left = calculateAnimatedValue(shapeLeftMemo, shapeLeft, fraction, false);
                        mFillShape.right = calculateAnimatedValue(shapeRightMemo, shapeRight, fraction, false);
                        mFillShape.top = calculateAnimatedValue(shapeTopMemo, shapeTop, fraction, false);
                        mFillShape.bottom = calculateAnimatedValue(shapeBottomMemo, shapeBottom, fraction, false);

                        invalidate();
                    }
        });

        animatorSet.playTogether(animatorColor, animatorShape);
        animatorSet.start();
    }

    private void animateClickedState(final boolean clicked) {
        if (mCurrentState != STATE_PHOTO) return;

        Animators.animateValue(0, 1, DECELERATE, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                float strokeWidth = calculateAnimatedValue(
                        dp(SHAPE_STROKE_WIDTH), dp(20), fraction, !clicked
                );

                mFillShape.top = calculateAnimatedValue(dp(SHAPE_STROKE_WIDTH), dp(SHAPE_STROKE_WIDTH + 5), fraction, !clicked);
                ;
                mFillShape.bottom = calculateAnimatedValue(dp(SHAPE_HEIGHT), dp(SHAPE_HEIGHT - 5), fraction, !clicked);
                mFillShape.left = calculateAnimatedValue(dp(SHAPE_LEFT_PHOTO), dp(SHAPE_LEFT_PHOTO + 5), fraction, !clicked);
                mFillShape.right = calculateAnimatedValue(dp(SHAPE_RIGHT_PHOTO), dp(SHAPE_RIGHT_PHOTO - 5), fraction, !clicked);
                mStrokePaint.setStrokeWidth(strokeWidth);
                invalidate();
            }
        }).start();
    }

    private void animateGone(boolean reverse) {
        animate()
                .scaleX(reverse ? 1 : 0)
                .scaleY(reverse ? 1 : 0)
                .setDuration(200)
                .setInterpolator(DECELERATE)
                .start();
    }

    private void handleTouch() {
        animateClickedState(true);

        mLongTapTask = makeLongClickTask();
        postDelayed(mLongTapTask, 500);
    }

    private void handleRelease(MotionEvent event) {
        final long touchTime = event.getEventTime() - event.getDownTime();
        removeCallbacks(mLongTapTask);

        if (touchTime < 500) {
            handleClick();
        } else {
            mEventListener.onStopRecord();

            animateRecord(true);
        }
    }

    private void handleClick() {
        if (mCurrentState == STATE_PHOTO) {
            mEventListener.onCapture();

            animateClickedState(false);
        } else {
            if (!isRecording()) mEventListener.onStartRecord();
            else mEventListener.onStopRecord();

            animateRecord(isRecording());
        }
    }

    private Runnable makeLongClickTask() {
        return new Runnable() {
            @Override
            public void run() {
                mEventListener.onStartRecord();

                animateClickedState(false);
                animateRecord(false);
            }
        };
    }

    public interface EventListener {
        void onStartRecord();
        void onStopRecord();
        void onCapture();
    }
}
