package com.rm.cameraphone;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.rm.cameraphone.components.CaptureWrapper;
import com.rm.cameraphone.components.StartView;
import com.rm.cameraphone.components.camera.CameraPreview;
import com.rm.cameraphone.constants.PermissionConstants;
import com.rm.cameraphone.controller.CameraController;
import com.rm.cameraphone.events.OnCameraReadyListener;
import com.rm.cameraphone.events.OnPreviewReadyListener;
import com.rm.cameraphone.util.PermissionsUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity implements OnPreviewReadyListener {

    @InjectView(R.id.camera_waiter) StartView mStartView;
    @InjectView(R.id.camera_capture_wrapper) CaptureWrapper mCaptureWrapper;
    @InjectView(R.id.camera_preview) FrameLayout mCameraPreviewWrapper;

    private CameraController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mController = new CameraController(this);
        mStartView.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mController.onStart(this, null);
        onTryCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mController.onStop();
        mCameraPreviewWrapper.removeAllViews();
        mStartView.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PermissionConstants.INITIAL_REQUEST: {
                if (PermissionsUtil.verifyPermissions(grantResults)) onAddPreview();
            }
        }
    }

    @Override
    public void onPreviewReady() {
        mStartView.hide();
    }

    private void onTryCamera() {
        final boolean hasPermission = PermissionsUtil.checkAll(
                this,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        );

        if (hasPermission) {
            onAddPreview();
        } else {
            PermissionsUtil.requestPermissions(
                    this,
                    PermissionConstants.INITIAL_REQUEST,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            );
        }
    }

    private void onAddPreview() {
        mController.retrieveCameraPreviewDefault(new OnCameraReadyListener() {
            @Override
            public void onCameraReceived(CameraPreview preview) {
                mCameraPreviewWrapper.addView(preview);
            }
        });
    }
}