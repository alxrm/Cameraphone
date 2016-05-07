package com.rm.cameraphone.worker;

import android.app.Activity;

/**
 * Created by alex
 */
public abstract class BaseWorker {
    protected Activity mHost;

    public BaseWorker(Activity host) {
        mHost = host;

        registerTasks();
        registerCallbacks();
    }

    protected abstract void registerTasks();

    protected abstract void registerCallbacks();

    public abstract void onResume();

    public abstract void onPause();

    public abstract void onDestroy();
}
