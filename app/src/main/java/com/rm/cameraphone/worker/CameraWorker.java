package com.rm.cameraphone.worker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.util.SparseArray;

import com.rm.cameraphone.components.camera.CameraPreviewSurface;
import com.rm.cameraphone.constants.FlashSwitcherConstants;
import com.rm.cameraphone.events.OnCameraFocusedListener;
import com.rm.cameraphone.util.bitmap.Bitmaps;
import com.rm.cameraphone.util.DispatchUtils;
import com.rm.cameraphone.util.FileUtils;
import com.rm.cameraphone.util.SharedMap;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import static com.rm.cameraphone.constants.CameraConstants.MAX_VIDEO_DURATION;
import static com.rm.cameraphone.constants.FileContants.OUTPUT_PHOTO;
import static com.rm.cameraphone.constants.FileContants.OUTPUT_VIDEO;
import static com.rm.cameraphone.constants.SharedMapConstants.KEY_CAMERA_PREVIEW;
import static com.rm.cameraphone.constants.SharedMapConstants.KEY_CAMERA_SHOT_PATH;
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

    private MediaRecorder mMediaRecorder;

    // photo middle calc data
    private File mCurOutputFile;

    private int mCurCameraId;
    private boolean mUsingFrontCamera;

    private Runnable mTaskSetupPreview;
    private Runnable mTaskClearPreview;
    private Runnable mTaskTakePicture;
    private Runnable mTaskVideoCaptureStart;
    private Runnable mTaskVideoCaptureStop;
    private Runnable mTaskReleaseComponents;

    // delayed callbacks
    private Camera.PictureCallback mOnPictureTaken;
    private Runnable mStopVideoHook;
    private Runnable mTakePictureHook;

    public CameraWorker(Activity host) {
        super(host);

        mParameters = new SparseArray<>();
    }

    @Override
    protected void registerTasks() {
        mTaskSetupPreview = registerTaskSetupPreview();
        mTaskClearPreview = registerTaskClearPreview();
        mTaskTakePicture = registerTaskTakePicture();
        mTaskVideoCaptureStart = registerTaskVideoCaptureStart();
        mTaskVideoCaptureStop = registerTaskVideoCaptureStop();
        mTaskReleaseComponents = registerTaskReleaseComponents();
    }

    @Override
    protected void registerCallbacks() {
        mOnPictureTaken = registerCallbackPictureTaken();
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
        DispatchUtils.getCameraQueue().postRunnable(mTaskReleaseComponents);
    }

    @Override
    public void onTrimMemory() {
    }

    /* IMPLEMENTATION PART */

    // registering runnable tasks
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
                mCamera.takePicture(null, null, mOnPictureTaken);
            }
        };
    }

    private Runnable registerTaskVideoCaptureStart() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "CAPTURE VIDEO");

                if (prepareVideoRecorder()) mMediaRecorder.start();
                else releaseMediaRecorder();
            }
        };
    }

    private Runnable registerTaskVideoCaptureStop() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    if (mMediaRecorder != null) {
                        mMediaRecorder.stop();
                    }
                } catch (RuntimeException e) { // the only one solution, srsly
                    if (mCurOutputFile != null && mCurOutputFile.delete()) {
                        Log.d("CameraWorker", "Deleted temp video file");
                    }
                    e.printStackTrace();
                } finally {
                    SharedMap.holder().put(KEY_CAMERA_SHOT_PATH, new WeakReference<Object>(mCurOutputFile.toString()));
                    releaseMediaRecorder();
                    mCamera.lock();
                }
            }
        };
    }

    private Runnable registerTaskReleaseComponents() {
        return new Runnable() {
            @Override
            public void run() {
                releaseMediaRecorder();
                releaseCamera();
            }
        };
    }

    private Runnable generateTaskWriteFile(final byte[] data) {
        return new Runnable() {
            @Override
            public void run() {
                mCurOutputFile = FileUtils.generateOutputFile(OUTPUT_PHOTO);
                if (mCurOutputFile == null) return;

                if (mCurCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    Bitmap photo = BitmapFactory.decodeByteArray(data, 0, data.length);
                    photo = Bitmaps.rotateBitmap(photo, 180); // revert vertical mirroring
                    FileUtils.writeBitmapToDevice(photo, mCurOutputFile);
                } else {
                    FileUtils.writeFileToDevice(data, mCurOutputFile);
                }

                SharedMap.holder().put(KEY_CAMERA_SHOT_PATH, new WeakReference<Object>(mCurOutputFile.toString()));
                runOnUiThread(mTakePictureHook);
            }
        };
    }

    // registering callbacks
    private Camera.PictureCallback registerCallbackPictureTaken() {
        return new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
//                if (mCameraPreview != null) mCameraPreview.onPictureTaken();

                DispatchUtils.getFileQueue().postRunnable(generateTaskWriteFile(data));
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

    public void pausePreview(final Runnable mainThreadCallback) {
        DispatchUtils.getCameraQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                mCamera.stopPreview();

                runOnUiThread(mainThreadCallback);
            }
        });
    }

    public void resumePreview(final Runnable mainThreadCallback) {
        DispatchUtils.getCameraQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    mCamera.reconnect();
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }

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
        Camera.Parameters parameters;
        String flashMode = null;

        try {
            parameters = mCamera.getParameters();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return;
        }

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

        if (flashMode == null || !hasFlashFeature(flashMode)) return;

        parameters.setFlashMode(flashMode);
        mParameters.append(mCurCameraId, parameters);
        mCamera.setParameters(parameters);
    }

    public void savePhotoToGallery() {
        DispatchUtils.getCameraQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                if (mCurOutputFile != null) {
                    FileUtils.addFileToSystemMedia(mCurOutputFile);
                }
            }
        });
    }

    public void deleteTempFile() {
        DispatchUtils.getCameraQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                if (mCurOutputFile != null && mCurOutputFile.delete()) {
                    mCurOutputFile = null;
                    Log.d("CameraWorker", "File has been deleted successfully");
                }
            }
        });
    }

    public boolean hasFlashFeature(String flashFeature) {
        if (mCamera == null) return false;
        if (flashFeature == null) flashFeature = Camera.Parameters.FLASH_MODE_AUTO;

        try {
            Camera.Parameters parameters = mCamera.getParameters();
            List<String> supportedFlashModes = parameters.getSupportedFlashModes();
            return supportedFlashModes != null && supportedFlashModes.contains(flashFeature);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    public void takePicture(final Runnable mainThreadCallback) {
        mTakePictureHook = mainThreadCallback;

        DispatchUtils.getCameraQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                mTaskTakePicture.run();
            }
        });
    }

    public void startVideoCapturing(final Runnable mainThreadCallback, Runnable stopVideoHook) {
        mStopVideoHook = stopVideoHook;

        DispatchUtils.getCameraQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                mTaskVideoCaptureStart.run();

                runOnUiThread(mainThreadCallback);
            }
        });
    }

    public void stopVideoCapturing(final Runnable mainThreadCallback) {
        DispatchUtils.getCameraQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                mTaskVideoCaptureStop.run();

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

    private synchronized boolean prepareVideoRecorder() {
        mCurOutputFile = FileUtils.generateOutputFile(OUTPUT_VIDEO);
        if (mCurOutputFile == null) return false;

        mCamera.unlock();

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(mCurCameraId, CamcorderProfile.QUALITY_HIGH));
        mMediaRecorder.setOutputFile(mCurOutputFile.toString());
        mMediaRecorder.setPreviewDisplay(mCameraPreview.getHolder().getSurface());
        mMediaRecorder.setMaxDuration(MAX_VIDEO_DURATION);
        mMediaRecorder.setOrientationHint(mCurCameraId == Camera.CameraInfo.CAMERA_FACING_BACK ? 90 : 270);

        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    DispatchUtils.runOnUiThread(mStopVideoHook);
                }
            }
        });

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCameraPreview.getHolder().removeCallback(mCameraPreview);
            mCamera.release();
            mCamera = null;
        }
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
