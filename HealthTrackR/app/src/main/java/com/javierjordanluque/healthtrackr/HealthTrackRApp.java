package com.javierjordanluque.healthtrackr;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.javierjordanluque.healthtrackr.ui.SessionViewModel;
import com.javierjordanluque.healthtrackr.util.Settings;

public class HealthTrackRApp extends Application {
    private SessionViewModel sessionViewModel;
    private static final String MEDICATION_CHANNEL_GROUP_ID = "medication_channel_group";
    private static final String MEDICAL_APPOINTMENT_CHANNEL_GROUP_ID = "medical_appointment_channel_group";
    public static final String PREVIOUS_MEDICATION_CHANNEL_ID = "previous_medication_channel";
    public static final String MEDICATION_CHANNEL_ID = "medication_channel";
    public static final String MEDICAL_APPOINTMENT_CHANNEL_ID = "medical_appointment_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        sessionViewModel = new ViewModelProvider.AndroidViewModelFactory(this).create(SessionViewModel.class);

        createNotificationChannelGroup(MEDICATION_CHANNEL_GROUP_ID, getString(R.string.notification_channel_group_name_medication));
        createNotificationChannelGroup(MEDICAL_APPOINTMENT_CHANNEL_GROUP_ID, getString(R.string.notification_channel_group_name_medical_appointment));

        createNotificationChannel(PREVIOUS_MEDICATION_CHANNEL_ID, getString(R.string.notification_channel_name_previous_medication), NotificationManager.IMPORTANCE_HIGH,
                getString(R.string.notification_channel_description_previous_medication), MEDICATION_CHANNEL_GROUP_ID);
        createNotificationChannel(MEDICATION_CHANNEL_ID, getString(R.string.notification_channel_name_medication), NotificationManager.IMPORTANCE_HIGH,
                getString(R.string.notification_channel_description_medication), MEDICATION_CHANNEL_GROUP_ID);
        createNotificationChannel(MEDICAL_APPOINTMENT_CHANNEL_ID, getString(R.string.notification_channel_name_medical_appointment), NotificationManager.IMPORTANCE_HIGH,
                getString(R.string.notification_channel_description_medical_appointment), MEDICAL_APPOINTMENT_CHANNEL_GROUP_ID);

        setSettings();
    }

    public SessionViewModel getSessionViewModel() {
        return sessionViewModel;
    }

    private void createNotificationChannelGroup(String groupId, CharSequence groupName) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannelGroup(new NotificationChannelGroup(groupId, groupName));
    }

    private void createNotificationChannel(String channelId, CharSequence channelName, int importance, String channelDescription, String groupId) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription(channelDescription);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        channel.setLightColor(R.color.light_primary);

        if (groupId != null)
            channel.setGroup(groupId);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void setSettings() {
        String[] settings = Settings.getSettings(this);
        String theme = settings[0];

        if (theme != null) {
            int appTheme = AppCompatDelegate.getDefaultNightMode();
            if (theme.equals(Settings.LIGHT) && appTheme != AppCompatDelegate.MODE_NIGHT_NO) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (theme.equals(Settings.DARK) && appTheme != AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else if (theme.equals(Settings.SYSTEM_DEFAULT) && appTheme != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        }
    }
}
