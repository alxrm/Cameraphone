package com.rm.cameraphone.util;

import android.animation.ArgbEvaluator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;

import static com.rm.cameraphone.util.Interpolators.DECELERATE;

/**
 * Created by alex
 */
public class Animators {

    public static ValueAnimator animateColor(int from, int to, @NonNull ValueAnimator.AnimatorUpdateListener listener) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), from, to);
        colorAnimation.setDuration(200);
        colorAnimation.setInterpolator(DECELERATE);
        colorAnimation.addUpdateListener(listener);

        return colorAnimation;
    }

    public static ValueAnimator animateValue(float from, float to,
                                    @NonNull TimeInterpolator interpolator,
                                    @NonNull ValueAnimator.AnimatorUpdateListener listener) {
        return animateValue(from, to, 200, interpolator, listener);
    }

    public static ValueAnimator animateValue(float from, float to, long duration,
                                             @NonNull TimeInterpolator interpolator,
                                             @NonNull ValueAnimator.AnimatorUpdateListener listener) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);
        animator.addUpdateListener(listener);

        return animator;
    }

    public static float calculateAnimatedValue(float start, float end, float fraction, boolean reverse) {
        if (reverse) return end - (end - start) * fraction;
        return start + (end - start) * fraction;
    }
}
