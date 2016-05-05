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
    }

    protected abstract void registerTasks();

    public abstract void onResume();

    public abstract void onPause();

    public abstract void onDestroy();
}
