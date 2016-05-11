package com.rm.cameraphone.util.bitmap;

import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by alex
 */
public class RectUtils {

    /**
     * Gets a float array of the 2D coordinates representing a rectangles
     * corners.
     * The order of the corners in the float array is:
     * 0------->1
     * ^        |
     * |        |
     * |        v
     * 3<-------2
     *
     * @param r the rectangle to get the corners of
     * @return the float array of corners (8 floats)
     */
    public static float[] getCornersFromRect(RectF r) {
        return new float[]{
                r.left, r.top,
                r.right, r.top,
                r.right, r.bottom,
                r.left, r.bottom
        };
    }

    /**
     * Gets a float array of two lengths representing a rectangles width and height
     * The order of the corners in the input float array is:
     * 0------->1
     * ^        |
     * |        |
     * |        v
     * 3<-------2
     *
     * @param corners the float array of corners (8 floats)
     * @return the float array of width and height (2 floats)
     */
    public static float[] getRectSidesFromCorners(float[] corners) {
        return new float[]{(float) Math.sqrt(Math.pow(corners[0] - corners[2], 2) + Math.pow(corners[1] - corners[3], 2)),
                (float) Math.sqrt(Math.pow(corners[2] - corners[4], 2) + Math.pow(corners[3] - corners[5], 2))};
    }

    public static float[] getCenterFromRect(RectF r) {
        return new float[]{r.centerX(), r.centerY()};
    }

    /**
     * Takes an array of 2D coordinates representing corners and returns the
     * smallest rectangle containing those coordinates.
     *
     * @param array array of 2D coordinates
     * @return smallest rectangle containing coordinates
     */
    public static RectF trapToRectF(float[] array) {
        RectF r = new RectF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (int i = 1; i < array.length; i += 2) {
            float x = array[i - 1];
            float y = array[i];
            r.left = Math.min(x, r.left);
            r.top = Math.min(y, r.top);
            r.right = Math.max(x, r.right);
            r.bottom = Math.max(y, r.bottom);
        }
        r.sort();
        return r;
    }

    public static Rect trapToRect(float[] array) {
        Rect r = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        for (int i = 1; i < array.length; i += 2) {
            float x = array[i - 1];
            float y = array[i];
            r.left = (int) Math.min(x, r.left);
            r.top = (int) Math.min(y, r.top);
            r.right = (int) Math.max(x, r.right);
            r.bottom = (int) Math.max(y, r.bottom);
        }
        r.sort();
        return r;
    }
}