package com.rm.cameraphone.util;

import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.rm.cameraphone.Cameraphone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import static android.os.Environment.DIRECTORY_DCIM;
import static com.rm.cameraphone.constants.FileContants.FILE_NAME_DATE_FORMAT;
import static com.rm.cameraphone.constants.FileContants.OUTPUT_PHOTO;
import static com.rm.cameraphone.constants.FileContants.PREFIX_PHOTO;
import static com.rm.cameraphone.constants.FileContants.PREFIX_VIDEO;
import static com.rm.cameraphone.constants.FileContants.SUFFIX_PHOTO;
import static com.rm.cameraphone.constants.FileContants.SUFFIX_VIDEO;

/**
 * Created by alex
 */
public class FileUtils {

    private static final String TAG = "FileUtils";

    public static File generateOutputFile(int outputType) {
        final File mediaStorageRoot = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM);
        final File mediaStorageDir = new File(mediaStorageRoot, Cameraphone.getAppName());

        final String timeStamp = FILE_NAME_DATE_FORMAT.format(new Date());
        final String namePrefix = outputType == OUTPUT_PHOTO ? PREFIX_PHOTO : PREFIX_VIDEO;
        final String nameSuffix = outputType == OUTPUT_PHOTO ? SUFFIX_PHOTO : SUFFIX_VIDEO;

        final String outputFilename = String.format("%s_%s.%s", namePrefix, timeStamp, nameSuffix);
        final File outputFile = new File(mediaStorageDir.getPath(), outputFilename);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        return outputFile;
    }

    public static void writeFileToDevice(byte[] dataToWrite, File outputPath) {
        if (dataToWrite == null || outputPath == null) return;

        try {
            FileOutputStream fos = new FileOutputStream(outputPath);
            fos.write(dataToWrite);
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void writeBitmapToDevice(Bitmap target, File outputPath) {
        if (target == null || outputPath == null) return;

        try {
            FileOutputStream fos = new FileOutputStream(outputPath);
            target.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void addFileToSystemMedia(File target) {
        MediaScannerConnection.scanFile(
                Cameraphone.getContext(),
                new String[] { target.getAbsolutePath() },
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.v(TAG, "file " + path + (uri == null ? " scan failed" : " was scanned seccessfully"));
                    }
                });
    }

}
