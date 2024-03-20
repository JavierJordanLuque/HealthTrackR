package com.javierjordanluque.healthtrackr.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class PermissionManager {
    public static final int REQUEST_CODE_PERMISSION_POST_NOTIFICATIONS = 101;

    public static boolean hasNotificationPermission(Context context) {
        boolean permission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;

        if (!permission) {
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PermissionManager.REQUEST_CODE_PERMISSION_POST_NOTIFICATIONS);
                // Implement @Override onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) on activity where appointment is created,
                // if permission granted it should call scheduleAppointmentNotification(context, NotificationScheduler.PREVIOUS_DEFAULT_MINUTES), if not don't schedule any notification

                // Implement @Override onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) on activity where appointment notification is modified,
                // if permission granted it should call scheduleAppointmentNotification(context, newPreviousNotificationTime), if not don't schedule any notification

                // Implement @Override onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) on activity where medicine is created,
                // if permission granted it should call scheduleMedicationNotification(context), if not don't schedule any notification

                // Implement @Override onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) on activity where medicine notifications are modified,
                // if permission granted it should call scheduleMedicationNotification(context), if not don't schedule any notification
            }
        }

        return permission;
    }
}
