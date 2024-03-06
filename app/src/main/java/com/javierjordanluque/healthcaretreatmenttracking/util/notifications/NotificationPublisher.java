package com.javierjordanluque.healthcaretreatmenttracking.util.notifications;

import android.Manifest;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.javierjordanluque.healthcaretreatmenttracking.HealthcareTreatmentTrackingApp;
import com.javierjordanluque.healthcaretreatmenttracking.R;
import com.javierjordanluque.healthcaretreatmenttracking.models.Medicine;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public class NotificationPublisher extends BroadcastReceiver {
    private final int NOW_MINUTES_MARGIN = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            String notificationId = intent.getStringExtra(NotificationScheduler.NOTIFICATION_ID);
            if (notificationId != null) {
                NotificationCompat.Builder builder = null;
                if (intent.getStringExtra(NotificationScheduler.MEDICINE_NAME) != null && intent.getStringExtra(NotificationScheduler.APPOINTMENT_PURPOSE) == null) {
                    String medicineName = intent.getStringExtra(NotificationScheduler.MEDICINE_NAME);
                    String treatmentTitle = intent.getStringExtra(NotificationScheduler.TREATMENT_TITLE);
                    ZonedDateTime initialDosingTime = (ZonedDateTime) intent.getSerializableExtra(NotificationScheduler.MEDICINE_INITIAL_DOSING_TIME);
                    int dosageFrequencyHours = intent.getIntExtra(NotificationScheduler.MEDICINE_DOSAGE_FREQUENCY_HOURS, -1);
                    int dosageFrequencyMinutes = intent.getIntExtra(NotificationScheduler.MEDICINE_DOSAGE_FREQUENCY_MINUTES, -1);
                    ZonedDateTime medicineNextDose = Medicine.calculateNextDose(initialDosingTime, dosageFrequencyHours, dosageFrequencyMinutes);

                    long timeDifferenceMillis = Duration.between(ZonedDateTime.now(), medicineNextDose).toMillis();
                    long nowMarginMinutesInMillis = NOW_MINUTES_MARGIN * 60 * 1000;
                    String message;
                    if (ZonedDateTime.now().isAfter(medicineNextDose) || timeDifferenceMillis <= nowMarginMinutesInMillis) {
                        message = context.getString(R.string.medication_notification_message_dose) + " " + medicineName + " " + context.getString(R.string.medication_notification_message_treatment) + " " + treatmentTitle + " " + context.getString(R.string.medication_notification_message_schedule_now);
                    } else {
                        message = context.getString(R.string.medication_notification_message_dose) + " " + medicineName + " " + context.getString(R.string.medication_notification_message_treatment) + " " + treatmentTitle + " " + context.getString(R.string.medication_notification_message_scheduled_in) + " " + formatTimeDifference(context, timeDifferenceMillis);
                    }

                    // @TODO
                    // Establish a hierarchy by adding the android:parentActivityName in AndroidManifest.xml
                    // Replace MyActivity with the activity which shows the medicine's info
                    /*Intent actionIntent = new Intent(context, MyActivity.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntentWithParentStack(actionIntent);
                    PendingIntent actionPendingIntent = stackBuilder.getPendingIntent(Integer.parseInt(notificationId),
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);*/

                    builder = new NotificationCompat.Builder(context, HealthcareTreatmentTrackingApp.MEDICATION_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle(context.getString(R.string.medication_notification_title))
                            .setContentText(message)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true);
                            //.setContentIntent(actionPendingIntent);
                } else if (intent.getStringExtra(NotificationScheduler.MEDICINE_NAME) == null && intent.getStringExtra(NotificationScheduler.APPOINTMENT_PURPOSE) != null) {
                    String appointmentPurpose = intent.getStringExtra(NotificationScheduler.APPOINTMENT_PURPOSE);
                    String treatmentTitle = intent.getStringExtra(NotificationScheduler.TREATMENT_TITLE);
                    String appointmentLocationLatitude = intent.getStringExtra(NotificationScheduler.APPOINTMENT_LOCATION_LATITUDE);
                    String appointmentLocationLongitude = intent.getStringExtra(NotificationScheduler.APPOINTMENT_LOCATION_LONGITUDE);
                    ZonedDateTime appointmentDateTime = (ZonedDateTime) intent.getSerializableExtra(NotificationScheduler.APPOINTMENT_DATE_TIME);

                    long timeDifferenceMillis = Duration.between(ZonedDateTime.now(), appointmentDateTime).toMillis();
                    long nowMarginMinutesInMillis = NOW_MINUTES_MARGIN * 60 * 1000;
                    String message;
                    if (ZonedDateTime.now().isAfter(appointmentDateTime) || timeDifferenceMillis <= nowMarginMinutesInMillis) {
                        message = context.getString(R.string.medical_appointment_notification_message_appointment) + " " + appointmentPurpose + " " + context.getString(R.string.medical_appointment_notification_message_treatment) + " " + treatmentTitle + " " + context.getString(R.string.medical_appointment_notification_scheduled_now) + ". " + context.getString(R.string.medical_appointment_notification_message_latitude) + " " + appointmentLocationLatitude + ", " + context.getString(R.string.medical_appointment_notification_message_longitude) + " " + appointmentLocationLongitude;
                    } else {
                        String timeDifferenceString = formatTimeDifference(context, timeDifferenceMillis);
                        message = context.getString(R.string.medical_appointment_notification_message_appointment) + " " + appointmentPurpose + " " + context.getString(R.string.medical_appointment_notification_message_treatment) + " " + treatmentTitle + " " + context.getString(R.string.medical_appointment_notification_message_scheduled_in) + " " + timeDifferenceString + ". " + context.getString(R.string.medical_appointment_notification_message_latitude) + " " + appointmentLocationLatitude + ", " + context.getString(R.string.medical_appointment_notification_message_longitude) + " " + appointmentLocationLongitude;
                    }

                    // @TODO
                    // Establish a hierarchy by adding the android:parentActivityName in AndroidManifest.xml
                    // Replace MyActivity with the activity which shows the appointment's info
                    /*Intent actionIntent = new Intent(context, MyActivity.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntentWithParentStack(actionIntent);
                    PendingIntent actionPendingIntent = stackBuilder.getPendingIntent(Integer.parseInt(notificationId),
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);*/

                    builder = new NotificationCompat.Builder(context, HealthcareTreatmentTrackingApp.MEDICAL_APPOINTMENT_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle(context.getString(R.string.medical_appointment_notification_title))
                            .setContentText(message)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true);
                            //.setContentIntent(actionPendingIntent);
                }

                if (builder != null) {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(Integer.parseInt(notificationId), builder.build());
                }
            }
        }
    }

    private String formatTimeDifference(Context context, long timeDifferenceMillis) {
        if (timeDifferenceMillis <= 0) {
            return "now";
        }

        long days = TimeUnit.MILLISECONDS.toDays(timeDifferenceMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis) % 60;

        String timeDifferenceString = "";
        if (days > 0) {
            timeDifferenceString += days + " " + context.getString(R.string.notification_message_days) + " ";
        }
        if (hours > 0) {
            timeDifferenceString += hours + " " + context.getString(R.string.notification_message_hours) + " ";
        }
        if (minutes > 0) {
            timeDifferenceString += minutes + " " + context.getString(R.string.notification_message_minutes);
        }

        return timeDifferenceString.trim();
    }
}
