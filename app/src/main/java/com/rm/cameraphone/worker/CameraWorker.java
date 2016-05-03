package com.rm.cameraphone.worker;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;

import com.rm.cameraphone.components.camera.CameraPreviewSurface;
import com.rm.cameraphone.events.OnCameraFocusedListener;
import com.rm.cameraphone.util.DispatchUtils;
import com.rm.cameraphone.util.SharedMap;

import java.io.IOException;
import java.lang.ref.WeakReference;

import static com.rm.cameraphone.constants.SharedMapConstants.KEY_CAMERA_PREVIEW;
import static com.rm.cameraphone.util.DispatchUtils.runOnUiThread;

/**
 * Created by alex
 */
public class CameraWorker extends BaseWorker {

    private static final String TAG = "CameraWorker";

    private Camera mCamera;
    private Camera.Parameters mParameters;
    private Camera.CameraInfo mCameraInfo;
    private CameraPreviewSurface mCameraPreview;

    private int mCameraId;
    private boolean mUsingFrontCamera;

    private Runnable mTaskSetupPreview;
    private Runnable mTaskClearPreview;
    private Runnable mTaskTakePicture;

    public CameraWorker(Activity host) {
        super(host);
    }

    @Override
    protected void register() {
        mTaskSetupPreview = registerTaskSetupPreview();
        mTaskClearPreview = registerTaskClearPreview();
        mTaskTakePicture = registerTaskTakePicture();
    }

    @Override
    public void onAllow() {

    }

    @Override
    public void onResume() {
        if (mCamera != null) {
            try {
                mCamera.reconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /* IMPLEMENTATION PART */
    private Runnable registerTaskSetupPreview() {
        return new Runnable() {
            @Override
            public void run() {
                mCamera = getCameraInstance(mUsingFrontCamera);
                if (mCamera == null) {
                    return;
                }

                mParameters = mCamera.getParameters();
                mCamera.setParameters(mParameters);

                mCameraInfo = getCameraInfo(mCameraId);

                mCameraPreview = new CameraPreviewSurface(
                        mHost, mCamera, mCameraInfo, (OnCameraFocusedListener) mHost
                );

                SharedMap.holder().put(KEY_CAMERA_PREVIEW, new WeakReference<Object>(mCameraPreview));
            }
        };
    }

    private Runnable registerTaskClearPreview() {
        return new Runnable() {
            @Override
            public void run() {
                mCamera.stopPreview();
                mCamera.release();
            }
        };
    }

    private Runnable registerTaskTakePicture() {
        return new Runnable() {
            @Override
            public void run() {

            }
        };
    }

    public void setupPreview(final Runnable mainThreadCallback) {
        DispatchUtils.getCameraQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                mTaskSetupPreview.run();
                runOnUiThread(mainThreadCallback);
            }
        });
    }

    public void changeCamera(final Runnable mainThreadCallback) {
        DispatchUtils.getCameraQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                mUsingFrontCamera = !mUsingFrontCamera;

                mTaskClearPreview.run();
                mTaskSetupPreview.run();
                runOnUiThread(mainThreadCallback);
            }
        });
    }

    public void takePicture(final Runnable mainThreadCallback) {
        DispatchUtils.getCameraQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                mTaskTakePicture.run();
                runOnUiThread(mainThreadCallback);
            }
        });
    }

    private synchronized Camera getCameraInstance(boolean useFrontCamera) {
        Camera c = null;
        try {
            c = Camera.open(getCameraId(useFrontCamera));
        } catch (Exception e) {
            Log.e(TAG, "Camera is not available");
        }
        return c;
    }

    private synchronized Camera.CameraInfo getCameraInfo(int cameraId) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        return cameraInfo;
    }

    private int getCameraId(boolean useFrontCamera) {
        int count = Camera.getNumberOfCameras();

        if (count > 0) {
            mCameraId = useFrontCamera ?
                    Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        Log.d("MainActivity", "getCameraId " + mCameraId);
        return mCameraId;
    }

}
