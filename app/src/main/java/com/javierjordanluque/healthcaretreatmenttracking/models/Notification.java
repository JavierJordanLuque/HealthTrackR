package com.javierjordanluque.healthcaretreatmenttracking.models;

public class Notification implements Identifiable {
    private long id;
    private Medicine medicine;
    private MedicalAppointment appointment;
    private long timestamp;

    public Notification(Medicine medicine, long timestamp) {
        this.medicine = medicine;
        this.timestamp = timestamp;
    }

    public Notification(MedicalAppointment appointment, long timestamp) {
        this.appointment = appointment;
        this.timestamp = timestamp;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public MedicalAppointment getAppointment() {
        return appointment;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
