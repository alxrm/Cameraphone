package com.rm.cameraphone.constants;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by alex
 */
public enum Interpolators {
    DECELERATE,
    ACCELERATE,
    ACCELERATE_DECELERATE,
    ANTICIPATE,
    OVERSHOOT,
    ANTICIPATE_OVERSHOOT;

    public Interpolator get() {
        switch (this) {
            case DECELERATE: return new DecelerateInterpolator();
            case ACCELERATE: return new AccelerateInterpolator();
            case ACCELERATE_DECELERATE: return new AccelerateDecelerateInterpolator();
            case ANTICIPATE: return new AnticipateInterpolator();
            case OVERSHOOT: return new OvershootInterpolator();
            case ANTICIPATE_OVERSHOOT: return new AnticipateOvershootInterpolator();
        }

        return new OvershootInterpolator();
    }
}
