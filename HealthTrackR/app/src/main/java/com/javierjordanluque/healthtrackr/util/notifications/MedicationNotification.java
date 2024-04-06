package com.javierjordanluque.healthtrackr.util.notifications;

import com.javierjordanluque.healthtrackr.models.Medicine;

public class MedicationNotification extends Notification {
    private final Medicine medicine;

    public MedicationNotification(Medicine medicine, long timestamp) {
        super(timestamp);
        this.medicine = medicine;
    }

    public Medicine getMedicine() {
        return medicine;
    }
}
