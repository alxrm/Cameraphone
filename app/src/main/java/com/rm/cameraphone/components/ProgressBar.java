package com.rm.cameraphone.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by alex
 */
public class ProgressBar extends View {

    private static final int COLOR_PROGRESS_BG = 0x55000000;

    private int mWidth;
    private int mHeight;

    private Paint mProgressPaint;

    // changing
    private float mProgressWidth;
    private float mMax;

    public ProgressBar(Context context) {
        super(context);
        initialize();
    }

    public ProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        setBackgroundColor(Color.TRANSPARENT);
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.FILL);
        mProgressPaint.setColor(Color.WHITE);

        mMax = 100; // default
        mProgressWidth = 0;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, mProgressWidth, mHeight, mProgressPaint);
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
        invalidate();
    }


    public void setProgress(int progress) {
        final float appliedProgress = Math.min(mMax, progress);

        mProgressWidth = ((appliedProgress / mMax) * mWidth);

        invalidate();
    }

    public void setMax(float max) {
        mMax = max;
        invalidate();
    }
}
