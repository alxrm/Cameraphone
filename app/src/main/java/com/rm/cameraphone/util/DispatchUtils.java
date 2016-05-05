package com.rm.cameraphone.util;

import android.os.Handler;
import android.os.Looper;

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

    public static void post(Runnable task) {
        runOnUiThread(task);
    }

    public static void postDelayed(Runnable task, long delay) {
        sHandler.postDelayed(task, delay);
    }
}
