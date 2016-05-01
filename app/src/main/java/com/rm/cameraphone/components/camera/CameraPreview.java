package com.rm.cameraphone.components.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.rm.cameraphone.constants.CameraConstants;
import com.rm.cameraphone.events.OnPreviewReadyListener;
import com.rm.cameraphone.util.DispatchQueue;

import java.io.IOException;
import java.util.List;

/**
 * Created by alex
 */
@SuppressLint("ViewConstructor")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraPreview";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private int mDisplayOrientation;

    // Camera settings
    private Camera mCamera;
    private Camera.CameraInfo mCameraInfo;
    private SurfaceHolder mHolder;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;

    // Callback
    private DispatchQueue mQueue;
    private OnPreviewReadyListener mListener;
    private Handler mHandler;

    public CameraPreview(Context context,
                         Camera camera,
                         Camera.CameraInfo cameraInfo,
                         int displayOrientation) {
        super(context);

        if (camera == null || cameraInfo == null) {
            return;
        }

        mCamera = camera;
        mCameraInfo = cameraInfo;
        mSupportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
        mDisplayOrientation = displayOrientation;

        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = calculateOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        checkQueue();

        mQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    mCamera.setPreviewDisplay(holder);
                    mCamera.startPreview();

                    notifyListener();
                    Log.d(TAG, "Camera preview started.");
                } catch (IOException e) {
                    Log.d(TAG, "Error setting camera preview: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("CameraPreview", "surfaceDestroyed");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        checkQueue();

        mQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (mHolder.getSurface() == null) {
                    Log.d(TAG, "Preview surface does not exist");
                    return;
                }

                // stop preview before making changes
                try {
                    mCamera.stopPreview();
                    Log.d(TAG, "Preview stopped.");
                } catch (Exception e) {
                    Log.d(TAG, "Error starting camera preview: " + e.getMessage());
                }

                int orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);

                try {
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    parameters.setAutoWhiteBalanceLock(true);

                    mCamera.setParameters(parameters);
                    mCamera.setDisplayOrientation(orientation);
                    mCamera.setPreviewDisplay(mHolder);
                    mCamera.startPreview();

                    notifyListener();
                    Log.d(TAG, "Camera preview started.");
                } catch (Exception e) {
                    Log.d(TAG, "Error starting camera preview: " + e.getMessage());
                }
            }
        });
    }

    public void setListener(OnPreviewReadyListener listener) {
        mListener = listener;
    }

    public void setQueue(DispatchQueue queue) {
        mQueue = queue;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    private Camera.Size calculateOptimalPreviewSize(List<Camera.Size> supportedPreviewSizes, int width, int height) {
        if (supportedPreviewSizes == null) return null;

        Camera.Size optimalSize = null;
        double targetRatio = (double) height / width;
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : supportedPreviewSizes) {
            double ratio = (double) size.width / size.height;

            if (Math.abs(ratio - targetRatio) > CameraConstants.ASPECT_TOLERANCE) continue;

            if (Math.abs(size.height - height) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - height);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;

            for (Camera.Size size : supportedPreviewSizes) {
                if (Math.abs(size.height - height) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - height);
                }
            }
        }

        return optimalSize;
    }

    private int calculatePreviewOrientation(Camera.CameraInfo info, int rotation) {
        int degrees = ORIENTATIONS.get(rotation);
        int result;

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    private void notifyListener() {
        if (mHandler == null || mListener == null) return;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mListener.onPreviewReady();
            }
        }, 400);
    }

    private void checkQueue() {
        if (mQueue == null) throw new IllegalStateException("Camera surface MUST have Event Loop");
    }

    public void updateParameters(final String key, final int value) {
        checkQueue();

        mQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                mCamera.getParameters().set(key, value);
                mCamera.startPreview();

                notifyListener();
            }
        });
    }

    public void updateParameters(final String key, final String value) {
        checkQueue();

        mQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                mCamera.getParameters().set(key, value);
                mCamera.startPreview();

                notifyListener();
            }
        });
    }
}