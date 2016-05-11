package com.rm.cameraphone.util;

import android.content.ComponentCallbacks2;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.rm.cameraphone.Cameraphone;
import com.rm.cameraphone.worker.DispatchQueue;

/**
 * Created by alex
 */
public class DispatchUtils {

    private static DispatchQueue sCameraQueue;
    private static DispatchQueue sFileQueue;
    private static DispatchQueue sCropQueue;

    private static Handler sHandler;

    public static void init() {
        if (sHandler == null) {
            sHandler = new Handler(Looper.getMainLooper());
        }
    }

    public static DispatchQueue getCameraQueue() {
        if (sCameraQueue == null) {
            sCameraQueue = new DispatchQueue("CameraQue");
        }
        return sCameraQueue;
    }

    public static DispatchQueue getCropQueue() {
        if (sCropQueue == null) {
            sCropQueue = new DispatchQueue("CropQue");
        }
        return sCropQueue;
    }

    public static DispatchQueue getFileQueue() {
        if (sFileQueue == null) {
            sFileQueue = new DispatchQueue("FileQue");
        }
        return sFileQueue;
    }

    public static void runOnUiThread(Runnable task) {
        sHandler.post(task);
    }

    public static void runOnUiThread(Runnable task, long delay) {
        sHandler.postDelayed(task, delay);
    }

    public static void cancelRunOnUiThread(Runnable task) {
        sHandler.removeCallbacks(task);
    }

    public static void cleanUp() {
        DispatchUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.get(Cameraphone.getContext()).clearMemory();
                Glide.get(Cameraphone.getContext()).trimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);
            }
        });
    }
}
