package com.rm.cameraphone.events;

import com.rm.cameraphone.components.camera.CameraPreview;

/**
 * Created by alex
 */
public interface OnCameraReadyListener {
    void onCameraReceived(CameraPreview preview);
}