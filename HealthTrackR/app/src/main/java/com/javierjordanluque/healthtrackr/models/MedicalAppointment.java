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
import java.util.concurrent.TimeUnit;

public class MedicalAppointment implements Identifiable {
    private long id;
    private Treatment treatment;
    private String subject;
    private ZonedDateTime dateTime;
    private Location location;
    private MedicalAppointmentNotification notification;

    public MedicalAppointment(Context context, Treatment treatment, ZonedDateTime dateTime, String subject, Location location) throws DBInsertException {
        this.treatment = treatment;
        this.dateTime = dateTime;
        this.subject = subject;
        this.location = location;

        if (context != null)
            this.treatment.addAppointment(context, this);
    }

    public void scheduleAppointmentNotification(Context context, int previousMinutes) throws DBInsertException {
        if (dateTime.isAfter(ZonedDateTime.now().plusMinutes(previousMinutes)) &&
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

    public void modifyMedicalAppointment(Context context, ZonedDateTime dateTime, String subject, Location location) throws DBUpdateException, DBFindException,
            DBDeleteException, DBInsertException {
        MedicalAppointment appointment = new MedicalAppointment();
        appointment.setId(this.id);

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

        if ((this.subject == null && subject != null ) || (subject != null && !this.subject.equals(subject))) {
            setSubject(subject);
            appointment.setSubject(this.subject);
        } else if (this.subject != null && subject == null) {
            setSubject(null);
            appointment.setSubject("");
        }

        if ((this.location == null && location != null ) || (location != null && !this.location.equals(location))) {
            setLocation(location);
            appointment.setLocation(this.location);
        } else if (this.location != null && location == null) {
            location = new Location(Long.MIN_VALUE, Long.MIN_VALUE);
            setLocation(null);
            appointment.setLocation(location);
        }

        if (!(appointment.getSubject() == null && appointment.getDateTime() == null && appointment.getLocation() == null)) {
            MedicalAppointmentRepository medicalAppointmentRepository = new MedicalAppointmentRepository(context);
            medicalAppointmentRepository.update(appointment);
        }
    }

    public void modifyMedicalAppointmentNotification(Context context, int notificationTimeHours, int notificationTimeMinutes, boolean notificationStatus) throws DBDeleteException, DBFindException, DBInsertException {
        notification = getNotification(context);
        int previousMinutes = (int) (TimeUnit.HOURS.toMinutes(notificationTimeHours) + notificationTimeMinutes);
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

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public String getSubject() {
        return subject;
    }

    private void setSubject(String subject) {
        this.subject = subject;
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

            notification.setAppointment(this);
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
                Objects.equals(subject, medicalAppointment.subject) &&
                Objects.equals(dateTime, medicalAppointment.dateTime) &&
                Objects.equals(location, medicalAppointment.location);
    }
}
