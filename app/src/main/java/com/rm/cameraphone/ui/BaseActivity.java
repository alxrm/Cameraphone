package com.rm.cameraphone.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.rm.cameraphone.worker.BaseWorker;

/**
 * Created by alex
 */
public abstract class BaseActivity<W extends BaseWorker> extends AppCompatActivity {

    protected W mWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWorker = setupWorker();
    }

    protected abstract W setupWorker();

    @Override
    protected void onResume() {
        super.onResume();
        mWorker.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWorker.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWorker.onDestroy();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        mWorker.onTrimMemory();
    }
}
