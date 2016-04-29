package com.rm.cameraphone.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.CountDownLatch;

/**
 * Created by alex
 */
public class DispatchQueue extends Thread {

    private volatile Handler handler = null;
    private CountDownLatch syncLatch = new CountDownLatch(1);

    public DispatchQueue(final String threadName) {
        setName(threadName);
        start();
    }

    public void cancelRunnable(Runnable runnable) {
        try {
            syncLatch.await();
            handler.removeCallbacks(runnable);
        } catch (Throwable e) {
            Log.e("dispatcher", e.getLocalizedMessage());
        }
    }

    public void postRunnable(Runnable runnable) {
        postRunnable(runnable, 0);
    }

    public void postRunnable(Runnable runnable, long delay) {
        try {
            syncLatch.await();
            if (delay <= 0) {
                handler.post(runnable);
            } else {
                handler.postDelayed(runnable, delay);
            }
        } catch (Throwable e) {
            Log.e("dispatcher", e.getLocalizedMessage());
        }
    }

    public void postInFrontOfQueue(Runnable runnable) {
        try {
            syncLatch.await();
            handler.postAtFrontOfQueue(runnable);
        } catch (Throwable e) {
            Log.e("dispatcher", e.getLocalizedMessage());
        }
    }

    public void cleanupQueue() {
        try {
            syncLatch.await();
            handler.removeCallbacksAndMessages(null);
        } catch (Throwable e) {
            Log.e("dispatcher", e.getLocalizedMessage());
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new Handler();
        syncLatch.countDown();
        Looper.loop();
    }
}