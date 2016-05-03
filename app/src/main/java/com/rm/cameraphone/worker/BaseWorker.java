package com.rm.cameraphone.worker;

import android.app.Activity;

/**
 * Created by alex
 */
public abstract class BaseWorker {
    protected Activity mHost;

    public BaseWorker(Activity host) {
        mHost = host;
        register();
    }

    protected abstract void register();

    public abstract void onAllow();

    public abstract void onResume();

    public abstract void onPause();

    public abstract void onDestroy();
}
