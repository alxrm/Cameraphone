package com.rm.cameraphone.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by alex
 */
public class Bitmaps {

    public static Bitmap rotateBitmap(Bitmap bitmap, int angle) {

        Matrix rotateRight = new Matrix();
        rotateRight.postRotate(angle);

        return Bitmap.createBitmap(
                bitmap, // source
                0, // x
                0, // y
                bitmap.getWidth(), // width
                bitmap.getHeight(), // height
                rotateRight, // transform matrix
                true // filter
        );
    }
}
