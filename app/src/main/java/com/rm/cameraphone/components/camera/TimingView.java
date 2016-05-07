package com.rm.cameraphone.components.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.rm.cameraphone.util.TimeUtils;

import java.util.Arrays;

import static com.rm.cameraphone.constants.ColorConstants.COLOR_VIDEO_TIMING_FILL;
import static com.rm.cameraphone.util.DimenUtils.dp;
import static com.rm.cameraphone.util.Interpolators.DECELERATE;

/**
 * Created by alex
 */
public class TimingView extends TextView {

    private ShapeDrawable mBackgroundDrawable;
    private Runnable mTimerTask;

    private int mHeight;
    private int mWidth;

    private long mInnerTimeMillis = 0;

    public TimingView(Context context) {
        super(context);
        initialize();
    }

    public TimingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public TimingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TimingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        final float cornerRadius = dp(6);
        final float[] cornerRadii = new float[8];
        final RectF shapeBounds = new RectF(0, 0, dp(58), dp(26));

        Arrays.fill(cornerRadii, 0, cornerRadii.length, cornerRadius);

        mBackgroundDrawable = new ShapeDrawable(new RoundRectShape(
                cornerRadii, shapeBounds, cornerRadii
        ));
        mBackgroundDrawable.getPaint().setColor(COLOR_VIDEO_TIMING_FILL);
        setBackground(mBackgroundDrawable);

        setTextColor(Color.WHITE);
        setGravity(Gravity.CENTER);
        updateTimerNumbers();

        mTimerTask = registerTimerTask();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    private Runnable registerTimerTask() {
        return new Runnable() {
            @Override
            public void run() {
                mInnerTimeMillis += 1000;

                updateTimerNumbers();
                postDelayed(mTimerTask, 1000);
            }
        };
    }

    public void start() {
        animateShow(true);
        postDelayed(mTimerTask, 1000);
    }

    public void stop() {
        animateShow(false);
        discardTimer();
    }

    private void discardTimer() {
        mInnerTimeMillis = 0;
        updateTimerNumbers();
        removeCallbacks(mTimerTask);
    }

    private void updateTimerNumbers() {
        setText(TimeUtils.formatTime(mInnerTimeMillis));
    }

    private void animateShow(boolean show) {
        animate()
                .alpha(show ? 1 : 0)
                .translationY(show ? dp(30) : 0)
                .setDuration(200)
                .setInterpolator(DECELERATE)
                .start();
    }
}
