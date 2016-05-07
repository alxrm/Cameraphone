package com.rm.cameraphone.constants;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by alex
 */
public interface FileContants {

    int OUTPUT_PHOTO = 0;
    int OUTPUT_VIDEO = 1;

    String PREFIX_VIDEO = "VID";
    String PREFIX_PHOTO = "IMG";

    String SUFFIX_PHOTO = "jpg";
    String SUFFIX_VIDEO = "mp4";

    SimpleDateFormat FILE_NAME_DATE_FORMAT =
            new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
}
