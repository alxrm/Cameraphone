package com.rm.cameraphone.worker;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import com.rm.cameraphone.components.camera.CameraPreviewSurface;
import com.rm.cameraphone.events.OnCameraFocusedListener;
import com.rm.cameraphone.util.DispatchUtils;
import com.rm.cameraphone.util.SharedMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

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
