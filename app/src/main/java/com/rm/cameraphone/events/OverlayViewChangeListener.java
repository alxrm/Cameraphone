package com.rm.cameraphone.events;

import android.graphics.RectF;

/**
 * Created by alex
 */
public interface OverlayViewChangeListener {
    void onCropRectUpdated(RectF cropRect);
}
