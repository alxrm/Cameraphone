package com.rm.cameraphone.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by alex
 */
public class PermissionsUtil {

    public static void requestPermissions(@NonNull Activity caller, int callbackKey, String... permissions) {
        if (permissions == null || permissions.length == 0) return;

        final boolean hasGranted = checkAll(caller, permissions);

        if (!hasGranted) {
            ActivityCompat.requestPermissions(caller, permissions, callbackKey);
        }
    }

    public static boolean checkAll(@NonNull Activity caller, @NonNull String... permissions) {
        boolean result = true;

        for (String perm : permissions) {
            result &= ContextCompat.checkSelfPermission(caller, perm) == PackageManager.PERMISSION_GRANTED;
        }

        return result;
    }

    public static boolean verifyPermissions(int[] grantResults) {

        // At least one result must be checked.
        if (grantResults.length < 1 ){
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}
