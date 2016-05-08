package com.rm.cameraphone.components.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.rm.cameraphone.constants.CameraConstants;
import com.rm.cameraphone.events.OnCameraFocusedListener;
import com.rm.cameraphone.util.DispatchUtils;

import java.text.MessageFormat;
import java.util.List;

/**
 * Created by alex
 */
@SuppressLint("ViewConstructor")
public class CameraPreviewSurface extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback {

    private static final String TAG = "CameraPreview";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private Activity mActivity;
    private Camera mCamera;
    private Camera.Size mPreviewSize;
    private Camera.CameraInfo mCameraInfo;
    private List<Camera.Size> mSupportedPreviewSizes;
    private OnCameraFocusedListener mFocusListener;

    private SurfaceHolder mHolder;
    private Runnable mTaskStartPreview;
    private Runnable mTaskStopPreview;
    private Runnable mTaskAutoFocus;

    private int mDisplayOrientation;
    private boolean mHasAutoFocus;
    private boolean mHasContiniousFocus;

    public CameraPreviewSurface(Activity activity, Camera camera, Camera.CameraInfo cameraInfo,
                                OnCameraFocusedListener focusListener) {
        super(activity);

        this.mActivity = activity;
        this.mCamera = camera;
        this.mSupportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
        this.mCameraInfo = cameraInfo;
        this.mFocusListener = focusListener;

        List<String> supportedFocusModes = camera.getParameters().getSupportedFocusModes();
        mHasAutoFocus = supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO);
        mHasContiniousFocus = supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        mDisplayOrientation = getScreenOrientation();

        registerTasks();
        initHolder();
    }

    private void initHolder() {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        SurfaceHolder holder = getHolder();
        if (holder != null) {
            holder.addCallback(this);
            holder.setKeepScreenOn(true);
        }
    }

    private void registerTasks() {
        mTaskStartPreview = registerTaskStartPreview();
        mTaskStopPreview = registerTaskStopPreview();
        mTaskAutoFocus = registerTaskAutoFocus();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        // The Surface has been created, now tell the camera where to draw the preview.
        startPreview(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        stopPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, MessageFormat.format("surfaceChanged({0}, {1})", width, height));
        // If your preview can change or rotate, get care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (holder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        stopPreview();
        startPreview(holder);
//        setOnTouchListener(new CameraTouchListener()); TODO implement that when I'll have enough time
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

    private void startPreview(SurfaceHolder holder) {
        mHolder = holder;
        DispatchUtils.getCameraQueue().postRunnable(mTaskStartPreview);
    }

    private void stopPreview() {
        DispatchUtils.getCameraQueue().postRunnable(mTaskStopPreview);
    }

    private Runnable registerTaskStartPreview() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "START PREVIEW");
                try {
                    int rotation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);

                    mCamera.setPreviewDisplay(mHolder);
                    mCamera.setDisplayOrientation(rotation);

                    Camera.Parameters parameters = mCamera.getParameters();

                    if (mHasContiniousFocus)
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    else if (mHasAutoFocus)
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

                    parameters.setRotation(rotation);
                    parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                    mCamera.setParameters(parameters);

                    mCamera.startPreview();
                } catch (Exception e) {
                    Log.e(TAG, "Error starting camera preview: " + e.getMessage());
                }
            }
        };
    }

    private Runnable registerTaskStopPreview() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "STOP PREVIEW");
                try {
                    mCamera.stopPreview();
                } catch (Exception e) {
                    Log.e(TAG, "Error stopping camera preview: " + e.getMessage());
                }
            }
        };
    }

    private Runnable registerTaskAutoFocus() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "AUTO FOCUS");
                mCamera.autoFocus(CameraPreviewSurface.this);
            }
        };
    }

    public void takePicture() {
        if (mHasAutoFocus || mHasContiniousFocus) {
            startFocusing();
        } else {
            focused();
        }
    }

    public void onPictureTaken() {
        clearCameraFocus();
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        DispatchUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                focused();
            }
        });
    }

    private void startFocusing() {
        DispatchUtils.getCameraQueue().postRunnable(mTaskAutoFocus);
    }

    private void focused() {
        if (mFocusListener != null) {
            mFocusListener.onFocused(mCamera);
        }
    }

    private void clearCameraFocus() {
        if (mHasAutoFocus) {
            mCamera.cancelAutoFocus();
        }

        stopPreview();
        startPreview(getHolder());
    }

    private int getScreenOrientation() {
        return mActivity.getWindowManager().getDefaultDisplay().getRotation();
    }

    // utility methods
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
}
