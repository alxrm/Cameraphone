package com.rm.cameraphone.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.rm.cameraphone.R;
import com.rm.cameraphone.components.ProgressBar;
import com.rm.cameraphone.components.SwipingFrameLayout;
import com.rm.cameraphone.components.VideoPlayerView;
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
import com.rm.cameraphone.events.VideoPlayerCallbacks;
import com.rm.cameraphone.util.Animators;
import com.rm.cameraphone.util.DispatchUtils;
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
import static com.rm.cameraphone.util.DimenUtils.dp;
import static com.rm.cameraphone.util.DimenUtils.width;
import static com.rm.cameraphone.util.DispatchUtils.cleanUp;
import static com.rm.cameraphone.util.Interpolators.DECELERATE;
import static com.rm.cameraphone.util.Interpolators.OVERSHOOT;

public class MainActivity extends BaseActivity<CameraWorker> implements
        OnCaptureButtonListener, OnCameraFocusedListener,
        OnChangeCameraListener, OnFlashModeListener,
        OnSwipeListener, VideoPlayerCallbacks {

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
    @InjectView(R.id.camera_photo_shot_preview) ImageView mPhotoShotPreview;
    @InjectView(R.id.camera_video_shot_preview) VideoPlayerView mVideoPreviewPlayer;
    @InjectView(R.id.camera_video_shot_progress) ProgressBar mVideoPreviewProgress;
    @InjectView(R.id.camera_btn_action) ImageView mButtonAction; // crop or play/pause
    @InjectView(R.id.camera_btn_done) ImageView mButtonDone;
    @InjectView(R.id.camera_btn_cancel) ImageView mButtonCancel;

    private CameraPreviewSurface mCameraPreview;
    private int mCurrentFlashMode = FLASH_MODE_AUTO;
    private int mCurrentCameraMode = CAMERA_MODE_PHOTO;
    private int mCurrentShotCameraMode;

    private String mImagePath;
    private Runnable mTaskClearPreview;

    private View.OnClickListener mActionVideoListener;
    private View.OnClickListener mActionCropListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        animateOverlay(true);

        registerTasks();

        setupViews();
        onTryCamera();
    }

    @Override
    protected CameraWorker setupWorker() {
        return new CameraWorker(this);
    }

    private void setupViews() {
        mCaptureButton.setOnCaptureButtonListener(this);
        mVideoPreviewPlayer.setVideoPlayerCallbacks(this);
        mFlashSwitcher.setFlashModeListener(this);
        mCameraPreviewWrapper.setOnSwipeListener(this);
        mCameraPreviewWrapper.setEnabled(false);

        mCameraPreviewWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWorker.startFocusing();
            }
        });

        mCameraSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraSwitcher.isEnabled()) {
                    mCameraSwitcher.toggle();
                    onChangeCamera();
                }
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
                animateToSavingState(false, mCurrentShotCameraMode);
            }
        });

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWorker.deleteTempFile();
                animateToSavingState(false, mCurrentShotCameraMode);
            }
        });

        mActionCropListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoCropActivity.start(MainActivity.this, mImagePath);
                mWorker.savePhotoToGallery();
                animateOverlay(true);

                onStopPreview();
            }
        };

        mActionVideoListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoPreviewPlayer.isPlaying()) {
                    mVideoPreviewPlayer.pause();
                } else {
                    mVideoPreviewPlayer.play();
                }
            }
        };

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

    private void registerTasks() {
        mTaskClearPreview = registerTasksClearPreview();
    }

    private Runnable registerTasksClearPreview() {
        return new Runnable() {
            @Override
            public void run() {
                animateOverlay(true);
                mCameraPreviewWrapper.setEnabled(false);
                mCameraPreviewWrapper.removeAllViews();

                if (mCameraPreview != null) {
                    mCameraPreview.getHolder().removeCallback(mCameraPreview);
                }

                mWorker.clearPreview();
            }
        };
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
        if (PermissionUtils.verifyPermissions(grantResults)) {
            onSetupPreview();
        }
    }

    private void onSetupPreview() {
        mWorker.setupPreview(new Runnable() {
            @Override
            public void run() {
                reloadCamera();
            }
        });
    }

    private void onStopPreview() {
        DispatchUtils.runOnUiThread(mTaskClearPreview, 10000);

        DispatchUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                animateToSavingState(false, mCurrentCameraMode);
                setControlsEnabled(false);
                mCameraPreviewWrapper.setEnabled(false);
            }
        }, 200);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCaptureButton.isRecording()) {
            mCaptureButton.animateRecord(true);
            onStopRecord();
        }

        mVideoPreviewPlayer.pause();
        cleanUp();
    }

    @Override
    protected void onStop() {
        super.onStop();
        onStopPreview();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DispatchUtils.cancelRunOnUiThread(mTaskClearPreview);
        animateToSavingState(false, mCurrentCameraMode);

        if (mWorker.isReleased()) {
            Log.d("MainActivity", "SHOULD NOT BE HERE");
            mWorker.setupPreview(new Runnable() {
                @Override
                public void run() {
                    onCameraChanged();
                }
            });
        } else {
            mWorker.resumePreview(new Runnable() {
                @Override
                public void run() {
                    mCameraPreviewWrapper.setEnabled(true);
                    setControlsEnabled(true);
                    animateOverlay(false);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.clear(mPhotoShotPreview);
    }

    @Override
    public void onFocused(Camera camera) {
        mWorker.takePicture(new Runnable() {
            @Override
            public void run() {
                animateToSavingState(true, CAMERA_MODE_PHOTO);
            }
        });
    }

    @Override
    public void onStartRecord() {
        mCameraSwitcher.setEnabled(false);
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
                animateToSavingState(true, CAMERA_MODE_VIDEO);
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
        if (!mWorker.hasFrontFacingCamera()) {
            Toast.makeText(this, "This device has no front facing camera", Toast.LENGTH_LONG).show();
            return;
        }

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
        DispatchUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reloadCamera();
            }
        }, 200);
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

    @Override
    public void onVideoProgressChanged(int progress) {
        mVideoPreviewProgress.setProgress(progress);
    }

    @Override
    public void onVideoPlayerStarted() {
        mButtonAction.setImageResource(R.drawable.video_pause);
    }

    @Override
    public void onVideoPlayerStopped() {
        mButtonAction.setImageResource(R.drawable.video_play);
    }

    @Override
    public void onVideoPlayerReady(int duration) {
        mVideoPreviewProgress.setMax(duration);
        mButtonAction.setOnClickListener(mActionVideoListener);
    }

    private void reloadCamera() {
        mCameraPreview = (CameraPreviewSurface) SharedMap.holder().get(KEY_CAMERA_PREVIEW);
        if (mCameraPreview == null) return;
        if (mCameraPreview.getParent() != null) return;

        mCameraPreviewWrapper.setEnabled(true);
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

    private void setupShotPreview(boolean show) {
        if (mCurrentShotCameraMode == CAMERA_MODE_PHOTO) {
            setupPhotoShotPreview(show);
        } else {
            setupVideoShotPreview(show);
        }
    }

    private void setupVideoShotPreview(boolean show) {
        mVideoPreviewPlayer.setVisibility(show ? View.VISIBLE : View.GONE);
        mVideoPreviewProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        mVideoPreviewProgress.setProgress(0);

        mButtonAction.setImageResource(R.drawable.video_play);

        mCaptureWrapper.animateToState(show ? STATE_TRANSPARENT :
                mCurrentCameraMode == CAMERA_MODE_PHOTO ? STATE_OPAQUE : STATE_TRANSPARENT);

        mImagePath = (String) SharedMap.holder().get(KEY_CAMERA_SHOT_PATH);
        if (mImagePath == null) return;

        mVideoPreviewPlayer.setVideoPath(mImagePath);

        if (show) {
            mVideoPreviewPlayer.showPreview();
        } else {
            mVideoPreviewPlayer.removePreview();
        }
    }

    private void setupPhotoShotPreview(boolean show) {
        mPhotoShotPreview.setVisibility(show ? View.VISIBLE : View.GONE);
        mButtonAction.setImageResource(R.drawable.crop);
        mButtonAction.setOnClickListener(mActionCropListener);

        if (!show) {
            Glide.clear(mPhotoShotPreview);
            SharedMap.holder().remove(KEY_CAMERA_SHOT_PATH);
            return;
        }

        mImagePath = (String) SharedMap.holder().get(KEY_CAMERA_SHOT_PATH);
        if (mImagePath != null) {
            Glide.with(this)
                    .load(mImagePath)
                    .override((int) width(), dp(200))
                    .into(mPhotoShotPreview);
        }
    }

    private void setControlsEnabled(boolean enabled) {
        mCaptureButton.show();
        mFlashSwitcher.setClickable(enabled);
        mCaptureButton.setEnabled(enabled);
        mCameraSwitcher.setEnabled(enabled);
    }

    private void animateToSavingState(boolean show, int mode) {
        mCurrentShotCameraMode = mode;
        mCameraPreviewWrapper.setEnabled(!show);

        if (show) {
            mWorker.pausePreview(new Runnable() {
                @Override
                public void run() {
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
                    animateSavingButtons(false);
                }
            });
        }
    }

    private void animateSavingButtons(final boolean show) {
        final TimeInterpolator interpolator = show ? OVERSHOOT : DECELERATE;
        final long animTime = show ? 400 : 200;

        final ValueAnimator animator = Animators.animateValue(animTime, interpolator, new ValueAnimator.AnimatorUpdateListener() {
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
                    animateOverlay(true);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!show) {
                    mButtonDone.setVisibility(View.GONE);
                    mButtonCancel.setVisibility(View.GONE);
                    mButtonAction.setVisibility(View.GONE);
                    animateOverlay(false);
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
}