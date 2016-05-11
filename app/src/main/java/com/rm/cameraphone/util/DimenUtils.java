package com.rm.cameraphone.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Created by alex
 */
public class DimenUtils {

    public static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private static DisplayMetrics sMetrics;
    private static WindowManager sWindowManager;

    public static void init(Context context) {
        sMetrics = context.getResources().getDisplayMetrics();
        sWindowManager = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
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

    public static int getScreenOrientation() {
        return sWindowManager.getDefaultDisplay().getRotation();
    }
}
