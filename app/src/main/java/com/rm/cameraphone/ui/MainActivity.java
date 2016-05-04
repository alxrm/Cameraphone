package com.rm.cameraphone.ui;

import android.Manifest;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.rm.cameraphone.R;
import com.rm.cameraphone.components.CameraSwitchButton;
import com.rm.cameraphone.components.CaptureButton;
import com.rm.cameraphone.components.CaptureWrapper;
import com.rm.cameraphone.components.camera.CameraPreviewSurface;
import com.rm.cameraphone.events.OnCameraFocusedListener;
import com.rm.cameraphone.events.OnCaptureButtonListener;
import com.rm.cameraphone.events.OnChangeCameraListener;
import com.rm.cameraphone.util.PermissionUtils;
import com.rm.cameraphone.util.SharedMap;
import com.rm.cameraphone.worker.CameraWorker;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.rm.cameraphone.constants.PermissionConstants.INITIAL_REQUEST;
import static com.rm.cameraphone.constants.SharedMapConstants.KEY_CAMERA_PREVIEW;
import static com.rm.cameraphone.util.Interpolators.DECELERATE;

public class MainActivity extends AppCompatActivity implements
        OnCaptureButtonListener, OnCameraFocusedListener, OnChangeCameraListener {

    private static final String TAG = "MainActivity";

    @InjectView(R.id.camera_preview_overlay) RelativeLayout mPreviewOverlay;
    @InjectView(R.id.camera_capture_wrapper) CaptureWrapper mCaptureWrapper;
    @InjectView(R.id.camera_capture_button) CaptureButton mCaptureButton;
    @InjectView(R.id.camera_switch_button) CameraSwitchButton mSwitchButton;
    @InjectView(R.id.camera_preview) FrameLayout mCameraPreviewWrapper;

    private CameraWorker mCameraWorker;
    private CameraPreviewSurface mCameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mCameraWorker = new CameraWorker(this);

        setupViews();
        onTryCamera();
    }

    private void setupViews() {
        mCaptureButton.setOnCaptureButtonListener(this);
        mSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwitchButton.toggle();
                onChangeCamera();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.verifyPermissions(grantResults)) onSetupPreview();
    }

    private void onSetupPreview() {
        mCameraWorker.setupPreview(new Runnable() {
            @Override
            public void run() {
                mCameraPreview = (CameraPreviewSurface) SharedMap.holder().get(KEY_CAMERA_PREVIEW);
                if (mCameraPreview == null) return;

                mCameraPreviewWrapper.addView(mCameraPreview);
                mCaptureButton.show();
                animateOverlay(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraWorker.onResume();
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
    public void onDestroy() {
        super.onDestroy();
        mCameraWorker.onDestroy();
    }

    @Override
    public void onFocused(Camera camera) {
        mCameraWorker.takePicture(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onStartRecord() {
        mCaptureWrapper.hide();
    }

    @Override
    public void onStopRecord() {
        mCaptureWrapper.show();
    }

    @Override
    public void onCapture() {
        mCameraPreview.takePicture();
    }

    @Override
    public void onChangeCamera() {
        animateOverlay(true);
        mCaptureButton.setEnabled(false);
        mSwitchButton.setEnabled(false);

        mCameraPreviewWrapper.removeAllViews();
        mCameraWorker.changeCamera(new Runnable() {
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

        mCaptureButton.setEnabled(true);
        mSwitchButton.setEnabled(true);
        animateOverlay(false);
    }

    private void animateOverlay(boolean show) {
        mPreviewOverlay.animate()
                .alpha(show ? 1 : 0)
                .setDuration(200)
                .setInterpolator(DECELERATE)
                .start();
    }
}