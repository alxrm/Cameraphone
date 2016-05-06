package com.rm.cameraphone.components.crop;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by alex
 */
public class CropOverlay extends View {

    public CropOverlay(Context context) {
        super(context);
    }

    public CropOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CropOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CropOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
