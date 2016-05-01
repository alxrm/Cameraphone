package com.rm.cameraphone.controller;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.rm.cameraphone.components.camera.CameraPreview;
import com.rm.cameraphone.constants.CameraConstants;
import com.rm.cameraphone.events.OnCameraReadyListener;
import com.rm.cameraphone.events.OnPreviewReadyListener;
import com.rm.cameraphone.util.DispatchQueue;

/**
 * Created by alex
 */
public class CameraController extends BaseController<Void> {
    private static final String TAG = "CameraPreview";

    private volatile CameraPreview mCameraPreview;
    private volatile int mCurrentCameraId;

    private DispatchQueue mCameraQueue;
    private Handler mMainThreadHandler;

    private Camera mCamera;
    private OnCameraReadyListener mOnCameraReadyListener;
    private OnPreviewReadyListener mOnPreviewListener;
    private Camera.CameraInfo mCameraInfo;
    private int mDisplayRotation;

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

    public void retrieveCameraPreviewDefault(OnCameraReadyListener listener) {
        retrieveCameraPreview(Camera.CameraInfo.CAMERA_FACING_BACK, listener);
    }

    public void retrieveCameraPreview(int cameraId, OnCameraReadyListener listener) {
        mOnCameraReadyListener = listener;
        mCurrentCameraId = cameraId;

        mCameraQueue.postRunnable(getCameraTask());
    }

    public void setFlashMode(String flashModeState) {
        if (mCameraPreview == null) return;

        mCameraPreview.updateParameters(CameraConstants.KEY_FLASH_MODE, flashModeState);
    }

    public void setOnCameraReadyListener(OnCameraReadyListener onCameraReadyListener) {
        mOnCameraReadyListener = onCameraReadyListener;
    }

    public void setOnPreviewListener(OnPreviewReadyListener onPreviewListener) {
        mOnPreviewListener = onPreviewListener;
    }

    private Runnable getCameraTask() {
        return new Runnable() {
            @Override
            public void run() {
                checkHost();

                mDisplayRotation = mHost.getWindowManager().getDefaultDisplay().getRotation();

                mCamera = getCameraInstance(mCurrentCameraId);
                mCameraInfo = getCameraInfo(mCurrentCameraId);

                if (mCamera == null || mCameraInfo == null) return;

                mCameraPreview = new CameraPreview(
                        mHost, mCamera, mCameraInfo, mDisplayRotation
                );

                mCameraPreview.setQueue(mCameraQueue);
                mCameraPreview.setHandler(mMainThreadHandler);
                mCameraPreview.setListener(mOnPreviewListener);

                mMainThreadHandler.post(getCallbackTask());
            }
        };
    }

    private Runnable getCallbackTask() {
        return new Runnable() {
            @Override
            public void run() {
                if (mOnCameraReadyListener != null) {
                    mOnCameraReadyListener.onCameraReceived(mCameraPreview);
                }
            }
        };
    }

    private Camera getCameraInstance(int cameraId) {
        Camera camera = null;
        try {
            camera = Camera.open(cameraId);
        } catch (Exception e) {
            Log.d(TAG, "Camera " + cameraId + " is not available: " + e.getMessage());
        }
        return camera;
    }

    private Camera.CameraInfo getCameraInfo(int cameraId) {
        Camera.CameraInfo cameraInfo = null;

        if (mCamera != null) {
            cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
        }

        return cameraInfo;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

}
