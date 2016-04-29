package com.rm.cameraphone.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by alex
 */
public class CaptureWrapper extends RelativeLayout {
    public CaptureWrapper(Context context) {
        super(context);
    }

    public CaptureWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CaptureWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CaptureWrapper(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
