package com.rm.cameraphone;

import android.app.Application;
import android.content.Context;

import com.rm.cameraphone.util.DimenUtil;

/**
 * Created by alex
 */
public class Cameraphone extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;

        DimenUtil.init(this);
    }

    public static Context getContext() {
        return sContext;
    }
}
