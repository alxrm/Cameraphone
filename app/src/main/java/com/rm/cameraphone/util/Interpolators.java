package com.rm.cameraphone.util;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by alex
 */
public interface Interpolators {
    DecelerateInterpolator DECELERATE = new DecelerateInterpolator();
    AccelerateInterpolator ACCELERATE = new AccelerateInterpolator();
    AccelerateDecelerateInterpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();
    AnticipateInterpolator ANTICIPATE = new AnticipateInterpolator();
    OvershootInterpolator OVERSHOOT = new OvershootInterpolator(1.1F);
    AnticipateOvershootInterpolator ANTICIPATE_OVERSHOOT = new AnticipateOvershootInterpolator();
}
