package com.rm.cameraphone.util.bitmap;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Created by alex
 */
public class Bitmaps {

    public static Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
        if (bitmap == null || bitmap.isRecycled()) return null;

        Matrix matrix = new Matrix();
        matrix.postRotate(degrees < 0 ? 360 + degrees : degrees);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

        if (newBitmap != bitmap) {
            bitmap.recycle();
        }

        return newBitmap;
    }

    public static Bitmap cropAndRotate(Bitmap bitmap, RectF cropBounds, RectF imgBounds, float scale, float degrees) {
        if (bitmap == null || bitmap.isRecycled()) return null;

        if (degrees != 0)
            return cropBitmap(rotateBitmap(bitmap, degrees), cropBounds, imgBounds, scale);
        else
            return cropBitmap(bitmap, cropBounds, imgBounds, scale);
    }

    public static Bitmap cropBitmap(Bitmap bitmap, RectF cropBounds, RectF imgBounds, float scale) {
        if (bitmap == null || bitmap.isRecycled()) return null;

        final int top = Math.round((cropBounds.top - imgBounds.top) / scale);
        final int left = Math.round((cropBounds.left - imgBounds.left) / scale);
        final int width = Math.round(cropBounds.width() / scale);
        final int height = Math.round(cropBounds.height() / scale);

        Bitmap res = Bitmap.createBitmap(
                bitmap,
                left, top,
                width, height,
                null,
                true
        );

        if (res != bitmap) {
            bitmap.recycle();
        }

        System.gc();
        return res;
    }
}