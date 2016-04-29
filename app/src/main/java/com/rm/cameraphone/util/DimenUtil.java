package com.rm.cameraphone.util;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by alex
 */
public class DimenUtil {

    private static DisplayMetrics sMetrics;

    public static void init(Context context) {
        sMetrics = context.getResources().getDisplayMetrics();
    }

    public static float width() {
        return sMetrics.widthPixels;
    }

    public static float height() {
        return sMetrics.heightPixels;
    }

    public static int px(float px) {
        return (int) (px / sMetrics.density);
    }

    public static int dp(float dp) {
        return (int) (dp * sMetrics.density);
    }
}
