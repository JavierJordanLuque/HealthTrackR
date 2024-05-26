package com.javierjordanluque.healthtrackr.util.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.javierjordanluque.healthtrackr.models.Medicine;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.NotificationException;

import java.util.concurrent.TimeUnit;

public class NotificationScheduler {
    public static final int PREVIOUS_DEFAULT_MINUTES = 60;
    private static final int WINDOW_LENGTH_MINUTES = 15;
    public static final String NOTIFICATION_ID = "notification_id";

    public static void scheduleInexactNotification(Context context, Notification notification) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            long windowLengthInMillis = TimeUnit.MINUTES.toMillis(WINDOW_LENGTH_MINUTES);
            PendingIntent pendingIntent = buildPendingIntent(context, notification.getId(), true, false);

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
                    PendingIntent pendingIntent = buildPendingIntent(context, notification.getId(), false, false);
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

    public static PendingIntent buildPendingIntent(Context context, long notificationId, boolean flagOneShot, boolean flagNoCreate) {
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.setAction(context.getPackageName() + ".SHOW_NOTIFICATION");
        notificationIntent.putExtra(NOTIFICATION_ID, notificationId);

        int flags = PendingIntent.FLAG_IMMUTABLE;
        if (flagOneShot)
            flags |= PendingIntent.FLAG_ONE_SHOT;
        if (flagNoCreate)
            flags |= PendingIntent.FLAG_NO_CREATE;

        return PendingIntent.getBroadcast(context, (int) notificationId, notificationIntent, flags);
    }

    public static void cancelNotification(Context context, Notification notification) throws DBFindException, DBDeleteException {
        PendingIntent pendingIntent;
        boolean flagOneShot = true;

        if (notification instanceof MedicationNotification) {
            Medicine medicine = ((MedicationNotification) notification).getMedicine();
            flagOneShot = medicine.getDosageFrequencyHours() == 0 && medicine.getDosageFrequencyMinutes() == 0;
        }

        pendingIntent = buildPendingIntent(context, notification.getId(), flagOneShot, true);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null && pendingIntent != null) {
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
