package com.javierjordanluque.healthtrackr.util.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.NotificationException;

import java.util.concurrent.TimeUnit;

public class NotificationScheduler {
    public static final int PREVIOUS_DEFAULT_MINUTES = 60;
    private static final int WINDOW_LENGTH_MINUTES = 15;
    protected static final String NOTIFICATION_ID = "notification_id";

    public static void scheduleInexactNotification(Context context, Notification notification) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            long windowLengthInMillis = TimeUnit.MINUTES.toMillis(WINDOW_LENGTH_MINUTES);
            PendingIntent pendingIntent = buildPendingIntent(context, notification);

            // Set the alarm timestamp to approximately ring halfway through the window of time set,
            // ensuring the alarm is triggered approximately at the indicated time
            long timestamp = notification.getTimestamp() - windowLengthInMillis / 2;

            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, timestamp, windowLengthInMillis, pendingIntent);
        }
    }

    public static void scheduleInexactRepeatingNotification(Context context, Notification notification) throws NotificationException {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            try {
                if (notification instanceof MedicationNotification) {
                    MedicationNotification medicationNotification = (MedicationNotification) notification;

                    int dosageFrequencyHours = medicationNotification.getMedicine().getDosageFrequencyHours();
                    int dosageFrequencyMinutes = medicationNotification.getMedicine().getDosageFrequencyMinutes();

                    long intervalMillis = TimeUnit.HOURS.toMillis(dosageFrequencyHours) + TimeUnit.MINUTES.toMillis(dosageFrequencyMinutes);
                    PendingIntent pendingIntent = buildPendingIntent(context, notification);
                    long nextNotificationTimeMillis = getNextNotificationTimeMillis(notification, System.currentTimeMillis(), intervalMillis);

                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, nextNotificationTimeMillis, intervalMillis, pendingIntent);
                } else {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException exception) {
                throw new NotificationException("Failed to set notification (" + notification.getClass().getSimpleName() + ") with id (" + notification.getId() + ")", exception);
            }
        }
    }

    private static long getNextNotificationTimeMillis(Notification notification, long currentTimeMillis, long intervalMillis) {
        long nextNotificationTimeMillis;

        // Set the alarm timestamp to approximately ring halfway through the window of time set,
        // ensuring the alarm is triggered approximately at the indicated time
        long timestamp = notification.getTimestamp() - TimeUnit.MINUTES.toMillis(WINDOW_LENGTH_MINUTES) / 2;
        currentTimeMillis = currentTimeMillis - TimeUnit.MINUTES.toMillis(WINDOW_LENGTH_MINUTES) / 2;

        if (timestamp <= currentTimeMillis) {
            long passedIntervals = (currentTimeMillis - timestamp) / intervalMillis;
            nextNotificationTimeMillis = timestamp + (passedIntervals + 1) * intervalMillis;
        } else {
            nextNotificationTimeMillis = timestamp;
        }

        return nextNotificationTimeMillis;
    }

    private static PendingIntent buildPendingIntent(Context context, Notification notification) {
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.setAction(context.getPackageName() + ".SHOW_NOTIFICATION");
        notificationIntent.putExtra(NOTIFICATION_ID, notification.getId());

        return PendingIntent.getBroadcast(context, (int) notification.getId(), notificationIntent, PendingIntent.FLAG_IMMUTABLE);
    }

    public static void cancelNotification(Context context, Notification notification) throws DBFindException, DBDeleteException {
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) notification.getId(), notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();

            if (notification instanceof MedicationNotification) {
                ((MedicationNotification) notification).getMedicine().removeNotification(context, (MedicationNotification) notification);
            } else if (notification instanceof MedicalAppointmentNotification) {
                ((MedicalAppointmentNotification) notification).getAppointment().removeNotification(context, (MedicalAppointmentNotification) notification);
            }
        }
    }
}
