package com.rm.cameraphone.events;

import android.view.MotionEvent;
import android.view.View;

import com.rm.cameraphone.util.DimenUtils;

/**
 * Created by alex
 */
public abstract class OnSwipeListener implements View.OnTouchListener {

    private static final float SWIPE_THRESHOLD = DimenUtils.dp(40);
    private float mInitialX = Float.NaN;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
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
        final float way = currentX - mInitialX;
        final float range = Math.abs(way);
        final boolean toLeft = way < 0;

        if (!isDown) {
            onSwipeStopped(range, toLeft);
            return;
        }

        if (range > SWIPE_THRESHOLD) {
            if (toLeft) onSwipeLeft(range);
            else onSwipeRight(range);
        }
    }

    protected abstract void onSwipeLeft(float distance);
    protected abstract void onSwipeRight(float distance);
    protected abstract void onSwipeStopped(float distance, boolean toLeft);
}
