package com.javierjordanluque.healthtrackr.models;

import android.content.Context;

import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;

import java.util.Objects;

public class Symptom implements Identifiable {
    private long id;
    private final Treatment treatment;
    private final String description;

    public Symptom(Context context, Treatment treatment, String description) throws DBInsertException {
        this.treatment = treatment;
        this.description = description;

        this.treatment.addSymptom(context, this);
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

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Symptom symptom = (Symptom) obj;
        return id == symptom.id &&
                Objects.equals(treatment, symptom.treatment) &&
                Objects.equals(description, symptom.description);
    }
}
