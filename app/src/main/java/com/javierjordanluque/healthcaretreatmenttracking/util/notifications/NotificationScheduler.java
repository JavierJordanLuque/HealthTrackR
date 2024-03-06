package com.javierjordanluque.healthcaretreatmenttracking.util.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.javierjordanluque.healthcaretreatmenttracking.models.Notification;

public class NotificationScheduler {
    public static final int PREVIOUS_DEFAULT_MINUTES = 60;
    public static final int MARGIN_MINUTES = 5;
    private static final int WINDOW_LENGTH_MINUTES = 5;
    protected static final String NOTIFICATION_ID = "notification_id";
    protected static final String MEDICINE_NAME = "medicine_name";
    protected static final String MEDICINE_INITIAL_DOSING_TIME = "medicine_initial_dosing_time";
    protected static final String MEDICINE_DOSAGE_FREQUENCY_HOURS = "medicine_dosage_frequency_hours";
    protected static final String MEDICINE_DOSAGE_FREQUENCY_MINUTES = "medicine_dosage_frequency_minutes";
    protected static final String APPOINTMENT_PURPOSE = "appointment_purpose";
    protected static final String APPOINTMENT_LOCATION_LATITUDE = "appointment_location_latitude";
    protected static final String APPOINTMENT_LOCATION_LONGITUDE = "appointment_location_longitude";
    protected static final String APPOINTMENT_DATE_TIME = "date_time";
    protected static final String TREATMENT_TITLE = "treatment_title";

    public static void scheduleInexactNotification(Context context, Notification notification) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            long windowLengthInMillis = WINDOW_LENGTH_MINUTES * 60 * 1000;
            PendingIntent pendingIntent = buildPendingIntent(context, notification);
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, notification.getTimestamp(), windowLengthInMillis, pendingIntent);
        }
    }

    public static void scheduleInexactRepeatingNotification(Context context, Notification notification) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (notification.getMedicine() != null && notification.getAppointment() == null) {
                long intervalMillis = (notification.getMedicine().getDosageFrequencyHours() * 60 * 60 * 1000) + (notification.getMedicine().getDosageFrequencyMinutes() * 60 * 1000);
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
                scheduleInexactNotification(context, notification);
            }
        }
    }

    private static PendingIntent buildPendingIntent(Context context, Notification notification) {
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.setAction("com.javierjordanluque.healthcaretreatmenttracking.SHOW_NOTIFICATION");

        if (notification.getMedicine() != null && notification.getAppointment() == null) {
            notificationIntent.putExtra(MEDICINE_NAME, notification.getMedicine().getName());
            notificationIntent.putExtra(TREATMENT_TITLE, notification.getAppointment().getTreatment().getTitle());
            notificationIntent.putExtra(MEDICINE_INITIAL_DOSING_TIME, notification.getMedicine().getInitialDosingTime());
            notificationIntent.putExtra(MEDICINE_DOSAGE_FREQUENCY_HOURS, notification.getMedicine().getDosageFrequencyHours());
            notificationIntent.putExtra(MEDICINE_DOSAGE_FREQUENCY_MINUTES, notification.getMedicine().getDosageFrequencyMinutes());
        } else if (notification.getMedicine() == null && notification.getAppointment() != null) {
            notificationIntent.putExtra(APPOINTMENT_PURPOSE, notification.getAppointment().getPurpose());
            notificationIntent.putExtra(TREATMENT_TITLE, notification.getAppointment().getTreatment().getTitle());
            notificationIntent.putExtra(APPOINTMENT_LOCATION_LATITUDE, notification.getAppointment().getLocation().getLatitude());
            notificationIntent.putExtra(APPOINTMENT_LOCATION_LONGITUDE, notification.getAppointment().getLocation().getLongitude());
            notificationIntent.putExtra(APPOINTMENT_DATE_TIME, notification.getAppointment().getDateTime());
        } // @TODO Maybe else throws IllegalArgumentException
        notificationIntent.putExtra(NOTIFICATION_ID, notification.getId());

        return PendingIntent.getBroadcast(context, (int) notification.getId(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void cancelNotification(Context context, int notificationId) {
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
            alarmManager.cancel(pendingIntent);
    }
}
