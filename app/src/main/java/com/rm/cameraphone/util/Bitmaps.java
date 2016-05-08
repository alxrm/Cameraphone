package com.rm.cameraphone.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by alex
 */
public class Bitmaps {

    public static Bitmap rotateBitmap(Bitmap bitmap, int angle) {

//        Matrix rotateRight = new Matrix();
//        rotateRight.preRotate(90);
//
//        float[] mirrorY = { -1, 0, 0, 0, 1, 0, 0, 0, 1};
//        rotateRight = new Matrix();
//        Matrix matrixMirrorY = new Matrix();
//        matrixMirrorY.setValues(mirrorY);
//
//        rotateRight.postConcat(matrixMirrorY);
//
//        rotateRight.preRotate(angle);
//
//        return Bitmap.createBitmap(
//                bitmap, // source
//                0, // x
//                0, // y
//                bitmap.getWidth(), // width
//                bitmap.getHeight(), // height
//                rotateRight, // transform matrix
//                true // filter
//        );

        Matrix matrix = new Matrix();

        matrix.postRotate(angle);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
