package com.javierjordanluque.healthtrackr.util.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.javierjordanluque.healthtrackr.db.repositories.NotificationRepository;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.NotificationException;

import java.util.ArrayList;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            NotificationRepository notificationRepository = new NotificationRepository(context);
            List<Notification> notifications = new ArrayList<>();
            try {
                notifications = notificationRepository.findAll();
            } catch (DBFindException ignored) {
            }

            for (Notification notification : notifications) {
                if (notification instanceof MedicationNotification) {
                    if (((MedicationNotification) notification).getMedicine().getDosageFrequencyHours() != 0 || ((MedicationNotification) notification).getMedicine().getDosageFrequencyMinutes() != 0) {
                        try {
                            NotificationScheduler.scheduleInexactRepeatingNotification(context, notification);
                        } catch (NotificationException ignored) {
                        }
                    } else {
                        NotificationScheduler.scheduleInexactNotification(context, notification);
                    }
                } else if (notification instanceof MedicalAppointmentNotification) {
                    NotificationScheduler.scheduleInexactNotification(context, notification);
                }
            }
        }
    }
}
