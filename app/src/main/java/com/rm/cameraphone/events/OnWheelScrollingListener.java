package com.rm.cameraphone.events;

/**
 * Created by alex
 */
public interface OnWheelScrollingListener {
    void onWheelScrollStart();
    void onWheelScroll(float delta, float totalDistance);
    void onWheelScrollEnd();
}