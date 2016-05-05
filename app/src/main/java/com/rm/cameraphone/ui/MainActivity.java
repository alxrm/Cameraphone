package com.rm.cameraphone.ui;

import android.Manifest;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.rm.cameraphone.R;
import com.rm.cameraphone.components.CameraSwitcher;
import com.rm.cameraphone.components.CaptureButton;
import com.rm.cameraphone.components.CaptureWrapper;
import com.rm.cameraphone.components.FlashSwitcher;
import com.rm.cameraphone.components.camera.CameraPreviewSurface;
import com.rm.cameraphone.events.OnCameraFocusedListener;
import com.rm.cameraphone.events.OnCaptureButtonListener;
import com.rm.cameraphone.events.OnChangeCameraListener;
import com.rm.cameraphone.events.OnFlashModeListener;
import com.rm.cameraphone.events.OnSwipeListener;
import com.rm.cameraphone.util.PermissionUtils;
import com.rm.cameraphone.util.SharedMap;
import com.rm.cameraphone.worker.CameraWorker;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.rm.cameraphone.constants.CameraConstants.CAMERA_MODE_PHOTO;
import static com.rm.cameraphone.constants.CameraConstants.CAMERA_MODE_VIDEO;
import static com.rm.cameraphone.constants.CaptureButtonConstants.STATE_PHOTO;
import static com.rm.cameraphone.constants.CaptureButtonConstants.STATE_VIDEO;
import static com.rm.cameraphone.constants.CaptureWrapperConstants.STATE_OPAQUE;
import static com.rm.cameraphone.constants.CaptureWrapperConstants.STATE_TRANSPARENT;
import static com.rm.cameraphone.constants.FlashSwitcherConstants.FLASH_MODE_AUTO;
import static com.rm.cameraphone.constants.PermissionConstants.INITIAL_REQUEST;
import static com.rm.cameraphone.constants.SharedMapConstants.KEY_CAMERA_PREVIEW;
import static com.rm.cameraphone.util.DimenUtils.width;
import static com.rm.cameraphone.util.Interpolators.DECELERATE;

