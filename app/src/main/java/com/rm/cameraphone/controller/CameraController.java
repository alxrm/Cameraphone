package com.rm.cameraphone.controller;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.rm.cameraphone.components.camera.CameraPreview;
import com.rm.cameraphone.util.DispatchQueue;

/**
 * Created by alex
 */
public class CameraController extends BaseController<Void> {
    public interface CameraListener {
        void onCameraReceived(CameraPreview preview);
    }

    private static final String TAG = "CameraPreview";

    public static final int CAMERA_REAR = 0;
    public static final int CAMERA_FRONT = 1;

    private DispatchQueue mCameraQueue;
    private Handler mMainThreadHandler;

    private Camera mCamera;

    public CameraController() {
        mCameraQueue = new DispatchQueue(TAG);
        mMainThreadHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onStart(Activity activity, Void metaData) {
        super.onStart(activity, metaData);
    }

    @Override
    public void onStop() {
        releaseCamera();
    }

    public boolean hasFront() {
        return Camera.getNumberOfCameras() > 1;
    }

    public void getCameraPreviewDefault(CameraListener listener) {
        getCameraPreview(CAMERA_REAR, listener);
    }

    public void getCameraPreview(final int cameraId, final CameraListener listener) {
        mCameraQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                checkHost();

                // Get the rotation of the screen to adjust the preview image accordingly.
                final int displayRotation = mHost.getWindowManager().getDefaultDisplay().getRotation();

                // Open an instance of the first camera and retrieve its info.
                mCamera = getCameraInstance(cameraId);
                Camera.CameraInfo cameraInfo = getCameraInfo(cameraId);

                if (mCamera == null || cameraInfo == null) return;

                // Create the Preview view and set it as the content of this Activity.
                CameraPreview preview = new CameraPreview(mHost, mCamera, cameraInfo, displayRotation);

                mMainThreadHandler.post(getListenerTask(preview, listener));
            }
        }, 300);
    }

    private Runnable getListenerTask(final CameraPreview preview, final CameraListener listener) {
        return new Runnable() {
            @Override
            public void run() {
                if (listener != null) listener.onCameraReceived(preview);
            }
        };
    }

    private Camera getCameraInstance(int cameraId) {
        Camera c = null;
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.d(TAG, "Camera " + cameraId + " is not available: " + e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.CameraInfo getCameraInfo(int cameraId) {
        Camera.CameraInfo cameraInfo = null;

        if (mCamera != null) {
            // Get camera info only if the camera is available
            cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
        }

        return cameraInfo;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }
    }
}
