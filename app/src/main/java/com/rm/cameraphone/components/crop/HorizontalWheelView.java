package com.rm.cameraphone.components.crop;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.rm.cameraphone.events.OnWheelScrollingListener;

import static com.rm.cameraphone.constants.ColorConstants.COLOR_TEXT_ACCENT;
import static com.rm.cameraphone.util.DimenUtils.dp;

/**
 * Created by alex
 */
public class HorizontalWheelView extends View {

    private OnWheelScrollingListener mOnWheelScrollingListener;
    private float mLastScrollPoint;

    private Paint mProgressLinePaint;
    private Paint mProgressMiddleLinePaint;

    private int mProgressLineWidth;
    private int mProgressLineMargin;

    private float mTotalScrollDistance;
    private boolean mScrollStarted;

    private int mHeight;
    private int mWidth;

    private float mCenterX;
    private float mCenterY;

    public HorizontalWheelView(Context context) {
        super(context);
        initialize();
    }

    public HorizontalWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public HorizontalWheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HorizontalWheelView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    public void setOnWheelScrollingListener(OnWheelScrollingListener onWheelScrollingListener) {
        mOnWheelScrollingListener = onWheelScrollingListener;
    }

    private void initialize() {
        mProgressLineWidth = dp(1);
        mProgressLineMargin = dp(7);

        mProgressLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressLinePaint.setStyle(Paint.Style.STROKE);
        mProgressLinePaint.setStrokeWidth(mProgressLineWidth);

        mProgressMiddleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressMiddleLinePaint.setStyle(Paint.Style.STROKE);
        mProgressMiddleLinePaint.setStrokeWidth(dp(3));
        mProgressMiddleLinePaint.setColor(COLOR_TEXT_ACCENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int linesCount = mWidth / (mProgressLineWidth + mProgressLineMargin);
        float deltaX = (mTotalScrollDistance) % (float) (mProgressLineMargin + mProgressLineWidth);

        mProgressLinePaint.setColor(Color.WHITE);

        for (int i = 0; i < linesCount; i++) {
            float scalar = 1;

            if (i < (linesCount / 4)) {
                scalar = (i / (float) (linesCount / 4));
            } else if (i > (linesCount * 3 / 4)) {
                scalar = ((linesCount - i) / (float) (linesCount / 4));
            }

            mProgressLinePaint.setAlpha((int) (0xFF * scalar));

            canvas.drawLine(
                    -deltaX + i * (mProgressLineWidth + mProgressLineMargin),
                    mCenterY - mHeight / 3,
                    -deltaX + i * (mProgressLineWidth + mProgressLineMargin),
                    mCenterY + mHeight / 3, mProgressLinePaint);
        }

        canvas.drawLine(mCenterX, 0, mCenterX, mHeight, mProgressMiddleLinePaint);
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
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastScrollPoint = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                if (mOnWheelScrollingListener != null) {
                    mScrollStarted = false;
                    mOnWheelScrollingListener.onWheelScrollEnd();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float distance = event.getX() - mLastScrollPoint;
                if (distance != 0) {
                    if (!mScrollStarted) {
                        mScrollStarted = true;
                        if (mOnWheelScrollingListener != null) {
                            mOnWheelScrollingListener.onWheelScrollStart();
                        }
                    }
                    onScrollEvent(event, distance);
                }
                break;
        }
        return true;
    }

    private void onScrollEvent(MotionEvent event, float distance) {
        mTotalScrollDistance -= distance;
        postInvalidate();
        mLastScrollPoint = event.getX();
        if (mOnWheelScrollingListener != null) {
            mOnWheelScrollingListener.onWheelScroll(-distance, mTotalScrollDistance);
        }
    }
}