package com.javierjordanluque.healthcaretreatmenttracking.models;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.javierjordanluque.healthcaretreatmenttracking.db.repositories.MedicalAppointmentRepository;
import com.javierjordanluque.healthcaretreatmenttracking.db.repositories.NotificationRepository;
import com.javierjordanluque.healthcaretreatmenttracking.util.PermissionConstants;
import com.javierjordanluque.healthcaretreatmenttracking.util.notifications.NotificationScheduler;

import java.time.ZonedDateTime;

public class MedicalAppointment implements Identifiable {
    private long id;
    private Treatment treatment;
    private String purpose;
    private ZonedDateTime dateTime;
    private Location location;
    private Notification notification;

    public MedicalAppointment(Context context, Treatment treatment, String purpose, ZonedDateTime dateTime, Location location) {
        this.treatment = treatment;
        this.purpose = purpose;
        this.dateTime = dateTime;
        this.location = location;
        this.treatment.addAppointment(context, this);

        if (context != null)
            scheduleAppointmentNotification(context, NotificationScheduler.PREVIOUS_DEFAULT_MINUTES);
    }

    public void scheduleAppointmentNotification(Context context, long previousMinutes) {
        if (dateTime.isAfter(ZonedDateTime.now().plusMinutes(previousMinutes + NotificationScheduler.MARGIN_MINUTES))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (context instanceof Activity) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PermissionConstants.REQUEST_CODE_PERMISSION_POST_NOTIFICATIONS);
                    // Implement @Override onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) on activity where appointment is created,
                    // if permission granted it should call scheduleAppointmentNotification(context), if not don't schedule any notification
                }
            } else {
                long timestamp = dateTime.minusHours(previousMinutes).toInstant().toEpochMilli();
                notification = new Notification(this, timestamp);

                NotificationRepository notificationRepository = new NotificationRepository(context);
                notification.setId(notificationRepository.insert(notification));

                NotificationScheduler.scheduleInexactNotification(context, notification);
            }
        }
    }

    private MedicalAppointment() {
    }

    public void modifyMedicalAppointment(Context context, String purpose, ZonedDateTime dateTime, Location location) {
        MedicalAppointment appointment = new MedicalAppointment();
        appointment.setId(this.id);

        if ((this.purpose == null && purpose != null ) || (purpose != null && !this.purpose.equals(purpose))) {
            setPurpose(purpose);
            appointment.setPurpose(this.purpose);
        }
        if (!this.dateTime.equals(dateTime)) {
            setDateTime(dateTime);
            appointment.setDateTime(this.dateTime);
        }
        if ((this.location == null && location != null ) || (location != null && !this.location.equals(location))) {
            setLocation(location);
            appointment.setLocation(this.location);
        }

        MedicalAppointmentRepository medicalAppointmentRepository = new MedicalAppointmentRepository(context);
        medicalAppointmentRepository.update(appointment);
    }

    public void modifyMedicalAppointmentNotification(Context context, int notificationTimeHours, int notificationTimeMinutes, boolean notificationStatus) {
        // @TODO
        // If notification time is different from current notification timestamp then delete current notification from NOTIFICATION table and insert the new notification
        // In that case, call NotificationScheduler's cancelNotification and then scheduleNotification methods
    }

    public boolean isPending() {
        ZonedDateTime now = ZonedDateTime.now();

        return dateTime.isAfter(now);
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public String getPurpose() {
        return purpose;
    }

    private void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    private void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Location getLocation() {
        return location;
    }

    private void setLocation(Location location) {
        this.location = location;
    }
}
