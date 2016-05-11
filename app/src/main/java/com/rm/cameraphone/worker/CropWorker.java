package com.rm.cameraphone.worker;

import android.app.Activity;
import android.util.Log;

import com.rm.cameraphone.util.DispatchUtils;
import com.rm.cameraphone.util.FileUtils;

import java.io.File;

/**
 * Created by alex
 */
public class CropWorker extends BaseWorker {

    private Runnable mTaskDeleteResult;
    private Runnable mTaskSaveResult;

    private String mResultPath;

    public CropWorker(Activity host) {
        super(host);
    }

    @Override
    protected void registerTasks() {
        mTaskSaveResult = registerTasksSaveResult();
        mTaskDeleteResult = registerTasksEraseResult();
    }

    private Runnable registerTasksEraseResult() {
        return new Runnable() {
            @Override
            public void run() {
                if (mResultPath == null) return;

                File result = new File(mResultPath);

                if (result.delete()) {
                    Log.d("CropWorker", "Cropped image has been deleted successfully");
                }
            }
        };
    }

    private Runnable registerTasksSaveResult() {
        return new Runnable() {
            @Override
            public void run() {
                if (mResultPath == null) return;
                FileUtils.addFileToSystemMedia(new File(mResultPath));
            }
        };
    }

    @Override
    protected void registerCallbacks() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onTrimMemory() {

    }

    public void saveResult(String resultPath) {
        mResultPath = resultPath;

        DispatchUtils.getCropQueue().postRunnable(mTaskSaveResult);
    }


    public void eraseResult(String resultPath) {
        mResultPath = resultPath;

        DispatchUtils.getCropQueue().postRunnable(mTaskDeleteResult);
    }
}
