package com.rm.cameraphone.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ComponentCallbacks2;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.rm.cameraphone.R;
import com.rm.cameraphone.components.SwipingFrameLayout;
import com.rm.cameraphone.components.camera.CameraPreviewSurface;
import com.rm.cameraphone.components.camera.CameraSwitcher;
import com.rm.cameraphone.components.camera.CaptureButton;
import com.rm.cameraphone.components.camera.CaptureWrapper;
import com.rm.cameraphone.components.camera.FlashSwitcher;
import com.rm.cameraphone.components.camera.SchemeIndicator;
import com.rm.cameraphone.components.camera.TimingView;
import com.rm.cameraphone.events.OnCameraFocusedListener;
import com.rm.cameraphone.events.OnCaptureButtonListener;
import com.rm.cameraphone.events.OnChangeCameraListener;
import com.rm.cameraphone.events.OnFlashModeListener;
import com.rm.cameraphone.events.OnSwipeListener;
import com.rm.cameraphone.util.Animators;
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
import static com.rm.cameraphone.constants.SharedMapConstants.KEY_CAMERA_SHOT_PATH;
import static com.rm.cameraphone.util.Interpolators.DECELERATE;
import static com.rm.cameraphone.util.Interpolators.OVERSHOOT;

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
    @InjectView(R.id.camera_timer) TimingView mCameraTimer;
    @InjectView(R.id.camera_preview) SwipingFrameLayout mCameraPreviewWrapper;

    // saving state
    @InjectView(R.id.camera_shot_preview) ImageView mShotPreview;
    @InjectView(R.id.camera_btn_action) ImageView mButtonAction; // crop or play/pause
    @InjectView(R.id.camera_btn_done) ImageView mButtonDone;
    @InjectView(R.id.camera_btn_cancel) ImageView mButtonCancel;

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

        mButtonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateToSavingState(false);
            }
        });

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWorker.deleteTempFile();
                animateToSavingState(false);
            }
        });

        mButtonAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // here is the method call in which we are switching camera mode states
                // TODO implement actions
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
        final String[] permissionsNeeded = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        };

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
                animateToSavingState(true);
            }
        });
    }

    @Override
    public void onStartRecord() {
        animateControlsForCapturing(false);

        mWorker.startVideoCapturing(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "VIDEO CAPTURE STARTED");
                mCameraTimer.start();
            }
        }, new Runnable() {
            @Override
            public void run() {
                if (mCaptureButton.isRecording()) mCaptureButton.animateRecord(true);
                onStopRecord();
            }
        });
    }

    @Override
    public void onStopRecord() {
        animateControlsForCapturing(true);

        mWorker.stopVideoCapturing(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "VIDEO CAPTURE STOPPED");
                mCameraTimer.stop();
                animateToSavingState(true);
            }
        });
    }

    @Override
    public void onCapture() {
        if (mCameraPreview == null) return;

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


    private void setupShotPreview(boolean show) {
        String previewPath = (String) SharedMap.holder().get(KEY_CAMERA_SHOT_PATH);
        if (previewPath == null) return;
        mShotPreview.setVisibility(show ? View.VISIBLE : View.GONE);

        if (!show) {
            Glide.clear(mShotPreview);
            SharedMap.holder().remove(KEY_CAMERA_SHOT_PATH);
            cleanUp();
            return;
        }

        Glide.with(this).load(previewPath).into(mShotPreview);
    }

    private void setControlsEnabled(boolean enabled) {
        mCaptureButton.show();
        mFlashSwitcher.setClickable(enabled);
        mCaptureButton.setEnabled(enabled);
        mCameraSwitcher.setEnabled(enabled);
    }

    private void animateToSavingState(boolean show) {
        mCameraPreviewWrapper.setEnabled(!show);

        if (show) {
            mWorker.pausePreview(new Runnable() {
                @Override
                public void run() {
                    animateOverlay(true);
                    animateControlsForSaving(false);
                    animateSavingButtons(true);

                    setupShotPreview(true);
                }
            });
        } else {
            mWorker.savePhotoToGallery();
            mWorker.resumePreview(new Runnable() {
                @Override
                public void run() {
                    setupShotPreview(false);

                    animateOverlay(false);
                    animateSavingButtons(false);
                }
            });
        }
    }

    private void animateSavingButtons(final boolean show) {
        ValueAnimator animator = Animators.animateValue(200, OVERSHOOT, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                float appliedFraction = Animators.calculateAnimatedValue(0, 1, fraction, !show);

                mButtonDone.setScaleY(appliedFraction);
                mButtonDone.setScaleX(appliedFraction);

                mButtonCancel.setScaleX(appliedFraction);
                mButtonCancel.setScaleY(appliedFraction);

                mButtonAction.setAlpha(appliedFraction);
            }
        });

        animator.setStartDelay(show ? 200 : 0);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (show) {
                    mButtonDone.setVisibility(View.VISIBLE);
                    mButtonCancel.setVisibility(View.VISIBLE);
                    mButtonAction.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!show) {
                    mButtonDone.setVisibility(View.GONE);
                    mButtonCancel.setVisibility(View.GONE);
                    mButtonAction.setVisibility(View.GONE);
                    animateControlsForSaving(true);
                }
            }
        });

        animator.start();
    }

    private void animateControlsForSaving(boolean show) {
        mCameraSwitcher.setEnabled(show);
        mFlashSwitcher.setClickable(show);

        if (show) {
            mSchemeIndicator.show();
            mCameraSwitcher.show();
            mFlashSwitcher.show();
            mCaptureButton.show();
        } else {
            mSchemeIndicator.hide();
            mCameraSwitcher.hide();
            mFlashSwitcher.hide();
            mCaptureButton.hide();
        }
    }

    private void animateControlsForCapturing(boolean show) {
        mCameraPreviewWrapper.setEnabled(show);

        if (show) {
            mSchemeIndicator.show();
            mCameraSwitcher.show();
            mCaptureWrapper.show();
            mFlashSwitcher.show();
        } else {
            mSchemeIndicator.hide();
            mCameraSwitcher.hide();
            mCaptureWrapper.hide();
            mFlashSwitcher.hide();
        }
    }

    private void animateOverlay(boolean show) {
        mPreviewOverlay.animate()
                .alpha(show ? 1 : 0)
                .setDuration(300)
                .setInterpolator(DECELERATE)
                .start();
    }

    public void cleanUp() {
        Glide.get(this).clearMemory();
        Glide.get(this).trimMemory(ComponentCallbacks2.TRIM_MEMORY_MODERATE);
    }
}