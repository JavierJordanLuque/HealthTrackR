package com.javierjordanluque.healthcaretreatmenttracking;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class HealthcareTreatmentTrackingApp extends Application {
    public static final String MEDICATION_CHANNEL_ID = "medication_channel_id";
    public static final String MEDICAL_APPOINTMENT_CHANNEL_ID = "medical_appointment_channel_id";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel(MEDICATION_CHANNEL_ID, getString(R.string.medication_channel_name),  getString(R.string.medication_channel_description));
        createNotificationChannel(MEDICAL_APPOINTMENT_CHANNEL_ID, getString(R.string.medical_appointment_channel_name), getString(R.string.medical_appointment_channel_description));
    }

    private void createNotificationChannel(String channelId, CharSequence channelName, String channelDescription) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(channelDescription);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
