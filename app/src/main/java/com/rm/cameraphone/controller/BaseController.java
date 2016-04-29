package com.rm.cameraphone.controller;

import android.app.Activity;

/**
 * Created by alex
 */
public abstract class BaseController<D> {

    protected Activity mHost;

    public abstract void onStop();
    public void onStart(Activity activity, D metaData) {
        mHost = activity;
    }

    protected void checkHost() {
        if (mHost == null) throw new IllegalStateException("Host activity cannot be null");
    }
}
