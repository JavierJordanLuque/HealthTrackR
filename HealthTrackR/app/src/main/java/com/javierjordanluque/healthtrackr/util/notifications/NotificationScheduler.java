package com.javierjordanluque.healthtrackr.util.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.NotificationException;

public class NotificationScheduler {
    public static final int PREVIOUS_DEFAULT_MINUTES = 60;
    public static final int MARGIN_MINUTES = 5;
    private static final int WINDOW_LENGTH_MINUTES = 10;
    protected static final String NOTIFICATION_ID = "notification_id";

    public static void scheduleInexactNotification(Context context, Notification notification) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            long windowLengthInMillis = WINDOW_LENGTH_MINUTES * 60 * 1000;
            PendingIntent pendingIntent = buildPendingIntent(context, notification);

            // Set the alarm timestamp to approximately ring halfway through the window of time set,
            // ensuring the alarm is triggered approximately at the indicated time
            long timestamp = notification.getTimestamp() - NotificationScheduler.WINDOW_LENGTH_MINUTES * 60 * 1000 / 2;

            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, timestamp, windowLengthInMillis, pendingIntent);
        }
    }

    public static void scheduleInexactRepeatingNotification(Context context, Notification notification) throws NotificationException {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            try {
                if (notification instanceof MedicationNotification) {
                    MedicationNotification medicationNotification = (MedicationNotification) notification;

                    long intervalMillis = (medicationNotification.getMedicine().getDosageFrequencyHours() * 60 * 60 * 1000) + (medicationNotification.getMedicine().getDosageFrequencyMinutes() * 60 * 1000);
                    PendingIntent pendingIntent = buildPendingIntent(context, notification);

                    long currentTimeMillis = System.currentTimeMillis();
                    long nextNotificationTimeMillis;

                    if (notification.getTimestamp() <= currentTimeMillis) {
                        long passedIntervals = (currentTimeMillis - notification.getTimestamp()) / intervalMillis;
                        nextNotificationTimeMillis = notification.getTimestamp() + (passedIntervals + 1) * intervalMillis;
                    } else {
                        nextNotificationTimeMillis = notification.getTimestamp();
                    }

                    if (nextNotificationTimeMillis - currentTimeMillis < MARGIN_MINUTES * 60 * 1000)
                        nextNotificationTimeMillis += intervalMillis;

                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, nextNotificationTimeMillis, intervalMillis, pendingIntent);
                } else {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException exception) {
                throw new NotificationException("Failed to set notification (" + notification.getClass().getSimpleName() + ") with id (" + notification.getId() + ")", exception);
            }
        }
    }

    private static PendingIntent buildPendingIntent(Context context, Notification notification) {
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.setAction("com.javierjordanluque.healthtrackr.SHOW_NOTIFICATION");
        notificationIntent.putExtra(NOTIFICATION_ID, notification.getId());

        return PendingIntent.getBroadcast(context, (int) notification.getId(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void cancelNotification(Context context, Notification notification) throws DBFindException, DBDeleteException {
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) notification.getId(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
