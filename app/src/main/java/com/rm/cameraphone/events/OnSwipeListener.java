package com.rm.cameraphone.events;

/**
 * Created by alex
 */
public interface OnSwipeListener {
    void onSwipeLeft(float fraction);
    void onSwipeRight(float fraction);
    void onSwipeStopped(float fraction, boolean toLeft);
}
