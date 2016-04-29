package com.rm.cameraphone.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by alex
 */
public class CaptureButton extends View {
    public CaptureButton(Context context) {
        super(context);
    }

    public CaptureButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CaptureButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CaptureButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
