package com.javierjordanluque.healthcaretreatmenttracking.models;

import android.content.Context;

import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DBInsertException;

public class Question implements Identifiable {
    private long id;
    private final Treatment treatment;
    private final String description;

    public Question(Context context, Treatment treatment, String description) throws DBInsertException {
        this.treatment = treatment;
        this.description = description;

        this.treatment.addQuestion(context, this);
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
