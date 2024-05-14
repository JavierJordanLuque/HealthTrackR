package com.javierjordanluque.healthtrackr.util;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

public class PermissionManager {
    public static final int REQUEST_CODE_PERMISSION_POST_NOTIFICATIONS = 101;
    public static final int REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE = 102;
    public static final int REQUEST_CODE_PERMISSION_READ_MEDIA_IMAGES = 103;
    public static final int REQUEST_CODE_PERMISSION_READ_MEDIA_VIDEO = 104;
    public static final int REQUEST_CODE_PERMISSION_READ_MEDIA_VISUAL_USER_SELECTED = 105;

    public static boolean hasNotificationPermission(Context context) {
        boolean permission;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        } else {
            permission = areNotificationsEnabled(context);
        }

        return permission;
    }

    private static boolean areNotificationsEnabled(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager.areNotificationsEnabled();
    }

    public static boolean hasReadMediaImagesPermission(Context context) {
        boolean permission;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            permission = hasReadExternalStoragePermission(context);
        }

        return permission;
    }

    public static boolean hasReadMediaVideoPermission(Context context) {
        boolean permission;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        } else {
            permission = hasReadExternalStoragePermission(context);
        }

        return permission;
    }

    private static boolean hasReadExternalStoragePermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    public static boolean hasReadMediaPartialPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED;
    }
}
