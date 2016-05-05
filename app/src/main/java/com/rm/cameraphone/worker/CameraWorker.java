package com.rm.cameraphone.worker;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.rm.cameraphone.components.camera.CameraPreviewSurface;
import com.rm.cameraphone.constants.FlashSwitcherConstants;
import com.rm.cameraphone.events.OnCameraFocusedListener;
import com.rm.cameraphone.util.DispatchUtils;
import com.rm.cameraphone.util.SharedMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.rm.cameraphone.constants.SharedMapConstants.KEY_CAMERA_PREVIEW;
import static com.rm.cameraphone.util.DispatchUtils.runOnUiThread;

/**
 * Created by alex
 */
public class CameraWorker extends BaseWorker {

    private static final String TAG = "CameraWorker";

    private Camera mCamera;
    private Camera.CameraInfo mCameraInfo;
    private CameraPreviewSurface mCameraPreview;

    private SparseArray<Camera.Parameters> mParameters;

    private int mCurCameraId;
    private boolean mUsingFrontCamera;

    private Runnable mTaskSetupPreview;
    private Runnable mTaskClearPreview;
    private Runnable mTaskTakePicture;

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.startPreview();

            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    public CameraWorker(Activity host) {
        super(host);

        mParameters = new SparseArray<>();
    }

    @Override
    protected void registerTasks() {
        mTaskSetupPreview = registerTaskSetupPreview();
        mTaskClearPreview = registerTaskClearPreview();
        mTaskTakePicture = registerTaskTakePicture();
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
                Log.d(TAG, "SETUP PREVIEW");
                mCamera = getCameraInstance(mUsingFrontCamera);
                if (mCamera == null) return;

                Camera.Parameters parameters = mParameters.get(mCurCameraId);

                if (parameters == null) {
                    parameters = mCamera.getParameters();
                    mParameters.append(mCurCameraId, parameters);
                }

                mCamera.setParameters(parameters);
                mCameraInfo = getCameraInfo(mCurCameraId);

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
                Log.d(TAG, "CLEAR PREVIEW");
                if (mCamera == null) return;

                try {
                    mCamera.stopPreview();
                    mCamera.release();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        };
    }

    private Runnable registerTaskTakePicture() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "TAKE PICTURE");
                mCamera.takePicture(null, null, mPicture);
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
                Log.d(TAG, "CHANGE CAMERA");
                mUsingFrontCamera = !mUsingFrontCamera;

                mTaskClearPreview.run();
                mTaskSetupPreview.run();
                runOnUiThread(mainThreadCallback);
            }
        });
    }

    public void setFlashMode(int flashModeKey) {
        Camera.Parameters parameters = mCamera.getParameters();
        String flashMode = null;

        switch (flashModeKey) {
            case FlashSwitcherConstants.FLASH_MODE_AUTO:
                flashMode = Camera.Parameters.FLASH_MODE_AUTO;
                break;
            case FlashSwitcherConstants.FLASH_MODE_OFF:
                flashMode = Camera.Parameters.FLASH_MODE_OFF;
                break;
            case FlashSwitcherConstants.FLASH_MODE_ON:
                flashMode = Camera.Parameters.FLASH_MODE_ON;
                break;
        }

        if (flashMode == null || !hasFlashFeature(flashMode) || mCamera == null) return;

        parameters.setFlashMode(flashMode);
        mParameters.append(mCurCameraId, parameters);
        mCamera.setParameters(parameters);
    }

    public boolean hasFlashFeature(String flashFeature) {
        if (mCamera == null) return false;
        if (flashFeature == null) flashFeature = Camera.Parameters.FLASH_MODE_AUTO;

        Camera.Parameters parameters = mCamera.getParameters();
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        return supportedFlashModes != null && supportedFlashModes.contains(flashFeature);
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
            mCurCameraId = useFrontCamera ?
                    Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        Log.d("MainActivity", "getCameraId " + mCurCameraId);
        return mCurCameraId;
    }
}
