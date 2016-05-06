package com.rm.cameraphone.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;

import com.rm.cameraphone.R;
import com.rm.cameraphone.components.SwipingFrameLayout;
import com.rm.cameraphone.components.camera.CameraPreviewSurface;
import com.rm.cameraphone.components.camera.CameraSwitcher;
import com.rm.cameraphone.components.camera.CaptureButton;
import com.rm.cameraphone.components.camera.CaptureWrapper;
import com.rm.cameraphone.components.camera.FlashSwitcher;
import com.rm.cameraphone.components.camera.SchemeIndicator;
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
import static com.rm.cameraphone.util.Interpolators.DECELERATE;

public class MainActivity extends BaseActivity<CameraWorker> implements
        OnCaptureButtonListener, OnCameraFocusedListener,
        OnChangeCameraListener, OnFlashModeListener,
        OnSwipeListener {

    private static final String TAG = "MainActivity";

    @InjectView(R.id.camera_preview_overlay) RelativeLayout mPreviewOverlay;
    @InjectView(R.id.camera_capture_wrapper) CaptureWrapper mCaptureWrapper;
    @InjectView(R.id.camera_capture_button) CaptureButton mCaptureButton;
    @InjectView(R.id.camera_flash_switcher) FlashSwitcher mFlashSwitcher;
    @InjectView(R.id.camera_indicator) SchemeIndicator mSchemeIndicator;
    @InjectView(R.id.camera_switcher) CameraSwitcher mCameraSwitcher;
    @InjectView(R.id.camera_preview) SwipingFrameLayout mCameraPreviewWrapper;

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
        mCameraPreviewWrapper.setOnSwipeListener(this);
        mCameraPreviewWrapper.setEnabled(false);

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

        mSchemeIndicator.setIndicatorListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mCameraPreviewWrapper.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mCameraPreviewWrapper.setEnabled(true);
                setControlsEnabled(true);
            }
        });
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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

                mCameraPreviewWrapper.setEnabled(true);
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
                mCameraPreviewWrapper.setEnabled(true);
                setControlsEnabled(true);
            }
        });
    }

    @Override
    public void onStartRecord() {
        mCameraPreviewWrapper.setEnabled(false);
        mSchemeIndicator.hide();
        mCameraSwitcher.hide();
        mCaptureWrapper.hide();
    }

    @Override
    public void onStopRecord() {
        mCameraPreviewWrapper.setEnabled(true);
        mSchemeIndicator.show();
        mCameraSwitcher.show();
        mCaptureWrapper.show();
    }

    @Override
    public void onCapture() {
        mCameraPreviewWrapper.setEnabled(false);
        setControlsEnabled(false);

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

        mCameraPreviewWrapper.setEnabled(false);
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
        mCameraPreviewWrapper.setEnabled(true);

        setupFlashMode();
        setControlsEnabled(true);
        animateOverlay(false);
    }

    @Override
    public void onSwipeStopped(float fraction, boolean toLeft) {
        final boolean overThreshold = fraction > 0.5;
        final boolean exactToLeft = mCurrentCameraMode == CAMERA_MODE_PHOTO && toLeft;
        final boolean exactToRight = mCurrentCameraMode == CAMERA_MODE_VIDEO && !toLeft;

        if (exactToLeft) {
            mSchemeIndicator.animateToState(overThreshold, fraction);
        } else if (exactToRight) {
            mSchemeIndicator.animateToState(!overThreshold, fraction);
        }

        if (overThreshold) {
            if (exactToLeft) {
                mCurrentCameraMode = CAMERA_MODE_VIDEO;
                mCaptureWrapper.animateToState(STATE_TRANSPARENT);
                mCaptureButton.animateToState(STATE_VIDEO);
            } else if (exactToRight) {
                mCurrentCameraMode = CAMERA_MODE_PHOTO;
                mCaptureWrapper.animateToState(STATE_OPAQUE);
                mCaptureButton.animateToState(STATE_PHOTO);
            }
        }
    }

    @Override
    public void onSwipeRight(float fraction) {
        if (mCurrentCameraMode == CAMERA_MODE_PHOTO) return;

        mSchemeIndicator.setFraction(fraction, true);
        setControlsEnabled(false);
    }

    @Override
    public void onSwipeLeft(float fraction) {
        if (mCurrentCameraMode == CAMERA_MODE_VIDEO) return;

        mSchemeIndicator.setFraction(fraction, false);
        setControlsEnabled(false);
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
        mFlashSwitcher.setClickable(enabled);
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