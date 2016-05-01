package com.rm.cameraphone.constants;

/**
 * Created by alex
 */
public interface CameraConstants {
    double ASPECT_TOLERANCE = 0.1;

    // camera parameter keys
    String KEY_PREVIEW_SIZE = "preview-size";
    String KEY_PREVIEW_FORMAT = "preview-format";
    String KEY_PREVIEW_FRAME_RATE = "preview-frame-rate";
    String KEY_PREVIEW_FPS_RANGE = "preview-fps-range";
    String KEY_PICTURE_SIZE = "picture-size";
    String KEY_PICTURE_FORMAT = "picture-format";
    String KEY_JPEG_THUMBNAIL_SIZE = "jpeg-thumbnail-size";
    String KEY_JPEG_THUMBNAIL_WIDTH = "jpeg-thumbnail-width";
    String KEY_JPEG_THUMBNAIL_HEIGHT = "jpeg-thumbnail-height";
    String KEY_JPEG_THUMBNAIL_QUALITY = "jpeg-thumbnail-quality";
    String KEY_JPEG_QUALITY = "jpeg-quality";
    String KEY_ROTATION = "rotation";
    String KEY_GPS_LATITUDE = "gps-latitude";
    String KEY_GPS_LONGITUDE = "gps-longitude";
    String KEY_GPS_ALTITUDE = "gps-altitude";
    String KEY_GPS_TIMESTAMP = "gps-timestamp";
    String KEY_GPS_PROCESSING_METHOD = "gps-processing-method";
    String KEY_WHITE_BALANCE = "whitebalance";
    String KEY_EFFECT = "effect";
    String KEY_ANTIBANDING = "antibanding";
    String KEY_SCENE_MODE = "scene-mode";
    String KEY_FLASH_MODE = "flash-mode";
    String KEY_FOCUS_MODE = "focus-mode";
    String KEY_FOCUS_AREAS = "focus-areas";
    String KEY_MAX_NUM_FOCUS_AREAS = "max-num-focus-areas";
    String KEY_FOCAL_LENGTH = "focal-length";
    String KEY_HORIZONTAL_VIEW_ANGLE = "horizontal-view-angle";
    String KEY_VERTICAL_VIEW_ANGLE = "vertical-view-angle";
    String KEY_EXPOSURE_COMPENSATION = "exposure-compensation";
    String KEY_MAX_EXPOSURE_COMPENSATION = "max-exposure-compensation";
    String KEY_MIN_EXPOSURE_COMPENSATION = "min-exposure-compensation";
    String KEY_EXPOSURE_COMPENSATION_STEP = "exposure-compensation-step";
    String KEY_AUTO_EXPOSURE_LOCK = "auto-exposure-lock";
    String KEY_AUTO_EXPOSURE_LOCK_SUPPORTED = "auto-exposure-lock-supported";
    String KEY_AUTO_WHITEBALANCE_LOCK = "auto-whitebalance-lock";
    String KEY_AUTO_WHITEBALANCE_LOCK_SUPPORTED = "auto-whitebalance-lock-supported";
    String KEY_METERING_AREAS = "metering-areas";
    String KEY_MAX_NUM_METERING_AREAS = "max-num-metering-areas";
    String KEY_ZOOM = "zoom";
    String KEY_MAX_ZOOM = "max-zoom";
    String KEY_ZOOM_RATIOS = "zoom-ratios";
    String KEY_ZOOM_SUPPORTED = "zoom-supported";
    String KEY_SMOOTH_ZOOM_SUPPORTED = "smooth-zoom-supported";
    String KEY_FOCUS_DISTANCES = "focus-distances";
    String KEY_VIDEO_SIZE = "video-size";
    String KEY_PREFERRED_PREVIEW_SIZE_FOR_VIDEO =
            "preferred-preview-size-for-video";
    String KEY_MAX_NUM_DETECTED_FACES_HW = "max-num-detected-faces-hw";
    String KEY_MAX_NUM_DETECTED_FACES_SW = "max-num-detected-faces-sw";
    String KEY_RECORDING_HINT = "recording-hint";
    String KEY_VIDEO_SNAPSHOT_SUPPORTED = "video-snapshot-supported";
    String KEY_VIDEO_STABILIZATION = "video-stabilization";
    String KEY_VIDEO_STABILIZATION_SUPPORTED = "video-stabilization-supported";
}
