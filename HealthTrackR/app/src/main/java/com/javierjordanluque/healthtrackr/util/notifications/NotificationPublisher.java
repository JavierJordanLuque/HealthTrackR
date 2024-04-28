package com.javierjordanluque.healthtrackr.util.notifications;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.javierjordanluque.healthtrackr.HealthTrackRApp;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.db.repositories.NotificationRepository;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.util.PermissionManager;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.NotificationException;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public class NotificationPublisher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long notificationId = intent.getLongExtra(NotificationScheduler.NOTIFICATION_ID, -1);

        if (notificationId > 0) {
            Notification notification = getNotification(context, notificationId);

            if (notification instanceof MedicationNotification) {
                Treatment treatment = ((MedicationNotification) notification).getMedicine().getTreatment();
                if (!treatment.isFinished()) {
                    medicationPublisher(context, notification);
                } else {
                    try {
                        NotificationScheduler.cancelNotification(context, notification);
                    } catch (DBFindException | DBDeleteException exception) {
                        try {
                            throw new NotificationException("Failed to cancel notification with id (" + notificationId + ") from a finished treatment", exception);
                        } catch (NotificationException ignored) {
                        }
                    }
                }
            } else if (notification instanceof MedicalAppointmentNotification) {
                Treatment treatment = ((MedicalAppointmentNotification) notification).getAppointment().getTreatment();
                if (!treatment.isFinished()) {
                    appointmentPublisher(context, notification);
                } else {
                    try {
                        NotificationScheduler.cancelNotification(context, notification);
                    } catch (DBFindException | DBDeleteException exception) {
                        try {
                            throw new NotificationException("Failed to cancel notification with id (" + notificationId + ") from a finished treatment", exception);
                        } catch (NotificationException ignored) {
                        }
                    }
                }
            }
        }
    }

    private Notification getNotification(Context context, long notificationId) {
        Notification notification = null;
        NotificationRepository notificationRepository = new NotificationRepository(context);

        try {
            notification = notificationRepository.findById(notificationId);
        } catch (DBFindException exception) {
            try {
                throw new NotificationException("Failed to send notification with id (" + notificationId + ")", exception);
            } catch (NotificationException ignored) {
            }
        }
        return notification;
    }

    private void medicationPublisher(Context context, Notification notification) {
        if (PermissionManager.hasNotificationPermission(context) && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            MedicationNotification medicationNotification = (MedicationNotification) notification;

            String medicineName = medicationNotification.getMedicine().getName();
            String treatmentTitle = medicationNotification.getMedicine().getTreatment().getTitle();

            // @TODO
            // Establish a hierarchy by adding the android:parentActivityName in AndroidManifest.xml
            // Replace MyActivity with the activity which shows the medicine's info
            /*
            Intent actionIntent = new Intent(context, MyActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(actionIntent);
            PendingIntent actionPendingIntent = stackBuilder.getPendingIntent(Integer.parseInt(notificationId),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            */

            NotificationCompat.Builder publicNotificationBuilder;
            NotificationCompat.Builder notificationBuilder;
            if (medicationNotification.getTimestamp() != medicationNotification.getMedicine().getInitialDosingTime().toInstant().toEpochMilli()) {
                ZonedDateTime medicineNextDose = medicationNotification.getMedicine().calculateNextDose();
                String formattedTimeDifference = formatTimeDifference(context, Duration.between(ZonedDateTime.now(), medicineNextDose).toMillis());

                String publicMessage = context.getString(R.string.notification_public_message_medication_scheduled_in) + " " + formattedTimeDifference + ".";
                String message = context.getString(R.string.notification_message_medication_dose) + " " + medicineName + " " + context.getString(R.string.notification_message_medication_treatment) + " " + treatmentTitle + " " + context.getString(R.string.notification_message_medication_scheduled_in) + " " +
                        formattedTimeDifference + ".";

                publicNotificationBuilder = new NotificationCompat.Builder(context, HealthTrackRApp.PREVIOUS_MEDICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_medication)
                        .setContentTitle(context.getString(R.string.notification_title_medication))
                        .setContentText(publicMessage)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(publicMessage))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                        //.setContentIntent(actionPendingIntent);

                notificationBuilder = new NotificationCompat.Builder(context, HealthTrackRApp.PREVIOUS_MEDICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_medication)
                        .setContentTitle(context.getString(R.string.notification_title_medication))
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .setPublicVersion(publicNotificationBuilder.build());
                        //.setContentIntent(actionPendingIntent);
            } else {
                String publicMessage = context.getString(R.string.notification_public_message_medication_scheduled_now);
                String message = context.getString(R.string.notification_message_medication_dose) + " " + medicineName + " " + context.getString(R.string.notification_message_medication_treatment) + " " +
                        treatmentTitle + " " + context.getString(R.string.notification_message_medication_schedule_now);

                publicNotificationBuilder = new NotificationCompat.Builder(context, HealthTrackRApp.MEDICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_medication)
                        .setContentTitle(context.getString(R.string.notification_title_medication))
                        .setContentText(publicMessage)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(publicMessage))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                        //.setContentIntent(actionPendingIntent);

                notificationBuilder = new NotificationCompat.Builder(context, HealthTrackRApp.MEDICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_medication)
                        .setContentTitle(context.getString(R.string.notification_title_medication))
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .setPublicVersion(publicNotificationBuilder.build());
                        //.setContentIntent(actionPendingIntent);
            }

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify((int) notification.getId(), notificationBuilder.build());
        } else {
            try {
                NotificationScheduler.cancelNotification(context, notification);
            } catch (DBFindException | DBDeleteException exception) {
                try {
                    throw new NotificationException("Failed to cancel notification with id (" + notification.getId() + ") from a finished treatment", exception);
                } catch (NotificationException ignored) {
                }
            }
        }
    }

    private void appointmentPublisher(Context context, Notification notification) {
        if (PermissionManager.hasNotificationPermission(context) && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            MedicalAppointmentNotification appointmentNotification = (MedicalAppointmentNotification) notification;

            String appointmentPurpose = appointmentNotification.getAppointment().getPurpose();
            String treatmentTitle = appointmentNotification.getAppointment().getTreatment().getTitle();
            double appointmentLocationLatitude = appointmentNotification.getAppointment().getLocation().getLatitude();
            double appointmentLocationLongitude = appointmentNotification.getAppointment().getLocation().getLongitude();
            ZonedDateTime appointmentDateTime = appointmentNotification.getAppointment().getDateTime();

            String publicMessage;
            String message;
            if (!ZonedDateTime.now().isBefore(appointmentDateTime.minusMinutes(1))) {
                publicMessage = context.getString(R.string.notification_public_message_medical_appointment_scheduled_now);
                message = context.getString(R.string.notification_message_medical_appointment_appointment) + " " + appointmentPurpose + " " + context.getString(R.string.notification_message_medical_appointment_treatment) + " " + treatmentTitle + " " +
                        context.getString(R.string.notification_message_medical_appointment_scheduled_now) + " " + context.getString(R.string.notification_message_medical_appointment_latitude) + " " + appointmentLocationLatitude + ", " + context.getString(R.string.notification_message_medical_appointment_longitude) + " " +
                        appointmentLocationLongitude + ".";
            } else {
                String formattedTimeDifference = formatTimeDifference(context, Duration.between(ZonedDateTime.now(), appointmentDateTime).toMillis());

                publicMessage = context.getString(R.string.notification_public_message_medical_appointment_scheduled_in) + " " + formattedTimeDifference + ".";
                message = context.getString(R.string.notification_message_medical_appointment_appointment) + " " + appointmentPurpose + " " + context.getString(R.string.notification_message_medical_appointment_treatment) + " " + treatmentTitle + " " +
                        context.getString(R.string.notification_message_medical_appointment_scheduled_in) + " " + formattedTimeDifference + ". " + context.getString(R.string.notification_message_medical_appointment_latitude) + " " + appointmentLocationLatitude +
                        ", " + context.getString(R.string.notification_message_medical_appointment_longitude) + " " + appointmentLocationLongitude + ".";
            }

            // @TODO
            // Establish a hierarchy by adding the android:parentActivityName in AndroidManifest.xml
            // Replace MyActivity with the activity which shows the appointment's info
            /*
            Intent actionIntent = new Intent(context, MyActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(actionIntent);
            PendingIntent actionPendingIntent = stackBuilder.getPendingIntent(Integer.parseInt(notificationId),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            */

            NotificationCompat.Builder publicNotificationBuilder = new NotificationCompat.Builder(context, HealthTrackRApp.MEDICAL_APPOINTMENT_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_medical_appointment)
                    .setContentTitle(context.getString(R.string.notification_title_medical_appointment))
                    .setContentText(publicMessage)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(publicMessage))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                    //.setContentIntent(actionPendingIntent);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, HealthTrackRApp.MEDICAL_APPOINTMENT_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_medical_appointment)
                    .setContentTitle(context.getString(R.string.notification_title_medical_appointment))
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setPublicVersion(publicNotificationBuilder.build());
                    //.setContentIntent(actionPendingIntent);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify((int) notification.getId(), notificationBuilder.build());
            try {
                appointmentNotification.getAppointment().removeNotification(context, appointmentNotification);
            } catch (DBDeleteException ignored) {
            }
        } else {
            try {
                NotificationScheduler.cancelNotification(context, notification);
            } catch (DBFindException | DBDeleteException exception) {
                try {
                    throw new NotificationException("Failed to cancel notification with id (" + notification.getId() + ") from a finished treatment", exception);
                } catch (NotificationException ignored) {
                }
            }
        }
    }

    private String formatTimeDifference(Context context, long timeDifferenceMillis) {
        long days = TimeUnit.MILLISECONDS.toDays(timeDifferenceMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis) % 60;

        if (TimeUnit.MILLISECONDS.toSeconds(timeDifferenceMillis) % 60 >= 30)
            minutes++;

        if (minutes == 60) {
            minutes = 0;
            hours++;
        }

        if (hours == 24) {
            hours = 0;
            days++;
        }

        String timeDifferenceString = "";
        if (days > 0)
            timeDifferenceString += " " + context.getResources().getQuantityString(R.plurals.notification_message_days, (int) days, days) + " ";
        if (hours > 0)
            timeDifferenceString += " " + context.getResources().getQuantityString(R.plurals.notification_message_hours, (int) hours, hours) + " ";
        if (minutes > 0)
            timeDifferenceString += " " + context.getResources().getQuantityString(R.plurals.notification_message_minutes, (int) minutes, minutes);

        return timeDifferenceString.trim();
    }
}
