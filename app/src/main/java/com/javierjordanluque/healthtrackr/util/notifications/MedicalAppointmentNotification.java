package com.javierjordanluque.healthtrackr.util.notifications;

import com.javierjordanluque.healthtrackr.models.MedicalAppointment;

public class MedicalAppointmentNotification extends Notification {
    private final MedicalAppointment appointment;

    public MedicalAppointmentNotification(MedicalAppointment appointment, long timestamp) {
        super(timestamp);
        this.appointment = appointment;
    }

    public MedicalAppointment getAppointment() {
        return appointment;
    }
}
