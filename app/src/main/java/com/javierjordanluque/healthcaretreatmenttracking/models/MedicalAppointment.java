package com.javierjordanluque.healthcaretreatmenttracking.models;

import android.content.Context;

import com.javierjordanluque.healthcaretreatmenttracking.db.repositories.MedicalAppointmentRepository;
import com.javierjordanluque.healthcaretreatmenttracking.db.repositories.NotificationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MedicalAppointment implements Identifiable {
    private long id;
    private Treatment treatment;
    private String purpose;
    private LocalDate date;
    private LocalTime time;
    private Location location;
    private Notification notification;

    public MedicalAppointment(Context context, Treatment treatment, String purpose, LocalDate date, LocalTime time, Location location) {
        this.treatment = treatment;
        this.purpose = purpose;
        this.date = date;
        this.time = time;
        this.location = location;
        this.treatment.addAppointment(context, this);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(this.date + " " + this.time, formatter);
        long timestamp = dateTime.minusHours(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        notification = new Notification(this, timestamp);
        NotificationRepository notificationRepository = new NotificationRepository(context);
        notification.setId(notificationRepository.insert(notification));
        // @TODO
        // Call NotificationScheduler's scheduleNotification method
    }

    public MedicalAppointment() {
    }

    public void modifyMedicalAppointment(Context context, String purpose, LocalDate date, LocalTime time, Location location) {
        MedicalAppointment appointment = new MedicalAppointment();
        appointment.setId(this.id);

        if ((this.purpose == null && purpose != null ) || (purpose != null && !this.purpose.equals(purpose))) {
            setPurpose(purpose);
            appointment.setPurpose(this.purpose);
        }
        if (!this.date.equals(date)) {
            setDate(date);
            appointment.setDate(this.date);
        }
        if (!this.time.equals(time)) {
            setTime(time);
            appointment.setTime(this.time);
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
        LocalDateTime dateTime = LocalDateTime.of(this.date, this.time);
        LocalDateTime now = LocalDateTime.now();

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

    public LocalDate getDate() {
        return date;
    }

    private void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    private void setTime(LocalTime time) {
        this.time = time;
    }

    public Location getLocation() {
        return location;
    }

    private void setLocation(Location location) {
        this.location = location;
    }
}
