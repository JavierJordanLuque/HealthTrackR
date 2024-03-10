package com.javierjordanluque.healthcaretreatmenttracking.util.notifications;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.javierjordanluque.healthcaretreatmenttracking.HealthcareTreatmentTrackingApp;
import com.javierjordanluque.healthcaretreatmenttracking.R;
import com.javierjordanluque.healthcaretreatmenttracking.db.repositories.NotificationRepository;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBFindException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.NotificationException;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public class NotificationPublisher extends BroadcastReceiver {
    private final int NOW_MARGIN_MINUTES = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            String notificationId = intent.getStringExtra(NotificationScheduler.NOTIFICATION_ID);

            if (notificationId != null) {
                Notification notification = null;
                NotificationRepository notificationRepository = new NotificationRepository(context);

                try {
                    notification = notificationRepository.findById(Long.parseLong(notificationId));
                } catch (DBFindException exception) {
                    try {
                        throw new NotificationException("Failed to send notification with id (" + notificationId + ")", exception);
                    } catch (NotificationException ignored) {
                    }
                }

                NotificationCompat.Builder builder = null;
                if (notification instanceof MedicationNotification) {
                    MedicationNotification medicationNotification = (MedicationNotification) notification;

                    String medicineName = medicationNotification.getMedicine().getName();
                    String treatmentTitle = medicationNotification.getMedicine().getTreatment().getTitle();
                    ZonedDateTime medicineNextDose = medicationNotification.getMedicine().calculateNextDose();

                    String message;
                    if (!ZonedDateTime.now().isBefore(medicineNextDose.minusMinutes(NOW_MARGIN_MINUTES))) {
                        message = context.getString(R.string.medication_notification_message_dose) + " " + medicineName + " " + context.getString(R.string.medication_notification_message_treatment) + " " + treatmentTitle + " " + context.getString(R.string.medication_notification_message_schedule_now);
                    } else {
                        message = context.getString(R.string.medication_notification_message_dose) + " " + medicineName + " " + context.getString(R.string.medication_notification_message_treatment) + " " + treatmentTitle + " " +
                                context.getString(R.string.medication_notification_message_scheduled_in) + " " + formatTimeDifference(context, Duration.between(ZonedDateTime.now(), medicineNextDose).toMillis());
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
                            .setAutoCancel(true)
                            .setCategory(NotificationCompat.CATEGORY_ALARM);
                            //.setContentIntent(actionPendingIntent);
                } else if (notification instanceof MedicalAppointmentNotification) {
                    MedicalAppointmentNotification appointmentNotification = (MedicalAppointmentNotification) notification;

                    String appointmentPurpose = appointmentNotification.getAppointment().getPurpose();
                    String treatmentTitle = appointmentNotification.getAppointment().getTreatment().getTitle();
                    double appointmentLocationLatitude = appointmentNotification.getAppointment().getLocation().getLatitude();
                    double appointmentLocationLongitude = appointmentNotification.getAppointment().getLocation().getLongitude();
                    ZonedDateTime appointmentDateTime = appointmentNotification.getAppointment().getDateTime();

                    String message;
                    if (!ZonedDateTime.now().isBefore(appointmentDateTime.minusMinutes(NOW_MARGIN_MINUTES))) {
                        message = context.getString(R.string.medical_appointment_notification_message_appointment) + " " + appointmentPurpose + " " + context.getString(R.string.medical_appointment_notification_message_treatment) + " " + treatmentTitle + " " +
                                context.getString(R.string.medical_appointment_notification_scheduled_now) + ". " + context.getString(R.string.medical_appointment_notification_message_latitude) + " " + appointmentLocationLatitude + ", " + context.getString(R.string.medical_appointment_notification_message_longitude) + " " +
                                appointmentLocationLongitude;
                    } else {
                        message = context.getString(R.string.medical_appointment_notification_message_appointment) + " " + appointmentPurpose + " " + context.getString(R.string.medical_appointment_notification_message_treatment) + " " + treatmentTitle + " " +
                                context.getString(R.string.medical_appointment_notification_message_scheduled_in) + " " +
                                formatTimeDifference(context, Duration.between(ZonedDateTime.now(), appointmentDateTime).toMillis()) + ". " +
                                context.getString(R.string.medical_appointment_notification_message_latitude) + " " + appointmentLocationLatitude + ", " + context.getString(R.string.medical_appointment_notification_message_longitude) + " " + appointmentLocationLongitude;
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
                            .setAutoCancel(true)
                            .setCategory(NotificationCompat.CATEGORY_REMINDER);
                            //.setContentIntent(actionPendingIntent);
                }

                if (builder != null) {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(Integer.parseInt(notificationId), builder.build());

                    try {
                        notificationRepository.delete(notification);
                    } catch (DBDeleteException ignored) {
                    } finally {
                        if (notification instanceof MedicationNotification) {
                            try {
                                ((MedicationNotification) notification).getMedicine().getNotifications(context).remove(notification);
                            } catch (DBFindException ignored) {
                            }
                        } else if (notification instanceof MedicalAppointmentNotification) {
                            ((MedicalAppointmentNotification) notification).getAppointment().setNotification(null);
                        }
                    }
                }
            }
        }
    }

    private String formatTimeDifference(Context context, long timeDifferenceMillis) {
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