public class MainActivity extends BaseActivity<CameraWorker> implements
        OnCaptureButtonListener, OnCameraFocusedListener, OnChangeCameraListener, OnFlashModeListener {

    private static final String TAG = "MainActivity";

    @InjectView(R.id.camera_preview_overlay) RelativeLayout mPreviewOverlay;
    @InjectView(R.id.camera_capture_wrapper) CaptureWrapper mCaptureWrapper;
    @InjectView(R.id.camera_capture_button) CaptureButton mCaptureButton;
    @InjectView(R.id.camera_flash_switcher) FlashSwitcher mFlashSwitcher;
    @InjectView(R.id.camera_switcher) CameraSwitcher mCameraSwitcher;
    @InjectView(R.id.camera_preview) FrameLayout mCameraPreviewWrapper;

    private CameraPreviewSurface mCameraPreview;
    private int mCurrentFlashMode = FLASH_MODE_AUTO;
    private int mCurrentCameraMode = CAMERA_MODE_PHOTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        setupViews();
        onTryCamera();
    }

    @Override
    protected CameraWorker setupWorker() {
        return new CameraWorker(this);
    }

    private void setupViews() {
        mCaptureButton.setOnCaptureButtonListener(this);
        mFlashSwitcher.setFlashModeListener(this);

        mCameraSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraSwitcher.toggle();
                onChangeCamera();
            }
        });

        mFlashSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlashSwitcher.switchFlashMode();
            }
        });

        mCameraPreviewWrapper.setOnTouchListener(new OnSwipeListener() {
            @Override
            protected void onSwipeLeft(float distance) {
                MainActivity.this.onSwipeLeft(distance);
            }

            @Override
            protected void onSwipeRight(float distance) {
                MainActivity.this.onSwipeRight(distance);
            }

            @Override
            protected void onSwipeStopped(float distance, boolean toLeft) {
                MainActivity.this.onSwipeStopped(distance, toLeft);
            }
        });
    }

    private void onSwipeStopped(float distance, boolean toLeft) {
        final float wayFraction = distance / (width() / 2);

        if (wayFraction > 0.7) {
            if (mCurrentCameraMode == CAMERA_MODE_PHOTO && toLeft) {
                mCurrentCameraMode = CAMERA_MODE_VIDEO;
                mCaptureWrapper.animateToState(STATE_TRANSPARENT);
                mCaptureButton.animateToState(STATE_VIDEO);
            } else if (mCurrentCameraMode == CAMERA_MODE_VIDEO && !toLeft){
                mCurrentCameraMode = CAMERA_MODE_PHOTO;
                mCaptureWrapper.animateToState(STATE_OPAQUE);
                mCaptureButton.animateToState(STATE_PHOTO);
            }
        }

        animateOverlay(false);
    }

    private void onSwipeRight(float distance) {
        if (mCurrentCameraMode == CAMERA_MODE_PHOTO) return;

        final float wayFraction = distance / (width() / 2);
        mPreviewOverlay.setAlpha(wayFraction);
    }

    private void onSwipeLeft(float distance) {
        if (mCurrentCameraMode == CAMERA_MODE_VIDEO) return;

        final float wayFraction = distance / (width() / 2);
        mPreviewOverlay.setAlpha(wayFraction);
    }

    private void onTryCamera() {
        final String[] permissionsNeeded = { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE };
        final boolean hasPermission = PermissionUtils.checkAll(this, permissionsNeeded);

        if (hasPermission) {
            onSetupPreview();
        } else {
            PermissionUtils.requestPermissions(this, INITIAL_REQUEST, permissionsNeeded);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.verifyPermissions(grantResults)) onSetupPreview();
    }

    private void onSetupPreview() {
        mWorker.setupPreview(new Runnable() {
            @Override
            public void run() {
                mCameraPreview = (CameraPreviewSurface) SharedMap.holder().get(KEY_CAMERA_PREVIEW);
                if (mCameraPreview == null) return;
                if (mCameraPreview.getParent() != null) return;

                mCameraPreviewWrapper.addView(mCameraPreview);

                setupFlashMode();
                setControlsEnabled(true);
                animateOverlay(false);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCaptureButton.isRecording()) {
            mCaptureButton.animateRecord(true);
            onStopRecord();
        }
    }

    @Override
    public void onFocused(Camera camera) {
        mWorker.takePicture(new Runnable() {
            @Override
            public void run() {
                mCameraPreview.onPictureTaken();
            }
        });
    }

    @Override
    public void onStartRecord() {
        mCameraSwitcher.hide();
        mCaptureWrapper.hide();
    }

    @Override
    public void onStopRecord() {
        mCameraSwitcher.show();
        mCaptureWrapper.show();
    }

    @Override
    public void onCapture() {
        mCameraPreview.takePicture();
    }

    @Override
    public void onFlashModeChanged(int flashMode) {
        mCurrentFlashMode = flashMode;
        mWorker.setFlashMode(flashMode);
    }

    @Override
    public void onChangeCamera() {
        setControlsEnabled(false);
        animateOverlay(true);

        mCameraPreviewWrapper.removeAllViews();
        mWorker.changeCamera(new Runnable() {
            @Override
            public void run() {
                onCameraChanged();
            }
        });
    }

    @Override
    public void onCameraChanged() {
        mCameraPreview = (CameraPreviewSurface) SharedMap.holder().get(KEY_CAMERA_PREVIEW);

        mCameraPreviewWrapper.addView(mCameraPreview);

        setupFlashMode();
        setControlsEnabled(true);
        animateOverlay(false);
    }

    private void setupFlashMode() {
        boolean hasFeature = mWorker.hasFlashFeature(null);

        if (hasFeature) {
            mWorker.setFlashMode(mCurrentFlashMode);
            mFlashSwitcher.switchFlashModeTo(mCurrentFlashMode);
        }

        mFlashSwitcher.setEnabled(hasFeature);
    }

    private void setControlsEnabled(boolean enabled) {
        mCaptureButton.show();
        mFlashSwitcher.setClickable(false);
        mCaptureButton.setEnabled(enabled);
        mCameraSwitcher.setEnabled(enabled);
    }

    private void animateOverlay(boolean show) {
        mPreviewOverlay.animate()
                .alpha(show ? 1 : 0)
                .setDuration(200)
                .setInterpolator(DECELERATE)
                .start();
    }
}