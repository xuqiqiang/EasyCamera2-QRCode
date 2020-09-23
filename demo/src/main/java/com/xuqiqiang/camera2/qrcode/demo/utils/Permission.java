package com.xuqiqiang.camera2.qrcode.demo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by xuqiqiang on 2020/07/12.
 */
public class Permission {

    public static final int REQUEST_CODE = 5;

    private static final String[] permission = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
    };

    public static boolean checkPermission(Activity activity) {
        if (isPermissionGranted(activity)) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity, permission, REQUEST_CODE);
            return false;
        }
    }

    public static boolean isPermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            for (String s : permission) {
                int checkPermission = ContextCompat.checkSelfPermission(activity, s);
                if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }
}