package com.javierjordanluque.healthtrackr.models;

import android.content.Context;

import com.javierjordanluque.healthtrackr.db.repositories.MedicalAppointmentRepository;
import com.javierjordanluque.healthtrackr.db.repositories.NotificationRepository;
import com.javierjordanluque.healthtrackr.util.PermissionManager;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;
import com.javierjordanluque.healthtrackr.util.notifications.MedicalAppointmentNotification;
import com.javierjordanluque.healthtrackr.util.notifications.NotificationScheduler;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class MedicalAppointment implements Identifiable {
    private long id;
    private Treatment treatment;
    private String purpose;
    private ZonedDateTime dateTime;
    private Location location;
    private MedicalAppointmentNotification notification;

    public MedicalAppointment(Context context, Treatment treatment, String purpose, ZonedDateTime dateTime, Location location) throws DBInsertException {
        this.treatment = treatment;
        this.purpose = purpose;
        this.dateTime = dateTime;
        this.location = location;

        if (context != null) {
            this.treatment.addAppointment(context, this);
            scheduleAppointmentNotification(context, NotificationScheduler.PREVIOUS_DEFAULT_MINUTES);
        }
    }

    public void scheduleAppointmentNotification(Context context, int previousMinutes) throws DBInsertException {
        // Check if the notification is valid by ensuring:
        // 1. It's not scheduled to trigger before the margin minutes
        // 2. It's not already past (considering the margin minutes)
        // 3. The app has permission to send notifications
        if (previousMinutes >= NotificationScheduler.MARGIN_MINUTES &&
                dateTime.isAfter(ZonedDateTime.now().plusMinutes(previousMinutes + NotificationScheduler.MARGIN_MINUTES)) &&
                PermissionManager.hasNotificationPermission(context)) {
            long timestamp = dateTime.minusMinutes(previousMinutes).toInstant().toEpochMilli();
            MedicalAppointmentNotification notification = new MedicalAppointmentNotification(this, timestamp);

            NotificationRepository notificationRepository = new NotificationRepository(context);
            notification.setId(notificationRepository.insert(notification));

            NotificationScheduler.scheduleInexactNotification(context, notification);
            this.notification = notification;
        }
    }

    public void removeNotification(Context context, MedicalAppointmentNotification notification) throws DBDeleteException {
        NotificationRepository notificationRepository = new NotificationRepository(context);
        notificationRepository.delete(notification);
        setNotification(null);
    }

    private MedicalAppointment() {
    }

    public void modifyMedicalAppointment(Context context, String purpose, ZonedDateTime dateTime, Location location) throws DBUpdateException, DBFindException,
            DBDeleteException, DBInsertException {
        MedicalAppointment appointment = new MedicalAppointment();
        appointment.setId(this.id);

        if ((this.purpose == null && purpose != null ) || (purpose != null && !this.purpose.equals(purpose))) {
            setPurpose(purpose);
            appointment.setPurpose(this.purpose);
        } else if (this.purpose != null && purpose == null) {
            setPurpose(null);
            appointment.setPurpose("");
        }

        if (!this.dateTime.equals(dateTime)) {
            ZonedDateTime oldDateTime = this.dateTime;

            setDateTime(dateTime);
            appointment.setDateTime(this.dateTime);

            MedicalAppointmentNotification medicalAppointmentNotification = getNotification(context);
            int previousMinutes = (int) ChronoUnit.MINUTES.between(oldDateTime, Instant.ofEpochMilli(medicalAppointmentNotification.getTimestamp())
                    .atZone(oldDateTime.getZone()));
            NotificationScheduler.cancelNotification(context, medicalAppointmentNotification);
            scheduleAppointmentNotification(context, previousMinutes);
        }

        if ((this.location == null && location != null ) || (location != null && !this.location.equals(location))) {
            setLocation(location);
            appointment.setLocation(this.location);
        } else if (this.location != null && location == null) {
            location = new Location(Long.MIN_VALUE, Long.MIN_VALUE);
            setLocation(null);
            appointment.setLocation(location);
        }

        MedicalAppointmentRepository medicalAppointmentRepository = new MedicalAppointmentRepository(context);
        medicalAppointmentRepository.update(appointment);
    }

    public void modifyMedicalAppointmentNotification(Context context, int notificationTimeHours, int notificationTimeMinutes, boolean notificationStatus) throws DBDeleteException, DBFindException, DBInsertException {
        notification = getNotification(context);
        int previousMinutes = notificationTimeHours * 60 + notificationTimeMinutes;
        long timestamp = dateTime.minusMinutes(previousMinutes).toInstant().toEpochMilli();

        if (notification != null && (!notificationStatus || notification.getTimestamp() != timestamp))
            NotificationScheduler.cancelNotification(context, notification);
        if (notificationStatus && (notification == null || notification.getTimestamp() != timestamp))
            scheduleAppointmentNotification(context, previousMinutes);
    }

    public boolean isPending() {
        return dateTime.isAfter(ZonedDateTime.now());
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

    public MedicalAppointmentNotification getNotification(Context context) throws DBFindException {
        if (notification == null) {
            NotificationRepository notificationRepository = new NotificationRepository(context);
            setNotification(notificationRepository.findAppointmentNotification(this.id));
        }

        return notification;
    }

    public void setNotification(MedicalAppointmentNotification notification) {
        this.notification = notification;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        MedicalAppointment medicalAppointment = (MedicalAppointment) obj;
        return id == medicalAppointment.id &&
                Objects.equals(treatment, medicalAppointment.treatment) &&
                Objects.equals(purpose, medicalAppointment.purpose) &&
                Objects.equals(dateTime, medicalAppointment.dateTime) &&
                Objects.equals(location, medicalAppointment.location);
    }
}
