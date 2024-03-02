package com.javierjordanluque.healthcaretreatmenttracking.models;

import android.content.Context;

public class Symptom implements Identifiable {
    private long id;
    private Treatment treatment;
    private String description;

    public Symptom(Context context, Treatment treatment, String description) {
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
}
