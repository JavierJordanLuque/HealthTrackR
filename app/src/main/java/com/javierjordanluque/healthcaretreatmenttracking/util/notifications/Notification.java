package com.javierjordanluque.healthcaretreatmenttracking.util.notifications;

import com.javierjordanluque.healthcaretreatmenttracking.models.Identifiable;

public abstract class Notification implements Identifiable {
    private long id;
    private final long timestamp;

    public Notification(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
